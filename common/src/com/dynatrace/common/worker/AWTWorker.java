package com.dynatrace.common.worker;

import java.awt.EventQueue;

public abstract class AWTWorker extends AbstractWorker {
	
	@Override
	protected boolean isUIThread() {
		return EventQueue.isDispatchThread();
	}

	@Override
	protected void invokeInUIThread(Runnable run) {
		EventQueue.invokeLater(run);
	}
}
