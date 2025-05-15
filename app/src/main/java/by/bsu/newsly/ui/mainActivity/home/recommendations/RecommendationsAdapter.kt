package by.bsu.newsly.ui.mainActivity.home.recommendations

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import by.bsu.newsly.R
import by.bsu.newsly.domain.model.Article
import by.bsu.newsly.ui.detailedArticleActivity.DetailedArticleActivity
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*

class RecommendationsAdapter(
    private var articles: List<Article>,
    private val fragment: RecommendationsFragment
) : RecyclerView.Adapter<RecommendationsAdapter.Holder>() {

    inner class Holder(v: View) : RecyclerView.ViewHolder(v) {
        val ivImage: ImageView = v.findViewById(R.id.article_image)
        val pb: ProgressBar = v.findViewById(R.id.image_progress_bar)
        val tvSource: TextView = v.findViewById(R.id.source_name)
        val tvAuthor: TextView = v.findViewById(R.id.author)
        val tvDate: TextView = v.findViewById(R.id.publishedAt)
        val tvTitle: TextView = v.findViewById(R.id.title)
        val tvDesc: TextView = v.findViewById(R.id.description)
        val ivHeart: ImageView = v.findViewById(R.id.heart_icon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = Holder(
        LayoutInflater.from(parent.context).inflate(R.layout.article_layout, parent, false)
    )

    override fun onBindViewHolder(h: Holder, pos: Int) {
        fun show() {
            h.pb.visibility = View.GONE; h.ivImage.visibility = View.VISIBLE
        }

        fun hide() {
            h.ivImage.visibility = View.GONE; h.pb.visibility = View.VISIBLE
        }
        hide()
        val a = articles[pos]
        h.tvSource.text = a.source
        h.tvAuthor.text = a.author
        h.tvDate.text = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            .format(
                SimpleDateFormat(
                    "yyyy-MM-dd'T'HH:mm:ss'Z'",
                    Locale.getDefault()
                ).parse(a.publishedAt)!!
            )
        h.tvTitle.text = a.title
        h.tvDesc.text = a.description
        h.ivHeart.setImageResource(if (a.isFavorite) R.drawable.heart else R.drawable.border_heart)
        if (a.urlToImage.isNullOrEmpty()) {
            h.ivImage.setImageResource(R.drawable.newspaper); show()
        } else {
            Picasso.get().load(a.urlToImage).error(R.drawable.newspaper)
                .into(h.ivImage, object : Callback {
                    override fun onSuccess() = show()
                    override fun onError(e: Exception?) = show()
                })
        }
        h.ivHeart.setOnClickListener { fragment.vm.updateArticle(a) }
        h.itemView.setOnClickListener {
            fragment.startActivity(
                Intent(it.context, DetailedArticleActivity::class.java)
                    .putExtra(DetailedArticleActivity.URL_KEY, a.url)
            )
        }
    }

    override fun getItemCount() = articles.size

    fun update(new: List<Article>) {
        articles = new
        notifyDataSetChanged()
    }
}