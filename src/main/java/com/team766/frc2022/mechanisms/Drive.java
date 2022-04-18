/**
 * TODO:
 * for some reason the sds team hasn't added the mk4iswervemodule helper to their library, make sure to update this code when they do
 * the motor definitions are also wrong, fix where indicated in the drive constructor
 */


package com.team766.frc2022.mechanisms;

import com.team766.framework.Mechanism;
import com.team766.hal.RobotProvider;
import com.team766.hal.CANSpeedController;
import com.team766.library.ValueProvider;
import com.team766.logging.Category;
import com.team766.config.ConfigFileReader;
import com.team766.controllers.PIDController;
//import com.ctre.phoenix.motorcontrol.can.TalonSRX;


import com.swervedrivespecialties.swervelib.Mk4SwerveModuleHelper;
import com.swervedrivespecialties.swervelib.SdsModuleConfigurations;
import com.swervedrivespecialties.swervelib.SwerveModule;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import com.kauailabs.navx.frc.*;

import static frc.robot.Constants.*;

public class Drive extends Mechanism {


 /**
   * The maximum voltage that will be delivered to the drive motors.
   * <p>
   * This can be reduced to cap the robot's maximum speed. Typically, this is useful during initial testing of the robot.
   */
  public static final double MAX_VOLTAGE = 12.0;
  // FIXME Measure the drivetrain's maximum velocity or calculate the theoretical.
  //  The formula for calculating the theoretical maximum velocity is:
  //   <Motor free speed RPM> / 60 * <Drive reduction> * <Wheel diameter meters> * pi
  //  By default this value is setup for a Mk3 standard module using Falcon500s to drive.
  //  An example of this constant for a Mk4 L2 module with NEOs to drive is:
  //   5880.0 / 60.0 / SdsModuleConfigurations.MK4_L2.getDriveReduction() * SdsModuleConfigurations.MK4_L2.getWheelDiameter() * Math.PI
  /**
   * The maximum velocity of the robot in meters per second.
   * <p>
   * This is a measure of how fast the robot should be able to drive in a straight line.
   */
  public static final double MAX_VELOCITY_METERS_PER_SECOND = 6380.0 / 60.0 *
          SdsModuleConfigurations.MK4_L2.getDriveReduction() *
          SdsModuleConfigurations.MK4_L2.getWheelDiameter() * Math.PI;
  /**
   * The maximum angular velocity of the robot in radians per second.
   * <p>
   * This is a measure of how fast the robot can rotate in place.
   */
  // Here we calculate the theoretical maximum angular velocity. You can also replace this with a measured amount.
  public static final double MAX_ANGULAR_VELOCITY_RADIANS_PER_SECOND = MAX_VELOCITY_METERS_PER_SECOND /
          Math.hypot(DRIVETRAIN_TRACKWIDTH_METERS / 2.0, DRIVETRAIN_WHEELBASE_METERS / 2.0);

  private final SwerveDriveKinematics m_kinematics = new SwerveDriveKinematics(
          // Front left
          new Translation2d(DRIVETRAIN_TRACKWIDTH_METERS / 2.0, DRIVETRAIN_WHEELBASE_METERS / 2.0),
          // Front right
          new Translation2d(DRIVETRAIN_TRACKWIDTH_METERS / 2.0, -DRIVETRAIN_WHEELBASE_METERS / 2.0),
          // Back left
          new Translation2d(-DRIVETRAIN_TRACKWIDTH_METERS / 2.0, DRIVETRAIN_WHEELBASE_METERS / 2.0),
          // Back right
          new Translation2d(-DRIVETRAIN_TRACKWIDTH_METERS / 2.0, -DRIVETRAIN_WHEELBASE_METERS / 2.0)
  );

  private Gyro m_navx;
  private CANSpeedController m_fL;
  private CANSpeedController m_fR;
  private CANSpeedController m_bL;
  private CANSpeedController m_bR;

	// Values for PID turning
	private PIDController controller;
	public double P_turn = ConfigFileReader.getInstance().getDouble("drive.turn.P").valueOr(0.0);
	public double I_turn = ConfigFileReader.getInstance().getDouble("drive.turn.I").valueOr(0.0);
	public double D_turn = ConfigFileReader.getInstance().getDouble("drive.turn.D").valueOr(0.0);
	public double threshold_turn = ConfigFileReader.getInstance().getDouble("drive.turn.thereshold").valueOr(0.0);
	public double minpower_turn = ConfigFileReader.getInstance().getDouble("drive.turn.minpower").valueOr(0.0);
	public double min_turn = -12;
	public double max_turn = 12;

