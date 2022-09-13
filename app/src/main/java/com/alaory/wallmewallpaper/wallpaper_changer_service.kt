package com.alaory.wallmewallpaper

import android.app.job.JobParameters
import android.app.job.JobService
import android.widget.Toast

class wallpaper_changer_service : JobService() {

    companion object{
        var resused = 1;
    }

    override fun onStartJob(param: JobParameters?): Boolean {
        Toast.makeText(this,"wallpaper background service $resused",Toast.LENGTH_SHORT).show();
        resused++;
        return true;
    }

    override fun onStopJob(param: JobParameters?): Boolean {
        jobFinished(param,true);
        return true;
    }

}