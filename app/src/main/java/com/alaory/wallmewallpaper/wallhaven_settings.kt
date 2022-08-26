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
import android.widget.EditText
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.children
import androidx.core.view.marginBottom
import androidx.fragment.app.strictmode.RetainInstanceUsageViolation
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup


class wallhaven_settings : Fragment() {

    var TagBoxWhiteList: ChipGroup? = null;
    var TagBoxBlackList: ChipGroup? = null;


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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_wallhaven_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        TagBoxWhiteList = view.findViewById(R.id.TagChipGroup_box_whitelist);
        TagBoxBlackList = view.findViewById(R.id.TagChipGroup_box_blacklist);

        for(i in TagsSequnce)
            addChip(i,requireContext(),resources);


        view.findViewById<Chip>(R.id.AddChip_box_whitelist).setOnClickListener {
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

        view.findViewById<Chip>(R.id.AddChip_box_blacklist).setOnClickListener {
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
    }
}