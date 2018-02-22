package com.dynatrace.profilediff;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.junit.Before;
import org.junit.Test;

import com.dynatrace.profilediff.lib.CharSequenceReader;

public class XmlMerger3Test extends DifferMergerTestBase {
	
	public XmlMerger3Test(XmlMerger3Factory xmlMerger3Factory,
			String mergerName, XmlDifferFactory xmlDifferFactory,
			String differName, int stringMetricDifferThreshold) {
		super(xmlMerger3Factory, mergerName, xmlDifferFactory, differName, stringMetricDifferThreshold);
	}

	XmlMerger3 merger;
	
	@Before
	public void before() {
		List<String> discriminatingAttributes = (Arrays.asList());
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		List<String> ignoreAttributeNames = Collections.emptyList();
		XmlDiffer differ = xmlDifferFactory.create(ignoreAttributeNames);
		merger = xmlMerger3Factory.create(lexer, differ);
	}
	
	static XmlMerger3.IncludeElementCallback callback(boolean add, boolean remove, boolean attr) {
		return new XmlMerger3.IncludeElementCallback() {
			@Override
			public boolean addElement(XmlElement element) {
				return add;
			}
	
			@Override
			public boolean removeElement(XmlElement element) {
				return remove;
			}
			
			@Override
			public boolean replaceAttributes(XmlElement element) {
				//System.out.println("attr? " + element.toPathString() + element.attributes + " vs. " + element.peer.attributes);
				return attr;
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
		
		XmlStruct merged = merger.merge(IO.asString(inputLeft), IO.asString(inputRight), false, callback(/*add*/ true, /*remove*/ false, /*attr*/ false), stop);
		String[] lines = IO.readLines(new CharSequenceReader(merged.data)); 
		
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
		
		merged = merger.merge(IO.asString(inputLeft), IO.asString(inputRight), false, callback(/*add*/ true, /*remove*/ true, /*attr*/ false), stop);
		lines = IO.readLines(new CharSequenceReader(merged.data)); 
		
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
		
		XmlStruct merged = merger.merge(IO.asString(inputLeft), IO.asString(inputRight), false, callback(/*add*/ true, /*remove*/ false, /*attr*/ false), stop);
		String[] lines = IO.readLines(new CharSequenceReader(merged.data)); 
		
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
		
		merged = merger.merge(IO.asString(inputLeft), IO.asString(inputRight), false, callback(/*add*/ true, /*remove*/ true, /*attr*/ false), stop);
		lines = IO.readLines(new CharSequenceReader(merged.data)); 
		
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
		
		XmlStruct merged = merger.merge(IO.asString(inputLeft), IO.asString(inputRight), false, callback(/*add*/ true, /*remove*/ false, /*attr*/ false), stop);
		String[] lines = IO.readLines(new CharSequenceReader(merged.data)); 
		
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
		
		merged = merger.merge(IO.asString(inputLeft), IO.asString(inputRight), false, callback(/*add*/ true, /*remove*/ true, /*attr*/ false), stop);
		lines = IO.readLines(new CharSequenceReader(merged.data)); 
		
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
		
		XmlStruct merged = merger.merge(IO.asString(inputLeft), IO.asString(inputRight), false, callback(/*add*/ true, /*remove*/ true, /*attr*/ false), stop);
		String[] lines = IO.readLines(new CharSequenceReader(merged.data)); 
		
		checkList(Arrays.asList(
				  "<element>"
				, "  <inserted key='new1'/>"
				, "</element>"
				), Arrays.asList(lines));
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
		
		XmlStruct merged = merger.merge(IO.asString(inputLeft), IO.asString(inputRight), false, callback(/*add*/ true, /*remove*/ true, /*attr*/ false), stop);
		String[] lines = IO.readLines(new CharSequenceReader(merged.data)); 
		
		checkList(Arrays.asList(
				  "<element>"
				, "  <inserted key='new1'/>"
				, "  <inserted key='new2'/>"
				, "</element>"
				), Arrays.asList(lines));
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
		
		XmlStruct merged = merger.merge(IO.asString(inputLeft), IO.asString(inputRight), false, callback(/*add*/ true, /*remove*/ true, /*attr*/ false), stop);
		String[] lines = IO.readLines(new CharSequenceReader(merged.data)); 
		
		checkList(Arrays.asList(
				  "<root>"
				, "<element>"
				, "  <inserted key='new1'/>"
				, "  <inserted key='new2'/>"
				, "</element>"
				, "</root>"
				), Arrays.asList(lines));
	}
	
	@Test
	public void intoEmptyParent3ChildrenNotRoot() throws XMLStreamException, IOException {
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
				,"  <inserted key='new3'/>"
				,"</element>"
				, "</root>"
		};
		
		XmlStruct merged = merger.merge(IO.asString(inputLeft), IO.asString(inputRight), false, callback(/*add*/ true, /*remove*/ true, /*attr*/ false), stop);
		String[] lines = IO.readLines(new CharSequenceReader(merged.data)); 
		
		checkList(Arrays.asList(
				  "<root>"
				, "<element>"
				, "  <inserted key='new1'/>"
				, "  <inserted key='new2'/>"
				, "  <inserted key='new3'/>"
				, "</element></root>"
				), Arrays.asList(lines));
	}
	
	@Test
	public void mergedFlag() throws XMLStreamException, IOException {
		String[] inputLeft = {
				  "<food>"
				, "   <fruits>"
				, "   </fruits>"
				, "</food>"
		};
		String[] inputRight = {
				"<food>"
				, "   <fruits>"
				, "     <fruit attr='1'/>"
				, "     <fruit attr='2'/>"
				, "     <fruit attr='3'/>"
				, "   </fruits>"
				, "</food>"
		};
		
		XmlStruct merged = merger.merge(IO.asString(inputLeft), IO.asString(inputRight), false, callback(/*add*/ true, /*remove*/ true, /*attr*/ false), stop);
		String[] lines = IO.readLines(new CharSequenceReader(merged.data)); 
		
		checkList(Arrays.asList(
				  "<food>"
				, "   <fruits>"
				, "     <fruit attr='1'/>"
				, "     <fruit attr='2'/>"
				, "     <fruit attr='3'/>"
				, "   </fruits>"
				, "</food>"
				), Arrays.asList(lines));
	
		merged = merger.merge(IO.asString(inputLeft), IO.asString(inputRight), false, new XmlMerger3.IncludeElementCallback() {
			int i = 0;
			
			@Override
			public boolean removeElement(XmlElement element) {
				return false;
			}
			
			@Override
			public boolean addElement(XmlElement element) {
				i++;
				return i == 2 || i == 3;
			}
		}, stop);
		lines = IO.readLines(new CharSequenceReader(merged.data)); 
		
		checkList(Arrays.asList(
				"<food>"
				, "   <fruits>"
				, "     <fruit attr='2'/>"
				, "     <fruit attr='3'/>"
				, "   </fruits>"
				, "</food>"
				), Arrays.asList(lines));
		
	}
	
	@Test
	public void intoEmptyParentAttributesSelectable() throws XMLStreamException, IOException {
		String[] inputLeft = {
				"<root>"
				, "  <element id='originalDocument'>"
				, "</element></root>"
		};
		String[] inputRight = {
				"<root>"
				, "<element key='newDocument'>"
				,"  <inserted key='new1'/>"
				,"  <inserted key='new2'/>"
				,"  <inserted key='new3'/>"
				,"</element>"
				, "</root>"
		};
		
		XmlStruct merged = merger.merge(IO.asString(inputLeft), IO.asString(inputRight), false, callback(/*add*/ true, /*remove*/ true, /*attr*/ true), stop);
		String[] lines = IO.readLines(new CharSequenceReader(merged.data)); 
		
		checkList(Arrays.asList(
				  "<root>"
				, "<element key='newDocument'>"
				, "  <inserted key='new1'/>"
				, "  <inserted key='new2'/>"
				, "  <inserted key='new3'/>"
				, "</element></root>"
				), Arrays.asList(lines));
		
		merged = merger.merge(IO.asString(inputLeft), IO.asString(inputRight), false, callback(/*add*/ true, /*remove*/ true, /*attr*/ false), stop);
		lines = IO.readLines(new CharSequenceReader(merged.data)); 
		
		checkList(Arrays.asList(
				"<root>"
				, "<element key='newDocument'>" //FIXME that's actually wrong! we should see originalDocument since attr => false
				, "  <inserted key='new1'/>"
				, "  <inserted key='new2'/>"
				, "  <inserted key='new3'/>"
				, "</element></root>"
				), Arrays.asList(lines));
	}
	
	@Test
	public void attrDifferentOrderLeftRight() throws XMLStreamException, IOException {
		String[] inputLeft = {
				  "<food>"
				, "   <fruits>"
				, "     <fruit attr='1'/>"
				, "     <fruit attr='1'/>"
				, "     <fruit attr='1'/>"
				, "     <fruit attr='1'/>"
				, "     <fruit attr='1'/>"

				, "     <apple a='1'/>"
				, "     <banana b='2'/>"
				, "     <orange o='3'/>"
				
				, "     <fruit attr='1'/>"
				, "     <fruit attr='1'/>"
				, "     <fruit attr='1'/>"
				, "     <fruit attr='1'/>"
				, "     <fruit attr='1'/>"
				, "   </fruits>"
				, "</food>"
		};
		String[] inputRight = {
				"<food>"
				, "   <fruits>"
				, "     <fruit attr='1'/>"
				, "     <orange o='30'/>"
				, "     <fruit attr='1'/>"
				, "     <fruit attr='1'/>"
				, "     <fruit attr='1'/>"
				, "     <fruit attr='1'/>"
				, "     <apple a='10'/>"
				, "     <fruit attr='1'/>"
				, "     <fruit attr='1'/>"
				, "     <fruit attr='1'/>"
				, "     <fruit attr='1'/>"
				, "     <fruit attr='1'/>"
				, "     <banana b='20'/>"
				, "   </fruits>"
				, "</food>"
		};
		
		XmlStruct merged = merger.merge(IO.asString(inputLeft), IO.asString(inputRight), false, callback(/*add*/ true, /*remove*/ true, /*attr*/ true), stop);
		String[] lines = IO.readLines(new CharSequenceReader(merged.data)); 
		
		checkList(Arrays.asList(
						  "<food>"
						, "   <fruits>"
						, "     <fruit attr='1'/>"
						, "     <fruit attr='1'/>"
						, "     <fruit attr='1'/>"
						, "     <fruit attr='1'/>"
						, "     <fruit attr='1'/>"
						
						, "     <apple a='10'/>"
						, "     <banana b='20'/>"
						, "     <orange o='30'/>"
						
						, "     <fruit attr='1'/>"
						, "     <fruit attr='1'/>"
						, "     <fruit attr='1'/>"
						, "     <fruit attr='1'/>"
						, "     <fruit attr='1'/>"
						, "   </fruits>"
						, "</food>"

				), Arrays.asList(lines));
	}
}
