package com.dynatrace.profilediff.ui;

import java.io.IOException;
import java.util.Arrays;

import javax.xml.stream.XMLStreamException;

import org.junit.Before;
import org.junit.Test;

import com.dynatrace.profilediff.XmlUtil;
import com.dynatrace.profilediff.ui.OneWayModel.ChangeItem;

public class OneWayModelFilterDeletionTest extends ModelTest_GEH_SystemProfile_Base {
	
	@Before
	public void before() throws XMLStreamException, IOException {
		boolean showParentPeers = true;
		// that's only present on the left side:
		XmlUtil.filter(xmlRight, "Yempty", showParentPeers);
		XmlUtil.filter(xmlLeft, "Yempty", showParentPeers);
	}

	
	@Test
	public void changes() throws XMLStreamException, IOException {
		ChangeItem item = ChangeItem.changes;
		
		checkModel(Arrays.asList(
				  "dynatrace"
				, "  (#) systemprofile"
				, "    measures"
				, "      (-) measure:Execution CPU Time Yempty:API Breakdown:Execution CPU Time"
				, "      (-) measure:Execution Time Yempty:API Breakdown:Execution Time"
				), item, xmlLeft, xmlRight);
		
		checkCollectChangedElements(Arrays.asList(
				  "(-) measure:Execution CPU Time Yempty:API Breakdown:Execution CPU Time"
				, "(-) measure:Execution Time Yempty:API Breakdown:Execution Time"
				, "(#) systemprofile"
				), 3, item, xmlLeft, xmlRight);
	}
	
	@Test
	public void structural() throws XMLStreamException, IOException {
		ChangeItem item = ChangeItem.structural;
		
		checkModel(Arrays.asList(
				  "dynatrace"
				, "  (#) systemprofile"
				, "    measures"
				, "      (-) measure:Execution CPU Time Yempty:API Breakdown:Execution CPU Time"
				, "      (-) measure:Execution Time Yempty:API Breakdown:Execution Time"
				), item, xmlLeft, xmlRight);
		
		checkCollectChangedElements(Arrays.asList(
				  "(-) measure:Execution CPU Time Yempty:API Breakdown:Execution CPU Time"
				, "(-) measure:Execution Time Yempty:API Breakdown:Execution Time"
				), 2, item, xmlLeft, xmlRight);
	}
	
	
	@Test
	public void insertions() throws XMLStreamException, IOException {
		ChangeItem item = ChangeItem.insertions;
		
		checkModel(Arrays.asList(
				  "dynatrace"
				, "  (#) systemprofile"
				, "    measures"

				), item, xmlLeft, xmlRight);
		
		checkCollectChangedElements(Arrays.asList(
				), 0, item, xmlLeft, xmlRight);
	}
	
	@Test
	public void deletions() throws XMLStreamException, IOException {
		ChangeItem item = ChangeItem.deletions;
		
		checkModel(Arrays.asList(
				  "dynatrace"
				, "  (#) systemprofile"
				, "    measures"
				, "      (-) measure:Execution CPU Time Yempty:API Breakdown:Execution CPU Time"
				, "      (-) measure:Execution Time Yempty:API Breakdown:Execution Time"

				), item, xmlLeft, xmlRight);
		
		checkCollectChangedElements(Arrays.asList(
				  "(-) measure:Execution CPU Time Yempty:API Breakdown:Execution CPU Time"
				, "(-) measure:Execution Time Yempty:API Breakdown:Execution Time"
				), 2, item, xmlLeft, xmlRight);
	}
	
	@Test
	public void attributes() throws XMLStreamException, IOException {
		ChangeItem item = ChangeItem.attributes;
		
		checkModel(Arrays.asList(
				  "dynatrace"
				, "  (#) systemprofile"
				, "    measures"
				), item, xmlLeft, xmlRight);
		
		checkCollectChangedElements(Arrays.asList(
				  "(#) systemprofile"
				), 1, item, xmlLeft, xmlRight);
	}
	
	@Test
	public void normal() throws XMLStreamException, IOException {
		ChangeItem item = ChangeItem.normal;
		
		checkModel(Arrays.asList(
				  "dynatrace"
				, "  (#) systemprofile"
				, "    measures"
				, "      (-) measure:Execution CPU Time Yempty:API Breakdown:Execution CPU Time"
				, "        color"
				, "      (-) measure:Execution Time Yempty:API Breakdown:Execution Time"
				, "        color"
				), item, xmlLeft, xmlRight);
		
		checkCollectChangedElements(Arrays.asList(
				  "(-) measure:Execution CPU Time Yempty:API Breakdown:Execution CPU Time"
				, "(-) measure:Execution Time Yempty:API Breakdown:Execution Time"
				, "(#) systemprofile"
				), 3, item, xmlLeft, xmlRight);
	}
}
