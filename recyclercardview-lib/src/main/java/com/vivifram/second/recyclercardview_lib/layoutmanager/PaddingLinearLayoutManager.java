package com.vivifram.second.recyclercardview_lib.layoutmanager;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashSet;
import java.util.List;

/**
 * 项目名称：RecyclerCardView
 * 类描述：
 * 创建人：zuowei
 * 创建时间：16-11-29 下午4:16
 * 修改人：zuowei
 * 修改时间：16-11-29 下午4:16
 * 修改备注：
 */
public class PaddingLinearLayoutManager extends RecyclerView.LayoutManager {


    private static final int REMOVE_VISIBLE = 0;
    private static final int REMOVE_INVISIBLE = 1;

    private static final int DIRECTION_NONE = -1;
    private static final int DIRECTION_UP = 1;
    private static final int DIRECTION_DOWN = 2;

    //只支持等高的child
    private int decoratedChildHeight;

    private int firstChangedPosition;
    private int changedPositionCount;

    private int firstVisiblePosition;

    private int visibleLineCount;

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.WRAP_CONTENT,
                RecyclerView.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        //如果没有数据,则移除并且缓存所有的View
        if (getItemCount() == 0){
            detachAndScrapAttachedViews(recycler);
            return;
        }

        //如果在layout之前,检查到没有child需要绘制,则返回
        if (getChildCount() == 0 && state.isPreLayout()){
            return;
        }

        //如果已经在进行layout了,则重置firstChangedPosition,changedPositionCount
        if (!state.isPreLayout()){
            firstChangedPosition = changedPositionCount = 0;
        }

        //开始layout第一个child
        if (getChildCount() == 0){
            View scrap = recycler.getViewForPosition(0);
            addView(scrap);
            measureChildWithMargins(scrap, 0, 0);

            //第一次计算所有child的高度,并且保存复用
            decoratedChildHeight = getDecoratedMeasuredHeight(scrap);

            detachAndScrapView(scrap, recycler);
        }

        updateVisibleLineCount();

        SparseIntArray removedCache = null;

        //在layout之前,监控所有即将被删除的child
        if (state.isPreLayout()){
            removedCache = new SparseIntArray(getChildCount());
            for (int i = 0; i < getChildCount(); i++) {
                final View view = getChildAt(i);
                LayoutParams lp = (LayoutParams) view.getLayoutParams();
                if (lp.isItemChanged()){
                    removedCache.put(lp.getViewAdapterPosition(),REMOVE_VISIBLE);
                }
            }

            //如果移除的不是可见的item,则存储为ROMOVE_INVISIBLE
            if (removedCache.size() == 0 && changedPositionCount > 0){
                for (int i = firstChangedPosition; i < (firstChangedPosition + changedPositionCount); i++) {
                    removedCache.put(i, REMOVE_INVISIBLE);
                }
            }
        }

        int childTop;
        if (getChildCount() == 0) {
            firstVisiblePosition = 0;
            childTop = getPaddingTop();
        } else if (!state.isPreLayout()
                && getVisibleChildCount() >= state.getItemCount()){//如果不需要滑动
            firstVisiblePosition = 0;
            childTop = getPaddingTop();
        } else { //adapter set changed
            final View topChild = getChildAt(0);
            childTop = getDecoratedTop(topChild);
            //这里你有两种特殊情况需要处理
            //1.如果减少数据集且不足以滑动时,需要将childtop值置0
            //2.如果新的数据集很小但是还是能够滑动时,则需要调整firstVisiblePosition的值。

            if (!state.isPreLayout() && state.getItemCount() < getChildCount()){
                childTop = 0;
            }

            int maxLine = state.getItemCount() - (getVisibleChildCount() - 1);
            boolean isOutBounds = firstVisiblePosition > maxLine;
            if (isOutBounds){
                firstVisiblePosition = maxLine;
            }

        }

        detachAndScrapAttachedViews(recycler);

        fillLine(DIRECTION_NONE, childTop, recycler, state, removedCache);

