package by.bsu.newsly.ui.mainActivity.settings

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import by.bsu.newsly.R
import by.bsu.newsly.databinding.DialogFeedbackBinding
import by.bsu.newsly.databinding.FragmentSettingsBinding
import by.bsu.newsly.domain.repository.SettingsRepository
import java.text.SimpleDateFormat
import java.util.*

class SettingsFragment : Fragment() {

    private var _b: FragmentSettingsBinding? = null
    private val b get() = _b!!
    private lateinit var vm: SettingsViewModel
    private val pickedUris = mutableListOf<Uri>()
    private lateinit var feedbackBinding: DialogFeedbackBinding

    private val pickImagesLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            requireContext().contentResolver
                .takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            pickedUris.add(it)
            addAttachmentPreview(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _b = FragmentSettingsBinding.inflate(inflater, container, false)
        val repo = SettingsRepository(requireContext())
        vm = ViewModelProvider(
            requireActivity(),
            SettingsViewModelFactory(repo)
        )[SettingsViewModel::class.java]
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        b.tvAppName.text = requireContext()
            .applicationInfo.loadLabel(requireContext().packageManager).toString()
        b.tvVersion.text = "Версия ${
            requireContext().packageManager
                .getPackageInfo(requireContext().packageName, 0).versionName
        }"

        vm.adBlockingEnabled.observe(viewLifecycleOwner) {
            b.switchAdBlocking.isChecked = it
        }
        vm.cacheSizeText.observe(viewLifecycleOwner) { size ->
            b.buttonClearCache.text = getString(R.string.clear_cache) + " ($size)"
        }
        vm.isClearing.observe(viewLifecycleOwner) { clearing ->
            b.buttonClearCache.isEnabled = !clearing
            b.progressClear.visibility = if (clearing) View.VISIBLE else View.GONE
            if (clearing) {
                val anim = ObjectAnimator.ofFloat(b.progressClear, View.ROTATION, 0f, 360f)
                anim.duration = 800
                anim.repeatCount = ValueAnimator.INFINITE
                anim.start()
                b.progressClear.tag = anim
            } else {
                (b.progressClear.tag as? ObjectAnimator)?.cancel()
            }
        }
        vm.didClear.observe(viewLifecycleOwner) { did ->
            if (did) Toast.makeText(requireContext(), "Кэш очищен", Toast.LENGTH_SHORT).show()
        }

        b.switchAdBlocking.setOnCheckedChangeListener { _, checked ->
            vm.setAdBlocking(checked)
        }

        b.buttonClearCache.setOnClickListener {
            vm.clearCache()
        }

        b.buttonFeedback.setOnClickListener {
            showFeedbackDialog()
        }
    }

    private fun showFeedbackDialog() {
        pickedUris.clear()
        feedbackBinding = DialogFeedbackBinding.inflate(layoutInflater)
        AlertDialog.Builder(requireContext())
            .setTitle("Обратная связь")
            .setView(feedbackBinding.root)
            .setPositiveButton("Отправить") { _, _ ->
                sendFeedback(feedbackBinding.etFeedback.text.toString())
            }
            .setNegativeButton("Отмена", null)
            .show()
        feedbackBinding.btnAttach.setOnClickListener {
            pickImagesLauncher.launch(arrayOf("image/*"))
        }
    }

    private fun addAttachmentPreview(uri: Uri) {
        val iv = ImageView(requireContext()).apply {
            layoutParams = ViewGroup.MarginLayoutParams(120, 120).apply { rightMargin = 12 }
            scaleType = ImageView.ScaleType.CENTER_CROP
            setImageURI(uri)
        }
        feedbackBinding.llImages.addView(iv)
    }

    private fun sendFeedback(text: String) {
        val body = """
            Устройство: ${Build.MANUFACTURER} ${Build.MODEL}
            Android: ${Build.VERSION.RELEASE}
            Версия приложения: ${
            requireContext().packageManager
                .getPackageInfo(requireContext().packageName, 0).versionName
        }
            Время: ${
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(Date())
        }

            $text
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf("antongric07@gmail.com"))
            putExtra(Intent.EXTRA_SUBJECT, "Feedback – Newsly Android")
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(pickedUris))
            putExtra(Intent.EXTRA_TEXT, body)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(intent, "Отправка письма"))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}