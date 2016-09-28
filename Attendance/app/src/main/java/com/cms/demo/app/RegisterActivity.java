package com.cms.demo.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.json.JSONObject;

public class RegisterActivity extends Activity {
	String smail,spass,suser,mobile;
	EditText edInitial,user,pass,user_name,mail,emp_title,mobileno;
    RadioButton maleRadioButton, femaleRadioButton;
    RelativeLayout formLayout,validateForm;
    Button submit;
    CheckBox terms;
    String empId;


    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.scroll_register);
		user=(EditText)findViewById(R.id.eduser);
		pass=(EditText)findViewById(R.id.edpass);
		user_name=(EditText)findViewById(R.id.emp_username);
        edInitial=(EditText)findViewById(R.id.edInitial);
		mail=(EditText)findViewById(R.id.edmail);
        emp_title=(EditText)findViewById(R.id.emp_title);
        mobileno=(EditText)findViewById(R.id.mobile_number);
        maleRadioButton = (RadioButton) findViewById(R.id.radioMale);
        femaleRadioButton = (RadioButton) findViewById(R.id.radioFemale);
        submit=(Button)findViewById(R.id.btnreg);
        terms=(CheckBox)findViewById(R.id.checkBox);
        formLayout=(RelativeLayout)findViewById(R.id.formLayout);
        validateForm=(RelativeLayout)findViewById(R.id.validateForm);
        maleRadioButton.setEnabled(false);
        femaleRadioButton.setEnabled(false);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    new Check_Internet().execute("store");
            }
        });



	}

    public void validateEmployee(View v){
        new Check_Internet().execute("onStart");
    }
	



	String username,title;

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

    private class Check_Internet extends AsyncTask<String,Void,Boolean>{
        String identifier;
        @Override
        protected Boolean doInBackground(String... params) {
            // TODO Auto-generated method stub
            identifier=params[0];
            check_internet=hasActiveInternetConnection(getApplicationContext());
            return check_internet;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            if (result) {


                if (identifier.equals("store")) {
                    suser=user.getText().toString(); //suser=employeeid
                    spass=md5(pass.getText().toString());
                    username=user_name.getText().toString();
                    smail=mail.getText().toString();
                    title=emp_title.getText().toString();
                    mobile=mobileno.getText().toString();
                    if(suser.trim().isEmpty() || spass.trim().isEmpty() || smail.trim().isEmpty() || username.trim().isEmpty() || title.trim().isEmpty() || mobile.trim().isEmpty() || !terms.isChecked()){
                        Toast.makeText(RegisterActivity.this, "Please Fill All The Fields", Toast.LENGTH_LONG).show();
                    }else{
                        if(Patterns.EMAIL_ADDRESS.matcher(smail).matches()){
                            if(Patterns.PHONE.matcher(mobile).matches() && mobile.length()==10){
                                String radio=null;
                                if(maleRadioButton.isChecked())
                                    radio="Male";
                                else
                                    radio="Female";

                                new Retreive().execute(suser,"store",spass,smail,mobile,title,radio);

                            }else
                                mobileno.setError("Invalid mobile number");

                        }else{

                            mail.setError("Invalid email address");
                            Toast.makeText(getApplicationContext(),"Invalid Email Address",Toast.LENGTH_SHORT).show();

                        }
                    }
                }else{
                    if(!edInitial.getText().toString().isEmpty()){
                     empId=edInitial.getText().toString();
                    new Retreive().execute(edInitial.getText().toString(),"onStart");
                    }else
                        Toast.makeText(getApplicationContext(), "Please enter a valid Employee ID", Toast.LENGTH_SHORT).show();

                }
                } else {
                Toast.makeText(getApplicationContext(), "Please check your internet connection", Toast.LENGTH_SHORT).show();
                finish();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }
    }

        public void showDialog(View v){

            AlertDialog.Builder dialog=new AlertDialog.Builder(RegisterActivity.this);
            dialog.setTitle("Terms and condition");

            dialog.setMessage("                Terms & Condition, Disclaimer\n" +
                    "Acceptance of the terms\n" +
                    "Any user logging on navigating on this App automatically agree to comply with and be bound by the following terms and conditions of use, which govern the relationship with user in relation to this app. All users are presumed to have read, understood and agreed to terms and conditions of this app.\t\n" +
                    "This disclaimer will be governed by and construed in accordance with Indian law, and any disputes relating to this disclaimer will be subject to the jurisdiction of the courts at Mumbai. The user warrants that he/she is fully aware of the laws of India.\n" +
                    "Vedang can alter or revoke any terms of conditions of this app without notice. The revised disclaimer will apply to the use of our website from the date of the publication. Please check this page regularly to ensure you are familiar with the current version.\n" +
                    "Information\n" +
                    "In no event will vedang be liable for any loss or direct or indirect damages arising from the use of or reliance on this app. Your use of any information or material on this app is entirely at your own risk, for which Vedang shall not be liable. It shall be your own responsibility to ensure that any products, services or information available through this website meet your specific requirements.\n" +
                    "The information on this app does not constitute an offer and / or contract of any such type between the owner/developer and the user.\n" +
                    "This app contains material which is owned by or licensed by vedang and is protected by copyright and intellectual Telecom laws. This material includes, but is not limited to, the design, layout, look, appearance, text and graphics. Reproduction is prohibited.\n" +
                    "\n" +
                    "Vedang has the right to alter the information of this app and is under no obligation to communicate any changes.\n" +
                    "Information From the user\n" +
                    "The user, by the act of logging onto the app or submitting information through the app or email has consented and expressly and irrevocably authorized vedang to use,  analyze, transmit or display all information and documents as may be required by it. The user warrants that he has provided true, accurate, current and complete information. By entering a mobile number, user had given consent to vedang to send alerts, promotional SMS and the permission to be contacted.\n" +
                    "In case the user does not wants to receive information from Vedang he will have to communicate to this effect to Vedang by sending an email to feedback @vedangradio.com\n" +
                    "Security\n" +
                    "Every effort is made to keep the app operative. However vedang takes no responsibility, and will not be liable for, the app or any of its components being temporarily unavailable due to technical issues beyond its control.\n" +
                    "Vedang does not warrant that this site will be free of viruses or other harmful components. This app and emails from Vedang are vulnerable to data corruption, interception, tampering, viruses as well as delivery error and the user accepts liability for any consequences that may arise from them.\n" +
                    "About Vedang projects\n" +
                    "The designs, plans, specifications, facilities, images, features, etc. Shown  on this app are only indicative and subject to the approval of this respective authorities and the owner/developer reserves the right to change the specification or feature or all these in the interest of a phase wise development or under a scheme without any notice or objections.\n" +
                    "Responsibilities\n" +
                    "Vedang or any of its employees, managers or representatives shall not be responsible for any consequential damage or economic loss arising or related to the use of the app.\n" +
                    "The app may contain links to other websites which are not under control of vedang. Any link to off-site pages or other may be accessed by the user at his own risk.  Vedang has no control over the nature, content and availability of those sites. The inclusion of any links does not necessarily imply a recommendation or endorsement of the views within them. It is the responsibility of the user to examine the copyright and licensing restrictions of linked pages and to secure all necessary permissions.\n" +
                    "Popup Advertisements\n" +
                    "When using our app, your app may produce pop-up advertisements. These advertisements are most likely produced by other web sites or by third party software installed on your devise. Vedang does not endorse or recommend products or services for which user may view a pop-up advertisement on your device while using this app.\n" +
                    "\n" +
                    " \n" +
                    "\n" +
                    "\n" +
                    "\n" +
                    "\n" +
                    "                \n");
            dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            dialog.show();
        }


    public void showTerms(View v){

        AlertDialog.Builder dialog=new AlertDialog.Builder(RegisterActivity.this);
        dialog.setTitle("Terms and condition");

        dialog.setMessage("                Terms & Condition, Disclaimer\n" +
                "Acceptance of the terms\n" +
                "Any user logging on navigating on this App automatically agree to comply with and be bound by the following terms and conditions of use, which govern the relationship with user in relation to this app. All users are presumed to have read, understood and agreed to terms and conditions of this app.\t\n" +
                "This disclaimer will be governed by and construed in accordance with Indian law, and any disputes relating to this disclaimer will be subject to the jurisdiction of the courts at Mumbai. The user warrants that he/she is fully aware of the laws of India.\n" +
                "Vedang can alter or revoke any terms of conditions of this app without notice. The revised disclaimer will apply to the use of our website from the date of the publication. Please check this page regularly to ensure you are familiar with the current version.\n" +
                "Information\n" +
                "In no event will vedang be liable for any loss or direct or indirect damages arising from the use of or reliance on this app. Your use of any information or material on this app is entirely at your own risk, for which Vedang shall not be liable. It shall be your own responsibility to ensure that any products, services or information available through this website meet your specific requirements.\n" +
                "The information on this app does not constitute an offer and / or contract of any such type between the owner/developer and the user.\n" +
                "This app contains material which is owned by or licensed by vedang and is protected by copyright and intellectual Telecom laws. This material includes, but is not limited to, the design, layout, look, appearance, text and graphics. Reproduction is prohibited.\n" +
                "\n" +
                "Vedang has the right to alter the information of this app and is under no obligation to communicate any changes.\n" +
                "Information From the user\n" +
                "The user, by the act of logging onto the app or submitting information through the app or email has consented and expressly and irrevocably authorized vedang to use,  analyze, transmit or display all information and documents as may be required by it. The user warrants that he has provided true, accurate, current and complete information. By entering a mobile number, user had given consent to vedang to send alerts, promotional SMS and the permission to be contacted.\n" +
                "In case the user does not wants to receive information from Vedang he will have to communicate to this effect to Vedang by sending an email to feedback @vedangradio.com\n" +
                "Security\n" +
                "Every effort is made to keep the app operative. However vedang takes no responsibility, and will not be liable for, the app or any of its components being temporarily unavailable due to technical issues beyond its control.\n" +
                "Vedang does not warrant that this site will be free of viruses or other harmful components. This app and emails from Vedang are vulnerable to data corruption, interception, tampering, viruses as well as delivery error and the user accepts liability for any consequences that may arise from them.\n" +
                "About Vedang projects\n" +
                "The designs, plans, specifications, facilities, images, features, etc. Shown  on this app are only indicative and subject to the approval of this respective authorities and the owner/developer reserves the right to change the specification or feature or all these in the interest of a phase wise development or under a scheme without any notice or objections.\n" +
                "Responsibilities\n" +
                "Vedang or any of its employees, managers or representatives shall not be responsible for any consequential damage or economic loss arising or related to the use of the app.\n" +
                "The app may contain links to other websites which are not under control of vedang. Any link to off-site pages or other may be accessed by the user at his own risk.  Vedang has no control over the nature, content and availability of those sites. The inclusion of any links does not necessarily imply a recommendation or endorsement of the views within them. It is the responsibility of the user to examine the copyright and licensing restrictions of linked pages and to secure all necessary permissions.\n" +
                "Popup Advertisements\n" +
                "When using our app, your app may produce pop-up advertisements. These advertisements are most likely produced by other web sites or by third party software installed on your devise. Vedang does not endorse or recommend products or services for which user may view a pop-up advertisement on your device while using this app.");
                dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
                });
        dialog.show();
    }

    public static final String md5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    private class Retreive extends AsyncTask<String, String, String> {
        String line,emp_id;
        String number,mobile;
        protected String doInBackground(String... params) {
            try{
                emp_id = params[0];

                String link="http://companytest.site88.net/attendance/data.php";
                String data  = URLEncoder.encode("emp_id", "UTF-8") + "=" + URLEncoder.encode(emp_id, "UTF-8");
                data += "&" + URLEncoder.encode("identifier", "UTF-8") + "=" + URLEncoder.encode(params[1], "UTF-8");
                if(params[1].equals("store")){

                    String password = params[2];
                    String email=params[3];
                    mobile=params[4];

                    number=generateOTP(4);
                    data += "&" + URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(password, "UTF-8");
                    data += "&" + URLEncoder.encode("email", "UTF-8") + "=" + URLEncoder.encode(email, "UTF-8");
                    data+="&"+URLEncoder.encode("mobile","UTF-8")+"="+URLEncoder.encode(params[4],"UTF-8");
                    data+="&"+URLEncoder.encode("title","UTF-8")+"="+URLEncoder.encode(params[5],"UTF-8");
                    data+="&"+URLEncoder.encode("gender","UTF-8")+"="+URLEncoder.encode(params[6],"UTF-8");
                    data+="&"+URLEncoder.encode("otp","UTF-8")+"="+URLEncoder.encode(number,"UTF-8");
                }
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



        protected void onPostExecute(String result) {

                progressDialog.cancel();
               if(result.equals("registered")){
                     Toast.makeText(getApplicationContext(), "No records found with this employee id, Please try again !" , Toast.LENGTH_SHORT).show();

               }else if (result.equals("error")){

                    Toast.makeText(getApplicationContext(), "Successfully Registered !" , Toast.LENGTH_SHORT).show();
				// result="NO";
				    Intent menu=new Intent(RegisterActivity.this,MobileVerification.class);
				    menu.putExtra("mobileno", mobile);
                    startActivity(menu);
                    SharedPreferences prefs;
                    prefs = PreferenceManager.getDefaultSharedPreferences(RegisterActivity.this);
                   SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("empid",emp_id);
                    editor.putString("username",username);
                    editor.putString("otp",number);
                    editor.putString("number",mobile);
                    editor.commit();
            	    finish();
                    }else if(result.equals("invalid")){
                               Toast.makeText(getApplicationContext(),"Please enter proper details",Toast.LENGTH_SHORT).show();
                   }else if(result.equals("mobile"))
                            Toast.makeText(getApplicationContext(),"Mobile number already registered",Toast.LENGTH_SHORT).show();
                   else if(result.equals("email"))
                   Toast.makeText(getApplicationContext(),"Email id already registered",Toast.LENGTH_SHORT).show();
               else{
                   try{
                       validateForm.setVisibility(View.GONE);
                       formLayout.setVisibility(View.VISIBLE);
                       submit.setText("Register");
                       JSONObject object=new JSONObject(result);
                       user.setText(empId);
                        user_name.setText(object.getString("username"));
                        mail.setText(object.getString("email"));
                        emp_title.setText(object.getString("title"));
                        mobileno.setText(object.getString("mobile"));
                        if(object.getString("sex").equals("Male"))
                            maleRadioButton.setChecked(true);
                       else
                           femaleRadioButton.setChecked(true);
                       mobileno.setEnabled(true);
                       mail.setEnabled(true);
                       pass.setEnabled(true);
                       maleRadioButton.setEnabled(true);
                       femaleRadioButton.setEnabled(true);
                       emp_title.setEnabled(true);
                   }catch (Exception e){

                   }
               }

        	//Toast.makeText(getApplicationContext(), "Result !" +result, Toast.LENGTH_SHORT).show();


   }

        ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog=new ProgressDialog(RegisterActivity.this);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setMessage("Please Wait");

            if(!progressDialog.isShowing()){
                progressDialog.show();
            }
        }

        protected void onProgressUpdate(Void... values) {
        }
    }
	
	
}
