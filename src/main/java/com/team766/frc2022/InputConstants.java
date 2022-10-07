package com.team766.frc2022;

/**
 * Constants used for the Operator Interface, eg for joyticks, buttons, dials, etc.
 * 
 * TODO: consider moving this into a config file.
 */
public final class InputConstants {

	//Joysticks
	public static final int LEFT_JOYSTICK = 0;
	public static final int RIGHT_JOYSTICK = 1;
	public static final int CONTROL_PANEL = 2;

	//Navigation
	public static final int AXIS_LEFT_RIGHT = 0;
	public static final int AXIS_FORWARD_BACKWARD = 1;
	public static final int AXIS_TWIST = 3;

	// Joystick buttons
	public static final int CROSS_DEFENSE = 7; 
	public static final int JOYSTICK_FAST_TURNING = 1;
	public static final int JOYSTICK_RESET_GYRO = 2;

	// Elevator Buttons
	public static final int JOYSTICK_ELEVATOR_UP_BUTTON = 3;
	public static final int JOYSTICK_ELEVATOR_DOWN_BUTTON = 4;
	public static final int JOYSTICK_ARMS_BACKWARDS_BUTTON = 3;
	public static final int JOYSTICK_ARMS_FORWARDS_BUTTON = 4;
	public static final int JOYSTICK_RESET_ELEVATOR = 2;
}