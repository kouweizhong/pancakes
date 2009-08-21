package edu.gatech.grits.pancakes.devices;

import org.jetlang.core.Callback;
import org.jetlang.fibers.Fiber;

import edu.gatech.grits.pancakes.lang.MotorPacket;
import edu.gatech.grits.pancakes.lang.Packet;
import edu.gatech.grits.pancakes.lang.Subscription;
import edu.gatech.grits.pancakes.core.Kernel;
import edu.gatech.grits.pancakes.core.Stream.CommunicationException;
import edu.gatech.grits.pancakes.devices.backend.Backend;
import edu.gatech.grits.pancakes.devices.backend.PlayerBackend;
import edu.gatech.grits.pancakes.devices.driver.HardwareDriver;

public class MotorDevice implements Device {
	
	private HardwareDriver<MotorPacket> driver;
	private final long delay = 0l;
	private final Subscription subscription;
	
	@SuppressWarnings("unchecked")
	public MotorDevice(Backend backend) {
		driver = (HardwareDriver<MotorPacket>) backend.getDriver("MotorDriver");
		Fiber fiber = Kernel.scheduler.newFiber();
		fiber.start();
		Callback<Packet> callback = new Callback<Packet>() {
			public void onMessage(Packet pkt) {
				if(pkt.getPacketType().equals("motors"))
					driver.request((MotorPacket) pkt);
			}
		};
		
		subscription = new Subscription("user", fiber, callback);
		
		try {
			Kernel.stream.subscribe(subscription);
		} catch (CommunicationException e) {
			// TODO Auto-generated catch block
			System.err.println(e.getMessage());
		}
	}
	
	public final long delay () {
		return delay;
	}
	
	public final boolean isRunnable() {
		return false;
	}
	
	public Packet query() {
		return driver.query();
	}
	
	public void request(Packet pkt) {
		driver.request((MotorPacket) pkt);
	};
	
	public void close() {
		Kernel.stream.unsubscribe(subscription);
		subscription.getFiber().dispose();
		driver.close();
	}
	
	public void debug() {
		MotorPacket pkt = driver.query();
		
		System.err.println("Player Velocity pose: (" + pkt.getVelocity() + "," + pkt.getRotationalVelocity() + ")");
	}
}
