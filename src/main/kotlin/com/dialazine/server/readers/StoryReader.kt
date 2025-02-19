package com.dialazine.server.readers

import com.dialazine.server.models.Story
import com.dialazine.server.models.ZineConfig
import java.io.BufferedReader

interface StoryReader : Iterable<BufferedReader>

class StoryReaderImpl(val zineConfig: ZineConfig, val story: Story) : StoryReader {
    override fun iterator(): Iterator<BufferedReader> {
        return object : Iterator<BufferedReader> {
            private var currentPage = 1
            override fun hasNext(): Boolean {
                return javaClass.getResourceAsStream(
                    "${zineConfig.basePath}/${story.directory}/${currentPage}.txt") != null
            }

            override fun next(): BufferedReader {
                val data = javaClass.getResourceAsStream(
                    "${zineConfig.basePath}/${story.directory}/${currentPage}.txt")!!.bufferedReader()
                currentPage++
                return data
            }
        }
    }
}