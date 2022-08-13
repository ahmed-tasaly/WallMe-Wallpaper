package com.alaory.wallmewallpaper

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader





class MyAdab(onimageclick : MyAdab.OnImageClick): RecyclerView.Adapter<MyAdab.myviewholder>() {
    private var onimgclick = onimageclick;
    lateinit var context: Context;
    companion object{
        enum class Image_Mode {
            Reddit
        }
        var Current_mode: Image_Mode = Image_Mode.Reddit;
    }
    //on create the image
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): myviewholder {
        val item_view = LayoutInflater.from(parent.context).inflate(R.layout.image_scrolable,parent,false);
        this.context = parent.context;
        return myviewholder(item_view);
    }

    //set image data
    override fun onBindViewHolder(mytypes: myviewholder, position: Int) {
        if(Current_mode == Image_Mode.Reddit){
            val request = coil.request.ImageRequest.Builder(this.context)
                .data(Reddit_Api.reddit_global_posts.get(position).Image_thumbnail)
                .target(mytypes.image_main)
                .placeholder(R.drawable.image_placeholder)
                .fallback(com.google.android.material.R.drawable.ic_mtrl_chip_close_circle)
                .listener(
                    onSuccess = {_,_ ->
                        mytypes.cricle_prograssBar.visibility = View.INVISIBLE;
                    },
                    onCancel = {
                        mytypes.cricle_prograssBar.visibility = View.INVISIBLE;
                    },
                    onError = {_,_ ->
                        mytypes.cricle_prograssBar.visibility = View.INVISIBLE;
                    },
                    onStart = {
                        mytypes.cricle_prograssBar.visibility = View.VISIBLE;
                    }

                )
                .build()
            var resu = ImageLoader(this.context).enqueue(request);
        }

        mytypes.root_view.setOnClickListener {
            onimgclick.onImageClick(position,mytypes.image_main.drawable);
        }
    }

    //get list size
    override fun getItemCount(): Int {
        if(Current_mode == Image_Mode.Reddit){
            return Reddit_Api.reddit_global_posts.size;
        }
        return  0;
    }
    //update list and recycleview
    fun refresh_itemList(){
        if(Current_mode == Image_Mode.Reddit)
            notifyItemInserted(Reddit_Api.last_index);
    }

    //what does the view hold
    class myviewholder(myview: View) : RecyclerView.ViewHolder(myview){
        //define xml types here ;)
        var root_view = itemView.findViewById(R.id.root_imageView) as ConstraintLayout;
        var image_main = itemView.findViewById(R.id.image_main) as ImageView;
        var cricle_prograssBar = itemView.findViewById(R.id.cricle_prograssBar) as ProgressBar;

    }

    //what to do when clicked
    interface OnImageClick{
        fun onImageClick(Pos: Int,thumbnail: Drawable);
    }


}