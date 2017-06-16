package com.mayankattri.aqua;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.HttpsURLConnection;

public class LoginActivity extends AppCompatActivity {

    public EditText ET_username, ET_password;
    public static String un, pswrd;
    public int code;
    public JSONObject postData;
    public static SharedPreferences sp;
    public static boolean exit = false;
    public Button B_login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        B_login = (Button) findViewById(R.id.B_store_navigation);
        ET_username = (EditText) findViewById(R.id.ET_username);
        ET_password = (EditText) findViewById(R.id.ET_password);

        sp = getSharedPreferences("LOGIN", Context.MODE_PRIVATE);

//        check if already logged in
        if (sp.getInt("FLAG", -1) == 1) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("username", sp.getString("USERNAME", ""));
                obj.put("password", sp.getString("PASSWORD", ""));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Intent intent = new Intent(this, HomeActivity.class);
            intent.putExtra("USER_DETAILS", obj.toString());
            startActivityForResult(intent, 1);
        }

        B_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkConnected()) {
                    postData = new JSONObject();
                    try {
                        postData.put("username", ET_username.getText().toString());
                        postData.put("password", ET_password.getText().toString());

                        final ProgressDialog dialog = new ProgressDialog(LoginActivity.this);
                        dialog.setMessage("Loging in...");
                        dialog.show();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    new SendLoginDetailsTask().execute("http://52.66.136.236/login/", postData.toString()).get();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                } catch (ExecutionException e) {
                                    e.printStackTrace();
                                }
                                dialog.dismiss();
                            }
                        }, 1000);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    final ProgressDialog dialog = new ProgressDialog(LoginActivity.this);
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
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (exit) {
                this.finishAffinity();
//                for api >= 21
//               getActivity().finishAndRemoveTask ();
            }
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    private class SendLoginDetailsTask extends AsyncTask<String, Void, String> {

        final ProgressDialog dialog = new ProgressDialog(LoginActivity.this);

        @Override
        protected void onPreExecute() {
            dialog.setMessage("Processing...");
            dialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL("http://52.66.136.236/login/");

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(params[1]);

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
                    dialog.dismiss();
                    code = 1;
                    un = ET_username.getText().toString();
                    pswrd = ET_password.getText().toString();

                    SharedPreferences.Editor editor = sp.edit();
                    editor.putInt("FLAG", 1);
                    editor.putString("USERNAME", un);
                    editor.putString("PASSWORD", pswrd);
                    editor.apply();

                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                    intent.putExtra("USER_DETAILS", postData.toString());
                    startActivityForResult(intent, 1);

                } else {
                    dialog.dismiss();
                    Toast.makeText(LoginActivity.this, "Wrong Credentials", Toast.LENGTH_LONG).show();
                    final ProgressDialog dialog = new ProgressDialog(LoginActivity.this);
                    dialog.setMessage("Wrong Username or Password");
                    dialog.show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            dialog.dismiss();
                        }
                    }, 1000);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}
