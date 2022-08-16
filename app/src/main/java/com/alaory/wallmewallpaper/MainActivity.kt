package com.alaory.wallmewallpaper

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.alaory.wallmewallpaper.databinding.ActivityMainBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity(){

    private lateinit var binding: ActivityMainBinding;
    private lateinit var myrec: RecyclerView;


    private var redditPosts  = Reddit_posts();
    private var reddit_filter = Reddit_settings();
    private var wallhavenPosts = wallhaven_posts();

    companion object{
        var Tag_fragment : TagFragment? = null
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        this.supportActionBar!!.hide();
        Reddit_settings.loadprefs(this);

        binding = ActivityMainBinding.inflate(layoutInflater);
        setContentView(binding.root);

        if(Tag_fragment == null)
            change_fragment(redditPosts);
        else
            change_fragment(Tag_fragment!!)


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