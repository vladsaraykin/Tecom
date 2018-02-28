package app.snmp.client;

import java.util.ArrayList;
import java.util.List;

import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.util.DefaultPDUFactory;
import org.snmp4j.util.TableEvent;
import org.snmp4j.util.TableUtils;

import app.logger.ConsoleLogger;
import app.logger.Logger;
import app.snmp.SnmpSessionManager;
import app.snmp.SnmpSessionManager.SnmpSessionType;
import app.snmp.SnmpUtil;
import app.snmp.exception.SnmpRequestException;
import app.snmp.exception.SnmpSessionException;

public class SnmpClient {

	private static final Logger LOGGER = new ConsoleLogger(SnmpClient.class);
	private static final int DEFAULT_SNMP_RETRIES = 3;
	private static final long DEFAULT_SNMP_TIMEOUT = 1000L;
	private static final String NO_SUCH_OBJECT = "noSuchObject";

	private static int clientIdCount = 0;

	private static int getNextClientId() {
		clientIdCount++;
		return clientIdCount;
	}

	private int clientId;
	private String clientName;
	private String address = null;
	private String readCommunity = null;
	private String writeCommunity = null;
	private Integer requestPort = null;
	private Integer trapPort = null;
	private int snmpVersion = SnmpConstants.version2c;
	private int retries = DEFAULT_SNMP_RETRIES;
	private long timeout = DEFAULT_SNMP_TIMEOUT;

	public SnmpClient(String address, String readCommunity, String writeCommunity, Integer requestPort,
			Integer trapPort) {
		this(address, readCommunity, writeCommunity, requestPort, trapPort, null);
	}

	public SnmpClient(String address, String readCommunity, String writeCommunity, Integer requestPort,
			Integer trapPort, String clientName) {
		super();

		if (address == null) {
			throw new IllegalArgumentException("The argument 'address' must not be null");
		}
		if (readCommunity == null) {
			throw new IllegalArgumentException("The argument 'readCommunity' must not be null");
		}
		if (writeCommunity == null) {
			throw new IllegalArgumentException("The argument 'writeCommunity' must not be null");
		}
		if (requestPort == null) {
			throw new IllegalArgumentException("The argument 'requestPort' must not be null");
		}
		if (trapPort == null) {
			throw new IllegalArgumentException("The argument 'trapPort' must not be null");
		}

		this.clientId = getNextClientId();
		this.address = address;
		this.readCommunity = readCommunity;
		this.writeCommunity = writeCommunity;
		this.requestPort = requestPort;
		this.trapPort = trapPort;
		this.clientName = clientName != null ? clientName : address;
	}

	public int getClientId() {
		return clientId;
	}

	public String getClientName() {
		return clientName;
	}

	public String getAddress() {
		return address;
	}

	public String getReadCommunity() {
		return readCommunity;
	}

	public String getWriteCommunity() {
		return writeCommunity;
	}

	public Integer getRequestPort() {
		return requestPort;
	}

	public Integer getTrapPort() {
		return trapPort;
	}

	public int getSnmpVersion() {
		return snmpVersion;
	}

	public int getRetries() {
		return retries;
	}

	public long getTimeout() {
		return timeout;
	}

	public PDU getRequest(OID oid) throws SnmpSessionException, SnmpRequestException {
		if (oid == null) {
			throw new IllegalArgumentException("The argument 'oid' must not be null");
		}

		Snmp snmpSession = SnmpSessionManager.getInstance().getSession(SnmpSessionType.CLIENT);
		PDU pdu = new PDU();
		pdu.add(new VariableBinding(oid));
		Target target = SnmpUtil.createReadTarget(this);

		PDU response = null;
		try {
			ResponseEvent responseEvent = snmpSession.get(pdu, target);
			if (checkResponse(responseEvent)) {
				response = responseEvent.getResponse();
			}
		} catch (Exception e) {
			throw new SnmpRequestException("Failed to get request for '" + oid.toDottedString() + "'", e);
		}

		return response;
	}

	public PDU getNextRequest(OID oid) throws SnmpSessionException, SnmpRequestException {
		if (oid == null) {
			throw new IllegalArgumentException("The argument 'oid' must not be null");
		}

		Snmp snmpSession = SnmpSessionManager.getInstance().getSession(SnmpSessionType.CLIENT);
		PDU pdu = new PDU();
		pdu.add(new VariableBinding(oid));
		Target target = SnmpUtil.createReadTarget(this);

		PDU response = null;
		try {
			ResponseEvent responseEvent = snmpSession.getNext(pdu, target);
			if (checkResponse(responseEvent)) {
				response = responseEvent.getResponse();
			}
		} catch (Exception e) {
			throw new SnmpRequestException("Failed to get-next request for '" + oid.toDottedString() + "'", e);
		}
		return response;
	}

