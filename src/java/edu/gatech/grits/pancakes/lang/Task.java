package edu.gatech.grits.pancakes.lang;

import javolution.util.FastList;

import org.jetlang.core.Callback;
import org.jetlang.fibers.Fiber;

import edu.gatech.grits.pancakes.core.Kernel;
import edu.gatech.grits.pancakes.core.Stream.CommunicationException;

/**
 * This class abstracts the basic functionality of a Pancakes Task. All user
 * and system components that must perform actions must subclass from Task.
 * @author pmartin
 *
 */
public abstract class Task implements Taskable {

//	private Subscription subscription = null;
	private boolean isEventDriven = false;
	private boolean isTimeDriven = false;
	private long timeDelay = 0l;
	
	private FastList<Subscription> subscriptions;
	private Fiber taskFiber;
	
	public Task() {
		taskFiber = Kernel.scheduler.newFiber();
		taskFiber.start();

		subscriptions = new FastList<Subscription>();
	}
	
	public final void setDelay(long delay) {
		if(delay > 0l){
			isTimeDriven = true;
		}
		else{
			isTimeDriven = false;
		}
		timeDelay = delay;
	}
	
	public final long delay() {
		return timeDelay;
	}
	
	public final boolean isEventDriven() {
		return isEventDriven;
	}
	
	public final boolean isTimeDriven() {
		return isTimeDriven;
	}

	public void subscribe(String chl, Callback cbk) {
		if(chl != null){
			Subscription s = new Subscription(chl, taskFiber, cbk);
			subscriptions.add(s);
			try {
				Kernel.stream.subscribe(s);
			} catch (CommunicationException e) {
				Kernel.syslog.error(e.getMessage());
			}
			if(subscriptions.size() > 0){
				this.isEventDriven = true;
			}
		}
	}
	
	public final void unsubscribe() {
		for(Subscription s : subscriptions){
			Kernel.stream.unsubscribe(s);
			s = null;
		}
		taskFiber.dispose();
//		if(subscription != null) {
//			Kernel.stream.unsubscribe(subscription);
//			subscription.getFiber().dispose();
//			subscription = null;
//		}
	}
	
	public final void publish(String channel, Packet packet) {
		try {
			Kernel.stream.publish(channel, packet);
		} catch (CommunicationException e) {
			Kernel.syslog.error("Unable to publish packet to " + channel + ".");
		}
	}
}
