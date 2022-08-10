package com.wallme.wallpaper

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.wallme.wallpaper.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(){

    private lateinit var binding: ActivityMainBinding;
    private lateinit var myrec: RecyclerView;

    private var redditPosts  = Reddit_posts();
    private var redditSettings = Reddit_settings();


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        this.supportActionBar!!.hide();
        Reddit_settings.loadprefs(this);

        binding = ActivityMainBinding.inflate(layoutInflater);
        setContentView(binding.root);
        change_fragment(redditPosts);

        Reddit_Api.Update_Api_key{
            redditPosts.update_adabter();
        }//init reddit api to get the key and set data to array

       findViewById<BottomNavigationView>(R.id.bottom_navigation).setOnItemSelectedListener {
           when (it.itemId){
               R.id.Reddit_posts_List -> {change_fragment(redditPosts);true}
               R.id.reddit_settings -> {change_fragment(redditSettings);true}
               else -> {true}
           }
       }

    }

    private fun change_fragment(fragment: Fragment){
        var fragman = supportFragmentManager.beginTransaction();
        fragman.replace(R.id.container,fragment);
        fragman.commit();
    }



    external fun stringFromJNI(): String


    companion object {
        init {
            System.loadLibrary("wallpaper");
        }
    }
}