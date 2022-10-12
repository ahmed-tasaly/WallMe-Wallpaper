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
import com.alaory.wallmewallpaper.wallpaperChanger_Worker
import com.google.android.material.switchmaterial.SwitchMaterial
import java.util.concurrent.TimeUnit

class settings( menuChange : MainActivity.MenuChange? = null) : Fragment() {

    val MenuChange = menuChange;
    var backbutton : ImageButton? =null;
    //wallpaper changer
    var wallpaper_changer : LinearLayout? = null;
    var wallpaper_changer_text : TextView? = null;

    //sources
    var wallhaven_source : SwitchMaterial? = null;
    var reddit_source : SwitchMaterial? = null;

    //screen
    var fullscreenapp : SwitchMaterial? = null;

    //cache
    var clearCache : LinearLayout? = null;
    var clearImages : LinearLayout? = null;

    //about
    var github : TextView? = null;
    var supportMe : TextView? = null;




    var editbox : EditText? = null;
    var timecount : Spinner? = null;
    var screenFlag : Spinner? = null;

    val JOBID = 212;




    companion object{
        var time: String = "15";
        var timecountSelection: Int = 0;
        var screenSelection: Int = 0;

        fun saveprefs(context: Context){
            val preferenceManager = context.getSharedPreferences("settings",Context.MODE_PRIVATE);

            preferenceManager.edit().putString("timecount",time).apply();
            preferenceManager.edit().putInt("timecountSelection",timecountSelection).apply();
            preferenceManager.edit().putInt("screenSelection",screenSelection).apply();
        }
        fun loadprefs(context: Context){
            val preferenceManager = context.getSharedPreferences("settings",Context.MODE_PRIVATE);
            time = preferenceManager.getString("timecount","15")!!.toString();
            timecountSelection = preferenceManager.getInt("timecountSelection",0);
            screenSelection = preferenceManager.getInt("screenSelection",0);
        }

        val WorkerTag = "WallpaperChanger";
        var isworkerrunning = false;

    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val layout = inflater.inflate(R.layout.fragment_settings, container, false);
        MenuChange?.Shownav(false);

        loadprefs(requireContext());

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            MenuChange?.ChangeTo(MainActivity.menu.favorite,true);
        }

        backbutton = layout.findViewById(R.id.backArrow_button);

        wallpaper_changer = layout.findViewById(R.id.wallpaper_changer_settings_button);
        wallpaper_changer_text = layout.findViewById(R.id.wallpaper_changer_settings_button_text);

        wallhaven_source = layout.findViewById(R.id.Switch_wallhaven_settings);
        reddit_source = layout.findViewById(R.id.Switch_reddit_settings);

        fullscreenapp = layout.findViewById(R.id.fullscreenapp_settings)

        clearCache = layout.findViewById(R.id.clear_cache_settings);
        clearImages = layout.findViewById(R.id.clear_saved_images_settings);


        github = layout.findViewById(R.id.github_settings);
        supportMe = layout.findViewById(R.id.support_settings);

        editbox = layout.findViewById(R.id.editTextTime);
        timecount = layout.findViewById(R.id.wallpaper_changer_time_spinner);
        screenFlag = layout.findViewById(R.id.wallpaper_changer_time_spinner_forScreen);

        val worklistinfo = WorkManager.getInstance(requireContext()).getWorkInfosByTag(WorkerTag)
        for (i in worklistinfo.get())
            if(!i.state.isFinished ){
                Log.i("workermee",i.tags.toString());
                isworkerrunning = true;
            }


        if(isworkerrunning)
            wallpaper_changer_text?.setText("Stop wallpaper changer")

        wallpaper_changer?.let {
            it.setOnClickListener {

               if(!isworkerrunning) {
                    saveprefs(requireContext());

                    var localtimecount: TimeUnit = TimeUnit.MINUTES;
                    when (timecountSelection){
                        0 -> {
                            localtimecount = TimeUnit.MINUTES;
                        }
                        1 -> {
                            localtimecount = TimeUnit.HOURS;
                        }
                        2 -> {
                            localtimecount = TimeUnit.DAYS;
                        }
                    }

                   val wrokerConstraints = Constraints.Builder()
                       .setRequiredNetworkType(NetworkType.CONNECTED)
                       .build()

                   val workreq = PeriodicWorkRequestBuilder<wallpaperChanger_Worker>(time.toLong(),localtimecount)
                       .addTag(WorkerTag)
                       .setConstraints(wrokerConstraints)
                       .build();


                   WorkManager.getInstance(requireContext()).enqueueUniquePeriodicWork(WorkerTag, ExistingPeriodicWorkPolicy.REPLACE,workreq);
                   wallpaper_changer_text?.setText("Stop wallpaper changer");
                   isworkerrunning = true;
                }else{
                    isworkerrunning = false;
                   wallpaper_changer_text?.setText("Start wallpaper changer");
                    WorkManager.getInstance(requireContext()).cancelAllWorkByTag(WorkerTag);
                }
            }
        }

        backbutton?.let {
            it.setOnClickListener {
                MenuChange?.ChangeTo(MainActivity.menu.favorite,true);
            }
        }

        editbox?.let {
            it.setText(time);
            it.addTextChangedListener(object : TextWatcher{
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    time = text.toString();
                }

                override fun afterTextChanged(p0: Editable?) {
                    p0?.let {
                        if(it.isEmpty()){
                            it.append("15")
                        }
                    }
                }
            })
        }
        timecount?.let {
            it.setSelection(timecountSelection);
            it.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, selection: Long) {
                    timecountSelection = selection.toInt();
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {}
            }
        }

        fullscreenapp?.let {
            it.isChecked = requireContext().getSharedPreferences("settings",Context.MODE_PRIVATE).getBoolean("fullscreenapp",true);
            it.setOnCheckedChangeListener { p0, ischecked ->
                val prefs = p0.context.getSharedPreferences("settings",Context.MODE_PRIVATE);
                prefs.edit().putBoolean("fullscreenapp",ischecked).apply();
            }
        }


        screenFlag?.let {
            it.setSelection(screenSelection);
            it.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, selection: Long) {
                    screenSelection = selection.toInt();
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {}
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