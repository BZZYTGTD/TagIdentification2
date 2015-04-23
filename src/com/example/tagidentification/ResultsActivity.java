package com.example.tagidentification;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.widget.TextView;

public class ResultsActivity extends Activity {
	
	String outputPath;
	TextView tv;
	private String resultUrl2 = "result2.txt";
	private CameraActivity mCameraActivity;
	
	public native int processFileNative();
	static {
		try {
			System.loadLibrary("processtxt");
		} catch (UnsatisfiedLinkError ule) {
			
		}
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.result);
		tv = (TextView)findViewById(R.id.textView1);
		String imageUrl = "unknown";
		
		Bundle extras = getIntent().getExtras();
		if( extras != null) {
			imageUrl = extras.getString("IMAGE_PATH" );
			outputPath = extras.getString( "RESULT_PATH" );
		}
		
		// Starting recognition process
		new AsyncProcessTask(this).execute(imageUrl, outputPath);

		
		
		
	}

	public void updateResults(Boolean success) {
		if (!success)
			return;
		try {
			processFileNative();
			StringBuffer contents = new StringBuffer();

//			FileInputStream fis = openFileInput(outputPath);
			FileInputStream fis = openFileInput(resultUrl2);
			
			try {
				Reader reader = new InputStreamReader(fis, "UTF-8");
				BufferedReader bufReader = new BufferedReader(reader);
				String text = null;
				while ((text = bufReader.readLine()) != null) {
					contents.append(text).append(System.getProperty("line.separator"));
					System.out.println("contents.toString():"+ contents.toString());
				}
				
			} finally {
				fis.close();
			}
			
			displayMessage(contents.toString());
		} catch (Exception e) {
			displayMessage("Error: " + e.getMessage());
		}
	}
	
	public void displayMessage( String text )
	{
		
		tv.post( new MessagePoster( text ) );
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_results, menu);
		return true;
	}

	class MessagePoster implements Runnable {
		public MessagePoster( String message )
		{
			_message = message;
		}

		public void run() {
			tv.append( _message + "\n" );
//			setContentView( tv );
		}

		private final String _message;
	}
	@Override  
	public boolean onKeyDown(int keyCode, KeyEvent event) {  
	    if(event.getAction() == KeyEvent.ACTION_DOWN && KeyEvent.KEYCODE_BACK == keyCode) {    
	    	        	System.exit(0);
        return true;    
	    }    
	    return super.onKeyDown(keyCode, event);    
	}
	
	
}
