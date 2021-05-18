package com.bpmlinks.vbank.helper.coroutines

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/*Coroutine dispatcher that determines what thread or threads the corresponding coroutine uses for its execution*/
interface DispatcherProvider {

    fun main(): CoroutineDispatcher = Dispatchers.Main

    fun default(): CoroutineDispatcher = Dispatchers.Default
    fun io(): CoroutineDispatcher = Dispatchers.IO
    fun unconfined(): CoroutineDispatcher = Dispatchers.Unconfined

}

class DefaultDispatcherProvider : DispatcherProvider