package com.team766.frc2022.procedures;

import com.team766.framework.Procedure;
import com.team766.framework.Context;
import com.team766.framework.LaunchedContext;
import com.team766.frc2022.Robot;
import com.team766.library.ValueProvider;
import com.team766.hal.RobotProvider;
import com.team766.frc2022.PointDir;
import com.team766.frc2022.Point;
import com.team766.hal.PositionReader;
import com.team766.config.ConfigFileReader;
import com.team766.logging.Category;
import com.team766.controllers.PIDController;
 
public class FollowPoints extends Procedure {
	private PointDir currentPos = new PointDir(0.0, 0.0, 0.0);
	private Point[] pointList;
	private Procedure[] proceduresAtPoints;
	private static double radius = ConfigFileReader.getInstance().getDouble("trajectory.radius").get();
	private static double leniency = ConfigFileReader.getInstance().getDouble("trajectory.leniency").get();
	private static double straightVelocity = ConfigFileReader.getInstance().getDouble("trajectory.straightVelocity").get();
	private static double theBrettConstant = ConfigFileReader.getInstance().getDouble("trajectory.theBrettConstant").get();
	private double finalHeader;
	private double turning;
	
	private PIDController p_turningController = new PIDController(0, 0, 0, -1, 1, 5);

	public FollowPoints() {
		parsePointList();
		proceduresAtPoints = new Procedure[pointList.length];
		for (int i = 0; i < proceduresAtPoints.length; i++) {
			proceduresAtPoints[i] = new DoNothing();
		}
		loggerCategory = Category.AUTONOMOUS;
	}

	public FollowPoints(Procedure[] procedureList) {
		parsePointList();
		proceduresAtPoints = procedureList;
		loggerCategory = Category.AUTONOMOUS;
	}

	public FollowPoints(Point[] points) {
		pointList = points;
		finalHeader = Robot.drive.getCurrentPosition().getHeading();
		proceduresAtPoints = new Procedure[pointList.length];
		for (int i = 0; i < proceduresAtPoints.length; i++) {
			proceduresAtPoints[i] = new DoNothing();
		}
		loggerCategory = Category.AUTONOMOUS;
	}

	public FollowPoints(Point[] points, Procedure[] procedureList) {
		pointList = points;
		finalHeader = Robot.drive.getCurrentPosition().getHeading();
		proceduresAtPoints = procedureList;
		loggerCategory = Category.AUTONOMOUS;
	}

	public FollowPoints(Point[] points, double header) {
		pointList = points;
		finalHeader = header;
		proceduresAtPoints = new Procedure[pointList.length];
		for (int i = 0; i < proceduresAtPoints.length; i++) {
			proceduresAtPoints[i] = new DoNothing();
		}
		loggerCategory = Category.AUTONOMOUS;
	}

	public FollowPoints(Point[] points, double header, Procedure[] procedureList) {
		pointList = points;
		finalHeader = header;
		proceduresAtPoints = procedureList;
		loggerCategory = Category.AUTONOMOUS;
	}

	//Takes pairs of points from pointDoubles (set in the config file) and converts them to Points, which are placed in pointList.
	private void parsePointList() {
		Double[] pointDoubles = ConfigFileReader.getInstance().getDoubles("trajectory.points").get();
		pointList = new Point[pointDoubles.length / 2];
		double pointX = 0;
		double pointY = 0;
		for (int i = 0; i < pointDoubles.length / 2 * 2; i++) {
			if (i % 2 == 0)
				pointX = pointDoubles[i];
			else {
				pointY = pointDoubles[i];
				pointList[i / 2] = new Point(pointX, pointY);
			}
		}
		if (pointDoubles.length % 2 == 1) {
			finalHeader = pointDoubles[pointDoubles.length - 1];
		} else {
			finalHeader = Robot.drive.getCurrentPosition().getHeading();
		}

		while (finalHeader > Math.round(Robot.drive.getCurrentPosition().getHeading() / 360) * 360 + 180) {
			finalHeader -= 360;
		}
		while (finalHeader <= Math.round(Robot.drive.getCurrentPosition().getHeading() / 360) * 360 - 180) {
			finalHeader += 360;
		}
		if (finalHeader - Robot.drive.getCurrentPosition().getHeading() >= 180) {
			finalHeader -= 360;
		}
		if (finalHeader - Robot.drive.getCurrentPosition().getHeading() < -180) {
			finalHeader += 360;
		}

		p_turningController.setSetpoint(finalHeader);
	}

