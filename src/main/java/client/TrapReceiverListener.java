package client;

import org.snmp4j.Snmp;

public class TrapReceiverListener {
	private Snmp snmpSession;
	private String listenHostAddress = "0.0.0.0";
	public void init(TrapReceiver trapReceiver) {
		SnmpEventDispatcher snmpEventDispatcher = new SnmpEventDispatcher();
		
	}
}
