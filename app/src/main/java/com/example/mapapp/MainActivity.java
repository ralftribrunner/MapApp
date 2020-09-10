package com.example.mapapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.datatype.Duration;

public class MainActivity extends AppCompatActivity implements MyRecyclerViewAdapter.ItemClickListener{

    ArrayList<String> list;
    RecyclerView recyclerView;
    MyRecyclerViewAdapter adapter;
    String countryname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        list=new ArrayList<String>();
        getCountries();
        recyclerView = findViewById(R.id.rec_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MyRecyclerViewAdapter(this, list);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
    }

    public void getCountries() {
        String[] locales = Locale.getISOCountries();

        for (String countryCode : locales) {

            Locale obj = new Locale("", countryCode);

            Log.i("countries","Country Name = " + obj.getDisplayCountry());

            list.add(obj.getDisplayCountry());

        }
        Collections.sort(list);
    }

    @Override
    public void onItemClick(View view, int position) {
        if (isNetworkConnected()) {
            new JsonTask().execute(adapter.getItem(position));
        }
        else Toast.makeText(getApplicationContext(),"No internet connection!",Toast.LENGTH_SHORT).show();


    }
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }


    private class JsonTask extends AsyncTask<String, Void, LatLng> {

        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected LatLng doInBackground(String... params) {

                HttpURLConnection connection = null;
                BufferedReader reader = null;

                try {
                    URL url=new URL("https://eu1.locationiq.com/v1/search.php?key=40da0bcb23a16d&q="+params[0]+"&format=json");

                    Log.i("url",url.toString());
                    connection = (HttpsURLConnection) url.openConnection();
                    connection.connect();


                    InputStream stream = connection.getInputStream();

                    reader = new BufferedReader(new InputStreamReader(stream));

                    StringBuffer buffer = new StringBuffer();
                    String line = "";

                    while ((line = reader.readLine()) != null) {
                        buffer.append(line+"\n");
                        Log.d("Response: ", "> " + line);
                    }
                    JSONArray jsonArray = new JSONArray(buffer.toString());
                    JSONObject jsonObject=jsonArray.getJSONObject(0);
                    Double lat= Double.valueOf(jsonObject.getString("lat")) ;
                    Double lon=Double.valueOf(jsonObject.getString("lon")) ;
                    countryname= (String) jsonObject.get("display_name");
                    return new LatLng(lat,lon);


                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                    try {
                        if (reader != null) {
                            reader.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return new LatLng(0,0);

        }

        protected void onPostExecute(LatLng latLng) {
            super.onPreExecute();
            Intent intent=new Intent(getApplicationContext(),MapsActivity.class);
            intent.putExtra("country",countryname);
            intent.putExtra("lat",latLng.latitude);
            intent.putExtra("long",latLng.longitude);
            startActivity(intent);
        }



    }
}
