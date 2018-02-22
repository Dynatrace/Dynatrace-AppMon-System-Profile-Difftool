package com.dynatrace.profilediff.togglemodel;

import com.dynatrace.common.conf.DebugManager;
import com.dynatrace.profilediff.XmlElement;
import com.dynatrace.profilediff.XmlStruct;
import com.dynatrace.profilediff.XmlUtil;

public class SystemProfileToggleModel implements ToggleModel<XmlElement> {
	
	private static final String TRANSACTIONS = "dynatrace/systemprofile/transactions";
	private static final String MEASURES = "dynatrace/systemprofile/measures";
	private static final String CONFIGS = "dynatrace/systemprofile/configurations";
	private static final String SENSORCONFIG = "dynatrace/systemprofile/configurations/configuration/sensorconfig";
	private static final String AGENTGROUPS = "dynatrace/systemprofile/agentgroups";

	private static boolean debugToggle = DebugManager.isFlagEnabled("debugToggle", false);
	
	private SelectionInterface<XmlElement> selectionInterface;
	
	/**
	 * Return <code>null</code> if ok, an error message otherwise.
	 */
	@Override
	public void toggle(XmlElement element, boolean selected) throws ToggleVeto {
		assert selectionInterface != null;
		
		if (debugToggle) {
			System.out.printf("Toggle element %s%n", element);
		}
		
		if (element.hasDirectStructureChange()) {
			if (element.rawPath.startsWith(MEASURES)) {
				toggleMeasure(element, traverseUp(element, MEASURES), selected);
			} else if (element.rawPath.startsWith(TRANSACTIONS)) {
				toggleTransaction(element, traverseUp(element, TRANSACTIONS), selected);
			} else if (element.rawPath.startsWith(SENSORCONFIG)) {
				toggleSensorConfig(element, traverseUp(element, SENSORCONFIG), selected);
			} else if (element.rawPath.startsWith(AGENTGROUPS)) {
				toggleAgentGroup(element, traverseUp(element, AGENTGROUPS), selected);
			}
		}
	}
	
	private static XmlElement traverseUp(XmlElement element, String path) {
		if (element.rawPath.equals(path)) {
			return element;
		}
		while (element.parent != null && !element.parent.rawPath.equals(path)) {
			element = element.parent;
		}
		return element;
	}
	
