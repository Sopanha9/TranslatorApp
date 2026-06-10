package com.sopanha.translatorapp.data.model

import com.google.gson.annotations.SerializedName

data class TranslateResponse(
    @SerializedName("translation") val translation: String?,
    @SerializedName("status") val status: Int?,
    @SerializedName("message") val message: String?
)