package app;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import org.snmp4j.PDU;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;

import app.logger.ConsoleLogger;
import app.logger.Logger;
import app.snmp.SnmpSessionManager;
import app.snmp.client.SnmpClient;
import app.snmp.trap.TrapReceiver;

public class Application {

	private static final Logger LOGGER = new ConsoleLogger(Application.class);

	public static void main(String[] args) throws InterruptedException, IOException {
		TrapReceiver trapReceiver = new TrapReceiver();
		Scanner scanner = new Scanner(System.in);
		boolean statusProgramm = true;
		do {
			System.out.println();
			printMenu();
			System.out.println();
			
			System.out.print("Please select menu item: ");
			int menuCommandSelected = scanner.nextInt();
			System.out.println();
			try {
				switch (menuCommandSelected) {
				case 1:
					trapReceiver.start();
					break;
				case 2:
					trapReceiver.stop();
					break;
				case 3:
					System.out.print("Enter name: ");
					String clientName = scanner.next();
					System.out.print("Enter ip address: ");
					String address = scanner.next();
					System.out.print("Enter read community: ");
					String readСommunity = scanner.next();
					System.out.print("Enter write community: ");
					String writeCommunity = scanner.next();
					System.out.print("Enter request port: ");
					Integer requestPort = scanner.nextInt();
					System.out.print("Enter trap port: ");
					Integer listenPort = scanner.nextInt();
					System.out.println();
					SnmpClient snmpClient = new SnmpClient(address, readСommunity, writeCommunity, requestPort, listenPort, clientName);
					trapReceiver.registerClient(snmpClient);
					break;
				case 4:
					if (trapReceiver.isStarted() && !trapReceiver.getClientsByListenPort().isEmpty()) {
						Set<SnmpClient> allClients = new HashSet<>();
						for (Set<SnmpClient> clients : trapReceiver.getClientsByListenPort().values()) {
							allClients.addAll(clients);
						}
						printClients(allClients);
						System.out.println();
						
						System.out.print("Enter client id: ");
						int clientId = scanner.nextInt();
						SnmpClient targetClient = null;
						for (SnmpClient client : allClients) {
							if (client.getClientId() == clientId) {
								targetClient = client;
								break;
							}
						}
						
						if (targetClient != null) {
							try {
								trapReceiver.unregisterClient(targetClient);
							} catch (Exception e) {
								LOGGER.error("Failed to unregister client '" + targetClient.getClientName() + "'", e);
							}
						} else {
							LOGGER.warn("Client with id=" + clientId + " not found");
						}
					} else {
						LOGGER.warn("The list of registered clients is empty");
					}
					break;
				case 5:
					if (trapReceiver.isStarted() && !trapReceiver.getClientsByListenPort().isEmpty()) {
						Set<SnmpClient> allClients = new HashSet<>();
						for (Set<SnmpClient> clients : trapReceiver.getClientsByListenPort().values()) {
							allClients.addAll(clients);
						}
						printClients(allClients);
					}
					break;
				case 6:
					if (trapReceiver.isStarted() && !trapReceiver.getClientsByListenPort().isEmpty()) {
						Set<SnmpClient> allClients = new HashSet<>();
						for (Set<SnmpClient> clients : trapReceiver.getClientsByListenPort().values()) {
							allClients.addAll(clients);
						}
						printClients(allClients);
						System.out.println();
						
						System.out.print("Enter client id: ");
						int clientId = scanner.nextInt();
						SnmpClient targetClient = null;
						for (SnmpClient client : allClients) {
							if (client.getClientId() == clientId) {
								targetClient = client;
								break;
							}
						}
						
						if (targetClient != null) {
							System.out.print("Enter command (1 -> GET, 2 -> GET-NEXT, 3 -> WALK, 4 -> SET, 5 -> EXIT): ");
							int clientCommandSelected = scanner.nextInt();
							try {
								switch (clientCommandSelected) {
								case 1:
									System.out.print("Enter oid: ");
									String getOid = scanner.next();
									System.out.println();
									PDU getResponse = targetClient.getRequest(new OID(getOid));
									VariableBinding getResponseVB = getResponse.getVariableBindings().get(0);
									System.out.println("The result of the GET request:");
									System.out.println("    " + getResponseVB.getOid().toDottedString() + "=" + getResponseVB.getVariable());
									break;
								case 2:
									System.out.print("Enter oid: ");
									String getNextOid = scanner.next();
									System.out.println();
									PDU getNextResponse = targetClient.getNextRequest(new OID(getNextOid));
									VariableBinding getNextResponseVB = getNextResponse.getVariableBindings().get(0);
									System.out.println("The result of the GET-NEXT request:");
									System.out.println("    " + getNextResponseVB.getOid().toDottedString() + "=" + getNextResponseVB.getVariable());
									break;
								case 3:
									System.out.print("Enter first oid: ");
									String firstOid = scanner.next();
									System.out.print("Enter last oid: ");
									String lastOid = scanner.next();
									System.out.println();
									List<PDU> walkResponses = targetClient.walkRequest(new OID(firstOid), new OID(lastOid));
									System.out.println("The result of the WALK request:");
									for (PDU response : walkResponses) {
										VariableBinding responseVB = response.getVariableBindings().get(0);
										System.out.println("    " + responseVB.getOid().toDottedString() + "=" + responseVB.getVariable());
									}
									break;
								case 4:
									System.out.print("Enter oid: ");
									String oid = scanner.next();
									System.out.print("Enter new value: ");
									String newValue = scanner.next();
									System.out.println();
									Variable variable = new OctetString(newValue);
									targetClient.setRequest(new OID(oid), variable);
									break;
								case 5:
								default:
									break;
								}
							} catch (Exception e) {
								
							}
						} else {
							LOGGER.warn("Client with id=" + clientId + " not found");
						}
					} else {
						LOGGER.warn("The list of registered clients is empty");
					}
					break;
				case 7:
					if (trapReceiver.isStarted()) {
						trapReceiver.stop();
					}
					SnmpSessionManager.getInstance().stopAllSnmpSession();
					statusProgramm = false;
					break;
				default:
					break;
				}
			} catch (Exception e) {
				LOGGER.error("Failed to execute the command " + menuCommandSelected, e);
			}
		} while (statusProgramm);
		
		scanner.close();
		System.exit(0);
	}

	private static void printMenu() {
		System.out.println("#####################################");
		System.out.println("#  1 - Start Trap Receiver          #");
		System.out.println("#  2 - Stop Trap Receiver           #");
		System.out.println("#  3 - Add client                   #");
		System.out.println("#  4 - Remove client                #");
		System.out.println("#  5 - View clients                 #");
		System.out.println("#  6 - Commands for client          #");
		System.out.println("#  7 - Exit                         #");
		System.out.println("#####################################");
	}

	private static void printClients(Set<SnmpClient> clients) {
		for (SnmpClient client : clients) {
			System.out.println(client);
		}
	}

}
