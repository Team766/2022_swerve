package com.team766.frc2022.mechanisms;
import com.team766.framework.Mechanism;
import com.team766.hal.EncoderReader;
import com.team766.hal.RobotProvider;
import com.team766.hal.SpeedController;
import com.team766.hal.CANSpeedController.ControlMode;
import com.team766.hal.mock.Joystick;
import com.team766.hal.simulator.Encoder;
import com.team766.hal.CANSpeedController;
import com.team766.library.RateLimiter;
import com.team766.library.ValueProvider;
import com.team766.logging.Category;
import com.team766.simulator.ProgramInterface.RobotMode;
import com.team766.config.ConfigFileReader;
import com.team766.framework.Mechanism;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.sensors.AbsoluteSensorRange;
import com.ctre.phoenix.sensors.CANCoder;
import com.ctre.phoenix.sensors.CANCoderConfiguration;
import com.team766.frc2022.Point;
import com.team766.frc2022.PointDir;


public class Drive extends Mechanism {
	public final ValueProvider<Double> LENGTH_OF_CHASSIS = ConfigFileReader.getDouble("LENGTH_OF_CHASSIS");
	public final ValueProvider<Double> WIDTH_OF_CHASSIS = ConfigFileReader.getDouble("WIDTH_OF_CHASSIS");

	private CANSpeedController m_DriveFrontRight;
    private CANSpeedController m_DriveFrontLeft;
	private CANSpeedController m_DriveBackRight;
	private CANSpeedController m_DriveBackLeft;

    private CANSpeedController m_SteerFrontRight;
    private CANSpeedController m_SteerFrontLeft;
	private CANSpeedController m_SteerBackRight;
	private CANSpeedController m_SteerBackLeft;

	private CANCoder e_FrontRight;
	private CANCoder e_FrontLeft;
	private CANCoder e_BackRight;
	private CANCoder e_BackLeft;
    
	private ValueProvider<Double> drivePower;

	private double gyroValue;

	private static PointDir currentPosition;

	//Uses centimeters
	private static final double WHEEL_DISTANCE = 11.0446616728 * 2.54;

	private static double prevBackLeft;
	private static double prevBackRight;
	private static double prevFrontLeft;
	private static double prevFrontRight;

	private static double currBackLeft;
	private static double currBackRight;
	private static double currFrontLeft;
	private static double currFrontRight;

	public static double avgX;
	public static double avgY;
	public static final double GEAR_RATIO = 6.75;
	public static final int ENCODER_TO_REVOLUTION_CONSTANT = 2048;

	RateLimiter odometryLimiter;
	
