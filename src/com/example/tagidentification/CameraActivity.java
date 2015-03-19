package com.example.tagidentification;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.media.MediaRecorder;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class CameraActivity extends Activity implements 
	Callback, OnClickListener, AutoFocusCallback{

	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;

	private int mNumberOfCameras;
    private int mCameraCurrentlyLocked;

    // The first rear facing camera
    private int mDefaultCameraId;

    private int mScreenWidth, mScreenHeight;
    private int mFocusLeft, mFocusTop, mFocusWidth, mFocusHeight;
    public static final String TAG = "mmmm";
	private Camera mCamera;
//    private CameraPreview mPreview;
    private MediaRecorder mMediaRecorder;
    long waitTime = 2000;    
	long touchTime = 0;  
	private int checkItem;
	Button captureButton;
	private boolean isRecording = false;
	
	private Camera.AutoFocusCallback mAutoFocusCallback;
	private SurfaceHolder mHolder;
	Camera.Parameters params;
	Size mPreviewSize;
	List<Size> mSupportedPreviewSizes;
	Bitmap bitmap;
	DrawCaptureRect mDraw;
	private boolean frameFocus = false;
	SurfaceView preview;
	LinearLayout linearrLayout01;
	
	
	private static final int RESULT_REQUEST_CODE = 2;
	private static final int CAMERA_REQUEST_CODE = 1;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.preview);
		
		//设置屏幕方向为竖屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        
		
		 // 得到屏幕的大小
        WindowManager wManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = wManager.getDefaultDisplay();
        mScreenHeight = display.getHeight();//1800
        mScreenWidth = display.getWidth();//1080
        mFocusTop = mFocusLeft = (mScreenWidth * 3) / 8;
        mFocusWidth = mFocusHeight = mScreenWidth / 4;
        
//        mSupportedPreviewSizes = mCamera.getParameters()
//                .getSupportedPreviewSizes();
        // Create our Preview view and set it as the content of our activity.
//        mPreview = new CameraPreview(this, mCamera);
//        mPreview.setFocusable(true);  
//        mPreview.setOnTouchListener(new OnTouchListener(){
//
//			@Override
//			public boolean onTouch(View v, MotionEvent event) {
//				// TODO Auto-generated method stub
//				mPreview.focusOnTouch(event);
//				return true;
//			}
//        	
//        });
       
        linearrLayout01 = (LinearLayout)findViewById(R.id.linearLayout01);
         preview = (SurfaceView) findViewById(R.id.camera_preview);
         /**用来设置surfaceview的大小*/
         LinearLayout.LayoutParams layoutParams = 
        		 new LinearLayout.LayoutParams(mScreenWidth, mScreenWidth);
         preview.setLayoutParams(layoutParams);
         preview.setOnClickListener(this);
         //获得句柄  
         mHolder = preview.getHolder(); 
         //添加回调  
         mHolder.addCallback(this); 
//         mHolder.setFixedSize(1280, 720);// 设置分辨率 
         //设置类型  
         mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);  
       
         
         mDraw = new DrawCaptureRect(CameraActivity.this,
        		 mFocusLeft,mFocusTop,mFocusWidth,mFocusHeight);
         
         
        captureButton = (Button) findViewById(R.id.button_capture);
        captureButton.setOnClickListener(this);
        
        // 得到默认的相机ID
        mDefaultCameraId = getDefaultCameraId();
        mCameraCurrentlyLocked = mDefaultCameraId;
