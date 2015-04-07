package com.example.tagidentification;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.Display;
import android.view.WindowManager;
import android.widget.ImageView;

@SuppressLint("DrawAllocation") public class DrawImageView extends ImageView {
	
	 public DrawImageView(Context context, AttributeSet attrs) {  
	        super(context, attrs);  
	        // TODO Auto-generated constructor stub  
	    }  
	      
	    Paint paint = new Paint();  
	    {  
	        paint.setAntiAlias(true);  
	        paint.setColor(Color.GREEN);  
	        paint.setStyle(Style.STROKE);  
	        paint.setStrokeWidth(2.5f);//线宽
	        paint.setAlpha(255);  //透明
	    };  
	      
	    private CameraActivity mCameraActivity;
	    @Override  
	    protected void onDraw(Canvas canvas) {  
	        // TODO Auto-generated method stub  
	        super.onDraw(canvas);  
	        System.out.println(" mCameraActivity.mScreenWidth:"+ mCameraActivity.mScreenWidth);
			canvas.drawRect(new Rect(0, 0, mCameraActivity.mScreenWidth,mCameraActivity.mScreenWidth), paint);//���ƾ���Ϊ���surfaceView  
//			canvas.drawRect(new Rect(330,440, 600,700), paint);
	    }  
	      
}
