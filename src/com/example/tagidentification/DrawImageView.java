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
	        paint.setStrokeWidth(7.5f);//线宽
	        paint.setAlpha(255);  //线的透明度，不透明
//	        paint.setShadowLayer(5, 3, 3, 0xFFFF0000);
	        
	       
	    };  
	    
	    //绘制灰色区域
	    Paint  mAreaPaint = new Paint();
	    {
	    		mAreaPaint = new Paint(Paint.ANTI_ALIAS_FLAG);  
		        mAreaPaint.setColor(Color.GRAY);  
		        mAreaPaint.setStyle(Style.FILL);  
		        mAreaPaint.setAlpha(150); //暂定为灰色不透明 
	    };
	      
	    private CameraActivity mCameraActivity;
	    private int yTop = mCameraActivity.mScreenWidth+mCameraActivity.top;
	    @Override  
	    protected void onDraw(Canvas canvas) {  
	        // TODO Auto-generated method stub  
	        super.onDraw(canvas);  
	        System.out.println(" mCameraActivity.mScreenWidth:"+ mCameraActivity.mScreenWidth);
			canvas.drawRect(new Rect(0, mCameraActivity.top, mCameraActivity.mScreenWidth,mCameraActivity.mScreenWidth+mCameraActivity.top), paint);//���ƾ���Ϊ���surfaceView  
			canvas.drawRect(new Rect(0,mCameraActivity.mScreenWidth+mCameraActivity.top, 
					mCameraActivity.mScreenWidth,mCameraActivity.mScreenHeight), mAreaPaint);
			canvas.drawRect(new Rect(0,0, 
					mCameraActivity.mScreenWidth,mCameraActivity.top), mAreaPaint);
	    }  
	      
}
