package com.vivifram.second.recyclercardview_lib;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.lang.reflect.Field;

/**
 * 项目名称：RecyclerCardView
 * 类描述：
 * 创建人：zuowei
 * 创建时间：16-11-30 上午10:53
 * 修改人：zuowei
 * 修改时间：16-11-30 上午10:53
 * 修改备注：
 */
public abstract class CardAdapter<T> extends RecyclerView.Adapter<RecyclerCardView.CardHolder<T>> implements FakeViewCreator{


    RecyclerView recyclerView;
    CardLinearLayoutManager cardLinearLayoutManager;
    int visibleCount;
    int minNap;

    public CardAdapter(RecyclerView recyclerView,int visibleCount,int minNap){
        this.minNap = minNap;
        this.recyclerView = recyclerView;
        if (recyclerView.getLayoutManager() instanceof CardLinearLayoutManager){
            this.cardLinearLayoutManager = (CardLinearLayoutManager) recyclerView.getLayoutManager();
            this.cardLinearLayoutManager.setFakeViewCreator(this);
        }else {
            throw new RuntimeException("layoutmanager is not CardLinearLayoutManager");
        }
        this.visibleCount = visibleCount;
        if (visibleCount == 0){
            throw new RuntimeException("visibleCount can not be zero");
        }
    }

    public CardAdapter(RecyclerView recyclerView,int visibleCount){
        this(recyclerView,visibleCount,0);
    }

    @Override
    public RecyclerCardView.CardHolder<T> onCreateViewHolder(ViewGroup parent, int viewType) {
        return createCardHolder(parent,viewType);
    }

    protected abstract RecyclerCardView.CardHolder<T> createCardHolder(ViewGroup parent, int viewType);

    @Override
    public void onBindViewHolder(RecyclerCardView.CardHolder<T> holder, int position) {
        if (position >= 0 && position < getItemCount()){
            int orientation = cardLinearLayoutManager.getOrientation();
            View itemView = holder.itemView;
            if (orientation == RecyclerCardView.VERTICAL) {
                int parentHeight = cardLinearLayoutManager.getVerticalSpace();
                int childHeight = cardLinearLayoutManager.getDecoratedChildHeight();
                int vDiff = (parentHeight - childHeight) / 2;
                int nap;
                if (visibleCount > 1) {
                    int times = visibleCount - 1;
                    if (visibleCount == 2) {
                        times = visibleCount;
                    }
                    nap = (parentHeight - (visibleCount - 1) * childHeight) / times;
                } else {
                    nap = vDiff;
                }
                nap = nap < 0 ? minNap : nap;
                int tP = nap / 2;
                int bp = nap / 2;

                if (position == 0) {
                    tP = vDiff;
                } else if (position == getItemCount() - 1) {
                    bp = vDiff;
                }

                itemView.setPadding(0, tP, 0, bp);
            }else {
                int parentWidth = cardLinearLayoutManager.getHorizontalSpace();
                int childWidth = cardLinearLayoutManager.getDecorateChildWidth();
                int hDiff = (parentWidth - childWidth) / 2;
                int nap;
                if (visibleCount > 1) {
                    int times = visibleCount - 1;
                    if (visibleCount == 2) {
                        times = visibleCount;
                    }
                    nap = (parentWidth - (visibleCount - 1) * childWidth) / times;
                } else {
                    nap = hDiff;
                }
                nap = nap <= 0 ? minNap : nap;
                int lP = nap / 2;
                int rp = nap / 2;

                if (position == 0) {
                    lP = hDiff;
                } else if (position == getItemCount() - 1) {
                    rp = hDiff;
                }

                itemView.setPadding(lP, 0, rp, 0);
            }

            holder.bindData(getItem(position));
        }else if (position == -1){
            holder.bindData(getItem(0));//init fakeview
        }
    }

    protected abstract T getItem(int position);

    @Override
    public View create(RecyclerView.Recycler recycler) {
        /*View viewForPosition = recycler.getViewForPosition(0);
        if (viewForPosition != null){
            viewForPosition.setPadding(0,0,0,0);
        }*/
        RecyclerCardView.CardHolder cardHolder = onCreateViewHolder(recyclerView,-1);
        onBindViewHolder(cardHolder,-1);
        Class<? extends ViewGroup.LayoutParams> aClass = cardHolder.itemView.getLayoutParams().getClass();
        try {
            Field mViewHolder = aClass.getDeclaredField("mViewHolder");
            mViewHolder.setAccessible(true);
            mViewHolder.set(cardHolder.itemView.getLayoutParams(),cardHolder);
        } catch (Exception e) {
        }

        return cardHolder.itemView;
    }
}
