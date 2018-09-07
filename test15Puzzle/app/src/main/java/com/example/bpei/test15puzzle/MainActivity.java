package com.example.bpei.test15puzzle;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionProvider;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener;

import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class MainActivity extends AppCompatActivity implements CvCameraViewListener, View.OnTouchListener{

    private static final String TAG="MainActivity";
    private CameraBridgeViewBase javaCameraView;
    //JavaCameraView javaCameraView;
    Mat mRgba;
    private puzzle15Processor mPuzzle15;
    private int mGameWidth;
    private int mGameHeight;

    private MenuItem mItemHideNumbers;
    private MenuItem mItemStartNewGame;


    BaseLoaderCallback mBaseLoaderCallback=new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status){
                case BaseLoaderCallback.SUCCESS:
                {
                  javaCameraView.setOnTouchListener(MainActivity.this);
                  javaCameraView.enableView();
                  break;
                }
                default:
                {
                    super.onManagerConnected(status);
                    break;
                }
            }

        }
    };


    @Override
    protected void onResume()
    {
        super.onResume();
        if (OpenCVLoader.initDebug()){
            Log.i(TAG,"OpenCV was loaded successfully...");
            mBaseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
        else{
            Log.i(TAG,"OpenCV was not loaded successfully...");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION,this,mBaseLoaderCallback);
        }

    }
    public void onDestroy()
    {
        super.onDestroy();
        if (javaCameraView!=null)
            javaCameraView.disableView();
    }

    // Used to load the 'native-lib' library on application startup.
    static {
        //System.loadLibrary("native-lib");
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Log.d(TAG,"Creating and Setting view");


        javaCameraView=(CameraBridgeViewBase) new JavaCameraView(this,-1)/*(JavaCameraView)findViewById(R.id.java_camera_view)*/;
        setContentView(javaCameraView);
        javaCameraView.setVisibility(SurfaceView.VISIBLE);
        javaCameraView.setCvCameraViewListener(this);



        mPuzzle15=new puzzle15Processor();
        mPuzzle15.preparedNewGame();
        // Example of a call to a native method
//        TextView tv = (TextView) findViewById(R.id.sample_text);
//        tv.setText(stringFromJNI());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present
        Log.i(TAG,"called onCreateOptionMenu");
        mItemHideNumbers=menu.add("Show/hide tile Numbers");
        mItemStartNewGame=menu.add("Start new game");

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        Log.i(TAG,"Menu Item Selected "+item);
        if (item==mItemStartNewGame)
        {
            mPuzzle15.preparedNewGame();
        }
        else if(item==mItemHideNumbers)
        {
            mPuzzle15.toggleTileNumbers();
        }
        return true;
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        int xpos,ypos;
        xpos=(view.getWidth()-mGameWidth)/2;
        xpos=(int) motionEvent.getX()-xpos;

        ypos=(view.getHeight()-mGameHeight)/2;
        ypos=(int)motionEvent.getY()-ypos;

        if (xpos>=0&&xpos<=mGameWidth&&ypos>=0&&ypos<=mGameHeight)
        {
            mPuzzle15.deliverTouchEvent(xpos,ypos);
        }
        return false;
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        //mRgba=new Mat(height,width, CvType.CV_8UC4);
        mGameWidth=width;
        mGameHeight=height;
        mPuzzle15.preparedGameSize(width,height);

    }

    @Override
    public void onCameraViewStopped() {
        //mRgba.release();
    }

    @Override
    public Mat onCameraFrame(Mat inputFrame) {
        return mPuzzle15.puzzleFrame(inputFrame);
    }


}
