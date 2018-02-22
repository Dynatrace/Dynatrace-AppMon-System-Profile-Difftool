package com.dynatrace.profilediff.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractButton;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import com.dynatrace.common.swing.ui.DocumentAdapter;
import com.dynatrace.common.swing.ui.TreeCheckboxManager;
import com.dynatrace.common.swing.ui.TreeExpandAllManager;
import com.dynatrace.common.worker.AWTWorker;
import com.dynatrace.profilediff.XmlElement;
import com.dynatrace.profilediff.XmlStruct;
import com.dynatrace.profilediff.ui.MainPanel.SelectionState;
import com.dynatrace.profilediff.ui.MainPanel.XmlContainer;
import com.dynatrace.profilediff.ui.TwoWayModel.ChangeItem;
import com.dynatrace.profilediff.ui.decorationmodel.DecorationModel;

@SuppressWarnings("serial")
class TwoWayPanel extends JPanel {
	
	private class FilePanel extends JPanel {
		final JTree tree;
		final TreeCheckboxManager treeCheckboxManager;
		final JProgressBar treeProgress;
		final JTextArea textArea;
		final JTextArea linesArea;
		final TreeExpandAllManager treeExpandAllManager;
		final AbstractButton syncSelection, checkAll, parseXml, attrChangesOnly, attrSyncSelection, attrAllAttributes, attrIncludeChildren, filterCheck;
		final JComboBox<TwoWayModel.ChangeItem> changeBox;
		final JTextField filter;
		final JLabel path;
		final JTable table;
		final DefaultTableModel tableModel;
		TreePath treeSelectionPath;
		FilePanel other;
		XmlStruct xml;
		boolean currentlySyncing;
		
		FilePanel(boolean isLeft, TreeCheckboxManager treeCheckboxManager, SelectionState selectionState) {
			this.treeCheckboxManager = treeCheckboxManager;
			setLayout(new BorderLayout());
			final JLabel status;
			final AbstractButton expandAll;
			JPanel p0, p1, p2;
			JSplitPane vsplit;
			JTabbedPane tab;
			JScrollPane treeScroll, textScroll;
			
			add(vsplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT), BorderLayout.CENTER);
			vsplit.setOneTouchExpandable(true);
			vsplit.setResizeWeight(0.75);
			vsplit.setDividerLocation(0.75);
			
			vsplit.add(p0 = new JPanel(new BorderLayout()));
			p0.add(p1 = new JPanel(new BorderLayout()), BorderLayout.CENTER);
			p1.add(treeScroll = new JScrollPane(tree = UITools.createTree()), BorderLayout.CENTER);
			p1.add(treeProgress = new JProgressBar(), BorderLayout.SOUTH);
			treeProgress.setString("Please open a file");
			treeProgress.setStringPainted(true);
			treeProgress.setPreferredSize(new Dimension(0, 25));
			p1.add(p2 = new JPanel(), BorderLayout.NORTH);
			
			GroupLayout group;
			p2.setLayout(group = new GroupLayout(p2));
			
			GroupLayout.SequentialGroup hGroup = group.createSequentialGroup();
			
			group.setAutoCreateGaps(true);
			group.setAutoCreateContainerGaps(true);
			
			hGroup.addComponent(filterCheck = new JCheckBox("Filter:"));
			hGroup.addComponent(filter = new JTextField());
			hGroup.addComponent(changeBox = new JComboBox<>(TwoWayModel.ChangeItem.values()));
			hGroup.addComponent(checkAll = new JCheckBox("Check all"));
			hGroup.addComponent(expandAll = new JCheckBox("Expand all"));
			hGroup.addComponent(syncSelection = new JCheckBox("Sync selection", true));
			
			group.setHorizontalGroup(hGroup);
			
			GroupLayout.Group v = group.createParallelGroup(Alignment.BASELINE);
			
			GroupLayout.Group pGroup = group.createParallelGroup(Alignment.BASELINE);
			v.addGroup(pGroup);
			pGroup.addComponent(filterCheck);
			pGroup.addComponent(filter);
			pGroup = group.createParallelGroup(Alignment.BASELINE);
			v.addGroup(pGroup);
			
			pGroup.addComponent(syncSelection);
			pGroup.addComponent(changeBox);
			pGroup.addComponent(checkAll);
			pGroup.addComponent(expandAll);
			
			group.setVerticalGroup(v);
			
			changeBox.setEditable(false);
			changeBox.setMaximumSize(new Dimension(175, 25));
			
			p0.add(p1 = new JPanel(new FlowLayout(FlowLayout.LEFT)), BorderLayout.SOUTH);
			p1.add(status = new JLabel("Ready."));
			status.setFont(status.getFont().deriveFont(Font.BOLD));
			selectionState.addStatusLabel(status);
			
