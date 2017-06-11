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
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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

public class ReceiveItemsActivity extends AppCompatActivity {

    public static ArrayList<Item> givenItemList;
    public static ArrayList<JSONObject> storeItemListJson;
    public static ArrayList<Item> receivedItemList;
    public static HashMap<String,Integer> totalItemList;
    public static int tripID;
    public static Button B_postDelivery;

    public static Adapter mAdapter2;

    public static int position;
    public static int editItemFlag;

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    public JSONObject makeJsonObject(int tripID) {
        JSONObject ldfs = new JSONObject();
        JSONObject obj = new JSONObject();
        try {
            ldfs.put("ldfs_id", tripID);
            obj.put("username", LoginActivity.un);
            obj.put("password", LoginActivity.pswrd);
            obj.put("data", ldfs);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return obj;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.activity_receive_items);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        totalItemList = new HashMap<>();
        receivedItemList = new ArrayList<>();
        storeItemListJson = new ArrayList<>();
        givenItemList = new ArrayList<>();

        ViewPager mViewPager = (ViewPager) findViewById(R.id.container);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        AdapterPager mSectionsPagerAdapter = new AdapterPager(getSupportFragmentManager());
        mSectionsPagerAdapter.addFragment(new Fragment1(), "Given Items");
        mSectionsPagerAdapter.addFragment(new Fragment2(), "Returned Items");
        mViewPager.setAdapter(mSectionsPagerAdapter);

        Intent intent = getIntent();
        Trip tripItem = (Trip) intent.getSerializableExtra("TRIP_ITEM");

        tripID = Integer.valueOf(tripItem.getId());

        TextView TV_vehicle = (TextView) findViewById(R.id.TV_vehicle_value);
        TextView TV_name = (TextView) findViewById(R.id.TV_name_value);
        final EditText ET_meter = (EditText) findViewById(R.id.ET_meter);

        TV_vehicle.setText(tripItem.getVehicle());
        TV_name.setText(tripItem.getName());

        JSONObject obj = makeJsonObject(tripID);
        Log.e("GetItemsActivity",obj.toString());

        FloatingActionButton FAB_plus = (FloatingActionButton) findViewById(R.id.FAB_minus);
        FAB_plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GetItemListDialogFragment d = new GetItemListDialogFragment();
                d.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
//                    Intent intent = getIntent();
//                    finish();
//                    startActivity(intent);
                    }
                });
                d.show(getFragmentManager(), "GetItemListDialogFragment");
            }
        });

        B_postDelivery = (Button) findViewById(R.id.B_postdelivery);
        B_postDelivery.setEnabled(false);
        B_postDelivery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s = "item";
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
                    data.put("ldfs_id", tripID);
                    data.put("meter_reading", ET_meter.getText().toString());
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

                if (isNetworkConnected()) {
                    final ProgressDialog dialog = new ProgressDialog(ReceiveItemsActivity.this);
                    dialog.setMessage("Saving");
                    dialog.show();

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                new SendPostDeliveryTask().execute(mainObject.toString()).get();
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
                    final ProgressDialog dialog = new ProgressDialog(ReceiveItemsActivity.this);
                    dialog.setMessage("No Internet");
                    dialog.show();

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            dialog.dismiss();
                        }
                    }, 2000);
                }

                Log.e("PostDelivery/save JSON", mainObject.toString());

            }
        });

        try {
            new GetGivenItemsDetailsTask().execute(obj.toString()).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        JSONObject obj2 = new JSONObject();
        try {
            obj2.put("username", LoginActivity.un);
            obj2.put("password", LoginActivity.pswrd);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            new GenerateItemListTask().execute(obj2.toString()).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
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

    public static class GetItemListDialogFragment extends DialogFragment {

        AutoCompleteTextView S_item;
        EditText ET_quantity, ET_empty, ET_filled, ET_leaked, ET_soiled, ET_broken, ET_okay, ET_uniqueID;

        public GetItemListDialogFragment() {
        }

        public void addItem() {
            int item = totalItemList.get(S_item.getText().toString());
            String quantity = ET_quantity.getText().toString();
            String empty = ET_empty.getText().toString();
            String filled = ET_filled.getText().toString();
            String leaked = ET_leaked.getText().toString();
            String soiled = ET_soiled.getText().toString();
            String broken = ET_broken.getText().toString();
            String okay = ET_okay.getText().toString();

            JSONObject itemObject = new JSONObject();
            try {

                itemObject.put("item_id", item);
                if (quantity.equals("")) itemObject.put("quantity", "0");
                else itemObject.put("quantity", quantity);
                if (empty.equals("")) itemObject.put("empty_jars", "0");
                else itemObject.put("empty_jars", empty);
                if (filled.equals("")) itemObject.put("filled_jars", "0");
                else itemObject.put("filled_jars", filled);
                if (leaked.equals("")) itemObject.put("leaked_filled", "0");
                else itemObject.put("leaked_filled", leaked);
                if (soiled.equals("")) itemObject.put("soiled_filled", "0");
                else itemObject.put("soiled_filled", soiled);
                if (broken.equals("")) itemObject.put("broken_filled", "0");
                else itemObject.put("broken_filled", broken);
                if (okay.equals("")) itemObject.put("okay_filled_bottles", "0");
                else itemObject.put("okay_filled_bottles", okay);
                storeItemListJson.add(itemObject);

                receivedItemList.add(new Item(S_item.getText().toString(), quantity, false));
                mAdapter2.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Log.e("Receive Item JSON",itemObject.toString());
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
            builder.setTitle("Receive items");

            LayoutInflater inflater = getActivity().getLayoutInflater();
            View view = inflater.inflate(R.layout.dialog_fragment_receive_item, null);

            S_item = (AutoCompleteTextView) view.findViewById(R.id.S_items);
            ET_uniqueID = (EditText) view.findViewById(R.id.ET_uniqueID);
            ET_quantity = (EditText) view.findViewById(R.id.ET_quantity);
            ET_empty = (EditText) view.findViewById(R.id.ET_empty);
            ET_filled = (EditText) view.findViewById(R.id.ET_filled);
            ET_leaked = (EditText) view.findViewById(R.id.ET_leaked_filled);
            ET_soiled = (EditText) view.findViewById(R.id.ET_soiled_filled);
            ET_broken = (EditText) view.findViewById(R.id.ET_broken_filled);
            ET_okay = (EditText) view.findViewById(R.id.ET_okay_filled);
            final TextView TV_uniqueID = (TextView) view.findViewById(R.id.TV_uniqueID);

            ET_uniqueID.setVisibility(View.GONE);
            TV_uniqueID.setVisibility(View.GONE);
            ArrayAdapter<String> itemAdapter = new ArrayAdapter<>
                    (getActivity(), android.R.layout.select_dialog_item, new ArrayList<>(totalItemList.keySet()));
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

            if (editItemFlag ==1) {
                editItemFlag = 0;
                builder.setView(view);
                builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Item temp = new Item(S_item.getText().toString(), ET_quantity.getText().toString(), false);
                        receivedItemList.set(position, temp);
                        mAdapter2.notifyDataSetChanged();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        GetItemListDialogFragment.this.getDialog().cancel();
                        editItemFlag = 0;
                    }
                });
            } else {
                builder.setView(view);
                builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        B_postDelivery.setEnabled(true);
                        B_postDelivery.setBackgroundColor(Color.parseColor("#FF1C3380"));
                        addItem();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        GetItemListDialogFragment.this.getDialog().cancel();
                    }
                });
            }

            return builder.create();
        }
    }

    public static class Fragment1 extends Fragment {

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.tab1, container, false);

            RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view1);
            Adapter mAdapter = new Adapter(6, givenItemList);
            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
            recyclerView.setLayoutManager(mLayoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setAdapter(mAdapter);

            return rootView;
        }
    }

    public static class Fragment2 extends Fragment {

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.tab2, container, false);

            RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view2);
            Adapter mAdapter = new Adapter(9, receivedItemList);
            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
            recyclerView.setLayoutManager(mLayoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setAdapter(mAdapter);

            return rootView;
        }
    }

    private class SendPostDeliveryTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL("http://52.66.136.236/api/postdelivery/save/");

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
                    BufferedReader in=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuffer sb = new StringBuffer("");
                    String line="";

                    while ((line = in.readLine()) != null) {
                        sb.append(line);
                        break;
                    }

                    in.close();
                    return sb.toString();

                }
                else {
                    return new String("false : "+responseCode);
                }
            }
            catch(Exception e){
                return new String("Exception: " + e.getMessage());
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.e("PostDel save response", result);
            try {
                JSONObject obj = new JSONObject(result);
                String msg = obj.getString("msg");
                int code = obj.getInt("code");
                Toast.makeText(ReceiveItemsActivity.this, msg, Toast.LENGTH_SHORT).show();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class GetGivenItemsDetailsTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL("http://52.66.136.236/api/postdelivery/receive/");

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
                    BufferedReader in=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuffer sb = new StringBuffer("");
                    String line="";

                    while ((line = in.readLine()) != null) {
                        sb.append(line);
                        break;
                    }

                    in.close();
                    return sb.toString();

                }
                else {
                    return new String("false : "+responseCode);
                }
            }
            catch(Exception e){
                return new String("Exception: " + e.getMessage());
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.e("PostDel Response JSON", result);
            try {
                JSONArray arr = new JSONArray(result);
                for (int i = 0; i < arr.length(); i++) {
                    String item = arr.getJSONObject(i).getString("item_name");
                    int quantity = arr.getJSONObject(i).getInt("quantity");
                    int empty = arr.getJSONObject(i).getInt("empty_quantity");
                    Log.e("TAG", item);
                    if (empty == 0) {
                        givenItemList.add(new Item(item, Integer.toString(quantity), false));
                    } else {
                        givenItemList.add(new Item(item, Integer.toString(empty), true));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class GenerateItemListTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL("http://52.66.136.236/api/postdelivery/details/");

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
                    BufferedReader in=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuffer sb = new StringBuffer("");
                    String line="";

                    while ((line = in.readLine()) != null) {
                        sb.append(line);
                        break;
                    }

                    in.close();
                    return sb.toString();

                }
                else {
                    return new String("false : "+responseCode);
                }
            }
            catch(Exception e){
                return new String("Exception: " + e.getMessage());
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.e("ItemList Receive JSON", result);
            try {
                JSONArray arr = new JSONArray(result);
                for (int i = 0; i < arr.length(); i++) {
                    String key = arr.getJSONObject(i).getJSONObject("fields").getString("item_name");
                    int value = arr.getJSONObject(i).getInt("pk");
                    totalItemList.put(key, value);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
