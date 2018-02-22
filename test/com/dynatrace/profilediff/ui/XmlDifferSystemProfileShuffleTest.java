package com.dynatrace.profilediff.ui;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.dynatrace.profilediff.IO;
import com.dynatrace.profilediff.StringMetricXmlDiffer.MetricResolver;
import com.dynatrace.profilediff.StringMetricXmlDifferFactory;
import com.dynatrace.profilediff.XmlDiffer;
import com.dynatrace.profilediff.XmlElement;
import com.dynatrace.profilediff.XmlLexer;
import com.dynatrace.profilediff.XmlStruct;
import com.dynatrace.profilediff.lib.StringMetrics;
import com.dynatrace.profilediff.ui.TwoWayModel.ChangeItem;

@RunWith(Parameterized.class)
public class XmlDifferSystemProfileShuffleTest extends ShuffleTestBase {
	
	public XmlDifferSystemProfileShuffleTest(StringMetricXmlDifferFactory stringMetricXmlDifferFactory, String name, int run) {
		super(stringMetricXmlDifferFactory, name, run);
	}

	@BeforeClass
	public static void beforeClass() {
		resetGlobalMaxDistances();
	}
	
	@AfterClass
	public static void afterClass() {
		expectGlobalMaxDistances("XmlDifferSystemProfileShuffleTest", 26, /*38*/ -1, 1, /*expectedMetricInvokeCount*/ -1);
	}

	private final ChangeItem item = ChangeItem.changes;
	
	XmlLexer lexer;
	XmlDiffer differ;

	private int stringMetricDifferThreshold = -1;
	
	@Before
	public void before() {
		List<String> discriminatorAttributeNames = concatLists(new ArrayList<>(), SYSTEM_PROFILE_DISCRIMINATOR_ATTRIBUTES, LEVENSHTEIN_DISCRIMINATOR_ATTRIBUTES);
		lexer = new XmlLexer(discriminatorAttributeNames, stringCache);
		MetricResolver levenshteinMetricResolver = (XmlElement) -> StringMetrics::getLevenshteinDistance;
		differ = newStringMetricXmlDiffer(SYSTEM_PROFILE_IGNORED_ATTRIBUTES, levenshteinMetricResolver, stringMetricDifferThreshold);
	}
	
	@Test
	public void case1() throws XMLStreamException, IOException {
		String[] inputLeft = IO.readLines(new FileReader("samples/shuffle-test-1.left.profile.xml"));
		String[] inputRight = IO.readLines(new FileReader("samples/shuffle-test-1.right.profile.xml"));
		
		XmlStruct xmlLeft = lexer.parse(IO.asString(inputLeft));
		XmlStruct xmlRight = lexer.parse(IO.asString(inputRight));
		
		XmlDiffer.Result diffResult = differ.diff(xmlLeft, xmlRight);
		Assert.assertEquals(0, diffResult.nAdded);
		Assert.assertEquals(0, diffResult.nRemoved);
		Assert.assertEquals(5, diffResult.nAttributeChanged);
		Assert.assertEquals(3, xmlLeft.depth);
		Assert.assertEquals(3, xmlRight.depth);
		
		List<XmlElement> collected;
		
		collected = checkCollectChangedElements(Arrays.asList(
				  "(#) technology:sdjhjsdfhsdhfsdhfsdjkhfsdjk"
				, "(#) technology:dotnet"
				, "(#) technology:ria"
				, "(#) technology:webserverXY"
				, "(#) technology:php"
				), 5, item, xmlRight);
		
		checkPeers(Arrays.asList(
				  "element (#) technology:sdjhjsdfhsdhfsdhfsdjkhfsdjk"
				, "---peer (#) technology:java"
				, "element (#) technology:dotnet"
				, "---peer (#) technology:dotnet"
				, "element (#) technology:ria"
				, "---peer (#) technology:ria"
				, "element (#) technology:webserverXY"
				, "---peer (#) technology:webserver"
				, "element (#) technology:php"
				, "---peer (#) technology:php"
				), collected);

		collected = checkCollectChangedElements(Arrays.asList(
				  "(#) technology:java"
				, "(#) technology:dotnet"
				, "(#) technology:ria"
				, "(#) technology:webserver"
				, "(#) technology:php"
				), 5, item, xmlLeft);
		
		checkPeers(Arrays.asList(
				  "element (#) technology:java"
				, "---peer (#) technology:sdjhjsdfhsdhfsdhfsdjkhfsdjk"
				, "element (#) technology:dotnet"
				, "---peer (#) technology:dotnet"
				, "element (#) technology:ria"
				, "---peer (#) technology:ria"
				, "element (#) technology:webserver"
				, "---peer (#) technology:webserverXY"
				, "element (#) technology:php"
				, "---peer (#) technology:php"
				), collected);
	}
	
