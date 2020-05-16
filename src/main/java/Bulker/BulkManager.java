package Bulker;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class BulkManager<Obj extends BulkObject<Obj, T>, T> implements RunnableKillable {

	private int wait;
	private long maxWait;
	private long maxSize;
	private long last;
	private final long maxFraction;
	private final BulkBuilder<Obj, T> builder;
	private AtomicBoolean alive;
	private AtomicBoolean busy;
	private AtomicLong lastSize;
	private AtomicLong maxBusy;
	private AtomicLong waitBusy;
	private AtomicLong size;
	private AtomicLong actualFraction;

	public BulkManager(long maxWait, long initialBulkSize, long maxBulkSize, long maxFraction) {
		this.maxWait = maxWait;
		this.maxFraction = maxFraction;
		this.maxSize = maxBulkSize;
		this.size = new AtomicLong(initialBulkSize);
		last = System.currentTimeMillis();

		wait = maxFraction > 8 ? 1 : (int) (maxWait / 20 + 1);

		actualFraction = new AtomicLong(1);

		alive = new AtomicBoolean(true);
		busy = new AtomicBoolean(false);
		lastSize = new AtomicLong(initialBulkSize);
		maxBusy = new AtomicLong(maxFraction);
		waitBusy = new AtomicLong();

		builder = new BulkBuilder<>(this);
		new Thread(this).start();
		new Thread(builder).start();
	}

	public void kill() {
		alive.set(false);
		builder.kill();
	}

	boolean isEnough(long size, long lastUnion) {

		long now = System.currentTimeMillis();
		boolean flag = this.size.get() < size || (size > 0 && maxWait < (now - last));

		if (flag) {
			last = now;
			lastSize.set(size);
		}

		if (lastUnion > this.size.get() / 5) {
			wait = wait > 1 ? wait - 1 : wait;
		} else if (lastUnion < this.size.get() / 30) {
			wait = wait > (maxWait / 10) ? wait : wait + 1;
		}

		return flag;
	}

	void builderWait() {
		try {
			Thread.sleep(wait);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	Obj union(List<Obj> objects) {

		Obj objectFinal = objects.remove(0);

		long af = actualFraction.get();

		if (af < 2) {

			for (Obj object : objects) {

				objectFinal = objectFinal.join(object);
			}

		} else {
			long i = 0;

			for (Obj object : objects) {
				i++;

				if (i % af != 0) {
					continue;
				}

				objectFinal = objectFinal.join(object);
			}
		}

		synchronized (builder) {
			builder.notify();
		}

		return objectFinal;
	}

	public void add(Obj object) {
		if (!busy.get()) {
			builder.add(object);
		} else if (maxBusy.addAndGet(-object.length()) < 1) {
			busy.set(false);
		}
	}

	public void run() {

		while (alive.get()) {

			if (busy.get()) {
				try {
					Thread.sleep(waitBusy.get());
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				busy.set(false);

			} else {
				synchronized (builder) {
					try {
						builder.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				double factor = (double) lastSize.get() / size.get() / 1.2,
						fraction = actualFraction.get(), newSize = size.get();

				if (factor > 1) {

					if (maxFraction == 0) {
						newSize *= factor;
						newSize = Math.min(newSize, maxSize);
					} else {
						if (size.get() * factor * fraction * 1.2 > maxSize) {
							fraction *= factor;

							if (fraction > maxFraction) {
								maxBusy.set((long) (maxSize * maxFraction * 2 * factor));
								waitBusy.set(maxWait / 2);
								busy.set(true);
								fraction = maxFraction;
							}

							newSize = maxSize;
						} else {
							newSize *= factor;
						}
					}
				} else {

					if (fraction > 1) {
						fraction *= factor;
					}

					newSize = factor * size.get();
				}

				size.set((long) newSize);
				actualFraction.set((long) fraction);
			}
		}
	}
}
