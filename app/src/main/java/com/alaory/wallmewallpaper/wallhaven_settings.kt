package com.alaory.wallmewallpaper

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.children
import androidx.fragment.app.Fragment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup


class wallhaven_settings : Fragment() {

    var TagBoxWhiteList: ChipGroup? = null;
    var TagBoxBlackList: ChipGroup? = null;


    val timeperiodlist : List<String> = listOf(
        "1d","3d","1w","1m","3m","6m","1y"
    );


    var Ratio_TagGroup : ChipGroup ? = null;
    var AddChip_box_whitelist : Chip ? = null;
    var AddChip_box_blacklist : Chip ? = null;
    var categories_TagGroup : ChipGroup? = null;
    var listmode_wallhaven: Spinner? = null;
    var listmode_timepriod: Spinner? = null;

    var tempTagSequence : Array<String> = emptyArray();

    companion object{
        var TagsSequnce: Array<String> = emptyArray();
        var sorting : String = "favorites";
        var ordering : String = "desc";
        var ratio : String = "";
        var categories : String = "100"
        var timeperiod: String = "";
        var defualtTimePeriod: Int = 3;
        var sortingint = 4;

        fun loadprefs(context: Context){
            val wallhavenPrefs = PreferenceManager.getDefaultSharedPreferences(context);
            sorting = wallhavenPrefs.getString("sorting","favorites").toString();
            ordering = wallhavenPrefs.getString("ordering","desc").toString();
            ratio = wallhavenPrefs.getString("ratio","").toString();
            categories = wallhavenPrefs.getString("categories","100").toString();
            timeperiod = wallhavenPrefs.getString("timeperiod","").toString();
            defualtTimePeriod = wallhavenPrefs.getInt("defualtTimePeriod",3);
            sortingint = wallhavenPrefs.getInt("sortingint",4);

            val tags = wallhavenPrefs.getString("tags","+sunset,+portrait display").toString();
            TagsSequnce = emptyArray();
            TagsSequnce = tags.split(",").toTypedArray();

            setapidata();
        }

        fun setapidata(){
            wallhaven_api.categories = if(categories != "") "&categories=$categories" else "";
            wallhaven_api.sorting = if(sorting != "") "&sorting=$sorting" else "";
            wallhaven_api.ratio = if(ratio != "") "&ratios=$ratio" else "";
            wallhaven_api.ordering = if(ordering != "") "&order=$ordering" else "";
            wallhaven_api.timeperiod = if(timeperiod != "") "&topRange=$timeperiod" else "";
            wallhaven_api.currentPage = 1;
        }

    }


    fun saveprefs(context: Context){
        var tags : String = "";
        if(TagsSequnce.isNotEmpty())
            for (tag in TagsSequnce)
                tags += ",$tag"

        if(tags.isNotEmpty())
            tags.drop(1);


        val wallhavenPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        wallhavenPrefs.edit().putString("tags",tags).apply();
        wallhavenPrefs.edit().putString("sorting",sorting).apply();
        wallhavenPrefs.edit().putString("ordering",ordering).apply();
        wallhavenPrefs.edit().putString("ratio",ratio).apply();
        wallhavenPrefs.edit().putString("categories",categories).apply();
        wallhavenPrefs.edit().putString("timeperiod",timeperiod).apply();
        wallhavenPrefs.edit().putInt("defualtTimePeriod",defualtTimePeriod).apply();
        wallhavenPrefs.edit().putInt("sortingint",sortingint).apply();
    }


    fun removeTag(TagName : String){
        for (i in 0 until TagsSequnce.size){
                val temparray = TagsSequnce.toMutableList();
                temparray.remove(TagName);
                val temptemparray = tempTagSequence.toMutableList();
                temptemparray.remove(TagName)
                tempTagSequence = temptemparray.toTypedArray();
                TagsSequnce = temparray.toTypedArray();
                break;
        }
    }



    fun addChip(name : String,context: Context,resources: android.content.res.Resources){

        val tagchipTemp = LayoutInflater.from(context).inflate(R.layout.tagchip,null) as Chip;

        if(name != ""){
            tagchipTemp.text = name.drop(1);
            tagchipTemp.chipIcon = ResourcesCompat.getDrawable(resources,R.drawable.remove_ic,null);

            tagchipTemp.setOnClickListener {
                if(name[0] == '+')
                    TagBoxWhiteList?.removeView(tagchipTemp);
                else
                    TagBoxBlackList?.removeView(tagchipTemp);

                removeTag(name);
            }


            if(name[0] == '+')
                TagBoxWhiteList?.addView(tagchipTemp);
            else
                TagBoxBlackList?.addView(tagchipTemp);
        }
    }



