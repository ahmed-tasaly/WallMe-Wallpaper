package com.wallme.wallpaper

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.compose.ui.text.toLowerCase
import com.google.android.material.textfield.TextInputEditText


class Reddit_settings : Fragment() {


    companion object{
        var subreddits_list_names : List<String> = listOf("wallpaper");
        var subredditsNames = "wallpaper";

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
        }
        fun loadprefs(context: Context){
            var sharedprefs = PreferenceManager.getDefaultSharedPreferences(context);
            var subtemp : String? = sharedprefs.getString("subreddits","wallpaper");
            if (subtemp != null){
                subredditsNames = subtemp;
                Log.i("Reddit_settings","value found $subredditsNames");
            }
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
        
        image_preview_quality.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(adapterview : AdapterView<*>?, view : View?, pos : Int, selectedItem: Long) {
                val tempvalue = adapterview!!.getItemAtPosition(pos) as Int?;
                if(tempvalue != null)
                    Reddit_Api.previewQulaity =  tempvalue -1;
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

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