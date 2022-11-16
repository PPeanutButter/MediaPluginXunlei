package com.peanut.xunleivpn

import android.content.Context
import android.os.Handler
import android.widget.Toast
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.regex.Pattern

object Cache {
    val task = mutableListOf<String>()
    val taskName = mutableStateListOf<String>()
    var callback :((String) -> Unit)? = null
    private val client = OkHttpClient()

    fun get(k:String) = SettingManager.getValue(k, "")

    fun clearTask(){
        task.clear()
        taskName.clear()
    }

    fun forEachTask(func:(String, String)->Unit){
        for (i in 0 until task.size){
            func.invoke(taskName[i], task[i])
        }
    }

    fun send(url: String, name: String, context: Context){
        try {
            val request: Request = Request.Builder()
                .url(
                    "http://${
                        SettingManager.getValue(
                            "ip",
                            ""
                        )
                    }/remote_download"
                )
                .post(
                    FormBody.Builder()
                        .add("out", name)
                        .add("url", url)
                        .build()
                )
                .build()
            val res = client.newCall(request).execute()
            if (res.code != 200){
                Handler(context.mainLooper).post {
                    Toast.makeText(
                        context,
                        res.body?.string(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }else {
                Handler(context.mainLooper).post {
                    Toast.makeText(
                        context,
                        name,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Handler(context.mainLooper).post {
                Toast.makeText(
                    context,
                    e.localizedMessage,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}