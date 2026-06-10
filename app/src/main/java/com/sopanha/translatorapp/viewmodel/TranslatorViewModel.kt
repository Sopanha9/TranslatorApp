package com.sopanha.translatorapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sopanha.translatorapp.data.repository.TranslatorRepository
import com.sopanha.translatorapp.utils.ApiResult
import com.sopanha.translatorapp.utils.Languages
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class TranslatorUiState(
    val result: ApiResult<String>? = null,
    val sourceLangIndex: Int = 0,   // "Auto Detect"
    val targetLangIndex: Int = 18   // "English" in targetLanguages list
)

class TranslatorViewModel : ViewModel() {

    private val repository = TranslatorRepository()

    private val _uiState = MutableStateFlow(TranslatorUiState())
    val uiState: StateFlow<TranslatorUiState> = _uiState

    fun translate(text: String) {
        if (text.isBlank()) return

        val fromCode = Languages.sourceLanguages[_uiState.value.sourceLangIndex].code
        val toCode = Languages.targetLanguages[_uiState.value.targetLangIndex].code

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(result = ApiResult.Loading)
            val apiResult = repository.translate(from = fromCode, to = toCode, text = text)
            _uiState.value = _uiState.value.copy(
                result = when (apiResult) {
                    is ApiResult.Success -> ApiResult.Success(apiResult.data.translation ?: "")
                    is ApiResult.Error -> ApiResult.Error(apiResult.message)
                    is ApiResult.Loading -> ApiResult.Loading
                }
            )
        }
    }

    fun setSourceLang(index: Int) {
        _uiState.value = _uiState.value.copy(sourceLangIndex = index)
    }

    fun setTargetLang(index: Int) {
        _uiState.value = _uiState.value.copy(targetLangIndex = index)
    }

    fun swapLanguages(sourceText: String): String {
        val state = _uiState.value
        val srcCode = Languages.sourceLanguages[state.sourceLangIndex].code
        val tgtCode = Languages.targetLanguages[state.targetLangIndex].code

        // Can only swap if source is not "auto"
        if (srcCode == "auto") return sourceText

        val newSrcIdx = Languages.sourceIndexOf(tgtCode)
        val newTgtIdx = Languages.targetIndexOf(srcCode)
        val swappedText = when (val r = state.result) {
            is ApiResult.Success -> r.data
            else -> sourceText
        }
        _uiState.value = _uiState.value.copy(
            sourceLangIndex = newSrcIdx,
            targetLangIndex = newTgtIdx,
            result = null
        )
        return swappedText
    }

    fun clearResult() {
        _uiState.value = _uiState.value.copy(result = null)
    }
}
