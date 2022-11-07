package com.alaory.wallmewallpaper.interpreter

import android.util.Log
import okhttp3.MediaType
import okhttp3.ResponseBody
import okio.*

class progressRespondBody(val responseBody: ResponseBody,progressListener: progressListener) : ResponseBody() {
    val TAG = "progressRespondBody";


    var buffersource : BufferedSource? = null;
    val progresslistener = progressListener;
    override fun contentLength(): Long {//content size in bytes
        Log.i(TAG,"yeet ${responseBody.contentLength()}")
        return responseBody.contentLength();
    }

    override fun contentType(): MediaType? {
        return responseBody.contentType();
    }

    override fun source(): BufferedSource {//content source
        if(buffersource == null){
            buffersource = source(responseBody.source()).buffer();
        }
        return buffersource!!;
    }

    fun source(source: Source): Source{
        return object : ForwardingSource(source){
            var totalbyteread: Long = 0;
            override fun read(sink: Buffer, byteCount: Long): Long {
                var byteread = super.read(sink, byteCount);
                totalbyteread += if(byteread != (-1).toLong()) byteread else 0;
                progresslistener.Update(totalbyteread,responseBody.contentLength(),byteread == (-1).toLong());
                return byteread;
            }
        }
    }


    interface progressListener{//call back interface
        fun Update(byteread: Long,contentLength : Long,done: Boolean)
    }
}