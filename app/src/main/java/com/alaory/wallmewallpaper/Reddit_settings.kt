package com.alaory.wallmewallpaper

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.addCallback
import androidx.core.view.children
import androidx.core.view.isVisible
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.alaory.wallmewallpaper.R


class Reddit_settings : Fragment() {


    companion object{
        var subreddits_list_names : List<String> = listOf("amoledbackgrounds","wallpaper");
        var subredditsNames = "amoledbackgrounds+wallpaper";
        var CheckedChipListMode : String = "Hot";
        var TimePeriod = "";
        var TimePeridLastInt = 4;
        var image_preview_qualiy_int = 2;

        fun parse_subreddits(SUBREDDITS : String){
            subredditsNames = SUBREDDITS.filter { !it.isWhitespace() };
            Log.i("subreddits",subredditsNames.toString());
            subredditsNames.replace("\\s".toRegex(), "");
            subreddits_list_names = subredditsNames.split("+");
        }

        private fun savepref(context: Context){
            val redditSettings = PreferenceManager.getDefaultSharedPreferences(context);
            redditSettings.edit().putString("subreddits",subredditsNames).apply();
            redditSettings.edit().putInt("image_preview",image_preview_qualiy_int).apply();
            redditSettings.edit().putInt("timePeriod",TimePeridLastInt).apply();
            redditSettings.edit().putString("listmode",CheckedChipListMode).apply();
            redditSettings.edit().putString("timePeriodString",TimePeriod).apply();
        }

        fun loadprefs(context: Context){
            val sharedprefs = PreferenceManager.getDefaultSharedPreferences(context);
            val subtemp : String? = sharedprefs.getString("subreddits", subredditsNames);
            val templistmode = sharedprefs.getString("listmode","Hot");
            val temptimeperiod = sharedprefs.getString("timePeriodString","");

            //set local settings
            subredditsNames = subtemp!!;
            CheckedChipListMode = templistmode!!;
            TimePeriod = temptimeperiod!!;
            image_preview_qualiy_int = sharedprefs.getInt("image_preview",2);
            TimePeridLastInt = sharedprefs.getInt("timePeriod",5);

            //set reddit api settings
            Reddit_Api.listMode = CheckedChipListMode;
            Reddit_Api.previewQulaity = image_preview_qualiy_int;
            Reddit_Api.timeperiod = timePreValue();
            //set subreddits
            parse_subreddits(subredditsNames);
        }

        //return value of timePeriod in a string like year,week etc when the option top is selected
        fun timePreValue(): String {
            return if(CheckedChipListMode.lowercase() == "top"){
                TimePeriod;
            }else{
                "";
            }
        }
    }




    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_reddit_settings, container, false);


        requireActivity().onBackPressedDispatcher.addCallback(this) {
            MainActivity.change_fragment(MainActivity.redditPosts,true);
        }

        //image preview quality spinner
        val image_preview_quality = view.findViewById(R.id.image_quality_spinner) as Spinner;

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
                image_preview_qualiy_int =  selectedItem.toInt();
                Log.i("Reddit_settings","user selected $selectedItem")
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {
                Log.i("Reddit_settings","spinner nothing selected")
            }
        }
        //-----------------------------------------------------------------------------------------------------------------------------------------------------
        //list mode with time period for top listing
        val listmode = view.findViewById(R.id.timePeriod_chipGroup) as ChipGroup;
        val timepriote = view.findViewById(R.id.TopTimePeriod) as Spinner;

        //on start check for user selection in shredprefrences
        for(child in listmode.children){
            val tempchild = child as Chip;
            tempchild.isChecked = tempchild.text == CheckedChipListMode;
            timepriote.isVisible = CheckedChipListMode == "Top";
            Reddit_Api.timeperiod = timePreValue();
        }

        //on change chip set user new chip
        listmode.setOnCheckedStateChangeListener { group, checkedIds ->
            CheckedChipListMode = group.findViewById<Chip>(checkedIds[0]).text.toString();
            timepriote.isVisible = CheckedChipListMode == "Top";
            Reddit_Api.timeperiod = timePreValue();
        }

        //time period now week month etc
        ArrayAdapter.createFromResource(requireContext(),R.array.TimePeriod,android.R.layout.simple_spinner_item).also{ arrayAdapter ->
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            timepriote.adapter = arrayAdapter;
            timepriote.setSelection(TimePeridLastInt)
        }

        //when user change time period
        timepriote.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                val current_view = p0!!.selectedView as TextView?;
                Log.i("Reddit_settings","${current_view?.text} is selected")
                TimePeriod = "&t=${current_view?.text.toString().lowercase()}"
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        }



        //subreddit input box and the save button
        val inputtext = view.findViewById(R.id.inputText) as TextInputEditText;
        inputtext.setText(subredditsNames);

        //when user clicks save
        view.findViewById<Button>(R.id.save_button_reddit_settings).setOnClickListener {
            parse_subreddits(inputtext.text!!.toString().lowercase());
            subredditsNames = inputtext.text!!.toString().lowercase();

            Reddit_posts.userHitSave = true;//save button have been clicked

            Reddit_Api.listMode = CheckedChipListMode;//aka top new and hot
            Reddit_Api.previewQulaity = image_preview_qualiy_int; // low meduim ultra etc
            Reddit_Api.timeperiod = timePreValue();// week day month etc

            savepref(requireContext());

            MainActivity.change_fragment(MainActivity.redditPosts,true);
        }

        view.findViewById<Button>(R.id.cancel_button_reddit_settings).setOnClickListener {
            loadprefs(requireContext());
            MainActivity.change_fragment(MainActivity.redditPosts,true);
        }



        return view;
    }

}