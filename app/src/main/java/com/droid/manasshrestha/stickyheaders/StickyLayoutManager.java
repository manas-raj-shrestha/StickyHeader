package com.droid.manasshrestha.stickyheaders;

import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.Px;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.droid.manasshrestha.stickyheaders.sticky.StickyHeader;
import com.droid.manasshrestha.stickyheaders.sticky.StickyHeaderHandler;
import com.droid.manasshrestha.stickyheaders.sticky.ViewRetriever;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class StickyLayoutManager extends LinearLayoutManager {

    static final int NO_ELEVATION = -1;
    private static final int INVALID_POSITION = -1;
    private final String TAG = this.getClass().getSimpleName();

    private int orientation;
    private boolean dirty;
    private int lastBoundPosition = INVALID_POSITION;
    private ArrayList<Integer> headerPositions;
    private boolean updateCurrentHeader;
    private boolean checkMargins;
    private int headerElevation = NO_ELEVATION;
    private int cachedElevation = NO_ELEVATION;

    private View currentHeader;
    private RecyclerView.ViewHolder currentViewHolder;
    private StickyHeaderHandler stickyHeaderHandler;
    private RecyclerView recyclerView;
    private ViewRetriever.RecyclerViewRetriever viewRetriever;

    public StickyLayoutManager(Context context, StickyHeaderHandler stickyHeaderHandler) {
        this(context, VERTICAL, false, stickyHeaderHandler);
    }

    public StickyLayoutManager(Context context, int orientation, boolean reverseLayout, StickyHeaderHandler stickyHeaderHandler) {
        super(context, orientation, reverseLayout);
        this.stickyHeaderHandler = stickyHeaderHandler;
        headerPositions = new ArrayList<>();
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        super.onLayoutChildren(recycler, state);
        cacheHeaderPositions();
        Log.e(TAG, "onLayoutChildren");

        reset();
        updateHeaderState(
                findFirstVisibleItemPosition(), getVisibleHeaders(), viewRetriever);
    }

    void updateHeaderState(int firstVisiblePosition, Map<Integer, View> visibleHeaders,
                           ViewRetriever viewRetriever) {
        int headerPositionToShow = getHeaderPositionToShow(
                firstVisiblePosition, visibleHeaders.get(firstVisiblePosition));
        View headerToCopy = visibleHeaders.get(headerPositionToShow);
        if (headerPositionToShow != lastBoundPosition || updateCurrentHeader) {
            if (headerPositionToShow == INVALID_POSITION) {
                detachHeader();
                lastBoundPosition = INVALID_POSITION;
            } else {
                // We don't want to attach yet if header view is not at edge
                if (checkMargins && headerAwayFromEdge(headerToCopy)) return;
                RecyclerView.ViewHolder viewHolder =
                        viewRetriever.getViewHolderForPosition(headerPositionToShow);
                attachHeader(viewHolder, headerPositionToShow);
                lastBoundPosition = headerPositionToShow;
            }
        } else if (checkMargins) {
            /**
             * This could still be our firstVisiblePosition even if another view is visible above it.
             * See {@link #getHeaderPositionToShow(int, View)} for explanation.
             */
            if (headerAwayFromEdge(headerToCopy)) {
                detachHeader();
                lastBoundPosition = INVALID_POSITION;
            }
        }
        checkHeaderPositions(visibleHeaders);
        recyclerView.post(new Runnable() {
            @Override
            public void run() {
                checkElevation();
            }
        });
    }

    private void cacheHeaderPositions() {
        headerPositions.clear();
        for (int i = 0; i < stickyHeaderHandler.getAdapterData().size(); i++) {
            if (stickyHeaderHandler.getAdapterData().get(i) instanceof StickyHeader) {
                headerPositions.add(i);
            }
        }
    }

    @Override
    public void onAttachedToWindow(RecyclerView view) {
        super.onAttachedToWindow(view);
        Log.e(TAG, "onAttachedToWindow");

        this.recyclerView = view;
        viewRetriever = new ViewRetriever.RecyclerViewRetriever(recyclerView);
        setElevateHeaders(headerElevation);
    }


    private Map<Integer, View> getVisibleHeaders() {
        Map<Integer, View> visibleHeaders = new LinkedHashMap<>();

        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            int dataPosition = getPosition(view);
            if (headerPositions.contains(dataPosition)) {
                visibleHeaders.put(dataPosition, view);
            }
        }

        return visibleHeaders;
    }

    void reset() {
        this.orientation = getOrientation();
        // Don't reset/detach if same header position is to be attached
        if (getHeaderPositionToShow(findFirstVisibleItemPosition(), null) == lastBoundPosition) {
            return;
        }

        dirty = true;
        safeDetachHeader();
        lastBoundPosition = INVALID_POSITION;
    }

    private int getHeaderPositionToShow(int firstVisiblePosition, @Nullable View headerForPosition) {
        int headerPositionToShow = INVALID_POSITION;
        if (headerIsOffset(headerForPosition)) {
            int offsetHeaderIndex = headerPositions.indexOf(firstVisiblePosition);
            if (offsetHeaderIndex > 0) {
                return headerPositions.get(offsetHeaderIndex - 1);
            }
        }
        for (Integer headerPosition : headerPositions) {
            if (headerPosition <= firstVisiblePosition) {
                headerPositionToShow = headerPosition;
            } else {
                break;
            }
        }
        return headerPositionToShow;
    }

    private boolean headerIsOffset(View headerForPosition) {
        if (headerForPosition != null) {
            return orientation == LinearLayoutManager.VERTICAL ?
                    headerForPosition.getY() > 0 : headerForPosition.getX() > 0;
        }
        return false;
    }

    private void safeDetachHeader() {
        getRecyclerParent().getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            getRecyclerParent().getViewTreeObserver()
                                    .removeOnGlobalLayoutListener(this);
                        } else {
                            //noinspection deprecation
                            getRecyclerParent().getViewTreeObserver()
                                    .removeGlobalOnLayoutListener(this);
                        }
                        if (dirty) {
                            detachHeader();
                        }
                    }
                });
    }

    private ViewGroup getRecyclerParent() {
        return (ViewGroup) recyclerView.getParent();
    }

    private void detachHeader() {
        if (currentHeader != null) {
            getRecyclerParent().removeView(currentHeader);
            currentHeader = null;
            currentViewHolder = null;
        }
    }

    private void checkElevation() {
        if (headerElevation != NO_ELEVATION && currentHeader != null) {
            if (orientation == LinearLayoutManager.VERTICAL && currentHeader.getTranslationY() == 0
                    || orientation == LinearLayoutManager.HORIZONTAL && currentHeader.getTranslationX() == 0) {
                elevateHeader();
            } else {
                settleHeader();
            }
        }
    }

    private boolean headerAwayFromEdge(View headerToCopy) {
        if (headerToCopy != null) {
            return orientation == LinearLayoutManager.VERTICAL ?
                    headerToCopy.getY() > 0 : headerToCopy.getX() > 0;
        }
        return false;
    }

    private void elevateHeader() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (currentHeader.getTag() != null) {
                // Already elevated, bail out
                return;
            }
            currentHeader.setTag(true);
            currentHeader.animate().z(headerElevation);
        }
    }

    private void settleHeader() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (currentHeader.getTag() != null) {
                currentHeader.setTag(null);
                currentHeader.animate().z(0);
            }
        }
    }

    private void attachHeader(RecyclerView.ViewHolder viewHolder, int headerPosition) {
        if (currentViewHolder == viewHolder) {
            //noinspection unchecked
            recyclerView.getAdapter().onBindViewHolder(currentViewHolder, headerPosition);
            updateCurrentHeader = false;
            return;
        }
        detachHeader();
        this.currentViewHolder = viewHolder;
        //noinspection unchecked
        recyclerView.getAdapter().onBindViewHolder(currentViewHolder, headerPosition);
        this.currentHeader = currentViewHolder.itemView;
        resolveElevationSettings(currentHeader.getContext());
        // Set to Invisible until we position it in #checkHeaderPositions.
        currentHeader.setVisibility(View.INVISIBLE);
        currentHeader.setId(R.id.header_view);
        getRecyclerParent().addView(currentHeader);
        if (checkMargins) {
            updateLayoutParams(currentHeader);
        }
        dirty = false;
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        int scroll = super.scrollVerticallyBy(dy, recycler, state);
        if (Math.abs(scroll) > 0) {
            updateHeaderState(
                    findFirstVisibleItemPosition(), getVisibleHeaders(), viewRetriever);
        }
        return scroll;
    }

    private void resolveElevationSettings(Context context) {
        if (cachedElevation != NO_ELEVATION && headerElevation == NO_ELEVATION) {
            headerElevation = (int) GeneralUtils.pxFromDp(context, cachedElevation);
        }
    }

    private void updateLayoutParams(View currentHeader) {
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) currentHeader.getLayoutParams();
        matchMarginsToPadding(params);
    }

    private void matchMarginsToPadding(ViewGroup.MarginLayoutParams layoutParams) {
        @Px int leftMargin = orientation == LinearLayoutManager.VERTICAL ?
                recyclerView.getPaddingLeft() : 0;
        @Px int topMargin = orientation == LinearLayoutManager.VERTICAL ?
                0 : recyclerView.getPaddingTop();
        @Px int rightMargin = orientation == LinearLayoutManager.VERTICAL ?
                recyclerView.getPaddingRight() : 0;
        layoutParams.setMargins(leftMargin, topMargin, rightMargin, 0);
    }

    void checkHeaderPositions(final Map<Integer, View> visibleHeaders) {
        if (currentHeader == null) return;
        // This can happen after configuration changes.
        if (currentHeader.getHeight() == 0) {
            waitForLayoutAndRetry(visibleHeaders);
            return;
        }
        boolean reset = false;
        for (Map.Entry<Integer, View> entry : visibleHeaders.entrySet()) {
            if (entry.getKey() == lastBoundPosition) {
                reset = true;
                continue;
            }
            View nextHeader = entry.getValue();
            reset = offsetHeader(nextHeader) == -1;
            break;
        }
        if (reset) resetTranslation();
        currentHeader.setVisibility(View.VISIBLE);
    }

    private void waitForLayoutAndRetry(final Map<Integer, View> visibleHeaders) {
        currentHeader.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override public void onGlobalLayout() {
                        // If header was removed during layout
                        if (currentHeader == null) return;

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            currentHeader.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        } else {
                            //noinspection deprecation
                            currentHeader.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        }
                        getRecyclerParent().requestLayout();
                        checkHeaderPositions(visibleHeaders);
                    }
                });
    }

    private void resetTranslation() {
        if (orientation == LinearLayoutManager.VERTICAL) {
            currentHeader.setTranslationY(0);
        } else {
            currentHeader.setTranslationX(0);
        }
    }

    private float offsetHeader(View nextHeader) {
        boolean shouldOffsetHeader = shouldOffsetHeader(nextHeader);
        float offset = -1;
        if (shouldOffsetHeader) {
            if (orientation == LinearLayoutManager.VERTICAL) {
                offset = -(currentHeader.getHeight() - nextHeader.getY());
                currentHeader.setTranslationY(offset);
                Log.e("####","Vertical");
            } else {
                offset = -(currentHeader.getWidth() - nextHeader.getX());
                currentHeader.setTranslationX(offset);
            }
        }
        return offset;
    }

    private boolean shouldOffsetHeader(View nextHeader) {
        if (orientation == LinearLayoutManager.VERTICAL) {
            return nextHeader.getY() < currentHeader.getHeight();
        } else {
            return nextHeader.getX() < currentHeader.getWidth();
        }
    }


    void setElevateHeaders(int dpElevation) {
        if (dpElevation != NO_ELEVATION) {
            // Context may not be available at this point, so caching the dp value to be converted
            // into pixels after first header is attached.
            cachedElevation = dpElevation;
        } else {
            headerElevation = NO_ELEVATION;
            cachedElevation = NO_ELEVATION;
        }
    }
}
