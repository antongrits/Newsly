package by.bsu.newsly.ui.mainActivity.home.favorite_articles

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import by.bsu.newsly.R
import by.bsu.newsly.data.local.db.ArticlesDatabase
import by.bsu.newsly.data.remote.api.ApiService
import by.bsu.newsly.domain.model.Article
import by.bsu.newsly.domain.repository.ArticleRepository
import by.bsu.newsly.domain.repository.BookmarkRepository
import by.bsu.newsly.domain.repository.HistoryRepository
import by.bsu.newsly.domain.repository.RecommendationRepository
import by.bsu.newsly.ui.mainActivity.home.all_articles.AllArticlesViewModel
import by.bsu.newsly.ui.mainActivity.home.all_articles.AllArticlesViewModelFactory
import by.bsu.newsly.ui.mainActivity.home.recommendations.RecommendationViewModel
import by.bsu.newsly.ui.mainActivity.home.recommendations.RecommendationViewModelFactory
import com.google.android.material.snackbar.Snackbar

class FavoriteArticlesFragment : Fragment() {
    private lateinit var vm: FavoriteArticlesViewModel
    private lateinit var allVm: AllArticlesViewModel
    private lateinit var recVM: RecommendationViewModel
    private lateinit var rv: RecyclerView
    private lateinit var tvInfo: TextView
    private lateinit var adapter: FavoriteArticlesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val db = Room.databaseBuilder(requireContext(), ArticlesDatabase::class.java, "articles_db")
            .build()
        val repo = ArticleRepository(db.articlesDao(), ApiService.create())
        val repoBookmark = BookmarkRepository(db.bookmarkDao())
        val repoHistory = HistoryRepository(db.historyDao())
        val repoRecommendation = RecommendationRepository(repo, repoHistory)
        vm = ViewModelProvider(
            requireActivity(),
            FavoriteArticlesViewModelFactory(repo, repoBookmark)
        )[FavoriteArticlesViewModel::class.java]
        allVm = ViewModelProvider(
            requireActivity(),
            AllArticlesViewModelFactory(repo, repoBookmark)
        )[AllArticlesViewModel::class.java]
        recVM = ViewModelProvider(
            requireActivity(),
            RecommendationViewModelFactory(repoRecommendation, repo)
        )[RecommendationViewModel::class.java]
        return inflater.inflate(R.layout.fragment_favorite_articles, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        rv = view.findViewById(R.id.recyclerViewFavoriteArticles)
        tvInfo = view.findViewById(R.id.tvInfo)
        adapter = FavoriteArticlesAdapter(emptyList(), this)
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        vm.favorites.observe(viewLifecycleOwner) { list ->
            if (list.isEmpty()) {
                rv.visibility = View.GONE
                tvInfo.visibility = View.VISIBLE
            } else {
                rv.visibility = View.VISIBLE
                tvInfo.visibility = View.GONE
            }
            adapter.updateArticles(list)
        }
        vm.errorMessage.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) Snackbar.make(view, it, Snackbar.LENGTH_LONG).show()
        }
        vm.loadFavorites()
    }

    fun deleteArticle(article: Article) {
        vm.deleteFavorite(article)
        allVm.loadAllArticles()
        recVM.syncFavorites()
    }

    companion object {
        fun newInstance() = FavoriteArticlesFragment()
    }
}