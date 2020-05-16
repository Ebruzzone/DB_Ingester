package Bulker;

public abstract class BulkObject<Obj extends BulkObject<Obj, T>, T> {

	protected T content;

	protected BulkObject(T content) {
		this.content = content;
	}

	public abstract Obj join(Obj other);

	public abstract void exec();

	public abstract long length();
}
