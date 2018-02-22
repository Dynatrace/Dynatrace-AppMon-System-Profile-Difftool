package com.dynatrace.profilediff.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.InputEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;
import javax.xml.stream.XMLStreamException;

import com.dynatrace.common.swing.ui.TreeCheckboxManager;
import com.dynatrace.common.worker.AWTWorker;
import com.dynatrace.profilediff.XmlElement;
import com.dynatrace.profilediff.XmlStruct;
import com.dynatrace.profilediff.XmlUtil;
import com.dynatrace.profilediff.togglemodel.ToggleModel;
import com.dynatrace.profilediff.togglemodel.ToggleVeto;
import com.dynatrace.profilediff.ui.decorationmodel.DecorationModel;

@SuppressWarnings("serial")
class MainPanel extends JPanel {
	
	private static final Object INTERNAL_TRIGGER_TRANSITIVE_SELECTION = "INTERNAL_TRIGGER_TRANSITIVE_SELECTION";

	static class XmlContainer {
		private XmlInput originalInput;
		XmlInput input;
		XmlStruct xml;
		
		void maybeFetch() throws XMLStreamException, IOException {
			if (xml == null && input != null) {
				xml = input.fetch(); // don't re-load when not necessary
			} else if (xml != null) {
				XmlUtil.resetFilter(xml); // need to reset filter in reused document.
			}
		}
		
		void take(XmlInput newInput) {
			input = newInput;
			xml = null; // force fetch again
		}
		
		void takeOriginal(XmlInput newInput) {
			originalInput = newInput;
			take(newInput);
		}
		
		void swapWith(XmlContainer other) {
			XmlInput i = other.input;
			other.input = input;
			input = i;
			XmlStruct x = other.xml;
			other.xml = xml;
			xml = x;
		}
	}
	
	static class SelectionState {
		int nCheckedAttr;
		int nCheckedAdded;
		int nCheckedRemoved;
		int nTotalAttr;
		int nTotalAdded;
		int nTotalRemoved;
		private boolean diffed;
		private final List<JLabel> statusLabels = new ArrayList<>();
		
		private void updateStatus(String errorMessage) {
			for (JLabel statusLabel : statusLabels) {
				updateStatus(statusLabel, errorMessage);
			}
		}
		
		private void updateStatus(JLabel statusLabel, String errorMessage) {
			if (errorMessage != null) {
				statusLabel.setForeground(Color.red);
				statusLabel.setText(errorMessage);
				return;
			} 
			statusLabel.setForeground(Color.black);
			if (diffed) {
				statusLabel.setText(String.format("Checked: %d/%d deletions %s; %d/%d insertions %s; %d/%d attribute changes %s"
						, nCheckedRemoved, nTotalRemoved, XmlElement.TAG_DELETION
						, nCheckedAdded, nTotalAdded, XmlElement.TAG_INSERTION
						, nCheckedAttr, nTotalAttr, XmlElement.TAG_ATTRIBUTE_CHANGE
				));
			} else {
				statusLabel.setText("Ready.");
			}
		}
		
		void addStatusLabel(JLabel label) {
			statusLabels.add(label);
		}
		
		private void diff(XmlStruct base, XmlStruct mod) {
			if (base != null && mod != null) {
				ProfileDiff.diff(base, mod); 
				nTotalRemoved = base.root().nStructureChanged;
				nTotalAdded = mod.root().nStructureChanged;
				nTotalAttr = mod.root().nAttributeChanged;
				
				/*
				 * Attention: default selection must be in sync with:
				 * TreeCheckboxManager.getInitialSelection() override of mainPanel
				 * initial-checked state of "checkAll" checkbox
				 * initial-drawing of highlights in OneWayPanel. 
				 */
				
				nCheckedAdded = ProfileDiff.getDefaultInitialSelectionInsertion() ? nTotalAdded : 0;
				nCheckedRemoved = ProfileDiff.getDefaultInitialSelectionDeletion() ? nTotalRemoved : 0;
				nCheckedAttr = ProfileDiff.getDefaultInitialSelectionAttribute() ? nTotalAttr : 0;
				
				diffed = true;
			} else {
				diffed = false;
			}
		}
	}

	private File fileChooserDirectory = new File(".");
	
