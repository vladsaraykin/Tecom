package trap;

import java.util.Set;

import org.snmp4j.CommandResponder;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.PDU;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.TcpAddress;
import org.snmp4j.smi.UdpAddress;

import client.SnmpClient;

public class SnmpEventDispatcher implements CommandResponder {
	final TrapReceiver trapReceiver;

	public SnmpEventDispatcher(TrapReceiver trapReceiver) {
		super();
		this.trapReceiver = trapReceiver;
	}

	@Override
	public void processPdu(CommandResponderEvent event) {

		System.out.println("Received PDU...");
		Address peerAddress = event.getPeerAddress();
		String peerAddressStr = null;
		if (peerAddress instanceof UdpAddress) {
			peerAddressStr = ((UdpAddress) peerAddress).getInetAddress().getHostAddress();
		} else if (peerAddress instanceof TcpAddress) {
			peerAddressStr = ((TcpAddress) peerAddress).getInetAddress().getHostAddress();
		}

		boolean isClientFined = false;
		for (Set<SnmpClient> clients : trapReceiver.getClientsByListenPort().values()) {
			for (SnmpClient client : clients)
				if (client.getAddress().equals(peerAddressStr)) {
					PDU pdu = event.getPDU();
					client.handle(pdu);
					isClientFined = true;
				}
		}
		if (!isClientFined) {
			System.out.println("Recerved trap for unknown client from " + peerAddressStr);
		}

	}

}
