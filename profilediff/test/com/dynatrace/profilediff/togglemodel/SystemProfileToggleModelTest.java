package com.dynatrace.profilediff.togglemodel;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.dynatrace.profilediff.IO;
import com.dynatrace.profilediff.TestBase;
import com.dynatrace.profilediff.XmlDiffer;
import com.dynatrace.profilediff.XmlDifferFactory;
import com.dynatrace.profilediff.XmlDifferFactoryImpl;
import com.dynatrace.profilediff.XmlElement;
import com.dynatrace.profilediff.XmlLexer;
import com.dynatrace.profilediff.XmlStruct;
import com.dynatrace.profilediff.togglemodel.ToggleModel.SelectionInterface;

public class SystemProfileToggleModelTest extends TestBase {
	
	XmlLexer lexer;
	private XmlDiffer differ;
	
	@Before
	public void before() {
		lexer = new XmlLexer(SYSTEM_PROFILE_DISCRIMINATOR_ATTRIBUTES, stringCache);
		XmlDifferFactory xmlDifferFactory = XmlDifferFactoryImpl.newDefault();
		differ = xmlDifferFactory .create(SYSTEM_PROFILE_IGNORED_ATTRIBUTES);
	}
	
	@Test
	public void case1() throws XMLStreamException, IOException, ToggleVeto {
		String[] inputLeft = IO.readLines(new FileReader("samples/NewProfile1.profile.xml"));
		String[] inputRight = IO.readLines(new FileReader("samples/NewProfile2.profile.xml"));
		
		XmlStruct xmlLeft = lexer.parse(IO.asString(inputLeft));
		XmlStruct xmlRight = lexer.parse(IO.asString(inputRight));
		
		XmlDiffer.Result diffResult = differ.diff(xmlLeft, xmlRight);
		Assert.assertEquals(4, diffResult.nAdded);
		Assert.assertEquals(0, diffResult.nRemoved);
		Assert.assertEquals(1, diffResult.nAttributeChanged);

		measuresAndBTs(xmlRight);
		
		diffResult = differ.diff(xmlRight, xmlLeft);
		Assert.assertEquals(0, diffResult.nAdded);
		Assert.assertEquals(4, diffResult.nRemoved);
		Assert.assertEquals(1, diffResult.nAttributeChanged);
	}
	
	@Test
	public void case2() throws XMLStreamException, IOException, ToggleVeto {
		String[] inputLeft = IO.readLines(new FileReader("samples/GEH_Production.profile.xml"));
		String[] inputRight = IO.readLines(new FileReader("samples/GEL_Production.profile.xml"));
		
		XmlStruct xmlLeft = lexer.parse(IO.asString(inputLeft));
		XmlStruct xmlRight = lexer.parse(IO.asString(inputRight));
		
		XmlDiffer.Result diffResult = differ.diff(xmlLeft, xmlRight);
		Assert.assertEquals(45, diffResult.nAdded);
		Assert.assertEquals(621, diffResult.nRemoved);
		Assert.assertEquals(276, diffResult.nAttributeChanged);
		
		sensorConfigsAndAgentGroups(xmlRight);
	}
	
	private static void measuresAndBTs(XmlStruct xml) throws ToggleVeto {
		SelectionInterface<XmlElement> selectionInterface = new TestSelectionInterface<XmlElement>();
		ToggleModel<XmlElement> toggleModel = new SystemProfileToggleModel();
		toggleModel.setSelectionInterface(selectionInterface);
		
		XmlElement transaction = xml.findByPath("dynatrace/systemprofile/transactions/transaction:NewBt1");
		XmlElement measure1 = xml.findByPath("dynatrace/systemprofile/measures/measure:Failed Transaction Count of NewBt1:Error Detection:Failed Transaction Count");
		XmlElement measure2 = xml.findByPath("dynatrace/systemprofile/measures/measure:Failed Transaction Percentage of NewBt1:Error Detection:Failed Transaction Percentage");
		XmlElement thresholds = xml.findByPath("dynatrace/systemprofile/measures/measure:Count:Exceptions:Count/thresholds");
		
		assert transaction != null;
		assert measure1 != null;
		assert measure2 != null;
		assert thresholds != null;
		
		selectionInterface.setSelected(transaction, true);
		selectionInterface.setSelected(measure1, true);
		selectionInterface.setSelected(measure2, true);
		selectionInterface.setSelected(thresholds, true);
		
		toggleModel.toggle(transaction, false);
		Assert.assertFalse("transitive selection", selectionInterface.isSelected(measure1));
		Assert.assertFalse("transitive selection", selectionInterface.isSelected(measure2));
		Assert.assertTrue( "transitive selection", selectionInterface.isSelected(thresholds));

		toggleModel.toggle(transaction, true);
		Assert.assertTrue("transitive selection", selectionInterface.isSelected(measure1));
		Assert.assertTrue("transitive selection", selectionInterface.isSelected(measure2));
		Assert.assertTrue("transitive selection", selectionInterface.isSelected(thresholds));

		try {
			toggleModel.toggle(measure1, true);
			Assert.fail("Expecting ToggleVeto");
		} catch (ToggleVeto e) {
			System.out.println(e); // OK
		}
		try {
			toggleModel.toggle(measure1, false);
			Assert.fail("Expecting ToggleVeto");
		} catch (ToggleVeto e) {
			System.out.println(e); // OK
		}
		try {
			toggleModel.toggle(measure2, true);
			Assert.fail("Expecting ToggleVeto");
		} catch (ToggleVeto e) {
			System.out.println(e); // OK
		}
		try {
			toggleModel.toggle(measure2, false);
			Assert.fail("Expecting ToggleVeto");
		} catch (ToggleVeto e) {
			System.out.println(e); // OK
		}
		try {
			toggleModel.toggle(thresholds, true);
		} catch (ToggleVeto e) {
			throw new RuntimeException(e);
		}
		try {
			toggleModel.toggle(thresholds, false);
			Assert.fail("Expecting ToggleVeto");
		} catch (ToggleVeto e) {
			System.out.println(e); // OK
		}
		
		selectionInterface.setSelected(transaction, false);
		selectionInterface.setSelected(measure1, false);
		selectionInterface.setSelected(measure2, false);
		selectionInterface.setSelected(thresholds, false);

		toggleModel.toggle(transaction, true);
		Assert.assertTrue("transitive selection", selectionInterface.isSelected(measure1));
		Assert.assertTrue("transitive selection", selectionInterface.isSelected(measure2));
		Assert.assertEquals("transitive selection", !false, selectionInterface.isSelected(thresholds));
	}
	