	private final FileFilter xmlFileFilter = new FileFilter() {
		@Override
		public String getDescription() {
			return "XML files";
		}

		@Override
		public boolean accept(File f) {
			return f.isDirectory() || f.getName().toLowerCase().endsWith(".xml");
		}
	};

	private final OneWayPanel oneWayPanel;
	private final TwoWayPanel twoWayPanel;
	private final TreeCheckboxManager treeCheckboxManager;
	
	private final AWTWorker.RequestCounter open = new AWTWorker.RequestCounter("open");
	
	private final XmlContainer base = new XmlContainer();
	private final XmlContainer mod = new XmlContainer();
	private final SelectionState selectionState = new SelectionState();
	
	MainPanel(UserObjectLogic userObjectLogic, ToggleModel<XmlElement> toggleModel, DecorationModel<XmlElement> decorationModel) {
		final JTabbedPane tab;
		JMenuBar menuBar;
		JMenuItem item;
		JMenu menu;
		
		setLayout(new BorderLayout());
		add(menuBar = new JMenuBar(), BorderLayout.NORTH);
		menuBar.add(menu = new JMenu("File"));
		menu.setMnemonic('F');
		menu.add(item = new JMenuItem("Open 2 files for comparison..."));
		item.setAccelerator(KeyStroke.getKeyStroke('O', InputEvent.CTRL_MASK));
		item.addActionListener((ActionEvent) -> openTwoFilesAction());
		menu.add(item = new JMenuItem("Open 1 file..."));
		item.setAccelerator(KeyStroke.getKeyStroke('F', InputEvent.CTRL_MASK));
		item.addActionListener((ActionEvent) -> openOneFileAction());
		menu.add(item = new JMenuItem("Swap left/right"));
		item.setAccelerator(KeyStroke.getKeyStroke('W', InputEvent.CTRL_MASK));
		item.addActionListener((ActionEvent) -> swapLeftRightAction());
		menu.add(item = new JMenuItem("Reload"));
		item.setAccelerator(KeyStroke.getKeyStroke('R', InputEvent.CTRL_MASK));
		item.addActionListener((ActionEvent) -> reloadAction());
		menu.add(item = new JMenuItem("Merge..."));
		item.setAccelerator(KeyStroke.getKeyStroke('M', InputEvent.CTRL_MASK));
		item.addActionListener((ActionEvent) -> mergeAction());
		menu.add(item = new JMenuItem("Merge & save..."));
		item.setAccelerator(KeyStroke.getKeyStroke('S', InputEvent.CTRL_MASK));
		item.addActionListener((ActionEvent) -> mergeAndSaveAction());
		menu.add(item = new JMenuItem("Merge & open as baseline"));
		item.setAccelerator(KeyStroke.getKeyStroke('B', InputEvent.CTRL_MASK));
		item.addActionListener((ActionEvent) -> mergeAndOpenAsBaseAction());
		menu.addSeparator();
		menu.add(item = new JMenuItem("Help..."));
		menu.addSeparator();
		item.setAccelerator(KeyStroke.getKeyStroke("F1"));
		item.addActionListener((ActionEvent) -> helpAction());
		menu.add(item = new JMenuItem("Quit"));
		item.setAccelerator(KeyStroke.getKeyStroke('Q', InputEvent.CTRL_MASK));
		item.addActionListener((ActionEvent) -> quitAction());
		
		treeCheckboxManager = new XmlElementTreeCheckboxManager(userObjectLogic) {
			private String errorMessage;
			private JTree recentTree;

			@Override
			protected boolean toggle(Object o, boolean oldSelected, boolean newSelected, Object internalTrigger, JTree tree) {
				XmlElement element = (XmlElement) o;
				
//				System.out.printf("toggle: %b => %b, internally: %b - %s%n", oldSelected, newSelected, internallyTriggered, element.toStateString());
				
				if (newSelected != oldSelected) { // selection state really changed
					errorMessage = null;
					if (internalTrigger == null) { // i.e. it's user triggered - ask the model
						recentTree = tree;
						try { 
							toggleModel.toggle(element, newSelected);
						} catch (ToggleVeto v) {
							errorMessage = v.getMessage();
							return false; // we can't toggle.
						}
					} else if (internalTrigger == INTERNAL_TRIGGER_TRANSITIVE_SELECTION) { // indirectly via toggle model. expand the path that has changed 
						showElementInTree(element, recentTree);
					}
					updateCounts(newSelected, element);
				}
				
				// we also must tell the panels (for highlight drawing etc.)
				oneWayPanel.toggle(element, oldSelected, newSelected);
				twoWayPanel.toggle(element, oldSelected, newSelected);
				
				return true; // we can toggle.
			}
			
			@Override
			protected void toggleDone() {
				selectionState.updateStatus(errorMessage);
			}

			private void updateCounts(boolean selected, XmlElement element) {
				int delta = selected ? 1 : -1;
				if (element.hasDirectStructureChange() && element.isDeletion()) {
					selectionState.nCheckedRemoved += delta;
				} else if (element.hasDirectStructureChange() && element.isInsertion()) {
					selectionState.nCheckedAdded += delta;
				} else if (element.hasDirectAttributeChange()) {
					selectionState.nCheckedAttr += delta;
				} else {
					assert false : "invalid state of element checkbox";
				}
			}
			
			@Override
			protected boolean getInitialSelection(Object element) {
				return ProfileDiff.getDefaultInitialSelection((XmlElement) element); 
			};
		};
		
		toggleModel.setSelectionInterface(new ToggleModel.SelectionInterface<XmlElement>() {
			@Override
			public void setSelected(XmlElement element, boolean selected) {
				treeCheckboxManager.setSelected(element, selected, INTERNAL_TRIGGER_TRANSITIVE_SELECTION);
			}
			
			@Override
			public boolean isSelected(XmlElement element) {
				return treeCheckboxManager.isSelected(element);
			}
		});
		
		add(tab = new JTabbedPane(), BorderLayout.CENTER);
		tab.addTab("One way", oneWayPanel = new OneWayPanel(userObjectLogic, selectionState, treeCheckboxManager, decorationModel));
		tab.addTab("Two way", twoWayPanel = new TwoWayPanel(userObjectLogic, selectionState, treeCheckboxManager, decorationModel));

		tab.addChangeListener((ChangeEvent) -> treeCheckboxManager.invalidate()); // checkbox drawing might be different among panels.
	}
	
