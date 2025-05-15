package by.bsu.newsly.ui.mainActivity.home.recommendations

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import by.bsu.newsly.R
import by.bsu.newsly.data.local.db.ArticlesDatabase
import by.bsu.newsly.data.remote.api.ApiService
import by.bsu.newsly.domain.repository.ArticleRepository
import by.bsu.newsly.domain.repository.BookmarkRepository
import by.bsu.newsly.domain.repository.HistoryRepository
import by.bsu.newsly.domain.repository.RecommendationRepository
import by.bsu.newsly.ui.mainActivity.home.all_articles.AllArticlesViewModel
import by.bsu.newsly.ui.mainActivity.home.all_articles.AllArticlesViewModelFactory
import by.bsu.newsly.ui.mainActivity.home.favorite_articles.FavoriteArticlesViewModel
import by.bsu.newsly.ui.mainActivity.home.favorite_articles.FavoriteArticlesViewModelFactory

class RecommendationsFragment : Fragment(R.layout.fragment_recommendations) {

    lateinit var vm: RecommendationViewModel
    private val allVm: AllArticlesViewModel by activityViewModels {
        val db = Room.databaseBuilder(requireContext(), ArticlesDatabase::class.java, "articles_db")
            .build()
        AllArticlesViewModelFactory(
            ArticleRepository(db.articlesDao(), ApiService.create()),
            BookmarkRepository(db.bookmarkDao())
        )
    }
    private val favVm: FavoriteArticlesViewModel by activityViewModels {
        val db = Room.databaseBuilder(requireContext(), ArticlesDatabase::class.java, "articles_db")
            .build()
        FavoriteArticlesViewModelFactory(
            ArticleRepository(db.articlesDao(), ApiService.create()),
            BookmarkRepository(db.bookmarkDao())
        )
    }

    private lateinit var adapter: RecommendationsAdapter
    private lateinit var swipe: SwipeRefreshLayout
    private lateinit var progress: ProgressBar
    private lateinit var tvInfo: TextView
    private lateinit var rv: RecyclerView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val db = Room.databaseBuilder(requireContext(), ArticlesDatabase::class.java, "articles_db")
            .build()
        val articleRepo = ArticleRepository(db.articlesDao(), ApiService.create())
        val historyRepo = HistoryRepository(db.historyDao())
        val recRepo = RecommendationRepository(articleRepo, historyRepo)

        vm = ViewModelProvider(
            requireActivity(),
            RecommendationViewModelFactory(recRepo, articleRepo)
        )
            .get(RecommendationViewModel::class.java)

        swipe = view.findViewById(R.id.swipeRecs)
        progress = view.findViewById(R.id.progressBarRecs)
        tvInfo = view.findViewById(R.id.tvInfoRecs)
        rv = view.findViewById(R.id.recyclerViewRecommendations)

        adapter = RecommendationsAdapter(emptyList(), this)
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        swipe.setOnRefreshListener { vm.load() }

        vm.loading.observe(viewLifecycleOwner, Observer { loading ->
            progress.visibility = if (loading) View.VISIBLE else View.GONE
            if (!loading) swipe.isRefreshing = false
        })

        vm.recommendations.observe(viewLifecycleOwner, Observer { list ->
            if (list.isNullOrEmpty()) {
                tvInfo.visibility = View.VISIBLE
                rv.visibility = View.GONE
            } else {
                tvInfo.visibility = View.GONE
                rv.visibility = View.VISIBLE
                adapter.update(list)
            }
        })

        vm.error.observe(viewLifecycleOwner, Observer { msg ->
            msg?.let {
                tvInfo.text = it
                tvInfo.visibility = View.VISIBLE
                rv.visibility = View.GONE
            }
        })

        vm.favoriteChanged.observe(viewLifecycleOwner, Observer {
            allVm.loadAllArticles()
            favVm.loadFavorites()
        })
    }

    companion object {
        fun newInstance() = RecommendationsFragment()
    }
}