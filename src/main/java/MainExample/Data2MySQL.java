package MainExample;

import Bulker.BulkObject;

import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicLong;

public class Data2MySQL extends BulkObject<Data2MySQL, StringBuilder> {

	private ConnectionManager connectionManager;
	private final HoldString sql;
	public static AtomicLong number = new AtomicLong(0);

	public Data2MySQL(StringBuilder datum, ConnectionManager manager, HoldString sql) {
		super(datum);
		this.connectionManager = manager;
		this.sql = sql;
	}

	// Join operation of the two contents of Data2MySQL objects
	@Override
	public Data2MySQL join(Data2MySQL other) {
		content.append("),(").append(other.content);
		return this;
	}

	@Override
	public void exec() {

		int num = content.toString().split(",").length;
		Main.logger.logDebug("Bulk " + num);
		number.addAndGet(num);

		synchronized (sql) {
			content = new StringBuilder(sql.getString().concat(content.toString())).append(");");

			// Inserts the rows in MySQL table
			try {
				connectionManager.getPreparedStatement(sql.getString(), content.toString()).execute();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	// The way of calculating how much this Data2MySQL object is big in the bulk
	@Override
	public long length() {
		return 1L;
	}
}
