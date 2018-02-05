package app.snmp;

import java.util.HashMap;
import java.util.Map;

import org.snmp4j.MessageDispatcher;
import org.snmp4j.MessageDispatcherImpl;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.mp.MPv1;
import org.snmp4j.mp.MPv2c;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.MultiThreadedMessageDispatcher;
import org.snmp4j.util.ThreadPool;

import app.snmp.exception.SnmpSessionException;

public class SnmpSessionManager {
	private static final String DEFAULT_TRAP_RECEIVER_THREAD_POLL_NAME = "Trap receiver pool";
	private static final int DEFAULT_TRAP_RECEIVER_THREAD_POLL_SIZE = 10;
	private static final String DEFAULT_CLIENT_THREAD_POLL_NAME = "Client pool";
	private static final int DEFAULT_CLIENT_THREAD_POLL_SIZE = 2;
	
	private static SnmpSessionManager INSTANCE;
	private Map<SnmpSessionType, Snmp> sessions = new HashMap<>();

	public static SnmpSessionManager getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new SnmpSessionManager();
			SecurityProtocols.getInstance().addDefaultProtocols();
		}
		return INSTANCE;
	}

	public enum SnmpSessionType {
		TRAP_RECEIVER, CLIENT
	}

	private MessageDispatcher createMessageDispatcher(String threadPoolName, int threadPoolSize) {
		ThreadPool threadPool = ThreadPool.create(threadPoolName, threadPoolSize);
		MessageDispatcher messageDispatcher = new MultiThreadedMessageDispatcher(
				threadPool, new MessageDispatcherImpl());
		messageDispatcher.addMessageProcessingModel(new MPv1());
		messageDispatcher.addMessageProcessingModel(new MPv2c());
		return messageDispatcher;
	}

	private Snmp createTrapReceiverSnmpSession(String threadPoolName, int threadPoolSize) {
		MessageDispatcher messageDispatcher = createMessageDispatcher(threadPoolName, threadPoolSize);
		Snmp session = new Snmp(messageDispatcher);
		return session;
	}

	private Snmp createClientSnmpSession(String threadPoolName, int threadPoolSize) throws SnmpSessionException {
		try {
			MessageDispatcher messageDispatcher = createMessageDispatcher(threadPoolName, threadPoolSize);
			TransportMapping<?> transport = new DefaultUdpTransportMapping();
			Snmp session = new Snmp(messageDispatcher, transport);
			return session;
		} catch (Exception e) {
			throw new SnmpSessionException("Failed to create client snmp session", e);
		}
	}

	public Snmp getSession(SnmpSessionType type) throws SnmpSessionException {
		if (!isSnmpSessionStarted(type)) {
			startSnmpSession(type);
		}
		Snmp session = sessions.get(type);
		return session;
	}

	public boolean isSnmpSessionStarted(SnmpSessionType type) {
		if (type == null) {
			throw new IllegalArgumentException("The argument 'type' must not be null");
		}
		
		Snmp session = sessions.get(type);
		return session != null ? true : false;
	}

	public void startSnmpSession(SnmpSessionType type) throws SnmpSessionException {
		if (type == null) {
			throw new IllegalArgumentException("The argument 'type' must not be null");
		}
		
		Snmp session = sessions.get(type);
		if (session == null) {
			switch (type) {
			case TRAP_RECEIVER:
				session = createTrapReceiverSnmpSession(DEFAULT_TRAP_RECEIVER_THREAD_POLL_NAME, DEFAULT_TRAP_RECEIVER_THREAD_POLL_SIZE);
				break;
			case CLIENT:
				session = createClientSnmpSession(DEFAULT_CLIENT_THREAD_POLL_NAME, DEFAULT_CLIENT_THREAD_POLL_SIZE);
				break;
			default:
				throw new IllegalArgumentException("Attempt to start an unsupported type session - " + type);
			}
			sessions.put(type, session);
		}
		
		try {
			session.listen();
		} catch (Exception e) {
			throw new SnmpSessionException("Failed to start '" + type + "' session", e);
		}
	}

	public void stopSnmpSession(SnmpSessionType type) throws SnmpSessionException {
		if (type == null) {
			throw new IllegalArgumentException("The argument 'type' must not be null");
		}
		
		if (isSnmpSessionStarted(type)) {
			Snmp session = sessions.get(type);
			try {
				session.close();
			} catch (Exception e) {
				throw new SnmpSessionException("Failed to stop '" + type + "' session", e);
			}
		}
	}

	public void stopAllSnmpSession() throws SnmpSessionException {
		for (SnmpSessionType type : sessions.keySet()) {
			stopSnmpSession(type);
		}
	}

}
