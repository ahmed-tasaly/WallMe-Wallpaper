package com.wallme.wallpaper

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.textfield.TextInputEditText


class Reddit_settings : Fragment() {


    companion object{
        var subreddits_list_names : List<String> = listOf("wallpaper");
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

        var inputtext = view.findViewById(R.id.inputText) as TextInputEditText;


        var names = inputtext.text!!;
        names.replace("\\s".toRegex(), "");
        subreddits_list_names = names.split("+");
        for (i in subreddits_list_names){
            Log.i("subreddits",i);
        }
    }
}