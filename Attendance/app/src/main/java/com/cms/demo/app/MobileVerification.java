package com.cms.demo.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
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
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Created by NoName on 5/9/2016.
 */
public class MobileVerification extends Activity {
    EditText otp_verify;
    TextView mobile,otp_text;
    String mobile_verify;
    int number;
    String emp_id;
    Animation animFadein,animFadeout;
    Button confirm;
    int pos[]=new int[2];
     Animation a=null;
     ImageButton edit_number,next;
    Button otp_button;
    String intent_mobile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mobile_activity);

        mobile = (TextView) findViewById(R.id.mobile);
        otp_verify = (EditText) findViewById(R.id.otp_verify);
        otp_button = (Button) findViewById(R.id.otp_button);
        edit_number = (ImageButton) findViewById(R.id.editNumber);
        otp_text = (TextView) findViewById(R.id.otp_text);

        confirm = (Button) findViewById(R.id.confirm_number);
        next=(ImageButton)findViewById(R.id.confirm);

        animFadein = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.fadein);

        animFadeout = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.fadein);
        mobile.startAnimation(animFadein);
        mobile.clearAnimation();
        SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);
        mobile.setText(prefs.getString("number",null));
        edit_number.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edit_number.setVisibility(View.INVISIBLE);
                edit_number.setClickable(false);
                mobile.setFocusableInTouchMode(true);
                otp_text.setText("An OTP will be sent to the above number, Please enter the OTP here");
