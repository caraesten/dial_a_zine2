package com.dialazine.server

import com.dialazine.server.models.ZineConfig
import com.dialazine.server.readers.IndexReader
import com.dialazine.server.readers.IndexReaderImpl
import com.dialazine.server.readers.StoryReader
import com.dialazine.server.readers.StoryReaderImpl
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.Socket
import java.nio.charset.Charset

interface ReaderConnection {
    val startTime: Long
    fun forceDisconnect()
}

class ReaderThread(private val clientSocket: Socket,
                   private val zineConfig: ZineConfig,
                   private val onDisconnect: (ReaderThread) -> Unit,
                   private val charset: Charset = Charsets.UTF_8) : Thread(), ReaderConnection {

    override val startTime = System.currentTimeMillis()

    override fun forceDisconnect() {
        performDisconnect()
    }

    private val indexReader: IndexReader = IndexReaderImpl(zineConfig, charset)
    override fun run() {
        try {
            clearScreen()
            clientSocket.getOutputStream().write(indexReader.readWelcomePage())
            waitForReturnKey()
        } catch (ex: Throwable) {
            ex.printStackTrace()
            performDisconnect()
        }
        while (!currentThread().isInterrupted) {
            try {
                clientSocket.getOutputStream().write(indexReader.readContentsPage())
                val selectedStoryItem = waitForStoryChoice()
                if (selectedStoryItem == -1) {
                    performDisconnect()
                    return
                }
                val selectedStory = zineConfig.index.contents.getOrNull(selectedStoryItem - 1)
                    ?: throw IllegalStateException("Bad Selection")
                clientSocket.getOutputStream().write(indexReader.readStorySelection(selectedStory))
                waitForReturnKey()
                val storyReader: StoryReader = StoryReaderImpl(zineConfig, selectedStory)
                clientSocket.getOutputStream().write("\n".toByteArray(charset))
                storyReader.forEach {
                    clearScreen()
                    clientSocket.getOutputStream().write(it)
                    waitForReturnKey()
                }
            } catch (ex: Throwable) { // TODO: be more specific
                ex.printStackTrace()
                performDisconnect()
            }
        }
    }

    private fun waitForReturnKey() {
        while (clientSocket.getInputStream().read() != ASCII_LF) {
            // do nothing
        }
    }

    private fun clearScreen() {
        if (charset != Charsets.UTF_8) {
            println("Unsafe clear screen!")
        }
        val bytesToClearScreen = "\u001b[2J\u001b[H".toByteArray(Charsets.UTF_8)
        clientSocket.getOutputStream().write(bytesToClearScreen)
    }

    private fun waitForStoryChoice(): Int {
        val inputStream = BufferedReader(InputStreamReader(clientSocket.getInputStream()))
        val selectionString = try {
            inputStream.readLine()
        } catch (ex: IllegalArgumentException) {
            ""
        } catch (ex: IOException) {
            ""
        }
        return if (selectionString.equals("x", ignoreCase = true)) {
            -1
        } else {
            val selectionNumber = selectionString.toIntOrNull() ?: -1
            if (selectionNumber > 0 && selectionNumber <= zineConfig.index.contents.size) {
                selectionNumber
            } else {
                clientSocket.getOutputStream().write(ERROR_INVALID_SELECTION.toByteArray(charset))
                waitForStoryChoice()
            }
        }
    }

    private fun performDisconnect() {
        clientSocket.close()
        onDisconnect(this)
        interrupt()
    }

    private companion object {
        const val ASCII_LF = 10
        const val ERROR_INVALID_SELECTION = "\nPick a story, or X to quit.\n"
    }
}
