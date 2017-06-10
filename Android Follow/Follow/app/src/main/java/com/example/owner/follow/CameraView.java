package com.example.owner.follow;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CameraView extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback{

    private SurfaceHolder mHolder;
    private Camera mCamera;

    private int width = 320, height = 240; //size of the preview image
    private boolean isFlashOn = false; //if the flash is on
    private List<ImageView> mBlocks = new ArrayList<ImageView>(); //list of buttons
    private int[] mBlocksMedian = new int[5]; //median value of each block
    private int colorDivisor = 126; //if >  than colorDivisor then is black
    private MainActivity parent;

    public CameraView(Context context, Camera camera){
        super(context);

        parent = (MainActivity)context;
        mCamera = camera;

        Camera.Parameters p = mCamera.getParameters();
        p.setPreviewSize(width, height);
        mCamera.setParameters(p);

        mCamera.setDisplayOrientation(90);
        //get the holder and set this class as the callback, so we can get camera data here
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_NORMAL);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try{
            //when the surface is created, we can set the camera to draw images in this surfaceholder
            mCamera.setPreviewDisplay(surfaceHolder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d("ERROR", "Camera error on surfaceCreated " + e.getMessage());
        }

        mBlocks.add((ImageView)parent.findViewById(R.id.btn1));
        mBlocks.add((ImageView)parent.findViewById(R.id.btn2));
        mBlocks.add((ImageView)parent.findViewById(R.id.btn3));
        mBlocks.add((ImageView)parent.findViewById(R.id.btn4));
        mBlocks.add((ImageView)parent.findViewById(R.id.btn5));

        mCamera.setPreviewCallback(this);
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
//        // If your preview can change or rotate, take care of those events here.
//        // Make sure to stop the preview before resizing or reformatting it.
//        if(mHolder.getSurface() == null)//check if the surface is ready to receive camera data
//            return;
//
//        try{
//            mCamera.stopPreview();
//        } catch (Exception e){
//            // ignore: tried to stop a non-existent preview
//        }
//
//        try{
//            mCamera.setPreviewDisplay(mHolder);
//            mCamera.startPreview();
//        } catch (IOException e) {
//            Log.d("ERROR", "Error starting camera preview " + e.getMessage());
//        }
//
//        mCamera.setPreviewCallback(this);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        //our app has only one screen, so we'll destroy the camera in the surface
        //if you are using more screens, please move this code your activity
        mCamera.stopPreview();
        mCamera.release();
    }

    public void switchFlash() {
        Camera.Parameters p = mCamera.getParameters();
        if(isFlashOn)
            p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        else
            p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        isFlashOn = !isFlashOn;
        mCamera.setParameters(p);
    }

    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
        synchronized (this) {

            int mBlocksQtd[] = new int[5]; //count number of pixels per block
            Arrays.fill(mBlocksMedian, 0); //we reset the block values

            int initialLine = 295; //where we start to analyze
            int finalLine = 300; //the final line to analyze
            int i, j = 5;

            for (i = 0; i < 240; i++) {
                if (i % 48 == 0) //we dived the 240 line in 5 blocks, each one with 48px
                    j--; //each 48px, we successfully read a block
                int value = 0;
                for (int k = initialLine; k < finalLine; k++) //for each line we read the value of the pixel
                    value += (bytes[k + 320 * i])  & 0xFF; //convert to a byte
                value /= (finalLine - initialLine); //get the median of this pixel

                mBlocksMedian[j] += value; //add the pixel median to the block
                mBlocksQtd[j]++; //add 1 to the number of pixels analyzed in each block.
                // Total will be 48, just use it to be easier to change the code :)
            }

            for (i = 0; i < 5; i++)
                mBlocksMedian[i] = mBlocksMedian[i] / mBlocksQtd[i]; //get the media value of each block

            //if we are not processing, we need to display the camera results in the buttons
            for(i = 0; i < 5; i++)
                mBlocks.get(i).setBackgroundColor(mBlocksMedian[i] > colorDivisor ? Color.WHITE : Color.BLACK);
        }
    }
}