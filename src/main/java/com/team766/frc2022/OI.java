package com.team766.frc2022;

import com.team766.framework.Procedure;
import com.ShooterVelociltyUtil;
import com.team766.config.ConfigFileReader;
import com.team766.framework.Context;
import com.team766.frc2022.Robot;
import com.team766.frc2022.mechanisms.Elevator;
import com.team766.frc2022.mechanisms.Limelight;
import com.team766.frc2022.procedures.*;
import com.team766.hal.JoystickReader;
import com.team766.hal.RobotProvider;
import com.team766.logging.Category;
// 1 min 18s to fill air
import com.team766.web.dashboard.StatusLight;

// TODO: extract joysticks, buttons into constants class or config file

/**
 * This class is the glue that binds the controls on the physical operator
 * interface to the code that allow control of the robot.
 */
public class OI extends Procedure {
	private JoystickReader m_leftJoystick;
	private JoystickReader m_rightJoystick;
	private JoystickReader m_ControlPanel;
	private boolean b = true;
	private StatusLight light;

	public OI() {
		loggerCategory = Category.OPERATOR_INTERFACE;

		m_leftJoystick = RobotProvider.instance.getJoystick(InputConstants.LEFT_JOYSTICK);
		m_rightJoystick = RobotProvider.instance.getJoystick(InputConstants.RIGHT_JOYSTICK);
		m_ControlPanel = RobotProvider.instance.getJoystick(InputConstants.CONTROL_PANEL);
		light = new StatusLight("Ready to fire");
		light.setColor("Maroon");
	}

