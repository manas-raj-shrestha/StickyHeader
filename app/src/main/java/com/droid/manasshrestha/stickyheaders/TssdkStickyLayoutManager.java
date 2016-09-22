package com.droid.manasshrestha.stickyheaders;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.droid.manasshrestha.stickyheaders.sticky.StickyHeaderHandler;
import com.droid.manasshrestha.stickyheaders.sticky.ViewRetriever;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class TssdkStickyLayoutManager extends LinearLayoutManager {

    ViewGroup viewGroup;
    Context context;
    View headerView;
    RecyclerView recyclerView;

    Map<Integer, View> allHeaders = new LinkedHashMap<>();

    ArrayList<Integer> headerPositions = new ArrayList<>();

    int previousPosition = -1;
    int currentPosition = -1;
    private ViewRetriever.RecyclerViewRetriever viewRetriever;

    public TssdkStickyLayoutManager(Context context, StickyHeaderHandler stickyHeaderHandler) {
        this(context, VERTICAL, false);
    }

    public TssdkStickyLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
        this.context = context;

        headerPositions.add(0);
        headerPositions.add(10);
        headerPositions.add(20);
        headerPositions.add(30);
        headerPositions.add(40);
        headerPositions.add(50);
        headerPositions.add(60);
        headerPositions.add(70);
        headerPositions.add(80);
        headerPositions.add(90);
    }

    @Override
    public void onAttachedToWindow(RecyclerView view) {
        super.onAttachedToWindow(view);
        viewGroup = (ViewGroup) view.getParent();
        recyclerView = view;

        headerView = LayoutInflater.from(context).inflate(R.layout.item_view, viewGroup, false);

        viewGroup.addView(headerView);
        viewRetriever = new ViewRetriever.RecyclerViewRetriever(recyclerView);
    }


    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (headerPositions.contains(findFirstCompletelyVisibleItemPosition())) {
            headerView.setTranslationY(-(headerView.getBottom() - getVisibleHeaders().get(findFirstCompletelyVisibleItemPosition()).getTop()));

            currentPosition = findFirstCompletelyVisibleItemPosition();
            int indexOfCurrentPosition = headerPositions.indexOf(currentPosition);

            if (indexOfCurrentPosition > 0)
                previousPosition = headerPositions.get(indexOfCurrentPosition - 1);

            if (allHeaders.containsKey(previousPosition)) {
                viewGroup.removeView(headerView);
                View previousView = allHeaders.get(previousPosition);
                if (previousView != null) {

                    RecyclerView.ViewHolder prevViewHolder = viewRetriever.getViewHolderForPosition(previousPosition);
                    recyclerView.getAdapter().onBindViewHolder(prevViewHolder, previousPosition);
                    headerView = prevViewHolder.itemView;
                    headerView.setTranslationY(-(headerView.getBottom() - getVisibleHeaders().get(findFirstCompletelyVisibleItemPosition()).getTop()));
                    viewGroup.addView(headerView);
                }
            }

        } else if (headerPositions.contains(findFirstCompletelyVisibleItemPosition() - 1)) {
            if (headerPositions.contains(findFirstCompletelyVisibleItemPosition() - 1)) {
                viewGroup.removeView(headerView);
                RecyclerView.ViewHolder prevViewHolder = viewRetriever.getViewHolderForPosition(findFirstCompletelyVisibleItemPosition() - 1);
                recyclerView.getAdapter().onBindViewHolder(prevViewHolder, findFirstCompletelyVisibleItemPosition() - 1);
                headerView = prevViewHolder.itemView;
                viewGroup.addView(headerView);
            }

            headerView.setTranslationY(0);
        } else {
            headerView.setTranslationY(0);
        }

        return super.scrollVerticallyBy(dy, recycler, state);
    }

    private Map<Integer, View> getVisibleHeaders() {
        Map<Integer, View> visibleHeaders = new LinkedHashMap<>();

        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            int dataPosition = getPosition(view);
            if (headerPositions.contains(dataPosition)) {
                allHeaders.put(dataPosition, view);
                visibleHeaders.put(dataPosition, view);
            }
        }

        return visibleHeaders;
    }

}
