package com.example.tagidentification;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.http.util.EncodingUtils;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Bitmap.Config;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

public class CameraActivity extends Activity implements 
	Callback, OnClickListener, AutoFocusCallback{

	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;

	private int mNumberOfCameras;
    private int mCameraCurrentlyLocked;

    // The first rear facing camera
    private int mDefaultCameraId;

    public static int mScreenWidth;
	public static int mScreenHeight;
	public WindowManager wManager;
	public Display display;
    private int mFocusLeft, mFocusTop, mFocusWidth, mFocusHeight;
    public static final String TAG = "TagIdentification";
	private Camera mCamera;
//    private CameraPreview mPreview;
    private MediaRecorder mMediaRecorder;
    long waitTime = 2000;    
	long touchTime = 0;  
	private int checkItem;
	Button captureButton;
	Button getPhotos;
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
	private DrawImageView mDrawIV;
	
	private final int menu_add = 101;
    private final int menu_help = 103;
	
	
	
	@SuppressWarnings("deprecation")
	@SuppressLint("WrongCall") @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.preview);
		
		
//		getActionBar().setTitle("haha");
        getActionBar().setDisplayHomeAsUpEnabled(true);
		//������Ļ����Ϊ����
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        
        
		 // �õ���Ļ�Ĵ�С
        wManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        display = wManager.getDefaultDisplay();
        mScreenHeight = display.getHeight();//1800
        mScreenWidth = display.getWidth();//1080
        mFocusTop = mFocusLeft = (mScreenWidth * 3) / 8;
        mFocusWidth = mFocusHeight = mScreenWidth / 4;
       
         
        linearrLayout01 = (LinearLayout)findViewById(R.id.linearLayout01);
         preview = (SurfaceView) findViewById(R.id.camera_preview);
         /**��������surfaceview�Ĵ�С*/
         FrameLayout.LayoutParams layoutParams = 
        		 new FrameLayout.LayoutParams(mScreenWidth, mScreenWidth);
         preview.setLayoutParams(layoutParams);
         preview.setOnClickListener(this);
         //��þ��  
         mHolder = preview.getHolder(); 
         //��ӻص�  
         mHolder.addCallback(this); 
//         mHolder.setFixedSize(1280, 720);// ���÷ֱ��� 
         //��������  
         mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);  
         mDrawIV = (com.example.tagidentification.DrawImageView)findViewById(R.id.drawIV);
         mDrawIV.onDraw(new Canvas());
         
         mDraw = new DrawCaptureRect(CameraActivity.this,
        		 mFocusLeft,mFocusTop,mFocusWidth,mFocusHeight);
         
         
        captureButton = (Button) findViewById(R.id.button_capture);
        getPhotos = (Button)findViewById(R.id.button_getPhotos);
        captureButton.setOnClickListener(this);
        getPhotos.setOnClickListener(this);
        // �õ�Ĭ�ϵ����ID
        mDefaultCameraId = getDefaultCameraId();
        mCameraCurrentlyLocked = mDefaultCameraId;
