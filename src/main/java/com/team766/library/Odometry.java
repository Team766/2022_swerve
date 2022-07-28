package com.team766.library;

import com.team766.framework.Loggable;
import com.team766.frc2022.PointDir;
import com.team766.hal.CANSpeedController;
import com.ctre.phoenix.sensors.CANCoder;
import com.team766.logging.Category;
import com.team766.frc2022.Robot;

public class Odometry extends Loggable {

	private RateLimiter odometryLimiter;
	private CANSpeedController[] motorList;
	private CANCoder[] CANCoderList;
	private	int motorCount; 

	private PointDir[] prevPositions;
	private PointDir[] currPositions;
	private double[] prevEncoderValues;
	private double[] currEncoderValues;
	private double gyroPosition;

	private PointDir currentPosition;

	//In Centimeters
	private static final double WHEEL_DISTANCE = 11.0446616728 * 2.54;
	public static final double GEAR_RATIO = 6.75;
	public static final int ENCODER_TO_REVOLUTION_CONSTANT = 2048;

	public Odometry(CANSpeedController[] motors, CANCoder[] CANCoders, double rateLimiterTime) {
		loggerCategory = Category.DRIVE;

		odometryLimiter = new RateLimiter(rateLimiterTime);
		motorList = motors;
		CANCoderList = CANCoders;
		motorCount = motorList.length;

		prevPositions = new PointDir[motorCount];
		currPositions = new PointDir[motorCount];
		prevEncoderValues = new double[motorCount];
		currEncoderValues = new double[motorCount];
	}

	public String getName() {
		return "Odometry";
	}

	public void resetCurrentPosition() {
		currentPosition.set(0, 0);
		for (int i = 0; i < motorCount; i++) {
			prevPositions[i].set(0,0);
			currPositions[i].set(0,0);
		}
	}

	private void setCurrentEncoderValues() {
		for (int i = 0; i < motorCount; i++) {
			prevEncoderValues[i] = currEncoderValues[i];
			currEncoderValues[i] = motorList[i].getSensorPosition();
		}
	}

	private void updateCurrentPositions() {
		double angleChange;
		double radius;
		double deltaX;
		double deltaY;
		gyroPosition = Robot.gyro.getGyroYaw();

		for (int i = 0; i < motorCount; i++) {
			prevPositions[i] = currPositions[i];
			currPositions[i].setHeading(CANCoderList[i].getAbsolutePosition() + gyroPosition);
			angleChange = currPositions[i].getHeading() - prevPositions[i].getHeading();
			radius = 180 * (currEncoderValues[i] - prevEncoderValues[i] / (Math.PI * angleChange));
			deltaY = radius * Math.sin(Math.toRadians(angleChange));
			deltaX = -1 * radius * (1 - Math.cos(Math.toRadians(angleChange)));
			currPositions[i].setX(prevPositions[i].getX() + (Math.cos(Math.toRadians(prevPositions[i].getHeading())) * deltaX  + Math.sin(Math.toRadians(prevPositions[i].getHeading())) * deltaY) * WHEEL_DISTANCE / (GEAR_RATIO * ENCODER_TO_REVOLUTION_CONSTANT));
			currPositions[i].setY(prevPositions[i].getY() + (Math.sin(Math.toRadians(prevPositions[i].getHeading())) * deltaX  + Math.cos(Math.toRadians(prevPositions[i].getHeading())) * deltaY) * WHEEL_DISTANCE / (GEAR_RATIO * ENCODER_TO_REVOLUTION_CONSTANT));
		}
	}

	private void findRobotPosition() {
		double avgX = 0;
		double avgY = 0;
		for (int i = 0; i < motorCount; i++) {
			avgX += currPositions[i].getX();
			avgY += currPositions[i].getY();
		}
		currentPosition.set(avgX / motorCount, avgY / motorCount, gyroPosition);
	}

	//Intended to be placed inside Robot.drive.run()
	public PointDir run() {
		if (odometryLimiter.next()) {
			setCurrentEncoderValues();
			updateCurrentPositions();
			findRobotPosition();
		}
		return currentPosition;
	}
}
