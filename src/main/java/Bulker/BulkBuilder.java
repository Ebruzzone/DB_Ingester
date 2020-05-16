package Bulker;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

class BulkBuilder<Obj extends BulkObject<Obj, T>, T> implements RunnableKillable {

	private AtomicBoolean alive;
	private BulkExecutor<Obj, T> executor;
	private BulkManager<Obj, T> manager;
	private ConcurrentLinkedQueue<Obj> objects;
	private List<Obj> prepObjects;
	private AtomicLong size;

	BulkBuilder(BulkManager<Obj, T> manager) {
		this.manager = manager;
		this.executor = new BulkExecutor<>();

		objects = new ConcurrentLinkedQueue<>();
		prepObjects = new LinkedList<>();

		alive = new AtomicBoolean(true);
		size = new AtomicLong(0);
		new Thread(executor).start();
	}

	public void kill() {
		alive.set(false);
		executor.kill();

		synchronized (this) {
			this.notify();
		}
	}

	void add(Obj object) {
		objects.add(object);
	}

	public void run() {

		long sizeTemp, len;

		while (alive.get()) {

			manager.builderWait();

			sizeTemp = objects.size();
			len = 0;

			for (long i = 0; i < sizeTemp; i++) {
				Obj s = objects.poll();
				prepObjects.add(prepObjects.size(), s);
				len += s == null ? 0 : s.length();
			}

			if (manager.isEnough(size.addAndGet(len), len)) {

				executor.exec(manager.union(prepObjects));

				prepObjects.clear();
				size.set(0);
			}
		}

		for (long i = 0; i < objects.size(); i++) {
			Obj s = objects.poll();
			prepObjects.add(prepObjects.size(), s);
		}

		if (prepObjects.size()>0) {
			executor.exec(manager.union(prepObjects));
		}
	}
}
