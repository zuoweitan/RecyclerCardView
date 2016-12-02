package com.vivifram.second.recyclercardview_lib;

import android.content.Context;
import android.graphics.PointF;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by zuowei on 16-11-27.
 */

public class RecyclerCardView extends RecyclerView implements ChildProximityListener{

    private List<OnScrollListener> scrollListeners;
    private OnScrollListener scrollListener;
    private OnFlingListener onFlingListener;
    private SnapHelper snapHelper;
    private int centerPos;

    public RecyclerCardView(Context context) {
        this(context,null);
    }

    public RecyclerCardView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecyclerCardView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        super.setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (scrollListeners != null) {
                    for (OnScrollListener listener : scrollListeners) {
                        listener.onScrollStateChanged(recyclerView,newState);
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (scrollListeners != null) {
                    for (OnScrollListener listener : scrollListeners) {
                        listener.onScrolled(recyclerView,dx,dy);
                    }
                }
            }
        });
    }

    public void setOnFlingListener(@Nullable OnFlingListener onFlingListener) {
        this.onFlingListener = onFlingListener;
    }

    @Nullable
    public OnFlingListener getOnFlingListener() {
        return onFlingListener;
    }

    public static abstract class OnFlingListener {

        public abstract boolean onFling(int velocityX, int velocityY);
    }

    public interface OnCenterProximityListener {
        void onCenterPosition(boolean animate);

        void onNonCenterPosition(boolean animate);
    }

    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        boolean b = super.dispatchNestedFling(velocityX, velocityY, consumed);

        if (onFlingListener != null && onFlingListener.onFling((int)velocityX, (int)velocityY)) {
            b =  true;
        }

        return b;
    }

    void setSnapHelper(SnapHelper snapHelper) {
        this.snapHelper = snapHelper;
    }

    public final void addOnScrollListener(OnScrollListener listener) {
        if (scrollListeners == null) {
            scrollListeners = new CopyOnWriteArrayList<>();
        }
        scrollListeners.add(listener);
    }

    public final void removeOnScrollListener(OnScrollListener listener) {
        if (scrollListeners != null) {
            scrollListeners.remove(listener);
        }
    }

    public final void clearOnScrollListeners() {
        if (scrollListeners != null) {
            scrollListeners.clear();
        }
    }

    //support-v7-22
    @Override
    public void setOnScrollListener(OnScrollListener listener) {
        if (scrollListener != null){
            removeOnScrollListener(scrollListener);
        }
        scrollListener = listener;
        addOnScrollListener(listener);
    }

    @Override
    public void notifyChildrenAboutProximity(boolean animate) {
        int count = getChildCount();
        View centerView = snapHelper.findSnapView(getLayoutManager());
        if (centerView != null){

            for (int i = 0; i < count; i++) {
                View v = getChildAt(i);
                CardHolder cardHolder = (CardHolder) getChildViewHolder(v);
                cardHolder.onCenterProximity(centerView == v,animate);
            }

            int nCenPos = getLayoutManager().getPosition(centerView);
            if (centerPos != nCenPos){
                centerPos = nCenPos;
            }
        }
    }

    public int getCenterPosition() {
        return centerPos;
    }

    @Override
    public void setLayoutManager(LayoutManager layout) {
        if (layout instanceof CardLinearLayoutManager){
            ((CardLinearLayoutManager) layout).setChildProximityListener(this);
        }
        super.setLayoutManager(layout);
    }

    public interface ScrollVectorProvider {

        PointF computeScrollVectorForPosition(int targetPosition);
    }

    public static abstract class CardHolder<T> extends RecyclerView.ViewHolder{

        public CardHolder(ViewGroup root, int layoutRes) {
            super(LayoutInflater.from(root.getContext()).inflate(layoutRes, root, false));
        }

        public abstract void bindData(T data);

        protected void onCenterProximity(boolean isCentralItem, boolean animate) {
            if(this instanceof RecyclerCardView.OnCenterProximityListener) {
                RecyclerCardView.OnCenterProximityListener item = (RecyclerCardView.OnCenterProximityListener)this;
                if(isCentralItem) {
                    item.onCenterPosition(animate);
                } else {
                    item.onNonCenterPosition(animate);
                }

            }
        }
    }
}
