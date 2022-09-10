package com.team766.frc2022;

import com.team766.logging.Category;
import java.lang.Math;
import com.team766.frc2022.Point;

//Currently unused

public class Polygon {
	private Point[] pointList;
	private int numOfSides;

	public Polygon(Point[] pointList) {
		this.pointList = pointList;
		numOfSides = pointList.length;
	}

	public double getArea() {
		double area = 0;
		for (int i = 0; i < numOfSides; i++) {
			area += ((pointList[i].getY() + pointList[(i + 1) % numOfSides].getY()) * (pointList[i].getX() - pointList[(i + 1) % numOfSides].getX()));
		}
		area /= 2;
		return Math.abs(area);
	}

	public Point[] getPoints() {
		return pointList;
	}

	public Point getPoint(int i) {
		return pointList[i];
	}

	public int getNumOfSides() {
		return numOfSides;
	}
}