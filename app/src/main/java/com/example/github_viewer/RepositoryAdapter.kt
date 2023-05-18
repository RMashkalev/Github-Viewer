package com.example.github_viewer

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.github_viewer.RepositoryDetails

class RepositoryAdapter : RecyclerView.Adapter<RepositoryAdapter.RepositoryViewHolder>() {

    private val repositories: MutableList<RepositoryDetails> = mutableListOf()

    fun setRepositories(list: List<RepositoryDetails>) {
        repositories.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RepositoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_repository, parent, false)
        return RepositoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: RepositoryViewHolder, position: Int) {
        val repository = repositories[position]
        holder.bind(repository)
    }

    override fun getItemCount(): Int {
        return repositories.size
    }

    inner class RepositoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.repositoryNameTextView)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.repositoryDescriptionTextView)
        private val languageTextView: TextView = itemView.findViewById(R.id.repositoryLanguageTextView)

        fun bind(repository: RepositoryDetails) {
            nameTextView.text = repository.name
            descriptionTextView.text = repository.description
            languageTextView.text = repository.language
        }
    }
}