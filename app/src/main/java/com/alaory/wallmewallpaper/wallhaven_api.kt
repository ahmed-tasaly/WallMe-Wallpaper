package com.alaory.wallmewallpaper



import android.util.Log
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class wallhaven_api {
    companion object{
        val wallhavenRequest  = OkHttpClient();
        var wallhaven_homepage_posts : MutableList<List_image> = emptyList<List_image>().toMutableList();
        var currentPage: Int = 1;


        fun GethomePagePosts(sorting: String = "&sorting=favorites",ordering:String = "&order=desc" ,callback: () -> Unit = {}){
            val homepagereq = Request.Builder()
                .url("https://wallhaven.cc/api/v1/search?page=$currentPage${sorting}${ordering}&q=+nature+landscape-sky&categories=010")
                .build();

            wallhavenRequest.newCall(homepagereq).enqueue(object : Callback{

                override fun onFailure(call: Call, e: IOException) {
                    Log.e("wallhaven_api",e.toString());
                }

                override fun onResponse(call: Call, response: Response) {
                    try {
                        val body = response.body!!.string();
                        Log.i("wallhaven_api",body);
                        var responseJson = JSONObject(body);
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
                        if(data.length() > 0){
                            callback();
                            currentPage++;
                        }

                    }
                    catch (e: Exception){
                        Log.e("wallhaven_api",e.toString());
                    }
                }

            })
        }

        //request tag page
        fun TagPosts(tag: Tag,sorting: String = "&sorting=views",ordering:String = "&order=desc" ,callback: () -> Unit = {}){
            var url = "https://wallhaven.cc/api/v1/search?page=${tag.Page_Tag}${tag.Name_Tag}${sorting}${ordering}";
            url = url.replace(" ","%20")
            var tagPosts_request = Request.Builder()
                .url(url)
                .build();
            Log.i("wallhaven_api","TagPosts URL: $url")
            wallhavenRequest.newCall(tagPosts_request).enqueue(object : Callback{

                override fun onFailure(call: Call, e: IOException) {
                    Log.e("wallhaven_api","Tag request failed: $e")
                }

                override fun onResponse(call: Call, response: Response) {
                    try {
                        val body = response.body!!.string();
                        Log.i("wallhaven_api",body);
                        var responseJson  = JSONObject(body);
                        val data = responseJson.getJSONArray("data");
                        for (i in 0 until data.length()) {
                            var post: List_image;
                            val postInfo = data.getJSONObject(i);

                            var found = false;
                            for (j in tag.Tag_Post_list) {
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
                            tag.Tag_Post_list += post;
                        }
                        if(data.length() > 0){
                            tag.Page_Tag++;
                            callback();
                        }
                    }catch (e:JSONException){
                        Log.e("wallhaven_api","Tag error: $e");
                    }
                }

            })
        }






        //add image info to post
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
                        try {
                            Image_Activity.TagNameList = emptyArray();
                            val TagsJson = data.getJSONArray("tags");
                            for(i in 0 until TagsJson.length())
                                Image_Activity.TagNameList += TagsJson.getJSONObject(i).getString("name");

                        }catch (e : JSONException){
                            Log.e("wallhaven_api","Tag error ${e.toString()}")
                        }

                        callback();
                    }catch (e: JSONException){
                        Log.e("wallhaven_api",e.toString());
                    }


                }
            })
        }


        fun userPosts(){

        }

    }

    data class Tag(
        var Name_Tag: String,
        var Page_Tag: Int=1,
        var Tag_Post_list: MutableList<List_image> = emptyList<List_image>().toMutableList()
    )

}