package main;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

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
			System.out.println("5 - Client list");
			System.out.println("6 - Exit");

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
				if(statusTrapReceiver == true) {
					System.out.println("Enter ip clients");
					String hostPort = scanner.next();
					System.out.println("Enter request port");
					Integer requestPort = scanner.nextInt();
					System.out.println("Enter port of trap");
					Integer listenPort = scanner.nextInt();
					System.out.println("Enter community this client");
					String community = scanner.next();
					SnmpClient snmpClient = new SnmpClient(hostPort, listenPort, requestPort, community);
					trapReceiver.registerClient(snmpClient);
					break;
				}else{
					System.out.println("Trap receiver isn't start!!!");
					break;
				}
			case 4:
				System.out.println("Enter client id");
				int clientId = scanner.nextInt();
				for (Set<SnmpClient> clients : trapReceiver.getClientsByListenPort().values()) {
					clients.forEach(client -> {
						if (client.getClientId() == clientId) {
							try {
								trapReceiver.unregisterClient(client);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					});
				}
				break;
			case 5: 
				System.out.println("Client list: ");
				for(Entry<Integer, Set<SnmpClient>> clientsByListenPort : trapReceiver.getClientsByListenPort().entrySet()) {
					System.out.println("  Listen port " + clientsByListenPort.getKey() + ":");
					for(SnmpClient client : clientsByListenPort.getValue()) {
						System.out.println("    Client #" + client.getClientId());
						System.out.println("    IP address: " + client.getAddress());
						System.out.println("    Community: " + client.getCommunity());
					}
				}
				
				break;
			case 6:
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
