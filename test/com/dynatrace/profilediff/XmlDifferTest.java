package com.dynatrace.profilediff;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.junit.Assert;
import org.junit.Test;

public class XmlDifferTest extends DifferMergerTestBase {
	
	public XmlDifferTest(XmlMerger3Factory xmlMerger3Factory,
			String mergerName, XmlDifferFactory xmlDifferFactory,
			String differName, int stringMetricDifferThreshold) {
		super(xmlMerger3Factory, mergerName, xmlDifferFactory, differName, stringMetricDifferThreshold);
	}

	private static final List<String> ignoreAttributeNames = Collections.emptyList();
	 
	@Test
	public void same() throws XMLStreamException, IOException {
		String[] inputLeft = {
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
		String[] inputRight = inputLeft;
		
		List<String> discriminatingAttributes = Arrays.asList("b", "d");
		
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		XmlStruct xmlLeft = lexer.parse(IO.asString(inputLeft));
		XmlStruct xmlRight = lexer.parse(IO.asString(inputRight));
		
		XmlDiffer differ = xmlDifferFactory.create(ignoreAttributeNames);
		XmlDiffer.Result diffResult = differ.diff(xmlLeft, xmlRight);
		basicChecks(xmlLeft, xmlRight, diffResult);
		
		/*
		 * most basic checks
		 */
		Assert.assertSame(xmlRight.isBase, Boolean.FALSE);
		Assert.assertSame(xmlLeft.isBase, Boolean.TRUE);
		
		Assert.assertEquals(0, diffResult.nAdded);
		Assert.assertEquals(0, diffResult.nRemoved);
		Assert.assertEquals(0, diffResult.nAttributeChanged);
		
		checkList(Arrays.asList(
				  "structure:unchanged/attr:unchanged: root [1:12]"
				, "structure:unchanged/attr:unchanged: root/level1:2 [2:10]"
				, "structure:unchanged/attr:unchanged: root/level1:2/level2 [3:9]"
				, "structure:unchanged/attr:unchanged: root/level1:2/level2/level3:4 [4:4]"
				, "structure:unchanged/attr:unchanged: root/level1:2/level2/level3:5 [5:5]"
				, "structure:unchanged/attr:unchanged: root/level1:2/level2/level3:6 [6:8]"
				, "structure:unchanged/attr:unchanged: root/level1:2/level2/level3:6/level4 [7:7]"
				, "structure:unchanged/attr:unchanged: root/level1:20 [11:11]"
				), toStateStrings(xmlRight.elements));
		
		checkList(Arrays.asList(
				  "structure:unchanged/attr:unchanged: root [1:12]"
				, "structure:unchanged/attr:unchanged: root/level1:2 [2:10]"
				, "structure:unchanged/attr:unchanged: root/level1:2/level2 [3:9]"
				, "structure:unchanged/attr:unchanged: root/level1:2/level2/level3:4 [4:4]"
				, "structure:unchanged/attr:unchanged: root/level1:2/level2/level3:5 [5:5]"
				, "structure:unchanged/attr:unchanged: root/level1:2/level2/level3:6 [6:8]"
				, "structure:unchanged/attr:unchanged: root/level1:2/level2/level3:6/level4 [7:7]"
				, "structure:unchanged/attr:unchanged: root/level1:20 [11:11]"
				), toStateStrings(xmlLeft.elements));
	}
	
	@Test
	public void leavesAdded() throws XMLStreamException, IOException {
		String[] inputLeft = {
				"<root a='1'>"
				, "  <level1 b='2'>"
				, "    <level2 c='3'>"
				, "      <level3 d='4'/>"
				, "      <level3 d='5'/>"
				, "      <level3 d='6'>"
				, "        <level4 e='1'/>"
				, "      </level3>"
				, "    </level2>"
				, "  </level1>"
				, "  <level1 b='20'/>"
				, "</root>"
		};
		String[] inputRight = {
				"<root a='1'>"
				, "  <level1 b='2'>"
				, "    <level2 c='3'>"
				, "      <level3 d='4'/>"
				, "      <level3 d='5'/>"
				, "      <level3 d='6'>"
				, "        <level4 e='1'/>"
				, "        <level4 e='NEW#1'/>"
				, "        <level4 e='NEW#2'/>"
				, "      </level3>"
				, "    </level2>"
				, "  </level1>"
				, "  <level1 b='20'/>"
				, "</root>"
		};
		
		List<String> discriminatingAttributes = (Arrays.asList("b", "d", "e"));
		
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		XmlStruct xmlLeft = lexer.parse(IO.asString(inputLeft));
		XmlStruct xmlRight = lexer.parse(IO.asString(inputRight));
		
		
		XmlDiffer differ = xmlDifferFactory.create(ignoreAttributeNames );
		XmlDiffer.Result diffResult = differ.diff(xmlLeft, xmlRight);
		basicChecks(xmlLeft, xmlRight, diffResult);
		Assert.assertEquals(2, diffResult.nAdded);
		Assert.assertEquals(0, diffResult.nRemoved);
		Assert.assertEquals(0, diffResult.nAttributeChanged);

		checkList(Arrays.asList(
				  "structure:descendantChanged/attr:unchanged: root [1:14]"
				, "structure:descendantChanged/attr:unchanged: root/level1:2 [2:12]"
				, "structure:descendantChanged/attr:unchanged: root/level1:2/level2 [3:11]"
				, "structure:unchanged/attr:unchanged: root/level1:2/level2/level3:4 [4:4]"
				, "structure:unchanged/attr:unchanged: root/level1:2/level2/level3:5 [5:5]"
				, "structure:descendantChanged/attr:unchanged: root/level1:2/level2/level3:6 [6:10]"
				, "structure:unchanged/attr:unchanged: root/level1:2/level2/level3:6/level4:1 [7:7]"
				, "structure:changed/attr:unchanged: root/level1:2/level2/level3:6/level4:NEW#1 [8:8]"
				, "structure:changed/attr:unchanged: root/level1:2/level2/level3:6/level4:NEW#2 [9:9]"
				, "structure:unchanged/attr:unchanged: root/level1:20 [13:13]"
				), toStateStrings(xmlRight.elements));
		
		checkList(Arrays.asList(
				  "structure:descendantChanged/attr:unchanged: root [1:12]"
				, "structure:descendantChanged/attr:unchanged: root/level1:2 [2:10]"
				, "structure:descendantChanged/attr:unchanged: root/level1:2/level2 [3:9]"
				, "structure:unchanged/attr:unchanged: root/level1:2/level2/level3:4 [4:4]"
				, "structure:unchanged/attr:unchanged: root/level1:2/level2/level3:5 [5:5]"
				, "structure:descendantChanged/attr:unchanged: root/level1:2/level2/level3:6 [6:8]"
				, "structure:unchanged/attr:unchanged: root/level1:2/level2/level3:6/level4:1 [7:7]"
				, "structure:unchanged/attr:unchanged: root/level1:20 [11:11]"
				), toStateStrings(xmlLeft.elements));
	}
	
	@Test
	public void leavesAddedAndRemoved() throws XMLStreamException, IOException {
		String[] inputLeft = {
				"<root a='1'>"
				, "  <level1 b='2'>"
				, "    <level2 c='3'>"
				, "      <level3 d='4'/>"
				, "      <level3 d='5'/>"
				, "      <level3 d='6'>"
				, "        <level4 e='1'/>"
				, "      </level3>"
				, "    </level2>"
				, "  </level1>"
				, "  <level1 b='20'/>"
				, "</root>"
		};
		String[] inputRight = {
				"<root a='1'>"
				, "  <level1 b='2'>"
				, "    <level2 c='3'>"
//				, "      <level3 d='4'/>" // REMOVED
				, "      <level3 d='5'/>"
				, "      <level3 d='6'>"
				, "        <level4 e='1'/>"
				, "        <level4 e='NEW#1'/>"
				, "        <level4 e='NEW#2'/>"
				, "      </level3>"
				, "    </level2>"
				, "  </level1>"
//				, "  <level1 b='20'/>" // REMOVED
				, "</root>"
		};
		
		List<String> discriminatingAttributes = Arrays.asList("b", "d", "e");
		
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		XmlStruct xmlLeft = lexer.parse(IO.asString(inputLeft));
		XmlStruct xmlRight = lexer.parse(IO.asString(inputRight));
		
		
		XmlDiffer differ = xmlDifferFactory.create(ignoreAttributeNames);
		XmlDiffer.Result diffResult = differ.diff(xmlLeft, xmlRight);
		basicChecks(xmlLeft, xmlRight, diffResult);
		Assert.assertEquals(2, diffResult.nAdded);
		Assert.assertEquals(2, diffResult.nRemoved);
		Assert.assertEquals(0, diffResult.nAttributeChanged);
		
		checkList(Arrays.asList(
				  "structure:descendantChanged/attr:unchanged: root [1:12]"
				, "structure:descendantChanged/attr:unchanged: root/level1:2 [2:11]"
				, "structure:descendantChanged/attr:unchanged: root/level1:2/level2 [3:10]"
				, "structure:unchanged/attr:unchanged: root/level1:2/level2/level3:5 [4:4]"
				, "structure:descendantChanged/attr:unchanged: root/level1:2/level2/level3:6 [5:9]"
				, "structure:unchanged/attr:unchanged: root/level1:2/level2/level3:6/level4:1 [6:6]"
				, "structure:changed/attr:unchanged: root/level1:2/level2/level3:6/level4:NEW#1 [7:7]"
				, "structure:changed/attr:unchanged: root/level1:2/level2/level3:6/level4:NEW#2 [8:8]"
				), toStateStrings(xmlRight.elements));
		
		checkList(Arrays.asList(
				  "structure:descendantChanged/attr:unchanged: root [1:12]"
				, "structure:descendantChanged/attr:unchanged: root/level1:2 [2:10]"
				, "structure:descendantChanged/attr:unchanged: root/level1:2/level2 [3:9]"
				, "structure:changed/attr:unchanged: root/level1:2/level2/level3:4 [4:4]"
				, "structure:unchanged/attr:unchanged: root/level1:2/level2/level3:5 [5:5]"
				, "structure:descendantChanged/attr:unchanged: root/level1:2/level2/level3:6 [6:8]"
				, "structure:unchanged/attr:unchanged: root/level1:2/level2/level3:6/level4:1 [7:7]"
				, "structure:changed/attr:unchanged: root/level1:20 [11:11]"
				), toStateStrings(xmlLeft.elements));
	}
	
	@Test
	public void branchAdded() throws XMLStreamException, IOException {
		String[] inputLeft = {
				"<root a='1'>"
				, "  <level1 b='2'>"
				, "    <level2 c='3'>"
				, "      <level3 d='4'/>"
				, "      <level3 d='5'/>"
				, "      <level3 d='6'>"
				, "        <level4 e='1'/>"
				, "      </level3>"
				, "    </level2>"
				, "  </level1>"
				, "  <level1 b='20'/>"
				, "</root>"
		};
		String[] inputRight = {
				"<root a='1'>"
				, "  <level1 b='2'>"
				, "    <level2 c='3'>"
				, "      <level3 d='4'/>"
				, "      <level3 d='5'/>"
				, "      <level3 d='6'>"
				, "        <level4 e='1'/>"
				, "      </level3>"
				, "      <level3 d='NEW#1'>"
				, "        <level4 e='NEWSUB#1'/>"
				, "        <level4 e='NEWSUB#2'>"
				, "          <level5 e='NEWSUB#2#SUBLEAF'/>"
				, "        </level4>"
				, "      </level3>"
				, "    </level2>"
				, "    <level2 d='afterbranch'/>"
				, "  </level1>"
				, "  <level1 b='20'/>"
				, "  <level1 d='afterbranch'/>"
				, "</root>"
		};
		
		List<String> discriminatingAttributes = (Arrays.asList("b", "d", "e"));
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		XmlStruct xmlLeft = lexer.parse(IO.asString(inputLeft));
		XmlStruct xmlRight = lexer.parse(IO.asString(inputRight));
		
		XmlDiffer differ = xmlDifferFactory.create(ignoreAttributeNames);
		XmlDiffer.Result diffResult = differ.diff(xmlLeft, xmlRight);
		basicChecks(xmlLeft, xmlRight, diffResult);
		Assert.assertEquals(3, diffResult.nAdded);
		Assert.assertEquals(0, diffResult.nRemoved);
		Assert.assertEquals(0, diffResult.nAttributeChanged);
		
		checkList(Arrays.asList(
				  "structure:descendantChanged/attr:unchanged: root [1:20]"
				, "structure:descendantChanged/attr:unchanged: root/level1:2 [2:17]"
				, "structure:descendantChanged/attr:unchanged: root/level1:2/level2 [3:15]"
				, "structure:unchanged/attr:unchanged: root/level1:2/level2/level3:4 [4:4]"
				, "structure:unchanged/attr:unchanged: root/level1:2/level2/level3:5 [5:5]"
				, "structure:unchanged/attr:unchanged: root/level1:2/level2/level3:6 [6:8]"
				, "structure:unchanged/attr:unchanged: root/level1:2/level2/level3:6/level4:1 [7:7]"
				, "structure:changed/attr:unchanged: root/level1:2/level2/level3:NEW#1 [9:14]"
				, "structure:parentChanged/attr:unchanged: root/level1:2/level2/level3:NEW#1/level4:NEWSUB#1 [10:10]"
				, "structure:parentChanged/attr:unchanged: root/level1:2/level2/level3:NEW#1/level4:NEWSUB#2 [11:13]"
				, "structure:parentChanged/attr:unchanged: root/level1:2/level2/level3:NEW#1/level4:NEWSUB#2/level5:NEWSUB#2#SUBLEAF [12:12]"
				, "structure:changed/attr:unchanged: root/level1:2/level2:afterbranch [16:16]"
				, "structure:unchanged/attr:unchanged: root/level1:20 [18:18]"
				, "structure:changed/attr:unchanged: root/level1:afterbranch [19:19]"
				), toStateStrings(xmlRight.elements));
		
		checkList(Arrays.asList(
				  "structure:descendantChanged/attr:unchanged: root [1:12]"
				, "structure:descendantChanged/attr:unchanged: root/level1:2 [2:10]"
				, "structure:descendantChanged/attr:unchanged: root/level1:2/level2 [3:9]"
				, "structure:unchanged/attr:unchanged: root/level1:2/level2/level3:4 [4:4]"
				, "structure:unchanged/attr:unchanged: root/level1:2/level2/level3:5 [5:5]"
				, "structure:unchanged/attr:unchanged: root/level1:2/level2/level3:6 [6:8]"
				, "structure:unchanged/attr:unchanged: root/level1:2/level2/level3:6/level4:1 [7:7]"
				, "structure:unchanged/attr:unchanged: root/level1:20 [11:11]"
				), toStateStrings(xmlLeft.elements));
	}
	
	@Test
	public void branchAddedAndRemoved() throws XMLStreamException, IOException {
		String[] inputLeft = {
				"<root a='1'>"
				, "  <level1 b='2'>"
				, "    <level2 c='3'>"
				, "      <level3 d='4'/>"
				, "      <level3 d='5'/>"
				, "      <level3 d='6'>"
				, "        <level4 e='1'/>"
				, "      </level3>"
				, "    </level2>"
				, "  </level1>"
				, "  <level1 b='20'/>"
				, "</root>"
		};
		String[] inputRight = {
				"<root a='1'>"
				, "  <level1 b='2'>"
				, "    <level2 c='3'>"
				, "      <level3 d='4'/>"
				, "      <level3 d='5'/>"
//				, "      <level3 d='6'>" // REMOVED
//				, "        <level4 e='1'/>"
//				, "      </level3>"
				, "      <level3 d='NEW#1'>"
				, "        <level4 e='NEWSUB#1'/>"
				, "        <level4 e='NEWSUB#2'>"
				, "          <level5 e='NEWSUB#2#SUBLEAF'/>"
				, "        </level4>"
				, "      </level3>"
				, "    </level2>"
				, "    <level2 d='afterbranch'/>"
				, "  </level1>"
//				, "  <level1 b='20'/>" // REMOVED
				, "  <level1 d='afterbranch'/>"
				, "</root>"
		};
		
		List<String> discriminatingAttributes = Arrays.asList("b", "d", "e");
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		XmlStruct xmlLeft = lexer.parse(IO.asString(inputLeft));
		XmlStruct xmlRight = lexer.parse(IO.asString(inputRight));
		
		
		XmlDiffer differ = xmlDifferFactory.create(ignoreAttributeNames);
		XmlDiffer.Result diffResult = differ.diff(xmlLeft, xmlRight);
		basicChecks(xmlLeft, xmlRight, diffResult);
		Assert.assertEquals(3, diffResult.nAdded);
		Assert.assertEquals(2, diffResult.nRemoved);
		Assert.assertEquals(0, diffResult.nAttributeChanged);
		
		checkList(Arrays.asList(
				  "structure:descendantChanged/attr:unchanged: root [1:16]"
				, "structure:descendantChanged/attr:unchanged: root/level1:2 [2:14]"
				, "structure:descendantChanged/attr:unchanged: root/level1:2/level2 [3:12]"
				, "structure:unchanged/attr:unchanged: root/level1:2/level2/level3:4 [4:4]"
				, "structure:unchanged/attr:unchanged: root/level1:2/level2/level3:5 [5:5]"
				, "structure:changed/attr:unchanged: root/level1:2/level2/level3:NEW#1 [6:11]"
				, "structure:parentChanged/attr:unchanged: root/level1:2/level2/level3:NEW#1/level4:NEWSUB#1 [7:7]"
				, "structure:parentChanged/attr:unchanged: root/level1:2/level2/level3:NEW#1/level4:NEWSUB#2 [8:10]"
				, "structure:parentChanged/attr:unchanged: root/level1:2/level2/level3:NEW#1/level4:NEWSUB#2/level5:NEWSUB#2#SUBLEAF [9:9]"
				, "structure:changed/attr:unchanged: root/level1:2/level2:afterbranch [13:13]"
				, "structure:changed/attr:unchanged: root/level1:afterbranch [15:15]"
				), toStateStrings(xmlRight.elements));
		
		checkList(Arrays.asList(
				  "structure:descendantChanged/attr:unchanged: root [1:12]"
				, "structure:descendantChanged/attr:unchanged: root/level1:2 [2:10]"
				, "structure:descendantChanged/attr:unchanged: root/level1:2/level2 [3:9]"
				, "structure:unchanged/attr:unchanged: root/level1:2/level2/level3:4 [4:4]"
				, "structure:unchanged/attr:unchanged: root/level1:2/level2/level3:5 [5:5]"
				, "structure:changed/attr:unchanged: root/level1:2/level2/level3:6 [6:8]"
				, "structure:parentChanged/attr:unchanged: root/level1:2/level2/level3:6/level4:1 [7:7]"
				, "structure:changed/attr:unchanged: root/level1:20 [11:11]"
				), toStateStrings(xmlLeft.elements));
	}
	
	@Test
	public void branchAddedAndRemovedWithAmbiguous() throws XMLStreamException, IOException {
		String[] inputLeft = {
				"<root>"
				, "  <config>"
				, "    <sensorgroup>"
				, "      <sensor key='sensor1'/>"
				, "    </sensorgroup>"
				, "    <sensorgroup>"
				, "      <sensor key='REMOVEDSENSOR'/>"
				, "    </sensorgroup>"
				, "    <sensorgroup>"
				, "      <sensor key='sensor2'/>"
				, "    </sensorgroup>"
				, "  </config>"
				, "</root>"
		};
		String[] inputRight = {
				"<root>"
				, "  <config>"
				, "    <sensorgroup>"
				, "      <sensor key='sensor1'/>"
				, "    </sensorgroup>"
				, "    <sensorgroup>"
				, "      <sensor key='NEWSENSOR'/>"
				, "    </sensorgroup>"
				, "    <sensorgroup>"
				, "      <sensor key='sensor2'/>"
				, "    </sensorgroup>"
				, "  </config>"
				, "</root>"
		};
		
		List<String> discriminatingAttributes = Arrays.asList("key");
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		XmlStruct xmlLeft = lexer.parse(IO.asString(inputLeft));
		XmlStruct xmlRight = lexer.parse(IO.asString(inputRight));
		
		XmlDiffer differ = xmlDifferFactory.create(ignoreAttributeNames);
		XmlDiffer.Result diffResult = differ.diff(xmlLeft, xmlRight);
		basicChecks(xmlLeft, xmlRight, diffResult);
		Assert.assertEquals(1, diffResult.nAdded);
		Assert.assertEquals(1, diffResult.nRemoved);
		Assert.assertEquals(0, diffResult.nAttributeChanged);

		checkList(Arrays.asList(
				  "structure:descendantChanged/attr:unchanged: root [1:13]"
				, "structure:descendantChanged/attr:unchanged: root/config [2:12]"
				, "structure:unchanged/attr:unchanged: root/config/sensorgroup [3:5]"
				, "structure:unchanged/attr:unchanged: root/config/sensorgroup/sensor:sensor1 [4:4]"
				, "structure:changed/attr:unchanged: root/config/sensorgroup [6:8]"
				, "structure:parentChanged/attr:unchanged: root/config/sensorgroup/sensor:NEWSENSOR [7:7]"
				, "structure:unchanged/attr:unchanged: root/config/sensorgroup [9:11]"
				, "structure:unchanged/attr:unchanged: root/config/sensorgroup/sensor:sensor2 [10:10]"
				), toStateStrings(xmlRight.elements));
		
		checkList(Arrays.asList(
				  "structure:descendantChanged/attr:unchanged: root [1:13]"
				, "structure:descendantChanged/attr:unchanged: root/config [2:12]"
				, "structure:unchanged/attr:unchanged: root/config/sensorgroup [3:5]"
				, "structure:unchanged/attr:unchanged: root/config/sensorgroup/sensor:sensor1 [4:4]"
				, "structure:changed/attr:unchanged: root/config/sensorgroup [6:8]"
				, "structure:parentChanged/attr:unchanged: root/config/sensorgroup/sensor:REMOVEDSENSOR [7:7]"
				, "structure:unchanged/attr:unchanged: root/config/sensorgroup [9:11]"
				, "structure:unchanged/attr:unchanged: root/config/sensorgroup/sensor:sensor2 [10:10]"
				), toStateStrings(xmlLeft.elements));
	}
	
	@Test
	public void branchAddedAndRemovedWithAmbiguousDeeperNesting() throws XMLStreamException, IOException {
		String[] inputLeft = {
				"<root>"
				, "  <config>"
				, "    <sensorgroup>"
				, "      <sensor key='sensor1'/>"
				, "    </sensorgroup>"
				, "    <sensorgroup>"
				, "      <sensor key='sensor2'/>"
				, "    </sensorgroup>"
				, "    <sensorgroup>"
				, "      <inbetween>"
				, "        <sensor key='sensor3'/>"
				, "      </inbetween>"
				, "    </sensorgroup>"
				, "    <sensorgroup>"
				, "      <inbetween>"
				, "        <sensor key='REMOVEDSENSOR'/>"
				, "      </inbetween>"
				, "    </sensorgroup>"
				, "  </config>"
				, "</root>"
		};
		String[] inputRight = {
				"<root>"
				, "  <config>"
				, "    <sensorgroup>"
				, "      <sensor key='sensor1'/>"
				, "    </sensorgroup>"
				, "    <sensorgroup>"
				, "      <sensor key='sensor2'/>"
				, "    </sensorgroup>"
				, "    <sensorgroup>"
				, "      <inbetween>"
				, "        <sensor key='ADDEDSENSOR'/>"
				, "      </inbetween>"
				, "    </sensorgroup>"
				, "    <sensorgroup>"
				, "      <inbetween>"
				, "        <sensor key='sensor3'/>"
				, "      </inbetween>"
				, "    </sensorgroup>"
				, "  </config>"
				, "</root>"
		};
		
		List<String> discriminatingAttributes = Arrays.asList("key");
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		XmlStruct xmlLeft = lexer.parse(IO.asString(inputLeft));
		XmlStruct xmlRight = lexer.parse(IO.asString(inputRight));
		
		XmlDiffer differ = xmlDifferFactory.create(ignoreAttributeNames);
		XmlDiffer.Result diffResult = differ.diff(xmlLeft, xmlRight);
		basicChecks(xmlLeft, xmlRight, diffResult);
		Assert.assertEquals(1, diffResult.nAdded);
		Assert.assertEquals(1, diffResult.nRemoved);
		Assert.assertEquals(0, diffResult.nAttributeChanged);
		
		if (isDifferImplStringMetric(differ)) { //FIXME we can only have same results with "sticky depth"
			checkList(Arrays.asList(
					  "structure:descendantChanged/attr:unchanged: root [1:20]"
					, "structure:descendantChanged/attr:unchanged: root/config [2:19]"
					, "structure:unchanged/attr:unchanged: root/config/sensorgroup [3:5]"
					, "structure:unchanged/attr:unchanged: root/config/sensorgroup/sensor:sensor1 [4:4]"
					, "structure:unchanged/attr:unchanged: root/config/sensorgroup [6:8]"
					, "structure:unchanged/attr:unchanged: root/config/sensorgroup/sensor:sensor2 [7:7]"
					, "structure:descendantChanged/attr:unchanged: root/config/sensorgroup [9:13]"
					, "structure:descendantChanged/attr:unchanged: root/config/sensorgroup/inbetween [10:12]"
					, "structure:changed/attr:unchanged: root/config/sensorgroup/inbetween/sensor:ADDEDSENSOR [11:11]"
					, "structure:unchanged/attr:unchanged: root/config/sensorgroup [14:18]"
					, "structure:unchanged/attr:unchanged: root/config/sensorgroup/inbetween [15:17]"
					, "structure:unchanged/attr:unchanged: root/config/sensorgroup/inbetween/sensor:sensor3 [16:16]"
					), toStateStrings(xmlRight.elements));
			
			checkList(Arrays.asList(
					  "structure:descendantChanged/attr:unchanged: root [1:20]"
					, "structure:descendantChanged/attr:unchanged: root/config [2:19]"
					, "structure:unchanged/attr:unchanged: root/config/sensorgroup [3:5]"
					, "structure:unchanged/attr:unchanged: root/config/sensorgroup/sensor:sensor1 [4:4]"
					, "structure:unchanged/attr:unchanged: root/config/sensorgroup [6:8]"
					, "structure:unchanged/attr:unchanged: root/config/sensorgroup/sensor:sensor2 [7:7]"
					, "structure:unchanged/attr:unchanged: root/config/sensorgroup [9:13]"
					, "structure:unchanged/attr:unchanged: root/config/sensorgroup/inbetween [10:12]"
					, "structure:unchanged/attr:unchanged: root/config/sensorgroup/inbetween/sensor:sensor3 [11:11]"
					, "structure:descendantChanged/attr:unchanged: root/config/sensorgroup [14:18]"
					, "structure:descendantChanged/attr:unchanged: root/config/sensorgroup/inbetween [15:17]"
					, "structure:changed/attr:unchanged: root/config/sensorgroup/inbetween/sensor:REMOVEDSENSOR [16:16]"
					), toStateStrings(xmlLeft.elements));
		} else {		
			checkList(Arrays.asList(
					  "structure:descendantChanged/attr:unchanged: root [1:20]"
					, "structure:descendantChanged/attr:unchanged: root/config [2:19]"
					, "structure:unchanged/attr:unchanged: root/config/sensorgroup [3:5]"
					, "structure:unchanged/attr:unchanged: root/config/sensorgroup/sensor:sensor1 [4:4]"
					, "structure:unchanged/attr:unchanged: root/config/sensorgroup [6:8]"
					, "structure:unchanged/attr:unchanged: root/config/sensorgroup/sensor:sensor2 [7:7]"
					, "structure:changed/attr:unchanged: root/config/sensorgroup [9:13]"
					, "structure:parentChanged/attr:unchanged: root/config/sensorgroup/inbetween [10:12]"
					, "structure:parentChanged/attr:unchanged: root/config/sensorgroup/inbetween/sensor:ADDEDSENSOR [11:11]"
					, "structure:unchanged/attr:unchanged: root/config/sensorgroup [14:18]"
					, "structure:unchanged/attr:unchanged: root/config/sensorgroup/inbetween [15:17]"
					, "structure:unchanged/attr:unchanged: root/config/sensorgroup/inbetween/sensor:sensor3 [16:16]"
					), toStateStrings(xmlRight.elements));
			
			checkList(Arrays.asList(
					"structure:descendantChanged/attr:unchanged: root [1:20]"
					, "structure:descendantChanged/attr:unchanged: root/config [2:19]"
					, "structure:unchanged/attr:unchanged: root/config/sensorgroup [3:5]"
					, "structure:unchanged/attr:unchanged: root/config/sensorgroup/sensor:sensor1 [4:4]"
					, "structure:unchanged/attr:unchanged: root/config/sensorgroup [6:8]"
					, "structure:unchanged/attr:unchanged: root/config/sensorgroup/sensor:sensor2 [7:7]"
					, "structure:unchanged/attr:unchanged: root/config/sensorgroup [9:13]"
					, "structure:unchanged/attr:unchanged: root/config/sensorgroup/inbetween [10:12]"
					, "structure:unchanged/attr:unchanged: root/config/sensorgroup/inbetween/sensor:sensor3 [11:11]"
					, "structure:changed/attr:unchanged: root/config/sensorgroup [14:18]"
					, "structure:parentChanged/attr:unchanged: root/config/sensorgroup/inbetween [15:17]"
					, "structure:parentChanged/attr:unchanged: root/config/sensorgroup/inbetween/sensor:REMOVEDSENSOR [16:16]"
					), toStateStrings(xmlLeft.elements));
		}
	}
	
	@Test
	public void branchAddedAndRemovedWithAmbiguousParentConflict() throws XMLStreamException, IOException {
		String[] inputLeft = {
				"<root>"
				, "  <config>"
				, "    <sensorgroup>"
				, "      <sensor key='sensor1'/>"
				, "    </sensorgroup>"
				, "    <sensorgroup>"
				, "      <sensor key='sensor2'/>"
				, "    </sensorgroup>"
				, "    <sensorgroup>"
				, "      <sensor key='sensor3'/>"
				, "    </sensorgroup>"
				, "  </config>"
				, "</root>"
		};
		String[] inputRight = {
				"<root>"
				, "  <config>"
				, "    <sensorgroup>"
				, "      <sensor key='sensor1'/>"
				, "      <sensor key='EVIL-ADDITION-MAKES-PARENT-CONFLICT'/>"
				, "    </sensorgroup>"
				, "    <sensorgroup>"
				, "      <sensor key='sensor3'/>"
				, "    </sensorgroup>"
				, "    <sensorgroup>"
				, "      <sensor key='sensor2'/>"
				, "    </sensorgroup>"
				, "  </config>"
				, "</root>"
		};
		
		List<String> discriminatingAttributes = Arrays.asList("key");
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		XmlStruct xmlLeft = lexer.parse(IO.asString(inputLeft));
		XmlStruct xmlRight = lexer.parse(IO.asString(inputRight));
		
		XmlDiffer differ = xmlDifferFactory.create(ignoreAttributeNames);
		XmlDiffer.Result diffResult = differ.diff(xmlLeft, xmlRight);
		basicChecks(xmlLeft, xmlRight, diffResult);
		Assert.assertEquals(1, diffResult.nAdded);
		Assert.assertEquals(1, diffResult.nRemoved);
		Assert.assertEquals(0, diffResult.nAttributeChanged);
		
		if (isDifferImplStringMetric(differ)) { //FIXME StringMetricXmlDiffer looks correct 
			checkList(Arrays.asList(
					  "structure:descendantChanged/attr:unchanged: root [1:14]"
					, "structure:descendantChanged/attr:unchanged: root/config [2:13]"
					, "structure:changed/attr:unchanged: root/config/sensorgroup [3:6]"
					, "structure:parentChanged/attr:unchanged: root/config/sensorgroup/sensor:sensor1 [4:4]"
					, "structure:parentChanged/attr:unchanged: root/config/sensorgroup/sensor:EVIL-ADDITION-MAKES-PARENT-CONFLICT [5:5]"
					, "structure:unchanged/attr:unchanged: root/config/sensorgroup [7:9]"
					, "structure:unchanged/attr:unchanged: root/config/sensorgroup/sensor:sensor3 [8:8]"
					, "structure:unchanged/attr:unchanged: root/config/sensorgroup [10:12]"
					, "structure:unchanged/attr:unchanged: root/config/sensorgroup/sensor:sensor2 [11:11]"
					), toStateStrings(xmlRight.elements));
			
			checkList(Arrays.asList(
					  "structure:descendantChanged/attr:unchanged: root [1:13]"
					, "structure:descendantChanged/attr:unchanged: root/config [2:12]"
					, "structure:changed/attr:unchanged: root/config/sensorgroup [3:5]"
					, "structure:parentChanged/attr:unchanged: root/config/sensorgroup/sensor:sensor1 [4:4]"
					, "structure:unchanged/attr:unchanged: root/config/sensorgroup [6:8]"
					, "structure:unchanged/attr:unchanged: root/config/sensorgroup/sensor:sensor2 [7:7]"
					, "structure:unchanged/attr:unchanged: root/config/sensorgroup [9:11]"
					, "structure:unchanged/attr:unchanged: root/config/sensorgroup/sensor:sensor3 [10:10]"
					), toStateStrings(xmlLeft.elements));
		} else {
			checkList(Arrays.asList(
					  "structure:descendantChanged/attr:unchanged: root [1:14]"
					, "structure:descendantChanged/attr:unchanged: root/config [2:13]"
					, "structure:changed/attr:unchanged: root/config/sensorgroup [3:6]"
					, "structure:unchanged/attr:unchanged: root/config/sensorgroup/sensor:sensor1 [4:4]"
					, "structure:parentChanged/attr:unchanged: root/config/sensorgroup/sensor:EVIL-ADDITION-MAKES-PARENT-CONFLICT [5:5]"
					, "structure:unchanged/attr:unchanged: root/config/sensorgroup [7:9]"
					, "structure:unchanged/attr:unchanged: root/config/sensorgroup/sensor:sensor3 [8:8]"
					, "structure:unchanged/attr:unchanged: root/config/sensorgroup [10:12]"
					, "structure:unchanged/attr:unchanged: root/config/sensorgroup/sensor:sensor2 [11:11]"
					), toStateStrings(xmlRight.elements));
			
			checkList(Arrays.asList(
					  "structure:descendantChanged/attr:unchanged: root [1:13]"
					, "structure:descendantChanged/attr:unchanged: root/config [2:12]"
					, "structure:changed/attr:unchanged: root/config/sensorgroup [3:5]"
					, "structure:unchanged/attr:unchanged: root/config/sensorgroup/sensor:sensor1 [4:4]"
					, "structure:unchanged/attr:unchanged: root/config/sensorgroup [6:8]"
					, "structure:unchanged/attr:unchanged: root/config/sensorgroup/sensor:sensor2 [7:7]"
					, "structure:unchanged/attr:unchanged: root/config/sensorgroup [9:11]"
					, "structure:unchanged/attr:unchanged: root/config/sensorgroup/sensor:sensor3 [10:10]"
					), toStateStrings(xmlLeft.elements));			
		}
	}
	
	@Test
	public void sameDifferentOrder() throws XMLStreamException, IOException {
		String[] inputLeft = {
				"<root>"
				, "  <config>"
				, "    <sensorgroup>"
				, "      <sensor key='sensor1'/>"
				, "    </sensorgroup>"
				, "    <sensorgroup>"
				, "      <sensor key='sensor2'/>"
				, "    </sensorgroup>"
				, "    <sensorgroup>"
				, "      <sensor key='sensor3'/>"
				, "    </sensorgroup>"
				, "  </config>"
				, "</root>"
		};
		String[] inputRight = {
				"<root>"
				, "  <config>"
				, "    <sensorgroup>"
				, "      <sensor key='sensor1'/>"
				, "    </sensorgroup>"
				, "    <sensorgroup>"
				, "      <sensor key='sensor3'/>"
				, "    </sensorgroup>"
				, "    <sensorgroup>"
				, "      <sensor key='sensor2'/>"
				, "    </sensorgroup>"
				, "  </config>"
				, "</root>"
		};
		
		List<String> discriminatingAttributes = Arrays.asList("key");
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		XmlStruct xmlLeft = lexer.parse(IO.asString(inputLeft));
		XmlStruct xmlRight = lexer.parse(IO.asString(inputRight));
		
		XmlDiffer differ = xmlDifferFactory.create(ignoreAttributeNames);
		XmlDiffer.Result diffResult = differ.diff(xmlLeft, xmlRight);
		basicChecks(xmlLeft, xmlRight, diffResult);
		Assert.assertEquals(0, diffResult.nAdded);
		Assert.assertEquals(0, diffResult.nRemoved);
		Assert.assertEquals(0, diffResult.nAttributeChanged);
		
		checkList(Arrays.asList(
				  "structure:unchanged/attr:unchanged: root [1:13]"
				, "structure:unchanged/attr:unchanged: root/config [2:12]"
				, "structure:unchanged/attr:unchanged: root/config/sensorgroup [3:5]"
				, "structure:unchanged/attr:unchanged: root/config/sensorgroup/sensor:sensor1 [4:4]"
				, "structure:unchanged/attr:unchanged: root/config/sensorgroup [6:8]"
				, "structure:unchanged/attr:unchanged: root/config/sensorgroup/sensor:sensor3 [7:7]"
				, "structure:unchanged/attr:unchanged: root/config/sensorgroup [9:11]"
				, "structure:unchanged/attr:unchanged: root/config/sensorgroup/sensor:sensor2 [10:10]"
				), toStateStrings(xmlRight.elements));
		
		checkList(Arrays.asList(
				  "structure:unchanged/attr:unchanged: root [1:13]"
				, "structure:unchanged/attr:unchanged: root/config [2:12]"
				, "structure:unchanged/attr:unchanged: root/config/sensorgroup [3:5]"
				, "structure:unchanged/attr:unchanged: root/config/sensorgroup/sensor:sensor1 [4:4]"
				, "structure:unchanged/attr:unchanged: root/config/sensorgroup [6:8]"
				, "structure:unchanged/attr:unchanged: root/config/sensorgroup/sensor:sensor2 [7:7]"
				, "structure:unchanged/attr:unchanged: root/config/sensorgroup [9:11]"
				, "structure:unchanged/attr:unchanged: root/config/sensorgroup/sensor:sensor3 [10:10]"
				), toStateStrings(xmlLeft.elements));
	}
	
	@Test
	public void ambiguousLeavesSame() throws XMLStreamException, IOException {
		String[] inputLeft = {
				  "<root>"
				, "  <config>"
		        ,"	   <sensor id=\"com.dynatrace.diagnostics.knowledgesensor.dotnet.logging\" includeprops=\"false\" capture=\"active\">"
	            ,"		  <property enabled=\"true\" severitymatch=\"starts\" loggernamematch=\"starts\" severity=\"SEVERE\" loggername=\"\" />"
	            ,"		  <property enabled=\"true\" severitymatch=\"starts\" loggernamematch=\"starts\" severity=\"FATAL\" loggername=\"\" />"
	            ,"		  <property enabled=\"true\" severitymatch=\"starts\" loggernamematch=\"starts\" severity=\"ERROR\" loggername=\"\" />"
				, "    </sensor>"
				, "  </config>"
				, "</root>"
		};
		String[] inputRight = {
				  "<root>"
				, "  <config>"
		        ,"	   <sensor id=\"com.dynatrace.diagnostics.knowledgesensor.dotnet.logging\" includeprops=\"false\" capture=\"active\">"
	            ,"		  <property enabled=\"true\" severitymatch=\"starts\" loggernamematch=\"starts\" severity=\"SEVERE\" loggername=\"\" />"
	            ,"		  <property enabled=\"true\" severitymatch=\"starts\" loggernamematch=\"starts\" severity=\"FATAL\" loggername=\"\" />"
	            ,"		  <property enabled=\"true\" severitymatch=\"starts\" loggernamematch=\"starts\" severity=\"ERROR\" loggername=\"\" />"
				, "    </sensor>"
				, "  </config>"
				, "</root>"
		};
		
		List<String> discriminatingAttributes = Arrays.asList("id");
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		XmlStruct xmlLeft = lexer.parse(IO.asString(inputLeft));
		XmlStruct xmlRight = lexer.parse(IO.asString(inputRight));
		
		XmlDiffer differ = xmlDifferFactory.create(ignoreAttributeNames);
		XmlDiffer.Result diffResult = differ.diff(xmlLeft, xmlRight);
		basicChecks(xmlLeft, xmlRight, diffResult);
		Assert.assertEquals(0, diffResult.nAdded);
		Assert.assertEquals(0, diffResult.nRemoved);
		Assert.assertEquals(0, diffResult.nAttributeChanged);
		
		checkList(Arrays.asList(
				  "structure:unchanged/attr:unchanged: root [1:9]"
				, "structure:unchanged/attr:unchanged: root/config [2:8]"
				, "structure:unchanged/attr:unchanged: root/config/sensor:com.dynatrace.diagnostics.knowledgesensor.dotnet.logging [3:7]"
				, "structure:unchanged/attr:unchanged: root/config/sensor:com.dynatrace.diagnostics.knowledgesensor.dotnet.logging/property [4:4]"
				, "structure:unchanged/attr:unchanged: root/config/sensor:com.dynatrace.diagnostics.knowledgesensor.dotnet.logging/property [5:5]"
				, "structure:unchanged/attr:unchanged: root/config/sensor:com.dynatrace.diagnostics.knowledgesensor.dotnet.logging/property [6:6]"
				), toStateStrings(xmlRight.elements));
		
		checkList(Arrays.asList(
				  "structure:unchanged/attr:unchanged: root [1:9]"
				, "structure:unchanged/attr:unchanged: root/config [2:8]"
				, "structure:unchanged/attr:unchanged: root/config/sensor:com.dynatrace.diagnostics.knowledgesensor.dotnet.logging [3:7]"
				, "structure:unchanged/attr:unchanged: root/config/sensor:com.dynatrace.diagnostics.knowledgesensor.dotnet.logging/property [4:4]"
				, "structure:unchanged/attr:unchanged: root/config/sensor:com.dynatrace.diagnostics.knowledgesensor.dotnet.logging/property [5:5]"
				, "structure:unchanged/attr:unchanged: root/config/sensor:com.dynatrace.diagnostics.knowledgesensor.dotnet.logging/property [6:6]"
				), toStateStrings(xmlLeft.elements));
	}
	
	@Test
	public void ambiguousLeavesAttributesDifferent() throws XMLStreamException, IOException {
		String[] inputLeft = {
				"<root>"
				, "  <config>"
				,"	   <sensor id=\"com.dynatrace.diagnostics.knowledgesensor.dotnet.logging\" includeprops=\"false\" capture=\"active\">"
				,"		  <property enabled=\"true\" severitymatch=\"starts\" loggernamematch=\"starts\" severity=\"SEVERE\" loggername=\"\" />"
				,"		  <property enabled=\"true\" severitymatch=\"starts\" loggernamematch=\"starts\" severity=\"FATAL\" loggername=\"\" />"
				,"		  <property enabled=\"true\" severitymatch=\"starts\" loggernamematch=\"starts\" severity=\"ERROR\" loggername=\"\" />"
				,"		  <property  ignored=\"1\" />"
				, "    </sensor>"
				, "  </config>"
				, "</root>"
		};
		String[] inputRight = {
				"<root>"
				, "  <config>"
				,"	   <sensor id=\"com.dynatrace.diagnostics.knowledgesensor.dotnet.logging\" includeprops=\"false\" capture=\"active\">"
				,"		  <property enabled=\"true\" severitymatch=\"starts\" loggernamematch=\"starts\" severity=\"SEVERE1\" loggername=\"\"/>"
				,"		  <property enabled=\"true\" severitymatch=\"starts\" loggernamematch=\"starts\" severity=\"FATAL\" loggername=\"foo\" />"
				,"		  <property enabled=\"true\" severitymatch=\"contains\" loggernamematch=\"starts\" severity=\"ERROR\" loggername=\"\" />"
				,"		  <property  ignored=\"2\" />"
				, "    </sensor>"
				, "  </config>"
				, "</root>"
		};
		
		List<String> discriminatingAttributes = Arrays.asList("id");
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		XmlStruct xmlLeft = lexer.parse(IO.asString(inputLeft));
		XmlStruct xmlRight = lexer.parse(IO.asString(inputRight));
		
		XmlDiffer differ = xmlDifferFactory.create(Arrays.asList());
		XmlDiffer.Result diffResult = differ.diff(xmlLeft, xmlRight);
		basicChecks(xmlLeft, xmlRight, diffResult);
		Assert.assertEquals(0, diffResult.nAdded);
		Assert.assertEquals(0, diffResult.nRemoved);
		Assert.assertEquals(4, diffResult.nAttributeChanged);
		
		checkList(Arrays.asList(
				  "structure:unchanged/attr:descendantChanged: root [1:10]"
				, "structure:unchanged/attr:descendantChanged: root/config [2:9]"
				, "structure:unchanged/attr:descendantChanged: root/config/sensor:com.dynatrace.diagnostics.knowledgesensor.dotnet.logging [3:8]"
				, "structure:unchanged/attr:changed: root/config/sensor:com.dynatrace.diagnostics.knowledgesensor.dotnet.logging/property [4:4]"
				, "structure:unchanged/attr:changed: root/config/sensor:com.dynatrace.diagnostics.knowledgesensor.dotnet.logging/property [5:5]"
				, "structure:unchanged/attr:changed: root/config/sensor:com.dynatrace.diagnostics.knowledgesensor.dotnet.logging/property [6:6]"
				, "structure:unchanged/attr:changed: root/config/sensor:com.dynatrace.diagnostics.knowledgesensor.dotnet.logging/property [7:7]"
				), toStateStrings(xmlRight.elements));
		
		checkList(Arrays.asList(
				  "structure:unchanged/attr:descendantChanged: root [1:10]"
				, "structure:unchanged/attr:descendantChanged: root/config [2:9]"
				, "structure:unchanged/attr:descendantChanged: root/config/sensor:com.dynatrace.diagnostics.knowledgesensor.dotnet.logging [3:8]"
				, "structure:unchanged/attr:changed: root/config/sensor:com.dynatrace.diagnostics.knowledgesensor.dotnet.logging/property [4:4]"
				, "structure:unchanged/attr:changed: root/config/sensor:com.dynatrace.diagnostics.knowledgesensor.dotnet.logging/property [5:5]"
				, "structure:unchanged/attr:changed: root/config/sensor:com.dynatrace.diagnostics.knowledgesensor.dotnet.logging/property [6:6]"
				, "structure:unchanged/attr:changed: root/config/sensor:com.dynatrace.diagnostics.knowledgesensor.dotnet.logging/property [7:7]"
				), toStateStrings(xmlLeft.elements));
		
		differ = xmlDifferFactory.create(Arrays.asList("ignored"));
		diffResult = differ.diff(xmlLeft, xmlRight);
		basicChecks(xmlLeft, xmlRight, diffResult);
		Assert.assertEquals(0, diffResult.nAdded);
		Assert.assertEquals(0, diffResult.nRemoved);
		Assert.assertEquals(3, diffResult.nAttributeChanged);
		
		checkList(Arrays.asList(
				  "structure:unchanged/attr:descendantChanged: root [1:10]"
				, "structure:unchanged/attr:descendantChanged: root/config [2:9]"
				, "structure:unchanged/attr:descendantChanged: root/config/sensor:com.dynatrace.diagnostics.knowledgesensor.dotnet.logging [3:8]"
				, "structure:unchanged/attr:changed: root/config/sensor:com.dynatrace.diagnostics.knowledgesensor.dotnet.logging/property [4:4]"
				, "structure:unchanged/attr:changed: root/config/sensor:com.dynatrace.diagnostics.knowledgesensor.dotnet.logging/property [5:5]"
				, "structure:unchanged/attr:changed: root/config/sensor:com.dynatrace.diagnostics.knowledgesensor.dotnet.logging/property [6:6]"
				, "structure:unchanged/attr:unchanged: root/config/sensor:com.dynatrace.diagnostics.knowledgesensor.dotnet.logging/property [7:7]"
				), toStateStrings(xmlRight.elements));
		
		checkList(Arrays.asList(
				  "structure:unchanged/attr:descendantChanged: root [1:10]"
				, "structure:unchanged/attr:descendantChanged: root/config [2:9]"
				, "structure:unchanged/attr:descendantChanged: root/config/sensor:com.dynatrace.diagnostics.knowledgesensor.dotnet.logging [3:8]"
				, "structure:unchanged/attr:changed: root/config/sensor:com.dynatrace.diagnostics.knowledgesensor.dotnet.logging/property [4:4]"
				, "structure:unchanged/attr:changed: root/config/sensor:com.dynatrace.diagnostics.knowledgesensor.dotnet.logging/property [5:5]"
				, "structure:unchanged/attr:changed: root/config/sensor:com.dynatrace.diagnostics.knowledgesensor.dotnet.logging/property [6:6]"
				, "structure:unchanged/attr:unchanged: root/config/sensor:com.dynatrace.diagnostics.knowledgesensor.dotnet.logging/property [7:7]"
				), toStateStrings(xmlLeft.elements));
		
		differ = xmlDifferFactory.create(Arrays.asList("root/config/sensor/property:ignored"));
		diffResult = differ.diff(xmlLeft, xmlRight);
		basicChecks(xmlLeft, xmlRight, diffResult);
		Assert.assertEquals(0, diffResult.nAdded);
		Assert.assertEquals(0, diffResult.nRemoved);
		Assert.assertEquals(3, diffResult.nAttributeChanged);
		
		checkList(Arrays.asList(
				  "structure:unchanged/attr:descendantChanged: root [1:10]"
				, "structure:unchanged/attr:descendantChanged: root/config [2:9]"
				, "structure:unchanged/attr:descendantChanged: root/config/sensor:com.dynatrace.diagnostics.knowledgesensor.dotnet.logging [3:8]"
				, "structure:unchanged/attr:changed: root/config/sensor:com.dynatrace.diagnostics.knowledgesensor.dotnet.logging/property [4:4]"
				, "structure:unchanged/attr:changed: root/config/sensor:com.dynatrace.diagnostics.knowledgesensor.dotnet.logging/property [5:5]"
				, "structure:unchanged/attr:changed: root/config/sensor:com.dynatrace.diagnostics.knowledgesensor.dotnet.logging/property [6:6]"
				, "structure:unchanged/attr:unchanged: root/config/sensor:com.dynatrace.diagnostics.knowledgesensor.dotnet.logging/property [7:7]"
				), toStateStrings(xmlRight.elements));
		
		checkList(Arrays.asList(
				  "structure:unchanged/attr:descendantChanged: root [1:10]"
				, "structure:unchanged/attr:descendantChanged: root/config [2:9]"
				, "structure:unchanged/attr:descendantChanged: root/config/sensor:com.dynatrace.diagnostics.knowledgesensor.dotnet.logging [3:8]"
				, "structure:unchanged/attr:changed: root/config/sensor:com.dynatrace.diagnostics.knowledgesensor.dotnet.logging/property [4:4]"
				, "structure:unchanged/attr:changed: root/config/sensor:com.dynatrace.diagnostics.knowledgesensor.dotnet.logging/property [5:5]"
				, "structure:unchanged/attr:changed: root/config/sensor:com.dynatrace.diagnostics.knowledgesensor.dotnet.logging/property [6:6]"
				, "structure:unchanged/attr:unchanged: root/config/sensor:com.dynatrace.diagnostics.knowledgesensor.dotnet.logging/property [7:7]"
				), toStateStrings(xmlLeft.elements));
		
		differ = xmlDifferFactory.create(Arrays.asList("sensor/property:ignored"));
		diffResult = differ.diff(xmlLeft, xmlRight);
		basicChecks(xmlLeft, xmlRight, diffResult);
		Assert.assertEquals(0, diffResult.nAdded);
		Assert.assertEquals(0, diffResult.nRemoved);
		Assert.assertEquals(3, diffResult.nAttributeChanged);
		
		checkList(Arrays.asList(
				"structure:unchanged/attr:descendantChanged: root [1:10]"
				, "structure:unchanged/attr:descendantChanged: root/config [2:9]"
				, "structure:unchanged/attr:descendantChanged: root/config/sensor:com.dynatrace.diagnostics.knowledgesensor.dotnet.logging [3:8]"
				, "structure:unchanged/attr:changed: root/config/sensor:com.dynatrace.diagnostics.knowledgesensor.dotnet.logging/property [4:4]"
				, "structure:unchanged/attr:changed: root/config/sensor:com.dynatrace.diagnostics.knowledgesensor.dotnet.logging/property [5:5]"
				, "structure:unchanged/attr:changed: root/config/sensor:com.dynatrace.diagnostics.knowledgesensor.dotnet.logging/property [6:6]"
				, "structure:unchanged/attr:unchanged: root/config/sensor:com.dynatrace.diagnostics.knowledgesensor.dotnet.logging/property [7:7]"
				), toStateStrings(xmlRight.elements));
		
		checkList(Arrays.asList(
				"structure:unchanged/attr:descendantChanged: root [1:10]"
				, "structure:unchanged/attr:descendantChanged: root/config [2:9]"
				, "structure:unchanged/attr:descendantChanged: root/config/sensor:com.dynatrace.diagnostics.knowledgesensor.dotnet.logging [3:8]"
				, "structure:unchanged/attr:changed: root/config/sensor:com.dynatrace.diagnostics.knowledgesensor.dotnet.logging/property [4:4]"
				, "structure:unchanged/attr:changed: root/config/sensor:com.dynatrace.diagnostics.knowledgesensor.dotnet.logging/property [5:5]"
				, "structure:unchanged/attr:changed: root/config/sensor:com.dynatrace.diagnostics.knowledgesensor.dotnet.logging/property [6:6]"
				, "structure:unchanged/attr:unchanged: root/config/sensor:com.dynatrace.diagnostics.knowledgesensor.dotnet.logging/property [7:7]"
				), toStateStrings(xmlLeft.elements));
		
		differ = xmlDifferFactory.create(Arrays.asList("/sensor/property:ignored")); // wrong path
		diffResult = differ.diff(xmlLeft, xmlRight);
		basicChecks(xmlLeft, xmlRight, diffResult);
		Assert.assertEquals(0, diffResult.nAdded);
		Assert.assertEquals(0, diffResult.nRemoved);
		Assert.assertEquals(4, diffResult.nAttributeChanged);
		
		checkList(Arrays.asList(
				  "structure:unchanged/attr:descendantChanged: root [1:10]"
				, "structure:unchanged/attr:descendantChanged: root/config [2:9]"
				, "structure:unchanged/attr:descendantChanged: root/config/sensor:com.dynatrace.diagnostics.knowledgesensor.dotnet.logging [3:8]"
				, "structure:unchanged/attr:changed: root/config/sensor:com.dynatrace.diagnostics.knowledgesensor.dotnet.logging/property [4:4]"
				, "structure:unchanged/attr:changed: root/config/sensor:com.dynatrace.diagnostics.knowledgesensor.dotnet.logging/property [5:5]"
				, "structure:unchanged/attr:changed: root/config/sensor:com.dynatrace.diagnostics.knowledgesensor.dotnet.logging/property [6:6]"
				, "structure:unchanged/attr:changed: root/config/sensor:com.dynatrace.diagnostics.knowledgesensor.dotnet.logging/property [7:7]"
				), toStateStrings(xmlRight.elements));
		
		checkList(Arrays.asList(
				  "structure:unchanged/attr:descendantChanged: root [1:10]"
				, "structure:unchanged/attr:descendantChanged: root/config [2:9]"
				, "structure:unchanged/attr:descendantChanged: root/config/sensor:com.dynatrace.diagnostics.knowledgesensor.dotnet.logging [3:8]"
				, "structure:unchanged/attr:changed: root/config/sensor:com.dynatrace.diagnostics.knowledgesensor.dotnet.logging/property [4:4]"
				, "structure:unchanged/attr:changed: root/config/sensor:com.dynatrace.diagnostics.knowledgesensor.dotnet.logging/property [5:5]"
				, "structure:unchanged/attr:changed: root/config/sensor:com.dynatrace.diagnostics.knowledgesensor.dotnet.logging/property [6:6]"
				, "structure:unchanged/attr:changed: root/config/sensor:com.dynatrace.diagnostics.knowledgesensor.dotnet.logging/property [7:7]"
				), toStateStrings(xmlLeft.elements));
		
		differ = xmlDifferFactory.create(Arrays.asList("root/config/sensor/propertyX:ignored")); // wrong path
		diffResult = differ.diff(xmlLeft, xmlRight);
		basicChecks(xmlLeft, xmlRight, diffResult);
		Assert.assertEquals(0, diffResult.nAdded);
		Assert.assertEquals(0, diffResult.nRemoved);
		Assert.assertEquals(4, diffResult.nAttributeChanged);
		
		checkList(Arrays.asList(
				"structure:unchanged/attr:descendantChanged: root [1:10]"
				, "structure:unchanged/attr:descendantChanged: root/config [2:9]"
				, "structure:unchanged/attr:descendantChanged: root/config/sensor:com.dynatrace.diagnostics.knowledgesensor.dotnet.logging [3:8]"
				, "structure:unchanged/attr:changed: root/config/sensor:com.dynatrace.diagnostics.knowledgesensor.dotnet.logging/property [4:4]"
				, "structure:unchanged/attr:changed: root/config/sensor:com.dynatrace.diagnostics.knowledgesensor.dotnet.logging/property [5:5]"
				, "structure:unchanged/attr:changed: root/config/sensor:com.dynatrace.diagnostics.knowledgesensor.dotnet.logging/property [6:6]"
				, "structure:unchanged/attr:changed: root/config/sensor:com.dynatrace.diagnostics.knowledgesensor.dotnet.logging/property [7:7]"
				), toStateStrings(xmlRight.elements));
		
		checkList(Arrays.asList(
				"structure:unchanged/attr:descendantChanged: root [1:10]"
				, "structure:unchanged/attr:descendantChanged: root/config [2:9]"
				, "structure:unchanged/attr:descendantChanged: root/config/sensor:com.dynatrace.diagnostics.knowledgesensor.dotnet.logging [3:8]"
				, "structure:unchanged/attr:changed: root/config/sensor:com.dynatrace.diagnostics.knowledgesensor.dotnet.logging/property [4:4]"
				, "structure:unchanged/attr:changed: root/config/sensor:com.dynatrace.diagnostics.knowledgesensor.dotnet.logging/property [5:5]"
				, "structure:unchanged/attr:changed: root/config/sensor:com.dynatrace.diagnostics.knowledgesensor.dotnet.logging/property [6:6]"
				, "structure:unchanged/attr:changed: root/config/sensor:com.dynatrace.diagnostics.knowledgesensor.dotnet.logging/property [7:7]"
				), toStateStrings(xmlLeft.elements));
	}
	
	@Test
	public void attrAndDescendandInsertedSameAttr() throws XMLStreamException, IOException {
		String[] inputLeft = {
				"<root>"
				, "  <config oldattr='oldattr'>"
				, "  </config>"
				, "</root>"
		};
		String[] inputRight = {
				"<root>"
				, "  <config oldattr='oldattrchanged'>"
				, "    <newitem/>"
				, "  </config>"
				, "</root>"
		};
		
		List<String> discriminatingAttributes = Arrays.asList("id");
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		XmlStruct xmlLeft = lexer.parse(IO.asString(inputLeft));
		XmlStruct xmlRight = lexer.parse(IO.asString(inputRight));
		
		XmlDiffer differ = xmlDifferFactory.create(Arrays.asList());
		XmlDiffer.Result diffResult = differ.diff(xmlLeft, xmlRight);
		basicChecks(xmlLeft, xmlRight, diffResult);
		Assert.assertEquals(1, diffResult.nAdded);
		Assert.assertEquals(0, diffResult.nRemoved);
		Assert.assertEquals(1, diffResult.nAttributeChanged);
		
		checkList(Arrays.asList(
				  "structure:descendantChanged/attr:descendantChanged: root [1:5]"
				, "structure:descendantChanged/attr:changed: root/config [2:4]"
				, "structure:changed/attr:unchanged: root/config/newitem [3:3]"
				), toStateStrings(xmlRight.elements));
		
		checkList(Arrays.asList(
				  "structure:descendantChanged/attr:descendantChanged: root [1:4]"
				, "structure:descendantChanged/attr:changed: root/config [2:3]"
				), toStateStrings(xmlLeft.elements));
	}
	
	@Test
	public void attrAndDescendandInsertedAttrAdded() throws XMLStreamException, IOException {
		String[] inputLeft = {
				"<root>"
				, "  <config>"
				, "  </config>"
				, "</root>"
		};
		String[] inputRight = {
				"<root>"
				, "  <config newattr='oldattrchanged'>"
				, "    <newitem/>"
				, "  </config>"
				, "</root>"
		};
		
		List<String> discriminatingAttributes = Arrays.asList("id");
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		XmlStruct xmlLeft = lexer.parse(IO.asString(inputLeft));
		XmlStruct xmlRight = lexer.parse(IO.asString(inputRight));
		
		XmlDiffer differ = xmlDifferFactory.create(Arrays.asList());
		XmlDiffer.Result diffResult = differ.diff(xmlLeft, xmlRight);
		basicChecks(xmlLeft, xmlRight, diffResult);
		Assert.assertEquals(1, diffResult.nAdded);
		Assert.assertEquals(0, diffResult.nRemoved);
		Assert.assertEquals(1, diffResult.nAttributeChanged);
		
		checkList(Arrays.asList(
				  "structure:descendantChanged/attr:descendantChanged: root [1:5]"
				, "structure:descendantChanged/attr:changed: root/config [2:4]"
				, "structure:changed/attr:unchanged: root/config/newitem [3:3]"
				), toStateStrings(xmlRight.elements));
		
		checkList(Arrays.asList(
				  "structure:descendantChanged/attr:descendantChanged: root [1:4]"
				, "structure:descendantChanged/attr:changed: root/config [2:3]"
				), toStateStrings(xmlLeft.elements));
	}
	
	@Test
	public void attrAndDescendandInsertedAttrRemoved() throws XMLStreamException, IOException {
		String[] inputLeft = {
				"<root>"
				, "  <config oldattr='oldattr'>"
				, "  </config>"
				, "</root>"
		};
		String[] inputRight = {
				"<root>"
				, "  <config>"
				, "    <newitem/>"
				, "  </config>"
				, "</root>"
		};
		
		List<String> discriminatingAttributes = Arrays.asList("id");
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		XmlStruct xmlLeft = lexer.parse(IO.asString(inputLeft));
		XmlStruct xmlRight = lexer.parse(IO.asString(inputRight));
		
		XmlDiffer differ = xmlDifferFactory.create(Arrays.asList());
		XmlDiffer.Result diffResult = differ.diff(xmlLeft, xmlRight);
		basicChecks(xmlLeft, xmlRight, diffResult);
		Assert.assertEquals(1, diffResult.nAdded);
		Assert.assertEquals(0, diffResult.nRemoved);
		Assert.assertEquals(1, diffResult.nAttributeChanged);
		
		checkList(Arrays.asList(
				  "structure:descendantChanged/attr:descendantChanged: root [1:5]"
				, "structure:descendantChanged/attr:changed: root/config [2:4]"
				, "structure:changed/attr:unchanged: root/config/newitem [3:3]"
				), toStateStrings(xmlRight.elements));
		
		checkList(Arrays.asList(
				  "structure:descendantChanged/attr:descendantChanged: root [1:4]"
				, "structure:descendantChanged/attr:changed: root/config [2:3]"
				), toStateStrings(xmlLeft.elements));
	}
	
	@Test
	public void conflictsWithAmbiguousParentsSame() throws XMLStreamException, IOException {
		String[] inputLeft = {
				  "    <sensorgroups>"
				, "      <sensorgroup>"
				, "        <sensor key='sensorOne'/>"
				, "      </sensorgroup>"
				, "      <sensorgroup>"
				, "        <sensor key='sensorTwo'/>"
				, "      </sensorgroup>"
				, "      <sensorgroup>"
				, "        <sensor key='sensorThree'/>"
				, "      </sensorgroup>"
				, "      <sensorgroup>"
				, "        <sensor key='sensorFour'/>"
				, "      </sensorgroup>"
				, "    </sensorgroups>"
		};
		String[] inputRight = {
				  "    <sensorgroups>"
				, "      <sensorgroup>"
				, "        <sensor key='sensorOne'/>"
				, "      </sensorgroup>"
				, "      <sensorgroup>"
				, "        <sensor key='sensorTwo'/>"
				, "      </sensorgroup>"
				, "      <sensorgroup>"
				, "        <sensor key='sensorThree'/>"
				, "      </sensorgroup>"
				, "      <sensorgroup>"
				, "        <sensor key='sensorFour'/>"
				, "      </sensorgroup>"
				, "    </sensorgroups>"
		};
		
		List<String> discriminatingAttributes = Arrays.asList("key");
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		XmlStruct xmlLeft = lexer.parse(IO.asString(inputLeft));
		XmlStruct xmlRight = lexer.parse(IO.asString(inputRight));
		
		XmlDiffer differ = xmlDifferFactory.create(ignoreAttributeNames);
		XmlDiffer.Result diffResult = differ.diff(xmlLeft, xmlRight);
		basicChecks(xmlLeft, xmlRight, diffResult);
		Assert.assertEquals(0, diffResult.nAdded);
		Assert.assertEquals(0, diffResult.nRemoved);
		Assert.assertEquals(0, diffResult.nAttributeChanged);
		
		checkList(Arrays.asList(
				  "structure:unchanged/attr:unchanged: sensorgroups [1:14]"
				, "structure:unchanged/attr:unchanged: sensorgroups/sensorgroup [2:4]"
				, "structure:unchanged/attr:unchanged: sensorgroups/sensorgroup/sensor:sensorOne [3:3]"
				, "structure:unchanged/attr:unchanged: sensorgroups/sensorgroup [5:7]"
				, "structure:unchanged/attr:unchanged: sensorgroups/sensorgroup/sensor:sensorTwo [6:6]"
				, "structure:unchanged/attr:unchanged: sensorgroups/sensorgroup [8:10]"
				, "structure:unchanged/attr:unchanged: sensorgroups/sensorgroup/sensor:sensorThree [9:9]"
				, "structure:unchanged/attr:unchanged: sensorgroups/sensorgroup [11:13]"
				, "structure:unchanged/attr:unchanged: sensorgroups/sensorgroup/sensor:sensorFour [12:12]"
				), toStateStrings(xmlRight.elements));
		
		checkList(Arrays.asList(
				  "structure:unchanged/attr:unchanged: sensorgroups [1:14]"
				, "structure:unchanged/attr:unchanged: sensorgroups/sensorgroup [2:4]"
				, "structure:unchanged/attr:unchanged: sensorgroups/sensorgroup/sensor:sensorOne [3:3]"
				, "structure:unchanged/attr:unchanged: sensorgroups/sensorgroup [5:7]"
				, "structure:unchanged/attr:unchanged: sensorgroups/sensorgroup/sensor:sensorTwo [6:6]"
				, "structure:unchanged/attr:unchanged: sensorgroups/sensorgroup [8:10]"
				, "structure:unchanged/attr:unchanged: sensorgroups/sensorgroup/sensor:sensorThree [9:9]"
				, "structure:unchanged/attr:unchanged: sensorgroups/sensorgroup [11:13]"
				, "structure:unchanged/attr:unchanged: sensorgroups/sensorgroup/sensor:sensorFour [12:12]"
				), toStateStrings(xmlLeft.elements));
	}
	
	@Test
	public void conflictsWithAmbiguousParentsDifferentGood() throws XMLStreamException, IOException {
		String[] inputLeft = {
				"    <sensorgroups>"
				, "      <sensorgroup>"
				, "        <sensor key='sensorOne'/>"
				, "      </sensorgroup>"
				, "      <sensorgroup>"
				, "        <sensor key='sensorTwo'/>"
				, "      </sensorgroup>"
				, "      <sensorgroup>"
				, "        <sensor key='sensorThree'/>"
				, "      </sensorgroup>"
				, "      <sensorgroup>"
				, "        <sensor key='sensorFour'/>"
				, "      </sensorgroup>"
				, "    </sensorgroups>"
		};
		String[] inputRight = {
				"    <sensorgroups>"
				, "      <sensorgroup>"
				, "        <sensor key='MyNewSensorOne'/>" // <- replaces 'sensorOne'
				, "      </sensorgroup>"
				, "      <sensorgroup>"
				, "        <sensor key='sensorTwo'/>"
				, "      </sensorgroup>"
				, "      <sensorgroup>"
				, "        <sensor key='sensorThree'/>"
				, "      </sensorgroup>"
				, "      <sensorgroup>"
				, "        <sensor key='sensorFour'/>"
				, "      </sensorgroup>"
				, "    </sensorgroups>"
		};
		
		List<String> discriminatingAttributes = Arrays.asList("key");
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		XmlStruct xmlLeft = lexer.parse(IO.asString(inputLeft));
		XmlStruct xmlRight = lexer.parse(IO.asString(inputRight));
		
		XmlDiffer differ = xmlDifferFactory.create(ignoreAttributeNames);
		XmlDiffer.Result diffResult = differ.diff(xmlLeft, xmlRight);
		basicChecks(xmlLeft, xmlRight, diffResult);
		Assert.assertEquals(1, diffResult.nAdded);
		Assert.assertEquals(1, diffResult.nRemoved);
		Assert.assertEquals(0, diffResult.nAttributeChanged);
		
		checkList(Arrays.asList(
				  "structure:descendantChanged/attr:unchanged: sensorgroups [1:14]"
				, "structure:changed/attr:unchanged: sensorgroups/sensorgroup [2:4]"
				, "structure:parentChanged/attr:unchanged: sensorgroups/sensorgroup/sensor:MyNewSensorOne [3:3]"
				, "structure:unchanged/attr:unchanged: sensorgroups/sensorgroup [5:7]"
				, "structure:unchanged/attr:unchanged: sensorgroups/sensorgroup/sensor:sensorTwo [6:6]"
				, "structure:unchanged/attr:unchanged: sensorgroups/sensorgroup [8:10]"
				, "structure:unchanged/attr:unchanged: sensorgroups/sensorgroup/sensor:sensorThree [9:9]"
				, "structure:unchanged/attr:unchanged: sensorgroups/sensorgroup [11:13]"
				, "structure:unchanged/attr:unchanged: sensorgroups/sensorgroup/sensor:sensorFour [12:12]"
				), toStateStrings(xmlRight.elements));
		
		checkList(Arrays.asList(
				  "structure:descendantChanged/attr:unchanged: sensorgroups [1:14]"
				, "structure:changed/attr:unchanged: sensorgroups/sensorgroup [2:4]"
				, "structure:parentChanged/attr:unchanged: sensorgroups/sensorgroup/sensor:sensorOne [3:3]"
				, "structure:unchanged/attr:unchanged: sensorgroups/sensorgroup [5:7]"
				, "structure:unchanged/attr:unchanged: sensorgroups/sensorgroup/sensor:sensorTwo [6:6]"
				, "structure:unchanged/attr:unchanged: sensorgroups/sensorgroup [8:10]"
				, "structure:unchanged/attr:unchanged: sensorgroups/sensorgroup/sensor:sensorThree [9:9]"
				, "structure:unchanged/attr:unchanged: sensorgroups/sensorgroup [11:13]"
				, "structure:unchanged/attr:unchanged: sensorgroups/sensorgroup/sensor:sensorFour [12:12]"
				), toStateStrings(xmlLeft.elements));
	}
	
	@Test
	public void conflictsWithAmbiguousParentsDifferentEvil() throws XMLStreamException, IOException {
		String[] inputLeft = {
				"    <sensorgroups>"
				, "      <sensorgroup>"
				, "        <sensor key='sensorOne'/>"
				, "      </sensorgroup>"
				, "      <sensorgroup>"
				, "        <sensor key='sensorTwo'/>"
				, "      </sensorgroup>"
				, "      <sensorgroup>"
				, "        <sensor key='sensorThree'/>"
				, "      </sensorgroup>"
				, "      <sensorgroup>"
				, "        <sensor key='sensorFour'/>"
				, "      </sensorgroup>"
				, "    </sensorgroups>"
		};
		String[] inputRight = {
				"    <sensorgroups>"
				, "      <sensorgroup>"
				, "        <sensor key='sensorOne'/>"
				, "        <sensor key='NewSensorInsideSameGroup'/>" // <- modifies the compound key of <sensorgroup>
				, "      </sensorgroup>"
				, "      <sensorgroup>"
				, "        <sensor key='sensorTwo'/>"
				, "      </sensorgroup>"
				, "      <sensorgroup>"
				, "        <sensor key='sensorThree'/>"
				, "      </sensorgroup>"
				, "      <sensorgroup>"
				, "        <sensor key='sensorFour'/>"
				, "      </sensorgroup>"
				, "    </sensorgroups>"
		};
		
		List<String> discriminatingAttributes = Arrays.asList("key");
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		XmlStruct xmlLeft = lexer.parse(IO.asString(inputLeft));
		XmlStruct xmlRight = lexer.parse(IO.asString(inputRight));
		
		XmlDiffer differ = xmlDifferFactory.create(ignoreAttributeNames);
		XmlDiffer.Result diffResult = differ.diff(xmlLeft, xmlRight);
		basicChecks(xmlLeft, xmlRight, diffResult);
		Assert.assertEquals(1, diffResult.nAdded);
		Assert.assertEquals(1, diffResult.nRemoved);
		Assert.assertEquals(0, diffResult.nAttributeChanged);
		
		if (isDifferImplStringMetric(differ)) { //FIXME StringMetricXmlDiffer looks correct
			checkList(Arrays.asList(
					  "structure:descendantChanged/attr:unchanged: sensorgroups [1:15]"
					, "structure:changed/attr:unchanged: sensorgroups/sensorgroup [2:5]"
					, "structure:parentChanged/attr:unchanged: sensorgroups/sensorgroup/sensor:sensorOne [3:3]"
					, "structure:parentChanged/attr:unchanged: sensorgroups/sensorgroup/sensor:NewSensorInsideSameGroup [4:4]"
					, "structure:unchanged/attr:unchanged: sensorgroups/sensorgroup [6:8]"
					, "structure:unchanged/attr:unchanged: sensorgroups/sensorgroup/sensor:sensorTwo [7:7]"
					, "structure:unchanged/attr:unchanged: sensorgroups/sensorgroup [9:11]"
					, "structure:unchanged/attr:unchanged: sensorgroups/sensorgroup/sensor:sensorThree [10:10]"
					, "structure:unchanged/attr:unchanged: sensorgroups/sensorgroup [12:14]"
					, "structure:unchanged/attr:unchanged: sensorgroups/sensorgroup/sensor:sensorFour [13:13]"
					), toStateStrings(xmlRight.elements));
			
			checkList(Arrays.asList(
					  "structure:descendantChanged/attr:unchanged: sensorgroups [1:14]"
					, "structure:changed/attr:unchanged: sensorgroups/sensorgroup [2:4]"
					, "structure:parentChanged/attr:unchanged: sensorgroups/sensorgroup/sensor:sensorOne [3:3]"
					, "structure:unchanged/attr:unchanged: sensorgroups/sensorgroup [5:7]"
					, "structure:unchanged/attr:unchanged: sensorgroups/sensorgroup/sensor:sensorTwo [6:6]"
					, "structure:unchanged/attr:unchanged: sensorgroups/sensorgroup [8:10]"
					, "structure:unchanged/attr:unchanged: sensorgroups/sensorgroup/sensor:sensorThree [9:9]"
					, "structure:unchanged/attr:unchanged: sensorgroups/sensorgroup [11:13]"
					, "structure:unchanged/attr:unchanged: sensorgroups/sensorgroup/sensor:sensorFour [12:12]"
					), toStateStrings(xmlLeft.elements));
		} else {
			checkList(Arrays.asList(
					  "structure:descendantChanged/attr:unchanged: sensorgroups [1:15]"
					, "structure:changed/attr:unchanged: sensorgroups/sensorgroup [2:5]"
					, "structure:unchanged/attr:unchanged: sensorgroups/sensorgroup/sensor:sensorOne [3:3]"
					, "structure:parentChanged/attr:unchanged: sensorgroups/sensorgroup/sensor:NewSensorInsideSameGroup [4:4]"
					, "structure:unchanged/attr:unchanged: sensorgroups/sensorgroup [6:8]"
					, "structure:unchanged/attr:unchanged: sensorgroups/sensorgroup/sensor:sensorTwo [7:7]"
					, "structure:unchanged/attr:unchanged: sensorgroups/sensorgroup [9:11]"
					, "structure:unchanged/attr:unchanged: sensorgroups/sensorgroup/sensor:sensorThree [10:10]"
					, "structure:unchanged/attr:unchanged: sensorgroups/sensorgroup [12:14]"
					, "structure:unchanged/attr:unchanged: sensorgroups/sensorgroup/sensor:sensorFour [13:13]"
					), toStateStrings(xmlRight.elements));
			
			checkList(Arrays.asList(
					  "structure:descendantChanged/attr:unchanged: sensorgroups [1:14]"
					, "structure:changed/attr:unchanged: sensorgroups/sensorgroup [2:4]"
					, "structure:unchanged/attr:unchanged: sensorgroups/sensorgroup/sensor:sensorOne [3:3]"
					, "structure:unchanged/attr:unchanged: sensorgroups/sensorgroup [5:7]"
					, "structure:unchanged/attr:unchanged: sensorgroups/sensorgroup/sensor:sensorTwo [6:6]"
					, "structure:unchanged/attr:unchanged: sensorgroups/sensorgroup [8:10]"
					, "structure:unchanged/attr:unchanged: sensorgroups/sensorgroup/sensor:sensorThree [9:9]"
					, "structure:unchanged/attr:unchanged: sensorgroups/sensorgroup [11:13]"
					, "structure:unchanged/attr:unchanged: sensorgroups/sensorgroup/sensor:sensorFour [12:12]"
					), toStateStrings(xmlLeft.elements));
		}
	}
	
	@Test
	public void regression1() throws XMLStreamException, IOException {
		String[] inputLeft = {
		          "<sensor featurehash=\"\" order=\"0\" description=\"App\" hint=\"Default Sensor Group for Java Method Sensor Rules\" jssensorttype=\"\" group=\"Methods\" type=\"java\" autoplace=\"true\" userdefined=\"false\" defaultorder=\"0\" key=\"com.dynatrace.diagnostics.sensorgroup.method.Java.3a95b5672fe\">"
		        , "  <class techtype=\"java\" pattern=\"my.class.for.Sensor\" match=\"equals\" delegationsuppression=\"false\" placed=\"true\">"
		        , "    <method capturereturn=\"false\" visibility=\"publicType\" overrideable=\"true\" argsspecified=\"false\" deepobjectaccessor=\"\" inheritance=\"auto\" pattern=\"\" onlyinstrumentifsynchappens=\"false\" capture=\"active\" api=\"&lt;api unknown&gt;\" synctime=\"true\" match=\"equals\" placed=\"include\" />"
		        , "  </class>"
		        , "</sensor>"
		};
		String[] inputRight = {
				  "<sensor featurehash=\"\" order=\"0\" description=\"App\" hint=\"Default Sensor Group for Java Method Sensor Rules\" jssensorttype=\"\" group=\"Methods\" type=\"java\" autoplace=\"true\" userdefined=\"false\" defaultorder=\"0\" key=\"com.dynatrace.diagnostics.sensorgroup.method.Java.3a95b5672fe\">"
		        , "  <class techtype=\"java\" pattern=\"my.class.for.Sensor\" match=\"equals\" delegationsuppression=\"false\" placed=\"true\">"
		        , "    <method capturereturn=\"false\" visibility=\"publicType\" overrideable=\"true\" argsspecified=\"false\" deepobjectaccessor=\"\" inheritance=\"auto\" pattern=\"\" onlyinstrumentifsynchappens=\"false\" capture=\"active\" api=\"&lt;api unknown&gt;\" synctime=\"true\" match=\"equals\" placed=\"include\" />"
		        , "  </class>"
		        , "  <class techtype=\"java\" pattern=\"another.sensor.rule.class\" match=\"equals\" delegationsuppression=\"false\" placed=\"true\">"
		        , "    <method capturereturn=\"false\" visibility=\"publicType\" overrideable=\"true\" argsspecified=\"false\" deepobjectaccessor=\"\" inheritance=\"auto\" pattern=\"\" onlyinstrumentifsynchappens=\"false\" capture=\"active\" api=\"&lt;api unknown&gt;\" synctime=\"true\" match=\"equals\" placed=\"include\" />"
		        , "  </class>"
		        , "  <class techtype=\"java\" pattern=\"thirdRule.sensor.rule.class\" match=\"equals\" delegationsuppression=\"false\" placed=\"true\">"
		        , "    <method capturereturn=\"false\" visibility=\"publicType\" overrideable=\"true\" argsspecified=\"false\" deepobjectaccessor=\"\" inheritance=\"auto\" pattern=\"\" onlyinstrumentifsynchappens=\"false\" capture=\"active\" api=\"&lt;api unknown&gt;\" synctime=\"true\" match=\"equals\" placed=\"include\" />"
		        , "  </class>"
		        , "</sensor>"
		      };
		
		List<String> discriminatingAttributes = (Arrays.asList("b", "d", "e"));
		
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		XmlStruct xmlLeft = lexer.parse(IO.asString(inputLeft));
		XmlStruct xmlRight = lexer.parse(IO.asString(inputRight));
		
		XmlDiffer differ = xmlDifferFactory.create(ignoreAttributeNames);
		XmlDiffer.Result diffResult = differ.diff(xmlLeft, xmlRight);
		basicChecks(xmlLeft, xmlRight, diffResult);
		Assert.assertEquals(2, diffResult.nAdded);
		Assert.assertEquals(0, diffResult.nRemoved);
		Assert.assertEquals(0, diffResult.nAttributeChanged);

		checkList(Arrays.asList(
				  "structure:descendantChanged/attr:unchanged: sensor [1:11]"
				, "structure:unchanged/attr:unchanged: sensor/class [2:4]"
				, "structure:unchanged/attr:unchanged: sensor/class/method [3:3]"
				, "structure:changed/attr:unchanged: sensor/class [5:7]"
				, "structure:parentChanged/attr:unchanged: sensor/class/method [6:6]"
				, "structure:changed/attr:unchanged: sensor/class [8:10]"
				, "structure:parentChanged/attr:unchanged: sensor/class/method [9:9]"
				), toStateStrings(xmlRight.elements));
		
		checkList(Arrays.asList(
				  "structure:descendantChanged/attr:unchanged: sensor [1:5]"
				, "structure:unchanged/attr:unchanged: sensor/class [2:4]"
				, "structure:unchanged/attr:unchanged: sensor/class/method [3:3]"
				), toStateStrings(xmlLeft.elements));
	}
	
	@Test
	public void attributes2() throws XMLStreamException, IOException {
		String[] inputLeft = {
				"<root attr='y' oldattr='o'>"
				,"  <element attr='x' />"
				,"  <element attr='y' />"
				,"  <element attr='z' >"
				,"    <child a='b'/>"
				,"    <child c='d'/>"
				,"    <child c='d'>"
				,"      <grandchild/>"
				,"    </child>"
				,"  </element>"
				,"</root>"
		};
		String[] inputRight = {
				"<root attr='z'>"
				,"  <element attr='a' newattr='b'/>"
				,"  <element attr='c' newattr='d'/>"
				,"  <element attr='z' >"
				,"    <child a='x'/>"
				,"    <child c='y'/>"
				,"    <child c='d'>"
				,"      <grandchild attr='attr'/>"
				,"    </child>"
				,"  </element>"
				,"</root>"
		};
		
		List<String> discriminatingAttributes = (Arrays.asList());
		
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		XmlStruct xmlLeft = lexer.parse(IO.asString(inputLeft));
		XmlStruct xmlRight = lexer.parse(IO.asString(inputRight));
		
		
		XmlDiffer differ = xmlDifferFactory.create(ignoreAttributeNames);
		XmlDiffer.Result diffResult = differ.diff(xmlLeft, xmlRight);
		basicChecks(xmlLeft, xmlRight, diffResult);
		Assert.assertEquals(0, diffResult.nAdded);
		Assert.assertEquals(0, diffResult.nRemoved);
		Assert.assertEquals(6, diffResult.nAttributeChanged);

		checkList(Arrays.asList(
				  "structure:unchanged/attr:changed: root [1:11]"
				, "structure:unchanged/attr:changed: root/element [2:2]"
				, "structure:unchanged/attr:changed: root/element [3:3]"
				, "structure:unchanged/attr:descendantChanged: root/element [4:10]"
				, "structure:unchanged/attr:changed: root/element/child [5:5]"
				, "structure:unchanged/attr:changed: root/element/child [6:6]"
				, "structure:unchanged/attr:descendantChanged: root/element/child [7:9]"
				, "structure:unchanged/attr:changed: root/element/child/grandchild [8:8]"
				), toStateStrings(xmlRight.elements));
		
		checkList(Arrays.asList(
				  "structure:unchanged/attr:changed: root [1:11]"
				, "structure:unchanged/attr:changed: root/element [2:2]"
				, "structure:unchanged/attr:changed: root/element [3:3]"
				, "structure:unchanged/attr:descendantChanged: root/element [4:10]"
				, "structure:unchanged/attr:changed: root/element/child [5:5]"
				, "structure:unchanged/attr:changed: root/element/child [6:6]"
				, "structure:unchanged/attr:descendantChanged: root/element/child [7:9]"
				, "structure:unchanged/attr:changed: root/element/child/grandchild [8:8]"
				), toStateStrings(xmlLeft.elements));
	}
	
	@Test
	public void attributesAndStructureChanged() throws XMLStreamException, IOException {
		String[] inputLeft = {
				"<root attr='left0' oldattr='left1'>"
				,"  <element attr='left2' />"
				,"  <element attr='left3' />"
				,"  <element attr='left4' >"
				,"    <child a='left5'/>"
				,"    <child c='left6'/>"
				,"    <child c='left7'>"
				,"      <grandchild/>"
				,"    </child>"
				,"  </element>"
				,"  <deletedelement/>"
				,"</root>"
		};
		String[] inputRight = {
				"<root attr='right0'>"
				,"  <element attr='right1' newattr='right2'/>"
				,"  <element attr='right3' newattr='right4'/>"
				,"  <newelement/>"
				,"  <element attr='right5' >"
				,"    <child a='right6'/>"
				,"    <child c='right7'/>"
				,"    <child c='right8'>"
				,"      <grandchild attr='right9'/>"
				,"    </child>"
				,"  </element>"
				,"</root>"
		};
		
		List<String> discriminatingAttributes = (Arrays.asList());
		
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		XmlStruct xmlLeft = lexer.parse(IO.asString(inputLeft));
		XmlStruct xmlRight = lexer.parse(IO.asString(inputRight));
		
		
		XmlDiffer differ = xmlDifferFactory.create(ignoreAttributeNames );
		XmlDiffer.Result diffResult = differ.diff(xmlLeft, xmlRight);
		basicChecks(xmlLeft, xmlRight, diffResult);
		Assert.assertEquals(1, diffResult.nAdded);
		Assert.assertEquals(1, diffResult.nRemoved);
		Assert.assertEquals(8, diffResult.nAttributeChanged);
		
		checkList(Arrays.asList(
				  "structure:descendantChanged/attr:changed: root [1:12]"
				, "structure:unchanged/attr:changed: root/element [2:2]"
				, "structure:unchanged/attr:changed: root/element [3:3]"
				, "structure:changed/attr:unchanged: root/newelement [4:4]"
				, "structure:unchanged/attr:changed: root/element [5:11]"
				, "structure:unchanged/attr:changed: root/element/child [6:6]"
				, "structure:unchanged/attr:changed: root/element/child [7:7]"
				, "structure:unchanged/attr:changed: root/element/child [8:10]"
				, "structure:unchanged/attr:changed: root/element/child/grandchild [9:9]"
				), toStateStrings(xmlRight.elements));
		
		checkList(Arrays.asList(
				  "structure:descendantChanged/attr:changed: root [1:12]"
				, "structure:unchanged/attr:changed: root/element [2:2]"
				, "structure:unchanged/attr:changed: root/element [3:3]"
				, "structure:unchanged/attr:changed: root/element [4:10]"
				, "structure:unchanged/attr:changed: root/element/child [5:5]"
				, "structure:unchanged/attr:changed: root/element/child [6:6]"
				, "structure:unchanged/attr:changed: root/element/child [7:9]"
				, "structure:unchanged/attr:changed: root/element/child/grandchild [8:8]"
				, "structure:changed/attr:unchanged: root/deletedelement [11:11]"
				), toStateStrings(xmlLeft.elements));
	}
	
	@Test
	public void peerDescendantChangeMarkedLeft() throws XMLStreamException, IOException {
		String[] inputLeft = {
				"<root>"
				, "  <config1>"
				, "    <kid />"
				, "  </config1>"
				, "</root>"
		};
		String[] inputRight = {
				"<root>"
				, "  <config1>"
				, "  </config1>"
				, "</root>"
		};
		
		List<String> discriminatingAttributes = Arrays.asList("id");
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		XmlStruct xmlLeft = lexer.parse(IO.asString(inputLeft));
		XmlStruct xmlRight = lexer.parse(IO.asString(inputRight));
		
		XmlDiffer differ = xmlDifferFactory.create(Arrays.asList());
		XmlDiffer.Result diffResult = differ.diff(xmlLeft, xmlRight);
		basicChecks(xmlLeft, xmlRight, diffResult);
		Assert.assertEquals(0, diffResult.nAdded);
		Assert.assertEquals(1, diffResult.nRemoved);
		Assert.assertEquals(0, diffResult.nAttributeChanged);
		
		checkList(Arrays.asList(
				  "structure:descendantChanged/attr:unchanged: root [1:4]"
				, "structure:descendantChanged/attr:unchanged: root/config1 [2:3]"
				), toStateStrings(xmlRight.elements));
		
		checkList(Arrays.asList(
				  "structure:descendantChanged/attr:unchanged: root [1:5]"
				, "structure:descendantChanged/attr:unchanged: root/config1 [2:4]"
				, "structure:changed/attr:unchanged: root/config1/kid [3:3]"
				), toStateStrings(xmlLeft.elements));
	}
	
	@Test
	public void peerDescendantChangeMarkedRight() throws XMLStreamException, IOException {
		String[] inputLeft = {
				"<root>"
				, "  <config1>"
				, "  </config1>"
				, "</root>"
		};
		String[] inputRight = {
				"<root>"
				, "  <config1>"
				, "    <kid />"
				, "  </config1>"
				, "</root>"
		};
		
		List<String> discriminatingAttributes = Arrays.asList("id");
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		XmlStruct xmlLeft = lexer.parse(IO.asString(inputLeft));
		XmlStruct xmlRight = lexer.parse(IO.asString(inputRight));
		
		XmlDiffer differ = xmlDifferFactory.create(Arrays.asList());
		XmlDiffer.Result diffResult = differ.diff(xmlLeft, xmlRight);
		basicChecks(xmlLeft, xmlRight, diffResult);
		Assert.assertEquals(1, diffResult.nAdded);
		Assert.assertEquals(0, diffResult.nRemoved);
		Assert.assertEquals(0, diffResult.nAttributeChanged);
		
		checkList(Arrays.asList(
				  "structure:descendantChanged/attr:unchanged: root [1:5]"
				, "structure:descendantChanged/attr:unchanged: root/config1 [2:4]"
				, "structure:changed/attr:unchanged: root/config1/kid [3:3]"
				), toStateStrings(xmlRight.elements));
		
		checkList(Arrays.asList(
				  "structure:descendantChanged/attr:unchanged: root [1:4]"
				, "structure:descendantChanged/attr:unchanged: root/config1 [2:3]"
				), toStateStrings(xmlLeft.elements));
	}
	
	@Test
	public void differentRootsSameAttr() throws XMLStreamException, IOException {
		String[] inputLeft = {
				"<root>"
				, "  <config1>"
				, "  </config1>"
				, "</root>"
		};
		String[] inputRight = {
				"<root1>"
				, "  <config1>"
				, "  </config1>"
				, "  <config1>"
				, "  </config1>"
				, "  <config1>"
				, "  </config1>"
				, "  <config1>"
				, "  </config1>"
				, "  <config1>"
				, "  </config1>"
				, "</root1>"
		};
		
		List<String> discriminatingAttributes = Arrays.asList("id");
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		XmlStruct xmlLeft = lexer.parse(IO.asString(inputLeft));
		XmlStruct xmlRight = lexer.parse(IO.asString(inputRight));
		
		XmlDiffer differ = xmlDifferFactory.create(Arrays.asList());
		XmlDiffer.Result diffResult = differ.diff(xmlLeft, xmlRight);
		basicChecks(xmlLeft, xmlRight, diffResult);
		Assert.assertEquals(1, diffResult.nAdded);
		Assert.assertEquals(1, diffResult.nRemoved);
		Assert.assertEquals(0, diffResult.nAttributeChanged);
		
		checkList(Arrays.asList(
				  "structure:changed/attr:unchanged: root1 [1:12]"
				, "structure:parentChanged/attr:unchanged: root1/config1 [2:3]"
				, "structure:parentChanged/attr:unchanged: root1/config1 [4:5]"
				, "structure:parentChanged/attr:unchanged: root1/config1 [6:7]"
				, "structure:parentChanged/attr:unchanged: root1/config1 [8:9]"
				, "structure:parentChanged/attr:unchanged: root1/config1 [10:11]"
				), toStateStrings(xmlRight.elements));
		
		checkList(Arrays.asList(
				  "structure:changed/attr:unchanged: root [1:4]"
				, "structure:parentChanged/attr:unchanged: root/config1 [2:3]"
				), toStateStrings(xmlLeft.elements));
	}
	
	@Test
	public void differentRootsDifferentAttr() throws XMLStreamException, IOException {
		String[] inputLeft = {
				"<root a='b'>"
				, "  <config1>"
				, "  </config1>"
				, "</root>"
		};
		String[] inputRight = {
				"<root1 b='c'>"
				, "  <config1>"
				, "  </config1>"
				, "  <config1>"
				, "  </config1>"
				, "  <config1>"
				, "  </config1>"
				, "  <config1>"
				, "  </config1>"
				, "  <config1>"
				, "  </config1>"
				, "</root1>"
		};
		
		List<String> discriminatingAttributes = Arrays.asList("id");
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		XmlStruct xmlLeft = lexer.parse(IO.asString(inputLeft));
		XmlStruct xmlRight = lexer.parse(IO.asString(inputRight));
		
		XmlDiffer differ = xmlDifferFactory.create(Arrays.asList());
		XmlDiffer.Result diffResult = differ.diff(xmlLeft, xmlRight);
		basicChecks(xmlLeft, xmlRight, diffResult);
		Assert.assertEquals(1, diffResult.nAdded);
		Assert.assertEquals(1, diffResult.nRemoved);
		Assert.assertEquals(0, diffResult.nAttributeChanged); // thats correct since the two roots are not peers!!!!
		
		checkList(Arrays.asList(
				"structure:changed/attr:unchanged: root1 [1:12]"
				, "structure:parentChanged/attr:unchanged: root1/config1 [2:3]"
				, "structure:parentChanged/attr:unchanged: root1/config1 [4:5]"
				, "structure:parentChanged/attr:unchanged: root1/config1 [6:7]"
				, "structure:parentChanged/attr:unchanged: root1/config1 [8:9]"
				, "structure:parentChanged/attr:unchanged: root1/config1 [10:11]"
				), toStateStrings(xmlRight.elements));
		
		checkList(Arrays.asList(
				"structure:changed/attr:unchanged: root [1:4]"
				, "structure:parentChanged/attr:unchanged: root/config1 [2:3]"
				), toStateStrings(xmlLeft.elements));
	}
	
	private static void basicChecks(XmlStruct base, XmlStruct mod, XmlDiffer.Result diffResult)  {
		Assert.assertEquals(base.root().nStructureChanged, diffResult.nRemoved);
		Assert.assertEquals(mod.root().nStructureChanged, diffResult.nAdded);
		Assert.assertEquals(mod.root().nAttributeChanged, diffResult.nAttributeChanged);
		
	}
	
	@Test
	public void emptyAttr() throws XMLStreamException, IOException {
		String[] inputLeft = {
				"<root>"
				, "  <config a='b'/>"
				, "  <config a='b' empty=''/>"
				, "  <config a='b' empty=''/>"
				, "  <config a='b' empty='NOTEMPTY'/>"
				, "  <config a='b' empty=''/>"
				, "</root>"
		};
		String[] inputRight = {
				"<root>"
				, "  <config a='b' empty=''/>"
				, "  <config a='b'/>"
				, "  <config a='b' empty='NOTEMPTY'/>"
				, "  <config a='b' empty=''/>"
				, "  <config a='b' empty=''/>"
				, "</root>"
		};
		
		List<String> discriminatingAttributes = Arrays.asList("id");
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		XmlStruct xmlLeft = lexer.parse(IO.asString(inputLeft));
		XmlStruct xmlRight = lexer.parse(IO.asString(inputRight));
		
		XmlDiffer differ = xmlDifferFactory.create(Arrays.asList());
		XmlDiffer.Result diffResult = differ.diff(xmlLeft, xmlRight);
		basicChecks(xmlLeft, xmlRight, diffResult);
		Assert.assertEquals(0, diffResult.nAdded);
		Assert.assertEquals(0, diffResult.nRemoved);
		Assert.assertEquals(2, diffResult.nAttributeChanged);
		
		checkList(Arrays.asList(
				  "structure:unchanged/attr:descendantChanged: root [1:7]"
				, "structure:unchanged/attr:unchanged: root/config [2:2]"
				, "structure:unchanged/attr:unchanged: root/config [3:3]"
				, "structure:unchanged/attr:changed: root/config [4:4]"
				, "structure:unchanged/attr:changed: root/config [5:5]"
				, "structure:unchanged/attr:unchanged: root/config [6:6]"
				), toStateStrings(xmlRight.elements));
		
		checkList(Arrays.asList(
				  "structure:unchanged/attr:descendantChanged: root [1:7]"
				, "structure:unchanged/attr:unchanged: root/config [2:2]"
				, "structure:unchanged/attr:unchanged: root/config [3:3]"
				, "structure:unchanged/attr:changed: root/config [4:4]"
				, "structure:unchanged/attr:changed: root/config [5:5]"
				, "structure:unchanged/attr:unchanged: root/config [6:6]"
				), toStateStrings(xmlLeft.elements));
	}
	
}
