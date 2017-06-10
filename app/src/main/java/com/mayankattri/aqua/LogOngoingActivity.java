package com.mayankattri.aqua;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
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

public class LogOngoingActivity extends AppCompatActivity {

    public static ArrayList<Item> givenItemList = new ArrayList<>();
    public static Adapter mAdapter;
    private RecyclerView recyclerView;
    TextView TV_vehicle, TV_name, TV_meter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_ongoing);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        Trip activeTripItem = (Trip) intent.getSerializableExtra("ACTIVE_TRIP_ITEM");

        givenItemList = new ArrayList<>();

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mAdapter = new Adapter(this, 4, givenItemList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        TV_vehicle = (TextView) findViewById(R.id.TV_vehicle_value);
        TV_name = (TextView) findViewById(R.id.TV_name_value);
        TV_meter = (TextView) findViewById(R.id.TV_meter_value);

        JSONObject obj = new JSONObject();
        JSONObject ldfs = new JSONObject();

        try {
            ldfs.put("ldfs_id", activeTripItem.getId());
            obj.put("username", LoginActivity.un);
            obj.put("password", LoginActivity.pswrd);
            obj.put("data", ldfs);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            new LogGivenItemsDetailsTask().execute(obj.toString()).get();
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

    private class LogGivenItemsDetailsTask extends AsyncTask<String, Void, String> {
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
            Log.e("Log PreDel Rec JSON", result);
            try {
                JSONArray arr1 = new JSONArray(result);
                JSONObject obj = arr1.getJSONObject(0);
                String vehicle_id = obj.getString("vehicle_name");
                Double meter = obj.getDouble("meter_reading");
                String name = obj.getString("employee_name");
                TV_vehicle.setText(vehicle_id);
                TV_meter.setText(meter.toString());
                TV_name.setText(name);

                JSONArray arr = obj.getJSONArray("item_details");
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
//                    givenAdapter.notifyDataSetChanged();
                    mAdapter.notifyDataSetChanged();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
