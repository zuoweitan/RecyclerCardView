package com.vivifram.second.recyclercardview_lib;

import android.support.v7.widget.RecyclerView;
import android.view.View;

interface FakeViewCreator {
    View create(RecyclerView.Recycler recycler);
}