    private fun setUidata(){
        Log.i("categories_TagGroup",categories);
        for ( child in categories_TagGroup?.children!!){

            if((child as Chip).text.toString().lowercase() == "general" )
                child.isChecked = categories[0] == '1';
            else if(child.text.toString().lowercase() == "anime" )
                child.isChecked = categories[1] == '1';

        }

        for ( child in Ratio_TagGroup?.children!!){
            val tempchild = child as Chip;
            if(tempchild.text.toString().lowercase() == ratio)
                tempchild.isChecked = true;
        }

        listmode_wallhaven?.setSelection(sortingint);
        if(sortingint == 5){
            listmode_timepriod?.visibility = View.VISIBLE;
            listmode_timepriod?.setSelection(defualtTimePeriod);
        }
        else
            listmode_timepriod!!.visibility = View.GONE;

        for(i in TagsSequnce)
            addChip(i, requireContext(),resources);
    }



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_wallhaven_settings, container, false)
    }




    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        TagBoxWhiteList = view.findViewById(R.id.TagChipGroup_box_whitelist);
        TagBoxBlackList = view.findViewById(R.id.TagChipGroup_box_blacklist);

        Ratio_TagGroup =  view.findViewById(R.id.Ratio_TagGroup);
        AddChip_box_whitelist = view.findViewById(R.id.AddChip_box_whitelist);
        AddChip_box_blacklist  = view.findViewById(R.id.AddChip_box_blacklist)
        categories_TagGroup  = view.findViewById(R.id.categories_TagGroup);
        listmode_wallhaven = view.findViewById(R.id.listmode_wallhaven);
        listmode_timepriod = view.findViewById(R.id.listmode_timePeriod_wallhaven);

        tempTagSequence = emptyArray();
        setUidata();

        //----------------------------------------------
        //white list tags
        AddChip_box_whitelist?.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            val inputText = EditText(requireContext());

            inputText.hint = "Enter a Tag name";
            inputText.inputType = InputType.TYPE_CLASS_TEXT

            builder.setView(inputText);
            builder.setTitle("Add Tag");

            builder.setPositiveButton("Add",DialogInterface.OnClickListener { dialogInterface, i ->
                val inputStringText = "+${inputText.text.toString().lowercase().trim()}";
                var found = false;
                for (j in TagsSequnce)
                    if(j == inputStringText)
                        found = true;

                for (j in tempTagSequence)
                    if(j == inputStringText)
                        found = true;

                if(!found){
                    tempTagSequence += inputStringText;
                    addChip(inputStringText,requireContext(),resources);
                }

            })

            builder.setNegativeButton("Cancel",DialogInterface.OnClickListener { dialogInterface, i -> })

            builder.show();
        }

        //black list tags
        AddChip_box_blacklist?.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            val inputText = EditText(requireContext());

            inputText.hint = "Enter a Tag name";
            inputText.inputType = InputType.TYPE_CLASS_TEXT

            builder.setView(inputText);
            builder.setTitle("Add Tag");

            builder.setPositiveButton("Add",DialogInterface.OnClickListener { dialogInterface, i ->
                val inputStringText = "-${inputText.text.toString().lowercase().trim()}";
                var found = false;
                for (j in TagsSequnce)
                    if(j == inputStringText)
                        found = true;

                for (j in tempTagSequence)
                    if(j == inputStringText)
                        found = true;

                if(!found){
                    tempTagSequence += inputStringText;
                    addChip(inputStringText,requireContext(),resources);
                }

            })

            builder.setNegativeButton("Cancel",DialogInterface.OnClickListener { dialogInterface, i -> })

            builder.show();

        }
        //----------------------------------------------

        Ratio_TagGroup?.setOnCheckedStateChangeListener { group, checkedIds ->
            if(checkedIds.size != 1){
                ratio = "";
            }else{
                ratio = group.findViewById<Chip>(checkedIds[0]).text.toString().lowercase()
            }
        }

        categories_TagGroup?.setOnCheckedStateChangeListener { group, checkedIds ->
            if(checkedIds.size == 2){
                categories = "110";
            }else if(group.findViewById<Chip>(checkedIds[0]).text.toString().lowercase() == "anime"){
                categories = "010";
            }else{
                categories = "100";
            }

        }


        listmode_wallhaven?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, itemslected: Long) {
                sorting = (parent?.selectedView as TextView?)?.text.toString().lowercase();
                sortingint = itemslected.toInt();

                if(sortingint == 5)
                    listmode_timepriod!!.visibility = View.VISIBLE;
                else{
                    listmode_timepriod!!.visibility = View.GONE;
                    timeperiod = "";
                }

            }
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        }

        listmode_timepriod?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, selected: Long) {
                timeperiod = timeperiodlist[selected.toInt()];
                defualtTimePeriod = selected.toInt();
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }


        view.findViewById<Button>(R.id.save_button_wallhaven_settings).setOnClickListener {
            TagsSequnce += tempTagSequence;
            tempTagSequence = emptyArray();

            saveprefs(requireContext());
            setapidata();

            wallhaven_api.wallhaven_homepage_posts = emptyList<List_image>().toMutableList();
            wallhaven_posts.userhitsave = true;
            MainActivity.change_fragment(MainActivity.wallhavenPosts);

        }

        view.findViewById<Button>(R.id.cancel_button_wallhaven_settings).setOnClickListener {
            loadprefs(requireContext());
            MainActivity.change_fragment(MainActivity.wallhavenPosts);
        }


    }





}