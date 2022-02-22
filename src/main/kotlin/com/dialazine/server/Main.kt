package com.dialazine.server

import java.net.InetAddress

fun main(args: Array<String>) {
    val basePath = System.getProperty("issueDirectory")
    val indexPath = System.getProperty("indexFile")
    val port = System.getProperty("port").toInt()
    val server = TelnetServerImpl(port, indexPath, basePath, object : ConnectionListener {
        override fun onConnect(inetAddress: InetAddress) {
            println("Received connection")
        }

        override fun onDisconnect(inetAddress: InetAddress) {
            println("Lost connection")
        }
    })

    server.start()
    while (server.isRunning()) {
        // do nothing
    }
}