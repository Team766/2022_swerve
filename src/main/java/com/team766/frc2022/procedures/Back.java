package com.team766.frc2022.procedures;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.frc2022.Robot;
import com.team766.logging.Category;

public class Back extends Procedure {

	public void run(Context context) {
		context.takeOwnership(Robot.drive);
		loggerCategory = Category.PROCEDURES;

		Robot.drive.setGyro(0);
		Robot.drive.swerveDrive(-1,0,0);
		context.waitForSeconds(5.0);
		Robot.drive.swerveDrive(0,0,0);
		context.releaseOwnership(Robot.drive);
	}
}