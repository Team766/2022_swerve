package com.team766.frc2022.procedures;

import com.team766.frc2022.Pose;
import java.util.ArrayList;
import java.util.Arrays;
import com.team766.framework.Context;
import com.team766.framework.Procedure;
import javax.swing.text.AsyncBoxView;
import com.team766.framework.LaunchedContext;
import com.team766.frc2022.Robot;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.geometry.Rotation2d;

public class StraightLines extends Procedure {

	public void run(Context context) {		
		context.takeOwnership(Robot.drive);
		Robot.drive.zeroGyroscope();
		ArrayList<Pose> poses = new ArrayList<Pose>();
		Pose pose1 = new Pose(1,0); //add in terms of meters from robot
		poses.add(pose1);
		double currentX = 0;
		double currentY = 0;
		double distance = 0;
		double x_raw = 0;
		double y_raw = 0;
		for(Pose nextPose : poses){
			Robot.drive.resetEncoders();
			distance = nextPose.distanceFromCurrent(currentX, currentY);
			x_raw = nextPose.bestVx(currentX, currentY);
			y_raw = nextPose.bestVy(currentX, currentY);
			double rotation = 0.0;
			while(Math.abs(distance-Robot.drive.getDistanceTraveled()) > 0.1){
			Robot.drive.setSwerve(ChassisSpeeds.fromFieldRelativeSpeeds(x_raw, y_raw, rotation, Rotation2d.fromDegrees(Robot.gyro.getFusedHeading())));
			}
			x_raw = 0;
			y_raw = 0;
			Robot.drive.setSwerve(ChassisSpeeds.fromFieldRelativeSpeeds(x_raw, y_raw, rotation, Rotation2d.fromDegrees(Robot.gyro.getFusedHeading())));
			currentX = nextPose.getX();
			currentY = nextPose.getY();
			context.waitForSeconds(2);
		}


	}
	
}