	private void toggleTransaction(XmlElement element, XmlElement transaction, boolean selected) {
		XmlStruct xml = transaction.xml;
		String transactionid = transaction.attributes.get("id");
		for (XmlElement measures : xml.findByRawPath(MEASURES)) {
			for (XmlElement measure : xml.getAllDescendants(measures)) {
				if (measure.hasDirectStructureChange() || measure.hasParentStructureChange() || measure.hasDescendantStructureChange()) {
					String transactionname = measure.attributes.get("transactionname");
					if (transactionname != null) {
						if (transactionname.equals(transactionid)) {
							if (measure.hasParentStructureChange()) {
								measure = XmlUtil.traverseToChange(measure);
							}
							if (measure.hasDirectStructureChange()) {
								selectionInterface.setSelected(measure, selected);
							}
						}
						continue;
					}
					
					if (selected) {
						String id = measure.attributes.get("id");
						String metricgroupid = measure.attributes.get("metricgroupid");
						String metricid = measure.attributes.get("metricid");
						if (id != null & metricgroupid != null && metricid != null) {
							for (XmlElement t : xml.getAllDescendants(transaction)) {
								String refmeasure = t.attributes.get("refmeasure");
								String refmetricgroup = t.attributes.get("refmetricgroup");
								String refmetric = t.attributes.get("refmetric");
								if (refmeasure != null && refmetricgroup != null && refmetric != null) {
									if (refmeasure.equals(id) && refmetricgroup.equals(metricgroupid) && refmetric.equals(metricid)) {
										if (measure.hasDirectStructureChange()) {
											selectionInterface.setSelected(measure, selected);
										} else if (measure.hasDescendantStructureChange()) {
											for (XmlElement m : xml.getAllDescendants(measure)) { // e.g. thresholds
												if (m.hasDirectStructureChange()) {
													selectionInterface.setSelected(m, selected);
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}
	
	private void toggleMeasure(XmlElement element, XmlElement measure, boolean selected) throws ToggleVeto {
		XmlStruct xml = measure.xml;
		String desc = element == measure ? "Measure" : "Parent measure";
		
		if (debugToggle) {
			System.out.printf("Toggle measure '%s' element='%s', selected=%b", measure, element, selected);
		}
		
		for (XmlElement m : xml.getAllDescendants(measure)) {
			String transactionname = m.attributes.get("transactionname");
			if (transactionname != null) {
				
				for (XmlElement transactions : xml.findByRawPath(TRANSACTIONS)) {
					for (XmlElement transaction : transactions.children) {
						if (transaction.hasDirectStructureChange() || transaction.hasParentStructureChange()) {
							String id = transaction.attributes.get("id");
							if (transactionname.equals(id)) {
								throw new ToggleVeto(String.format("%s is internally referenced by transaction '%s'", desc, transactionname));
							}
						}
					}
				}
			}
		}
				
		/*
		 * see if it's legal to de-select a measure
		 */
		if (!selected) {
			String id = measure.attributes.get("id");
			String metricgroupid = measure.attributes.get("metricgroupid");
			String metricid = measure.attributes.get("metricid");
			
			for (XmlElement transactions : xml.findByRawPath(TRANSACTIONS)) {
				for (XmlElement transaction : xml.getAllDescendants(transactions)) {
					if (transaction.hasDirectStructureChange() || transaction.hasParentStructureChange()) {
						String refmeasure = transaction.attributes.get("refmeasure");
						String refmetricgroup = transaction.attributes.get("refmetricgroup");
						String refmetric = transaction.attributes.get("refmetric");
						if (id.equals(refmeasure) && metricgroupid.equals(refmetricgroup) && metricid.equals(refmetric)) {
							if (transaction.hasParentStructureChange()) {
								transaction = XmlUtil.traverseToChange(transaction);
							}
							boolean userSelected = selectionInterface.isSelected(transaction);
							if (userSelected) {
								throw new ToggleVeto(String.format("%s is referenced by transaction '%s'", desc, transaction.attributes.get("id")));
							}
						}
					}
				}
			}
		}
	}
	
	private void toggleSensorConfig(XmlElement element, XmlElement sensorconfig, boolean selected) {
		if (selected) {
			String refagentgroup = sensorconfig.attributes.get("refagentgroup");
			XmlStruct xml = sensorconfig.xml;
			for (XmlElement agentgroups : xml.findByRawPath(AGENTGROUPS)) {
				for (XmlElement agentgroup : agentgroups.children) {
					if (agentgroup.hasDirectStructureChange() || agentgroup.hasParentStructureChange()) {
						String id = agentgroup.attributes.get("id");
						if (id != null && id.equals(refagentgroup)) {
							if (agentgroup.hasParentStructureChange()) {
								agentgroup = XmlUtil.traverseToChange(agentgroup);
							}
							selectionInterface.setSelected(agentgroup, selected);
						}
					}
				}
			}
		}
	}

	private void toggleAgentGroup(XmlElement element, XmlElement agentgroup, boolean selected) throws ToggleVeto {
		if (!selected) {
			XmlStruct xml = agentgroup.xml;
			String agentgroupid = agentgroup.attributes.get("id");
			String desc = element == agentgroup ? "Agent group" : "Parent agent group";
			
			for (XmlElement configs : xml.findByRawPath(CONFIGS)) {
				for (XmlElement config : xml.getAllDescendants(configs)) {
					if (config.rawPath.equals(SENSORCONFIG)) {
						if (config.hasDirectStructureChange() || config.hasParentStructureChange() || config.hasDescendantStructureChange()) {
							String refagentgroup = config.attributes.get("refagentgroup");
							if (refagentgroup != null && refagentgroup.equals(agentgroupid)) {
								if (selectionInterface.isSelected(config)) {
									throw new ToggleVeto(String.format("%s is referenced by sensor config", desc));
								}
							}
						}
					}
				}
			}
		}
	}
	
	@Override
	public void setSelectionInterface(SelectionInterface<XmlElement> selectionInterface) {
		this.selectionInterface = selectionInterface;
	}
}
