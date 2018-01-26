package client;

import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.*;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import main.SnmpSessionManager;
import main.SnmpSessionManager.SnmpSessionType;

import java.io.IOException;

public class SnmpClient {

	private final static String WRITE_COMMUNITY = "private";
	private final static String READ_COMMUNITY = "public";
	private final static int SNMP_RETRIES = 3;
	private final static long SNMP_TIMEOUT = 1000L;

	private String address = null;
	private TransportMapping transport = null;
	private String community = null;
	private Integer listenPort = null;
	private Integer requestPort = null;
	private static int snmpVersion = SnmpConstants.version2c;

	public String getAddress() {
		return address;
	}

	public String getCommunity() {
		return community;
	}

	public Integer getListenPort() {
		return listenPort;
	}

	public Integer getRequestPort() {
		return requestPort;
	}

	public SnmpClient(String address, Integer listenPort, Integer requestPort, String community) {
		super();
		this.address = address;
		this.community = community;
		this.listenPort = listenPort;
		this.requestPort = requestPort;
	}

	public void getRequest(OID oidValue) throws IOException {
		PDU responsePDU = getResponseEvent(oidValue).getResponse();
		System.out.println("\nResponse:\nGot Get Response from Agent...");
		checkError(responsePDU);
	}

	public ResponseEvent getNextRequest(String oidValue) throws IOException {
		PDU pdu = new PDU();

		pdu.add(new VariableBinding(new OID(oidValue)));

		pdu.setType(PDU.GETNEXT);
		ResponseEvent response = SnmpSessionManager.getInstance().getSession(SnmpSessionType.CLIENT).getNext(pdu,
				getTarget(READ_COMMUNITY));
		if (response != null) {
			System.out.println("\nResponse:\nGot GetNext Response from Agent...");
			PDU responsePDU = response.getResponse();
			checkError(responsePDU);
			return response;
		} else {
			System.out.println("Error: Agent Timeout... ");
		}
		return null;
	}

	public void getRangeValues(OID oidValue1, OID oidValue2) throws IOException {

		ResponseEvent response = getResponseEvent(oidValue1);

		if (!response.getResponse().getVariable(oidValue1).toString().contains("noSuchObject")) {
			System.out.println(response.getResponse().get(0));
			while (true) {
				response = getNextRequest(oidValue1.toDottedString());
				OID next = response.getResponse().get(0).getOid();
				if (!oidValue2.equals(next)) {
					oidValue1 = next;
				} else {
					break;
				}
			}
		} else {
			System.out.println("This number oid: " + oidValue1 + " isn't in the table");
		}
		System.out.println("get range complite");
	}

	public void setRequest(String oidValue, String newVariable) throws IOException {
		OID oid = new OID(oidValue);
		Variable var = new OctetString(newVariable);
		VariableBinding variableBinding = new VariableBinding(oid, var);
		PDU pdu = new PDU();
		pdu.add(variableBinding);
		pdu.setType(PDU.SET);
		ResponseEvent response = SnmpSessionManager.getInstance().getSession(SnmpSessionType.CLIENT).set(pdu,
				getTarget(WRITE_COMMUNITY));
		if (response != null) {
			System.out.println("\nResponse:\nGot Snmp Set Response from Agent");
			PDU responsePDU = response.getResponse();
			checkError(responsePDU);
		} else {
			System.out.println("Error: Agent Timeout... ");
		}
	}

	public ResponseEvent getResponseEvent(OID oidValue) throws IOException {
		PDU pdu = new PDU();
		pdu.add(new VariableBinding(oidValue));
		pdu.setType(PDU.GET);
		System.out.println("Sending Request to Agent");
		ResponseEvent response = SnmpSessionManager.getInstance().getSession(SnmpSessionType.CLIENT).get(pdu,
				getTarget(READ_COMMUNITY));
		if (response != null) {
			return response;
		}
		throw new RuntimeException();
	}

	public Target getTarget(String community) {
		Address targetAddress = GenericAddress.parse(address + "/" + requestPort);
		CommunityTarget target = new CommunityTarget();
		target.setCommunity(new OctetString(READ_COMMUNITY));
		target.setVersion(snmpVersion);
		target.setAddress(targetAddress);
		target.setRetries(SNMP_RETRIES);
		target.setTimeout(SNMP_TIMEOUT);
		return target;
	}

	private void checkError(PDU responsePDU) {
		if (responsePDU != null) {
			int errorStatus = responsePDU.getErrorStatus();
			int errorIndex = responsePDU.getErrorIndex();
			String errorStatusText = responsePDU.getErrorStatusText();
			if (errorStatus == PDU.noError) {
				System.out.println("Snmp Response = " + responsePDU.getVariableBindings());
			} else {
				System.out.println("Error: Request Failed");
				System.out.println("Error Status = " + errorStatus);
				System.out.println("Error Index = " + errorIndex);
				System.out.println("Error Status Text = " + errorStatusText);
			}
		} else {
			System.out.println("Error: Response PDU is null");
		}
	}

	public void handle(PDU pdu) {
		System.out.println("Resend message " + pdu);
	}

}
