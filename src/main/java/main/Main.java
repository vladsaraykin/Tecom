package main;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

import client.SnmpClient;
import trap.TrapReceiver;

public class Main {

	public static void main(String[] args) throws InterruptedException, IOException {

		boolean statusProgramm = true;

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
				if (!trapReceiver.isStarted()) {
					trapReceiver.start();
				} else {
					System.out.println("Trap Receiver running");
				}
				break;
			case 2:
				trapReceiver.stop();

				break;
			case 3:
				if (trapReceiver.isStarted()) {
					System.out.println("Enter ip clients");
					String hostPort = scanner.next();
					System.out.println("Enter request port");
					Integer requestPort = scanner.nextInt();
					System.out.println("Enter port of trap");
					Integer listenPort = scanner.nextInt();
					System.out.println("Enter read community this client");
					String readСommunity = scanner.next();
					System.out.println("Enter write community this client");
					String writeCommunity = scanner.next();
					SnmpClient snmpClient = new SnmpClient(hostPort, listenPort, requestPort, readСommunity, writeCommunity);
					trapReceiver.registerClient(snmpClient);
					break;
				} else {
					System.out.println("Trap receiver isn't start!!!");
					break;
				}
			case 4:
				if (trapReceiver.isStarted()) {
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
				} else {
					System.out.println("Trap receiver isn't start");
				}

				break;
			case 5:
				if (trapReceiver.isStarted()) {
					System.out.println("Client list: ");
					for (Entry<Integer, Set<SnmpClient>> clientsByListenPort : trapReceiver.getClientsByListenPort()
							.entrySet()) {
						System.out.println("  Listen port " + clientsByListenPort.getKey() + ":");
						for (SnmpClient client : clientsByListenPort.getValue()) {
							System.out.println("    Client #" + client.getClientId());
							System.out.println("    IP address: " + client.getAddress());
							System.out.println("    Community: " + client.getCommunity());
						}
					}
				} else {
					System.out.println("Trap receiver isn't start");
				}

				break;
			case 6:
				if (trapReceiver != null) {
					if (trapReceiver.isStarted()) {
						trapReceiver.stop();
					}
					
				}
				SnmpSessionManager.getInstance().stopSnmpSession(SnmpSessionManager.SnmpSessionType.CLIENT);
				statusProgramm = false;
				System.out.println("Exit");
				break;
			default:
				System.out.println("Entered command unsupported");
				break;

			}

		} while (statusProgramm);

	}
}
