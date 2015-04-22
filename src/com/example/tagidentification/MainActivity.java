package com.example.tagidentification;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
//MainActivity暂时没用到
public class MainActivity extends Activity {
	ImageButton soso;
	ImageButton chi;
	RelativeLayout is;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        soso = (ImageButton)findViewById(R.id.soso);
        soso = (ImageButton)findViewById(R.id.soso);
        chi = (ImageButton)findViewById(R.id.chi);
        is = (RelativeLayout)findViewById(R.id.is);
        is.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(MainActivity.this,CameraActivity.class);
				 startActivity(intent);
			}
        	
        });
        chi.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(MainActivity.this,CameraActivity.class);
				 startActivity(intent);
			}
        	
        });
        soso.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				 Intent intent = new Intent(MainActivity.this,CameraActivity.class);
				 startActivity(intent);
				 
			}
        	
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
}
