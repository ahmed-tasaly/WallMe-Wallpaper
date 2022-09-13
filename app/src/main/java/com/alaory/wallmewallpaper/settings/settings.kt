package com.alaory.wallmewallpaper.settings

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
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


    val JOBID = 212;
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val layout = inflater.inflate(R.layout.fragment_settings, container, false);
        val jobsc = requireContext().getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler;

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            MainActivity.change_fragment(MainActivity.LastFragmentMode!!, true);
        }


        wallpaper_changer = layout.findViewById(R.id.wallpaper_changer_settings_button);
        clearCache = layout.findViewById(R.id.clear_cache_settings);
        clearImages = layout.findViewById(R.id.clear_saved_images_settings);
        github = layout.findViewById(R.id.github_settings);
        supportMe = layout.findViewById(R.id.support_settings);

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
                    val jobinfo = JobInfo.Builder(
                        JOBID,
                        ComponentName(
                            "com.alaory.wallmewallpaper",
                            "com.alaory.wallmewallpaper.wallpaper_changer_service"
                        )
                    );
                    val job = jobinfo
                        .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                        .setPeriodic(JobInfo.getMinPeriodMillis())
                        .build();
                    jobsc.schedule(job);
                    wallpaper_changer?.setText("Stop wallpaper changer");
                }else{
                    wallpaper_changer?.setText("Start wallpaper changer")
                    jobsc.cancel(JOBID);
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