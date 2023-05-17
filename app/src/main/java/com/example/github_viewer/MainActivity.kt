package com.example.github_viewer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {
    private lateinit var tokenEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var apiService: GitHubApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
        val call = apiService.getUserRepositories("token $token")
        call.enqueue(object : Callback<List<RepositoryDetails>> {
            override fun onResponse(call: Call<List<RepositoryDetails>>, response: Response<List<RepositoryDetails>>) {
                if (response.isSuccessful) {
                    val user = response.body()
                    if (user != null) {
                        val intent = Intent(this@MainActivity, RepositoryListActivity::class.java)
                        intent.putExtra("token", token)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this@MainActivity, "Ошибка получения данных пользователя", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Ошибка аутентификации", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<RepositoryDetails>>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Ошибка при выполнении запроса", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
