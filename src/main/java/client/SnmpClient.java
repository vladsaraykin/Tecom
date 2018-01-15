package client;

import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import java.io.IOException;
import java.util.ArrayList;

public class SnmpClient {

    private final static String SNMP_COMMUNITY = "public";
    private final static int SNMP_RETRIES = 3;
    private final static long SNMP_TIMEOUT = 1000L;

    private Snmp snmp = null;
    private String address = null;
    private TransportMapping transport = null;
    private static String oidValue = ".1.3.6.1.2.1.1.1.0";
    private static int snmpVersion = SnmpConstants.version1;
    private static PDU pdu = null;

    public SnmpClient(String address) {
        this.address = address;
    }

    public void start() throws IOException {
        transport = new DefaultUdpTransportMapping();
        transport.listen();
        snmp = new Snmp(transport);
        pdu = new PDU();
    }

    public void getRequest() throws IOException {
        PDU responsePDU = getResponseEvent().getResponse();
        System.out.println("\nResponse:\nGot Get Response from Agent...");
        checkError(responsePDU);
    }


    public void getNextRequest(ArrayList<String> oidValues) throws IOException {
        for (String value : oidValues) {
            pdu.add(new VariableBinding(new OID(value)));
        }
        pdu.setType(PDU.GETNEXT);
        ResponseEvent response = snmp.getNext(pdu, getTarget());
        if (response != null) {
            System.out.println("\nResponse:\nGot GetNext Response from Agent...");
            PDU responsePDU = response.getResponse();
            checkError(responsePDU);
        } else {
            System.out.println("Error: Agent Timeout... ");
        }
    }

    public void setRequest(String oidValue, String newVariable) throws IOException {
        OID oid = new OID(oidValue);
        Variable var = new OctetString(newVariable);
        VariableBinding variableBinding = new VariableBinding(oid, var);
        pdu.add(variableBinding);
        pdu.setType(PDU.SET);
        ResponseEvent response = snmp.set(pdu, getTarget());
        if (response != null) {
            System.out.println("\nResponse:\nGot Snmp Set Response from Agent");
            PDU responsePDU = response.getResponse();
            checkError(responsePDU);
        } else System.out.println("Error: Agent Timeout... ");
    }

    public ResponseEvent getResponseEvent() throws IOException {
        pdu.add(new VariableBinding(new OID(oidValue)));
        pdu.setType(PDU.GET);
        System.out.println("Sending Request to Agent");
        ResponseEvent response = snmp.get(pdu, getTarget());
        if (response != null) {
            return response;
        }
        throw new RuntimeException();
    }


    public Target getTarget() {
        Address targetAddress = GenericAddress.parse(address);
        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString(SNMP_COMMUNITY));
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

    public void stop() throws IOException {
        try {
            if (transport != null) {
                transport.close();
                transport = null;
            }
        } finally {
            if (snmp != null) {
                snmp.close();
                snmp = null;
            }
        }
    }

}