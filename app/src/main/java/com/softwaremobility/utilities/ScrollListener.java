package com.softwaremobility.utilities;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;


public abstract class ScrollListener extends RecyclerView.OnScrollListener {

    public static String TAG = ScrollListener.class.getSimpleName();

    private int previousTotal = 0;
    private boolean loading = false;
    private int visibleThreshold = 2;
    private int itemsPerPage = 10;
    private int count = 0;
    int firstVisibleItem, visibleItemCount, totalItemCount;

    private int current_page = 0;
    private int last_page = 0;
    private boolean isGrid = false;

    private LinearLayoutManager mLinearLayoutManager;
    private GridLayoutManager mGridLayoutManager;

    public ScrollListener(RecyclerView.LayoutManager layoutManager, boolean isGrid, int page) {
        current_page = last_page = page;
        this.isGrid = isGrid;
        if (isGrid) {
            mGridLayoutManager = (GridLayoutManager) layoutManager;
        }else{
            mLinearLayoutManager = (LinearLayoutManager) layoutManager;
        }
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        visibleItemCount = recyclerView.getChildCount();
        totalItemCount = isGrid ? mGridLayoutManager.getItemCount() : mLinearLayoutManager.getItemCount();
        firstVisibleItem = isGrid ? mGridLayoutManager.findFirstVisibleItemPosition() : mLinearLayoutManager.findFirstVisibleItemPosition();


        if (loading) {
            if (totalItemCount > previousTotal) {
                loading = false;
                previousTotal = totalItemCount;
                current_page++;
            }else {

                if ((totalItemCount - itemsPerPage) == firstVisibleItem){
                    if (isGrid){
                        if (count >= 1){
                            if (!(current_page < 0) && current_page > (last_page - 1)) {
                                current_page--;
                            }
                            count = 0;
                            loading = false;
                        }else {
                            count++;
                        }
                    }else {
                        current_page--;
                        loading = false;
                    }
                }
            }
        }

        if (!loading
                && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
            // End has been reached
            Log.d(TAG,"" + current_page + 1);
            onLoadMore(current_page + 1);

            loading = true;
        }


    }

    public abstract void onLoadMore(int current_page);

    public void setCurrent_page(int current_page) {
        this.current_page = current_page;
    }
}
