package com.team766.frc2022.procedures;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.frc2022.Robot;
import com.team766.logging.Category;
import com.team766.controllers.PIDController;

public class GoForward extends Procedure {

	public void run(Context context) {
		context.takeOwnership(Robot.drive);
		context.takeOwnership(Robot.gyro);
		loggerCategory = Category.AUTONOMOUS;
		log("Something is happening");
		Robot.drive.resetCurrentPosition();
		double x = Robot.drive.getCurrentPosition().getX();
		PIDController pid_x = new PIDController(0.1, 0, 0, -1, 1, 0.5);
		pid_x.setSetpoint(1);
		while(true){
			x = Robot.drive.getCurrentPosition().getX();
			log("X: " + x);
			pid_x.calculate(x, true);
			Robot.drive.setGyro(Robot.gyro.getGyroYaw());
			log("Output: " + pid_x.getOutput());
			Robot.drive.swerveDrive(pid_x.getOutput(), 0, 0);
			log("Error: %f", pid_x.getCurrentError());
		}
	}

	public void forward(){
		double x = Robot.drive.getCurrentPosition().getX();
		PIDController pid_x = new PIDController(1, 0, 0, -1, 1, 0.05);
		pid_x.setSetpoint(1);
		while(!pid_x.isDone()){
			x = Robot.drive.getCurrentPosition().getX();
			pid_x.calculate(x, true);
			Robot.drive.setGyro(Robot.gyro.getGyroYaw());
			Robot.drive.swerveDrive(pid_x.getOutput(), 0, 0);
			log("Error: %f", pid_x.getCurrentError());
		}
	}
	public void side(){
		double y = Robot.drive.getCurrentPosition().getY();
		PIDController pid_y = new PIDController(1, 0, 0, -1, 1, 0.05);
		pid_y.setSetpoint(1);
		while(!pid_y.isDone()){
			y = Robot.drive.getCurrentPosition().getY();
			pid_y.calculate(y, true);
			Robot.drive.setGyro(Robot.gyro.getGyroYaw());
			Robot.drive.swerveDrive(0, pid_y.getOutput(), 0);
			log("Error: " + pid_y.getCurrentError());
		}
	}
	public void rotate(){
		double H = Robot.drive.getCurrentPosition().getH();
		PIDController pid_H = new PIDController(1, 0, 0, -1, 1, 0.05);
		pid_H.setSetpoint(90);
		while(!pid_H.isDone()){
			H = Robot.drive.getCurrentPosition().getH();
			pid_H.calculate(H, true);
			Robot.drive.setGyro(Robot.gyro.getGyroYaw());
			Robot.drive.swerveDrive(0, 0, pid_H.getOutput());
			log("Error: " + pid_H.getCurrentError());
		}
	}


	
}