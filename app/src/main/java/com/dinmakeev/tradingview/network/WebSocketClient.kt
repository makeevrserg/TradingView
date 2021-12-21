package com.dinmakeev.tradingview.network

import android.util.Log
import java.net.URI
import java.nio.ByteBuffer
import javax.websocket.*

@ClientEndpoint
class WebSocketClient(val endpointURI: URI?) {

    companion object {

        val symbolMap: MutableMap<String, WebSocketClient> = mutableMapOf()

        /**
         * Создаем соединение или передаем ссылку на него, если уже существует
         */
        fun createConnection(symbol: String, messageHandler: MessageHandler): WebSocketClient {
            val client = symbolMap[symbol] ?: WebSocketClient()
            client.closeConnection()
            client.openConnection()
            client.sendMessage(getMessage(symbol))
            client.addMessageHandler(messageHandler)
            symbolMap[symbol] = client
            return client
        }

        /**
         * Закрываем соединения для всех котировок, которые присутсвуют в текущем списке
         */
        fun closeConnections(symbol: List<String>) {
            symbolMap.filter { symbol.contains(it.key) }.forEach { (s, client) ->
                client.closeConnection()
                symbolMap.remove(s)
            }
        }

        /**
         * Закрываем соединения для всех котировок, которые не содержатся в текущем переданном листе
         */
        fun closeConnectionsIfNotInList(symbols: List<String>) {
            symbolMap.filter { !symbols.contains(it.key) }
                .forEach { (s, client) ->
                    client.closeConnection()
                    symbolMap.remove(s)
                }
        }

        fun getMessage(symbol: String) =
            "{\"command\": \"subscribe_symbols\", \"payload\": [\"${symbol}\"]}"

        fun resumeAll() {
            symbolMap.forEach { (symbol, client) ->
                client.openConnection()
                client.sendMessage(getMessage(symbol))
            }
        }

        fun pauseAll() {
            symbolMap.forEach { symbol, client -> client.closeConnection() }
        }
    }


    constructor(uri: String = "wss://alex9.fvds.ru:443") : this(URI(uri))

    var userSession: Session? = null
    private var messageHandler: MessageHandler? = null
    var container: WebSocketContainer? = null


    @OnOpen
    fun onOpen(userSession: Session?) {
        println("opening websocket")
        this.userSession = userSession
    }


    @OnClose
    fun onClose(userSession: Session?, reason: CloseReason?) {
        println("closing websocket")
        this.userSession = null
    }


    @OnMessage
    suspend fun onMessage(message: String?) {
        if (messageHandler != null) {
            messageHandler?.handleMessage(message)
        }
    }

    @OnMessage
    fun onMessage(bytes: ByteBuffer?) {
        println("Handle byte buffer")
    }


    fun addMessageHandler(msgHandler: MessageHandler?) {
        messageHandler = msgHandler
    }


    fun sendMessage(message: String?) {
        if (userSession == null) {
            openConnection()
        }
        userSession?.asyncRemote?.sendText(message)
    }


    interface MessageHandler {
        suspend fun handleMessage(message: String?)
    }

    fun closeConnection() {
        userSession?.close()
        container = null
        userSession = null
    }

    fun openConnection() {
        closeConnection()
        try {
            container = ContainerProvider.getWebSocketContainer()
            container?.connectToServer(this, endpointURI)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}