package com.dynatrace.profilediff.ui;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.swing.tree.TreeModel;

import org.junit.Assert;

import com.dynatrace.profilediff.TestBase;
import com.dynatrace.profilediff.XmlElement;
import com.dynatrace.profilediff.XmlStruct;

public abstract class ModelTestBase extends TestBase {
	
	static List<String> modelToStrings(TreeModel model) {
		List<String> list = new ArrayList<>();
		modelToStrings0(model, (XmlElement) model.getRoot(), "", list);
		return list;
	}
	
	private static void modelToStrings0(TreeModel model, XmlElement parent, String prefix, List<String> list) {
		list.add(prefix + toShortStateString(parent));
		for (int i = 0; i < model.getChildCount(parent); i++) {
			XmlElement element = (XmlElement) model.getChild(parent, i);
			modelToStrings0(model, element, prefix + "  ", list);
		}
	}
	
	static String toShortStateString(XmlElement element) {
		String tag = element.getTag();
		return tag != null ? tag + " " + element.name : element.name;
	}
	
	static List<String> toShortStateStrings(List<XmlElement> list) {
		return new AbstractList<String>() {
			
			@Override
			public String get(int index) {
				return toShortStateString(list.get(index));
			}
			
			@Override
			public int size() {
				return list.size();
			}
		};
	}
	
	static List<String> toAttributeStrings(Object[][] tableData) {
		return new AbstractList<String>() {
			
			@Override
			public String get(int index) {
				return toAttributeString(tableData[index]);
			}
			
			@Override
			public int size() {
				return tableData.length;
			}
		};
	}
	
	static String toAttributeString(Object[] row) {
		StringBuilder buf = new StringBuilder();
		String sep = "";
		
		SpecialTableCell.Kind firstCellKind = null;
		
		for (Object cell : row) {
			if (cell instanceof SpecialTableCell) {
				SpecialTableCell.Kind kind = ((SpecialTableCell) cell).kind;
				if (firstCellKind == null) {
					firstCellKind = kind;
					buf.append(firstCellKind).append(": ");
				} else {
					assert kind == firstCellKind;
				}
			}
			if (cell != null) {
				buf.append(sep).append(cell);
				sep = " | ";
			}
		}
		
		return buf.toString();
	}
	
	static XmlElement find(XmlStruct xml, String s) {
		for (XmlElement element : xml.elements) {
			if (s.equals(toShortStateString(element))) {
				return element;
			}
		}
		return null;
	}
	
	static final EnumSet<OneWayModel.ChangeItem> uncheckedModels1Way = EnumSet.allOf(OneWayModel.ChangeItem.class);
	static final EnumSet<OneWayModel.ChangeItem> uncheckedChangedElements1Way = EnumSet.noneOf(OneWayModel.ChangeItem.class);
	
	static final EnumSet<TwoWayModel.ChangeItem> uncheckedModels2Way = EnumSet.allOf(TwoWayModel.ChangeItem.class);
	static final EnumSet<TwoWayModel.ChangeItem> uncheckedChangedElements2Way = EnumSet.noneOf(TwoWayModel.ChangeItem.class);
	
	final List<XmlElement> checkCollectChangedElements(List<String> expected, int nExpected, OneWayModel.ChangeItem item, XmlStruct xmlLeft, XmlStruct xmlRight) {
		List<XmlElement> collected = new ArrayList<XmlElement>();
		CommonModel.collectChangedElements(xmlLeft, item, collected, collected, collected);
		CommonModel.collectChangedElements(xmlRight, item, collected, collected, collected);
		checkList(expected, toShortStateStrings(collected));
		Assert.assertEquals(nExpected, collected.size());
		uncheckedChangedElements1Way.remove(item);
		return collected;
	}
	
	final List<XmlElement> checkCollectChangedElements(List<String> expected, int nExpected, TwoWayModel.ChangeItem item, XmlStruct xml) {
		List<XmlElement> collected = new ArrayList<XmlElement>();
		CommonModel.collectChangedElements(xml, item, collected, collected, collected);
		checkList(expected, toShortStateStrings(collected));
		Assert.assertEquals("Number of changes", nExpected, collected.size());
		uncheckedChangedElements2Way.remove(item);
		return collected;
	}
	
	final void checkPeers(List<String> expected, List<XmlElement> elements) {
		checkList(expected, insertPeers(elements));
	}
	
	private static List<String> insertPeers(List<XmlElement> list) {
		List<String> result = new ArrayList<>();
		for (XmlElement element : list) {
			result.add("element " + toShortStateString(element));
			result.add("---peer " + (element.peer == null ? "<none>" : toShortStateString(element.peer)));
		}
		return result;
		
	}
	
	private static final UserObjectLogic userObjectLogic = UserObjectLogic.newDefault();
	
	final void checkModel(List<String> expected, OneWayModel.ChangeItem item, XmlStruct xmlLeft, XmlStruct xmlRight) {
		TreeModel model = OneWayModel.createTreeModel(item, xmlRight, userObjectLogic);
		checkList(expected, modelToStrings(model));
		uncheckedModels1Way.remove(item);
	}
	
	final void checkModel(List<String> expected, TwoWayModel.ChangeItem item, XmlStruct xml) {
		TreeModel model = TwoWayModel.createTreeModel(item, xml, userObjectLogic);
		checkList(expected, modelToStrings(model));
		uncheckedModels2Way.remove(item);
	}
}