			vsplit.add(p0 = new JPanel(new BorderLayout()));
			p0.add(p1 = new JPanel(new FlowLayout(FlowLayout.LEFT)), BorderLayout.NORTH);
			p1.add(path = new JLabel("-"));
			p0.add(tab = new JTabbedPane(JTabbedPane.BOTTOM), BorderLayout.CENTER);
			
			tab.addTab("Attributes", p1 = new JPanel(new BorderLayout()));
			p1.add(new JScrollPane(table = new JTable(tableModel = new DefaultTableModel(TwoWayModel.ATTR_EMPTY, TwoWayModel.ATTR_COLS))), BorderLayout.CENTER);
			p1.add(p2 = new JPanel(new FlowLayout(FlowLayout.RIGHT)), BorderLayout.SOUTH);
			p2.add(attrChangesOnly = new JCheckBox("Changes only", true));
			p2.add(attrAllAttributes = new JCheckBox("Ignored attributes"));
			p2.add(attrIncludeChildren = new JCheckBox("Include children", true));
			p2.add(attrSyncSelection = new JCheckBox("Sync selection", true));
			
			tab.addTab("Source", p1 = new JPanel(new BorderLayout()));
			p1.add(textScroll = new JScrollPane(textArea = TextTools.createTextArea()), BorderLayout.CENTER);
			textScroll.setRowHeaderView(linesArea = TextTools.createTextArea());
			linesArea.setText("...");
			linesArea.setBackground(Color.lightGray);
			linesArea.setEditable(false);
			
			p1.add(p2 = new JPanel(new FlowLayout(FlowLayout.RIGHT)), BorderLayout.SOUTH);
			p2.add(parseXml = new JButton("Parse this!"));
			
			tree.addTreeSelectionListener(new TreeSelectionListener() {
				@Override
				public void valueChanged(TreeSelectionEvent e) {
					TreePath treePath = e.getNewLeadSelectionPath();
					if (treePath == null) {
						currentElement = null;
						clear();
						return;
					}
					XmlElement element = (XmlElement) treePath.getLastPathComponent();
					if (element == null) {
						return;
					}
					path.setText(element.toPathString());
					treeSelectionPath = treePath;
					currentElement = element;
					showElement(element);
					if (!currentlySyncing && syncSelection.isSelected()) {
						syncSelection(element);
					}
				}
			});
			
			treeCheckboxManager.addUsingTree(tree);
			treeCheckboxManager.addListeners(tree);
			
