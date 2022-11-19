/*
 * Source code from OpenCV samples
 * https://github.com/opencv/opencv/blob/master/samples
 *
 * CalibrationResult.java
 * https://github.com/opencv/opencv/blob/master/samples/android/camera-calibration/src/org/opencv/samples/cameracalibration/CalibrationResult.java
 *
 */

package mg.rivolink.app.aruco.camera.calibration;

import org.opencv.core.Mat;

import android.app.Activity;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.ArrayList;

public abstract class CalibrationResult {
    
	private static final int CAMERA_MATRIX_ROWS = 3;
    private static final int CAMERA_MATRIX_COLS = 3;
    private static final int DISTORTION_COEFFICIENTS_SIZE = 5;
	
	private static final int FILE_CODE = 3092;
	private static final String FILE_TYPE = "text/plain";
	private static final String FILE_NAME = "camera-params.txt";
	
	private static List<String> data;

    public static void save(Activity activity, Mat cameraMatrix, Mat distortionCoefficients) {
		data = new ArrayList<String>();
		
        double[] cameraMatrixArray = new double[CAMERA_MATRIX_ROWS * CAMERA_MATRIX_COLS];
        cameraMatrix.get(0,  0, cameraMatrixArray);
        for (int i = 0; i < CAMERA_MATRIX_ROWS; i++) {
            for (int j = 0; j < CAMERA_MATRIX_COLS; j++) {
                Integer id = i * CAMERA_MATRIX_ROWS + j;
                data.add(Double.toString(cameraMatrixArray[id]));
            }
        }

        double[] distortionCoefficientsArray = new double[DISTORTION_COEFFICIENTS_SIZE];
        distortionCoefficients.get(0, 0, distortionCoefficientsArray);
        int shift = CAMERA_MATRIX_ROWS * CAMERA_MATRIX_COLS;
        for (Integer i = shift; i < DISTORTION_COEFFICIENTS_SIZE + shift; i++) {
            data.add(Double.toString(distortionCoefficientsArray[i-shift]));
        }
		
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(FILE_TYPE);
        intent.putExtra(Intent.EXTRA_TITLE, FILE_NAME);

		activity.startActivityForResult(intent, FILE_CODE);
    }
    
	public static void onActivityResult(Context context, int requestCode, int resultCode, Intent intent) {
		if((requestCode == FILE_CODE) && (resultCode == Activity.RESULT_OK)){
			try {
				Uri uri = intent.getData();
				OutputStream output = context.getContentResolver().openOutputStream(uri);

				String text = String.join(",", data);
				
				output.write(text.getBytes());
				output.flush();
				output.close();
				
				Toast.makeText(context, context.getString(R.string.success_camera_params_saving), Toast.LENGTH_SHORT).show();
			}
			catch(IOException e) {
				Toast.makeText(context, context.getString(R.string.error_camera_params_saving), Toast.LENGTH_SHORT).show();
			}
		}
	}
}

