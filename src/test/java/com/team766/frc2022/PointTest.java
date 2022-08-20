package com.team766.frc2022;

import static org.junit.Assert.*;
import org.junit.Test;
import com.team766.frc2022.Point;

public class PointTest {

	@Test
	public void test() {
		final double EPSILON = 1;
		Point targetPoint = new Point(0, 10);
		assertEquals(targetPoint.slope(new Point(0, 0)), -1000, EPSILON);
		System.out.println(targetPoint.slope(new Point(0, 20)));
		assertEquals(targetPoint.slope(new Point(0, 20)), 1000, EPSILON);
	}
}