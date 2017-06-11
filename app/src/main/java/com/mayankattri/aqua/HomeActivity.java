package com.mayankattri.aqua;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.CardView;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.MPPointF;

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

import javax.net.ssl.HttpsURLConnection;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        OnChartValueSelectedListener {

    public static String userDetailsJson;
    public static String userDetails;

    private PieChart mChart;

    public static int ongoing, completed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        SharedPreferences sp = getSharedPreferences("LOGIN", Context.MODE_PRIVATE);
        LoginActivity.un = sp.getString("USERNAME", "");
        LoginActivity.pswrd = sp.getString("PASSWORD", "");

        JSONObject userCredentials = new JSONObject();
        try {
            userCredentials.put("username", LoginActivity.un);
            userCredentials.put("password", LoginActivity.pswrd);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        userDetails = userCredentials.toString();

        Log.e("User Credentials Json", userDetails);

        if (isNetworkConnected()) {
            new SendUserDetailsTask().execute(userDetails);
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

        CardView B_give = (CardView) findViewById(R.id.cv1);
        CardView B_receive = (CardView) findViewById(R.id.cv2);

        View navHeaderView = navigationView.inflateHeaderView(R.layout.nav_header_home);
        TextView TV_NavHeaderName = (TextView) navHeaderView.findViewById(R.id.TV_nav_header_name);
        TV_NavHeaderName.setText("Hi, " + LoginActivity.un);

        B_give.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, StartTripActivity.class);
                intent.putExtra("USER_DETAILS2", userDetailsJson);
                startActivityForResult(intent, 1);
            }
        });

        B_receive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, EndTripActivity.class);
                intent.putExtra("USER_DETAILS3", userDetails);
                startActivityForResult(intent, 1);
            }
        });
    }

    public void pieChart() {
        mChart = (PieChart) findViewById(R.id.chart1);
        mChart.setUsePercentValues(true);
        mChart.getDescription().setEnabled(false);
        mChart.setExtraOffsets(5, 10, 5, 5);

        mChart.setDragDecelerationFrictionCoef(0.95f);

//        mChart.setCenterTextTypeface(mTfLight);
        mChart.setCenterText(generateCenterSpannableText());

        mChart.setDrawHoleEnabled(true);
        mChart.setHoleColor(Color.WHITE);

        mChart.setTransparentCircleColor(Color.WHITE);
        mChart.setTransparentCircleAlpha(110);

        mChart.setHoleRadius(58f);
        mChart.setTransparentCircleRadius(61f);

        mChart.setDrawCenterText(true);

        mChart.setRotationAngle(0);
        // enable rotation of the chart by touch
        mChart.setRotationEnabled(true);
        mChart.setHighlightPerTapEnabled(true);

        // mChart.setUnit(" €");
        // mChart.setDrawUnitsInChart(true);

        // add a selection listener
        mChart.setOnChartValueSelectedListener(this);

        setData(2, 100);

        mChart.animateY(1400, Easing.EasingOption.EaseInOutQuad);
        // mChart.spin(2000, 0, 360);

        Legend l = mChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(false);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(0f);
        l.setYOffset(0f);

        // entry label styling
        mChart.setEntryLabelColor(Color.WHITE);
//        mChart.setEntryLabelTypeface(mTfRegular);
        mChart.setEntryLabelTextSize(12f);
    }

    private void setData(int count, float range) {
        float mult = range;

        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(ongoing));
        entries.add(new PieEntry(completed));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setDrawIcons(false);
        dataSet.setSliceSpace(3f);
        dataSet.setIconsOffset(new MPPointF(0, 60));
        dataSet.setSelectionShift(5f);

        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.rgb(50,150,250)); // blue
        colors.add(Color.GREEN);

        dataSet.setColors(colors);
        //dataSet.setSelectionShift(0f);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.BLACK);
//        data.setValueTypeface(mTfLight);
        mChart.setData(data);

        // undo all highlights
        mChart.highlightValues(null);
        mChart.invalidate();
    }

    private SpannableString generateCenterSpannableText() {
        SpannableString s = new SpannableString("Total\nTrips");
        s.setSpan(new RelativeSizeSpan(1.7f), 0, 11, 0);
        return s;
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        if (e == null)
            return;
        Log.i("VAL SELECTED",
                "Value: " + e.getY() + ", index: " + h.getX()
                        + ", DataSet index: " + h.getDataSetIndex());
    }

    @Override
    public void onNothingSelected() {
        Log.i("PieChart", "nothing selected");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            onBackPressed();    //Call the back button's method
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
        LoginActivity.exit = true;
        Intent returnIntent = new Intent();
        setResult(RESULT_CANCELED, returnIntent);
        finish();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (isNetworkConnected()) {
                new SendUserDetailsTask().execute(userDetails);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav1) {
            if (isNetworkConnected()) {
                Intent intent = new Intent(HomeActivity.this, StartTripActivity.class);
                intent.putExtra("USER_DETAILS2", userDetailsJson);
                startActivityForResult(intent, 1);
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
        } else if (id == R.id.nav2) {
            if (isNetworkConnected()) {
                Intent intent = new Intent(HomeActivity.this, EndTripActivity.class);
                intent.putExtra("USER_DETAILS3", userDetails);
                startActivityForResult(intent, 1);
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
        } else if (id == R.id.nav4) {
            if (isNetworkConnected()) {
                Intent intent = new Intent(HomeActivity.this, OngoingTripsActivity.class);
                startActivityForResult(intent, 1);
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
        } else if (id == R.id.nav5) {
            if (isNetworkConnected()) {
                Intent intent = new Intent(HomeActivity.this, CompletedTripsActivity.class);
                startActivityForResult(intent, 1);
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
        } else if (id == R.id.nav9) {
            SharedPreferences.Editor editor = LoginActivity.sp.edit();
            editor.putInt("FLAG", -1);
            editor.apply();
            final ProgressDialog dialog = new ProgressDialog(this);
            dialog.setMessage("Loging Out...");
            dialog.show();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    dialog.dismiss();
                    finish();
                }
            }, 1000);
            return true;
        } else if (id == R.id.nav8) {
//            Intent intent = new Intent(HomeActivity.this, TestActivity.class);
//            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    private class SendUserDetailsTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL("http://52.66.136.236/api/predelivery/");

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
            Log.e("full data", result);
            super.onPostExecute(result);
            userDetailsJson = result;
            String[] arr = result.split("!");

            try {
                JSONArray array = new JSONArray(arr[4]);
                for (int i = 0; i < array.length(); i++) {
                    completed = array.getJSONObject(i).getInt("completed_today");
                    ongoing = array.getJSONObject(i).getInt("active_count");
                    pieChart();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