			table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
				@Override
				public void valueChanged(ListSelectionEvent e) {
					if (currentlySyncing || !attrSyncSelection.isSelected()) {
						return;
					}
					if (attributeData != null) {
						int i = table.getSelectedRow();
						if (0 <= i && i < attributeData.length) {
							Object cell = attributeData[i][0];
							if (cell instanceof SpecialTableCell) {
								syncAttrSelection((SpecialTableCell) cell);
							}
						}
					}
				}
			});

			changeBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					updateTreeModel(xml);
					initTreeRenderer(); // drawing of checkboxes might change
				}
			});
			syncSelection.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (syncSelection.isSelected() && treeSelectionPath != null) {
						XmlElement element = (XmlElement) treeSelectionPath.getLastPathComponent();
						syncSelection(element);
					}
				}
			});
			checkAll.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					checkAllAction(checkAll.isSelected());
				}
			});
			parseXml.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					parseXml(textArea.getText(), FilePanel.this);
				}
			});
			attrChangesOnly.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (currentElement != null) {
						showElement(currentElement);
					}
				}
			});
			attrAllAttributes.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (currentElement != null) {
						showElement(currentElement);
					}
				}
			});
			attrIncludeChildren.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (currentElement != null) {
						showElement(currentElement);
					}
				}
			});
			filter.getDocument().addDocumentListener(DocumentAdapter.adapt(new DocumentAdapter() {
				@Override
				public void changed(DocumentEvent e) {
					boolean showParentPeers = false;
					boolean mayChangeCheckbox = true;
					UITools.filter(xml, mayChangeCheckbox , filter, filterCheck, showParentPeers);
					updateTreeModel(xml);
				}
			}));
			filterCheck.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					boolean showParentPeers = false;
					boolean mayChangeCheckbox = false;
					UITools.filter(xml, mayChangeCheckbox , filter, filterCheck, showParentPeers);
					updateTreeModel(xml);
				}
			});
			treeExpandAllManager = new TreeExpandAllManager(expandAll, /*collapseUntilRow*/ 2);
			treeExpandAllManager.addListeners(tree, treeScroll);
		}
		
		private void initRenderers() {
			initTreeRenderer();
			initTableRenderer();
		}
		
		private void initTreeRenderer() {
			treeCheckboxManager.invalidate(); // need to invalidate since maybe some checkboxes will get hidden. but keep state.
			
			Color mainColor = xml != null && other.xml != null ? Renderers.unchangedColor : Renderers.changedColor;
			Renderers.initTreeCellRenderers(tree, treeCheckboxManager, decorationModel, mainColor, getChangeItem());
		}
		
		private void initTableRenderer() {
			Color mainColor = xml != null && other.xml != null ? Renderers.unchangedColor : Renderers.changedColor;
			Renderers.initTableCellRenderers(table, mainColor);
		}
		
		private void checkAllAction(boolean checkAll) {
			UITools.checkAll(getChangeItem(), checkAll, treeCheckboxManager, xml);
		}

		XmlElement currentElement;
		Object[][] attributeData;
		
		private final AWTWorker.RequestCounter setXmlText = new AWTWorker.RequestCounter("setXmlText");
		
		void showElement(XmlElement element) {
			boolean compare = left.xml != null && right.xml != null;
			boolean changesOnly = attrChangesOnly.isSelected();
			boolean allAttributes = attrAllAttributes.isSelected();
			boolean includeChildren = attrIncludeChildren.isSelected();
			tableModel.setDataVector(attributeData = TwoWayModel.enumAttributes(element, compare, changesOnly, allAttributes, includeChildren), TwoWayModel.ATTR_COLS);
			
			/*
			 * setting XML texts asyncly, since it might get huge.
			 */
			ProfileDiff.worker.submit(setXmlText).start(new AWTWorker.Callback() {
				volatile String text; 
				volatile String lines; 
				
				@Override
				public void work() throws Exception {
					if (element == element.xml.root()) {
						text = element.xml.data.subSequence(0, element.closeTag.end).toString();
						lines = TextTools.createLineNumberString(1, element.closeTag.lineEnd);
					} else {
						text = element.getContent().toString();
						lines = TextTools.createLineNumberString(element.openTag.lineStart, element.closeTag.lineEnd);
					}
				}
				
				@Override
				public void update() throws Exception {
					assert text != null;
					assert lines != null;
					textArea.setText(text);
					linesArea.setText(lines);
					textArea.setCaretPosition(0);
					linesArea.setCaretPosition(0);
				}
			});
		}
		
		private void fetchPrepare(XmlInput newInput) {
			treeProgress.setString(UITools.loadingString(newInput));
			treeCheckboxManager.clear();
			userObjectLogic.clear();
			treeProgress.setValue(1);
			treeProgress.setMaximum(3);
			currentElement = null;
			treeSelectionPath = null;
			checkAll.setSelected(ProfileDiff.getDefaultInitialSelectionInsertion() && ProfileDiff.getDefaultInitialSelectionDeletion() && ProfileDiff.getDefaultInitialSelectionAttribute());
//			expandAll.setSelected(false);
			filterCheck.setSelected(false);
		}
		
		private void fetchUpdate(XmlContainer container) {
			xml = container.xml;
			treeProgress.setString(container.input == null ? "" : container.input.name);
			treeProgress.setValue(0);
			clear();
		}
		
		private void clear() {
			path.setText("-");
			tableModel.setDataVector(TwoWayModel.ATTR_EMPTY, TwoWayModel.ATTR_COLS);
		}
		
		private void newDocument() {
			updateTreeModel(xml);
		}
		
		private TwoWayModel.ChangeItem getChangeItem() {
			assert changeBox.getSelectedItem() != null;
			return (TwoWayModel.ChangeItem) changeBox.getSelectedItem();
		}
		
		private void updateTreeModel(XmlStruct xml) {
			if (xml == null) {
//				new Throwable("NULL MODEL").printStackTrace();
				tree.setModel(new DefaultTreeModel((DefaultMutableTreeNode) null));
			} else {
				ChangeItem item = getChangeItem();
				if (other.xml != null) {
					tree.setModel(TwoWayModel.createTreeModel(item, xml, userObjectLogic));
					expandInitial(xml, /*childrenToo*/ item.isChangesOnly());
				} else {
					tree.setModel(XmlStructTreeModelFactory.normal(xml, userObjectLogic));
					expandInitial(xml, /*childrenToo*/ false);
				}
				treeExpandAllManager.reset(tree);
				if (treeSelectionPath != null) {
					tree.setSelectionPath(treeSelectionPath);
					tree.scrollPathToVisible(treeSelectionPath);
				}
			}
		}
		
		private void expandInitial(XmlStruct xml, boolean childrenToo) {
			XmlElement toExpand = xml.root();
			while (toExpand.children.size() == 1) {
				toExpand = toExpand.children.get(0);
			}
			tree.expandPath(UITools.makeTreePath(toExpand));
			if (childrenToo) {
				for (XmlElement child : toExpand.children) {
					tree.expandPath(UITools.makeTreePath(child));
				}
			}
		}
		
		void syncSelection(XmlElement element) {
			if (xml == null || other.xml == null) {
				return;
			}
			try {
				other.currentlySyncing = true;
				if (element.peer != null) {
					TreePath treePath = UITools.makeTreePath(element.peer);
					other.tree.setSelectionPath(treePath);
					other.tree.scrollPathToVisible(treePath);
				} else {
					other.tree.setSelectionPath(null);
				}
			} finally {
				other.currentlySyncing = false;
			}
		}
		
		private int findAttrPeer(SpecialTableCell findCell, Object[][] peerAttributeData) {
			if (findCell.kind == SpecialTableCell.Kind.AttributeAdded || findCell.kind == SpecialTableCell.Kind.AttributeRemoved) {
				return -1; // can't be found
			}
			for (int i = 0; i < peerAttributeData.length; i++) {
				Object cell = peerAttributeData[i][0];
				if (!(cell instanceof SpecialTableCell)) {
					continue;
				}
				SpecialTableCell specialCell = (SpecialTableCell) cell;
				if (specialCell.element.peer == findCell.element && specialCell.text.equals(findCell.text)) {
					return i;
				}
			}
			return -1;
		}
		
		private void syncAttrSelection(SpecialTableCell cell) {
			if (other.attributeData != null) {
				int index = findAttrPeer(cell, other.attributeData);
				try {
					other.currentlySyncing = true;
					if (index != -1) {
						other.table.getSelectionModel().setSelectionInterval(index, index);
						Rectangle aRect = other.table.getCellRect(index, 0, /*includeSpacing*/ true);
						if (aRect != null) {
							other.table.scrollRectToVisible(aRect);
						}
					} else {
						other.table.getSelectionModel().clearSelection();
					}
				} finally {
					other.currentlySyncing = false;
				}
			}
		}
	}
	
	private final FilePanel left;
	private final FilePanel right;
	
	private final UserObjectLogic userObjectLogic;
	private final DecorationModel<XmlElement> decorationModel;
	
	TwoWayPanel(UserObjectLogic userObjectLogic, SelectionState selectionState, TreeCheckboxManager treeCheckboxManager, DecorationModel<XmlElement> decorationModel) {
		this.userObjectLogic = userObjectLogic;
		this.decorationModel = decorationModel;
		
		JSplitPane hsplit;
		setLayout(new BorderLayout());
		add(hsplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT), BorderLayout.CENTER);
		hsplit.setOneTouchExpandable(true);
		hsplit.setResizeWeight(0.5);
		hsplit.setDividerLocation(0.5);
		hsplit.add(left = new FilePanel(/*isLeft*/ true, treeCheckboxManager, selectionState));
		hsplit.add(right = new FilePanel(/*isLeft*/ false, treeCheckboxManager, selectionState));
		left.other = right;
		right.other = left;
	}
	
	void fetchPrepare(XmlContainer baseContainer, XmlContainer modContainer) {
		left.fetchPrepare(baseContainer.input);
		right.fetchPrepare(modContainer.input);
	}
	
	void fetchUpdate(XmlContainer baseContainer, XmlContainer modContainer) {
		left.fetchUpdate(baseContainer);
		right.fetchUpdate(modContainer);
		
		left.newDocument();
		left.initRenderers();
		right.newDocument();
		right.initRenderers();
	}
	
	void fetchProgress(int n, XmlInput baseInput, XmlInput modInput) {
		left.treeProgress.setValue(n);
		right.treeProgress.setValue(n);
		if (n == 3) {
			left.treeProgress.setString("Diffing..."); 
			right.treeProgress.setString("Diffing..."); 
		}
	}
	
	void parseXml(CharSequence xmlText, FilePanel dest) {
		XmlInput input = XmlInput.fromText("[parsed xml]", xmlText);
		if (dest == left) {
			ProfileDiff.mainPanel.openBase(input, /*parseXml*/ true);
		} else {
			ProfileDiff.mainPanel.openMod(input, /*parseXml*/ true);
		}
	}
	
	 void refreshRenderers() {
		 left.initRenderers();
		 right.initRenderers();
	}
	 
	void toggle(XmlElement element, boolean oldSelected, boolean newSelected) {
	}
	
	void parseXmlFailed() {
	}
}
