package com.alaory.wallmewallpaper.api

import android.util.Log
import com.alaory.wallmewallpaper.*
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener
import java.io.IOException


class Reddit_Api_Contorller() {
    //reddit rquest token
    var api_key = "NOKEY";

    //number of posts to load
    var PostRequestNumber = 25;

    //token time left
    var time_left = 0;

    //okhttp client
    var reddit = OkHttpClient();

    //list of all subreddits posts
    var reddit_global_posts: MutableList<Image_Info> = emptyList<Image_Info>().toMutableList();

    //list of subreddits that have been made
    var Subreddits: Array<Reddit_Api> = emptyArray();

    //global post last index list
    var last_index: Int = 0;

    //image preview quality
    var previewQulaity: Int = 1 // from 0 to 5

    //list mode
    var listMode = "Top";

    //time period
    var timeperiod = "&t=all";


    val TAG = "Reddit_Api";


    fun Update_Api_key(callback_update: () -> Unit = {}) {
        Log.i("Reddit_Api", "Function called");
        try {
            if (BuildConfig.API_KEY_Base.trim() == "") {
                callback_update();
                return;
            }
        } catch (e: Exception) {
            callback_update();
            return;
        }

        val Myrequest = Request.Builder()
            .url("https://www.reddit.com/api/v1/access_token?grant_type=https%3A%2F%2Foauth.reddit.com%2Fgrants%2Finstalled_client&device_id=DO_NOT_TRACK_THIS_DEVICE")
            .post(RequestBody.create("application/x-www-form-urlencoded".toMediaTypeOrNull(), "="))
            .addHeader("Authorization", "Basic ${BuildConfig.API_KEY_Base}")
            .addHeader("content-type", "application/x-www-form-urlencoded")
            .build()

        reddit.newCall(Myrequest).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.i("Reddit_Api", "TOKEN error $e");
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val respond_json =
                        JSONTokener(response.body!!.string()).nextValue() as JSONObject;
                    api_key = respond_json.getString("access_token");
                    time_left = respond_json.getInt("expires_in");
                    Log.i("Reddit_Api", "you got $api_key with time $time_left");
                    callback_update();
                } catch (e: Exception) {
                    Log.e(TAG, "TOKEN FAILED: ${e.toString()}");
                    callback_update();
                }

            }
        });


    }


    fun get_allposts_andGive(callback_update: (Status: Int) -> Unit = {}) {
        for (subreddit in Subreddits) {

            subreddit.listMode = listMode;
            subreddit.api_key = api_key;
            subreddit.reddit = reddit;
            subreddit.previewQulaity = previewQulaity;
            subreddit.timeperiod = timeperiod;
            subreddit.PostRequestNumber = PostRequestNumber;


            //loop threw all subreddits

            subreddit.get_subreddit_posts { posts, Status ->
                if(Status == 400){
                    //try again
                    subreddit.get_subreddit_posts { Posts, status ->
                        reddit_global_posts += Posts;
                        last_index = reddit_global_posts.size;
                        callback_update(status);
                    }
                }else{
                    reddit_global_posts += posts;
                    last_index = reddit_global_posts.size;
                    callback_update(Status);
                }
            }
        }

    }


    fun search_subreddits(query: String, callback: (listnames: Array<String>) -> Unit) {
        var baseurl = "https://www.reddit.com/search.json?q=$query&type=sr";

        var searchRequest: Request = Request.Builder()
            .tag("SubredditsList")
            .url(baseurl)
            .build();



        for (call in reddit.dispatcher.runningCalls()) {
            call.request().tag()?.let {
                if (it.equals("SubredditsList")) {
                    call.cancel();
                    Log.e("SubredditsList", "cancles")
                }
            }
        }

        reddit.newCall(searchRequest).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, e.toString());
            }

            override fun onResponse(call: Call, response: Response) {
                var subredditsNames: Array<String> = emptyArray();
                try {
                    val subredditlist = JSONObject(response.body!!.string()).getJSONObject("data")
                        .getJSONArray("children");
                    for (i in 0 until subredditlist.length()) {
                        val displayname = subredditlist.getJSONObject(i).getJSONObject("data")
                            .getString("display_name").lowercase();
                        if (Reddit_Api.filter_words(displayname) || subredditlist.getJSONObject(i)
                                .getJSONObject("data").optBoolean("over18", true) || Reddit_Api.ban_subreddits(displayname.lowercase())
                        )
                            continue;
                        subredditsNames += subredditlist.getJSONObject(i).getJSONObject("data")
                            .getString("display_name");
                    }

                    callback(subredditsNames);
                } catch (e: Exception) {
                    Log.e(TAG, e.toString());
                }
            }
        })
    }
}





class Reddit_Api(subredditname: String) {


