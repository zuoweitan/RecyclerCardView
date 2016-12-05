package com.vivifram.second.recyclercardview_lib.layoutmanager;

import android.content.Context;
import android.graphics.PointF;
import android.support.v4.widget.ScrollerCompat;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
public class PaddingLayoutManager extends RecyclerView.LayoutManager implements Runnable{

    private static final String TAG = "PaddingLayoutManager";

    public static final int HORIZONTAL = OrientationHelper.HORIZONTAL;

    public static final int VERTICAL = OrientationHelper.VERTICAL;

    private static final int REMOVE_VISIBLE = 0;
    private static final int REMOVE_INVISIBLE = 1;

    private static final int DIRECTION_NONE = -1;
    private static final int DIRECTION_UP = 1;
    private static final int DIRECTION_DOWN = 2;

    //view type

    private static int TYPE_NORMAL = 0;

    //只支持等高或等宽的child
    private int decoratedChildWidth;
    private int decoratedChildHeight;

    private int firstChangedPosition;
    private int changedPositionCount;

    private int firstVisiblePosition;

    private int visibleLineCount;
    private int visibled;

    private final int scrollDistance;

    int orientation;
    int maxGap;
    int startOffset;
    int firstOffset;
    Context ctx;
    ScrollerCompat offsetScroller;

    public PaddingLayoutManager(Context context){
        this(context,3);
    }

    public PaddingLayoutManager(Context context,int visibled,int orientation){
        this.visibled = visibled;
        this.orientation = orientation;
        ctx = context;
        DisplayMetrics dm = ctx.getResources().getDisplayMetrics();
        scrollDistance = (int) (80 * dm.density + 0.5f);
        offsetScroller = ScrollerCompat.create(ctx,sQuinticInterpolator);
    }

