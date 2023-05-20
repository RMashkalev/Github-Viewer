package com.example.github_viewer

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header

interface GitHubApiService {
    @GET("user/repos")
    fun getUserRepositories(@Header("Authorization") token: String): Call<List<RepositoryDetails>>
}