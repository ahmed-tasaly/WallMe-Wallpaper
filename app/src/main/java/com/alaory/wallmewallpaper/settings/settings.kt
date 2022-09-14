package com.alaory.wallmewallpaper.settings

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.addCallback
import androidx.core.content.getSystemService
import com.alaory.wallmewallpaper.MainActivity
import com.alaory.wallmewallpaper.R

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


    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val layout = inflater.inflate(R.layout.fragment_settings, container, false);
        val jobsc = requireContext().getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler;

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



        for(i in jobsc.allPendingJobs){
            if(i.id == JOBID){
                wallpaper_changer?.setText("Stop wallpaper changer")
            }
        }

        wallpaper_changer?.let {
            it.setOnClickListener {
                var isrunning = false;
                for(i in jobsc.allPendingJobs){
                    if(i.id == JOBID){
                        isrunning = true;
                    }
                }

                if(!isrunning) {
                    saveprefs(requireContext());
                    var localtimecount = 60;
                    when (timecountSelection){
                        0 -> {
                            localtimecount = 60;
                        }
                        1 -> {
                            localtimecount = 60 * 60;
                        }
                        2 -> {
                            localtimecount = 60 * 60 * 24;
                        }
                    }

                    val timeperiodTochangewallpaper: Long = time.toLong() * localtimecount * 1000;//min is 15m


                    val jobinfo = JobInfo.Builder(
                        JOBID,
                        ComponentName(
                            "com.alaory.wallmewallpaper",
                            "com.alaory.wallmewallpaper.wallpaper_changer_service"
                        )
                    );

                    val job = jobinfo
                        .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                        .setPeriodic(timeperiodTochangewallpaper)
                        .build();


                    jobsc.schedule(job);
                    wallpaper_changer?.setText("Stop wallpaper changer");
                }else{
                    wallpaper_changer?.setText("Start wallpaper changer")
                    jobsc.cancel(JOBID);
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