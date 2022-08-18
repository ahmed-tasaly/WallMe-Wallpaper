package com.alaory.wallmewallpaper

import android.app.AlertDialog
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
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup


class wallhaven_settings : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        val TagBox: ChipGroup = view.findViewById(R.id.TagChipGroup_box);
        val ChipAdd: Chip = view.findViewById(R.id.AddChip_box);

        ChipAdd.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            val tagchipTemp = LayoutInflater.from(requireContext()).inflate(R.layout.tagchip,TagBox,false) as Chip;
            val inputText = EditText(requireContext());




            inputText.hint = "Enter a Tag name";
            inputText.inputType = InputType.TYPE_CLASS_TEXT

            builder.setView(inputText);
            builder.setTitle("Add Tag");

            builder.setPositiveButton("Add",DialogInterface.OnClickListener { dialogInterface, i ->
                val inputStringText = inputText.text.toString();
                if(inputStringText != ""){
                    tagchipTemp.text = inputStringText;
                    tagchipTemp.setOnClickListener {
                        TagBox.removeView(tagchipTemp);
                    }
                    tagchipTemp.chipIcon = ResourcesCompat.getDrawable(resources,R.drawable.remove_ic,null);
                    TagBox.addView(tagchipTemp);
                }
            })
            builder.setNegativeButton("Cancel",DialogInterface.OnClickListener { dialogInterface, i ->
                Log.i("wallhaven_settings","i am negtive");
            })
            builder.show();

        }

    }
}