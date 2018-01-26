package main;

import java.io.IOException;
import java.util.Scanner;

import client.SnmpClient;
import trap.TrapReceiver;

public class Main {

	private static final String SYS_DESCR = "1.3.6.1.2.1.1.1.0";
	private static final String SYS_SERVICE = "1.3.6.1.2.1.1.7.0";
	private static final String IP_ADDRESS_PRINTER = "192.168.0.104/7166";
	private static final String IP_ADDRESS_RDC = "192.168.0.104/7167";
	private static final String IP_ADDRESS_LOCAL_PC = "192.168.0.106/162";
	public static final String PORT_TRAP = "162";

	public static void main(String[] args) throws InterruptedException, IOException {

		boolean flag = true;
		boolean statusTrapReceiver = false;
		SnmpSessionManager manager = SnmpSessionManager.getInstance();
		TrapReceiver trapReceiver = new TrapReceiver();

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
				if (!statusTrapReceiver) {
					trapReceiver.start();
					statusTrapReceiver = true;
				} else {
					System.out.println("Trap Receiver running");
				}
				break;
			case 2:
				trapReceiver.stop();
				statusTrapReceiver = false;
				break;
			case 3:
					System.out.println("Enter ip clients");
					String hostPort = scanner.next();
					System.out.println("Enter request port");
					Integer requestPort = scanner.nextInt();
					System.out.println("Enter port of trap");
					Integer listenPort = scanner.nextInt();
					System.out.println("Enter community this client");
					String community = scanner.next();
					SnmpClient snmpClient = new SnmpClient(hostPort, requestPort, listenPort,community);
					trapReceiver.registerClient(snmpClient);
					break;
			case 4:
				System.out.println("Select one of the available clients");
				System.out.println("1 - Hp printer");
				System.out.println("2 - RDC");
				select = scanner.nextInt();
				switch (select) {
				case 1:
//					trapReceiver.unregisterClient(snmpPrinter);
					break;
				case 2:
//					trapReceiver.unregisterClient(snmpRDC);
					break;
				}

				break;
			case 5:
				flag = false;
				trapReceiver.stop();
				System.out.println("Exit");
				break;
			default:
				System.out.println("Not found commands");
				break;
			}

		} while (flag);

	}
}
