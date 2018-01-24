package client;

import org.snmp4j.*;
import org.snmp4j.mp.MPv1;
import org.snmp4j.mp.MPv2c;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.MultiThreadedMessageDispatcher;
import org.snmp4j.util.ThreadPool;

import java.io.IOException;

import java.util.HashMap;
import java.util.Map;

public class Dispatcher implements CommandResponder {
    private final String READ_COMMUNITY = "public";

    private Snmp snmp = null;
    private TransportMapping transport = null;
    private MessageDispatcher mtDispatcher;
    private CommunityTarget target;
    private String address;
    private Map<SnmpClient, String> clientList;

   

    public Dispatcher(String address) {
        this.address = address;
    }

    public void init() throws IOException {
        transport = new DefaultUdpTransportMapping((UdpAddress) GenericAddress.parse(address));
        ThreadPool threadPool = ThreadPool.create("Dispatcher poll", 10);
        mtDispatcher = new MultiThreadedMessageDispatcher(threadPool, new MessageDispatcherImpl());
        mtDispatcher.addMessageProcessingModel(new MPv1());
        mtDispatcher.addMessageProcessingModel(new MPv2c());
        SecurityProtocols.getInstance().addDefaultProtocols();
        target = new CommunityTarget();
        target.setVersion(SnmpConstants.version2c);
        target.setCommunity(new OctetString(READ_COMMUNITY));
        snmp = new Snmp(mtDispatcher, transport);
        snmp.addCommandResponder(this);
        clientList = new HashMap<>();
    }

    public void register(SnmpClient snmpClient,String port){
        clientList.put(snmpClient, port);
     }

    public synchronized void listen() {
        try {
            System.out.println("Listening to address " + address);
            transport.listen();

        } catch (IOException e) {
            System.err.println("Unable to listen");
            e.printStackTrace();
        }

        try {
            this.wait();
        } catch (InterruptedException e) {
            System.err.println("Interrupted while waiting for traps: " + e);
        }

    }

    @Override
    public synchronized void processPdu(CommandResponderEvent cmdResponderEvent) {
        System.out.println("Received PDU...");
        PDU pdu = cmdResponderEvent.getPDU();
        Address address = cmdResponderEvent.getPeerAddress();
        if (pdu != null) {
            System.out.println("Trap ip is " + address);
            System.out.println("Type community traps is " + new String(cmdResponderEvent.getSecurityName()));
        } else {
            System.out.println("PDU is null");
        }

        for (SnmpClient snmpClient : clientList.keySet()) {
            if (address.toString().contains(snmpClient.getAddress())) {
                snmpClient.handle(pdu);
            }
        }

        
    }

    public void close() throws IOException {
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
