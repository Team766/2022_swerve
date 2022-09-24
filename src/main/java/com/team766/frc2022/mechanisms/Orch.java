package com.team766.frc2022.mechanisms;
import com.team766.framework.Mechanism;
import com.team766.hal.RobotProvider;
import com.team766.hal.CANSpeedController.ControlMode;
import com.team766.hal.CANSpeedController;
import com.team766.library.ValueProvider;
import com.team766.logging.Category;
import com.team766.config.ConfigFileReader;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.sensors.AbsoluteSensorRange;
import com.ctre.phoenix.sensors.CANCoder;
import com.ctre.phoenix.sensors.CANCoderConfiguration;
import java.util.ArrayList;
import com.ctre.phoenix.motorcontrol.can.TalonFX;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonFX;

import com.ctre.phoenix.music.Orchestra;

import com.team766.logging.Category;


public class Orch extends Mechanism{
	Orchestra _orchestra;

	WPI_TalonFX [] _fxes =  { 
		new WPI_TalonFX(10, "rio"),
		new WPI_TalonFX(11, "rio"),
		new WPI_TalonFX(12, "rio"),
		new WPI_TalonFX(13, "rio"),
		new WPI_TalonFX(6, "rio"),
		new WPI_TalonFX(7, "rio"),
		new WPI_TalonFX(8, "rio"),
		new WPI_TalonFX(9, "rio")
	};
	// String[] _songs = new String[] {
	//"song1.chrp" };
	public Orch(){	loggerCategory = Category.DRIVE;}
	
	/*public void playSong(int x){
		_orchestra.loadMusic(_songs[x]); 
		ArrayList<TalonFX> _instruments = new ArrayList<TalonFX>();

		for (int i = 0; i < _fxes.length; ++i) {
            _instruments.add(_fxes[i]);
        }
		
	}*/
	public void loadSong(){
		log("1Loading song!");
		ArrayList<TalonFX> _instruments = new ArrayList<TalonFX>();

		for (int i = 0; i < _fxes.length; ++i) {
            _instruments.add(_fxes[i]);	
			log(i+"|");
		}
		log("loop");
		_orchestra = new Orchestra(_instruments);

		_orchestra.loadMusic("song1.chrp");
		log("post load");
		
	}

	public void play(){
		log("Play song");
		_orchestra.play();
	}
}
