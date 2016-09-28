package com.cms.demo.app;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;

import android.content.SharedPreferences;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import fr.ganfra.materialspinner.MaterialSpinner;

/**
 * Created by NoName on 2/20/2016.
 */
public class AssignInventory extends Fragment {
    MaterialSpinner spinner_category;
    AutoCompleteTextView auto_serial;
    TextView serial_label;
    TextView name_values;
    TextView description_values;
    TextView value;
    TextView purchase_date;
    TextView expiry_date;
    LinearLayout details;
    SharedPreferences prefs;
    String inventory_id,department_id;
    ArrayAdapter<String> adapter;
    public AssignInventory(){

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {



        View rootView=inflater.inflate(R.layout.assign_inventory,container,false);
        spinner_category=(MaterialSpinner)rootView.findViewById(R.id.category_values);


        auto_serial=(AutoCompleteTextView)rootView.findViewById(R.id.serial_values);

        serial_label=(TextView)rootView.findViewById(R.id.serialLabel);
        name_values=(TextView) rootView.findViewById(R.id.name_value);
        description_values=(TextView) rootView.findViewById(R.id.description_value);
        value=(TextView) rootView.findViewById(R.id.inventory_value);
        purchase_date=(TextView) rootView.findViewById(R.id.purchase_value);
        expiry_date=(TextView) rootView.findViewById(R.id.expiry_value);
        Button submit_inventory=(Button)rootView.findViewById(R.id.submit);
        details=(LinearLayout)rootView.findViewById(R.id.details);
        prefs=PreferenceManager.getDefaultSharedPreferences(getActivity());
        department_id=String.valueOf(prefs.getInt("departmentId",0));
        new ListCategory().execute("listCategory","",department_id);


        spinner_category.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                boolean check=prefs.getBoolean("check",false);
                if(!check && !spinner_category.getSelectedItem().toString().equals("Select a category."))
                    Toast.makeText(getActivity(),"Please select an task to assign a inventory",Toast.LENGTH_SHORT).show();
                else{
                if(!spinner_category.getSelectedItem().toString().equals("Select a category."))
                    new ListCategory().execute("category",spinner_category.getSelectedItem().toString(),department_id);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

        auto_serial.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                new ListCategory().execute("serial",parent.getItemAtPosition(position).toString(),department_id);

            }
        });

