package com.dynatrace.profilediff;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.junit.Assert;
import org.junit.Test;

public class XmlLexerTest  extends TestBase {
	
	@Test
	public void flat() throws XMLStreamException, IOException {
		String[] input = {
				  "<root>"
				, "</root>"
		};
		
		List<String> discriminatingAttributes = (Arrays.asList());
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		XmlStruct xml = lexer.parse(IO.asString(input));
		
		Assert.assertEquals(1, xml.depth);
		
		checkList(Arrays.asList(
				  "root [1:2]"
				), toPathStrings(xml.elements));
	}
	
	@Test
	public void small() throws XMLStreamException, IOException {
		String[] input = {
				"<root>"
				, "  <level1>"
				, "  </level1>"
				, "</root>"
		};
		
		List<String> discriminatingAttributes = (Arrays.asList());
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		XmlStruct xml = lexer.parse(IO.asString(input));
		
		Assert.assertEquals(2, xml.depth);
		
		checkList(Arrays.asList(
				"root [1:4]"
				, "root/level1 [2:3]"
				), toPathStrings(xml.elements));
	}
	
	@Test
	public void plain() throws XMLStreamException, IOException {
		String[] input = {
				"<root>"
				, "  <level1>"
				, "    <level2>"
				, "      <level3/>"
				, "    </level2>"
				, "  </level1>"
				, "</root>"
		};
		
		List<String> discriminatingAttributes = (Arrays.asList());
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		XmlStruct xml = lexer.parse(IO.asString(input));
		
		Assert.assertEquals(4, xml.depth);
		
		checkList(Arrays.asList(
				"root [1:7]"
				, "root/level1 [2:6]"
				, "root/level1/level2 [3:5]"
				, "root/level1/level2/level3 [4:4]"
				), toPathStrings(xml.elements));
	}
	
	@Test
	public void singletonContainer() throws XMLStreamException, IOException {
		String[] input = {
				"<root>"
				, "  <config>"
				, "    <sensorgroup>"
				, "      <sensor key='a'/>"
				, "    </sensorgroup>"
				, "    <sensorgroup>"
				, "      <sensor key='b'/>"
				, "    </sensorgroup>"
				, "  </config>"
				, "</root>"
		};
		
		List<String> discriminatingAttributes = (Arrays.asList("key"));
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		XmlStruct xml = lexer.parse(IO.asString(input));
		
		checkList(Arrays.asList(
				  "root [1:10]"
				, "root/config [2:9]"
				, "root/config/sensorgroup [3:5]"
				, "root/config/sensorgroup/sensor:a [4:4]"
				, "root/config/sensorgroup [6:8]"
				, "root/config/sensorgroup/sensor:b [7:7]"
				), toPathStrings(xml.elements));
	}
	
	@Test
	public void withAttr1() throws XMLStreamException, IOException {
		String[] input = {
				"<root a='1'>"
				, "  <level1 b='2'>"
				, "    <level2 c='3'>"
				, "      <level3 d='4'/>"
				, "    </level2>"
				, "  </level1>"
				, "</root>"
		};
		
		List<String> discriminatingAttributes = (Arrays.asList());
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		XmlStruct xml = lexer.parse(IO.asString(input));
		
		checkList(Arrays.asList(
				  "root [1:7]"
				, "root/level1 [2:6]"
				, "root/level1/level2 [3:5]"
				, "root/level1/level2/level3 [4:4]"
				), toPathStrings(xml.elements));
	}

