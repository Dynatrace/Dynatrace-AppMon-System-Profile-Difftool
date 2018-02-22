package com.dynatrace.profilediff.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import com.dynatrace.profilediff.XmlElement;
import com.dynatrace.profilediff.XmlStruct;
import com.dynatrace.profilediff.XmlUtil;

public class XmlStructTreeModel implements TreeModel {
	
	static class ModelChildren {
		private final List<XmlElement> data;
		private final int modCount;
		
		private ModelChildren(List<XmlElement> data, int modCount) {
			this.data = data;
			this.modCount = modCount;
		}
	}
	
	private final XmlStruct xml;
	
	XmlStructTreeModel(XmlStruct xml) {
		this.xml = xml;
	}

	@Override
	public Object getRoot() {
		return xml.root();
	}
	
	static XmlElement asElement(Object o) {
		return (XmlElement) o;
	}

	protected List<XmlElement> getChildren(Object o) {
		return asElement(o).children;
	}
	
	@Override
	public Object getChild(Object parent, int index) {
		return getChildren(parent).get(index);
	}

	@Override
	public int getChildCount(Object parent) {
		return getChildren(parent).size();
	}

	@Override
	public boolean isLeaf(Object node) {
		return getChildren(node).isEmpty();
	}

	@Override
	public void valueForPathChanged(TreePath path, Object newValue) {
	}

	@Override
	public int getIndexOfChild(Object parent, Object child) {
		return getChildren(parent).indexOf(child);
	}

	@Override
	public void addTreeModelListener(TreeModelListener l) {
	}

	@Override
	public void removeTreeModelListener(TreeModelListener l) {
	}
	
	private static abstract class WithModelChildren extends XmlStructTreeModel {
		private static int nextModCount;
		private final int modCount = ++nextModCount;
		final UserObjectLogic userObjectLogic;
		
		WithModelChildren(XmlStruct xml, UserObjectLogic userObjectLogic) {
			super(xml);
			this.userObjectLogic = userObjectLogic;
		}
		
		@Override
		protected final List<XmlElement> getChildren(Object o) {
			XmlElement element = asElement(o);
			ModelChildren modelChildren = userObjectLogic.getModelChildren(element);
			if (modelChildren == null || modelChildren.modCount != modCount) {
				modelChildren = new ModelChildren(createModelView(element), modCount);
				userObjectLogic.setModelChildren(element, modelChildren);
//				System.out.println("FETCH CHILDREN " + modCount);
			}
			
			return modelChildren.data;
		}
		
		abstract List<XmlElement> createModelView(XmlElement parent);
	}
	
	static class Normal extends WithModelChildren {
		
		Normal(XmlStruct xml, UserObjectLogic userObjectLogic) {
			super(xml, userObjectLogic);
		}
		
		@Override
		List<XmlElement> createModelView(XmlElement parent) {
			List<XmlElement> result = new ArrayList<>();
			
			for	(XmlElement element : parent.children) {
				if (showInternal(element) && show(element)) {
					result.add(element);
				}
			}
			return result;
		}
		
		final boolean showInternal(XmlElement element) {
			return XmlUtil.isFilteredVisible(element);
		}
		
		boolean show(XmlElement element) {
			return true;
		}
	}
	
	static class Combined extends WithModelChildren {
		
		Combined(XmlStruct xml, UserObjectLogic userObjectLogic) {
			super(xml, userObjectLogic);
		}
		
		@Override
		List<XmlElement> createModelView(XmlElement parent) {
			List<XmlElement> result = new ArrayList<>();
			
			for	(XmlElement element : parent.children) {
				if (showInternal(element) && show(element)) {
					result.add(element);
				}
			}
			if (parent.peer != null) {
				for	(XmlElement element : parent.peer.children) {
					if (showInternal(element) && showPeer(element)) {
						result.add(element);
					}
				}
				/*
				 * bring parent peer's children in right positions
				 */
				Collections.sort(result, (XmlElement left, XmlElement right) -> left.openTag.prevEnd - right.openTag.prevEnd);
			}
			return result;
		}

		final boolean showInternal(XmlElement element) {
			return XmlUtil.isFilteredVisible(element);
		}
		
		boolean showPeer(XmlElement element) {
			return element.hasDirectStructureChange();
		}

		boolean show(XmlElement element) {
			return true;
		}
	}
}
