package com.dynatrace.profilediff.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileWriter;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.dynatrace.common.swing.ui.TreeCheckboxManager;
import com.dynatrace.common.worker.AWTWorker;
import com.dynatrace.profilediff.MergeStopException;
import com.dynatrace.profilediff.XmlElement;
import com.dynatrace.profilediff.XmlMerger3;
import com.dynatrace.profilediff.XmlStruct;
import com.dynatrace.profilediff.ui.MainPanel.XmlContainer;

@SuppressWarnings("serial")
class ResultPanel extends JPanel {
	
	final JTextArea text;
	final JTextArea lines;
	final JProgressBar progressBar;
	final JButton openAsBaseButton;
	final JButton saveFileButton;
	final JButton closeButton;
	final JButton stopButton;
	
	final TreeCheckboxManager deletionManager;
	final TreeCheckboxManager insertionManager;
	final TreeCheckboxManager attrManager;
	
	volatile XmlStruct merged;

	private final AWTWorker.RequestCounter merge = new AWTWorker.RequestCounter("merge");
	private final AWTWorker.RequestCounter save = new AWTWorker.RequestCounter("save");

	ResultPanel(Window owner, TreeCheckboxManager deletionManager, TreeCheckboxManager insertionManager, TreeCheckboxManager attrManager) {
		this.deletionManager = deletionManager;
		this.insertionManager = insertionManager;
		this.attrManager = attrManager;
		
		JScrollPane scroll;
		JPanel p;
		setLayout(new BorderLayout());
		
		add(scroll = new JScrollPane(text = TextTools.createTextArea()), BorderLayout.CENTER);
		scroll.setRowHeaderView(lines = TextTools.createTextArea());
		lines.setText("...");
		lines.setBackground(Color.lightGray);
		lines.setEditable(false);
		text.setEditable(false);
		
		add(p = new JPanel(new FlowLayout(FlowLayout.CENTER)), BorderLayout.SOUTH);
		p.add(progressBar = new JProgressBar());
		progressBar.setPreferredSize(new Dimension(200, 25));
		progressBar.setStringPainted(true);
		progressBar.setString("Initializing...");
		p.add(stopButton = new JButton("Stop"));
		p.add(openAsBaseButton = new JButton("Open merged document as baseline"));
		p.add(saveFileButton = new JButton("Save merged document..."));
		p.add(closeButton = new JButton("Close"));
		
		stopButton.setMnemonic('p');
		openAsBaseButton.setMnemonic('b');
		saveFileButton.setMnemonic('S');
		closeButton.setMnemonic('C');
		
		openAsBaseButton.setEnabled(false);
		saveFileButton.setEnabled(false);
		closeButton.setEnabled(false);
		
		stopButton.addActionListener((ActionEvent) -> stopAction(owner));
		openAsBaseButton.addActionListener((ActionEvent) -> openAsBaseAction(owner)); 
		saveFileButton.addActionListener((ActionEvent) -> saveAction(owner));
		closeButton.addActionListener((ActionEvent) -> closeAction(owner));
		
		owner.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				stop.set(true);
			};
		});
	}
	
	private void openAsBaseAction(Component owner) {
		owner.setVisible(false);
		assert merged != null;
		ProfileDiff.mainPanel.openBase(XmlInput.fromText("[merge result]", merged.data), /*parseXml*/ false);
	}
	
	private void saveAction(Component owner)  {
		owner.setVisible(false);
		File newFile = ProfileDiff.mainPanel.openFileDialog("Select destination file", /*checkFileExists*/ false);
		if (newFile == null) {
			return;
		}

		assert merged != null;
		ProfileDiff.worker.submit(save).start(new AWTWorker.Callback() {
			@Override
			public void work() throws Exception {
				try (FileWriter out = new FileWriter(newFile)) {
					out.write(merged.data.toString());
				}
			}
			
			@Override
			public void workException(Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(ProfileDiff.mainPanel, "I/O error: \n" + e, "Error", JOptionPane.ERROR_MESSAGE);
			}
			
			@Override
			public void update() throws Exception {
				JOptionPane.showMessageDialog(ProfileDiff.mainPanel, "Successfully written file: " + newFile.getName(), "Success", JOptionPane.INFORMATION_MESSAGE);
			}
		});
	}
	
	private void closeAction(Component owner) {
		owner.setVisible(false);
	}
	
	private void stopAction(Component owner) {
		stop.set(true);
	}
	
	private final AtomicBoolean stop = new AtomicBoolean();
	
	void mergeAction(XmlContainer base, XmlContainer mod, boolean openAsBase, boolean saveFile) {
		XmlStruct baseXml = base.xml;
		XmlStruct modXml = mod.xml;
		assert baseXml != null;
		assert modXml != null;
		int total = baseXml.root().nStructureChanged + modXml.root().nStructureChanged + modXml.root().nAttributeChanged;
		
		ProfileDiff.mainPanel.mergeStart();
		progressBar.setValue(0);
		progressBar.setMaximum(total);
		
		UpdatableElementCallback callback = new UpdatableElementCallback(baseXml, modXml, total);
		ProfileDiff.worker.submit(merge).start(new AWTWorker.Callback() {
			@Override
			public void work() throws Exception {
				merged = ProfileDiff.merge(baseXml, modXml, /*addComments*/ false, callback, stop);
			}
			
			@Override
			public void workException(Exception e) {
				e.printStackTrace();
				if (e instanceof MergeStopException) {
					merged = ((MergeStopException) e).getMerged();
				}
				callback.error(e);
			}
			
			@Override
			public void workFinally() {
				ProfileDiff.mainPanel.mergeDoneWork();
			}
			
			@Override
			public void update() throws Exception {
				callback.summarize(base.input, mod.input);
				if (merged != null) {
					openAsBaseButton.setEnabled(true);
					saveFileButton.setEnabled(true);
				}
				closeButton.setEnabled(true);
				stopButton.setEnabled(false);
				
				if (openAsBase)  {
					EventQueue.invokeLater(clickButton(openAsBaseButton));
				} else if(saveFile) {
					EventQueue.invokeLater(clickButton(saveFileButton));
				}
			}
			
			@Override
			public void updateFinally() {
				ProfileDiff.mainPanel.mergeDoneUpdate();
			}
		});
	}
	
	static Runnable clickButton(AbstractButton button) {
		return () -> {
			try {
				Thread.sleep(250);
				button.doClick(250);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		};
	}
	
	boolean deletionChecked(XmlElement element) {
		return !deletionManager.isSelected(element); // negated!
	}

	boolean insertionChecked(XmlElement element) {
		return insertionManager.isSelected(element);
	}
	
	boolean attrChecked(XmlElement element) {
		return attrManager.isSelected(element);
	}
	
	static class AcceptCounter {
		volatile int accepted;
		volatile int total;
		
		void increase(boolean wasAccepted) {
			total++;
			if (wasAccepted) {
				accepted++;
			}
		}
	}
	
	class UpdatableElementCallback implements XmlMerger3.IncludeElementCallback, Runnable {
		final XmlStruct base;
		final XmlStruct mod;
		final int total;
		final StringBuilder textBuf = new StringBuilder();
		final StringBuilder linesBuf = new StringBuilder();
		final AcceptCounter deletionCounter = new AcceptCounter();
		final AcceptCounter insertionCounter = new AcceptCounter();
		final AcceptCounter attrCounter = new AcceptCounter();
		final AcceptCounter overallCounter = new AcceptCounter();
		
		UpdatableElementCallback(XmlStruct base, XmlStruct mod, int total) {
			this.base = base;
			this.mod = mod;
			this.total = total;
		}

		@Override
		public boolean addElement(XmlElement element) {
			assert mod.elements.contains(element); // make sure we know the element
			return message(insertionChecked(element), element, insertionCounter);
		}
		
		@Override
		public boolean removeElement(XmlElement element) {
			assert base.elements.contains(element); // make sure we know the element
			return message(deletionChecked(element), element, deletionCounter);
		}
		
		@Override
		public boolean replaceAttributes(XmlElement element) {
			assert mod.elements.contains(element); // make sure we know the element
			return message(attrChecked(element), element, attrCounter);
		}
		
		private boolean message(boolean accepted, XmlElement element, AcceptCounter counter) {
			counter.increase(accepted);
			overallCounter.increase(accepted);
			textBuf.append(accepted ? "A" : "R");
			textBuf.append(element.getTag()).append(" ");
			textBuf.append(element.toPathString()).append(TextTools.LF);
			
			linesBuf.append(overallCounter.total).append(" ");
			linesBuf.append(TextTools.LF);
			
			EventQueue.invokeLater(this);
			return accepted;
		}
		
		void summarize(XmlInput baseInput, XmlInput modInput) {
			textBuf.append(TextTools.LF);
			linesBuf.append(TextTools.LF);
			
			if (overallCounter.total == 0) {
				textBuf.append(String.format("No changes present from  '%s' to baseline '%s'.", modInput.name, baseInput.name));
				textBuf.append(TextTools.LF);
				linesBuf.append(TextTools.LF);
			} else if (overallCounter.accepted == 0) {
				textBuf.append(String.format("No changes accepted from  '%s' to baseline '%s'.", modInput.name, baseInput.name));
				textBuf.append(TextTools.LF);
				linesBuf.append(TextTools.LF);
			} else {
				textBuf.append(String.format("Merged changes in '%s' to baseline '%s'.", modInput.name, baseInput.name));
				textBuf.append(TextTools.LF);
				linesBuf.append(TextTools.LF);
			}
			
			textBuf.append(String.format("Accepted: %d/%d deletions (-); %d/%d insertions (+); %d/%d attribute changes (#); overall: %d/%d"
					, deletionCounter.accepted
					, deletionCounter.total
					, insertionCounter.accepted
					, insertionCounter.total
					, attrCounter.accepted
					, attrCounter.total
					, overallCounter.accepted
					, overallCounter.total
			));
			textBuf.append(TextTools.LF);
			linesBuf.append(TextTools.LF);
			
			String textString = textBuf.toString();
			String linesString = linesBuf.toString();
			System.out.println(textString);
			updateText(textString, linesString);
		}
		
		void error(Throwable t) {
			textBuf.append(t).append(TextTools.LF);
			linesBuf.append(TextTools.LF);
			updateText(textBuf.toString(), linesBuf.toString());
		}
		
		@Override
		public void run() {
			int count = overallCounter.total;
			progressBar.setValue(count);
			progressBar.setString(count + "/" + total);
			if (count == 10 || count % 20 == 0) {
				updateText(textBuf.toString(), linesBuf.toString());
			}
		}
		
		private void updateText(String textString, String linesString) {
			text.setText(textString); 
			lines.setText(linesString);
		}
	}
}
