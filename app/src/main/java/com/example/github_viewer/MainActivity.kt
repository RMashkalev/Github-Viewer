package com.example.github_viewer

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {
    private lateinit var tokenEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var apiService: GitHubApiService
    private val YOUR_REQUEST_CODE = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val savedToken = getAuthToken()
        if (savedToken != null) {
            authenticateWithGitHub(savedToken)
        }

        tokenEditText = findViewById(R.id.tokenEditText)
        loginButton = findViewById(R.id.loginButton)

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
                Toast.makeText(this, "Введите токен", Toast.LENGTH_SHORT).show()
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
                        val intent = Intent(this@MainActivity, RepositoryListActivity::class.java)
                        intent.putExtra("token", token)
                        startActivityForResult(intent, YOUR_REQUEST_CODE)
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            "Ошибка получения данных пользователя",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Ошибка аутентификации", Toast.LENGTH_SHORT)
                        .show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@MainActivity,
                    "Ошибка при выполнении запроса",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == YOUR_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val logout = data?.getBooleanExtra("logout", false) ?: false
            if (logout) {
                clearAuthToken()
                Toast.makeText(this@MainActivity, "Выход из аккаунта", Toast.LENGTH_SHORT).show()
            }
        }
    }
        private fun saveAuthToken(token: String) {
            val sharedPreferences = getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString("token", token)
            editor.apply()
        }

        private fun getAuthToken(): String? {
            val sharedPreferences = getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE)
            return sharedPreferences.getString("token", null)
        }

        private fun clearAuthToken() {
            val sharedPreferences = getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.remove("token")
            editor.apply()
        }
    }
