package com.wallme.wallpaper

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wallme.wallpaper.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(),MyAdab.OnImageClick {

    private lateinit var binding: ActivityMainBinding;
    private lateinit var myrec: RecyclerView;
    private lateinit var mylist: Array<List_image>;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(layoutInflater);
        setContentView(binding.root);



        val temp_data = List_image("https://lh5.googleusercontent.com/1BJEx3IaxUYit3Sd1AItlUCObS_f3bth78qKrzh9PygNkOPDA83QT-_HEdJ3Ox6ByJLpwYomLQ1lOuRww3C2clYyCb8QFhWZKz35qTOkMBesCI-9bOoXCPIp9vW9h6Ir6cn8w4N-EY-iUbYHLHo");
        val temp_reddit_data = List_image("https://preview.redd.it/4pvg047t7bd91.jpg?width=640&crop=smart&auto=webp&s=eaba8ae7b0dc20cec4b5216c954392ed57c0e050");
        mylist = arrayOf(
            temp_reddit_data,temp_data,
            temp_data,temp_reddit_data,
            temp_data,temp_data,
            temp_reddit_data,temp_data
        );
        myrec = findViewById(R.id.Mainrec);
        myrec.layoutManager = LinearLayoutManager(this);
        myrec.setHasFixedSize(false);
        myrec.adapter = MyAdab(mylist,this);

    }

    external fun stringFromJNI(): String

    companion object {
        // Used to load the 'wallpaper' library on application startup.
        init {
            System.loadLibrary("wallpaper")
        }
    }

    override fun onImageClick(Pos: Int) {
        val intent = Intent(this,Image_Activity::class.java);
        intent.putExtra("UrlText",mylist.get(Pos).Image_url);
        intent.putExtra("AutherText",mylist.get(Pos).Image_auther);
        startActivity(intent);
    }
}