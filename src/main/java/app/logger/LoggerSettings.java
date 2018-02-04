package app.logger;

public final class LoggerSettings {

	public static final LoggingLevel GENERIC_LEVEL = LoggingLevel.ALL;

	public static final LoggingLevel CLIENT_LEVEL = LoggingLevel.INFO;
	public static final String CLIENT_PACKAGE_NAME = "app.snmp.client";

	public static final LoggingLevel TRAP_RECEIVER_LEVEL = LoggingLevel.WARN;
	public static final String TRAP_RECEIVER_PACKAGE_NAME = "app.snmp.trap";

}
