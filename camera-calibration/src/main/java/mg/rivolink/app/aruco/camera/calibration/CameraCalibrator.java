package mg.rivolink.app.aruco.camera.calibration;

import org.opencv.aruco.Dictionary;
import org.opencv.aruco.GridBoard;
import org.opencv.core.Mat;
import org.opencv.aruco.Board;
import org.opencv.core.*;
import java.util.*;
import org.opencv.aruco.*;
import org.opencv.imgproc.*;

public class CameraCalibrator{
	
	private int captured=0;
	
	private int markerNumX=5;
	private int markerNumY=7;
	
	private float markerLength=0.032f;
	private float markerDistance=0.003f;
	
	private Size size;
	private Board board;
	private Dictionary dictionary;

	private Mat distCoeffs;
	private Mat cameraMatrix;

	private Mat allIdsConcatenated;
	private List<Mat> allCornersConcatenated;
	private MatOfInt markerCounterPerFrame;

	private MatOfInt ids;
	private List<Mat> corners;
	private List<Mat> rejected;
	private DetectorParameters parameters;
	
	public CameraCalibrator(int width,int height){
		size=new Size(width,height);
		dictionary=Aruco.getPredefinedDictionary(Aruco.DICT_6X6_50);
		board=GridBoard.create(markerNumX,markerNumY,markerLength,markerDistance,dictionary);
		
		cameraMatrix=Mat.eye(3,3,CvType.CV_64FC1);
        distCoeffs=Mat.zeros(5,1,CvType.CV_64FC1);
		
		allIdsConcatenated=new Mat();
		allCornersConcatenated=new LinkedList<>();
		markerCounterPerFrame=new MatOfInt();
		
		corners=new LinkedList<>();
		rejected=new LinkedList<>();
		parameters=DetectorParameters.create();
	}
	
	public void render(Mat rgb,Mat gray){
		detectMarkers(rgb,gray);
		
		Imgproc.putText(rgb,"Captured: "+captured,new Point(rgb.cols()/3*2,rgb.rows()* 0.1),
					 Core.FONT_HERSHEY_SIMPLEX,1.0,new Scalar(255,255,0));
	}
	
	private void detectMarkers(Mat rgb,Mat gray){
		ids=new MatOfInt();
		
		corners.clear();
		rejected.clear();
		
		Aruco.detectMarkers(gray,dictionary,corners,ids,parameters,rejected);
		Aruco.refineDetectedMarkers(gray,board,corners,ids,rejected);

		if(corners.size()>0){
			//calibrator.set(corners,ids);
			Aruco.drawDetectedMarkers(rgb,corners);
		}
	}
}
