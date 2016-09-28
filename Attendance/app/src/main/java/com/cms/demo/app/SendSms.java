package com.cms.demo.app;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/**
 * Created by NoName on 5/17/2016.
 */
public class SendSms extends Activity {
    EditText emp_id,message_data;
    TextView mobilenumber,next;
    Button send_message,verify;
    String employee_id=null;
    String value_id,mobile_number;
    ScrollView message_scroll;
    Button verify_otp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sendsms);
        emp_id=(EditText)findViewById(R.id.emp_id);
        verify=(Button)findViewById(R.id.verifyId);
        next=(TextView)findViewById(R.id.textView4);
        mobilenumber=(TextView)findViewById(R.id.smsnumber);
        message_data=(EditText)findViewById(R.id.message_data);
        send_message=(Button)findViewById(R.id.send_message);
        message_scroll=(ScrollView)findViewById(R.id.scroll);
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
        employee_id=prefs.getString("empid",null);
        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new Check_Internet().execute();
            }
        });


    }


    private  boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
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



    private class Check_Internet extends AsyncTask<Void,Void,Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO Auto-generated method stub
            check_internet = hasActiveInternetConnection(getApplicationContext());
            return check_internet;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            if (result) {
                SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(SendSms.this);
                value_id = emp_id.getText().toString();
                if (value_id != null && !value_id.equals(prefs.getString("empid",null))) {
                    new CheckNumber().execute(value_id);
                } else
                    Toast.makeText(getApplicationContext(), "Invalid id.", Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(getApplicationContext(), "Please check your internet connection", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }



    private class CheckNumber extends AsyncTask<String,Void,String>{
        String line,username;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String aVoid) {
            super.onPostExecute(aVoid);
            if(aVoid.equals("notfound")){
                emp_id.setError("This employee id is not valid");

                Toast.makeText(getApplicationContext(),"Please try another employee id",Toast.LENGTH_SHORT).show();
            }else{
                try{
                    Toast.makeText(getApplicationContext(),"Verified",Toast.LENGTH_SHORT).show();
                    JSONObject jObject=new JSONObject(aVoid);
                    username=jObject.getString("username");
                    mobile_number=jObject.getString("mobile_number");
                    mobilenumber.setVisibility(View.VISIBLE);

                    Animation animFadein = AnimationUtils.loadAnimation(getApplicationContext(),
                            R.anim.fadein);
                   final Animation animFadeout = AnimationUtils.loadAnimation(getApplicationContext(),
                            R.anim.fadeout);
                    final Animation topDown = AnimationUtils.loadAnimation(getApplicationContext(),
                            R.anim.topdown);



              //      mobilenumber.startAnimation(topDown);
               //     verify.startAnimation(animFadeout);
                //    emp_id.startAnimation(animFadeout);
                    verify.setVisibility(View.GONE);
                    emp_id.setVisibility(View.GONE);

                    mobilenumber.setText(username+" ("+value_id+") ");
                  //   message_data.setVisibility(View.VISIBLE);
                  //  message_data.startAnimation(animFadein);
                  //  send_message.setVisibility(View.VISIBLE);
                  //  send_message.startAnimation(animFadein);
                    message_scroll.setVisibility(View.VISIBLE);
                }catch (Exception e){

                }
                }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected String doInBackground(String... params) {
            try{
                String link="http://companytest.site88.net/attendance/check_sms.php";
                String data= URLEncoder.encode("emp_id","UTF-8")+"="+URLEncoder.encode(params[0],"UTF-8");
                URL url=new URL(link);
                URLConnection conn=url.openConnection();
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
                    break;
                }
                return line;
            }catch(Exception e){
                    Log.d("Exception sms",e.getMessage());
            }
            return null;
        }
    }

    public void sendMessage(View v) {
        Log.i("Send SMS", "");
        String phoneNo =mobile_number;
        String message = message_data.getText().toString();

        try {
            if(message!=null){
                new SendData().execute(employee_id,value_id,message);
                message_data.setText(null);
                emp_id.setText(null);
//              mobilenumber.setVisibility(View.GONE);
 //             message_data.setVisibility(View.GONE);
  //            send_message.setVisibility(View.GONE);
                message_scroll.setVisibility(View.GONE);
                emp_id.setVisibility(View.VISIBLE);
                verify.setVisibility(View.VISIBLE);

            }else
                Toast.makeText(getApplicationContext(), "Enter some text.", Toast.LENGTH_LONG).show();

        }catch (Exception e) {
            Toast.makeText(getApplicationContext(), "SMS failed, please try again.", Toast.LENGTH_LONG).show();
            Log.d("Exception",""+e.getMessage());
            e.printStackTrace();
        }
    }

    private class SendData extends AsyncTask<String,Void,String>{
        String line,refference;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String aVoid) {
            super.onPostExecute(aVoid);
            Toast.makeText(getApplicationContext(), "SMS sent to "+refference, Toast.LENGTH_LONG).show();
        }

        @Override
        protected String doInBackground(String... params) {
            try{
                refference=params[1];
                String link="http://companytest.site88.net/attendance/sendgateway.php";
                String data= URLEncoder.encode("eid","UTF-8")+"="+URLEncoder.encode(params[0],"UTF-8");
                data +="&"+URLEncoder.encode("receiver","UTF-8")+"="+URLEncoder.encode(params[1],"UTF-8");
                data +="&"+URLEncoder.encode("message","UTF-8")+"="+URLEncoder.encode(params[2],"UTF-8");
                URL url=new URL(link);
                URLConnection conn=url.openConnection();
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
                    break;
                }
                return line;
            }catch(Exception e){
                Log.d("Exception sms",e.getMessage());
            }
            return null;

        }
    }

}
