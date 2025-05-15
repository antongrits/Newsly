package by.bsu.newsly.ui.mainActivity.history

import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import by.bsu.newsly.R
import by.bsu.newsly.domain.model.History
import java.text.SimpleDateFormat
import java.util.*

class HistoryAdapter(
    private var data: List<History>,
    private val frag: HistoryFragment
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val HEADER = 0
    private val ITEM = 1

    private val inFmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private val headerFmt = SimpleDateFormat("yyyy-MM-dd, EEE", Locale.getDefault())
    private val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())

    private val grouped = linkedMapOf<String, List<History>>()
    private val expanded = mutableSetOf<String>()
    private val display = mutableListOf<Pair<Int, Any>>()

    init {
        setHasStableIds(true)
        rebuild()
    }

    private fun rebuild() {
        grouped.clear()
        data.groupBy { it.date.substring(0, 10) }
            .toSortedMap(compareByDescending { it })
            .forEach { (k, v) -> grouped[k] = v.sortedByDescending { it.date } }

        display.clear()
        grouped.forEach { (k, v) ->
            display += HEADER to k
            if (expanded.contains(k)) v.forEach { display += ITEM to it }
        }
        if (expanded.isEmpty() && grouped.isNotEmpty()) {
            val k = grouped.keys.first()
            expanded += k
            display.addAll(1, grouped[k]!!.map { ITEM to it })
        }
    }

    override fun getItemId(position: Int): Long {
        val item = display[position].second
        return when (item) {
            is String -> item.hashCode().toLong()
            is History -> item.date.hashCode().toLong()
            else -> position.toLong()
        }
    }

    override fun getItemViewType(pos: Int) = display[pos].first
    override fun getItemCount() = display.size

    override fun onCreateViewHolder(p: ViewGroup, f: Int) = if (f == HEADER)
        HeaderVH(
            LayoutInflater.from(p.context).inflate(R.layout.history_header_layout, p, false),
            p
        )
    else
        ItemVH(LayoutInflater.from(p.context).inflate(R.layout.history_layout, p, false))

    override fun onBindViewHolder(h: RecyclerView.ViewHolder, pos: Int) {
        val v = display[pos].second
        if (v is String) (h as HeaderVH).bind(v)
        else (h as ItemVH).bind(v as History)
    }

    inner class HeaderVH(v: View, private val parent: ViewGroup) : RecyclerView.ViewHolder(v) {
        private val tv: TextView = v.findViewById(R.id.tvHeaderDate)
        private val iv: ImageView = v.findViewById(R.id.ivArrow)

        fun bind(date: String) {
            tv.text = headerFmt.format(inFmt.parse("$date 00:00:00") ?: Date())
            iv.setImageResource(if (expanded.contains(date)) R.drawable.expand_less else R.drawable.expand_more)
            val action = {
                val p = adapterPosition
                if (p != RecyclerView.NO_POSITION) {
                    if (expanded.contains(date)) collapse(date, p) else expand(date, p)
                }
            }
            iv.setOnClickListener { action() }
            itemView.setOnClickListener { action() }
        }
    }

    inner class ItemVH(v: View) : RecyclerView.ViewHolder(v) {
        private val q: TextView = v.findViewById(R.id.tvQuery)
        private val s: TextView = v.findViewById(R.id.tvStartDate)
        private val e: TextView = v.findViewById(R.id.tvEndDate)
        private val t: TextView = v.findViewById(R.id.tvDate)

        init {
            v.setOnLongClickListener {
                val p = adapterPosition
                if (p != RecyclerView.NO_POSITION) (display[p].second as? History)?.let {
                    frag.deleteHistory(it)
                }
                true
            }
            v.setOnClickListener {
                val p = adapterPosition
                if (p != RecyclerView.NO_POSITION) (display[p].second as? History)?.let {
                    frag.updateSelectedHistory(it)
                }
            }
        }

        fun bind(h: History) {
            q.text = h.query
            s.text = h.startDate
            e.text = h.endDate
            t.text = timeFmt.format(inFmt.parse(h.date) ?: Date())
        }
    }

    private fun collapse(k: String, pos: Int) {
        val c = grouped[k] ?: return
        val f = pos + 1
        TransitionManager.beginDelayedTransition(frag.view as ViewGroup, AutoTransition())
        display.subList(f, f + c.size).clear()
        notifyItemRangeRemoved(f, c.size)
        expanded -= k
        notifyItemChanged(pos)
    }

    private fun expand(k: String, pos: Int) {
        val c = grouped[k] ?: return
        val at = pos + 1
        TransitionManager.beginDelayedTransition(frag.view as ViewGroup, AutoTransition())
        display.addAll(at, c.map { ITEM to it })
        notifyItemRangeInserted(at, c.size)
        expanded += k
        notifyItemChanged(pos)
    }

    fun updateHistory(new: MutableList<History>) {
        data = new.sortedByDescending { it.date }
        rebuild()
        notifyDataSetChanged()
    }
}