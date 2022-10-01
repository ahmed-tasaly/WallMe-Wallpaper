package com.alaory.wallmewallpaper.settings

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.addCallback
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alaory.wallmewallpaper.MainActivity
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.alaory.wallmewallpaper.R
import com.alaory.wallmewallpaper.api.Reddit_Api
import com.alaory.wallmewallpaper.postPage.Reddit_posts
import com.alaory.wallmewallpaper.adabter.list_item_adabter


class Reddit_settings( menuChange : MainActivity.MenuChange? = null) : Fragment() {
    val MenuChange = menuChange;

    companion object{
        var subreddits_list_names : MutableList<String> = listOf("amoledbackgrounds","wallpaper").toMutableList();
        var CheckedChipListMode : String = "Top";
        var TimePeriod = "";
        var TimePeridLastInt = 4;
        var image_preview_qualiy_int = 2;

        fun parse_subreddits(SUBREDDITS : String){
            val subredditsNames = SUBREDDITS.filter { !it.isWhitespace() };
            Log.i("subreddits", subredditsNames.toString());
            subredditsNames.replace("\\s".toRegex(), "");
            subreddits_list_names = subredditsNames.split("+").toMutableList();
        }

        private fun savepref(context: Context){

            var subredditsNames = "";
            for(sub in subreddits_list_names){
                subredditsNames += sub;
                if(subreddits_list_names.last() != sub)
                    subredditsNames += '+';
            }

            val redditSettings = context.getSharedPreferences("redditsettings",Context.MODE_PRIVATE);
            redditSettings.edit().putString("subreddits", subredditsNames).apply();
            redditSettings.edit().putInt("image_preview", image_preview_qualiy_int).apply();
            redditSettings.edit().putInt("timePeriod", TimePeridLastInt).apply();
            redditSettings.edit().putString("listmode", CheckedChipListMode).apply();
            redditSettings.edit().putString("timePeriodString", TimePeriod).apply();
        }

        fun loadprefs(context: Context){
            val sharedprefs = context.getSharedPreferences("redditsettings",Context.MODE_PRIVATE);
            val subtemp : String? = sharedprefs.getString("subreddits", "iphonexwallpapers+phonewallpapers");
            val templistmode = sharedprefs.getString("listmode","Top");
            val temptimeperiod = sharedprefs.getString("timePeriodString","&t=all");

            //set local settings
            if(subtemp!!.isNotEmpty())
                parse_subreddits(subtemp!!);
            CheckedChipListMode = templistmode!!;
            TimePeriod = temptimeperiod!!;
            image_preview_qualiy_int = sharedprefs.getInt("image_preview",2);
            TimePeridLastInt = sharedprefs.getInt("timePeriod",5);

            //set reddit api settings
            Reddit_Api.listMode = CheckedChipListMode;
            Reddit_Api.previewQulaity = image_preview_qualiy_int;
            Reddit_Api.timeperiod = timePreValue();
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
        loadprefs(requireContext());
        MenuChange?.Shownav(false);//hide nav
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_reddit_settings, container, false);


        requireActivity().onBackPressedDispatcher.addCallback(this) {
            loadprefs(requireContext());
            MenuChange?.ChangeTo(MainActivity.menu.reddit,true);
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
        val chipsearch : Chip = view.findViewById(R.id.reddit_subreddit_search);

        //subreddit input box and the save button

        val addmore  = object : list_item_adabter.Onclick{
            override fun onclick(name: String) {}

            override fun addmore() {
               Toast.makeText(requireContext(),"toolazytoaddthefeature",Toast.LENGTH_SHORT).show();
            }
        }
        val subredditlist_view = view.findViewById(R.id.recyclerview_subreddit) as RecyclerView;
        val adabter  = list_item_adabter(subreddits_list_names,addmore,true);
        subredditlist_view.adapter = adabter;
        subredditlist_view.layoutManager = GridLayoutManager(requireContext(),1,GridLayoutManager.VERTICAL,false);




        chipsearch.setOnClickListener {

            val dialogBuilder = AlertDialog.Builder(requireContext());
            val layout = LayoutInflater.from(requireContext()).inflate(R.layout.search_list_box,null);
            val subredditList: MutableList<String> = emptyList<String>().toMutableList();


            val adapter = list_item_adabter(subredditList,object : list_item_adabter.Onclick{
                override fun onclick(name: String) {
                    var found = false;
                    for (i in subreddits_list_names){
                        if(i.lowercase().trim() == name.lowercase().trim())
                            found = true;
                    }
                    if(found)
                        return;
                    Log.i("subreddits_list_names",name);
                    subreddits_list_names += name;
                    Toast.makeText(requireContext(),"subreddit has been added",Toast.LENGTH_SHORT).show();
                }

                override fun addmore() {}
            });


            val recyclerView = layout.findViewById<RecyclerView>(R.id.search_list_recyclerView);
            recyclerView.layoutManager = GridLayoutManager(requireContext(),1,GridLayoutManager.VERTICAL,false);
            recyclerView.adapter = adapter;

            layout.findViewById<SearchView>(R.id.search_list_textInputLayout).setOnQueryTextListener(object : SearchView.OnQueryTextListener{
                override fun onQueryTextSubmit(text: String?): Boolean {
                    return true;
                }

                override fun onQueryTextChange(text: String?): Boolean {
                    Reddit_Api.search_subreddits(text!!) {
                        subredditList.clear()
                        subredditList += it;
                        requireActivity().runOnUiThread {
                            adapter.notifyDataSetChanged();
                        }

                        Log.i("onQueryTextSubmit", subredditList.toString());
                    }
                    return true;
                }
            })
            dialogBuilder.setView(layout);
            dialogBuilder.show();
        }

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





        //when user clicks save
        view.findViewById<Button>(R.id.save_button_reddit_settings).setOnClickListener {

            Reddit_posts.userHitSave = true;//save button have been clicked

            Reddit_Api.listMode = CheckedChipListMode;//aka top new and hot
            Reddit_Api.previewQulaity = image_preview_qualiy_int; // low meduim ultra etc
            Reddit_Api.timeperiod = timePreValue();// week day month etc

            savepref(requireContext());
            Reddit_Api.reddit.dispatcher.cancelAll();
            MenuChange?.ChangeTo(MainActivity.menu.reddit,true);
        }

        view.findViewById<Button>(R.id.cancel_button_reddit_settings).setOnClickListener {
            loadprefs(requireContext());
            MenuChange?.ChangeTo(MainActivity.menu.reddit,true);
        }



        return view;
    }

}