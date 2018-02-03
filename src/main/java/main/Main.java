package main;

import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

import org.snmp4j.PDU;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.OID;

import client.SnmpClient;
import trap.TrapReceiver;

public class Main {
	private static final OID SYS_DESCR = new OID("1.3.6.1.2.1.1.1.0");
	private static final OID SYS_SERVICE = new OID("1.3.6.1.2.1.1.7.0");

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
			System.out.println("6 - Request client");
			System.out.println("7 - Exit");

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
					SnmpClient snmpClient = new SnmpClient(hostPort, listenPort, requestPort, readСommunity,
							writeCommunity);
					trapReceiver.registerClient(snmpClient);
					break;
				} else {
					System.out.println("Trap receiver isn't start!!!");
					break;
				}
			case 4:
				if (trapReceiver.isStarted()) {
					if(trapReceiver.getClientsByListenPort().size() != 0) {
						trapReceiver.showClients();
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
					}else {
						System.out.println("Client list is empty");
					}
					
				} else {
					System.out.println("Trap receiver isn't start");
				}

				break;
			case 5:
				if (trapReceiver.isStarted()) {
					System.out.println("Client list: ");
					trapReceiver.showClients();
				} else {
					System.out.println("Trap receiver isn't start");
				}

				break;
			case 6:
				if (trapReceiver.isStarted()) {
					if (!trapReceiver.getClientsByListenPort().isEmpty()) {
						System.out.println("Select client by#");
						trapReceiver.showClients();
						final int clientId = scanner.nextInt();
						for (Set<SnmpClient> clients : trapReceiver.getClientsByListenPort().values()) {
							clients.forEach(client -> {

								if (client.getClientId() == clientId) {

									System.out.println(" Select request");
									System.out.println("1 - GET");
									System.out.println("2 - GET NEXT");
									System.out.println("3 - GET RANGE VALUES");
									System.out.println("4 - SET");
									System.out.println("5 - Exit from requests menu");
									int numberOfRequest = scanner.nextInt();
									switch (numberOfRequest) {
									case 1:
										PDU pduGet = client.getRequest(SnmpConstants.sysDescr);
										if (pduGet != null) {
											System.out.println(pduGet.getVariableBindings());
										} else {
											System.out.println("Pdu is null");
										}
										break;
									case 2:
										PDU pduGetNext = client.getNextRequest(SYS_DESCR);
										if (pduGetNext != null) {
											System.out.println(pduGetNext.getVariableBindings());
										} else {
											System.out.println("Pdu is null");
										}
										break;
									case 3:
										List<PDU> pduList = client.getRangeValues(SYS_DESCR, SYS_SERVICE);
										for (int i = 0; i < pduList.size(); i++) {
											System.out.println(pduList.get(i));
										}
										break;
									case 4:
										System.out.println("Enter OID which you want change");
										String oid = scanner.next();
										System.out.println("Enter new variable for OID");
										String newVariable = scanner.next();
										client.setRequest(new OID(oid) , newVariable);
										break;
									case 5:
										break;
									default:
										System.out.println("Entered command unsupported");
										break;
									}

								}
							});
						}

					} else {
						System.out.println("Client list is empty");
					}
				} else {
					System.out.println("Trap receiver isn't start");
				}
				break;
			case 7:
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
		System.exit(0);
	}
}
