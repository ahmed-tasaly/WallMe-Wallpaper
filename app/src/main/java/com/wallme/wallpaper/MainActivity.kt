package com.wallme.wallpaper

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.util.Xml
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.wallme.wallpaper.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(),MyAdab.OnImageClick {

    private lateinit var binding: ActivityMainBinding;
    private lateinit var myrec: RecyclerView;
    private lateinit var myadab: MyAdab;


    var reddit_api : Reddit_Api = Reddit_Api("wallpaper");

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(layoutInflater);
        setContentView(binding.root);
        Picasso.get().setIndicatorsEnabled(true);





        myrec = findViewById(R.id.Mainrec);
        myrec.layoutManager = GridLayoutManager(this,2,GridLayoutManager.VERTICAL,false);
        myrec.setHasFixedSize(false);
        myadab = MyAdab(Reddit_Api.reddit_global_posts,this);
        myrec.adapter = myadab;

        reddit_api.get_subreddit_posts {
            Log.i("MainRecyclerView", "CallBack called");
            runOnUiThread { myadab.refresh_itemList(Reddit_Api.reddit_global_posts); }
        }


        myrec.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState);
                if(!myrec.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE){
                    Log.i("MainRecyclerView", "User hit bottom");
                    update_adabter();
                }

            }
        })




        findViewById<Button>(R.id.refresh).setOnClickListener{
            Log.i("Reddit_Api","Refresh button pressed")
            reddit_api.get_subreddit_posts{
                update_adabter();
            };

        }


        Reddit_Api.Update_Api_key{
            update_adabter();
        }//init reddit api to get the key
    }

    external fun stringFromJNI(): String

    fun update_adabter(){
        reddit_api.get_subreddit_posts {
            Log.i("MainRecyclerView", "CallBack called");
            runOnUiThread { myadab.refresh_itemList(Reddit_Api.reddit_global_posts); }
        }
    }


    companion object {
        init {
            System.loadLibrary("wallpaper");
        }
    }

    override fun onImageClick(Pos: Int) {
        val intent = Intent(this,Image_Activity::class.java);
        Image_Activity.myData = reddit_api.subreddit_posts_list.get(Pos);
        startActivity(intent);
    }
}