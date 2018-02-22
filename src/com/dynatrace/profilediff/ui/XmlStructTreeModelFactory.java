package com.dynatrace.profilediff.ui;

import com.dynatrace.profilediff.XmlElement;
import com.dynatrace.profilediff.XmlStruct;

public class XmlStructTreeModelFactory {
	
	static XmlStructTreeModel all(XmlStruct xml) {
		return new XmlStructTreeModel(xml);
	}
	
	static XmlStructTreeModel normal(XmlStruct xml, UserObjectLogic userObjectLogic) {
		return new XmlStructTreeModel.Normal(xml, userObjectLogic);
	}
	
	static XmlStructTreeModel normalChangesOnly(XmlStruct xml, UserObjectLogic userObjectLogic) {
		return new XmlStructTreeModel.Normal(xml, userObjectLogic) {
			@Override
			public boolean show(XmlElement element) {
				return element.hasDirectStructureChange() || element.hasDescendantStructureChange() || element.hasDirectAttributeChange() || element.hasDescendantAttributeChange();
			}
		};
	}
	
	static XmlStructTreeModel normalStructuralChangesOnly(XmlStruct xml, UserObjectLogic userObjectLogic) {
		return new XmlStructTreeModel.Normal(xml, userObjectLogic) {
			@Override
			public boolean show(XmlElement element) {
				return element.hasDirectStructureChange() || element.hasDescendantStructureChange();
			}
		};
	}
	
	static XmlStructTreeModel normalAttributeChangesOnly(XmlStruct xml, UserObjectLogic userObjectLogic) {
		return new XmlStructTreeModel.Normal(xml, userObjectLogic) {
			@Override
			public boolean show(XmlElement element) {
				return element.hasDirectAttributeChange() || element.hasDescendantAttributeChange();
			}
		};
	}
	
	static XmlStructTreeModel combined(XmlStruct xml, UserObjectLogic userObjectLogic) {
		return new XmlStructTreeModel.Combined(xml, userObjectLogic);
	}
	
	static XmlStructTreeModel combinedChangesOnly(XmlStruct xml, UserObjectLogic userObjectLogic) {
		return new XmlStructTreeModel.Combined(xml, userObjectLogic) {
			@Override
			boolean show(XmlElement element) {
				return element.hasDirectStructureChange() || element.hasDescendantStructureChange() || element.hasDirectAttributeChange() || element.hasDescendantAttributeChange() || element.hasPeerDescendantStructureChange();
			}
		};
	}
	
	static XmlStructTreeModel combinedStructuralChangesOnly(XmlStruct xml, UserObjectLogic userObjectLogic) {
		return new XmlStructTreeModel.Combined(xml, userObjectLogic) {
			@Override
			boolean show(XmlElement element) {
				return element.hasDirectStructureChange() || element.hasDescendantStructureChange() || element.hasPeerDescendantStructureChange();
			}
		};
	}
	
	static XmlStructTreeModel combinedAttributeChangesOnly(XmlStruct xml, UserObjectLogic userObjectLogic) {
		return new XmlStructTreeModel.Combined(xml, userObjectLogic) {
			@Override
			boolean show(XmlElement element) {
				return element.hasDirectAttributeChange() || element.hasDescendantAttributeChange();
			}
			
			@Override
			boolean showPeer(XmlElement element) {
				return false;
			}
		};
	}
	
	static XmlStructTreeModel combinedInsertionsOnly(XmlStruct xml, UserObjectLogic userObjectLogic) {
		return new XmlStructTreeModel.Combined(xml, userObjectLogic) {
			@Override
			boolean show(XmlElement element) {
				return element.hasDirectStructureChange() || element.hasDescendantStructureChange();
			}
			
			@Override
			boolean showPeer(XmlElement element) {
				return false;
			}
		};
	}
	
	static XmlStructTreeModel combinedAdditionsOnly(XmlStruct xml, UserObjectLogic userObjectLogic) {
		return new XmlStructTreeModel.Combined(xml, userObjectLogic) {
			@Override
			boolean show(XmlElement element) {
				return element.hasDirectStructureChange() || element.hasDescendantStructureChange() || element.hasDirectAttributeChange() || element.hasDescendantAttributeChange();
			}
			
			@Override
			boolean showPeer(XmlElement element) {
				return false;
			}
		};
	}
	
	static XmlStructTreeModel combinedDeletionsOnly(XmlStruct xml, UserObjectLogic userObjectLogic) {
		return new XmlStructTreeModel.Combined(xml, userObjectLogic) {
			@Override
			boolean show(XmlElement element) {
				return element.hasPeerDescendantStructureChange();
			}
		};
	}
}
