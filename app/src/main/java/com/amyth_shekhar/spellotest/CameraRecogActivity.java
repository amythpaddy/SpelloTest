package com.amyth_shekhar.spellotest;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.opencv.android.Utils.bitmapToMat;
import static org.opencv.android.Utils.matToBitmap;

public class CameraRecogActivity extends FullScreenActivity {
    //Set up Variables Needed

    private static final int RC_HANDLE_CAMERA_PERM = 2;
    CameraSource mCameraSource;
    TessBaseAPI mTess;
    private int ImageHeight=1080, ImageWidth=1920;
    private int MSERLowerLimit =700, MSERHigherLimit = 830;
    private int ScalingFactor =4;



    //Open Cv Initialisation
    static {

        if(OpenCVLoader.initDebug())
        {
            Log.i(TAG, "OpenCV initialised");
        }
    }

    //Tessarect Functions

    public void InitTess()
    {

        String language = "eng";
        String datapath = getFilesDir() + "/tesseract/";
        mTess = new TessBaseAPI();
        checkFile(new File(datapath + "tessdata/"));
        mTess.init(datapath, language, TessBaseAPI.OEM_DEFAULT);
    }

    private void checkFile(File dir) {
        if (!dir.exists() && dir.mkdirs()) {
            copyFiles();
        }
        if (dir.exists()) {
            String datapath = getFilesDir() + "/tesseract/";
            String datafilepath = datapath + "/tessdata/eng.traineddata";
            File datafile = new File(datafilepath);

            if (!datafile.exists()) {
                copyFiles();
            }
        }
    }

    private void copyFiles() {
        try {
            String datapath = getFilesDir() + "/tesseract/";
            String filepath = datapath + "tessdata/eng.traineddata";
            AssetManager assetManager = getAssets();

            InputStream instream = assetManager.open("tessdata/eng.traineddata");
            OutputStream outstream = new FileOutputStream(filepath);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = instream.read(buffer)) != -1) {
                outstream.write(buffer, 0, read);
            }


            outstream.flush();
            outstream.close();
            instream.close();

            File file = new File(filepath);
            if (!file.exists()) {
                throw new FileNotFoundException();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String ScanForTextTessarect(Bitmap temp)
    {

        temp.setConfig(Bitmap.Config.ARGB_8888);
        mTess.setImage(temp);
        // mTess.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SINGLE_LINE);
        mTess.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        String ocrResult = mTess.getUTF8Text();
        String newResult = ocrResult.replaceAll("[^A-Za-z]", "");
        Log.i(TAG, "Extracted Text" + newResult + " " + mTess.meanConfidence());
        temp.recycle();
        temp=null;
        mTess.clear();
        return  newResult;
    }

    private void ScanForTextTessarectSmallLetter(Bitmap temp)
    {

        temp.setConfig(Bitmap.Config.ARGB_8888);
        mTess.setImage(temp);
        //mTess.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SINGLE_LINE);
        mTess.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "abcdefghijklmnopqrstuvwxyz");
        String ocrResult = mTess.getUTF8Text();
        Log.w("Processor", ocrResult + " " + mTess.meanConfidence());
        String newResult = ocrResult.replaceAll("[^A-Za-z]", "");
        temp.recycle();
        temp=null;
        mTess.clear();
    }


    //Initalisation Function to be called on create

