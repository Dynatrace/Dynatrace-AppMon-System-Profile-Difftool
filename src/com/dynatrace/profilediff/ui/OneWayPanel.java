package com.dynatrace.profilediff.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
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
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.HighlightPainter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import com.dynatrace.common.swing.ui.DocumentAdapter;
import com.dynatrace.common.swing.ui.TreeCheckboxManager;
import com.dynatrace.common.swing.ui.TreeExpandAllManager;
import com.dynatrace.profilediff.XmlElement;
import com.dynatrace.profilediff.XmlStruct;
import com.dynatrace.profilediff.XmlUtil;
import com.dynatrace.profilediff.ui.MainPanel.SelectionState;
import com.dynatrace.profilediff.ui.MainPanel.XmlContainer;
import com.dynatrace.profilediff.ui.decorationmodel.DecorationModel;

@SuppressWarnings("serial")
class OneWayPanel extends JPanel {
	
	private static class XmlRenderer {
		XmlStruct xml;
		JTextArea textArea;
		JTextArea linesArea;
		final List<Object> highlightedLines = new ArrayList<>();
	}
	
	private final XmlRenderer base = new XmlRenderer();
	private final XmlRenderer mod = new XmlRenderer();
	
	private final TreeCheckboxManager treeCheckboxManager;
	private final UserObjectLogic userObjectLogic;
	private final DecorationModel<XmlElement> decorationModel;
	
	final JProgressBar treeProgress;
	final JTabbedPane textTab;
	final JTree tree;
	final TreeExpandAllManager treeExpandAllManager;
	final AbstractButton checkAll, attrChangesOnly, attrAllAttributes, attrIncludeChildren, filterCheck;
	final JComboBox<OneWayModel.ChangeItem> changeBox;
	final JTextField filter;
	final JLabel path;
	final JTable table;
	final DefaultTableModel tableModel;
	
	TreePath treeSelectionPath;
	XmlElement selectedElement;
	
	static final HighlightPainter selectedPainter = new DefaultHighlighter.DefaultHighlightPainter(Renderers.textSelectionColor);
	final HighlightPainter addedPainter = new DefaultHighlighter.DefaultHighlightPainter(Renderers.textAddedColor);
	final HighlightPainter removedPainter = new DefaultHighlighter.DefaultHighlightPainter(Renderers.textRemovedColor);
	final HighlightPainter attrChangedPainter = new DefaultHighlighter.DefaultHighlightPainter(Renderers.textAttrChangedColor);
	