	public Drive() {
		
		loggerCategory = Category.DRIVE;
        // Initializations of motors
		//Initialize the drive motors
        m_DriveFrontRight = RobotProvider.instance.getCANMotor("drive.DriveFrontRight"); 
		m_DriveFrontLeft = RobotProvider.instance.getCANMotor("drive.DriveFrontLeft"); 
		m_DriveBackRight = RobotProvider.instance.getCANMotor("drive.DriveBackRight"); 
		m_DriveBackLeft = RobotProvider.instance.getCANMotor("drive.DriveBackLeft"); 
		//Initialize the steering motors
		m_SteerFrontRight = RobotProvider.instance.getCANMotor("drive.SteerFrontRight"); 
		m_SteerFrontLeft = RobotProvider.instance.getCANMotor("drive.SteerFrontLeft"); 
		m_SteerBackRight = RobotProvider.instance.getCANMotor("drive.SteerBackRight"); 
		m_SteerBackLeft = RobotProvider.instance.getCANMotor("drive.SteerBackLeft");
		
		//Setting up the "config" 
		CANCoderConfiguration config = new CANCoderConfiguration();
		config.absoluteSensorRange = AbsoluteSensorRange.Signed_PlusMinus180;
		//The encoders output "encoder" values, so we need to convert that to degrees (because that is what the cool kids are using)
		config.sensorCoefficient = 360.0 / 4096.0;
		//The offset is going to be changed in ctre, but we can change it here too.
		//config.magnetOffsetDegrees = Math.toDegrees(configuration.getOffset());
		config.sensorDirection = true;

		//initialize the encoders
		e_FrontRight = new CANCoder(1);
		//e_FrontRight.configAllSettings(config, 250);
		e_FrontLeft = new CANCoder(2);
		//e_FrontLeft.configAllSettings(config, 250);
		e_BackRight = new CANCoder(4);
		//e_BackRight.configAllSettings(config, 250);
		e_BackLeft = new CANCoder(3);
		//e_BackLeft.configAllSettings(config, 250);
 
		
		//Current limit for motors to avoid breaker problems (mostly to avoid getting electrical people to yell at us)
		m_DriveFrontRight.setCurrentLimit(15);
		m_DriveFrontLeft.setCurrentLimit(15);
		m_DriveBackRight.setCurrentLimit(15);
		m_DriveBackLeft.setCurrentLimit(15);
		m_DriveBackLeft.setInverted(true);
		m_DriveBackRight.setInverted(true);
		m_SteerFrontRight.setCurrentLimit(10);
		m_SteerFrontLeft.setCurrentLimit(10);
		m_SteerBackRight.setCurrentLimit(10);
		m_SteerBackLeft.setCurrentLimit(10);

		//Setting up the connection between steering motors and cancoders
		//m_SteerFrontRight.setRemoteFeedbackSensor(e_FrontRight, 0);
		//m_SteerFrontLeft.setRemoteFeedbackSensor(e_FrontLeft, 0);
		//m_SteerBackRight.setRemoteFeedbackSensor(e_BackRight, 0);
		//m_SteerBackLeft.setRemoteFeedbackSensor(e_BackLeft, 0);

		m_SteerFrontRight.setSelectedFeedbackSensor(FeedbackDevice.IntegratedSensor);
		m_SteerFrontLeft.setSelectedFeedbackSensor(FeedbackDevice.IntegratedSensor);
		m_SteerBackRight.setSelectedFeedbackSensor(FeedbackDevice.IntegratedSensor);	
		m_SteerBackLeft.setSelectedFeedbackSensor(FeedbackDevice.IntegratedSensor);
		configPID();

		//Circumference of each wheel, in centimeters
		currentPosition = new PointDir(0, 0, 0);

		odometryLimiter = new RateLimiter(0.05);
	}
	//If you want me to repeat code, then no.
	public double pythagrian(double x, double y) {
		return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
	}
	public double getAngle(double LR, double FB){
		return Math.toDegrees(Math.atan2(LR ,-FB));
	}
	public double round(double value, int places) {
		double scale = Math.pow(10, places);
		return Math.round(value * scale) / scale;
	}
	public double NewAng(double FirstMag, double FirstAng, double SecondMag, double SecondAng){
		double FinalX = FirstMag*Math.cos(Math.toRadians(FirstAng)) + SecondMag*Math.cos(Math.toRadians(SecondAng));
		double FinalY = FirstMag*Math.sin(Math.toRadians(FirstAng)) + SecondMag*Math.sin(Math.toRadians(SecondAng));
		return round(Math.toDegrees(Math.atan2(FinalY,FinalX)),5);
	}
	public double NewMag(double FirstMag, double FirstAng, double SecondMag, double SecondAng){
		double FinalX = FirstMag*Math.cos(Math.toRadians(FirstAng)) + SecondMag*Math.cos(Math.toRadians(SecondAng));
		double FinalY = FirstMag*Math.sin(Math.toRadians(FirstAng)) + SecondMag*Math.sin(Math.toRadians(SecondAng));
		return round(Math.sqrt(Math.pow(FinalX,2) + Math.pow(FinalY,2)),5);
	}

