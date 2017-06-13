package com.mayankattri.aqua;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.widget.ArrayAdapter;
import android.widget.TextView;

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
import java.util.concurrent.ExecutionException;

import javax.net.ssl.HttpsURLConnection;

public class LogCompletedActivity extends AppCompatActivity {

    public static ArrayList<Item> givenItemList = new ArrayList<>();
    public static ArrayList<Item> receivedItemList = new ArrayList<>();
    public TextView TV_vehicle, TV_name, TV_meter;
    public static Adapter mAdapter1, mAdapter2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_completed);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        givenItemList = new ArrayList<>();
        receivedItemList = new ArrayList<>();

        mAdapter1 = new Adapter(7, givenItemList);
        mAdapter2 = new Adapter(8, receivedItemList);

        TV_vehicle = (TextView) findViewById(R.id.TV_vehicle_value);
        TV_name = (TextView) findViewById(R.id.TV_name_value);
        TV_meter = (TextView) findViewById(R.id.TV_meter_value);

        ViewPager mViewPager = (ViewPager) findViewById(R.id.container);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        AdapterPager mSectionsPagerAdapter = new AdapterPager(getSupportFragmentManager());
        mSectionsPagerAdapter.addFragment(new Fragment1(), "Given Items");
        mSectionsPagerAdapter.addFragment(new Fragment2(), "Returned Items");
        mViewPager.setAdapter(mSectionsPagerAdapter);

        Intent intent = getIntent();
        Trip activeTripItem = (Trip) intent.getSerializableExtra("COMPLETED_TRIP_ITEM");

        JSONObject obj = new JSONObject();
        JSONObject lras = new JSONObject();

        try {
            lras.put("lras_id", activeTripItem.getId());
            obj.put("username", LoginActivity.un);
            obj.put("password", LoginActivity.pswrd);
            obj.put("data", lras);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            new LogPostDeliveryItemsDetailsTask().execute(obj.toString()).get();
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

    private class LogPostDeliveryItemsDetailsTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL("http://52.66.136.236/api/logs/Details/");

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
            Log.e("Log PostDel Rec JSON", result);
            try {
                JSONArray A = new JSONArray(result);
                JSONObject obj = A.getJSONObject(0);
                String vehicle_id = obj.getString("vehicle_name");
                Double meter = obj.getDouble("Meter_Reading");
                String name = obj.getString("employee_name");
                TV_vehicle.setText(vehicle_id);
                TV_meter.setText(meter.toString());
                TV_name.setText(name);

                JSONArray arr = obj.getJSONArray("item_given");
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

                JSONArray arr1 = obj.getJSONArray("item_rec");
                for (int i = 0; i < arr1.length(); i++) {
                    String item = arr1.getJSONObject(i).getString("item_name");
                    int quantity = arr1.getJSONObject(i).getInt("quantity");
                    Log.e("TAG", item);
                    receivedItemList.add(new Item(item, Integer.toString(quantity), false));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            LogCompletedActivity.mAdapter1.notifyDataSetChanged();
            LogCompletedActivity.mAdapter2.notifyDataSetChanged();
        }
    }

    public static class Fragment1 extends Fragment {

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.tab1, container, false);

            RecyclerView recyclerView1 = (RecyclerView) rootView.findViewById(R.id.recycler_view1);
            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
            recyclerView1.setLayoutManager(mLayoutManager);
            recyclerView1.setItemAnimator(new DefaultItemAnimator());
            recyclerView1.setAdapter(mAdapter1);

            return rootView;
        }
    }

    public static class Fragment2 extends Fragment {

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.tab2, container, false);

            RecyclerView recyclerView2 = (RecyclerView) rootView.findViewById(R.id.recycler_view2);
            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
            recyclerView2.setLayoutManager(mLayoutManager);
            recyclerView2.setItemAnimator(new DefaultItemAnimator());
            recyclerView2.setAdapter(mAdapter2);

            return rootView;
        }
    }
}
