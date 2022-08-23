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

    private var MlaoutManager : RecyclerView.LayoutManager? = null;
    private var scrolllistener : BottonLoading.ViewLodMore? = null;


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
        Tag_recyclerView = findViewById(R.id.tag_recyclye);

        /*
            @brief set local var and set layout mangaer and scroll callback
         */



        LoadMore();
        setLayoutForRv();
        setScrollListenerForRv();

    }


    private fun setLayoutForRv(){
        MlaoutManager = GridLayoutManager(this,MainActivity.num_post_in_Column);
        Tag_recyclerView!!.layoutManager = MlaoutManager;
        Tag_recyclerView!!.setHasFixedSize(false);
        Tag_recyclerView!!.adapter = TagAdab;
        (MlaoutManager as GridLayoutManager)!!.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup(){
            override fun getSpanSize(position: Int): Int {
                return when(TagAdab!!.getItemViewType(position)){
                    0 -> 1
                    1 -> MainActivity.num_post_in_Column
                    else -> -1
                }
            }
        }
    }

    private fun setScrollListenerForRv(){
        scrolllistener = BottonLoading.ViewLodMore(MlaoutManager as GridLayoutManager);
        scrolllistener!!.setOnLoadMoreListener(object : BottonLoading.OnLoadMoreListener{
            override fun onLoadMore() {
                TagAdab!!.addLoadingView();
                LoadMore();
            }
        })
        Tag_recyclerView!!.addOnScrollListener(scrolllistener!!);

    }



    fun LoadMore(){
        wallhaven_api.TagPosts(tag_post_list!!) {
            scrolllistener!!.setLoaded();
            runOnUiThread {
                TagAdab!!.removeLoadingView();
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