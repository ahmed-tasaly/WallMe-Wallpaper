package com.alaory.wallmewallpaper.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import com.alaory.wallmewallpaper.MainActivity
import com.alaory.wallmewallpaper.R

class settings : Fragment() {

    var clearCache : TextView? = null;
    var clearImages : TextView? = null;
    var github : TextView? = null;
    var supportMe : TextView? = null;

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val layout = inflater.inflate(R.layout.fragment_settings, container, false);

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            MainActivity.change_fragment(MainActivity.favoriteList, true);
        }

        clearCache = layout.findViewById(R.id.clear_cache_settings);
        clearImages = layout.findViewById(R.id.clear_saved_images_settings);
        github = layout.findViewById(R.id.github_settings);
        supportMe = layout.findViewById(R.id.support_settings);

        clearCache?.let {
            it.setOnClickListener {
                requireContext().cacheDir.resolve("imagePreview").deleteRecursively();
                Toast.makeText(requireContext(),"cleared cache",Toast.LENGTH_SHORT).show();
            }
        }
        clearImages?.let {
            it.setOnClickListener {
                requireContext().cacheDir.resolve("imagesaved").deleteRecursively();
                Toast.makeText(requireContext(),"cleared saved images",Toast.LENGTH_SHORT).show();
            }
        }
        github?.let {
            it.setOnClickListener {
                val uri = Uri.parse("https://github.com/Alaory/WallMe-Wallpaper");
                requireContext().startActivity(Intent(Intent.ACTION_VIEW,uri));
            }
        }
        supportMe?.let {
            it.setOnClickListener {
                val uri = Uri.parse("https://www.patreon.com/Alaory");
                requireContext().startActivity(Intent(Intent.ACTION_VIEW,uri));
            }
        }

        return layout;
    }

}