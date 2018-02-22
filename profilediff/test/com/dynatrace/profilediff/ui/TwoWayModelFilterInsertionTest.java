package com.dynatrace.profilediff.ui;

import java.io.IOException;
import java.util.Arrays;

import javax.xml.stream.XMLStreamException;

import org.junit.Before;
import org.junit.Test;

import com.dynatrace.profilediff.XmlUtil;
import com.dynatrace.profilediff.ui.TwoWayModel.ChangeItem;

public class TwoWayModelFilterInsertionTest extends ModelTest_GEH_SystemProfile_Base {
	
	@Before
	public void before() throws XMLStreamException, IOException {
		boolean showParentPeers = false;
		// that's only present on the left side:
		XmlUtil.filter(xmlRight, "Oracle", showParentPeers);
		XmlUtil.filter(xmlLeft, "Oracle", showParentPeers);
	}

	@Test
	public void changes() throws XMLStreamException, IOException {
		ChangeItem item = ChangeItem.changes;
		
		checkModel(Arrays.asList(
				  "dynatrace"
				, "  (#) systemprofile"
				, "    measures"
				, "      (+) measure:Sun/Oracle Compressed class space used:Java Virtual Machine:Memory Pool"
				, "      (+) measure:Sun/Oracle Metaspace used:Java Virtual Machine:Memory Pool"
				), item, xmlRight);
		
		checkModel(Arrays.asList(
				  "dynatrace"
				, "  (#) systemprofile"
				, "    measures"
				), item, xmlLeft);
		
		checkCollectChangedElements(Arrays.asList(
				  "(#) systemprofile"
				, "(+) measure:Sun/Oracle Compressed class space used:Java Virtual Machine:Memory Pool"
				, "(+) measure:Sun/Oracle Metaspace used:Java Virtual Machine:Memory Pool"
				), 3, item, xmlRight);
		
		checkCollectChangedElements(Arrays.asList(
				  "(#) systemprofile"
				), 1, item, xmlLeft);
	}
	
	@Test
	public void structural() throws XMLStreamException, IOException {
		ChangeItem item = ChangeItem.structural;
		
		checkModel(Arrays.asList(
				  "dynatrace"
				, "  (#) systemprofile"
				, "    measures"
				, "      (+) measure:Sun/Oracle Compressed class space used:Java Virtual Machine:Memory Pool"
				, "      (+) measure:Sun/Oracle Metaspace used:Java Virtual Machine:Memory Pool"
				), item, xmlRight);
		
		checkModel(Arrays.asList(
				  "dynatrace"
				, "  (#) systemprofile"
				, "    measures"
				), item, xmlLeft);
		
		checkCollectChangedElements(Arrays.asList(
				  "(+) measure:Sun/Oracle Compressed class space used:Java Virtual Machine:Memory Pool"
				, "(+) measure:Sun/Oracle Metaspace used:Java Virtual Machine:Memory Pool"
				), 2, item, xmlRight);
		
		checkCollectChangedElements(Arrays.asList(
				), 0, item, xmlLeft);
	}
	
	@Test
	public void attributes() throws XMLStreamException, IOException {
		ChangeItem item = ChangeItem.attributes;
		
		checkModel(Arrays.asList(
				  "dynatrace"
				, "  (#) systemprofile"
				, "    measures"
				), item, xmlRight);
		
		checkModel(Arrays.asList(
				  "dynatrace"
				, "  (#) systemprofile"
				, "    measures"
				), item, xmlLeft);
		
		checkCollectChangedElements(Arrays.asList(
				  "(#) systemprofile"
				), 1, item, xmlRight);
		
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
				, "  (#) systemprofile"
				, "    measures"
				, "      measure:Sun/Oracle Code Cache used:Java Virtual Machine:Memory Pool"
				, "        memorypoolmeasureconfig"
				, "        color"
				, "      (+) measure:Sun/Oracle Compressed class space used:Java Virtual Machine:Memory Pool"
				, "        memorypoolmeasureconfig"
				, "        color"
				, "      measure:Sun/Oracle Eden Space used:Java Virtual Machine:Memory Pool"
				, "        memorypoolmeasureconfig"
				, "        color"
				, "      (+) measure:Sun/Oracle Metaspace used:Java Virtual Machine:Memory Pool"
				, "        memorypoolmeasureconfig"
				, "        color"
				, "      measure:Sun/Oracle Old Gen used:Java Virtual Machine:Memory Pool"
				, "        memorypoolmeasureconfig"
				, "        color"
				, "      measure:Sun/Oracle Perm Gen used:Java Virtual Machine:Memory Pool"
				, "        memorypoolmeasureconfig"
				, "        color"
				, "      measure:Sun/Oracle Survivor Space used:Java Virtual Machine:Memory Pool"
				, "        memorypoolmeasureconfig"
				, "        color"
				, "      measure:Sun/Oracle Tenured Gen used:Java Virtual Machine:Memory Pool"
				, "        memorypoolmeasureconfig"
				, "        color"
				), item, xmlRight);
		
		checkCollectChangedElements(Arrays.asList(
				  "(#) systemprofile"
				, "(+) measure:Sun/Oracle Compressed class space used:Java Virtual Machine:Memory Pool"
				, "(+) measure:Sun/Oracle Metaspace used:Java Virtual Machine:Memory Pool"
				), 3, item, xmlRight);
		
		checkCollectChangedElements(Arrays.asList(
				  "(#) systemprofile"
				), 1, item, xmlLeft);
	}
	
	@Test
	public void normalLeft() throws XMLStreamException, IOException {
		ChangeItem item = ChangeItem.normal;
		
		checkModel(Arrays.asList(
				  "dynatrace"
				, "  (#) systemprofile"
				, "    measures"
				, "      measure:Sun/Oracle Code Cache used:Java Virtual Machine:Memory Pool"
				, "        memorypoolmeasureconfig"
				, "        color"
				, "      measure:Sun/Oracle Eden Space used:Java Virtual Machine:Memory Pool"
				, "        memorypoolmeasureconfig"
				, "        color"
				, "      measure:Sun/Oracle Old Gen used:Java Virtual Machine:Memory Pool"
				, "        memorypoolmeasureconfig"
				, "        color"
				, "      measure:Sun/Oracle Perm Gen used:Java Virtual Machine:Memory Pool"
				, "        memorypoolmeasureconfig"
				, "        color"
				, "      measure:Sun/Oracle Survivor Space used:Java Virtual Machine:Memory Pool"
				, "        memorypoolmeasureconfig"
				, "        color"
				, "      measure:Sun/Oracle Tenured Gen used:Java Virtual Machine:Memory Pool"
				, "        memorypoolmeasureconfig"
				, "        color"
			), item, xmlLeft);

		
		checkCollectChangedElements(Arrays.asList(
				  "(#) systemprofile"
				, "(+) measure:Sun/Oracle Compressed class space used:Java Virtual Machine:Memory Pool"
				, "(+) measure:Sun/Oracle Metaspace used:Java Virtual Machine:Memory Pool"
				), 3, item, xmlRight);
		
		checkCollectChangedElements(Arrays.asList(
				  "(#) systemprofile"
				), 1, item, xmlLeft);
	}
}