    public void initialise()
    {

        InitTess();
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED)
            createCameraSource();
        else
            requestCameraPermission();
        SharedPreferences mSettings = PreferenceManager.getDefaultSharedPreferences(this);
        if(mSettings.getBoolean(AlignmentDone, false))
        {
            Log.i(TAG, "Alignemnt Already Done");

        }
        else
        {
            Log.e(TAG, "Alignment Not Done, may Cause Errors");
        }
        MSERLowerLimit = mSettings.getInt(LowerLimit, 700);
        MSERHigherLimit = mSettings.getInt(UpperLimit, 850);

    }

    private void requestCameraPermission()
    {
        final String[] permissions = new String[]{Manifest.permission.CAMERA};
        ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RC_HANDLE_CAMERA_PERM)
            if (grantResults[0] == PackageManager.PERMISSION_DENIED)
            {
                Toast.makeText(this, "Camera Permission Required.", Toast.LENGTH_SHORT).show();
                finish();
            }
            else
            {
                createCameraSource();
            }
    }


    private void createCameraSource()
    {
        Context context = getApplicationContext();
        mCameraSource = new CameraSource.Builder(context).setFacing(CameraSource.CAMERA_FACING_BACK).setRequestedFps(30f).build();
    }



    //Activity Life Cycle elements

    @Override
    protected void onResume() {
        super.onResume();
        if(mTess==null)
            InitTess();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
        {
            Toast.makeText(this, "Permissions aren't available", Toast.LENGTH_SHORT).show();
            return;
        }
        try
        {
            StartCamera();
        } catch (IOException e)
            {
                e.printStackTrace();
            } catch (Exception e)
                {
                    e.printStackTrace();
                }
    }

    @RequiresPermission(Manifest.permission.CAMERA)
    private void StartCamera() throws IOException, SecurityException {
        mCameraSource.start();
        Log.i(TAG, " Camera Started. Focusing The Camera To Begin");
        CameraParamsAlignment();
        FocusTheCamera();
        
    }

    @Override
    protected void onPause() {
        if (mCameraSource != null)
            mCameraSource.stop();
        super.onPause();


    }

    @Override
    protected void onDestroy() {
        if (mCameraSource != null) {
            mCameraSource.release();
            mCameraSource = null;
        }
        if(mTess!=null)
        {
            mTess.end();
            mTess=null;
        }


        super.onDestroy();
    }


    //Camera interfaces
    public void FocusTheCamera()
    {
        mCameraSource.autoFocus(new CameraSource.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success)
            {
                Log.i(TAG, "Inital AutoFocusing Done");
                ReadyToStartCapturing();
            }
        });
    }

    private void CameraParamsAlignment()
    {
        if (mCameraSource != null)
        {

            int tWidth = mCameraSource.getPictureSize().getWidth();
            int tHeight = mCameraSource.getPictureSize().getHeight();
            ImageHeight = (ImageWidth * tHeight) / (tWidth);

            if(tWidth > ImageWidth || tHeight > ImageWidth )
            {
                ScalingFactor=1;
                final int halfwdith = tWidth/2;
                final int halfheight = tHeight/2;
                while((halfheight/ScalingFactor)>= ImageHeight && (halfwdith/ScalingFactor) >= ImageWidth )
                {
                    ScalingFactor*=2;
                }
            }
            else
                ScalingFactor =1;
            Log.i(TAG, "Bitmap Image Width * Height * ScalingFactor" + ImageWidth + " X " + ImageHeight + " X " + ScalingFactor);
        }
    }

    //OverrideFunction
    public void ReadyToStartCapturing()
    {
    }
    //OverrideFunction
    public void BeginCapturing()
    {

    }




    //Picture Clicking

    CameraSource.AutoFocusCallback ParamAlignment = new CameraSource.AutoFocusCallback()
    {
        @Override
        public void onAutoFocus(boolean success)
        {
            mCameraSource.takePicture(null,  new CameraSource.PictureCallback()
            {
                @Override
                public void onPictureTaken(byte[] bytes)
                {
                    Bitmap Temp = null;
                    BitmapFactory.Options mOptions = new BitmapFactory.Options();
                    mOptions.inSampleSize = ScalingFactor;
                    mOptions.inJustDecodeBounds = false;
                    Temp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length,mOptions);
                    Bitmap image = Bitmap.createScaledBitmap(Temp, ImageWidth, ImageHeight, true);
                    Log.i(TAG, "Image Captured, Starting Alignemnt" );
                    Temp.recycle();
                    Temp=null;
                    MserCalculationAndChecking(image);
                    image.recycle();
                    image=null;
                }
            });

        }
    };


    CameraSource.AutoFocusCallback SentenceCaptial = new CameraSource.AutoFocusCallback()
    {
        @Override
        public void onAutoFocus(boolean success)
        {
            mCameraSource.takePicture(null,  new CameraSource.PictureCallback()
            {
                @Override
                public void onPictureTaken(byte[] bytes)
                {
                    Bitmap Temp = null;
                    BitmapFactory.Options mOptions = new BitmapFactory.Options();
                    mOptions.inSampleSize = ScalingFactor;
                    mOptions.inJustDecodeBounds = false;
                    Temp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length,mOptions);
                    Bitmap image = Bitmap.createScaledBitmap(Temp, ImageWidth, ImageHeight, true);
                    Log.i(TAG, "Image Captured, Recognizing now" );
                    RecognitionDone(RecognizeSentenceCaptial(image));
                    Temp.recycle();
                    Temp=null;
                    image.recycle();
                    image=null;
                }
            });

        }
    };


    CameraSource.AutoFocusCallback LetterCaptial = new CameraSource.AutoFocusCallback()
    {
        @Override
        public void onAutoFocus(boolean success)
        {
            mCameraSource.takePicture(null,  new CameraSource.PictureCallback()
            {
                @Override
                public void onPictureTaken(byte[] bytes)
                {
                    Bitmap Temp = null;
                    BitmapFactory.Options mOptions = new BitmapFactory.Options();
                    mOptions.inSampleSize = ScalingFactor;
                    mOptions.inJustDecodeBounds = false;
                    Temp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length,mOptions);
                    Bitmap image = Bitmap.createScaledBitmap(Temp, ImageWidth, ImageHeight, true);
                    Log.i(TAG, "Image Captured, Recognizing now" );
                    RecognitionDone(RecognizeLetterCaptial(image));
                    Temp.recycle();
                    Temp=null;
                    image.recycle();
                    image=null;
                }
            });

        }
    };

    CameraSource.AutoFocusCallback SentenceSmall = new CameraSource.AutoFocusCallback()
    {
        @Override
        public void onAutoFocus(boolean success)
        {
            mCameraSource.takePicture(null,  new CameraSource.PictureCallback()
            {
                @Override
                public void onPictureTaken(byte[] bytes)
                {
                    Bitmap Temp = null;
                    BitmapFactory.Options mOptions = new BitmapFactory.Options();
                    mOptions.inSampleSize = ScalingFactor;
                    mOptions.inJustDecodeBounds = false;
                    Temp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length,mOptions);
                    Bitmap image = Bitmap.createScaledBitmap(Temp, ImageWidth, ImageHeight, true);
                    Log.i(TAG, "Image Captured, Recognizing now" );

                    RecognitionDone(RecognizeSentenceSmall(image));
                    Temp.recycle();
                    Temp=null;
                    image.recycle();
                    image=null;
                }
            });

        }
    };


    CameraSource.AutoFocusCallback LetterSmall = new CameraSource.AutoFocusCallback()
    {
        @Override
        public void onAutoFocus(boolean success)
        {
            mCameraSource.takePicture(null,  new CameraSource.PictureCallback()
            {
                @Override
                public void onPictureTaken(byte[] bytes)
                {
                    Bitmap Temp = null;
                    BitmapFactory.Options mOptions = new BitmapFactory.Options();
                    mOptions.inSampleSize = ScalingFactor;
                    mOptions.inJustDecodeBounds = false;
                    Temp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length,mOptions);
                    Bitmap image = Bitmap.createScaledBitmap(Temp, ImageWidth, ImageHeight, true);
                    Log.i(TAG, "Image Captured, Recognizing now" );

                    RecognitionDone(RecognizeLetterSmall(image));
                    Temp.recycle();
                    Temp=null;
                    image.recycle();
                    image=null;
                }
            });

        }
    };



    public void TakePicture(CameraSource.AutoFocusCallback mCallBack)
    {
        BeginCapturing();
        mCameraSource.cancelAutoFocus();
        mCameraSource.autoFocus(mCallBack);
    }


    //Image Analysis Code

    private class Sortbyroll implements Comparator<KeyPoint>
    {


        @Override
        public int compare(KeyPoint k1, KeyPoint k2) {
            if  (k1.pt.x > k2.pt.x)
            {
                return 1;
            }
            else
            {
                return -1;
            }
        }
    }


    private void MserCalculationAndChecking(Bitmap bmp)
    {
        Mat matA = new Mat();
        bitmapToMat(bmp, matA, false);
        Mat matB = new Mat();
        Imgproc.cvtColor(matA,matB, Imgproc.COLOR_RGB2GRAY);
        Imgproc.blur(matB,matA,new Size(9,9));
        Imgproc.morphologyEx(matA,matA, Imgproc.MORPH_DILATE, Imgproc.getStructuringElement(Imgproc.CV_SHAPE_RECT, new Size(11,9)));
        FeatureDetector fd = FeatureDetector.create(FeatureDetector.GRID_MSER);
        MatOfKeyPoint mokp = new MatOfKeyPoint();
        MatOfKeyPoint newMokp = new MatOfKeyPoint();
        Mat edges = new Mat();
        fd.detect(matA, mokp, edges);
        //Scalar CONTOUR_COLOR =new Scalar(255);
        //Features2d.drawKeypoints(matA, newMokp, matA, CONTOUR_COLOR, Features2d.DRAW_RICH_KEYPOINTS);
        if(mokp.empty())
        {
            Toast.makeText(this, "Please Place the Let Us Begin Card", Toast.LENGTH_SHORT).show();
            ParamAlignmentDone(false);
            return;
        }
        List<KeyPoint> listpoint = mokp.toList();
        int min=2000, max=0, avg=0;

        for(int i=0;i<listpoint.size();i++)
        {
            int posy= (int)(listpoint.get(i).pt.y- 0.5*listpoint.get(i).size );
            avg+=posy;
            if(posy<min)
                min = posy;
            if(posy>max)
                max = posy;
        }


        avg = avg/listpoint.size();
        MSERLowerLimit = min-10;
        MSERHigherLimit = (avg + (avg-min) + 10);
        Log.i(TAG,"Mser Alignment Done. Avg :- " + avg + "  LowerLimit :- " + (min-10) + "  Higher Limit :- " + (avg + (avg-min) + 10) );
        String result = RecognizeSentenceCaptial(bmp);
        bmp.recycle();
        bmp=null;
        SharedPreferences mSettings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putBoolean(AlignmentDone, true);
        editor.putInt(LowerLimit, MSERLowerLimit);
        editor.putInt(UpperLimit, MSERHigherLimit);
        editor.apply();
        Log.i(TAG,"Shared Preference Updated");

        if(result.equalsIgnoreCase("LETUSBEGIN"))
        {
            ParamAlignmentDone(true);
            Log.i(TAG, "Phone Placement Verified");
        }
        else
        {
            ParamAlignmentDone(false);
            Log.e(TAG, "Phone Placement Not Verified");
        }

    }

    private Rect MserBase(Mat matA, boolean isSmall) //
    {
        Mat matB = new Mat();
        Imgproc.cvtColor(matA,matB, Imgproc.COLOR_RGB2GRAY);
        Imgproc.blur(matB,matA,new Size(9,9));
        Imgproc.morphologyEx(matA,matA, Imgproc.MORPH_DILATE, Imgproc.getStructuringElement(Imgproc.CV_SHAPE_RECT, new Size(11,9)));
        FeatureDetector fd = FeatureDetector.create(FeatureDetector.GRID_MSER);
        MatOfKeyPoint mokp = new MatOfKeyPoint();
        Mat edges = new Mat();
        fd.detect(matA, mokp, edges);
        if(mokp.empty())
        {
            return null;
        }
        List<KeyPoint> listpoint = mokp.toList();
        List<KeyPoint> Newlistpoint = new ArrayList<KeyPoint>();


        for(int i=0;i<listpoint.size();i++)
        {
            int posy= (int)(listpoint.get(i).pt.y- 0.5*listpoint.get(i).size );
            if(posy>=MSERLowerLimit && posy<=MSERHigherLimit) {
                Newlistpoint.add(listpoint.get(i));
            }
        }
        mokp = new MatOfKeyPoint();
        mokp.fromList(Newlistpoint);
        if(mokp.empty())
        {
            return null;
        }
        listpoint = mokp.toList();

        Collections.sort(listpoint, new Sortbyroll());
        int newX = (int)(listpoint.get(0).pt.x - 0.5 * listpoint.get(0).size )-25;
        int newWidth = ( (int)(listpoint.get(listpoint.size()-1).pt.x + 0.5 * listpoint.get(listpoint.size()-1).size) - newX )+ 75;
        int newY,newHeight;
        if(isSmall)
        {
            newY= MSERLowerLimit+10;
            newHeight = MSERHigherLimit +25 - MSERLowerLimit;
        }
        else
        {
            newY= MSERLowerLimit-10;
            newHeight = MSERHigherLimit +35 - MSERLowerLimit;

        }


        if(newX<=0)
            newX=1;
        if(newY<=0)
            newY=1;
        if(newX + newWidth > matA.width())
            newWidth = matA.width()-newX;
        if(newY+newHeight>matA.height())
            newHeight=matA.height()-newY;

        Log.i(TAG,"X :- " + newX + " y:- " + newY + " width :- " + newWidth + " height :-" + newHeight);

        Rect HugeRectangle = new Rect(newX,newY,newWidth,newHeight);
        return HugeRectangle;
    }



    private String RecognizeSentenceCaptial(Bitmap temp)
    {
        Mat matA = new Mat();
        bitmapToMat(temp, matA, false);
        Mat TestMat = new Mat();
        matA.copyTo(TestMat);
        Imgproc.cvtColor(matA,matA, Imgproc.COLOR_RGB2GRAY);
        Rect FinalRect = MserBase(TestMat, false);
        if (FinalRect==null)
        {
            Log.e(TAG, "Found Nothing " );
            mTess.clear();
            return "";
        }
        Mat matB = new Mat(matA,FinalRect);

        Bitmap bmp = Bitmap.createBitmap(matB.width(), matB.height(), Bitmap.Config.ARGB_8888);
        matToBitmap(matB, bmp, true);
        mTess.setImage(bmp);
        mTess.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SINGLE_WORD);
        mTess.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        String ocrResult = mTess.getUTF8Text();
        String newResult = ocrResult.replaceAll("[^A-Za-z]", "");
        Log.i(TAG, "SpellEnglishLettersDatabase Recgnized:- " +  newResult + " " + mTess.meanConfidence());
        bmp.recycle();
        bmp=null;
        mTess.clear();
        return newResult;
    }

    private String RecognizeLetterCaptial(Bitmap temp)
    {
        Mat matA = new Mat();
        bitmapToMat(temp, matA, false);
        Mat TestMat = new Mat();
        matA.copyTo(TestMat);
        Imgproc.cvtColor(matA,matA, Imgproc.COLOR_RGB2GRAY);
        Rect FinalRect = MserBase(TestMat, false);
        if (FinalRect==null)
        {
            Log.e(TAG, "No Result:- ");
            mTess.clear();
            return "";
        }
        Mat matB = new Mat(matA,FinalRect);
        Imgproc.blur(matB,matB, new Size(7,7));
        Bitmap bmp = Bitmap.createBitmap(matB.width(), matB.height(), Bitmap.Config.ARGB_8888);
        matToBitmap(matB, bmp, true);
        mTess.setImage(bmp);
        mTess.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SINGLE_CHAR);
        mTess.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        String ocrResult = mTess.getUTF8Text();
        String newResult = ocrResult.replaceAll("[^A-Za-z]", "");
        Log.i(TAG, "SpellEnglishLettersDatabase Recgnized:- " +  newResult + " " + mTess.meanConfidence());
        bmp.recycle();
        bmp=null;
        mTess.clear();
        return newResult;
    }

    private String RecognizeSentenceSmall(Bitmap temp)
    {
        Mat matA = new Mat();
        bitmapToMat(temp, matA, false);
        Mat TestMat = new Mat();
        matA.copyTo(TestMat);
        Imgproc.cvtColor(matA,matA, Imgproc.COLOR_RGB2GRAY);
        Rect FinalRect = MserBase(TestMat,true);
        if (FinalRect==null)
        {
            Log.e(TAG, "No Data Found");
            mTess.clear();
            return "";
        }
        Mat matB = new Mat(matA,FinalRect);
        Bitmap bmp = Bitmap.createBitmap(matB.width(), matB.height(), Bitmap.Config.ARGB_8888);
        matToBitmap(matB, bmp, true);
        mTess.setImage(bmp);
        //mTess.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SINGLE_WORD);
        mTess.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "abcdefghijklmnopqrstuvwxyz");
        String ocrResult = mTess.getUTF8Text();
        String newResult = ocrResult.replaceAll("[^A-Za-z]", "");
        Log.w(TAG, "SpellEnglishLettersDatabase Recgnized:- " +  newResult + " " + mTess.meanConfidence());
        bmp.recycle();
        bmp=null;
        mTess.clear();
        return newResult;
    }

    private String RecognizeLetterSmall(Bitmap temp)
    {
        Mat matA = new Mat();
        bitmapToMat(temp, matA, false);
        Mat TestMat = new Mat();
        matA.copyTo(TestMat);
        Imgproc.cvtColor(matA,matA, Imgproc.COLOR_RGB2GRAY);
        Rect FinalRect = MserBase(TestMat, true);
        if (FinalRect==null)
        {
            Log.e(TAG, "Found Nothing ");
            mTess.clear();
            return "";
        }
        Mat matB = new Mat(matA,FinalRect);
        //Core.bitwise_not(matB,matB);
        //Core.bitwise_not(matB,matB);
        Imgproc.blur(matB,matB, new Size(7,7));
//        Imgproc.bilateralFilter(matB,matA,5,10,2.0);
//        matB=matA;
        Bitmap bmp = Bitmap.createBitmap(matB.width(), matB.height(), Bitmap.Config.ARGB_8888);
        matToBitmap(matB, bmp, true);
        mTess.setImage(bmp);
        mTess.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SINGLE_CHAR);
        mTess.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "abcdefghijklmnopqrstuvwxyz");
        String ocrResult = mTess.getUTF8Text();
        String newResult = ocrResult.replaceAll("[^A-Za-z]", "");
        Log.w(TAG, "SpellEnglishLettersDatabase Recgnized:- " +  newResult + " " + mTess.meanConfidence());
        bmp.recycle();
        bmp=null;
        mTess.clear();
        return newResult;
    }


    //Recognition Functions and CallBacks

    //Alignment Checking Call
    public void ParametersAlignementCheckBegin()
    {
        TakePicture(ParamAlignment);
    }

    //Alignemnt Cheking CallBack, to be overrided at the user. True if the params are ok, false if we can't see the image;
    public void ParamAlignmentDone(Boolean bool)
    {

    }



    //Recognition Functions and Call Backs
    public void RecogniseSentenceCapital(String msg)
    {
        TakePicture(SentenceCaptial);
    }

    public void RecogniseSentenceSmall(String msg)
    {
        TakePicture(SentenceSmall);
    }

    public void RecogniseLetterCapital(String msg)
    {
        TakePicture(LetterCaptial);
    }

    public void RecogniseLetterSmall(String msg)
    {
        TakePicture(LetterSmall);
    }




    //Recognition CallBack
    public void RecognitionDone(String spelling)
    {

    }














}
