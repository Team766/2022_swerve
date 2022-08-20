package com.team766.frc2022;

import com.team766.logging.Category;
import java.lang.Math;

public class Point {
	private double x;
	private double y;

	public Point(double x, double y){
		this.x = x;
		this.y = y;
	}
	public void setX(double x){
		this.x = x;
	}
	public void setY(double y){
		this.y = y;
	}
	public void set(double x, double y){
		setX(x);
		setY(y);
	}
	public boolean hasAngle(){
		return false;
	}

	public double getX(){
		return x;
	}
	public double getY(){
		return x;
	}

	public String toString() {
		return "X: " + getX() + " Y: " + getY();
	}

}