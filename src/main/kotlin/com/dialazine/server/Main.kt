package com.dialazine.server

import java.net.InetAddress
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

fun main(args: Array<String>) {
    val basePath = System.getProperty("issueDirectory")
    val indexPath = System.getProperty("indexFile")
    val port = System.getProperty("port").toInt()
    val logFile = System.getProperty("logFile")
    val timeFormatter = DateTimeFormatter.ISO_ZONED_DATE_TIME
    val server = TelnetServerImpl(port, indexPath, basePath, logFile, object : ConnectionListener {
        override fun onConnect(inetAddress: InetAddress) {
            println("Received connection at: ${ZonedDateTime.now().format(timeFormatter)}")
        }

        override fun onDisconnect(inetAddress: InetAddress, reason: DisconnectReason) {
            println("Lost connection at: ${ZonedDateTime.now().format(timeFormatter)} due to: $reason")
        }
    })

    server.start()
    while (server.isRunning()) {
        // do nothing
    }
}