package com.dialazine.server.readers

import com.dialazine.server.models.Story
import com.dialazine.server.models.ZineConfig
import java.nio.charset.Charset

interface IndexReader {
    fun readWelcomePage(): ByteArray
    fun readContentsPage(): ByteArray
    fun readStorySelection(story: Story): ByteArray
}

class IndexReaderImpl(private val zineConfig: ZineConfig,
                      private val charset: Charset = Charsets.UTF_8) : IndexReader {
    override fun readWelcomePage(): ByteArray {
        return javaClass.getResourceAsStream(zineConfig.helloFullPath)?.readBytes() ?: ByteArray(0)
    }

    override fun readContentsPage(): ByteArray {
        val contentsList = zineConfig.index.contents.mapIndexed { index, item ->
            "${index + 1} > ${item.title} < ...by ${item.author}\n"
        } + "(or X to quit!)\n"
        return contentsList.joinToString("\n").toByteArray(charset)
    }

    override fun readStorySelection(story: Story): ByteArray {
        return """
            
            ...you picked: ${story.title} by ${story.author}
            ...press RETURN to start reading, and to continue after each page...
        """.trimIndent().toByteArray(charset)
    }
}
