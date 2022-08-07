package com.wallme.wallpaper

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wallme.wallpaper.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(),MyAdab.OnImageClick {

    private lateinit var binding: ActivityMainBinding;
    private lateinit var myrec: RecyclerView;
    private lateinit var myadabter: MyAdab;



    var reddit_api : Array<Reddit_Api> = arrayOf(Reddit_Api("wallpaper"));


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        this.supportActionBar!!.hide();

        binding = ActivityMainBinding.inflate(layoutInflater);
        setContentView(binding.root);

        Reddit_Api.Update_Api_key{
            update_adabter();
        }//init reddit api to get the key and set data to array



        myrec = findViewById(R.id.Mainrec);
        myrec.layoutManager = GridLayoutManager(this,2,GridLayoutManager.VERTICAL,false);
        myrec.setHasFixedSize(false);
        myadabter = MyAdab(emptyArray(),this);
        myrec.adapter = myadabter;



        myrec.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState);
                if(!myrec.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE){
                    Log.i("MainRecyclerView", "User hit bottom");
                    update_adabter();
                }

            }
        })




    }

    external fun stringFromJNI(): String

    fun update_adabter(){
        Reddit_Api.get_shuffle_andGive {
         runOnUiThread{
             myadabter.refresh_itemList();
            }
        }
    }


    companion object {
        init {
            System.loadLibrary("wallpaper");
        }
    }

    override fun onImageClick(Pos: Int,thumbnail : Drawable) {
        val intent = Intent(this,Image_Activity::class.java);
        Image_Activity.myData = Reddit_Api.reddit_global_posts.get(Pos);
        Image_Activity.thumbnail = thumbnail;
        startActivity(intent);
    }
}