    public PaddingLayoutManager(Context context, int visibled){
        this(context,visibled,VERTICAL);
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

        updateVisibleCount();

        updateGap();

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
            childTop = getPaddingTop() + firstOffset;
        } else if (!state.isPreLayout()
                && getVisibleChildCount() >= state.getItemCount()){//如果不需要滑动
            firstVisiblePosition = 0;
            childTop = getPaddingTop() + firstOffset;
        } else { //adapter set changed
            final View topChild = getChildAt(0);
            childTop = getDecoratedTop(topChild);
            //这里你有两种特殊情况需要处理
            //1.如果减少数据集且不足以滑动时,需要将childtop重置
            //2.如果新的数据集很小但是还是能够滑动时,则需要调整firstVisiblePosition的值。

            if (!state.isPreLayout() && state.getItemCount() < getChildCount()){
                childTop = getPaddingTop() + firstOffset;
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
        if (firstVisiblePosition >= getItemCount()) firstVisiblePosition = getItemCount() - 1;

        SparseArray<View> viewCache = new SparseArray<>(getChildCount());
        int startTopOffset = emptyTop;
        if (getChildCount() != 0) {
            final View topView = getChildAt(0);
            startTopOffset = getDecoratedTop(topView);

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

            topOffset += decoratedChildHeight + maxGap;

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
        int layoutTop = getDecoratedTop(referenceView) + lineDelta * (decoratedChildHeight + maxGap);

        measureChildWithMargins(child, 0, 0);
        layoutDecorated(child, 0, layoutTop, getHorizontalSpace(),
                layoutTop + decoratedChildHeight);
    }

    private void updateVisibleCount() {

        visibleLineCount = (getVerticalSpace()/ decoratedChildHeight) + 1;
        if (getVerticalSpace() % decoratedChildHeight > 0) {
            visibleLineCount++;
        }

        if (visibleLineCount > getItemCount()) {
            visibleLineCount = getItemCount();
        }

        if (visibled > visibleLineCount){
            visibled = visibleLineCount;
        }
    }

    private void updateGap() {
        if (visibled < visibleLineCount){
            int t = 0;
            if (visibled % 2 == 0 || visibled == 1){
                t = getVerticalSpace() - (visibled * decoratedChildHeight);
                t /= (visibled + 1);
                startOffset = t < 0?0:t;
                firstOffset = startOffset;
            }else {
                t = getVerticalSpace() - ((visibled - 1) * decoratedChildHeight);
                t /= (visibled - 1);
                startOffset = -decoratedChildHeight / 2;
                firstOffset = getVerticalSpace() / 2 + startOffset;
            }
            maxGap = t < 0?0:t;
        }else {
            maxGap = 0;
        }
    }


    private boolean isFitCenter(){
       return !(visibled % 2 == 0 || visibled == 1);
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler,
                                  RecyclerView.State state) {
        if (getChildCount() == 0) {
            return 0;
        }
        int scrolled = 0;
        final int left = getPaddingLeft();
        final int right = getWidth() - getPaddingRight();
        int totalGap = 0;
        if (dy < 0) {
            while (scrolled > dy) {
                final View topView = getChildAt(0);
                final int hangingTop = Math.max(-getDecoratedTop(topView), 0);
                final int scrollBy = Math.min(scrolled - dy, hangingTop);
                scrolled -= scrollBy;
                offsetChildrenVertical(scrollBy);
                if (firstVisiblePosition > 0 && scrolled > dy) {
                    firstVisiblePosition--;

                    View v = recycler.getViewForPosition(firstVisiblePosition);
                    if (v != null) {
                        addView(v, 0);
                        LayoutParams lp = (LayoutParams) v.getLayoutParams();
                        lp.line = firstVisiblePosition;
                        measureChildWithMargins(v, 0, 0);
                        totalGap += maxGap;
                        final int bottom = getDecoratedTop(topView) - totalGap;
                        final int top = bottom - getDecoratedMeasuredHeight(v);
                        layoutDecorated(v, left, top, right, bottom);
                    }
                } else {
                    break;
                }
            }
        } else if (dy > 0) {
            final int parentHeight = getHeight();
            while (scrolled < dy) {
                final View bottomView = getChildAt(getChildCount() - 1);
                final int hangingBottom =
                        Math.max(getDecoratedBottom(bottomView) - parentHeight, 0);
                final int scrollBy = -Math.min(dy - scrolled, hangingBottom);
                scrolled -= scrollBy;
                offsetChildrenVertical(scrollBy);

                if (scrolled < dy && state.getItemCount() > firstVisiblePosition + getChildCount()) {

                    View v = recycler.getViewForPosition(firstVisiblePosition + getChildCount());
                    if (v != null) {
                        totalGap += maxGap;
                        final int top = getDecoratedBottom(getChildAt(getChildCount() - 1)) + totalGap;
                        addView(v);
                        LayoutParams lp = (LayoutParams) v.getLayoutParams();
                        lp.line = firstVisiblePosition + getChildCount();
                        measureChildWithMargins(v, 0, 0);
                        final int bottom = top + getDecoratedMeasuredHeight(v);
                        layoutDecorated(v, left, top, right, bottom);
                    }
                } else {
                    break;
                }
            }

        }

        recycleViewsOutOfBounds(recycler);
        return scrolled;
    }

    private void recycleViewsOutOfBounds(RecyclerView.Recycler recycler) {
        final int childCount = getChildCount();
        final int parentWidth = getWidth();
        final int parentHeight = getHeight();
        boolean foundFirst = false;
        int first = 0;
        int last = 0;

        for (int i = 0; i < childCount; i++) {
            final View v = getChildAt(i);
            if (v.hasFocus() || (getDecoratedRight(v) >= 0 &&
                    getDecoratedLeft(v) <= parentWidth &&
                    getDecoratedBottom(v) >= 0 &&
                    getDecoratedTop(v) <= parentHeight + maxGap)) {

                if (!foundFirst) {
                    first = i;
                    foundFirst = true;
                }
                last = i;
            }
        }

        for (int i = childCount - 1; i > last; i--) {
            removeAndRecycleViewAt(i, recycler);
        }

        for (int i = first - 1; i >= 0; i--) {
            removeAndRecycleViewAt(i, recycler);
        }


        if (getChildCount() == 0) {
            firstVisiblePosition = 0;
        } else {
            firstVisiblePosition += first;
        }
    }

    /*
   * You must override this method if you would like to support external calls
   * to shift the view to a given adapter position. In our implementation, this
   * is the same as doing a fresh layout with the given position as the top-left
   * (or first visible), so we simply set that value and trigger onLayoutChildren()
   */
    @Override
    public void scrollToPosition(int position) {
        if (position >= getItemCount()) {
            Log.e(TAG, "Cannot scroll to "+position+", item count is "+getItemCount());
            return;
        }

        //Set requested position as first visible
        firstVisiblePosition = position;
        //Toss all existing views away
        removeAllViews();
        //Trigger a new view layout
        requestLayout();
    }

    /*
     * You must override this method if you would like to support external calls
     * to animate a change to a new adapter position. The framework provides a
     * helper scroller implementation (LinearSmoothScroller), which we leverage
     * to do the animation calculations.
     */
    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, final int position) {
        if (position >= getItemCount()) {
            Log.e(TAG, "Cannot scroll to "+position+", item count is "+getItemCount());
            return;
        }

        /*
         * LinearSmoothScroller's default behavior is to scroll the contents until
         * the child is fully visible. It will snap to the top-left or bottom-right
         * of the parent depending on whether the direction of travel was positive
         * or negative.
         */
        LinearSmoothScroller scroller = new LinearSmoothScroller(recyclerView.getContext()) {
            /*
             * LinearSmoothScroller, at a minimum, just need to know the vector
             * (x/y distance) to travel in order to get from the current positioning
             * to the target.
             */
            @Override
            public PointF computeScrollVectorForPosition(int targetPosition) {

                return PaddingLayoutManager.this.computeScrollVectorForPosition(targetPosition);
            }
        };
        scroller.setTargetPosition(position);
        startSmoothScroll(scroller);
    }

    public PointF computeScrollVectorForPosition(int targetPosition) {
        if (getChildCount() == 0) {
            return null;
        }
        if (orientation == HORIZONTAL) {
            return new PointF(1, 0);
        } else {
            return new PointF(0, 1);
        }
    }

    @Override
    public void onScrollStateChanged(int state) {
        super.onScrollStateChanged(state);
        if (state == RecyclerView.SCROLL_STATE_IDLE){
            if (getChildCount() > 0){
                if (!offsetScroller.isFinished()){
                    offsetScroller.abortAnimation();
                    removeCallbacks(this);
                }
                View fChild = getChildAt(0);
                View lChild = getChildAt(getChildCount() - 1);
                LayoutParams fLayoutParams = (LayoutParams) fChild.getLayoutParams();
                LayoutParams lLayoutParams = (LayoutParams) lChild.getLayoutParams();
                int startY = getDecoratedTop(fChild);
                int dy = 0;
                if (fLayoutParams.line == 0 && (startY == getPaddingTop() || startY == firstOffset)){
                    dy = firstOffset - startY;
                } else if (lLayoutParams.line == getItemCount() &&
                        (getDecoratedBottom(lChild) == getVerticalSpace()
                                || getDecoratedBottom(lChild) == getVerticalSpace() - firstOffset)){
                    dy = -firstOffset - getDecoratedBottom(lChild) + getVerticalSpace();
                } else {
                    dy = startOffset - startY;
                }
                offsetScroller.startScroll(0,startY,0,dy, computeScrollDuration(0,dy));
                postOnAnimation(this);
            }
        }
    }

    private int computeScrollDuration(int dx, int dy) {
        final int absDx = Math.abs(dx);
        final int absDy = Math.abs(dy);
        final boolean horizontal = absDx > absDy;
        final int delta = (int) Math.sqrt(dx * dx + dy * dy);
        final int containerSize = horizontal ? getWidth() : getHeight();
        final int halfContainerSize = containerSize / 2;
        final float distanceRatio = Math.min(1.f, 1.f * delta / containerSize);
        final float distance = halfContainerSize + halfContainerSize *
                distanceInfluenceForSnapDuration(distanceRatio);

        final int duration;
        float absDelta = (float) (horizontal ? absDx : absDy);
        duration = (int) (((absDelta / containerSize) + 1) * 300);
        return Math.min(duration, 2000);
    }


    private float distanceInfluenceForSnapDuration(float f) {
        f -= 0.5f; // center the values about 0.
        f *= 0.3f * Math.PI / 2.0f;
        return (float) Math.sin(f);
    }

    @Override
    public boolean canScrollVertically() {
        return orientation == VERTICAL;
    }

    @Override
    public boolean canScrollHorizontally() {
        return orientation == HORIZONTAL;
    }

    private int getLastVisiblePosition() {
        return getFirstVisiblePosition() + getVisibleChildCount();
    }

    private int getFirstVisiblePosition() {
        return firstVisiblePosition;
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

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(
                RecyclerView.LayoutParams.WRAP_CONTENT,
                RecyclerView.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public RecyclerView.LayoutParams generateLayoutParams(Context c, AttributeSet attrs) {
        return new LayoutParams(c, attrs);
    }

    @Override
    public RecyclerView.LayoutParams generateLayoutParams(ViewGroup.LayoutParams lp) {
        if (lp instanceof ViewGroup.MarginLayoutParams) {
            return new LayoutParams((ViewGroup.MarginLayoutParams) lp);
        } else {
            return new LayoutParams(lp);
        }
    }

    @Override
    public boolean checkLayoutParams(RecyclerView.LayoutParams lp) {
        return lp instanceof LayoutParams;
    }

    @Override
    public void run() {
        if (offsetScroller.computeScrollOffset()){
            View topView = getChildAt(0);
            int delta = offsetScroller.getCurrY() - getDecoratedTop(topView);
            offsetChildrenVertical(delta);
            View bottom = getChildAt(getChildCount() - 1);
            reAttach(bottom);
            if (!offsetScroller.isFinished()) {
                postOnAnimation(this);
            }
        }
    }

    private void reAttach(View v){
        detachView(v);
        removeDetachedView(v);
        eatRequestLayout();
        addView(v);
        layoutDecorated(v,getDecoratedLeft(v),getDecoratedTop(v)
        ,getDecoratedRight(v),getDecoratedBottom(v));
        resumeRequestLayout();
    }

    private void eatRequestLayout() {
        try {
            Method m = RecyclerView.class.getDeclaredMethod("eatRequestLayout");
            m.setAccessible(true);
            m.invoke(getRecyclerView());
        }catch (Exception e){
            Log.e(TAG,"eatRequestLayout failed :",e);
        }
    }


    private void resumeRequestLayout() {
        try {
            Method m = RecyclerView.class.getDeclaredMethod("resumeRequestLayout",boolean.class);
            m.setAccessible(true);
            m.invoke(getRecyclerView(),false);
        }catch (Exception e){
            Log.e(TAG,"resumeRequestLayout failed :",e);
        }
    }


    private RecyclerView getRecyclerView(){
        try {
            Field f = getClass().getSuperclass().getDeclaredField("mRecyclerView");
            f.setAccessible(true);
            return (RecyclerView) f.get(this);
        }catch (Exception e){
            throw new RuntimeException(e.fillInStackTrace());
        }
    }

    public static class LayoutParams extends RecyclerView.LayoutParams {

        int line;
        int type;
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

    private static final Interpolator sQuinticInterpolator = new Interpolator() {
        public float getInterpolation(float t) {
            t -= 1.0f;
            return t * t * t * t * t + 1.0f;
        }
    };
}
