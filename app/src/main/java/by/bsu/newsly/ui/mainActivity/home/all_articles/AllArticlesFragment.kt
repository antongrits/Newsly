package by.bsu.newsly.ui.mainActivity.home.all_articles

import android.animation.ObjectAnimator
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import by.bsu.newsly.R
import by.bsu.newsly.data.local.db.ArticlesDatabase
import by.bsu.newsly.data.remote.api.ApiService
import by.bsu.newsly.domain.model.Article
import by.bsu.newsly.domain.model.History
import by.bsu.newsly.domain.repository.ArticleRepository
import by.bsu.newsly.domain.repository.BookmarkRepository
import by.bsu.newsly.domain.repository.HistoryRepository
import by.bsu.newsly.domain.repository.RecommendationRepository
import by.bsu.newsly.ui.mainActivity.home.favorite_articles.FavoriteArticlesViewModel
import by.bsu.newsly.ui.mainActivity.home.favorite_articles.FavoriteArticlesViewModelFactory
import by.bsu.newsly.ui.mainActivity.history.HistoryViewModel
import by.bsu.newsly.ui.mainActivity.history.HistoryViewModelFactory
import by.bsu.newsly.ui.mainActivity.home.recommendations.RecommendationViewModel
import by.bsu.newsly.ui.mainActivity.home.recommendations.RecommendationViewModelFactory
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.util.*

class AllArticlesFragment : Fragment() {

    private lateinit var vm: AllArticlesViewModel
    private lateinit var favVm: FavoriteArticlesViewModel
    private lateinit var historyVm: HistoryViewModel
    private lateinit var recVM: RecommendationViewModel
    private lateinit var adapter: AllArticlesAdapter
    private lateinit var recycler: RecyclerView
    private lateinit var progress: ProgressBar
    private lateinit var tvInfo: TextView
    private lateinit var filters: View
    private lateinit var toggle: ImageButton
    private lateinit var etQ: TextInputEditText
    private lateinit var etS: TextInputEditText
    private lateinit var etE: TextInputEditText
    private lateinit var ddSort: MaterialAutoCompleteTextView
    private lateinit var ddLang: MaterialAutoCompleteTextView
    private lateinit var swipe: SwipeRefreshLayout
    private lateinit var frame: View
    private lateinit var bar: AppBarLayout
    private lateinit var barContent: View

    private val sortMap = mapOf(
        "По дате публикации" to "publishedAt",
        "По актуальности" to "relevancy",
        "По популярности" to "popularity"
    )
    private val langMap = mapOf(
        "Все" to "", "Русский" to "ru", "Английский" to "en"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val db = Room.databaseBuilder(requireContext(), ArticlesDatabase::class.java, "articles_db")
            .build()
        val repo = ArticleRepository(db.articlesDao(), ApiService.create())
        val repoBookmark = BookmarkRepository(db.bookmarkDao())
        val repoHistory = HistoryRepository(db.historyDao())
        val repoRecommendation = RecommendationRepository(repo, repoHistory)
        vm = ViewModelProvider(
            requireActivity(),
            AllArticlesViewModelFactory(repo, repoBookmark)
        )[AllArticlesViewModel::class.java]
        favVm = ViewModelProvider(
            requireActivity(),
            FavoriteArticlesViewModelFactory(repo, repoBookmark)
        )[FavoriteArticlesViewModel::class.java]
        historyVm = ViewModelProvider(
            requireActivity(),
            HistoryViewModelFactory(HistoryRepository(db.historyDao()))
        )[HistoryViewModel::class.java]
        recVM = ViewModelProvider(
            requireActivity(),
            RecommendationViewModelFactory(repoRecommendation, repo)
        )[RecommendationViewModel::class.java]
        return inflater.inflate(R.layout.fragment_all_articles, container, false)
    }