	public static double correctedJoysticks(double Joystick){
		if(Joystick >= 0)
			  return(3.0*Math.pow(Joystick,2)-2.0*Math.pow(Joystick,3));
		  else  
		  return(-1*3.0*Math.pow(-1*Joystick,2)+2.0*Math.pow(-1*Joystick,3));
	}

	
	public static double fieldAngle(double angle, double gyro){
		double newAngle;
		newAngle = angle - gyro;
		if(newAngle < 0){
			newAngle = newAngle + 360;
		}
		if(newAngle >= 180){
			newAngle = newAngle -360;
		}
		return newAngle;
	}
	public static double newAngle(double newAngle, double lastAngle){
		while(newAngle<0) newAngle += 360;
		while(newAngle < (lastAngle - 180)) newAngle+=360;
		while(newAngle > (lastAngle + 180)) newAngle-=360;
		return newAngle;
	}
	//Not the actual gyro, but I am passing it through the OI.java to get it here
	public void setGyro(double value){
		gyroValue = value;
	}
	//This is the method that is called to drive the robot in the 2D plane
    public void drive2D(double JoystickX, double JoystickY) {
		checkContextOwnership();
		//logs();
		//double power = pythagrian((JoystickX), correctedJoysticks(JoystickY))/Math.sqrt(2);
		double power = Math.max(Math.abs(JoystickX),Math.abs(JoystickY));
		double angle = fieldAngle(getAngle(JoystickX, JoystickY),gyroValue);
		log("Given angle: " + getAngle(JoystickX,JoystickY) + " || Gyro: " + gyroValue + " || New angle: " + angle);
		//Temporary Drive code, kinda sucks
		m_DriveFrontRight.set(power);
		m_DriveFrontLeft.set(power);
		m_DriveBackRight.set(power);
		m_DriveBackLeft.set(power);
		//Steer code
		setFrontRightAngle(newAngle(angle, Math.pow((2048.0/360.0 * (150.0/7.0)), -1) * m_SteerFrontRight.getSensorPosition()));
		setFrontLeftAngle(newAngle(angle, Math.pow((2048.0/360.0 * (150.0/7.0)), -1) * m_SteerFrontLeft.getSensorPosition()));
		setBackRightAngle(newAngle(angle, Math.pow((2048.0/360.0 * (150.0/7.0)), -1) * m_SteerBackRight.getSensorPosition()));
		setBackLeftAngle(newAngle(angle, Math.pow((2048.0/360.0 * (150.0/7.0)), -1) * m_SteerBackLeft.getSensorPosition()));
	}

	public void drive2D(Point joystick) {
		drive2D(joystick.getX(), joystick.getY());
	}

    public void stopDriveMotors() {
		checkContextOwnership();
		m_DriveFrontRight.stopMotor();
		m_DriveFrontLeft.stopMotor();
		m_DriveBackRight.stopMotor();
		m_DriveBackLeft.stopMotor();
	}
	public void stopSteerMotors() {
		checkContextOwnership();
		m_SteerFrontRight.stopMotor();
		m_SteerFrontLeft.stopMotor();
		m_SteerBackRight.stopMotor();
		m_SteerBackLeft.stopMotor();
	}


