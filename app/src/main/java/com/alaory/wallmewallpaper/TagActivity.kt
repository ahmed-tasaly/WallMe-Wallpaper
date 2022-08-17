package com.alaory.wallmewallpaper

import android.content.Intent
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class TagActivity : AppCompatActivity(),Wallhaven_adab.OnImageClick {

    companion object{
        var Tag_Assing : wallhaven_api.Tag = wallhaven_api.Tag("");
    }

    lateinit var TagAdab : Wallhaven_adab;
    lateinit var Tag_recyclerView : RecyclerView;
    lateinit var tag_post_list : wallhaven_api.Tag;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tag)
        this.supportActionBar!!.hide();
        tag_post_list = Tag_Assing;
        TagAdab = Wallhaven_adab(tag_post_list.Tag_Post_list,this);
        update_adabter();

        Tag_recyclerView = findViewById(R.id.tag_recyclye);

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
        wallhaven_api.TagPosts(tag_post_list) {
            runOnUiThread {
                for (i in tag_post_list.Tag_Post_list)
                    Log.i("TagActivity","Tag: ${tag_post_list.Name_Tag}, Page: ${tag_post_list.Page_Tag} , Post: ${i.post_url}")
                TagAdab.refresh_itemList();
            }
        }
    }


    override fun onImageClick(Pos: Int, thumbnail: Drawable) {
        try{
            var intent = Intent(this,Image_Activity::class.java);
            Image_Activity.MYDATA = tag_post_list.Tag_Post_list[Pos];
            Image_Activity.THUMBNAIL = thumbnail;
            Image_Activity.postmode = Image_Activity.mode.wallhaven;
            startActivity(intent);
        }catch (e:Exception){
            Log.e("wallhaven_posts",e.toString())
        }
    }
}