	public void run(Context context) {
		int index = 0; // index counter for the intake
		double autopower = 0; // power given during auto shooting
		boolean startShoot = false; //is it staring to shoot
		double prev_time = RobotProvider.instance.getClock().getTime();

		context.takeOwnership(Robot.drive);
		context.takeOwnership(Robot.shooter);

		Robot.shooter.setPIDValues();

		//LaunchedContext climbingContext = null;
		while (true) {
			// TODO: tweak all of this based on actual revB controls
			Robot.drive.setArcadeDrivePower(
				-m_leftJoystick.getAxis(InputConstants.AXIS_FORWARD_BACKWARD)*12, 
				m_rightJoystick.getAxis(InputConstants.AXIS_LEFT_RIGHT)*12,true);

			double cur_time = RobotProvider.instance.getClock().getTime();
			// if (cur_time-prev_time >= 0.5){
			// 	/* log("Elevator: " + Robot.elevator.getElevatorPosition() + " Arms: " + Robot.elevator.getArmsPower());
			// 	 log("Pitch: " + Robot.gyro.getGyroPitch() + " Yaw: " + Robot.gyro.getGyroYaw() + " Roll: " + Robot.gyro.getGyroRoll());
			// 	 log("Top: " + Robot.elevator.getLimitSwitchTop() + " Bottom: " + Robot.elevator.getLimitSwitchBottom());
			// 	 log("Top switch: " + Boolean.toString(Robot.elevator.getLimitSwitchTop()) + " Bottom switch:" + Boolean.toString(Robot.elevator.getLimitSwitchBottom()));*/
			// 	log("Velocity: "+Robot.shooter.getVelocity());
			// 	//log("Distance: "+Robot.limelight.distanceFromTarget());
			// 	prev_time = cur_time;
			// }
		
			log("Distance: "+Robot.limelight.distanceFromTarget());
			log("Velocity: "+Robot.shooter.getVelocity());

			if (m_ControlPanel.getButton(InputConstants.CONTROL_PANEL_ELEVATOR_UP_BUTTON)) {
				context.takeOwnership(Robot.elevator);
				Robot.elevator.setElevatorPower(1);
				context.releaseOwnership(Robot.elevator);
				log("UP");
			} else if (m_ControlPanel.getButtonReleased(InputConstants.CONTROL_PANEL_ELEVATOR_UP_BUTTON)) {
				context.takeOwnership(Robot.elevator);
				Robot.elevator.setElevatorPower(0);
				context.releaseOwnership(Robot.elevator);
				log("UP Stop");
			} 

			if (m_ControlPanel.getButton(InputConstants.CONTROL_PANEL_ELEVATOR_DOWN_BUTTON)) {
				context.takeOwnership(Robot.elevator);
				Robot.elevator.setElevatorPower(-1);
				context.releaseOwnership(Robot.elevator);
				log("Down");
			} else if (m_ControlPanel.getButtonReleased(InputConstants.CONTROL_PANEL_ELEVATOR_DOWN_BUTTON)) {
				context.takeOwnership(Robot.elevator);
				Robot.elevator.setElevatorPower(0);
				context.releaseOwnership(Robot.elevator);
				log("down stop");
			} 

			// if (m_ControlPanel.getButtonPressed(InputConstants.CONTROL_PANEL_ELEVATOR_TOP_BUTTON)) {				
			// 	context.startAsync(new ExtendElevator());
			// 	log("Elevator to Top");
			// }
			// if (m_ControlPanel.getButtonPressed(InputConstants.CONTROL_PANEL_ELEVATOR_BOTTOM_BUTTON)) {
			// 	context.startAsync(new RetractElevator());
			// 	log("Elevator to Bottom");	
			// }

			//1 pushes the arms forward, -1 pushes the arms backwards
			
			if (m_ControlPanel.getButtonPressed(InputConstants.CONTROL_PANEL_ARMS_SWITCH)) {
				context.takeOwnership(Robot.elevator);				
				Robot.elevator.setArmsPower(1);
				context.releaseOwnership(Robot.elevator);
			} else if (m_ControlPanel.getButtonReleased(InputConstants.CONTROL_PANEL_ARMS_SWITCH)) {
				context.takeOwnership(Robot.elevator);				
				Robot.elevator.setArmsPower(-1);
				context.releaseOwnership(Robot.elevator);
			}

			// if(m_ControlPanel.getButtonPressed(InputConstants.CONTROL_PANEL_AUTOCLIMB_BUTTON)){
			// 	context.startAsync(new Climb());
			// }

			if (m_leftJoystick.getButtonPressed(InputConstants.JOYSTICK_ELEVATOR_RESET_BUTTON)){
				//context.takeOwnership(Robot.shooter);
				log("Activated");
				context.startAsync(new ResetElevator());
			}

			// if(m_leftJoystick.getButtonPressed(InputConstants.JOYSTICK_CLIMB_RUNG_BUTTON)){
			// 	context.startAsync(new ClimbRung());
			// }

			// if(m_leftJoystick.getButtonPressed(InputConstants.JOYSTICK_CLIMB_FIRST_RUNG_BUTTON)){
			// 	context.startAsync(new ClimbFirstRung());
			// }
			// 
			//log(""+Robot.elevator.getElevatorPosition());

			double dialPower = m_ControlPanel.getAxis(InputConstants.AXIS_SHOOTER_DIAL);
			if (m_ControlPanel.getButton(InputConstants.CONTROL_PANEL_SHOOTER_SWITCH)){
				log("Manual shooting starting.");
				double configPower = ConfigFileReader.getInstance().getDouble("shooter.velocity").get();
				double power = ((dialPower - 0.6456)*3.734)*configPower;
				Robot.shooter.setVelocity(power);
				log("Power set to:" + power);
				light.setColor("Maroon"); //color when we aren't ready to shoot
				startShoot = false;
			} 

			//log(""+m_ControlPanel.getAxis(InputConstants.AXIS_SHOOTER_DIAL));
			if (m_ControlPanel.getButtonPressed(InputConstants.CONTROL_PANEL_INTAKE_BUTTON)) {
				if (index % 2 == 0){
					new StartIntake().run(context);
				}else{
					new StopIntake().run(context);
				}
				index++;

			} 

			/*if (m_joystick0.getButtonPressed(5)) {
				context.takeOwnership(Robot.gyro);
				Robot.gyro.resetGyro();
				context.releaseOwnership(Robot.gyro);
			}*/

/*
			if (m_ControlPanel.getButtonPressed(InputConstants.CONTROL_PANEL_ELEVATOR_UP_BUTTON)) {
				context.startAsync(new ExtendElevator());
			}
			if (m_ControlPanel.getButtonPressed(InputConstants.CONTROL_PANEL_ELEVATOR_DOWN_BUTTON)) {
				context.startAsync(new RetractElevator());
			}
*/
			if (m_rightJoystick.getButtonPressed(InputConstants.JOYSTICK_TRIGGER)) {
				new StartBelts().run(context);
			} else if (m_rightJoystick.getButtonReleased(InputConstants.JOYSTICK_TRIGGER)){
				new StopBelts().run(context);
			}
			
			if (m_ControlPanel.getButtonPressed(InputConstants.CONTROL_PANEL_SPITBALL_BUTTON)){
				new SpitBall().run(context);
			} else if (m_ControlPanel.getButtonReleased(InputConstants.CONTROL_PANEL_SPITBALL_BUTTON)) {
				new StopBelts().run(context);
				new StopIntake().run(context);
				new StopArms().run(context);
			}

			if (m_ControlPanel.getButtonPressed(InputConstants.CONTROL_PANEL_AUTO_SHOOT) && m_ControlPanel.getButton(InputConstants.CONTROL_PANEL_SHOOTER_SWITCH) == false) {
				double distance = Robot.limelight.limelightFilter(context);
				log("Autoshooting starting.");
				log("Calculated distance");
				if (distance >= 2.5 && distance <= 5){
					//context.takeOwnership(Robot.shooter);
					log("Auto Shooting");
					autopower = ShooterVelociltyUtil.computeVelocityForDistance(distance);
					Robot.shooter.setVelocity(autopower);
					if(autopower == 0.0) {
						log("out of range");
					} else {
						log("Velocity set to:" + autopower);
						startShoot = true;
					}
				}
			} 

			if (startShoot){
				if (Math.abs(Robot.shooter.getVelocity()-autopower) <= 30.0){
					startShoot = false;
					light.setColor("LimeGreen"); //color when we're ready to shoot
				}
			}
			// else if (m_leftJoystick.getButtonReleased(InputConstants.CONTROL_PANEL_AUTO_SHOOT)){
			// 	Robot.shooter.stopShoot();
			// }

			// if (m_leftJoystick.getButtonPressed(1)){
			// 	Robot.shooter.basicShoot();
			// } else if (m_leftJoystick.getButtonReleased(1)){
			// 	Robot.shooter.stopShoot();
			// }

			// log("Velocity: "+Robot.shooter.getVelocity());
			// log("Distance: "+Robot.limelight.distanceFromTarget());
			
			/* if (m_leftJoystick.getButtonPressed(3)) {
				context.startAsync(new activateShooter());
			} else if (m_leftJoystick.getButtonReleased(3)) {
				context.startAsync(new StopShooter());
			} */

			context.waitFor(() -> RobotProvider.instance.hasNewDriverStationData());
		}
	}
}
