package app.snmp.exception;

public class SnmpSessionException extends Exception {

	private static final long serialVersionUID = -8068754151871775683L;

	public SnmpSessionException(String message) {
		super(message);
	}

	public SnmpSessionException(String message, Throwable e) {
		super(message, e);
	}

}
