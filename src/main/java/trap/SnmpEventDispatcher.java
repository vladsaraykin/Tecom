package trap;

import java.util.Set;

import org.snmp4j.CommandResponder;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.PDU;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.TcpAddress;
import org.snmp4j.smi.UdpAddress;

import client.SnmpClient;

public class SnmpEventDispatcher implements CommandResponder{
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
        	peerAddressStr = ((TcpAddress)peerAddress).getInetAddress().getHostAddress();
        }
        final String address = peerAddressStr;
        
        for (Set<SnmpClient> clients : trapReceiver.getClientsByListenPort().values()) {
        	clients.forEach(client -> {
        		if (client.getAddress().equals(address)) {
        			PDU pdu = event.getPDU();
        			client.handle(pdu);
        		}
        	});
        }
        
	}

}
