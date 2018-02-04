package app.logger;

public class ConsoleLogger extends Logger {

	public ConsoleLogger(Class<?> clazz) {
		super(clazz);
	}

	@Override
	public void trace(String message) {
		trace(message, null);
	}

	@Override
	public void trace(String message, Exception e) {
		log(LoggingLevel.TRACE, message, e);
	}

	@Override
	public void debug(String message) {
		debug(message, null);
	}

	@Override
	public void debug(String message, Exception e) {
		log(LoggingLevel.DEBUG, message, e);
	}

	@Override
	public void info(String message) {
		info(message, null);
	}

	@Override
	public void info(String message, Exception e) {
		log(LoggingLevel.INFO, message, e);
	}

	@Override
	public void warn(String message) {
		warn(message, null);
	}

	@Override
	public void warn(String message, Exception e) {
		log(LoggingLevel.WARN, message, e);
	}

	@Override
	public void error(String message) {
		error(message, null);
	}

	@Override
	public void error(String message, Exception e) {
		log(LoggingLevel.ERROR, message, e);
	}

}
