package com.alaory.wallmewallpaper

import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class Reddit_posts : Fragment(),Image_list_adapter.OnImageClick {

    private var myrec: RecyclerView? = null;
    private var PostsAdabter: Image_list_adapter? = null;


    companion object{
         var firsttime = true;
         var userHitSave = false;
    }


    var reddit_api : Array<Reddit_Api> = emptyArray();



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);

        if(firsttime || userHitSave){
            Log.i("Reddit_posts","i have beeen created");
            reddit_api = emptyArray();
            Reddit_Api.Subreddits = emptyArray();
            Reddit_Api.reddit_global_posts = emptyList<List_image>().toMutableList();

            for (i in Reddit_settings.subreddits_list_names){
                reddit_api += Reddit_Api(i);
            }
            PostsAdabter = Image_list_adapter(Reddit_Api.reddit_global_posts,this);
            update_adabter();
            firsttime = false;
            userHitSave = false;
        }

        if(Resources.getSystem().configuration.orientation != MainActivity.last_orein)
            update_adabter();

        if(PostsAdabter == null)
            PostsAdabter = Image_list_adapter(Reddit_Api.reddit_global_posts,this);

    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState);




        myrec = view.findViewById(R.id.fragmentrec) as RecyclerView;
        myrec!!.layoutManager = GridLayoutManager(requireContext(),MainActivity.num_post_in_Column, GridLayoutManager.VERTICAL,false);
        myrec!!.setHasFixedSize(false);
        myrec?.adapter = PostsAdabter;

        if(Resources.getSystem().configuration.orientation != MainActivity.last_orein)
            update_adabter();

        myrec!!.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState);
                if(!myrec!!.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE && isAdded){
                    Log.i("MainRecyclerView", "User hit bottom");
                    update_adabter();
                }

            }
        })


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_reddit_posts, container, false)
    }



    fun update_adabter(){
            Reddit_Api.get_shuffle_andGive {
                if(isAdded) {
                    requireActivity().runOnUiThread {
                        PostsAdabter?.refresh_itemList(Reddit_Api.last_index);
                    }
                }
            }

    }


    override fun onImageClick(Pos: Int,thumbnail : Drawable) {
        try {
            val intent = Intent(requireContext(), Image_Activity::class.java);
            Image_Activity.MYDATA = Reddit_Api.reddit_global_posts.get(Pos);
            Image_Activity.THUMBNAIL = thumbnail;
            Image_Activity.postmode = Image_Activity.mode.reddit;
            startActivity(intent);
        }catch (e: Exception){
            Log.e("Reddit_posts","error while trying to set image activity")
        }
    }

}