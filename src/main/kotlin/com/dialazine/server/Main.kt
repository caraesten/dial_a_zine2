package com.dialazine.server

import java.net.InetAddress

fun main(args: Array<String>) {
    val basePath = "/example_issue"
    val indexPath = "/example_issue/index.json"
    val port = 8085
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