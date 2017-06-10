package com.mayankattri.aqua;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by mayank on 5/6/17.
 */

public class AdapterGiveItem extends RecyclerView.Adapter<AdapterGiveItem.MyViewHolder> {

    private List<Item> itemList;
    private Context context;
    private int ID;

    public AdapterGiveItem(int ID, List<Item> itemList) {
        this.itemList = itemList;
        this.ID = ID;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView name, quantity, empty;

        public MyViewHolder(View view) {
            super(view);
            context = view.getContext();
            name = (TextView) view.findViewById(R.id.TV_name);
            quantity = (TextView) view.findViewById(R.id.TV_quantity);
            empty = (TextView) view.findViewById(R.id.TV_empty);
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = null;
        if (ID == 1 || ID == 2) {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.element_log_predel_item, parent, false);
        } else if (ID == 3) {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.element_logpostdel_item, parent, false);
        }

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Item item = itemList.get(position);
        holder.name.setText(item.getName());
        holder.quantity.setText(item.getQuantity());
        if (item.isEmpty()) holder.empty.setText("Empty");
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }
}