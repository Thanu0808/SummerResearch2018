package com.example.a96llegend.ar4ece.Gate;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.util.SparseArray;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class TextFrameProcessor extends Detector<TextBlock> {

    private static Detector<TextBlock> mDelegate;
    private static int roi_x;
    private static int roi_y;
    private static int roi_length;

    public TextFrameProcessor(Detector<TextBlock> delegate) {
        mDelegate = delegate;
    }

    public void setRoi(int x, int y, int l){
        roi_x = x;
        roi_y = y;
        roi_length = l;
    }

    //Crop the frame
    public SparseArray<TextBlock> detect(Frame frame) {

        //Get Frame size
        int width = frame.getMetadata().getWidth();
        int height = frame.getMetadata().getHeight();
        YuvImage yuvImage = new YuvImage(frame.getGrayscaleImageData().array(),
                ImageFormat.NV21, width, height, null);

        //Get Byte array as YUV array
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, width, height), 100, byteArrayOutputStream);

        //Form BitMap from YUV Array
        byte[] jpegArray = byteArrayOutputStream.toByteArray();
        Bitmap full_BitMap = BitmapFactory.decodeByteArray(jpegArray, 0, jpegArray.length);

        //Get roi in BitMap
        Bitmap roi_BitMap = Bitmap.createBitmap(full_BitMap, roi_x, roi_y, (int)Math.round(roi_length * 0.7), roi_length);

        //Convert BitMap to Frame
        Frame roi_Frame = new Frame.Builder().setBitmap(roi_BitMap).build();
        return mDelegate.detect(roi_Frame);
    }

    public boolean isOperational() {
        return mDelegate.isOperational();
    }

    public boolean setFocus(int id) {
        return mDelegate.setFocus(id);
    }
}

