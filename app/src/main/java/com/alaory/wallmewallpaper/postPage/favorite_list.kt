package com.alaory.wallmewallpaper.postPage

import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.alaory.wallmewallpaper.*
import com.alaory.wallmewallpaper.adabter.Image_list_adapter


class favorite_list(menuChange : MainActivity.MenuChange? = null) : Fragment(), Image_list_adapter.OnImageClick {

    val MenuChange : MainActivity.MenuChange? = menuChange;
    var favoriteList_adabter: Image_list_adapter? = null;
    var favoriteList_recycler: RecyclerView? = null;
    var mlayout : RecyclerView.LayoutManager? = null;
    var emptyCon : ConstraintLayout ? = null;

    var favoritelist = database.imageinfo_list.toMutableList();


    val TAG = "favorite_list";
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        favoriteList_adabter = Image_list_adapter(favoritelist,this);
        favoriteList_adabter!!.save_local_external = true;

        BottonLoading.loctionbottom = 0;
        MenuChange?.PlayAnimation_forNav {
            it?.translationY(0f);
        }
    }

    override fun onResume() {
        super.onResume();
        favoritelist = database.imageinfo_list.toMutableList();
        refrech_adabter();
        if(database.imageinfo_list.isEmpty()){
            emptyCon?.visibility = View.VISIBLE;
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig);
        wallmewallpaper.checkorein();
        setLayout();
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
        mainview.findViewById<TextView>(R.id.loading_text).visibility = View.GONE;
        emptyCon = mainview.findViewById(R.id.EmptyCon);

        if(database.imageinfo_list.isEmpty()){
            emptyCon?.visibility = View.VISIBLE;
        }



        setLayout();
        return mainview;
    }
    private fun setLayout(){
        mlayout = StaggeredGridLayoutManager( wallmewallpaper.num_post_in_Column,StaggeredGridLayoutManager.VERTICAL);
        favoriteList_recycler!!.adapter = favoriteList_adabter;
        favoriteList_recycler!!.layoutManager = mlayout;
        favoriteList_recycler!!.setHasFixedSize(false);
    }


    fun refrech_adabter(){
        favoriteList_adabter!!.listPosts = favoritelist;
        requireActivity().runOnUiThread {
            favoriteList_adabter!!.notifyDataSetChanged();
            favoriteList_recycler!!.invalidate();
        }
    }


    override fun onImageClick(Pos: Int, thumbnail: Drawable?,loaded : Boolean) {
        try {
            val intent = Intent(requireContext(), Image_Activity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            Image_Activity.THUMBNAIL = thumbnail;
            Image_Activity.MYDATA = database.imageinfo_list[Pos];
            Image_Activity.postmode = Image_Activity.mode.reddit;
            Image_Activity.save_local_external = true;
            Image_Activity.loadedPreview = loaded;
            startActivity(intent);
        }catch (e : Exception){
            Log.e(TAG,e.toString());
        }
    }

    override fun onDestroy() {
        super.onDestroy();
        Log.d("DestoryLog",this::class.java.simpleName);
    }
}