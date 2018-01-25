package client;

import org.snmp4j.CommandResponder;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.PDU;

public class SnmpEventDispatcher implements CommandResponder{

	@Override
	public void processPdu(CommandResponderEvent event) {
        System.out.println("Received PDU...");
        PDU pdu = event.getPDU();
	}

}
