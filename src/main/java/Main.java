import client.Dispatcher;
import client.SnmpClient;
import moduls.Agent;
import moduls.NetworkPrinter;
import org.snmp4j.smi.OID;

import java.io.IOException;
import java.util.ArrayList;

public class Main {

    private static final String  SYS_DESCR = "1.3.6.1.2.1.1.1.0";
    private static final String  SYS_SERVICE = "1.3.6.1.2.1.1.7.0";

    private static final String IP_ADDRESS_PRINTER = "192.168.0.104/7166";
    private static final String IP_ADDRESS_RDC = "192.168.0.104/7167";
    private static final String IP_ADDRESS_LOCAL_PC = "192.168.0.106/162";


    public static void main(String[] args) throws InterruptedException, IOException {
        OID sysDescr = new OID(SYS_DESCR);
        OID sysService = new OID(SYS_SERVICE);

        Dispatcher dispatcher = new Dispatcher(IP_ADDRESS_LOCAL_PC);



        try {
            dispatcher.init();
            dispatcher.register(IP_ADDRESS_PRINTER);
            dispatcher.register(IP_ADDRESS_RDC);
            dispatcher.getClientList().get(0).getRequest(sysDescr);
            dispatcher.getClientList().get(1).getRangeValues(sysDescr, sysService);
            dispatcher.listen();
//            snmpClient.getRangeValues(sysDescr, sysService);
//            snmpClient.setRequest("1.3.6.1.2.1.1.5.0", "Printer HP Canon");
//            snmpClient.getNextRequest(SYS_DESCR);
        } catch (RuntimeException e) {
            e.getStackTrace();
        } finally {
            dispatcher.getClientList().get(0).stop();
            dispatcher.getClientList().get(1).stop();
        }

    }
}
