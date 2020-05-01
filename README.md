# Aruco Android

> It is an application to detect Aruco Markers.

<center>
	<img width="75%" src="screenshots/marker_drawing_axis.png" alt="screenshot_home" />
</center>

## Credits

This application uses:

- **opencv-contrib** for computer vision, see [LICENSE](opencv344-contrib/LICENSE),
- **aruco**, module from opencv-contrib, for ***aruco markers*** detection.
- **rajawali**, for 3D models renderer.

See more about ***opencv***, and ***aruco*** from:
 
- [opencv](https://opencv.org)
- [opencv-contrib](https://github.com/opencv/opencv_contrib)
- [aruco](http://www.uco.es/investiga/grupos/ava/node/26)
- [rajawali](https://github.com/Rajawali/Rajawali)

## About

Aruco Android is an application to detect ***Aruco Markers***, and try to render a 3D model above it.

## Using

Camera must be calibrated before detect markers, for that, 

- compile and run ***camera-calibration*** projet, 
- present the ***calibration board*** to the camera
- capture between ***15 and 20 images***, and 
- click on ***calibrate*** menu.

Each frame must contain at least ***10 markers***

<center>
	<img width="75%" src="screenshots/board_detecting_markers.png" alt="screenshot_home" />
</center>

After that, compile ***app*** project and enjoy... 

<center>
	<img width="75%" src="screenshots/board_drawing_axis.png" alt="screenshot_home" />
</center>

## Contributions

You can help turn this application into Augmented Reality, there are some bugs on the positioning of the 3D model above marker. 

<center>
	<img width="75%" src="screenshots/marker_drawing_model.png" alt="screenshot_home" />
</center>

Thank you :)
