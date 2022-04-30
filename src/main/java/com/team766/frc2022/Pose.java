package com.team766.frc2022;

public class Pose {
	private double x = 0;
	private double y = 0;
	private double angle = 0;
	public Pose(){}
	public Pose(double x, double y){
		this.x = x;
		this.y = y;
	}
	public Pose(double x, double y, double angle){
		this.x = x;
		this.y = y;
		this.angle = angle%180;
	}
	public double getX(){
		return x;
	}
	public double getY(){
		return y;
	}
	public double getAngle(){
		return angle;
	}
	public void setX(double x){
		this.x = x;
	}
	public void setY(double y){
		this.y = y;
	}
	public void setAngle(double angle){
		this.angle = angle%180;
	}
	public double distanceFromCurrent(double CurrentX, double CurrentY){
		return Math.sqrt(Math.pow((x-CurrentX),2)+Math.pow((y-CurrentY),2));
	}
	public double getTheta(double oldX, double oldY){
		return(Math.atan2((x - oldX), (y - oldY)));
	} 
	private double maxPercent = 1; //0 to 1
	public double bestVx(double oldX, double oldY){
		return(maxPercent/(Math.cos(getTheta(oldX, oldY))));
	}
	public double bestVy(double oldX, double oldY){
		return(maxPercent/(Math.sin(getTheta(oldX, oldY))));
	}

}
