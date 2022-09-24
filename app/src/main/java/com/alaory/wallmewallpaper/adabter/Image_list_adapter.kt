package com.alaory.wallmewallpaper.adabter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.size.Precision
import com.alaory.wallmewallpaper.*
import com.google.android.material.floatingactionbutton.FloatingActionButton

class Image_list_adapter(var listPosts: MutableList<Image_Info>, onimageclick : OnImageClick): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val VIEW_TYPE_LOADING = 1;
    val VIEW_TYPE_ITEM = 0;
    var LoadingIndex = -1;


    var imgclick = onimageclick;
    var context: Context? = null;

    val TAG = "Image_list_adapter";

    var adab_ImageLoader : ImageLoader? = null;
    var blockDatabase : database? = null;
    var favoriteDatabase : database? = null;

    var lastLongpressedItem : PostItemView? = null;


    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        if(blockDatabase ==null && favoriteDatabase == null){
            blockDatabase = database(recyclerView.context,database.ImageBlock_Table,"${database.ImageBlock_Table}.dp");
            favoriteDatabase = database(recyclerView.context);
        }



        this.context = recyclerView.context;
        adab_ImageLoader = ImageLoader.Builder(recyclerView.context!!)
            .allowRgb565(true)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .precision(Precision.INEXACT)
            .bitmapFactoryMaxParallelism(6)
            .networkCachePolicy(CachePolicy.READ_ONLY)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .allowHardware(false)
            .crossfade(true)
            .memoryCache {
                MemoryCache.Builder(recyclerView.context!!)
                    .maxSizePercent(0.15)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory( recyclerView.context!!.cacheDir.resolve("imagePreview"))
                    .maxSizePercent(0.05)
                    .build();
            }
            .build();

    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if(viewType == VIEW_TYPE_ITEM){
            val itemtoView = LayoutInflater.from(parent.context).inflate(R.layout.image_scrolable,parent,false);
            return PostItemView(itemtoView);
        }else{
            val itemtoload = LayoutInflater.from(parent.context).inflate(R.layout.bottomloading_prograssbar,parent,false);
            return LoadingViewHolder(itemtoload);
        }

    }



    private fun getImagerequest(holder: PostItemView): ImageRequest{
        val tempBitmap : Bitmap = Bitmap.createBitmap(holder.imageRatio.Width,holder.imageRatio.Height,Bitmap.Config.ARGB_8888);
        val tempDrawable = tempBitmap.toDrawable(context!!.resources);
        val request = coil.request.ImageRequest.Builder(this.context!!)
            .data(listPosts.get(holder.pos).Image_thumbnail)
            .placeholder(tempDrawable)
            .target(holder.image_main)
            .listener(
                onSuccess = {_,_ ->
                    holder.cricle_prograssBar.visibility = View.GONE;
                    holder.loaded = true;
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

        return request;
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        super.onViewDetachedFromWindow(holder);
        if(holder.itemViewType == VIEW_TYPE_ITEM){
            val holder = holder as PostItemView;
            if(holder.buttonframe.isVisible)
                holder.buttonframe.visibility = View.GONE;
        }
    }





    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(holder.itemViewType == VIEW_TYPE_ITEM){
            val holder = holder as PostItemView;


            var width = 1;
            var height = 1;
            listPosts.get(position).imageRatio?.let {
                val imageRatio = it;
                val ratio = imageRatio.Width.toFloat() / imageRatio.Height.toFloat()
                width = (50 * ratio).toInt() ;
                height = 50;
            }

            MainActivity.setImageView_asLoading(holder.cricle_prograssBar);
            holder.pos = position;
            holder.imageRatio = Image_Ratio(width,height);

            adab_ImageLoader?.let {
                it.enqueue(getImagerequest(holder));
            }


            holder.root_view.setOnClickListener {
                holder.buttonframe.visibility = View.GONE;
                imgclick.onImageClick(position,holder.image_main.drawable,holder.loaded);
            }

            holder.root_view.setOnLongClickListener {
                lastLongpressedItem?.buttonframe?.visibility = View.GONE;
                lastLongpressedItem = holder;

                if(holder.buttonframe.isVisible){
                    holder.buttonframe.visibility = View.GONE;
                }else{
                    var found = false;
                    for(i in database.imageinfo_list){
                        if(i.Image_name == listPosts.get(position).Image_name){
                            found = true;
                        }
                    }
                    if(found)
                        holder.favoriteButton.setImageResource(R.drawable.ic_heartfull);
                    holder.buttonframe.visibility = View.VISIBLE;
                }
                return@setOnLongClickListener true;
            }

            holder.favoriteButton.setOnClickListener {
                FavoriteButton(holder);
            }

            holder.blockButton.setOnClickListener {
                blockButton(holder);
            }

            holder.root_view.startAnimation(AnimationUtils.loadAnimation(holder.itemView.context,R.anim.item_scroll_animation))

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



    fun FavoriteButton(holder: PostItemView){
        var found = false;
        val position = holder.layoutPosition;
        for(i in database.imageinfo_list){
            if(i.Image_name == listPosts.get(position).Image_name){
                found = true;
            }
        }

        if(!found){
            favoriteDatabase!!.add_image_info_to_database(listPosts.get(position));
            holder.favoriteButton.setImageResource(R.drawable.ic_heartfull);
        }
        else{
            favoriteDatabase!!.remove_image_info_from_database(listPosts.get(position));
            holder.favoriteButton.setImageResource(R.drawable.ic_favorite);
        }

    }
    fun blockButton(holder: PostItemView){
        var found = false;
        val position = holder.layoutPosition;
        for(i in database.imageblock_list){
            if(i.Image_name == listPosts.get(position).Image_name){
                found = true;
            }
        }

        if(!found){
            blockDatabase!!.add_image_info_to_database(listPosts.get(position));
            Toast.makeText(context,"added to the block list",Toast.LENGTH_SHORT).show();
            removeItem(position);
        }
        else{
            blockDatabase!!.remove_image_info_from_database(listPosts.get(position));
            Toast.makeText(context,"removed from  the block list",Toast.LENGTH_SHORT).show();
        }

    }


    class PostItemView(view : View) : RecyclerView.ViewHolder(view) {
        var pos = 0;
        var loaded = false;
        var imageRatio = Image_Ratio(1,1);
        var root_view = itemView.findViewById(R.id.root_imageView) as LinearLayout;
        var image_main = itemView.findViewById(R.id.image_main) as ImageView;
        var cricle_prograssBar = itemView.findViewById(R.id.cricle_prograssBar) as ImageView;
        var favoriteButton = itemView.findViewById(R.id.favorite_scrollable_floatingbutton) as FloatingActionButton;
        var blockButton = itemView.findViewById(R.id.block_scrollable_floatingbutton) as FloatingActionButton;
        var buttonframe = itemView.findViewById(R.id.frame_scrollable_floatingbutton) as FrameLayout
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

    fun removeItem(position : Int){
        this.notifyItemRemoved(position);
        listPosts.removeAt(position);
        this.notifyItemRangeChanged(position,listPosts.size);
    }



    fun removeLoadingView(){
        if(LoadingIndex != -1){
            removeItem(LoadingIndex);
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
        fun onImageClick(Pos: Int,thumbnail: Drawable,loaded : Boolean = false);
    }


}
