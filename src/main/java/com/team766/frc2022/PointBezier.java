package com.team766.frc2022;

import com.team766.logging.Category;
import java.lang.Math;

public class PointBezier {
	private double Θ;
	private double x;
	private double y;
	private boolean endpoint = false;

	public PointBezier(double x, double y, double Θ){
		endpoint = true;
		this.x = x;
		this.y = y;
		this.Θ = Θ;
	}
	public PointBezier(double x, double y){
		this.x = x;
		this.y = y;
	}

	public double getX(){
		return x;
	}
	public double getY(){
		return x;
	}
	public double getΘ(){
		return Θ;
	}
	public boolean getEnd(){
		return endpoint;
	}
	public String toString() {
		return "";
	}
}