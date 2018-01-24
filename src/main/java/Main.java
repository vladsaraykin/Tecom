import client.Dispatcher;
import client.SnmpClient;
import client.TrapReceiver;
import moduls.Agent;
import moduls.NetworkPrinter;
import org.snmp4j.smi.OID;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {

	private static final String SYS_DESCR = "1.3.6.1.2.1.1.1.0";
	private static final String SYS_SERVICE = "1.3.6.1.2.1.1.7.0";
	private static final String IP_ADDRESS_PRINTER = "192.168.0.104/7166";
	private static final String IP_ADDRESS_RDC = "192.168.0.104/7167";
	private static final String IP_ADDRESS_LOCAL_PC = "192.168.0.106/162";
	public static final String PORT_TRAP = "162";

	public static void main(String[] args) throws InterruptedException, IOException {

		OID sysDescr = new OID(SYS_DESCR);
		OID sysService = new OID(SYS_SERVICE);
		boolean flag = true;
		TrapReceiver trapReceiver = new TrapReceiver();
		Dispatcher dispatcher = new Dispatcher(args[0] + "/" + args[1]);
		SnmpClient snmpPrinter = new SnmpClient(IP_ADDRESS_PRINTER);
		SnmpClient snmpRDC = new SnmpClient(IP_ADDRESS_RDC);

		do {
			System.out.println("Please select menu item");
			System.out.println("1 - Start Trap Receiver");
			System.out.println("2 - Stop Trap Receiver");
			System.out.println("3 - Add client");
			System.out.println("4 - Remove client");
			System.out.println("5 - exit");

			Scanner scanner = new Scanner(System.in);
			int select = scanner.nextInt();

			switch (select) {
			case 1:
				trapReceiver.start();
				break;
			case 2:
				break;
			case 3:
				if (trapReceiver != null) {
					System.out.println("Select one of the available clients");
					System.out.println("1 - Hp printer");
					System.out.println("2 - RDC");
					select = scanner.nextInt();
					switch (select) {
					case 1:
						trapReceiver.registerClient(snmpPrinter);
						break;
					case 2:
						trapReceiver.registerClient(snmpRDC);
						break;
					}
				} else {
					System.out.println("Run Trap Receiver");
				}

				break;
			case 4:
				System.out.println("Select one of the available clients");
				System.out.println("1 - Hp printer");
				System.out.println("2 - RDC");
				select = scanner.nextInt();
				switch (select) {
				case 1:
					trapReceiver.unregisterClient(snmpPrinter);
					break;
				case 2:
					trapReceiver.unregisterClient(snmpRDC);
					break;
				}
				
				break;
			case 5:
				flag = false;
				trapReceiver.stop();
				break;
			default:
				System.out.println("Select");
			}

		} while (flag);

		try {
			dispatcher.init();
			dispatcher.register(snmpPrinter, PORT_TRAP);
			dispatcher.register(snmpRDC, PORT_TRAP);
			snmpPrinter.start();
			snmpRDC.start();
			dispatcher.listen();
			// snmpClient.getRangeValues(sysDescr, sysService);
			// snmpClient.setRequest("1.3.6.1.2.1.1.5.0", "Printer HP Canon");
			// snmpClient.getNextRequest(SYS_DESCR);
		} catch (RuntimeException e) {
			e.getStackTrace();
		} finally {
			snmpPrinter.stop();
			snmpRDC.stop();
			dispatcher.close();
		}

	}
}
