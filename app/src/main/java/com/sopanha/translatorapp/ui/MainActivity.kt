package com.sopanha.translatorapp.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.sopanha.translatorapp.R
import com.sopanha.translatorapp.databinding.ActivityMainBinding
import com.sopanha.translatorapp.utils.ApiResult
import com.sopanha.translatorapp.utils.Languages
import com.sopanha.translatorapp.viewmodel.TranslatorViewModel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: TranslatorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSpinners()
        setupListeners()
        observeViewModel()
    }

    private fun setupSpinners() {
        val sourceAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            Languages.names()
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        val targetAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            Languages.targetNames()
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        binding.spinnerSource.adapter = sourceAdapter
        binding.spinnerTarget.adapter = targetAdapter

        // Set defaults from ViewModel
        val state = viewModel.uiState.value
        binding.spinnerSource.setSelection(state.sourceLangIndex)
        binding.spinnerTarget.setSelection(state.targetLangIndex)

        binding.spinnerSource.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                viewModel.setSourceLang(pos)
            }
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }

        binding.spinnerTarget.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                viewModel.setTargetLang(pos)
            }
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }
    }

    private fun setupListeners() {
        binding.btnTranslate.setOnClickListener {
            val text = binding.etInput.text.toString()
            if (text.isBlank()) {
                binding.etInput.error = getString(R.string.error_empty_text)
                return@setOnClickListener
            }
            viewModel.translate(text)
        }

        binding.btnClear.setOnClickListener {
            binding.etInput.setText("")
            binding.tvOutput.text = ""
            viewModel.clearResult()
        }

        binding.btnSwap.setOnClickListener {
            val currentInput = binding.etInput.text.toString()
            val swapped = viewModel.swapLanguages(currentInput)
            binding.etInput.setText(swapped)
            binding.tvOutput.text = ""

            // Update spinner selections
            val state = viewModel.uiState.value
            binding.spinnerSource.setSelection(state.sourceLangIndex)
            binding.spinnerTarget.setSelection(state.targetLangIndex)
        }

        binding.btnCopy.setOnClickListener {
            val text = binding.tvOutput.text.toString()
            if (text.isBlank()) return@setOnClickListener
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("translation", text))
            Toast.makeText(this, R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (val result = state.result) {
                    is ApiResult.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.btnTranslate.isEnabled = false
                        binding.tvOutput.text = ""
                        binding.tvError.visibility = View.GONE
                    }
                    is ApiResult.Success -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnTranslate.isEnabled = true
                        binding.tvOutput.text = result.data
                        binding.tvError.visibility = View.GONE
                        binding.btnCopy.visibility = View.VISIBLE
                    }
                    is ApiResult.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnTranslate.isEnabled = true
                        binding.tvOutput.text = ""
                        binding.tvError.text = result.message
                        binding.tvError.visibility = View.VISIBLE
                        binding.btnCopy.visibility = View.GONE
                    }
                    null -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnTranslate.isEnabled = true
                        binding.tvError.visibility = View.GONE
                        binding.btnCopy.visibility = View.GONE
                    }
                }
            }
        }
    }
}
