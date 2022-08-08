package com.wallme.wallpaper

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class Reddit_posts : Fragment(),MyAdab.OnImageClick {

    private lateinit var myrec: RecyclerView;
    private lateinit var myadabter: MyAdab;
    var reddit_api : Array<Reddit_Api> = emptyArray();


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        reddit_api = emptyArray();
        for (i in Reddit_settings.subreddits_list_names){
            reddit_api += Reddit_Api(i);
        }
        myadabter = MyAdab(emptyArray(),this);
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState);


        myrec = view.findViewById(R.id.fragmentrec) as RecyclerView;
        myrec.layoutManager = GridLayoutManager(requireContext(),2, GridLayoutManager.VERTICAL,false);
        myrec.setHasFixedSize(false);
        myrec.adapter = myadabter;

        myrec.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState);
                if(!myrec.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE && isAdded){
                    Log.i("MainRecyclerView", "User hit bottom");
                    update_adabter();
                }

            }
        })

        Reddit_Api.Update_Api_key{
            update_adabter();
        }//init reddit api to get the key and set data to array
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
                        myadabter.refresh_itemList();
                    }
                }
            }

    }


    override fun onImageClick(Pos: Int,thumbnail : Drawable) {
        val intent = Intent(requireContext(),Image_Activity::class.java);
        Image_Activity.myData = Reddit_Api.reddit_global_posts.get(Pos);
        Image_Activity.thumbnail = thumbnail;
        startActivity(intent);
    }

}