	@Test
	public void case2() throws XMLStreamException, IOException {
		String[] inputLeft = IO.readLines(new FileReader("samples/shuffle-test-2.left.profile.xml"));
		String[] inputRight = IO.readLines(new FileReader("samples/shuffle-test-2.right.profile.xml"));
		
		XmlStruct xmlLeft = lexer.parse(IO.asString(inputLeft));
		XmlStruct xmlRight = lexer.parse(IO.asString(inputRight));
		
		XmlDiffer.Result diffResult = differ.diff(xmlLeft, xmlRight);
		Assert.assertEquals(4, diffResult.nAdded);
		Assert.assertEquals(0, diffResult.nRemoved);
		Assert.assertEquals(5, diffResult.nAttributeChanged);
		Assert.assertEquals(3, xmlLeft.depth);
		Assert.assertEquals(3, xmlRight.depth);
		
		List<XmlElement> collected;
		
		collected = checkCollectChangedElements(Arrays.asList(
				  "(+) technology:sdjhjsdfhsdhfsdhfsdjkhfsdjk"
				, "(#) technology:dotnet"
				, "(#) technology:ria"
				, "(#) technology:webserverXY"
				, "(#) technology:php"
				, "(#) technology:javaNew"
				, "(+) technology:someOtherNewElement"
				, "(+) technology:ria1"
				, "(+) technology:ria2"
				), 9, item, xmlRight);
		
		checkPeers(Arrays.asList(
				  "element (+) technology:sdjhjsdfhsdhfsdhfsdjkhfsdjk"
				, "---peer <none>"
				, "element (#) technology:dotnet"
				, "---peer (#) technology:dotnet"
				, "element (#) technology:ria"
				, "---peer (#) technology:ria"
				, "element (#) technology:webserverXY"
				, "---peer (#) technology:webserver"
				, "element (#) technology:php"
				, "---peer (#) technology:php"
				, "element (#) technology:javaNew"
				, "---peer (#) technology:java"
				, "element (+) technology:someOtherNewElement"
				, "---peer <none>"
				, "element (+) technology:ria1"
				, "---peer <none>"
				, "element (+) technology:ria2"
				, "---peer <none>"
				), collected);
		
		collected = checkCollectChangedElements(Arrays.asList(
				"(#) technology:java"
				, "(#) technology:dotnet"
				, "(#) technology:ria"
				, "(#) technology:webserver"
				, "(#) technology:php"
				), 5, item, xmlLeft);
		
		checkPeers(Arrays.asList(
				  "element (#) technology:java"
				, "---peer (#) technology:javaNew"
				, "element (#) technology:dotnet"
				, "---peer (#) technology:dotnet"
				, "element (#) technology:ria"
				, "---peer (#) technology:ria"
				, "element (#) technology:webserver"
				, "---peer (#) technology:webserverXY"
				, "element (#) technology:php"
				, "---peer (#) technology:php"
				), collected);
	}
	