	public void run(Context context) {
		context.takeOwnership(Robot.drive);
		if (pointList.length > 0) {
			int targetNum = 0;
			Point targetPoint = new Point(0.0, 0.0);
			
			currentPos.set(Robot.drive.getCurrentPosition().getX(), Robot.drive.getCurrentPosition().getY(), Robot.drive.getCurrentPosition().getHeading());
			while (currentPos.distance(pointList[pointList.length - 1]) > leniency || targetNum != pointList.length - 1) {
				currentPos.set(Robot.drive.getCurrentPosition().getX(), Robot.drive.getCurrentPosition().getY(), Robot.drive.getCurrentPosition().getHeading());
				if (currentPos.distance(pointList[targetNum]) <= radius && checkIntersection(targetNum, currentPos, pointList, radius)) {
					if (proceduresAtPoints.length < targetNum) {
						context.startAsync(proceduresAtPoints[targetNum]);
					}
					targetNum++;
					log("Going to Next Point!");
				}
				targetPoint = selectTargetPoint(targetNum, currentPos, pointList, radius);
				//double diff = currentPos.getAngleDifference(targetPoint);
				//Robot.drive.setDrivePower(straightVelocity + Math.signum(diff) * Math.min(Math.abs(diff) * theBrettConstant, 1 - straightVelocity), straightVelocity - Math.signum(diff) * Math.min(Math.abs(diff) * theBrettConstant, 1 - straightVelocity));
				
				p_turningController.calculate(currentPos.getHeading(), true);
				
				if(p_turningController.isDone()){
					turning = 0;
				} else {
					turning = p_turningController.getOutput();
				}
				Robot.drive.swerveDrive(new PointDir(currentPos.getUnitVector(targetPoint), turning));

				context.yield();
			}
			while (!p_turningController.isDone()) {
				turning = p_turningController.getOutput();
				Robot.drive.turning(turning);
				context.yield();
			}
			Robot.drive.drive2D(0, 0);
			log("Finished method!");
		} else {
			log("No points!");
		}
	}

	//Returns whether the circle around the robot intersects the line connecting the two next points.
	public static boolean checkIntersection(int targetNum, PointDir currentPos, Point[] pointList, double radius) {
		double aValue;
		double bValue;
		double cValue;
		double slope;
		if (targetNum < pointList.length - 1) {
			slope = pointList[targetNum].slope(pointList[targetNum + 1]);
			aValue = slope * slope + 1;
			bValue = -2 * currentPos.getX() - 2 * slope * slope * pointList[targetNum].getX() + 2 * slope * pointList[targetNum].getY() - 2 * currentPos.getY() * slope;
			cValue = currentPos.getX() * currentPos.getX() + slope * slope * pointList[targetNum].getX() * pointList[targetNum].getX() - 2 * slope * pointList[targetNum].getX() * pointList[targetNum].getY() + pointList[targetNum].getY() * pointList[targetNum].getY() + 2 * currentPos.getY() * slope * pointList[targetNum].getX() - 2 * currentPos.getY() * pointList[targetNum].getY() + currentPos.getY() * currentPos.getY() - radius * radius;
			if (bValue * bValue - 4 * aValue * cValue > 0) {
				return true;                                        
			}
		}
		return false;
	}

	//If the circle around the robot intersects the line connecting the previous and next points, returns whichever intersection point is closest to the next point. Otherwise, returns the next point.
	public static Point selectTargetPoint(int targetNum, PointDir currentPos, Point[] pointList, double radius) {
		double aValue;
		double bValue;
		double cValue;
		double slope;
		double potX1;
		double potX2;
		double potY1;
		double potY2;
		Point pot1 = new Point(0.0, 0.0);
		Point pot2 = new Point(0.0, 0.0);
		if (targetNum == 0) {
			return pointList[0];
		} else {
			slope = pointList[targetNum - 1].slope(pointList[targetNum]);
			aValue = slope * slope + 1;
			bValue = -2 * currentPos.getX() - 2 * slope * slope * pointList[targetNum - 1].getX() + 2 * slope * pointList[targetNum - 1].getY() - 2 * currentPos.getY() * slope;
			cValue = currentPos.getX() * currentPos.getX() + slope * slope * pointList[targetNum - 1].getX() * pointList[targetNum - 1].getX() - 2 * slope * pointList[targetNum - 1].getX() * pointList[targetNum - 1].getY() + pointList[targetNum - 1].getY() * pointList[targetNum - 1].getY() + 2 * currentPos.getY() * slope * pointList[targetNum - 1].getX() - 2 * currentPos.getY() * pointList[targetNum - 1].getY() + currentPos.getY() * currentPos.getY() - radius * radius;
			if (bValue * bValue - 4 * aValue * cValue < 0) {
				return pointList[targetNum];
			} else {
				potX1 = (-1 * bValue + Math.sqrt(bValue * bValue - 4 * aValue * cValue))/ (2 * aValue);
				potX2 = (-1 * bValue - Math.sqrt(bValue * bValue - 4 * aValue * cValue))/ (2 * aValue);
				potY1 = slope * (potX1 - pointList[targetNum - 1].getX()) + pointList[targetNum - 1].getY();
				potY2 = slope * (potX2 - pointList[targetNum - 1].getX()) + pointList[targetNum - 1].getY();
				pot1.set(potX1, potY1);
				pot2.set(potX2, potY2);
				if (pot1.distance(pointList[targetNum]) <= pot2.distance(pointList[targetNum])) {
					return pot1;
				} else {
					return pot2;
				}
			}
		}
	}
}
