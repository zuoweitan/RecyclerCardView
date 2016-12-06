package com.vivifram.second.recyclercardview;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.vivifram.second.recyclercardview_lib.OnCenterProximityListener;
import com.vivifram.second.recyclercardview_lib.RecyclerCardView;

import java.util.ArrayList;
import java.util.List;

/**
 * 项目名称：RecyclerCardView
 * 类描述：
 * 创建人：zuowei
 * 创建时间：16-12-6 下午4:53
 * 修改人：zuowei
 * 修改时间：16-12-6 下午4:53
 * 修改备注：
 */
public class PaddingSampleAdapter extends RecyclerView.Adapter<PaddingSampleAdapter.SimpleCardHolder>{

    private List<CardItem> list = new ArrayList<>();
    private boolean animateEnabled;

    @Override
    public SimpleCardHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SimpleCardHolder(parent,R.layout.padding_item);
    }

    @Override
    public void onBindViewHolder(SimpleCardHolder holder, int position) {
        holder.bindData(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public PaddingSampleAdapter setList(List<CardItem> list) {
        this.list = list;
        return this;
    }

    public void setAnimate(boolean b) {
        animateEnabled = b;
    }

    class SimpleCardHolder extends RecyclerCardView.ViewHolder implements OnCenterProximityListener {

        private TextView tv;
        public SimpleCardHolder(ViewGroup root, int layoutRes) {
            super(LayoutInflater.from(root.getContext()).inflate(layoutRes,root,false));
            tv = (TextView) itemView.findViewById(R.id.tv);
        }

        public void bindData(CardItem data) {
            tv.setText(getAdapterPosition()+"");
        }

        @Override
        public void onCenterPosition(boolean animate) {
            if (animateEnabled) {
                itemView.animate().scaleX(1.2f);
                itemView.animate().scaleY(1.2f);
                itemView.setZ(1f);
            }
        }

        @Override
        public void onNonCenterPosition(boolean animate) {
            if (animateEnabled) {
                itemView.animate().scaleX(1.0f);
                itemView.animate().scaleY(1.0f);
                itemView.setZ(0.9f);
            }
        }
    }
}