	@Test
	public void case3() throws XMLStreamException, IOException {
		String[] inputLeft = IO.readLines(new FileReader("samples/shuffle-test-3.left.profile.xml"));
		String[] inputRight = IO.readLines(new FileReader("samples/shuffle-test-3.right.profile.xml"));
		
		XmlStruct xmlLeft = lexer.parse(IO.asString(inputLeft));
		XmlStruct xmlRight = lexer.parse(IO.asString(inputRight));
		
		XmlDiffer.Result diffResult = differ.diff(xmlLeft, xmlRight);
		Assert.assertEquals(0, diffResult.nAdded);
		Assert.assertEquals(3, diffResult.nRemoved);
		Assert.assertEquals(4, diffResult.nAttributeChanged);
		Assert.assertEquals(3, xmlLeft.depth);
		Assert.assertEquals(3, xmlRight.depth);
		
		List<XmlElement> collected;
		
		collected = checkCollectChangedElements(Arrays.asList(
				  "(#) technology:dotnet"
				, "(#) technology:webserver"
				, "(#) technology:php"
				, "(#) technology:ria"
				), 4, item, xmlRight);
		
		checkPeers(Arrays.asList(
				  "element (#) technology:dotnet"
				, "---peer (#) technology:dotnet"
				, "element (#) technology:webserver"
				, "---peer (#) technology:webserverXY"
				, "element (#) technology:php"
				, "---peer (#) technology:php"
				, "element (#) technology:ria"
				, "---peer (#) technology:ria1"
				), collected);
		
		collected = checkCollectChangedElements(Arrays.asList(
				  "(-) technology:sdjhjsdfhsdhfsdhfsdjkhfsdjk"
				, "(#) technology:dotnet"
				, "(#) technology:webserverXY"
				, "(#) technology:php"
				, "(-) technology:someOtherNewElement"
				, "(#) technology:ria1"
				, "(-) technology:ria11"
				), 7, item, xmlLeft);
		
		checkPeers(Arrays.asList(
				  "element (-) technology:sdjhjsdfhsdhfsdhfsdjkhfsdjk"
				, "---peer <none>"
				, "element (#) technology:dotnet"
				, "---peer (#) technology:dotnet"
				, "element (#) technology:webserverXY"
				, "---peer (#) technology:webserver"
				, "element (#) technology:php"
				, "---peer (#) technology:php"
				, "element (-) technology:someOtherNewElement"
				, "---peer <none>"
				, "element (#) technology:ria1"
				, "---peer (#) technology:ria"
				, "element (-) technology:ria11"
				, "---peer <none>"
				), collected);
	}
	
	@Test
	public void case4() throws XMLStreamException, IOException {
		String[] inputLeft = IO.readLines(new FileReader("samples/shuffle-test-4.left.profile.xml"));
		String[] inputRight = IO.readLines(new FileReader("samples/shuffle-test-4.right.profile.xml"));
		
		XmlStruct xmlLeft = lexer.parse(IO.asString(inputLeft));
		XmlStruct xmlRight = lexer.parse(IO.asString(inputRight));
		
		XmlDiffer.Result diffResult = differ.diff(xmlLeft, xmlRight);
		Assert.assertEquals(0, diffResult.nAdded);
		Assert.assertEquals(0, diffResult.nRemoved);
		Assert.assertEquals(5, diffResult.nAttributeChanged);
		Assert.assertEquals(8, xmlLeft.depth);
		Assert.assertEquals(8, xmlRight.depth);
		
		List<XmlElement> collected;
		
		collected = checkCollectChangedElements(Arrays.asList(
				"(#) technology:sdjhjsdfhsdhfsdhfsdjkhfsdjk"
				, "(#) technology:dotnet"
				, "(#) technology:ria"
				, "(#) technology:webserverXY"
				, "(#) technology:php"
				), 5, item, xmlRight);
		
		checkPeers(Arrays.asList(
				"element (#) technology:sdjhjsdfhsdhfsdhfsdjkhfsdjk"
				, "---peer (#) technology:java"
				, "element (#) technology:dotnet"
				, "---peer (#) technology:dotnet"
				, "element (#) technology:ria"
				, "---peer (#) technology:ria"
				, "element (#) technology:webserverXY"
				, "---peer (#) technology:webserver"
				, "element (#) technology:php"
				, "---peer (#) technology:php"
				), collected);
		
		collected = checkCollectChangedElements(Arrays.asList(
				"(#) technology:java"
				, "(#) technology:dotnet"
				, "(#) technology:ria"
				, "(#) technology:webserver"
				, "(#) technology:php"
				), 5, item, xmlLeft);
		
		checkPeers(Arrays.asList(
				"element (#) technology:java"
				, "---peer (#) technology:sdjhjsdfhsdhfsdhfsdjkhfsdjk"
				, "element (#) technology:dotnet"
				, "---peer (#) technology:dotnet"
				, "element (#) technology:ria"
				, "---peer (#) technology:ria"
				, "element (#) technology:webserver"
				, "---peer (#) technology:webserverXY"
				, "element (#) technology:php"
				, "---peer (#) technology:php"
				), collected);
	}
	
