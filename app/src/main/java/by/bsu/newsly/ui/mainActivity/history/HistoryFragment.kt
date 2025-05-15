package by.bsu.newsly.ui.mainActivity.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import by.bsu.newsly.R
import by.bsu.newsly.data.local.db.ArticlesDatabase
import by.bsu.newsly.domain.model.History
import by.bsu.newsly.domain.repository.HistoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HistoryFragment : Fragment() {
    private lateinit var vm: HistoryViewModel
    private lateinit var rv: RecyclerView
    private lateinit var tvInfo: TextView
    private lateinit var adapter: HistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val db = Room.databaseBuilder(
            requireContext(),
            ArticlesDatabase::class.java,
            "articles_db"
        ).build()
        val repo = HistoryRepository(db.historyDao())
        vm = ViewModelProvider(
            requireActivity(),
            HistoryViewModelFactory(repo)
        )[HistoryViewModel::class.java]
        lifecycleScope.launch(Dispatchers.IO) {
            vm.loadWholeHistory()
        }
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        rv = view.findViewById(R.id.recyclerViewHistory)
        tvInfo = view.findViewById(R.id.tvInfo)
        adapter = HistoryAdapter(emptyList(), this)
        rv.layoutManager = LinearLayoutManager(activity)
        rv.adapter = adapter

        vm.historyFromDB.observe(viewLifecycleOwner) { list ->
            if (list.isEmpty()) {
                rv.visibility = View.GONE
                tvInfo.visibility = View.VISIBLE
            } else {
                tvInfo.visibility = View.GONE
                rv.visibility = View.VISIBLE
            }
            list.sortByDescending { it.date }
            adapter.updateHistory(list)
        }
    }

    fun deleteHistory(h: History) {
        vm.deleteHistory(h)
    }

    fun updateSelectedHistory(h: History) {
        vm.selectedHistory.postValue(h)
        Navigation.findNavController(requireView()).popBackStack()
    }
}