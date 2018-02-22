package com.dynatrace.common.swing.ui;

import java.awt.Rectangle;

import javax.swing.AbstractButton;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;

import com.dynatrace.common.conf.DebugManager;

public class TreeExpandAllManager {
	
	private final AbstractButton expandAll;
	private final int collapseUntilRow;
	private int expandedUntilRow;
	
	public TreeExpandAllManager(AbstractButton expandAll, int collapseUntilRow) {
		this.expandAll = expandAll;
		this.collapseUntilRow = collapseUntilRow;
	}
	
	public void addListeners(JTree tree, JScrollPane scrollPane) {
		tree.addTreeWillExpandListener(new TreeWillExpandListener() {
			@Override
			public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
				expandFrom(event.getPath());
			}
			
			@Override
			public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
				expandFrom(event.getPath());
			}
			
			private void expandFrom(TreePath path) {
				if (expandAll.isSelected()) {
					expandedUntilRow = tree.getRowForPath(path) + 1; // +1 important to not undo the user's collapse.
				}
			}
		});
		scrollPane.getViewport().addChangeListener((ChangeEvent) -> {
			if (expandAll.isSelected()) {
				expandVisibleRows(tree);
			}
		});
		expandAll.addActionListener((ActionEvent) -> {
			resetInternal();
			if (expandAll.isSelected()) {
				expandVisibleRows(tree);
			} else {
				collapseAll(tree, collapseUntilRow);
			}
		});
	}
	
	private void resetInternal() {
		expandedUntilRow = 0;
	}
	
	public void reset(JTree tree) {
		resetInternal();
		if (expandAll.isSelected()) {
			expandVisibleRows(tree);
		}
	}
	
	private static boolean debugExpand = DebugManager.isFlagEnabled("debugExpand", false);
	
	private void expandVisibleRows(JTree tree) {
		Rectangle visibleRect = tree.getVisibleRect();
		int count = 0;
		int iterations = 0;
		int startRow = expandedUntilRow;
		int endRow =  tree.getClosestRowForLocation(visibleRect.x + visibleRect.width, visibleRect.y + visibleRect.height);
		
		if (startRow >= endRow) {
			if (debugExpand) {
				System.out.printf("TreeExpandAllManager.expandVisibleRows() Already Expanded! count=%d, iterations=%d startRow=%d, endRow=%d %n", count ,iterations, expandedUntilRow, endRow);
			}
			return;
		}
		
		for (;; iterations++) {
			for (int row = startRow; row <= endRow; row++, count++) {
				doExpand(tree, row);
			}
			int newEndRow = tree.getClosestRowForLocation(visibleRect.x + visibleRect.width, visibleRect.y + visibleRect.height);
			if (newEndRow == endRow) {
				break; // done (not to repeat on fully collapsed tree since then, endRow changed and is not fully expanded)
			}
			startRow = endRow;
			endRow = newEndRow;
		}
		if (debugExpand) {
			System.out.printf("TreeExpandAllManager.expandVisibleRows() count=%d, iterations=%d startRow=%d, endRow=%d %n", count ,iterations, expandedUntilRow, endRow);
		}
		expandedUntilRow = endRow;
	}
	
	private void doExpand(JTree tree, int row) {
		if (mayExpand(tree, row)) {
			tree.expandRow(row);
		}
	}
	
	protected boolean mayExpand(JTree tree, int row) {
		return true; // subclassing hook
	}
	
	private static void collapseAll(JTree tree, int collapseUntilRow) {
		for (int row = tree.getRowCount() - 1; row >= collapseUntilRow; row--) {
			tree.collapseRow(row);
		}
	}
}
