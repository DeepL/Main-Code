package com.cms.demo.app;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import fr.ganfra.materialspinner.MaterialSpinner;

public class AssetsForm extends Fragment  {




    Button mFab;

    String[] mobileArray ;
    Integer[] inventory_id;
    ListView listView;
    String[] listData;
    String emp_name;
    LinearLayout showForm;
    EditText serial,name,description,emp;
    MaterialSpinner spinner_department,categories_val;
    Button upload;
    String emp_id;

    WebView webView;
    ImageButton close;
    public AssetsForm(){

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //   return super.onCreateView(inflater, container, savedInstanceState);

        View rootView=inflater.inflate(R.layout.assetsform,container,false);

        mFab=(Button)rootView.findViewById(R.id.fab);
        spinner_department=(MaterialSpinner)rootView.findViewById(R.id.department_values);
        categories_val=(MaterialSpinner)rootView.findViewById(R.id.category_values);
        showForm=(LinearLayout)rootView.findViewById(R.id.showForm);
        listView=(ListView)rootView.findViewById(R.id.listView);

        close=(ImageButton)rootView.findViewById(R.id.closeButton);
        String[] array_categories={"OHS","Admin","IT","DT"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, array_categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        webView=(WebView)rootView.findViewById(R.id.webview);
        spinner_department.setAdapter(adapter);
        String[] array_approve={"Yes","No"};
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(getActivity());
        emp_id=prefs.getString("empid",null);
        new SendInventory().execute(emp_id,"display");



        spinner_department.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(!spinner_department.getSelectedItem().toString().equalsIgnoreCase("Select a department.")){

                String selected=spinner_department.getSelectedItem().toString();
                    new Check_Internet().execute(selected);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(showForm.getVisibility()==View.VISIBLE){
                    listView.setVisibility(View.VISIBLE);
                    showForm.setVisibility(View.GONE);

                }else{
                    listView.setVisibility(View.GONE);
                    showForm.setVisibility(View.VISIBLE);
                    close.setVisibility(View.GONE);
                    mFab.setVisibility(View.GONE);
                }
            }
        });
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinner_department.setSelection(0);
                webView.setVisibility(View.GONE);
                close.setVisibility(View.GONE);
            }
        });

        return rootView;
    }




    private  boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }
    boolean check_internet;
    public  boolean hasActiveInternetConnection(Context context) {
        if (isNetworkAvailable()) {
            try {
                HttpURLConnection urlc = (HttpURLConnection) (new URL("http://www.google.com").openConnection());
                urlc.setRequestProperty("User-Agent", "Test");
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(10000);
                urlc.connect();
                return (urlc.getResponseCode() == 200);
            } catch (IOException e) {
            }
        } else {
        }
        return false;
    }

    private class Check_Internet extends AsyncTask<String,Void,Boolean> {
        String selected;

        @Override
        protected Boolean doInBackground(String... params) {
            // TODO Auto-generated method stub
            selected = params[0];
            check_internet = hasActiveInternetConnection(getActivity());
            return check_internet;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            if (result) {


                webView.setVisibility(View.VISIBLE);
                webView.getSettings().setLoadsImagesAutomatically(true);
                webView.getSettings().setJavaScriptEnabled(true);
                webView.setWebViewClient(new WebViewClient());

                 if(selected.equals("OHS"))
                    webView.loadUrl("http://m.vedangradio.com/txns/new?empid="+emp_id+"&deptid=1");
                if(selected.equals("Admin"))
                    webView.loadUrl("http://m.vedangradio.com/txns/new?empid="+emp_id+"&deptid=2");
                if(selected.equals("IT"))
                    webView.loadUrl("http://m.vedangradio.com/txns/new?empid="+emp_id+"&deptid=3");
                if(selected.equals("DT"))
                    webView.loadUrl("http://m.vedangradio.com/txns/new?empid="+emp_id+"&deptid=4");
                webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
                webView.setWebViewClient(new WebViewClient(){
                    @Override
                    public void onPageFinished(WebView view, String url) {
                        super.onPageFinished(view, url);
                        if(webView.getVisibility()==View.VISIBLE)
                        close.setVisibility(View.VISIBLE);
                    }
                });
             } else {
                    Toast.makeText(getActivity(), "Please check your internet connection", Toast.LENGTH_SHORT).show();

                }

        }

            @Override
            protected void onPreExecute () {
                super.onPreExecute();

            }
        }




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }



    private class SendInventory extends AsyncTask<String,Void,String> {
        String line,identifier;
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
            if(identifier.equals("retreive")){
                try{
                    JSONObject object=new JSONObject(aVoid);
                    JSONArray array_val=object.getJSONArray("categories");
                    String[] categories=new String[array_val.length()];
                    for(int i=0;i<array_val.length();i++){
                        categories[i]=array_val.get(i).toString();
                    }
                     ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                            android.R.layout.simple_spinner_item, categories);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                    categories_val.setAdapter(adapter);
                    upload.setClickable(true);
                }catch (JSONException e){

                }
            }else {
                if (showForm.getVisibility() == View.VISIBLE) {
                    showForm.setVisibility(View.GONE);
                    listView.setVisibility(View.VISIBLE);

                }

                if(aVoid!=null){
                if (identifier.equals("store"))
                    Toast.makeText(getActivity(), "Your inventory has been stored successfully !", Toast.LENGTH_SHORT).show();

                try {

                    //Converting to dip unit
                    JSONObject object = new JSONObject(aVoid);
                    JSONArray array_val = object.getJSONArray("data");

                    if (array_val.length() > 0) {

                        mobileArray = new String[array_val.length()];

                        for (int current = 0; current < array_val.length(); current++) {
                            JSONObject asd = new org.json.JSONObject(array_val.get(current).toString());
                            mobileArray[current] = current+1+". " + asd.getString("name")+"\n    Department: " + asd.getString("dept_name").toString() + "\n    Category: " + asd.getString("cat_name").toString()
                                    + "\n    Serial Number: " + asd.getString("serial").toString();
                        }

                        listView.setEnabled(true);
                    } else {
                        mobileArray = new String[1];
                        mobileArray[0] = "You have not submitted any assets.";
                        listView.setEnabled(false);
                    }
                    listView.setAdapter(null);
                    ArrayAdapter adapter = new ArrayAdapter<String>(getActivity(), R.layout.assetslist, mobileArray);
                    listView.setAdapter(adapter);


                } catch (JSONException e) {

                }
            }else{
                Toast.makeText(getActivity(),"There was aproblem connecting the server, Please try again later.",Toast.LENGTH_SHORT).show();
                }
            }
        }

        @Override
        protected String doInBackground(String... params) {

            try{
                identifier=params[1];
                String link="http://companytest.site88.net/attendance/sendForm.php";
                URL url = new URL(link);

                String data  = URLEncoder.encode("eid", "UTF-8") + "=" + URLEncoder.encode(emp_id, "UTF-8");
                data += "&" + URLEncoder.encode("identifier", "UTF-8") + "=" + URLEncoder.encode(params[1], "UTF-8");

                if(params[1].equals("retreive")){
                    data += "&" + URLEncoder.encode("department", "UTF-8") + "=" + URLEncoder.encode(params[2], "UTF-8");
               }
                if(params[1].equals("store")){
                    data += "&" + URLEncoder.encode("name", "UTF-8") + "=" + URLEncoder.encode(params[2], "UTF-8");
                    data += "&" + URLEncoder.encode("serial", "UTF-8") + "=" + URLEncoder.encode(params[3], "UTF-8");
                    data += "&" + URLEncoder.encode("description", "UTF-8") + "=" + URLEncoder.encode(params[4], "UTF-8");
                    data += "&" + URLEncoder.encode("department", "UTF-8") + "=" + URLEncoder.encode(params[5], "UTF-8");
                    data += "&" + URLEncoder.encode("category", "UTF-8") + "=" + URLEncoder.encode(params[6], "UTF-8");
                }

                URLConnection conn =(URLConnection) url.openConnection();
                conn.setRequestProperty("Connection", "close");
                conn.setDoOutput(true);
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
