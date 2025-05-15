package by.bsu.newsly.ui.detailedArticleActivity

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.ActionMode
import android.view.Menu
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import by.bsu.newsly.R
import by.bsu.newsly.core.utils.AdBlockingWebViewClient
import by.bsu.newsly.data.local.db.ArticlesDatabase
import by.bsu.newsly.databinding.ActivityDetailedArticleBinding
import by.bsu.newsly.domain.repository.BookmarkRepository
import by.bsu.newsly.ui.detailedArticleActivity.bookmarks.BookmarkAdapter
import by.bsu.newsly.ui.detailedArticleActivity.bookmarks.BookmarkViewModel
import by.bsu.newsly.ui.detailedArticleActivity.bookmarks.BookmarkViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.json.JSONTokener
import java.util.regex.Pattern

class DetailedArticleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailedArticleBinding
    private lateinit var webView: WebView
    private lateinit var initialUrl: String
    private var headerOffsetPx = 0
    private var canBookmark = false
    private val handler = Handler(Looper.getMainLooper())
    private val htmlTagPattern = Pattern.compile("</?(a|script|iframe|ins|div)(\\s|>|$)")

    private val bookmarkViewModel: BookmarkViewModel by viewModels {
        val dao = Room.databaseBuilder(
            this,
            ArticlesDatabase::class.java,
            "articles_db"
        ).build().bookmarkDao()
        BookmarkViewModelFactory(BookmarkRepository(dao))
    }

    companion object {
        const val URL_KEY = "url"
        const val MIN_TEXT_LENGTH = 5
    }

    @SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailedArticleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.progressBar.visibility = android.view.View.VISIBLE
        binding.webViewArticle.visibility = android.view.View.GONE
        binding.webViewNavContainer.visibility = android.view.View.GONE

        initialUrl = intent.getStringExtra(URL_KEY) ?: ""
        headerOffsetPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 56f, resources.displayMetrics
        ).toInt()

        webView = binding.webViewArticle
        with(webView.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            cacheMode = WebSettings.LOAD_DEFAULT
            allowFileAccessFromFileURLs = true
            allowUniversalAccessFromFileURLs = true
        }

        webView.webChromeClient = WebChromeClient()
        webView.webViewClient = object : AdBlockingWebViewClient(this) {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                runOnUiThread {
                    binding.progressBar.visibility = android.view.View.VISIBLE
                    binding.webViewArticle.visibility = android.view.View.GONE
                    binding.webViewNavContainer.visibility = android.view.View.GONE
                }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                canBookmark = url == initialUrl
                runOnUiThread {
                    binding.progressBar.visibility = android.view.View.GONE
                    binding.webViewArticle.visibility = android.view.View.VISIBLE
                    binding.webViewNavContainer.visibility = android.view.View.VISIBLE
                }
                if (canBookmark) {
                    injectMarkJs()
                    enableTextSelection()
                    handler.postDelayed({ loadFullTextAndBookmarks() }, 500)
                }
            }
        }
        webView.loadUrl(initialUrl)

        binding.btnCancel.setOnClickListener { finish() }
        binding.backBtn.setOnClickListener { if (webView.canGoBack()) webView.goBack() }
        binding.forwardBtn.setOnClickListener { if (webView.canGoForward()) webView.goForward() }
        binding.refreshBtn.setOnClickListener { webView.reload() }
        binding.bookmarkBtn.setOnClickListener { if (canBookmark) showBookmarks() }
    }

    private fun injectMarkJs() {
        val js = assets.open("mark.min.js").bufferedReader().use { it.readText() }
        webView.evaluateJavascript(js, null)
    }

    private fun enableTextSelection() {
        val js = """
            document.documentElement.style.userSelect='text';
            document.documentElement.style.webkitUserSelect='text';
            document.documentElement.style.webkitTouchCallout='text';
        """.trimIndent()
        webView.evaluateJavascript(js, null)
    }

    private fun loadFullTextAndBookmarks() {
        lifecycleScope.launch {
            bookmarkViewModel.loadBookmarks(initialUrl)
            injectHighlights()
            scrollToLastBookmark()
        }
    }

    override fun onActionModeStarted(mode: ActionMode) {
        if (mode.type == ActionMode.TYPE_FLOATING && canBookmark) {
            mode.menu.clear()
            mode.menu.add(Menu.NONE, 1, Menu.NONE, "Добавить закладку")
                .setOnMenuItemClickListener {
                    captureAndSave(mode)
                    true
                }
        } else {
            super.onActionModeStarted(mode)
        }
    }

    private fun captureAndSave(mode: ActionMode) {
        if (webView.url != initialUrl) {
            showError("Вернитесь на исходную страницу")
            return
        }

        val js = """
      (function(){
  function getOffset(node, offset) {
    var walker = document.createTreeWalker(document.body, NodeFilter.SHOW_TEXT, null, false);
    var idx = 0;
    while (walker.nextNode()) {
      var n = walker.currentNode;
      if (n === node) return idx + offset;
      idx += n.textContent.length;
    }
    return -1;
  }
  
  function checkForbiddenTags(range) {
    const tags = ['a', 'script', 'iframe', 'ins', 'div'];
    const iterator = document.createNodeIterator(range.commonAncestorContainer, NodeFilter.SHOW_ELEMENT);
    let node;
    while(node = iterator.nextNode()) {
      if(range.intersectsNode(node) && tags.includes(node.tagName.toLowerCase())) {
        return true;
      }
    }
    return false;
  }

  var sel = window.getSelection();
  if (!sel || sel.rangeCount===0) return null;
  var range = sel.getRangeAt(0);
  if(checkForbiddenTags(range)) return JSON.stringify({error:'forbidden_tags'});
  
  var full = document.body.textContent;
  var text = range.toString().trim();
  if (!text) return JSON.stringify({error:'empty'});
  var start = getOffset(range.startContainer, range.startOffset);
  var end   = getOffset(range.endContainer,   range.endOffset);
  if (start<0||end<0) return JSON.stringify({error:'offset'});
  var pos = range.getBoundingClientRect().top + window.scrollY;
  return JSON.stringify({ full, text, start, end, position:pos });
})();
    """.trimIndent()

        webView.evaluateJavascript(js) { raw ->
            if (raw == null || raw == "null" || raw == "undefined") {
                showError("Не удалось получить выделение")
                return@evaluateJavascript
            }

            val obj = JSONTokener(raw).nextValue().let {
                when (it) {
                    is String -> JSONObject(it)
                    is JSONObject -> it
                    else -> {
                        showError("Ошибка формата")
                        return@evaluateJavascript
                    }
                }
            }

            if (obj.has("error")) {
                when (obj.getString("error")) {
                    "forbidden_tags" -> showError("Выделение содержит запрещенные элементы")
                    "empty" -> showError("Выделите текст")
                    "offset" -> showError("Ошибка определения позиции")
                }
                return@evaluateJavascript
            }

            val full = obj.optString("full", "")
            val text = obj.optString("text", "").trim()
            val start = obj.optInt("start", -1)
            val end = obj.optInt("end", -1)
            val pos = obj.optDouble("position", 0.0).toInt()

            mode.finish()

            when {
                text.length < MIN_TEXT_LENGTH -> {
                    showError("Выделите минимум $MIN_TEXT_LENGTH символов")
                    return@evaluateJavascript
                }

                htmlTagPattern.matcher(text).find() -> {
                    showError("Выделение содержит HTML-теги")
                    return@evaluateJavascript
                }

                start < 0 || end <= start -> {
                    showError("Некорректное выделение")
                    return@evaluateJavascript
                }
            }

            bookmarkViewModel.setFullText(full)
            lifecycleScope.launch {
                try {
                    val id = bookmarkViewModel.mergeAndSave(
                        initialUrl,
                        text,
                        start,
                        end,
                        pos
                    )
                    bookmarkViewModel.loadBookmarks(initialUrl)
                    injectHighlights()
                    scrollToMark(id)
                } catch (e: Exception) {
                    showError(e.message ?: "Ошибка сохранения")
                }
            }
        }
    }

    private fun injectHighlights() {
        if (webView.url != initialUrl) return

        lifecycleScope.launch {
            val list = bookmarkViewModel.bookmarks.value
                .sortedByDescending { it.startOffset }

            val js = """
        (function(){
          document.querySelectorAll('mark.bsu').forEach(m => m.outerHTML = m.innerHTML);

          function createRange(start, end) {
            var walker = document.createTreeWalker(document.body, NodeFilter.SHOW_TEXT, null, false);
            var idx = 0, range = document.createRange(), node;
            while (walker.nextNode()) {
              node = walker.currentNode;
              var next = idx + node.textContent.length;
              if (start >= idx && start < next) range.setStart(node, start - idx);
              if (end > idx && end <= next) {
                range.setEnd(node, end - idx);
                break;
              }
              idx = next;
            }
            return range;
          }

          ${
                list.joinToString(",", "var data = [", "];") {
                    """{start:${it.startOffset},end:${it.endOffset},id:${it.id}}"""
                }
            }
          
          data.forEach(m => {
            var r = createRange(m.start, m.end);
            if (r.collapsed) return;
            var markEl = document.createElement('mark');
            markEl.className = 'bsu';
            markEl.setAttribute('data-id', m.id);
            r.surroundContents(markEl);
          });
        })();
        """.trimIndent()

            runOnUiThread {
                webView.evaluateJavascript(js, null)
            }
        }
    }

    private fun scrollToLastBookmark() {
        val last = bookmarkViewModel.bookmarks.value
            .sortedBy { it.createdAt }
            .lastOrNull() ?: return
        scrollToMark(last.id)
    }

    private fun scrollToMark(id: Long) {
        val js = """
            (function(){
              var el = document.querySelector("mark.bsu[data-id='$id']");
              if(!el) return;
              
              el.style.transition = 'all 0.5s ease';
              el.style.boxShadow = '0 0 0 4px rgba(255,200,0,0.5)';
              el.style.backgroundColor = 'rgba(255,235,59,0.8)';
              window.getComputedStyle(el).getPropertyValue('transform');
              el.style.transform = 'scale(1.02)';

              setTimeout(() => {
                el.style.boxShadow = '';
                el.style.transform = '';
                el.style.backgroundColor = '';
              }, 1500);

              var y = el.getBoundingClientRect().top + window.scrollY - $headerOffsetPx;
              window.scrollTo({top:y,behavior:'smooth'});
            })();
        """.trimIndent()
        webView.evaluateJavascript(js, null)
    }

    private fun showBookmarks() {
        val dlg = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_bookmarks, null)
        val rv = view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvBookmarks)
        val empty = view.findViewById<android.widget.TextView>(R.id.bsEmpty)
        val title = view.findViewById<android.widget.TextView>(R.id.bsTitle)
        rv.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        lifecycleScope.launch {
            val list = bookmarkViewModel.bookmarks.value.sortedByDescending { it.createdAt }
            runOnUiThread {
                title.text = "Закладки (${list.size})"
                if (list.isEmpty()) {
                    empty.visibility = android.view.View.VISIBLE
                    rv.visibility = android.view.View.GONE
                } else {
                    empty.visibility = android.view.View.GONE
                    rv.visibility = android.view.View.VISIBLE
                    rv.adapter = BookmarkAdapter(
                        list,
                        onClick = { b ->
                            dlg.dismiss()
                            if (webView.url == initialUrl) scrollToMark(b.id)
                        },
                        onLongClick = { b ->
                            dlg.dismiss()
                            AlertDialog.Builder(this@DetailedArticleActivity)
                                .setMessage("Удалить закладку?")
                                .setPositiveButton("Да") { _, _ ->
                                    lifecycleScope.launch {
                                        bookmarkViewModel.deleteBookmark(b, initialUrl)
                                        injectHighlights()
                                    }
                                }
                                .setNegativeButton("Нет", null)
                                .show()
                        })
                }
            }
        }
        dlg.setContentView(view)
        dlg.show()
    }

    private fun showError(msg: String) {
        AlertDialog.Builder(this)
            .setMessage(msg)
            .setPositiveButton("OK", null)
            .show()
    }
}