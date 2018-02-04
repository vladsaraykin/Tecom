package app.snmp.exception;

public class SnmpRequestException extends Exception {

	private static final long serialVersionUID = -7728444178972983768L;

	public SnmpRequestException(String message) {
		super(message);
	}

	public SnmpRequestException(String message, Throwable cause) {
		super(message, cause);
	}

}
