package com.alaory.wallmewallpaper

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader

class Wallhaven_adab(var listPosts: MutableList<List_image>, onimageclick : OnImageClick): RecyclerView.Adapter<Wallhaven_adab.Post>() {


    var imgclick = onimageclick;


    lateinit var context: Context;


    class Post(view : View) : RecyclerView.ViewHolder(view) {
        var root_view = itemView.findViewById(R.id.root_imageView) as ConstraintLayout;
        var image_main = itemView.findViewById(R.id.image_main) as ImageView;
        var cricle_prograssBar = itemView.findViewById(R.id.cricle_prograssBar) as ProgressBar;
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Post {
        val itemtoView = LayoutInflater.from(parent.context).inflate(R.layout.image_scrolable,parent,false);
        this.context = parent.context;
        return Post(itemtoView);
    }

    override fun onBindViewHolder(holder: Post, position: Int) {
        val request = coil.request.ImageRequest.Builder(this.context)
            .data(listPosts!!.get(position).Image_thumbnail)
            .target(holder.image_main)
            .placeholder(R.drawable.image_placeholder)
            .fallback(com.google.android.material.R.drawable.ic_mtrl_chip_close_circle)
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
        var resu = ImageLoader(this.context).enqueue(request);

        holder.root_view.setOnClickListener {
            imgclick.onImageClick(position,holder.image_main.drawable);
        }
    }

    fun refresh_itemList(){
        notifyItemInserted(listPosts!!.lastIndex);
    }
    override fun getItemCount(): Int {
        return listPosts!!.size;
    }
    interface OnImageClick{
        fun onImageClick(Pos: Int,thumbnail: Drawable);
    }
}
