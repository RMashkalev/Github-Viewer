package com.example.github_viewer

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
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
import java.lang.NullPointerException

class RepositoryListFragment : Fragment() {
    private lateinit var exitButton: ImageView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RepositoryAdapter
    private lateinit var apiService: GitHubApiService
    private lateinit var prevButton: Button
    private lateinit var nextButton: Button
    private lateinit var startButton: Button
    private lateinit var endButton: Button
    private lateinit var navigation: TextView
    private lateinit var originalRepositoryList: MutableList<RepositoryDetails>
    private lateinit var pagingRepositoryList: MutableList<RepositoryDetails>
    private var maxPos: Int = 0
    private var start = 0
    private var end = 0
    private var step = 0
    private var page = 0
    private var maxPage = 0
    private var flag: Boolean = false

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
        prevButton = view.findViewById(R.id.prevButton)
        nextButton = view.findViewById(R.id.nextButton)
        startButton = view.findViewById(R.id.startButton)
        endButton = view.findViewById(R.id.endButton)
        navigation = view.findViewById(R.id.navigation)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        adapter = RepositoryAdapter()
        recyclerView.adapter = adapter

        val searchButton = view.findViewById<ImageView>(R.id.searchButton)
        val searchEditText = view.findViewById<EditText>(R.id.SearchEditText)
        val repositoryListText = view.findViewById<TextView>(R.id.textView)

        searchButton.setOnClickListener {
            if (searchEditText.visibility == View.VISIBLE) {
                val repositoryName = searchEditText.text.toString().trim()
                if (repositoryName.isNotEmpty()) {
                    performSearch(repositoryName)
                } else {
                    Toast.makeText(activity, "Please enter a repository name", Toast.LENGTH_SHORT)
                        .show()
                }
            } else {
                searchEditText.visibility = View.VISIBLE
                repositoryListText.visibility = View.GONE
            }
        }
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                performSearch(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

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
                            originalRepositoryList = repositories.toMutableList()
                            setData()
                            navigationChange()
                            resetRepos()
                            adapter.setRepositories(pagingRepositoryList)
                        } else {
                            Toast.makeText(
                                activity,
                                "Ошибка получения списка репозиториев",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            activity,
                            "Ошибка получения списка репозиториев",
                            Toast.LENGTH_SHORT
                        )
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
        prevButton.setOnClickListener {
            if (start >= step) {
                start -= step
                dataModel.start.value = start
                end -= step
                if (end - start < step) {
                    end += step - (end - start)
                }
                dataModel.end.value = end
                page--
                dataModel.page.value = page
            } else {
                start = 0
                dataModel.start.value = start
                end = step
                dataModel.end.value = end
                page--
                dataModel.page.value = page
            }
            navigationChange()
            resetRepos()
            adapter.setRepositories(pagingRepositoryList)
        }
        nextButton.setOnClickListener {
            if (end < maxPos - step) {
                start += step
                dataModel.start.value = start
                end += step
                dataModel.end.value = end
                page++
                dataModel.page.value = page
            } else if(end != maxPos) {
                start = end
                dataModel.start.value = start
                end = maxPos
                dataModel.end.value = end
                page++
                dataModel.page.value = page
            }
            navigationChange()
            resetRepos()
            adapter.setRepositories(pagingRepositoryList)
        }
        startButton.setOnClickListener {
            start = 0
            dataModel.start.value = start
            end = step
            dataModel.end.value = end
            page = 0
            dataModel.page.value = page
            navigationChange()
            resetRepos()
            adapter.setRepositories(pagingRepositoryList)
        }
        endButton.setOnClickListener {
            start = (maxPos / step) * step
            dataModel.start.value = start
            end = maxPos
            dataModel.end.value = end
            page = maxPos / step
            dataModel.page.value = page
            navigationChange()
            resetRepos()
            adapter.setRepositories(pagingRepositoryList)
        }
    }

    private fun setData() {
        maxPos = originalRepositoryList.size
        dataModel.flag.observe(viewLifecycleOwner) {
            flag = it
        }
        dataModel.step.observe(viewLifecycleOwner) {
            step = it
        }

        if (flag) {
            dataModel.start.observe(viewLifecycleOwner) {
                start = it
            }
            dataModel.end.observe(viewLifecycleOwner) {
                end = it
            }
            dataModel.page.observe(viewLifecycleOwner) {
                page = it
            }
            dataModel.maxPage.observe(viewLifecycleOwner) {
                maxPage = it
            }

        } else {
            end = if (maxPos >= step) {
                step
            } else {
                maxPos
            }
            maxPage = maxPos / step
            dataModel.maxPage.value = maxPage
            dataModel.flag.value = true
        }

    }

    private fun navigationChange() {
        dataModel.page.observe(viewLifecycleOwner) {
            when (it) {
                0 -> {
                    navigation.text = "(0)..$maxPage"
                }
                maxPage -> {
                    navigation.text = "0..($maxPage)"
                }
                else -> {
                    navigation.text = "0..($page)..$maxPage"
                }
            }

        }
    }

    private fun resetRepos() {
        pagingRepositoryList = originalRepositoryList.subList(start, end)

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

    private fun performSearch(repositoryName: String) {
        val filteredList = mutableListOf<RepositoryDetails>()

        if (repositoryName != "") {
            for (repository in originalRepositoryList) {
                if (repository.name.contains(repositoryName, ignoreCase = true)) {
                    filteredList.add(repository)
                }
            }

            adapter.setRepositories(filteredList)
        } else {
            adapter.setRepositories(pagingRepositoryList)
        }

    }


    companion object {
        fun newInstance() = RepositoryListFragment()
    }
}
