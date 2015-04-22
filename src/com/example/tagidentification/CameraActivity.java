package com.example.tagidentification;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.http.util.EncodingUtils;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
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
import android.media.ExifInterface;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.provider.MediaStore;
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
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.RelativeLayout.LayoutParams;
public class CameraActivity extends Activity implements 
	Callback, OnClickListener, AutoFocusCallback

	{

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
    public static int top ;
    public static final String TAG = "TagIdentification";
	private static final int TAKE_PICTURE = 1;
	private static final int SELECT_FILE = 2;
	public static Camera mCamera;
//    private CameraPreview mPreview;
    private MediaRecorder mMediaRecorder;
    long waitTime = 2000;    
	long touchTime = 0;  
	private int checkItem;
	ImageButton captureButton;
	ImageButton uploading;
	
	ImageButton PictureFromFile;
	ImageButton goHome;
	ImageButton back;
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
	private DrawImageView2 mDrawIV2;
	
	private final int menu_add = 101;
    private final int menu_help = 103;
//	private native int processFileNative();

	
	
	@SuppressWarnings("deprecation")
	@SuppressLint("WrongCall") @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//设置全屏
//		requestWindowFeature(Window.FEATURE_NO_TITLE);  //无title  
//		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,  
//		              WindowManager.LayoutParams.FLAG_FULLSCREEN);  //全屏  
		setContentView(R.layout.preview);
		
		

//        getActionBar().setDisplayHomeAsUpEnabled(true);
		//竖屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        
        
		 //获取屏幕尺寸
        wManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        display = wManager.getDefaultDisplay();
        mScreenHeight = display.getHeight();//1800
        mScreenWidth = display.getWidth();//1080
        mFocusTop = mFocusLeft = (mScreenWidth * 3) / 8;
        mFocusWidth = mFocusHeight = mScreenWidth / 4;
        top = (mScreenHeight-mScreenWidth)/3; 
       
         

         preview = (SurfaceView) findViewById(R.id.camera_preview);
//         //surfaceview预览尺寸：1080*1080
//         FrameLayout.LayoutParams layoutParams = 
//        		 new FrameLayout.LayoutParams(mScreenWidth, mScreenWidth);
//         preview.setLayoutParams(layoutParams);
         preview.setOnClickListener(this);
        
         mHolder = preview.getHolder(); 
          
         mHolder.addCallback(this); 
//         mHolder.setFixedSize(1280, 720);
     
         mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);  
         mDrawIV = (com.example.tagidentification.DrawImageView)findViewById(R.id.drawIV);
         LayoutParams params = (LayoutParams) mDrawIV.getLayoutParams();  
         params.height= mScreenHeight;  
         params.width = mScreenWidth;  
         mDrawIV.setLayoutParams(params);  
         mDrawIV.onDraw(new Canvas());
         
//         mDrawIV2 = (com.example.tagidentification.DrawImageView2)findViewById(R.id.drawIV2);
//         LayoutParams params2 = (LayoutParams) mDrawIV2.getLayoutParams();  
//         params2.height= top;  
//         params2.width = mScreenWidth;  
//         mDrawIV2.setLayoutParams(params);  
//         mDrawIV2.onDraw(new Canvas());
//         mDrawIV.setOnClickListener(this);
         mDraw = new DrawCaptureRect(CameraActivity.this,
        		 mFocusLeft,mFocusTop,mFocusWidth,mFocusHeight);
         back = (ImageButton) findViewById(R.id.back);
         PictureFromFile = (ImageButton) findViewById(R.id.PictureFromFile);
        captureButton = (ImageButton) findViewById(R.id.button_capture);
        uploading = (ImageButton)findViewById(R.id.button_UpLoading);
        goHome = (ImageButton)findViewById(R.id.goHome);
       
        back.setOnClickListener(this);
        goHome.setOnClickListener(this);
        PictureFromFile.setOnClickListener(this);
        captureButton.setOnClickListener(this);
        uploading.setOnClickListener(this);
        // 获取相机ID
        mDefaultCameraId = getDefaultCameraId();
        mCameraCurrentlyLocked = mDefaultCameraId;