//        System.out.println("mCameraCurrentlyLocked "+mCameraCurrentlyLocked);
	}

	 //˫���˳�
	@Override  
	public boolean onKeyDown(int keyCode, KeyEvent event) {  
	    if(event.getAction() == KeyEvent.ACTION_DOWN && KeyEvent.KEYCODE_BACK == keyCode) {    
//	        long currentTime = System.currentTimeMillis();    
//	        if((currentTime-touchTime)>=waitTime) {    
//	          //  Toast����ʾʱ��͵ȴ�ʱ����ͬ  
//	            Toast.makeText(this, "�ٰ�һ���˳�", (int)waitTime).show();    
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
	
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    getMenuInflater().inflate(R.menu.main, menu);
//	    menu.findItem(R.id.menu_pictureFromFile).setVisible(false);
//	    menu.findItem(R.id.menu_help).setVisible(false);
	    menu.add(0, menu_add, 1, "���ͼ����Ƭ");
        menu.add(0, menu_help, 1, "����");
	    return true;
	}



	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch(item.getItemId()){
			case R.id.menu_pictureFromFile:
				Toast.makeText(this, "please add photos from native file!", Toast.LENGTH_SHORT).show();	
				 return true;
			case R.id.menu_help:
				System.out.println("help~~");
				 return true;
		}
			 return super.onOptionsItemSelected(item);
	}



	//ʵ�ֽ����Ļ�ͼ,������Ϊ�Զ���ȡ����
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
	                // ���û�к�������ͷ
	                defaultId = 0;
	            }
	            else
	            {
	                // û������ͷ
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

	public Bitmap rectBitmap;
	private PictureCallback mPicture = new PictureCallback() {

	    @Override
	    public void onPictureTaken(byte[] data, Camera camera) {

	        if(null != data){  
	        	bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);//data���ֽ����ݣ����������λͼ  
	        	  System.out.println("bitmap.getWidth() + "+bitmap.getWidth()+"bitmap.getHeight():"+bitmap.getHeight());
	        	mCamera.stopPreview();  
//                isPreview = false;  
            }  
	        Matrix matrix = new Matrix();  
            matrix.postRotate((float)90.0);  
            Bitmap rotaBitmap = Bitmap.createBitmap(bitmap, 
            		0, 0, bitmap.getWidth(), bitmap.getHeight(), 
            		matrix, false);
          
            
            //��ת��rotaBitmap��3264*2448.Ԥ��surfaview�Ĵ�С��1080��1080 
            //��3264*2448���ŵ�1080��1080  
            Bitmap sizeBitmap = Bitmap.createScaledBitmap(rotaBitmap, bitmap.getHeight(), bitmap.getHeight(), true);  
             rectBitmap = Bitmap.createBitmap(sizeBitmap, 0, 0, bitmap.getHeight(), bitmap.getHeight());//��ȡ
            
            System.out.println("rectBitmap.getWidth() :"+rectBitmap.getWidth() + "rectBitmap.getHeight() :" + rectBitmap.getHeight());
	       
            if(null != rectBitmap)  {
	        		 savePhotos(rectBitmap);
	         }
		    // ���պ����¿�ʼԤ��
	        mCamera.stopPreview();
	        mCamera.startPreview();
	        bitmap.recycle();//����bitmap�ռ�
	    }

	};
	
	public  String jpegName;
	//save photos 
	public void savePhotos(Bitmap bm){  
        String savePath = "/mnt/sdcard/MyTagApp/";  
        File folder = new File(savePath);  
        if(!folder.exists()) //����ļ��в������򴴽�  
        {  
            folder.mkdir();  
        }  
        long dataTake = System.currentTimeMillis();  
         jpegName = savePath + dataTake +".jpg";  
        Log.i(TAG, "saveJpeg:jpegName--" + jpegName);  
        try {  
        	System.out.println("jpegName:  "+jpegName);
            FileOutputStream fout = new FileOutputStream(jpegName);  
            BufferedOutputStream bos = new BufferedOutputStream(fout);  
  
            //          //�����Ҫ�ı��С(Ĭ�ϵ��ǿ�960����1280),��ĳɿ�600����800  
            //          Bitmap newBM = bm.createScaledBitmap(bm, 600, 800, false);  
  
            bm.compress(Bitmap.CompressFormat.JPEG, 100, bos);  
            bos.flush();  
            bos.close();  
            Log.i(TAG, "Photos have saved!");  
        } catch (IOException e) {  
            // TODO Auto-generated catch block  
            Log.i(TAG, "Photos save  failed!");  
            e.printStackTrace();  
        }  
    }  
	
	public Bitmap readPhotos(Bitmap bm){
		System.out.println("jpegName :"+ jpegName);
		String readPath = "/mnt/sdcard/MyTagApp/1.jpg"; 
		bm = BitmapFactory.decodeFile(readPath); 
		return bm;
		}
	/** Create a file Uri for saving an image or video */
//	private static Uri getOutputMediaFileUri(int type){
//	      return Uri.fromFile(getOutputMediaFile(type));
//	}
//	private static File mediaFileType = getOutputMediaFile(MEDIA_TYPE_IMAGE);
//	private static File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
//            Environment.DIRECTORY_PICTURES), "MyTagApp");
	private  static File mediaFile;
	 
	/** Create a File for saving an image or video */
