package com.alaory.wallmewallpaper

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class BottonLoading {
    class ViewLodMore(layoutManager: GridLayoutManager) : RecyclerView.OnScrollListener() {
        private var visableThreshold = 5;
        private lateinit var onloadmore : OnLoadMoreListener;
        private var isLoading = false;
        private var lastVisableItem : Int = 0;
        private var totalItemCount: Int = 0;
        private var MlayoutManager: RecyclerView.LayoutManager = layoutManager;

        fun setLoaded(){
            isLoading = false;
        }
        fun setOnLoadMoreListener(listener : OnLoadMoreListener){
            onloadmore = listener;
        }
        init {
            visableThreshold *= layoutManager.spanCount
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if(dy <= 0) return;
            totalItemCount = MlayoutManager.itemCount;
            lastVisableItem = (MlayoutManager as GridLayoutManager).findLastVisibleItemPosition();
            if(!isLoading && totalItemCount <= lastVisableItem + visableThreshold){
                onloadmore.onLoadMore();
                isLoading =true;
            }
        }
    }
    interface OnLoadMoreListener {
        fun onLoadMore()
    }
}