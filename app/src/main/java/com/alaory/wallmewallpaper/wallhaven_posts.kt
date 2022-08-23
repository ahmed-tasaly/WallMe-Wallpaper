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
     var mLayoutManager : RecyclerView.LayoutManager? =null;
     var bottomloading : BottonLoading.ViewLodMore? = null;



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        wallhaven_adabter = Image_list_adapter(wallhaven_api.wallhaven_homepage_posts,this);
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.postlist_mainwindow, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        wallhaven_recycle = view.findViewById(R.id.fragmentrec) as RecyclerView;
        SetRVLayoutManager();
        SetRvScrollListener();

        if(Resources.getSystem().configuration.orientation != MainActivity.last_orein)
            LoadMore();

    }

    override fun onDetach() {
        super.onDetach()
        wallhaven_adabter!!.removeLoadingView();
    }

    private fun SetRVLayoutManager(){
        mLayoutManager = GridLayoutManager(requireContext(),MainActivity.num_post_in_Column);
        wallhaven_recycle!!.layoutManager = mLayoutManager;
        wallhaven_recycle!!.setHasFixedSize(false);
        wallhaven_recycle?.adapter = wallhaven_adabter;
        (mLayoutManager as GridLayoutManager?)!!.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup(){
            override fun getSpanSize(position: Int): Int {
                return when(wallhaven_adabter!!.getItemViewType(position)){
                    0 -> 1
                    1 -> MainActivity.num_post_in_Column
                    else -> -1
                }
            }
        }
    }



    private fun SetRvScrollListener(){
        bottomloading = BottonLoading.ViewLodMore(mLayoutManager as GridLayoutManager);
        bottomloading!!.setOnLoadMoreListener(object : BottonLoading.OnLoadMoreListener{
            override fun onLoadMore() {
                LoadMore();
                wallhaven_adabter!!.addLoadingView();
            }
        })
        wallhaven_recycle!!.addOnScrollListener(bottomloading!!);
    }


    fun LoadMore(){
        wallhaven_api.GethomePagePosts {
            bottomloading?.setLoaded();
            if(isAdded){
                requireActivity().runOnUiThread {
                    wallhaven_adabter!!.removeLoadingView();
                    wallhaven_adabter!!.refresh_itemList(wallhaven_api.lastindex);
                }
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