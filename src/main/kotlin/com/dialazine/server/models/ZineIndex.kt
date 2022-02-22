package com.dialazine.server.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ZineIndex(
    @SerialName("hello")
    val helloFilePath: String,
    val contents: List<Story>
)
