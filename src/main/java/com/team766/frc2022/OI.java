package com.team766.frc2022;

import com.team766.framework.Procedure;
import com.team766.framework.Context;
import com.team766.frc2022.Robot;
import com.team766.frc2022.procedures.DefenseCross;
import com.team766.frc2022.procedures.*;
import com.team766.hal.JoystickReader;
import com.team766.hal.RobotProvider;
import com.team766.logging.Category;

/**
 * This class is the glue that binds the controls on the physical operator
 * interface to the code that allow control of the robot.
 */
public class OI extends Procedure {
	//declaring the variables we will use later on
	private JoystickReader m_leftJoystick;
	private JoystickReader m_rightJoystick;
	private double RightJoystick_X = 0;
	private double RightJoystick_Y = 0;
	private double RightJoystick_Z = 0;
	private double RightJoystick_Theta = 0;
	private double LeftJoystick_X = 0;
	private double LeftJoystick_Y = 0;
	private double LeftJoystick_Z = 0;
	private double LeftJoystick_Theta = 0;
	double turningValue = 0;

	public OI() {
		//this sets what category our logs will go to
		loggerCategory = Category.OPERATOR_INTERFACE;
		//setup the joysticks
		m_leftJoystick = RobotProvider.instance.getJoystick(0);
		m_rightJoystick = RobotProvider.instance.getJoystick(1);

	}

	public void run(Context context) {
		double prev_time = RobotProvider.instance.getClock().getTime();
		//take ownership of the appropriate threads
		context.takeOwnership(Robot.gyro);
		context.takeOwnership(Robot.drive);
		
		//make sure our encoders are setup right
		Robot.drive.setFrontRightEncoders();
		Robot.drive.setFrontLeftEncoders();
		Robot.drive.setBackRightEncoders();
		Robot.drive.setBackLeftEncoders();
		
		//this is where the actual "controlling" happens
		while (true) {
			//get the robot orientation
			Robot.drive.setGyro(Robot.gyro.getGyroYaw());

			//Check the joystick positions but don't count drift		
			if(Math.abs(m_rightJoystick.getAxis(InputConstants.AXIS_FORWARD_BACKWARD)) > 0.05){
				RightJoystick_Y = m_rightJoystick.getAxis(InputConstants.AXIS_FORWARD_BACKWARD);
			} else {
				RightJoystick_Y = 0;
			}
			if(Math.abs(m_rightJoystick.getAxis(InputConstants.AXIS_LEFT_RIGHT)) > 0.05){
				RightJoystick_X = m_rightJoystick.getAxis(InputConstants.AXIS_LEFT_RIGHT)/2;
				if(m_rightJoystick.getButton(InputConstants.JOYSTICK_FAST_TURNING)){
					RightJoystick_X *= 2;
				}	
			} else {
				RightJoystick_X = 0;	
			}
			if(Math.abs(m_rightJoystick.getAxis(InputConstants.AXIS_TWIST)) > 0.05){
				RightJoystick_Theta = m_rightJoystick.getAxis(InputConstants.AXIS_TWIST);
			} else {
				RightJoystick_Theta = 0;
			}
			if(Math.abs(m_leftJoystick.getAxis(InputConstants.AXIS_FORWARD_BACKWARD)) > 0.05){
				LeftJoystick_Y = m_leftJoystick.getAxis(InputConstants.AXIS_FORWARD_BACKWARD);
			} else {
				LeftJoystick_Y = 0;
			}
			if(Math.abs(m_leftJoystick.getAxis(InputConstants.AXIS_LEFT_RIGHT)) > 0.05){
				LeftJoystick_X = m_leftJoystick.getAxis(InputConstants.AXIS_LEFT_RIGHT);
			} else {
				LeftJoystick_X = 0;
			}
			if(Math.abs(m_leftJoystick.getAxis(InputConstants.AXIS_TWIST)) > 0.05){
				LeftJoystick_Theta = m_leftJoystick.getAxis(InputConstants.AXIS_TWIST);
			} else {
				LeftJoystick_Theta = 0;
			}

			if(m_rightJoystick.getButtonPressed(InputConstants.JOYSTICK_RESET_GYRO)){
				Robot.gyro.resetGyro();
			}

			//pass the joystick info to the swerve drive
			if(Math.abs(LeftJoystick_X)+Math.abs(LeftJoystick_Y)+Math.abs(RightJoystick_X) > 0){
				Robot.drive.swerveDrive( 
					(LeftJoystick_X),
					(LeftJoystick_Y),
					(RightJoystick_X)
				);
			} else{
				//if the joysticks aren't being used, stop driving
				Robot.drive.stopDriveMotors();
				Robot.drive.stopSteerMotors();				
			}

			//check how long this took
			double cur_time = RobotProvider.instance.getClock().getTime();
			//only run the next iteration of the loop when we have new joystick data from the driver station
			context.waitFor(() -> RobotProvider.instance.hasNewDriverStationData());
		}
	}
}