	private void showElementInTree(XmlElement element, JTree tree) {
		assert tree != null : "no tree";
		if (tree == oneWayPanel.tree) { // need special treepath for oneWay panel (with peers)
			UITools.showElementInTree(tree, UITools.makeTreePathOneWay(element), /*select*/ false, /*scroll*/ false);
		} else {
			UITools.showElementInTree(tree, UITools.makeTreePath(element), /*select*/ false, /*scroll*/ false);
		}
	}
	
	private static void helpAction() {
		ProfileDiff.openHelpWindow("xmldiff help");
	}
	
	private static void quitAction() {
		System.exit(0);
	}

	private void openOneFileAction() {
		File file = openFileDialog("Open document", /*checkFileExists*/ true);
		if (file != null) {
			open(XmlInput.fromFile(file), /*mod*/ null);
		}
	}
	
	private void openTwoFilesAction() {
		File base = openFileDialog("Open baseline", /*checkFileExists*/ true);
		if (base != null) {
			File mod = openFileDialog("Open modification", /*checkFileExists*/ true);
			if (mod != null) {
				open(XmlInput.fromFile(base), XmlInput.fromFile(mod));
			}
		}
	}
	
	private void swapLeftRightAction() {
		if (base.xml != null || mod.xml != null) {
			base.swapWith(mod);
			open0(/*clearStringCache*/ false, /*parseXml*/ false); // will only diff, not fetch anything
		}
	}
	
	private void reloadAction() {
		base.take(base.originalInput);
		mod.take(mod.originalInput);
		open0(/*clearStringCache*/ false, /*parseXml*/ false);
	}
	
	private void mergeAction() {
		mergeAction0(/*openAsBase*/ false, /*saveFile*/ false);
	}
	
	private void mergeAndOpenAsBaseAction() {
		mergeAction0(/*openAsBase*/ true, /*saveFile*/ false);
	}
	
	private void mergeAndSaveAction() {
		mergeAction0(/*openAsBase*/ false, /*saveFile*/ true);
	}
	
