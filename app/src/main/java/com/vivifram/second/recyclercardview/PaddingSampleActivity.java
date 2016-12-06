package com.vivifram.second.recyclercardview;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

import com.vivifram.second.recyclercardview_lib.layoutmanager.PaddingLayoutManager;

import java.util.ArrayList;
import java.util.List;

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

    private List<CardItem> list = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_padding_sample);

        //init
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        int visibled = 3;

        PaddingLayoutManager layoutManager = new PaddingLayoutManager(this,visibled);

        recyclerView.setLayoutManager(layoutManager);

        for (int i = 0; i < 10; i++) {
            list.add(new CardItem().setResId(R.drawable.lufei_2_1));
            list.add(new CardItem().setResId(R.drawable.lufei_2));
            list.add(new CardItem().setResId(R.drawable.lufei_2_3));
        }

        PaddingSampleAdapter paddingSampleAdapter = new PaddingSampleAdapter().setList(list);

        paddingSampleAdapter.setAnimate(visibled % 2 != 0);

        recyclerView.setAdapter(paddingSampleAdapter);
    }
}
