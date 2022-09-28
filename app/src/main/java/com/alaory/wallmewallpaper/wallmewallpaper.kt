package com.alaory.wallmewallpaper

import android.app.Application
import com.alaory.wallmewallpaper.settings.Reddit_settings
import com.alaory.wallmewallpaper.settings.wallhaven_settings

class wallmewallpaper : Application() {

    //init database
    val DataBase = database(this);
    val blockdatabase = database(this,database.ImageBlock_Table,"${database.ImageBlock_Table}.dp");
    override fun onCreate() {
        super.onCreate();
        DataBase.onCreate(DataBase.writableDatabase);
        blockdatabase.onCreate(blockdatabase.writableDatabase);

        DataBase.update_image_info_list_from_database();
        blockdatabase.update_image_info_list_from_database();

        //update settings
        Reddit_settings.loadprefs(this);
        wallhaven_settings.loadprefs(this);
    }
}