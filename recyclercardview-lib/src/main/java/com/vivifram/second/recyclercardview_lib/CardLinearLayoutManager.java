package com.vivifram.second.recyclercardview_lib;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by zuowei on 16-11-27.
 */

public class CardLinearLayoutManager extends LinearLayoutManager implements RecyclerCardView.ScrollVectorProvider {

    private int decoratedChildHeight;
    private int decorateChildWidth;
    private ChildProximityListener childProximityListener;
    private FakeViewCreator fakeViewCreator;

    public CardLinearLayoutManager(Context context) {
        super(context);
    }

    public CardLinearLayoutManager(Context context, boolean reverseLayout) {
        this(context, VERTICAL, reverseLayout);
    }

    public CardLinearLayoutManager(Context context,int orientation,boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (getChildCount() == 0){
            View fakeView = fakeViewCreator.create(recycler);
            if (fakeView != null) {
                addView(fakeView);
                measureChildWithMargins(fakeView, 0, 0);

                //decoratedChildHeight = getDecoratedMeasuredHeight(fakeView) - fakeView.getPaddingTop() - fakeView.getPaddingBottom();
                decoratedChildHeight = getDecoratedMeasuredHeight(fakeView);
                decorateChildWidth = getDecoratedMeasuredWidth(fakeView);

                removeView(fakeView);//not scrap just remove it
            }
        }

        super.onLayoutChildren(recycler, state);

        if (childProximityListener != null) {
            childProximityListener.notifyChildrenAboutProximity(false);
        }

    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        int rdy = super.scrollVerticallyBy(dy, recycler, state);

        if (childProximityListener != null) {
            childProximityListener.notifyChildrenAboutProximity(true);
        }

        return rdy;
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        int rdx = super.scrollHorizontallyBy(dx, recycler, state);

        if (childProximityListener != null) {
            childProximityListener.notifyChildrenAboutProximity(true);
        }

        return rdx;
    }

    void setChildProximityListener(ChildProximityListener childProximityListener) {
        this.childProximityListener = childProximityListener;
    }

    public void setFakeViewCreator(FakeViewCreator fakeViewCreator) {
        this.fakeViewCreator = fakeViewCreator;
    }

    public int getDecoratedChildHeight() {
        return decoratedChildHeight;
    }

    public int getDecorateChildWidth() {
        return decorateChildWidth;
    }

    int getVerticalSpace() {
        return getHeight() - getPaddingBottom() - getPaddingTop();
    }

    int getHorizontalSpace() {
        return getWidth() - getPaddingRight() - getPaddingLeft();
    }
}
