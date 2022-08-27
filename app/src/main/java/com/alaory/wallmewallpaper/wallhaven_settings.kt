package com.alaory.wallmewallpaper

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.InputType
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.children
import androidx.core.view.marginBottom
import androidx.fragment.app.strictmode.RetainInstanceUsageViolation
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup


class wallhaven_settings : Fragment() {

    var TagBoxWhiteList: ChipGroup? = null;
    var TagBoxBlackList: ChipGroup? = null;


    var sorting : String = "favorites";
    var ordering : String = "desc";
    var ratio : String = "";
    var categories : String = "010"
    var sortingint = 4;


    var Ratio_TagGroup : ChipGroup ? = null;
    var AddChip_box_whitelist : Chip ? = null;
    var AddChip_box_blacklist : Chip ? = null;
    var categories_TagGroup : ChipGroup? = null;
    var listmode_wallhaven: Spinner? = null;


    companion object{
        var TagsSequnce: MutableList<String> = listOf<String>("+nature").toMutableList();
    }

    fun removeTag(TagName : String){
        for (i in 0 until TagsSequnce.size){
            if(TagsSequnce[i] == TagName){
                TagsSequnce.removeAt(i)

                for (j in TagsSequnce)
                    Log.i("wallhaven_settings","Found $j")
                break;
            }
        }
    }



    fun addChip(name : String,context: Context,resources: android.content.res.Resources){

        val tagchipTemp = LayoutInflater.from(context).inflate(R.layout.tagchip,null) as Chip;

        if(name != ""){
            tagchipTemp.text = name.drop(1);
            tagchipTemp.chipIcon = ResourcesCompat.getDrawable(resources,R.drawable.remove_ic,null);

            tagchipTemp.setOnClickListener {
                TagBoxWhiteList?.removeView(tagchipTemp);
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



        setUidata();

        for(i in TagsSequnce)
            addChip(i,requireContext(),resources);




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
                val inputStringText = "+${inputText.text.toString().lowercase()}";
                var found = false;
                for (j in TagsSequnce)
                    if(j == inputStringText)
                        found = true;


                if(!found){
                    TagsSequnce += inputStringText;
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
                val inputStringText = "-${inputText.text.toString().lowercase()}";
                var found = false;
                for (j in TagsSequnce)
                    if(j == inputStringText)
                        found = true;


                if(!found){
                    TagsSequnce += inputStringText;
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
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        }


        view.findViewById<Button>(R.id.save_button_wallhaven_settings).setOnClickListener {
            wallhaven_api.categories = if(categories != "") "&categories=$categories" else "";
            wallhaven_api.sorting = if(sorting != "") "&sorting=$sorting" else "";
            wallhaven_api.ratio = if(ratio != "") "&ratios=$ratio" else "";
            wallhaven_api.ordering = if(ordering != "") "&order=$ordering" else "";


            wallhaven_api.wallhaven_homepage_posts = emptyList<List_image>().toMutableList();
            wallhaven_posts.userhitsave = true;
            MainActivity.change_fragment(MainActivity.wallhavenPosts);

        }

        view.findViewById<Button>(R.id.cancel_button_wallhaven_settings).setOnClickListener {
            MainActivity.change_fragment(MainActivity.wallhavenPosts);
        }


    }





}