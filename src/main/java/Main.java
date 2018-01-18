import client.SnmpClient;
import moduls.Agent;
import moduls.NetworkPrinter;
import org.snmp4j.smi.OID;

import java.io.IOException;
import java.util.ArrayList;

public class Main {

    public static final String OID1 = ".1.3.6.1.2.1.1.1";
    public static final String OID2 = ".1.3.6.1.2.1.1.7.0";
    public static final String IP_ADDRESS_PRINTER = "192.168.56.101/7166";


    public static void main(String[] args) throws InterruptedException, IOException {

        SnmpClient snmpClient = new SnmpClient("localhost/162");
        try {
            snmpClient.start();
            snmpClient.listen();
//            snmpClient.setRequest("1.3.6.1.2.1.1.5.0", "Printer HP Canon");
//            snmpClient.getNextRequest(OID1);
//            snmpClient.getRangeValues(OID1, OID2);
        } catch (RuntimeException e) {
            e.getStackTrace();
        } finally {
            snmpClient.stop();
        }

    }
}
