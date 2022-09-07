package com.alaory.wallmewallpaper.interpreter

import android.util.Log
import okhttp3.MediaType
import okhttp3.ResponseBody
import okio.BufferedSource
import okio.Okio

class progressRespondBody(val responseBody: ResponseBody) : ResponseBody() {
    val TAG = "progressRespondBody";
    var buffersource : BufferedSource? = null;
    override fun contentLength(): Long {
        Log.i(TAG,"yeet ${responseBody.contentLength()}")
        return responseBody.contentLength();
    }

    override fun contentType(): MediaType? {

        return responseBody.contentType();
    }

    override fun source(): BufferedSource {
        if(buffersource == null){
            buffersource = responseBody.source();
        }
        Log.i(TAG,"yeet ${responseBody.contentLength().toFloat()/1000}kb")
        return buffersource!!;
    }
    interface progressListener{
        fun Update(byteread: Long,contentLength : Long,done: Boolean)
    }
}