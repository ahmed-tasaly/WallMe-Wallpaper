package com.alaory.wallmewallpaper.settings

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.work.*
import com.alaory.wallmewallpaper.MainActivity
import com.alaory.wallmewallpaper.R
import com.alaory.wallmewallpaper.api.Reddit_Api
import com.alaory.wallmewallpaper.api.Reddit_Api_Contorller
import com.alaory.wallmewallpaper.wallmewallpaper
import com.alaory.wallmewallpaper.wallpaperChanger_Worker
import com.google.android.material.switchmaterial.SwitchMaterial
import java.util.concurrent.TimeUnit

class settings( menuChange : MainActivity.MenuChange? = null) : Fragment() {

    val MenuChange = menuChange;
    var backbutton : ImageButton? =null;


    //sources
    var wallhaven_source : SwitchMaterial? = null;
    var reddit_source : SwitchMaterial? = null;

    //screen
    var fullscreenapp : SwitchMaterial? = null;

    //cache
    var clearCache : LinearLayout? = null;
    var clearImages : LinearLayout? = null;

    //post list settings
    var Show_favorite_insearch : SwitchMaterial? = null;

    //about
    var export_data : TextView? = null;
    var import_data : TextView? = null;
    var github : TextView? = null;
    var supportMe : TextView? = null;


    val JOBID = 212;



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val layout = inflater.inflate(R.layout.fragment_settings, container, false);
        MenuChange?.Shownav(false);

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            MenuChange?.ChangeTo(MainActivity.menu.wallpaperchanger,true);
        }

        backbutton = layout.findViewById(R.id.backArrow_button);


        wallhaven_source = layout.findViewById(R.id.Switch_wallhaven_settings);
        reddit_source = layout.findViewById(R.id.Switch_reddit_settings);

        fullscreenapp = layout.findViewById(R.id.fullscreenapp_settings)

        clearCache = layout.findViewById(R.id.clear_cache_settings);
        clearImages = layout.findViewById(R.id.clear_saved_images_settings);

        Show_favorite_insearch = layout.findViewById(R.id.Switch_favorite_settings)

        export_data = layout.findViewById(R.id.Export_settings)
        import_data = layout.findViewById(R.id.Import_settings)
        github = layout.findViewById(R.id.github_settings);
        supportMe = layout.findViewById(R.id.support_settings);



        backbutton?.let {
            it.setOnClickListener {
                MenuChange?.ChangeTo(MainActivity.menu.wallpaperchanger,true);
            }
        }



        wallhaven_source?.let {
            it.isChecked = requireContext().getSharedPreferences("settings",Context.MODE_PRIVATE).getBoolean("wallhaven_source",false);
            it.setOnCheckedChangeListener { p0, ischecked ->
                val prefs = p0.context.getSharedPreferences("settings",Context.MODE_PRIVATE);
                prefs.edit().putBoolean("wallhaven_source",ischecked).apply();
                if(ischecked){
                    AlertDialog.Builder(p0.context,R.style.Dialog_first)
                        .setTitle("Wait ")
                        .setMessage("I've disabled this source by default because it may contain disturbing or sketchy images")
                        .setPositiveButton("enable",object : DialogInterface.OnClickListener{
                            override fun onClick(p0: DialogInterface?, p1: Int) {
                               MenuChange?.hidenavbuttons(MainActivity.menu.wallhaven,true);
                            }
                        })
                        .setNegativeButton("disable",object : DialogInterface.OnClickListener{
                            override fun onClick(p0: DialogInterface?, p1: Int) {
                                prefs.edit().putBoolean("wallhaven_source",false).apply();
                                it.isChecked = false;
                            }
                        })
                        .create()
                        .show()
                }else{
                    MenuChange?.hidenavbuttons(MainActivity.menu.wallhaven,false);
                }
            }
        }
        reddit_source?.let {
            it.isChecked = requireContext().getSharedPreferences("settings",Context.MODE_PRIVATE).getBoolean("reddit_source",true);
            it.setOnCheckedChangeListener { p0, ischecked ->
                val prefs = p0.context.getSharedPreferences("settings",Context.MODE_PRIVATE);
                prefs.edit().putBoolean("reddit_source",ischecked).apply();
                if(ischecked){
                    MenuChange?.hidenavbuttons(MainActivity.menu.reddit,true);
                }else{
                    MenuChange?.hidenavbuttons(MainActivity.menu.reddit,false);
                }
            }
        }



        clearCache?.let {
            it.setOnClickListener {
                requireContext().cacheDir.resolve("imagePreview").deleteRecursively();
                Toast.makeText(requireContext(),"cleared cache",Toast.LENGTH_SHORT).show();
            }
        }
        clearImages?.let {
            it.setOnClickListener {
                requireContext().cacheDir.resolve("imagesaved").deleteRecursively();
                Toast.makeText(requireContext(),"cleared saved images",Toast.LENGTH_SHORT).show();
            }
        }



        Show_favorite_insearch?.let {
            it.isChecked = !requireContext().getSharedPreferences("settings",Context.MODE_PRIVATE).getBoolean("show_fav",true);
            it.setOnCheckedChangeListener { compoundButton, ischecked ->
                requireContext().getSharedPreferences("settings",Context.MODE_PRIVATE).edit().putBoolean("show_fav",!ischecked).apply();
                Reddit_Api.showfav = !ischecked;
            }
        }

        export_data?.let {
            val intentExport = Intent(Intent.ACTION_CREATE_DOCUMENT)
                .setType("application/zip")
                .putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/zip"))
                .putExtra(Intent.EXTRA_TITLE,"Export app Data");
            requireActivity().startActivityForResult(intentExport,wallmewallpaper.EBACKUP_CODE);
        }




        github?.let {
            it.setOnClickListener {
                val uri = Uri.parse("https://github.com/Alaory/WallMe-Wallpaper");
                requireContext().startActivity(Intent(Intent.ACTION_VIEW,uri));
            }
        }
        supportMe?.let {
            it.setOnClickListener {
                val uri = Uri.parse("https://www.patreon.com/Alaory");
                requireContext().startActivity(Intent(Intent.ACTION_VIEW,uri));
            }
        }

        return layout;
    }

}