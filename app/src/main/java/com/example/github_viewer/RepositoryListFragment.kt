package com.example.github_viewer

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RepositoryListFragment : Fragment() {
    private lateinit var exitButton: ImageView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RepositoryAdapter
    private lateinit var apiService: GitHubApiService
    private val dataModel: DataModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_repos_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        exitButton = view.findViewById(R.id.button_exit)
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        adapter = RepositoryAdapter()
        recyclerView.adapter = adapter

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        apiService = retrofit.create(GitHubApiService::class.java)

        exitButton.setOnClickListener {
            clearAuthToken()
            Toast.makeText(activity, "Выход из аккаунта", Toast.LENGTH_SHORT).show()
            dataModel.fragmentNum.value = 1
        }

        val authToken = getAuthToken()

        if (authToken != null) {
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val response = withContext(Dispatchers.IO) {
                        apiService.getUserRepositories("token $authToken").execute()
                    }
                    if (response.isSuccessful) {
                        val repositories = response.body()
                        if (repositories != null) {
                            adapter.setRepositories(repositories)
                        } else {
                            Toast.makeText(
                                activity,
                                "Ошибка получения списка репозиториев",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(activity, "Ошибка получения списка репозиториев", Toast.LENGTH_SHORT)
                            .show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(
                        activity,
                        "Ошибка при выполнении запроса",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun clearAuthToken() {
        val sharedPreferences = activity?.getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences?.edit()
        editor?.remove("token")
        editor?.apply()
    }
    private fun getAuthToken(): String? {
        val sharedPreferences = activity?.getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE)
        return sharedPreferences?.getString("token", null)
    }

    companion object {
        fun newInstance() = RepositoryListFragment()
    }
}