	public List<PDU> getWalkRequest(OID firstOid) throws SnmpSessionException, SnmpRequestException {
		return walkRequest(firstOid, null);
	}

	public List<PDU> walkRequest(OID firstOid, OID lastOid) throws SnmpSessionException, SnmpRequestException {
		List<PDU> results = new ArrayList<>();
		if (firstOid == null) {
			return results;
		}

		PDU firstResponse = getRequest(firstOid);
		if (firstResponse != null && firstResponse.getVariable(firstOid) != null
				&& !firstResponse.getVariable(firstOid).toString().contains(NO_SUCH_OBJECT)) {
			OID nextOid = firstOid;
			PDU nextResponse = firstResponse;
			do {
				results.add(nextResponse);

				nextResponse = getNextRequest(nextOid);
				if (nextResponse == null || nextResponse.getVariable(nextOid) == null
						|| nextResponse.getVariable(nextOid).toString().contains(NO_SUCH_OBJECT)) {
					if (lastOid != null) {
						LOGGER.warn("Last oid '" + lastOid.toDottedString() + "' was not reached");
					}
					break;
				}

				if (nextOid.equals(lastOid)) {
					results.add(nextResponse);
					break;
				}

				if (nextResponse.get(0) == null) {
					LOGGER.warn("The request was interrupted on '" + nextOid.toDottedString() + "'");
					break;
				}
				nextOid = nextResponse.get(0).getOid();
			} while (true);
		}

		return results;
	}

	public boolean setRequest(OID oid, Variable variable) throws SnmpSessionException, SnmpRequestException {
		Snmp snmpSession = SnmpSessionManager.getInstance().getSession(SnmpSessionType.CLIENT);
		PDU pdu = new PDU();
		pdu.add(new VariableBinding(oid, variable));
		Target target = SnmpUtil.createWriteTarget(this);

		boolean isOperationSucceeded = false;
		try {
			ResponseEvent responseEvent = snmpSession.set(pdu, target);
			if (checkResponse(responseEvent)) {
				isOperationSucceeded = true;
			}
		} catch (Exception e) {
			throw new SnmpRequestException("Failed to set request for '" + oid.toDottedString() + "'", e);
		}

		return isOperationSucceeded;
	}

	public List<TableEvent> getTable(OID[] columnOIDs) throws SnmpSessionException {
		if ((columnOIDs == null) || (columnOIDs.length == 0)) {
			LOGGER.error("No column OIDs specified");
			return null;
		}
		Snmp snmpSession = SnmpSessionManager.getInstance().getSession(SnmpSessionType.CLIENT);
		TableUtils tUtils = new TableUtils(snmpSession, new DefaultPDUFactory());
		Target target = SnmpUtil.createReadTarget(this);
		List<TableEvent> tableOid = tUtils.getTable(target, columnOIDs, null, null);

		return tableOid;
	}

	public List<TableEvent> getTable(OID[] columnOIDs, OID lowerBoundIndex, OID upperBoundIndex)
			throws SnmpSessionException {
		if ((columnOIDs == null) || (columnOIDs.length == 0)) {
			LOGGER.error("No column OIDs specified");
			return null;
		}
		Snmp snmpSession = SnmpSessionManager.getInstance().getSession(SnmpSessionType.CLIENT);
		TableUtils tUtils = new TableUtils(snmpSession, new DefaultPDUFactory());
		Target target = SnmpUtil.createReadTarget(this);
		List<TableEvent> tableOid = tUtils.getTable(target, columnOIDs, lowerBoundIndex, upperBoundIndex);

		return tableOid;
	}

	private boolean checkResponse(ResponseEvent event) {
		boolean isChecked = false;
		if (event != null) {
			PDU pdu = event.getResponse();
			if (pdu != null) {
				int errorStatus = pdu.getErrorStatus();
				int errorIndex = pdu.getErrorIndex();
				if (errorStatus == PDU.noError) {
					isChecked = true;
				} else {
					String message = pdu.getErrorStatusText() != null && !pdu.getErrorStatusText().isEmpty()
							? pdu.getErrorStatusText()
							: "errorStatus= " + errorStatus + ", errorIndex=" + errorIndex;
					LOGGER.error("The response contains the error: " + message);
				}
			}
		} else {
			LOGGER.error("Response PDU is null");
		}
		return isChecked;
	}

	public void handle(PDU pdu) {
		LOGGER.debug("Client '" + clientName + "' received message: " + pdu);
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(clientName).append("[");
		result.append("clientId=").append(clientId).append(", ");
		result.append("address=").append(address).append(", ");
		result.append("readCommunity=").append(readCommunity).append(", ");
		result.append("writeCommunity=").append(writeCommunity).append(", ");
		result.append("requestPort=").append(requestPort).append(", ");
		result.append("trapPort=").append(trapPort).append("]");
		return result.toString();
	}

}
