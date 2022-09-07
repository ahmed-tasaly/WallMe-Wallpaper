package com.alaory.wallmewallpaper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Animatable2
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.ViewAnimator
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.drawable.toDrawable
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.alaory.wallmewallpaper.interpreter.progressRespondBody
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Runnable
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.internal.wait
import okio.Path.Companion.toPath
import kotlin.coroutines.CoroutineContext

class Image_list_adapter(var listPosts: MutableList<Image_Info>, onimageclick : OnImageClick): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val VIEW_TYPE_LOADING = 1;
    val VIEW_TYPE_ITEM = 0;

    var LoadingIndex = -1;

    var imgclick = onimageclick;
    var context: Context? = null;

    val TAG = "Image_list_adapter";

    var adab_ImageLoader : ImageLoader? = null;

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.context = recyclerView.context;
        adab_ImageLoader = ImageLoader.Builder(recyclerView.context!!)
            .memoryCache {
                MemoryCache.Builder(recyclerView.context!!)
                    .maxSizePercent(0.2)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory( "${recyclerView.context!!.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!}/Preview_Image".toPath())
                    .maxSizePercent(0.2)
                    .build();
            }
            .okHttpClient {
                OkHttpClient().newBuilder()
                    .addInterceptor(object : Interceptor{
                        override fun intercept(chain: Interceptor.Chain): Response {

                            val orginalres = chain.proceed(chain.request());
                            Log.i("OkHttpClient",orginalres.body!!.contentLength().toString());
                            return orginalres.newBuilder()
                                .body(progressRespondBody(orginalres.body!!))
                                .build()
                        }
                    })
                    .build()
            }
            .build();

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if(viewType == VIEW_TYPE_ITEM){
            val itemtoView = LayoutInflater.from(parent.context).inflate(R.layout.image_scrolable,parent,false);
            return ItemViewHolder(itemtoView);
        }else{
            val itemtoload = LayoutInflater.from(parent.context).inflate(R.layout.bottomloading_prograssbar,parent,false);
            return LoadingViewHolder(itemtoload);
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(holder.itemViewType == VIEW_TYPE_ITEM){
            val holder = holder as ItemViewHolder;

            var width = 1;
            var height = 1;
            if(listPosts.get(position).imageRatio != null){
                val imageRatio = listPosts.get(position).imageRatio!!;
                val ratio = imageRatio.Width.toFloat() / imageRatio.Height.toFloat()
                width = (50 * ratio).toInt() ;
                height = 50;
            }
            val tempBitmap : Bitmap = Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);

            MainActivity.setImageView_asLoading(holder.cricle_prograssBar);



            val request = coil.request.ImageRequest.Builder(this.context!!)
                .data(listPosts.get(position).Image_thumbnail)
                .target(holder.image_main)
                .placeholder(tempBitmap.toDrawable(context!!.resources))
                .listener(
                    onSuccess = {_,_ ->
                        holder.cricle_prograssBar.visibility = View.GONE;
                        tempBitmap.recycle();
                    },
                    onCancel = {
                        holder.cricle_prograssBar.visibility = View.GONE;
                    },
                    onError = {_,_ ->
                        holder.cricle_prograssBar.visibility = View.GONE;
                    },
                    onStart = {
                        holder.cricle_prograssBar.visibility = View.VISIBLE;
                    }
                )
                .build()

            adab_ImageLoader.let {
                it?.enqueue(request);
            }


            holder.root_view.setOnClickListener {
                imgclick.onImageClick(position,holder.image_main.drawable);
            }
        }else if(holder.itemViewType == VIEW_TYPE_LOADING){
            val holder = holder as LoadingViewHolder;
            val layoutparams = holder.itemView.layoutParams as StaggeredGridLayoutManager.LayoutParams;
            MainActivity.setImageView_asLoading(holder.cricle_prograssBar);
            layoutparams.isFullSpan = true;
        }

    }


    fun refresh_itemList(LastIndex : Int){
        try {
            //notifyDataSetChanged();//i really hate this. but i got no idea on how to solve it...Unless YOU yes YOUUUUU do you know a solution :)
            notifyItemInserted(LastIndex);
        }catch (e:Exception){
            Log.i("Image_list_adapter",e.toString());
        }

    }


    class ItemViewHolder(view : View) : RecyclerView.ViewHolder(view) {
        var root_view = itemView.findViewById(R.id.root_imageView) as ConstraintLayout;
        var image_main = itemView.findViewById(R.id.image_main) as ImageView;
        var cricle_prograssBar = itemView.findViewById(R.id.cricle_prograssBar) as ImageView;
    }

    class LoadingViewHolder(view: View): RecyclerView.ViewHolder(view){
        var cricle_prograssBar = itemView.findViewById(R.id.bottomloading_prograssCricle) as ImageView;
    }


    fun addLoadingView(){
        if(LoadingIndex != -1)
            removeLoadingView();

        listPosts.add(
            Image_Info("LOADING","")
        );
        LoadingIndex = listPosts.lastIndex;
        refresh_itemList(listPosts.lastIndex);
    }

    fun removeLoadingView(){
        if(LoadingIndex != -1){
            listPosts.removeAt(LoadingIndex);
            notifyItemRemoved(LoadingIndex);
            LoadingIndex = -1;
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if(listPosts[position].Image_url == "LOADING"){
            VIEW_TYPE_LOADING;
        }else{
            VIEW_TYPE_ITEM;
        }
    }


    override fun getItemCount(): Int {
        return listPosts.size;
    }


    interface OnImageClick{
        fun onImageClick(Pos: Int,thumbnail: Drawable);
    }


}
