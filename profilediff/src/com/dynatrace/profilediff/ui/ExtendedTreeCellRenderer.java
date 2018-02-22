package com.dynatrace.profilediff.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

import com.dynatrace.common.swing.ui.TreeCheckboxManager;
import com.dynatrace.profilediff.XmlElement;
import com.dynatrace.profilediff.ui.decorationmodel.DecorationModel;

@SuppressWarnings("serial")
class ExtendedTreeCellRenderer extends JPanel implements TreeCellRenderer {
	private final DefaultTreeCellRenderer delegate;
	private final TreeCheckboxManager manager;
	private final DecorationModel<XmlElement> decorationModel;
	private boolean drawCheckbox;
	
	void setDrawCheckbox(boolean drawCheckbox) {
		this.drawCheckbox = drawCheckbox;
	}

	ExtendedTreeCellRenderer(DefaultTreeCellRenderer delegate, TreeCheckboxManager manager, DecorationModel<XmlElement> decorationModel) {
		this.delegate = delegate;
		this.manager = manager;
		this.decorationModel = decorationModel;
		setLayout(new BorderLayout());
		setOpaque(false);
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
		if (!(value instanceof XmlElement)) {
			return delegate.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
		}
		XmlElement element = (XmlElement) value;
		
		Icon icon = decorationModel.getIcon(element);
		if (icon != null) {
			delegate.setOpenIcon(icon);
			delegate.setClosedIcon(icon);
			delegate.setLeafIcon(icon);
		} else {
			delegate.setOpenIcon(delegate.getDefaultOpenIcon());
			delegate.setClosedIcon(delegate.getDefaultClosedIcon());
			delegate.setLeafIcon(delegate.getDefaultLeafIcon());
		}
		
		removeAll(); // actually important to work correctly!
		if (drawCheckbox) {
			add(manager.getCheckbox(element), BorderLayout.WEST);
		}
		String text = getElementText(element);
		add(delegate.getTreeCellRendererComponent(tree, text, selected, expanded, leaf, row, hasFocus), BorderLayout.CENTER);
		String tag = element.getTag();
		if (tag != null) {
			JLabel label;
			add(label = new JLabel(tag), BorderLayout.EAST);
			label.setForeground(Color.lightGray);
		}
		return this;
	}
	
	private String getElementText(XmlElement element) {
		String modelText = decorationModel.getText(element);
		if (modelText != null) {
			return modelText;
		}
		if (element.firstDiscriminator != null) {
			return element.firstDiscriminator;
		}
		return element.rawName; // the name is shown with tooltip!
	}
}
