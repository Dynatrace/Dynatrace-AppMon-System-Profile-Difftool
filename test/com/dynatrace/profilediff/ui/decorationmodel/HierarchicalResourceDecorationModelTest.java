package com.dynatrace.profilediff.ui.decorationmodel;

import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.dynatrace.profilediff.IO;
import com.dynatrace.profilediff.TestBase;
import com.dynatrace.profilediff.XmlElement;
import com.dynatrace.profilediff.XmlLexer;
import com.dynatrace.profilediff.XmlStruct;

public class HierarchicalResourceDecorationModelTest extends TestBase {
	
	XmlLexer lexer;
	private String[] imageExt = { ".gif", ".png" };
	private int rawPathLevelThreshold = 2;
	
	@Before
	public void before() {
		lexer = new XmlLexer(SYSTEM_PROFILE_DISCRIMINATOR_ATTRIBUTES, stringCache);
	}
	
	@Test
	public void dynatrace() throws XMLStreamException, IOException {
		HierarchicalResourceDecorationModel dynatraceModel = new HierarchicalResourceDecorationModel("dynatrace", imageExt, rawPathLevelThreshold);
		String[] input = IO.readLines(new FileReader("samples/NewProfile2.profile.xml"));
		XmlStruct xml = lexer.parse(IO.asString(input));
		
		XmlElement transaction = xml.findByPath("dynatrace/systemprofile/transactions/transaction:NewBt1");
		XmlElement measure1 = xml.findByPath("dynatrace/systemprofile/measures/measure:Failed Transaction Count of NewBt1:Error Detection:Failed Transaction Count");
		XmlElement measure2 = xml.findByPath("dynatrace/systemprofile/measures/measure:Failed Transaction Percentage of NewBt1:Error Detection:Failed Transaction Percentage");
		XmlElement thresholds = xml.findByPath("dynatrace/systemprofile/measures/measure:Count:Exceptions:Count/thresholds");
		XmlElement techjava = xml.findByPath("dynatrace/systemprofile/technology:java");
		
		assert transaction != null;
		assert measure1 != null;
		assert measure2 != null;
		assert thresholds != null;
		assert techjava != null;
		
		Assert.assertTrue("transaction icon", dynatraceModel.getIcon(transaction).toString().endsWith("/decorationmodel/dynatrace/dynatrace.systemprofile.transactions.gif"));
		Assert.assertTrue("measure1 icon", dynatraceModel.getIcon(measure1).toString().endsWith("/decorationmodel/dynatrace/dynatrace.systemprofile.measures.png"));
		Assert.assertTrue("measure2 icon", dynatraceModel.getIcon(measure2).toString().endsWith("/decorationmodel/dynatrace/dynatrace.systemprofile.measures.png"));
		Assert.assertTrue("thresholds", dynatraceModel.getIcon(thresholds).toString().endsWith("/decorationmodel/dynatrace/dynatrace.systemprofile.measures.png"));
		Assert.assertTrue("techjava", dynatraceModel.getIcon(techjava).toString().endsWith("/decorationmodel/dynatrace/dynatrace.systemprofile.technology_java.png"));
		
		Assert.assertEquals("Business Transactions", dynatraceModel.getText(transaction.parent));
		Assert.assertEquals("Measures", dynatraceModel.getText(measure1.parent));
		
		/*
		 * we're not doing more testing here on purpose, since the icon paths or element texts might change!
		 */
		
		// just to see that we don't crash:
		for (XmlElement element : xml.elements) {
			dynatraceModel.getIcon(element);
			dynatraceModel.getText(element);
		}
		
		Assert.assertEquals("tryLoadIconCount", 108, dynatraceModel.tryLoadIconCount);
		
		// fetch all again:
		for (int i = 0; i < 30; i++) {
			for (XmlElement element : xml.elements) {
				dynatraceModel.getIcon(element);
				dynatraceModel.getText(element);
			}
		}
		
		// attempted loading icons stayed the same - 
		Assert.assertEquals("tryLoadIconCount", 108, dynatraceModel.tryLoadIconCount);
	}
	
	@Test
	public void foo() {
		new HierarchicalResourceDecorationModel("foo", imageExt, rawPathLevelThreshold ); // folder does not exist - must not crash
	}
	
	@Test
	public void plain() throws XMLStreamException {
		checkPlainIcons(new HierarchicalResourceDecorationModel("plain", imageExt, rawPathLevelThreshold), /*expectNull*/ false);
		checkPlainIcons(new HierarchicalResourceDecorationModel("dynatrace", imageExt, rawPathLevelThreshold), /*expectNull*/ false);
		checkPlainIcons(new HierarchicalResourceDecorationModel("foo", imageExt, rawPathLevelThreshold), /*expectNull*/ true);
	}
	
	private void checkPlainIcons(HierarchicalResourceDecorationModel decorationModel, boolean expectNull) throws XMLStreamException {
		String[] input = {
				"<root a='1'>"
				, "  <level1 b='2'>"
				, "    <level2 c='3'>"
				, "      <level3 d='4'/>"
				, "      <level3 d='5'/>"
				, "      <level3 d='6'>"
				, "        <level4 />"
				, "      </level3>"
				, "    </level2>"
				, "  </level1>"
				, "  <level1 b='20'/>"
				, "</root>"
		};
		
		
		List<String> discriminatingAttributes = Arrays.asList("b", "d");
		
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		XmlStruct xml= lexer.parse(IO.asString(input));
		
		if (expectNull) {
			Assert.assertNull("node icon", decorationModel.getIcon(xml.root()));
			Assert.assertNull("leaf icon", decorationModel.getIcon(xml.elements.get(3)));
			return;
		}
		
		Assert.assertTrue("node icon", decorationModel.getIcon(xml.root()).toString().endsWith(".node.gif"));
		Assert.assertTrue("leaf icon", decorationModel.getIcon(xml.elements.get(3)).toString().endsWith(".leaf.gif"));
		
		Assert.assertEquals("tryLoadIconCount", 8, decorationModel.tryLoadIconCount);
		
		// fetch all again:
		for (int i = 0; i < 30; i++) {
			for (XmlElement element : xml.elements) {
				decorationModel.getIcon(element);
				decorationModel.getText(element);
			}
		}
		
		// attempted loading icons stayed the same - 
		Assert.assertEquals("tryLoadIconCount", 10, decorationModel.tryLoadIconCount);
		
		// fetch all again:
		for (int i = 0; i < 30; i++) {
			for (XmlElement element : xml.elements) {
				decorationModel.getIcon(element);
				decorationModel.getText(element);
			}
		}
		
		// attempted loading icons stayed the same - 
		Assert.assertEquals("tryLoadIconCount", 10, decorationModel.tryLoadIconCount);
	}
}
