@file:OptIn(ExperimentalSerializationApi::class)

package com.dialazine.server

import com.dialazine.server.models.ZineConfig
import com.dialazine.server.models.ZineIndex
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.net.InetAddress
import java.net.ServerSocket
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

interface TelnetServer {
    fun start()

    /**
     * Stops the server; returns true if the server was stopped
     */
    fun stop(): Boolean

    fun isRunning(): Boolean
}

interface ConnectionListener {
    fun onConnect(inetAddress: InetAddress)
    fun onDisconnect(inetAddress: InetAddress)
}

class TelnetServerImpl(port: Int,
                       indexPath: String,
                       private val issuePath: String,
                       private val connectionListener: ConnectionListener) : TelnetServer {
    private val zineIndex: ZineIndex
    private val server = ServerSocket(port).apply {
        soTimeout = SOCKET_TIMEOUT_MILLIS.toInt()
    }
    private var coordinatorFuture: Future<*>? = null
    private var isRunning = false

    init {
        val indexStream = javaClass.getResourceAsStream(indexPath) ?: throw IllegalStateException("Can't find index")
        zineIndex = Json.decodeFromStream(indexStream)
    }

    override fun start() {
        coordinatorFuture = Executors.newSingleThreadExecutor().submit {
            while (true) {
                // Supports 50 connections in the queue, is this too many?
                val socket = server.accept()
                connectionListener.onConnect(socket.inetAddress)
                ReaderThread(socket, ZineConfig(zineIndex, issuePath), {
                    connectionListener.onDisconnect(socket.inetAddress)
                }).start()
            }
        }
        isRunning = true
    }

    override fun stop(): Boolean {
        isRunning = false
        return coordinatorFuture?.cancel(true) ?: false
    }

    override fun isRunning() = isRunning

    private companion object {
        private const val SOCKET_TIMEOUT_SECONDS = 5L
        private val SOCKET_TIMEOUT_MILLIS = TimeUnit.MINUTES.toMillis(SOCKET_TIMEOUT_SECONDS)
    }
}
