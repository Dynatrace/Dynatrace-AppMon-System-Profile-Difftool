package com.dynatrace.common.worker;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class AbstractWorkerTest {

	static class TestQueue extends Thread {

		private final LinkedBlockingDeque<Runnable> queue = new LinkedBlockingDeque<>();

		@Override
		public void run() {
			for (;;) {
				try {
					queue.takeFirst().run();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		void enqueue(Runnable run) {
			queue.addLast(run);
		}
	}

	static class TestWorker extends AbstractWorker {

		private final TestQueue queue;

		public TestWorker(TestQueue queue) {
			this.queue = queue;
		}

		@Override
		protected boolean isUIThread() {
			return Thread.currentThread() == queue;
		}

		@Override
		protected void invokeInUIThread(Runnable run) {
			queue.enqueue(run);
		}

		@Override
		protected void startThread(Runnable run) {
			new Thread(run).start();
		}
	}

	static TestQueue queue;
	static TestWorker worker;

	@BeforeClass
	public static void beforeClass() {
		queue = new TestQueue();
		worker = new TestWorker(queue);
		queue.start();


	}

	@Test
	public void simple() throws InterruptedException {
		AbstractWorker.RequestCounter counter = new AbstractWorker.RequestCounter("simple");

		CountDownLatch latch = new CountDownLatch(1);
		AtomicBoolean worked = new AtomicBoolean();
		AtomicBoolean updated = new AtomicBoolean();

		queue.enqueue(() -> {
			worker.submit(counter).start(new AbstractWorker.Callback() {
				@Override
				public void work() throws InterruptedException {
					System.out.println("WORK...");
					Thread.sleep(500);
					System.out.println("WORKED");
					worked.set(true);
				}

				@Override
				public void update() {
					updated.set(true);
					latch.countDown();
				}
			});
		});

		latch.await();
		Assert.assertTrue(worked.get());
		Assert.assertTrue(updated.get());
	}

	@Test
	public void outdated() throws InterruptedException {
		AbstractWorker.RequestCounter counter = new AbstractWorker.RequestCounter("outdated");

		CountDownLatch latch = new CountDownLatch(2);
		AtomicBoolean worked = new AtomicBoolean();
		AtomicBoolean updated1 = new AtomicBoolean();
		AtomicBoolean updated2 = new AtomicBoolean();
		AbstractWorker.RequestWatcher[] watchers = new AbstractWorker.RequestWatcher[2];

		queue.enqueue(() -> {
			watchers[0] = worker.submit(counter);
			watchers[0].start(new AbstractWorker.Callback() {
				@Override
				public void work() throws InterruptedException {
					System.out.println("WORK LONG...");
					Thread.sleep(2000);
					System.out.println("WORKED");
					worked.set(true);
				}

				@Override
				public void update() {
					updated1.set(true);
				}

				@Override
				public void updateFinally() {
					latch.countDown();
				}
			});
			watchers[1] = worker.submit(counter);
			watchers[1].start(new AbstractWorker.Callback() {
				@Override
				public void work() throws InterruptedException {
					System.out.println("WORK...");
					Thread.sleep(100);
					System.out.println("WORKED");
					worked.set(true);
				}

				@Override
				public void update() {
					updated2.set(true);
				}

				@Override
				public void updateFinally() {
					latch.countDown();
				}

			});
		});

		latch.await();
		Assert.assertTrue(worked.get());
		Assert.assertFalse(updated1.get());
		Assert.assertTrue(updated2.get());
		Assert.assertTrue(watchers[0].outdated());
		Assert.assertFalse(watchers[1].outdated());
	}

	@Test
	public void progress() throws InterruptedException {
		AbstractWorker.RequestCounter counter = new AbstractWorker.RequestCounter("progress");

		CountDownLatch latch = new CountDownLatch(1);
		AtomicBoolean worked = new AtomicBoolean();
		AtomicBoolean updated = new AtomicBoolean();
		List<Integer> progress = new ArrayList<>();

		queue.enqueue(() -> {
			AbstractWorker.RequestWatcher watcher = worker.submit(counter);
			watcher.start(new AbstractWorker.Callback() {
				@Override
				public void work() throws InterruptedException {
					System.out.println("WORK 1...");
					Thread.sleep(100);
					watcher.notifyProgress(1);
					System.out.println("WORK 2...");
					Thread.sleep(100);
					watcher.notifyProgress(2);
					System.out.println("WORK 3...");
					Thread.sleep(100);
					watcher.notifyProgress(3);
					System.out.println("WORKED");
					worked.set(true);
				}

				@Override
				public void update() {
					updated.set(true);
				}

				@Override
				public void progress(int n) {
					progress.add(n);
				}

				@Override
				public void updateFinally() {
					latch.countDown();
				}
			});
		});

		latch.await();
		Assert.assertTrue(worked.get());
		Assert.assertTrue(updated.get());
		Assert.assertEquals("[1, 2, 3]", progress.toString());
	}

	@Test
	public void workExceptionUpdate() throws InterruptedException {
		AbstractWorker.RequestCounter counter = new AbstractWorker.RequestCounter("workExceptionUpdate");

		CountDownLatch latch = new CountDownLatch(1);
		AtomicBoolean worked = new AtomicBoolean();
		AtomicBoolean updated = new AtomicBoolean();

		queue.enqueue(() -> {
			worker.submit(counter).start(new AbstractWorker.Callback() {
				@Override
				public void work() throws Exception {
					System.out.println("WORK...");
					if (1==1) throw new Exception("Hey");
				}

				@Override
				public void workFinally() {
					worked.set(true);
					latch.countDown();
				}

				@Override
				public void update() {
					updated.set(true);
				}
			});
		});

		latch.await();
		Assert.assertTrue(worked.get());
		Assert.assertFalse(updated.get());
	}

	@Test
	public void workExceptionUpdateFinally() throws InterruptedException {
		AbstractWorker.RequestCounter counter = new AbstractWorker.RequestCounter("workExceptionUpdateFinally");

		CountDownLatch latch = new CountDownLatch(1);
		AtomicBoolean worked = new AtomicBoolean();
		AtomicBoolean updated = new AtomicBoolean();

		queue.enqueue(() -> {
			worker.submit(counter).start(new AbstractWorker.Callback() {
				@Override
				public void work() throws Exception {
					System.out.println("WORK...");
					if (1==1) throw new Exception("Hey");
				}

				@Override
				public void workFinally() {
					worked.set(true);
				}

				@Override
				public void updateFinally() {
					updated.set(true);
					latch.countDown();
				}
			});
		});

		latch.await();
		Assert.assertTrue(worked.get());
		Assert.assertTrue(updated.get());
	}

	@Test
	public void updateException() throws InterruptedException {
		AbstractWorker.RequestCounter counter = new AbstractWorker.RequestCounter("updateException");

		CountDownLatch latch = new CountDownLatch(1);
		AtomicBoolean worked = new AtomicBoolean();
		AtomicBoolean updated = new AtomicBoolean();

		queue.enqueue(() -> {
			worker.submit(counter).start(new AbstractWorker.Callback() {
				@Override
				public void work() throws Exception {
					System.out.println("WORK...");
					worked.set(true);
				}

				@Override
				public void update() throws Exception {
					if(1==1) throw new Exception("Ho");
				}

				@Override
				public void updateFinally() {
					updated.set(true);
					latch.countDown();
				}
			});
		});


		latch.await();
		Assert.assertTrue(worked.get());
		Assert.assertTrue(updated.get());
	}
}