	@Test
	public void case5() throws XMLStreamException, IOException {
		String[] inputLeft = IO.readLines(new FileReader("samples/shuffle-test-5.left.profile.xml"));
		String[] inputRight = IO.readLines(new FileReader("samples/shuffle-test-5.right.profile.xml"));
		
		XmlStruct xmlLeft = lexer.parse(IO.asString(inputLeft));
		XmlStruct xmlRight = lexer.parse(IO.asString(inputRight));
		
		XmlDiffer.Result diffResult = differ.diff(xmlLeft, xmlRight);
		Assert.assertEquals(1, diffResult.nAdded);
		Assert.assertEquals(2, diffResult.nRemoved);
		Assert.assertEquals(10, diffResult.nAttributeChanged);
		Assert.assertEquals(8, xmlLeft.depth);
		Assert.assertEquals(8, xmlRight.depth);
		
		List<XmlElement> collected;
		
		collected = checkCollectChangedElements(Arrays.asList(
				  "(#) technology:sdjhjsdfhsdhfsdhfsdjkhfsdjk"
				, "(#) technology:dotnet"
				, "(#) technology:ria"
				, "(#) technology:webserverXY"
				, "(#) technology:php"
				, "(#) capabilities:dotnet2.0"
				, "(+) capabilities:jvmtiEventNotifications"
				, "(#) sensor:com.dynatrace.diagnostics.sensorgroup.method.Java.137a9553e0938832x"
				, "(#) sensor:com.dynatrace.diagnostics.knowledgesensor.java.ejb2.0"
				, "(#) sensor:com.dynatrace.diagnostics.knowledgesensor.javax.rmi"
				, "(#) sensor:com.dynatrace.diagnostics.sensorgroup.method..NET.5c1b7d402f7ba"
				), 11, item, xmlRight);
		
		checkPeers(Arrays.asList(
				  "element (#) technology:sdjhjsdfhsdhfsdhfsdjkhfsdjk"
				, "---peer (#) technology:java"
				, "element (#) technology:dotnet"
				, "---peer (#) technology:dotnet"
				, "element (#) technology:ria"
				, "---peer (#) technology:ria"
				, "element (#) technology:webserverXY"
				, "---peer (#) technology:webserver"
				, "element (#) technology:php"
				, "---peer (#) technology:php"
				, "element (#) capabilities:dotnet2.0"
				, "---peer (#) capabilities:dotnet"
				, "element (+) capabilities:jvmtiEventNotifications"
				, "---peer <none>"
				, "element (#) sensor:com.dynatrace.diagnostics.sensorgroup.method.Java.137a9553e0938832x"
				, "---peer (#) sensor:com.dynatrace.diagnostics.sensorgroup.method.Java.137a9553e0938832"
				, "element (#) sensor:com.dynatrace.diagnostics.knowledgesensor.java.ejb2.0"
				, "---peer (#) sensor:com.dynatrace.diagnostics.knowledgesensor.java.ejb"
				, "element (#) sensor:com.dynatrace.diagnostics.knowledgesensor.javax.rmi"
				, "---peer (#) sensor:com.dynatrace.diagnostics.knowledgesensor.java.rmi"
				, "element (#) sensor:com.dynatrace.diagnostics.sensorgroup.method..NET.5c1b7d402f7ba"
				, "---peer (#) sensor:com.dynatrace.diagnostics.sensorgroup.method..NET.5c1b7d402f7b"
				), collected);
		
		collected = checkCollectChangedElements(Arrays.asList(
				  "(#) technology:java"
				, "(#) technology:dotnet"
				, "(#) technology:ria"
				, "(#) technology:webserver"
				, "(#) technology:php"
				, "(#) capabilities:dotnet"
				, "(#) sensor:com.dynatrace.diagnostics.sensorgroup.method.Java.137a9553e0938832"
				, "(#) sensor:com.dynatrace.diagnostics.knowledgesensor.java.ejb"
				, "(#) sensor:com.dynatrace.diagnostics.knowledgesensor.java.rmi"
				, "(-) sensor:com.dynatrace.diagnostics.knowledgesensor.dotnet.adonet"
				, "(-) sensor:com.dynatrace.diagnostics.knowledgesensor.java.awt"
				, "(#) sensor:com.dynatrace.diagnostics.sensorgroup.method..NET.5c1b7d402f7b"
				), 12, item, xmlLeft);
		
		checkPeers(Arrays.asList(
				  "element (#) technology:java"
				, "---peer (#) technology:sdjhjsdfhsdhfsdhfsdjkhfsdjk"
				, "element (#) technology:dotnet"
				, "---peer (#) technology:dotnet"
				, "element (#) technology:ria"
				, "---peer (#) technology:ria"
				, "element (#) technology:webserver"
				, "---peer (#) technology:webserverXY"
				, "element (#) technology:php"
				, "---peer (#) technology:php"
				, "element (#) capabilities:dotnet"
				, "---peer (#) capabilities:dotnet2.0"
				, "element (#) sensor:com.dynatrace.diagnostics.sensorgroup.method.Java.137a9553e0938832"
				, "---peer (#) sensor:com.dynatrace.diagnostics.sensorgroup.method.Java.137a9553e0938832x"
				, "element (#) sensor:com.dynatrace.diagnostics.knowledgesensor.java.ejb"
				, "---peer (#) sensor:com.dynatrace.diagnostics.knowledgesensor.java.ejb2.0"
				, "element (#) sensor:com.dynatrace.diagnostics.knowledgesensor.java.rmi"
				, "---peer (#) sensor:com.dynatrace.diagnostics.knowledgesensor.javax.rmi"
				, "element (-) sensor:com.dynatrace.diagnostics.knowledgesensor.dotnet.adonet"
				, "---peer <none>"
				, "element (-) sensor:com.dynatrace.diagnostics.knowledgesensor.java.awt"
				, "---peer <none>"
				, "element (#) sensor:com.dynatrace.diagnostics.sensorgroup.method..NET.5c1b7d402f7b"
				, "---peer (#) sensor:com.dynatrace.diagnostics.sensorgroup.method..NET.5c1b7d402f7ba"
				), collected);
	}
	