//        System.out.println("mCameraCurrentlyLocked "+mCameraCurrentlyLocked);
//        if(msc!=null){
//            msc.disconnect();
//        }
//        msc=new MediaScannerConnection(CameraActivity.this, CameraActivity.this);
//        msc.connect();
	}

	 //返回方法
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
//	    menu.findItem(R.id.menu_pictureFromFile).setVisible(true);
//	    menu.findItem(R.id.menu_help).setVisible(true);
	    menu.add(0, menu_add, 1, "PictureFromFile");
        menu.add(0, menu_help, 1, "Help");
      
	    return true;
	}



	@SuppressLint("SdCardPath")
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch(item.getItemId()){
			case menu_add:
//				//读取固定路径下单张图片
//				readPath = "/mnt/sdcard/MyTagApp/4.jpg";
//				mBitmap = BitmapFactory.decodeFile(readPath);
//				processPhotos(mBitmap);
				Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		    	intent.setType("image/*");
		    	startActivityForResult(intent, SELECT_FILE);
				 return true;
			case menu_help:
				Toast.makeText(this, "help~", Toast.LENGTH_SHORT).show();
				 return true;
		}
			 return super.onOptionsItemSelected(item);
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != Activity.RESULT_OK){
			return;
		}
//			return;
		switch (requestCode) {
			case SELECT_FILE: 
				if(data.getData()!=null){
					Uri imageUri = data.getData();
		
					String[] projection = { MediaStore.Images.Media.DATA };
					Cursor cur = managedQuery(imageUri, projection, null, null, null);
					cur.moveToFirst();
					jpegName = cur.getString(cur.getColumnIndex(MediaStore.Images.Media.DATA));
					
					//Remove output file
					deleteFile(resultUrl);
					mBitmap = BitmapFactory.decodeFile(jpegName);
					processPhotos(mBitmap);//处理函数之后已经传过去结果了
					}else{
						Log.i(TAG, "Select None!");
						
					}
				break;
			case TAKE_PICTURE:  
                //将保存在本地的图片取出并缩小后显示在界面上  
                 bitmap = BitmapFactory.decodeFile(
                		 Environment.getExternalStorageDirectory()+"/image.jpg");  
                 System.out.println("bitmap.getWidth() + "+bitmap.getWidth()+"bitmap.getHeight():"+bitmap.getHeight());
 	        	mCamera.stopPreview();  
 	        	 Matrix matrix = new Matrix();  
 	            matrix.postRotate((float)90.0);  
 	            rotaBitmap = Bitmap.createBitmap(bitmap, 
 	            		0, 0, bitmap.getWidth(), bitmap.getHeight(), 
 	            		matrix, false);
 	           System.out.println("rotaBitmap.getWidth() :"+ rotaBitmap.getWidth()+"rotaBitmap.getHeight()"+rotaBitmap.getHeight());
 	          int width,height;
 	          if(rotaBitmap.getWidth() > rotaBitmap.getHeight())    {
 	        	  width = rotaBitmap.getHeight();
 	        	  height = rotaBitmap.getHeight();
 	          }else{
 	        	  width = rotaBitmap.getWidth();
 	        	  height = rotaBitmap.getWidth();
 	          }
 	          sizeBitmap = Bitmap.createBitmap(rotaBitmap, 0,0,width, height);  
 	    	     //  System.out.println("sizeBitmap.getWidth()"+sizeBitmap.getWidth()+"sizeBitmap.getHeight()"+sizeBitmap.getHeight());
 	             if(null != sizeBitmap)  {
 	    	        		 savePhotos(sizeBitmap);
 	    	         }
 	       		
 	    		    // 重新预览
 	    	        mCamera.stopPreview();
 	    	        mCamera.startPreview();
 	    	        bitmap.recycle();//回收bitmap
 	    	        rotaBitmap.recycle();
 	    	        sizeBitmap.recycle();
 	    	       break;
			default:
					break;
		}
		
			
	}


	//画焦点框矩形
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

	//拍照回调时，修剪图片
		
		public Bitmap sizeBitmap;
		public Bitmap rotaBitmap;
		Bundle bundle = null; 
	private PictureCallback mPicture = new PictureCallback() {

	    @Override
	    public void onPictureTaken(byte[] data, Camera camera) {
	    	
	    	bundle = new Bundle();  
            bundle.putByteArray("bytes", data); //将图片字节数据保存在bundle当中，实现数据交换  
            File fileFolder = new File(Environment.getExternalStorageDirectory()  
                    , "image.jpg");  
            System.out.println("fileFolder :"+ fileFolder);
            if (!fileFolder.exists()) { // 如果目录不存在，则创建一个名为"finger"的目录  
                fileFolder.mkdir();  
            }  
            File jpgFile = new File(fileFolder, "image.jpg");  
            FileOutputStream outputStream;
			try {
				outputStream = new FileOutputStream(jpgFile);
				outputStream.write(data); // 写入sd卡中  
	            outputStream.close(); // 关闭输出流  
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} // 文件输出流  
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
//			bitmap = BitmapFactory.decodeFile("fileFolder");
//			 mCamera.stopPreview();  
			
			bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
      	  System.out.println("bitmap.getWidth() + "+bitmap.getWidth()+"bitmap.getHeight():"+bitmap.getHeight());
//      	  mCamera.stopPreview();  
//	      	List<Camera.Size> psizes = params.getSupportedPictureSizes(); 
//	        Camera.Size pcs = (Camera.Size) psizes.get(0); 
//	        psizeheight = pcs.height; 
//	        psizewidth = pcs.width;

      	
	    	//480*640还是不行
//	    	File temFile = new File(Environment.getExternalStorageDirectory(),"image.jpg");  
//	    	
//	    	 try {
//	             FileOutputStream fos = new FileOutputStream(temFile);
//	             fos.write(data);
//	             fos.close();
//	         } catch (FileNotFoundException e) {
//	             Log.d(TAG, "File not found: " + e.getMessage());
//	         } catch (IOException e) {
//	             Log.d(TAG, "Error accessing file: " + e.getMessage());
//	         }
	    	
//	    	bitmap = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory()+"/image.jpg"); 
//	    	 System.out.println("bitmap.getWidth() + "+bitmap.getWidth()+"bitmap.getHeight():"+bitmap.getHeight());
//        	mCamera.stopPreview();
	    	//也不行，还是缩略图的形式
//	    	 if(null != data){
//		    	 ByteArrayInputStream bais = new ByteArrayInputStream(data);
//		    	 bitmap =  BitmapFactory.decodeStream(bais);
//		    	 System.out.println("bitmap.getWidth() + "+bitmap.getWidth()+"bitmap.getHeight():"+bitmap.getHeight());
//		        	mCamera.stopPreview();
//	    	 }
	    	//小米2手机是缩略图的形式640*480.这是不可以的。
//	        if(null != data){  
//	        	bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
//	        	  System.out.println("bitmap.getWidth() + "+bitmap.getWidth()+"bitmap.getHeight():"+bitmap.getHeight());
//	        	mCamera.stopPreview();  
////                isPreview = false;  
//            }  
			
			//解决显示旋转问题
      	if(bitmap.getWidth()>bitmap.getHeight()){
	        Matrix matrix = new Matrix();  
            matrix.postRotate((float)90.0);  
            rotaBitmap = Bitmap.createBitmap(bitmap, 
            		0, 0, bitmap.getWidth(), bitmap.getHeight(), 
            		matrix, false);
            
      	}else{
      		 Matrix matrix = new Matrix(); 
      		rotaBitmap = Bitmap.createBitmap(bitmap, 
            		0, 0, bitmap.getWidth(), bitmap.getHeight(), 
            		matrix, false);
      	}
      	
        //     旋转之后3264*2448. 到2448 *3264
        System.out.println("rotaBitmap.getWidth() :"+ rotaBitmap.getWidth()+"rotaBitmap.getHeight()"+rotaBitmap.getHeight());
        int width,height;
        if(rotaBitmap.getWidth() > rotaBitmap.getHeight())    {
      	  width = rotaBitmap.getHeight();
      	  height = rotaBitmap.getHeight();
        }else{
      	  width = rotaBitmap.getWidth();
      	  height = rotaBitmap.getWidth();
        }
//            //下面也可以实现截取图片的一部分为正方形区域
//          Mat rotaBitmapMat = new Mat();
//          Mat newrotaBitmapMat = new Mat(); 
//          Utils.bitmapToMat(rotaBitmap, rotaBitmapMat);
//          org.opencv.core.Size size;
//          size =  rotaBitmapMat.size();
//       
//          rotaBitmapMat = rotaBitmapMat.rowRange(0, (int)size.width);
//           System.out.println("rotaBitmapMat.size"+rotaBitmapMat.size());
//           sizeBitmap = Bitmap.createBitmap(bitmap, 0,0, width,  height);
//         Utils.matToBitmap(rotaBitmapMat, sizeBitmap);

       
        sizeBitmap = Bitmap.createBitmap(rotaBitmap, 0,0,width, height);  
	     //  System.out.println("sizeBitmap.getWidth()"+sizeBitmap.getWidth()+"sizeBitmap.getHeight()"+sizeBitmap.getHeight());
         if(null != sizeBitmap)  {
	        		 savePhotos(sizeBitmap);
	         }
         
         	bitmap.recycle();//回收bitmap
	        rotaBitmap.recycle();
	        sizeBitmap.recycle();
		    // 重新预览
	        mCamera.stopPreview();
	        mCamera.startPreview();
	       
	    }

	};
	
	private  String jpegName;
	private String savePath;
	private String Exposure_Time  ;//曝光时间
	private String FNumber ;//光圈
	//save photos 
	public void savePhotos(Bitmap bm){  

	    File folder = new File(Environment.getExternalStoragePublicDirectory(
	              Environment.DIRECTORY_DCIM), "Camera");
		
//         savePath = "/mnt/sdcard/MyTagApp/";  
//        File folder = new File(savePath);  
        if(!folder.exists()) 
        {  
            folder.mkdir();  
        }  
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//        long dataTake = System.currentTimeMillis();  
         jpegName = folder.getPath() + File.separator + timeStamp +".jpg";  
        Log.i(TAG, "saveJpeg:jpegName--" + jpegName);  
        try {  
        	System.out.println("jpegName:  "+jpegName);
            FileOutputStream fout = new FileOutputStream(jpegName);  
            BufferedOutputStream bos = new BufferedOutputStream(fout);  
  
            //          //�����Ҫ�ı��С(Ĭ�ϵ��ǿ�960�x�1280),��ĳɿ�600�x�800  
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
        //网上有一个小米手机用的是下面的方法
        Intent intent = new Intent(
                Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(folder);
        intent.setData(uri);
        sendBroadcast(intent);
        //以下两种都不能及时刷新图库
//        Uri localUri = Uri.fromFile(folder);
//
//        Intent localIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, localUri);
//
//        sendBroadcast(localIntent);
        //第三种
//        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse(folder.getPath())));  
        //下面语句不行，会死掉
//        sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,Uri.fromFile(folder))); 
//        MediaScannerConnection.scanFile(this, new String[]{Environment.getExternalStoragePublicDirectory(
//        		Environment.DIRECTORY_DCIM).getPath() + "/" + "Camera"}, null, null);  
    }  
	private String readPath;
	public Bitmap readPhotos(Bitmap bm){
		System.out.println("jpegName :"+ jpegName);
//		String readPath = "/mnt/sdcard/MyTagApp/1.jpg"; 
		readPath = jpegName; 
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
	
	//ԭ4API��û����д����
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
	 
	//OpenCV����ز���ʼ���ɹ���Ļص���  
	    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {  
	  
	        @Override  
	        public void onManagerConnected(int status) {  
	            // TODO Auto-generated method stub  
	            switch (status){  
	            case BaseLoaderCallback.SUCCESS:  
	                System.out.println( "success");  
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
		private Bitmap newotsuBitmap;
		private Bitmap otsuBitmap;
		private Bitmap edgesBitmap;
		private Bitmap linesBitmap;
		private Bitmap imageCorrectGrayBitmap;
		private Bitmap imageOTUSSBitmap;
		private int mBitmapWidth;
		private int mBitmapHeight;
		private String imagePath;
		private String resultUrl = "result.txt";
//		private String resultUrl = "mnt/sdcard/28ChinesePRC+English.txt";
		private Intent results;
		private boolean process = false;
		private ResultsActivity mResultsActivity;
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			 switch (v.getId()) {
             case R.id.button_capture: //����
//            	 小米手机这里预览的话会变得模糊
//                     mCamera.autoFocus(this);//自动聚焦
                     mCamera.takePicture(null, null, mPicture);
                     
                     Toast.makeText(getApplicationContext(), "Please click the Yes button", Toast.LENGTH_SHORT).show();
                     
                     break;
             case R.id.camera_preview:
            	 mCamera.autoFocus(this);//自动聚焦
            	 break;
             case R.id.button_UpLoading:
            	 Toast.makeText(this, "Start UpLoading", Toast.LENGTH_SHORT).show();
            	 mBitmap = readPhotos(sizeBitmap);
            	 processPhotos(mBitmap);
            	  break;
             case R.id.PictureFromFile:
            	 Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
 		    	intent.setType("image/*");
 		    	startActivityForResult(intent, SELECT_FILE);
            	 break;
             case R.id.goHome:
            	 Intent mHomeIntent = new Intent(Intent.ACTION_MAIN);  
            	 mHomeIntent.addCategory(Intent.CATEGORY_HOME);  
            	 mHomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK  
            	                 | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);  
            	 startActivity(mHomeIntent);  
            	 break;
             case R.id.back:
            	 mCamera.setPreviewCallback(null) ;
             	mCamera.stopPreview();
             
             	mCamera.release();
             	mCamera = null;
             	System.out.println("camera release!");
             	System.exit(0);
             	break;
             default:
                	  break;
			 }
		}

		public void processPhotos(Bitmap bitmap){
			 Mat rgbMat = new Mat();  
             Mat grayMat = new Mat(); 
             Mat otsuMat = new Mat();
             Mat edgesMat = new Mat();
             Mat linesMat = new Mat();
             Mat imageCorrectGrayMat = new Mat();
             
             mBitmapWidth = mBitmap.getWidth();
        	 mBitmapHeight = mBitmap.getHeight();
        	 //显示原始图片
        	 mDrawIV.setImageBitmap(mBitmap);
        	
        	 //灰度化
        	 grayBitmap = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(), Config.RGB_565);  
        	 Utils.bitmapToMat(mBitmap, rgbMat);//convert original bitmap to Mat, R G B.  
        	 Imgproc.cvtColor(rgbMat, grayMat, Imgproc.COLOR_RGB2GRAY);//rgbMat to gray grayMat  
        	        Utils.matToBitmap(grayMat, grayBitmap); //convert mat to bitmap  
        	 //显示灰度图
//        	 mDrawIV.setImageBitmap(grayBitmap);
//        	        savePhotos(grayBitmap);   
        	 //整体大津法
        	 otsuBitmap = Bitmap.createBitmap(grayBitmap);  
        	 Imgproc.threshold(grayMat, otsuMat, 0, 255, Imgproc.THRESH_BINARY|Imgproc.THRESH_OTSU);
        	 Utils.matToBitmap(otsuMat, otsuBitmap);
        	 
        //	 mDrawIV.setImageBitmap(otsuBitmap);
      // 	 savePhotos(otsuBitmap);
        	 
////        	 //  #Hough变换矫正图像
//        	 edgesBitmap = Bitmap.createBitmap(otsuBitmap);
//        	 Imgproc.Canny(otsuMat, edgesMat, 50, 150);
//        	 Utils.matToBitmap(edgesMat, edgesBitmap);
//        	 Imgproc.HoughLines(edgesMat, linesMat, 1, Math.PI/360, mBitmapWidth/5);
//        	 
//        	 if(linesMat.get(0, 0) != null){
//        		 double[] lines = linesMat.get(0, 0);
//        		 double rho = lines[0];
//        		 double theta = lines[1];
//            	 float thetabSum = 0;
//                 float thetabNum = 0;
//                 thetabSum += (theta-Math.PI/2);
//                 thetabNum += 1;
////                 
////                 
//                 imageCorrectGrayBitmap = rotate_about_center(grayBitmap,(float)(thetabSum/thetabNum*180/Math.PI),1.0);//角度
//////                 imageCorrectGrayBitmap = rotate_about_center(grayBitmap,(float)(30),1.0);
//            	Utils.bitmapToMat(imageCorrectGrayBitmap, imageCorrectGrayMat);
//            	
//        	 }else{
//        		 imageCorrectGrayMat = grayMat;
//        		 imageCorrectGrayBitmap = grayBitmap;
//        	 }
        	 
        	
//             mDrawIV.setImageBitmap(imageCorrectGrayBitmap);
//             savePhotos(imageCorrectGrayBitmap);
//             
             double rowStep = 15.0;
            
//             org.opencv.core.Size size;
//             size =  grayMat.size();
//             System.out.println("size :"+size+"size.height:"+size.height);//c*r 221*124
//              double sLength = size.height/rowStep;
//              int i;
//              for(i = 1; i <= rowStep; i++){
//	            	  Imgproc.threshold(grayMat.rowRange((int)(sLength*(i-1)), (int)(sLength*i)), grayMat.rowRange((int)(sLength*(i-1)), (int)(sLength*i)), 0, 255, Imgproc.THRESH_BINARY|Imgproc.THRESH_OTSU);
//            	  }
              
              org.opencv.core.Size size;
              size =  grayMat.size();
              System.out.println("size :"+size+"size.height:"+size.height);//c*r 221*124
               double sLength = size.height/rowStep;
               int i;
               for(i = 1; i <= rowStep; i++){
 	            	  Imgproc.threshold(grayMat.rowRange((int)(sLength*(i-1)), (int)(sLength*i)), grayMat.rowRange((int)(sLength*(i-1)), (int)(sLength*i)), 0, 255, Imgproc.THRESH_BINARY|Imgproc.THRESH_OTSU);
             	  }
             Utils.matToBitmap(grayMat, grayBitmap);
             
             //特殊大津法
              newotsuBitmap = Bitmap.createBitmap(grayBitmap);
              Utils.matToBitmap(grayMat, newotsuBitmap);
//              mDrawIV.setImageBitmap(newotsuBitmap);
//              savePhotos(newotsuBitmap);
               
//             
        	 results = new Intent(this, ResultsActivity.class);
        	 results.putExtra("IMAGE_PATH", jpegName);
        	 results.putExtra("RESULT_PATH", resultUrl);
        	 startActivity(results);
			
		}
		
   
			
			
//            Matrix matrix = new Matrix();
////            jangle = (float)(angle / Math.PI  * 180);
////            System.out.println("jangle :"+ jangle);
//            double w = bitmap.getWidth();
//            double h = bitmap.getHeight();
//            double rangle = Math.toRadians(angle);//�Ƕ�ת���ɻ���
//           
//			double nw = (Math.abs(Math.sin(rangle)*h) + Math.abs(Math.cos(rangle)*w));
//			double nh = (Math.abs(Math.cos(rangle)*h) + Math.abs(Math.sin(rangle)*w));
//			double x = (nw-w)*0.5;//ȷ��ԭ�����  
//		    double y = (nh-h)*0.5;  
//            matrix.postRotate(angle, (float) nw / 2, (float) nh / 2);
//            matrix.postTranslate((float)x, (float) y);
//            Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
//                            bitmap.getHeight(), matrix, true);
//            return newBitmap;
//    }
		public Bitmap rotate_about_center(Bitmap bitmap,double angle,double scale){
			Mat bitmapMat = new Mat(); 
			Mat desMat = new Mat();
			Bitmap rotatedBitmap;
			 Utils.bitmapToMat(bitmap, bitmapMat);
			double w = bitmap.getWidth();
			double h = bitmap.getHeight();
			double rangle = Math.toRadians(angle);//
			System.out.println("angle"+ angle + "rangle :"+ rangle);
			double nw = (Math.abs(Math.sin(rangle)*h) + Math.abs(Math.cos(rangle)*w))*scale;
			double nh = (Math.abs(Math.cos(rangle)*h) + Math.abs(Math.sin(rangle)*w))*scale;
			double x = (nw-w)*0.5;//
		    double y = (nh-h)*0.5;  
			Point center = new Point((nw*0.5), (nh*0.5));
			Mat rot_mat = Imgproc.getRotationMatrix2D(center, angle, scale);
//			double array[] = new double[]{x,y,0};
//			MatOfDouble A = new MatOfDouble();
//
//			A.fromArray(x,y,0);
//			 rot_mat.dot(A);
			org.opencv.core.Size s = bitmapMat.size();
			//Ceiling������ȡ��  
			Imgproc.warpAffine(bitmapMat, desMat, rot_mat, s, Imgproc.INTER_LANCZOS4);
			Utils.matToBitmap(desMat, bitmap);
			return bitmap;
		}
//		
		private Camera.Size cs;
		private float psizeheight;
		private float psizewidth;
		private float sizeheight;
		private float sizewidth;
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
				 //只有两种场景模式“text”,"auto"
//				 List<String> supportSceneMode = params.getSupportedSceneModes();    
//				 for (int i = 0; i < supportSceneMode.size(); i++) {  
//				     System.out.println( "supportSceneMode : " + i + " : " + supportSceneMode.get(i));  
//				 } 
				 //不知道是什么模式。。。文本模式？
//				 params.setSceneMode("text");
				 
//			System.out.println("params.getExposureCompensation() "+
//					params.getExposureCompensation() +  
//					"parameters.getExposureCompensationStep()"+ 
//					params.getExposureCompensationStep()+" params.getAutoExposureLock()"+ 
//					params.getAutoExposureLock()+
//					"params.getMaxExposureCompensation() : "+ params.getMaxExposureCompensation()
//					+"params.getMminExposureCompensation() : "+ params.getMinExposureCompensation()
//					);//曝光补偿为0，步长为0.5，AutoExposureLock为false,最大补偿为4，最小为-4.
//			params.setAutoExposureLock(true);//true的话就是自动调节曝光补偿
//			params.setExposureCompensation(1);
			
			
			params.setPictureFormat(PixelFormat.JPEG);  

	          List<Camera.Size> psizes = params.getSupportedPictureSizes(); 
	            Camera.Size pcs = (Camera.Size) psizes.get(0); 
	            psizeheight = pcs.height; 
 	            psizewidth = pcs.width;
 	            params.setPictureSize((int)psizewidth, (int)psizeheight);
 	            float n = psizeheight/psizewidth;
 	          List<Camera.Size> sizes = params.getSupportedPreviewSizes(); 
 	         for (int i = 0; i < sizes.size(); i++) { 
 	            Camera.Size cs = (Camera.Size) sizes.get(i); 
 	             sizeheight = cs.height; 
 	             sizewidth = cs.width; 
 	             if(n == (sizeheight/ sizewidth)){
 	            	params.setPreviewSize((int)sizewidth, (int)sizeheight);
 	            	break;
 	             }
 	         }
// 	          cs = sizes.get(1); 
// 	        params.setPreviewSize(cs.width, cs.height);//800*600
// 	           System.out.println("cs.width"+ cs.width + "cs.height"+ cs.height);

			params.setJpegQuality(100);// ������Ƭ����
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
//				 params.setPreviewSize(640, 480);
       	      List<Camera.Size> psizes = params.getSupportedPictureSizes(); 
	            Camera.Size pcs = (Camera.Size) psizes.get(0); 
	            psizeheight = pcs.height; 
	            psizewidth = pcs.width;
	            params.setPictureSize((int)psizewidth, (int)psizeheight);
	            float n = psizeheight/psizewidth;
	          List<Camera.Size> sizes = params.getSupportedPreviewSizes(); 
	         for (int i = 0; i < sizes.size(); i++) { 
	            Camera.Size cs = (Camera.Size) sizes.get(i); 
	             sizeheight = cs.height; 
	             sizewidth = cs.width; 
	             if(n == (sizeheight/ sizewidth)){
	            	params.setPreviewSize((int)sizewidth, (int)sizeheight);
	            	break;
	             }
	         }
				 params.setJpegQuality(100);// ������Ƭ����
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
