package com.beyarkay.opencvdeus;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Rational;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageAnalysisConfig;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import static org.opencv.imgproc.Imgproc.INTER_AREA;

public class CalibrateCameraActivity extends AppCompatActivity {
    private static final String TAG = "(┛ಠ_ಠ)┛ 彡 ┻━┻";

    private int REQUEST_CODE_PERMISSIONS = 101;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE"};

    TextureView textureView;
    ImageView ivBitmap;
    ImageAnalysis imageAnalysis;
    Preview preview;


    Button btnReset, btnCapture, btnDone;

    static {
        if (!OpenCVLoader.initDebug())
            Log.d("ERROR", "Unable to load OpenCV");
        else
            Log.d("SUCCESS", "OpenCV loaded");
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibrate_camera);

        btnReset = findViewById(R.id.btnReset);
        btnCapture = findViewById(R.id.btnCapture);
        btnDone = findViewById(R.id.btnDone);


        textureView = findViewById(R.id.textureView);
        ivBitmap = findViewById(R.id.ivBitmap);

//        if (allPermissionsGranted()) {
        startCamera();
//        } else {
//            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
//        }

    }

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
                output -> {
                    ViewGroup parent = (ViewGroup) textureView.getParent();
                    parent.removeView(textureView);
                    parent.addView(textureView, 0);
                    textureView.setSurfaceTexture(output.getSurfaceTexture());
                    updateTransform();
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
                    final Bitmap bitmap = textureView.getBitmap();
                    if (bitmap == null) {
                        return;
                    }

                    int expected_num_markers = 24;
                    Mat screenMatrix = new Mat();
                    // Convert the bitmap to a matrix
                    Utils.bitmapToMat(bitmap, screenMatrix);

                    // Downsize the image to 500 x 500
                    Mat dst = new Mat();
                    org.opencv.core.Size dsize = new org.opencv.core.Size(500, 500);
                    Imgproc.resize(screenMatrix, dst, dsize, 0, 0, INTER_AREA);



                    Bitmap output = Bitmap.createBitmap(
                            screenMatrix.width(),
                            screenMatrix.height(),
                            Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(screenMatrix, output);
                    runOnUiThread(() -> ivBitmap.setImageBitmap(output));
                });
        return imageAnalysis;
    }

    private void rescale() {
        /*
        def downsize(from_dir, to_dir, factor):
            image_paths = sorted(glob.glob(os.path.join(from_dir, "*")))
            if len(image_paths) == 0:
                if input(f"len(image_paths) == 0, change from_dir? [y]/n: ") == "y":
                    from_dir = input("Enter new from_dir: ")
                    image_paths = sorted(glob.glob(os.path.join(from_dir, "*")))
            for image_path in image_paths:
                img = cv2.imread(image_path, cv2.IMREAD_UNCHANGED)
                width = int(img.shape[1] * factor)
                height = int(img.shape[0] * factor)

                dim = (width, height)
                resized = cv2.resize(img, dim, interpolation = cv2.INTER_AREA)
                path = os.path.join(to_dir, image_path.split(os.sep)[-1])
                if not os.path.exists(to_dir):
                    os.makedirs(to_dir)
                cv2.imwrite(os.path.join(to_dir, image_path.split(os.sep)[-1]), resized)
            print(f"Resized@{factor}, saved {len(image_paths)} files to {to_dir}")
         */


    }

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


//    private boolean allPermissionsGranted() {
//        for (String permission : REQUIRED_PERMISSIONS) {
//            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
//                return false;
//            }
//        }
//        return true;
//    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        if (requestCode == REQUEST_CODE_PERMISSIONS) {
//            if (allPermissionsGranted()) {
//                startCamera();
//            } else {
//                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
//                finish();
//            }
//        }
//    }

    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.btnCapture:
                Log.d(TAG, "onClick - btnCapture");


                break;
            case R.id.btnDone:
                Log.d(TAG, "onClick - btnDone");

                break;
            case R.id.btnReset:
                Log.d(TAG, "onClick - btnReset");
                break;
        }
    }

}
