import client.SnmpClient;
import moduls.Agent;
import moduls.NetworkPrinter;
import org.snmp4j.smi.OID;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws InterruptedException, IOException {

        SnmpClient snmpClient = new SnmpClient("127.0.0.1/7166");
        try {
            snmpClient.start();
            snmpClient.getRequest();
            snmpClient.getNextRequest();

        } catch (RuntimeException e) {
            e.getStackTrace();
        }finally {
            snmpClient.stop();
        }

    }
}
