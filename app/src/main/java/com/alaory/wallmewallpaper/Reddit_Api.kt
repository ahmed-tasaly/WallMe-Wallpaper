package com.alaory.wallmewallpaper

import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener
import java.io.IOException



class Reddit_Api(subredditname: String) {


    companion object{
        //reddit rquest token
        var api_key = "NOKEY";
        //token time left
        var time_left = 0;
        //okhttp client
        var reddit = OkHttpClient();
        //list of all subreddits posts
        var reddit_global_posts : MutableList<List_image> = emptyList<List_image>().toMutableList();
        //list of subreddits that have been made
        var Subreddits : Array<Reddit_Api> = emptyArray();
        //global post last index list
        var last_index : Int = 0;
        //image preview quality
        var previewQulaity: Int = 1 // from 0 to 5
        //list mode
        var listMode = "Hot";
        //time period
        var timeperiod = "&t=year";


        fun Update_Api_key(callback_update: () -> Unit = {}) {
            Log.i("Reddit_Api", "Function called");
            val Myrequest = Request.Builder()
                .url("https://www.reddit.com/api/v1/access_token?grant_type=https%3A%2F%2Foauth.reddit.com%2Fgrants%2Finstalled_client&device_id=DO_NOT_TRACK_THIS_DEVICE")
                .post(RequestBody.create("application/x-www-form-urlencoded".toMediaTypeOrNull(),"="))
                .addHeader("Authorization", "Basic ${com.alaory.wallmewallpaper.BuildConfig.API_KEY_Base}")
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


        fun get_shuffle_andGive(callback_update: () -> Unit = {}){
            var temp_array_of_posts: Array<List_image> = emptyArray();

            for (subreddit in 0 until Subreddits.size){
                Subreddits.get(subreddit).get_subreddit_posts{ posts ->

                    temp_array_of_posts += posts;

                    if(subreddit == Subreddits.lastIndex){
                        Log.i("Reddit_Api","temp_array_of_posts size is ${temp_array_of_posts.size}")
                        temp_array_of_posts.shuffle();
                        last_index = reddit_global_posts.size
                        reddit_global_posts += temp_array_of_posts;
                        callback_update();
                    }
                }
            }
        }

    }





    fun get_subreddit_posts(callback_update: (list_data : Array<List_image>) -> Unit= {}){
        if(api_key != "NOKEY"){
            Log.i("Reddit_Api", api_key)
            val url: String;


            if(subreddit_posts_list.isNotEmpty())
                url = "https://oauth.reddit.com/r/$subreddit/${listMode.lowercase()}?count=25&after=${last_before_id}${timeperiod.lowercase()}";
            else
                url = "https://oauth.reddit.com/r/$subreddit/${listMode.lowercase()}?limit=25${timeperiod.lowercase()}";


            Log.i("Reddit_Api",url);


            val json_req = Request.Builder()
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
                    try {
                        //parse json
                        respond_json = JSONObject(res);

                        var size = respond_json.getJSONObject("data").getInt("dist");

                        val children_json = respond_json.getJSONObject("data").getJSONArray("children");
                        //----------------------
                        // temp array to add data to
                        var temp_list: Array<List_image> = emptyArray();

                        for (i in 0 until children_json.length()) {
                            try {
                                val dataJson = children_json.getJSONObject(i)
                                    .getJSONObject("data") as JSONObject;

                                // check if worth adding
                                var found: Boolean = false;

                                for (j in 0 until subreddit_posts_list.size) {
                                    if (dataJson.getString("name") == subreddit_posts_list.get(j).Image_name)
                                        found = true;
                                }

                                if (dataJson.getBoolean("over_18") || found || dataJson.optBoolean(
                                        "is_video",
                                        false
                                    )
                                )
                                    continue;
                                //----------------------------------------------


                                //add the image
                                //get image name to skip it next time
                                last_before_id = dataJson.getString("name");

                                //check again if its an image
                                if (dataJson.getString("thumbnail") == "self" || dataJson.getString(
                                        "thumbnail"
                                    ) == "default"
                                )
                                    continue;


                                //parse image gallery post
                                if (dataJson.optBoolean("is_gallery", false)) {
                                    val gallery_images_name = dataJson.getJSONObject("gallery_data")
                                        .getJSONArray("items");

                                    for (i in 0 until gallery_images_name.length()) {
                                        try {
                                            val current_metadata =
                                                dataJson.getJSONObject("media_metadata")
                                                    .getJSONObject(
                                                        gallery_images_name.getJSONObject(i)
                                                            .getString("media_id")
                                                    );
                                            Log.i("Reddit_Api", "Gallery found")
                                            val list_image_gallery: List_image = List_image(
                                                current_metadata.getJSONObject("s").getString("u")
                                                    .replace("amp;", ""),
                                                current_metadata.getJSONArray("p")
                                                    .getJSONObject(previewQulaity).getString("u")
                                                    .replace("amp;", ""),
                                                dataJson.getString("name"),
                                                dataJson.getString("author"),
                                                dataJson.getString("title"),
                                                "reddit.com${dataJson.getString("permalink")}"
                                            );
                                            temp_list += list_image_gallery;
                                        } catch (e: JSONException) {
                                            Log.e("Reddit_Api", "gallary error: ${e.toString()}, Url is: $url")
                                        }

                                    }
                                    continue;
                                }

                                val one_post: List_image;
                                if (dataJson.optString("preview").isNullOrBlank()) {
                                    one_post = List_image(
                                        dataJson.getString("url"),
                                        dataJson.getString("url"),
                                        dataJson.getString("name"),
                                        dataJson.getString("author"),
                                        dataJson.getString("title"),
                                        "reddit.com${dataJson.getString("permalink")}"
                                    )

                                } else {
                                    val image_source_url = dataJson
                                        .getJSONObject("preview").getJSONArray("images")
                                        .getJSONObject(0)
                                        .getJSONObject("source").getString("url")
                                        .replace("amp;", "");

                                    val image_preview_url = dataJson
                                        .getJSONObject("preview").getJSONArray("images")
                                        .getJSONObject(0)
                                        .getJSONArray("resolutions").getJSONObject(previewQulaity)
                                        .getString("url").replace("amp;", "");
                                    //parse json into data to use
                                    one_post = List_image(
                                        image_source_url,
                                        image_preview_url,
                                        dataJson.getString("name"),
                                        dataJson.getString("author"),
                                        dataJson.getString("title"),
                                        "reddit.com${dataJson.getString("permalink")}"
                                    )
                                }




                                temp_list += one_post;
                                //----------------------------------
                            }
                            catch (e:Exception ){
                                Log.i("Reddit_Api","Reddit_Api for loop erorr")
                            }
                        }

                        if(temp_list.isNotEmpty()){
                            subreddit_posts_list += temp_list;

                            callback_update(temp_list);
                        }
                    }
                    catch (e : JSONException){
                        Log.e("Reddit_Api", e.toString());
                    }

                }
            });
        }

    }

    init {
        Subreddits += this;
    }


    var subreddit_posts_list : Array<List_image> = emptyArray();
    var last_before_id = "";
    var subreddit = subredditname;
}