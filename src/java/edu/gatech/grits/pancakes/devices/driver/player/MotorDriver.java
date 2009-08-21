package edu.gatech.grits.pancakes.devices.driver.player;

import java.lang.Math;

import javaclient2.Position2DInterface;
import javaclient2.structures.PlayerConstants;
import javaclient2.structures.PlayerPose;

import edu.gatech.grits.pancakes.core.Kernel;
import edu.gatech.grits.pancakes.devices.backend.Backend;
import edu.gatech.grits.pancakes.devices.backend.PlayerBackend;
import edu.gatech.grits.pancakes.devices.driver.HardwareDriver;
import edu.gatech.grits.pancakes.lang.MotorPacket;

public class MotorDriver implements HardwareDriver<MotorPacket> {
	
	private Position2DInterface device;
	
	public MotorDriver(Backend backend) {
		while(!((PlayerBackend) backend).getHandle().isReadyRequestDevice()) {
			Kernel.syslog.debug("Trying to get an interface for the MotorDevice.");
			device = ((PlayerBackend) backend).getHandle().requestInterfacePosition2D(0, PlayerConstants.PLAYER_OPEN_MODE);
		}
		Kernel.syslog.debug("Received an interface.");
		device.setControlMode(PlayerConstants.PLAYER_POSITION2D_REQ_VELOCITY_MODE);
		device.setMotorPower(1);
	}
	
	public void request(MotorPacket pkt) {
		float vel = pkt.getVelocity();
		float rad = pkt.getRotationalVelocity();
		device.setSpeed(vel, rad);
	}
	
	public MotorPacket query() {
		MotorPacket pkt = new MotorPacket();
		
		
		if(device.isDataReady()) {
			PlayerPose pose = device.getData().getVel();
			pkt.setVelocity((float) Math.sqrt(Math.pow(pose.getPx(),2) + Math.pow(pose.getPy(), 2)));
			pkt.setRotationalVelocity((float) pose.getPa());
		} else {
			// invalid packet
		}
		
		return pkt;
	}
	
	public void close() {
		// do nothing
	}
}
