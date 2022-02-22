package com.dialazine.server.models

data class ZineConfig(
    val index: ZineIndex,
    val basePath: String
) {
    val helloFullPath = "$basePath/${index.helloFilePath}"
}