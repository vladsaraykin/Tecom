package app.logger;

import java.time.OffsetDateTime;

public abstract class Logger {

	protected Class<?> clazz;
	protected LoggingLevel level;

	public Logger(Class<?> clazz) {
		super();
		
		String packageName = clazz.getPackage().getName();
		LoggingLevel level = LoggerSettings.GENERIC_LEVEL;
		if (LoggerSettings.CLIENT_PACKAGE_NAME.contains(packageName)) {
			level = LoggerSettings.CLIENT_LEVEL;
		} else if (LoggerSettings.TRAP_RECEIVER_PACKAGE_NAME.contains(packageName)) {
			level = LoggerSettings.TRAP_RECEIVER_LEVEL;
		}
		this.clazz = clazz;
		this.level = level;
	}

	protected void log(LoggingLevel level, String message, Exception e) {
		OffsetDateTime currentDateTime = OffsetDateTime.now();
		String className = clazz.getName();
		if (level == LoggingLevel.ALL) {
			level = LoggingLevel.TRACE;
		}
		System.out.println("[" + level.name() + "] " + currentDateTime.toString() + " " + className + ": " + message);
		if (e != null) {
			e.printStackTrace();
		}
	}

	public LoggingLevel getLevel() {
		return level;
	}

	public void setLevel(LoggingLevel level) {
		this.level = level;
	}

	public abstract void trace(String message);
	public abstract void trace(String message, Exception e);
	public abstract void debug(String message);
	public abstract void debug(String message, Exception e);
	public abstract void info(String message);
	public abstract void info(String message, Exception e);
	public abstract void warn(String message);
	public abstract void warn(String message, Exception e);
	public abstract void error(String message);
	public abstract void error(String message, Exception e);

}
