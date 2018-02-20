package app.snmp.trap;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import app.logger.ConsoleLogger;
import app.logger.Logger;
import app.snmp.SnmpSessionManager;
import app.snmp.SnmpSessionManager.SnmpSessionType;
import app.snmp.client.SnmpClient;
import app.snmp.exception.SnmpSessionException;

public class TrapReceiver {

	private static final Logger LOGGER = new ConsoleLogger(TrapReceiver.class);
	private static final Integer DEFAULT_LISTEN_PORT = 162;
	
	private TrapReceiverListener listener;
	private boolean status = false;
	private Map<Integer, Set<SnmpClient>> clientsByListenPort = new HashMap<>();

	public Map<Integer, Set<SnmpClient>> getClientsByListenPort() {
		return clientsByListenPort;
	}

	public boolean isStarted() {
		return status;
	}

	public void start() throws SnmpSessionException, IOException {
		if (!isStarted()) {
			LOGGER.info("Trap receiver starting...");
			SnmpSessionManager.getInstance().startSnmpSession(SnmpSessionType.TRAP_RECEIVER);
			
			Set<SnmpClient> allClients = new HashSet<>();
			for (Set<SnmpClient> clients : clientsByListenPort.values()) {
				allClients.addAll(clients);
			}
			if(listener == null) {
				listener = new TrapReceiverListener();
			}
			listener.init(allClients);
			listener.addListener(DEFAULT_LISTEN_PORT);

			status = true;
			LOGGER.info("Trap receiver is started");
		} else {
			LOGGER.warn("Trap receiver is already running");
		}
	}

	public void stop() throws SnmpSessionException {
		if (isStarted()) {
			LOGGER.info("Trap receiver stopping...");
			SnmpSessionManager.getInstance().stopSnmpSession(SnmpSessionType.TRAP_RECEIVER);
			clientsByListenPort.clear();
			status = false;
			LOGGER.info("Trap receiver is stopped");
		}else {
			LOGGER.warn("Trap receiver is already stopping");
		}
	}

	public void registerClient(SnmpClient client) throws SnmpSessionException, IOException {
			if(listener == null){
				listener = new TrapReceiverListener();
			}
			listener.addListener(client.getTrapPort());
			LOGGER.info("Register of client '" + client.getClientName() + "'...");
			Set<SnmpClient> clients = clientsByListenPort.get(client.getTrapPort());
			if (clients == null) {
				clients = new HashSet<>();
			}
			clients.add(client);
			clientsByListenPort.put(client.getTrapPort(), clients);
			LOGGER.info("Client '" + client.getClientName() + "' is registered");

	}

	public void unregisterClient(SnmpClient client) throws SnmpSessionException, IOException {
		if (isStarted()) {
			Set<SnmpClient> clients = clientsByListenPort.get(client.getTrapPort());
			if (clients != null) {
				LOGGER.info("Unregister of client '" + client.getClientName() + "'...");
				clients.remove(client);
				if (clients.isEmpty()) {
					listener.removeListener(client.getTrapPort());
					clientsByListenPort.remove(client.getTrapPort());
				}
				LOGGER.info("Client '" + client.getClientName() + "' is unregistered");
			}
		}
	}

}
