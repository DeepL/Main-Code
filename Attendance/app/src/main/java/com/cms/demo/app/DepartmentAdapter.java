package com.cms.demo.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by NoName on 7/6/2016.
 */
public class DepartmentAdapter extends BaseAdapter implements View.OnClickListener {

    private Activity activity;
    private ArrayList data;
    private static LayoutInflater inflater=null;
    public Resources res;
    DepartmentClass tempValues=null;
    List<TextView> empText=new ArrayList<TextView>();
    List<String> mainId=new ArrayList<String>();
    List<String> empId=new ArrayList<String>();
    List<String> inventoryId=new ArrayList<String>();
    List<String> operationId=new ArrayList<String>();

    Button operation;
    int i=0;


    @Override
    public void onClick(View v) {
        Log.v("CustomAdapter", "=====Row button clicked=====");
        Toast.makeText(activity,"ASD",Toast.LENGTH_SHORT).show();
    }

    public DepartmentAdapter(Activity a,ArrayList list){
        activity = a;
        data=list;

        activity = a;


        /***********  Layout inflator to call external xml layout () ***********/
        inflater = ( LayoutInflater )activity.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {

        return data.size();

    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View details=convertView;
        ViewHolder holder=null;

        if(convertView==null){
            details=inflater.inflate(R.layout.detailed_department,null);
                holder=new ViewHolder();
        holder.managerId=(TextView)details.findViewById(R.id.manager_id);
        holder.empId=(TextView)details.findViewById(R.id.emp_id);
        holder.name=(TextView)details.findViewById(R.id.name);
        holder.description=(TextView)details.findViewById(R.id.description);
        holder.submit=(Button)details.findViewById(R.id.operation);
            details.setTag(holder);
        }else
            holder=(ViewHolder)details.getTag();
        tempValues=null;
        tempValues=(DepartmentClass)data.get(position);
        holder.managerId.setText(tempValues.getManagerId());
        holder.empId.setText(tempValues.getEmpId());
        holder.name.setText(tempValues.getName());
        holder.description.setText(tempValues.getDescription());
        holder.submit.setText(tempValues.getOperation());

        empText.add(holder.empId);
        mainId.add(tempValues.getMain_id());
        inventoryId.add(String.valueOf(tempValues.getInventory_id()));
        empId.add(String.valueOf(tempValues.getEmpId()));
        operationId.add(String.valueOf(tempValues.getOperation()));

        if(holder.description.getText().toString().equals("Description"))
            holder.submit.setVisibility(View.INVISIBLE);

        holder.submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(activity);
               final SharedPreferences.Editor editor=prefs.edit();
                if(operationId.get(position).equalsIgnoreCase("Requested")){
                    final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);

                alertDialogBuilder.setTitle("Are you sure ?");
                alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                            editor.putString("main_id",mainId.get(position));
                            editor.putString("assign_emp",empId.get(position));
                            editor.putBoolean("check",true);
                            editor.commit();
                            ((Department) activity).selectPage(1);
                        }
                });
                alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                    alertDialogBuilder.show();
                }else
                    new StoreInventory().execute(empId.get(position),String.valueOf(prefs.getInt("departmentId",0)),inventoryId.get(position),operationId.get(position),mainId.get(position));

            }
        });
//        holder.submit.setOnClickListener(new OnItemClickListener(position));

        return details;
    }


    private class StoreInventory extends AsyncTask<String,Void,String> {
        String line;
        ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog=new ProgressDialog(activity);
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
            Toast.makeText(activity,"The inventory has been released !",Toast.LENGTH_SHORT).show();
            Intent start_main=new Intent(activity,MainScreen.class);
            activity.startActivity(start_main);
            activity.finish();

        }

        @Override
        protected String doInBackground(String... params) {

            try{
                String link="http://companytest.site88.net/attendance/operate_inventory.php";
                URL url = new URL(link);

                String data  = URLEncoder.encode("eid", "UTF-8") + "=" + URLEncoder.encode(params[0], "UTF-8");
                data+="&"+ URLEncoder.encode("department_id", "UTF-8") + "=" + URLEncoder.encode(params[1], "UTF-8");
                data+="&"+ URLEncoder.encode("inventory_id", "UTF-8") + "=" + URLEncoder.encode(params[2], "UTF-8");
                data+="&"+ URLEncoder.encode("type", "UTF-8") + "=" + URLEncoder.encode(params[3], "UTF-8");
                data+="&"+ URLEncoder.encode("main_id", "UTF-8") + "=" + URLEncoder.encode(params[4], "UTF-8");

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





    public static class ViewHolder{

        public TextView managerId;
        public TextView empId;
        TextView name;
        public TextView description;
        public Button submit;

    }


}
