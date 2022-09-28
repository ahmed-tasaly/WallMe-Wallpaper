package com.alaory.wallmewallpaper

import android.util.Log

class Image_Ratio(){
    var Width: Int = 1;
    var Height: Int = 1;

    constructor(width: Int,height: Int) : this() {
        Width = width;
        Height = height;
    }

    constructor(Ratio: String) : this() {
        try {
            val res = Ratio.split("x");
            Width = res[0].toInt()
            Height = res[1].toInt()
        }catch (e: Exception){
            Log.e("Image_Ratio","Error converting to int")
        }
    }
}

enum class UrlType {
    Image,
    Gif,
    Video
}

data class Image_Info(
    var Image_url:String,
    var Image_thumbnail: String,
    var Image_name:String = "Unknown",
    var Image_auther:String = "Unknown",
    var Image_title: String = "Unknown",
    var post_url: String = "",
    var imageRatio: Image_Ratio = Image_Ratio(1,1),
    var type : UrlType = UrlType.Image,
) {}

