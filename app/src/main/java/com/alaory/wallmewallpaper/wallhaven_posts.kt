package com.alaory.wallmewallpaper

import android.content.Intent
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


class wallhaven_posts : Fragment() , Image_list_adapter.OnImageClick{

     var wallhaven_recycle : RecyclerView? = null;
     var wallhaven_adabter : Image_list_adapter? = null;


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);

        wallhaven_adabter = Image_list_adapter(wallhaven_api.wallhaven_homepage_posts,this);

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
        wallhaven_recycle!!.layoutManager = GridLayoutManager(requireContext(),MainActivity.num_post_in_Column,GridLayoutManager.VERTICAL,false);
        wallhaven_recycle!!.setHasFixedSize(false)
        wallhaven_recycle?.adapter = wallhaven_adabter;

        if(Resources.getSystem().configuration.orientation != MainActivity.last_orein)
            update_adabter();

        wallhaven_recycle!!.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState);
                if(!wallhaven_recycle!!.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE && isAdded){
                    update_adabter();
                }

            }
        })




    }



    fun update_adabter(){
        wallhaven_api.GethomePagePosts {
            requireActivity().runOnUiThread {
                wallhaven_adabter?.refresh_itemList();
            }
        }
    }


    override fun onImageClick(Pos: Int, thumbnail: Drawable) {
        try{
            var intent = Intent(requireContext(),Image_Activity::class.java);
            Image_Activity.MYDATA = wallhaven_api.wallhaven_homepage_posts[Pos];
            Image_Activity.THUMBNAIL = thumbnail;
            Image_Activity.postmode = Image_Activity.mode.wallhaven;
            startActivity(intent);
        }catch (e:Exception){
            Log.e("wallhaven_posts",e.toString())
        }

    }
}