    companion object{
        var redditcon :  Reddit_Api_Contorller? = null;
        var prefswords = "";

        //show fav
        var showfav = true;

        fun filter_words(word : String): Boolean{
            val word = word.lowercase();
            val filterWords: Array<String> = arrayOf("hentai","horny","bitch","fap","shit","cursed","ass","semen","porn","cum","nud","fuck","pornhub","dick","blowjob","pussy","cunt","nsfw","adult","gender","gay","demon","summon","cross","bible","chris","lgbt","gods","lgb","sex","rainbow","pride","furry","jerk","waifu","deadbedrooms","askwomen","cuteanime","animegirls","unpopularopinion","funny","nun","girl","women","lady","conspiracy","wtf","TwoXChromosomes","exmormon","todayilearned","tooafraidtoask","4chan","worldnews","askmen","mensrights","atheism","jokes","news","teenagers","showerthoughts","politics","relationship_advice","askreddit","relationships")

            for(i in filterWords)
                if(word.contains(i))
                    return true

            return false;
        }

        fun ban_subreddits(subname : String): Boolean{
            val word = subname.lowercase();
            val filterWords: Array<String> = arrayOf("animewallpaper","animewallpapers");

            for(i in filterWords)
                if(word.lowercase() == (i.lowercase()))
                    return true

            for(i in prefswords.split(","))
                if(word.lowercase() == i.lowercase())
                    return true


            return false;
        }
    }

    //list mode
    var listMode = "Top";
    //time period
    var timeperiod = "&t=all";
    //reddit rquest token
    var api_key = "NOKEY";
    //number of posts to load
    var PostRequestNumber = 25;
    //okhttp client
    var reddit = OkHttpClient();
    //image preview quality
    var previewQulaity: Int = 1 // from 0 to 5

    var subreddit_posts_list : Array<Image_Info> = emptyArray();
    var last_before_id = "";
    var subreddit = subredditname;

    init {
        if (redditcon != null)
            redditcon!!.Subreddits += this;
        else
            Log.e(this::class.java.simpleName,"Subreddit ${this.subreddit} not added");
    }



    fun get_subreddit_posts(callback_update: (list_data : MutableList<Image_Info>, Status : Int) -> Unit= { _, _->}){

            Log.i("Reddit_Api", api_key)
            val url: String;

            if(api_key == "NOKEY"){
                url = "https://reddit.com/r/$subreddit/${listMode.lowercase()}.json?limit=$PostRequestNumber${ if(last_before_id.isNotEmpty()) "&after=${last_before_id}" else ""}${timeperiod.lowercase()}";
            }else{
                url = "https://oauth.reddit.com/r/$subreddit/${listMode.lowercase()}?limit=$PostRequestNumber${ if(last_before_id.isNotEmpty()) "&after=${last_before_id}" else ""}${timeperiod.lowercase()}";
            }

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
                    callback_update(emptyList<Image_Info>().toMutableList(),400);
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
                        var temp_list: MutableList<Image_Info> = emptyList<Image_Info>().toMutableList();

                        for (i in 0 until children_json.length()) {
                            try {
                                var dataJson = children_json.getJSONObject(i)
                                    .getJSONObject("data") as JSONObject;

                                // check if worth adding


                                var found: Boolean = false;
                                last_before_id = dataJson.getString("name");

                                //check if its an embedded  from a diff subreddit
                                if(dataJson.optJSONArray("crosspost_parent_list") != null){
                                    Log.d("embedded",dataJson.getJSONArray("crosspost_parent_list").length().toString())
                                    if(dataJson.getJSONArray("crosspost_parent_list").length() > 0)
                                        dataJson = dataJson.getJSONArray("crosspost_parent_list").getJSONObject(0);
                                }


                                val posturl = dataJson.getString("url");

                                for (j in 0 until subreddit_posts_list.size) {
                                    if (posturl == subreddit_posts_list.get(j).Image_url)
                                        found = true;
                                }
                                for (p in database.imageblock_list) {
                                    if (posturl == p.Image_url)
                                        found = true;
                                }



                                if (dataJson.getBoolean("over_18") || found)
                                    continue;

                                //check if its a valid post that contains a wallpaper

                                val lastchars = posturl.reversed().substring(0,5);
                                val is_thumbnail_notvalid = dataJson.getString("thumbnail").isNullOrEmpty()
                                val is_media_notvalid =  dataJson.getString("media") == "null"
                                val is_media_metadata_notvalid : JSONObject? = dataJson.optJSONObject("media_metadata");
                                if((!lastchars.contains('.') && is_thumbnail_notvalid  && is_media_notvalid && is_media_metadata_notvalid == null) || lastchars.get(0) == '/')
                                    continue;

                                var isfavored_skip = false;
                                if(!showfav){
                                    for(post in database.imageinfo_list){
                                        if(post.Image_url == posturl){
                                            isfavored_skip = true;
                                            break;
                                        }
                                    }
                                }
                                if(isfavored_skip)
                                    continue;
                                //----------------------------------------------
                                //post is worth adding




                                //add the image
                                //get image name to skip it next time
                                if (filter_words(dataJson.getString("title")))
                                    continue;


                                var type = UrlType.Image;

                                if(dataJson.optBoolean("is_video", false))
                                    type = UrlType.Video;

                                if(dataJson.optString("url","").lowercase().contains(".gif"))
                                    type = UrlType.Gif;

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



                                            val ImageRatio = Image_Ratio(
                                                current_metadata.getJSONArray("p")
                                                .getJSONObject(previewQulaity).getInt("x"),
                                                current_metadata.getJSONArray("p")
                                                    .getJSONObject(previewQulaity).getInt("y"))

                                            val postImageUrl = current_metadata.getJSONObject("s").getString("u")
                                                .replace("amp;", "");

                                            var foundgallery = false;
                                            for (p in database.imageblock_list) {
                                                if (postImageUrl == p.Image_url)
                                                    foundgallery = true;
                                            }
                                            if(foundgallery){
                                                continue;//skip image
                                            }

                                            val imageInfo_gallery: Image_Info = Image_Info(
                                                postImageUrl,
                                                current_metadata.getJSONArray("p")
                                                    .getJSONObject(previewQulaity).getString("u")
                                                    .replace("amp;", ""),
                                                dataJson.getString("name"),
                                                dataJson.getString("author"),
                                                dataJson.getString("title"),
                                                "reddit.com${dataJson.getString("permalink")}",
                                                ImageRatio
                                            );
                                            temp_list.add(imageInfo_gallery);

                                        } catch (e: JSONException) {
                                            Log.e("Reddit_Api", "gallary error: ${e.toString()}, Url is: $url")
                                        }

                                    }
                                    continue;
                                }

