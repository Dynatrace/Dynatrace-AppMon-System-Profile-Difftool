package com.dynatrace.profilediff.ui;

import java.io.IOException;
import java.util.Arrays;

import javax.xml.stream.XMLStreamException;

import org.junit.Before;
import org.junit.Test;

import com.dynatrace.profilediff.XmlUtil;
import com.dynatrace.profilediff.ui.TwoWayModel.ChangeItem;

public class TwoWayModelFilterDeletionTest extends ModelTest_GEH_SystemProfile_Base {
	
	@Before
	public void before() throws XMLStreamException, IOException {
		boolean showParentPeers = false;
		// that's only present on the left side:
		XmlUtil.filter(xmlRight, "Yempty", showParentPeers);
		XmlUtil.filter(xmlLeft, "Yempty", showParentPeers);
	}

	@Test
	public void changes() throws XMLStreamException, IOException {
		ChangeItem item = ChangeItem.changes;
		
		checkModel(Arrays.asList(
				  "dynatrace"
				), item, xmlRight);
		
		checkModel(Arrays.asList(
				  "dynatrace"
				, "  (#) systemprofile"
				, "    measures"
				, "      (-) measure:Execution CPU Time Yempty:API Breakdown:Execution CPU Time"
				, "      (-) measure:Execution Time Yempty:API Breakdown:Execution Time"
				), item, xmlLeft);
		
		checkCollectChangedElements(Arrays.asList(
				), 0, item, xmlRight);
		
		checkCollectChangedElements(Arrays.asList(
				  "(#) systemprofile"
				, "(-) measure:Execution CPU Time Yempty:API Breakdown:Execution CPU Time"
				, "(-) measure:Execution Time Yempty:API Breakdown:Execution Time"
				), 3, item, xmlLeft);
	}
	
	@Test
	public void structural() throws XMLStreamException, IOException {
		ChangeItem item = ChangeItem.structural;
		
		checkModel(Arrays.asList(
				  "dynatrace"
				), item, xmlRight);
		
		checkModel(Arrays.asList(
				  "dynatrace"
				, "  (#) systemprofile"
				, "    measures"
				, "      (-) measure:Execution CPU Time Yempty:API Breakdown:Execution CPU Time"
				, "      (-) measure:Execution Time Yempty:API Breakdown:Execution Time"
				), item, xmlLeft);
		
		checkCollectChangedElements(Arrays.asList(
				), 0, item, xmlRight);
		
		checkCollectChangedElements(Arrays.asList(
				  "(-) measure:Execution CPU Time Yempty:API Breakdown:Execution CPU Time"
				, "(-) measure:Execution Time Yempty:API Breakdown:Execution Time"
				), 2, item, xmlLeft);
	}
	
	@Test
	public void attributes() throws XMLStreamException, IOException {
		ChangeItem item = ChangeItem.attributes;
		
		checkModel(Arrays.asList(
				  "dynatrace"
				), item, xmlRight);
		
		checkModel(Arrays.asList(
				  "dynatrace"
				, "  (#) systemprofile"
				, "    measures"
				), item, xmlLeft);
		
		checkCollectChangedElements(Arrays.asList(
				), 0, item, xmlRight);
		
		checkCollectChangedElements(Arrays.asList(
				  "(#) systemprofile"
				), 1, item, xmlLeft);
	}
	
	/*
	 * Interesting trivia - splitted case "normal" in right and left side to not exceed the 64K method limit.
	 */
	
	@Test
	public void normalRight() throws XMLStreamException, IOException {
		ChangeItem item = ChangeItem.normal;
		
		checkModel(Arrays.asList(
				  "dynatrace"
				), item, xmlRight);
		
		checkCollectChangedElements(Arrays.asList(
				), 0, item, xmlRight);
		
		checkCollectChangedElements(Arrays.asList(
				  "(#) systemprofile"
				, "(-) measure:Execution CPU Time Yempty:API Breakdown:Execution CPU Time"
				, "(-) measure:Execution Time Yempty:API Breakdown:Execution Time"
				), 3, item, xmlLeft);
	}
	
	@Test
	public void normalLeft() throws XMLStreamException, IOException {
		ChangeItem item = ChangeItem.normal;
		
		checkModel(Arrays.asList(
				  "dynatrace"
				, "  (#) systemprofile"
				, "    measures"
				, "      (-) measure:Execution CPU Time Yempty:API Breakdown:Execution CPU Time"
				, "        color"
				, "      (-) measure:Execution Time Yempty:API Breakdown:Execution Time"
				, "        color"
			), item, xmlLeft);


				
		
		checkCollectChangedElements(Arrays.asList(
				), 0, item, xmlRight);
		
		checkCollectChangedElements(Arrays.asList(
				  "(#) systemprofile"
				, "(-) measure:Execution CPU Time Yempty:API Breakdown:Execution CPU Time"
				, "(-) measure:Execution Time Yempty:API Breakdown:Execution Time"
				), 3, item, xmlLeft);
	}
}
