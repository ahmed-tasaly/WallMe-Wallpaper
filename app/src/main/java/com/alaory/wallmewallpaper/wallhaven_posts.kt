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
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.alaory.wallmewallpaper.adabter.Image_list_adapter
import com.alaory.wallmewallpaper.api.wallhaven_api


class wallhaven_posts : Fragment() , Image_list_adapter.OnImageClick{

     var wallhaven_recycle : RecyclerView? = null;
     var wallhaven_adabter : Image_list_adapter? = null;
     var mLayoutManager : RecyclerView.LayoutManager? =null;
     var bottomloading : BottonLoading.ViewLodMore? = null;

    var imageloading: ImageView? =null;
    var textloading: TextView? = null;
    var buttonLoading: Button? =null;

    var appfirstneedLoading = false;

    companion object{
        var userhitsave : Boolean = false;
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        wallhaven_adabter = Image_list_adapter(wallhaven_api.wallhaven_homepage_posts,this);

        if(userhitsave){
            LoadMore();
            userhitsave = false;
        }
    }

    fun showloading(){
        buttonLoading!!.visibility = View.GONE;
        imageloading!!.visibility = View.VISIBLE;
        textloading!!.visibility = View.VISIBLE;
    }
    fun hideloading(){
        buttonLoading!!.visibility = View.VISIBLE;
        imageloading!!.visibility = View.GONE
        textloading!!.visibility = View.GONE
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val layoutfragment = inflater.inflate(R.layout.postlist_mainwindow, container, false);

        wallhaven_recycle = layoutfragment.findViewById(R.id.fragmentrec) as RecyclerView;
        SetRVLayoutManager();
        SetRvScrollListener();

        imageloading =layoutfragment.findViewById(R.id.loading_recyclye);
        textloading = layoutfragment.findViewById(R.id.loading_text);
        buttonLoading = layoutfragment.findViewById(R.id.load_failer_button);


        buttonLoading!!.setOnClickListener {
            LoadMore();
            showloading();
            appfirstneedLoading = false;
        }

        if(appfirstneedLoading){
            hideloading();
        }

        MainActivity.setImageView_asLoading(imageloading!!);

        if(Resources.getSystem().configuration.orientation != MainActivity.last_orein)
            LoadMore();

        return layoutfragment;
    }


    override fun onDetach() {
        super.onDetach()
        wallhaven_adabter!!.removeLoadingView();
    }

    private fun SetRVLayoutManager(){
        mLayoutManager = StaggeredGridLayoutManager(MainActivity.num_post_in_Column,StaggeredGridLayoutManager.VERTICAL)
        wallhaven_recycle!!.layoutManager = mLayoutManager;
        wallhaven_recycle!!.setHasFixedSize(true);
        wallhaven_recycle?.adapter = wallhaven_adabter;

    }



    private fun SetRvScrollListener(){
        bottomloading = BottonLoading.ViewLodMore(mLayoutManager as StaggeredGridLayoutManager);
        bottomloading!!.setOnLoadMoreListener(object : BottonLoading.OnLoadMoreListener{
            override fun onLoadMore() {
                wallhaven_adabter!!.addLoadingView();
                LoadMore();
            }
        })
        wallhaven_recycle!!.addOnScrollListener(bottomloading!!);
    }


    fun LoadMore(){
        wallhaven_api.GethomePagePosts ({
            appfirstneedLoading = false;
            if(isAdded){
                requireActivity().runOnUiThread {
                    wallhaven_adabter?.removeLoadingView();
                    bottomloading?.setLoaded();
                    wallhaven_recycle!!.post {
                        wallhaven_adabter?.refresh_itemList(wallhaven_api.lastindex);
                    }
                }
            }
        },
            {
                //on failer
                appfirstneedLoading = true;
                if(isAdded){
                requireActivity().runOnUiThread {
                    if (appfirstneedLoading) {
                        hideloading();
                    }
                    wallhaven_adabter?.removeLoadingView();
                    bottomloading?.setLoaded();
                    }
                }
            }
        )
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