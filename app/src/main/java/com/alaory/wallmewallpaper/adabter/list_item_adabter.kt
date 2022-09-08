package com.alaory.wallmewallpaper.adabter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.alaory.wallmewallpaper.R

class list_item_adabter(var NameList: MutableList<String>,var onclick:Onclick): RecyclerView.Adapter<list_item_adabter.Item>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Item {
        val item = LayoutInflater.from(parent.context).inflate(R.layout.list_item_add_source,parent,false);
        return Item(item);
    }

    override fun onBindViewHolder(holder: Item, position: Int) {
        holder.textTitle.setText(NameList[position]);
        holder.list_item_add.setOnClickListener {
            onclick.onclick(NameList[position]);
        }
    }

    override fun getItemCount(): Int {
        return NameList.size;
    }


    class Item(view: View): RecyclerView.ViewHolder(view){
        val textTitle = view.findViewById<TextView>(R.id.list_item_name);
        val list_item_add = view.findViewById<ImageButton>(R.id.list_item_add);
        val list_item_remove = view.findViewById<ImageButton>(R.id.list_item_remove);
    }
    interface Onclick{
        fun onclick(name : String);
    }


}