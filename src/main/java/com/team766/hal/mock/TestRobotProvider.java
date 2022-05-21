package com.team766.hal.mock;

import com.team766.hal.AnalogInputReader;
import com.team766.hal.CANSpeedController;
import com.team766.hal.CameraInterface;
import com.team766.hal.CameraReader;
import com.team766.hal.Clock;
import com.team766.hal.DigitalInputReader;
import com.team766.hal.EncoderReader;
import com.team766.hal.GyroReader;
import com.team766.hal.JoystickReader;
import com.team766.hal.PositionReader;
import com.team766.hal.RelayOutput;
import com.team766.hal.RobotProvider;
import com.team766.hal.SolenoidController;
import com.team766.hal.SpeedController;
import com.team766.hal.wpilib.SystemClock;

public class TestRobotProvider extends RobotProvider{

	private boolean m_hasDriverStationUpdate = false;
	private CANSpeedController[] canMotors = new CANSpeedController[64];

	@Override
	public SpeedController getMotor(int index) {
		if(motors[index] == null)
			motors[index] = new Victor(index);
		return motors[index];
	}

	@Override
	public CANSpeedController getCANMotor(int index, CANSpeedController.Type type) {
		if(canMotors[index] == null)
			canMotors[index] = new Talon(index);
		return canMotors[index];
	}

	@Override
	public EncoderReader getEncoder(int index1, int index2) {
		if(encoders[index1] == null)
			encoders[index1] = new Encoder(index1, index2);
		return encoders[index1];
	}

	@Override
	public SolenoidController getSolenoid(int index) {
		if(solenoids[index] == null)
			solenoids[index] = new Solenoid(index);
		return solenoids[index];
	}

	@Override
	public GyroReader getGyro(int index) {
		if(gyros[0] == null)
			gyros[0] = new Gyro();
		return gyros[0];
	}

	@Override
	public CameraReader getCamera(String id, String value) {
		if(!cams.containsKey(id))
			cams.put(id, new Camera());
		return cams.get(id);
	}

	@Override
	public JoystickReader getJoystick(int index) {
		if(joysticks[index] == null)
			joysticks[index] = new Joystick();
		return joysticks[index];
	}
	
	@Override
	public DigitalInputReader getDigitalInput(int index) {
		if(digInputs[index] == null)
			digInputs[index] = new DigitalInput();
		return digInputs[index];
	}

	@Override
	public CameraInterface getCamServer() {
		return null;
	}
	
	@Override
	public AnalogInputReader getAnalogInput(int index) {
		if(angInputs[index] == null)
			angInputs[index] = new AnalogInput();
		return angInputs[index];
	}
	
	public RelayOutput getRelay(int index) {
		if(relays[index] == null)
			relays[index] = new Relay(index);
		return relays[index];
	}

	@Override
	public PositionReader getPositionSensor() {
		if (positionSensor == null)
			positionSensor = new PositionSensor();
		return positionSensor;
	}

	@Override
	public Clock getClock() {
		// TODO Replace this with a controlled clock
		return SystemClock.instance;
	}

	@Override
	public boolean hasNewDriverStationData() {
		boolean result = m_hasDriverStationUpdate;
		m_hasDriverStationUpdate = false;
		return result;
	}

	public void setHasNewDriverStationData() {
		m_hasDriverStationUpdate = true;
	}
}
