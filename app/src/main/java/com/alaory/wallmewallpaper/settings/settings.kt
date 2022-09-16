package com.alaory.wallmewallpaper.settings

import android.content.Context
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
import androidx.fragment.app.Fragment
import androidx.work.*
import com.alaory.wallmewallpaper.MainActivity
import com.alaory.wallmewallpaper.R
import com.alaory.wallmewallpaper.wallpaperChanger_Worker
import java.util.concurrent.TimeUnit

class settings : Fragment() {

    var wallpaper_changer : TextView? = null;
    var clearCache : TextView? = null;
    var clearImages : TextView? = null;
    var github : TextView? = null;
    var supportMe : TextView? = null;


    var editbox : EditText? = null;
    var timecount : Spinner? = null;
    var screen : Spinner? = null;

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
        MainActivity.hidenav();

        loadprefs(requireContext());

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            MainActivity.change_fragment(MainActivity.LastFragmentMode!!, true);
        }


        wallpaper_changer = layout.findViewById(R.id.wallpaper_changer_settings_button);
        clearCache = layout.findViewById(R.id.clear_cache_settings);
        clearImages = layout.findViewById(R.id.clear_saved_images_settings);
        github = layout.findViewById(R.id.github_settings);
        supportMe = layout.findViewById(R.id.support_settings);

        editbox = layout.findViewById(R.id.editTextTime);
        timecount = layout.findViewById(R.id.wallpaper_changer_time_spinner);
        screen = layout.findViewById(R.id.wallpaper_changer_time_spinner_forScreen);

        val worklistinfo = WorkManager.getInstance(requireContext()).getWorkInfosByTag(WorkerTag)
        for (i in worklistinfo.get())
            if(!i.state.isFinished ){
                Log.i("workermee",i.tags.toString());
                isworkerrunning = true;
            }


        if(isworkerrunning)
            wallpaper_changer?.setText("Stop wallpaper changer")

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
                   wallpaper_changer?.setText("Stop wallpaper changer");
                   isworkerrunning = true;
                }else{
                    isworkerrunning = false;
                    wallpaper_changer?.setText("Start wallpaper changer");
                    WorkManager.getInstance(requireContext()).cancelAllWorkByTag(WorkerTag);
                }
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


        screen?.let {
            it.setSelection(screenSelection);
            it.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, selection: Long) {
                    screenSelection = selection.toInt();
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {}
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