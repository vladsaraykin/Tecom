package client;

import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TrapReceiver {
    private Integer defaultListenPort = 162;
    private TrapReceiverListener listener;
    Map<Integer, Set<SnmpClient>> clientsByListenPort;
    Set<SnmpClient> listClients;


    public void start() throws IOException {
        listener = new TrapReceiverListener();
        listener.init(this);
        listener.start();
        listener.addListener(defaultListenPort);
        clientsByListenPort = new HashMap<>();
        listClients = new HashSet<>();
    }

    public void stop() throws IOException {
        listener.close();
//      clean clentsByListener
    }

    public void registerClient(Integer listenPort, SnmpClient client) throws IOException {
        listener.addListener(listenPort);
        listClients.add(client);
        clientsByListenPort.put(listenPort, listClients);
    }

    public void unregisterClient(SnmpClient client) throws IOException {

    }

}
