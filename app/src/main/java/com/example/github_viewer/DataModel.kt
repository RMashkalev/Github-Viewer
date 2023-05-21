package com.example.github_viewer

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

open class DataModel : ViewModel() {
    val token: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    val fragmentNum: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    val tokenSave: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    val start: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    val end: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    val step: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    val flag: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    val page: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    val maxPage: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }
}