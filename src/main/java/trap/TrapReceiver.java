package trap;

import java.io.IOException;
import java.util.*;

import client.SnmpClient;
import main.SnmpSessionManager;
import main.SnmpSessionManager.SnmpSessionType;
import org.snmp4j.TransportMapping;

public class TrapReceiver {
    private Integer defaultListenPort = 162;
    private TrapReceiverListener listener;
    private Map<Integer, Set<SnmpClient>> clientsByListenPort = new HashMap<>();

    public Map<Integer, Set<SnmpClient>> getClientsByListenPort() {
        return clientsByListenPort;
    }

    public void start() throws IOException {

        listener = new TrapReceiverListener();
        listener.init(this);
        listener.addListener(defaultListenPort);

    }

    public void stop() throws IOException {

            if(clientsByListenPort.isEmpty()){
                System.out.println("Trap receiver is not run");
            }else{
                SnmpSessionManager.getInstance().stopSnmpSession(SnmpSessionType.TRAP_RECEIVER);
                clientsByListenPort.clear();
                System.out.println("Trap receiver is stopped");
            }




    }

    public void registerClient(SnmpClient client) throws IOException {
        listener.addListener(client.getListenPort());
        Set<SnmpClient> clients = clientsByListenPort.get(client.getListenPort());
        if (clients == null) {
            clients = new HashSet<>();
        }
        clients.add(client);
        clientsByListenPort.put(client.getListenPort(), clients);
    }

    public void unregisterClient(SnmpClient client) throws IOException {

        Set<SnmpClient> clients = clientsByListenPort.get(client.getListenPort());
        clients.remove(client);
        if (clients.isEmpty()) {
            try {
                listener.removeListener(client.getListenPort());
                clientsByListenPort.remove(client.getListenPort());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}
