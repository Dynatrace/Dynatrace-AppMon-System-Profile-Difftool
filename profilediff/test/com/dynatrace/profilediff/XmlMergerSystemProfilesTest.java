package com.dynatrace.profilediff;

import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

import javax.xml.stream.XMLStreamException;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.dynatrace.profilediff.lib.CharSequenceReader;

public class XmlMergerSystemProfilesTest extends DifferMergerTestBase {
	
	public XmlMergerSystemProfilesTest(XmlMerger3Factory xmlMerger3Factory,
			String mergerName, XmlDifferFactory xmlDifferFactory,
			String differName, int stringMetricDifferThreshold) {
		super(xmlMerger3Factory, mergerName, xmlDifferFactory, differName, stringMetricDifferThreshold);
	}

	static XmlMerger3.IncludeElementCallback callback(boolean add, boolean remove) {
		return new XmlMerger3.IncludeElementCallback() {
			@Override
			public boolean addElement(XmlElement element) {
				return add;
			}
	
			@Override
			public boolean removeElement(XmlElement element) {
				return remove;
			}
		};
	}
	
	@BeforeClass
	public static void beforeClass() {
		resetGlobalMaxDistances();
	}
	
	@AfterClass
	public static void afterClass() {
		expectGlobalMaxDistances("XmlMergerSystemProfilesTest", 0, 0, 4, /*expectedMetricInvokeCount*/ 0);
	}
	
	XmlLexer lexer;
	XmlDiffer differ;
	XmlMerger3 merger;
	
	@Before
	public void before() {
		lexer = new XmlLexer(SYSTEM_PROFILE_DISCRIMINATOR_ATTRIBUTES, stringCache);
		differ = xmlDifferFactory.create(SYSTEM_PROFILE_IGNORED_ATTRIBUTES);
		merger = xmlMerger3Factory.create(lexer, differ);
	}
	
	@Test
	public void case1() throws XMLStreamException, IOException {
		String[] inputLeft = IO.readLines(new FileReader("samples/NewProfile4.profile.xml"));
		String[] inputRight = IO.readLines(new FileReader("samples/NewProfile2_Evil_SensorGroup_Mod.profile.xml"));
		
		XmlStruct xmlLeft = lexer.parse(IO.asString(inputLeft));
		XmlStruct xmlRight = lexer.parse(IO.asString(inputRight));
		
		XmlDiffer.Result diffResult = differ.diff(xmlLeft, xmlRight);
		Assert.assertEquals(10, diffResult.nAdded);
		Assert.assertEquals(3, diffResult.nRemoved);
		Assert.assertEquals(2, diffResult.nAttributeChanged);
		Assert.assertEquals(9, xmlLeft.depth);
		Assert.assertEquals(9, xmlRight.depth);
		
		XmlStruct merged = merger.merge(IO.asString(inputLeft), IO.asString(inputRight), false, callback(/*add*/ true, /*remove*/ true), stop);
		String[] lines = IO.readLines(new CharSequenceReader(merged.data));
		
		checkList(Arrays.asList(IO.readLines(new FileReader("samples/case1.profile.xml"))), Arrays.asList(lines));
		
		lexer.parse(IO.asString(lines));
		
		checkAttributesStorage(xmlLeft);
		checkAttributesStorage(xmlRight);
		checkCallbackCounts(xmlLeft, xmlRight, /*max*/ 10, differ, merger, /*removeElement*/ true);
		checkCallbackCounts(xmlLeft, xmlRight, /*max*/ 10, differ, merger, /*removeElement*/ false);
	}
	
	@Test
	public void case2() throws XMLStreamException, IOException {
		String[] inputLeft = IO.readLines(new FileReader("samples/NewProfile2_Evil_SensorGroup_Mod.profile.xml"));
		String[] inputRight = IO.readLines(new FileReader("samples/NewProfile4.profile.xml"));
		
		XmlStruct xmlLeft = lexer.parse(IO.asString(inputLeft));
		XmlStruct xmlRight = lexer.parse(IO.asString(inputRight));
		
		XmlDiffer.Result diffResult = differ.diff(xmlLeft, xmlRight);
		Assert.assertEquals(3, diffResult.nAdded);
		Assert.assertEquals(10, diffResult.nRemoved);
		Assert.assertEquals(2, diffResult.nAttributeChanged);
		Assert.assertEquals(9, xmlLeft.depth);
		Assert.assertEquals(9, xmlRight.depth);
		
		XmlStruct merged = merger.merge(IO.asString(inputLeft), IO.asString(inputRight), false, callback(/*add*/ true, /*remove*/ true), stop);
		String[] lines = IO.readLines(new CharSequenceReader(merged.data));
		
		checkList(Arrays.asList(IO.readLines(new FileReader("samples/case2.profile.xml"))), Arrays.asList(lines));
		
		lexer.parse(IO.asString(lines));
		
		checkAttributesStorage(xmlLeft);
		checkAttributesStorage(xmlRight);
		checkCallbackCounts(xmlLeft, xmlRight, /*max*/ 10, differ, merger, /*removeElement*/ true);
		checkCallbackCounts(xmlLeft, xmlRight, /*max*/ 10, differ, merger, /*removeElement*/ false);
	}
	