  // These are our modules. We initialize them in the constructor.
  private final SwerveModule m_frontLeftModule;
  private final SwerveModule m_frontRightModule;
  private final SwerveModule m_backLeftModule;
  private final SwerveModule m_backRightModule;

  private ChassisSpeeds m_chassisSpeeds = new ChassisSpeeds(0.0, 0.0, 0.0);

  public Drive() {
    loggerCategory = Category.DRIVE;
    // There are 4 methods you can call to create your swerve modules.
    // The method you use depends on what motors you are using.
    //
    // Mk3SwerveModuleHelper.createFalcon500(...)
    //   Your module has two Falcon 500s on it. One for steering and one for driving.
    //
    // Mk3SwerveModuleHelper.createNeo(...)
    //   Your module has two NEOs on it. One for steering and one for driving.
    //
    // Mk3SwerveModuleHelper.createFalcon500Neo(...)
    //   Your module has a Falcon 500 and a NEO on it. The Falcon 500 is for driving and the NEO is for steering.
    //
    // Mk3SwerveModuleHelper.createNeoFalcon500(...)
    //   Your module has a NEO and a Falcon 500 on it. The NEO is for driving and the Falcon 500 is for steering.
    //
    // Similar helpers also exist for Mk4 modules using the Mk4SwerveModuleHelper class.

    // By default we will use Falcon 500s in standard configuration. But if you use a different configuration or motors
    // you MUST change it. If you do not, your code will crash on startup.
    // FIXME Setup motor configuration
    m_frontLeftModule = Mk4SwerveModuleHelper.createFalcon500(
            // This can either be STANDARD or FAST depending on your gear configuration
            Mk4SwerveModuleHelper.GearRatio.L2,
            // This is the ID of the drive motor
            FRONT_LEFT_MODULE_DRIVE_MOTOR,
            // This is the ID of the steer motor
            FRONT_LEFT_MODULE_STEER_MOTOR,
            // This is the ID of the steer encoder
            FRONT_LEFT_MODULE_STEER_ENCODER,
            // This is how much the steer encoder is offset from true zero (In our case, zero is facing straight forward)
            FRONT_LEFT_MODULE_STEER_OFFSET
    );

    // We will do the same for the other modules
    m_frontRightModule = Mk4SwerveModuleHelper.createFalcon500(
            Mk4SwerveModuleHelper.GearRatio.L2,
            FRONT_RIGHT_MODULE_DRIVE_MOTOR,
            FRONT_RIGHT_MODULE_STEER_MOTOR,
            FRONT_RIGHT_MODULE_STEER_ENCODER,
            FRONT_RIGHT_MODULE_STEER_OFFSET
    );

    m_backLeftModule = Mk4SwerveModuleHelper.createFalcon500(
            Mk4SwerveModuleHelper.GearRatio.L2,
            BACK_LEFT_MODULE_DRIVE_MOTOR,
            BACK_LEFT_MODULE_STEER_MOTOR,
            BACK_LEFT_MODULE_STEER_ENCODER,
            BACK_LEFT_MODULE_STEER_OFFSET
    );

    m_backRightModule = Mk4SwerveModuleHelper.createFalcon500(
            Mk4SwerveModuleHelper.GearRatio.L2,
            BACK_RIGHT_MODULE_DRIVE_MOTOR,
            BACK_RIGHT_MODULE_STEER_MOTOR,
            BACK_RIGHT_MODULE_STEER_ENCODER,
            BACK_RIGHT_MODULE_STEER_OFFSET
    );
    //TODO: These values are placeholders and need to be changed, they are intentionally bad. The naming convention for the variables is m_(front or back abbreviated to one letter)(Right or left motor abbreviated to one letter)
    m_fL = RobotProvider.instance.getCANMotor("drive.leftTalon1");
    m_fR = RobotProvider.instance.getCANMotor("drive.leftTalon2");
    m_bL = RobotProvider.instance.getCANMotor("drive.leftTalon3");
    m_bR = RobotProvider.instance.getCANMotor("drive.rightTalon1");
    m_navx = new Gyro();

  }

  /**
   * Sets the gyroscope angle to zero. This can be used to set the direction the robot is currently facing to the
   * 'forwards' direction.
   */
  public void zeroGyroscope() {
    m_navx.zeroYaw();
  }

  public Rotation2d getGyroscopeRotation() {
    checkContextOwnership();
    if (m_navx.isMagnetometerCalibrated()) {
      // We will only get valid fused headings if the magnetometer is calibrated
      return Rotation2d.fromDegrees(m_navx.getFusedHeading());
    }

    // We have to invert the angle of the NavX so that rotating the robot counter-clockwise makes the angle increase.
    return Rotation2d.fromDegrees(360.0 - m_navx.getYaw());
  }

