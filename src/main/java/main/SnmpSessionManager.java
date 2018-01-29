package main;

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

public class SnmpSessionManager {
    private static SnmpSessionManager INSTANCE;
    private Map<SnmpSessionType, Snmp> sessions = new HashMap<>();

    public static SnmpSessionManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SnmpSessionManager();
        }
        return INSTANCE;
    }

    public enum SnmpSessionType {
        TRAP_RECEIVER, CLIENT
    }

    public Snmp getSession(SnmpSessionType type) {
        Snmp session = sessions.get(type);
        if (session == null) {
            switch (type) {
                case TRAP_RECEIVER:
                    session = createSnmpSession();
                    break;
                case CLIENT:
                    session = createSnmpSession();
                    break;
                default:
                    System.out.println("Request snmp session for uknown type " + type);
                    break;
            }
            sessions.put(type, session);
        }
        return session;

    }

    private Snmp createSnmpSession() {
        Snmp session = null;
        try {
            ThreadPool threadPool = ThreadPool.create("Dispatcher pool", 10);
            MessageDispatcher mtDispatcher = new MultiThreadedMessageDispatcher(threadPool,
                    new MessageDispatcherImpl());
            mtDispatcher.addMessageProcessingModel(new MPv1());
            mtDispatcher.addMessageProcessingModel(new MPv2c());
            SecurityProtocols.getInstance().addDefaultProtocols();
            session = new Snmp(mtDispatcher);

        } catch (Exception e) {
            System.out.println("filed init snmp session");
        }

        return session;
    }
    public void startSnmpSession(SnmpSessionType type) {
    	try {
        	getSession(type).listen();

    	}
    	catch(Exception e) {
    		e.printStackTrace();
    	}
    }
    public void stopSnmpSession(SnmpSessionType type) {
    	try {
    		getSession(type).close();
    	}
    	catch (Exception e) {
			e.printStackTrace();
		}
    }

}