	@Test
	public void case2RegressionDiffState() throws XMLStreamException, IOException {
		String[] inputLeft = IO.readLines(new FileReader("samples/NewProfile4.profile.xml"));
		String[] inputRight = inputLeft;
		
		XmlStruct xmlLeft = lexer.parse(IO.asString(inputLeft));
		XmlStruct xmlRight = lexer.parse(IO.asString(inputRight));
		
		XmlDiffer.Result diffResult = differ.diff(xmlLeft, xmlRight);
		Assert.assertEquals(0, diffResult.nAdded);
		Assert.assertEquals(0, diffResult.nRemoved);
		Assert.assertEquals(0, diffResult.nAttributeChanged);
		
		XmlStruct merged = merger.merge(IO.asString(inputLeft), IO.asString(inputRight), false, callback(/*add*/ true, /*remove*/ true), stop);
		String[] lines = IO.readLines(new CharSequenceReader(merged.data));
		
		checkList(Arrays.asList(inputLeft), Arrays.asList(lines));
		
		lexer.parse(IO.asString(lines));
		
		inputLeft = IO.readLines(new FileReader("samples/NewProfile2_Evil_SensorGroup_Mod.profile.xml"));
		
		xmlLeft = lexer.parse(IO.asString(inputLeft));
		
		/*
		 * in this diffing run, we reuse xmlRight incl. the diffing state. this must work with same results.
		 */
		diffResult = differ.diff(xmlLeft, xmlRight);
		Assert.assertEquals(3, diffResult.nAdded);
		Assert.assertEquals(10, diffResult.nRemoved);
		Assert.assertEquals(2, diffResult.nAttributeChanged);
		
		merged = merger.merge(IO.asString(inputLeft), IO.asString(inputRight), false, callback(/*add*/ true, /*remove*/ true), stop);
		lines = IO.readLines(new CharSequenceReader(merged.data));
		
		checkList(Arrays.asList(IO.readLines(new FileReader("samples/case2.profile.xml"))), Arrays.asList(lines));
		
		lexer.parse(IO.asString(lines));
	}
	
	@Test
	public void case3() throws XMLStreamException, IOException {
		String[] inputLeft = IO.readLines(new FileReader("samples/EmptyDefaultSystemProfileOct2014.profile.xml"));
		String[] inputRight = IO.readLines(new FileReader("samples/EmptyDefaultSystemProfileOct2014_stuff_added2.profile.xml"));
		
		XmlStruct xmlLeft = lexer.parse(IO.asString(inputLeft));
		XmlStruct xmlRight = lexer.parse(IO.asString(inputRight));
		
		XmlDiffer.Result diffResult = differ.diff(xmlLeft, xmlRight);
		Assert.assertEquals(16, diffResult.nAdded);
		Assert.assertEquals(0, diffResult.nRemoved);
		Assert.assertEquals(1, diffResult.nAttributeChanged);
		Assert.assertEquals(9, xmlLeft.depth);
		Assert.assertEquals(9, xmlRight.depth);

		XmlStruct merged = merger.merge(IO.asString(inputLeft), IO.asString(inputRight), false,  callback(/*add*/ true, /*remove*/ true), stop);
		String[] lines = IO.readLines(new CharSequenceReader(merged.data));
		
		checkList(Arrays.asList(IO.readLines(new FileReader("samples/case3.profile.xml"))), Arrays.asList(lines));
		
		lexer.parse(IO.asString(lines));
		
		checkAttributesStorage(xmlLeft);
		checkAttributesStorage(xmlRight);
		checkCallbackCounts(xmlLeft, xmlRight, /*max*/ 10, differ, merger, /*removeElement*/ true);
		checkCallbackCounts(xmlLeft, xmlRight, /*max*/ 10, differ, merger, /*removeElement*/ false);
		
		diffResult = differ.diff(xmlLeft, xmlRight);
	}
}
