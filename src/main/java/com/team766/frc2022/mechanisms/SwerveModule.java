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

public interface SwerveModule{
	default void setup(CANCoder e_steer, CANSpeedController m_drive, CANSpeedController m_steer){
		CANCoderConfiguration config = new CANCoderConfiguration();
		config.absoluteSensorRange = AbsoluteSensorRange.Signed_PlusMinus180;
		//The encoders output "encoder" values, so we need to convert that to degrees (because that is what the cool kids are using)
		config.sensorCoefficient = 360.0 / 4096.0;
		//The offset is going to be changed in ctre, but we can change it here too.
		//config.magnetOffsetDegrees = Math.toDegrees(configuration.getOffset());
		config.sensorDirection = true;
		e_steer.configAllSettings(config, 250);
		m_drive.setCurrentLimit(15);
		m_steer.setCurrentLimit(10);
	}
	
	//bounds angle between -180 and 180 deg
	default double newAngle(double newAngle, double lastAngle){
		while(newAngle<0) newAngle += 360;
		while(newAngle < (lastAngle - 180)) newAngle+=360;
		while(newAngle > (lastAngle + 180)) newAngle-=360;
		return newAngle;
	}

	void setSteerAngle();

	void setDrivePower();

}
