package com.vivifram.second.recyclercardview;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

import com.vivifram.second.recyclercardview_lib.CardLinearLayoutManager;
import com.vivifram.second.recyclercardview_lib.LinearSnapHelper;
import com.vivifram.second.recyclercardview_lib.RecyclerCardView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private RecyclerCardView recyclerView;
    private List<CardItem> list = new ArrayList<>();
    private LinearSnapHelper cardSnapHelper = null;
    private int minNap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        for (int i = 0; i < 10; i++) {
            list.add(new CardItem().setResId(R.drawable.lufei_2_1));
            list.add(new CardItem().setResId(R.drawable.lufei_2));
            list.add(new CardItem().setResId(R.drawable.lufei_2_3));
        }
        boolean vImg = false;//true to vertical demo

        recyclerView = (RecyclerCardView) findViewById(R.id.recyclerView);
        final CardLinearLayoutManager linearLayoutManager = new CardLinearLayoutManager(this,
                vImg? RecyclerCardView.VERTICAL : RecyclerView.HORIZONTAL,false);
        recyclerView.setLayoutManager(linearLayoutManager);

        SampleCardAdapter sampleCardAdapter = new SampleCardAdapter(recyclerView,3,vImg ? 0:60)
                .setvImg(vImg)
                .setList(list);
        recyclerView.setAdapter(sampleCardAdapter);

        cardSnapHelper = new LinearSnapHelper();
        cardSnapHelper.attachToRecyclerView(recyclerView);

    }

}