	public void swerveDrive(double JoystickX, double JoystickY, double JoystickTheta){
		checkContextOwnership();
		double power = Math.max(Math.abs(JoystickX),Math.abs(JoystickY));
		double angle = fieldAngle(getAngle(JoystickX, JoystickY),gyroValue);
		double frPower;
		double flPower;
		double brPower;
		double blPower;
		double frAngle;
		double flAngle;
		double brAngle;
		double blAngle;
		if(JoystickTheta >= 0){
			frPower = NewMag(power, angle, JoystickTheta, 135);
			flPower = NewMag(power, angle, JoystickTheta, 45);
			brPower = NewMag(power, angle, JoystickTheta, -135);
			blPower = NewMag(power, angle, JoystickTheta, -45);
			frAngle = NewAng(power, angle, JoystickTheta, 135);
			flAngle = NewAng(power, angle, JoystickTheta, 45);
			brAngle = NewAng(power, angle, JoystickTheta, -135);
			blAngle = NewAng(power, angle, JoystickTheta, -45);
		}
		else{
			frPower = NewMag(power, angle, Math.abs(JoystickTheta), -45);
			flPower = NewMag(power, angle, Math.abs(JoystickTheta), -135);
			brPower = NewMag(power, angle, Math.abs(JoystickTheta), 45);
			blPower = NewMag(power, angle, Math.abs(JoystickTheta), 135);
			frAngle = NewAng(power, angle, Math.abs(JoystickTheta), -45);
			flAngle = NewAng(power, angle, Math.abs(JoystickTheta), -135);
			brAngle = NewAng(power, angle, Math.abs(JoystickTheta), 45);
			blAngle = NewAng(power, angle, Math.abs(JoystickTheta), 135);
		}
		if(Math.max(Math.max(frPower,flPower), Math.max(brPower,blPower)) > 1){
			frPower /= Math.max(Math.max(frPower,flPower), Math.max(brPower,blPower));
			flPower /= Math.max(Math.max(frPower,flPower), Math.max(brPower,blPower));
			brPower /= Math.max(Math.max(frPower,flPower), Math.max(brPower,blPower));
			blPower /= Math.max(Math.max(frPower,flPower), Math.max(brPower,blPower));
		}
		m_DriveFrontRight.set(frPower);
		m_DriveFrontLeft.set(flPower);
		m_DriveBackRight.set(brPower);
		m_DriveBackLeft.set(blPower);
		//Steer code
		setFrontRightAngle(newAngle(frAngle, Math.pow((2048.0/360.0 * (150.0/7.0)), -1) * m_SteerFrontRight.getSensorPosition()));
		setFrontLeftAngle(newAngle(flAngle, Math.pow((2048.0/360.0 * (150.0/7.0)), -1) * m_SteerFrontLeft.getSensorPosition()));
		setBackRightAngle(newAngle(brAngle, Math.pow((2048.0/360.0 * (150.0/7.0)), -1) * m_SteerBackRight.getSensorPosition()));
		setBackLeftAngle(newAngle(blAngle, Math.pow((2048.0/360.0 * (150.0/7.0)), -1) * m_SteerBackLeft.getSensorPosition()));
	}

	public void swerveDrive(PointDir joystick) {
		swerveDrive(joystick.getY(), -1 * joystick.getX(), joystick.getHeading());
	}
	    
public void turning(double Joystick){
	checkContextOwnership();
	if(Joystick > 0){
		setFrontRightAngle(newAngle(135, Math.pow((2048.0/360.0 * (150.0/7.0)), -1) * m_SteerFrontRight.getSensorPosition()));
		setFrontLeftAngle(newAngle(45, Math.pow((2048.0/360.0 * (150.0/7.0)), -1) * m_SteerFrontLeft.getSensorPosition()));
		setBackRightAngle(newAngle(-135, Math.pow((2048.0/360.0 * (150.0/7.0)), -1) * m_SteerBackRight.getSensorPosition()));
		setBackLeftAngle(newAngle(-45, Math.pow((2048.0/360.0 * (150.0/7.0)), -1) * m_SteerBackLeft.getSensorPosition()));
		m_DriveFrontRight.set(Math.abs(Joystick));
		m_DriveFrontLeft.set(Math.abs(Joystick));
		m_DriveBackRight.set(Math.abs(Joystick));
		m_DriveBackLeft.set(Math.abs(Joystick));
	}
	if(Joystick < 0){
		setFrontRightAngle(newAngle(-45, Math.pow((2048.0/360.0 * (150.0/7.0)), -1) * m_SteerFrontRight.getSensorPosition()));
		setFrontLeftAngle(newAngle(-135, Math.pow((2048.0/360.0 * (150.0/7.0)), -1) * m_SteerFrontLeft.getSensorPosition()));
		setBackRightAngle(newAngle(45, Math.pow((2048.0/360.0 * (150.0/7.0)), -1) * m_SteerBackRight.getSensorPosition()));
		setBackLeftAngle(newAngle(135, Math.pow((2048.0/360.0 * (150.0/7.0)), -1) * m_SteerBackLeft.getSensorPosition()));
		m_DriveFrontRight.set(Math.abs(Joystick));
		m_DriveFrontLeft.set(Math.abs(Joystick));
		m_DriveBackRight.set(Math.abs(Joystick));
		m_DriveBackLeft.set(Math.abs(Joystick));
	}
}

