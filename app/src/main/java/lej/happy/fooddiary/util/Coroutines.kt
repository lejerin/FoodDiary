package lej.happy.fooddiary.util

import android.os.Handler
import kotlinx.coroutines.*

object Coroutines {

    fun<T: Any> ioThenMain(work: suspend (() -> T?), callback: ((T?) -> Unit)) =

        CoroutineScope(Job() + Dispatchers.Main).launch {
            val data = CoroutineScope(Dispatchers.Default).async rt@{
                return@rt work()
            }.await()
            callback(data)
        }


}