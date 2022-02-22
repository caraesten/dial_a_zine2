package com.dialazine.server.models

import kotlinx.serialization.Serializable

@Serializable
data class Story(
    val title: String,
    val author: String,
    val type: String = "story",
    val directory: String
)
