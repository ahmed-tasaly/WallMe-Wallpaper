package com.alaory.wallmewallpaper

import android.util.Log
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

class BottonLoading {
    class ViewLodMore() : RecyclerView.OnScrollListener() {
        private var visableThreshold = 5;
        private lateinit var onloadmore : OnLoadMoreListener;
        private var isLoading = false;
        private var lastVisableItem : Int = 0;
        private var totalItemCount: Int = 0;
        private lateinit var MlayoutManager: RecyclerView.LayoutManager;


        constructor(layoutManager: GridLayoutManager) : this() {
            this.MlayoutManager = layoutManager;
            visableThreshold *= layoutManager.spanCount;
        }
        constructor(layoutManager: StaggeredGridLayoutManager) : this() {
            this.MlayoutManager = layoutManager;
            visableThreshold *= layoutManager.spanCount;
        }


        fun setLoaded(){
            isLoading = false;
        }
        fun setOnLoadMoreListener(listener : OnLoadMoreListener){
            onloadmore = listener;
        }


        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if(dy <= 0) return;

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
        fun onLoadMore()
    }
}