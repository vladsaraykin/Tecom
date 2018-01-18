package client;

import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.log.LogFactory;
import org.snmp4j.mp.*;
import org.snmp4j.security.Priv3DES;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.smi.*;
import org.snmp4j.tools.console.SnmpRequest;
import org.snmp4j.transport.AbstractTransportMapping;
import org.snmp4j.transport.DefaultTcpTransportMapping;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.MultiThreadedMessageDispatcher;
import org.snmp4j.util.ThreadPool;

import java.io.IOException;
import java.util.Date;

public class SnmpClient implements CommandResponder {

    private final static String WRITE_COMMUNITY = "private";
    private final static String READ_COMMUNITY = "public";
    private final static int SNMP_RETRIES = 3;
    private final static long SNMP_TIMEOUT = 1000L;

    private Snmp snmp = null;
    private String address = null;
    private TransportMapping transport = null;
    private static int snmpVersion = SnmpConstants.version2c;
    private MessageDispatcher mtDispatcher;

    public SnmpClient(String address) {
        this.address = address;
    }

    public void start() throws IOException {

        transport = new DefaultUdpTransportMapping(new UdpAddress(address));
        snmp = new Snmp(createMessageDispatcher(), transport);
        snmp.addCommandResponder(this);
        transport.listen();
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
        ResponseEvent response = snmp.getNext(pdu, getTarget(READ_COMMUNITY));
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

        if(!response.getResponse().getVariable(oidValue1).toString().contains("noSuchObject")) {
            System.out.println(response.getResponse().get(0));
            while (true) {
                response = getNextRequest(oidValue1.toDottedString());
                OID next = response.getResponse().get(0).getOid();
                if (!oidValue2.equals(next)) {
                    oidValue1 = next;
                } else
                    break;
            }
        }else
         System.out.println("This number oid: " + oidValue1 + " isn't in the table");

    }

    public void setRequest(String oidValue, String newVariable) throws IOException {
        OID oid = new OID(oidValue);
        Variable var = new OctetString(newVariable);
        VariableBinding variableBinding = new VariableBinding(oid, var);
        PDU pdu = new PDU();
        pdu.add(variableBinding);
        pdu.setType(PDU.SET);
        ResponseEvent response = snmp.set(pdu, getTarget(WRITE_COMMUNITY));
        if (response != null) {
            System.out.println("\nResponse:\nGot Snmp Set Response from Agent");
            PDU responsePDU = response.getResponse();
            checkError(responsePDU);
        } else System.out.println("Error: Agent Timeout... ");
    }

    public ResponseEvent getResponseEvent(OID oidValue) throws IOException {
        PDU pdu = new PDU();
        pdu.add(new VariableBinding(oidValue));
        pdu.setType(PDU.GET);
        System.out.println("Sending Request to Agent");
        ResponseEvent response = snmp.get(pdu, getTarget(READ_COMMUNITY));
        if (response != null) {
            return response;
        }
        throw new RuntimeException();
    }


    public Target getTarget(String community) {
        Address targetAddress = GenericAddress.parse(address);
        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString(community));
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

    public synchronized void listen() {

        SecurityProtocols.getInstance().addDefaultProtocols();
        SecurityProtocols.getInstance().addPrivacyProtocol(new Priv3DES());
        System.out.println("Listening on " + address);
        try {
            this.wait();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private MessageDispatcher createMessageDispatcher() {
        ThreadPool threadPool = ThreadPool.create("DispatcherPooll", 10);
        mtDispatcher = new MultiThreadedMessageDispatcher(threadPool, new MessageDispatcherImpl());
        mtDispatcher.addMessageProcessingModel(new MPv1());
        mtDispatcher.addMessageProcessingModel(new MPv2c());
        return mtDispatcher;
    }

    @Override
    public void processPdu(CommandResponderEvent commandResponderEvent) {
        System.out.println("Received PDU...");
        PDU pdu = commandResponderEvent.getPDU();
        if (pdu != null) {
            System.out.println("Trap type = " + pdu.getType());
            System.out.println("Variable bindings = " + pdu.getVariableBindings());
            int pduType = pdu.getType();
            if ((pduType != PDU.TRAP) && (pduType != PDU.V1TRAP) && (pduType != PDU.REPORT)
                    && (pduType != PDU.RESPONSE)) {
                pdu.setErrorIndex(0);
                pdu.setErrorStatus(0);
                pdu.setType(PDU.RESPONSE);
                StatusInformation statusInformation = new StatusInformation();
                StateReference ref = commandResponderEvent.getStateReference();
                try {
                    System.out.println(commandResponderEvent.getPDU());
                    commandResponderEvent.getMessageDispatcher().returnResponsePdu(
                            commandResponderEvent.getMessageProcessingModel(),
                            commandResponderEvent.getSecurityModel(),
                            commandResponderEvent.getSecurityName(),
                            commandResponderEvent.getSecurityLevel(),
                            pdu, commandResponderEvent.getMaxSizeResponsePDU(),
                            ref, statusInformation);

                } catch (MessageException e) {
                    System.err.println(e.getMessage());
                    LogFactory.getLogger(SnmpRequest.class).error(e);
                }
            }

        }
    }
}
