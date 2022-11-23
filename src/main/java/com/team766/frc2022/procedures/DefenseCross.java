package com.team766.frc2022.procedures;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.frc2022.Robot;
import com.team766.logging.Category;

/* This procedure sets a defensive wheel orientation.
The orientation reduces the load on our drive motors by 1.4x */

public class DefenseCross extends Procedure {

	public void run(Context context) {
		context.takeOwnership(Robot.drive);
		loggerCategory = Category.PROCEDURES;

		Robot.drive.setSFR(45);
		Robot.drive.setSFL(-45);
		Robot.drive.setSBR(-45);
		Robot.drive.setSBL(45);
		Robot.drive.stopDriveMotors();
		
		context.releaseOwnership(Robot.drive);
	}
}