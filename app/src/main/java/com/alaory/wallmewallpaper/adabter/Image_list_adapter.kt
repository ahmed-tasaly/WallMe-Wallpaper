package com.alaory.wallmewallpaper.adabter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Animatable
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.net.toFile
import androidx.core.os.HandlerCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.decode.VideoFrameDecoder
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.alaory.wallmewallpaper.*
import com.google.android.exoplayer2.util.UriUtil
import com.google.android.material.floatingactionbutton.FloatingActionButton
import okio.Path
import okio.Path.Companion.toPath


class Image_list_adapter(var listPosts: MutableList<Image_Info>, onimageclick : OnImageClick): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val VIEW_TYPE_LOADING = 1;
    val VIEW_TYPE_ITEM = 0;
    var LoadingIndex = -1;


    var imgclick = onimageclick;
    var context: Context? = null;

    var adab_ImageLoader : ImageLoader? = null;
    var blockDatabase : database? = null;
    var favoriteDatabase : database? = null;

    var lastLongpressedItem : PostItemView? = null;

    var save_local_external = false;
    var handler: Handler? = null;

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        if(blockDatabase ==null && favoriteDatabase == null){
            blockDatabase = database(recyclerView.context,database.ImageBlock_Table,"${database.ImageBlock_Table}.dp");
            favoriteDatabase = database(recyclerView.context);
        }



        this.context = recyclerView.context;

        handler = HandlerCompat.createAsync(this.context!!.mainLooper)

        var path  = recyclerView.context!!.cacheDir.resolve("imagePreview").path.toPath();



        adab_ImageLoader = ImageLoader.Builder(recyclerView.context!!)
            .allowRgb565(true)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .bitmapFactoryMaxParallelism(4)
            .networkCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .allowHardware(false)
            .crossfade(true)
            //.logger(DebugLogger())
            .components {
                add(VideoFrameDecoder.Factory())
                if (android.os.Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }

            }
            .memoryCache {
                MemoryCache.Builder(recyclerView.context!!)
                    .maxSizePercent(0.15)
                    .build()
            }
            .diskCache {
                if(save_local_external){
                    path = this.context!!.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!.path.toPath();
                    DiskCache.Builder()
                        .directory(path)
                        .maxSizePercent(0.5)
                        .build();
                }else{
                    DiskCache.Builder()
                        .directory(path)
                        .maxSizePercent(0.1)
                        .build();
                }
            }
            .networkObserverEnabled(false)
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
        var tempBitmap : Bitmap? = Bitmap.createBitmap(holder.imageRatio.Width,holder.imageRatio.Height,Bitmap.Config.ALPHA_8);
        var tempDrawable: BitmapDrawable? = tempBitmap?.toDrawable(context!!.resources);

        val request = ImageRequest.Builder(this.context!!)
            .data(listPosts.get(holder.pos).Image_thumbnail)
            .placeholder(tempDrawable)
            .target(holder.image_main!!)
            .allowHardware(false)
            .listener(
                onSuccess = {req,res ->
                    holder.cricle_prograssBar.visibility = View.GONE;
                    holder.cricle_prograssBar.setImageDrawable(null);
                    holder.loaded = true;
                    tempDrawable = null;
                    tempBitmap = null;
                },
                onCancel = {
                    holder.cricle_prograssBar.visibility = View.GONE;
                    holder.cricle_prograssBar.setImageDrawable(null);
                },
                onError = {errres,imgreq ->


                    holder.cricle_prograssBar.visibility = View.GONE;
                    holder.cricle_prograssBar.setImageDrawable(null);
                    tempDrawable = null;
                    tempBitmap = null;
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


            holder.cricle_prograssBar.setImageDrawable(null);
            holder.image_main?.setImageDrawable(null);
            holder.root_view.setOnClickListener(null);


        }else{
            val holder = holder as LoadingViewHolder;
            holder.cricle_prograssBar.setImageDrawable(null);
        }
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        super.onViewAttachedToWindow(holder)
        var loadingdraw : AnimatedVectorDrawable? = null;
        if(holder.itemViewType == VIEW_TYPE_ITEM) {
            val holder = holder as PostItemView;
            loadingdraw = ResourcesCompat.getDrawable(this.context!!.applicationContext!!.resources,R.drawable.loading_anim,this.context!!.applicationContext.theme) as AnimatedVectorDrawable;
            holder.cricle_prograssBar.setImageDrawable(loadingdraw);
            loadingdraw.start();
            holder.root_view.setOnClickListener {
                holder.buttonframe.visibility = View.GONE;
                adab_ImageLoader!!.memoryCache!!.clear();
                imgclick.onImageClick(holder.layoutPosition,holder.image_main?.drawable,holder.loaded);
            }
            val uriInfo = Uri.parse(listPosts[holder.layoutPosition].Image_url)
            if(uriInfo.scheme == "content"){
                holder.cricle_prograssBar.visibility = View.GONE;
                holder.cricle_prograssBar.setImageDrawable(null);

                var updateCallback : (bitmap : Bitmap?,daw : Drawable?) -> Unit= { bit , draw ->
                    handler?.post {
                        if(bit != null)
                            holder.image_main?.setImageBitmap(bit);
                        else
                            holder.image_main?.setImageDrawable(draw);
                    }
                }

                val imageresRunnable = Runnable {
                    var postbitmap : Bitmap? = null;
                    var postdrawable : Drawable? = null;
                    if(uriInfo.scheme == "content"){
                        val contentres = this.context?.contentResolver!!;

                        when(listPosts[holder.layoutPosition].type){
                            UrlType.Video ->{
                                if(Build.VERSION.SDK_INT > 28){
                                    try {
                                        postbitmap = this.context!!.contentResolver.loadThumbnail(uriInfo,android.util.Size(480,480),null);
                                    }catch (e : Exception){
                                        Log.e(this@Image_list_adapter::class.java.simpleName, e.toString());
                                        try {//in case of contentresolver failer
                                            val mediaret = MediaMetadataRetriever();
                                            mediaret.setDataSource(contentres.openFileDescriptor(uriInfo,"r")!!.fileDescriptor);

                                            postbitmap = mediaret.frameAtTime
                                        } catch (ee : Exception){
                                            Log.e(this@Image_list_adapter::class.java.simpleName,ee.toString());
                                        }
                                    }
                                }else{//is sdk 28 and below
                                    try {
                                        val mediaret = MediaMetadataRetriever();
                                        mediaret.setDataSource(contentres.openFileDescriptor(uriInfo,"r")!!.fileDescriptor);

                                        postbitmap = mediaret.frameAtTime
                                    }catch (ee : Exception){
                                        Log.e(this@Image_list_adapter::class.java.simpleName,ee.toString());
                                    }

                                }

                            }
                            else ->{//is a gif or an image
                                try {
                                    postdrawable = Drawable.createFromStream(contentres.openInputStream(uriInfo),listPosts[holder.layoutPosition].Image_name);

                                    (postdrawable as? Animatable)?.start();
                                }catch (ee : Exception){
                                    Log.e(this@Image_list_adapter::class.java.simpleName,ee.toString());
                                }

                            }
                        }
                    }else{//is a "file" scheme
                        try{
                            val inputstreamfile = uriInfo.toFile().inputStream();
                            postbitmap = BitmapFactory.decodeStream(inputstreamfile);
                            inputstreamfile.close();
                        }catch (ee : Exception){
                            Log.e(this@Image_list_adapter::class.java.simpleName,ee.toString());
                        }
                    }
                    updateCallback(postbitmap,postdrawable);
                }

                wallmewallpaper.executor.execute {
                    imageresRunnable.run();
                }
            }

        }else{
            val holder = holder as LoadingViewHolder;
            loadingdraw = ResourcesCompat.getDrawable(this.context!!.applicationContext!!.resources,R.drawable.loading_anim,this.context!!.applicationContext.theme) as AnimatedVectorDrawable;
            holder.cricle_prograssBar.setImageDrawable(loadingdraw);
            loadingdraw.start();
        }
    }




    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(holder.itemViewType == VIEW_TYPE_ITEM){
            val holder = holder as PostItemView;


            var width = 1;
            var height = 1;
            val currentpost = listPosts.get(position);
            currentpost.imageRatio?.let {
                val imageRatio = it;
                val ratio = imageRatio.Width.toFloat() / imageRatio.Height.toFloat()
                width = (50 * ratio).toInt() ;
                height = 50;
            }


            holder.pos = position;
            holder.imageRatio = Image_Ratio(width,height);

            //set image type to show the user
            if(currentpost.type != UrlType.Image){
                holder.texttype.visibility = View.VISIBLE;
                holder.texttype.setText(currentpost.type.name.uppercase());
            }else{
                holder.texttype.visibility = View.INVISIBLE;
            }


            val pathuri = Uri.parse(currentpost.Image_url);
            if(pathuri.scheme != "content" && pathuri.scheme != "file"){
                adab_ImageLoader?.let {
                    it.enqueue(getImagerequest(holder));
                }
            }


            holder.root_view.setOnLongClickListener {
                lastLongpressedItem?.buttonframe?.visibility = View.GONE;
                lastLongpressedItem = holder;

                if(holder.buttonframe.isVisible){
                    holder.buttonframe.visibility = View.GONE;
                }else{
                    var found = false;
                    for(i in database.imageinfo_list){
                        if(i.Image_url == currentpost.Image_url){
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
            if(i.Image_url == listPosts.get(position).Image_url){
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
            if(i.Image_url == listPosts.get(position).Image_url){
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
        var image_main = itemView.findViewById(R.id.image_main) as? ImageView;
        var cricle_prograssBar = itemView.findViewById(R.id.cricle_prograssBar) as ImageView;
        var favoriteButton = itemView.findViewById(R.id.favorite_scrollable_floatingbutton) as FloatingActionButton;
        var blockButton = itemView.findViewById(R.id.block_scrollable_floatingbutton) as FloatingActionButton;
        var buttonframe = itemView.findViewById(R.id.frame_scrollable_floatingbutton) as FrameLayout
        var texttype = itemView.findViewById(R.id.text_type) as TextView;
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
        if(position == -1)
            return;
        this.notifyItemRemoved(position);
        listPosts.removeAt(position);
        if(position -1 >= 0)
            this.notifyItemRangeChanged(position-1,listPosts.size);
        else
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
        fun onImageClick(Pos: Int,thumbnail: Drawable?,loaded : Boolean = false);
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        Log.d("DestoryLog",this::class.java.simpleName);

        adab_ImageLoader?.memoryCache?.clear();
        adab_ImageLoader?.shutdown();
    }


}
