package app.snmp.trap;

import java.util.HashSet;
import java.util.Set;

import org.snmp4j.CommandResponder;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.PDU;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.TcpAddress;
import org.snmp4j.smi.UdpAddress;

import app.logger.ConsoleLogger;
import app.logger.Logger;
import app.snmp.client.SnmpClient;

public class SnmpEventDispatcher implements CommandResponder {

	private static final Logger LOGGER = new ConsoleLogger(SnmpEventDispatcher.class);

	private Set<SnmpClient> clients = new HashSet<>();

	public SnmpEventDispatcher(Set<SnmpClient> clients) {
		super();
		if (clients == null) {
			throw new IllegalArgumentException("The argument 'clients' was not be null");
		}
		this.clients.addAll(clients);
	}

	@Override
	public void processPdu(CommandResponderEvent event) {
		LOGGER.trace("Received PDU: " + event);
		if (event.isProcessed()) {
			return;
		}
		
		Address peerAddress = event.getPeerAddress();
		String peerAddressStr = null;
		if (peerAddress instanceof UdpAddress) {
			peerAddressStr = ((UdpAddress) peerAddress).getInetAddress().getHostAddress();
		} else if (peerAddress instanceof TcpAddress) {
			peerAddressStr = ((TcpAddress) peerAddress).getInetAddress().getHostAddress();
		}
		
		boolean isClientFound = false;
		for (SnmpClient client : clients) {
			if (client.getAddress().equals(peerAddressStr)) {
				PDU pdu = event.getPDU();
				client.handle(pdu);
				isClientFound = true;
				break;
			}
		}
		
		if (!isClientFound) {
			LOGGER.warn("Recerved trap for unknown client from " + peerAddressStr);
		}
		
		event.setProcessed(true);
	}

}
