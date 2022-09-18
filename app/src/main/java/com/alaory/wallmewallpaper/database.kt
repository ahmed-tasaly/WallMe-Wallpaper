package com.alaory.wallmewallpaper

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import android.widget.Toast

class database(val context: Context,val table_name: String = ImageInfo_Table,val databaseNameFile: String = "$ImageInfo_Table.dp") : SQLiteOpenHelper(context, databaseNameFile,null, database_version) {

    companion object{
        var imageinfo_list : MutableList<Image_Info> = emptyList<Image_Info>().toMutableList();
        var imageblock_list : MutableList<Image_Info> = emptyList<Image_Info>().toMutableList();
        var lastaddedImageInfo : Image_Info? = null;


        val database_version: Int = 1 ;


        val ImageInfo_Table = "image_info_list";
        val ImageBlock_Table = "image_block_list";



        val name = "name";
        val auther = "auther";
        val url = "url";
        val thumbnail = "thumbnail";
        val title = "title";
        val post_source = "source"
        val width = "width";
        val height = "height";
    }

    override fun onCreate(dp: SQLiteDatabase?) {
        //create sql table and add image_info coloums
        val sql_query_createtable = "CREATE TABLE IF NOT EXISTS $table_name ($name TEXT,$auther TEXT,$url TEXT,$thumbnail TEXT,$title TEXT,$post_source TEXT,$width INTEGER,$height INTEGER);"
        dp!!.execSQL(sql_query_createtable);
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {

    }

    fun add_image_info_to_database(image_info: Image_Info){
        try {
            lastaddedImageInfo = image_info;
            val dp = this.writableDatabase ?: return;
            val CV = ContentValues();

            //set column data
            CV.put(name, image_info.Image_name);
            CV.put(auther, image_info.Image_auther);
            CV.put(url, image_info.Image_url);
            CV.put(thumbnail, image_info.Image_thumbnail);
            CV.put(title, image_info.Image_title);
            CV.put(post_source, image_info.post_url);
            CV.put(width, image_info.imageRatio!!.Width);
            CV.put(height, image_info.imageRatio!!.Height);

            val result = dp.insert(table_name, null, CV);

            if (result.toInt() == -1)
                Toast.makeText(context, "Image database save failed ;(", Toast.LENGTH_LONG).show();
            else
                update_image_info_list_from_database();
        }catch (e : Exception){
            Log.e("database",e.toString());
        }
    }

    fun remove_image_info_from_database(image_info: Image_Info){
        val dp = this.writableDatabase ?: return;
        val query = "DELETE FROM $table_name WHERE $name = '${image_info.Image_name}' AND $auther = '${image_info.Image_auther}';";
        Log.i("database","remove query: $query");
        dp.execSQL(query);
        update_image_info_list_from_database();
    }

    fun update_image_info_list_from_database(){

        imageinfo_list.clear();
        val request_imginfo = "SELECT * FROM $table_name";
        val dp = this.readableDatabase ?: return;
        val curser = dp.rawQuery(request_imginfo,null);

        while (curser.moveToNext()){
            val imageInfo = Image_Info(
                curser.getString(2),
                curser.getString(3),
                curser.getString(0),
                curser.getString(1),
                curser.getString(4),
                curser.getString(5),
                Image_Ratio(curser.getInt(6),curser.getInt(7))
                );
            if(table_name == ImageInfo_Table)
                imageinfo_list += imageInfo;
            else
                imageblock_list += imageInfo;
        }
        curser.close();
        closedp();
    }
    private fun closedp(){
        val dp = this.readableDatabase;
        if(dp != null && dp.isOpen)
            dp.close();
    }
}