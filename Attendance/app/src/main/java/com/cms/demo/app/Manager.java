package com.cms.demo.app;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fr.ganfra.materialspinner.MaterialSpinner;

/**
 * Created by NoName on 6/19/2016.
 */
public class Manager extends AppCompatActivity implements ListAdapter.customButtonListener {

    String emp_id;
    String department_id;
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.managerlayout);

        SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(Manager.this);
        emp_id=prefs.getString("empid",null);
        department_id=prefs.getString("department_Id",null);
        Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        listView = (ListView) findViewById(R.id.listView);
        new ReceiveInventory().execute(emp_id);
    }

    @Override
    public void onButtonClickListner(int position,final String value) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.request_options, null);
        final MaterialSpinner sp=(MaterialSpinner)customView.findViewById(R.id.spinner_values);
        String[] arraySpinner={"OHS","IT","Admin","DT"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, arraySpinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        sp.setAdapter(adapter);
        final EditText message=(EditText)customView.findViewById(R.id.description);
        final AlertDialog.Builder dialog= new AlertDialog.Builder(Manager.this);
//        dialog.setHeaderDrawable(R.drawable.ic_library_add_white_18dp);
        dialog.setIcon(R.drawable.ic_library_add_white_24dp);
        dialog.setTitle("Request Inventory");
        dialog.setView(customView);
        dialog.setPositiveButton("Send", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if(sp.getSelectedItem().toString()!=null && sp.getSelectedItemPosition()!=0){
                    dialog.dismiss();
                    if(sp.getSelectedItem().toString().equals("OHS"))
                        new StoreInventory().execute(value,"1",message.getText().toString(),"request");
                    if(sp.getSelectedItem().toString().equals("Admin"))
                        new StoreInventory().execute(value,"2",message.getText().toString(),"request");
                    if(sp.getSelectedItem().toString().equals("IT"))
                        new StoreInventory().execute(value,"3",message.getText().toString(),"request");
                    if(sp.getSelectedItem().toString().equals("DT"))
                        new StoreInventory().execute(value,"4",message.getText().toString(),"request");
                }
                else
                    Toast.makeText(getApplicationContext(),"Please fill the required details",Toast.LENGTH_SHORT).show();

                Log.d("MaterialStyledDialogs", "Do something!");
            }}
        );

        dialog.show();





       // final Dialog alertDialogBuilder = new Dialog(Manager.this);
        //alertDialogBuilder.setTitle("Request Inventory");
      //  alertDialogBuilder.setContentView(someLayout);




    }

    private class StoreInventory extends AsyncTask<String,Void,String> {
        String line;
        ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog=new ProgressDialog(Manager.this);
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
            Toast.makeText(Manager.this,"Inventory has been requested to the department head",Toast.LENGTH_SHORT).show();
        }

        @Override
        protected String doInBackground(String... params) {

            try{
                String link="http://companytest.site88.net/attendance/update_details.php";
                URL url = new URL(link);
                String data  = URLEncoder.encode("eid", "UTF-8") + "=" + URLEncoder.encode(params[0], "UTF-8");
                data+="&"+ URLEncoder.encode("department_id", "UTF-8") + "=" + URLEncoder.encode(params[1], "UTF-8");
                data+="&"+ URLEncoder.encode("description", "UTF-8") + "=" + URLEncoder.encode(params[2], "UTF-8");
                data+="&"+ URLEncoder.encode("inventory_id", "UTF-8") + "=" + URLEncoder.encode("0", "UTF-8");
                data+="&"+ URLEncoder.encode("type", "UTF-8") + "=" + URLEncoder.encode(params[3], "UTF-8");
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


    public int dp2px(int dp) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }



    @Override
    public  void onTextViewClickListener(int position,String value){



        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(Manager.this);
        SharedPreferences.Editor editor=prefs.edit();
        editor.putString("employee_inventory",value);
        editor.commit();

        Intent start_department=new Intent(Manager.this,InventoriesDepartment.class);
        startActivity(start_department);


    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                Intent start_previous=new Intent(Manager.this,MainScreen.class);
                startActivity(start_previous);
                finish();
                return true;

            default:        return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent start_previous=new Intent(Manager.this,MainScreen.class);
        startActivity(start_previous);
        finish();
        }

    private class ReceiveInventory extends AsyncTask<String,Void,String> {
        String line;
        ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog=new ProgressDialog(Manager.this);
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

                JSONArray array_items=obj.getJSONArray("names");
                JSONArray array_names=obj.getJSONArray("username");
                String[] mobileArray=new String[array_items.length()];
                for(int i=0;i<array_items.length();i++){
                    mobileArray[i]=i+1+". "+array_names.get(i).toString()+"("+array_items.get(i).toString()+") ";
                }
                List<String> dataTemp = Arrays.asList(mobileArray);

                ArrayList<String> dataItems = new ArrayList<String>();
                dataItems.addAll(dataTemp);
                ListAdapter adapter = new ListAdapter(Manager.this, dataItems);
                adapter.setCustomButtonListner(Manager.this);
                adapter.setCustomTextListener(Manager.this);
                listView.setAdapter(adapter);


            }catch (JSONException e){
                Log.d("ExceptionInventory",""+e.toString());

            }
        }

        @Override
        protected String doInBackground(String... params) {

            try{
                String link="http://companytest.site88.net/attendance/manager_names.php";
                URL url = new URL(link);
                String data  = URLEncoder.encode("eid", "UTF-8") + "=" + URLEncoder.encode(params[0], "UTF-8");

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
