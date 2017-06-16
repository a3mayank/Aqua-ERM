package com.mayankattri.aqua;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.HttpsURLConnection;

public class EndTripActivity extends AppCompatActivity {

    public ArrayList<Trip> tripsList;
    public JSONObject userCredentials;
    public static Adapter mAdapter;
    private RecyclerView recyclerView;
    public RecyclerView.LayoutManager mLayoutManager;
    public static final int AdapterID = 2;

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end_trip);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tripsList = new ArrayList<>();
        userCredentials = new JSONObject();

        try {
            userCredentials.put("username", LoginActivity.un);
            userCredentials.put("password", LoginActivity.pswrd);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            new TripDetailsTask().execute(userCredentials.toString()).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        mAdapter = new Adapter(this, AdapterID, tripsList);
        mLayoutManager = new LinearLayoutManager(this);
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

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            tripsList.clear();
            if (isNetworkConnected()) {
                new TripDetailsTask().execute(userCredentials.toString());
            } else {
                final ProgressDialog dialog = new ProgressDialog(this);
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
    }

    private class TripDetailsTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL("http://52.66.136.236/api/postdelivery/");

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
            Log.e("Active Trips JSON", result);

            try {
                JSONObject obj = new JSONObject(result);
                Iterator keysToCopyIterator = obj.keys();
                List<String> keysList = new ArrayList<>();
                while (keysToCopyIterator.hasNext()) {
                    String key = (String) keysToCopyIterator.next();
                    keysList.add(key);
                }
                for (String s: keysList) {
                    String tripsItem = obj.getString(s);
                    JSONArray arr = new JSONArray(tripsItem);
                    String[] timeArr = arr.getString(3).split(":");
                    String time = timeArr[0] + ":" + timeArr[1];
                    Trip trip = new Trip(arr.getString(2), arr.getString(1), arr.getString(0), time);
                    tripsList.add(trip);
                    Log.e("Time", arr.getString(3));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Collections.reverse(tripsList);
            mAdapter = new Adapter(EndTripActivity.this, AdapterID, tripsList);
            mLayoutManager = new LinearLayoutManager(EndTripActivity.this);
            recyclerView.setLayoutManager(mLayoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setAdapter(mAdapter);
        }
    }
}
