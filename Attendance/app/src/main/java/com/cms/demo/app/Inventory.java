package com.cms.demo.app;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
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

public class Inventory extends Fragment {
    String[] mobileArray ;
    String[] checkInventory;
    Integer[] inventory_id;
    ListView listView;
    String[] listData;
    String emp_name;
    CheckBox terms;
    public Inventory(){

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
     //   return super.onCreateView(inflater, container, savedInstanceState);

        View rootView=inflater.inflate(R.layout.listviewinventory,container,false);

        listView=(ListView) rootView.findViewById(R.id.listView);
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(getActivity());
        emp_name=prefs.getString("empid",null);
        new SendRequest().execute(emp_name,"onStart");

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                if (checkInventory[position].equals("true"))
                    Toast.makeText(getActivity(), "The inventory has already been sent. It will be discarded once accepted by the receiving employee", Toast.LENGTH_SHORT).show();
                else {

                LayoutInflater li = LayoutInflater.from(getActivity());
                LinearLayout someLayout = (LinearLayout) li.inflate(R.layout.dialogcontent, null);
                int layoutItemId = android.R.layout.simple_dropdown_item_1line;

                List<String> listValues = new ArrayList<String>();
                listValues = Arrays.asList(listData);

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), layoutItemId, listValues);


                final Dialog alertDialogBuilder = new Dialog(getActivity());
                alertDialogBuilder.setContentView(someLayout);
                alertDialogBuilder.setTitle("Send a inventory");
                terms=(CheckBox)alertDialogBuilder.findViewById(R.id.checkBox);
                    SpannableString content = new SpannableString("I agree and accept the terms and coondition");
                    content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
                    terms.setText(content);
                    terms.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showTerms();
                        }
                    });
                final AutoCompleteTextView autocompleteView =
                        (AutoCompleteTextView) alertDialogBuilder.findViewById(R.id.autocompleteView);
                autocompleteView.setAdapter(adapter);
                Button send_inventory = (Button) alertDialogBuilder.findViewById(R.id.button_send);
                send_inventory.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                     if( !autocompleteView.getText().toString().isEmpty()){
                        if(terms.isChecked()){


                        String value = autocompleteView.getText().toString();
                        alertDialogBuilder.dismiss();
                        new SendInventory().execute(emp_name, value, inventory_id[position].toString());
                        }else{
                            Toast.makeText(getActivity(),"Please agree the terms and conditions.",Toast.LENGTH_SHORT).show();
                        }
                     }else
                         Toast.makeText(getActivity(),"Please enter a employee name.",Toast.LENGTH_SHORT).show();
                    }
                });
                alertDialogBuilder.show();
            }
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                new SendRequest().execute(emp_name,"GetValue",listView.getItemAtPosition(position).toString());

                return true;
            }
        });

        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }


    private class SendInventory extends AsyncTask<String,Void,String> {
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
                Toast.makeText(getActivity(),"The inventory will be released once accepted by the employee",Toast.LENGTH_SHORT).show();
                JSONObject obj=new JSONObject(aVoid);
                    JSONArray array_items=obj.getJSONArray("inventory");
                    String mobileArray[];
                    if(array_items.length()>0){
                    mobileArray=new String[array_items.length()];
                    inventory_id=new Integer[array_items.length()];
                        checkInventory=new String[array_items.length()];
                    for(int i=0;i<array_items.length();i++){
                        JSONObject object=new JSONObject(array_items.get(i).toString());
                        mobileArray[i]=object.getString("name");
                        inventory_id[i]=object.getInt("inventory_id");
                        checkInventory[i]=object.getString("isSent");
                    }
                        listView.setEnabled(true);
                    }else{
                        mobileArray=new String[0];
                        mobileArray[0]="No inventories found, Please contact your manager for requesting a inventory";
                        listView.setEnabled(false);
                        Toast.makeText(getActivity(),"You don't have any inventories left",Toast.LENGTH_SHORT).show();
                    }
                    listView.setAdapter(null);
                    ArrayAdapter adapter = new ArrayAdapter<String>(getActivity(), R.layout.inventoryitems, mobileArray);
                    listView.setAdapter(adapter);
                Intent startMain=new Intent(getActivity(),MainScreen.class);
                startActivity(startMain);
                getActivity().finish();
                }catch (JSONException e){
                Log.d("ExceptionInventory",""+e.toString());
                 }
        }

        @Override
        protected String doInBackground(String... params) {

            try{
                String link="http://companytest.site88.net/attendance/transfer.php";
                URL url = new URL(link);
                String data  = URLEncoder.encode("eid", "UTF-8") + "=" + URLEncoder.encode(params[0], "UTF-8");
                data += "&" + URLEncoder.encode("receiver_eid", "UTF-8") + "=" + URLEncoder.encode(params[1], "UTF-8");
                data += "&" + URLEncoder.encode("inventory_id", "UTF-8") + "=" + URLEncoder.encode(params[2], "UTF-8");
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

    public void showTerms(){

        android.app.AlertDialog.Builder dialog=new android.app.AlertDialog.Builder(getActivity());
        dialog.setTitle("Terms and condition");

        dialog.setMessage("USE OF VEDANG IT ASSETS\n" +
                "\n" +
                "We would like to inform you that during the course of your employment with Vedang., Vedang may provide you with the use of a laptop computer, a PC, or other IT Asset (hereinafter referred to as \"IT Assets\") for purposes of enabling you to perform your job. The IT Assets will be provided to you under the terms and conditions set forth in this document:\n" +
                "\n" +
                "1.\tThe IT Assets remain the property of Vedang at all times and you will be allowed to use them during your employment with Vedang, subject to the remainder of these provisions.\n" +
                "\n" +
                "2.\tAny data stored on the IT Assets will be the property of Vedang.  This data may be updated or deleted solely by Vedang at any time.\n" +
                "\n" +
                "3.\tAll of Vedang security policies will apply to your use of the IT Assets at all times. You will notify Vedang’ IT Help Desk immediately in the event any of the IT Assets are lost or stolen.\n" +
                "\n" +
                "4.\tVedang has the right to terminate your use of the IT Assets for any reason at any time, in its sole discretion. You will promptly return the IT Assets to Vedang if requested to do so. \n" +
                "\n" +
                "5.\tUpon termination, for any reason whatsoever, you shall immediately deliver to the Company any IT Assets which may be in your possession or control.  If all of the IT Assets are not returned to Vedang on or before the last date of your employment, Vedang may be entitled to withhold some or all of your final paycheck, to the maximum extent allowed by law.  Your signature on this document is authorization for such withholding.  To the extent Vedang is unable to withhold the full amount of the debt owed, you acknowledge that Vedang may pursue all legal remedies available to recover the amount owed by you.\n" +
                "\n" +
                "Confidentiality And Invention Rights Agreement\n" +
                "\n" +
                "\tAs a condition of my employment and in consideration for the pay and benefits offered to  me by Vedang, I agree that during my employment and, if applicable, for the appropriate stated time after the end of my employment, I promise and agree to the following Confidentiality and Invention Rights provisions:\n" +
                "\n" +
                "Confidentiality\n" +
                "\n" +
                "In the course of Employee’s employment with the Company and, if applicable, during Employee’s prior employment with the Company or an affiliated company, Employee will have access to and be in possession of items and information that are valuable, special or classified and constitute unique and proprietary assets.  Among these, but not limited to, are proprietary technology or know how, computer programs, software, customer lists, files, financial data, financial affairs, and other items and information belonging to the Company, its personnel, its customers and their affiliates (collectively, the “Confidential Information”).  Employee specifically acknowledges that this Confidentiality provision shall apply to the confidential and proprietary information of the Company’s customers.  Employee shall not, at any time during his employment with the Company or for a period of three (3) years after termination thereof, disclose, use or make public, directly or indirectly, any such items or information, or any part thereof to any person or entity, for any reason or purpose whatsoever except with the prior written consent of the Company or as may otherwise be required by law.  Employee shall not damage or harm the Company’s reputation, goodwill, and business relations with any entity or person including but not limited to customers, official bodies, agencies, and the Company’s personnel.  Employee will use all means available to him to prevent any disclosure of Confidential Information by any other person and will ensure that any Confidential Information of which he has knowledge is safely and securely stored.  Employee acknowledges that the Company may suffer extensive loss or damage from disclosure of Confidential Information.\n" +
                "\n" +
                "Invention Rights\n" +
                "\tAt any time during the Employee’s employment with the Company, Employee hereby assigns and shall assign to the Company all right, title and interest in all Inventions (herein defined), and all intellectual property rights in such Inventions, subject to the applicable provisions below.  The term “Inventions” means, and shall include but not be limited to, improvements, discoveries, concepts and ideas, whether patentable or not, including, but not limited to, processes, methods, formulae and techniques, which Employee may have made, discovered, conceived, or assisted in making, discovering or conceiving (whether alone or jointly with others) and all works of authorship, including but not limited to, software created by Employee, solely or jointly with others, during the term of Employee’s employment with the Company, and for one year thereafter, and which relate in any manner to the business of the Company, except in so far as this is modified by applicable provisions below.\n" +
                "\tEmployee hereby agrees to hold all such Inventions in complete trust for the benefit of the Company.\n" +
                "Employee hereby agrees to keep the Company informed promptly in writing of any and all such Inventions and to provide to Company copies of all Inventions.\n" +
                "Employee hereby agrees to assist the Company or its nominees, at the Company’s expense, to obtain patents for such Inventions in any country throughout the world by giving testimony, signing documents, providing information, attending proceedings and performing such other acts as Company may reasonably request, at all times during Employee’s employment with Company and thereafter.\n" +
                "\n" +
                "NOTE: the HR & Administration department is responsible for determining market value and/or damages, and for collection of money, if necessary.\n" +
                "In case of Handover to other employee, I will inform the authorized person in the office and will be responsible for the loss of the Asset if I do not inform on time.\n" +
                "I acknowledge that I have read and understood the terms and conditions of this letter and further agree to abide by all the terms and conditions set forth herein.\n");
        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        dialog.show();
    }



    private class SendRequest extends AsyncTask<String,Void,String> {
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
            try {
                progressDialog.cancel();
                if(identifier.equals("onStart")){
                JSONObject obj=new JSONObject(aVoid);
                JSONArray array_values=obj.getJSONArray("names");
                listData=new String[array_values.length()];
                String[] usernames=new String[array_values.length()];
                for(int i=0;i<listData.length;i++){
                        listData[i]=array_values.get(i).toString();
                }

                JSONArray array_items=obj.getJSONArray("inventory");
                if(array_items.length()>0){
                    Toast.makeText(getActivity(),"Long click on any asset to view its details !",Toast.LENGTH_SHORT).show();
                mobileArray=new String[array_items.length()];
                inventory_id=new Integer[array_items.length()];
                    checkInventory=new String[array_items.length()];
                for(int i=0;i<array_items.length();i++){
                    JSONObject object=new JSONObject(array_items.get(i).toString());
                    mobileArray[i]=object.getString("name");
                    inventory_id[i]=object.getInt("inventory_id");
                    checkInventory[i]=object.getString("isSent");
                }
                    listView.setEnabled(true);
                }else{
                    mobileArray=new String[1];
                    mobileArray[0]="No inventories found, Please contact your manager for requesting a inventory";
                    listView.setEnabled(false);
                }
                ArrayAdapter adapter = new ArrayAdapter<String>(getActivity(), R.layout.inventoryitems, mobileArray);
                listView.setAdapter(adapter);

                }else{
                    LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View customView = inflater.inflate(R.layout.inventory_details, null);
                    TextView serial=(TextView)customView.findViewById(R.id.serial_value);
                    TextView name=(TextView)customView.findViewById(R.id.name_value);
                    TextView category=(TextView)customView.findViewById(R.id.category_value);
                    TextView department=(TextView)customView.findViewById(R.id.department_value);
                    JSONObject obj=new JSONObject(aVoid);
                    JSONArray array=obj.getJSONArray("details");
                    serial.setText(array.get(0).toString());
                    name.setText(array.get(1).toString());
                    category.setText(array.get(2).toString());
                    department.setText(array.get(3).toString());
                    final AlertDialog.Builder dialog= new AlertDialog.Builder(getActivity());
                    dialog.setIcon(R.drawable.ic_library_add_white_24dp);

                    dialog.setTitle("Inventory Details");
                    dialog.setView(customView);
                    dialog.show();
                }
            }catch (JSONException e){

            }
        }

        @Override
        protected String doInBackground(String... params) {

            try{
                identifier=params[1];
                String link="http://companytest.site88.net/attendance/usernames.php";
                URL url = new URL(link);

                String data  = URLEncoder.encode("eid", "UTF-8") + "=" + URLEncoder.encode(params[0], "UTF-8");
                data  +="&"+URLEncoder.encode("identifier", "UTF-8") + "=" + URLEncoder.encode(params[1], "UTF-8");

                if(params[1].equals("GetValue"))
                    data  +="&"+URLEncoder.encode("inventory_name", "UTF-8") + "=" + URLEncoder.encode(params[2], "UTF-8");
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
