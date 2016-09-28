package com.cms.demo.app;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import android.widget.ListView;
import android.widget.Toast;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class ThirdDept extends Fragment {
    String[] mobileArray ;
    String departmentId;
    ListView listView;
    Integer[] inventory_id;
    String emp_name,emp_id;
    public ThirdDept(){

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //   return super.onCreateView(inflater, container, savedInstanceState);

        View rootView=inflater.inflate(R.layout.listviewinventory,container,false);

        listView=(ListView) rootView.findViewById(R.id.listView);
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(getActivity());
        emp_name=prefs.getString("employee_inventory",null);

        emp_id=prefs.getString("empid",null);
        // departmentId=prefs.getString("departmentId",null);

        departmentId="3";
        new ReceiveInventory().execute(emp_name,departmentId);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {


                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

                alertDialogBuilder.setTitle("Release Inventory ?");
                alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new StoreInventory().execute(emp_name,departmentId,"",inventory_id[position].toString(),"release");

                    }
                });
                alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                alertDialogBuilder.show();

            }
        });


        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


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
            Toast.makeText(getActivity(),"The department has been notified to release the inventory !",Toast.LENGTH_SHORT).show();
            Intent start_main=new Intent(getActivity(),Manager.class);
            startActivity(start_main);
            getActivity().finish();
        }

        @Override
        protected String doInBackground(String... params) {

            try{
                String link="http://companytest.site88.net/attendance/update_details.php";
                URL url = new URL(link);
                String data  = URLEncoder.encode("eid", "UTF-8") + "=" + URLEncoder.encode(params[0], "UTF-8");
                data+="&"+ URLEncoder.encode("department_id", "UTF-8") + "=" + URLEncoder.encode(params[1], "UTF-8");
                data+="&"+ URLEncoder.encode("description", "UTF-8") + "=" + URLEncoder.encode(params[2], "UTF-8");
                data+="&"+ URLEncoder.encode("inventory_id", "UTF-8") + "=" + URLEncoder.encode(params[3], "UTF-8");
                data+="&"+ URLEncoder.encode("type", "UTF-8") + "=" + URLEncoder.encode(params[4], "UTF-8");
                data+="&"+ URLEncoder.encode("request_id", "UTF-8") + "=" + URLEncoder.encode(emp_id, "UTF-8");


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



    private class ReceiveInventory extends AsyncTask<String,Void,String> {
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
            try {
                JSONObject obj=new JSONObject(aVoid);


                JSONArray array_items=obj.getJSONArray(departmentId);
                if(array_items.length()>0){
                    mobileArray=new String[array_items.length()];
                    inventory_id=new Integer[array_items.length()];
                    for(int i=0;i<array_items.length();i++){
                        JSONObject obj_val=new JSONObject(array_items.get(i).toString());
                        mobileArray[i]=obj_val.getString("name");
                        inventory_id[i]=obj_val.getInt("id");
                    }
                    listView.setEnabled(true);
                }else{
                    mobileArray=new String[1];
                    mobileArray[0]="No inventories in this department";
                    listView.setEnabled(false);
                }

                ArrayAdapter adapter = new ArrayAdapter<String>(getActivity(), R.layout.inventoryitems, mobileArray);
                listView.setAdapter(adapter);
            }catch (JSONException e){
                Log.d("ExceptionInventory",""+e.toString());

            }
        }

        @Override
        protected String doInBackground(String... params) {

            try{
                String link="http://companytest.site88.net/attendance/display_inventory.php";
                URL url = new URL(link);
                String data  = URLEncoder.encode("eid", "UTF-8") + "=" + URLEncoder.encode(params[0], "UTF-8");
                data += "&" + URLEncoder.encode("department_id", "UTF-8") + "=" + URLEncoder.encode(params[1], "UTF-8");


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


}
