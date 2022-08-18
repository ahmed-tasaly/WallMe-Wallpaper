package com.alaory.wallmewallpaper

import android.content.res.Configuration
import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.alaory.wallmewallpaper.databinding.ActivityMainBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity(){

    private var binding: ActivityMainBinding? = null;


    private var redditPosts  = Reddit_posts();
    private var reddit_filter = Reddit_settings();
    private var wallhavenPosts = wallhaven_posts();
    private var wallhaven_filter = wallhaven_settings();

    companion object{
        var num_post_in_Column = 2;
        var last_orein = Configuration.ORIENTATION_PORTRAIT;
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        this.supportActionBar!!.hide();
        Reddit_settings.loadprefs(this);


        when(Resources.getSystem().configuration.orientation){
            Configuration.ORIENTATION_PORTRAIT ->{
                num_post_in_Column = 2;
                last_orein = Configuration.ORIENTATION_PORTRAIT;
            }
            Configuration.ORIENTATION_LANDSCAPE -> {
                num_post_in_Column = 4;
                last_orein = Configuration.ORIENTATION_LANDSCAPE;
            }
            Configuration.ORIENTATION_UNDEFINED -> {
                num_post_in_Column = 2;
                last_orein = Configuration.ORIENTATION_UNDEFINED;
            }
        }




        binding = ActivityMainBinding.inflate(layoutInflater);
        setContentView(binding!!.root);

        change_fragment(redditPosts);


        Reddit_Api.Update_Api_key{
            redditPosts.update_adabter();
            wallhaven_api.GethomePagePosts();
        }//init wallhaven & reddit api to get the key and set data to array

        val bottomnav = findViewById<BottomNavigationView>(R.id.bottom_navigation);

        bottomnav.selectedItemId = R.id.Reddit_posts_List;

        bottomnav.setOnItemSelectedListener {
           when (it.itemId){
               R.id.Reddit_posts_List -> {change_fragment(redditPosts);true}
               R.id.wallhaven_posts_list -> {change_fragment(wallhavenPosts);true}
               else -> {true}
           }
        }
        findViewById<FloatingActionButton>(R.id.filterbutton).setOnClickListener {
            when(bottomnav.selectedItemId){
                R.id.Reddit_posts_List -> {change_fragment(reddit_filter);true}
                R.id.wallhaven_posts_list -> {change_fragment(wallhaven_filter);true}
                else -> {true}
            }
        }


    }


    fun change_fragment(fragment: Fragment){
        var fragman = supportFragmentManager.beginTransaction();
        fragman.replace(R.id.container,fragment);
        fragman.commit();
    }


}