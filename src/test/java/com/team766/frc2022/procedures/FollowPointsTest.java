package com.team766.frc2022.procedures;
import static org.junit.Assert.*;

import org.junit.Test;
import com.team766.frc2022.procedures.FollowPoints;

import com.team766.frc2022.PointDir;
import com.team766.frc2022.Point;

public class FollowPointsTest {
	@Test

	public void test() {
		final double EPSILON = 0.0001;
		Point[] points = {new Point(0, 0), new Point(1, 1)};
		PointDir currentPos = new PointDir(0, 0, 0);
		assertTrue(FollowPoints.checkIntersection(0, currentPos, points, 1));
		currentPos.set(-1, -1, 0);
		assertTrue(FollowPoints.checkIntersection(0, currentPos, points, 1));
		currentPos.set(-1.5, -1, 0);
		assertTrue(FollowPoints.checkIntersection(0, currentPos, points, 1));
		assertFalse(FollowPoints.checkIntersection(0, currentPos, points, 0.1));

		Point[] pointList = {new Point(0, 0), new Point(1, 1), new Point(2, 0), new Point(2, -2), new Point(0, 0)};
		currentPos.set(5, 5, 0);
		assertEquals(FollowPoints.selectTargetPoint(0, currentPos, pointList, 1).distance(new Point(0, 0)), 0.0, EPSILON);
		currentPos.set(0, 0, 0);
		assertEquals(FollowPoints.selectTargetPoint(0, currentPos, pointList, 1).distance(new Point(0, 0)), 0.0, EPSILON);
		assertEquals(FollowPoints.selectTargetPoint(2, currentPos, pointList, 1).distance(new Point(2, 0)), 0.0, EPSILON);
		currentPos.set(0.75, 0, 0);
		assertEquals(FollowPoints.selectTargetPoint(2, currentPos, pointList, 1).distance(new Point(1.70571891388, 0.294281086117)), 0.0, EPSILON);
		currentPos.set(1.5, 0, 0);
		System.out.println(FollowPoints.selectTargetPoint(3, currentPos, pointList, 1).toString());
		assertEquals(FollowPoints.selectTargetPoint(3, currentPos, pointList, 1).distance(new Point(2.00086552512, -0.865525115609)), 0.0, EPSILON);
	}
}