	private static void sensorConfigsAndAgentGroups(XmlStruct xml) throws ToggleVeto {
		TestSelectionInterface<XmlElement> selectionInterface = new TestSelectionInterface<XmlElement>();
		ToggleModel<XmlElement> toggleModel = new SystemProfileToggleModel();
		toggleModel.setSelectionInterface(selectionInterface);
		
		XmlElement sensorconfigAppServer = xml.findByPath("dynatrace/systemprofile/configurations/configuration:Default/sensorconfig:AppServer");
		XmlElement sensorconfigWebServer = xml.findByPath("dynatrace/systemprofile/configurations/configuration:Default/sensorconfig:WebServer");
		XmlElement agentgroupAppServer = xml.findByPath("dynatrace/systemprofile/agentgroups/agentgroup:AppServer");
		XmlElement agentgroupWebServer = xml.findByPath("dynatrace/systemprofile/agentgroups/agentgroup:WebServer");
		
		assert sensorconfigAppServer != null;
		assert sensorconfigWebServer != null;
		assert agentgroupAppServer != null;
		assert agentgroupWebServer != null;
		
		Assert.assertFalse("initial selection", selectionInterface.isSelected(sensorconfigAppServer));
		Assert.assertFalse("initial selection", selectionInterface.isSelected(sensorconfigWebServer));
		Assert.assertFalse("initial selection", selectionInterface.isSelected(agentgroupAppServer));
		Assert.assertFalse("initial selection", selectionInterface.isSelected(agentgroupWebServer));

		toggleModel.toggle(sensorconfigAppServer, true);
		Assert.assertTrue("transitive selection", selectionInterface.isSelected(agentgroupAppServer));
		Assert.assertFalse("transitive selection", selectionInterface.isSelected(agentgroupWebServer));
		
		selectionInterface.clear();
		
		toggleModel.toggle(sensorconfigWebServer, true);
		Assert.assertTrue("transitive selection", selectionInterface.isSelected(agentgroupWebServer));
		Assert.assertFalse("transitive selection", selectionInterface.isSelected(agentgroupAppServer));
		
		selectionInterface.clear();
		
		selectionInterface.setSelected(sensorconfigAppServer, true);
		selectionInterface.setSelected(sensorconfigWebServer, true);
		
		try {
			toggleModel.toggle(agentgroupAppServer, false);
			Assert.fail("Expecting ToggleVeto");
		} catch (ToggleVeto e) {
			System.out.println(e); // OK
		}
		
		try {
			toggleModel.toggle(agentgroupWebServer, false);
			Assert.fail("Expecting ToggleVeto");
		} catch (ToggleVeto e) {
			System.out.println(e); // OK
		}
		
		selectionInterface.clear();
		
		toggleModel.toggle(agentgroupAppServer, false);
		toggleModel.toggle(agentgroupWebServer, false);
	}
	
	static class TestSelectionInterface<T> implements SelectionInterface<T> {
		
		private final Set<T> data = new HashSet<>();

		@Override
		public void setSelected(T element, boolean selected) {
			if (selected) {
				data.add(element); 
			} else {
				data.remove(element);
			}
		}

		@Override
		public boolean isSelected(T element) {
			return data.contains(element);
		}
		
		void clear() {
			data.clear();
		}
	}
}
