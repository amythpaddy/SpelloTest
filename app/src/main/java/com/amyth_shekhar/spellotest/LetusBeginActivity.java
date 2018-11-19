package com.amyth_shekhar.spellotest;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
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

public class LetusBeginActivity extends FullScreenActivity {

    //Set up Values
    private static final String TAG = "Spello Alignment";
    private static final int RC_HANDLE_CAMERA_PERM = 2;

    private CameraSource mCameraSource;
    private CameraSourcePreview mPreview;
    TessBaseAPI mTess;
    private int ImageHeight=1080, ImageWidth=1920;
    private int MSERLowerLimit =700, MSERHigherLimit = 830;
    private int ScalingFactor =4;
    private String LowerLimit = "MserLowerLimit";
    private String UpperLimit = "MserHigherLimit";
    private String AlignmentDone="AlignemtDone";

    Button Check ;
    Button CheckAgain,Next;
    TextView tv1, tv2, tv3,tv4;

    //Open Cv Initialisation
    static {
        if(OpenCVLoader.initDebug())
        {
            Log.i(TAG, "OpenCV initialised");
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_letus_begin);
        mPreview = findViewById(R.id.CameraSourcePreview);
        tv1 = findViewById(R.id.LetUsBeginText1);
        tv2 = findViewById(R.id.LetUsBeginText2);
        tv3= findViewById(R.id.LetUsBeginText3);
        tv4= findViewById(R.id.LetUsBeginText4);

        InitTess();
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource();
        } else {
            requestCameraPermission();
        }

