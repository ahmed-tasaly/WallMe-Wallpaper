package com.alaory.wallmewallpaper

import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class TagActivity : AppCompatActivity(),Wallhaven_adab.OnImageClick {

    companion object{
        var tag_post_list : wallhaven_api.Tag = wallhaven_api.Tag("");
    }

    lateinit var TagAdab : Wallhaven_adab;
    lateinit var Tag_recyclerView : RecyclerView;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tag)
        TagAdab = Wallhaven_adab(tag_post_list.Tag_Post_list,this);


        Tag_recyclerView = findViewById(R.id.wallhavenRec) as RecyclerView;
        Tag_recyclerView.layoutManager = GridLayoutManager(this,2,
            GridLayoutManager.VERTICAL,false);
        Tag_recyclerView.setHasFixedSize(false)
        Tag_recyclerView.adapter = TagAdab;

        Tag_recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState);
                if(!Tag_recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE ){
                    Log.i("MainRecyclerView", "User hit bottom");
                    update_adabter();
                }

            }
        })
    }

    fun update_adabter(){
        wallhaven_api.GethomePagePosts {
            runOnUiThread {
                TagAdab.refresh_itemList();
            }
        }
    }


    override fun onImageClick(Pos: Int, thumbnail: Drawable) {

    }
}