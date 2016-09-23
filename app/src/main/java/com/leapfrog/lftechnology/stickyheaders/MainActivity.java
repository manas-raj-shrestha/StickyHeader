package com.leapfrog.lftechnology.stickyheaders;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;

import com.droid.manasshrestha.stickyheaders.R;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        ItemListAdapter itemListAdapter = new ItemListAdapter(this, DataProvider.getInstance().getDataList());
        recyclerView.setAdapter(itemListAdapter);
        recyclerView.setLayoutManager(new StickyHeaderLayoutManager(this));
    }
}
