package edu.gatech.grits.pancakes.net;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;

import javolution.util.FastMap;

import org.jetlang.core.Callback;

import edu.gatech.grits.pancakes.core.Kernel;
import edu.gatech.grits.pancakes.lang.CoreChannel;
import edu.gatech.grits.pancakes.lang.NetworkNeighbor;
import edu.gatech.grits.pancakes.lang.NetworkNeighborPacket;
import edu.gatech.grits.pancakes.lang.NetworkPacket;
import edu.gatech.grits.pancakes.lang.Packet;
import edu.gatech.grits.pancakes.lang.PacketType;
import edu.gatech.grits.pancakes.lang.Task;
import edu.gatech.grits.pancakes.service.NetworkService;

public class NetworkClient extends Task {

	private FastMap<String, NetworkNeighbor> neighbors;
	
	public NetworkClient() {
		setDelay(0l);
		neighbors = new FastMap<String, NetworkNeighbor>();
		
		Callback<Packet> netCallback = new Callback<Packet>() {
			public void onMessage(Packet packet) {
				
				final NetworkNeighbor n = neighbors.get(((NetworkPacket) packet).getDestination());
				if(n != null) {
					try {	
						final Socket socket = new Socket(n.getHostname(), n.getNetworkPort());
						
						OutputStream out = socket.getOutputStream();
						ObjectOutputStream oout = new ObjectOutputStream(out);
						oout.writeObject(packet);
						oout.flush();
						socket.close();
					} catch (ConnectException ce){
						Kernel.syslog.error("Failed to connect to: " + n.getHostname());
						synchronized(this) {
							neighbors.remove(n.getID());
						}
					} catch(IOException e) {
						Kernel.syslog.error("Send failed.");
					}	
				} else {
					Kernel.syslog.error("Destination not in the reachable network neighborhood");
				}
			}
		};
		
		Callback<Packet> neighborCallback = new Callback<Packet>(){

			public void onMessage(Packet message) {
				if(message.getPacketType().equals(PacketType.NEIGHBOR)){
					NetworkNeighborPacket np = (NetworkNeighborPacket) message;
					if(!np.isExpired()){
//						Kernel.syslog.debug("NC: Adding " + np.getNeighbor().getID());
						String id = np.getNeighbor().getID();
						synchronized(this) {
							neighbors.put(id, np.getNeighbor());
						}
					}
					else{
//						Kernel.syslog.debug("NC: Removing " + np.getNeighbor().getID());
						synchronized(this) {
							neighbors.remove(np.getNeighbor().getID());
						}
					}
				}
			}
			
		};
		
		subscribe(CoreChannel.NETWORK, netCallback);
		subscribe(NetworkService.NEIGHBORHOOD, neighborCallback);
		
	}
	
	public void close() {
		unsubscribe();
	}

	public void run() {
		// TODO Auto-generated method stub
		
	}
}
