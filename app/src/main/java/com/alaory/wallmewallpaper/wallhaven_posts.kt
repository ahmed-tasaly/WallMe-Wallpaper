package com.alaory.wallmewallpaper

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


class wallhaven_posts : Fragment() , Wallhaven_adab.OnImageClick{

    lateinit var wallhaven_recycle : RecyclerView;
    lateinit var wallhaven_adabter : Wallhaven_adab;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wallhaven_adabter = Wallhaven_adab(this);
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_wallhaven_posts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        wallhaven_recycle = view.findViewById(R.id.wallhavenRec) as RecyclerView;
        wallhaven_recycle.layoutManager = GridLayoutManager(requireContext(),2,GridLayoutManager.VERTICAL,false);
        wallhaven_recycle.setHasFixedSize(false)
        wallhaven_recycle.adapter = wallhaven_adabter;

        wallhaven_recycle.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState);
                if(!wallhaven_recycle.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE && isAdded){
                    Log.i("MainRecyclerView", "User hit bottom");
                    update_adabter();
                }

            }
        })
    }

    fun update_adabter(){
        wallhaven_api.currentPage++;
        wallhaven_api.GethomePagePosts {
            requireActivity().runOnUiThread {
                wallhaven_adabter.refresh_itemList();
            }
        }
    }


    override fun onImageClick(Pos: Int, thumbnail: Drawable) {
        try{
            var intent = Intent(requireContext(),Image_Activity::class.java);
            wallhaven_api.imageInfo(wallhaven_api.wallhaven_homepage_posts[Pos]);
            Image_Activity.myData = wallhaven_api.wallhaven_homepage_posts[Pos];
            Image_Activity.thumbnail = thumbnail;
            startActivity(intent);
        }catch (e:Exception){
            Log.e("wallhaven_posts",e.toString())
        }

    }
}