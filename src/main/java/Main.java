import client.SnmpClient;
import moduls.Agent;
import moduls.NetworkPrinter;
import org.snmp4j.smi.OID;

import java.io.IOException;
import java.util.ArrayList;

public class Main {

    private static final String  SYS_DESCR = "1.3.6.1.2.1.1.1.0";
    private static final String  SYS_SERVICE = "1.3.6.1.2.1.1.7.0";
    private static final String IP_ADDRESS_PRINTER = "192.168.126.6/161";
    private static final String IP_ADDRESS_LOCAL_PC = "192.168.126.49/162";

    public static void main(String[] args) throws InterruptedException, IOException {
        OID sysDescr = new OID(SYS_DESCR);
        OID sysService = new OID(SYS_SERVICE);

        String address = args[0]+"/"+args[1];
		SnmpClient snmpClient = new SnmpClient("0.0.0.0/162");
        try {
            snmpClient.start();
            snmpClient.listen();
//            snmpClient.getRangeValues(sysDescr, sysService);
//            snmpClient.setRequest("1.3.6.1.2.1.1.5.0", "Printer HP Canon");
//            snmpClient.getNextRequest(SYS_DESCR);
        } catch (RuntimeException e) {
            e.getStackTrace();
        } finally {
            snmpClient.stop();
        }

    }
}
