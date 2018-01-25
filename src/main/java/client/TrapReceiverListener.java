package client;

import org.snmp4j.*;
import org.snmp4j.mp.MPv1;
import org.snmp4j.mp.MPv2c;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.MultiThreadedMessageDispatcher;
import org.snmp4j.util.ThreadPool;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Collection;
import java.util.HashMap;

public class TrapReceiverListener {
    private Snmp snmpSession;
    private String listenHostAddress = "0.0.0.0";

    public void init(TrapReceiver trapReceiver) throws IOException {
        SnmpEventDispatcher snmpEventDispatcher = new SnmpEventDispatcher();
        ThreadPool threadPool = ThreadPool.create("Dispatcher pool", 10);
        MessageDispatcher mtDispatcher = new MultiThreadedMessageDispatcher(threadPool, new MessageDispatcherImpl());
        mtDispatcher.addMessageProcessingModel(new MPv1());
        mtDispatcher.addMessageProcessingModel(new MPv2c());
        SecurityProtocols.getInstance().addDefaultProtocols();
        snmpSession = new Snmp(mtDispatcher);

        snmpSession.addCommandResponder(snmpEventDispatcher);
    }

    public void addListener(Integer listenPort) throws IOException {
        UdpAddress addressListener = new UdpAddress(InetAddress.getByName(listenHostAddress), listenPort);
        if (snmpSession.getMessageDispatcher().getTransportMappings().size() == 0) {
            DefaultUdpTransportMapping transport = new DefaultUdpTransportMapping(addressListener);
            snmpSession.getMessageDispatcher().addTransportMapping(transport);
            transport.listen();
        }else {
            for (TransportMapping transportMapping : snmpSession.getMessageDispatcher().getTransportMappings()) {

                if (!(addressListener.toString().equals(transportMapping.getListenAddress().toString()))) {
                    DefaultUdpTransportMapping transport = new DefaultUdpTransportMapping(addressListener);
                    snmpSession.getMessageDispatcher().addTransportMapping(transport);
                    transport.listen();
                }
            }
        }
    }


    public void removeListener(Integer listenPort) throws IOException {
        UdpAddress addressListener = new UdpAddress(InetAddress.getByName(listenHostAddress), listenPort);
        Collection<TransportMapping> transportMappings = snmpSession.getMessageDispatcher().getTransportMappings();
        for (TransportMapping transport : transportMappings) {
            if (transport.getListenAddress().toString().equals(addressListener.toString())) {
                if (transport.isListening()) {
                    transport.close();
                    transportMappings.remove(transport);
                }
            } else {
                System.out.println("This port does not monitor traps");
            }
        }
    }

    public void start() throws IOException {
        snmpSession.listen();
    }

    public void close() throws IOException {
        if (snmpSession != null) {
            snmpSession.close();
        } else {
            System.out.println("Snmp session is close");
        }
    }
}