        Check = findViewById(R.id.AlignButton);
        CheckAgain = findViewById(R.id.AlignAgainButton);
        Next = findViewById(R.id.NextButton);
        Check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    StartAlignment();
            }
        });

        CheckAgain.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               StartAlignment();

           }
        });
        Next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StartTestActivity();
            }
        });


    }


    @SuppressLint("InlinedApi")
    private void createCameraSource() {
        mCameraSource = new CameraSource.Builder(getApplicationContext())
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .build();
    }


    private void requestCameraPermission() {
        Log.w(TAG, "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

        final Activity thisActivity = this;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_HANDLE_CAMERA_PERM);
            }
        };

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

    private void startCameraSource() throws SecurityException {
        // check that the device has play services available.
        if (mCameraSource != null) {
            try
            {
                Log.i(TAG, "Starting Preview");
                mPreview.start(mCameraSource);

            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }

    }




    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
        {
            Toast.makeText(this, "Permissions aren't available", Toast.LENGTH_SHORT).show();
            return;
        }
        startCameraSource();
        Log.i(TAG, "Creating Camera Source for Preview Genration");
        FocusTheCamera();

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mPreview != null) {
            mPreview.stop();
        }
    }

    /**
     * Releases the resources associated with the camera source, the associated detectors, and the
     * rest of the processing pipeline.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPreview != null) {
            mPreview.release();
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

    private String RecognizeSentenceCaptial(Bitmap temp)
    {
        Mat matA = new Mat();
        bitmapToMat(temp, matA, false);
        Mat TestMat = new Mat();
        matA.copyTo(TestMat);
        Imgproc.cvtColor(matA,matA, Imgproc.COLOR_RGB2GRAY);
        Rect FinalRect = MserBase(TestMat);
        if (FinalRect==null)
        {
            Toast.makeText(this, "No Text Found", Toast.LENGTH_SHORT).show();
            return "";
        }
        Mat matB = new Mat(matA,FinalRect);
        //Core.bitwise_not(matB,matB);
        Imgproc.bilateralFilter(matB,matA,5,10,2.5);
        matB=matA;
        Bitmap bmp = Bitmap.createBitmap(matB.width(), matB.height(), Bitmap.Config.ARGB_8888);
        matToBitmap(matB, bmp, true);
        mTess.setImage(bmp);
        mTess.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        String ocrResult = mTess.getUTF8Text();
        String newResult = ocrResult.replaceAll("[^A-Za-z]", "");
        Log.w(TAG, "SpellEnglishLettersDatabase Recognized:- " +  newResult + " " + mTess.meanConfidence());
        bmp.recycle();
        bmp=null;
        mTess.clear();
        return newResult;
    }


    //Opencv Functions

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
            int bottomy = (int)(listpoint.get(i).pt.y + 0.5*listpoint.get(i).size );
            avg+=posy;
            if(posy<min)
                min = posy;
            if(bottomy>max)
                max = bottomy;
        }


        avg = avg/listpoint.size();
        MSERLowerLimit = min-10;
        MSERHigherLimit = (avg + (avg-min) + 50);
        Log.i(TAG,"Mser Alignment Done. Avg :- " + avg + "  LowerLimit :- " + (min-10) + "  Higher Limit :- " +(avg + (avg-min) + 10)  );
        String result = RecognizeSentenceCaptial(bmp);
        bmp.recycle();
        bmp=null;


        if(result.length()>=9) {
            result = result.substring(0, 9);
            Log.i(TAG, "New Result" + result);
        }


        if(result.equalsIgnoreCase("LETUSBEGI") )
        {
            if(((MSERHigherLimit) - (MSERLowerLimit)) < 290)
            {
                ParamAlignmentDone(true);
                Log.i(TAG, "Phone Placement Verified");
                SharedPreferences mSettings = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = mSettings.edit();
                editor.putBoolean(AlignmentDone, true);
                editor.putInt(LowerLimit, MSERLowerLimit);
                editor.putInt(UpperLimit, MSERHigherLimit);
                editor.apply();
                Log.i(TAG, "Shared Preference Updated");
            }
            else
            {
                ParamAlignmentDone(false);
                Log.e(TAG, "Mser Height issue" + (MSERHigherLimit-MSERLowerLimit));
            }
        }
        else
        {
            ParamAlignmentDone(false);
            Log.e(TAG, "Phone Placement Not Verified");
        }

    }

    private Rect MserBase(Mat matA)
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

        SharedPreferences mSettings = PreferenceManager.getDefaultSharedPreferences(this);

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
        int newY = (int)(listpoint.get(0).pt.y  - 0.5 * listpoint.get(0).size);
        int newHeight = (int) (listpoint.get(0).size);
        int newWidth = ( (int)(listpoint.get(listpoint.size()-1).pt.x + 0.5 * listpoint.get(listpoint.size()-1).size) - newX )+ 75;
        int posy;
        int tempHeight;
        for(int i=0;i<listpoint.size();i++)
        {
            posy= (int)(listpoint.get(i).pt.y- 0.5*listpoint.get(i).size );
            if(posy>=MSERLowerLimit && posy<=MSERHigherLimit)
            {
                tempHeight = (int) (listpoint.get(i).size);
                if (posy < newY)
                    newY = posy;
                if (newHeight < tempHeight)
                    newHeight = tempHeight;
            }
        }
        newY = newY-15;
        newHeight = newHeight+80;
        newY= MSERLowerLimit-10;
        newHeight = MSERHigherLimit +25 - MSERLowerLimit;


        Log.i(TAG,"X :- " + newX + " y:- " + newY + " width :- " + newWidth + " height :-" + newHeight);

        if(newX<=0)
            newX=1;
        if(newY<=0)
            newY=1;
        if(newX + newWidth > matA.width())
            newWidth = matA.width()-newX;
        if(newY+newHeight>matA.height())
            newHeight=matA.height()-newY;

        Rect HugeRectangle = new Rect(newX,newY,newWidth,newHeight);
        return HugeRectangle;
    }


    //Camera Functions
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


    public void FocusTheCamera()
    {
        Log.i(TAG, "Inital AutoFocusing Started");
        mCameraSource.autoFocus(new CameraSource.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success)
            {
                Log.i(TAG, "Inital AutoFocusing Done");
                ReadyToStartCapturing();
            }
        });
    }

    public void TakePicture()
    {
        BeginCapturing();
        mCameraSource.cancelAutoFocus();
        mCameraSource.autoFocus(ParamAlignment);
    }



    //Call Backs

    public void ParamAlignmentDone(Boolean bool)
    {
        Check.setEnabled(true);
        CheckAgain.setEnabled(true);
        if(bool)
        {
            Log.i(TAG, " Parameters Aligned");
            CheckAgain.setVisibility(View.GONE);
            Check.setVisibility(View.GONE);
            tv1.setVisibility(View.GONE);
            tv2.setVisibility(View.GONE);
            findViewById(R.id.LetUsBeginParent).setBackgroundColor(Color.parseColor("#69b90d"));
            mPreview.setVisibility(View.GONE);
            Next.setVisibility(View.VISIBLE);
            tv3.setVisibility(View.VISIBLE);
            tv4.setVisibility(View.VISIBLE);
            findViewById(R.id.LetUsBeginBracket).setVisibility(View.VISIBLE);
            findViewById(R.id.LetUsBeginCheck).setVisibility(View.VISIBLE);

        }
        else
        {
            Log.e(TAG, " Parameters not Aligned");
            Check.setVisibility(View.GONE);
            CheckAgain.setVisibility(View.VISIBLE);
            tv1.setText("Please move the stand to a spot with better lighting \n Or");
            tv2.setText("Move the device left/right");
            findViewById(R.id.LetUsBeginParent).setBackgroundColor(Color.parseColor("#ff7179"));
        }
    }

    public void ReadyToStartCapturing()
    {
        CameraParamsAlignment();
        Check.setEnabled(true);
        CheckAgain.setEnabled(true);
    }

    public void BeginCapturing()
    {
        Check.setEnabled(false);
        CheckAgain.setEnabled(false);
    }

    public void StartTestActivity()
    {
        /*Intent intent = new Intent(this, ActivityLetterRecognition.class);
        startActivity(intent);*/

        setResult(ALIGNMENT_SUCCESS);
        finish();
    }

    public void StartAlignment()
    {
        if(mPreview.mSurfaceAvailable && !mPreview.mStartRequested)
        {
            CameraParamsAlignment();
            TakePicture();
        }
        else
        {
            Toast.makeText(this, "Camera not Ready Yet", Toast.LENGTH_SHORT).show();
        }
    }


}
