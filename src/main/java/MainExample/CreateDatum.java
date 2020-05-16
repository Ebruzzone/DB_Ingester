package MainExample;

import Bulker.BulkManager;
import Scheduler.Action;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class CreateDatum extends Action<Integer, Integer> {

	private Random r;
	private HoldString name;
	private BulkManager<Data2MySQL, StringBuilder> bulkManager;
	private ConnectionManager connectionManager;
	public static AtomicLong number = new AtomicLong(0);

	public CreateDatum(HoldString name, long progress, Long random, BulkManager<Data2MySQL, StringBuilder> bulkManager,
						  ConnectionManager connectionManager) {
		super(progress);
		this.name = name;
		this.bulkManager = bulkManager;
		this.connectionManager = connectionManager;
		r = new Random(random);
	}

	@Override
	protected Integer infoProcessing(Object info) {
		return (int) (info == null ? r.nextInt(100) : info);
	}

	@Override
	protected Integer exe(Integer info) {

		number.incrementAndGet();
		int i = info + 1;

		bulkManager.add(new Data2MySQL(new StringBuilder(String.valueOf(i)), connectionManager, name));
		return i;
	}
}
