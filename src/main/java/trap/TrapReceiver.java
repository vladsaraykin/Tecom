package trap;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import client.SnmpClient;

public class TrapReceiver {
	private Integer defaultListenPort = 162;
	private TrapReceiverListener listener;
	private Map<Integer, Set<SnmpClient>> clientsByListenPort;

	public Map<Integer, Set<SnmpClient>> getClientsByListenPort() {
		return clientsByListenPort;
	}

	public void setClientsByListenPort(Map<Integer, Set<SnmpClient>> clientsByListenPort) {
		this.clientsByListenPort = clientsByListenPort;
	}

	public void start() throws IOException {
		listener = new TrapReceiverListener();
		listener.init(this);
		listener.addListener(defaultListenPort);

	}

	public void stop() throws IOException {
		clientsByListenPort.clear();
	}

	public void registerClient(SnmpClient client) throws IOException {
		if (clientsByListenPort == null) {
			clientsByListenPort = new HashMap<>();
		}
		listener.addListener(client.getListenPort());
		Set<SnmpClient> clients = clientsByListenPort.get(client.getListenPort());
		if (clients == null) {
			clients = new HashSet<>();
		}
		clients.add(client);
		clientsByListenPort.put(client.getListenPort(), clients);
	}

	public void unregisterClient(SnmpClient client) throws IOException {
		
		
	}

}
