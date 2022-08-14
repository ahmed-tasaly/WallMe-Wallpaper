package com.alaory.wallmewallpaper



import android.util.Log
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class wallhaven_api {
    companion object{
        val wallhavenRequest  = OkHttpClient();
        var wallhaven_homepage_posts : Array<List_image> = emptyArray();
        var currentPage: Int = 1;


        fun GethomePagePosts(page: Int = currentPage,callback: () -> Unit = {}){
            val homepagereq = Request.Builder()
                .url("https://wallhaven.cc/api/v1/search?page=$page")
                .build();

            wallhavenRequest.newCall(homepagereq).enqueue(object : Callback{
                lateinit var responseJson : JSONObject;
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("wallhaven_api",e.toString());
                }

                override fun onResponse(call: Call, response: Response) {
                    try {
                        val body = response.body!!.string();
                        Log.i("wallhaven_api",body);
                        responseJson = JSONObject(body);
                        val data = responseJson.getJSONArray("data");

                        for (i in 0 until data.length()) {
                            try {
                                var post: List_image;
                                val postInfo = data.getJSONObject(i);

                                var found = false;
                                for (j in wallhaven_homepage_posts) {
                                    if (j.Image_name == postInfo.getString("id"))
                                        found = true;
                                }
                                if (found)
                                    continue;

                                post = List_image(
                                    postInfo.getString("path"),
                                    postInfo.getJSONObject("thumbs").getString("original"),
                                    postInfo.getString("id"),
                                    "",
                                    "",
                                    postInfo.getString("url")
                                );
                                wallhaven_homepage_posts += post;
                            } catch (e: JSONException) {
                                Log.e("wallhaven_api", e.toString());
                            }
                        }
                        callback();
                    }
                    catch (e: Exception){
                        Log.e("wallhaven_api",e.toString());
                    }
                }

            })
        }

        fun imageInfo(listimage_ref : List_image,callback: () -> Unit = {}){
            val requestImageInfo = Request.Builder()
                .url("https://wallhaven.cc/api/v1/w/${listimage_ref.Image_name}")
                .build();
            wallhavenRequest.newCall(requestImageInfo).enqueue(object : Callback{
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("wallhaven_api",e.toString());
                }

                override fun onResponse(call: Call, response: Response) {
                    try {
                        val body = response.body!!.string();
                        val data = JSONObject(body).getJSONObject("data");
                        listimage_ref.Image_auther = data.getJSONObject("uploader").getString("username");
                        callback();
                    }catch (e: JSONException){
                        Log.e("wallhaven_api",e.toString());
                    }


                }
            })
        }


        fun userPosts(){

        }
        fun TagPosts(){

        }
    }

}