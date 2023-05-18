package com.example.github_viewer

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class RepositoryListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_repository_list)

        val exitButton: Button = findViewById(R.id.button_exit)
        exitButton.setOnClickListener {
            val intent = Intent()
            intent.putExtra("logout", true)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }
}