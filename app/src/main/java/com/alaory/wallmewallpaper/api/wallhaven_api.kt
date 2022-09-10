package com.alaory.wallmewallpaper.api



import android.util.Log
import com.alaory.wallmewallpaper.Image_Activity
import com.alaory.wallmewallpaper.Image_Info
import com.alaory.wallmewallpaper.Image_Ratio
import com.alaory.wallmewallpaper.settings.wallhaven_settings
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class wallhaven_api {
    companion object{
        val wallhavenRequest  = OkHttpClient();


        var wallhaven_homepage_posts : MutableList<Image_Info> = emptyList<Image_Info>().toMutableList();
        var lastindex: Int = 0;
        var currentPage: Int = 1;

        var sorting : String = "&sorting=favorites";
        var ordering : String = "&order=desc";
        var ratio : String = "";
        var categories : String = "&categories=010"
        var timeperiod: String = "";


        fun GethomePagePosts(callback: () -> Unit = {},onfailercallback: () -> Unit = {}){
            var Tags_String = "&q=";
            try{
                for(i in wallhaven_settings.TagsSequnce){
                    Tags_String += i;
                }
            }catch (e:Exception){
                Log.e("wallhaven_api",e.toString())
            }


            val url_homepage = "https://wallhaven.cc/api/v1/search?page=${currentPage}&purity=100$sorting$ratio$ordering$timeperiod$categories${if(Tags_String != "&q=")Tags_String else ""}";

            val homepagereq = Request.Builder()
                .url(url_homepage)
                .build();

            Log.i("wallhaven_api","Home page Url $url_homepage")
            wallhavenRequest.newCall(homepagereq).enqueue(object : Callback{

                override fun onFailure(call: Call, e: IOException) {
                    Log.e("wallhaven_api",e.toString());
                    onfailercallback();
                }

                override fun onResponse(call: Call, response: Response) {
                    try {
                        val body = response.body!!.string();

                        val responseJson = JSONObject(body);
                        val data = responseJson.getJSONArray("data");

                        Log.i("wallhaven_api",body);

                        var TempList : Array<Image_Info> = emptyArray();

                        for (i in 0 until data.length()) {
                            try {
                                var post: Image_Info;
                                val postInfo = data.getJSONObject(i);

                                var found = false;
                                for (j in wallhaven_homepage_posts) {
                                    if (j.Image_name == postInfo.getString("id"))
                                        found = true;
                                }
                                if (found)
                                    continue;

                                post = Image_Info(
                                    postInfo.getString("path"),
                                    postInfo.getJSONObject("thumbs").getString("original"),
                                    postInfo.getString("id"),
                                    "",
                                    "",
                                    postInfo.getString("url"),
                                    Image_Ratio(postInfo.getString("resolution"))
                                );

                                TempList += post;
                            } catch (e: JSONException) {
                                Log.e("wallhaven_api", "err: ${e.toString()} url: $body");
                            }
                        }
                        if(TempList.size > 0){
                            currentPage++;
                            lastindex = wallhaven_homepage_posts.size;
                            wallhaven_homepage_posts += TempList;
                            callback();
                        }else{
                            onfailercallback();
                        }

                    }
                    catch (e: Exception){
                        onfailercallback();
                        Log.e("wallhaven_api",e.toString());
                    }
                }

            })
        }


        //request tag page
        fun TagPosts(tag: Tag, sorting: String = "&sorting=views", ordering:String = "&order=desc", callback: (Status : Int) -> Unit = {}){
            var url = "https://wallhaven.cc/api/v1/search?page=${tag.Page_Tag}${tag.Name_Tag}${sorting}${ordering}";
            url = url.replace(" ","%20")
            var tagPosts_request = Request.Builder()
                .url(url)
                .build();


            Log.i("wallhaven_api","TagPosts URL: $url")
            wallhavenRequest.newCall(tagPosts_request).enqueue(object : Callback{

                override fun onFailure(call: Call, e: IOException) {
                    Log.e("wallhaven_api","Tag request failed: $e");
                    callback(400);
                }

                override fun onResponse(call: Call, response: Response) {
                    try {
                        val body = response.body!!.string();
                        Log.i("wallhaven_api",body);
                        val responseJson  = JSONObject(body);
                        val data = responseJson.getJSONArray("data");

                        var tempList: Array<Image_Info> = emptyArray();

                        for (i in 0 until data.length()) {
                            var post: Image_Info;
                            val postInfo = data.getJSONObject(i);

                            var found = false;
                            for (j in tag.Tag_Post_list) {
                                if (j.Image_name == postInfo.getString("id"))
                                    found = true;
                            }
                            if (found)
                                continue;
                            post = Image_Info(
                                postInfo.getString("path"),
                                postInfo.getJSONObject("thumbs").getString("original"),
                                postInfo.getString("id"),
                                "",
                                "",
                                postInfo.getString("url"),
                                Image_Ratio(postInfo.getString("resolution"))
                            );
                            tempList += post;
                        }
                        if(tempList.size > 0){
                            tag.Page_Tag++;
                            tag.lastindex = tag.Tag_Post_list.size;
                            tag.Tag_Post_list += tempList;
                            callback(200);
                        }else{
                            callback(400)
                        }
                    }catch (e:JSONException){
                        callback(400);
                        Log.e("wallhaven_api","Tag error: $e");
                    }
                }

            })
        }






        //add image info to post
        fun imageInfo(listimage_ref : Image_Info, callback: (Status: Int) -> Unit = {}){
            val requestImageInfo = Request.Builder()
                .url("https://wallhaven.cc/api/v1/w/${listimage_ref.Image_name}")
                .build();
            wallhavenRequest.newCall(requestImageInfo).enqueue(object : Callback{
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("wallhaven_api",e.toString());
                    callback(400);
                }

                override fun onResponse(call: Call, response: Response) {
                    try {
                        val body = response.body!!.string();
                        val data = JSONObject(body).getJSONObject("data");
                        listimage_ref.Image_auther = data.getJSONObject("uploader").getString("username");
                        try {
                            Image_Activity.TagNameList = emptyArray();
                            val TagsJson = data.getJSONArray("tags");
                            for(i in 0 until TagsJson.length()){
                                if(TagsJson.getJSONObject(i).getString("purity") == "sfw")
                                    Image_Activity.TagNameList += TagsJson.getJSONObject(i).getString("name");
                            }


                        }catch (e : JSONException){
                            Log.e("wallhaven_api","Tag error ${e.toString()}")
                        }

                        callback(200);
                    }catch (e: JSONException){
                        Log.e("wallhaven_api",e.toString());
                        callback(400);
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
        var lastindex : Int = 0,
        var Tag_Post_list: MutableList<Image_Info> = emptyList<Image_Info>().toMutableList()
    )

}