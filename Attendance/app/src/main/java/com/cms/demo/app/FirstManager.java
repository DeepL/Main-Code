package com.cms.demo.app;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

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

public class FirstManager extends Fragment {
    ListView listView;
    String[] main_id;
    String emp_name;
    Integer departmentId;
    SharedPreferences prefs;
    FirstManager firstManager;
    public FirstManager(){

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //   return super.onCreateView(inflater, container, savedInstanceState);

        View rootView=inflater.inflate(R.layout.listviewinventory,container,false);
        firstManager=this;
        listView=(ListView) rootView.findViewById(R.id.listView);
         prefs= PreferenceManager.getDefaultSharedPreferences(getActivity());
        emp_name=prefs.getString("employee_inventory",null);
        departmentId=prefs.getInt("departmentId",0);
        new ReceiveInventory().execute(departmentId.toString());




        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

                alertDialogBuilder.setTitle("Are you sure ?");
                alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(type[position].equals("request")){
                            SharedPreferences.Editor editor=prefs.edit();
                            editor.putString("main_id",main_id[position]);
                            editor.putString("assign_emp",array_emp[position]);
                            editor.putBoolean("check",true);
                            editor.commit();
                            ((Department) getActivity()).selectPage(1);
                        }    }
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

    String array_emp[];
    String inventory[];
    String type[];
    Integer[] inventory_id;

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
                JSONArray array_items=obj.getJSONArray(departmentId.toString());
                if(array_items.length()>0){
                String[] mobileArray=new String[array_items.length()];
                array_emp=new String[array_items.length()];
                inventory=new String[array_items.length()];
                type=new String[array_items.length()];
                main_id=new String[array_items.length()];
                inventory_id=new Integer[array_items.length()];

                    ArrayList<DepartmentClass> store=new ArrayList<>();
                    DepartmentClass assign_main=new DepartmentClass();
                    assign_main.setManagerId("Manager ID");
                    assign_main.setEmpId("Employee Id");
                    assign_main.setName("Name");
                    assign_main.setDescription("Description");
                    store.add(assign_main);
                for(int i=0;i<array_items.length();i++){
                    JSONObject object=array_items.getJSONObject(i);
                    inventory[i]=object.getString("inventory");
                    array_emp[i]=object.getString("forEmployee");
                    type[i]=object.getString("type");
                    inventory_id[i]=object.getInt("inventory_id");
                    main_id[i]=object.getString("main_id");
                    String comments=object.getString("comments");
                    DepartmentClass assign=new DepartmentClass();
                    if(TextUtils.isEmpty(comments))
                        comments="--";
                    if(type[i].equals("release")){
                        assign.setDescription(comments);
                        assign.setManagerId(object.getString("manager_id"));
                        assign.setEmpId(array_emp[i]);
                        assign.setName(inventory[i]);
                        assign.setOperation("released");
                        assign.setMain_id(main_id[i]);
                        assign.setInventory_id(inventory_id[i]);
                        mobileArray[i]=i+1+". An "+inventory[i]+" has to be released from "+array_emp[i]+"\n\nDescription: "+comments+"\nManager ID: "+object.getString("manager_id");
                    }else{
                        assign.setDescription(comments);
                        assign.setManagerId(object.getString("manager_id"));
                        assign.setEmpId(array_emp[i]);
                        assign.setMain_id(main_id[i]);
                        assign.setName("--");
                        assign.setOperation("requested");
                        mobileArray[i]=i+1+". An inventory has been requested for "+array_emp[i]+"\n\nDescription: "+comments+"\nManagerID: "+object.getString("manager_id");
                    }
                    store.add(assign);
                }
                DepartmentAdapter adapter = new DepartmentAdapter(getActivity(), store);
//                  ArrayAdapter adapter=new ArrayAdapter(getActivity(),R.layout.inventoryitems,mobileArray);
                listView.setAdapter(adapter);
                }else
                    Toast.makeText(getActivity(),"No activities !",Toast.LENGTH_SHORT).show();
            }catch (JSONException e){
                Log.d("ExceptionInventory",""+e.toString());

            }
        }

        @Override
        protected String doInBackground(String... params) {

            try{
                String link="http://companytest.site88.net/attendance/retreive.php";
                URL url = new URL(link);
                String data  = URLEncoder.encode("department_id", "UTF-8") + "=" + URLEncoder.encode(params[0], "UTF-8");


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
