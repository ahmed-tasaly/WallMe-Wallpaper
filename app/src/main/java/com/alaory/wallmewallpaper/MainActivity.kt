package com.alaory.wallmewallpaper

import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.get
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.alaory.wallmewallpaper.databinding.ActivityMainBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity(){

    private var binding: ActivityMainBinding? = null;

    var wallhaven_filter = wallhaven_settings();
    var reddit_filter = Reddit_settings();



    companion object{
        var num_post_in_Column = 2;
        var last_orein = Configuration.ORIENTATION_PORTRAIT;
        var LastFragmentMode: Fragment?  = null;

        var mainactivity : MainActivity? = null;

        //fragmenst
         var redditPosts  = Reddit_posts();
         var wallhavenPosts = wallhaven_posts();

        //nav
        var bottomnav : BottomNavigationView ? = null;
        var filterbutton : FloatingActionButton ? = null;
        var navbox : ConstraintLayout ?  = null;


        fun checkorein(){
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
                else -> {}
            }
        }


        fun enableBottomButtons(enable: Boolean){
            filterbutton!!.isEnabled = enable;
            for(i in 0 until bottomnav!!.menu.size())
                bottomnav!!.menu.get(i).isEnabled = enable;
        }


        fun hidenav(){
            enableBottomButtons(false)
            navbox?.animate().apply {
                this!!.startDelay = 200;
                this!!.duration = 300;
                this!!.translationY(500f);
            }
        }

        fun shownav(){
            enableBottomButtons(true)
            navbox?.animate().apply {
                this!!.startDelay = 200;
                this!!.duration = 300;
                this!!.translationY(0f);

            }
        }

        fun change_fragment(fragment: Fragment,shownav : Boolean = false){


            LastFragmentMode = fragment;
            val fragman = mainactivity?.supportFragmentManager?.beginTransaction();
            fragman?.replace(R.id.container,fragment);
            fragman?.commit();

            if(shownav)
                shownav();

        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        if(Configuration.ORIENTATION_LANDSCAPE == Resources.getSystem().configuration.orientation){
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P ){
                window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            }
        }
        this.supportActionBar!!.hide();


        //set global mainActivity
        mainactivity = this;


        //update settings
        Reddit_settings.loadprefs(this);
        wallhaven_settings.loadprefs(this);

        //set ui
        binding = ActivityMainBinding.inflate(layoutInflater);
        setContentView(binding!!.root);

        //set ui fragment
        if(LastFragmentMode!=null)
            change_fragment(LastFragmentMode!!);
        else
            change_fragment(redditPosts);

        //update screen orientation data
        checkorein();

        //pull posts from apis
        Reddit_Api.Update_Api_key{
            redditPosts.LoadMore();
            wallhavenPosts.LoadMore();
        }//init wallhaven & reddit api to get the key and set data to array



        //set buttom navigtion
        filterbutton = findViewById<FloatingActionButton>(R.id.filterbutton);
        navbox = findViewById(R.id.navigation_constraint_box);
        bottomnav = findViewById<BottomNavigationView>(R.id.bottom_navigation);
        bottomnav?.selectedItemId = R.id.Reddit_posts_List;

        //set button navitgtion actions
        bottomnav?.setOnItemSelectedListener {
           when (it.itemId){
               R.id.Reddit_posts_List -> {change_fragment(redditPosts);true}
               R.id.wallhaven_posts_list -> {change_fragment(wallhavenPosts);true}
               else -> {true}
           }
        }


        //set floating button actions
        filterbutton?.setOnClickListener {
            hidenav();
            when(bottomnav?.selectedItemId){
                R.id.Reddit_posts_List -> {
                    change_fragment(reddit_filter);
                    true
                }
                R.id.wallhaven_posts_list -> {change_fragment(wallhaven_filter);true}
                else -> {true}
            }
        }



    }





}