	//Logging the encoder values (also I love Github Copilot <3)
	public void logs(){
		log("Front Right Encoder: " + getFrontRight() + " Front Left Encoder: " + getFrontLeft() + " Back Right Encoder: " + getBackRight() + " Back Left Encoder: " + getBackLeft());
	}
	public void setFrontRightEncoders(){
		m_SteerFrontRight.setPosition((int)Math.round(2048.0/360.0 * (150.0/7.0) * e_FrontRight.getAbsolutePosition()));
	}
	public void setFrontLeftEncoders(){
		m_SteerFrontLeft.setPosition((int)Math.round(2048.0/360.0 * (150.0/7.0) * e_FrontLeft.getAbsolutePosition()));
		//log("New encoder value: " + (int)Math.round(2048.0/360.0 * (150.0/7.0) * e_FrontLeft.getAbsolutePosition()) + " || Motor value: " + m_SteerFrontLeft.getSensorPosition());
		}
	public void setBackRightEncoders(){
		m_SteerBackRight.setPosition((int)Math.round(2048.0/360.0 * (150.0/7.0) * e_BackRight.getAbsolutePosition()));
	}
	public void setBackLeftEncoders(){
		m_SteerBackLeft.setPosition((int)Math.round(2048.0/360.0 * (150.0/7.0) * e_BackLeft.getAbsolutePosition()));
	}
	//To control each steering individually with a PID
	public void setFrontRightAngle(double angle){
		//log("Angle: " + getFrontRight() + " || Motor angle: " + 360.0/ 2048.0 * m_SteerFrontRight.getSensorPosition());
		m_SteerFrontRight.set(ControlMode.Position, 2048.0/360.0 * (150.0/7.0) * angle);
	}
	public void setFrontLeftAngle(double angle){
		//log("Angle: " + getFrontLeft() + " || Motor angle: " + Math.pow((2048.0/360.0 * (150.0/7.0)),-1) * m_SteerFrontLeft.getSensorPosition());
		//log("Angle: %f Motor angle: %f", getFrontLeft(), Math.pow((2048.0/360.0 * (150.0/7.0)),-1) * m_SteerFrontLeft.getSensorPosition());
		m_SteerFrontLeft.set(ControlMode.Position, 2048.0/360.0 * (150.0/7.0) * angle);
	}
	public void setBackRightAngle(double angle){
		//log("Angle: " + getBackRight() + " || Motor angle: " + m_SteerBackRight.getSensorPosition());
		m_SteerBackRight.set(ControlMode.Position, 2048.0/360.0 * (150.0/7.0) * angle);
	}
	public void setBackLeftAngle(double angle){
		//log("Angle: " + getBackLeft() + " || Motor angle: " + m_SteerBackLeft.getSensorPosition());
		m_SteerBackLeft.set(ControlMode.Position, 2048.0/360.0 * (150.0/7.0) * angle);
	}
	public void setSFR(double angle){
		setFrontRightAngle(newAngle(angle, Math.pow((2048.0/360.0 * (150.0/7.0)), -1) * m_SteerFrontRight.getSensorPosition()));
	}
	public void setSFL(double angle){
		setFrontLeftAngle(newAngle(angle, Math.pow((2048.0/360.0 * (150.0/7.0)), -1) * m_SteerFrontRight.getSensorPosition()));
	}
	public void setSBR(double angle){
		setBackRightAngle(newAngle(angle, Math.pow((2048.0/360.0 * (150.0/7.0)), -1) * m_SteerFrontRight.getSensorPosition()));
	}
	public void setSBL(double angle){
		setBackLeftAngle(newAngle(angle, Math.pow((2048.0/360.0 * (150.0/7.0)), -1) * m_SteerFrontRight.getSensorPosition()));
	}
	public void configPID(){
		//PID for turning the various steering motors. Here is a good link to a tuning website: https://www.robotsforroboticists.com/pid-control/
		m_SteerFrontRight.setP(0.2);
		m_SteerFrontRight.setI(0);
		m_SteerFrontRight.setD(0.1);
		m_SteerFrontRight.setFF(0);

		m_SteerFrontLeft.setP(0.2);
		m_SteerFrontLeft.setI(0);
		m_SteerFrontLeft.setD(0.1);
		m_SteerFrontLeft.setFF(0);
		
		m_SteerBackRight.setP(0.2);
		m_SteerBackRight.setI(0);
		m_SteerBackRight.setD(0.1);
		m_SteerBackRight.setFF(0);

		m_SteerBackLeft.setP(0.2);
		m_SteerBackLeft.setI(0);
		m_SteerBackLeft.setD(0.1);
		m_SteerBackLeft.setFF(0);

		//pid values from sds for Flacons 500: P = 0.2 I = 0.0 D = 0.1 FF = 0.0

		//IDK what those do tbh, but I like to keep them here.
		//m_SteerFrontRight.setSensorInverted(false);
		//m_SteerFrontLeft.setSensorInverted(false);
		//m_SteerBackRight.setSensorInverted(false);
		//m_SteerBackLeft.setSensorInverted(false);
	}