//                next.startAnimation(animFadein);
  //              next.clearAnimation();
                next.setVisibility(View.VISIBLE);
                next.setClickable(true);
                otp_verify.setVisibility(View.INVISIBLE);
                otp_verify.setVisibility(View.INVISIBLE);
                otp_button.setVisibility(View.INVISIBLE);
                otp_button.setClickable(false);

            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                data=mobile.getText().toString();

                String randomNum=generateOTP(4); //String.valueOf(number);
                mobile_verify=randomNum;
                Log.d("Code",""+randomNum);

                new UpdateData().execute(emp_id,data,randomNum);

            }
        });


    }

    String data;

    public static String generateOTP(int otpLengthNumber){
        String otp = new String();
        int otpSample=0;
        for(int i=0;i<otpLengthNumber;i++){
            otp=otp+"9";
        }

        otpSample=Integer.parseInt(otp);

        SecureRandom prng;
        try {
            prng = SecureRandom.getInstance("SHA1PRNG"); //Number Generation Algorithm
            otp = new Integer(prng.nextInt(otpSample)).toString();
            otp = (otp.length() < otpLengthNumber) ? padleft(otp, otpLengthNumber, '0') : otp;
        } catch (NoSuchAlgorithmException e) {
        }

//        If generated OTP exists in DB -regenerate OTP again
        boolean otpExists=false;
        if (otpExists) {
            generateOTP(otpLengthNumber);
        } else {
            return otp;
        }
        return otp;
    }

    private static String padleft(String s, int len, char c) { //Fill with some char or put some logic to make more secure
        s = s.trim();
        StringBuffer d = new StringBuffer(len);
        int fill = len - s.length();
        while (fill-- > 0)
            d.append(c);
        d.append(s);
        return d.toString();
    }

    private class UpdateData extends AsyncTask<String,Void,String>{
        String line;ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog=new ProgressDialog(MobileVerification.this);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setMessage("Please Wait");

            if(!progressDialog.isShowing()){
                progressDialog.show();
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressDialog.cancel();
            if(s.equals("registered")){
                Toast.makeText(getApplicationContext(),"Mobile number already registered. Please re-enter a unregistered number ",Toast.LENGTH_SHORT).show();

            }else{
            mobile_verify=s;

                Toast.makeText(getApplicationContext(),"Details updated, Enter the sent OTP ",Toast.LENGTH_SHORT).show();
                edit_number.setVisibility(View.VISIBLE);
                SharedPreferences prefs;
                prefs = PreferenceManager.getDefaultSharedPreferences(MobileVerification.this);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("number",data);
                editor.commit();
                otp_text.setText("An OTP has been sent to the above number, Please enter the OTP here");
                otp_button.setVisibility(View.VISIBLE);
                otp_button.setClickable(true);
                otp_verify.setVisibility(View.VISIBLE);
                        next.setVisibility(View.INVISIBLE);
                        next.setClickable(false);

                mobile.setFocusable(false);
                edit_number.setClickable(true);
            }

        }

        @Override
        protected String doInBackground(String... params) {

            try{
                String link="http://companytest.site88.net/attendance/otp_update.php";
                String data  = URLEncoder.encode("eid", "UTF-8") + "=" + URLEncoder.encode(params[0], "UTF-8");
                data += "&" + URLEncoder.encode("numbeer", "UTF-8") + "=" + URLEncoder.encode(params[1], "UTF-8");
                data += "&" + URLEncoder.encode("code", "UTF-8") + "=" + URLEncoder.encode(params[2], "UTF-8");

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
                System.out.println("Your ID"+e.getMessage());
            }
            //   return_rec();
            //    Boolean status=Boolean.valueOf(line);
            //  Log.d("Receiving", "" + status);

            return line;

        }
    }


    public void verifyOtp(View v){

            new Check_Internet().execute("update");
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



    private class Check_Internet extends AsyncTask<String,Void,Boolean> {

        String identifier;
        @Override
        protected Boolean doInBackground(String... params) {
            // TODO Auto-generated method stub
            identifier=params[0];
            check_internet = hasActiveInternetConnection(getApplicationContext());
            return check_internet;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            if (result) {

                if(identifier.equals("update")){
                    String number=otp_verify.getText().toString();
                    if(mobile_verify.equals(number)){
                        Toast.makeText(getApplicationContext(),"OTP Verified",Toast.LENGTH_SHORT).show();
                        new CheckOtp().execute("update");
                    }else{
                        Toast.makeText(getApplicationContext(),"Invalid OTP",Toast.LENGTH_SHORT).show();
                    }
                }else if(identifier.equals("select")){
                    new CheckOtp().execute("select");
                }
            } else {
                Toast.makeText(getApplicationContext(), "Please check your internet connection", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }



    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        emp_id = prefs.getString("empid", null);
        new Check_Internet().execute("select");

    }

    private class CheckOtp extends AsyncTask<String,Void,String>{
        String line,identifier;ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog=new ProgressDialog(MobileVerification.this);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setMessage("Please Wait");

            if(!progressDialog.isShowing()){
                progressDialog.show();
            }
        }

        @Override
        protected void onPostExecute(String aVoid) {
            super.onPostExecute(aVoid);
            mobile_verify=aVoid;

            progressDialog.cancel();
            if(identifier.equals("update")){

                SharedPreferences prefs;
                prefs = PreferenceManager.getDefaultSharedPreferences(MobileVerification.this);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("login",true);
                editor.commit();
                Intent start_main=new Intent(MobileVerification.this,MainScreen.class);
                startActivity(start_main);
                finish();
            }else if(identifier.equals("get")){
                mobile.setText(aVoid);
                intent_mobile=aVoid;
            }

        }

        @Override
        protected String doInBackground(String... params) {

            try{
                identifier=params[0];
            String link="http://companytest.site88.net/attendance/otp.php";
            String data  = URLEncoder.encode("emp_id", "UTF-8") + "=" + URLEncoder.encode(emp_id, "UTF-8");
             data  +=  "&" +URLEncoder.encode("identifier", "UTF-8") + "=" + URLEncoder.encode(params[0], "UTF-8");
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
                //     return sb.toString();
            }
            catch(Exception e){
                Log.d("Exception",""+e.getMessage());
            }
            //   return_rec();
            //    Boolean status=Boolean.valueOf(line);
            //  Log.d("Receiving", "" + status);

            return line;
        }
    }
}