	private void mergeAction0(boolean openAsBase, boolean saveFile) {
		if (base.xml != null && mod.xml != null) {
			TreeCheckboxManager deletionManager = treeCheckboxManager;
			TreeCheckboxManager insertionManager = treeCheckboxManager;
			TreeCheckboxManager attrManager = treeCheckboxManager;
			ResultPanel panel = ProfileDiff.openResultDialog("xmlmerge", deletionManager, insertionManager, attrManager);
			panel.mergeAction(base, mod, openAsBase, saveFile);
		}
	}
	
	File openFileDialog(String title, boolean checkFileExists) {
		JFileChooser chooser = new JFileChooser(fileChooserDirectory);
		chooser.setFileFilter(xmlFileFilter);
		if (chooser.showDialog(this, title) == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			if (file != null) {
				if (!checkFileExists || checkFileArg(file)) {
					fileChooserDirectory = file.getParentFile();
					return file;
				} else {
					JOptionPane.showMessageDialog(this, "File not found: \n" + file, "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		return null;
	}
	
	private boolean checkFileArg(File file) {
		if (file.isFile()) {
			return true;
		}
		System.err.println("File not found: " + file);
		return false;
	}
	
	void mergeStart() {
		ProfileDiff.mainFrame.setEnabled(false); // simulate a modal dialog
	}
	
	void mergeDoneWork() {
		/*
		 * We need to diff again since the merge used the actual objects.
		 * It needed to use them to get the checkbox states.
		 */
		ProfileDiff.diff(base.xml, mod.xml);
	}
	
	void mergeDoneUpdate() {
		ProfileDiff.mainFrame.setEnabled(true);
		/*
		 * to be sure, issue some refresh (renderers)
		 */
		oneWayPanel.refreshRenderers();
		twoWayPanel.refreshRenderers();
	}
	
	void openBase(XmlInput input, boolean parseXml) {
		base.take(input);
		open0(/*clearStringCache*/ false, parseXml);
	}
	
	void openMod(XmlInput input, boolean parseXml) {
		mod.take(input);
		open0(/*clearStringCache*/ false, parseXml);
	}
	
	void open(XmlInput baseInput, XmlInput modInput) {
		base.takeOriginal(baseInput);
		mod.takeOriginal(modInput);
		open0(/*clearStringCache*/ true, /*parseXml*/ false);
	}

	private void open0(boolean clearStringCache, boolean parseXml) {
		oneWayPanel.fetchPrepare(base, mod);
		twoWayPanel.fetchPrepare(base, mod);
		AWTWorker.RequestWatcher request = ProfileDiff.worker.submit(open);
		request.start(new AWTWorker.Callback() {
			@Override
			public void work() throws Exception {
				if (clearStringCache) {
					ProfileDiff.clearStringCache();
				}
				request.notifyProgress(1);
				long t0 = System.currentTimeMillis();
				base.maybeFetch();
				request.notifyProgress(2);
				long t1 = System.currentTimeMillis();
				mod.maybeFetch();
				request.notifyProgress(3);
				long t2 = System.currentTimeMillis();
				selectionState.diff(base.xml, mod.xml);
				long t3 = System.currentTimeMillis();
				System.out.printf("Loading done, elapsed time [ms] parse/parse/diff/total: %d/%d/%d/%d%n", t1 - t0, t2 - t1, t3 - t2, t3 - t0);
				ProfileDiff.printStringCacheStats();
			}
			
			@Override
			public void workException(Exception e) {
				e.printStackTrace();
				if (parseXml && e instanceof XMLStreamException) {
					JOptionPane.showMessageDialog(MainPanel.this, "Parse error: \n" + e, "Error", JOptionPane.ERROR_MESSAGE);
					oneWayPanel.parseXmlFailed();
					twoWayPanel.parseXmlFailed();
				} else {
					JOptionPane.showMessageDialog(MainPanel.this, "I/O error: \n" + e, "Error", JOptionPane.ERROR_MESSAGE);
				}				
			}
			
			@Override
			public void update() throws Exception {
				oneWayPanel.fetchUpdate(base, mod);
				twoWayPanel.fetchUpdate(base, mod);
				selectionState.updateStatus(/*errorMessage*/ null);
			}
			
			@Override
			public void progress(int n) throws Exception {
				oneWayPanel.fetchProgress(n, base.input, mod.input);
				twoWayPanel.fetchProgress(n, base.input, mod.input);
			}
		});
	}
}
