package mg.rivolink.app.aruco.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import org.opencv.core.Mat;

public class CameraParameters {

	private static final int CAMERA_MATRIX_ROWS = 3;
    private static final int CAMERA_MATRIX_COLS = 3;
    private static final int DISTORTION_COEFFICIENTS_SIZE = 5;

	private static final String CAMERA_CALIBRATION_PKG = "mg.rivolink.app.aruco.camera.calibration";
	private static final String CAMERA_CALIBRATION_PREFS = "CameraCalibrationActivity";

	public static boolean tryLoad(Activity activity, Mat cameraMatrix, Mat distCoeffs){
		try {
			Context context = activity.createPackageContext(CAMERA_CALIBRATION_PKG, Context.CONTEXT_IGNORE_SECURITY);
			SharedPreferences cameraPrefs = context.getSharedPreferences(CAMERA_CALIBRATION_PREFS, Context.MODE_WORLD_READABLE);
			
			if(cameraPrefs.getFloat("0", -1) == -1)
				return false;
			else
				load(cameraPrefs, cameraMatrix, distCoeffs);
			
		}
		catch(PackageManager.NameNotFoundException e){
			e.printStackTrace();
		}
		return true;
	}

	private static void load(SharedPreferences sharedPref, Mat cameraMatrix, Mat distCoeffs){
		double[] cameraMatrixArray = new double[CAMERA_MATRIX_ROWS*CAMERA_MATRIX_COLS];
        for(int i = 0; i < CAMERA_MATRIX_ROWS; i++){
            for(int j = 0; j < CAMERA_MATRIX_COLS; j++){
                int id  =  i*CAMERA_MATRIX_ROWS + j;
                cameraMatrixArray[id] = sharedPref.getFloat(Integer.toString(id), -1);
            }
        }
        cameraMatrix.put(0, 0, cameraMatrixArray);
        
        double[] distortionCoefficientsArray = new double[DISTORTION_COEFFICIENTS_SIZE];
        int shift = CAMERA_MATRIX_ROWS*CAMERA_MATRIX_COLS;
        for(int i = shift; i < DISTORTION_COEFFICIENTS_SIZE + shift; i++){
            distortionCoefficientsArray[i - shift] = sharedPref.getFloat(Integer.toString(i), -1);
        }
        distCoeffs.put(0, 0, distortionCoefficientsArray);
	}
	
}
