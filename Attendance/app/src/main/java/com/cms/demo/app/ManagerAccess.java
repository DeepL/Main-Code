package com.cms.demo.app;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by NoName on 7/21/2016.
 */
public class ManagerAccess extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manager_access);
        TextView text=(TextView)findViewById(R.id.alert);
        String link="<a href='http://companytest.site88.net'>app.vedangradio.com</a>";
     //   text.setMovementMethod(LinkMovementMethod.getInstance());
        String display= "\n" +
                "Vedang Android App has been discontinued.\n" +
                "\n" +
                "Please use http://companytest.site88.net for Vedang attendance and inventory\n" +
                "\n" +
                "Username : your employee id\n" +
                "Default password : 123456\n" +
                "\n" +
                "If you have already submitted assets, you don't need to submit it again. It will be displayed on inventory page\n" +
                "\n" +
                "If you have already verified your mobile number, you don't need to verify again.";
        text.setText(display);
        Button b=(Button)findViewById(R.id.button);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri=Uri.parse("http://companytest.site88.net");
                Intent intent=new Intent(Intent.ACTION_VIEW,uri);
                startActivity(intent);
            }
        });
    }
}
