package com.dynatrace.profilediff.ui;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import com.dynatrace.common.swing.ui.TreeCheckboxManager;
import com.dynatrace.profilediff.XmlElement;
import com.dynatrace.profilediff.XmlStruct;
import com.dynatrace.profilediff.XmlUtil;

class UITools {
	
	static TreePath makeTreePathOneWay(XmlElement element) {
		ArrayList<XmlElement> pathComponents = new ArrayList<>();
		while (element != null) {
			pathComponents.add(0, element.isDeletion() && element.peer != null ? element.peer : element);
			element = element.parent;
		}
		
		return new TreePath(pathComponents.toArray(new XmlElement[pathComponents.size()]));
	}
	
	static TreePath makeTreePath(XmlElement element) {
		ArrayList<XmlElement> pathComponents = new ArrayList<>();
		while (element != null) {
			pathComponents.add(0, element);
			element = element.parent;
		}
		
		return new TreePath(pathComponents.toArray(new XmlElement[pathComponents.size()]));
	}
	
	static void showElementInTree(JTree tree, TreePath path, boolean select, boolean scroll) {
		if (select) {
			tree.setSelectionPath(path);
		} else {
			tree.expandPath(path.getParentPath());
		}
		if (scroll) {
			tree.scrollPathToVisible(path);
		}
	}

	static String loadingString(XmlInput input) {
		return input == null ? "" : "Loading " + input.name + "...";
	}
	
	@SuppressWarnings("serial")
	static JTree createTree() {
		JTree tree = new JTree((DefaultMutableTreeNode) null) {
			@Override
			public String getToolTipText(MouseEvent e) {
				TreePath treePath = getPathForLocation(e.getX(), e.getY());
				if (treePath == null) {
					return null;
				}
				XmlElement element = (XmlElement) treePath.getLastPathComponent();
				if (element == null) {
					return null;
				}
				String tag = element.getDescendantsTag();
				if (tag == null) {
					tag = ""; //TODO
				}
				return String.format("<html>%s<br/>%s<br/>%s<br/>distance=%d cardinality=%d</html>", tag, element.path, element.name, element.distance, element.cardinality);
			}
		};
		ToolTipManager.sharedInstance().registerComponent(tree);
		return tree;
	}
	
	private static final Object INTERNAL_TRIGGER_CHECK_ALL = "INTERNAL_TRIGGER_CHECK_ALL";
	
	static void checkAll(CommonModel.ChangeItemInterface item, boolean checkAll, TreeCheckboxManager treeCheckboxManager, XmlStruct... xmls) {
		List<XmlElement> elements = new ArrayList<>();
		for (XmlStruct xml : xmls) {
			if (xml != null) {
				CommonModel.collectChangedElements(xml, item, elements, elements, elements);
			}
		}
		checkElements(elements, checkAll, treeCheckboxManager, INTERNAL_TRIGGER_CHECK_ALL);
	}
	
	private static void checkElements(List<XmlElement> elements, boolean selected, TreeCheckboxManager treeCheckboxManager, Object internalTrigger) {
		for (XmlElement element : elements) {
			treeCheckboxManager.setSelected(element, selected, internalTrigger);
		}
	}

	static void filter(XmlStruct xml, boolean mayChangeCheckbox, JTextField filter, AbstractButton filterCheck, boolean showParentPeers) {
		if (xml == null) {
			return;
		}
		
		String criterion = filter.getText();
		
		if (mayChangeCheckbox) {
			if (criterion.isEmpty()) {
				filterCheck.setSelected(false);
				XmlUtil.resetFilter(xml);
			} else {
				filterCheck.setSelected(true);
				XmlUtil.filter(xml, criterion, showParentPeers);
			}
		} else {
			if (filterCheck.isSelected()) {
				XmlUtil.filter(xml, criterion, showParentPeers);
			} else {
				XmlUtil.resetFilter(xml);
			}
		}
	}
}
