package com.alaory.wallmewallpaper.postPage

import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.drawable.AnimatedVectorDrawable
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
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.alaory.wallmewallpaper.*
import com.alaory.wallmewallpaper.adabter.Image_list_adapter
import com.alaory.wallmewallpaper.api.wallhaven_api


class wallhaven_posts( menuChange : MainActivity.MenuChange? = null) : Fragment() , Image_list_adapter.OnImageClick{

     val MenuChange = menuChange;
     var wallhaven_recycle : RecyclerView? = null;
     var wallhaven_adabter : Image_list_adapter? = null;
     var mLayoutManager : RecyclerView.LayoutManager? =null;
     var bottomloading : BottonLoading.ViewLodMore? = null;

    var imageloading: ImageView? =null;
    var textloading: TextView? = null;
    var buttonLoading: Button? =null;

    var failedFirstLoading = false;

    companion object{
        var userhitsave : Boolean = false;
        var lastPastImageInfo : Image_Info? = null;
        var lastPastImageInfo_pos : Int = -1;
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig);
        wallmewallpaper.checkorein();
        SetRVLayoutManager();
        SetRvScrollListener();
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        wallhaven_adabter = wallhaven_api.wallhavenApi?.wallhaven_homepage_posts?.let { Image_list_adapter(it,this) };
        if(userhitsave){
            LoadMore();
            userhitsave = false;
        }
    }

    fun showloading(){
        buttonLoading?.let{it.visibility = View.GONE;}
        imageloading?.let{it.visibility = View.VISIBLE;}
        textloading?.let{it.visibility = View.VISIBLE;}
    }
    fun hideloading(){
        buttonLoading?.let{it.visibility = View.VISIBLE;}
        imageloading?.let{it.visibility = View.GONE;}
        textloading?.let{it.visibility = View.GONE}
    }
    fun disableloading(){
        buttonLoading?.let{it.visibility = View.GONE;}
        imageloading?.let{it.visibility = View.GONE;}
        textloading?.let{it.visibility = View.GONE;}
        failedFirstLoading = false;
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            AlertDialog.Builder(requireContext(), R.style.Dialog_first)
                .setTitle("Do you want to leave the app")
                .setPositiveButton("Yes",object : DialogInterface.OnClickListener{
                    override fun onClick(p0: DialogInterface?, p1: Int) {
                        requireActivity().finish()
                        System.exit(0);
                    }
                })
                .setNegativeButton("No",null)
                .show()
        }


        val layoutfragment = inflater.inflate(R.layout.postlist_mainwindow, container, false);

        wallhaven_recycle = layoutfragment.findViewById(R.id.fragmentrec) as RecyclerView;
        SetRVLayoutManager();
        SetRvScrollListener();

        imageloading =layoutfragment.findViewById(R.id.loading_recyclye);
        textloading = layoutfragment.findViewById(R.id.loading_text);
        buttonLoading = layoutfragment.findViewById(R.id.load_failer_button);


        buttonLoading!!.setOnClickListener {
            LoadMore();
        }

        if(failedFirstLoading){
            hideloading();
        }

        val loadingdraw = ResourcesCompat.getDrawable(this.resources,R.drawable.loading_anim,requireContext().theme) as AnimatedVectorDrawable;
        loadingdraw.start();
        imageloading!!.setImageDrawable(loadingdraw);

        if(Resources.getSystem().configuration.orientation !=  wallmewallpaper.last_orein)
            LoadMore();

        return layoutfragment;
    }


    override fun onDetach() {
        super.onDetach()
        wallhaven_adabter?.removeLoadingView();
    }

    override fun onResume() {
        super.onResume()
        if(lastPastImageInfo != null && database.lastblockedaddedImageInfo != null){
            if(lastPastImageInfo!!.Image_name == database.lastblockedaddedImageInfo!!.Image_name){
                wallhaven_adabter!!.notifyDataSetChanged();
                wallhaven_api.wallhavenApi!!.wallhaven_homepage_posts.removeAt(lastPastImageInfo_pos);
                lastPastImageInfo = null;
            }
        }
    }

    private fun SetRVLayoutManager(){
        mLayoutManager = StaggeredGridLayoutManager( wallmewallpaper.num_post_in_Column,StaggeredGridLayoutManager.VERTICAL);
        //(mLayoutManager as StaggeredGridLayoutManager).gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS;
        wallhaven_recycle!!.layoutManager = mLayoutManager;
        wallhaven_recycle!!.setHasFixedSize(true);
        wallhaven_recycle?.adapter = wallhaven_adabter;

    }



    private fun SetRvScrollListener(){
        bottomloading =   BottonLoading.ViewLodMore(mLayoutManager as StaggeredGridLayoutManager, MenuChange);
        bottomloading!!.setOnLoadMoreListener(object : BottonLoading.OnLoadMoreListener {
            override fun onLoadMore() {
                wallhaven_recycle?.post {
                    wallhaven_adabter!!.addLoadingView();
                    LoadMore();
                }
            }
        })
        wallhaven_recycle!!.addOnScrollListener(bottomloading!!);
    }


    fun LoadMore(){
        if(isAdded){
            requireActivity().runOnUiThread {
                showloading();
            }
        }

        wallhaven_api.wallhavenApi!!.GethomePagePosts ({
            failedFirstLoading = false;
            if(isAdded){
                requireActivity().runOnUiThread {
                    wallhaven_adabter?.removeLoadingView();
                    bottomloading?.setLoaded();
                    wallhaven_adabter?.refresh_itemList(wallhaven_api.wallhavenApi!!.lastindex);

                    disableloading();
                }
            }
        },
            {// on failer callback

                failedFirstLoading = true;
                if(isAdded){
                requireActivity().runOnUiThread {
                    wallhaven_adabter?.removeLoadingView();
                    bottomloading?.setLoaded();
                    if (failedFirstLoading) {
                        hideloading();
                    }
                    }
                }
            }
        )
    }



    override fun onImageClick(Pos: Int, thumbnail: Drawable,loaded : Boolean) {
        try{
            lastPastImageInfo =  wallhaven_api.wallhavenApi!!.wallhaven_homepage_posts[Pos];
            lastPastImageInfo_pos = Pos;
            var intent = Intent(requireContext(), Image_Activity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            Image_Activity.MYDATA = wallhaven_api.wallhavenApi!!.wallhaven_homepage_posts[Pos];
            Image_Activity.THUMBNAIL = thumbnail;
            Image_Activity.postmode = Image_Activity.mode.wallhaven;
            Image_Activity.loadedPreview = loaded;
            startActivity(intent);
        }catch (e:Exception){
            Log.e("wallhaven_posts",e.toString())
        }
    }

    override fun onDestroy() {
        super.onDestroy();
        Log.d("DestoryLog",this::class.java.simpleName);
    }


}