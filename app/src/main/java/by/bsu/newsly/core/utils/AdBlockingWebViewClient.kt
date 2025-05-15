package by.bsu.newsly.core.utils

import android.content.Context
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import by.bsu.newsly.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

open class AdBlockingWebViewClient(private val context: Context) : WebViewClient() {
    private val adHosts = mutableSetOf<String>()
    private val adBlockingEnabled = context
        .getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        .getBoolean("ad_blocking_enabled", true)
    private val preloadJob: Job

    init {
        preloadJob = CoroutineScope(Dispatchers.IO).launch {
            adHosts.addAll(loadAdHosts())
        }
    }

    private suspend fun loadAdHosts(): Set<String> = withContext(Dispatchers.IO) {
        val hosts = mutableSetOf<String>()
        val stream: InputStream = context.resources.openRawResource(R.raw.ad_hosts)
        BufferedReader(InputStreamReader(stream)).use { reader ->
            reader.lineSequence().forEach { line ->
                if (line.isNotBlank()) hosts += line.trim()
            }
        }
        hosts
    }

    override fun shouldInterceptRequest(
        view: WebView?,
        request: WebResourceRequest?
    ): WebResourceResponse? {
        runBlocking { preloadJob.join() }
        if (adBlockingEnabled) {
            val host = request?.url?.host ?: return super.shouldInterceptRequest(view, request)
            if (adHosts.any { host.contains(it) }) {
                return WebResourceResponse("text/plain", "utf-8", null)
            }
        }
        return super.shouldInterceptRequest(view, request)
    }
}