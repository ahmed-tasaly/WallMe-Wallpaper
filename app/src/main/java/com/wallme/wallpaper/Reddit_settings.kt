package com.wallme.wallpaper

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.google.android.material.textfield.TextInputEditText


class Reddit_settings : Fragment() {


    companion object{
        var subreddits_list_names : List<String> = listOf("wallpaper");
    }
    private fun savepref(){
        //val redditSettings: SharedPreferences;
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

        val inputtext = view.findViewById(R.id.inputText) as TextInputEditText;

        view.findViewById<Button>(R.id.save_button_reddit_settings).setOnClickListener {
            val names = inputtext.text!!;
            Log.i("subreddits",names.toString());
            names.replace("\\s".toRegex(), "");
            subreddits_list_names = names.split("+");
            for (i in subreddits_list_names){
                Log.i("subreddits",i);
            }
            Reddit_posts.userHitSave = true;
        }


    }
}