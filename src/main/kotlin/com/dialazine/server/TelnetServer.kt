@file:OptIn(ExperimentalSerializationApi::class)

package com.dialazine.server

import com.dialazine.server.models.ZineConfig
import com.dialazine.server.models.ZineIndex
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.*
import java.io.File
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
                       private val logFilePath: String,
                       private val connectionListener: ConnectionListener) : TelnetServer {
    private val activeConnectionDeque = java.util.ArrayDeque<ReaderConnection>()
    private val zineIndex: ZineIndex
    private val logWriterThread = Executors.newSingleThreadExecutor()
    private val cleanupThread = Executors.newSingleThreadExecutor()
    private val server = ServerSocket(port).apply {
        soTimeout = 0 // block on .accept till a connection comes in; never time out!
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
                socket.soTimeout = SOCKET_TIMEOUT_MILLIS.toInt()
                activeConnectionDeque.add(ReaderThread(socket, ZineConfig(zineIndex, issuePath), {
                    connectionListener.onDisconnect(socket.inetAddress)
                    cleanupThread.submit {
                        activeConnectionDeque.remove(it)
                        tidyUpConnections()
                    }
                }).apply { start() })

                connectionListener.onConnect(socket.inetAddress)

                cleanupThread.submit {
                    tidyUpConnections()
                }
            }
        }
        isRunning = true
    }

    override fun stop(): Boolean {
        isRunning = false
        return coordinatorFuture?.cancel(true) ?: false
    }

    override fun isRunning() = isRunning

    private fun tidyUpConnections() {
        val deadConnections = activeConnectionDeque.filter {
            System.currentTimeMillis() - it.startTime > OVERALL_TIMEOUT_MILLIS
        }.toSet()
        activeConnectionDeque.removeAll(deadConnections)
        deadConnections.forEach {
            it.forceDisconnect()
        }
        writeConnectionLog()
    }

    private fun writeConnectionLog() {
        logWriterThread.execute {
            val json = buildJsonObject {
                put("activeConnections", activeConnectionDeque.size)
            }
            try {
                File(logFilePath).writeText(json.toString())
            } catch (throwable: Throwable) {
                println("Cannot write to log file")
                println("Connections: ${activeConnectionDeque.size}")
            }
        }
    }

    private companion object {
        private const val SOCKET_TIMEOUT_MINUTES = 5L
        private val SOCKET_TIMEOUT_MILLIS = TimeUnit.MINUTES.toMillis(SOCKET_TIMEOUT_MINUTES)
        // Any session over this length gets kicked regardless of whether it's active
        private const val OVERALL_TIMEOUT_MINUTES = 60L
        private val OVERALL_TIMEOUT_MILLIS = TimeUnit.MINUTES.toMillis(OVERALL_TIMEOUT_MINUTES)
    }
}
