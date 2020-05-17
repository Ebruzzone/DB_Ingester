package MainExample;

import Scheduler.Action;

import java.sql.SQLException;

public class ExecuteStatement extends Action<Object, Object> {

	private final HoldString sql;
	private final HoldString sqlInsert;
	private ConnectionManager connectionManager;

	public ExecuteStatement(String name, long progress, ConnectionManager connectionManager,
							HoldString sql, HoldString sqlInsert) {
		super(name, progress);
		this.sql = sql;
		this.sqlInsert = sqlInsert;
		this.connectionManager = connectionManager;
	}

	@Override
	protected Object infoProcessing(Object info) {
		return null;
	}

	@Override
	protected Object exe(Object info) {

		sql.swapStrings("1", "4");
		sql.swapStrings("2", "5");
		sql.swapStrings("3", "6");

		synchronized (sqlInsert) {
			sqlInsert.swapStrings("1", "4");
			sqlInsert.swapStrings("2", "5");
			sqlInsert.swapStrings("3", "6");

			// Executes the drop statement and the creation statement
			try {
				connectionManager.getPreparedStatement(name, sql.getString().split("\n")[0]).execute();
				connectionManager.getPreparedStatement(name, sql.getString().split("\n")[1]).execute();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		// Appends current timestamp to the logs
		appendLog(System.currentTimeMillis() + " ");
		return null;
	}
}
