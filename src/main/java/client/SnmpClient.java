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
	private Snmp snmpSession = SnmpSessionManager.getInstance().getSession(SnmpSessionType.CLIENT);

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

	public SnmpClient(String address, Integer listenPort, Integer requestPort, String community, String writeCommunity) {
		super();
		this.address = address;
		this.readCommunity = community;
		this.listenPort = listenPort;
		this.requestPort = requestPort;
		this.writeCommunity = writeCommunity;
		clientId = clientIdCount;
		clientIdCount++;
	}

	public PDU getRequest(OID oidValue) throws IOException {
		PDU pdu = new PDU();
		pdu.add(new VariableBinding(new OID(oidValue)));
		pdu.setType(PDU.GET);
		ResponseEvent response = snmpSession.get(pdu, getTarget(readCommunity));
		if (checkResponse(response)) {
			return pdu;
		} else {
			return null;
		}

	}

	public PDU getNextRequest(String oidValue) throws IOException {
		PDU pdu = new PDU();
		pdu.add(new VariableBinding(new OID(oidValue)));
		pdu.setType(PDU.GETNEXT);
		ResponseEvent response = snmpSession.getNext(pdu, getTarget(readCommunity));
		if (checkResponse(response)) {
			return pdu;
		} else {
			return null;
		}

	}

	public List getRangeValues(OID firstOid, OID lastOid) throws IOException {
		List<PDU> listPDU = new ArrayList<>();
		
		PDU pdu = new PDU();
		pdu.add(new VariableBinding(new OID(firstOid)));
		pdu.setType(PDU.GET);
		
		listPDU.add(pdu);
		ResponseEvent response = snmpSession.get(pdu,getTarget(readCommunity));
		if(checkResponse(response)){
			if (!response.getResponse().getVariable(firstOid).toString().contains(NO_SUCH_OBJECT)) {
				if(getRequest(lastOid) != null){
					System.out.println(response.getResponse().get(0));
					while (true) {
						PDU nextPdu = getNextRequest(firstOid.toDottedString());
						if(checkResponse(snmpSession.get(nextPdu,getTarget(readCommunity)))){

							listPDU.add(nextPdu);
							OID nextOID = nextPdu.get(0).getOid();
							if (!lastOid.equals(nextOID)) {
								firstOid = nextOID;
							} else {
								break;
							}
						}
					}
				}else{
					System.out.println("The last OID is not found");
				}

			} else {
				System.out.println("This number oid: " + firstOid + " isn't in the table");
			}
		}else {
			System.out.println(response + " is null");
		}
		
		return listPDU;
	}

	public boolean setRequest(String oidValue, String newVariable) throws IOException {
		OID oid = new OID(oidValue);
		Variable var = new OctetString(newVariable);
		VariableBinding variableBinding = new VariableBinding(oid, var);
		PDU pdu = new PDU();
		pdu.add(variableBinding);
		pdu.setType(PDU.SET);
		ResponseEvent response = SnmpSessionManager.getInstance().getSession(SnmpSessionType.CLIENT).set(pdu,
				getTarget(writeCommunity));

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