	@Test
	public void withAttr2() throws XMLStreamException, IOException {
		String[] input = {
				"<root a='1'>"
				, "  <level1 b='2'>"
				, "    <level2 c='3'>"
				, "      <level3 d='4'/>"
				, "    </level2>"
				, "  </level1>"
				, "</root>"
		};
		
		List<String> discriminatingAttributes = (Arrays.asList("a"));
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		XmlStruct xml = lexer.parse(IO.asString(input));
		
		checkList(Arrays.asList(
				  "root:1 [1:7]"
				, "root:1/level1 [2:6]"
				, "root:1/level1/level2 [3:5]"
				, "root:1/level1/level2/level3 [4:4]"
				), toPathStrings(xml.elements));
		
		discriminatingAttributes = (Arrays.asList("b"));
		lexer = new XmlLexer(discriminatingAttributes, stringCache);
		xml = lexer.parse(IO.asString(input));
		
		checkList(Arrays.asList(
				  "root [1:7]"
				, "root/level1:2 [2:6]"
				, "root/level1:2/level2 [3:5]"
				, "root/level1:2/level2/level3 [4:4]"
				), toPathStrings(xml.elements));
		
		discriminatingAttributes = (Arrays.asList("c"));
		lexer = new XmlLexer(discriminatingAttributes, stringCache);
		xml = lexer.parse(IO.asString(input));
		
		checkList(Arrays.asList(
				  "root [1:7]"
				, "root/level1 [2:6]"
				, "root/level1/level2:3 [3:5]"
				, "root/level1/level2:3/level3 [4:4]"
				), toPathStrings(xml.elements));
		
		discriminatingAttributes = (Arrays.asList("d"));
		lexer = new XmlLexer(discriminatingAttributes, stringCache);
		xml = lexer.parse(IO.asString(input));
		
		checkList(Arrays.asList(
				  "root [1:7]"
				, "root/level1 [2:6]"
				, "root/level1/level2 [3:5]"
				, "root/level1/level2/level3:4 [4:4]"
				), toPathStrings(xml.elements));
		
		discriminatingAttributes = (Arrays.asList("e")); // not present
		lexer = new XmlLexer(discriminatingAttributes, stringCache);
		xml = lexer.parse(IO.asString(input));
		
		checkList(Arrays.asList(
				  "root [1:7]"
				, "root/level1 [2:6]"
				, "root/level1/level2 [3:5]"
				, "root/level1/level2/level3 [4:4]"
				), toPathStrings(xml.elements));
		
		discriminatingAttributes = (Arrays.asList("a", "b", "c", "d"));
		lexer = new XmlLexer(discriminatingAttributes, stringCache);
		xml = lexer.parse(IO.asString(input));
		
		checkList(Arrays.asList(
				  "root:1 [1:7]"
				, "root:1/level1:2 [2:6]"
				, "root:1/level1:2/level2:3 [3:5]"
				, "root:1/level1:2/level2:3/level3:4 [4:4]"
				), toPathStrings(xml.elements));
	}
	
	@Test
	public void withAttr3() throws XMLStreamException, IOException {
		String[] input = {
				"<root a='1'>"
				, "  <level1 b='2'>"
				, "    <level2 c='3' b='33' a='333'>"
				, "      <level3 d='4' a='44' />"
				, "    </level2>"
				, "  </level1>"
				, "</root>"
		};
		
		List<String> discriminatingAttributes = (Arrays.asList());
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		XmlStruct xml = lexer.parse(IO.asString(input));
		
		checkList(Arrays.asList(
				  "root [1:7]"
				, "root/level1 [2:6]"
				, "root/level1/level2 [3:5]"
				, "root/level1/level2/level3 [4:4]"
				), toPathStrings(xml.elements));
		
		discriminatingAttributes = (Arrays.asList("a", "b", "c", "d"));
		lexer = new XmlLexer(discriminatingAttributes, stringCache);
		xml = lexer.parse(IO.asString(input));
		
		checkList(Arrays.asList(
				  "root:1 [1:7]"
				, "root:1/level1:2 [2:6]"
				, "root:1/level1:2/level2:333:33:3 [3:5]"
				, "root:1/level1:2/level2:333:33:3/level3:44:4 [4:4]"
				), toPathStrings(xml.elements));
		
		checkList(Arrays.asList(
				  "root"
				, "root/level1"
				, "root/level1/level2"
				, "root/level1/level2/level3"
				), toRawPathStrings(xml.elements));
	}
	
