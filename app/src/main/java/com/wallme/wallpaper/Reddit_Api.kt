package com.wallme.wallpaper

import android.content.Context
import android.util.Log
import net.dean.jraw.RedditClient
import net.dean.jraw.android.AndroidHelper
import net.dean.jraw.android.ManifestAppInfoProvider
import net.dean.jraw.android.SharedPreferencesTokenStore
import net.dean.jraw.android.SimpleAndroidLogAdapter
import net.dean.jraw.http.LogAdapter
import net.dean.jraw.http.OkHttpNetworkAdapter
import net.dean.jraw.http.SimpleHttpLogger
import net.dean.jraw.http.UserAgent
import java.util.*


class Reddit_Api(con: Context) {
    private  var context: Context = con;
    fun call_api(){

        Log.i("Reddit_Api","Function called");
        var userAgent = UserAgent("Android","Wallme","0.1","wallme-wallpaper");
        var network_adabter = OkHttpNetworkAdapter(userAgent);

        var appProvider = ManifestAppInfoProvider(context);


        var accounthepler = AndroidHelper.accountHelper("TOKEN","http://localhost:8080",userAgent, UUID.randomUUID())

        accounthepler.onSwitch { r: RedditClient ->
            // By default, JRAW logs HTTP activity to System.out. We're going to use Log.i()
            // instead.
            // By default, JRAW logs HTTP activity to System.out. We're going to use Log.i()
            // instead.
            val logAdapter: LogAdapter = SimpleAndroidLogAdapter(Log.INFO)

            // We're going to use the LogAdapter to write down the summaries produced by
            // SimpleHttpLogger

            // We're going to use the LogAdapter to write down the summaries produced by
            // SimpleHttpLogger
            r.logger = (
                SimpleHttpLogger(SimpleHttpLogger.DEFAULT_LINE_LENGTH, logAdapter)
            )

            // If you want to disable logging, use a NoopHttpLogger instead:
            // redditClient.setLogger(new NoopHttpLogger());


            // If you want to disable logging, use a NoopHttpLogger instead:
            // redditClient.setLogger(new NoopHttpLogger());
        }

    }

}