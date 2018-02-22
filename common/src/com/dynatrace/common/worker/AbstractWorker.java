package com.dynatrace.common.worker;


public abstract class AbstractWorker {
	
	public static interface RequestWatcher {
		void start(Callback callback);
		void notifyProgress(int n);
		boolean outdated();
	}
	
	public static interface Callback {
		default public void work() throws Exception {
		}
		
		default public void workException(Exception e) {
			e.printStackTrace();
		}
		
		default public void workError(Error e) {
			e.printStackTrace(); // print the stacktrace since otherwise, the error will be swallowed
			throw e;
		}
		
		default public void workFinally() {
		}
		
		default public void progress(int n) throws Exception {
		}
		
		default public void progressException(Exception e) {
			e.printStackTrace();
		}
		
		default public void progressError(Error e) {
			throw e;
		}
		
		default public void update() throws Exception {
		}
		
		default public void updateException(Exception e) {
			e.printStackTrace();
		}
		
		default public void updateError(Error e) {
			throw e;
		}
		
		default public void updateFinally() {
		}
	}
	
	public static class RequestCounter {
		private final String desc;
		private volatile int requestNo;
		
		public RequestCounter(String desc) {
			this.desc = desc;
		}
		
		@Override
		public String toString() {
			return desc + "/" + requestNo;
		}
	}

	public RequestWatcher submit(RequestCounter counter) {
		checkUIThread();
		return new RequestHandler(counter);
	}

	private class RequestHandler implements RequestWatcher {
		private final RequestCounter counter;
		private final int requestNoIssued;
		private final Throwable submittedStackTrace;
		private Callback callback;
		
		RequestHandler(RequestCounter counter) {
			this.counter = counter;
			requestNoIssued = ++counter.requestNo;
			submittedStackTrace = new Throwable("Submitted: " + counter);
		}
		
		private void update() {
			checkUIThread();
			try {
				if (!checkOutdated()) {
					callback.update();
				}
			} catch (Exception e) {
				appendCause(e, submittedStackTrace);
				callback.updateException(e);
			} catch (Error e) {
				appendCause(e, submittedStackTrace);
				callback.updateError(e);
			} finally {
				callback.updateFinally(); // always called, even for outdated requests.
			}
		}

		private Runnable progressRunner(int progress) {
			return () -> {
				checkUIThread();
				if (!checkOutdated()) {
					try {
						callback.progress(progress);
					} catch (Exception e) {
						appendCause(e, submittedStackTrace);
						callback.progressException(e);
					} catch (Error e) {
						appendCause(e, submittedStackTrace);
						callback.progressError(e);
					}
				}
			};
		}
		
		private void work() {
			checkThread();
			try {
				callback.work();
			} catch (Exception e) {
				appendCause(e, submittedStackTrace);
				callback.workException(e);
			} catch (Error e) {
				appendCause(e, submittedStackTrace);
				callback.workError(e);
			} finally {
				try {
					callback.workFinally();
				} finally {
					invokeInUIThread(this::update); // we will always invoke update runner, to guarantee updateFinally() is invoked.
				}
			}
		}
		
		private boolean checkOutdated() {
			boolean outdated = outdated();
			if (outdated) {
				System.err.println("Request outdated: " + counter);
			}
			return outdated;
		}

		@Override
		public boolean outdated() {
			return requestNoIssued != counter.requestNo;
		}

		@Override
		public void notifyProgress(int progress) {
			checkThread();
			if (!checkOutdated()) {
				invokeInUIThread(progressRunner(progress));
			}
		}

		@Override
		public void start(Callback callback) {
			this.callback = callback;
			startThread(this::work);
		}
	}
	
	private void checkThread() {
		assert !isUIThread() : "wrong thread";
	}
	
	private void checkUIThread() {
		assert isUIThread() : "wrong thread";
	}
	
	protected abstract boolean isUIThread();
	
	protected abstract void invokeInUIThread(Runnable run);
	
	protected abstract void startThread(Runnable run);
	
    /**
     * Appends a new cause to an exception chain
     *
     * @param head the head of the exception chain
     * @param cause the new cause to add
     * @return <code>true</code> if the new cause was added; duplicate causes are not added, see {@link ExceptionCollator#exceptionsEqual(Throwable, Throwable)}
     * @author cwat-pgrasboe
     */
    public static void appendCause(Throwable head, Throwable cause) {
    	Throwable prev = null;
    	for (Throwable curr = head; curr != null; curr = curr.getCause()) {
    		prev = curr;
    	}
    	if (prev != null) {
    		try {
    			prev.initCause(cause);
    		} catch (Exception e) { // avoid exception from initCause()
    			System.err.println("[ExceptionCollator] Cannot initCause() although getCause() returned null: " + e + " for throwable: " + prev);
    		}
    	}
    }
}
