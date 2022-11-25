package mg.rivolink.app.aruco.utils;

import android.app.Activity;
import android.app.AlertDialog;

import android.content.Intent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import android.net.Uri;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import mg.rivolink.app.aruco.R;

import org.opencv.core.Mat;

public class CameraParameters {

	private static final int CAMERA_MATRIX_ROWS = 3;
    private static final int CAMERA_MATRIX_COLS = 3;
    private static final int DISTORTION_COEFFICIENTS_SIZE = 5;

	private static final int FILE_CODE = 3092;
	private static final String FILE_TYPE = "text/plain";
	private static final String FILE_NAME = "camera-params.txt";
	
	private static boolean loaded = false;
	
	public static boolean isLoaded(){
		return loaded;
	}
	
	public static boolean fileExists(Activity activity){
		return new File(activity.getExternalCacheDir(), FILE_NAME).exists();
	}
	
	public static void selectFile(final Activity activity){
		AlertDialog.Builder alert = new AlertDialog.Builder(activity);
		alert.setTitle("Warning");
		alert.setMessage(activity.getString(R.string.error_camera_params));
		alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					
					Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
					intent.addCategory(Intent.CATEGORY_OPENABLE);
					intent.setType(FILE_TYPE);
					activity.startActivityForResult(intent, FILE_CODE);
				}
			});
		alert.show();
	}
	
	public static boolean copyFile(Context context, Uri source){
		try {
			File target = new File(context.getExternalCacheDir(), FILE_NAME);

			InputStream in = context.getContentResolver().openInputStream(source);
			OutputStream out = new FileOutputStream(target);

			int size = in.available();
			byte[] buffer = new byte[size];

			while((size = in.read(buffer)) > 0){
				out.write(buffer, 0, size);
			}

			in.close();
			out.close();
			
			return true;
		}
		catch(Exception e){
			return false;
		}
	}
	
	public static boolean tryLoad(Context context, Mat cameraMatrix, Mat distCoeffs){
		File file = new File(context.getExternalCacheDir(), FILE_NAME);
		
		try {
			
			InputStream in = new FileInputStream(file);
			
			int size = in.available();
			byte[] buffer = new byte[size];
			
			in.read(buffer);
			in.close();
			
			String[] params = new String(buffer).split(",");
			
			int index = 0;
			int length = params.length;
			
			double[] cameraMatrixArray = new double[CAMERA_MATRIX_ROWS*CAMERA_MATRIX_COLS];
			for(int i = 0; i < CAMERA_MATRIX_ROWS; i++){
				for(int j = 0; j < CAMERA_MATRIX_COLS; j++){
					int id  =  i*CAMERA_MATRIX_ROWS + j;
					
					if(index < length){
						cameraMatrixArray[id] = Double.parseDouble(params[index]);
						index++;
					}
				}
			}
			
			cameraMatrix.put(0, 0, cameraMatrixArray);
			
			double[] distortionCoefficientsArray = new double[DISTORTION_COEFFICIENTS_SIZE];
			int shift = CAMERA_MATRIX_ROWS*CAMERA_MATRIX_COLS;
			for(int i = shift; i < DISTORTION_COEFFICIENTS_SIZE + shift; i++){
				if(index < length){
					distortionCoefficientsArray[i - shift] = Double.parseDouble(params[index]);
					index++;
				}
			}
			
			distCoeffs.put(0, 0, distortionCoefficientsArray);
			
			Toast.makeText(context, context.getString(R.string.success_camera_params), Toast.LENGTH_SHORT).show();
			
			loaded = true;
			return loaded;
		}
		catch(Exception e){
			return false;
		}
	}
	
	public static boolean onActivityResult(Context context, int requestCode, int resultCode, Intent intent, Mat cameraMatrix, Mat distCoeffs){
		if((requestCode == FILE_CODE) && (resultCode == Activity.RESULT_OK)){
			Uri uri = intent.getData();
			
			boolean success = true;
			
			success &= copyFile(context, uri);
			success &= tryLoad(context, cameraMatrix, distCoeffs);
			
			return success;
		}
		
		return false;
	}
	
}
