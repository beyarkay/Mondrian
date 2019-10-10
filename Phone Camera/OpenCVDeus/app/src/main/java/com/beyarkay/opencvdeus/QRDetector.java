package com.beyarkay.opencvdeus;

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;


class QRDetector {
    private static final String TAG = QRDetector.class.toString();
    private Mat src;


    QRDetector(Mat src) {

//        Hysteresis: The final step. Canny does use two thresholds (upper and lower):
//        If a pixel gradient is higher than the upper threshold, the pixel is accepted as an edge
//        If a pixel gradient value is below the lower threshold, then it is rejected.
//                If the pixel gradient is between the two thresholds, then it will be accepted only if it is connected to a pixel that is above the upper threshold.
//        Canny recommended a upper:lower ratio between 2:1 and 3:1.
        //        thresh = 200;

        this.src = src;
    }

    public Mat getCorners(int depth, double canny_thresh1, double canny_thresh2) {
        double[] corners = new double[4];

        Mat matrix = new Mat();
        Imgproc.cvtColor(this.src, matrix, Imgproc.COLOR_RGB2GRAY);

        Imgproc.blur(matrix, matrix, new Size(5, 5));

        Mat processed = new Mat();
        Imgproc.Canny(matrix, processed, canny_thresh1 / 255, canny_thresh2 / 255);

        Mat heirarcy = new Mat();
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(processed, contours, heirarcy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);


        List<Integer> marker_indexes = new ArrayList<>();
        List<MatOfPoint> goodContours = new ArrayList<>();
        int j;
        int count;
        for (int i = 0; i < contours.size(); i++) {
            j = i;
            count = 0;
//            System.out.println(Arrays.toString(heirarcy.get(j, 0)));

            /*
            Heirarch is an 2D array of size 1xN (row x col), where N is the number of contours found
            Each of the N elements is in turn a double[4]
            Each element in the double[4] is the id of:
                    [0] next,
                    [1] previous,
                    [2] parent,
                    [3] and first_child, respectively
            a negative id means that that object doesn't exist
             */

            // while the current child has a parent
            while (heirarcy.get(0, j)[2] != -1) {
                j = (int) heirarcy.get(0, j)[2];
                count++;
            }
            if (count >= depth) {
                marker_indexes.add(i);
            }


        }
        Mat withContours = Mat.zeros(this.src.size(), CvType.CV_8UC3);
        for (int i = 0; i < marker_indexes.size(); i++) {
            goodContours.add(contours.get(marker_indexes.get(i)));
        }

        for (int i = 0; i < goodContours.size(); i++) {
            Imgproc.drawContours(withContours, goodContours, i, new Scalar(255, 191, 0));
        }


        return withContours;


//        return marker_indexes;
    }


}
