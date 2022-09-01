package com.alaory.wallmewallpaper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.drawable.toDrawable
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import coil.ImageLoader
import okhttp3.internal.wait

class Image_list_adapter(var listPosts: MutableList<Image_Info>, onimageclick : OnImageClick): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val VIEW_TYPE_LOADING = 1;
    val VIEW_TYPE_ITEM = 0;

    var LoadingIndex = -1;

    var imgclick = onimageclick;
    var context: Context? = null;


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        this.context = parent.context;
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
                width = (30 * ratio).toInt() ;
                height = 30;
            }
            val tempBitmap : Bitmap = Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);

            val request = coil.request.ImageRequest.Builder(this.context!!)
                .data(listPosts.get(position).Image_thumbnail)
                .target(holder.image_main)
                .placeholder(tempBitmap.toDrawable(context!!.resources))
                .listener(
                    onSuccess = {_,_ ->
                        holder.cricle_prograssBar.visibility = View.INVISIBLE;
                    },
                    onCancel = {
                        holder.cricle_prograssBar.visibility = View.INVISIBLE;
                    },
                    onError = {_,_ ->
                        holder.cricle_prograssBar.visibility = View.INVISIBLE;
                    },
                    onStart = {
                        holder.cricle_prograssBar.visibility = View.VISIBLE;
                    }
                )
                .build()

            ImageLoader(this.context!!).enqueue(request);

            holder.root_view.setOnClickListener {
                imgclick.onImageClick(position,holder.image_main.drawable);
            }
        }else if(holder.itemViewType == VIEW_TYPE_LOADING){
            val layoutparams = holder.itemView.layoutParams as StaggeredGridLayoutManager.LayoutParams;
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
        var cricle_prograssBar = itemView.findViewById(R.id.cricle_prograssBar) as ProgressBar;
    }

    class LoadingViewHolder(view: View): RecyclerView.ViewHolder(view)


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
