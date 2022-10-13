package com.team766.frc2022;

import com.team766.framework.AutonomousProcedure;
import com.team766.frc2022.procedures.*;

public enum AutonomousModes {
	//@AutonomousProcedure(procedureClass = AutonomousMode.class) AutonomousMode,
	@AutonomousProcedure(procedureClass = FollowPoints.class) FollowPoints,
	//@AutonomousProcedure(procedureClass = FollowPointsH.class) FollowPointsH,
	//@AutonomousProcedure(procedureClass = Back.class) Back,
	@AutonomousProcedure(procedureClass = DoNothing.class) DoNothing
}
