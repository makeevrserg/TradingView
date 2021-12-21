package com.dinmakeev.tradingview.network

import com.dinmakeev.tradingview.application.App
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URI
import java.nio.ByteBuffer
import javax.websocket.*

@ClientEndpoint
class WebSocketClient(val endpointURI: URI?) {

    companion object {

        private val cache = mutableListOf<Pair<String,WebSocketClient>>()
        private val cachedList: List<Pair<String,WebSocketClient>>
            get() = synchronized(App) {
                cache
            }

        /**
         * Создаем соединение или передаем ссылку на него, если уже существует
         */
        fun createConnection(symbol: String, messageHandler: MessageHandler): WebSocketClient {
            val client = WebSocketClient()
            client.subscribe(symbol)
            client.addMessageHandler(messageHandler)
            return client
        }




        fun getOrCreateConnection(symbol: String, messageHandler: MessageHandler): WebSocketClient {
            val client = cachedList.firstOrNull { it.first==symbol }?.second
            client?.addMessageHandler(messageHandler)
            return client?: createConnection(symbol, messageHandler)
        }
        suspend fun pauseCached() {
            cachedList.forEach{it.second.closeConnection()}
        }
        suspend fun resumeCached(){
            cachedList.forEach {
                it.second.openConnection()
                it.second.subscribe(it.first)
            }
        }
        suspend fun addToCache(symbol: String?,socket:WebSocketClient?) = synchronized(App){
            cache.add(Pair(symbol?:return@synchronized,socket?:return@synchronized ))
        }
        suspend fun closeCachedConnection(symbol: String?) = synchronized(App){
            val pair= cachedList.firstOrNull { it.first==symbol }?:return@synchronized
            cache.remove(pair)
            pair.second.closeConnection()
        }
        suspend fun clearCached(){
            val list = cachedList.toList()
            cache.clear()
            list.forEach {
                it.second.closeConnection()
            }
        }
        fun getMessage(symbol: String) =
            "{\"command\": \"subscribe_symbols\", \"payload\": [\"${symbol}\"]}"


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
    fun onMessage(message: String?) {
        if (messageHandler != null) {
            CoroutineScope(Dispatchers.IO).launch {
                messageHandler?.handleMessage(message)
            }
        }
    }

    @OnMessage
    fun onMessage(bytes: ByteBuffer?) {
        println("Handle byte buffer")
    }


    fun addMessageHandler(msgHandler: MessageHandler?) {
        messageHandler = msgHandler
    }

    fun subscribe(symbol: String?) {
        sendMessage(getMessage(symbol ?: return))
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