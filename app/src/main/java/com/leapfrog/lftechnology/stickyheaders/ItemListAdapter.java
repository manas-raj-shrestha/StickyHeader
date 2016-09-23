package com.leapfrog.lftechnology.stickyheaders;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.droid.manasshrestha.stickyheaders.R;
import com.leapfrog.lftechnology.stickyheaders.itemmodels.Item;
import com.leapfrog.lftechnology.stickyheaders.sticky.StickyHeader;
import com.leapfrog.lftechnology.stickyheaders.sticky.StickyHeaderHandler;

import java.util.ArrayList;
import java.util.List;

public class ItemListAdapter extends RecyclerView.Adapter<ItemListAdapter.ViewHolder> implements StickyHeaderHandler {

    ArrayList<Item> dataList;
    Context context;

    public ItemListAdapter(MainActivity mainActivity, ArrayList<Item> dataList) {
        this.dataList = dataList;
        this.context = mainActivity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Item item = dataList.get(position);
        holder.tvItemName.setText(dataList.get(position).itemName + "\n" + dataList.get(position).itemDesc);

        if (item instanceof StickyHeader) {
            holder.itemView.setBackgroundColor(Color.parseColor("#ff669900"));
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    @Override
    public List<?> getAdapterData() {
        return dataList;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvItemName;

        public ViewHolder(View itemView) {
            super(itemView);

            tvItemName = (TextView) itemView.findViewById(R.id.tv_item_name);
        }
    }
}