	/**
	 * "ria" <=> "ria1" and "ria2" same distance of 1
	 */
	@Test
	public void case6NonEqualDistance() throws XMLStreamException, IOException {
		String[] inputLeft = IO.readLines(new FileReader("samples/shuffle-test-6.left.profile.xml"));
		String[] inputRight = IO.readLines(new FileReader("samples/shuffle-test-6.right.profile.xml"));
		
		XmlStruct xmlLeft = lexer.parse(IO.asString(inputLeft));
		XmlStruct xmlRight = lexer.parse(IO.asString(inputRight));
		
		XmlDiffer.Result diffResult = differ.diff(xmlLeft, xmlRight);
		Assert.assertEquals(0, diffResult.nAdded);
		Assert.assertEquals(3, diffResult.nRemoved);
		Assert.assertEquals(4, diffResult.nAttributeChanged);
		Assert.assertEquals(3, xmlLeft.depth);
		Assert.assertEquals(3, xmlRight.depth);
		
		List<XmlElement> collected;
		
		collected = checkCollectChangedElements(Arrays.asList(
				  "(#) technology:dotnet"
				, "(#) technology:webserver"
				, "(#) technology:php"
				, "(#) technology:ria"
				), 4, item, xmlRight);
		
		checkPeers(Arrays.asList(
				  "element (#) technology:dotnet"
				, "---peer (#) technology:dotnet"
				, "element (#) technology:webserver"
				, "---peer (#) technology:webserverXY"
				, "element (#) technology:php"
				, "---peer (#) technology:php"
				, "element (#) technology:ria"
				, "---peer (#) technology:ria1"
				), collected);
		
		collected = checkCollectChangedElements(Arrays.asList(
				  "(-) technology:sdjhjsdfhsdhfsdhfsdjkhfsdjk"
				, "(#) technology:dotnet"
				, "(#) technology:webserverXY"
				, "(#) technology:php"
				, "(-) technology:someOtherNewElement"
				, "(#) technology:ria1"
				, "(-) technology:ria2"
				), 7, item, xmlLeft);
		
		checkPeers(Arrays.asList(
				  "element (-) technology:sdjhjsdfhsdhfsdhfsdjkhfsdjk"
				, "---peer <none>"
				, "element (#) technology:dotnet"
				, "---peer (#) technology:dotnet"
				, "element (#) technology:webserverXY"
				, "---peer (#) technology:webserver"
				, "element (#) technology:php"
				, "---peer (#) technology:php"
				, "element (-) technology:someOtherNewElement"
				, "---peer <none>"
				, "element (#) technology:ria1"
				, "---peer (#) technology:ria"
				, "element (-) technology:ria2"
				, "---peer <none>"
				), collected);
	}

}
