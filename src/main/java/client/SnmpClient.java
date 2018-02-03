package client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;

import main.SnmpSessionManager;
import main.SnmpSessionManager.SnmpSessionType;

public class SnmpClient {

	private static int clientIdCount = 1;

	private final static int SNMP_RETRIES = 3;
	private final static long SNMP_TIMEOUT = 1000L;
	private final static String NO_SUCH_OBJECT = "noSuchObject";

	private int clientId;
	private String address = null;
	private TransportMapping transport = null;
	private String readCommunity = null;
	private String writeCommunity = null;
	private Integer listenPort = null;
	private Integer requestPort = null;
	private static int snmpVersion = SnmpConstants.version2c;
	

	public String getAddress() {

		return address;
	}

	public String getCommunity() {
		return readCommunity;
	}

	public int getClientId() {
		return clientId;

	}

	public Integer getListenPort() {
		return listenPort;
	}

	public Integer getRequestPort() {
		return requestPort;
	}

	public SnmpClient(String address, Integer listenPort, Integer requestPort, String community,
			String writeCommunity) {
		super();
		this.address = address;
		this.readCommunity = community;
		this.listenPort = listenPort;
		this.requestPort = requestPort;
		this.writeCommunity = writeCommunity;
		clientId = clientIdCount;
		clientIdCount++;
	}

	public PDU getRequest(OID oidValue) {
		Snmp snmpSession = SnmpSessionManager.getInstance().getSession(SnmpSessionType.CLIENT);
		try {
			PDU pdu = new PDU();
			pdu.add(new VariableBinding(new OID(oidValue)));
			pdu.setType(PDU.GET);
			ResponseEvent response = snmpSession.get(pdu, getTarget(readCommunity));
			if (checkResponse(response)) {
				return response.getResponse();
			} else {
				return null;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public PDU getNextRequest(OID oidValue) {
		Snmp snmpSession = SnmpSessionManager.getInstance().getSession(SnmpSessionType.CLIENT);
		try {

			PDU pdu = new PDU();
			pdu.add(new VariableBinding(oidValue));
			pdu.setType(PDU.GETNEXT);
			ResponseEvent response = snmpSession.getNext(pdu, getTarget(readCommunity));
			if (checkResponse(response)) {
				return response.getResponse();
			} else {
				return null;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public List<PDU> getRangeValues(OID firstOid, OID lastOid) {

		List<PDU> results = new ArrayList<>();
		if(firstOid == null && lastOid == null) {
			return results;
		}
		PDU firstResponse = getRequest(firstOid);
		OID nextOid = firstOid;
		PDU nextResponse = firstResponse;

		if (firstResponse != null && !firstResponse.getVariable(firstOid).toString().contains(NO_SUCH_OBJECT)) {

			do {
				results.add(nextResponse);
				if (lastOid.equals(nextOid)) {
					break;
				}
				nextResponse = getNextRequest(nextOid);
				nextOid = nextResponse.get(0).getOid();
				if (nextResponse == null || nextResponse.getVariable(nextOid).toString().contains(NO_SUCH_OBJECT)) {
					break;
				}

			} while (true);
		}

		return results;

	}

	public boolean setRequest(OID oidValue, String newVariable) {
		ResponseEvent response = null;
		try {
			Variable var = new OctetString(newVariable);
			VariableBinding variableBinding = new VariableBinding(oidValue, var);
			PDU pdu = new PDU();
			pdu.add(variableBinding);
			pdu.setType(PDU.SET);
			response = SnmpSessionManager.getInstance().getSession(SnmpSessionType.CLIENT).set(pdu,
					getTarget(writeCommunity));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return checkResponse(response);

	}

	private Target getTarget(String community) {
		Address targetAddress = GenericAddress.parse(address + "/" + requestPort);
		CommunityTarget target = new CommunityTarget();
		target.setCommunity(new OctetString(community));
		target.setVersion(snmpVersion);
		target.setAddress(targetAddress);
		target.setRetries(SNMP_RETRIES);
		target.setTimeout(SNMP_TIMEOUT);
		return target;
	}

	private boolean checkResponse(ResponseEvent event) {
		if (event != null) {
			PDU pdu = event.getResponse();
			if (pdu != null) {
				int errorStatus = pdu.getErrorStatus();
				int errorIndex = pdu.getErrorIndex();
				String errorStatusText = pdu.getErrorStatusText();
				if (errorStatus == PDU.noError) {
					System.out.println("Snmp session there are no erros");
					return true;
				} else {
					System.out.println("Error: Request Failed");
					System.out.println("Error Status = " + errorStatus);
					System.out.println("Error Index = " + errorIndex);
					System.out.println("Error Status Text = " + errorStatusText);
				}
			}
		} else {
			System.out.println("Error: Response PDU is null");

		}
		return false;
	}

	public void handle(PDU pdu) {
		System.out.println("Client #" + clientId + " received message: " + pdu);
	}

}
