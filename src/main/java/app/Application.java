package app;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.snmp4j.PDU;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.util.TableEvent;

import app.logger.ConsoleLogger;
import app.logger.Logger;
import app.snmp.SnmpSessionManager;
import app.snmp.client.SnmpClient;
import app.snmp.exception.SnmpSessionException;
import app.snmp.trap.TrapReceiver;

public class Application {

	private static final Logger LOGGER = new ConsoleLogger(Application.class);
	private static Map<Integer, SnmpClient> snmpClients = new HashMap<>();

	public static void main(String[] args) {
		TrapReceiver trapReceiver = new TrapReceiver();
		try {
			trapReceiver.start();
		} catch (Exception e1) {
			LOGGER.error(e1.getMessage(), e1);
			System.exit(0);

		}

		boolean statusProgramm = true;
		ConsoleScanner scanner = new ConsoleScanner();
		do {

			System.out.println();
			printMenu();
			System.out.println();

			System.out.print("Please select menu item: ");
			Integer menuCommandSelected = scanner.nextInt();
			scanner.nextLine();
			if (menuCommandSelected == null) {
				System.out.println("Enter correct value");
				continue;
			}
			try {
				switch (menuCommandSelected) {
				case 1:
					addClient(trapReceiver, scanner);
					break;
				case 2:
					removeClient(trapReceiver, scanner);
					break;
				case 3:
					showClients();
					break;
				case 4:
					clientsCommands(trapReceiver, scanner);
					break;
				case 5:
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

		System.exit(0);

	}

	private static void clientsCommands(TrapReceiver trapReceiver, ConsoleScanner scanner) {
		if (!snmpClients.isEmpty()) {
			showClients();
			System.out.println();

			System.out.print("Enter client id: ");
			Integer clientId = scanner.nextInt();
			SnmpClient targetClient = snmpClients.get(clientId);
			boolean statusCommands = true;
			do {
				if (targetClient != null) {
					System.out.print(
							"Enter command (1 -> GET, 2 -> GET-NEXT, 3 -> WALK, 4 -> SET, 5 - GET TABLE, 6 -> EXIT): ");
					Integer clientCommandSelected = scanner.nextInt();
					scanner.nextLine();
					if (clientCommandSelected == null) {
						do {
							System.out.print(
									"Enter command (1 -> GET, 2 -> GET-NEXT, 3 -> WALK, 4 -> SET, 5 - GET TABLE, 6 -> EXIT): ");
							scanner.nextLine();
							Integer correctValue = scanner.nextInt();

							if (correctValue != null) {
								clientCommandSelected = correctValue;
								break;
							}
						} while (true);
					}
					try {
						switch (clientCommandSelected) {
						case 1:
							System.out.print("Enter oid: ");
							String getOid = scanner.next();
							System.out.println();
							PDU getResponse = targetClient.getRequest(new OID(getOid));
							VariableBinding getResponseVB = getResponse.getVariableBindings().get(0);
							System.out.println("The result of the GET request:");
							System.out.println("    " + getResponseVB.getOid().toDottedString() + "="
									+ getResponseVB.getVariable());
							break;
						case 2:
							System.out.print("Enter oid: ");
							String getNextOid = scanner.next();
							System.out.println();
							PDU getNextResponse = targetClient.getNextRequest(new OID(getNextOid));
							VariableBinding getNextResponseVB = getNextResponse.getVariableBindings().get(0);
							System.out.println("The result of the GET-NEXT request:");
							System.out.println("    " + getNextResponseVB.getOid().toDottedString() + "="
									+ getNextResponseVB.getVariable());
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
								System.out.println(
										"    " + responseVB.getOid().toDottedString() + "=" + responseVB.getVariable());
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
							// get table
							ArrayList<OID> tmpOIDs = new ArrayList<>();
							System.out.print("Enter the OID table (q for exit)");
							do {
								String oidArr = scanner.next();
								scanner.nextLine();
								if (oidArr.contains("q")) {
									break;
								}
								if (oidArr != null) {
									try {
										OID correctOid = new OID(oidArr);
										if (!correctOid.isException()) {
											tmpOIDs.add(correctOid);
										}
									} catch (Exception ex) {
										System.out.println(oidArr + " isn't valid");
									}
								}

							} while (true);
							OID[] oids = new OID[tmpOIDs.size()];
							for (int i = 0; i < tmpOIDs.size(); i++) {
								oids[i] = tmpOIDs.get(i);
							}
							System.out.println("Do you want to specify the upper and lower bounds of the table? (y/n)");
							boolean isBounds = false;
							String upperBound = null;
							String lowerBound = null;
							do {
								String answer = scanner.next();
								if (answer.contains("y")) {
									System.out.println("Enter upper and lower bounds of the table");
									System.out.print("Upper bound: ");
									upperBound = scanner.next();
									System.out.print("Lower bound: ");
									lowerBound = scanner.next();
									isBounds = true;
									break;
								}
								if (answer.contains("n")) {
									break;
								} else {
									System.out.println("Enter correct value");
								}
							} while (true);
							List<TableEvent> tblEvent = null;
							if (!isBounds) {
								tblEvent = targetClient.getTable(oids);
							} else {
								tblEvent = targetClient.getTable(oids, new OID(lowerBound), new OID(upperBound));
							}
							if (tblEvent != null) {
								for (TableEvent event : tblEvent) {
									if (event.isError()) {
										LOGGER.debug("SNMP event error: " + event.getErrorMessage());
										continue;
									}
									for (VariableBinding vb : event.getColumns()) {
										String key = vb.getOid().toString();
										String value = vb.getVariable().toString();
										System.out.print(key + " = " + value);
										System.out.println();
									}
								}
							} else {
								System.out.println("Incorrect values");
							}
						case 6:
							statusCommands = false;
						default:
							break;
						}
					} catch (Exception e) {

					}
				} else {
					LOGGER.warn("Client with id=" + clientId + " not found");
					break;
				}
			} while (statusCommands);
		} else {
			System.out.println("No register clients");
		}
	}

	private static void showClients() {
		for (SnmpClient client : snmpClients.values()) {
			System.out.println(client);
		}

	}

	private static void removeClient(TrapReceiver trapReceiver, ConsoleScanner scanner) {
		if (!snmpClients.isEmpty()) {
			showClients();
			System.out.println();

			System.out.print("Enter client id: ");
			Integer clientId = scanner.nextInt();
			if (clientId != null) {
				SnmpClient targetClient = snmpClients.get(clientId);
				if (targetClient != null) {
					try {
						trapReceiver.unregisterClient(targetClient);
						snmpClients.remove(targetClient.getClientId());
					} catch (Exception e) {
						LOGGER.error("Failed to unregister client '" + targetClient.getClientName() + "'", e);
					}
				} else {
					LOGGER.warn("Client with id=" + clientId + " not found");
				}

			} else {
				System.out.println("Enter correct value");
			}
		} else {
			System.out.println("No register client");
		}

	}

	private static void addClient(TrapReceiver trapReceiver, ConsoleScanner scanner)
			throws SnmpSessionException, IOException {

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
		scanner.nextLine();
		if (requestPort == null) {
			do {
				System.out.println("Enter correct value for the request port");
				Integer correctValue;
				correctValue = scanner.nextInt();
				scanner.nextLine();
				if (correctValue != null) {
					requestPort = correctValue;
					break;
				}

			} while (true);
		}
		System.out.println();
		System.out.print("Enter trap port: ");
		Integer listenPort = scanner.nextInt();
		scanner.nextLine();
		if (listenPort == null) {
			do {
				System.out.println("Enter correct value for the trap port");
				scanner.nextLine();
				Integer correctValue;
				correctValue = scanner.nextInt();
				if (correctValue != null) {
					listenPort = correctValue;
					break;
				}
			} while (true);
		}
		System.out.println();
		SnmpClient snmpClient = new SnmpClient(address, readСommunity, writeCommunity, requestPort, listenPort,
				clientName);
		snmpClients.put(snmpClient.getClientId(), snmpClient);
		trapReceiver.registerClient(snmpClient);

	}

	private static void printMenu() {
		System.out.println("#####################################");
		System.out.println("#  1 - Add client                   #");
		System.out.println("#  2 - Remove client                #");
		System.out.println("#  3 - View clients                 #");
		System.out.println("#  4 - Commands for client          #");
		System.out.println("#  5 - Exit                         #");
		System.out.println("#####################################");
	}

}
