package com.dinmakeev.tradingview.utils


/**
 * После сворачивания/разворачивания приложений observer моежет заново обсервнуть LiveData.
 * Чтобы этого не происходило - для одноразовых сообщений используется этот класс.
 */
class MessageHandler<T>(private val content: T?) {

    /**
     * При создании класса эвент считаетсся включенным
     */
    private var isEnabled = true

    /**
     * Получаем контент во фрагменте, созданный в ViewModel
     * После вызова функии
     */
    fun getContent(): T? {
        if (!isEnabled)
            return null
        isEnabled = false
        return content
    }

}