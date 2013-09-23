package com.jingz.app.pano.sensor;

import com.jingz.app.pano.math.Vector3;

public class SensorReader {
	private float accelFilterCoefficient = 0.15f;
	private Vector3 accelerationVector3 = new Vector3();
	private float angularVelocitySqrRad = 0.0f;
	private OrientationEKF ekf = new OrientationEKF();
}