                                var source_url = dataJson.getString("url");
                                if(type == UrlType.Video){
                                    source_url = dataJson.getJSONObject("secure_media").getJSONObject("reddit_video").getString("fallback_url").replace("?source=fallback","");
                                }else if(type == UrlType.Gif){
                                    source_url = dataJson.getString("url");
                                    if(source_url.last() == 'v'){
                                        source_url = source_url.removeRange(source_url.lastIndex,source_url.lastIndex+1);
                                    }
                                }

                                val one_post: Image_Info;
                                //post doesn't have a preview
                                if (dataJson.optString("preview").isNullOrBlank()) {
                                    one_post = Image_Info(
                                        source_url,
                                        source_url,
                                        dataJson.getString("name"),
                                        dataJson.getString("author"),
                                        dataJson.getString("title"),
                                        "reddit.com${dataJson.getString("permalink")}",
                                        Image_Ratio(1,1),
                                        type
                                    )

                                }
                                //post does have a preview
                                else {
                                    //get image preview from list
                                    val image_preview_url : String =
                                        when(type){
                                            UrlType.Gif -> {
                                                try{
                                                    dataJson
                                                        .getJSONObject("preview")
                                                        .getJSONArray("images")
                                                        .getJSONObject(0)
                                                        .getJSONObject("variants")
                                                        .getJSONObject("gif")
                                                        .getJSONArray("resolutions")
                                                        .getJSONObject(previewQulaity)
                                                        .getString("url").replace("amp;", "");

                                                }catch (e : Exception){
                                                    dataJson
                                                        .getJSONObject("preview").getJSONArray("images")
                                                        .getJSONObject(0)
                                                        .getJSONArray("resolutions").getJSONObject(previewQulaity)
                                                        .getString("url").replace("amp;", "")
                                                }

                                            }
                                            else -> {
                                                dataJson
                                                    .getJSONObject("preview").getJSONArray("images")
                                                    .getJSONObject(0)
                                                    .getJSONArray("resolutions").getJSONObject(previewQulaity)
                                                    .getString("url").replace("amp;", "")
                                            }
                                        };

                                    //get image Ratio from list
                                    val imageRatio = Image_Ratio(
                                        //width
                                        dataJson
                                        .getJSONObject("preview").getJSONArray("images")
                                        .getJSONObject(0)
                                        .getJSONArray("resolutions").getJSONObject(previewQulaity)
                                        .getInt("width"),
                                        //height
                                        dataJson
                                            .getJSONObject("preview").getJSONArray("images")
                                            .getJSONObject(0)
                                            .getJSONArray("resolutions").getJSONObject(
                                                previewQulaity
                                            )
                                            .getInt("height")
                                    );


                                    //parse json into data to use
                                    one_post = Image_Info(
                                        source_url,
                                        image_preview_url,
                                        dataJson.getString("name"),
                                        dataJson.getString("author"),
                                        dataJson.getString("title"),
                                        "reddit.com${dataJson.getString("permalink")}",
                                        imageRatio,
                                        type
                                    )
                                }




                                temp_list.add(one_post);
                            }
                            catch (e:Exception ){
                                Log.d("Reddit_Api","Reddit_Api getting one post error")
                            }
                        }
                        if(temp_list.isNotEmpty()){
                            subreddit_posts_list += temp_list;
                            callback_update(temp_list,200);
                            temp_list.clear();
                        }else{

                            callback_update(emptyList<Image_Info>().toMutableList(),400);
                        }
                    }
                    catch (e : JSONException){
                        callback_update(emptyList<Image_Info>().toMutableList(),400);
                        Log.e("Reddit_Api", e.toString());
                    }

                }
            });

    }




}