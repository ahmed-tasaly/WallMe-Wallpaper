package com.alaory.wallmewallpaper.adabter

import android.content.res.ColorStateList

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.alaory.wallmewallpaper.R

class list_item_adabter(var NameList: MutableList<String>,var onclick:Onclick?,var showremoveonly : Boolean = false): RecyclerView.Adapter<list_item_adabter.Item>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Item {
        val item = LayoutInflater.from(parent.context).inflate(R.layout.list_item_add_source,parent,false);
        return Item(item);
    }

    override fun onBindViewHolder(holder: Item, position: Int) {
        holder.textTitle.setText(NameList[position]);



        if(showremoveonly){
            holder.list_item_add.visibility = View.GONE;
            holder.list_item_remove.visibility = View.VISIBLE;
            holder.list_item_remove.imageTintList = ColorStateList.valueOf(holder.list_item_remove.context.resources.getColor(R.color.Buttons,holder.list_item_remove.context.theme));
            holder.list_item_remove.setOnClickListener {
                removeItem(holder.layoutPosition);
            }
        }else{
            holder.list_item_add.setOnClickListener {
                onclick!!.onclick(NameList[position]);
            }
        }

        if(holder.textTitle.text.isEmpty()){
            holder.list_item_remove.visibility = View.GONE;
            holder.textTitle.visibility = View.GONE;
            holder.list_item_add.visibility = View.GONE;
            holder.list_item_add_new.visibility = View.VISIBLE;
            holder.list_item_add_new.setOnClickListener {
                onclick?.addmore();
            }
        }

    }

    override fun getItemCount(): Int {
        return NameList.size;
    }

    fun removeItem(holder: Int){
        notifyItemRemoved(holder);
        NameList.removeAt(holder);
        if (NameList.isNotEmpty())
            notifyItemChanged(holder,NameList.size);
    }


    class Item(view: View): RecyclerView.ViewHolder(view){
        val rootlistitem = view.findViewById<ConstraintLayout>(R.id.root_list_item);
        val textTitle = view.findViewById<TextView>(R.id.list_item_name);
        val list_item_add = view.findViewById<ImageButton>(R.id.list_item_add);
        val list_item_remove = view.findViewById<ImageButton>(R.id.list_item_remove);
        val list_item_add_new = view.findViewById<ImageButton>(R.id.list_item_add_new);
    }
    interface Onclick{
        fun onclick(name : String);
        fun addmore();
    }


}