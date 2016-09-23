package com.droid.manasshrestha.stickyheaders;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.droid.manasshrestha.stickyheaders.sticky.ViewRetriever;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class StickyHeaderLayoutManager extends LinearLayoutManager {

    private static final int INVALID_POSITION = -1;
    private static final int FIRST_ITEM = 0;
    private static final int INITIAL_TRANSLATION = 0;

    private ViewGroup viewGroup;
    private View headerView;
    private RecyclerView recyclerView;
    private ViewRetriever.RecyclerViewRetriever viewRetriever;

    private Map<Integer, View> allHeaders = new LinkedHashMap<>();
    private ArrayList<Integer> headerPositions = new ArrayList<>();

    private int previousPosition = INVALID_POSITION;
    private int currentPosition = INVALID_POSITION;

    public StickyHeaderLayoutManager(Context context) {
        this(context, VERTICAL, false);
    }

    public StickyHeaderLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);

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

        viewRetriever = new ViewRetriever.RecyclerViewRetriever(recyclerView);
        headerView = getItemView(FIRST_ITEM);

        viewGroup.addView(headerView);
    }


    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (headerPositions.contains(findFirstCompletelyVisibleItemPosition())) {
            int translationY = -(headerView.getBottom() - getVisibleHeaders().get(findFirstCompletelyVisibleItemPosition()).getTop());
            headerView.setTranslationY(translationY);

            Log.e("EXP", headerView.getBottom() - getVisibleHeaders().get(findFirstCompletelyVisibleItemPosition()).getTop() + "");

            currentPosition = findFirstCompletelyVisibleItemPosition();
            int indexOfCurrentPosition = headerPositions.indexOf(currentPosition);

            if (indexOfCurrentPosition > 0) {
                previousPosition = headerPositions.get(indexOfCurrentPosition - 1);
            }

            attachPreviousHeader();

        } else if (headerPositions.contains(findFirstCompletelyVisibleItemPosition() - 1)) {
            if (headerPositions.contains(findFirstCompletelyVisibleItemPosition() - 1)) {
                viewGroup.removeView(headerView);
                headerView = getItemView(findFirstCompletelyVisibleItemPosition() - 1);
                viewGroup.addView(headerView);
            }

            headerView.setTranslationY(INITIAL_TRANSLATION);
        } else {
            headerView.setTranslationY(INITIAL_TRANSLATION);
        }

        return super.scrollVerticallyBy(dy, recycler, state);
    }

    private void attachPreviousHeader() {
        if (allHeaders.containsKey(previousPosition)) {
            viewGroup.removeView(headerView);
            View previousView = allHeaders.get(previousPosition);

            if (previousView != null) {
                headerView = getItemView(previousPosition);
                headerView.setTranslationY(-(headerView.getBottom() - getVisibleHeaders().get(findFirstCompletelyVisibleItemPosition()).getTop()));
                viewGroup.addView(headerView);
            }
        }
    }

    private Map<Integer, View> getVisibleHeaders() {
        Map<Integer, View> visibleHeaders = new LinkedHashMap<>();

        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            int viewPosition = getPosition(view);
            if (headerPositions.contains(viewPosition)) {
                allHeaders.put(viewPosition, view);
                visibleHeaders.put(viewPosition, view);
            }
        }

        return visibleHeaders;
    }

    private View getItemView(int position) {
        RecyclerView.ViewHolder prevViewHolder = viewRetriever.getViewHolderForPosition(position);
        recyclerView.getAdapter().onBindViewHolder(prevViewHolder, position);

        return prevViewHolder.itemView;
    }

}
