package util;

public class Lock {
	private volatile boolean locked;

	public synchronized void lock() {
		locked = true;
	}

	public synchronized void unlock() {
		locked = false;
	}

	public synchronized boolean tryLock() {
		return locked ? false : (locked = true);
	}
}