//        System.out.println("mCameraCurrentlyLocked "+mCameraCurrentlyLocked);
	}

	 //双击退出
	@Override  
	public boolean onKeyDown(int keyCode, KeyEvent event) {  
	    if(event.getAction() == KeyEvent.ACTION_DOWN && KeyEvent.KEYCODE_BACK == keyCode) {    
//	        long currentTime = System.currentTimeMillis();    
//	        if((currentTime-touchTime)>=waitTime) {    
//	          //  Toast的显示时间和等待时间相同  
//	            Toast.makeText(this, "再按一次退出", (int)waitTime).show();    
//	            touchTime = currentTime;    
//	        }else { 
//	        	mCamera.setPreviewCallback(null) ;
//	        	mCamera.stopPreview();
//	        
//	        	mCamera.release();
//	        	mCamera = null;
//	        	System.out.println("camera release!");
//	        	System.exit(0);
//	        }   
	    	mCamera.setPreviewCallback(null) ;
        	mCamera.stopPreview();
        
        	mCamera.release();
        	mCamera = null;
        	System.out.println("camera release!");
        	System.exit(0);
        return true;    
	    }    
	    return super.onKeyDown(keyCode, event);    
	}
	
	
	
//实现焦点框的画图
	class DrawCaptureRect extends View
    {
	     private int mcolorfill;
	     private int mleft, mtop, mwidth, mheight;
	     public DrawCaptureRect(Context context,int left, int top,        
	    		 int width, int height) {
		  		super(context);
			   // TODO Auto-generated constructor stub
//			   this.mcolorfill = colorfill;
			   this.mleft = left;
			   this.mtop = top;
			   this.mwidth = width;
			   this.mheight = height;	
		  }
		  @Override
		  protected void onDraw(Canvas canvas) {
		   // TODO Auto-generated method stub
			   Paint mpaint = new Paint();
			   mpaint.setColor(Color.WHITE);
			   mpaint.setStyle(Paint.Style.FILL);
			   mpaint.setStrokeWidth(1.0f);
			   canvas.drawLine(mleft, mtop, mleft+mwidth, mtop, mpaint);
			   canvas.drawLine(mleft+mwidth, mtop, mleft+mwidth, mtop+mheight, mpaint);
			   canvas.drawLine(mleft, mtop, mleft, mtop+mheight, mpaint);
			   canvas.drawLine(mleft, mtop+mheight, mleft+mwidth, mtop+mheight, mpaint);
			   super.onDraw(canvas); 
		  }
                      
    }

	private int getDefaultCameraId()
	    {
	        int defaultId = -1;

	        // Find the total number of cameras available
	        mNumberOfCameras = Camera.getNumberOfCameras();

	        // Find the ID of the default camera
	        CameraInfo cameraInfo = new CameraInfo();
	        for (int i = 0; i < mNumberOfCameras; i++)
	        {
	            Camera.getCameraInfo(i, cameraInfo);
	            if (cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK)
	            {
	                defaultId = i;
	                System.out.println("defaultId " +defaultId);
	               
	            }
	        }
	        if (-1 == defaultId)
	        {
	            if (mNumberOfCameras > 0)
	            {
	                // 如果没有后向摄像头
	                defaultId = 0;
	            }
	            else
	            {
	                // 没有摄像头
	                Toast.makeText(getApplicationContext(), R.string.no_camera,
	                        Toast.LENGTH_LONG).show();
	            }
	        }
	        return defaultId;
	    }
	 
	 
	/** Check if this device has a camera */
	private boolean checkCameraHardware(Context context) {
	    if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
	        // this device has a camera
	        return true;
	    } else {
	        // no camera on this device
	        return false;
	    }
	}

	
	/** A safe way to get an instance of the Camera object. */
	public static Camera getCameraInstance(int cameraId){
	    Camera c = null;
	    try {
	        c = Camera.open(cameraId); // attempt to get a Camera instance
	        System.out.println("camera opened!");
	    }
	    catch (Exception e){
	        // Camera is not available (in use or does not exist)
	    }
	    return c; // returns null if camera is unavailable
	}
	
	
	private PictureCallback mPicture = new PictureCallback() {

	    @Override
	    public void onPictureTaken(byte[] data, Camera camera) {

	        File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
	        if (pictureFile == null){
//	            Log.d(TAG, "Error creating media file, check storage permissions: " +
//	                e.getMessage());
	        	System.out.println("Error creating media file!");
	            return;
	        }

	        try {
	        	 bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
	            FileOutputStream fos = new FileOutputStream(pictureFile);
	            fos.write(data);
	            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);//将图片压缩的流里面
	            fos.close();
	        } catch (FileNotFoundException e) {
	            Log.d(TAG, "File not found: " + e.getMessage());
	        } catch (IOException e) {
	            Log.d(TAG, "Error accessing file: " + e.getMessage());
	        }

		    // 拍照后重新开始预览
	        mCamera.stopPreview();
	        mCamera.startPreview();
	        bitmap.recycle();//回收bitmap空间
	    }

	   
	};
	
	//剪裁图片
   public void startPhotoZoom(Uri uri){
	   Intent intent = new Intent("com.android.camera.action.CROP");
	   intent.setDataAndType(uri,"image/*");
	   intent.putExtra("crop", "true");
	   intent.putExtra("aspectX", 1);
	   intent.putExtra("aspectY", 1);
	   intent.putExtra("outputX", 150);//剪裁区的宽高
	   intent.putExtra("outputY", 150);  
	   intent.putExtra("return - data", true);  //是否将数据保留在data中
	   Uri cropUri = Uri.fromFile( getOutputMediaFile(MEDIA_TYPE_IMAGE));//设置剪切的图片保存位置
	   intent.putExtra(MediaStore.EXTRA_OUTPUT, cropUri);
	   startActivityForResult(intent,RESULT_REQUEST_CODE);
   } 
	
	
	/** Create a file Uri for saving an image or video */
	private static Uri getOutputMediaFileUri(int type){
	      return Uri.fromFile(getOutputMediaFile(type));
	}

	private static File mediaStorageDir;
	/** Create a File for saving an image or video */
	private static File getOutputMediaFile(int type){
	    // To be safe, you should check that the SDCard is mounted
	    // using Environment.getExternalStorageState() before doing this.

	     mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
	              Environment.DIRECTORY_PICTURES), "MyTagApp");
	    // This location works best if you want the created images to be shared
	    // between applications and persist after your app has been uninstalled.

	    // Create the storage directory if it does not exist
	    if (! mediaStorageDir.exists()){
	        if (! mediaStorageDir.mkdirs()){
	            Log.d("MyTagApp", "failed to create directory");
	            return null;
	        }
	    }

	    // Create a media file name
	    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	    File mediaFile;
	    if (type == MEDIA_TYPE_IMAGE){
	        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
	        "IMG_"+ timeStamp + ".jpg");
	    } else if(type == MEDIA_TYPE_VIDEO) {
	        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
	        "VID_"+ timeStamp + ".mp4");
	    } else {
	        return null;
	    }

	    return mediaFile;
	}
	
	//原来API中没有重写这个方法
	 @Override
	    protected void onResume()
	    {
	        
	        super.onResume();
	        System.out.println("onResume");

	        // Open the default i.e. the first rear facing camera.
	        mCamera = getCameraInstance(mCameraCurrentlyLocked);
	        
//	        preview.setCamera(mCamera);
	    }
	 
	  @Override
	    protected void onPause() {
	        super.onPause();
	        System.out.println("onPause");
	        releaseMediaRecorder();       // if you are using MediaRecorder, release it first
	        releaseCamera();              // release the camera immediately on pause event
	    }

	    private void releaseMediaRecorder(){
	        if (mMediaRecorder != null) {
	            mMediaRecorder.reset();   // clear recorder configuration
	            mMediaRecorder.release();// release the recorder object
	            System.out.println("realese the mediarecorder");
	            mMediaRecorder = null;
	            mCamera.lock();           // lock camera for later use
	        }
	    }

	    private void releaseCamera(){
	        if (mCamera != null){
	            mCamera.release();        // release the camera for other applications
	           System.out.println("camera release");
	            mCamera = null;
	        }
	    }

		@Override
		public void onAutoFocus(boolean success, Camera camera) {
			   System.out.println("________onAutoFocus");
               if(success)  
               {  
            	    params = camera.getParameters();
                   params.setPictureFormat(PixelFormat.JPEG);//图片格式
//                   params.setPreviewSize(800, 480);//图片大小
                   params.setRotation(90);  
      		
                   camera.setParameters(params);//将参数设置到我的camera
                   camera.setDisplayOrientation(90);  

                     
               }  

			
		}

		
		@Override
		protected void onActivityResult(int requestCode, int resultCode,
				Intent data) {
			super.onActivityResult(requestCode, resultCode, data);
			if (resultCode != Activity.RESULT_CANCELED) {//通过结果码来判断是否拍取了图片
				switch (requestCode) {//通过请求码来判断是哪个请求的数据
				case RESULT_REQUEST_CODE:
					System.out.println("已剪切并保存");
					break;
				case CAMERA_REQUEST_CODE://需要注意的是这里直接返回的图片信息是通过Intent data传递的，data.getData()得到该图片的uri的,然后通过该图片的uri来做相应的事
					
					Uri uri = Uri.fromFile(getOutputMediaFile(MEDIA_TYPE_IMAGE));
					startPhotoZoom(uri);
					break;
				}
			}
		}
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			 switch (v.getId()) {
             case R.id.button_capture: //拍照
//                     addContentView(mDraw, new 
//                    		 LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
//                     frameFocus = true;
                     
                     mCamera.autoFocus(this);//自动对焦
                     mCamera.takePicture(null, null, mPicture);
                     Toast.makeText(getApplicationContext(), "Yes", Toast.LENGTH_SHORT).show();
//                     Uri uri = Uri.fromFile(getOutputMediaFile(MEDIA_TYPE_IMAGE));
//                     startPhotoZoom(uri);
                     break;
             case R.id.camera_preview:
            	 mCamera.autoFocus(this);//自动对焦
            	 
//            	 System.out.println("duijao");
//            	 linearrLayout01.removeView(mDraw);
            	 break;
              default:
                	  break;
			 }
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			 System.out.println("surface changed");
			 try
	         {
	             if (null != mCamera)
	             {
	                 mCamera.stopPreview();
	             }
	         }
	         catch (Exception e)
	         {
	             // ignore: tried to stop a non-existent preview
	         }
			 
			 if (null != mCamera)
	         {  
				 params = mCamera.getParameters();
			
 	           params.setPictureFormat(PixelFormat.JPEG);  
				 
//			 params.setPreviewSize(mScreenWidth, mScreenWidth);  
//			 params.setPictureSize(2592, 1936); 
 	           
			 params.setJpegQuality(85);// 设置照片的质量
			 params.setRotation(90);  
			 mCamera.setParameters(params);
			 mCamera.setDisplayOrientation(90);
	         }
				try {

		             if (null != mCamera)
		             {
		     
		                 mCamera.setPreviewDisplay(mHolder);
		                 mCamera.startPreview();
		                 mCamera.lock();
		                 System.out.println("camera locked!");
		             }


				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
					}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			System.out.println("surfaceCreated");
	        // The Surface has been created, now tell the camera where to draw the preview.
			 //开启相机  
            if(mCamera == null)  
            {  
                    mCamera = Camera.open(); 
       	              params = mCamera.getParameters();
       	           params.setPictureFormat(PixelFormat.JPEG);  
				 
//				 params.setPreviewSize(720,720);  
//       	             requestLayout();
//				 params.setPictureSize(1900, 1900); 
				 params.setJpegQuality(85);// 设置照片的质量
				 params.setRotation(90);  
				 mCamera.setParameters(params);
				 mCamera.setDisplayOrientation(90);
				  try {
					mCamera.setPreviewDisplay(holder);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}  
				 Log.d(TAG, "camera set parameters successfully!: "
				         + params);  
            }
                   
			
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			// TODO Auto-generated method stub
			
		}
	   

}
