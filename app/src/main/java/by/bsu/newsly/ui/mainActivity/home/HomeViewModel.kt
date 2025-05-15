package by.bsu.newsly.ui.mainActivity.home

import androidx.lifecycle.ViewModel
import by.bsu.newsly.ui.mainActivity.home.all_articles.AllArticlesFragment
import by.bsu.newsly.ui.mainActivity.home.favorite_articles.FavoriteArticlesFragment
import androidx.fragment.app.Fragment
import by.bsu.newsly.ui.mainActivity.home.recommendations.RecommendationsFragment

class HomeViewModel : ViewModel() {
    val fragList: List<Fragment> = listOf(
        RecommendationsFragment.newInstance(),
        AllArticlesFragment.newInstance(),
        FavoriteArticlesFragment.newInstance()
    )
}