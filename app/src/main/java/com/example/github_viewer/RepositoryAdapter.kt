package com.example.github_viewer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView

class RepositoryAdapter : RecyclerView.Adapter<RepositoryAdapter.RepositoryViewHolder>() {
    private val repositories: MutableList<RepositoryDetails> = mutableListOf()
    private var onItemClickListener: OnItemClickListener? = null
    fun setRepositories(list: List<RepositoryDetails>) {
        repositories.clear()
        repositories.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RepositoryViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_repository, parent, false)
        return RepositoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: RepositoryViewHolder, position: Int) {
        val repository = repositories[position]
        holder.bind(repository)
        holder.itemView.setOnClickListener {
            val repositoryInstanceFragment = RepositoryInstanceFragment.newInstance(repository)

            val fragmentManager =
                (holder.itemView.context as AppCompatActivity).supportFragmentManager
            fragmentManager.beginTransaction()
                .replace(R.id.mainFrame, repositoryInstanceFragment)
                .commit()
        }
    }

    override fun getItemCount(): Int {
        return repositories.size
    }

    inner class RepositoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.repositoryNameTextView)
        private val descriptionTextView: TextView =
            itemView.findViewById(R.id.repositoryDescriptionTextView)
        private val languageTextView: TextView =
            itemView.findViewById(R.id.repositoryLanguageTextView)
        private val heartImageView: ImageView = itemView.findViewById(R.id.HeartImageView)

        init {
            heartImageView.setOnClickListener {
                onItemClickListener?.onHeartClick(repositories[adapterPosition])
            }
        }
        fun bind(repository: RepositoryDetails) {
            nameTextView.text = repository.name
            descriptionTextView.text = repository.description
            languageTextView.text = repository.language
        }
    }
    interface OnItemClickListener {
        fun onHeartClick(repository: RepositoryDetails)
    }
    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }
}
