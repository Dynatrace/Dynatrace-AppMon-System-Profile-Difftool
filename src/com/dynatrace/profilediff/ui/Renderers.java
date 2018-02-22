package com.dynatrace.profilediff.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

import com.dynatrace.common.swing.ui.TreeCheckboxManager;
import com.dynatrace.profilediff.XmlElement;
import com.dynatrace.profilediff.ui.CommonModel.ChangeItemInterface;
import com.dynatrace.profilediff.ui.decorationmodel.DecorationModel;

class Renderers {
	
	static final Color textAddedColor = Color.cyan;
	static final Color textRemovedColor = Color.green;
	static final Color textAttrChangedColor = Color.orange;
	static final Color textSelectionColor = Color.white;
	
	static final Color changedColor = Color.black;
	static final Color unchangedColor = Color.lightGray;
	static final Color addedColor = Color.blue;
	static final Color removedColor = Color.green.darker().darker();
	
	static void initTreeCellRenderers(JTree tree, TreeCheckboxManager treeCheckboxManager, DecorationModel<XmlElement> decorationModel, Color mainColor, ChangeItemInterface item) {
		Font normalFont = tree.getFont();
		DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
		ExtendedTreeCellRenderer extendedRenderer = new ExtendedTreeCellRenderer(renderer, treeCheckboxManager, decorationModel);
		
		tree.setCellRenderer(new TreeCellRenderer() {
			@Override
			public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
				Font font = normalFont;
				renderer.setTextNonSelectionColor(mainColor);
				renderer.setTextSelectionColor(Color.white);
				renderer.setFont(font);
				
				extendedRenderer.setDrawCheckbox(false);
				
				if (value instanceof XmlElement) {
					XmlElement element = (XmlElement) value;
					if (element.hasDirectStructureChange()) {
						font = font.deriveFont(Font.BOLD);
						renderer.setFont(font);
					}
					if (element.hasDirectAttributeChange()) {
						font = font.deriveFont(Font.BOLD);
						renderer.setFont(font);
					}
					
					if (element.hasDirectAttributeChange() || element.hasDescendantAttributeChange()) {
						renderer.setTextNonSelectionColor(changedColor);
					}
					if (element.hasDirectStructureChange() || element.hasDescendantStructureChange() || element.hasPeerDescendantStructureChange()) {
						renderer.setTextNonSelectionColor(changedColor);
					}
					if (element.hasDirectStructureChange() || element.hasParentStructureChange()) {
						if (element.isInsertion()) {
							renderer.setTextNonSelectionColor(addedColor);
						}
						if (element.isDeletion()) {
							renderer.setTextNonSelectionColor(removedColor);
						}
					}
					if (element.hasDirectStructureChange()) {
						if (element.isInsertion()) {
							extendedRenderer.setDrawCheckbox(item.containsInsertions());
						}
						if (element.isDeletion()) {
							extendedRenderer.setDrawCheckbox(item.containsDeletions());
						}
					} else if (element.hasDirectAttributeChange()) {
						extendedRenderer.setDrawCheckbox(item.containsAttributeChanges());
					}
				}
				return extendedRenderer.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
			}
		});
	}
	
	static void initTableCellRenderers(JTable table, Color mainColor) {
		Color backgroundColor = Color.white;
		Color changedColor = Color.black;
		Color headerColor = Color.darkGray;
		Color headerBackgroundColor = Color.orange;
		
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer(); 
		
		table.setDefaultRenderer(Object.class, new TableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
				renderer.setForeground(mainColor);
				renderer.setBackground(backgroundColor);
				
				if (value instanceof SpecialTableCell) {
					SpecialTableCell cell = (SpecialTableCell) value;
					switch (cell.kind) {
						case ElementHeader: 
							renderer.setForeground(headerColor);
							renderer.setBackground(headerBackgroundColor);
							break;
							
						case AttributeValueEqual:
							// default colors already set
							break;
						
						case AttributeValueDifferent:
							renderer.setForeground(changedColor);
							break;
							
						case AttributeAdded:
							renderer.setForeground(addedColor);
							break;
							
						case AttributeRemoved:
							renderer.setForeground(removedColor);
							break;
					}
				}
				return renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			}
		});
	}
}
