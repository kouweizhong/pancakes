package edu.gatech.grits.pancakes.devices.driver.player;

import javaclient3.Position2DInterface;
import javaclient3.structures.*;
import edu.gatech.grits.pancakes.core.Kernel;
import edu.gatech.grits.pancakes.devices.backend.Backend;
import edu.gatech.grits.pancakes.devices.backend.PlayerBackend;
import edu.gatech.grits.pancakes.devices.driver.HardwareDriver;
import edu.gatech.grits.pancakes.lang.LocalPosePacket;

public class LocalPoseDriver implements HardwareDriver<LocalPosePacket> {
	
	private Position2DInterface device;
	private PlayerBackend backend;
	private LocalPosePacket cachedPkt;
	
	public LocalPoseDriver(Backend backend) {
		this.backend = (PlayerBackend) backend;
		cachedPkt = new LocalPosePacket();
		
		while(!((PlayerBackend) backend).getHandle().isReadyRequestDevice()) {
			Kernel.getInstance().getSyslog().debug("Trying to get an interface for the LocalPoseDevice.");
			device = ((PlayerBackend) backend).getHandle().requestInterfacePosition2D(0, PlayerConstants.PLAYER_OPEN_MODE);
		}
		Kernel.getInstance().getSyslog().debug("Received an interface!");
		
		device.setControlMode(PlayerConstants.PLAYER_POSITION2D_REQ_VELOCITY_MODE);
	}
	
	public void request(LocalPosePacket pkt) {
		return;
	}
	
	public LocalPosePacket query() {
		LocalPosePacket pkt = new LocalPosePacket();
		
		backend.update();
		
		if(device.isDataReady()) {
			PlayerPose2d pose = device.getData().getPos();
			pkt.setPose((float)pose.getPx(), (float)pose.getPy(), (float)pose.getPa());
			cachedPkt = pkt;
			
			return pkt;
		} else {
			// if there is no update, return the last known position
			Kernel.getInstance().getSyslog().debug("Used cached local pose.");
			return cachedPkt;
		}
		
	}
	
	public void close() {
		// do nothing
	}
}
