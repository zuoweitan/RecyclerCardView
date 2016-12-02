package com.vivifram.second.recyclercardview;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.vivifram.second.recyclercardview_lib.RecyclerCardView;

import java.util.ArrayList;
import java.util.List;

import static com.vivifram.second.recyclercardview.R.id.vImg;


/**
 * 项目名称：RecyclerCardView
 * 类描述：
 * 创建人：zuowei
 * 创建时间：16-11-30 上午11:53
 * 修改人：zuowei
 * 修改时间：16-11-30 上午11:53
 * 修改备注：
 */
public class SampleCardAdapter extends com.vivifram.second.recyclercardview_lib.CardAdapter<CardItem>{

    //for test

    public boolean isVImg = true;

    public SampleCardAdapter setvImg(boolean isVImg) {
        this.isVImg = isVImg;
        return this;
    }

    private List<CardItem> list = new ArrayList<>();

    public SampleCardAdapter(RecyclerView recyclerView, int visibleCount) {
        this(recyclerView, visibleCount,0);
    }

    public SampleCardAdapter(RecyclerView recyclerView, int visibleCount,int minNap) {
        super(recyclerView, visibleCount,minNap);
    }

    public SampleCardAdapter setList(List<CardItem> list) {
        this.list = list;
        return this;
    }

    @Override
    protected RecyclerCardView.CardHolder<CardItem> createCardHolder(ViewGroup parent, int viewType) {
        return new SimpleCardHolder(parent,R.layout.view_card_item);
    }

    @Override
    protected CardItem getItem(int position) {
        return list.get(position);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class SimpleCardHolder extends RecyclerCardView.CardHolder<CardItem> implements RecyclerCardView.OnCenterProximityListener{

        public ImageView vImg;
        public ImageView hImg;
        public SimpleCardHolder(ViewGroup root, int layoutRes) {
            super(root, layoutRes);
            vImg = (ImageView) itemView.findViewById(R.id.vImg);
            hImg = (ImageView) itemView.findViewById(R.id.hImg);
            if (isVImg){
                vImg.setVisibility(View.VISIBLE);
                hImg.setVisibility(View.GONE);
            }else {
                vImg.setVisibility(View.GONE);
                hImg.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void bindData(CardItem data) {
            if (isVImg) {
                vImg.setImageResource(data.resId);
            }else {
                hImg.setImageResource(data.resId);
            }
        }

        @Override
        public void onCenterPosition(boolean animate) {
            itemView.animate().scaleX(1.2f);
            itemView.animate().scaleY(1.2f);
            itemView.setZ(1f);
        }

        @Override
        public void onNonCenterPosition(boolean animate) {
            itemView.animate().scaleX(1.0f);
            itemView.animate().scaleY(1.0f);
            itemView.setZ(0.9f);
        }
    }

}
