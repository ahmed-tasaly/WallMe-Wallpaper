package com.wallme.wallpaper

import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import org.json.JSONTokener
import java.io.IOException



class Reddit_Api() {
    companion object{
        var timed_api_key = "NOKEY";
        var time_left = 0;
        fun Update_Api_key() {

            Log.i("Reddit_Api", "Function called");
            var MyPost = OkHttpClient();

            var Myrequest = Request.Builder()
                .url("https://www.reddit.com/api/v1/access_token?grant_type=https%3A%2F%2Foauth.reddit.com%2Fgrants%2Finstalled_client&device_id=DO_NOT_TRACK_THIS_DEVICE")
                .post(RequestBody.create("application/x-www-form-urlencoded".toMediaTypeOrNull(),"="))
                .addHeader("Authorization", "Basic ${com.wallme.wallpaper.BuildConfig.API_KEY_Base}")
                .addHeader("content-type", "application/x-www-form-urlencoded")
                .build()

            val repond = MyPost.newCall(Myrequest).enqueue(UpdateKey_callback());
        }
    }

    class UpdateKey_callback : Callback {
        override fun onFailure(call: Call, e: IOException) {
            Log.i("Reddit_Api","error $e");
        }

        override fun onResponse(call: Call, response: Response) {
            var respond_json = JSONTokener(response.body!!.string()).nextValue() as JSONObject;
            timed_api_key = respond_json.getString("access_token");
            time_left = respond_json.getInt("expires_in");
            Log.i("Reddit_Api","you got ${timed_api_key} with time $time_left");
        }

    }





}