        submit_inventory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String main_id=prefs.getString("main_id",null);
                final String emp_id=prefs.getString("assign_emp",null);
                new StoreInventory().execute(emp_id,inventory_id,"request",main_id);
            }
        });



        return rootView;
    }



    private class StoreInventory extends AsyncTask<String,Void,String> {
        String line;
        ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog=new ProgressDialog(getActivity());
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setMessage("Please Wait");

            if(!progressDialog.isShowing()){
                progressDialog.show();
            }
        }


        @Override
        protected void onPostExecute(String aVoid) {
            super.onPostExecute(aVoid);
            progressDialog.cancel();
            Toast.makeText(getActivity(),"Inventory assigned successfully",Toast.LENGTH_SHORT).show();

            SharedPreferences.Editor editor=prefs.edit();
            editor.remove("main_id");
            editor.remove("assign_emp");
            editor.remove("check");
            editor.commit();
            Intent start_main=new Intent(getActivity(),MainScreen.class);
            startActivity(start_main);
            getActivity().finish();

        }

        @Override
        protected String doInBackground(String... params) {

            try{
                String link="http://companytest.site88.net/attendance/operate_inventory.php";
                URL url = new URL(link);

                String data  = URLEncoder.encode("eid", "UTF-8") + "=" + URLEncoder.encode(params[0], "UTF-8");
                data+="&"+ URLEncoder.encode("inventory_id", "UTF-8") + "=" + URLEncoder.encode(params[1], "UTF-8");
                data+="&"+ URLEncoder.encode("type", "UTF-8") + "=" + URLEncoder.encode(params[2], "UTF-8");
                data+="&"+ URLEncoder.encode("main_id", "UTF-8") + "=" + URLEncoder.encode(params[3], "UTF-8");

                HttpURLConnection conn =(HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Connection", "close");
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.connect();
                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                Log.d("sent",""+data);
                wr.write(data);
                wr.flush();

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                StringBuilder sb = new StringBuilder();
                // Read Server Response
                while((line = reader.readLine()) != null){
                    Log.d("Receiving", "" + line);
                    sb.append(line);

                    //sb.append(line);
                    break;
                }
                //     return sb.toString();
                reader.close();

            }

            catch(Exception e){
                Log.d("ExceptionInventory",""+e.toString());
            }
            //   return_rec();
            //    Boolean status=Boolean.valueOf(line);
            //  Log.d("Receiving", "" + status);

            return line;


        }
    }



    private class ListCategory extends AsyncTask<String,Void,String>{
        ProgressDialog progressDialog;
        String identifier,line;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog=new ProgressDialog(getActivity());
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setMessage("Please Wait");

            if(!progressDialog.isShowing()){
                progressDialog.show();
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try{
                if(identifier.equals("category")){
                org.json.JSONObject getArray=new org.json.JSONObject(s);
                JSONArray array_items=getArray.getJSONArray("categories");

                String[] display_values=new String[array_items.length()];
                for(int i=0;i<display_values.length;i++){
                    display_values[i]=array_items.get(i).toString();
                }

                auto_serial.setVisibility(View.VISIBLE);
                serial_label.setVisibility(View.VISIBLE);
                ArrayAdapter<String> showSerial=new ArrayAdapter<String>(getActivity(),android.R.layout.simple_dropdown_item_1line,display_values);
                auto_serial.setAdapter(showSerial);
                }else if(identifier.equals("listCategory")){

                    org.json.JSONObject object=new org.json.JSONObject(s);
                    JSONArray array_cat=object.getJSONArray("list");


                    String[] category_values=new String[array_cat.length()];
                            for(int i=0;i<array_cat.length();i++){
                                category_values[i]=array_cat.get(i).toString();
                            }

                    adapter = new ArrayAdapter<String>(getActivity(),
                            android.R.layout.simple_spinner_item, category_values);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner_category.setAdapter(adapter);
                }else{

                    org.json.JSONObject getArray=new org.json.JSONObject(s);
                    JSONArray array_items=getArray.getJSONArray("categories");
                    org.json.JSONObject values=new org.json.JSONObject(array_items.get(0).toString());
                    inventory_id=values.getString("inventoryId");
                    name_values.setText(values.getString("name").toString());
                    description_values.setText(values.getString("description"));
                    value.setText(values.getString("value"));
                    purchase_date.setText(values.getString("purchase_date"));
                    expiry_date.setText(values.getString("expiry_date"));
                    details.setVisibility(View.VISIBLE);

                }

                progressDialog.cancel();
            }catch (Exception e){
                Log.d("ExceptionAssign",""+e.toString());
                Toast.makeText(getActivity(),"There was a problem connecting the server, Please try again later",Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected String doInBackground(String... params) {
            try{
                identifier=params[0];
                String link="http://companytest.site88.net/attendance/assign_inventory.php";
                String data  = URLEncoder.encode("identifier", "UTF-8") + "=" + URLEncoder.encode(params[0], "UTF-8");
                data+="&"+URLEncoder.encode("category", "UTF-8") + "=" + URLEncoder.encode(params[1], "UTF-8");
                data+="&"+URLEncoder.encode("departmentId", "UTF-8") + "=" + URLEncoder.encode(params[2], "UTF-8");

                URL url = new URL(link);
                URLConnection conn = url.openConnection();
                conn.setDoOutput(true);
                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                Log.d("sent",""+data);
                wr.write(data);
                wr.flush();
                Log.d("writeen",""+data);
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                StringBuilder sb = new StringBuilder();
                // Read Server Response
                while((line = reader.readLine()) != null){
                    Log.d("Receiving", "" + line);
                    sb.append(line);

                    //sb.append(line);
                    break;
                }
                //     return sb.toString();
            }
            catch(Exception e){
                Log.d("ExceptionAssign",e.toString());
            }
            //   return_rec();
            //    Boolean status=Boolean.valueOf(line);
            //  Log.d("Receiving", "" + status);

            return line;
        }
    }

    private  boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }
    boolean check_internet;

    public  boolean hasActiveInternetConnection(Context context) {
        if (isNetworkAvailable()) {
            try {
                HttpURLConnection urlc = (HttpURLConnection)
                        (new URL("http://clients3.google.com/generate_204")
                                .openConnection());
                urlc.setRequestProperty("User-Agent", "Android");
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(10000);
                urlc.connect();
                return (urlc.getResponseCode() == 204 &&
                        urlc.getContentLength() == 0);
            } catch (IOException e) {
                Log.e("Internet", "Error checking internet connection", e);
            }
        } else {
            Log.d("Internet", "No network available!");
        }
        return false;
    }

    private class Check_Internet extends AsyncTask<String,Void,Boolean> {
        String status;

        @Override
        protected Boolean doInBackground(String... params) {
            // TODO Auto-generated method stub
            check_internet = hasActiveInternetConnection(getActivity());
            status = params[0];
            return check_internet;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            if (result) {
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
     }


}
