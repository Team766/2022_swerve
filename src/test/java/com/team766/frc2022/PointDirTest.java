package com.team766.frc2022;

import static org.junit.Assert.*;

import org.junit.Test;
import com.team766.frc2022.PointDir;
import com.team766.frc2022.Point;

public class PointDirTest {

	@Test
	public void test() {
		final double EPSILON = 0.0001;
		PointDir currentPoint = new PointDir(0, 0, 0);
		Point targetPoint = new Point(0, 10);
		assertEquals(currentPoint.getX(), 0, EPSILON);
		assertEquals(currentPoint.getAngleDifference(targetPoint), -0.5, EPSILON);
		currentPoint.set(1, 5, 0);
		targetPoint.set(1, 15);
		assertEquals(currentPoint.getAngleDifference(targetPoint), -0.5, EPSILON);
		currentPoint.set(1, 5, 0);
		targetPoint.set(1, -15);
		assertEquals(currentPoint.getAngleDifference(targetPoint), 0.5, EPSILON);
		currentPoint.set(1, 5, 90);
		targetPoint.set(1, -15);
		assertEquals(currentPoint.getAngleDifference(targetPoint), -1, EPSILON);
	}
}
