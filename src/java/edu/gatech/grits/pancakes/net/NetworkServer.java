package edu.gatech.grits.pancakes.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

import edu.gatech.grits.pancakes.core.Kernel;
import edu.gatech.grits.pancakes.core.Stream.CommunicationException;
import edu.gatech.grits.pancakes.lang.CoreChannel;
import edu.gatech.grits.pancakes.lang.Packet;

public class NetworkServer {

	private ServerSocket listener;
	private Thread mainThread;
	private volatile boolean stopRequested = false;
	private volatile boolean initialAccept = true;
	
	public NetworkServer(int port) {
		try {
			listener = new ServerSocket(port);
		} catch (IOException e) {
			System.out.println("Error!");
		}
		
		Runnable task = new Runnable() {
			public void run() {
				while(!stopRequested) {
					try {
						//System.out.println("Server waiting for client on port " + listener.getLocalPort() + "!");
						final Socket client = listener.accept();
						
						if(initialAccept) {
							handleConnection(client);
//							Kernel.getInstance().getSyslog().debug("Got initial connection.");
							initialAccept = false;
						} else {
						
							Runnable task = new Runnable() {
								public void run() {
//									Kernel.getInstance().getSyslog().debug("Handling an incoming packet.");
									handleConnection(client);
//									Kernel.getInstance().getSyslog().debug("Finished handling the packet.");
								}
							};
							Kernel.getInstance().getScheduler().execute(task);
						}
					} catch (IOException e) {
						System.out.println("Error receiving object or socket closed.");
						//e.printStackTrace();
						return;
					}
				}
			}
		};
		mainThread = new Thread(task);
		mainThread.start();
	}
	
	public void handleConnection(Socket client) {
		InputStream in;
		try {
			in = client.getInputStream();
			ObjectInputStream oin = new ObjectInputStream(in);
			Packet pkt = (Packet) oin.readObject();
//			oin.close();
//			in.close();
			Kernel.getInstance().getStream().publish(CoreChannel.SYSTEM, pkt);
		} catch (IOException e) {
			System.err.println("Error receiving object.");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CommunicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public final void close() {
		stopRequested = true;
		try {
			listener.close();
		} catch (IOException e) {
			System.err.println("Unable to close socket");
		}
		//mainThread.interrupt();
	}
}
