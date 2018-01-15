import client.SnmpClient;
import moduls.Agent;
import moduls.NetworkPrinter;
import org.snmp4j.smi.OID;

import java.io.IOException;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) throws InterruptedException, IOException {

        SnmpClient snmpClient = new SnmpClient("192.168.56.101/7166");
        ArrayList<String> oidsList = new ArrayList<>();
        oidsList.add(".1.3.6.1.2.1.1.1");
        oidsList.add(".1.3.6.1.2.1.1.2");
        oidsList.add(".1.3.6.1.2.1.1.3");
        oidsList.add(".1.3.6.1.2.1.1.4");
        oidsList.add(".1.3.6.1.2.1.1.5");
        oidsList.add(".1.3.6.1.2.1.1.6");
        oidsList.add(".1.3.6.1.2.1.1.7");
        try {
            snmpClient.start();
//            snmpClient.getRequest();
//            snmpClient.setRequest("1.3.6.1.2.1.1.1.0", "Printer HP Canon");
//            snmpClient.getRequest();
            snmpClient.getNextRequest(oidsList);
        } catch (RuntimeException e) {
            e.getStackTrace();
        }finally {
            snmpClient.stop();
        }

    }
}
