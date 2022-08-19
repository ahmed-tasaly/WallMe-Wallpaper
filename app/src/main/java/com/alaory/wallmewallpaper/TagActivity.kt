package com.alaory.wallmewallpaper

import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class TagActivity : AppCompatActivity(),Image_list_adapter.OnImageClick {

    companion object{
        var Tag_Assing : wallhaven_api.Tag = wallhaven_api.Tag("");
    }

     var TagAdab : Image_list_adapter? = null;
     var Tag_recyclerView : RecyclerView? = null;
     var tag_post_list : wallhaven_api.Tag? = null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tag);

        if(Configuration.ORIENTATION_LANDSCAPE == Resources.getSystem().configuration.orientation){
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P ){
                window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            }
        }
        this.supportActionBar!!.hide();

        MainActivity.checkorein();

        //set local data
        tag_post_list = Tag_Assing;
        TagAdab = Image_list_adapter(tag_post_list!!.Tag_Post_list,this);

        update_adabter();

        Tag_recyclerView = findViewById(R.id.tag_recyclye);

        Tag_recyclerView!!.layoutManager = GridLayoutManager(this,MainActivity.num_post_in_Column,
            GridLayoutManager.VERTICAL,false);
        Tag_recyclerView!!.setHasFixedSize(false)
        Tag_recyclerView!!.adapter = TagAdab;

        Tag_recyclerView!!.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState);
                if(!Tag_recyclerView!!.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE ){
                    update_adabter();
                }
            }
        })

    }


    fun update_adabter(){
        wallhaven_api.TagPosts(tag_post_list!!) {
            runOnUiThread {
                tag_post_list?.lastindex?.let { TagAdab!!.refresh_itemList(it) };
            }
        }
    }


    override fun onImageClick(Pos: Int, thumbnail: Drawable) {
        try{
            val intent = Intent(this,Image_Activity::class.java);
            Image_Activity.MYDATA = tag_post_list!!.Tag_Post_list[Pos];
            Image_Activity.THUMBNAIL = thumbnail;
            Image_Activity.postmode = Image_Activity.mode.wallhaven;
            startActivity(intent);
        }catch (e:Exception){
            Log.e("wallhaven_posts",e.toString())
        }
    }
}