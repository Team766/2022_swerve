package com.team766.frc2022.procedures;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.frc2022.Robot;

public class ExtendElevator extends Procedure{
	//bot = -1500
	//top = -17000

	public void run(Context context){
		context.takeOwnership(Robot.elevator);

		Robot.elevator.setElevatorPower(0.5);
		context.waitFor(() -> Robot.elevator.getElevatorPosition() <= -16500);
		Robot.elevator.setElevatorPower(0.0);

/*
		try{
			Robot.climber.setElevatorMotor(1.0);
			context.waitFor(() -> Robot.climber.getEncoderDistance() >= 620);
			Robot.climber.setElevatorMotor(0.0);

			while(true){
				if(Robot.climber.getEncoderDistance() < 630){
					Robot.climber.setElevatorMotor(0.4);
					context.waitFor(() -> Robot.climber.getEncoderDistance() >= 630);
					Robot.climber.setElevatorMotor(0.0);
				}
				context.yield();
			}
		}finally{
			Robot.climber.setElevatorMotor(0.0);
		}
*/
	}
}