package com.mayankattri.aqua;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionMenu;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.HttpsURLConnection;

public class StartTripActivity extends AppCompatActivity {

    public static ArrayList<JSONObject> storeItemListJson;
    public static ArrayList<String> storeItemListString;
    public static ArrayList<String> typeList;
    public static ArrayList<Item> addedItemList;

    public HashMap<String,Integer> vehicleList;
    public HashMap<String,Integer> nameList;
    public HashMap<String,Integer> storeList;
    public static HashMap<String,Integer> itemList;

    int code;
    static final int AdapterID = 1;
    static int addItemFlag;
    static int editItemFlag;
    static int position;

    private FloatingActionMenu FAB_menu;
    private com.github.clans.fab.FloatingActionButton FAB_item;
    private com.github.clans.fab.FloatingActionButton FAB_item_empty;
    public FrameLayout RL_items;
    public AutoCompleteTextView S_vehicle, S_name, S_type;
    public static Button B_predelivery;
    public EditText ET_meter;
    private RecyclerView recyclerView;

    public static Adapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.activity_start_trip);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        vehicleList = new HashMap<>();
        nameList = new HashMap<>();
        itemList = new HashMap<>();
        storeList = new HashMap<>();
        storeItemListJson = new ArrayList<>();
        storeItemListString = new ArrayList<>();
        addedItemList = new ArrayList<>();
        typeList = new ArrayList<>();
        typeList.add("Sales_Person");
        typeList.add("Feeder");

        ET_meter = (EditText) findViewById(R.id.ET_meter);
        S_vehicle = (AutoCompleteTextView) findViewById(R.id.S_vehicle);
        S_name = (AutoCompleteTextView) findViewById(R.id.S_name);
        S_type = (AutoCompleteTextView) findViewById(R.id.S_type);
        B_predelivery = (Button) findViewById(R.id.B_predelivery);
        B_predelivery.setEnabled(false);

        Intent intent = getIntent();
        String userDetailsJson = intent.getStringExtra("USER_DETAILS2");

        parseData(userDetailsJson);

        ArrayAdapter<String> vehicleAdapter = new ArrayAdapter<>
                (this, android.R.layout.select_dialog_item, new ArrayList<>(vehicleList.keySet()));
        S_vehicle.setThreshold(0);
        S_vehicle.setAdapter(vehicleAdapter);

        ArrayAdapter<String> nameAdapter = new ArrayAdapter<>
                (this, android.R.layout.select_dialog_item, new ArrayList<>(nameList.keySet()));
        S_name.setThreshold(0);
        S_name.setAdapter(nameAdapter);

        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>
                (this, android.R.layout.select_dialog_item, typeList);
        S_type.setThreshold(0);
        S_type.setAdapter(typeAdapter);

        S_type.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event){
                S_type.showDropDown();
                return false;
            }
        });

        B_predelivery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s = "item";
                int vehicle_id = vehicleList.get(S_vehicle.getText().toString());
                int employee_id = nameList.get(S_name.getText().toString());
                int store_id = storeList.get("Saket");

                JSONObject listObject = new JSONObject();
                for (int i=1; i<=storeItemListJson.size(); i++) {
                    try {
                        listObject.put(s+i, storeItemListJson.get(i-1));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                JSONObject data = new JSONObject();
                try {
                    data.put("store_id", store_id);
                    data.put("vehicle_id", vehicle_id);
                    data.put("employee_id", employee_id);
                    data.put("delivery_to", S_type.getText().toString());
                    data.put("meter_reading", ET_meter.getText().toString());
                    data.put("active", "True");
                    data.put("store_item_list", listObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                final JSONObject mainObject = new JSONObject();
                try {
                    mainObject.put("username", LoginActivity.un);
                    mainObject.put("password", LoginActivity.pswrd);
                    mainObject.put("data", data);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Log.e("TAG", mainObject.toString());

                if (isNetworkConnected()) {
                    final ProgressDialog dialog = new ProgressDialog(StartTripActivity.this);
                    dialog.setMessage("Saving");
                    dialog.show();

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                new GiveItemsTask().execute(mainObject.toString()).get();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            }
                            dialog.dismiss();
                            Intent returnIntent = new Intent();
                            setResult(RESULT_CANCELED, returnIntent);
                            finish();
                        }
                    }, 1000);

                } else {
                    final ProgressDialog dialog = new ProgressDialog(StartTripActivity.this);
                    dialog.setMessage("No Internet");
                    dialog.show();

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            dialog.dismiss();
                        }
                    }, 2000);
                }

            }
        });

        FAB_menu = (FloatingActionMenu) findViewById(R.id.FAB_menu);

        FAB_item = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.FAB_item);
        FAB_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addItemFlag = 0;
                ItemListDialogFragment d = new ItemListDialogFragment();
                d.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                    }
                });
                d.show(getFragmentManager(), "ItemListDialogFragment");
            }
        });

        FAB_item_empty = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.FAB_item_empty);
        FAB_item_empty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addItemFlag = 1;
                ItemListDialogFragment d = new ItemListDialogFragment();
                d.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                    }
                });
                d.show(getFragmentManager(), "ItemListDialogFragment");
            }
        });

        RL_items = (FrameLayout) findViewById(R.id.RL_items);
        RL_items.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(FAB_menu.isOpened())
                    FAB_menu.close(true);
            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(FAB_menu.isOpened())
                    FAB_menu.close(true);
                return false;
            }
        });
        mAdapter = new Adapter(this, AdapterID, addedItemList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();    //Call the back button's method
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Intent returnIntent = new Intent();
        setResult(RESULT_CANCELED, returnIntent);
        finish();
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    public void parseData(String s) {
        String[] arr = s.split("!");

        try {
            JSONArray array = new JSONArray(arr[0]);
            for (int i = 0; i < array.length(); i++) {
                String key = array.getJSONObject(i).getJSONObject("fields").getString("vehicle_id");
                int value = array.getJSONObject(i).getInt("pk");
                vehicleList.put(key, value);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            JSONArray array = new JSONArray(arr[1]);
            for (int i = 0; i < array.length(); i++) {
                String key = array.getJSONObject(i).getJSONObject("fields").getString("employee_name");
                int value = array.getJSONObject(i).getInt("pk");
                nameList.put(key, value);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            JSONArray array = new JSONArray(arr[2]);
            for (int i = 0; i < array.length(); i++) {
                String key = array.getJSONObject(i).getJSONObject("fields").getString("item_name");
                int value = array.getJSONObject(i).getInt("pk");
                itemList.put(key, value);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            JSONArray array = new JSONArray(arr[3]);
            for (int i = 0; i < array.length(); i++) {
                String key = array.getJSONObject(i).getJSONObject("fields").getString("store_id");
                int value = array.getJSONObject(i).getInt("pk");
                storeList.put(key, value);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private class GiveItemsTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL("http://52.66.136.236/api/predelivery/save/");

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(params[0]);

                writer.flush();
                writer.close();
                os.close();

                int responseCode=conn.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuffer sb = new StringBuffer("");
                    String line = "";

                    while ((line = in.readLine()) != null) {
                        sb.append(line);
                        break;
                    }

                    in.close();
                    return sb.toString();

                }
                else {
                    return "false : " + responseCode;
                }
            }
            catch(Exception e){
                return "Exception: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.e("TAG", result);
            try {
                JSONObject obj = new JSONObject(result);
                String value = obj.getString("code");
                if (Integer.parseInt(value) == 1) {
                    code = 1;
                    Toast.makeText(StartTripActivity.this, "Saved Successfully", Toast.LENGTH_LONG).show();
                } else if (value.equals("-999")) {
                    code = -999;
                    Toast.makeText(StartTripActivity.this, "Duplicate Data" +
                            "", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(StartTripActivity.this, "Wrong Credentials", Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public static class ItemListDialogFragment extends DialogFragment {

        AutoCompleteTextView S_item;
        EditText ET_quantity, ET_empty, ET_uniqueID;

        public ItemListDialogFragment() {
        }

        public void addItem() {
            int item = itemList.get(S_item.getText().toString());
            String quantity, empty;

            if (ET_quantity.getText().toString().equals(""))
                quantity = "0";
            else
                quantity = ET_quantity.getText().toString();

            if (ET_empty.getText().toString().equals(""))
                empty = "0";
            else
                empty = ET_empty.getText().toString();

            JSONObject itemObject = new JSONObject();
            try {
                itemObject.put("item_id", item);
                itemObject.put("quantity", quantity);

                if (empty.equals("0"))
                    itemObject.put("empty", "False");
                else
                    itemObject.put("empty", "True");

                itemObject.put("empty_quantity", empty);
                storeItemListJson.add(itemObject);
                if (empty.equals("0") || empty.equals("")) {
                    storeItemListString.add(S_item.getText().toString() + " : " + quantity);
                    addedItemList.add(new Item(S_item.getText().toString(), quantity, false));
                }
                else {
                    storeItemListString.add(S_item.getText().toString() + " : " + empty + " : Empty");
                    addedItemList.add(new Item(S_item.getText().toString(), empty, true));
                }
                mAdapter.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            System.out.println(itemObject.toString());
        }

        private DialogInterface.OnDismissListener onDismissListener;

        public void setOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
            this.onDismissListener = onDismissListener;
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            super.onDismiss(dialog);
            if (onDismissListener != null) {
                onDismissListener.onDismiss(dialog);
            }
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            if (editItemFlag == 1) {
                builder.setTitle("Edit Item");

                LayoutInflater inflater = getActivity().getLayoutInflater();
                View view = inflater.inflate(R.layout.dialog_fragment_item_list, null);

                TextView TV_empty = (TextView) view.findViewById(R.id.TV_empty);
                final TextView TV_uniqueID = (TextView) view.findViewById(R.id.TV_uniqueID);
                S_item = (AutoCompleteTextView) view.findViewById(R.id.S_items);
                ET_quantity = (EditText) view.findViewById(R.id.ET_quantity);
                ET_empty = (EditText) view.findViewById(R.id.ET_empty);
                ET_uniqueID = (EditText) view.findViewById(R.id.ET_uniqueID);

                ET_empty.setVisibility(View.GONE);
                ET_uniqueID.setVisibility(View.GONE);
                TV_empty.setVisibility(View.GONE);
                TV_uniqueID.setVisibility(View.GONE);

                ArrayAdapter<String> itemAdapter = new ArrayAdapter<>
                        (getActivity(), android.R.layout.select_dialog_item, new ArrayList<>(itemList.keySet()));
                S_item.setThreshold(0);
                S_item.setAdapter(itemAdapter);

                S_item.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                        if (S_item.getText().toString().contains("Ladoo")) {
                            TV_uniqueID.setVisibility(View.VISIBLE);
                            ET_uniqueID.setVisibility(View.VISIBLE);
                        } else {
                            TV_uniqueID.setVisibility(View.GONE);
                            ET_uniqueID.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parentView) {
                        // your code here
                    }

                });

                builder.setView(view);
                builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
//                        addItem();
                        Item temp = new Item(S_item.getText().toString(), ET_quantity.getText().toString(), false);
                        addedItemList.set(position, temp);
                        mAdapter.notifyDataSetChanged();
                        editItemFlag = 0;
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ItemListDialogFragment.this.getDialog().cancel();
                        editItemFlag = 0;
                    }
                });

                return builder.create();
            }
            else if (StartTripActivity.addItemFlag == 0) {
                builder.setTitle("Add Item");

                LayoutInflater inflater = getActivity().getLayoutInflater();
                View view = inflater.inflate(R.layout.dialog_fragment_item_list, null);

                TextView TV_empty = (TextView) view.findViewById(R.id.TV_empty);
                final TextView TV_uniqueID = (TextView) view.findViewById(R.id.TV_uniqueID);
                S_item = (AutoCompleteTextView) view.findViewById(R.id.S_items);
                ET_quantity = (EditText) view.findViewById(R.id.ET_quantity);
                ET_empty = (EditText) view.findViewById(R.id.ET_empty);
                ET_uniqueID = (EditText) view.findViewById(R.id.ET_uniqueID);

                ET_empty.setVisibility(View.GONE);
                ET_uniqueID.setVisibility(View.GONE);
                TV_empty.setVisibility(View.GONE);
                TV_uniqueID.setVisibility(View.GONE);

                ArrayAdapter<String> vehicleAdapter = new ArrayAdapter<>
                        (getActivity(), android.R.layout.select_dialog_item, new ArrayList<>(itemList.keySet()));
                S_item.setThreshold(0);
                S_item.setAdapter(vehicleAdapter);

                S_item.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                        if (S_item.getText().toString().contains("Ladoo")) {
                            TV_uniqueID.setVisibility(View.VISIBLE);
                            ET_uniqueID.setVisibility(View.VISIBLE);
                        } else {
                            TV_uniqueID.setVisibility(View.GONE);
                            ET_uniqueID.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parentView) {
                        // your code here
                    }

                });

                builder.setView(view);
                builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Log.e("Predelivery", "Button should enabled");
                        B_predelivery.setEnabled(true);
                        B_predelivery.setBackgroundColor(Color.parseColor("#FF1C3380"));
                        addItem();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ItemListDialogFragment.this.getDialog().cancel();
                    }
                });

                return builder.create();

            } else {
                builder.setTitle("Add Empty Item");

                LayoutInflater inflater = getActivity().getLayoutInflater();
                View view = inflater.inflate(R.layout.dialog_fragment_item_list, null);

                final TextView TV_uniqueID = (TextView) view.findViewById(R.id.TV_uniqueID);
                TextView TV_quantity = (TextView) view.findViewById(R.id.TV_quantity);
                S_item = (AutoCompleteTextView) view.findViewById(R.id.S_items);
                ET_quantity = (EditText) view.findViewById(R.id.ET_quantity);
                ET_empty = (EditText) view.findViewById(R.id.ET_empty);
                ET_uniqueID = (EditText) view.findViewById(R.id.ET_uniqueID);

                ET_quantity.setVisibility(View.GONE);
                TV_quantity.setVisibility(View.GONE);
                ET_uniqueID.setVisibility(View.GONE);
                TV_uniqueID.setVisibility(View.GONE);

                S_item.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                        if (S_item.getText().toString().contains("Ladoo")) {
                            TV_uniqueID.setVisibility(View.VISIBLE);
                            ET_uniqueID.setVisibility(View.VISIBLE);
                        } else {
                            TV_uniqueID.setVisibility(View.GONE);
                            ET_uniqueID.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parentView) {
                        // your code here
                    }

                });

                ArrayAdapter<String> vehicleAdapter = new ArrayAdapter<>
                        (getActivity(), android.R.layout.select_dialog_item, new ArrayList<>(itemList.keySet()));
                S_item.setThreshold(0);
                S_item.setAdapter(vehicleAdapter);

                builder.setView(view);
                builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Log.e("Predelivery", "Button should enabled");
                        B_predelivery.setEnabled(true);
                        addItem();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ItemListDialogFragment.this.getDialog().cancel();
                    }
                });

                return builder.create();
            }
        }
    }
}
