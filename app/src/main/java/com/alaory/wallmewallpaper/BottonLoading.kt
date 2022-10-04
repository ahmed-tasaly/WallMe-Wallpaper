package com.alaory.wallmewallpaper

import android.util.Log
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

class BottonLoading {
    companion object{
        var loctionbottom = 0;
        val TAG = "BottonLoading_log";
    }


    class ViewLodMore() : RecyclerView.OnScrollListener() {
        private var visableThreshold = 5;
        private lateinit var onloadmore : OnLoadMoreListener;
        private var isLoading = false;
        private var lastVisableItem : Int = 0;
        private var totalItemCount: Int = 0;
        private lateinit var MlayoutManager: RecyclerView.LayoutManager;
        private var MenuChange : MainActivity.MenuChange? = null;


        constructor(layoutManager: GridLayoutManager,menuChange : MainActivity.MenuChange? = null) : this() {
            this.MlayoutManager = layoutManager;
            visableThreshold *= layoutManager.spanCount;
            this.MenuChange = menuChange;
        }
        constructor(layoutManager: StaggeredGridLayoutManager,menuChange : MainActivity.MenuChange? = null) : this() {
            this.MlayoutManager = layoutManager;
            visableThreshold *= layoutManager.spanCount;
            this.MenuChange = menuChange;
        }


        fun setLoaded(){
            isLoading = false;
        }
        fun setOnLoadMoreListener(listener : OnLoadMoreListener){
            onloadmore = listener;
        }



        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            MenuChange?.PlayAnimation_forNav {
                loctionbottom += if(dy < 0) dy * 2 else dy;
                it?.duration = 0;
                if(loctionbottom > 1000){
                    loctionbottom = 1000;
                }else if(loctionbottom < 0){
                    loctionbottom = 0;
                }
                it?.translationY(loctionbottom.toFloat());
            }


            totalItemCount = MlayoutManager.itemCount;
            if(MlayoutManager is GridLayoutManager)
                lastVisableItem = (MlayoutManager as GridLayoutManager).findLastVisibleItemPosition();
            else if(MlayoutManager is StaggeredGridLayoutManager){
                val lastVisableItemtemp = (MlayoutManager as StaggeredGridLayoutManager).findLastVisibleItemPositions(null);
                lastVisableItem = getLastVisibleItem(lastVisableItemtemp)
            }


            if(!isLoading && totalItemCount <= lastVisableItem + visableThreshold){
                onloadmore.onLoadMore();
                isLoading = true;
            }
        }

        private fun getLastVisibleItem(lastVisibleItemPositions: IntArray): Int {
            var maxSize = 0

            for(i in lastVisibleItemPositions.indices){
                if(i == 0 || maxSize < lastVisibleItemPositions[i]){
                    maxSize = lastVisibleItemPositions[i];
                }
            }
            return maxSize
        }
    }
    interface OnLoadMoreListener {
        fun onLoadMore();
    }
}