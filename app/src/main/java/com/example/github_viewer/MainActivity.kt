package com.example.github_viewer

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels

class MainActivity : AppCompatActivity() {
    private val dataModel: DataModel by viewModels()
    private lateinit var token: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        val tokenValue = getAuthToken()
        if (tokenValue == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.mainFrame, StartFragment.newInstance())
                .commit()
        }
        else {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.mainFrame, LoadingFragment.newInstance())
                .commit()
        }
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
                4 -> supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.mainFrame, RepositoryInstanceFragment.newInstance())
                    .commit()
            }
        }
    }

    fun goToRepository(view: View) {
        dataModel.fragmentNum.value = 4
    }
    private fun getAuthToken(): String? {
        val sharedPreferences = getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE)
        return sharedPreferences?.getString("token", null)
    }
    private fun clearAuthToken(context: Context) {
        val sharedPreferences = context.getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove("token")
        editor.apply()
    }

    override fun onStop() {
        super.onStop()

        val tokenSaveValue = dataModel.tokenSave.value
        Log.d("TokenSaveValue", "Value of tokenSave: $tokenSaveValue")
        if (tokenSaveValue == 0) {
            clearAuthToken(this)
        }
    }
}
