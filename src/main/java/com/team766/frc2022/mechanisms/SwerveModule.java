//This was my bad attempt at a custom swerve, the sds code is way better and lets us focus on the cool stuff but I left it here just incase we wanted to use a custom swerve implementation for some reason
package com.team766.frc2022.mechanisms;

import com.team766.framework.Mechanism;
import com.team766.frc2022.Robot;
import com.team766.framework.Context;
import com.team766.logging.Category;
import com.team766.controllers.PIDController;
import com.team766.controllers.PIDController;
import com.team766.hal.SpeedController;
import com.team766.hal.CANSpeedController.ControlMode;
import com.team766.hal.CANSpeedController;

public class SwerveModule extends Mechanism
{
	/*
    public PIDController directionController;
	private CANSpeedController m_turn;
	private CANSpeedController m_drive;
	public double direction;
	public double power;
	public final double ppr = 256; //encodure pulses per revolutipn
	public final double radius = 0.075; //radius of wheel
    public SwerveModule(double P, double I, double D, CANSpeedController m_turn, CANSpeedController m_drive)
    {
        this.m_turn = m_turn;
		this.m_drive = m_drive;
        directionController = new PIDController(P, I, D, -12, 12, 0);
    }
	public void resetEncoders(){
		checkContextOwnership();
		m_turn.setPosition(0);
		m_drive.setPosition(0);
	}

	//returns wheel angle in deg
	public double getAngle(){
		checkContextOwnership();
		return (m_turn.getSensorPosition()/ppr)*360;
	}
    public void setDirection(double setpoint)
    {
		context.takeOwnership(Robot.drive);
        Robot.drive.resetGyro();
		directionController.reset();
		int gain;

		while (directionController.isDone()==false){ //not sure this is the right way to do it
			double currentAngle = getAngle();
			// find closest angle to setpoint
			double setpointAngle = closestAngle(currentAngle, setpoint);
			// find closest angle to setpoint + 180
			double setpointAngleFlipped = closestAngle(currentAngle, setpoint + 180.0);
			// if the closest angle to setpoint is shorter
			if (Math.abs(setpointAngle) <= Math.abs(setpointAngleFlipped))
			{
				// unflip the motor direction use the setpoint
				gain = 1;
				directionController.setSetpoint(currentAngle + setpointAngle);
			}
			// if the closest angle to setpoint + 180 is shorter
			else
			{
				// flip the motor direction and use the setpoint + 180
				gain = -1;
				directionController.setSetpoint(currentAngle + setpointAngleFlipped);
			}
			directionController.calculate(currentAngle, true);
			m_turn.set(directionController.getOutput()*gain);
		}


        
    }

    public void setSpeed(double speed)
    {
        m_drive.set(speed);
    }
	*/
}