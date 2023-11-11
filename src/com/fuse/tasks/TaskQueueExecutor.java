package com.fuse.tasks;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.Executor;

public class TaskQueueExecutor implements Executor {
	private static Queue<Runnable> queue;
	private static Runnable task;

	public static TaskQueueExecutor getInstance() {
		if (queue == null) {
			queue = new ArrayDeque<Runnable>();
		}

		return new TaskQueueExecutor();
	}

	@Override
	public synchronized void execute(Runnable r) {

		queue.offer(new Runnable() {
			public void run() {
				try {
					r.run();
				} finally {
					next();
				}
			}
		});

		if (task == null) {
			next();
		}
	}

	private synchronized void next() {
		if ((task = queue.poll()) != null) {
			new Thread(task).start();

		}
	}

}
