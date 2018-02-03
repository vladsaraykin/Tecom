package trap;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

import client.SnmpClient;
import main.SnmpSessionManager;
import main.SnmpSessionManager.SnmpSessionType;
import org.snmp4j.TransportMapping;

public class TrapReceiver {
	private static final Integer defaultListenPort = 162;
	private TrapReceiverListener listener;
	private Map<Integer, Set<SnmpClient>> clientsByListenPort = new HashMap<>();
	private boolean status = false;

	public Map<Integer, Set<SnmpClient>> getClientsByListenPort() {
		return clientsByListenPort;
	}

	public void start() throws IOException {

		listener = new TrapReceiverListener();
		listener.init(this);
		listener.addListener(defaultListenPort);
		status = true;
	}

	public void stop() throws IOException {

		if (status) {
			SnmpSessionManager.getInstance().stopSnmpSession(SnmpSessionType.TRAP_RECEIVER);
			clientsByListenPort.clear();
			System.out.println("Trap receiver is stopped");
			status = false;
		}else {
			System.out.println("Trap receiver is not run");
		}

	}

	public void registerClient(SnmpClient client) throws IOException {
		listener.addListener(client.getListenPort());
		Set<SnmpClient> clients = clientsByListenPort.get(client.getListenPort());
		if (clients == null) {
			clients = new HashSet<>();
		}
		clients.add(client);
		clientsByListenPort.put(client.getListenPort(), clients);
	}

	public void unregisterClient(SnmpClient client) throws IOException {

		Set<SnmpClient> clients = clientsByListenPort.get(client.getListenPort());
		clients.remove(client);
		if (clients.isEmpty()) {
			try {
				listener.removeListener(client.getListenPort());
				clientsByListenPort.remove(client.getListenPort());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	public void showClients() {
		for (Entry<Integer, Set<SnmpClient>> clientsByListenPort : clientsByListenPort.entrySet()) {
			System.out.println("  Listen port " + clientsByListenPort.getKey() + ":");
			for (SnmpClient client : clientsByListenPort.getValue()) {
				System.out.println("   Client #" + client.getClientId());
				System.out.println("    IP address: " + client.getAddress());
				System.out.println("      Request port: " + client.getRequestPort());
				System.out.println("    Community: " + client.getCommunity());
			}
	}
	}

	public boolean isStarted() {
		return status;
	}

}