	@Test
	public void withAttrCheckOrder() throws XMLStreamException, IOException {
		String[] input = {
				"<root a='a1'>"
				, "  <level1 b='b2'>"
				, "    <level2 c='c3' b='b3' a='a3'>"
				, "      <level3 d='d4' a='a4' c='c4' b='b4' />"
				, "    </level2>"
				, "  </level1>"
				, "</root>"
		};
		
		List<String> discriminatingAttributes;
		XmlLexer lexer;
		XmlStruct xml;
		
		discriminatingAttributes = (Arrays.asList("a", "b", "c", "d"));
		lexer = new XmlLexer(discriminatingAttributes, stringCache);
		xml = lexer.parse(IO.asString(input));
		
		checkList(Arrays.asList(
				  "root:a1 [1:7]"
				, "root:a1/level1:b2 [2:6]"
				, "root:a1/level1:b2/level2:a3:b3:c3 [3:5]"
				, "root:a1/level1:b2/level2:a3:b3:c3/level3:a4:b4:c4:d4 [4:4]"
				), toPathStrings(xml.elements));
		
		discriminatingAttributes = (Arrays.asList("d", "c", "a", "b"));
		lexer = new XmlLexer(discriminatingAttributes, stringCache);
		xml = lexer.parse(IO.asString(input));
		
		checkList(Arrays.asList(
				  "root:a1 [1:7]"
				, "root:a1/level1:b2 [2:6]"
				, "root:a1/level1:b2/level2:c3:a3:b3 [3:5]"
				, "root:a1/level1:b2/level2:c3:a3:b3/level3:d4:c4:a4:b4 [4:4]"
				), toPathStrings(xml.elements));
	}
	
	@Test
	public void withAttrCheckSpecifier() throws XMLStreamException, IOException {
		String[] input = {
				"<root a='a1'>"
				, "  <level1 b='b2'>"
				, "    <level2 c='c3' b='b3' a='a3'>"
				, "      <level3 d='d4' a='a4' c='c4' b='b4' />"
				, "    </level2>"
				, "  </level1>"
				, "</root>"
		};
		
		List<String> discriminatingAttributes;
		XmlLexer lexer;
		XmlStruct xml;
		
		discriminatingAttributes = Arrays.asList("root:a", "root/level1:b", "root/level1/level2:c", "root/level1/level2/level3:d");
		lexer = new XmlLexer(discriminatingAttributes, stringCache);
		xml = lexer.parse(IO.asString(input));
		
		checkList(Arrays.asList(
				  "root:a1 [1:7]"
				, "root:a1/level1:b2 [2:6]"
				, "root:a1/level1:b2/level2:c3 [3:5]"
				, "root:a1/level1:b2/level2:c3/level3:d4 [4:4]"
				), toPathStrings(xml.elements));
		
		discriminatingAttributes = Arrays.asList("root:a", "root/level1:b", "root/level1/level2:c", "root/level1/level2:b", "root/level1/level2:a", "root/level1/level2/level3:d");
		lexer = new XmlLexer(discriminatingAttributes, stringCache);
		xml = lexer.parse(IO.asString(input));
		
		checkList(Arrays.asList(
				  "root:a1 [1:7]"
				, "root:a1/level1:b2 [2:6]"
				, "root:a1/level1:b2/level2:c3:b3:a3 [3:5]"
				, "root:a1/level1:b2/level2:c3:b3:a3/level3:d4 [4:4]"
				), toPathStrings(xml.elements));
	}
	
