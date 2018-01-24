package client;

import java.util.Map;
import java.util.Set;

public class TrapReceiver {
	private Integer defaultListenPort = 162;
	private TrapReceiverListener listener;
	private Map<Integer, Set<SnmpClient>> clientsByListenPort;
	
	
	public void start() {
		
	}
	
	public void stop() {
		
	}
	
	public  void registerClient(SnmpClient client) {
		
	}
	
	public void unregisterClient(SnmpClient client) {
		
	}

}
