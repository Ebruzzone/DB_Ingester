package MainExample;

import Scheduler.Action;

public class HelloWorld extends Action<Object, Object> {

	private ExecuteStatement[] executeStatements;

	protected HelloWorld(String name, long progress, ExecuteStatement[] executeStatements) {
		super(name, progress);
		this.executeStatements = executeStatements;
	}

	@Override
	protected Object infoProcessing(Object info) {
		return null;
	}

	@Override
	protected Object exe(Object info) {
		Main.logger.logInfo("Hello World"); // Logs Hello World
		for (ExecuteStatement executeStatement : executeStatements) {
			Main.logger.logInfo(executeStatement.getLogs()); // Logs the logs of the 3 executionStatements
			executeStatement.clearLogs(); // Clears the logs
		}

		return null;
	}
}