	@Test
	public void objectGraph() throws XMLStreamException, IOException {
		String[] input = {
				"<root a='1'>"
				, "  <level1 b='2'>"
				, "    <level2 c='3'>"
				, "      <level3 d='4'/>"
				, "      <level3 d='5'/>"
				, "      <level3 d='5'>"
				, "        <level4 />"
				, "      </level3>"
				, "    </level2>"
				, "  </level1>"
				, "  <level1 b='20'/>"
				, "</root>"
		};
		
		List<String> discriminatingAttributes = (Arrays.asList());
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		XmlStruct xml = lexer.parse(IO.asString(input));
		
		checkList(Arrays.asList(
				  "root [1:12]"
				, "root/level1 [2:10]"
				, "root/level1/level2 [3:9]"
				, "root/level1/level2/level3 [4:4]"
				, "root/level1/level2/level3 [5:5]"
				, "root/level1/level2/level3 [6:8]"
				, "root/level1/level2/level3/level4 [7:7]"
				, "root/level1 [11:11]"
				), toPathStrings(xml.elements));
		
		// parent
		Assert.assertTrue(xml.elements.get(0).parent == null);
		Assert.assertTrue(xml.elements.get(1).parent == xml.elements.get(0));
		Assert.assertTrue(xml.elements.get(2).parent == xml.elements.get(1));
		Assert.assertTrue(xml.elements.get(3).parent == xml.elements.get(2));
		Assert.assertTrue(xml.elements.get(4).parent == xml.elements.get(2));
		Assert.assertTrue(xml.elements.get(5).parent == xml.elements.get(2));
		Assert.assertTrue(xml.elements.get(6).parent == xml.elements.get(5));
		Assert.assertTrue(xml.elements.get(7).parent == xml.elements.get(0));
		
		// prev
		Assert.assertTrue(xml.elements.get(0).pred == null);
		Assert.assertTrue(xml.elements.get(1).pred == null);
		Assert.assertTrue(xml.elements.get(2).pred == null);
		Assert.assertTrue(xml.elements.get(3).pred == null);
		Assert.assertTrue(xml.elements.get(4).pred == xml.elements.get(3));
		Assert.assertTrue(xml.elements.get(5).pred == xml.elements.get(4));
		Assert.assertTrue(xml.elements.get(6).pred == null);
		Assert.assertTrue(xml.elements.get(7).pred == xml.elements.get(1));
		
		// children
		Assert.assertEquals(2, xml.elements.get(0).children.size());
		Assert.assertTrue(xml.elements.get(0).children.contains(xml.elements.get(1)));
		Assert.assertTrue(xml.elements.get(0).children.contains(xml.elements.get(7)));
		Assert.assertEquals(1, xml.elements.get(1).children.size());
		Assert.assertTrue(xml.elements.get(1).children.contains(xml.elements.get(2)));
		Assert.assertEquals(3, xml.elements.get(2).children.size());
		Assert.assertTrue(xml.elements.get(2).children.contains(xml.elements.get(3)));
		Assert.assertTrue(xml.elements.get(2).children.contains(xml.elements.get(4)));
		Assert.assertTrue(xml.elements.get(2).children.contains(xml.elements.get(5)));
		Assert.assertEquals(0, xml.elements.get(3).children.size());
		Assert.assertEquals(0, xml.elements.get(4).children.size());
		Assert.assertEquals(1, xml.elements.get(5).children.size());
		Assert.assertTrue(xml.elements.get(5).children.contains(xml.elements.get(6)));
		Assert.assertEquals(0, xml.elements.get(6).children.size());
		Assert.assertEquals(0, xml.elements.get(7).children.size());
		
		/*
		 * descendants
		 */
		
		checkList(Arrays.asList(
				  "root/level1 [2:10]"
				, "root/level1/level2 [3:9]"
				, "root/level1/level2/level3 [4:4]"
				, "root/level1/level2/level3 [5:5]"
				, "root/level1/level2/level3 [6:8]"
				, "root/level1/level2/level3/level4 [7:7]"
				, "root/level1 [11:11]"
				), toPathStrings(xml.getAllDescendants(xml.root())));
		
		checkList(Arrays.asList(
				"root/level1 [2:10]"
				, "root/level1/level2 [3:9]"
				, "root/level1/level2/level3 [4:4]"
				, "root/level1/level2/level3 [5:5]"
				, "root/level1/level2/level3 [6:8]"
				, "root/level1/level2/level3/level4 [7:7]"
				, "root/level1 [11:11]"
				), toPathStrings(xml.getAllDescendants(xml.elements.get(0))));
		
		checkList(Arrays.asList(
				  "root/level1/level2 [3:9]"
				, "root/level1/level2/level3 [4:4]"
				, "root/level1/level2/level3 [5:5]"
				, "root/level1/level2/level3 [6:8]"
				, "root/level1/level2/level3/level4 [7:7]"
				), toPathStrings(xml.getAllDescendants(xml.elements.get(1))));
		
		checkList(Arrays.asList(
				  "root/level1/level2/level3 [4:4]"
				, "root/level1/level2/level3 [5:5]"
				, "root/level1/level2/level3 [6:8]"
				, "root/level1/level2/level3/level4 [7:7]"
				), toPathStrings(xml.getAllDescendants(xml.elements.get(2))));
		
		checkList(Arrays.asList(
				), toPathStrings(xml.getAllDescendants(xml.elements.get(3))));
		
		checkList(Arrays.asList(
				), toPathStrings(xml.getAllDescendants(xml.elements.get(4))));
		
		checkList(Arrays.asList(
				  "root/level1/level2/level3/level4 [7:7]"
				), toPathStrings(xml.getAllDescendants(xml.elements.get(5))));
		
		checkList(Arrays.asList(
				), toPathStrings(xml.getAllDescendants(xml.elements.get(6))));
		
		checkList(Arrays.asList(
				), toPathStrings(xml.getAllDescendants(xml.elements.get(7))));
	}
}
