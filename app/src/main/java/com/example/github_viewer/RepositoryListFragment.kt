package com.example.github_viewer

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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
    private lateinit var prevButton: Button
    private lateinit var nextButton: Button
    private lateinit var startButton: Button
    private lateinit var endButton: Button
    private lateinit var allButton: RadioButton
    private lateinit var favouriteButton: RadioButton
    private lateinit var navigation: TextView
    private lateinit var originalRepositoryList: MutableList<RepositoryDetails>
    private lateinit var pagingRepositoryList: MutableList<RepositoryDetails>
    private var favoriteRepositoryList: MutableList<RepositoryDetails> = mutableListOf()
    private var filteredList: MutableList<RepositoryDetails> = mutableListOf()
    private var maxPos: Int = 0
    private var start = 0
    private var end = 0
    private var step = 0
    private var page = 0
    private var maxPage = 0
    private var flag: Boolean = false
    private var searchFlag: Boolean = false
    private val dataModel: DataModel by activityViewModels()

    private val sharedPrefName = "MySharedPref"
    private val favoriteRepoKey = "FavoriteRepoList"


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_repos_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        favouriteButton = view.findViewById(R.id.favoriteButton)
        exitButton = view.findViewById(R.id.button_exit)
        recyclerView = view.findViewById(R.id.recyclerView)
        prevButton = view.findViewById(R.id.prevButton)
        nextButton = view.findViewById(R.id.nextButton)
        startButton = view.findViewById(R.id.startButton)
        endButton = view.findViewById(R.id.endButton)
        navigation = view.findViewById(R.id.navigation)
        allButton = view.findViewById(R.id.allButton)
        favouriteButton = view.findViewById(R.id.favoriteButton)

        recyclerView.layoutManager = LinearLayoutManager(activity)
        adapter = RepositoryAdapter()
        recyclerView.adapter = adapter

        val searchButton = view.findViewById<ImageView>(R.id.searchButton)
        val searchEditText = view.findViewById<EditText>(R.id.SearchEditText)
        val repositoryListText = view.findViewById<TextView>(R.id.textView)

        loadFavoriteRepositories()

        savedInstanceState?.let { bundle ->
            val savedFavoriteList =
                bundle.getParcelableArrayList<RepositoryDetails>("favoriteRepositoryList")
            savedFavoriteList?.let {
                favoriteRepositoryList = it.toMutableList()
            }
        }

        adapter.setOnItemClickListener(object : RepositoryAdapter.OnItemClickListener {
            override fun onHeartClick(repository: RepositoryDetails) {
                if (favoriteRepositoryList.contains(repository)) {
                    favoriteRepositoryList.remove(repository)
                } else {
                    favoriteRepositoryList.add(repository)
                }
                adapter.notifyDataSetChanged()
                saveFavoriteRepositories()
            }
        })
        allButton.setOnClickListener {
            updateRepositoryList()
            for (pagingRepository in pagingRepositoryList) {
                for (favoriteRepository in favoriteRepositoryList) {
                    if (pagingRepository.name == favoriteRepository.name) {
                        pagingRepository.isFavorite = favoriteRepository.isFavorite
                    }
                    if (pagingRepository.name != favoriteRepository.name) {
                        pagingRepository.isFavorite = false
                    }
                }
            }
        }

        favouriteButton.setOnClickListener {
            updateRepositoryList()
        }

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
                if (s.toString().isEmpty()) {
                    searchFlag = false
                    if (allButton.isChecked) {
                        setDataOriginal()
                        resetRepos(originalRepositoryList)
                    } else {
                        setDataFavorite()
                        resetRepos(favoriteRepositoryList)
                    }
                }
                navigationChange()
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
                            for (originalRepository in originalRepositoryList) {
                                for (favoriteRepository in favoriteRepositoryList) {
                                    if (originalRepository.name == favoriteRepository.name) {
                                        originalRepository.isFavorite =
                                            favoriteRepository.isFavorite
                                    }
                                }
                            }
                            setDataOriginal()
                            navigationChange()
                            resetRepos(originalRepositoryList)
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
        favouriteButton.setOnClickListener {
            setDataFavorite()
            navigationChange()
            resetRepos(favoriteRepositoryList)
            adapter.setRepositories(pagingRepositoryList)
        }
        allButton.setOnClickListener {
            setDataOriginal()
            navigationChange()
            resetRepos(originalRepositoryList)
            adapter.setRepositories(pagingRepositoryList)
        }
        prevButton.setOnClickListener {
            if (start >= step) {
                start -= step
                end -= step
                if (end - start < step) {
                    end += step - (end - start)
                }
                page--
            }

            if (searchFlag) {
                resetRepos(filteredList)
            } else if (allButton.isChecked) {
                resetRepos(originalRepositoryList)
                saveDataOriginal()
            } else {
                resetRepos(favoriteRepositoryList)
                saveDataFavorite()
            }
            navigationChange()
            adapter.setRepositories(pagingRepositoryList)
        }
        nextButton.setOnClickListener {
            if (end < maxPos - step) {
                start += step
                end += step
                page++
            } else if (end != maxPos) {
                start = end
                end = maxPos
                page++
            }

            if (searchFlag) {
                resetRepos(filteredList)
            } else if (allButton.isChecked) {
                resetRepos(originalRepositoryList)
                saveDataOriginal()
            } else {
                resetRepos(favoriteRepositoryList)
                saveDataFavorite()
            }
            navigationChange()
            adapter.setRepositories(pagingRepositoryList)
        }
        startButton.setOnClickListener {
            start = 0
            end = if (step < maxPos) {
                step
            } else {
                maxPos
            }
            page = 0

            if (searchFlag) {
                resetRepos(filteredList)
            } else if (allButton.isChecked) {
                resetRepos(originalRepositoryList)
                saveDataOriginal()
            } else {
                resetRepos(favoriteRepositoryList)
                saveDataFavorite()
            }
            navigationChange()
            adapter.setRepositories(pagingRepositoryList)
        }
        endButton.setOnClickListener {
            start = maxPage * step
            end = maxPos
            page = maxPage

            if (searchFlag) {
                resetRepos(filteredList)
            } else if (allButton.isChecked) {
                resetRepos(originalRepositoryList)
                saveDataOriginal()
            } else {
                resetRepos(favoriteRepositoryList)
                saveDataFavorite()
            }
            navigationChange()
            adapter.setRepositories(pagingRepositoryList)
        }
    }

    private fun setDataOriginal() {
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
            page = 0
            maxPage = (maxPos - 1) / step
            dataModel.flag.value = true
        }
        saveDataOriginal()
    }

    private fun setDataFavorite() {
        maxPos = favoriteRepositoryList.size
        dataModel.favFlag.observe(viewLifecycleOwner) {
            flag = it
        }
        dataModel.step.observe(viewLifecycleOwner) {
            step = it
        }

        maxPos = favoriteRepositoryList.size
        start = 0
        end = if (maxPos >= step) {
            step
        } else {
            maxPos
        }
        page = 0
        maxPage = (maxPos - 1) / step
        dataModel.favFlag.value = true
        saveDataFavorite()
    }


    private fun saveDataOriginal() {
        dataModel.start.value = start
        dataModel.end.value = end
        dataModel.page.value = page
        dataModel.maxPage.value = maxPage
    }

    private fun saveDataFavorite() {
        dataModel.favStart.value = start
        dataModel.favEnd.value = end
        dataModel.favPage.value = page
        dataModel.favMaxPage.value = maxPage
    }

    private fun navigationChange() {
        var page: Int = 0
        var maxPage: Int = 0
        if (searchFlag) {
            page = this.page
            maxPage = this.maxPage
        } else if (allButton.isChecked) {
                dataModel.page.observe(viewLifecycleOwner) {
                    page = it
                }
                dataModel.maxPage.observe(viewLifecycleOwner) {
                    maxPage = it
                }
            } else {
                dataModel.favPage.observe(viewLifecycleOwner) {
                    page = it
                }
                dataModel.favMaxPage.observe(viewLifecycleOwner) {
                    maxPage = it
                }
            }

        when (maxPage) {
            0 -> {
                navigation.text = "(1)"
            }

            1 -> {
                when (page) {
                    0 -> {
                        navigation.text = "(1) 2"
                    }

                    1 -> {
                        navigation.text = "1 (2)"
                    }
                }
            }

            2 -> {
                when (page) {
                    0 -> {
                        navigation.text = "(1) 2 3"
                    }

                    1 -> {
                        navigation.text = "1 (2) 3"
                    }

                    2 -> {
                        navigation.text = "1 2 (3)"
                    }
                }
            }

            3 -> {
                when (page) {
                    0 -> {
                        navigation.text = "(1) 2 3 4"
                    }

                    1 -> {
                        navigation.text = "1 (2) 3 4"
                    }

                    2 -> {
                        navigation.text = "1 2 (3) 4"
                    }

                    3 -> {
                        navigation.text = "1 2 3 (4)"
                    }
                }
            }

            else -> {
                when (page) {
                    0 -> {
                        navigation.text = "(1) 2 .. ${maxPage + 1}"
                    }

                    1 -> {
                        navigation.text = "1 (2) 3 .. ${maxPage + 1}"
                    }

                    maxPage - 1 -> {
                        navigation.text = "1 .. ${maxPage - 1} ($maxPage) ${maxPage + 1}"
                    }

                    maxPage -> {
                        navigation.text = "1 .. $maxPage (${maxPage + 1})"
                    }

                    else -> {
                        navigation.text = "1 .. $page (${page + 1}) ${page + 2} ..${maxPage + 1}"
                    }
                }
            }
        }
    }

    private fun saveFavoriteRepositories() {
        val sharedPreferences = activity?.getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE)
        val editor = sharedPreferences?.edit()
        val gson = Gson()
        val json = gson.toJson(favoriteRepositoryList)
        editor?.putString(favoriteRepoKey, json)
        editor?.apply()
    }

    private fun loadFavoriteRepositories() {
        val sharedPreferences = activity?.getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences?.getString(favoriteRepoKey, null)
        val type = object : TypeToken<List<RepositoryDetails>>() {}.type
        favoriteRepositoryList = gson.fromJson(json, type) ?: mutableListOf()
    }

    private fun updateRepositoryList() {
        val selectedList = if (favouriteButton.isChecked) {
            favoriteRepositoryList
        } else {
            pagingRepositoryList
        }
        adapter.setRepositories(selectedList)
    }

    private fun resetRepos(list: MutableList<RepositoryDetails>) {
        pagingRepositoryList = list.subList(start, end)
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
        filteredList.clear()
        if (allButton.isChecked) {
            if (repositoryName.isNotBlank()) {
                for (repository in originalRepositoryList) {
                    if (repository.name.contains(repositoryName, ignoreCase = true)) {
                        filteredList.add(repository)
                    }
                }
            } else {
                filteredList.addAll(originalRepositoryList)
            }
            maxPos = filteredList.size
            start = 0
            end = if (maxPos >= step) {
                step
            } else {
                maxPos
            }
            page = 0
            maxPage = (maxPos - 1) / step
            searchFlag = true
            resetRepos(filteredList)

        } else if (favouriteButton.isChecked) {
            if (repositoryName.isNotBlank()) {
                for (repository in favoriteRepositoryList) {
                    if (repository.name.contains(repositoryName, ignoreCase = true)) {
                        filteredList.add(repository)
                    }
                }
            } else {
                filteredList.addAll(favoriteRepositoryList)
            }
            maxPos = filteredList.size
            start = 0
            end = if (maxPos >= step) {
                step
            } else {
                maxPos
            }
            page = 0
            maxPage = (maxPos - 1) / step
            searchFlag = true
            resetRepos(filteredList)
        }

        adapter.setRepositories(pagingRepositoryList)
    }


    companion object {
        fun newInstance() = RepositoryListFragment()
    }
}
