package com.mayankattri.aqua;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

/**
 * Created by mayank on 7/6/17.
 */

public class AdapterReceiveItem extends RecyclerView.Adapter<AdapterReceiveItem.MyViewHolder> {

private List<Item> itemList;
private Context context;

public AdapterReceiveItem(List<Item> itemList) {
        this.itemList = itemList;
        }

public class MyViewHolder extends RecyclerView.ViewHolder {
    public TextView name, quantity, empty;
    public Button edit;

    public MyViewHolder(View view) {
        super(view);
        context = view.getContext();
        name = (TextView) view.findViewById(R.id.TV_name);
        quantity = (TextView) view.findViewById(R.id.TV_quantity);
        empty = (TextView) view.findViewById(R.id.TV_empty);
        edit = (Button) view.findViewById(R.id.B_edit);

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReceiveItemsActivity.position = getAdapterPosition();
                Log.e("Item Position", ""+getLayoutPosition());
                ReceiveItemsActivity.editItemFlag = 1;
                ReceiveItemsActivity.GetItemListDialogFragment d = new ReceiveItemsActivity.GetItemListDialogFragment();
                d.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                    }
                });
                d.show(((AppCompatActivity)context).getFragmentManager(), "GetItemListDialogFragment");
            }
        });
    }
}

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.element_item_given, parent, false);

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