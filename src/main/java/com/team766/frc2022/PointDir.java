package com.team766.frc2022;

import com.team766.logging.Category;
import java.lang.Math;

public class PointDir extends Point{
	private double H;

	public PointDir(double x, double y, double H){
		super(x, y);
	}
	public boolean hasAngle(){
		return true;
	}
	public double getH(){
		return H;
	}
	public void setH(double H){
		this.H = H;
	}

	public void set(double x, double y, double H){
		setH(H);
		super.set(x,y);
	}
	public String toString() {
		return super.toString() + " Heading: " + getH();
	}
}