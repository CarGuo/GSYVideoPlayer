package com.example.gsyvideoplayer

import android.content.Context
import android.content.res.AssetManager
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

object GetJsonDataUtil {

    /**
     * 读取本地的JSON 数据
     *
     * @param context
     * @param fileName
     * @return
     */
    fun getJson(context: Context, fileName: String): String {
        val stringBuffer = StringBuffer()
        try {
            val assetsManager: AssetManager = context.assets
            val bufferedReader = BufferedReader(InputStreamReader(assetsManager.open(fileName)))
            var line: String?
            while (bufferedReader.readLine().also { line = it } != null) {
                stringBuffer.append(line)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return stringBuffer.toString()
    }

}
