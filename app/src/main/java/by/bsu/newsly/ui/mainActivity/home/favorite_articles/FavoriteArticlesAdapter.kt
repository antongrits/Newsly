package by.bsu.newsly.ui.mainActivity.home.favorite_articles

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import by.bsu.newsly.R
import by.bsu.newsly.ui.detailedArticleActivity.DetailedArticleActivity
import by.bsu.newsly.domain.model.Article
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*

class FavoriteArticlesAdapter(
    private var articles: List<Article>,
    private val fragment: FavoriteArticlesFragment
) : RecyclerView.Adapter<FavoriteArticlesAdapter.ArticleViewHolder>() {

    inner class ArticleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivArticleImage: ImageView = itemView.findViewById(R.id.article_image)
        val tvSourceName: TextView = itemView.findViewById(R.id.source_name)
        val tvAuthor: TextView = itemView.findViewById(R.id.author)
        val tvPublishedAt: TextView = itemView.findViewById(R.id.publishedAt)
        val tvTitle: TextView = itemView.findViewById(R.id.title)
        val tvDescription: TextView = itemView.findViewById(R.id.description)
        val ivHeart: ImageView = itemView.findViewById(R.id.heart_icon)
        val progressBar: ProgressBar = itemView.findViewById(R.id.image_progress_bar)

        init {
            itemView.setOnLongClickListener {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    fragment.deleteArticle(articles[pos])
                }
                true
            }
            itemView.setOnClickListener {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    val url = articles[pos].url
                    val intent = Intent(itemView.context, DetailedArticleActivity::class.java)
                        .apply { putExtra(DetailedArticleActivity.URL_KEY, url) }
                    itemView.context.startActivity(intent)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.article_layout, parent, false)
        return ArticleViewHolder(v)
    }

    override fun getItemCount(): Int = articles.size

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        val article = articles[position]
        holder.tvSourceName.text = article.source
        holder.tvAuthor.text = article.author
        holder.tvPublishedAt.text = formatDate(article.publishedAt)
        holder.tvTitle.text = article.title
        holder.tvDescription.text = article.description
        holder.ivHeart.setImageResource(R.drawable.heart)
        holder.progressBar.visibility = View.VISIBLE
        holder.ivArticleImage.visibility = View.INVISIBLE

        if (article.urlToImage.isNullOrEmpty()) {
            holder.ivArticleImage.setImageResource(R.drawable.newspaper)
            holder.progressBar.visibility = View.GONE
            holder.ivArticleImage.visibility = View.VISIBLE
        } else {
            Picasso.get()
                .load(article.urlToImage)
                .error(R.drawable.newspaper)
                .into(holder.ivArticleImage, object : Callback {
                    override fun onSuccess() {
                        holder.progressBar.visibility = View.GONE
                        holder.ivArticleImage.visibility = View.VISIBLE
                    }

                    override fun onError(e: Exception?) {
                        holder.progressBar.visibility = View.GONE
                        holder.ivArticleImage.visibility = View.VISIBLE
                    }
                })
        }
    }

    private fun formatDate(dateStr: String): String {
        val original = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        val target = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return runCatching { original.parse(dateStr) }
            .getOrNull()
            ?.let { target.format(it) }
            ?: dateStr
    }

    fun updateArticles(newArticles: List<Article>) {
        articles = newArticles.sortedByDescending { it.publishedAt }
        notifyDataSetChanged()
    }
}