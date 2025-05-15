package by.bsu.newsly.ui.detailedArticleActivity.bookmarks

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import by.bsu.newsly.R
import by.bsu.newsly.data.local.db.entity.BookmarkEntity
import java.text.SimpleDateFormat
import java.util.*

class BookmarkAdapter(
    private val items: List<BookmarkEntity>,
    private val onClick: (BookmarkEntity) -> Unit,
    private val onLongClick: (BookmarkEntity) -> Unit
) : RecyclerView.Adapter<BookmarkAdapter.VH>() {

    private val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvText: TextView = itemView.findViewById(R.id.tvSelectedText)
        private val tvDate: TextView = itemView.findViewById(R.id.tvCreatedAt)

        fun bind(b: BookmarkEntity) {
            tvText.text = b.selectedText
            tvDate.text = fmt.format(Date(b.createdAt))
            itemView.setOnClickListener { onClick(b) }
            itemView.setOnLongClickListener {
                onLongClick(b)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.bs_item_bookmark, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}