package by.bsu.newsly.ui.mainActivity.home

import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup as VG
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import androidx.viewpager2.widget.ViewPager2
import by.bsu.newsly.R
import by.bsu.newsly.data.local.db.ArticlesDatabase
import by.bsu.newsly.domain.repository.HistoryRepository
import by.bsu.newsly.ui.mainActivity.history.HistoryViewModel
import by.bsu.newsly.ui.mainActivity.history.HistoryViewModelFactory
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class HomeFragment : Fragment() {

    private lateinit var homeVM: HomeViewModel
    private lateinit var historyVM: HistoryViewModel
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val db = Room.databaseBuilder(requireContext(), ArticlesDatabase::class.java, "articles_db")
            .build()
        historyVM = ViewModelProvider(
            requireActivity(),
            HistoryViewModelFactory(HistoryRepository(db.historyDao()))
        )[HistoryViewModel::class.java]
        homeVM =
            ViewModelProvider(requireActivity(), HomeViewModelFactory())[HomeViewModel::class.java]
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewPager = view.findViewById(R.id.viewPager2Container)
        tabLayout = view.findViewById(R.id.tabLayout)
        viewPager.adapter = VpAdapter(requireActivity(), homeVM.fragList)

        TabLayoutMediator(tabLayout, viewPager) { tab, pos ->
            tab.text = when (pos) {
                0 -> getString(R.string.tab_recommendations)
                1 -> getString(R.string.tab_all)
                2 -> getString(R.string.tab_favorites)
                else -> ""
            }
        }.attach()

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                styleTabs(position)
            }
        })

        historyVM.selectedHistory.observe(viewLifecycleOwner) { h ->
            if (h != null) {
                viewPager.post {
                    viewPager.setCurrentItem(1, true)
                    styleTabs(1)
                }
            }
        }
    }

    private fun styleTabs(selectedPos: Int) {
        val selColor = ContextCompat.getColor(requireContext(), R.color.md_theme_light_primary)
        val normColor =
            ContextCompat.getColor(requireContext(), R.color.md_theme_light_onSecondaryContainer)
        val gradDrawable =
            ContextCompat.getDrawable(requireContext(), R.drawable.gradient_indicator)!!

        if (selectedPos == 0) tabLayout.setSelectedTabIndicator(gradDrawable)
        else tabLayout.setSelectedTabIndicatorColor(selColor)

        val strip = tabLayout.getChildAt(0) as VG
        strip.children.forEachIndexed { idx, child ->
            val tv = (child as VG).children.filterIsInstance<TextView>().first()
            tv.paint.shader = null
            tv.setTextColor(
                when {
                    idx == 0 && idx == selectedPos -> applyGradient(tv)
                    idx == selectedPos -> selColor
                    else -> normColor
                }
            )
            tv.invalidate()
        }
    }

    @ColorInt
    private fun applyGradient(tv: TextView): Int {
        val w = tv.paint.measureText(tv.text.toString())
        tv.paint.shader = LinearGradient(
            0f, 0f, w, 0f,
            intArrayOf(
                ContextCompat.getColor(requireContext(), R.color.gradient_start),
                ContextCompat.getColor(requireContext(), R.color.gradient_center),
                ContextCompat.getColor(requireContext(), R.color.gradient_end)
            ), null, Shader.TileMode.CLAMP
        )
        return ContextCompat.getColor(requireContext(), R.color.md_theme_light_primary)
    }
}