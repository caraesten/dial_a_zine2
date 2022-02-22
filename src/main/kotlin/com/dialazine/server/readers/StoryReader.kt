package com.dialazine.server.readers

import com.dialazine.server.models.Story
import com.dialazine.server.models.ZineConfig

interface StoryReader : Iterable<ByteArray>

class StoryReaderImpl(val zineConfig: ZineConfig, val story: Story) : StoryReader {
    override fun iterator(): Iterator<ByteArray> {
        return object : Iterator<ByteArray> {
            private var currentPage = 1
            override fun hasNext(): Boolean {
                return javaClass.getResourceAsStream(
                    "${zineConfig.basePath}/${story.directory}/${currentPage}.txt") != null
            }

            override fun next(): ByteArray {
                val data = javaClass.getResourceAsStream(
                    "${zineConfig.basePath}/${story.directory}/${currentPage}.txt")!!.readBytes()
                currentPage++
                return data
            }
        }
    }
}