//	private static File getOutputMediaFile(int type){
//	    // To be safe, you should check that the SDCard is mounted
//	    // using Environment.getExternalStorageState() before doing this.
//
////	     mediaStorageDir  = new File(Environment.getExternalStoragePublicDirectory(
////	             Environment.DIRECTORY_PICTURES), "MyTagApp");
//	    // This location works best if you want the created images to be shared
//	    // between applications and persist after your app has been uninstalled.
//
//	    // Create the storage directory if it does not exist
//	    if (! mediaStorageDir.exists()){
//	        if (! mediaStorageDir.mkdirs()){
//	            Log.d("MyTagApp", "failed to create directory");
//	            return null;
//	        }
//	    }

//	    // Create a media file name
//	    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//	    if (type == MEDIA_TYPE_IMAGE){
//	        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
//	        "IMG_"+ timeStamp + ".jpg");
//	    } else if(type == MEDIA_TYPE_VIDEO) {
//	        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
//	        "VID_"+ timeStamp + ".mp4");
//	    } else {
//	        return null;
//	    }
//
//	    return mediaFile;
//	}
	
	//ԭ��API��û����д�������
	 @Override
	    protected void onResume()
	    {
	        
	        super.onResume();
	        System.out.println("onResume");

	        // Open the default i.e. the first rear facing camera.
	        mCamera = getCameraInstance(mCameraCurrentlyLocked);
	        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_11, getApplicationContext(), mLoaderCallback);  
	        System.out.println("onResume sucess load OpenCV...");
	    }
	 
	//OpenCV����ز���ʼ���ɹ���Ļص�����  
	    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {  
	  
	        @Override  
	        public void onManagerConnected(int status) {  
	            // TODO Auto-generated method stub  
	            switch (status){  
	            case BaseLoaderCallback.SUCCESS:  
	                System.out.println( "�ɹ�����");  
	                break;  
	            default:  
	                super.onManagerConnected(status);  
	                System.out.println( "����ʧ��");  
	                break;  
	            }  
	              
	        }  
	    };  
	 
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
                   params.setPictureFormat(PixelFormat.JPEG);//ͼƬ��ʽ
