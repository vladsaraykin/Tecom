package app.snmp.trap;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Collection;
import java.util.Set;

import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.TcpAddress;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import app.snmp.SnmpSessionManager;
import app.snmp.SnmpSessionManager.SnmpSessionType;
import app.snmp.client.SnmpClient;
import app.snmp.exception.SnmpSessionException;

public class TrapReceiverListener {

	private static final String LISTEN_HOST_ADDRESS = "0.0.0.0";

	public void init(Set<SnmpClient> clients) throws SnmpSessionException {
		SnmpEventDispatcher snmpEventDispatcher = new SnmpEventDispatcher(clients);
		Snmp snmpSession = SnmpSessionManager.getInstance().getSession(SnmpSessionType.TRAP_RECEIVER);
		snmpSession.addCommandResponder(snmpEventDispatcher);
	}

	@SuppressWarnings("rawtypes")
	private TransportMapping<?> findTransportMapping(Integer listenPort, Collection<TransportMapping> transportMappings) {
		TransportMapping<?> transport = null;
		for (TransportMapping<?> transportMapping : transportMappings) {
			Address peerAddress = transportMapping.getListenAddress();
			int peerPort = 0;
			if (peerAddress instanceof UdpAddress) {
				peerPort = ((UdpAddress) peerAddress).getPort();
			} else if (peerAddress instanceof TcpAddress) {
				peerPort = ((TcpAddress) peerAddress).getPort();
			}
			if (peerPort == listenPort) {
				transport = transportMapping;
				break;
			}
		}
		return transport;
	}

	public void addListener(Integer listenPort) throws SnmpSessionException, IOException {
		Snmp snmpSession = SnmpSessionManager.getInstance().getSession(SnmpSessionType.TRAP_RECEIVER);
		TransportMapping<?> transport = findTransportMapping(listenPort, snmpSession.getMessageDispatcher().getTransportMappings());
		if (transport == null) {
			UdpAddress addressListener = new UdpAddress(InetAddress.getByName(LISTEN_HOST_ADDRESS), listenPort);
			transport = new DefaultUdpTransportMapping(addressListener);
			snmpSession.getMessageDispatcher().addTransportMapping(transport);
			transport.addTransportListener(snmpSession.getMessageDispatcher());
			transport.listen();
		}
	}

	public void removeListener(Integer listenPort) throws SnmpSessionException, IOException {
		Snmp snmpSession = SnmpSessionManager.getInstance().getSession(SnmpSessionType.TRAP_RECEIVER);
		TransportMapping<?> transport = findTransportMapping(listenPort, snmpSession.getMessageDispatcher().getTransportMappings());
		if (transport != null) {
			if (transport.isListening()) {
				transport.close();
			}
			snmpSession.getMessageDispatcher().getTransportMappings().remove(transport);
		}
	}

}
