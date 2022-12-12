package com.alaory.wallmewallpaper.postPage

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.work.*
import com.alaory.wallmewallpaper.R
import com.alaory.wallmewallpaper.wallpaperChanger_Worker
import com.google.android.material.switchmaterial.SwitchMaterial
import java.util.concurrent.TimeUnit


class wallpaper_changer : Fragment() {

    val WorkerTag = "WallpaperChanger";



    companion object{
        var time: String = "15";
        var timecountSelection: Int = 0;
        var screenSelection: Int = 0;
        var enableAutoWallpaper = true;

        fun saveprefs(context: Context){
            val preferenceManager = context.getSharedPreferences("settings",Context.MODE_PRIVATE);
            preferenceManager.edit().putString("timecount",time).apply();
            preferenceManager.edit().putInt("timecountSelection",timecountSelection).apply();
            preferenceManager.edit().putInt("screenSelection",screenSelection).apply();
            preferenceManager.edit().putBoolean("shouldEnable",enableAutoWallpaper).apply();
        }
        fun loadprefs(context: Context){
            val preferenceManager = context.getSharedPreferences("settings",Context.MODE_PRIVATE);
            time = preferenceManager.getString("timecount","15")!!.toString();
            timecountSelection = preferenceManager.getInt("timecountSelection",0);
            screenSelection = preferenceManager.getInt("screenSelection",0);
            enableAutoWallpaper = preferenceManager.getBoolean("shouldEnable",false);
        }


        var isworkerrunning = false;

    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        loadprefs(requireContext());
        val WallpaperChanger_Layout = inflater.inflate(R.layout.fragment_wallpaperchanger, container, false);

        //wallpaper changer views
        val wallpaper_changer : LinearLayout = WallpaperChanger_Layout.findViewById(R.id.wallpaper_changer_settings_button);
        val wallpaper_changer_text : TextView = WallpaperChanger_Layout.findViewById(R.id.wallpaper_changer_settings_button_text);
        val editbox : EditText= WallpaperChanger_Layout.findViewById(R.id.editTextTime);
        val timecount : Spinner = WallpaperChanger_Layout.findViewById(R.id.wallpaper_changer_time_spinner);
        val screenFlag :  Spinner = WallpaperChanger_Layout.findViewById(R.id.wallpaper_changer_time_spinner_forScreen);

        //enable/disable
        var enableautowallpaper : SwitchMaterial = WallpaperChanger_Layout.findViewById(R.id.wallpaper_changer_title);
        var autowall_container : ConstraintLayout = WallpaperChanger_Layout.findViewById(R.id.autowallpaper_containter);

        autowall_container.isVisible = enableAutoWallpaper;
        enableautowallpaper.isChecked = enableAutoWallpaper;

        enableautowallpaper.setOnCheckedChangeListener { compoundButton, checked ->
            enableAutoWallpaper = checked;
            autowall_container.isVisible = enableAutoWallpaper;
            saveprefs(compoundButton.context)
        }




        //set wallpaper changer time  Min is 15m
        editbox.let {
            it.setText(time);
            it.addTextChangedListener(object : TextWatcher {
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
        //time count is Min,hour or days only
        timecount.let {
            it.setSelection(timecountSelection);
            it.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, selection: Long) {
                    timecountSelection = selection.toInt();
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {}
            }
        }
        //which screen to change the wallpaper for
        screenFlag.let {
            it.setSelection(screenSelection);
            it.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, selection: Long) {
                    screenSelection = selection.toInt();
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {}
            }
        }


        //check if wallpaper changer is working
        val worklistinfo = WorkManager.getInstance(requireContext()).getWorkInfosByTag(WorkerTag)
        for (i in worklistinfo.get())
            if(!i.state.isFinished ){
                Log.i("workermee",i.tags.toString());
                isworkerrunning = true;
            }

        if(isworkerrunning)//tell the user if the wallpaper changer is working
            wallpaper_changer_text.setText("Stop wallpaper changer")


        //set the wallpaper changer worker
        wallpaper_changer.let {
            it.setOnClickListener {

                //if not working start the work
                if(!isworkerrunning) {
                    saveprefs(requireContext());

                    //time format hours,days,etc
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

                    //set the work Constraints, need access to the Internet
                    val wrokerConstraints = Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()

                    //make the work
                    val workreq = PeriodicWorkRequestBuilder<wallpaperChanger_Worker>(time.toLong(),localtimecount)
                        .addTag(WorkerTag)
                        .setConstraints(wrokerConstraints)
                        .build();


                    //start the work
                    WorkManager.getInstance(requireContext()).enqueueUniquePeriodicWork(WorkerTag, ExistingPeriodicWorkPolicy.REPLACE,workreq);
                    wallpaper_changer_text?.setText("Stop wallpaper changer");
                    isworkerrunning = true;
                }else{//else stop the work
                    isworkerrunning = false;
                    wallpaper_changer_text?.setText("Start wallpaper changer");
                    WorkManager.getInstance(requireContext()).cancelAllWorkByTag(WorkerTag);
                }
            }
        }



        return WallpaperChanger_Layout;
    }


}