//                   params.setPreviewSize(800, 480);//ͼƬ��С
                   params.setRotation(90);  
      		
                   camera.setParameters(params);//���������õ��ҵ�camera
                   camera.setDisplayOrientation(90);  
               }  
		}

		private static Uri uri;
		private Bitmap mBitmap;
		private Bitmap grayBitmap;
		private Bitmap otsuBitmap;
		private Bitmap edgesBitmap;
		private int mBitmapWidth;
		private int mBitmapHeight;
		private String imagePath;
		private String resultUrl = "result.txt";
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			 switch (v.getId()) {
             case R.id.button_capture: //����
                     mCamera.autoFocus(this);//�Զ��Խ�
                     mCamera.takePicture(null, null, mPicture);
                     Toast.makeText(getApplicationContext(), "Yes", Toast.LENGTH_SHORT).show();
                     break;
             case R.id.camera_preview:
            	 mCamera.autoFocus(this);//�Զ��Խ�
            	 break;
             case R.id.button_getPhotos://���Զ���app·����ȡ��Ƭ
//            	 Mat rgbMat = new Mat();  
//                 Mat grayMat = new Mat(); 
//                 Mat otsuMat = new Mat();
//                 Mat edgesMat = new Mat();
//                 Mat linesMat = new Mat();
            	 mBitmap = readPhotos(rectBitmap);
            	 processPhotos(mBitmap);//���ص��Ǵ�򷨵�ͼƬ
////            	 System.out.println("mBitmap.getWidth() :"+mBitmap.getWidth() +"mBitmap.getHeight():"+mBitmap.getHeight() );
//            	 mBitmapWidth = mBitmap.getWidth();
//            	 mBitmapHeight = mBitmap.getHeight();
//            	 //��ʾ��ʼͼƬ����Ԥ��λ���ϣ�
////            	 mDrawIV.setImageBitmap(mBitmap);
//            	 
//            	 //ԭʼͼ��ҶȻ�
//            	 grayBitmap = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(), Config.RGB_565);  
//            	 Utils.bitmapToMat(mBitmap, rgbMat);//convert original bitmap to Mat, R G B.  
//            	 Imgproc.cvtColor(rgbMat, grayMat, Imgproc.COLOR_RGB2GRAY);//rgbMat to gray grayMat  
//            	        Utils.matToBitmap(grayMat, grayBitmap); //convert mat to bitmap  
//            	 //��ʾ�Ҷ�ͼ��
////            	 mDrawIV.setImageBitmap(grayBitmap);
//            	        
//            	 //�Ҷ�ͼ���򷨶�ֵ��
//            	 otsuBitmap = Bitmap.createBitmap(grayBitmap);  
//            	 Imgproc.threshold(grayMat, otsuMat, 0, 255, Imgproc.THRESH_BINARY|Imgproc.THRESH_OTSU);
//            	 Utils.matToBitmap(otsuMat, otsuBitmap);
//            	 //��ʾ��򷨶�ֵ��ͼ��
//            	 mDrawIV.setImageBitmap(otsuBitmap);
//            	 savePhotos(otsuBitmap);
//            	 
//            	 //Hough�任����ͼ��
//            	 edgesBitmap = Bitmap.createBitmap(otsuBitmap);
//            	 Imgproc.Canny(otsuMat, edgesMat, 50, 150);
//            	 Utils.matToBitmap(edgesMat, edgesBitmap);
//            	 Imgproc.HoughLines(edgesMat, linesMat, 1, Math.PI/360, mBitmapWidth/5);
            	 Intent results = new Intent(this, ResultsActivity.class);
            	 results.putExtra("IMAGE_PATH", jpegName);
            	 results.putExtra("RESULT_PATH", resultUrl);
            	 startActivity(results);
            	 break;
             default:
                	  break;
			 }
		}

		public Bitmap processPhotos(Bitmap bitmap){
			 Mat rgbMat = new Mat();  
             Mat grayMat = new Mat(); 
             Mat otsuMat = new Mat();
             Mat edgesMat = new Mat();
             Mat linesMat = new Mat();
             mBitmapWidth = mBitmap.getWidth();
        	 mBitmapHeight = mBitmap.getHeight();
        	 //��ʾ��ʼͼƬ����Ԥ��λ���ϣ�
//        	 mDrawIV.setImageBitmap(mBitmap);
        	 
        	 //ԭʼͼ��ҶȻ�
        	 grayBitmap = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(), Config.RGB_565);  
        	 Utils.bitmapToMat(mBitmap, rgbMat);//convert original bitmap to Mat, R G B.  
        	 Imgproc.cvtColor(rgbMat, grayMat, Imgproc.COLOR_RGB2GRAY);//rgbMat to gray grayMat  
        	        Utils.matToBitmap(grayMat, grayBitmap); //convert mat to bitmap  
        	 //��ʾ�Ҷ�ͼ��
//        	 mDrawIV.setImageBitmap(grayBitmap);
        	        
        	 //�Ҷ�ͼ���򷨶�ֵ��
        	 otsuBitmap = Bitmap.createBitmap(grayBitmap);  
        	 Imgproc.threshold(grayMat, otsuMat, 0, 255, Imgproc.THRESH_BINARY|Imgproc.THRESH_OTSU);
        	 Utils.matToBitmap(otsuMat, otsuBitmap);
        	 //��ʾ��򷨶�ֵ��ͼ��
        	 mDrawIV.setImageBitmap(otsuBitmap);
        	 savePhotos(otsuBitmap);
        	 
        	 //Hough�任����ͼ��
//        	 edgesBitmap = Bitmap.createBitmap(otsuBitmap);
//        	 Imgproc.Canny(otsuMat, edgesMat, 50, 150);
//        	 Utils.matToBitmap(edgesMat, edgesBitmap);
//        	 Imgproc.HoughLines(edgesMat, linesMat, 1, Math.PI/360, mBitmapWidth/5);
			return otsuBitmap;
			
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
				 
// 	          params.setPictureSize(800, 600); 
//			 params.setPictureSize(2592, 1936); 
 	           
			 params.setJpegQuality(100);// ������Ƭ������
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
			 //�������  
            if(mCamera == null)  
            {  
                 mCamera = Camera.open(); 
       	         params = mCamera.getParameters();
       	         params.setPictureFormat(PixelFormat.JPEG);  
				 
//       	         params.setPictureSize(800, 600);   
				 params.setJpegQuality(100);// ������Ƭ������
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
