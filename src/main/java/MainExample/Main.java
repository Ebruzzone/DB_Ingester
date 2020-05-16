package MainExample;

import AsyncLogger.AsyncLoggerManager;
import Bulker.BulkManager;
import Scheduler.Action;
import Scheduler.ScheduleManager;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Main {

	public static AsyncLoggerManager logger = new AsyncLoggerManager(AsyncLoggerManager.Severity.DEBUG, 20L);

	public static void main(String[] args) {

		ConnectionManager connectionManager;
		try {
			connectionManager = new ConnectionManager("connection.properties", 1000);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return;
		}

		ScheduleManager scheduleManager = new ScheduleManager();

		BulkManager<Data2MySQL, StringBuilder> bulkManager1 =
				new BulkManager<>(500L, 2000L, 20000L, 0L),
				bulkManager2 = new BulkManager<>(500L, 2000L, 20000L, 0L),
				bulkManager3 = new BulkManager<>(500L, 2000L, 20000L, 0L);

		Map<String, List<Action<?, ?>>> map = new HashMap<>();

		List<Action<?, ?>> list = new LinkedList<>(), list1 = new LinkedList<>(), hw = new LinkedList<>();

		final HoldString sql1 = new HoldString("INSERT INTO `mydatabase`.`data1` (`data`) VALUES ("),
				sql2 = new HoldString("INSERT INTO `mydatabase`.`data2` (`data`) VALUES ("),
				sql3 = new HoldString("INSERT INTO `mydatabase`.`data3` (`data`) VALUES (");

		long timestamp = System.currentTimeMillis();

		for (int i = 0; i < 100; i++) {
			list.add(new CreateDatum(sql1, 1, timestamp, bulkManager1, connectionManager));
		}

		map.put("1", list);
		timestamp = System.currentTimeMillis() + 1;
		list = new LinkedList<>();

		for (int i = 0; i < 100; i++) {
			list.add(new CreateDatum(sql2, 1, timestamp, bulkManager2, connectionManager));
		}

		map.put("2", list);
		timestamp = System.currentTimeMillis() + 3;
		list = new LinkedList<>();

		for (int i = 0; i < 50; i++) {
			list.add(new CreateDatum(sql3, 2, timestamp, bulkManager3, connectionManager));
		}

		map.put("3", list);

		Join join1 = new Join("Join1", 50, map), join2 = new Join("Join2", 50, map);
		list = new LinkedList<>();
		list.add(join1);
		list.add(join2);

		ExecuteStatement executeStatement1 = new ExecuteStatement("Truncate1",
				100, connectionManager, new HoldString("DROP TABLE `mydatabase`.`data4`;\n" +
				"CREATE TABLE `mydatabase`.`data4` (" +
				"`ID` INT NOT NULL AUTO_INCREMENT," +
				"`data` DOUBLE NOT NULL," +
				"PRIMARY KEY (`ID`));"), sql1),
				executeStatement2 = new ExecuteStatement("Truncate2", 100,
						connectionManager, new HoldString("DROP TABLE `mydatabase`.`data5`;\n" +
						"CREATE TABLE `mydatabase`.`data5` (" +
						"`ID` INT NOT NULL AUTO_INCREMENT," +
						"`data` DOUBLE NOT NULL," +
						"PRIMARY KEY (`ID`));"), sql2),
				executeStatement3 = new ExecuteStatement("Truncate3", 100,
						connectionManager, new HoldString("DROP TABLE `mydatabase`.`data6`;\n" +
						"CREATE TABLE `mydatabase`.`data6` (" +
						"`ID` INT NOT NULL AUTO_INCREMENT," +
						"`data` DOUBLE NOT NULL," +
						"PRIMARY KEY (`ID`));"), sql3);
		list1.add(executeStatement1);

		hw.add(new HelloWorld("Hello World", 100,
				new ExecuteStatement[]{executeStatement1, executeStatement2, executeStatement3}));

		long init = System.currentTimeMillis() / 1000 + 1, s = (init % 60000) / 1000;

		scheduleManager.addScheduler("Hello World", hw, new JSONObject()
				.put("secs", new Integer[]{(int) s}), init * 1000);

		List<Long> waitMilli = new LinkedList<>();
		waitMilli.add(40L);

		scheduleManager.addScheduler(
				"DataCreator1", list, waitMilli, null, 0, 3);

		waitMilli = new LinkedList<>();
		waitMilli.add(60L);

		scheduleManager.addScheduler(
				"DataCreator2", list, waitMilli, null, 0, 3);

		scheduleManager.addScheduler(
				"DeleteData1", list1, new String[]{"90S 123s"}, null, 0, 3);

		list1.clear();
		list1.add(executeStatement2);
		scheduleManager.addScheduler(
				"DeleteData2", list1, new String[]{"90S 123s"}, null, 0, 3);

		list1.clear();
		list1.add(executeStatement3);
		scheduleManager.addScheduler(
				"DeleteData3", list1, new String[]{"90S 123s"}, null, 0, 3);

		for (int i = 0; i < 60; i++) {
			try {
				Thread.sleep(30000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			logger.logInfo(String.valueOf(scheduleManager.getTimes("DataCreator1")));
			logger.logInfo(String.valueOf(scheduleManager.getTimes("DataCreator2")));
		}

		logger.logInfo("Finish");
		ScheduleManager.SchedulerParameters[] schedulerParameters = scheduleManager.killAndGetParameters(
				new String[]{"DataCreator1", "DataCreator2"});
		logger.logInfo(String.valueOf(schedulerParameters[0].times));
		logger.logInfo(String.valueOf(schedulerParameters[1].times));

		try {
			Thread.sleep(30000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		scheduleManager.notifySchedule("DeleteData1");
		scheduleManager.notifySchedule("DeleteData2");
		scheduleManager.notifySchedule("DeleteData3");

		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		schedulerParameters = scheduleManager.killAndGetParameters(
				new String[]{"DeleteData1", "DeleteData2", "DeleteData3"});
		logger.logInfo(String.valueOf(schedulerParameters[0].times));
		logger.logInfo(String.valueOf(schedulerParameters[1].times));
		logger.logInfo(String.valueOf(schedulerParameters[2].times));
		logger.logInfo(executeStatement1.getLogs());
		scheduleManager.killSchedule("Hello World");

		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		bulkManager1.kill();
		bulkManager2.kill();
		bulkManager3.kill();
		connectionManager.close();
		logger.logInfo("Kill all. Data: " + CreateDatum.number.get() + " BulkData: " + Data2MySQL.number.get());
		logger.kill();
	}
}
