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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by NoName on 12/10/2015.
 */
public class Login extends Activity {

    EditText email,passwd;
    Button login;
    String id,password;
    String line = null;
	CheckBox terms;
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
	
	private class Check_Internet extends AsyncTask<Void,Void,Boolean>{

		@Override
		protected Boolean doInBackground(Void... params) {
			// TODO Auto-generated method stub
		 check_internet=hasActiveInternetConnection(getApplicationContext());
			return check_internet;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if(result){
			    id=email.getText().toString();
		        password=md5(passwd.getText().toString());
		    Boolean empty=true;//empty_passwd,email;

		        if (id.isEmpty()) {
		            Toast.makeText(getApplicationContext(),"Invalid Email Address !",Toast.LENGTH_SHORT).show();;
		            empty=false;

		        }
		            if(password.isEmpty()){
		            if(empty)
		            Toast.makeText(getApplicationContext(),"Enter Password",Toast.LENGTH_SHORT).show();;
		            empty=false;
		        }if(empty){

                  Retreive rr=new Retreive();
		        	rr.execute(id,password);
		            }
			}else{
				Toast.makeText(getApplicationContext(), "Please check your internet connection", Toast.LENGTH_SHORT).show();
				finish();
			}
		}

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }
    }

	private final Dialog newGameRequest() {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Alert!");
		builder.setMessage("Aww Snap! We are sorry that you forgot your password. Please send us an email with Subject as \"Forgot  Password\" to -- We\'ll help you with your password on your registered e-mail ID within 24hrs. Ciao!");

		builder.setPositiveButton("OK", new Dialog.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
			
				
//Toast.makeText(getApplicationContext(), "Here", Toast.LENGTH_SHORT).show();		
				}
			});

		return builder.create();
	}
	

    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        email=(EditText)findViewById(R.id.email);
        passwd=(EditText)findViewById(R.id.passwd);
        login=(Button)findViewById(R.id.button4);
		terms=(CheckBox)findViewById(R.id.checkBox);
       // login.setBackgroundColor(Color.TRANSPARENT);
      String username=getIntent().getStringExtra("username");
      String password=getIntent().getStringExtra("passwd");
      email.setText(username);
      passwd.setText(password);
      Button forgot_password=(Button)findViewById(R.id.forgotP);
	
      
      forgot_password.setOnClickListener(new View.OnClickListener() {
		
    	  
    	  
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			newGameRequest().show();
		}
	});
      //        email.setText(id);
  //      passwd.setText(password);
   }

    public void login(View v){
			if(terms.isChecked())
    		new Check_Internet().execute();
		else
			Toast.makeText(Login.this,"Please agree the terms and conditions.",Toast.LENGTH_SHORT).show();
		}


	public void showTerms(View v){

		AlertDialog.Builder dialog=new AlertDialog.Builder(Login.this);
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


	/////////////////////////////////////////////////////////
    private class Retreive extends AsyncTask<String, String, String> {
		String id;
        protected String doInBackground(String... params) {

            try{
                id = params[0];
                String password = params[1];
                System.out.println("Your ID"+id);
                System.out.println("Your Passwd"+password);
                String link="http://companytest.site88.net/attendance/login.php";
                String data  = URLEncoder.encode("eid", "UTF-8") + "=" + URLEncoder.encode(id, "UTF-8");
                data += "&" + URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(password, "UTF-8");

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



        protected void onPostExecute(String result) {
        	//Toast.makeText(getApplicationContext(), "Result !" +result, Toast.LENGTH_SHORT).show();
			progressDialog.cancel();
			if(result.equals("Invalid"))
                Toast.makeText(getApplicationContext(),"The password you entered is incorrect. Please try again.",Toast.LENGTH_SHORT).show();
            else if(result.equals("left")){
				Toast.makeText(getApplicationContext(),"Your OTP verification is pending. Please enter your OTP",Toast.LENGTH_SHORT).show();
				Intent start_otp=new Intent(Login.this,MobileVerification.class);
				startActivity(start_otp);
				finish();
			}
			else if(result.equals("Unregistered"))
                Toast.makeText(getApplicationContext(),"Unregistered user",Toast.LENGTH_SHORT).show();
			else{

				// 	boolean deep=Boolean.valueOf(user_name);
				//	String check_user=result[1];
				SharedPreferences prefs;
				prefs = PreferenceManager.getDefaultSharedPreferences(Login.this);
				Editor editor = prefs.edit();
				editor.putBoolean("login",true);
				editor.putString("empid",id);
				editor.putInt("departmentId",Integer.parseInt(result));
				editor.commit();
				Intent start_main=new Intent(Login.this,MainScreen.class);
				startActivity(start_main);
				finish();
				//	check_status(result);
			}
        }



		ProgressDialog progressDialog;
		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			progressDialog=new ProgressDialog(Login.this);
			progressDialog.setCanceledOnTouchOutside(false);
			progressDialog.setMessage("Please Wait");

			if(!progressDialog.isShowing()){
				progressDialog.show();
			}
		}

        protected void onProgressUpdate(Void... values) {
        }
    }

	@Override
	protected void onStart() {
		super.onStart();
		SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);
		boolean check_login=prefs.getBoolean("login",false);
		if(check_login){

			Intent i = new Intent(this,MainScreen.class);
			startActivity(i);
			finish();
		}
	}

	public void RegisterPage(View view){
		Intent i = new Intent(this,RegisterActivity.class);
		startActivity(i);
		finish();
	}

}