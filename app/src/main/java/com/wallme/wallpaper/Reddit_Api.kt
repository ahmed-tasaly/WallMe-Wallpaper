package com.wallme.wallpaper

import android.util.Log
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result

class Reddit_Api {
    fun call_api(){
        var redditAsync = "https://www.reddit.com/r/wallpaper/hot".httpGet().responseString{ req,resp,resu -> {
            when (resu) {
                is Result.Failure -> {
                    Log.i("Reddit_Api","Error");
                }
                is Result.Success -> {
                    Log.i("Reddit_Api",resp.responseMessage);
                }
            }

        }};
        redditAsync.join();
    }

}