    override fun onViewCreated(v: View, s: Bundle?) {
        recycler = v.findViewById(R.id.recyclerViewAllArticles)
        progress = v.findViewById(R.id.progressBar)
        tvInfo = v.findViewById(R.id.tvInfo)
        filters = v.findViewById(R.id.llDateAndSortContainer)
        toggle = v.findViewById(R.id.btnToggleDateAndSort)
        etQ = v.findViewById(R.id.etInputArticle)
        etS = v.findViewById(R.id.etStartDate)
        etE = v.findViewById(R.id.etEndDate)
        ddSort = v.findViewById(R.id.spinnerSortOptions)
        ddLang = v.findViewById(R.id.spinnerLanguageOptions)
        swipe = v.findViewById(R.id.swipeRefreshLayout)
        frame = v.findViewById(R.id.contentFrame)
        bar = v.findViewById(R.id.appBarLayout)
        barContent = v.findViewById(R.id.appBarContent)

        adapter = AllArticlesAdapter(emptyList(), this)
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        ddSort.setAdapter(
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1,
                resources.getStringArray(R.array.sort_options_ui)
            )
        )
        ddLang.setAdapter(
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1,
                resources.getStringArray(R.array.language_options_ui)
            )
        )

        if (vm.articlesFromAPI.value.isNullOrEmpty()) {
            showInfo("Здесь пока ничего нет! Введите запрос")
            vm.errorMessage.postValue("")
        }

        if (vm.isDateAndSortContVisible) showFilters(false)

        toggle.setOnClickListener { toggleFilters() }
        etS.setOnClickListener { pickDate { etS.setText(it) } }
        etE.setOnClickListener { pickDate { etE.setText(it) } }
        v.findViewById<TextInputLayout>(R.id.textInputLayoutSearch)
            .setStartIconOnClickListener { search() }
        swipe.setOnRefreshListener { search(true) }

        observe()
    }

    private fun observe() {
        historyVm.selectedHistory.observe(viewLifecycleOwner) { h ->
            h ?: return@observe
            etQ.setText(h.query)
            etS.setText(h.startDate)
            etE.setText(h.endDate)
            ddSort.setText(h.sortBy, false)
            ddLang.setText(h.language, false)
            h.date = now(true)
            historyVm.updateHistory(h)
            hideInfo(); showProg(); hideCardsAndFilters()
            vm.getArticlesFromApi(
                h.query,
                h.startDate,
                h.endDate,
                sortMap[h.sortBy] ?: "publishedAt",
                langMap[h.language] ?: ""
            )
            historyVm.selectedHistory.postValue(null)
        }

        vm.articlesFromAPI.observe(viewLifecycleOwner) { lst ->
            swipe.isRefreshing = false
            recycler.visibility = View.VISIBLE
            val ok = !lst.isNullOrEmpty()
            onData(ok)
            lst?.sortByDescending { it.isFavorite }
            adapter.updateArticles(lst ?: emptyList())
        }

        vm.errorMessage.observe(viewLifecycleOwner) {
            swipe.isRefreshing = false
            if (!it.isNullOrEmpty()) {
                hideProg()
                if (vm.articlesFromAPI.value.isNullOrEmpty()) showInfo("Ошибка! Ничего не найдено!")
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            }
        }

        vm.articlesFromDB.observe(viewLifecycleOwner) {
            vm.syncArticles()
            favVm.loadFavorites()
            recVM.syncFavorites()
        }
    }

    private fun hideCardsAndFilters() {
        recycler.visibility = View.GONE
        hideFilters()
    }

    private fun showFilters(animate: Boolean) {
        filters.visibility = View.VISIBLE
        if (animate) ObjectAnimator.ofFloat(filters, "alpha", 0f, 1f).apply {
            duration = 300
            interpolator = AccelerateDecelerateInterpolator()
        }.start()
        toggle.setImageResource(R.drawable.expand_less)
        vm.isDateAndSortContVisible = true
    }

    private fun hideFilters() {
        if (filters.visibility == View.VISIBLE) {
            ObjectAnimator.ofFloat(filters, "alpha", 1f, 0f).apply {
                duration = 300
                interpolator = AccelerateDecelerateInterpolator()
                addUpdateListener {
                    if (it.animatedFraction == 1f) filters.visibility = View.GONE
                }
            }.start()
            toggle.setImageResource(R.drawable.expand_more)
            vm.isDateAndSortContVisible = false
        }
    }

    private fun toggleFilters() {
        if (filters.visibility == View.VISIBLE) hideFilters() else showFilters(true)
    }

    private fun search(fromSwipe: Boolean = false) {
        var q = etQ.text.toString()
        var s = etS.text.toString()
        var e = etE.text.toString()
        val sort = sortMap[ddSort.text.toString()] ?: "publishedAt"
        val lang = langMap[ddLang.text.toString()] ?: ""
        val today = now(false)
        if (s.isEmpty()) s = today
        if (e.isEmpty()) e = today
        if (!fromSwipe && q.isNotEmpty()) {
            historyVm.insertHistory(
                History(
                    UUID.randomUUID(),
                    q,
                    s,
                    e,
                    ddSort.text.toString(),
                    ddLang.text.toString(),
                    now(true)
                )
            )
        }
        if (!fromSwipe) {
            hideInfo(); showProg(); hideCardsAndFilters()
        }
        vm.getArticlesFromApi(q, s, e, sort, lang)
    }

    fun updateArticle(a: Article) {
        if (a.isFavorite) vm.deleteArticle(a) else vm.insertArticle(a.copy(isFavorite = true))
    }

    private fun onData(ok: Boolean) {
        adjust(ok)
        if (ok) {
            hideInfo(); frame.setPadding(0, 0, 0, 0)
        } else {
            showInfo("По вашему запросу ничего не найдено!")
            val p = if (filters.visibility == View.VISIBLE) filters.height else 0
            frame.setPadding(0, p, 0, 0)
        }
        hideProg()
    }

    private fun adjust(has: Boolean) {
        val land = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val allow = if (land) true else has
        val lp = (barContent.layoutParams as AppBarLayout.LayoutParams)
        lp.scrollFlags = if (allow)
            AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS
        else 0
        barContent.layoutParams = lp
        if (!allow) bar.setExpanded(true, false)
    }

    private fun pickDate(cb: (String) -> Unit) {
        MaterialDatePicker.Builder.datePicker()
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds()).build().apply {
                addOnPositiveButtonClickListener {
                    cb(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it)))
                }
            }.show(parentFragmentManager, "date")
    }

    private fun now(full: Boolean) = SimpleDateFormat(
        if (full) "yyyy-MM-dd HH:mm:ss" else "yyyy-MM-dd", Locale.getDefault()
    ).format(Date())

    private fun showProg() {
        progress.visibility = View.VISIBLE
    }

    private fun hideProg() {
        progress.visibility = View.GONE
    }

    private fun showInfo(s: String) {
        tvInfo.text = s
        tvInfo.visibility = View.VISIBLE
    }

    private fun hideInfo() {
        tvInfo.visibility = View.GONE
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        adjust(adapter.itemCount > 0)
    }

    companion object {
        fun newInstance() = AllArticlesFragment()
    }
}