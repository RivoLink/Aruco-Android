package mg.rivolink.app.aruco.camera.calibration;

import java.util.List;
import java.util.LinkedList;

import org.opencv.aruco.Aruco;
import org.opencv.aruco.Board;
import org.opencv.aruco.DetectorParameters;
import org.opencv.aruco.Dictionary;
import org.opencv.aruco.GridBoard;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class CameraCalibrator{
	private static final int MIN_FRAMES = 15;
	
	private int captured = 0;
	
	private int markerNumX = 5;
	private int markerNumY = 7;
	
	private float markerLength = 0.032f;
	private float markerDistance = 0.003f;
	
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
	
	private boolean addFrame = false;
	private OnAddFrameListener listener;
	
	interface OnAddFrameListener{
		public void onAddFrame(boolean added);
	}
	
	public Mat getCameraMatrix(){
		return cameraMatrix;
	}
	
	public Mat getDistorsionCoefficients(){
		return distCoeffs;
	}
	
	public CameraCalibrator(int width, int height){
		size = new Size(width, height);
		dictionary = Aruco.getPredefinedDictionary(Aruco.DICT_6X6_50);
		board = GridBoard.create(markerNumX, markerNumY, markerLength, markerDistance, dictionary);
		
		cameraMatrix = Mat.eye(3, 3, CvType.CV_64FC1);
        distCoeffs = Mat.zeros(5, 1, CvType.CV_64FC1);
		
		allIdsConcatenated = new Mat();
		allCornersConcatenated = new LinkedList<>();
		markerCounterPerFrame = new MatOfInt();
		
		corners = new LinkedList<>();
		rejected = new LinkedList<>();
		parameters = DetectorParameters.create();
	}
	
	public void addFrame(){
		addFrame = true;
	}
	
	public void setOnAddFrameListener(OnAddFrameListener listener){
		this.listener = listener;
	}
	
	public boolean canCalibrate(){
		return MIN_FRAMES <= captured;
	}
	
	public void release(){
		ids.release();
		allIdsConcatenated.release();
		markerCounterPerFrame.release();
	}
	
	public void clear(){
		captured = 0;
		
		allCornersConcatenated.clear();
		
		allIdsConcatenated.release();
		allIdsConcatenated = new Mat();
		
		markerCounterPerFrame.release();
		markerCounterPerFrame = new MatOfInt();
	}
	
	public double calibrate(){
		List<Mat> rvecs = new LinkedList<>();
		List<Mat> tvecs = new LinkedList<>();

		return Aruco.calibrateCameraAruco(
			allCornersConcatenated,
			allIdsConcatenated,
			markerCounterPerFrame,
			board,
			size,
			cameraMatrix,
			distCoeffs,
			rvecs,
			tvecs
		);
	}
	
	public void render(Mat rgb, Mat gray){
		detectMarkers(rgb, gray);
		
		Imgproc.putText(rgb, "Captured: "+captured, new Point(rgb.cols()/3*2, rgb.rows()*0.1),
					 Core.FONT_HERSHEY_SIMPLEX, 1.0, new Scalar(255, 255, 0));
	}
	
	private boolean saveFrame(){
		if(corners.isEmpty())
			return false;

		captured++;

		allCornersConcatenated.addAll(corners);
		allIdsConcatenated.push_back(ids);
		markerCounterPerFrame.push_back(new MatOfInt(corners.size()));

		return true;
	}
	
	private void detectMarkers(Mat rgb, Mat gray){
		ids = new MatOfInt();
		
		corners.clear();
		rejected.clear();
		
		Aruco.detectMarkers(gray, dictionary, corners, ids, parameters, rejected);
		Aruco.refineDetectedMarkers(gray, board, corners, ids, rejected);

		if(corners.size()>0)
			Aruco.drawDetectedMarkers(rgb, corners);
		
		if(addFrame){
			addFrame = false;
			boolean saved = saveFrame();
			
			if(listener != null)
				listener.onAddFrame(saved);
		}
	}
}

