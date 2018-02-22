package com.dynatrace.profilediff;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.junit.Assert;
import org.junit.Test;

import com.dynatrace.profilediff.XmlMerger3.IncludeElementCallback;
import com.dynatrace.profilediff.lib.CharSequenceReader;

public class XmlMergerTest extends DifferMergerTestBase {
	
	public XmlMergerTest(XmlMerger3Factory xmlMerger3Factory,
			String mergerName, XmlDifferFactory xmlDifferFactory,
			String differName, int stringMetricDifferThreshold) {
		super(xmlMerger3Factory, mergerName, xmlDifferFactory, differName, stringMetricDifferThreshold);
	}

	private static final List<String> ignoreAttributeNames = Collections.emptyList();
	
	static XmlMerger3.IncludeElementCallback callback(boolean add, boolean remove, boolean replaceAttributes) {
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
				return replaceAttributes;
			}
		};
	}

	@Test
	public void stuffAdded() throws XMLStreamException, IOException {
		String[] inputLeft = {
				"<root a='1' attr1='only present in original document'>"
				, "  <level1 b='2'>"
				, "    <level2 c='3'>"
				, "      <level3 d='4' attr2='only present in original document'/>"
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
				"<root a='1' attr1='different in changed document'>"
				, "  <level1 b='2'>"
				, "    <level2 c='3'>"
				, "      <level3 d='4' attr1='different in changed document'>"
				, "        <level4 e='NEW#1'/>"
				, "        <level4 e='NEW#1a'/>"
				, "      </level3>"
				, "      <level3 d='5'/>"
				, "      <level3 d='6'>"
				, "        <level4 e='1'/>"
				, "        <level4 e='NEW#2'/>"
				, "        <level4 e='NEW#3'/>"
				, "      </level3>"
				, "    </level2>"
				, "    <level2 e='NEW#4'>"
				, "      <level3/>"
				, "    </level2>"
				, "  </level1>"
				, "  <level1 b='20'/>"
				, "  <level1 b='NEW#5'/>"
				, "</root>"
		};
		
		List<String> discriminatingAttributes = (Arrays.asList("b", "c", "d", "e"));
		
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		
		XmlDiffer differ = xmlDifferFactory.create(ignoreAttributeNames);
		
		XmlMerger3 merger = xmlMerger3Factory.create(lexer, differ);
		
		XmlStruct merged = merger.merge(IO.asString(inputLeft), IO.asString(inputRight), false, callback(/*add*/ true, /*remove*/ true, /*replaceAttributes*/ false), stop);
		String[] lines = IO.readLines(new CharSequenceReader(merged.data));
		
		checkList(Arrays.asList(
				  "<root a='1' attr1='only present in original document'>"
				, "  <level1 b='2'>"
				, "    <level2 c='3'>"
				, "      <level3 d='4' attr1='different in changed document'>"
				, "        <level4 e='NEW#1'/>"
				, "        <level4 e='NEW#1a'/>"
				, "      </level3>"
				, "      <level3 d='5'/>"
				, "      <level3 d='6'>"
				, "        <level4 e='1'/>"
				, "        <level4 e='NEW#2'/>"
				, "        <level4 e='NEW#3'/>"
				, "      </level3>"
				, "    </level2>"
				, "    <level2 e='NEW#4'>"
				, "      <level3/>"
				, "    </level2>"
				, "  </level1>"
				, "  <level1 b='20'/>"
				, "  <level1 b='NEW#5'/>"
				, "</root>"
				), Arrays.asList(lines));
		
		merged = merger.merge(IO.asString(inputLeft), IO.asString(inputRight), true, callback(/*add*/ true, /*remove*/ true, /*replaceAttributes*/ false), stop);
		lines = IO.readLines(new CharSequenceReader(merged.data));
		
		checkList(Arrays.asList(
				  "<root a='1' attr1='only present in original document'>"
				, "  <level1 b='2'>"
				, "    <level2 c='3'>"
				, "      <level3 d='4' attr1='different in changed document'>"
				, "<!-- @xmlmerge: lines [5:5] inserted  (empty parent replaced) -->"
				, "        <level4 e='NEW#1'/>"
				, "<!-- @xmlmerge: lines [6:6] inserted -->"
				, "        <level4 e='NEW#1a'/>"
				, "      </level3>"
				, "      <level3 d='5'/>"
				, "      <level3 d='6'>"
				, "        <level4 e='1'/>"
				, "<!-- @xmlmerge: lines [11:11] inserted -->"
				, "        <level4 e='NEW#2'/>"
				, "<!-- @xmlmerge: lines [12:12] inserted -->"
				, "        <level4 e='NEW#3'/>"
				, "      </level3>"
				, "    </level2>"
				, "<!-- @xmlmerge: lines [15:17] inserted -->"
				, "    <level2 e='NEW#4'>"
				, "      <level3/>"
				, "    </level2>"
				, "  </level1>"
				, "  <level1 b='20'/>"
				, "<!-- @xmlmerge: lines [20:20] inserted -->"
				, "  <level1 b='NEW#5'/>"
				, "</root>"
				), Arrays.asList(lines));
	}
	
	@Test
	public void stuffAdded2() throws XMLStreamException, IOException {
		String[] inputLeft = {
				"<root a='1' attr1='only present in original document'>"
				, "  <level1 b='2'>"
				, "    <level2 c='3'>"
				, "      <level3 d='4' attr2='only present in original document'/>"
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
				"<root a='1' attr1='different in changed document'>"
				, "  <level1 b='2'>"
				, "    <level2 c='3'>"
				, "      <level3 d='4' attr1='different in changed document'>"
				, "        <level4 e='NEW#1'/>"
				, "      </level3>"
				, "      <level3 d='5'/>"
				, "      <level3 d='6'>"
				, "        <level4 e='1'/>"
				, "        <level4 e='NEW#2'>"
				, "				<level5/>"
				, "				<level5/>"
				, "        </level4>"
				, "        <level4 e='NEW#3'>"
				, "				<level5/>"
				, "        </level4>"
				, "      </level3>"
				, "    </level2>"
				, "    <level2 e='NEW#4'>"
				, "      <level3/>"
				, "    </level2>"
				, "  </level1>"
				, "  <level1 b='20'/>"
				, "  <level1 b='NEW#5'/>"
				, "</root>"
		};
		
		List<String> discriminatingAttributes = (Arrays.asList("b", "c", "d", "e"));
		
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		XmlDiffer differ = xmlDifferFactory.create(ignoreAttributeNames);
		XmlMerger3 merger = xmlMerger3Factory.create(lexer, differ);
		
		XmlStruct merged = merger.merge(IO.asString(inputLeft), IO.asString(inputRight), false, callback(/*add*/ true, /*remove*/ true, /*replaceAttributes*/ false), stop);
		String[] lines = IO.readLines(new CharSequenceReader(merged.data));
		
		checkList(Arrays.asList(
				  "<root a='1' attr1='only present in original document'>"
				, "  <level1 b='2'>"
				, "    <level2 c='3'>"
				, "      <level3 d='4' attr1='different in changed document'>"
				, "        <level4 e='NEW#1'/>"
				, "      </level3>"
				, "      <level3 d='5'/>"
				, "      <level3 d='6'>"
				, "        <level4 e='1'/>"
				, "        <level4 e='NEW#2'>"
				, "				<level5/>"
				, "				<level5/>"
				, "        </level4>"
				, "        <level4 e='NEW#3'>"
				, "				<level5/>"
				, "        </level4>"
				, "      </level3>"
				, "    </level2>"
				, "    <level2 e='NEW#4'>"
				, "      <level3/>"
				, "    </level2>"
				, "  </level1>"
				, "  <level1 b='20'/>"
				, "  <level1 b='NEW#5'/>"
				, "</root>"
				), Arrays.asList(lines));
	}

	
	@Test
	public void stuffAddedAndRemoved() throws XMLStreamException, IOException {
		String[] inputLeft = {
				  "<!--01--><root a='1' attr1='only present in original document'>"
				, "<!--02-->  <level1 b='2'>"
				, "<!--03-->    <level2 c='3'>"
				, "<!--04-->      <level3 d='4' attr2='only present in original document'/>"
				, "<!--05-->      <level3 d='5'/>"
				, "<!--06-->      <level3 d='6' b='NOT-TO-BE-SEEN-IN-RESULT'>"
				, "<!--07-->        <level4 e='1' b='NOT-TO-BE-SEEN-IN-RESULT'/>"
				, "<!--08-->      </level3>"
				, "<!--09-->    </level2>"
				, "<!--10-->  </level1>"
				, "<!--11-->  <level1 b='NOT-TO-BE-SEEN-IN-RESULT'/>"
				, "<!--12--></root>"
		};
		String[] inputRight = {
				  "<!--01--><root a='1'>"
				, "<!--02-->  <level1 b='2'>"
				, "<!--03-->    <level2 c='3'>"
				, "<!--04-->      <level3 d='4'>"
				, "<!--05-->        <level4 e='NEW#1'/>"
				, "<!--06-->      </level3>"
				, "<!--07-->      <level3 d='5'/>"
//				, "<!--07-->      <level3 d='6'>"  // REMOVED
//				, "<!--07-->        <level4 e='1'/>"
//				, "<!--07-->      </level3>"
				, "<!--08-->    </level2>"
				, "<!--09-->    <level2 e='NEW#2'>"
				, "<!--10-->      <level3/>"
				, "<!--11-->    </level2>"
				, "<!--12-->  </level1>"
//				, "<!--12-->  <level1 b='20'/>" // REMOVED
				, "<!--13--></root>"
		};
		
		List<String> discriminatingAttributes = (Arrays.asList("b", "c", "d", "e"));
		
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		XmlDiffer differ = xmlDifferFactory.create(ignoreAttributeNames);
		
		XmlMerger3 merger = xmlMerger3Factory.create(lexer, differ);
		XmlStruct merged = merger.merge(IO.asString(inputLeft), IO.asString(inputRight), false, callback(/*add*/ true, /*remove*/ true, /*replaceAttributes*/ false), stop);
		String[] lines = IO.readLines(new CharSequenceReader(merged.data));
		
		checkList(Arrays.asList(
				  "<!--01--><root a='1' attr1='only present in original document'>"
				, "<!--02-->  <level1 b='2'>"
				, "<!--03-->    <level2 c='3'>"
				, "<!--04-->      <level3 d='4'>"
				, "<!--05-->        <level4 e='NEW#1'/>"
				, "<!--06-->      </level3>"
				, "<!--05-->      <level3 d='5'/>"
				, "<!--09-->    </level2>"
				, "<!--09-->    <level2 e='NEW#2'>"
				, "<!--10-->      <level3/>"
				, "<!--11-->    </level2>"
				, "<!--10-->  </level1>"
				, "<!--12--></root>"
				), Arrays.asList(lines));
		
		merged = merger.merge(IO.asString(inputLeft), IO.asString(inputRight), false, callback(/*add*/ true, /*remove*/ false, /*replaceAttributes*/ false), stop); // NOT REMOVING
		lines = IO.readLines(new CharSequenceReader(merged.data));
		
		checkList(Arrays.asList(
				  "<!--01--><root a='1' attr1='only present in original document'>"
				, "<!--02-->  <level1 b='2'>"
				, "<!--03-->    <level2 c='3'>"
				, "<!--04-->      <level3 d='4'>"
				, "<!--05-->        <level4 e='NEW#1'/>"
				, "<!--06-->      </level3>"
				, "<!--05-->      <level3 d='5'/>"
				, "<!--06-->      <level3 d='6' b='NOT-TO-BE-SEEN-IN-RESULT'>"
				, "<!--07-->        <level4 e='1' b='NOT-TO-BE-SEEN-IN-RESULT'/>"
				, "<!--08-->      </level3>"
				, "<!--09-->    </level2>"
				, "<!--09-->    <level2 e='NEW#2'>"
				, "<!--10-->      <level3/>"
				, "<!--11-->    </level2>"
				, "<!--10-->  </level1>"
				, "<!--11-->  <level1 b='NOT-TO-BE-SEEN-IN-RESULT'/>"
				, "<!--12--></root>"
				), Arrays.asList(lines));
		
		merged = merger.merge(IO.asString(inputLeft), IO.asString(inputRight), false, callback(/*add*/ false, /*remove*/ true, /*replaceAttributes*/ false), stop); // NOT ADDING
		lines = IO.readLines(new CharSequenceReader(merged.data));
		
		checkList(Arrays.asList(
				  "<!--01--><root a='1' attr1='only present in original document'>"
				, "<!--02-->  <level1 b='2'>"
				, "<!--03-->    <level2 c='3'>"
				, "<!--04-->      <level3 d='4' attr2='only present in original document'/>"
				, "<!--05-->      <level3 d='5'/>"
				, "<!--09-->    </level2>"
				, "<!--10-->  </level1>"
				, "<!--12--></root>"
				), Arrays.asList(lines));
		
		merged = merger.merge(IO.asString(inputLeft), IO.asString(inputRight), false, callback(/*add*/ false, /*remove*/ false, /*replaceAttributes*/ false), stop); // NOT ADDING NOR REMOVING
		lines = IO.readLines(new CharSequenceReader(merged.data));
			
		checkList(Arrays.asList(inputLeft), Arrays.asList(lines)); // not merged at all
	}
	
	@Test
	public void stuffRemoved() throws XMLStreamException, IOException {
		String[] inputLeft = {
				  "<!--01--><root a='1' attr1='only present in original document'>"
				, "<!--02-->  <level1 b='2'>"
				, "<!--03-->    <level2 c='3'>"
				, "<!--04-->      <level3 d='4' attr2='only present in original document'/>"
				, "<!--05-->      <level3 d='5'/>"
				, "<!--06-->      <level3 d='6' b='NOT-TO-BE-SEEN-IN-RESULT'>"
				, "<!--07-->        <level4 e='1' b='NOT-TO-BE-SEEN-IN-RESULT'/>"
				, "<!--08-->      </level3>"
				, "<!--09-->    </level2>"
				, "<!--10-->  </level1>"
				, "<!--11-->  <level1 b='NOT-TO-BE-SEEN-IN-RESULT'/>"
				, "<!--12--></root>"
		};
		String[] inputRight = {
				  "<!--01--><root a='1' attr1='only present in original document'>"
				, "<!--02-->  <level1 b='2'>"
				, "<!--03-->    <level2 c='3'>"
				, "<!--04-->      <level3 d='4' attr2='only present in original document'/>"
				, "<!--05-->      <level3 d='5'/>"
//				, "<!--06-->      <level3 d='6'>"
//				, "<!--07-->        <level4 e='1'/>"
//				, "<!--08-->      </level3>"
				, "<!--09-->    </level2>"
				, "<!--10-->  </level1>"
//				, "<!--11-->  <level1 b='20'/>"
				, "<!--12--></root>"
		};
		
		List<String> discriminatingAttributes = (Arrays.asList("b", "c", "d", "e"));
		
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		XmlDiffer differ = xmlDifferFactory.create(ignoreAttributeNames);
		XmlMerger3 merger = xmlMerger3Factory.create(lexer, differ);
		
		XmlStruct merged = merger.merge(IO.asString(inputLeft), IO.asString(inputRight), false, callback(/*add*/ true, /*remove*/ true, /*replaceAttributes*/ false), stop);
		String[] lines = IO.readLines(new CharSequenceReader(merged.data));
		
		checkList(Arrays.asList(
				  "<!--01--><root a='1' attr1='only present in original document'>"
				, "<!--02-->  <level1 b='2'>"
				, "<!--03-->    <level2 c='3'>"
				, "<!--04-->      <level3 d='4' attr2='only present in original document'/>"
				, "<!--05-->      <level3 d='5'/>"
				, "<!--09-->    </level2>"
				, "<!--10-->  </level1>"
				, "<!--12--></root>"
				), Arrays.asList(lines));
		
		merged = merger.merge(IO.asString(inputLeft), IO.asString(inputRight), true, callback(/*add*/ true, /*remove*/ true, /*replaceAttributes*/ false), stop);
		lines = IO.readLines(new CharSequenceReader(merged.data));
		
		checkList(Arrays.asList(
				  "<!--01--><root a='1' attr1='only present in original document'>"
				, "<!--02-->  <level1 b='2'>"
				, "<!--03-->    <level2 c='3'>"
				, "<!--04-->      <level3 d='4' attr2='only present in original document'/>"
				, "<!--05-->      <level3 d='5'/>"
				, "<!-- @xmlmerge: lines [6:8] removed -->"
				, "<!--09-->    </level2>"
				, "<!--10-->  </level1>"
				, "<!-- @xmlmerge: lines [11:11] removed -->"
				, "<!--12--></root>"
				), Arrays.asList(lines));
	}
	
	@Test
	public void stuffRemovedWithEvilComments() throws XMLStreamException, IOException {
		String[] inputLeft = {
				  "<!--01--><root a='1' attr1='only present in original document'>"
				, "<!--02-->  <level1 b='2'>"
				, "<!--03-->    <level2 c='3'>"
				, "<!--04-->      <level3 d='4' attr2='only present in original document'/>"
				, "<!--05-->      <level3 d='5'/>"
				, "<!--06-->      <level3 d='6' b='NOT-TO-BE-SEEN-IN-RESULT'>"
				, "<!--07-->        <level4 e='1' b='NOT-TO-BE-SEEN-IN-RESULT'/>"
				, "<!--08-->      </level3>"
				, "<!--09-->    </level2>"
				, "           </level1> "
				, "              <level1 b='NOT-TO-BE-SEEN-IN-RESULT1'/> <!--"
				, " something inside comment"
				, " -->"
				, " <!--hello-->"
				, "         </root>"
		};
		String[] inputRight = {
				"<!--01--><root a='1' attr1='only present in original document'>"
				, "<!--02-->  <level1 b='2'>"
				, "<!--03-->    <level2 c='3'>"
				, "<!--04-->      <level3 d='4' attr2='only present in original document'/>"
				, "<!--05-->      <level3 d='5'/>"
//				, "<!--06-->      <level3 d='6'>"
//				, "<!--07-->        <level4 e='1'/>"
//				, "<!--08-->      </level3>"
				, "<!--09-->    </level2>"
				, "<!--10-->  </level1>"
//				, "<!--11-->  <level1 b='20'/>"
				, "<!--12--></root>"
		};
		
		List<String> discriminatingAttributes = (Arrays.asList("b", "c", "d", "e"));
		
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		XmlDiffer differ = xmlDifferFactory.create(ignoreAttributeNames);
		XmlMerger3 merger = xmlMerger3Factory.create(lexer, differ);
		
		XmlStruct merged = merger.merge(IO.asString(inputLeft), IO.asString(inputRight), false, callback(/*add*/ true, /*remove*/ true, /*replaceAttributes*/ false), stop);
		String[] lines = IO.readLines(new CharSequenceReader(merged.data));
		
		checkList(Arrays.asList(
				  "<!--01--><root a='1' attr1='only present in original document'>"
				, "<!--02-->  <level1 b='2'>"
				, "<!--03-->    <level2 c='3'>"
				, "<!--04-->      <level3 d='4' attr2='only present in original document'/>"
				, "<!--05-->      <level3 d='5'/>"
				, "<!--09-->    </level2>"
				, "           </level1> <!--"
				, " something inside comment"
				, " -->"
				, " <!--hello-->"
				, "         </root>"
				), Arrays.asList(lines));
	}
	
	@Test
	public void stuffAddedIntoParentAsFirst() throws XMLStreamException, IOException {
		String[] inputLeft = {
			      "<measure isapplicationaggregated=\"false\" errorseverity=\"none\" servicecontext=\"SERVER\" messagematch=\"contains\" userdefined=\"false\" message=\"\" id=\"Count\" calculatepercentiles=\"false\" createdtimestamp=\"1414677064931\" errortype=\"none\" rate=\"purepath\" description=\"Number of thrown Exceptions per PurePath (optionally filtered by a throwable class).\" ischartable=\"true\" metricid=\"Count\" measuretype=\"ExceptionMeasure\" isaggregated=\"false\" throwableclassmath=\"contains\" metricgroupid=\"Exceptions\" displayaggregations=\"31\" calculatebaseline=\"false\" throwableclass=\"\" exceptiontype=\"captured_exception\" displayunit=\"number\">"
			      , "  <color color.blue=\"64\" color.green=\"0\" color.red=\"64\" />"
			      , "</measure>"
		};
		String[] inputRight = {
			      "<measure isapplicationaggregated=\"false\" errorseverity=\"none\" servicecontext=\"SERVER\" messagematch=\"contains\" userdefined=\"false\" message=\"\" id=\"Count\" calculatepercentiles=\"false\" createdtimestamp=\"1414677142108\" errortype=\"none\" rate=\"purepath\" description=\"Number of thrown Exceptions per PurePath (optionally filtered by a throwable class).\" ischartable=\"true\" metricid=\"Count\" measuretype=\"ExceptionMeasure\" isaggregated=\"false\" throwableclassmath=\"contains\" metricgroupid=\"Exceptions\" displayaggregations=\"31\" calculatebaseline=\"false\" throwableclass=\"\" exceptiontype=\"captured_exception\" displayunit=\"number\">"
			      , "  <thresholds threshold.upper.severe=\"5.0\" threshold.lower.warning=\"0.0\" threshold.lower.severe=\"0.0\" threshold.upper.warning=\"5.0\" />"
			      , "  <color color.blue=\"64\" color.green=\"0\" color.red=\"64\" />"
			      , "</measure>"
		};
		
		List<String> discriminatingAttributes = (Arrays.asList("id"));
		
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		XmlDiffer differ = xmlDifferFactory.create(ignoreAttributeNames);
		XmlMerger3 merger = xmlMerger3Factory.create(lexer, differ);
		
		XmlStruct merged = merger.merge(IO.asString(inputLeft), IO.asString(inputRight), false, callback(/*add*/ true, /*remove*/ true, /*replaceAttributes*/ false), stop);
		String[] lines = IO.readLines(new CharSequenceReader(merged.data));
		
		checkList(Arrays.asList(
				  "<measure isapplicationaggregated=\"false\" errorseverity=\"none\" servicecontext=\"SERVER\" messagematch=\"contains\" userdefined=\"false\" message=\"\" id=\"Count\" calculatepercentiles=\"false\" createdtimestamp=\"1414677064931\" errortype=\"none\" rate=\"purepath\" description=\"Number of thrown Exceptions per PurePath (optionally filtered by a throwable class).\" ischartable=\"true\" metricid=\"Count\" measuretype=\"ExceptionMeasure\" isaggregated=\"false\" throwableclassmath=\"contains\" metricgroupid=\"Exceptions\" displayaggregations=\"31\" calculatebaseline=\"false\" throwableclass=\"\" exceptiontype=\"captured_exception\" displayunit=\"number\">"
				, "  <thresholds threshold.upper.severe=\"5.0\" threshold.lower.warning=\"0.0\" threshold.lower.severe=\"0.0\" threshold.upper.warning=\"5.0\" />"
				, "  <color color.blue=\"64\" color.green=\"0\" color.red=\"64\" />"
				, "</measure>"
				), Arrays.asList(lines));
	}
	
	@Test
	public void stuffAddedAdterPredAsSecond() throws XMLStreamException, IOException {
		String[] inputLeft = {
				"<measure isapplicationaggregated=\"false\" errorseverity=\"none\" servicecontext=\"SERVER\" messagematch=\"contains\" userdefined=\"false\" message=\"\" id=\"Count\" calculatepercentiles=\"false\" createdtimestamp=\"1414677064931\" errortype=\"none\" rate=\"purepath\" description=\"Number of thrown Exceptions per PurePath (optionally filtered by a throwable class).\" ischartable=\"true\" metricid=\"Count\" measuretype=\"ExceptionMeasure\" isaggregated=\"false\" throwableclassmath=\"contains\" metricgroupid=\"Exceptions\" displayaggregations=\"31\" calculatebaseline=\"false\" throwableclass=\"\" exceptiontype=\"captured_exception\" displayunit=\"number\">"
				, "  <color color.blue=\"64\" color.green=\"0\" color.red=\"64\" />"
				, "</measure>"
		};
		String[] inputRight = {
				"<measure isapplicationaggregated=\"false\" errorseverity=\"none\" servicecontext=\"SERVER\" messagematch=\"contains\" userdefined=\"false\" message=\"\" id=\"Count\" calculatepercentiles=\"false\" createdtimestamp=\"1414677142108\" errortype=\"none\" rate=\"purepath\" description=\"Number of thrown Exceptions per PurePath (optionally filtered by a throwable class).\" ischartable=\"true\" metricid=\"Count\" measuretype=\"ExceptionMeasure\" isaggregated=\"false\" throwableclassmath=\"contains\" metricgroupid=\"Exceptions\" displayaggregations=\"31\" calculatebaseline=\"false\" throwableclass=\"\" exceptiontype=\"captured_exception\" displayunit=\"number\">"
				, "  <color color.blue=\"64\" color.green=\"0\" color.red=\"64\" />"
				, "  <thresholds threshold.upper.severe=\"5.0\" threshold.lower.warning=\"0.0\" threshold.lower.severe=\"0.0\" threshold.upper.warning=\"5.0\" />"
				, "</measure>"
				
		};
		
		List<String> discriminatingAttributes = (Arrays.asList("id"));
		
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		XmlDiffer differ = xmlDifferFactory.create(ignoreAttributeNames);
		XmlMerger3 merger = xmlMerger3Factory.create(lexer, differ);
		
		XmlStruct merged = merger.merge(IO.asString(inputLeft), IO.asString(inputRight), false, callback(/*add*/ true, /*remove*/ true, /*replaceAttributes*/ false), stop);
		String[] lines = IO.readLines(new CharSequenceReader(merged.data));
		
		checkList(Arrays.asList(
				"<measure isapplicationaggregated=\"false\" errorseverity=\"none\" servicecontext=\"SERVER\" messagematch=\"contains\" userdefined=\"false\" message=\"\" id=\"Count\" calculatepercentiles=\"false\" createdtimestamp=\"1414677064931\" errortype=\"none\" rate=\"purepath\" description=\"Number of thrown Exceptions per PurePath (optionally filtered by a throwable class).\" ischartable=\"true\" metricid=\"Count\" measuretype=\"ExceptionMeasure\" isaggregated=\"false\" throwableclassmath=\"contains\" metricgroupid=\"Exceptions\" displayaggregations=\"31\" calculatebaseline=\"false\" throwableclass=\"\" exceptiontype=\"captured_exception\" displayunit=\"number\">"
				, "  <color color.blue=\"64\" color.green=\"0\" color.red=\"64\" />"
				, "  <thresholds threshold.upper.severe=\"5.0\" threshold.lower.warning=\"0.0\" threshold.lower.severe=\"0.0\" threshold.upper.warning=\"5.0\" />"
				, "</measure>"
				), Arrays.asList(lines));
	}
	
	@Test
	public void stuffAddedAdterPredAsThird() throws XMLStreamException, IOException {
		String[] inputLeft = {
				"<measure isapplicationaggregated=\"false\" errorseverity=\"none\" servicecontext=\"SERVER\" messagematch=\"contains\" userdefined=\"false\" message=\"\" id=\"Count\" calculatepercentiles=\"false\" createdtimestamp=\"1414677064931\" errortype=\"none\" rate=\"purepath\" description=\"Number of thrown Exceptions per PurePath (optionally filtered by a throwable class).\" ischartable=\"true\" metricid=\"Count\" measuretype=\"ExceptionMeasure\" isaggregated=\"false\" throwableclassmath=\"contains\" metricgroupid=\"Exceptions\" displayaggregations=\"31\" calculatebaseline=\"false\" throwableclass=\"\" exceptiontype=\"captured_exception\" displayunit=\"number\">"
				, "  <color color.blue=\"64\" color.green=\"0\" color.red=\"64\" />"
				, "  <color color.blue=\"64\" color.green=\"0\" color.red=\"64\" />"
				, "</measure>"
		};
		String[] inputRight = {
				"<measure isapplicationaggregated=\"false\" errorseverity=\"none\" servicecontext=\"SERVER\" messagematch=\"contains\" userdefined=\"false\" message=\"\" id=\"Count\" calculatepercentiles=\"false\" createdtimestamp=\"1414677142108\" errortype=\"none\" rate=\"purepath\" description=\"Number of thrown Exceptions per PurePath (optionally filtered by a throwable class).\" ischartable=\"true\" metricid=\"Count\" measuretype=\"ExceptionMeasure\" isaggregated=\"false\" throwableclassmath=\"contains\" metricgroupid=\"Exceptions\" displayaggregations=\"31\" calculatebaseline=\"false\" throwableclass=\"\" exceptiontype=\"captured_exception\" displayunit=\"number\">"
				, "  <color color.blue=\"64\" color.green=\"0\" color.red=\"64\" />"
				, "  <color color.blue=\"64\" color.green=\"0\" color.red=\"64\" />"
				, "  <thresholds threshold.upper.severe=\"5.0\" threshold.lower.warning=\"0.0\" threshold.lower.severe=\"0.0\" threshold.upper.warning=\"5.0\" />"
				, "</measure>"
				
		};
		
		List<String> discriminatingAttributes = (Arrays.asList("id"));
		
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		XmlDiffer differ = xmlDifferFactory.create(ignoreAttributeNames);
		XmlMerger3 merger = xmlMerger3Factory.create(lexer, differ);
		
		XmlStruct merged = merger.merge(IO.asString(inputLeft), IO.asString(inputRight), false, callback(/*add*/ true, /*remove*/ true, /*replaceAttributes*/ false), stop);
		String[] lines = IO.readLines(new CharSequenceReader(merged.data));
		
		checkList(Arrays.asList(
				"<measure isapplicationaggregated=\"false\" errorseverity=\"none\" servicecontext=\"SERVER\" messagematch=\"contains\" userdefined=\"false\" message=\"\" id=\"Count\" calculatepercentiles=\"false\" createdtimestamp=\"1414677064931\" errortype=\"none\" rate=\"purepath\" description=\"Number of thrown Exceptions per PurePath (optionally filtered by a throwable class).\" ischartable=\"true\" metricid=\"Count\" measuretype=\"ExceptionMeasure\" isaggregated=\"false\" throwableclassmath=\"contains\" metricgroupid=\"Exceptions\" displayaggregations=\"31\" calculatebaseline=\"false\" throwableclass=\"\" exceptiontype=\"captured_exception\" displayunit=\"number\">"
				, "  <color color.blue=\"64\" color.green=\"0\" color.red=\"64\" />"
				, "  <color color.blue=\"64\" color.green=\"0\" color.red=\"64\" />"
				, "  <thresholds threshold.upper.severe=\"5.0\" threshold.lower.warning=\"0.0\" threshold.lower.severe=\"0.0\" threshold.upper.warning=\"5.0\" />"
				, "</measure>"
				), Arrays.asList(lines));
	}
	
	@Test
	public void conflictsWithAmbiguousParents() throws XMLStreamException, IOException {
		String[] inputLeft = {
				  "    <sensorgroups>"
				, "      <sensorgroup>"
				, "        <sensor featurehash=\"\" order=\"0\" description=\"App\" hint=\"Default Sensor Group for Java Method Sensor Rules\" jssensorttype=\"\" group=\"Methods\" type=\"java\" autoplace=\"true\" userdefined=\"false\" defaultorder=\"0\" key=\"com.dynatrace.diagnostics.sensorgroup.method.Java.b34876b4c508\" />"
				, "      </sensorgroup>"
				, "      <sensorgroup>"
				, "        <sensor featurehash=\"\" order=\"0\" description=\"App\" hint=\"Default Sensor Group for Java Memory Sensor Rules\" jssensorttype=\"\" group=\"Memory\" type=\"heap\" autoplace=\"true\" userdefined=\"false\" defaultorder=\"0\" key=\"com.dynatrace.diagnostics.sensorgroup.memory.Java.b34876b4c50a\" />"
				, "      </sensorgroup>"
				, "      <sensorgroup>"
				, "        <sensor featurehash=\"\" order=\"0\" description=\"App\" hint=\"Default Sensor Group for .NET Method Sensor Rules\" jssensorttype=\"\" group=\"Methods\" type=\"dotnet\" autoplace=\"true\" userdefined=\"false\" defaultorder=\"0\" key=\"com.dynatrace.diagnostics.sensorgroup.method..NET.b34876b4c509\" />"
				, "      </sensorgroup>"
				, "      <sensorgroup>"
				, "        <sensor featurehash=\"\" order=\"0\" description=\"App\" hint=\"Default Sensor Group for .NET Memory Sensor Rules\" jssensorttype=\"\" group=\"Memory\" type=\"dotnetheap\" autoplace=\"true\" userdefined=\"false\" defaultorder=\"0\" key=\"com.dynatrace.diagnostics.sensorgroup.memory..NET.b34876b4c50b\" />"
				, "      </sensorgroup>"
				, "      <sensorgroup>"
				, "        <sensor featurehash=\"\" order=\"0\" description=\"App\" hint=\"Default Sensor Group for PHP Sensor Rules\" jssensorttype=\"\" group=\"PHP\" type=\"php\" autoplace=\"true\" userdefined=\"false\" defaultorder=\"0\" key=\"com.dynatrace.diagnostics.sensorgroup.php.PHP.b34876b4c50c\" />"
				, "      </sensorgroup>"
				, "    </sensorgroups>"
		};
		String[] inputRight = {
				  "    <sensorgroups>"
				, "      <sensorgroup>"
				, "        <sensor featurehash=\"\" order=\"0\" description=\"App\" hint=\"Default Sensor Group for Java Method Sensor Rules\" jssensorttype=\"\" group=\"Methods\" type=\"java\" autoplace=\"true\" userdefined=\"false\" defaultorder=\"0\" key=\"com.dynatrace.diagnostics.sensorgroup.method.Java.b34876b4c508\" />"
				, "      </sensorgroup>"
				, "      <sensorgroup>"
				, "        <sensor featurehash=\"\" order=\"0\" description=\"App\" hint=\"Default Sensor Group for PHP Sensor Rules\" jssensorttype=\"\" group=\"PHP\" type=\"php\" autoplace=\"true\" userdefined=\"false\" defaultorder=\"0\" key=\"MYNEWSENSOR-CORRECTLY-ADDED\" />"
				, "      </sensorgroup>"
				, "      <sensorgroup>"
				, "        <sensor featurehash=\"\" order=\"0\" description=\"App\" hint=\"Default Sensor Group for Java Memory Sensor Rules\" jssensorttype=\"\" group=\"Memory\" type=\"heap\" autoplace=\"true\" userdefined=\"false\" defaultorder=\"0\" key=\"com.dynatrace.diagnostics.sensorgroup.memory.Java.b34876b4c50a\" />"
				, "      </sensorgroup>"
				, "      <sensorgroup>"
				, "        <sensor featurehash=\"\" order=\"0\" description=\"App\" hint=\"Default Sensor Group for .NET Method Sensor Rules\" jssensorttype=\"\" group=\"Methods\" type=\"dotnet\" autoplace=\"true\" userdefined=\"false\" defaultorder=\"0\" key=\"com.dynatrace.diagnostics.sensorgroup.method..NET.b34876b4c509\" />"
				, "      </sensorgroup>"
				, "      <sensorgroup>"
				, "        <sensor featurehash=\"\" order=\"0\" description=\"App\" hint=\"Default Sensor Group for .NET Memory Sensor Rules\" jssensorttype=\"\" group=\"Memory\" type=\"dotnetheap\" autoplace=\"true\" userdefined=\"false\" defaultorder=\"0\" key=\"com.dynatrace.diagnostics.sensorgroup.memory..NET.b34876b4c50b\" />"
				, "      </sensorgroup>"
				, "      <sensorgroup>"
				, "        <sensor featurehash=\"\" order=\"0\" description=\"App\" hint=\"Default Sensor Group for PHP Sensor Rules\" jssensorttype=\"\" group=\"PHP\" type=\"php\" autoplace=\"true\" userdefined=\"false\" defaultorder=\"0\" key=\"MYNEWSENSOR\" />"
				, "        <sensor featurehash=\"\" order=\"0\" description=\"App\" hint=\"Default Sensor Group for PHP Sensor Rules\" jssensorttype=\"\" group=\"PHP\" type=\"php\" autoplace=\"true\" userdefined=\"false\" defaultorder=\"0\" key=\"MYNEWSENSOR2\" />"
				, "      </sensorgroup>"
				, "      <sensorgroup>"
				, "        <sensor featurehash=\"\" order=\"0\" description=\"App\" hint=\"Default Sensor Group for PHP Sensor Rules\" jssensorttype=\"\" group=\"PHP\" type=\"php\" autoplace=\"true\" userdefined=\"false\" defaultorder=\"0\" key=\"com.dynatrace.diagnostics.sensorgroup.php.PHP.b34876b4c50c\" />"
				, "        <sensor featurehash=\"\" order=\"0\" description=\"App\" hint=\"Default Sensor Group for PHP Sensor Rules\" jssensorttype=\"\" group=\"PHP\" type=\"php\" autoplace=\"true\" userdefined=\"false\" defaultorder=\"0\" key=\"MYNEWSENSOR3\" />"
				, "      </sensorgroup>"
				, "    </sensorgroups>"				
		};
		
		List<String> discriminatingAttributes = (Arrays.asList("key"));
		
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		XmlDiffer differ = xmlDifferFactory.create(ignoreAttributeNames);
		XmlMerger3 merger = xmlMerger3Factory.create(lexer, differ);
		
		XmlStruct merged = merger.merge(IO.asString(inputLeft), IO.asString(inputRight), false, callback(/*add*/ true, /*remove*/ true, /*replaceAttributes*/ false), stop);
		String[] lines = IO.readLines(new CharSequenceReader(merged.data));
		
		checkList(Arrays.asList(
				  "    <sensorgroups>"
				, "      <sensorgroup>"
				, "        <sensor featurehash=\"\" order=\"0\" description=\"App\" hint=\"Default Sensor Group for Java Method Sensor Rules\" jssensorttype=\"\" group=\"Methods\" type=\"java\" autoplace=\"true\" userdefined=\"false\" defaultorder=\"0\" key=\"com.dynatrace.diagnostics.sensorgroup.method.Java.b34876b4c508\" />"
				, "      </sensorgroup>"
				, "      <sensorgroup>"
				, "        <sensor featurehash=\"\" order=\"0\" description=\"App\" hint=\"Default Sensor Group for PHP Sensor Rules\" jssensorttype=\"\" group=\"PHP\" type=\"php\" autoplace=\"true\" userdefined=\"false\" defaultorder=\"0\" key=\"MYNEWSENSOR-CORRECTLY-ADDED\" />"
				, "      </sensorgroup>"
				, "      <sensorgroup>"
				, "        <sensor featurehash=\"\" order=\"0\" description=\"App\" hint=\"Default Sensor Group for Java Memory Sensor Rules\" jssensorttype=\"\" group=\"Memory\" type=\"heap\" autoplace=\"true\" userdefined=\"false\" defaultorder=\"0\" key=\"com.dynatrace.diagnostics.sensorgroup.memory.Java.b34876b4c50a\" />"
				, "      </sensorgroup>"
				, "      <sensorgroup>"
				, "        <sensor featurehash=\"\" order=\"0\" description=\"App\" hint=\"Default Sensor Group for .NET Method Sensor Rules\" jssensorttype=\"\" group=\"Methods\" type=\"dotnet\" autoplace=\"true\" userdefined=\"false\" defaultorder=\"0\" key=\"com.dynatrace.diagnostics.sensorgroup.method..NET.b34876b4c509\" />"
				, "      </sensorgroup>"
				, "      <sensorgroup>"
				, "        <sensor featurehash=\"\" order=\"0\" description=\"App\" hint=\"Default Sensor Group for .NET Memory Sensor Rules\" jssensorttype=\"\" group=\"Memory\" type=\"dotnetheap\" autoplace=\"true\" userdefined=\"false\" defaultorder=\"0\" key=\"com.dynatrace.diagnostics.sensorgroup.memory..NET.b34876b4c50b\" />"
				, "      </sensorgroup>"
				, "      <sensorgroup>"
				, "        <sensor featurehash=\"\" order=\"0\" description=\"App\" hint=\"Default Sensor Group for PHP Sensor Rules\" jssensorttype=\"\" group=\"PHP\" type=\"php\" autoplace=\"true\" userdefined=\"false\" defaultorder=\"0\" key=\"MYNEWSENSOR\" />"
				, "        <sensor featurehash=\"\" order=\"0\" description=\"App\" hint=\"Default Sensor Group for PHP Sensor Rules\" jssensorttype=\"\" group=\"PHP\" type=\"php\" autoplace=\"true\" userdefined=\"false\" defaultorder=\"0\" key=\"MYNEWSENSOR2\" />"
				, "      </sensorgroup>"
				, "      <sensorgroup>"
				, "        <sensor featurehash=\"\" order=\"0\" description=\"App\" hint=\"Default Sensor Group for PHP Sensor Rules\" jssensorttype=\"\" group=\"PHP\" type=\"php\" autoplace=\"true\" userdefined=\"false\" defaultorder=\"0\" key=\"com.dynatrace.diagnostics.sensorgroup.php.PHP.b34876b4c50c\" />"
				, "        <sensor featurehash=\"\" order=\"0\" description=\"App\" hint=\"Default Sensor Group for PHP Sensor Rules\" jssensorttype=\"\" group=\"PHP\" type=\"php\" autoplace=\"true\" userdefined=\"false\" defaultorder=\"0\" key=\"MYNEWSENSOR3\" />"
				, "      </sensorgroup>"
				, "    </sensorgroups>"
				), Arrays.asList(lines));
	}
	
	@Test
	public void conflictsWithAmbiguousParentsDifferentGood() throws XMLStreamException, IOException {
		String[] inputLeft = {
				"    <sensorgroups attr='onlyforparent'>"
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
		
		List<String> discriminatingAttributes = (Arrays.asList("key"));
		
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		XmlDiffer differ = xmlDifferFactory.create(ignoreAttributeNames);
		XmlMerger3 merger = xmlMerger3Factory.create(lexer, differ);
		
		XmlStruct merged = merger.merge(IO.asString(inputLeft), IO.asString(inputRight), false, callback(/*add*/ true, /*remove*/ true, /*replaceAttributes*/ false), stop);
		String[] lines = IO.readLines(new CharSequenceReader(merged.data));
		
		checkList(Arrays.asList(
				  "    <sensorgroups attr='onlyforparent'>"
				, "      <sensorgroup>"
				, "        <sensor key='MyNewSensorOne'/>"
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
				), Arrays.asList(lines));
		
		merged = merger.merge(IO.asString(inputLeft), IO.asString(inputRight), false, callback(/*add*/ false, /*remove*/ true, /*replaceAttributes*/ false), stop);
		lines = IO.readLines(new CharSequenceReader(merged.data));
		
		checkList(Arrays.asList(
				  "    <sensorgroups attr='onlyforparent'>"
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
				), Arrays.asList(lines));
		
		merged = merger.merge(IO.asString(inputLeft), IO.asString(inputRight), false, callback(/*add*/ true, /*remove*/ false, /*replaceAttributes*/ false), stop);
		lines = IO.readLines(new CharSequenceReader(merged.data));
		
		checkList(Arrays.asList(
				  "    <sensorgroups attr='onlyforparent'>"
				, "      <sensorgroup>"
				, "        <sensor key='MyNewSensorOne'/>"
				, "      </sensorgroup>"
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
				), Arrays.asList(lines));
	}
	
	@Test
	public void conflictsWithAmbiguousParentsDifferentEvilHandledGood() throws XMLStreamException, IOException {
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
				, "        <sensor key='NewSensorInsideSameGroup'/>" // <- modifies the compound key of <sensorgroup>
				, "      </sensorgroup>"
				, "      <sensorgroup>"
				, "        <sensor key='sensorFour'/>"
				, "      </sensorgroup>"
				, "    </sensorgroups>"
		};
		
		List<String> discriminatingAttributes = (Arrays.asList("key"));
		
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		XmlDiffer differ = xmlDifferFactory.create(ignoreAttributeNames);
		XmlMerger3 merger = xmlMerger3Factory.create(lexer, differ);
		
		XmlStruct merged = merger.merge(IO.asString(inputLeft), IO.asString(inputRight), false, callback(/*add*/ true, /*remove*/ true, /*replaceAttributes*/ false), stop);
		String[] lines = IO.readLines(new CharSequenceReader(merged.data));
		
		checkList(Arrays.asList(
				  "    <sensorgroups>"
				, "      <sensorgroup>"
				, "        <sensor key='sensorOne'/>"
				, "      </sensorgroup>"
				, "      <sensorgroup>"
				, "        <sensor key='sensorTwo'/>"
				, "      </sensorgroup>"
				, "      <sensorgroup>"
				, "        <sensor key='sensorThree'/>"
				, "        <sensor key='NewSensorInsideSameGroup'/>"
				, "      </sensorgroup>"
				, "      <sensorgroup>"
				, "        <sensor key='sensorFour'/>"
				, "      </sensorgroup>"
				, "    </sensorgroups>"
				), Arrays.asList(lines));
	}
	
	@Test
	public void conflictsWithAmbiguousParentsDifferentEvilHandledBad() throws XMLStreamException, IOException {
		String[] inputLeft = {
				  "    <sensorgroups>"
				, "      <sensorgroup>"
				, "        <sensor key='sensorOne'/>"
				, "      </sensorgroup>"
		/*5*/	, "      <sensorgroup>"
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
		/*8*/   , "      <sensorgroup>"
				, "        <sensor key='NewOne'/>"
				, "        <sensor key='NewTwo'/>"
				, "      </sensorgroup>"
	/*12*/		, "      <sensorgroup>"
				, "        <sensor key='NewSensorInsideSameGroup'/>" // <- modifies the compound key of <sensorgroup>
				, "        <sensor key='sensorFour'/>"
				, "      </sensorgroup>"
				, "    </sensorgroups>"
		};
		
		List<String> discriminatingAttributes = (Arrays.asList("key"));
		
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		XmlDiffer differ = xmlDifferFactory.create(ignoreAttributeNames);
		XmlMerger3 merger = xmlMerger3Factory.create(lexer, differ);
		
		XmlStruct merged = merger.merge(IO.asString(inputLeft), IO.asString(inputRight), false, new IncludeElementCallback() {
			@Override
			public boolean removeElement(XmlElement element) {
				return true;
			}
			
			@Override
			public boolean addElement(XmlElement element) {
				return element.toPathString().equals("sensorgroups/sensorgroup [8:11]");
			}
		}, stop);
		String[] lines = IO.readLines(new CharSequenceReader(merged.data));
		
		checkList(Arrays.asList(
				  "    <sensorgroups>"
				, "      <sensorgroup>"
				, "        <sensor key='sensorOne'/>"
				, "      </sensorgroup>"
				, "      <sensorgroup>"
				, "        <sensor key='sensorTwo'/>"
				, "      </sensorgroup>"
				, "      <sensorgroup>"
				, "        <sensor key='NewOne'/>"
				, "        <sensor key='NewTwo'/>"
				, "      </sensorgroup>"
				, "    </sensorgroups>"
				), Arrays.asList(lines));
		
		merged = merger.merge(IO.asString(inputLeft), IO.asString(inputRight), false, new IncludeElementCallback() {
			@Override
			public boolean removeElement(XmlElement element) {
				return true;
			}
			@Override
			public boolean addElement(XmlElement element) {
				return element.toPathString().equals("sensorgroups/sensorgroup [12:15]");
			}
		}, stop);
		lines = IO.readLines(new CharSequenceReader(merged.data));
		
		checkList(Arrays.asList(
				  "    <sensorgroups>"
				, "      <sensorgroup>"
				, "        <sensor key='sensorOne'/>"
				, "      </sensorgroup>"
				, "      <sensorgroup>"
				, "        <sensor key='sensorTwo'/>"
				, "      </sensorgroup>"
				, "      <sensorgroup>"
				, "        <sensor key='NewSensorInsideSameGroup'/>"
				, "        <sensor key='sensorFour'/>"
				, "      </sensorgroup>"
				, "    </sensorgroups>"
				), Arrays.asList(lines));
		
		merged = merger.merge(IO.asString(inputLeft), IO.asString(inputRight), false, callback(/*add*/ true, /*remove*/ true, /*replaceAttributes*/ false), stop);
		lines = IO.readLines(new CharSequenceReader(merged.data));
		
		checkList(Arrays.asList(
				  "    <sensorgroups>"
				, "      <sensorgroup>"
				, "        <sensor key='sensorOne'/>"
				, "      </sensorgroup>"
				, "      <sensorgroup>"
				, "        <sensor key='sensorTwo'/>"
				, "      </sensorgroup>"
				, "      <sensorgroup>"
				, "        <sensor key='NewOne'/>"
				, "        <sensor key='NewTwo'/>"
				, "      </sensorgroup>"
				, "      <sensorgroup>"
				, "        <sensor key='NewSensorInsideSameGroup'/>"
				, "        <sensor key='sensorFour'/>"
				, "      </sensorgroup>"
				, "    </sensorgroups>"
				), Arrays.asList(lines));
	}
	
	@Test
	public void intoEmptyParent1() throws XMLStreamException, IOException {
		String[] inputLeft = {
				"<element id='originalDocument'/>"
		};
		String[] inputRight = {
				"<element>"
				,"  <inserted id='new'/>"
				,"</element>"
		};
		
		List<String> discriminatingAttributes = (Arrays.asList("b", "c", "d", "e"));
		
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		XmlDiffer differ = xmlDifferFactory.create(ignoreAttributeNames);
		XmlMerger3 merger = xmlMerger3Factory.create(lexer, differ);
		
		XmlStruct merged = merger.merge(IO.asString(inputLeft), IO.asString(inputRight), false, callback(/*add*/ true, /*remove*/ true, /*replaceAttributes*/ false), stop);
		String[] lines = IO.readLines(new CharSequenceReader(merged.data));
		
		checkList(Arrays.asList(
				  "<element>"
				, "  <inserted id='new'/>"
				, "</element>"
				), Arrays.asList(lines));
		
		merged = merger.merge(IO.asString(inputLeft), IO.asString(inputRight), true, callback(/*add*/ true, /*remove*/ true, /*replaceAttributes*/ false), stop);
		lines = IO.readLines(new CharSequenceReader(merged.data));
		
		checkList(Arrays.asList(
				  "<element>"
				, "<!-- @xmlmerge: lines [2:2] inserted  (empty parent replaced) -->"
				, "  <inserted id='new'/>"
				, "</element>"
				), Arrays.asList(lines));
	}
	
	@Test
	public void intoEmptyParent2() throws XMLStreamException, IOException {
		String[] inputLeft = {
				"<element id='originalDocument'></element>"
		};
		String[] inputRight = {
				"<element>"
				,"  <inserted id='new'/>"
				,"</element>"
		};
		
		List<String> discriminatingAttributes = (Arrays.asList("b", "c", "d", "e"));
		
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		XmlDiffer differ = xmlDifferFactory.create(ignoreAttributeNames);
		XmlMerger3 merger = xmlMerger3Factory.create(lexer, differ);
		
		XmlStruct merged = merger.merge(IO.asString(inputLeft), IO.asString(inputRight), false, callback(/*add*/ true, /*remove*/ true, /*replaceAttributes*/ false), stop);
		String[] lines = IO.readLines(new CharSequenceReader(merged.data));
		
		checkList(Arrays.asList(
				  "<element>"
				, "  <inserted id='new'/>"
				, "</element>"
				), Arrays.asList(lines));
		
		merged = merger.merge(IO.asString(inputLeft), IO.asString(inputRight), true, callback(/*add*/ true, /*remove*/ true, /*replaceAttributes*/ false), stop);
		lines = IO.readLines(new CharSequenceReader(merged.data));
		
		checkList(Arrays.asList(
				  "<element>"
				, "<!-- @xmlmerge: lines [2:2] inserted  (empty parent replaced) -->"
				, "  <inserted id='new'/>"
				, "</element>"
				), Arrays.asList(lines));
	}
	
	@Test
	public void intoEmptyParent3() throws XMLStreamException, IOException {
		String[] inputLeft = {
				"<element id='originalDocument'>  </element>  "
		};
		String[] inputRight = {
				"<element>"
				,"  <inserted id='new'/>"
				,"</element>"
		};
		
		List<String> discriminatingAttributes = (Arrays.asList("b", "c", "d", "e"));
		
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		XmlDiffer differ = xmlDifferFactory.create(ignoreAttributeNames);
		XmlMerger3 merger = xmlMerger3Factory.create(lexer, differ);
		
		XmlStruct merged = merger.merge(IO.asString(inputLeft), IO.asString(inputRight), false, callback(/*add*/ true, /*remove*/ true, /*replaceAttributes*/ false), stop);
		String[] lines = IO.readLines(new CharSequenceReader(merged.data));
		
		checkList(Arrays.asList(
				  "<element>"
				, "  <inserted id='new'/>"
				, "</element>  "
				), Arrays.asList(lines));
		
		merged = merger.merge(IO.asString(inputLeft), IO.asString(inputRight), true, callback(/*add*/ true, /*remove*/ true, /*replaceAttributes*/ false), stop);
		lines = IO.readLines(new CharSequenceReader(merged.data));
		
		checkList(Arrays.asList(
				  "<element>"
				, "<!-- @xmlmerge: lines [2:2] inserted  (empty parent replaced) -->"
				, "  <inserted id='new'/>"
				, "</element>  "
				), Arrays.asList(lines));
	}
	
	@Test
	public void rootsDifferent() throws XMLStreamException, IOException {
		String[] inputLeft = {
				  "<element id='originalDocument'>"
				, "      <sensorgroup>"
				, "        <sensor key='sensorOne'/>"
				, "        <sensor key='sensorTwo'/>"
				, "      </sensorgroup>"
				, "</element>  "
		};
		String[] inputRight = {
				"<root>"
				, "      <sensorgroup>"
				, "        <sensor key='sensorTwo'/>"
				, "      </sensorgroup>"
				,"</root>"
		};
		
		List<String> discriminatingAttributes = (Arrays.asList("b", "c", "d", "e"));
		
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		XmlDiffer differ = xmlDifferFactory.create(ignoreAttributeNames);
		XmlMerger3 merger = xmlMerger3Factory.create(lexer, differ);
		
		try {
			merger.merge(IO.asString(inputLeft), IO.asString(inputRight), false, callback(/*add*/ true, /*remove*/ true, /*replaceAttributes*/ false), stop);
			Assert.fail("Expecting IllegalStateException");
		} catch (IllegalStateException e) {
			System.out.println(e);
		}
	}
	
	@Test
	public void wrongUsage() throws XMLStreamException, IOException {
		String[] inputLeft = {
				"<element id='originalDocument'>  </element>  "
		};
		String[] inputRight = {
				"<element>"
				,"  <inserted id='new'/>"
				,"</element>"
		};
		
		List<String> discriminatingAttributes = (Arrays.asList("b", "c", "d", "e"));
		
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		XmlDiffer differ = xmlDifferFactory.create(ignoreAttributeNames);
		XmlMerger3 merger = xmlMerger3Factory.create(lexer, differ);
		try {
			// left/right swapped!
			merger.merge(IO.asString(inputRight), IO.asString(inputLeft), false, callback(/*add*/ true, /*remove*/ true, /*replaceAttributes*/ false), stop);
//			Assert.fail("Expecting IllegalStateException");
		} catch (IllegalStateException e) {
			System.out.println(e);
			// ok, expected
		}
	}
	
	@Test
	public void insertionPositions() throws XMLStreamException, IOException {
		String[] inputLeft = {
				  "<food>"
				, "   <fruits>"
				, "     <apple/>"
				, "     <banana/>"
				, "   </fruits>"
				, "   <noodles>"
				, "   </noodles>"
				, "   <vegetables>"
				, "     <sninach/>"
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
				, "     <respberry/>"
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
		XmlDiffer differ = xmlDifferFactory.create(ignoreAttributeNames);
		XmlMerger3 merger = xmlMerger3Factory.create(lexer, differ);
		
		XmlStruct merged = merger.merge(IO.asString(inputLeft), IO.asString(inputRight), false, callback(/*add*/ true, /*remove*/ true, /*replaceAttributes*/ false), stop);
		String[] lines = IO.readLines(new CharSequenceReader(merged.data));
		
		checkList(Arrays.asList(
				  "<food>"
				, "   <fruits>"
				, "     <respberry/>"
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
		
		/*
		 * actual checking of insertion positions goes here...
		 */
	}
	
	@Test
	public void uglySyntax() throws XMLStreamException, IOException {
		String[] inputLeft = {
				"<food>"
				, "   <fruits>"
				, "     <apple/>"
				, "     <banana/>"
				, "   </fruits>"
				, "   <noodles>"
				, "   </noodles>"
				, "   <vegetables>"
				, "     <sninach/>"
				, "     <carrot/>"
				, "   </vegetables><rubbish>"
				, "     <ham/>"
				, "   </rubbish>"
				, "</food>"
		};
		String[] inputRight = {
				"<food>"
				, "   <fruits>"
				, "     <respberry/>"
				, "     <apple/>"
				, "     <banana/>"
				, "     <strawberry/>"
				, "   </fruits>"
				, "   <vegetables>"
				, "     <carrot/>"
				, "     <potato/></vegetables><sweets>"
				, "     <chocolate/>"
				, "     <candy/>"
				, "   </sweets>"
				, "   <other>"
				, "     <dogfood/>"
				, "   </other></food>"
		};
		
		List<String> discriminatingAttributes = (Arrays.asList());
		
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		XmlDiffer differ = xmlDifferFactory.create(ignoreAttributeNames);
		XmlMerger3 merger = xmlMerger3Factory.create(lexer, differ);
		
		XmlStruct merged = merger.merge(IO.asString(inputLeft), IO.asString(inputRight), false, callback(/*add*/ true, /*remove*/ true, /*replaceAttributes*/ false), stop);
		String[] lines = IO.readLines(new CharSequenceReader(merged.data));
		
		checkList(Arrays.asList(
				  "<food>"
				, "   <fruits>"
				, "     <respberry/>"
				, "     <apple/>"
				, "     <banana/>"
				, "     <strawberry/>"
				, "   </fruits>"
				, "   <vegetables>"
				, "     <carrot/>"
				, "     <potato/>"
				, "   </vegetables><sweets>"
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
	public void attributes1() throws XMLStreamException, IOException {
		String[] inputLeft = {
				"<root attr='y' oldattr='o'>"
				,"  <element attr='x' />"
				,"  <element attr='y' />"
				,"</root>"
		};
		String[] inputRight = {
				"<root attr='z'>"
				,"  <element attr='a' newattr='b'/>"
				,"  <element attr='c' newattr='d'/>"
				,"</root>"
		};
		
		List<String> discriminatingAttributes = (Arrays.asList());
		
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		XmlDiffer differ = xmlDifferFactory.create(ignoreAttributeNames);
		XmlMerger3 merger = xmlMerger3Factory.create(lexer, differ);
		
		XmlStruct merged = merger.merge(IO.asString(inputLeft), IO.asString(inputRight), false, callback(/*add*/ true, /*remove*/ true, /*replaceAttributes*/ true), stop);
		String[] lines = IO.readLines(new CharSequenceReader(merged.data));
		
		checkList(Arrays.asList(
				  "<root attr='z'>"
				, "  <element attr='a' newattr='b'/>"
				, "  <element attr='c' newattr='d'/>"
				, "</root>"
				), Arrays.asList(lines));
		
		merged = merger.merge(IO.asString(inputLeft), IO.asString(inputRight), false, new IncludeElementCallback() {
			@Override
			public boolean replaceAttributes(XmlElement element) {
				return element.attributes.containsValue("z");
			}
		}, stop);
		lines = IO.readLines(new CharSequenceReader(merged.data));
		
		checkList(Arrays.asList(
				  "<root attr='z'>"
				, "  <element attr='x' />"
				, "  <element attr='y' />"
				, "</root>"
				), Arrays.asList(lines));
		
		merged = merger.merge(IO.asString(inputLeft), IO.asString(inputRight), true, callback(/*add*/ true, /*remove*/ true, /*replaceAttributes*/ true), stop);
		lines = IO.readLines(new CharSequenceReader(merged.data));
		
		checkList(Arrays.asList(
				  "<!-- @xmlmerge:  attr='y' oldattr='o' --><root attr='z'>"
				, "  <!-- @xmlmerge:  attr='x'  --><element attr='a' newattr='b'/>"
				, "  <!-- @xmlmerge:  attr='y'  --><element attr='c' newattr='d'/>"
				, "</root>"
				), Arrays.asList(lines));
	}
	
	@Test
	public void attributes2() throws XMLStreamException, IOException {
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
				,"</root>"
		};
		String[] inputRight = {
				"<root attr='right0'>"
				,"  <element attr='right1' newattr='right2'/>"
				,"  <element attr='right3' newattr='right4'/>"
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
		XmlDiffer differ = xmlDifferFactory.create(ignoreAttributeNames);
		XmlMerger3 merger = xmlMerger3Factory.create(lexer, differ);
		
		XmlStruct merged = merger.merge(IO.asString(inputLeft), IO.asString(inputRight), false, callback(/*add*/ true, /*remove*/ true, /*replaceAttributes*/ true), stop);
		String[] lines = IO.readLines(new CharSequenceReader(merged.data));
		
		checkList(Arrays.asList(inputRight), Arrays.asList(lines));
		
		merged = merger.merge(IO.asString(inputLeft), IO.asString(inputRight), false, new IncludeElementCallback() {
			@Override
			public boolean replaceAttributes(XmlElement element) {
				return element.attributes.containsValue("right0");
			}
		}, stop);
		lines = IO.readLines(new CharSequenceReader(merged.data));
		
		checkList(Arrays.asList(
				  "<root attr='right0'>"
				, "  <element attr='left2' />"
				, "  <element attr='left3' />"
				, "  <element attr='left4' >"
				, "    <child a='left5'/>"
				, "    <child c='left6'/>"
				, "    <child c='left7'>"
				, "      <grandchild/>"
				, "    </child>"
				, "  </element>"
				, "</root>"
				), Arrays.asList(lines));
		
		merged = merger.merge(IO.asString(inputLeft), IO.asString(inputRight), false, new IncludeElementCallback() {
			@Override
			public boolean replaceAttributes(XmlElement element) {
				return element.attributes.containsValue("right0")
					|| element.attributes.containsValue("right1")
					|| element.attributes.containsValue("right3")
				;
			}
		}, stop);
		lines = IO.readLines(new CharSequenceReader(merged.data));
		
		checkList(Arrays.asList(
				  "<root attr='right0'>"
				, "  <element attr='right1' newattr='right2'/>"
				, "  <element attr='right3' newattr='right4'/>"
				, "  <element attr='left4' >"
				, "    <child a='left5'/>"
				, "    <child c='left6'/>"
				, "    <child c='left7'>"
				, "      <grandchild/>"
				, "    </child>"
				, "  </element>"
				, "</root>"
				), Arrays.asList(lines));
		
		merged = merger.merge(IO.asString(inputLeft), IO.asString(inputRight), false, new IncludeElementCallback() {
			@Override
			public boolean replaceAttributes(XmlElement element) {
				return element.attributes.containsValue("right1")
						|| element.attributes.containsValue("right5")
						|| element.attributes.containsValue("right9")
					;
			}
		}, stop);
		lines = IO.readLines(new CharSequenceReader(merged.data));
		
		checkList(Arrays.asList(
				  "<root attr='left0' oldattr='left1'>"
				, "  <element attr='right1' newattr='right2'/>"
				, "  <element attr='left3' />"
				, "  <element attr='right5' >"
				, "    <child a='left5'/>"
				, "    <child c='left6'/>"
				, "    <child c='left7'>"
				, "      <grandchild attr='right9'/>"
				, "    </child>"
				, "  </element>"
				, "</root>"
				), Arrays.asList(lines));
	}
	
	@Test
	public void attributesEmptyDifferent() throws XMLStreamException, IOException {
		String[] inputLeft = {
				"<root attr='y' oldattr='o'>"
				,"  <element attr='x' />"
				,"  <element attr='0'></element>"
				,"  <element attr='0'></element>"
				,"</root>"
		};
		String[] inputRight = {
				"<root attr='z'>"
				,"  <element attr='a' newattr='b'></element>"
				,"  <element attr='2' newattr='3'></element>"
				,"  <element attr='y' />"
				,"</root>"
		};
		
		List<String> discriminatingAttributes = (Arrays.asList("b", "c", "d", "e"));
		
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		XmlDiffer differ = xmlDifferFactory.create(ignoreAttributeNames);
		XmlMerger3 merger = xmlMerger3Factory.create(lexer, differ);
		
		XmlStruct merged = merger.merge(IO.asString(inputLeft), IO.asString(inputRight), false, callback(/*add*/ true, /*remove*/ true, /*replaceAttributes*/ true), stop);
		String[] lines = IO.readLines(new CharSequenceReader(merged.data));
		
		checkList(Arrays.asList(
				  "<root attr='z'>"
				, "  <element attr='a' newattr='b'/>"
				, "  <element attr='2' newattr='3'></element>"
				, "  <element attr='y' ></element>"
				, "</root>"
				), Arrays.asList(lines));
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
		XmlDiffer differ = xmlDifferFactory.create(ignoreAttributeNames);
		XmlMerger3 merger = xmlMerger3Factory.create(lexer, differ);
		
		XmlStruct merged = merger.merge(IO.asString(inputLeft), IO.asString(inputRight), false, callback(/*add*/ true, /*remove*/ true, /*replaceAttributes*/ true), stop);
		String[] lines = IO.readLines(new CharSequenceReader(merged.data));
		
		checkList(Arrays.asList(inputRight), Arrays.asList(lines));
		
		merged = merger.merge(IO.asString(inputLeft), IO.asString(inputRight), true, callback(/*add*/ true, /*remove*/ true, /*replaceAttributes*/ true), stop);
		lines = IO.readLines(new CharSequenceReader(merged.data));
		
		checkList(Arrays.asList(
				  "<!-- @xmlmerge:  attr='left0' oldattr='left1' --><root attr='right0'>"
				, "  <!-- @xmlmerge:  attr='left2'  --><element attr='right1' newattr='right2'/>"
				, "  <!-- @xmlmerge:  attr='left3'  --><element attr='right3' newattr='right4'/>"
				, "<!-- @xmlmerge: lines [4:4] inserted -->"
				, "  <newelement/>"
				, "  <!-- @xmlmerge:  attr='left4'  --><element attr='right5' >"
				, "    <!-- @xmlmerge:  a='left5' --><child a='right6'/>"
				, "    <!-- @xmlmerge:  c='left6' --><child c='right7'/>"
				, "    <!-- @xmlmerge:  c='left7' --><child c='right8'>"
				, "      <!-- @xmlmerge:  --><grandchild attr='right9'/>"
				, "    </child>"
				, "  </element>"
				, "<!-- @xmlmerge: lines [11:11] removed -->"
				, "</root>"

			), Arrays.asList(lines));
	}
}
