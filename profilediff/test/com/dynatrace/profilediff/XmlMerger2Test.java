package com.dynatrace.profilediff;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.junit.Test;

public class XmlMerger2Test extends DifferMergerTestBase {
	

	public XmlMerger2Test(XmlMerger3Factory xmlMerger3Factory,
			String mergerName, XmlDifferFactory xmlDifferFactory,
			String differName, int stringMetricDifferThreshold) {
		super(xmlMerger3Factory, mergerName, xmlDifferFactory, differName, stringMetricDifferThreshold);
	}

	private static final List<String> ignoreAttributeNames = Collections.emptyList();
	
	static XmlMerger2.IncludeElementCallback callback(boolean add, boolean remove) {
		return new XmlMerger2.IncludeElementCallback() {
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

	@Test
	public void prettySyntax() throws XMLStreamException, IOException {
		String[] inputLeft = {
				  "<food>"
				, "   <fruits>"
				, "     <apple/>"
				, "     <banana/>"
				, "   </fruits>"
				, "   <noodles>"
				, "   </noodles>"
				, "   <vegetables>"
				, "     <spinach/>"
				, "     <carrot/>"
				, "   </vegetables>"
				, "   <rubbish>"
				, "     <ham/>"
				, "   </rubbish>"
				, "</food>"
		};
		String[] inputRight = {
				"<food>"
				, "   <fruits>"
				, "     <raspberry/>"
				, "     <apple/>"
				, "     <banana/>"
				, "     <strawberry/>"
				, "   </fruits>"
				, "   <vegetables>"
				, "     <carrot/>"
				, "     <potato/>"
				, "   </vegetables>"
				, "   <sweets>"
				, "     <chocolate/>"
				, "     <candy/>"
				, "   </sweets>"
				, "   <other>"
				, "     <dogfood/>"
				, "   </other>"
				, "</food>"
		};
		
		List<String> discriminatingAttributes = (Arrays.asList());
		
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		XmlStruct xmlLeft = lexer.parse(IO.asString(inputLeft));
		XmlStruct xmlRight = lexer.parse(IO.asString(inputRight));
		
		XmlDiffer differ = xmlDifferFactory.create(ignoreAttributeNames);
		differ.diff(xmlLeft, xmlRight);
		
		XmlMerger2 merger = new XmlMerger2();
		
		String merged = merger.merge(xmlLeft, xmlRight, /*addComments*/ false, callback(/*add*/ true, /*remove*/ false));
		String[] lines = IO.readLines(new StringReader(merged)); 
		
		checkList(Arrays.asList(
				  "<food>"
				, "   <fruits>"
				, "     <raspberry/>"
				, "     <apple/>"
				, "     <banana/>"
				, "     <strawberry/>"
				, "   </fruits>"
				, "   <noodles>"
				, "   </noodles>"
				, "   <vegetables>"
				, "     <spinach/>"
				, "     <carrot/>"
				, "     <potato/>"
				, "   </vegetables>"
				, "   <sweets>"
				, "     <chocolate/>"
				, "     <candy/>"
				, "   </sweets>"
				, "   <other>"
				, "     <dogfood/>"
				, "   </other>"
				, "   <rubbish>"
				, "     <ham/>"
				, "   </rubbish>"
				, "</food>"
				), Arrays.asList(lines));
		
		lexer.parse(IO.asString(lines));
		
		merged = merger.merge(xmlLeft, xmlRight, /*addComments*/ false, callback(/*add*/ true, /*remove*/ true));
		lines = IO.readLines(new StringReader(merged)); 
		
		checkList(Arrays.asList(
				  "<food>"
				, "   <fruits>"
				, "     <raspberry/>"
				, "     <apple/>"
				, "     <banana/>"
				, "     <strawberry/>"
				, "   </fruits>"
				, "   <vegetables>"
				, "     <carrot/>"
				, "     <potato/>"
				, "   </vegetables>"
				, "   <sweets>"
				, "     <chocolate/>"
				, "     <candy/>"
				, "   </sweets>"
				, "   <other>"
				, "     <dogfood/>"
				, "   </other>"
				, "</food>"
				), Arrays.asList(lines));
		
		lexer.parse(IO.asString(lines));
	}
	
	@Test
	public void uglySyntax() throws XMLStreamException, IOException {
		String[] inputLeft = {
				  " <food>"
				, "   <fruits><apple/>"
				, "     <banana/>"
				, "   </fruits><noodles>"
				, "   </noodles>"
				, "   <vegetables>"
				, "     <spinach/>"
				, "     <carrot/></vegetables>"
				, "   <rubbish>"
				, "     <ham/>"
				, "   </rubbish></food>"
		};
		String[] inputRight = {
				"<food>"
				, "   <fruits>"
				, "     <raspberry/>"
				, "     <apple/>"
				, "     <banana/>"
				, "     <strawberry/></fruits>"
				, "   <vegetables>"
				, "     <carrot/><potato/>"
				, "   </vegetables><sweets>"
				, "     <chocolate/>"
				, "     <candy/>"
				, "   </sweets><other>"
				, "     <dogfood/>"
				, "   </other>"
				, "</food>"
		};
		
		List<String> discriminatingAttributes = (Arrays.asList());
		
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		XmlStruct xmlLeft = lexer.parse(IO.asString(inputLeft));
		XmlStruct xmlRight = lexer.parse(IO.asString(inputRight));
		
		XmlDiffer differ = xmlDifferFactory.create(ignoreAttributeNames);
		differ.diff(xmlLeft, xmlRight);
		
		XmlMerger2 merger = new XmlMerger2();
		
		String merged = merger.merge(xmlLeft, xmlRight, /*addComments*/ false, callback(/*add*/ true, /*remove*/ false));
		String[] lines = IO.readLines(new StringReader(merged)); 
		
		checkList(Arrays.asList(
				  " <food>"
				, "   <fruits>"
				, "     <raspberry/><apple/>"
				, "     <banana/>"
				, "     <strawberry/>"
				, "   </fruits><noodles>"
				, "   </noodles>"
				, "   <vegetables>"
				, "     <spinach/>"
				, "     <carrot/><potato/></vegetables><sweets>"
				, "     <chocolate/>"
				, "     <candy/>"
				, "   </sweets><other>"
				, "     <dogfood/>"
				, "   </other>"
				, "   <rubbish>"
				, "     <ham/>"
				, "   </rubbish></food>"
				), Arrays.asList(lines));
		
		lexer.parse(IO.asString(lines));
		
		merged = merger.merge(xmlLeft, xmlRight, /*addComments*/ false, callback(/*add*/ true, /*remove*/ true));
		lines = IO.readLines(new StringReader(merged)); 
		
		checkList(Arrays.asList(
				  " <food>"
				, "   <fruits>"
				, "     <raspberry/><apple/>"
				, "     <banana/>"
				, "     <strawberry/>"
				, "   </fruits>"
				, "   <vegetables>"
				, "     <carrot/><potato/></vegetables><sweets>"
				, "     <chocolate/>"
				, "     <candy/>"
				, "   </sweets><other>"
				, "     <dogfood/>"
				, "   </other></food>"
				), Arrays.asList(lines));
		
		lexer.parse(IO.asString(lines));
	}
	
	@Test
	public void evenUglierSyntax() throws XMLStreamException, IOException {
		String[] inputLeft = {
				  " <food a='b'"
				, " x='y'"
				, ">   <fruits><apple"
				, "/>     <banana/>"
				, "   </fruits><noodles>"
				, "   </noodles>"
				, "   <vegetables"
				, "     ><spinach/>"
				, "     <carrot/></vegetables>"
				, "   <rubbish>"
				, "     <ham/>"
				, "   </rubbish></food>"
		};
		String[] inputRight = {
				"<food a='c'>"
				, "   <fruits"
				, "     ><raspberry/>"
				, "     <apple />"
				, "     <banana  />"
				, "     <strawberry/> </fruits"
				, ">"
				, "   <vegetables>"
				, "     <carrot/><potato/>"
				, "   </vegetables><sweets>"
				, "     <chocolate/>"
				, "     <candy/>"
				, "   </sweets><other>"
				, "     <dogfood/>"
				, "   </other>"
				, "</food>"
		};
		
		List<String> discriminatingAttributes = (Arrays.asList());
		
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		XmlStruct xmlLeft = lexer.parse(IO.asString(inputLeft));
		XmlStruct xmlRight = lexer.parse(IO.asString(inputRight));
		
		XmlDiffer differ = xmlDifferFactory.create(ignoreAttributeNames);
		differ.diff(xmlLeft, xmlRight);
		
		XmlMerger2 merger = new XmlMerger2();
		
		String merged = merger.merge(xmlLeft, xmlRight, /*addComments*/ false, callback(/*add*/ true, /*remove*/ false));
		String[] lines = IO.readLines(new StringReader(merged)); 
		
		checkList(Arrays.asList(
				  " <food a='b'"
				, " x='y'"
				, ">   <fruits><raspberry/><apple"
				, "/>     <banana/>"
				, "     <strawberry/>"
				, "   </fruits><noodles>"
				, "   </noodles>"
				, "   <vegetables"
				, "     ><spinach/>"
				, "     <carrot/><potato/></vegetables><sweets>"
				, "     <chocolate/>"
				, "     <candy/>"
				, "   </sweets><other>"
				, "     <dogfood/>"
				, "   </other>"
				, "   <rubbish>"
				, "     <ham/>"
				, "   </rubbish></food>"
				), Arrays.asList(lines));
		
		lexer.parse(IO.asString(lines));
		
		merged = merger.merge(xmlLeft, xmlRight, /*addComments*/ false, callback(/*add*/ true, /*remove*/ true));
		lines = IO.readLines(new StringReader(merged)); 
		
		checkList(Arrays.asList(
				  " <food a='b'"
				, " x='y'"
				, ">   <fruits><raspberry/><apple"
				, "/>     <banana/>"
				, "     <strawberry/>"
				, "   </fruits>"
				, "   <vegetables"
				, "     >"
				, "     <carrot/><potato/></vegetables><sweets>"
				, "     <chocolate/>"
				, "     <candy/>"
				, "   </sweets><other>"
				, "     <dogfood/>"
				, "   </other></food>"
				), Arrays.asList(lines));
		
		lexer.parse(IO.asString(lines));
	}
	
	@Test
	public void intoEmptyParent1Child() throws XMLStreamException, IOException {
		String[] inputLeft = {
				"<element id='originalDocument'/>"
		};
		String[] inputRight = {
				"<element>"
				,"  <inserted key='new1'/>"
				,"</element>"
		};
		
		List<String> discriminatingAttributes = (Arrays.asList("key"));
		
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		XmlStruct xmlLeft = lexer.parse(IO.asString(inputLeft));
		XmlStruct xmlRight = lexer.parse(IO.asString(inputRight));
		
		XmlDiffer differ = xmlDifferFactory.create(ignoreAttributeNames);
		differ.diff(xmlLeft, xmlRight);
		
		XmlMerger2 merger = new XmlMerger2();
		
		String merged = merger.merge(xmlLeft, xmlRight, /*addComments*/ false, callback(/*add*/ true, /*remove*/ true));
		String[] lines = IO.readLines(new StringReader(merged)); 
		
		checkList(Arrays.asList(
				  "<element>"
				, "  <inserted key='new1'/>"
				, "</element>"
				), Arrays.asList(lines));
		
		lexer.parse(IO.asString(lines));
	}
	
	@Test
	public void intoEmptyParent2Children() throws XMLStreamException, IOException {
		String[] inputLeft = {
				"<element id='originalDocument'/>"
		};
		String[] inputRight = {
				"<element>"
				,"  <inserted key='new1'/>"
				,"  <inserted key='new2'/>"
				,"</element>"
		};
		
		List<String> discriminatingAttributes = (Arrays.asList("key"));
		
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		XmlStruct xmlLeft = lexer.parse(IO.asString(inputLeft));
		XmlStruct xmlRight = lexer.parse(IO.asString(inputRight));
		
		XmlDiffer differ = xmlDifferFactory.create(ignoreAttributeNames);
		differ.diff(xmlLeft, xmlRight);
		
		XmlMerger2 merger = new XmlMerger2();
		
		String merged = merger.merge(xmlLeft, xmlRight, /*addComments*/ false, callback(/*add*/ true, /*remove*/ true));
		String[] lines = IO.readLines(new StringReader(merged)); 
		
		checkList(Arrays.asList(
				  "<element>"
				, "  <inserted key='new1'/>"
				, "  <inserted key='new2'/>"
				, "</element>"
				), Arrays.asList(lines));
		
		lexer.parse(IO.asString(lines));
	}
	
	@Test
	public void intoEmptyParent2ChildrenNotRoot() throws XMLStreamException, IOException {
		String[] inputLeft = {
				  "<root>"
				, "  <element id='originalDocument'/>"
				, "</root>"
		};
		String[] inputRight = {
				  "<root>"
				, "<element>"
				,"  <inserted key='new1'/>"
				,"  <inserted key='new2'/>"
				,"</element>"
				, "</root>"
		};
		
		List<String> discriminatingAttributes = (Arrays.asList("key"));
		
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		XmlStruct xmlLeft = lexer.parse(IO.asString(inputLeft));
		XmlStruct xmlRight = lexer.parse(IO.asString(inputRight));
		
		XmlDiffer differ = xmlDifferFactory.create(ignoreAttributeNames);
		differ.diff(xmlLeft, xmlRight);
		
		XmlMerger2 merger = new XmlMerger2();
		
		String merged = merger.merge(xmlLeft, xmlRight, /*addComments*/ false, callback(/*add*/ true, /*remove*/ true));
		String[] lines = IO.readLines(new StringReader(merged)); 
		
		checkList(Arrays.asList(
				  "<root>"
				, "<element>"
				, "  <inserted key='new1'/>"
				, "  <inserted key='new2'/>"
				, "</element>"
				, "</root>"
				), Arrays.asList(lines));
		
		lexer.parse(IO.asString(lines));
	}
	
	@Test
	public void intoEmptyParent2ChildrenNotRoot2() throws XMLStreamException, IOException {
		String[] inputLeft = {
				"<root>"
				, "  <element id='originalDocument'>"
				, "</element></root>"
		};
		String[] inputRight = {
				"<root>"
				, "<element>"
				,"  <inserted key='new1'/>"
				,"  <inserted key='new2'/>"
				,"</element>"
				, "</root>"
		};
		
		List<String> discriminatingAttributes = (Arrays.asList("key"));
		
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		XmlStruct xmlLeft = lexer.parse(IO.asString(inputLeft));
		XmlStruct xmlRight = lexer.parse(IO.asString(inputRight));
		
		XmlDiffer differ = xmlDifferFactory.create(ignoreAttributeNames);
		differ.diff(xmlLeft, xmlRight);
		
		XmlMerger2 merger = new XmlMerger2();
		
		String merged = merger.merge(xmlLeft, xmlRight, /*addComments*/ false, callback(/*add*/ true, /*remove*/ true));
		String[] lines = IO.readLines(new StringReader(merged)); 
		
		checkList(Arrays.asList(
				  "<root>"
				, "<element>"
				, "  <inserted key='new1'/>"
				, "  <inserted key='new2'/>"
				, "</element></root>"
				), Arrays.asList(lines));
		
		lexer.parse(IO.asString(lines));
	}
}
