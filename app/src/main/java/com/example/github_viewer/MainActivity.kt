package com.example.github_viewer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels

class MainActivity : AppCompatActivity() {
    private val dataModel: DataModel by viewModels()
    private lateinit var token: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.mainFrame, LoadingFragment.newInstance())
            .commit()

        dataModel.token.observe(this) {
            token = it
        }
        dataModel.fragmentNum.observe(this) {
            when (it) {
                1 -> supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.mainFrame, LoadingFragment.newInstance())
                    .commit()
                2 -> supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.mainFrame, StartFragment.newInstance())
                    .commit()
                3 -> supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.mainFrame, RepositoryListFragment.newInstance())
                    .commit()
            }
        }


    }

}
