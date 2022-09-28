package com.alaory.wallmewallpaper.postPage

import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.alaory.wallmewallpaper.*
import com.alaory.wallmewallpaper.adabter.Image_list_adapter


class favorite_list() : Fragment(), Image_list_adapter.OnImageClick {

    var favoriteList_adabter: Image_list_adapter? = null;
    var favoriteList_recycler: RecyclerView? = null;
    var mlayout : RecyclerView.LayoutManager? = null;
    var textmain : TextView? = null;

    val TAG = "favorite_list";
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        favoriteList_adabter = Image_list_adapter(database.imageinfo_list.toMutableList(),this);
        BottonLoading.loctionbottom = 0;
        BottonLoading.updatebottom_navtigation(0);
    }

    override fun onResume() {
        super.onResume();
        refrech_adabter();
        if(database.imageinfo_list.isEmpty()){
            textmain!!.visibility = View.VISIBLE;
            textmain!!.text = "Empty 'woosh' ;)"
        }
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

        val mainview = inflater.inflate(R.layout.postlist_mainwindow, container, false);
        favoriteList_recycler = mainview.findViewById(R.id.fragmentrec);
        mainview.findViewById<ImageView>(R.id.loading_recyclye).visibility = View.INVISIBLE;
        textmain = mainview.findViewById<TextView>(R.id.loading_text);


        if(database.imageinfo_list.isNotEmpty())
            textmain!!.visibility = View.INVISIBLE;
        else{
            textmain!!.text = "Empty 'woosh' ;)"
        }


        setLayout();
        return mainview;
    }
    private fun setLayout(){
        mlayout = StaggeredGridLayoutManager(MainActivity.num_post_in_Column,StaggeredGridLayoutManager.VERTICAL);
        favoriteList_recycler!!.adapter = favoriteList_adabter;
        favoriteList_recycler!!.layoutManager = mlayout;
        favoriteList_recycler!!.setHasFixedSize(true);
    }

    fun refrech_adabter(){
        requireActivity().runOnUiThread {
            favoriteList_adabter!!.notifyDataSetChanged();
        }
    }


    override fun onImageClick(Pos: Int, thumbnail: Drawable,loaded : Boolean) {
        try {
            val intent = Intent(requireContext(), Image_Activity::class.java);
            Image_Activity.THUMBNAIL = thumbnail;
            Image_Activity.MYDATA = database.imageinfo_list[Pos];
            Image_Activity.postmode = Image_Activity.mode.reddit;
            Image_Activity.loadedPreview = loaded;
            startActivity(intent);
        }catch (e : Exception){
            Log.e(TAG,e.toString());
        }
    }
}