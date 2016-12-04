package com.vivifram.second.recyclercardview;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.vivifram.second.recyclercardview_lib.layoutmanager.PaddingLayoutManager;

/**
 * 项目名称：RecyclerCardView
 * 类描述：
 * 创建人：zuowei
 * 创建时间：16-12-2 下午4:43
 * 修改人：zuowei
 * 修改时间：16-12-2 下午4:43
 * 修改备注：
 */
public class PaddingSampleActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_padding_sample);

        //
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        PaddingLayoutManager layoutManager = new PaddingLayoutManager(this);

        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setAdapter(new PaddingAdapter());
    }

    class PaddingAdapter extends RecyclerView.Adapter<PaddingHolder>{


        @Override
        public PaddingHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.padding_item,parent,false);
            return new PaddingHolder(v);
        }

        @Override
        public void onBindViewHolder(PaddingHolder holder, int position) {
            holder.setText(position+"");
        }

        @Override
        public int getItemCount() {
            return 100;
        }
    }

    class PaddingHolder extends RecyclerView.ViewHolder{
        TextView textView;
        public PaddingHolder(View itemView) {
            super(itemView);

            textView = (TextView) itemView.findViewById(R.id.tv);
        }

        public void setText(String text){
            textView.setText(text);
        }
    }
}
