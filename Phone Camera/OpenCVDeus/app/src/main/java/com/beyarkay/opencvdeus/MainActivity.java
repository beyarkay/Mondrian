
package com.beyarkay.opencvdeus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.Html;
import android.util.Log;
import android.util.Rational;
import android.util.Size;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageAnalysisConfig;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.aruco.Aruco;
import org.opencv.aruco.Dictionary;
import org.opencv.aruco.GridBoard;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.imgproc.Imgproc;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MainActivity extends AppCompatActivity implements View.OnTouchListener {


    private static final String TAG = "(╯°□°)╯︵ ┻━┻";
    private static final String ENDPOINT_SERIAL = "serial?data=";
    private static final String ENDPOINT_LED = "led?state=";
    private static final String ENDPOINT_ROOT = "";
    private static final String CMD_LEFT = "l";
    private static final String CMD_RIGHT = "r";
    private static final String CMD_FOREWARDS = "f";
    private static final String CMD_BACKWARDS = "b";
    private static final String CMD_HALT = "h";

    private int REQUEST_CODE_PERMISSIONS = 101;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE"};
    TextureView textureView;
    ImageView ivBitmap;

    ImageAnalysis imageAnalysis;
    Preview preview;

    Button btnLeft, btnRight, btnForewards, btnBackwards, btnConnect;

    EditText etData;
    TextView tvHtml;

    Dictionary dictionary;
    Mat boardMat;
    GridBoard arucoBoard;


    static {
        if (!OpenCVLoader.initDebug())
            Log.d("ERROR", "Unable to load OpenCV");
        else
            Log.d("SUCCESS", "OpenCV loaded");
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnBackwards = findViewById(R.id.btnBackwards);
        btnForewards = findViewById(R.id.btnForewards);
        btnLeft = findViewById(R.id.btnLeft);
        btnRight = findViewById(R.id.btnRight);
        btnConnect = findViewById(R.id.btnConnect);

        btnBackwards.setOnTouchListener(this);
        btnForewards.setOnTouchListener(this);
        btnLeft.setOnTouchListener(this);
        btnRight.setOnTouchListener(this);
        btnConnect.setOnTouchListener(this);

        textureView = findViewById(R.id.textureView);
        ivBitmap = findViewById(R.id.ivBitmap);

        etData = findViewById(R.id.etData);
        tvHtml = findViewById(R.id.tvHtml);

        int markersX = 5;
        int markersY = 5;
        int boardMatLength = markersX * 100;
        float markerSizeMeters = 0.0031f;
        float markerSeparation = 0.0008f;
        dictionary = Aruco.getPredefinedDictionary(Aruco.DICT_4X4_250);

        boardMat = new Mat(boardMatLength, boardMatLength, CvType.CV_8UC3);

        arucoBoard = GridBoard.create(markersX, markersY, markerSizeMeters, markerSeparation, dictionary);
        arucoBoard.draw(new org.opencv.core.Size(boardMatLength, boardMatLength), boardMat, 10);

//        List<Mat> objPoints = new ArrayList<>();
//        // for each of the six sides of the cube
//        for (int i = 0; i < 6; i++) {
//            // add a new set of 4 coordinates for the corners of the face of the cube
//            objPoints.add(new MatOfPoint3f(
//                    // 4 points, one for each corner of the Aruco marker, c
//                    new Point3(0, 0, 0),
//                    new Point3(0, 0, 0),
//                    new Point3(0, 0, 0),
//                    new Point3(0, 0, 0)
//            ));
//        }


//        Mat ids = new Mat();
//        ids.put(0, 0, new int[]{0, 1, 2, 3, 4, 5, 6});

//        aruCube = Board.create(objPoints, dictionary, ids);


        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
    }

    //# Variables
    //nx = 2
    //ny = 2
    //num_gridboards = 4
    //marker_size = 0.025 # 25mm
    //between_markers = 0.005
    //aruco_dict = aruco.Dictionary_get(aruco.DICT_4X4_250)
    //save_dir = "generated/gridboards"
//
//    private GridBoard[] createGridboards(
//            int nx, int ny, float marker_size,
//            float between_markers, Dictionary aruco_dict,
//            int num_gridboards){
//
//
//
//    }

//    private void cheeky(rvec0, tvec0){
//
//        /*
//        R, _ = cv2.Rodrigues(rvec0)
//        M = np.linalg.inv(np.matmul(rotation_matrix, np.identioty(3));
//
//
//        def convert_to_2d(point):
//            return np.matmul(M, p - tvec0)
//
//        def convert_to_3d(point):
//            rot_mat, _ = cv2.Rodrigues(rvec0)
//            t = np.matmul(rot_mat, p)
//            return t + tvec0
//
//         */
//
//    }

    private void startCamera() {
        CameraX.unbindAll();
        preview = setPreview();
        imageAnalysis = setImageAnalysis();

        //bind to lifecycle:
        CameraX.bindToLifecycle(this, preview, imageAnalysis);
    }

    private Preview setPreview() {
        Rational aspectRatio = new Rational(textureView.getWidth(), textureView.getHeight());
        Size screenSize = new Size(textureView.getWidth(), textureView.getHeight());

        PreviewConfig pConfig = new PreviewConfig.Builder()
                .setTargetAspectRatio(aspectRatio)
                .setTargetResolution(screenSize)
                .setLensFacing(CameraX.LensFacing.BACK)
                .build();
        Preview preview = new Preview(pConfig);

        preview.setOnPreviewOutputUpdateListener(
                new Preview.OnPreviewOutputUpdateListener() {
                    @Override
                    public void onUpdated(Preview.PreviewOutput output) {
                        ViewGroup parent = (ViewGroup) textureView.getParent();
                        parent.removeView(textureView);
                        parent.addView(textureView, 0);
                        textureView.setSurfaceTexture(output.getSurfaceTexture());
                        updateTransform();
                    }
                });
        return preview;
    }

    @SuppressLint("DefaultLocale")
    private ImageAnalysis setImageAnalysis() {
        HandlerThread analyzerThread = new HandlerThread("OpenCVAnalysis");
        analyzerThread.start();

        ImageAnalysisConfig imageAnalysisConfig = new ImageAnalysisConfig.Builder()
                .setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
                .setCallbackHandler(new Handler(analyzerThread.getLooper()))
                .setImageQueueDepth(1).build();

        ImageAnalysis imageAnalysis = new ImageAnalysis(imageAnalysisConfig);

        imageAnalysis.setAnalyzer(
                (image, rotationDegrees) -> {
                    //Analyzing live camera feed begins.
                    final Bitmap bitmap = textureView.getBitmap();
                    float length = 39.0f;   // aruco square side length (mm)

                    if (bitmap == null) {
                        return;
                    }
                    Mat screenMatrix = new Mat();
                    // Convert the bitmap to a matrix
                    Utils.bitmapToMat(bitmap, screenMatrix);

                    // Remove the alpha channel
                    Imgproc.cvtColor(screenMatrix, screenMatrix, Imgproc.COLOR_RGBA2RGB);

                    // Define output variables
                    List<Mat> corners = new ArrayList<>();
                    Mat ids = new Mat();

                    // Find all the markers on the screen
                    Aruco.detectMarkers(screenMatrix, dictionary, corners, ids);

                    if (ids.total() > 0) {
                        Aruco.drawDetectedMarkers(screenMatrix, corners, ids);


                        Mat cameraMatrix = getCameraMatrix();
//                            MatOfDouble distCoeffs = new MatOfDouble(0, 0, 0, 0);
                        MatOfDouble distCoeffs = new MatOfDouble(
                                1.35662720e+00,
                                9.08181872e+01,
                                2.20891497e-03,
                                -1.22403374e-03,
                                3.28137049e+02,
                                1.07859235e+00,
                                8.98243136e+01,
                                3.22195142e+02
                        );
                        /*
                        array([[ 1.35662720e+00],
                               [ 9.08181872e+01],
                               [ 2.20891497e-03],
                               [-1.22403374e-03],
                               [ 3.28137049e+02],
                               [ 1.07859235e+00],
                               [ 8.98243136e+01],
                               [ 3.22195142e+02],
                               [ 0.00000000e+00],
                               [ 0.00000000e+00],
                               [ 0.00000000e+00],
                               [ 0.00000000e+00],
                               [ 0.00000000e+00],
                               [ 0.00000000e+00]])
                         */
                        Mat rvec = new Mat(1, 3, CvType.CV_64F);
                        Mat tvec = new Mat(1, 3, CvType.CV_64F);

                        int valid = Aruco.estimatePoseBoard(corners, ids, arucoBoard, cameraMatrix, distCoeffs, rvec, tvec);
                        if (valid > 0) {
                            Aruco.drawAxis(
                                    screenMatrix,
                                    cameraMatrix,
                                    distCoeffs,
                                    rvec,
                                    tvec,
                                    0.005f

                            );
                        }
//                            Aruco.estimatePoseSingleMarkers(
//                                    corners,
//                                    length,
//                                    cameraMatrix,
//                                    distCoeffs,
//                                    rvec,
//                                    tvec
//                            );

//                            for (int i = 0; i < ids.total(); i++) {
//
//                                MatOfPoint3f objectPoints = new MatOfPoint3f();
//                                MatOfPoint2f imagePoints = new MatOfPoint2f();
//                                Calib3d.projectPoints(
//                                        objectPoints,
//                                        rvec,
//                                        tvec,
//                                        cameraMatrix,
//                                        distCoeffs,
//                                        imagePoints
//                                );
//
//                                Aruco.drawAxis(
//                                        screenMatrix,
//                                        cameraMatrix,
//                                        distCoeffs,
//                                        rvec.row(i),
//                                        tvec.row(i),
//                                        length / 2.0f
//                                );
//                            }
                    }


//                            Imgproc.cvtColor(boardMat, boardMat, Imgproc.COLOR_GRAY2RGB);

//                        Log.d(TAG, "boardMat.type(): " + boardMat.type());
//                        Log.d(TAG, "screenMatrix.type(): " + screenMatrix.type());
//                        boardMat.copyTo(screenMatrix.submat(new Rect(0, 0, boardMat.cols(), boardMat.rows())));


                    final Bitmap output = addBitmaps(boardMat, screenMatrix);
//                        final Bitmap output = Bitmap.createBitmap(screenMatrix.width(), screenMatrix.height(), Bitmap.Config.ARGB_8888);
//                        Utils.matToBitmap(screenMatrix, output);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ivBitmap.setImageBitmap(output);
                        }
                    });
                });
        return imageAnalysis;
    }

    private void getRelativeRVecAndTVec(Mat rvec0, Mat tvec0, Mat rvec1, Mat tvec1) {
    }


    private Mat getCameraMatrix() {
        /*
        array([[760.02078232,   0.        , 462.51719693],
               [  0.        , 760.02078232, 233.01656859],
               [  0.        ,   0.        ,   1.        ]])
         */

        float f_x = 760.02078232f;
        float f_y = 760.02078232f;
        float c_x = 462.51719693f;
        float c_y = 233.01656859f;

        // make and populate a camera matrix with f_x, f_y, c_x, and c_y
        float[][] arr = new float[][]{
                new float[]{f_x, 0f, c_x},
                new float[]{0f, f_y, c_y},
                new float[]{0f, 0f, 1f}
        };
        Mat cameraMatrix = new Mat(3, 3, CvType.CV_64F);
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++)
                cameraMatrix.put(row, col, arr[row][col]);
        }
        return cameraMatrix;
    }


    private Bitmap addBitmaps(Mat smallMat, Mat bigMat) {

        Bitmap smallBitmap = Bitmap.createBitmap(smallMat.width(), smallMat.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(smallMat, smallBitmap);

        Bitmap bigBitmap = Bitmap.createBitmap(bigMat.width(), bigMat.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(bigMat, bigBitmap);

        Bitmap result = Bitmap.createBitmap(bigMat.width(), bigMat.height(), Bitmap.Config.ARGB_8888);
        Canvas cv = new Canvas(result);

        cv.drawBitmap(bigBitmap, 0, 0, null);
        Paint alphaPaint = new Paint();
        alphaPaint.setAlpha(50);
        cv.drawBitmap(smallBitmap, 0, 0, alphaPaint);
        cv.save();
        cv.restore();

        return result;
    }

//    private static Bitmap combineMatrixesToBitmap(Mat[] matrices) {
//        Bitmap[] bitmaps = new Bitmap[4];
//
//        for (int i = 0; i < matrices.length; i++) {
//            bitmaps[i] = Bitmap.createBitmap(matrices[i].width(), matrices[i].height(), Bitmap.Config.ARGB_8888);
//            Utils.matToBitmap(matrices[i], bitmaps[i]);
//        }
//
//        Bitmap result;
//        try {
//            if (bitmaps[0] == null) {
//                return null;
//            }
//            int bgWidth = bitmaps[0].getWidth();
//            int bgHeight = bitmaps[0].getHeight();
//
//            for (int i = 0; i < bitmaps.length; i++) {
//
//            }
//
//            result = Bitmap.createBitmap(bgWidth, bgHeight, Bitmap.Config.ARGB_8888);
//            Canvas cv = new Canvas(result);
//            cv.drawBitmap(bitmaps[0], 0, 0, null);
////            cv.drawBitmap(bitmaps[1], (bgWidth) / 2, 0, null);
////            cv.drawBitmap(bitmaps[2], 0, (bgHeight / 2), null);
////            cv.drawBitmap(bitmaps[3], (bgWidth) / 2, (bgHeight / 2), null);
//            cv.save();
//            cv.restore();
//            return result;
//        } catch (Exception e) {
//            return null;
//        }
//    }

    private void updateTransform() {
        Matrix matrix = new Matrix();
        float w = textureView.getMeasuredWidth();
        float h = textureView.getMeasuredHeight();

        float cX = w / 2f;
        float cY = h / 2f;

        int rotationDegrees;
        int rotation = (int) textureView.getRotation();

        switch (rotation) {
            case Surface.ROTATION_0:
                rotationDegrees = 0;
                break;
            case Surface.ROTATION_90:
                rotationDegrees = 90;
                break;
            case Surface.ROTATION_180:
                rotationDegrees = 180;
                break;
            case Surface.ROTATION_270:
                rotationDegrees = 270;
                break;
            default:
                return;
        }
        matrix.postRotate((float) rotationDegrees, cX, cY);
        textureView.setTransform(matrix);
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    public void onClick(View view) {
        view.setEnabled(false);
        switch (view.getId()) {
            case R.id.btnConnect:
                Log.d(TAG, "onClick: Attempting to Connect to riversong - melodypond");
                if (connectToNetworkWPA("riversong", "melodypond")) {
                    ((Button) view).setText(getString(R.string.connected));
                } else {
                    ((Button) view).setText(getString(R.string.connect));
                }
                break;
            case R.id.btnSend:
                sendHttp(etData.getText().toString());
                break;
        }
        view.setEnabled(true);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_calibrate) {
            Toast.makeText(MainActivity.this, "Starting Calibration", Toast.LENGTH_LONG).show();


            Intent intent = new Intent(this, CalibrateCameraActivity.class);
            startActivity(intent);

            return true;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if (view.getId() == R.id.btnConnect || view.getId() == R.id.btnSend) {
            return false;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.d(TAG, ((Button) view).getText().toString() + " PUSHED");
                switch (view.getId()) {
                    case R.id.btnForewards:
                        sendHttp(ENDPOINT_SERIAL + CMD_FOREWARDS);
                        break;
                    case R.id.btnBackwards:
                        sendHttp(ENDPOINT_SERIAL + CMD_BACKWARDS);
                        break;
                    case R.id.btnLeft:
                        sendHttp(ENDPOINT_SERIAL + CMD_LEFT);
                        break;
                    case R.id.btnRight:
                        sendHttp(ENDPOINT_SERIAL + CMD_RIGHT);
                        break;
                }
                view.performClick();
                break;
            case MotionEvent.ACTION_UP:
                sendHttp(ENDPOINT_SERIAL + CMD_HALT);
                view.performClick();
                break;
        }
        return true;
    }

    public boolean connectToNetworkWPA(String networkSSID, String password) {
        try {
            WifiConfiguration conf = new WifiConfiguration();
            conf.SSID = "\"" + networkSSID + "\"";
            conf.preSharedKey = "\"" + password + "\"";

            conf.status = WifiConfiguration.Status.ENABLED;
            conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);

            Log.d(TAG, "connectToNetworkWPA: Connecting to: " + conf.SSID + " " + conf.preSharedKey);

            WifiManager wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            wifiManager.addNetwork(conf);

            List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
            for (WifiConfiguration i : list) {
                if (i.SSID != null && i.SSID.equals("\"" + networkSSID + "\"")) {
                    wifiManager.disconnect();
                    wifiManager.enableNetwork(i.networkId, true);
                    wifiManager.reconnect();
                    Log.d(TAG, "connectToNetworkWPA: reconnecting" + i.SSID + " " + conf.preSharedKey);
                    break;
                }
            }

            //WiFi Connection success, return true
            Log.d(TAG, "connectToNetworkWPA: Current SSID: " + wifiManager.getConnectionInfo().getSSID());
            return wifiManager.getConnectionInfo().getSSID().equals(networkSSID);

        } catch (Exception ex) {
            System.out.println(Arrays.toString(ex.getStackTrace()));
            return false;
        }
    }

    private void sendHttp(String data) {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://192.168.4.1/" + data;
        Log.d(TAG, "sendHttp: " + url);
        tvHtml.setText("sendHttp: " + url);


        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        Log.d(TAG, "Response is: " + response.substring(0, Math.min(response.length(), 500)));
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            tvHtml.setText(Html.fromHtml(response, Html.FROM_HTML_MODE_LEGACY));    // ..._LEGACY Flag ensures line breaks in the right places
                        } else {
                            tvHtml.setText(Html.fromHtml(response));
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                StringWriter sw = new StringWriter();
                error.printStackTrace(new PrintWriter(sw));
                Log.e(TAG, sw.toString());

                tvHtml.setText(System.currentTimeMillis() + ": \n" + sw.toString());

                Log.d(TAG, "That didn't work!: ");
                error.printStackTrace();
            }
        });

// Add the request to the RequestQueue.
        queue.add(stringRequest);
    }
}