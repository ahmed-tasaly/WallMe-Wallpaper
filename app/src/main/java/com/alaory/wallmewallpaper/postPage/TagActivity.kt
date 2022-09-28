package com.alaory.wallmewallpaper.postPage

import android.content.Intent
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.alaory.wallmewallpaper.*
import com.alaory.wallmewallpaper.adabter.Image_list_adapter
import com.alaory.wallmewallpaper.api.Reddit_Api
import com.alaory.wallmewallpaper.api.wallhaven_api

class TagActivity : AppCompatActivity(), Image_list_adapter.OnImageClick {

    private var MlaoutManager : RecyclerView.LayoutManager? = null;
    private var scrolllistener : BottonLoading.ViewLodMore? = null;


    companion object{
        var Tag_Assing : wallhaven_api.Tag = wallhaven_api.Tag("");
        var lastPastImageInfo : Image_Info? = null;
        var lastPastImageInfo_pos : Int = -1;
    }

     var TagAdab : Image_list_adapter? = null;
     var Tag_recyclerView : RecyclerView? = null;
     var tag_post_list : wallhaven_api.Tag? = null;

    override fun onResume() {
        super.onResume()
        MainActivity.HideSystemBar(window);
        if(lastPastImageInfo != null && database.lastblockedaddedImageInfo != null){
            if(lastPastImageInfo!!.Image_name == database.lastblockedaddedImageInfo!!.Image_name){
                TagAdab!!.notifyDataSetChanged();
                Reddit_Api.reddit_global_posts.removeAt(Reddit_posts.lastPastImageInfo_pos);
                lastPastImageInfo = null;
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tag);

        MainActivity.HideSystemBar(window);

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
        MlaoutManager = StaggeredGridLayoutManager(MainActivity.num_post_in_Column,StaggeredGridLayoutManager.VERTICAL);
        Tag_recyclerView!!.layoutManager = MlaoutManager;
        Tag_recyclerView!!.setHasFixedSize(false);
        Tag_recyclerView!!.adapter = TagAdab;
    }

    private fun setScrollListenerForRv(){
        scrolllistener = BottonLoading.ViewLodMore(MlaoutManager as StaggeredGridLayoutManager);
        scrolllistener!!.setOnLoadMoreListener(object : BottonLoading.OnLoadMoreListener {
            override fun onLoadMore() {
                TagAdab!!.addLoadingView();
                LoadMore();
            }
        })
        Tag_recyclerView!!.addOnScrollListener(scrolllistener!!);

    }



    fun LoadMore(){
        wallhaven_api.TagPosts(tag_post_list!!) {
            if(it == 400){
                runOnUiThread {
                    TagAdab?.removeLoadingView();
                    scrolllistener?.setLoaded();
                }
                return@TagPosts;
            }
            runOnUiThread {
                TagAdab?.removeLoadingView();
                scrolllistener?.setLoaded();
                TagAdab!!.refresh_itemList(tag_post_list!!.lastindex);
            }
        }
    }


    override fun onImageClick(Pos: Int, thumbnail: Drawable,loaded : Boolean) {
        try{
            lastPastImageInfo = tag_post_list!!.Tag_Post_list[Pos];
            lastPastImageInfo_pos = Pos;
            val intent = Intent(this, Image_Activity::class.java);
            Image_Activity.MYDATA = tag_post_list!!.Tag_Post_list[Pos];
            Image_Activity.THUMBNAIL = thumbnail;
            Image_Activity.postmode = Image_Activity.mode.wallhaven;
            Image_Activity.loadedPreview = loaded;
            startActivity(intent);
        }catch (e:Exception){
            Log.e("wallhaven_posts",e.toString())
        }
    }
}