  public void setSwerve(ChassisSpeeds chassisSpeeds) {
    checkContextOwnership();
    m_chassisSpeeds = chassisSpeeds;
    SwerveModuleState[] states = m_kinematics.toSwerveModuleStates(m_chassisSpeeds);
    SwerveDriveKinematics.desaturateWheelSpeeds(states, MAX_VELOCITY_METERS_PER_SECOND);

    m_frontLeftModule.set(states[0].speedMetersPerSecond / MAX_VELOCITY_METERS_PER_SECOND * MAX_VOLTAGE, states[0].angle.getRadians());
    m_frontRightModule.set(states[1].speedMetersPerSecond / MAX_VELOCITY_METERS_PER_SECOND * MAX_VOLTAGE, states[1].angle.getRadians());
    m_backLeftModule.set(states[2].speedMetersPerSecond / MAX_VELOCITY_METERS_PER_SECOND * MAX_VOLTAGE, states[2].angle.getRadians());
    m_backRightModule.set(states[3].speedMetersPerSecond / MAX_VELOCITY_METERS_PER_SECOND * MAX_VOLTAGE, states[3].angle.getRadians());
  }

	/**
	 * Uses PID to precisely turn the swerve modules
	 */
  public void preciseTurn(Rotation2d angle_rad){
      Rotation2d initial_angle = Rotation2d.fromDegrees(m_navx.getYaw());
      controller = new PIDController(P_turn, I_turn, D_turn, min_turn, max_turn, threshold_turn);
      controller.setSetpoint(initial_angle.add(angle_rad));
      while(!controller.isDone()){
				controller.calculate(m_navx.getYaw(), true);
				double turn = controller.getOutput();
				if (Math.abs(turn) < minpower_turn){
					if (turn>0){
            turn = minpower_turn;
          } else {
              turn = -minpower_turn;
          }  
        }
				setSwerve(new ChassisSpeeds(0, 0, turn));
      }

  }

	//Turn the individual swerve modules (not the robot!!!) to a given angle relative to their zero.
	public void swerveWheelTurn(Rotation2d angle){
    m_frontLeftModule.set(0, angle.getRadians());
    m_frontRightModule.set(0, angle.getRadians());
    m_backLeftModule.set(0, angle.getRadians());
    m_backRightModule.set(0, angle.getRadians());	
	}
  /**
   * Approximates the torque applied by a motor, https://things-in-motion.blogspot.com/2018/12/how-to-estimate-torque-of-bldc-pmsm.html
   * It uses a Kv constant for a Neo motor because Rev/Ctr didn't publish the specs for the falcon500
   * Since the Talon motor controler can't measure stator current and is just measuring input current, this number becomes less accurate
   * as the stator energization duty cycle drops (bldc motors are controlled with pwm and as the duty cycle goes down from %100 this current value gets more wrong)
   * I tried to correct this by trying to get a solid value for the duty cycle but this more of a relative measurment.
   * The function returns the torque applied by the motor in units of N/m
   */
  public double getTorque(CANSpeedController motor){
    return 8.3*motor.getOutputCurrent()/473; //using Kv for a Neo motor which should be roughly similar to falcon 500, 8.3*I/kV
  }

  public double getPWM(CANSpeedController motor){
    return motor.getMotorOutputPercent();
  }
  //returns motor power in watts, useful for dissipation, more accurate limiting, etc.
  public double getOutputPower(CANSpeedController motor){
    return motor.getMotorOutputVoltage()*motor.getOutputCurrent();
  }
  //returns the direction of the robot with the most net force (torque*wheel_r*Math.relevantcomponent(wheel angle)) but since we don't care about magnitude I can leave out wheel radius since Torque is proportional to force when every wheel has the same radius
  public Rotation2d netForceDirection(){
    Rotation2d netForce = new Rotation2d(getTorque(m_fL)*Math.cos(m_frontLeftModule.getSteerAngle())+getTorque(m_fR)*Math.cos(m_frontRightModule.getSteerAngle())+getTorque(m_bL)*Math.cos(m_backLeftModule.getSteerAngle())+getTorque(m_bR)*Math.cos(m_backRightModule.getSteerAngle()),
                                         getTorque(m_fL)*Math.sin(m_frontLeftModule.getSteerAngle())+getTorque(m_fR)*Math.sin(m_frontRightModule.getSteerAngle())+getTorque(m_bL)*Math.sin(m_backLeftModule.getSteerAngle())+getTorque(m_bR)*Math.sin(m_backRightModule.getSteerAngle())
    );
    return netForce;
  }
}