	OneWayPanel(UserObjectLogic userObjectLogic, SelectionState selectionState, TreeCheckboxManager treeCheckboxManager, DecorationModel<XmlElement> decorationModel) {
		this.treeCheckboxManager = treeCheckboxManager;
		this.userObjectLogic = userObjectLogic;
		this.decorationModel = decorationModel;
		
		final JLabel status;
		final AbstractButton expandAll;
		setLayout(new BorderLayout());
		JPanel p0, p1, p2;
		JSplitPane vsplit, hsplit;
		JTabbedPane tab;
		JScrollPane treeScroll, textScroll;
		
		add(p0 = new JPanel(new FlowLayout(FlowLayout.LEFT)), BorderLayout.SOUTH);
		p0.add(path = new JLabel("-"));
		
		add(hsplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT), BorderLayout.CENTER);
		hsplit.setOneTouchExpandable(true);
		hsplit.setResizeWeight(0.3);
		hsplit.setDividerLocation(0.3);
		hsplit.add(vsplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT));
		vsplit.setOneTouchExpandable(true);
		vsplit.setResizeWeight(0.75);
		vsplit.setDividerLocation(0.75);
		
		vsplit.add(p0 = new JPanel(new BorderLayout()));
		p0.add(p1 = new JPanel(new BorderLayout()), BorderLayout.CENTER);
		p1.add(treeScroll = new JScrollPane(tree = UITools.createTree()), BorderLayout.CENTER);
		p1.add(treeProgress = new JProgressBar(), BorderLayout.SOUTH);
		treeProgress.setPreferredSize(new Dimension(0, 25));
		treeProgress.setStringPainted(true);
		treeProgress.setString("Please open a file");
		p1.add(p2 = new JPanel(), BorderLayout.NORTH);
		
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
			}
		});
		
		GroupLayout group;
		p2.setLayout(group = new GroupLayout(p2));
		
		GroupLayout.SequentialGroup hGroup = group.createSequentialGroup();
		
		group.setAutoCreateGaps(true);
		group.setAutoCreateContainerGaps(true);
		
		hGroup.addComponent(filterCheck = new JCheckBox("Filter:"));
		hGroup.addComponent(filter = new JTextField());
		hGroup.addComponent(changeBox = new JComboBox<>(OneWayModel.ChangeItem.values()));
		hGroup.addComponent(checkAll = new JCheckBox("Check all"));
		hGroup.addComponent(expandAll = new JCheckBox("Expand all"));
		
		group.setHorizontalGroup(hGroup);
		
		GroupLayout.Group v = group.createParallelGroup(Alignment.BASELINE);
		
		GroupLayout.Group pGroup = group.createParallelGroup(Alignment.BASELINE);
		v.addGroup(pGroup);
		pGroup.addComponent(filterCheck);
		pGroup.addComponent(filter);
		pGroup.addComponent(changeBox);
		pGroup.addComponent(checkAll);
		pGroup.addComponent(expandAll);
		group.setVerticalGroup(v);
		
		checkAll.setMnemonic('c');
		expandAll.setMnemonic('e');
		changeBox.setEditable(false);
		changeBox.setMaximumSize(new Dimension(175, 25));
		
		p0.add(p1 = new JPanel(new FlowLayout(FlowLayout.LEFT)), BorderLayout.SOUTH);
		p1.add(status = new JLabel("Ready."));
		status.setFont(status.getFont().deriveFont(Font.BOLD));
		selectionState.addStatusLabel(status);
		
		vsplit.add(p0 = new JPanel(new BorderLayout()));
		p0.add(tab = new JTabbedPane(JTabbedPane.BOTTOM), BorderLayout.CENTER);
		
		tab.addTab("Attributes", p1 = new JPanel(new BorderLayout()));
		p1.add(new JScrollPane(table = new JTable(tableModel = new DefaultTableModel(OneWayModel.ATTR_EMPTY, OneWayModel.ATTR_COLS))), BorderLayout.CENTER);
		p1.add(p2 = new JPanel(new FlowLayout(FlowLayout.RIGHT)), BorderLayout.SOUTH);
		p2.add(attrChangesOnly = new JCheckBox("Changes only", true));
		p2.add(attrAllAttributes = new JCheckBox("Ignored attributes"));
		p2.add(attrIncludeChildren = new JCheckBox("Include children", true));
		
		hsplit.add(p0 = new JPanel(new BorderLayout()));
		
		p0.add(textTab = new JTabbedPane(JTabbedPane.BOTTOM));
		textTab.addTab("Baseline", textScroll = new JScrollPane(base.textArea = TextTools.createTextArea()));
		textScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		textScroll.setRowHeaderView(base.linesArea = TextTools.createTextArea());
		base.textArea.setEditable(false);
		base.linesArea.setBackground(Color.lightGray);
		base.linesArea.setEditable(false);

		textTab.addTab("Modification", textScroll = new JScrollPane(mod.textArea = TextTools.createTextArea()));
		textScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		textScroll.setRowHeaderView(mod.linesArea = TextTools.createTextArea());
		mod.textArea.setEditable(false);
		mod.linesArea.setBackground(Color.lightGray);
		mod.linesArea.setEditable(false);
		
		treeCheckboxManager.addUsingTree(tree);
		treeCheckboxManager.addListeners(tree);
		
		changeBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateTreeModel();
				initTreeRenderer(); // drawing of checkboxes might change
			}
		});
		checkAll.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				checkAllAction(checkAll.isSelected());
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
				// It's quite important to do this in the right order! 
				boolean showParentPeers = true;
				boolean mayChangeCheckbox = true;
				UITools.filter(mod.xml, mayChangeCheckbox , filter, filterCheck, showParentPeers );
				UITools.filter(base.xml, mayChangeCheckbox, filter, filterCheck, showParentPeers);
				updateTreeModel();
			}
		}));
		filterCheck.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean showParentPeers = true;
				boolean mayChangeCheckbox = false;
				UITools.filter(mod.xml, mayChangeCheckbox , filter, filterCheck, showParentPeers );
				UITools.filter(base.xml, mayChangeCheckbox, filter, filterCheck, showParentPeers);
				updateTreeModel();
			}
		});
		
		base.textArea.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				showElementAt(base, e.getPoint());
			}
		});
		mod.textArea.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				showElementAt(mod, e.getPoint());
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
		treeCheckboxManager.invalidate(); // need to invalidate since maybe some checkboxs will get hidden. but keep state.
		
		Color mainColor = mod.xml != null && base.xml != null ? Renderers.unchangedColor : Renderers.changedColor;
		Renderers.initTreeCellRenderers(tree, treeCheckboxManager, decorationModel, mainColor, getChangeItem()); 
	}
	
	private void initTableRenderer() {
		Color mainColor = mod.xml != null && base.xml != null ? Renderers.unchangedColor : Renderers.changedColor;
		Renderers.initTableCellRenderers(table, mainColor);
	}
	
	private XmlRenderer getRenderer(XmlElement element) {
		return element.xml == base.xml ? base : mod;
	}
	
	private void updateHighlight(XmlElement element, boolean selected)  {
		Highlighter highlighter = getRenderer(element).textArea.getHighlighter();
		Highlighter.Highlight highlight = userObjectLogic.getTextHighlight(element);
		Highlighter peerHighlighter = null;
		Highlighter.Highlight peerHighlight = null;
		
		assert highlight != null;
		
		if (element.peer != null) {
			peerHighlighter = getRenderer(element.peer).textArea.getHighlighter();
			peerHighlight = userObjectLogic.getTextHighlight(element.peer);
			assert peerHighlight != null;
		}
		
		/*
		 * first, let's remove highlights
		 */
		
		highlighter.removeHighlight(highlight);
		if (peerHighlighter != null) {
			peerHighlighter.removeHighlight(peerHighlight);
		}
		
		/*
		 * now, re-add highlights, if selected is true.
		 */

		if (selected) {
			try {
				userObjectLogic.setTextHighlight(element, highlighter.addHighlight(highlight.getStartOffset(), highlight.getEndOffset(), highlight.getPainter()));
				if (peerHighlighter != null) {
					userObjectLogic.setTextHighlight(element.peer, peerHighlighter.addHighlight(peerHighlight.getStartOffset(), peerHighlight.getEndOffset(), peerHighlight.getPainter())); 
				}
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
	}
	
	private XmlElement currentElement;
	
	private void showElement(XmlElement element) {
		boolean compare = base.xml != null && mod.xml != null;
		boolean changesOnly = attrChangesOnly.isSelected();
		boolean allAttributes = attrAllAttributes.isSelected();
		boolean includeChildren = attrIncludeChildren.isSelected();
		tableModel.setDataVector(OneWayModel.enumAttributes(element, compare, changesOnly, allAttributes, includeChildren), OneWayModel.ATTR_COLS);
		
		showXml(element);
		if (element.peer != null) {
			showXml(element.peer);
		} else if (element.parent != null && element.parent.peer != null) {
			showXml(element.parent.peer);
		}
		if (element.hasDirectStructureChange() || element.hasParentStructureChange()) {
			if (element.xml == base.xml)  {
				textTab.setSelectedIndex(0);
			} else {
				textTab.setSelectedIndex(1);
			}
		}
	}
	
	private boolean scroll = true;
	
	private void showXml(XmlElement element) {
		boolean changesOnly = getChangeItem().isChangesOnly();
		XmlRenderer renderer = getRenderer(element);
		updateLinesHighlight(element, renderer, changesOnly);
		if (scroll) {
			scrollToElement(element, renderer.textArea, changesOnly);
		}
		scroll = true;
	}
	
	private void clear() {
		path.setText("-");
		tableModel.setDataVector(OneWayModel.ATTR_EMPTY, OneWayModel.ATTR_COLS);
	}
	
	private OneWayModel.ChangeItem getChangeItem() {
		assert changeBox.getSelectedItem() != null;
		return (OneWayModel.ChangeItem) changeBox.getSelectedItem();
	}
	
	private void updateTreeModel() {
		OneWayModel.ChangeItem item = getChangeItem();
		if (base.xml != null && mod.xml != null) {
			tree.setModel(OneWayModel.createTreeModel(item, mod.xml, userObjectLogic));
			expandInitial(mod.xml, /*childrenToo*/ item.isChangesOnly());
		} else if (base.xml != null){
			tree.setModel(XmlStructTreeModelFactory.normal(base.xml, userObjectLogic));
			expandInitial(base.xml, /*childrenToo*/ false);
		} else if (mod.xml != null) {
			tree.setModel(XmlStructTreeModelFactory.normal(mod.xml, userObjectLogic));
			expandInitial(mod.xml, /*childrenToo*/ false);
		} else {
			tree.setModel(new DefaultTreeModel((DefaultMutableTreeNode) null));
			return;
		}
		treeExpandAllManager.reset(tree);
		if (treeSelectionPath != null) {
			tree.setSelectionPath(treeSelectionPath);
			tree.scrollPathToVisible(treeSelectionPath);
		}
	}
	
	private void expandInitial(XmlStruct xml, boolean childrenToo) {
		XmlElement toExpand = xml.root();
		while (toExpand.children.size() == 1) {
			toExpand = toExpand.children.get(0);
		}
		tree.expandPath(UITools.makeTreePathOneWay(toExpand));
		if (childrenToo) {
			for (XmlElement child : toExpand.children) {
				tree.expandPath(UITools.makeTreePathOneWay(child));
			}
		}
	}
	
	private void checkAllAction(boolean checkAll) {
		UITools.checkAll(getChangeItem(), checkAll, treeCheckboxManager, base.xml, mod.xml);
	}
	
	void fetchPrepare(XmlContainer baseContainer, XmlContainer modContainer) {
		treeSelectionPath = null;
		currentElement = null;
		treeProgress.setMaximum(3);
		treeCheckboxManager.clear();
		userObjectLogic.clear();
		filterCheck.setSelected(false);
		checkAll.setSelected(ProfileDiff.getDefaultInitialSelectionInsertion() && ProfileDiff.getDefaultInitialSelectionDeletion() && ProfileDiff.getDefaultInitialSelectionAttribute());
	}
	
	void fetchProgress(int n, XmlInput baseInput, XmlInput modInput) {
		treeProgress.setValue(n);
		switch (n) {
			case 1: treeProgress.setString(UITools.loadingString(baseInput)); break;
			case 2: treeProgress.setString(UITools.loadingString(modInput)); break;
			case 3: treeProgress.setString("Diffing..."); break;
		}
	}
	
	void fetchUpdate(XmlContainer baseContainer, XmlContainer modContainer) {
		base.xml = baseContainer.xml;
		mod.xml = modContainer.xml;
		clear();
		updateTreeModel();
		initRenderers();
		updateFileLabel(baseContainer.input, modContainer.input);
		setXmlText();
		treeProgress.setValue(0);
		treeProgress.setString("Ready.");
	}
	
	private void updateFileLabel(XmlInput baseInput, XmlInput modInput) {
		textTab.setTitleAt(0, baseInput != null ? baseInput.name : "-");
		textTab.setTitleAt(1, modInput != null ? modInput.name : "-");
	}
	
	private void setXmlText() {
		setXmlText(base);
		setXmlText(mod);
		
		try {
			if (base.xml != null) {
				highlightChanges(base.xml, base.textArea);
			}
			if (mod.xml != null) {
				highlightChanges(mod.xml, mod.textArea);
			}
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}
	
	private static void setXmlText(XmlRenderer renderer) {
		if (renderer.xml != null) {
			renderer.textArea.setText(renderer.xml.data.toString());
			renderer.textArea.setCaretPosition(0);
			renderer.linesArea.setText(TextTools.createLineNumberString(1, renderer.xml.root().closeTag.lineEnd + 1));
			renderer.linesArea.setCaretPosition(0);
		} else {
			renderer.textArea.setText("");
			renderer.linesArea.setText("...");
		}
	}
	
	private void highlightChanges(XmlStruct xml, JTextArea textArea) throws BadLocationException {
		Highlighter highlighter = textArea.getHighlighter();
		highlighter.removeAllHighlights();
		for (XmlElement element : xml.elements) {
			highlightChange(highlighter, element);
		}
	}
	

	private void highlightChange(Highlighter highlighter, XmlElement element) throws BadLocationException {
		/*
		 * Note: if a change is not default initial selected, we must still add the highlight to the userObject (and remove from hightlighter again)
		 * for updateHighlight() to work correctly.
		 */
		if (element.hasDirectStructureChange()) {
			if (element.isInsertion()) {
				Object highlight = highlighter.addHighlight(element.openTag.prevEnd, element.closeTag.end, addedPainter);
				userObjectLogic.setTextHighlight(element, highlight);
				if (!ProfileDiff.getDefaultInitialSelectionInsertion()) {
					highlighter.removeHighlight(highlight);
				}
			} else if (element.isDeletion()) {
				Object highlight = highlighter.addHighlight(element.openTag.prevEnd, element.closeTag.end, removedPainter);
				userObjectLogic.setTextHighlight(element, highlight);
				if (!ProfileDiff.getDefaultInitialSelectionDeletion()) {
					highlighter.removeHighlight(highlight);
				}
			}
		} else if (element.hasDirectAttributeChange()) {
			Object highlight = highlighter.addHighlight(element.openTag.start, element.openTag.end, attrChangedPainter);
			userObjectLogic.setTextHighlight(element, highlight);
			if (!ProfileDiff.getDefaultInitialSelectionAttribute()) {
				highlighter.removeHighlight(highlight);
			}
		}
	}
	
	
	private void showElementAt(XmlRenderer renderer, Point pt) {
		int offset = renderer.textArea.viewToModel(pt);
		XmlElement element = renderer.xml.findElementAtOffset(offset);
		boolean changesOnly = getChangeItem().isChangesOnly();
		
		if (element.hasNoChanges()) {
			if (!changesOnly) {
				scroll = false;
				UITools.showElementInTree(tree, UITools.makeTreePathOneWay(element), /*select*/ true, /*scroll*/ true);
			}
			return; // no change
		}
		if (changesOnly && element.hasParentStructureChange()) {
			element = XmlUtil.traverseToChange(element);
		}
//		System.out.println("now at: " + element);
		assert element.hasDirectStructureChange() || element.hasParentStructureChange() || element.hasDirectAttributeChange();
		scroll = false;
		UITools.showElementInTree(tree, UITools.makeTreePathOneWay(element), /*select*/ true, /*scroll*/ true);
	}
	
	private static void updateLinesHighlight(XmlElement element, XmlRenderer renderer, boolean changesOnly) {
		Highlighter highlighter = renderer.linesArea.getHighlighter();
		for (Object highlight : renderer.highlightedLines) {
			highlighter.removeHighlight(highlight);
		}
		renderer.highlightedLines.clear();
		
		try {
			if (changesOnly) {
				if (element.hasDirectStructureChange()) {
					int fromPosHighlight = renderer.linesArea.getLineStartOffset(element.openTag.lineStart - 1);
					int toPosHighligt = renderer.linesArea.getLineEndOffset(element.closeTag.lineEnd - 1);
					renderer.highlightedLines.add(highlighter.addHighlight(fromPosHighlight, toPosHighligt, selectedPainter));
				} else if (element.hasDirectAttributeChange()) {
					int fromPosHighlight = renderer.linesArea.getLineStartOffset(element.openTag.lineStart - 1);
					int toPosHighligt = renderer.linesArea.getLineEndOffset(element.openTag.lineEnd - 1);
					renderer.highlightedLines.add(highlighter.addHighlight(fromPosHighlight, toPosHighligt, selectedPainter));
				}
			} else {
				int fromPosHighlight = renderer.linesArea.getLineStartOffset(element.openTag.lineStart - 1);
				int toPosHighligt = renderer.linesArea.getLineEndOffset(element.closeTag.lineEnd - 1);
				renderer.highlightedLines.add(highlighter.addHighlight(fromPosHighlight, toPosHighligt, selectedPainter));
			}
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}
	
	private static void scrollToElement(XmlElement element, JTextArea textArea, boolean changesOnly) {
		boolean hasDirectStructureChange = element.hasDirectStructureChange();
		boolean hasDirectAttributeChange = element.hasDirectAttributeChange();
		if (!changesOnly || hasDirectStructureChange || hasDirectAttributeChange) {
			int fromPos = element.openTag.prevEnd + 1; // +1 takes the next newline also, which we want
			int toPos = hasDirectStructureChange || !changesOnly ? element.closeTag.end + 1 : element.openTag.end + 1;
			try {
				Rectangle fromRect = textArea.modelToView(fromPos);
				Rectangle toRect = textArea.modelToView(toPos);
				Rectangle scroll = fromRect.union(toRect);
				textArea.scrollRectToVisible(scroll);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
	}
	
	void refreshRenderers() {
		initRenderers();
	}

	void toggle(XmlElement element, boolean oldSelected, boolean newSelected) {
		updateHighlight(element, newSelected);
	}
	
	void parseXmlFailed() {
	}
}
