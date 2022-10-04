package com.alaory.wallmewallpaper.postPage

import android.content.DialogInterface
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
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.alaory.wallmewallpaper.*
import com.alaory.wallmewallpaper.adabter.Image_list_adapter
import com.alaory.wallmewallpaper.api.Reddit_Api
import com.alaory.wallmewallpaper.settings.Reddit_settings

class Reddit_posts(menuChange : MainActivity.MenuChange? = null) : Fragment(), Image_list_adapter.OnImageClick {

    private val MenuChange : MainActivity.MenuChange? = menuChange;
    var myrec: RecyclerView? = null;
    var PostsAdabter: Image_list_adapter? = null;
    var mLayoutManager : RecyclerView.LayoutManager? = null;
    var scrollListener : BottonLoading.ViewLodMore? = null;

    var imageloading: ImageView? =null;
    var textloading: TextView? = null;
    var buttonLoading: Button? =null;

    var failedFirstLoading = false;



    companion object{
         var firsttime = true;
         var userHitSave = false;
         var lastPastImageInfo : Image_Info? = null;
         var lastPastImageInfo_pos : Int = -1;
    }


    var reddit_api : Array<Reddit_Api> = emptyArray();



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);

        if(PostsAdabter == null)
            PostsAdabter = Reddit_Api.redditcon?.reddit_global_posts?.let { Image_list_adapter(it,this) };

        if(firsttime || userHitSave){
            Log.i("Reddit_posts","i have beeen created");
            reddit_api = emptyArray();
            Reddit_Api.redditcon?.Subreddits = emptyArray();
            Reddit_Api.redditcon?.reddit_global_posts = emptyList<Image_Info>().toMutableList();

            for (i in Reddit_settings.subreddits_list_names){
                reddit_api += Reddit_Api(i);
            }
            PostsAdabter = Reddit_Api.redditcon?.reddit_global_posts?.let { Image_list_adapter(it,this) };
            LoadMore();
            firsttime = false;
            userHitSave = false;
        }

        if(Resources.getSystem().configuration.orientation != MainActivity.last_orein)
            LoadMore();

    }


    override fun onDetach() {
        super.onDetach()
        PostsAdabter?.removeLoadingView();
    }
    override fun onResume() {
        super.onResume()
        if(lastPastImageInfo != null && database.lastblockedaddedImageInfo != null){
            if(lastPastImageInfo!!.Image_name == database.lastblockedaddedImageInfo!!.Image_name){
                PostsAdabter!!.notifyDataSetChanged();
                Reddit_Api.redditcon!!.reddit_global_posts.removeAt(lastPastImageInfo_pos);
                lastPastImageInfo = null;
            }
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


        myrec = layoutfragment.findViewById(R.id.fragmentrec) as RecyclerView;
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

        MainActivity.setImageView_asLoading(imageloading);

        //for screen rotaion
        if(Resources.getSystem().configuration.orientation != MainActivity.last_orein)
            LoadMore();

        return layoutfragment;
    }




    private fun SetRVLayoutManager(){
        mLayoutManager = StaggeredGridLayoutManager(MainActivity.num_post_in_Column,StaggeredGridLayoutManager.VERTICAL);
        myrec!!.layoutManager = mLayoutManager;
        myrec!!.setHasFixedSize(true);
        myrec?.adapter = PostsAdabter;
    }



    private fun SetRvScrollListener(){
        scrollListener =  BottonLoading.ViewLodMore(mLayoutManager as StaggeredGridLayoutManager, MenuChange);
        scrollListener!!.setOnLoadMoreListener(object : BottonLoading.OnLoadMoreListener {
            override fun onLoadMore() {
                myrec?.post {
                    LoadMore();
                    PostsAdabter!!.addLoadingView();
                }
            }
        })
        myrec!!.addOnScrollListener(scrollListener!!);
    }



    fun LoadMore(){
        if(isAdded){
            requireActivity().runOnUiThread {
                showloading();
            }
        }
        Reddit_Api.redditcon?.get_allposts_andGive { Status ->
            if(Status == 400)
                failedFirstLoading = true;

            if(isAdded) {
                if(Status == 400){
                    requireActivity().runOnUiThread {
                        PostsAdabter!!.removeLoadingView();
                        scrollListener?.setLoaded();
                        if(failedFirstLoading){
                            hideloading();
                        }
                    }
                    return@get_allposts_andGive;
                }
                requireActivity().runOnUiThread {
                    PostsAdabter?.removeLoadingView();
                    scrollListener?.setLoaded();
                    PostsAdabter?.refresh_itemList(Reddit_Api.redditcon!!.reddit_global_posts.lastIndex);
                    disableloading();
                }
            }
        }
    }


    override fun onImageClick(Pos: Int,thumbnail : Drawable,loaded : Boolean) {
        try {
            lastPastImageInfo = Reddit_Api.redditcon!!.reddit_global_posts.get(Pos);
            lastPastImageInfo_pos = Pos;
            val intent = Intent(requireContext(), Image_Activity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            Image_Activity.MYDATA = Reddit_Api.redditcon!!.reddit_global_posts.get(Pos);
            Image_Activity.THUMBNAIL = thumbnail;
            Image_Activity.postmode = Image_Activity.mode.reddit;
            Image_Activity.loadedPreview = loaded;
            startActivity(intent);
        }catch (e: Exception){
            Log.e("Reddit_posts","error while trying to set image activity")
        }
    }
    override fun onDestroy() {
        super.onDestroy();
        Log.d("DestoryLog",this::class.java.simpleName);
    }


}