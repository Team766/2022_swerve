package com.team766.frc2022.mechanisms;
import com.team766.framework.Mechanism;
import com.team766.hal.EncoderReader;
import com.team766.hal.RobotProvider;
import com.team766.hal.SpeedController;
import com.team766.hal.CANSpeedController.ControlMode;
import com.team766.hal.mock.Joystick;
import com.team766.hal.simulator.Encoder;
import com.team766.hal.CANSpeedController;
import com.team766.library.ValueProvider;
import com.team766.logging.Category;
import com.team766.config.ConfigFileReader;
import com.team766.framework.Mechanism;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.sensors.AbsoluteSensorRange;
import com.ctre.phoenix.sensors.CANCoder;
import com.ctre.phoenix.sensors.CANCoderConfiguration;

public class FalconSwerveModule implements SwerveModule{
	CANCoder e_steer;
	CANSpeedController m_drive;
	CANSpeedController m_steer;
	final double CPR = 2048.0;
	final double reduction = 150.0/7.0;
	public FalconSwerveModule(CANCoder e_steer, CANSpeedController m_drive, CANSpeedController m_steer){
		this.e_steer = e_steer;
		this.m_drive = m_drive;
		this.m_steer = m_steer;
		setup(e_steer, m_drive, m_steer);
		configPID(m_drive);
		configPID(m_steer);
		//zero out the steer motor encoder
		m_steer.setPosition((int) Math.round(CPR/360.0 * (reduction) * e_steer.getAbsolutePosition()))
	}

	public void setSteerAngle(double angle){
		angle = newAngle(angle, Math.pow((CPR/360.0 * (reduction)), -1) * m_steer.getSensorPosition());
		m_steer.set(ControlMode.Position, CPR/360.0 * reduction * angle);
	}

	public void setDrivePower(double power){
		m_drive.set(power);
	}

	public void configPID(CANSpeedController m) {
		m.setP(0.2);
		m.setI(0);
		m.setD(0.1);
		m.setFF(0);
	}
}