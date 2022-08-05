package com.wallme.wallpaper

import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener
import java.io.IOException



class Reddit_Api(subreddit: String) {


    companion object{
        var api_key = "NOKEY";
        var time_left = 0;
        var reddit = OkHttpClient();
        var reddit_global_posts : Array<List_image> = emptyArray();


        fun Update_Api_key(callback_update: () -> Unit = {}) {
            Log.i("Reddit_Api", "Function called");
            var Myrequest = Request.Builder()
                .url("https://www.reddit.com/api/v1/access_token?grant_type=https%3A%2F%2Foauth.reddit.com%2Fgrants%2Finstalled_client&device_id=DO_NOT_TRACK_THIS_DEVICE")
                .post(RequestBody.create("application/x-www-form-urlencoded".toMediaTypeOrNull(),"="))
                .addHeader("Authorization", "Basic ${com.wallme.wallpaper.BuildConfig.API_KEY_Base}")
                .addHeader("content-type", "application/x-www-form-urlencoded")
                .build()

            reddit.newCall(Myrequest).enqueue(object : Callback{
                override fun onFailure(call: Call, e: IOException) {
                    Log.i("Reddit_Api","error $e");
                }
                override fun onResponse(call: Call, response: Response) {
                    var respond_json = JSONTokener(response.body!!.string()).nextValue() as JSONObject;
                    api_key = respond_json.getString("access_token");
                    time_left = respond_json.getInt("expires_in");
                    Log.i("Reddit_Api","you got ${api_key} with time $time_left");
                    callback_update();
                }
            });
        }
    }





    fun get_subreddit_posts(callback_update: () -> Unit= {}){
        if(api_key != "NOKEY"){
            Log.i("Reddit_Api", api_key)
            var url: String;

            if(subreddit_posts_list.isNotEmpty())
                url = "https://oauth.reddit.com/r/$subreddit/top?count=10&after=${last_before_id}&t=year";
            else
                url = "https://oauth.reddit.com/r/$subreddit/top";


            var json_req = Request.Builder()
                .url(url)
                .addHeader("Authorization" , "Bearer $api_key")
                .addHeader("content-type", "application/x-www-form-urlencoded")
                .build();




            reddit.newCall(json_req).enqueue(object : Callback{
                lateinit var respond_json: JSONObject;
                override fun onFailure(call: Call, e: IOException) {
                    Log.i("Reddit_Api","error $e");
                }

                override fun onResponse(call: Call, response: Response) {
                    val res = response.body!!.string();
                    Log.i("Reddit_Api", " I got $res");
                    reddit_global_posts = emptyArray();
                    try {
                        respond_json = JSONObject(res);
                        var size = respond_json.getJSONObject("data").getInt("dist");
                        last_before_id = respond_json.getJSONObject("data").getString("before");
                        var children_json = respond_json.getJSONObject("data").getJSONArray("children");
                        var temp_list: Array<List_image> = emptyArray();

                        for (i in 0 until children_json.length()) {
                            if (children_json.getJSONObject(i).getJSONObject("data").getBoolean("over_18"))
                                continue;


                            var found : Boolean = false;
                            for (j in 0 until subreddit_posts_list.size){
                                if(children_json.getJSONObject(i).getJSONObject("data").getString("name") == subreddit_posts_list.get(j).Image_name)
                                    found = true;
                            }
                            if(found)
                                continue;

                            var one_post: List_image = List_image(
                                children_json.getJSONObject(i).getJSONObject("data").getString("url"),
                                children_json.getJSONObject(i).getJSONObject("data").getString("thumbnail"),
                                children_json.getJSONObject(i).getJSONObject("data").getString("name"),
                                children_json.getJSONObject(i).getJSONObject("data").getString("author"),
                                children_json.getJSONObject(i).getJSONObject("data").getString("title")
                            )
                            temp_list += one_post;
                        }
                        if(temp_list.isNotEmpty()){
                            reddit_global_posts += temp_list;
                            subreddit_posts_list += temp_list;
                            callback_update();
                        }
                    }
                    catch (e : JSONException){
                        Log.w("Reddit_Api",e);
                    }

                }
            });
        }

    }




    var subreddit_posts_list : Array<List_image> = emptyArray();
    var last_before_id = "";
    var subreddit = subreddit;
}