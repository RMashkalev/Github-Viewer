package com.example.github_viewer

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class StartFragment : Fragment() {
    private lateinit var tokenEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var apiService: GitHubApiService
    private val YOUR_REQUEST_CODE = 1
    private val dataModel: DataModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_start, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tokenEditText = view.findViewById(R.id.tokenEditText)
        loginButton = view.findViewById(R.id.loginButton)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        apiService = retrofit.create(GitHubApiService::class.java)

        loginButton.setOnClickListener {
            val token = tokenEditText.text.toString().trim()
            if (token.isNotEmpty()) {
                authenticateWithGitHub(token)
            } else {
                Toast.makeText(activity, "Введите токен", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun authenticateWithGitHub(token: String) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    apiService.getUserRepositories("token $token").execute()
                }
                if (response.isSuccessful) {
                    val user = response.body()
                    if (user != null) {
                        saveAuthToken(token)
                        dataModel.token.value = token
                        dataModel.fragmentNum.value = 1
                    } else {
                        Toast.makeText(
                            activity,
                            "Ошибка получения данных пользователя",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(activity, "Ошибка аутентификации", Toast.LENGTH_SHORT)
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
    private fun saveAuthToken(token: String) {
        val sharedPreferences = activity?.getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences?.edit()
        editor?.putString("token", token)
        editor?.apply()
    }
    companion object {
        fun newInstance() = StartFragment()
    }
}