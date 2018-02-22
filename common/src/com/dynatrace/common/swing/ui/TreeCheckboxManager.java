package com.dynatrace.common.swing.ui;

import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.JCheckBox;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

public abstract class TreeCheckboxManager {
	
	public static class ModelCheckbox {
		private final AbstractButton checkbox;
		private int modCount;
		private int validCount;

		private ModelCheckbox(AbstractButton checkbox) {
			this.checkbox = checkbox;
		}
	}
	
	private final List<JTree> usingTrees = new ArrayList<>();
	
	private static int nextModCount;
	private int modCount = ++nextModCount;
	
	private static int nextValidCount;
	private int validCount = ++nextValidCount;
	
	public void addUsingTree(JTree tree) {
		usingTrees.add(tree);
	}
	
	public void clear() {
		modCount = ++nextModCount;
	}
	
	public void invalidate() {
		validCount = ++nextValidCount;
	}
	
	public final JCheckBox getCheckbox(Object element) {
		ModelCheckbox modelCheckbox = getCheckboxState(element);
		if (modelCheckbox == null) {
			modelCheckbox = new ModelCheckbox(new JCheckBox());
			setCheckboxState(element, modelCheckbox);
		}
		if (modelCheckbox.modCount != modCount) {
			modelCheckbox.checkbox.setSelected(getInitialSelection(element));
		}
		
		modelCheckbox.modCount = modCount;
		modelCheckbox.validCount = validCount;
		return (JCheckBox) modelCheckbox.checkbox;
	}
	
	protected boolean getInitialSelection(Object element) {
		return false;
	}
	
	public void addListeners(JTree tree) {
		tree.addMouseListener(createMouseListener(tree));
		tree.addKeyListener(createKeyListener(tree));
	}
	
	private MouseListener createMouseListener(JTree tree) {
		return new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getClickCount() > 1) {
					return;
				}
				TreePath path = tree.getPathForLocation(e.getX(), e.getY());
				if (path == null) {
					return;
				}
				Object element = path.getLastPathComponent();
				if (element == null) {
					return;
				}
				ModelCheckbox modelCheckbox = getCheckboxState(element);
				if (modelCheckbox != null && modelCheckbox.validCount == validCount) {
					int hotspot = modelCheckbox.checkbox.getSize().width;
					Rectangle pathBounds = tree.getPathBounds(path);
			        boolean clicked = pathBounds != null && e.getX() <= pathBounds.x + hotspot;
			        if (clicked) {
						toggleUserTriggered(modelCheckbox.checkbox, element, !modelCheckbox.checkbox.isSelected(), tree);
						toggleDone();
						treeChanged();
						e.consume(); // has no effect, double click still expands/collapses
			        }
				}
			}
		};
	}
	
	private KeyListener createKeyListener(JTree tree) {
		return new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() != ' ') {
					return;
				}
				TreePath[] paths = tree.getSelectionPaths();
				if (paths == null) {
					return;
				}
				Boolean newSelected = null;
				for (TreePath path : paths) {
					Object element = path.getLastPathComponent();
					if (element == null) {
						return;
					}
					ModelCheckbox modelCheckbox = getCheckboxState(element);
					if (modelCheckbox != null && modelCheckbox.validCount == validCount) {
						if (newSelected == null) {
							newSelected = !modelCheckbox.checkbox.isSelected();
						}
						toggleUserTriggered(modelCheckbox.checkbox, element, newSelected, tree);
					}
				}
				toggleDone();
				treeChanged();
				e.consume();
			}
		};
	}
	
	private void toggleUserTriggered(AbstractButton checkbox, Object element, boolean newSelected, JTree tree) {
		if (toggle(element, checkbox.isSelected(), newSelected, /*internalTrigger*/ null, tree)) {
			checkbox.setSelected(newSelected);
		}
	}
	
	private void toggleInternallyTriggered(AbstractButton checkbox, Object element, boolean newSelected, Object internalTrigger) {
		if (toggle(element, checkbox.isSelected(), newSelected, internalTrigger, /*tree*/ null)) {
			checkbox.setSelected(newSelected);
		}
	}
	
	protected boolean toggle(Object element, boolean oldSelected, boolean newSelected, Object internalTrigger, JTree tree) {
		// subclassing hook
		return true; // return true if toggle is allowed (selected value will be applied), false to veto
	}
	
	protected void toggleDone() {
	}
	
	public boolean isSelected(Object element) {
		ModelCheckbox modelCheckbox = getCheckboxState(element);
		if (modelCheckbox != null && modelCheckbox.modCount == modCount) {
			return modelCheckbox.checkbox.isSelected();
		}
		return getInitialSelection(element);
	}
	
	public void setSelected(Object element, boolean newSelected, Object internalTrigger) {
		toggleInternallyTriggered(getCheckbox(element), element, newSelected, internalTrigger);
		toggleDone();
		treeChanged();
	}
	
	private void treeChanged() {
		for (JTree tree : usingTrees) {
			tree.treeDidChange();
		}
	}
	
	protected abstract ModelCheckbox getCheckboxState(Object element);
	
	protected abstract void setCheckboxState(Object element, ModelCheckbox value);
}
