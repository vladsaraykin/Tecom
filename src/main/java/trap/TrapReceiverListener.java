package trap;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Collection;
import org.snmp4j.TransportMapping;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.TcpAddress;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import main.SnmpSessionManager;
import main.SnmpSessionManager.SnmpSessionType;

public class TrapReceiverListener {

	private String listenHostAddress = "0.0.0.0";

	public void init(TrapReceiver trapReceiver) throws IOException {
		SnmpEventDispatcher snmpEventDispatcher = new SnmpEventDispatcher(trapReceiver);
		SnmpSessionManager.getInstance().startSnmpSession(SnmpSessionType.TRAP_RECEIVER);
		SnmpSessionManager.getInstance().getSession(SnmpSessionType.TRAP_RECEIVER).addCommandResponder(snmpEventDispatcher);


	}

	@SuppressWarnings("rawtypes")
	public void addListener(Integer listenPort) throws IOException {
		UdpAddress addressListener = new UdpAddress(InetAddress.getByName(listenHostAddress), listenPort);
		if (SnmpSessionManager.getInstance().getSession(SnmpSessionType.TRAP_RECEIVER).getMessageDispatcher().getTransportMappings().size() == 0) {
			DefaultUdpTransportMapping transport = new DefaultUdpTransportMapping(addressListener);
			SnmpSessionManager.getInstance().getSession(SnmpSessionType.TRAP_RECEIVER).getMessageDispatcher().addTransportMapping(transport);
			transport.addTransportListener(SnmpSessionManager.getInstance().getSession(SnmpSessionType.TRAP_RECEIVER).getMessageDispatcher());
			transport.listen();
		} else {
			for (TransportMapping transportMapping : SnmpSessionManager.getInstance().getSession(SnmpSessionType.TRAP_RECEIVER).getMessageDispatcher().getTransportMappings()) {

				Address peerAddress = transportMapping.getListenAddress();
				int peerPort = 0;
				if (peerAddress instanceof UdpAddress) {
					peerPort = ((UdpAddress) peerAddress).getPort();
				} else if (peerAddress instanceof TcpAddress) {
					peerPort = ((TcpAddress) peerAddress).getPort();
				}
				if (peerPort != listenPort) {
					DefaultUdpTransportMapping transport = new DefaultUdpTransportMapping(addressListener);
					SnmpSessionManager.getInstance().getSession(SnmpSessionType.TRAP_RECEIVER).getMessageDispatcher().addTransportMapping(transport);
					transport.addTransportListener(SnmpSessionManager.getInstance().getSession(SnmpSessionType.TRAP_RECEIVER).getMessageDispatcher());
					transport.listen();
				}
			}
		}
	}

	@SuppressWarnings("rawtypes")
	public void removeListener(Integer listenPort) throws IOException {
		Collection<TransportMapping> transportMappings = SnmpSessionManager.getInstance().getSession(SnmpSessionType.TRAP_RECEIVER).getMessageDispatcher().getTransportMappings();
		for (TransportMapping transport : transportMappings) {
			Address peerAddress = transport.getListenAddress();
			int peerPort = 0;
			if (peerAddress instanceof UdpAddress) {
				peerPort = ((UdpAddress) peerAddress).getPort();
			} else if (peerAddress instanceof TcpAddress) {
				peerPort = ((TcpAddress) peerAddress).getPort();
			}
			if (peerPort != listenPort) {
				if (transport.isListening()) {
					transport.close();
					transportMappings.remove(transport);
				}
			} else {
				System.out.println("This port does not monitor traps");
			}
		}
	}

}
