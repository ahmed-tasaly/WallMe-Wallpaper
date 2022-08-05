package com.wallme.wallpaper

import android.app.WallpaperManager
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.set
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import java.lang.Exception


class get_bitmap: Target{
    var mybitmap : Bitmap = Bitmap.createBitmap(1000,1000,Bitmap.Config.RGB_565);
    override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
        if(bitmap != null){
            mybitmap = bitmap;
        }
    }

    override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
        mybitmap.set(100,200,Color.WHITE);
    }

    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
        mybitmap = placeHolderDrawable!!.toBitmap()

    }

}

class Image_Activity(): AppCompatActivity(){
    //private lateinit var pageInfo: List_image;
    private lateinit var Image_name_text: TextView;
    private lateinit var Auther_text: TextView;
    private lateinit var Full_image: ImageView;
    private lateinit var Loadimage : get_bitmap;

    companion object{
        lateinit var myData : List_image;
    }

    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle);
        setContentView(R.layout.show_image_fs);
        Image_name_text = findViewById<TextView>(R.id.Image_name_text);
        Auther_text = findViewById<TextView>(R.id.auther_text);
        Full_image = findViewById<ImageView>(R.id.full_image);
        Image_name_text.setText(myData.Image_name);
        Auther_text.setText(myData.Image_auther);
        Picasso.get().load(myData.Image_url).into(Full_image);


        Loadimage = get_bitmap();
        Picasso.get().load(myData.Image_url).placeholder(com.google.android.material.R.drawable.ic_m3_chip_checked_circle).into(Loadimage);

        findViewById<ImageButton>(R.id.set_imageButton).setOnClickListener { setWallpaper(); };

    }

    private fun setWallpaper(){

        var wall: WallpaperManager = WallpaperManager.getInstance(this);
        wall.setBitmap(Loadimage.mybitmap);

    }

}








class MyAdab(list_ : Array<List_image>,onimageclick : MyAdab.OnImageClick): RecyclerView.Adapter<MyAdab.myviewholder>() {
    var ItemsList: Array<List_image> = list_;
    private var onimgclick = onimageclick;

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): myviewholder {
        val item_view = LayoutInflater.from(parent.context).inflate(R.layout.image_scrolable,parent,false);
        return myviewholder(item_view);
    }

    override fun onBindViewHolder(mytypes: myviewholder, position: Int) {
        Picasso.get().load(ItemsList[position].Image_thumbnail).placeholder(com.google.android.material.R.drawable.ic_m3_chip_checked_circle).into(mytypes.image_main);
        mytypes.root_view.setOnClickListener {
            onimgclick.onImageClick(position);
        }
    }

    override fun getItemCount(): Int {
        return ItemsList.size;
    }

    fun refresh_itemList(Updated_list : Array<List_image>){
        var lastIndex = itemCount;
        if(Updated_list.isNotEmpty()){
            ItemsList += Updated_list;
            notifyItemInserted(lastIndex);
        }

    }

    class myviewholder(myview: View) : RecyclerView.ViewHolder(myview){
        //define xml types here ;)
        var root_view = itemView.findViewById<ConstraintLayout>(R.id.root_imageView);
        var image_main = itemView.findViewById<ImageView>(R.id.image_main);
    }

    interface OnImageClick{
        fun onImageClick(Pos: Int);
    }


}