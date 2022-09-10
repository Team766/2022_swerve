package com.team766.frc2022.procedures;

import com.team766.framework.Context;
import com.team766.framework.Procedure;
import com.team766.frc2022.Point;
import com.team766.logging.Category;

public class FollowPointsH extends Procedure {

	public FollowPointsH(){
		loggerCategory = Category.AUTONOMOUS;
	}

	public void run(Context context) {
		log("Starting FollowPointsH");
		Point[] pointList = {
			new Point(0, 0), 
			new Point(100, 0),
			new Point(100, -100)
		};
		new FollowPoints(pointList).run(context);
	}
	
}