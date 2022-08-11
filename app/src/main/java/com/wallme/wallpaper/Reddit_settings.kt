package com.wallme.wallpaper

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.compose.ui.text.toLowerCase
import com.google.android.material.textfield.TextInputEditText


class Reddit_settings : Fragment() {


    companion object{
        var subreddits_list_names : List<String> = listOf("mobilewallpaper");
        var subredditsNames = "mobilewallpaper";

        fun parse_subreddits(SUBREDDITS : String){
            subredditsNames = SUBREDDITS;
            Log.i("subreddits",subredditsNames.toString());
            subredditsNames.replace("\\s".toRegex(), "");
            subreddits_list_names = subredditsNames.split("+");
        }

        private fun savepref(subreddits : String,context: Context){
            val redditSettings = PreferenceManager.getDefaultSharedPreferences(context);
            redditSettings.edit().putString("subreddits",subreddits).apply().apply {
                Log.i("Reddit_settings","Settings have been saved with value $subreddits");
            };
            redditSettings.edit().putInt("image_preview",Reddit_Api.previewQulaity).apply();
        }

        fun loadprefs(context: Context){
            val sharedprefs = PreferenceManager.getDefaultSharedPreferences(context);
            val subtemp : String? = sharedprefs.getString("subreddits","wallpaper");
            val imagepreview = sharedprefs.getInt("image_preview",2);


            subredditsNames = subtemp!!;
            Reddit_Api.previewQulaity = imagepreview!!;

            parse_subreddits(subredditsNames);
        }
    }




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_reddit_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        var image_preview_quality = view.findViewById(R.id.image_quality_spinner) as Spinner;

        ArrayAdapter.createFromResource(requireContext(),R.array.preview_quality,android.R.layout.simple_spinner_item).also { arrayAdapter ->
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            image_preview_quality.adapter = arrayAdapter;
            image_preview_quality.setSelection(Reddit_Api.previewQulaity);
            Log.i("Reddit_settings","Spinner adabter set")
        }

        image_preview_quality.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent : AdapterView<*>?, view : View?, pos : Int, selectedItem: Long) {

                if(view != null){
                    val current_view = view as TextView;
                    current_view.setTextColor(Color.parseColor("#EEEEEE"))
                }


                Reddit_Api.previewQulaity =  selectedItem.toInt();
                Log.i("Reddit_settings","user selected $selectedItem")


            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                Log.i("Reddit_settings","spinner nothing selected")
            }


        }

        val inputtext = view.findViewById(R.id.inputText) as TextInputEditText;

        inputtext.setText(subredditsNames);

        view.findViewById<Button>(R.id.save_button_reddit_settings).setOnClickListener {
            parse_subreddits(inputtext.text!!.toString().lowercase());
            Reddit_posts.userHitSave = true;
            savepref(subredditsNames,requireContext());
        }


    }
}