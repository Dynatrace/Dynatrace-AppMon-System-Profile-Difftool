package com.dynatrace.profilediff;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class XmlDifferOrderTest extends DifferMergerTestBase {
	
	public XmlDifferOrderTest(XmlMerger3Factory xmlMerger3Factory,
			String mergerName, XmlDifferFactory xmlDifferFactory,
			String differName, int stringMetricDifferThreshold) {
		super(xmlMerger3Factory, mergerName, xmlDifferFactory, differName, stringMetricDifferThreshold);
	}

	@Test
	public void orderChanged() throws XMLStreamException, IOException {
		String[] inputLeft = {
				"<root>"
				, "  <config a1='1' b='2'/>"
				, "  <config a2='1' b='2' c='3'/>"
				, "  <config x3='1' y='2'/>"
				, "</root>"
		};
		String[] inputRight = {
				"<root>"
				, "  <config a2='1' b='2' c='3'/>"
				, "  <config a1='1' b='2'/>"
				, "  <config x3='1' y='2'/>"
				, "</root>"
		};
		
		List<String> discriminatingAttributes = Arrays.asList();
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		XmlStruct xmlLeft = lexer.parse(IO.asString(inputLeft));
		XmlStruct xmlRight = lexer.parse(IO.asString(inputRight));
		
		XmlDiffer differ = xmlDifferFactory.create(Arrays.asList());
		XmlDiffer.Result diffResult = differ.diff(xmlLeft, xmlRight);
		Assert.assertEquals(0, diffResult.nAdded);
		Assert.assertEquals(0, diffResult.nRemoved);
		Assert.assertEquals(0, diffResult.nAttributeChanged);
		
		checkList(Arrays.asList(
				  "structure:unchanged/attr:unchanged: root [1:5]"
				, "structure:unchanged/attr:unchanged: root/config [2:2]"
				, "structure:unchanged/attr:unchanged: root/config [3:3]"
				, "structure:unchanged/attr:unchanged: root/config [4:4]"
				), toStateStrings(xmlRight.elements));
		
		checkList(Arrays.asList(
				  "structure:unchanged/attr:unchanged: root [1:5]"
				, "structure:unchanged/attr:unchanged: root/config [2:2]"
				, "structure:unchanged/attr:unchanged: root/config [3:3]"
				, "structure:unchanged/attr:unchanged: root/config [4:4]"
				), toStateStrings(xmlLeft.elements));
	}
	
	@Test
	public void orderAndAttributesChanged1() throws XMLStreamException, IOException {
		String[] inputLeft = {
				"<root>"
				, "  <config a1='1' b='2'/>"
				, "  <config a2='1' b='2' c='3'/>"
				, "  <config x3='1' y='2'/>"
				, "  <config changed='no'/>"
				, "</root>"
		};
		String[] inputRight = {
				"<root>"
				, "  <config a2='1' b='2' c='3'/>"
				, "  <config a1='1' b='2'/>"
				, "  <config x3='1' y='2'/>"
				, "  <config xchanged='yes'/>"
				, "</root>"
		};
		
		List<String> discriminatingAttributes = Arrays.asList();
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		XmlStruct xmlLeft = lexer.parse(IO.asString(inputLeft));
		XmlStruct xmlRight = lexer.parse(IO.asString(inputRight));
		
		XmlDiffer differ = xmlDifferFactory.create(Arrays.asList());
		XmlDiffer.Result diffResult = differ.diff(xmlLeft, xmlRight);
		Assert.assertEquals(0, diffResult.nAdded);
		Assert.assertEquals(0, diffResult.nRemoved);
		Assert.assertEquals(1, diffResult.nAttributeChanged); 
		
		checkList(Arrays.asList(
				  "structure:unchanged/attr:descendantChanged: root [1:6]"
				, "structure:unchanged/attr:unchanged: root/config [2:2]"
				, "structure:unchanged/attr:unchanged: root/config [3:3]"
				, "structure:unchanged/attr:unchanged: root/config [4:4]"
				, "structure:unchanged/attr:changed: root/config [5:5]"
				), toStateStrings(xmlRight.elements));
		
		checkList(Arrays.asList(
				  "structure:unchanged/attr:descendantChanged: root [1:6]"
				, "structure:unchanged/attr:unchanged: root/config [2:2]"
				, "structure:unchanged/attr:unchanged: root/config [3:3]"
				, "structure:unchanged/attr:unchanged: root/config [4:4]"
				, "structure:unchanged/attr:changed: root/config [5:5]"
				), toStateStrings(xmlLeft.elements));
	}
	
	@Test
	public void orderAndAttributesChanged2() throws XMLStreamException, IOException {
		String[] inputLeft = {
				"<root>"
				, "  <config a1='1' b='2'/>"
				, "  <config changed='no'/>"
				, "  <config a2='1' b='2' c='3'/>"
				, "  <config x3='1' y='2'/>"
				, "</root>"
		};
		String[] inputRight = {
				"<root>"
				, "  <config a2='1' b='2' c='3'/>"
				, "  <config a1='1' b='2'/>"
				, "  <config x3='1' y='2'/>"
				, "  <config xchanged='yes'/>"
				, "</root>"
		};
		
		List<String> discriminatingAttributes = Arrays.asList();
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		XmlStruct xmlLeft = lexer.parse(IO.asString(inputLeft));
		XmlStruct xmlRight = lexer.parse(IO.asString(inputRight));
		
		XmlDiffer differ = xmlDifferFactory.create(Arrays.asList());
		XmlDiffer.Result diffResult = differ.diff(xmlLeft, xmlRight);
		Assert.assertEquals(0, diffResult.nAdded);
		Assert.assertEquals(0, diffResult.nRemoved);
		Assert.assertEquals(1, diffResult.nAttributeChanged); 
		
		checkList(Arrays.asList(
				"structure:unchanged/attr:descendantChanged: root [1:6]"
				, "structure:unchanged/attr:unchanged: root/config [2:2]"
				, "structure:unchanged/attr:unchanged: root/config [3:3]"
				, "structure:unchanged/attr:unchanged: root/config [4:4]"
				, "structure:unchanged/attr:changed: root/config [5:5]"
				), toStateStrings(xmlRight.elements));
		
		checkList(Arrays.asList(
				  "structure:unchanged/attr:descendantChanged: root [1:6]"
				, "structure:unchanged/attr:unchanged: root/config [2:2]"
				, "structure:unchanged/attr:changed: root/config [3:3]"
				, "structure:unchanged/attr:unchanged: root/config [4:4]"
				, "structure:unchanged/attr:unchanged: root/config [5:5]"
				), toStateStrings(xmlLeft.elements));
	}
	
	@Test
	public void orderAndAttributesAndStructureChanged1() throws XMLStreamException, IOException {
		String[] inputLeft = {
				"<root>"
				, "  <config a1='1' b='2'/>"
				, "  <config a2='1' b='2' c='3'/>"
				, "  <config x3='1' y='2'/>"
				, "</root>"
		};
		String[] inputRight = {
				"<root>"
				, "  <config a2='1' b='2' c='3'/>"
				, "  <config a1='1' b='2'/>"
				, "  <config xchanged='yes'/>"
				, "  <config x3='1' y='2'/>"
				, "</root>"
		};
		
		List<String> discriminatingAttributes = Arrays.asList();
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		XmlStruct xmlLeft = lexer.parse(IO.asString(inputLeft));
		XmlStruct xmlRight = lexer.parse(IO.asString(inputRight));
		
		XmlDiffer differ = xmlDifferFactory.create(Arrays.asList());
		XmlDiffer.Result diffResult = differ.diff(xmlLeft, xmlRight);
		Assert.assertEquals(1, diffResult.nAdded);
		Assert.assertEquals(0, diffResult.nRemoved);
		Assert.assertEquals(0, diffResult.nAttributeChanged); 
		
		//FIXME wrong
		checkList(Arrays.asList(
				  "structure:descendantChanged/attr:unchanged: root [1:6]"
				, "structure:unchanged/attr:unchanged: root/config [2:2]"
				, "structure:unchanged/attr:unchanged: root/config [3:3]"
				, "structure:unchanged/attr:unchanged: root/config [4:4]"
				, "structure:changed/attr:unchanged: root/config [5:5]"
				), toStateStrings(xmlRight.elements));
		
		checkList(Arrays.asList(
				  "structure:descendantChanged/attr:unchanged: root [1:5]"
				, "structure:unchanged/attr:unchanged: root/config [2:2]"
				, "structure:unchanged/attr:unchanged: root/config [3:3]"
				, "structure:descendantChanged/attr:unchanged: root/config [4:4]"
				), toStateStrings(xmlLeft.elements));
	}
	
	@Test
	public void orderAndAttributesAndStructureChanged2() throws XMLStreamException, IOException {
		String[] inputLeft = {
				"<root>"
				, "  <config a1='1' b='2'/>"
				, "  <config changed='no'/>"
				, "  <config a2='1' b='2' c='3'/>"
				, "  <config x3='1' y='2'/>"
				, "</root>"
		};
		String[] inputRight = {
				"<root>"
				, "  <config a2='1' b='2' c='3'/>"
				, "  <config a1='1' b='2'/>"
				, "  <config x3='1' y='2'/>"
				, "</root>"
		};
		
		List<String> discriminatingAttributes = Arrays.asList();
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		XmlStruct xmlLeft = lexer.parse(IO.asString(inputLeft));
		XmlStruct xmlRight = lexer.parse(IO.asString(inputRight));
		
		XmlDiffer differ = xmlDifferFactory.create(Arrays.asList());
		XmlDiffer.Result diffResult = differ.diff(xmlLeft, xmlRight);
		Assert.assertEquals(0, diffResult.nAdded);
		Assert.assertEquals(1, diffResult.nRemoved);
		Assert.assertEquals(0, diffResult.nAttributeChanged); 
		
		checkList(Arrays.asList(
				  "structure:descendantChanged/attr:unchanged: root [1:5]"
				, "structure:unchanged/attr:unchanged: root/config [2:2]"
				, "structure:unchanged/attr:unchanged: root/config [3:3]"
				, "structure:descendantChanged/attr:unchanged: root/config [4:4]"
				), toStateStrings(xmlRight.elements));
		
		checkList(Arrays.asList(
				  "structure:descendantChanged/attr:unchanged: root [1:6]"
				, "structure:unchanged/attr:unchanged: root/config [2:2]"
				, "structure:unchanged/attr:unchanged: root/config [3:3]"
				, "structure:unchanged/attr:unchanged: root/config [4:4]"
				, "structure:changed/attr:unchanged: root/config [5:5]"
				), toStateStrings(xmlLeft.elements));
	}
}
