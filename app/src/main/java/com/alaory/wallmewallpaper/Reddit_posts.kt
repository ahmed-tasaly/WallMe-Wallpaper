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

class Reddit_posts : Fragment(), Image_list_adapter.OnImageClick {

    private var myrec: RecyclerView? = null;
    private var PostsAdabter: Image_list_adapter? = null;
    private var mLayoutManager : RecyclerView.LayoutManager? = null;
    var scrollListener : BottonLoading.ViewLodMore? = null;

    var imageloading: ImageView? =null;
    var textloading: TextView? = null;
    var buttonLoading: Button? =null;

    var appfirstneedLoading = false;

    companion object{
         var firsttime = true;
         var userHitSave = false;
    }


    var reddit_api : Array<Reddit_Api> = emptyArray();



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);



        if(firsttime || userHitSave){
            Reddit_Api.reddit.dispatcher.cancelAll();
            Log.i("Reddit_posts","i have beeen created");
            reddit_api = emptyArray();
            Reddit_Api.Subreddits = emptyArray();
            Reddit_Api.reddit_global_posts = emptyList<Image_Info>().toMutableList();

            for (i in Reddit_settings.subreddits_list_names){
                reddit_api += Reddit_Api(i);
            }
            PostsAdabter = Image_list_adapter(Reddit_Api.reddit_global_posts,this);
            LoadMore();
            firsttime = false;
            userHitSave = false;
        }

        if(Resources.getSystem().configuration.orientation != MainActivity.last_orein)
            LoadMore();

        if(PostsAdabter == null)
            PostsAdabter = Image_list_adapter(Reddit_Api.reddit_global_posts,this);

    }


    override fun onDetach() {
        super.onDetach()
        PostsAdabter!!.removeLoadingView();
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val layoutfragment = inflater.inflate(R.layout.postlist_mainwindow, container, false);


        myrec = layoutfragment.findViewById(R.id.fragmentrec) as RecyclerView;
        SetRVLayoutManager();
        SetRvScrollListener();


        imageloading =layoutfragment.findViewById(R.id.loading_recyclye);
        textloading = layoutfragment.findViewById(R.id.loading_text);
        buttonLoading = layoutfragment.findViewById(R.id.load_failer_button);

        buttonLoading!!.setOnClickListener {
            LoadMore();
            buttonLoading!!.visibility = View.GONE;
            imageloading!!.visibility = View.VISIBLE;
            textloading!!.visibility = View.VISIBLE;
            appfirstneedLoading = false;
        }

        if(appfirstneedLoading){
            buttonLoading!!.visibility = View.VISIBLE;
            imageloading!!.visibility = View.GONE
            textloading!!.visibility = View.GONE
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
        scrollListener = BottonLoading.ViewLodMore(mLayoutManager as StaggeredGridLayoutManager);
        scrollListener!!.setOnLoadMoreListener(object : BottonLoading.OnLoadMoreListener{
            override fun onLoadMore() {
                LoadMore();
                PostsAdabter!!.addLoadingView();
            }
        })
        myrec!!.addOnScrollListener(scrollListener!!);
    }



    fun LoadMore(){
            Reddit_Api.get_shuffle_andGive { Status ->
                if(Status == 400)
                    appfirstneedLoading = true;

                if(isAdded) {
                    if(Status == 400){
                        requireActivity().runOnUiThread {
                            if(appfirstneedLoading){
                                buttonLoading!!.visibility = View.VISIBLE;
                                imageloading!!.visibility = View.GONE
                                textloading!!.visibility = View.GONE
                            }
                            PostsAdabter!!.removeLoadingView();
                            scrollListener?.setLoaded();
                        }
                        return@get_shuffle_andGive;
                    }
                    requireActivity().runOnUiThread {
                        PostsAdabter?.removeLoadingView();
                        scrollListener?.setLoaded();
                        PostsAdabter?.refresh_itemList(Reddit_Api.reddit_global_posts.lastIndex);
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