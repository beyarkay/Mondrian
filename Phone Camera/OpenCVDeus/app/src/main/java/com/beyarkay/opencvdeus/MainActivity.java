
package com.beyarkay.opencvdeus;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Rational;
import android.util.Size;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageAnalysisConfig;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureConfig;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.aruco.Aruco;
import org.opencv.aruco.Dictionary;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    private static final String TAG = "(╯°□°)╯︵ ┻━┻";
    private final Scalar[] COLOURS = new Scalar[]{
            new Scalar(83, 200, 172),
            new Scalar(170, 214, 132),
            new Scalar(251, 248, 125),
            new Scalar(252, 198, 110),
            new Scalar(242, 152, 177),
            new Scalar(152, 118, 177),
            new Scalar(188, 135, 30),
            new Scalar(236, 168, 40),
            new Scalar(254, 252, 212),
            new Scalar(166, 219, 205),
            new Scalar(102, 184, 176),
            new Scalar(124, 194, 246),
            new Scalar(175, 129, 228),
            new Scalar(231, 132, 186),
            new Scalar(249, 193, 160),
            new Scalar(183, 246, 175)
    };

    private int REQUEST_CODE_PERMISSIONS = 101;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE"};
    TextureView textureView;
    ImageView ivBitmap;
    LinearLayout llBottom;

    int currentImageType = Imgproc.COLOR_RGB2GRAY;

    ImageCapture imageCapture;
    ImageAnalysis imageAnalysis;
    Preview preview;

    Button btnCapture, btnOk, btnCancel;
    int slider1;      // Threshold
    int slider2;      // size in mm
    int slider3;      // Canny thresh 1
    int slider4;      // Canny thresh 2
    int slider5;
    int slider6;


    TextView tvSlider2;
    TextView tvSlider1;
    TextView tvSlider4;
    TextView tvSlider3;
    TextView tvSlider6;
    TextView tvSlider5;

    LinearLayout llHue;
    LinearLayout llSat;

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


        btnCapture = findViewById(R.id.btnCapture);
        btnOk = findViewById(R.id.btnAccept);
        btnCancel = findViewById(R.id.btnReject);

        btnOk.setOnClickListener(this);
        btnCancel.setOnClickListener(this);

        llBottom = findViewById(R.id.llBottom);
        textureView = findViewById(R.id.textureView);
        ivBitmap = findViewById(R.id.ivBitmap);

        tvSlider1 = findViewById(R.id.tvSlider1);
        tvSlider2 = findViewById(R.id.tvSlider2);
        tvSlider3 = findViewById(R.id.tvSlider3);
        tvSlider4 = findViewById(R.id.tvSlider4);
        tvSlider5 = findViewById(R.id.tvSlider5);
        tvSlider6 = findViewById(R.id.tvSlider6);

        slider1 = Integer.parseInt(getString(R.string.default_slider_1));
        slider2 = Integer.parseInt(getString(R.string.default_slider_2));
        slider3 = Integer.parseInt(getString(R.string.default_slider_3));
        slider4 = Integer.parseInt(getString(R.string.default_slider_4));
        slider5 = Integer.parseInt(getString(R.string.default_slider_5));
        slider6 = Integer.parseInt(getString(R.string.default_slider_6));

        llHue = findViewById(R.id.ll1and2);
        llSat = findViewById(R.id.ll3and4);

        SeekBar.OnSeekBarChangeListener sbListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateSeekBars(progress, seekBar.getId());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        };

        SeekBar sbSlider1 = findViewById(R.id.sbSlider1);
        sbSlider1.setOnSeekBarChangeListener(sbListener);

        SeekBar sbSlider2 = findViewById(R.id.sbSlider2);
        sbSlider2.setOnSeekBarChangeListener(sbListener);

        SeekBar sbSlider3 = findViewById(R.id.sbSlider3);
        sbSlider3.setOnSeekBarChangeListener(sbListener);

        SeekBar sbSlider4 = findViewById(R.id.sbSlider4);
        sbSlider4.setOnSeekBarChangeListener(sbListener);

        SeekBar sbSlider5 = findViewById(R.id.sbSlider5);
        sbSlider5.setOnSeekBarChangeListener(sbListener);

        SeekBar sbSlider6 = findViewById(R.id.sbSlider6);
        sbSlider6.setOnSeekBarChangeListener(sbListener);

        sbSlider1.setProgress(Integer.parseInt(getString(R.string.default_slider_1)));
        tvSlider1.setText(getString(R.string.default_slider_1));
        sbSlider2.setProgress(Integer.parseInt(getString(R.string.default_slider_2)));
        tvSlider2.setText(getString(R.string.default_slider_2));
        sbSlider3.setProgress(Integer.parseInt(getString(R.string.default_slider_3)));
        tvSlider3.setText(getString(R.string.default_slider_3));
        sbSlider4.setProgress(Integer.parseInt(getString(R.string.default_slider_4)));
        tvSlider4.setText(getString(R.string.default_slider_4));
        sbSlider5.setProgress(Integer.parseInt(getString(R.string.default_slider_5)));
        tvSlider5.setText(getString(R.string.default_slider_5));
        sbSlider6.setProgress(Integer.parseInt(getString(R.string.default_slider_6)));
        tvSlider6.setText(getString(R.string.default_slider_6));


        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
    }

    private void updateSeekBars(int progress, int id) {
        switch (id) {
            case R.id.sbSlider1:
                slider1 = progress;
                tvSlider1.setText(String.format("%d", slider1));
                break;
            case R.id.sbSlider2:
                slider2 = progress;
                tvSlider2.setText(String.format("%d", slider2));
                break;
            case R.id.sbSlider3:
                slider3 = progress;
                tvSlider3.setText(String.format("%d", slider3));
                break;
            case R.id.sbSlider4:
                slider4 = progress;
                tvSlider4.setText(String.format("%d", slider4));
                break;
            case R.id.sbSlider5:
                slider5 = progress;
                tvSlider5.setText(String.format("%d", slider5));
                break;
            case R.id.sbSlider6:
                slider6 = progress;
                tvSlider6.setText(String.format("%d", slider6));
                break;
        }
    }

    private void startCamera() {

        CameraX.unbindAll();
        preview = setPreview();
        imageCapture = setImageCapture();
        imageAnalysis = setImageAnalysis();

        //bind to lifecycle:
        CameraX.bindToLifecycle(this, preview, imageCapture, imageAnalysis);
    }

    private Preview setPreview() {

        Rational aspectRatio = new Rational(textureView.getWidth(), textureView.getHeight());
        Size screen = new Size(textureView.getWidth(), textureView.getHeight()); //size of the screen


        PreviewConfig pConfig = new PreviewConfig.Builder()
                .setTargetAspectRatio(aspectRatio)
                .setTargetResolution(screen)
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

    private ImageCapture setImageCapture() {
        ImageCaptureConfig imageCaptureConfig = new ImageCaptureConfig.Builder().setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
                .setTargetRotation(getWindowManager().getDefaultDisplay().getRotation()).build();
        final ImageCapture imgCapture = new ImageCapture(imageCaptureConfig);


//        btnCapture.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//
//
//                imgCapture.takePicture(new ImageCapture.OnImageCapturedListener() {
//                    @Override
//                    public void onCaptureSuccess(ImageProxy image, int rotationDegrees) {
//                        Bitmap bitmap = textureView.getBitmap();
//                        showAcceptedRejectedButton(true);
//                        ivBitmap.setImageBitmap(bitmap);
//                    }
//
//                    @Override
//                    public void onError(ImageCapture.UseCaseError useCaseError, String message, @Nullable Throwable cause) {
//                        super.onError(useCaseError, message, cause);
//                    }
//                });
//
//
//                /*File file = new File(
//                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "" + System.currentTimeMillis() + "_JDCameraX.jpg");
//                imgCapture.takePicture(file, new ImageCapture.OnImageSavedListener() {
//                    @Override
//                    public void onImageSaved(@NonNull File file) {
//                        Bitmap bitmap = textureView.getBitmap();
//                        showAcceptedRejectedButton(true);
//                        ivBitmap.setImageBitmap(bitmap);
//                    }
//
//                    @Override
//                    public void onError(@NonNull ImageCapture.UseCaseError useCaseError, @NonNull String message, @Nullable Throwable cause) {
//
//                    }
//                });*/
//            }
//        });

        return imgCapture;
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
                new ImageAnalysis.Analyzer() {

                    @SuppressLint("DefaultLocale")
                    @Override
                    public void analyze(ImageProxy image, int rotationDegrees) {
                        //Analyzing live camera feed begins.
                        final Bitmap bitmap = textureView.getBitmap();
                        float length = slider2 / 1000.0f;

                        if (bitmap == null)
                            return;

                        Mat[] matrices = getDisplayMatrices(bitmap);
                        List<Point> centres = new ArrayList<>();

                        Dictionary dictionary = Aruco.getPredefinedDictionary(Aruco.DICT_4X4_50);
                        List<Mat> corners = new ArrayList<>();
                        Mat ids = new Mat();

                        Aruco.detectMarkers(matrices[0], dictionary, corners, ids);

//                        drawPolys(matrices[0], corners, ids);
                        Aruco.drawDetectedMarkers(matrices[0], corners, ids, COLOURS[9]);

                        Mat cameraMatrix = getCameraMatrix();
                        MatOfDouble distCoeffs = new MatOfDouble(0, 0, 0, 0);
                        Mat rvec = new Mat(1, 3, CvType.CV_64F);
                        Mat tvec = new Mat(1, 3, CvType.CV_64F);

                        Aruco.estimatePoseSingleMarkers(
                                corners,
                                length,
                                cameraMatrix,
                                distCoeffs,
                                rvec,
                                tvec
                        );


                        System.out.println("rvec = " + rvec.dump());
                        System.out.println("tvec = " + tvec.dump());
                        for (int i = 0; i < rvec.height(); i++) {
                            Calib3d.drawFrameAxes(
                                    matrices[0],
                                    cameraMatrix,
                                    distCoeffs,
                                    rvec.row(i),
                                    tvec.row(i),
                                    length
                            );
                        }


                        final Bitmap result = combineMatrixesToBitmap(matrices);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ivBitmap.setImageBitmap(result);
                            }
                        });

                    }
                });


        return imageAnalysis;

    }

    private Mat getCameraMatrix() {
//    c_x = matrices[0].width() / 2.0f;
//    c_y = matrices[0].height() / 2.0f;
        float f_x = 521.45f;
        float f_y = 520.38f;
        float c_x = 320.80f;
        float c_y = 238.60f;

        float[][] arr = new float[][]{
                new float[]{f_x, 0, c_x},
                new float[]{0, f_y, c_y},
                new float[]{0, 0, 1}
        };

        Mat cameraMatrix = new Mat(3, 3, CvType.CV_64F);
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++)
                cameraMatrix.put(row, col, arr[row][col]);
        }
        return cameraMatrix;
    }


    private Mat[] getDisplayMatrices(Bitmap bitmap) {
        if (bitmap == null)
            return null;

        Mat original = new Mat();
        Utils.bitmapToMat(bitmap, original);

        double thresh = slider1;
        double canny_thresh2 = slider3 / 255.0;
        double canny_thresh1 = slider4 / 255.0;
        double thresh_max = 255;

        if (thresh == 0.0) {
            thresh = 150;
        }
        Mat grey = new Mat();
        Imgproc.cvtColor(original, grey, Imgproc.COLOR_RGB2GRAY);

        Mat blurMat = new Mat();
        Imgproc.blur(grey, blurMat, new org.opencv.core.Size(5, 5));

        Mat thresholdMat = new Mat();
        Imgproc.threshold(blurMat, thresholdMat, thresh, thresh_max, Imgproc.THRESH_BINARY);

        Mat cannyMat = new Mat();
        Imgproc.Canny(thresholdMat, cannyMat, canny_thresh1, canny_thresh2);
        Imgproc.putText(cannyMat, slider2 + "mm side length", new Point(20, 40), Core.FONT_HERSHEY_PLAIN, 3, COLOURS[9], 1);


        List<MatOfPoint> deepContours = getDeepContours(cannyMat);
        Mat contourMat = Mat.zeros(original.size(), CvType.CV_8UC3);
        // Draw the 3 or 0 found contours onto the contourMatrix, and return it
        for (int i = 0; i < deepContours.size(); i++) {
            Imgproc.drawContours(contourMat, deepContours, i, COLOURS[(int) (Math.random() * COLOURS.length - 1)]);
        }
        Imgproc.cvtColor(grey, grey, Imgproc.COLOR_GRAY2RGB);
        Imgproc.cvtColor(blurMat, blurMat, Imgproc.COLOR_GRAY2RGB);
        Imgproc.cvtColor(thresholdMat, thresholdMat, Imgproc.COLOR_GRAY2RGB);

        return new Mat[]{
                blurMat,
                thresholdMat,
                cannyMat,
                contourMat
        };
    }

    private List<MatOfPoint> getDeepContours(Mat cannyMat) {

        /*
        Does Canny Outline detection to find the contours and the heirarchy defining those contours.
        Look for contours that are at the given depth.
        If none are found, increase the required depth until exactly 3 are found
        The depth is increased up until 20, then reset and increased again from depth=0
         */


        Mat hierarchy = new Mat();
        List<MatOfPoint> contoursList = new ArrayList<>();
        Imgproc.findContours(cannyMat, contoursList, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        List<MatOfPoint> deepContours = new ArrayList<>();
//        int desiredDepth = 9;

        for (int j = 8; j >= 5; j--) {
            deepContours = new ArrayList<>();
            for (int i = 0; i < contoursList.size(); i++) {
                int current_child = i;
                int current_depth = 0;

                // Go through the parents of the current child until the root parent is found
                while (hierarchy.get(0, current_child)[3] != -1) {
                    current_child = (int) hierarchy.get(0, current_child)[3];
                    current_depth++;

                    if (current_depth >= j) {
                        deepContours.add(contoursList.get(i));
                        break;
                    }
                }
            }

            if (deepContours.size() >= 3) {
                break;
            }
        }
        if (deepContours.size() != 3) {
//            Log.e(TAG, "Found " + deepContours.size() + " contours");
        }
        return deepContours;
    }

    private List<Point> getQrCodeCentres(Mat[] matrices) {
//        Mat[] matrices = getDisplayMatrices(original);
        /*
        0 blurMat,        
        1 thresholdMat,
        2 cannyMat,
        3 contourMat
        A Moment is defined with i, j being two powers in this equation:
            m_ji = foreach x (foreach y ( array[x, y] * x^i * y^j)

            this means that m_00 is simply the sum of all pixel values in an image
         */
        List<Point> centres = new ArrayList<>();
        List<MatOfPoint> deepContours = getDeepContours(matrices[2]);

        List<Moments> moments = new ArrayList<>();
        for (int i = 0; i < deepContours.size(); i++) {
            moments.add(Imgproc.moments(deepContours.get(i)));

            // Avoid division by zero
            if (moments.get(i).m00 == 0.0) {
                centres.add(new Point(0, 0));
            } else {
                centres.add(new Point(
                        (int) (moments.get(i).m10 / moments.get(i).m00),
                        (int) (moments.get(i).m01 / moments.get(i).m00)));
            }
//            Imgproc.circle(matrices[0], centres.get(i), 5, COLOURS[(int) (Math.random() * COLOURS.length - 1)]);
        }


        return extrapolate4thCorner(centres);

    }


    private List<Point> extrapolate4thCorner(List<Point> centres) {
        if (centres.size() != 3) {
            return centres;
        }
        Point A = centres.get(0);
        Point B = centres.get(1);
        Point C = centres.get(2);

        double AB = distanceP2P(A, B);
        double BC = distanceP2P(B, C);
        double CA = distanceP2P(C, A);

        Point p0_0;
        Point p0_1;
        Point p1_0;
        Point temp1;
        Point temp2;

        //Find the vertex of triangle ABC that isn't part of the longest side:
        if (AB > BC && AB > CA) {
            p0_0 = C;
            temp1 = A;
            temp2 = B;
        } else if (BC > AB && BC > CA) {
            p0_0 = A;
            temp1 = C;
            temp2 = B;
        } else if (CA > AB && CA > BC) {
            p0_0 = B;
            temp1 = A;
            temp2 = C;
        } else {
            // Not enough information to get the 4th corner
            return centres;
        }

        // Take care of all the rotations possible where p0_1 = B and p1_0 = A;
        if (temp1.y < p0_0.y && temp2.y > p0_0.y        // when the three corners make a '<' position
                || temp1.x > p0_0.x && temp2.x < p0_0.x     // when the three corners make a '\/' position
                || temp1.y > p0_0.y && temp2.y < p0_0.y     // when the three corners make a '>' position
                || temp1.x < p0_0.x && temp2.x > p0_0.x     // when the three corners make a '^' position
        ) {
            p0_1 = temp2;
            p1_0 = temp1;
        } else {
            p0_1 = temp1;
            p1_0 = temp2;
        }
        assert p0_1 != null && p1_0 != null && p0_0 != null;

//                             Construct Point D as a parallelogram, starting from the CW Point
        Point p1_1 = new Point(
                p0_1.x + (p1_0.x - p0_0.x),
                p0_1.y + (p1_0.y - p0_0.y));

        List<Point> corners = new ArrayList<>();
        corners.add(0, p0_0);
        corners.add(1, p0_1);
        corners.add(2, p1_1);
        corners.add(3, p1_0);
        return corners;
    }

    private void drawQrCode(List<Point> centres, Mat toDrawOn) {
        if (centres.size() == 4) {
            for (int i = 0; i < centres.size(); i++) {
                Point current = centres.get(i);
                Point next = centres.get((i + 1) % centres.size());
                Imgproc.line(toDrawOn, current, next, COLOURS[(int) (Math.random() * COLOURS.length - 1)], 3);
                Imgproc.putText(toDrawOn, "p=" + i, current, Core.FONT_HERSHEY_PLAIN, 3, COLOURS[11], 3);

            }
        }
    }

    private void drawPolys(Mat toDrawOn, List<Mat> mats, Mat ids) {
        Scalar line = COLOURS[6];
        Scalar text = COLOURS[9];

        for (int i = 0; i < mats.size(); i++) {
            double[] average = new double[]{0, 0};
            int width = mats.get(i).width();

            for (int j = 0; j < mats.get(i).width(); j++) {
                Point current = new Point(mats.get(i).get(0, j));
                Point next = new Point(mats.get(i).get(0, (j + 1) % width));
                Imgproc.line(toDrawOn, current, next, line, 3);
                average[0] += mats.get(i).get(0, j)[0];
                average[1] += mats.get(i).get(0, j)[1];
            }
            average[0] /= width;
            average[1] /= width;

            Imgproc.putText(toDrawOn, Arrays.toString(ids.get(0, i)), new Point(average), Core.FONT_HERSHEY_PLAIN, 2, text, 3);
        }
    }

    private void drawCube(MatOfPoint2f imagePoints, Mat matrix) {
        Scalar lowerColour = COLOURS[(int) (Math.random() * COLOURS.length - 1)];
        Scalar upperColour = COLOURS[(int) (Math.random() * COLOURS.length - 1)];
        Scalar middleColour = COLOURS[(int) (Math.random() * COLOURS.length - 1)];

        MatOfPoint lowerSquare = new MatOfPoint();
        lowerSquare.fromArray(new Point(imagePoints.get(0, 0)),
                new Point(imagePoints.get(1, 0)),
                new Point(imagePoints.get(2, 0)),
                new Point(imagePoints.get(3, 0)));
        Imgproc.fillConvexPoly(matrix, lowerSquare, lowerColour);

        Imgproc.line(matrix, new Point(imagePoints.get(4, 0)), new Point(imagePoints.get(5, 0)), upperColour, 3);
        Imgproc.line(matrix, new Point(imagePoints.get(5, 0)), new Point(imagePoints.get(6, 0)), upperColour, 3);
        Imgproc.line(matrix, new Point(imagePoints.get(6, 0)), new Point(imagePoints.get(7, 0)), upperColour, 3);
        Imgproc.line(matrix, new Point(imagePoints.get(7, 0)), new Point(imagePoints.get(4, 0)), upperColour, 3);

        Imgproc.line(matrix, new Point(imagePoints.get(0, 0)), new Point(imagePoints.get(4, 0)), middleColour, 3);
        Imgproc.line(matrix, new Point(imagePoints.get(1, 0)), new Point(imagePoints.get(5, 0)), middleColour, 3);
        Imgproc.line(matrix, new Point(imagePoints.get(2, 0)), new Point(imagePoints.get(6, 0)), middleColour, 3);
        Imgproc.line(matrix, new Point(imagePoints.get(3, 0)), new Point(imagePoints.get(7, 0)), middleColour, 3);

        for (int i = 0; i < imagePoints.rows(); i++) {
            Point currentPoint = new Point(imagePoints.get(i, 0));
            Imgproc.putText(matrix, i + "", currentPoint, Core.FONT_HERSHEY_PLAIN, 3, COLOURS[(int) (Math.random() * COLOURS.length - 1)], 2);

        }
    }


    private static double distanceP2P(Point p1, Point p2) {
        return Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
    }

    private static Bitmap combineMatrixesToBitmap(Mat[] matrices) {
        Bitmap[] bitmaps = new Bitmap[4];

        for (int i = 0; i < matrices.length; i++) {
            bitmaps[i] = Bitmap.createBitmap(matrices[i].width(), matrices[i].height(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(matrices[i], bitmaps[i]);
        }

        Bitmap result;
        try {
            if (bitmaps[0] == null) {
                return null;
            }
            int bgWidth = bitmaps[0].getWidth();
            int bgHeight = bitmaps[0].getHeight();

            for (int i = 0; i < bitmaps.length; i++) {

            }

            result = Bitmap.createBitmap(bgWidth, bgHeight, Bitmap.Config.ARGB_8888);
            Canvas cv = new Canvas(result);
            cv.drawBitmap(bitmaps[0], 0, 0, null);
//            cv.drawBitmap(bitmaps[1], (bgWidth) / 2, 0, null);
//            cv.drawBitmap(bitmaps[2], 0, (bgHeight / 2), null);
//            cv.drawBitmap(bitmaps[3], (bgWidth) / 2, (bgHeight / 2), null);
            cv.save();
            cv.restore();
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    private void showAcceptedRejectedButton(boolean acceptedRejected) {
        if (acceptedRejected) {
            CameraX.unbind(preview, imageAnalysis);
            llBottom.setVisibility(View.VISIBLE);
            btnCapture.setVisibility(View.INVISIBLE);
            textureView.setVisibility(View.GONE);
        } else {
            btnCapture.setVisibility(View.VISIBLE);
            llBottom.setVisibility(View.GONE);
            textureView.setVisibility(View.VISIBLE);
            textureView.post(new Runnable() {
                @Override
                public void run() {
                    startCamera();
                }
            });
        }
    }

    private void updateTransform() {


        Matrix mx = new Matrix();
        float w = textureView.getMeasuredWidth();
        float h = textureView.getMeasuredHeight();

        float cX = w / 2f;
        float cY = h / 2f;

        int rotationDgr;
        int rotation = (int) textureView.getRotation();

        switch (rotation) {
            case Surface.ROTATION_0:
                rotationDgr = 0;
                break;
            case Surface.ROTATION_90:
                rotationDgr = 90;
                break;
            case Surface.ROTATION_180:
                rotationDgr = 180;
                break;
            case Surface.ROTATION_270:
                rotationDgr = 270;
                break;
            default:
                return;
        }

        mx.postRotate((float) rotationDgr, cX, cY);
        textureView.setTransform(mx);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.botspot:
                currentImageType = Imgproc.COLOR_RGB2HLS;
                startCamera();
                return true;

            case R.id.black_white:
                currentImageType = Imgproc.COLOR_RGB2GRAY;
                startCamera();
                return true;

            case R.id.hsv:
                currentImageType = Imgproc.COLOR_RGB2HSV;
                startCamera();
                return true;

            case R.id.lab:
                currentImageType = Imgproc.COLOR_RGB2Lab;
                startCamera();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

//    @Override
//    public void onClick(View v) {
//        switch (v.getId()) {
//            case R.id.btnReject:
//                showAcceptedRejectedButton(false);
//                break;
//
//            case R.id.btnAccept:
//                File file = new File(
//                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "" + System.currentTimeMillis() + "_JDCameraX.jpg");
//                imageCapture.takePicture(file, new ImageCapture.OnImageSavedListener() {
//                    @Override
//                    public void onImageSaved(@NonNull File file) {
//                        showAcceptedRejectedButton(false);
//
//                        Toast.makeText(getApplicationContext(), "Image saved successfully in Pictures Folder", Toast.LENGTH_LONG).show();
//                    }
//
//                    @Override
//                    public void onError(@NonNull ImageCapture.UseCaseError useCaseError, @NonNull String message, @Nullable Throwable cause) {
//
//                    }
//                });
//                break;
//        }
//    }

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

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}

