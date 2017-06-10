package com.mayankattri.aqua;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mayank on 4/6/17.
 */

public class Adapter extends RecyclerView.Adapter<Adapter.MyViewHolder> {

    private List<Item> itemList;
    private ArrayList<Trip> tripList;
    private Context context;
    private int id;

    public Adapter(Context context, int id, List<Item> itemList) {
        this.context = context;
        this.itemList = itemList;
        this.id = id;
    }

    public Adapter(Context context, int id, ArrayList<Trip> tripList) {
        this.context = context;
        this.tripList = tripList;
        this.id = id;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        // for item
        public TextView itemName, quantity, empty, date;
        public Button edit;
        // for trip
        public TextView name, vehicle, tripID, time;
        public ImageView pic;

        public MyViewHolder(View view) {
            super(view);
            if (id == 1) {
                context = view.getContext();
                itemName = (TextView) view.findViewById(R.id.TV_name);
                quantity = (TextView) view.findViewById(R.id.TV_quantity);
                empty = (TextView) view.findViewById(R.id.TV_empty);
                edit = (Button) view.findViewById(R.id.B_edit);

                edit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        StartTripActivity.position = getAdapterPosition();
                        Log.e("Item Position", "" + getLayoutPosition());
                        StartTripActivity.editItemFlag = 1;
                        StartTripActivity.ItemListDialogFragment d = new StartTripActivity.ItemListDialogFragment();
                        d.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                            }
                        });
                        d.show(((AppCompatActivity) context).getFragmentManager(), "ItemListDialogFragment");
                    }
                });
            } else if (id == 2) {
                context = view.getContext();
                name = (TextView) view.findViewById(R.id.TV_name);
                vehicle = (TextView) view.findViewById(R.id.TV_vehicle);
                tripID = (TextView) view.findViewById(R.id.TV_tripID);
                time = (TextView) view.findViewById(R.id.TV_time);
                pic = (ImageView) view.findViewById(R.id.IV_pic);

                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.e("id = 2", "Inside OnClick()");
                        Trip item = tripList.get(getAdapterPosition());
                        Intent intent = new Intent(context, ReceiveItemsActivity.class);
                        intent.putExtra("TRIP_ITEM", item);
                        ((Activity) context).startActivityForResult(intent, 1);
//                        context.startActivity(intent);
                    }
                });
            } else if (id == 3) {
                context = view.getContext();
                name = (TextView) view.findViewById(R.id.TV_name);
                vehicle = (TextView) view.findViewById(R.id.TV_vehicle);
                tripID = (TextView) view.findViewById(R.id.TV_tripID);
                time = (TextView) view.findViewById(R.id.TV_time);
                pic = (ImageView) view.findViewById(R.id.IV_pic);

                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.e("Adapter", "Inside OnClick()");
                        Trip item = tripList.get(getAdapterPosition());
                        Intent intent = new Intent(context, LogOngoingActivity.class);
                        intent.putExtra("ACTIVE_TRIP_ITEM", item);
                        context.startActivity(intent);
                    }
                });
            } else if( id == 4) {
                context = view.getContext();
                itemName = (TextView) view.findViewById(R.id.TV_name);
                quantity = (TextView) view.findViewById(R.id.TV_quantity);
                empty = (TextView) view.findViewById(R.id.TV_empty);
            } else if (id == 5) {
                context = view.getContext();
                name = (TextView) view.findViewById(R.id.TV_name);
                vehicle = (TextView) view.findViewById(R.id.TV_vehicle);
                date = (TextView) view.findViewById(R.id.TV_date);
                time = (TextView) view.findViewById(R.id.TV_time);
                pic = (ImageView) view.findViewById(R.id.IV_pic);

                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.e("Adapter", "Inside OnClick()");
                        Trip item = tripList.get(getAdapterPosition());
                        Intent intent = new Intent(context, LogCompletedActivity.class);
                        intent.putExtra("COMPLETED_TRIP_ITEM", item);
                        context.startActivity(intent);
                    }
                });
            }
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = null;
        if (id == 1) {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.element_item, parent, false);
        } else if (id == 2) {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.element_trip, parent, false);
        } else if (id == 3) {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.element_trip, parent, false);
        } else if (id == 4) {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.element_log_predel_item, parent, false);
        } else if (id == 5) {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.element_item_postdelivery, parent, false);
        }

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        if (id == 1) {
            Item item = itemList.get(position);
            holder.itemName.setText(item.getName());
            holder.quantity.setText(item.getQuantity());
            if (item.isEmpty()) holder.empty.setText("Empty");
        } else if (id == 2 || id == 3) {
            Trip trip = tripList.get(position);
            holder.name.setText(trip.getName());
            holder.vehicle.setText(trip.getVehicle());
            holder.tripID.setText(trip.getId());
            holder.pic.setImageResource(R.drawable.kit);
            holder.time.setText(trip.getTime());
        } else if (id == 4) {
            Item item = itemList.get(position);
            holder.itemName.setText(item.getName());
            holder.quantity.setText(item.getQuantity());
            if (item.isEmpty()) holder.empty.setText("Empty");
        } else if (id == 5) {
            Trip trip = tripList.get(position);
            holder.name.setText(trip.getName());
            holder.vehicle.setText(trip.getVehicle());
            holder.date.setText(trip.getDate());
            holder.pic.setImageResource(R.drawable.kit);
            holder.time.setText(trip.getTime());
        }
    }

    @Override
    public int getItemCount() {
        if (id == 1 || id == 4) return itemList.size();
        else if (id == 2 || id == 3 || id == 5) return tripList.size();
        else return 0;
    }
}