        if (!state.isPreLayout() && !recycler.getScrapList().isEmpty()) {
            final List<RecyclerView.ViewHolder> scrapList = recycler.getScrapList();
            final HashSet<View> disappearingViews = new HashSet<View>(scrapList.size());

            for (RecyclerView.ViewHolder holder : scrapList) {
                final View child = holder.itemView;
                final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                if (!lp.isItemRemoved()) {
                    disappearingViews.add(child);
                }
            }

            for (View child : disappearingViews) {
                layoutDisappearingView(child);
            }
        }

    }

    private void fillLine(int direction, RecyclerView.Recycler recycler, RecyclerView.State state) {
        fillLine(direction, 0, recycler, state, null);
    }

    private void fillLine(int direction, int emptyTop,
                          RecyclerView.Recycler recycler,
                          RecyclerView.State state,
                          SparseIntArray removedPositions) {
        if (firstVisiblePosition < 0) firstVisiblePosition = 0;
        if (firstVisiblePosition >= getItemCount()) firstVisiblePosition = (getItemCount() - 1);

        SparseArray<View> viewCache = new SparseArray<View>(getChildCount());
        int startTopOffset = emptyTop;
        if (getChildCount() != 0) {
            final View topView = getChildAt(0);
            startTopOffset = getDecoratedTop(topView);
            switch (direction) {
                case DIRECTION_UP:
                    startTopOffset -= decoratedChildHeight;
                    break;
                case DIRECTION_DOWN:
                    startTopOffset += decoratedChildHeight;
                    break;
            }

            //Cache all views by their existing position, before updating counts
            for (int i=0; i < getChildCount(); i++) {
                int position = positionOfIndex(i);
                final View child = getChildAt(i);
                viewCache.put(position, child);
            }

            //Temporarily detach all views.
            // Views we still need will be added back at the proper index.
            for (int i=0; i < viewCache.size(); i++) {
                detachView(viewCache.valueAt(i));
            }
        }

        /*
         * Next, we advance the visible position based on the fill direction.
         * DIRECTION_NONE doesn't advance the position in any direction.
         */
        switch (direction) {
            case DIRECTION_UP:
                firstVisiblePosition -= 1;
                break;
            case DIRECTION_DOWN:
                firstVisiblePosition += 1;
                break;
        }

        /*
         * Next, we supply the grid of items that are deemed visible.
         * If these items were previously there, they will simply be
         * re-attached. New views that must be created are obtained
         * from the Recycler and added.
         */
        int topOffset = startTopOffset;

        for (int i = 0; i < getVisibleChildCount(); i++) {
            int nextPosition = positionOfIndex(i);

            /*
             * When a removal happens out of bounds, the pre-layout positions of items
             * after the removal are shifted to their final positions ahead of schedule.
             * We have to track off-screen removals and shift those positions back
             * so we can properly lay out all current (and appearing) views in their
             * initial locations.
             */
            int offsetPositionDelta = 0;
            if (state.isPreLayout()) {
                int offsetPosition = nextPosition;

                for (int offset = 0; offset < removedPositions.size(); offset++) {
                    //调整当前显示的view的position
                    if (removedPositions.valueAt(offset) == REMOVE_INVISIBLE
                            && removedPositions.keyAt(offset) < nextPosition) {
                        //Offset position to match
                        offsetPosition--;
                    }
                }
                offsetPositionDelta = nextPosition - offsetPosition;
                nextPosition = offsetPosition;
            }

            if (nextPosition < 0 || nextPosition >= state.getItemCount()) {
                //Item space beyond the data set, don't attempt to add a view
                continue;
            }

            //Layout this position
            View view = viewCache.get(nextPosition);
            if (view == null) {
                /*
                 * The Recycler will give us either a newly constructed view,
                 * or a recycled view it has on-hand. In either case, the
                 * view will already be fully bound to the data by the
                 * adapter for us.
                 */
                view = recycler.getViewForPosition(nextPosition);
                addView(view);

                /*
                 * Update the new view's metadata, but only when this is a real
                 * layout pass.
                 */
                if (!state.isPreLayout()) {
                    LayoutParams lp = (LayoutParams) view.getLayoutParams();
                    lp.line = nextPosition;
                }

                /*
                 * It is prudent to measure/layout each new view we
                 * receive from the Recycler. We don't have to do
                 * this for views we are just re-arranging.
                 */
                measureChildWithMargins(view, 0, 0);
                layoutDecorated(view, 0, topOffset,
                        getHorizontalSpace(),
                        topOffset + decoratedChildHeight);

            } else {
                //Re-attach the cached view at its new index
                attachView(view);
                viewCache.remove(nextPosition);
            }

            topOffset += decoratedChildHeight;

            //During pre-layout, on each column end, apply any additional appearing views
            if (state.isPreLayout()) {
                layoutAppearingViews(recycler, view, nextPosition, removedPositions.size(), offsetPositionDelta);
            }
        }

        /*
         * Finally, we ask the Recycler to scrap and store any views
         * that we did not re-attach. These are views that are not currently
         * necessary because they are no longer visible.
         */
        for (int i=0; i < viewCache.size(); i++) {
            final View removingView = viewCache.valueAt(i);
            recycler.recycleView(removingView);
        }
    }

    private int positionOfIndex(int i) {
        return firstVisiblePosition + i;
    }

    /* Helper to obtain and place extra appearing views */
    private void layoutAppearingViews(RecyclerView.Recycler recycler, View referenceView, int referencePosition, int extraCount, int offset) {
        //Nothing to do...
        if (extraCount < 1) return;

        //FIXME: This code currently causes double layout of views that are still visible…
        for (int extra = 1; extra <= extraCount; extra++) {
            //Grab the next position after the reference
            final int extraPosition = referencePosition + extra;
            if (extraPosition < 0 || extraPosition >= getItemCount()) {
                //Can't do anything with this
                continue;
            }

            /*
             * Obtain additional position views that we expect to appear
             * as part of the animation.
             */
            View appearing = recycler.getViewForPosition(extraPosition);
            addView(appearing);

            //Find layout delta from reference position
            final int newLine = extraPosition + offset;
            final int rowDelta = newLine - referencePosition - offset;

            layoutTempChildView(appearing, rowDelta, referenceView);
        }
    }

    private void layoutDisappearingView(View disappearingChild) {
        addDisappearingView(disappearingChild);

        final LayoutParams lp = (LayoutParams) disappearingChild.getLayoutParams();

        final int newLine = lp.getViewAdapterPosition();
        final int lineDelta = newLine - lp.line;

        layoutTempChildView(disappearingChild, lineDelta, disappearingChild);
    }

    private void layoutTempChildView(View child, int lineDelta, View referenceView) {
        int layoutTop = getDecoratedTop(referenceView) + lineDelta * decoratedChildHeight;

        measureChildWithMargins(child, 0, 0);
        layoutDecorated(child, 0, layoutTop, getHorizontalSpace(),
                layoutTop + decoratedChildHeight);
    }

    private void updateVisibleLineCount() {

        visibleLineCount = (getVerticalSpace()/ decoratedChildHeight) + 1;
        if (getVerticalSpace() % decoratedChildHeight > 0) {
            visibleLineCount++;
        }

        if (visibleLineCount > getItemCount()) {
            visibleLineCount = getItemCount();
        }
    }

    private int getTotalChildHeight() {

        return 0;
    }

    private int getVisibleChildCount() {
        return visibleLineCount;
    }

    private int getHorizontalSpace() {
        return getWidth() - getPaddingRight() - getPaddingLeft();
    }

    private int getVerticalSpace() {
        return getHeight() - getPaddingBottom() - getPaddingTop();
    }

    public static class LayoutParams extends RecyclerView.LayoutParams {

        int line;
        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }
        public LayoutParams(int width, int height) {
            super(width, height);
        }
        public LayoutParams(ViewGroup.MarginLayoutParams source) {
            super(source);
        }
        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }
        public LayoutParams(RecyclerView.LayoutParams source) {
            super(source);
        }
    }
}
