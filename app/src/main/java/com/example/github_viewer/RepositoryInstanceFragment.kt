package com.example.github_viewer

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class RepositoryInstanceFragment : Fragment() {
    private var repository: RepositoryDetails? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            repository = it.getParcelable("repository")
        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_reposity_instance, container, false)
        repository?.let { repo ->
            view.findViewById<TextView>(R.id.NameTextView).text = repo.name
            view.findViewById<TextView>(R.id.DescriptionTextView).text = repo.description
            view.findViewById<TextView>(R.id.HtmlTextView).text = repo.html_url
            view.findViewById<TextView>(R.id.LicenseTextView).text = repo.license?.name.toString()
            view.findViewById<TextView>(R.id.StarsTextView).text = repo.stars.toString()
            view.findViewById<TextView>(R.id.ForksTextView).text = repo.forks.toString()
            view.findViewById<TextView>(R.id.WatchersTextView).text = repo.watchers.toString()
        }


        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    companion object {
        fun newInstance(repository: RepositoryDetails): RepositoryInstanceFragment {
            val fragment = RepositoryInstanceFragment()
            val bundle = Bundle()
            bundle.putParcelable("repository", repository)
            fragment.arguments = bundle
            return fragment
        }
    }
}