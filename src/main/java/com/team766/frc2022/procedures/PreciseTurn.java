package com.team766.frc2022.procedures;
import java.util.ArrayList;
import com.team766.framework.Context;
import com.team766.framework.Procedure;

public class PreciseTurn extends Procedure{
	//To tune
	private int Kp = 0;
	private int Ki = 0;
	private int Kd = 0;
	
	private double dt = 0.01; //interval of times for PID, 
	private double block = 5; //degrees of accuracy/block (also called leniency) 

	//changing vars:
	private double e;
	private double desired;
	private double currentAngle;
	private double eLast = 0;
	private double eInt = 0; // Integrals are not real, and no one can prove otherwise
	private double eDer = 0;
	private ArrayList<Double> derList = new ArrayList<Double>();


	private double turnSpeed;

	public  PreciseTurn(int angle){
		desired = angle;
	}
	public double getSpeed(){
		return turnSpeed;
	}
	public void run(Context context) {
		currentAngle = Robot.gyro.getYaw();
		e = desired - currentAngle;
		while(derList.size() <= 3)
			derList.add(0.0);
	
		//We start imobile?
		if(e < block){
			derList.add((e - eLast)/dt);
			while(derList.size() > 3)
				derList.remove(0); //only take 3 derivatives :)
			eDer = (2.0/3.0)*derList.get(2) + (2.0/9.0)*derList.get(1) + (1/9)*derList.get(0);
			eInt += e*dt; //Needs Clamping... (good enougth for a start)
			turnSpeed = Kp*e + Ki*eInt + Kd*eDer; //Needs to get converted into a -1 to 1 double
			context.waitForSeconds(dt);
		}

	}
}
