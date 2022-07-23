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
		forward();
	}
	public void forward(){
		double x = Robot.drive.getCurrentPosition().getX();
		PIDController p_pid = new PIDController(1, 0, 0, -1, 1, 0.05);
		p_pid.setSetpoint(1);
		while(!p_pid.isDone()){
			x = Robot.drive.getCurrentPosition().getX();
			p_pid.calculate(x, true);
			Robot.drive.setGyro(Robot.gyro.getGyroYaw());
			Robot.drive.swerveDrive(p_pid.getOutput(), 0, 0);
			log("Error: " + p_pid.getCurrentError());
		}
	}
	public void side(){
		double y = Robot.drive.getCurrentPosition().getY();
		PIDController p_pid = new PIDController(1, 0, 0, -1, 1, 0.05);
		p_pid.setSetpoint(1);
		while(!p_pid.isDone()){
			y = Robot.drive.getCurrentPosition().getY();
			p_pid.calculate(y, true);
			Robot.drive.setGyro(Robot.gyro.getGyroYaw());
			Robot.drive.swerveDrive(0, p_pid.getOutput(), 0);
			log("Error: " + p_pid.getCurrentError());
		}
	}
	public void rotate(){
		double Θ = Robot.drive.getCurrentPosition().getHeading();
		PIDController p_pid = new PIDController(1, 0, 0, -1, 1, 0.05);
		p_pid.setSetpoint(90);
		while(!p_pid.isDone()){
			Θ = Robot.drive.getCurrentPosition().getHeading();
			p_pid.calculate(Θ, true);
			Robot.drive.setGyro(Robot.gyro.getGyroYaw());
			Robot.drive.swerveDrive(0, 0, p_pid.getOutput());
			log("Error: " + p_pid.getCurrentError());
		}
	}


	
}