	//Method to get the encoder values, the encoders are in degrees from -180 to 180. To change that, we need to change the syntax and use getPosition()
	public double getFrontRight(){
		return e_FrontRight.getAbsolutePosition();
	}
	public double getFrontLeft(){
		return e_FrontLeft.getAbsolutePosition();
	}
	public double getBackRight(){
		return e_BackRight.getAbsolutePosition();
	}
	public double getBackLeft(){
		return e_BackLeft.getAbsolutePosition();
	}

	public PointDir getCurrentPosition() {
		return currentPosition;
	}

	public void resetCurrentPosition() {
		currentPosition.set(0.0, 0.0);
		prevBackLeft = 0;
		prevBackRight = 0;
		prevFrontLeft = 0;
		prevFrontRight = 0;
	}

	public void resetDriveEncoders() {
		m_DriveBackLeft.setPosition(0);
		m_DriveBackRight.setPosition(0);
		m_DriveFrontLeft.setPosition(0);
		m_DriveFrontRight.setPosition(0);
	}

	public void setCurrentWheelPositions() {
		prevBackLeft = currBackLeft;
		prevBackRight = currBackRight;
		prevFrontLeft = currFrontLeft;
		prevFrontRight = currFrontRight;

		currBackLeft = m_DriveBackLeft.getSensorPosition();
		currBackRight = m_DriveBackRight.getSensorPosition();
		currFrontLeft = m_DriveFrontLeft.getSensorPosition();
		currFrontRight = m_DriveFrontRight.getSensorPosition();
	}

	//Odometry
	@Override
	public void run() {
		if (odometryLimiter.next()) {
			setCurrentWheelPositions();
			avgX = ((currBackLeft - prevBackLeft) * Math.cos(Math.toRadians(getBackLeft() + gyroValue)) + (currBackRight - prevBackRight) * Math.cos(Math.toRadians(getBackRight() + gyroValue)) + (currFrontLeft - prevFrontLeft) * Math.cos(Math.toRadians(getFrontLeft() + gyroValue)) + (currFrontRight - prevFrontRight) * Math.cos(Math.toRadians(getFrontRight() + gyroValue))) * WHEEL_DISTANCE / (4 * GEAR_RATIO * ENCODER_TO_REVOLUTION_CONSTANT);
			avgY = ((currBackLeft - prevBackLeft) * Math.sin(Math.toRadians(getBackLeft() + gyroValue)) + (currBackRight - prevBackRight) * Math.sin(Math.toRadians(getBackRight() + gyroValue)) + (currFrontLeft - prevFrontLeft) * Math.sin(Math.toRadians(getFrontLeft() + gyroValue)) + (currFrontRight - prevFrontRight) * Math.sin(Math.toRadians(getFrontRight() + gyroValue))) * WHEEL_DISTANCE / (4 * GEAR_RATIO * ENCODER_TO_REVOLUTION_CONSTANT);
			currentPosition.set(currentPosition.getX() + avgX, currentPosition.getY() + avgY, gyroValue);
			log("Current Position: " + currentPosition.toString() + " " + getBackLeft() + " " + getBackRight() + " " + getFrontLeft() + " " + getFrontRight());
		}
	}
}

//AS