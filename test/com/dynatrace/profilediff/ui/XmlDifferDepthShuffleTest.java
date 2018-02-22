package com.dynatrace.profilediff.ui;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.junit.Assert;
import org.junit.Before;
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
public class XmlDifferDepthShuffleTest extends ShuffleTestBase {
	
	public XmlDifferDepthShuffleTest(StringMetricXmlDifferFactory stringMetricXmlDifferFactory, String name, int run) {
		super(stringMetricXmlDifferFactory, name, run);
	}

	@Before
	public void beforeClass() {
		resetGlobalMaxDistances();
	}
	
	private final ChangeItem item = ChangeItem.changes;
	
	XmlLexer lexer;

	private int stringMetricDifferThreshold = -1;
	
	@Before
	public void before() {
		lexer = new XmlLexer(LEVENSHTEIN_DISCRIMINATOR_ATTRIBUTES, stringCache);
	}
	
	@Test
	public void depth1Equal() throws XMLStreamException, IOException {
		String[] inputLeft = {
				"<root>"
				, "  <class pattern='classOne'>"
				, "    <method pattern='methodOne'/>"
				, "  </class>"
				, "  <class pattern='classOne'>"
				, "    <method pattern='methodOne'/>"
				, "    <method pattern='methodTwo'/>"
				, "  </class>"
				, "</root>"
		};
		String[] inputRight = {
				"<root>"
				, "  <class pattern='classOne'>"
				, "    <method pattern='methodOne'/>"
				, "  </class>"
				, "  <class pattern='classOne'>"
				, "    <method pattern='methodOne'/>"
				, "    <method pattern='methodTwo'/>"
				, "  </class>"
				, "</root>"
		};
		
		List<String> discriminatingAttributes = Arrays.asList("pattern");
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		
		XmlStruct xmlLeft = lexer.parse(IO.asString(inputLeft));
		XmlStruct xmlRight = lexer.parse(IO.asString(inputRight));
		
		MetricResolver levenshteinMetricResolver = (XmlElement) -> StringMetrics::getLevenshteinDistance;
		XmlDiffer differ = newStringMetricXmlDiffer(SYSTEM_PROFILE_IGNORED_ATTRIBUTES, levenshteinMetricResolver, stringMetricDifferThreshold);
		
		XmlDiffer.Result diffResult = differ.diff(xmlLeft, xmlRight);
		Assert.assertEquals(0, diffResult.nAdded);
		Assert.assertEquals(0, diffResult.nRemoved);
		Assert.assertEquals(0, diffResult.nAttributeChanged);
		
		List<XmlElement> collected;
		
		collected = checkCollectChangedElements(Arrays.asList(
				), 0, item, xmlRight);
		
		checkPeers(Arrays.asList(
				), collected);
		
		collected = checkCollectChangedElements(Arrays.asList(
				), 0, item, xmlLeft);
		
		checkPeers(Arrays.asList(
				), collected);
	}
	
	@Test
	public void depth1Different() throws XMLStreamException, IOException {
		String[] inputLeft = {
				"<root>"
				, "  <class pattern='classOne'>"
				, "    <method pattern='methodOne'/>"
				, "  </class>"
				, "  <class pattern='classOne'>"
				, "    <method pattern='methodOne'/>"
				, "    <method pattern='methodTwo'/>"
				, "  </class>"
				, "</root>"
		};
		String[] inputRight = {
				"<root>"
				, "  <class pattern='classOne'>"
				, "    <method pattern='methodOne'/>"
				, "  </class>"
				, "  <class pattern='classOne'>"
				, "    <method pattern='methodOne'/>"
				, "    <method pattern='methodTwoRenamed'/>"
				, "  </class>"
				, "</root>"
		};
		
		List<String> discriminatingAttributes = Arrays.asList("pattern");
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		
		XmlStruct xmlLeft = lexer.parse(IO.asString(inputLeft));
		XmlStruct xmlRight = lexer.parse(IO.asString(inputRight));
		
		MetricResolver levenshteinMetricResolver = (XmlElement) -> StringMetrics::getLevenshteinDistance;
		XmlDiffer differ = newStringMetricXmlDiffer(SYSTEM_PROFILE_IGNORED_ATTRIBUTES, levenshteinMetricResolver, stringMetricDifferThreshold);
		
		XmlDiffer.Result diffResult = differ.diff(xmlLeft, xmlRight);
		Assert.assertEquals(0, diffResult.nAdded);
		Assert.assertEquals(0, diffResult.nRemoved);
		Assert.assertEquals(1, diffResult.nAttributeChanged);
		
		List<XmlElement> collected;
		
		collected = checkCollectChangedElements(Arrays.asList(
				  "(#) method:methodTwoRenamed"
				), 1, item, xmlRight);
		
		checkPeers(Arrays.asList(
				  "element (#) method:methodTwoRenamed"
				, "---peer (#) method:methodTwo"
				), collected);
		
		collected = checkCollectChangedElements(Arrays.asList(
				  "(#) method:methodTwo"
				), 1, item, xmlLeft);
		
		checkPeers(Arrays.asList(
				  "element (#) method:methodTwo"
				, "---peer (#) method:methodTwoRenamed"
				), collected);
	}
	
	@Test
	public void depth2Equal() throws XMLStreamException, IOException {
		String[] inputLeft = {
				"<root>"
				, "  <class pattern='classOne'>"
				, "    <method pattern='methodOne'/>"
				, "  </class>"
				, "  <class pattern='classOne'>"
				, "    <method pattern='methodOne'/>"
				, "    <method pattern='methodOne'>"
				, "      <argument pattern='argOne'/>"
				, "      <argument pattern='argTwo'/>"
				, "    </method>"
				, "  </class>"
				, "</root>"
		};
		String[] inputRight = {
				"<root>"
				, "  <class pattern='classOne'>"
				, "    <method pattern='methodOne'/>"
				, "  </class>"
				, "  <class pattern='classOne'>"
				, "    <method pattern='methodOne'/>"
				, "    <method pattern='methodOne'>"
				, "      <argument pattern='argOne'/>"
				, "      <argument pattern='argTwo'/>"
				, "    </method>"
				, "  </class>"
				, "</root>"
		};
		
		List<String> discriminatingAttributes = Arrays.asList("pattern");
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		
		XmlStruct xmlLeft = lexer.parse(IO.asString(inputLeft));
		XmlStruct xmlRight = lexer.parse(IO.asString(inputRight));
		
		MetricResolver levenshteinMetricResolver = (XmlElement) -> StringMetrics::getLevenshteinDistance;
		XmlDiffer differ = newStringMetricXmlDiffer(SYSTEM_PROFILE_IGNORED_ATTRIBUTES, levenshteinMetricResolver, stringMetricDifferThreshold);
		
		XmlDiffer.Result diffResult = differ.diff(xmlLeft, xmlRight);
		Assert.assertEquals(0, diffResult.nAdded);
		Assert.assertEquals(0, diffResult.nRemoved);
		Assert.assertEquals(0, diffResult.nAttributeChanged);
		
		List<XmlElement> collected;
		
		collected = checkCollectChangedElements(Arrays.asList(
				), 0, item, xmlRight);
		
		checkPeers(Arrays.asList(
				), collected);
		
		collected = checkCollectChangedElements(Arrays.asList(
				), 0, item, xmlLeft);
		
		checkPeers(Arrays.asList(
				), collected);
	}
	
	@Test
	public void depth3Equal() throws XMLStreamException, IOException {
		String[] inputLeft = {
				"<root>"
				, "  <class pattern='classOne'>"
				, "    <method pattern='methodOne'/>"
				, "  </class>"
				, "  <class pattern='classOne'>"
				, "    <method pattern='methodOne'/>"
				, "    <method pattern='methodOne'>"
				, "      <argument pattern='argOne'/>"
				, "      <argument pattern='argOne'/>"
				, "    </method>"
				, "  </class>"
				, "</root>"
		};
		String[] inputRight = {
				"<root>"
				, "  <class pattern='classOne'>"
				, "    <method pattern='methodOne'/>"
				, "  </class>"
				, "  <class pattern='classOne'>"
				, "    <method pattern='methodOne'/>"
				, "    <method pattern='methodOne'>"
				, "      <argument pattern='argOne'/>"
				, "      <argument pattern='argOne'/>"
				, "    </method>"
				, "  </class>"
				, "</root>"
		};
		
		List<String> discriminatingAttributes = Arrays.asList("pattern");
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		
		XmlStruct xmlLeft = lexer.parse(IO.asString(inputLeft));
		XmlStruct xmlRight = lexer.parse(IO.asString(inputRight));
		
		MetricResolver levenshteinMetricResolver = (XmlElement) -> StringMetrics::getLevenshteinDistance;
		XmlDiffer differ = newStringMetricXmlDiffer(SYSTEM_PROFILE_IGNORED_ATTRIBUTES, levenshteinMetricResolver, stringMetricDifferThreshold);
		
		XmlDiffer.Result diffResult = differ.diff(xmlLeft, xmlRight);
		Assert.assertEquals(0, diffResult.nAdded);
		Assert.assertEquals(0, diffResult.nRemoved);
		Assert.assertEquals(0, diffResult.nAttributeChanged);
		
		List<XmlElement> collected;
		
		collected = checkCollectChangedElements(Arrays.asList(
				), 0, item, xmlRight);
		
		checkPeers(Arrays.asList(
				), collected);
		
		collected = checkCollectChangedElements(Arrays.asList(
				), 0, item, xmlLeft);
		
		checkPeers(Arrays.asList(
				), collected);
	}
	
	@Test
	public void depth3Equal2() throws XMLStreamException, IOException {
		String[] inputLeft = {
				  "<root>"
				, "  <class pattern='classOne'>"
				, "    <method pattern='methodOne'/>"
				, "    <method pattern='methodOne'>"
				, "      <argument pattern='argOne'/>"
				, "      <argument pattern='argOne'/>"
				, "    </method>"
				, "  </class>"
				, "  <class pattern='classOne'>"
				, "    <method pattern='methodOne'/>"
				, "    <method pattern='methodOne'>"
				, "      <argument pattern='argOne'/>"
				, "      <argument pattern='argOne'/>"
				, "    </method>"
				, "  </class>"
				, "</root>"
		};
		String[] inputRight = {
				  "<root>"
				, "  <class pattern='classOne'>"
				, "    <method pattern='methodOne'/>"
				, "    <method pattern='methodOne'>"
				, "      <argument pattern='argOne'/>"
				, "      <argument pattern='argOne'/>"
				, "    </method>"
				, "  </class>"
				, "  <class pattern='classOne'>"
				, "    <method pattern='methodOne'/>"
				, "    <method pattern='methodOne'>"
				, "      <argument pattern='argOne'/>"
				, "      <argument pattern='argOne'/>"
				, "    </method>"
				, "  </class>"
				, "</root>"
		};
		
		List<String> discriminatingAttributes = Arrays.asList("pattern");
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		
		XmlStruct xmlLeft = lexer.parse(IO.asString(inputLeft));
		XmlStruct xmlRight = lexer.parse(IO.asString(inputRight));
		
		MetricResolver levenshteinMetricResolver = (XmlElement) -> StringMetrics::getLevenshteinDistance;
		XmlDiffer differ = newStringMetricXmlDiffer(SYSTEM_PROFILE_IGNORED_ATTRIBUTES, levenshteinMetricResolver, stringMetricDifferThreshold);
		
		XmlDiffer.Result diffResult = differ.diff(xmlLeft, xmlRight);
		Assert.assertEquals(0, diffResult.nAdded);
		Assert.assertEquals(0, diffResult.nRemoved);
		Assert.assertEquals(0, diffResult.nAttributeChanged);
		
		List<XmlElement> collected;
		
		collected = checkCollectChangedElements(Arrays.asList(
				), 0, item, xmlRight);
		
		checkPeers(Arrays.asList(
				), collected);
		
		collected = checkCollectChangedElements(Arrays.asList(
				), 0, item, xmlLeft);
		
		checkPeers(Arrays.asList(
				), collected);
	}
	
	@Test
	public void depth3Different() throws XMLStreamException, IOException {
		String[] inputLeft = {
				"<root>"
				, "  <class pattern='classOne'>"
				, "    <method pattern='methodOne'/>"
				, "  </class>"
				, "  <class pattern='classOne'>"
				, "    <method pattern='methodOne'/>"
				, "    <method pattern='methodOne'>"
				, "      <argument pattern='argOne'/>"
				, "      <argument pattern='argOne'/>"
				, "    </method>"
				, "  </class>"
				, "</root>"
		};
		String[] inputRight = {
				"<root>"
				, "  <class pattern='classOne'>"
				, "    <method pattern='methodOne'/>"
				, "  </class>"
				, "  <class pattern='classOne'>"
				, "    <method pattern='methodOne'/>"
				, "    <method pattern='methodOne'>"
				, "      <argument pattern='argOne'/>"
				, "      <argument pattern='argOneRenamed'/>"
				, "    </method>"
				, "  </class>"
				, "</root>"
		};
		
		List<String> discriminatingAttributes = Arrays.asList("pattern");
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		
		XmlStruct xmlLeft = lexer.parse(IO.asString(inputLeft));
		XmlStruct xmlRight = lexer.parse(IO.asString(inputRight));
		
		MetricResolver levenshteinMetricResolver = (XmlElement) -> StringMetrics::getLevenshteinDistance;
		XmlDiffer differ = newStringMetricXmlDiffer(SYSTEM_PROFILE_IGNORED_ATTRIBUTES, levenshteinMetricResolver, stringMetricDifferThreshold);
		
		XmlDiffer.Result diffResult = differ.diff(xmlLeft, xmlRight);
		Assert.assertEquals(0, diffResult.nAdded);
		Assert.assertEquals(0, diffResult.nRemoved);
		Assert.assertEquals(1, diffResult.nAttributeChanged);
		
		List<XmlElement> collected;
		
		collected = checkCollectChangedElements(Arrays.asList(
				  "(#) argument:argOneRenamed"
				), 1, item, xmlRight);
		
		checkPeers(Arrays.asList(
				  "element (#) argument:argOneRenamed"
				, "---peer (#) argument:argOne"
				), collected);
		
		collected = checkCollectChangedElements(Arrays.asList(
				  "(#) argument:argOne"
				), 1, item, xmlLeft);
		
		checkPeers(Arrays.asList(
				  "element (#) argument:argOne"
				, "---peer (#) argument:argOneRenamed"
				), collected);
	}
	
	//@Test
	//@Ignore
	public void depth3Different2() throws XMLStreamException, IOException {
		String[] inputLeft = {
				  "<root>"
				, "  <class pattern='classOne'>"
				, "    <method pattern='methodOne'/>"
				, "    <method pattern='methodOne'>"
				, "      <argument pattern='argOne' i='0'/>"
				, "      <argument pattern='argOne' i='1'/>"
				, "    </method>"
				, "  </class>"
				, "  <class pattern='classOne'>"
				, "    <method pattern='methodOne'/>"
				, "    <method pattern='methodOne'>"
				, "      <argument pattern='argOne' i='2'/>"
				, "      <argument pattern='argOne' i='3'/>"
				, "    </method>"
				, "  </class>"
				, "  <class pattern='classOne'>"
				, "    <method pattern='methodOne'/>"
				, "    <method pattern='methodOne'>"
				, "      <argument pattern='argOne' i='4'/>"
				, "      <argument pattern='argOne' i='5'/>"
				, "    </method>"
				, "  </class>"
				, "</root>"
		};
		String[] inputRight = {
				  "<root>"
				, "  <class pattern='classOne'>"
				, "    <method pattern='methodOne'/>"
				, "    <method pattern='methodOne'>"
				, "      <argument pattern='argOne' i='0'/>"
				, "      <argument pattern='argOneRenamed'/>"
				, "    </method>"
				, "  </class>"
				, "  <class pattern='classOne'>"
				, "    <method pattern='methodOne'/>"
				, "    <method pattern='methodOne'>"
				, "      <argument pattern='argOne' i='2'/>"
				, "      <argument pattern='argOneR'/>"
				, "    </method>"
				, "  </class>"
				, "  <class pattern='classOne'>"
				, "    <method pattern='methodOne'/>"
				, "    <method pattern='methodOne'>"
				, "      <argument pattern='argOne' i='4'/>"
				, "      <argument pattern='argOne' i='5'/>"
				, "    </method>"
				, "  </class>"
				, "</root>"
		};
		
		List<String> discriminatingAttributes = Arrays.asList("pattern");
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		
		XmlStruct xmlLeft = lexer.parse(IO.asString(inputLeft));
		XmlStruct xmlRight = lexer.parse(IO.asString(inputRight));
		
		MetricResolver levenshteinMetricResolver = (XmlElement) -> StringMetrics::getLevenshteinDistance;
		XmlDiffer differ = newStringMetricXmlDiffer(SYSTEM_PROFILE_IGNORED_ATTRIBUTES, levenshteinMetricResolver, stringMetricDifferThreshold);
		
		XmlDiffer.Result diffResult = differ.diff(xmlLeft, xmlRight);
		Assert.assertEquals(0, diffResult.nAdded);
		Assert.assertEquals(0, diffResult.nRemoved);
		Assert.assertEquals(6, diffResult.nAttributeChanged);
		
		List<XmlElement> collected;
		
		collected = checkCollectChangedElements(Arrays.asList(
				  "(#) argument:argOne"
				, "(#) argument:argOneRenamed"
				, "(#) argument:argOne"
				, "(#) argument:argOneR"
				, "(#) argument:argOne"
				, "(#) argument:argOne"
				), 6, item, xmlRight);
		
		checkPeers(Arrays.asList(
				  "element (#) argument:argOneRenamed"
				, "---peer (#) argument:argOne"
				, "element (#) argument:argOneR"
				, "---peer (#) argument:argOne"
				), collected);
		
		collected = checkCollectChangedElements(Arrays.asList(
				  "(#) argument:argOne"
				, "(#) argument:argOne"
				), 2, item, xmlLeft);
		
//		checkPeers(Arrays.asList(
//				  "element (#) argument:argOne"
//				, "---peer (#) argument:argOneRenamed"
//				, "element (#) argument:argOne"
//				, "---peer (#) argument:argOneR"
//				), collected);
	}
	
	@Test
	public void deepEqual() throws XMLStreamException, IOException {
		String[] inputLeft = {
 				  "<root>"
				, "  <class pattern='classOne'>"
				, "    <inter>"
				, "      <inter>"
				, "        <method pattern='methodOne'/>"
				, "      </inter>"
				, "    </inter>"
				, "  </class>"
				, "  <class pattern='classOne'>"
				, "    <inter>"
				, "      <inter>"
				, "        <method pattern='methodOne'/>"
				, "      </inter>"
				, "    </inter>"
				, "  </class>"
				, "  <class pattern='classOne'>"
				, "    <inter>"
				, "      <inter>"
				, "        <method pattern='methodOne'/>"
				, "        <method pattern='methodTwo'/>"
				, "      </inter>"
				, "    </inter>"
				, "  </class>"
				, "</root>"
		};
		String[] inputRight = {
				"<root>"
				, "  <class pattern='classOne'>"
				, "    <inter>"
				, "      <inter>"
				, "        <method pattern='methodOne'/>"
				, "      </inter>"
				, "    </inter>"
				, "  </class>"
				, "  <class pattern='classOne'>"
				, "    <inter>"
				, "      <inter>"
				, "        <method pattern='methodOne'/>"
				, "      </inter>"
				, "    </inter>"
				, "  </class>"
				, "  <class pattern='classOne'>"
				, "    <inter>"
				, "      <inter>"
				, "        <method pattern='methodOne'/>"
				, "        <method pattern='methodTwo'/>"
				, "      </inter>"
				, "    </inter>"
				, "  </class>"
				, "</root>"
		};
		
		List<String> discriminatingAttributes = Arrays.asList("pattern");
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		
		XmlStruct xmlLeft = lexer.parse(IO.asString(inputLeft));
		XmlStruct xmlRight = lexer.parse(IO.asString(inputRight));
		Assert.assertEquals(5, xmlLeft.depth);
		Assert.assertEquals(5, xmlRight.depth);
		
		MetricResolver levenshteinMetricResolver = (XmlElement) -> StringMetrics::getLevenshteinDistance;
		XmlDiffer differ = newStringMetricXmlDiffer(SYSTEM_PROFILE_IGNORED_ATTRIBUTES, levenshteinMetricResolver, stringMetricDifferThreshold);
		
		XmlDiffer.Result diffResult = differ.diff(xmlLeft, xmlRight);
		Assert.assertEquals(0, diffResult.nAdded);
		Assert.assertEquals(0, diffResult.nRemoved);
		Assert.assertEquals(0, diffResult.nAttributeChanged);
		
		expectGlobalMaxDistances("XmlDifferDepthShuffleTest", 0, 0, 4, /*expectedMetricInvokeCount*/ 0);
	}
	
	@Test
	public void deeperEqual() throws XMLStreamException, IOException {
		String[] inputLeft = {
				  "<root>"
				, "  <class pattern='classOne'>"
				, "    <inter>"
				, "      <inter>"
				, "        <inter>"
				, "          <inter>"
				, "			    <inter>"
				, "      			<inter>"
				, "			          <inter>"
				, "         			 <inter>"
				, "				            <method pattern='methodOne'/>"
				, "          			</inter>"
				, "        			  </inter>"
				, "      			</inter>"
				, "    			 </inter>"
				, "          </inter>"
				, "        </inter>"
				, "      </inter>"
				, "    </inter>"
				, "  </class>"
				, "  <class pattern='classOne'>"
				, "    <inter>"
				, "      <inter>"
				, "        <inter>"
				, "          <inter>"
				, "			    <inter>"
				, "      			<inter>"
				, "			          <inter>"
				, "         			 <inter>"
				, "				            <method pattern='methodOne'/>"
				, "				            <method pattern='methodTwo'/>"
				, "          			</inter>"
				, "        			  </inter>"
				, "      			</inter>"
				, "    			 </inter>"
				, "          </inter>"
				, "        </inter>"
				, "      </inter>"
				, "    </inter>"
				, "  </class>"
				, "  <class pattern='classOne'>"
				, "    <inter>"
				, "      <inter>"
				, "        <inter>"
				, "          <inter>"
				, "			    <inter>"
				, "      			<inter>"
				, "			          <inter>"
				, "         			 <inter>"
				, "				            <method pattern='methodOne'/>"
				, "          			</inter>"
				, "        			  </inter>"
				, "      			</inter>"
				, "    			 </inter>"
				, "          </inter>"
				, "        </inter>"
				, "      </inter>"
				, "    </inter>"
				, "  </class>"
				, "</root>"
		};
		String[] inputRight = {
				  "<root>"
				, "  <class pattern='classOne'>"
				, "    <inter>"
				, "      <inter>"
				, "        <inter>"
				, "          <inter>"
				, "			    <inter>"
				, "      			<inter>"
				, "			          <inter>"
				, "         			 <inter>"
				, "				            <method pattern='methodOne'/>"
				, "          			</inter>"
				, "        			  </inter>"
				, "      			</inter>"
				, "    			 </inter>"
				, "          </inter>"
				, "        </inter>"
				, "      </inter>"
				, "    </inter>"
				, "  </class>"
				, "  <class pattern='classOne'>"
				, "    <inter>"
				, "      <inter>"
				, "        <inter>"
				, "          <inter>"
				, "			    <inter>"
				, "      			<inter>"
				, "			          <inter>"
				, "         			 <inter>"
				, "				            <method pattern='methodOne'/>"
				, "				            <method pattern='methodTwo'/>"
				, "          			</inter>"
				, "        			  </inter>"
				, "      			</inter>"
				, "    			 </inter>"
				, "          </inter>"
				, "        </inter>"
				, "      </inter>"
				, "    </inter>"
				, "  </class>"
				, "  <class pattern='classOne'>"
				, "    <inter>"
				, "      <inter>"
				, "        <inter>"
				, "          <inter>"
				, "			    <inter>"
				, "      			<inter>"
				, "			          <inter>"
				, "         			 <inter>"
				, "				            <method pattern='methodOne'/>"
				, "          			</inter>"
				, "        			  </inter>"
				, "      			</inter>"
				, "    			 </inter>"
				, "          </inter>"
				, "        </inter>"
				, "      </inter>"
				, "    </inter>"
				, "  </class>"
				, "</root>"
		};
		
		List<String> discriminatingAttributes = Arrays.asList("pattern");
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		
		XmlStruct xmlLeft = lexer.parse(IO.asString(inputLeft));
		XmlStruct xmlRight = lexer.parse(IO.asString(inputRight));
		Assert.assertEquals(11, xmlLeft.depth);
		Assert.assertEquals(11, xmlRight.depth);
		
		MetricResolver levenshteinMetricResolver = (XmlElement) -> StringMetrics::getLevenshteinDistance;
		XmlDiffer differ = newStringMetricXmlDiffer(SYSTEM_PROFILE_IGNORED_ATTRIBUTES, levenshteinMetricResolver, stringMetricDifferThreshold);
		
		XmlDiffer.Result diffResult = differ.diff(xmlLeft, xmlRight);
		Assert.assertEquals(0, diffResult.nAdded);
		Assert.assertEquals(0, diffResult.nRemoved);
		Assert.assertEquals(0, diffResult.nAttributeChanged);
		
		expectGlobalMaxDistances("XmlDifferDepthShuffleTest", 0, 0, 10, /*expectedMetricInvokeCount*/ 0);
	}
	
	@Test
	public void deeperDifferentAttr() throws XMLStreamException, IOException {
		String[] inputLeft = {
				  "<root>"
				, "  <class pattern='classOne'>"
				, "    <inter>"
				, "      <inter>"
				, "        <inter>"
				, "          <inter>"
				, "			    <inter>"
				, "      			<inter>"
				, "			          <inter>"
				, "         			 <inter>"
				, "				            <method pattern='methodOne'/>"
				, "          			</inter>"
				, "        			  </inter>"
				, "      			</inter>"
				, "    			 </inter>"
				, "          </inter>"
				, "        </inter>"
				, "      </inter>"
				, "    </inter>"
				, "  </class>"
				, "  <class pattern='classOne'>"
				, "    <inter>"
				, "      <inter>"
				, "        <inter>"
				, "          <inter>"
				, "			    <inter>"
				, "      			<inter>"
				, "			          <inter>"
				, "         			 <inter>"
				, "				            <method pattern='methodOne'/>"
				, "				            <method pattern='methodTwo'/>"
				, "          			</inter>"
				, "        			  </inter>"
				, "      			</inter>"
				, "    			 </inter>"
				, "          </inter>"
				, "        </inter>"
				, "      </inter>"
				, "    </inter>"
				, "  </class>"
				, "  <class pattern='classOne'>"
				, "    <inter>"
				, "      <inter>"
				, "        <inter>"
				, "          <inter>"
				, "			    <inter>"
				, "      			<inter>"
				, "			          <inter>"
				, "         			 <inter>"
				, "				            <method pattern='methodOne'/>"
				, "          			</inter>"
				, "        			  </inter>"
				, "      			</inter>"
				, "    			 </inter>"
				, "          </inter>"
				, "        </inter>"
				, "      </inter>"
				, "    </inter>"
				, "  </class>"
				, "</root>"
		};
		String[] inputRight = {
				  "<root>"
				, "  <class pattern='classOne'>"
				, "    <inter>"
				, "      <inter>"
				, "        <inter>"
				, "          <inter>"
				, "			    <inter>"
				, "      			<inter>"
				, "			          <inter>"
				, "         			 <inter>"
				, "				            <method pattern='methodOne'/>"
				, "          			</inter>"
				, "        			  </inter>"
				, "      			</inter>"
				, "    			 </inter>"
				, "          </inter>"
				, "        </inter>"
				, "      </inter>"
				, "    </inter>"
				, "  </class>"
				, "  <class pattern='classOne'>"
				, "    <inter>"
				, "      <inter>"
				, "        <inter>"
				, "          <inter>"
				, "			    <inter>"
				, "      			<inter>"
				, "			          <inter>"
				, "         			 <inter>"
				, "				            <method pattern='methodOne'/>"
				, "				            <method pattern='methodTwoThree'/>"
				, "          			</inter>"
				, "        			  </inter>"
				, "      			</inter>"
				, "    			 </inter>"
				, "          </inter>"
				, "        </inter>"
				, "      </inter>"
				, "    </inter>"
				, "  </class>"
				, "  <class pattern='classOne'>"
				, "    <inter>"
				, "      <inter>"
				, "        <inter>"
				, "          <inter>"
				, "			    <inter>"
				, "      			<inter>"
				, "			          <inter>"
				, "         			 <inter>"
				, "				            <method pattern='methodOne'/>"
				, "          			</inter>"
				, "        			  </inter>"
				, "      			</inter>"
				, "    			 </inter>"
				, "          </inter>"
				, "        </inter>"
				, "      </inter>"
				, "    </inter>"
				, "  </class>"
				, "</root>"
		};
		
		List<String> discriminatingAttributes = Arrays.asList("pattern");
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		
		XmlStruct xmlLeft = lexer.parse(IO.asString(inputLeft));
		XmlStruct xmlRight = lexer.parse(IO.asString(inputRight));
		Assert.assertEquals(11, xmlLeft.depth);
		Assert.assertEquals(11, xmlRight.depth);
		
		MetricResolver levenshteinMetricResolver = (XmlElement) -> StringMetrics::getLevenshteinDistance;
		XmlDiffer differ = newStringMetricXmlDiffer(SYSTEM_PROFILE_IGNORED_ATTRIBUTES, levenshteinMetricResolver, stringMetricDifferThreshold);
		
		XmlDiffer.Result diffResult = differ.diff(xmlLeft, xmlRight);
		Assert.assertEquals(0, diffResult.nAdded);
		Assert.assertEquals(0, diffResult.nRemoved);
		Assert.assertEquals(1, diffResult.nAttributeChanged);
		
		List<XmlElement> collected;
		
		collected = checkCollectChangedElements(Arrays.asList(
				  "(#) method:methodTwoThree"
				), 1, item, xmlRight);
		
		checkPeers(Arrays.asList(
				  "element (#) method:methodTwoThree"
				, "---peer (#) method:methodTwo"
				), collected);
		
		collected = checkCollectChangedElements(Arrays.asList(
				  "(#) method:methodTwo"
				), 1, item, xmlLeft);
		
		checkPeers(Arrays.asList(
				  "element (#) method:methodTwo"
				, "---peer (#) method:methodTwoThree"
				), collected);
		
		expectGlobalMaxDistances("XmlDifferDepthShuffleTest", 5, -1, 10, /*expectedMetricInvokeCount*/ -1);
	}	
	
	@Test
	public void deeperDifferentStruct() throws XMLStreamException, IOException {
		String[] inputLeft = {
				"<root>"
				, "  <class pattern='classOne'>"
				, "    <inter>"
				, "      <inter>"
				, "        <inter>"
				, "          <inter>"
				, "			    <inter>"
				, "      			<inter>"
				, "			          <inter>"
				, "         			 <inter>"
				, "				            <method pattern='methodOne'/>"
				, "          			</inter>"
				, "        			  </inter>"
				, "      			</inter>"
				, "    			 </inter>"
				, "          </inter>"
				, "        </inter>"
				, "      </inter>"
				, "    </inter>"
				, "  </class>"
				, "  <class pattern='classOne'>"
				, "    <inter>"
				, "      <inter>"
				, "        <inter>"
				, "          <inter>"
				, "			    <inter>"
				, "      			<inter>"
				, "			          <inter>"
				, "         			 <inter>"
				, "				            <method pattern='methodOne'/>"
				, "				            <method pattern='methodTwo'/>"
				, "          			</inter>"
				, "        			  </inter>"
				, "      			</inter>"
				, "    			 </inter>"
				, "          </inter>"
				, "        </inter>"
				, "      </inter>"
				, "    </inter>"
				, "  </class>"
				, "  <class pattern='classOne'>"
				, "    <inter>"
				, "      <inter>"
				, "        <inter>"
				, "          <inter>"
				, "			    <inter>"
				, "      			<inter>"
				, "			          <inter>"
				, "         			 <inter>"
				, "				            <method pattern='methodOne'/>"
				, "          			</inter>"
				, "        			  </inter>"
				, "      			</inter>"
				, "    			 </inter>"
				, "          </inter>"
				, "        </inter>"
				, "      </inter>"
				, "    </inter>"
				, "  </class>"
				, "</root>"
		};
		String[] inputRight = {
				"<root>"
				, "  <class pattern='classOne'>"
				, "    <inter>"
				, "      <inter>"
				, "        <inter>"
				, "          <inter>"
				, "			    <inter>"
				, "      			<inter>"
				, "			          <inter>"
				, "         			 <inter>"
				, "				            <method pattern='methodOne'/>"
				, "          			</inter>"
				, "        			  </inter>"
				, "      			</inter>"
				, "    			 </inter>"
				, "          </inter>"
				, "        </inter>"
				, "      </inter>"
				, "    </inter>"
				, "  </class>"
				, "  <class pattern='classOne'>"
				, "    <inter>"
				, "      <inter>"
				, "        <inter>"
				, "          <inter>"
				, "			    <inter>"
				, "      			<inter>"
				, "			          <inter>"
				, "         			 <inter>"
				, "				            <method pattern='methodOne'/>"
				, "				            <method pattern='methodTwo'/>"
				, "				            <method pattern='methodThree'/>"
				, "          			</inter>"
				, "        			  </inter>"
				, "      			</inter>"
				, "    			 </inter>"
				, "          </inter>"
				, "        </inter>"
				, "      </inter>"
				, "    </inter>"
				, "  </class>"
				, "  <class pattern='classOne'>"
				, "    <inter>"
				, "      <inter>"
				, "        <inter>"
				, "          <inter>"
				, "			    <inter>"
				, "      			<inter>"
				, "			          <inter>"
				, "         			 <inter>"
				, "          			</inter>"
				, "        			  </inter>"
				, "      			</inter>"
				, "    			 </inter>"
				, "          </inter>"
				, "        </inter>"
				, "      </inter>"
				, "    </inter>"
				, "  </class>"
				, "</root>"
		};
		
		List<String> discriminatingAttributes = Arrays.asList("pattern");
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		
		XmlStruct xmlLeft = lexer.parse(IO.asString(inputLeft));
		XmlStruct xmlRight = lexer.parse(IO.asString(inputRight));
		Assert.assertEquals(11, xmlLeft.depth);
		Assert.assertEquals(11, xmlRight.depth);
		
		MetricResolver levenshteinMetricResolver = (XmlElement) -> StringMetrics::getLevenshteinDistance;
		XmlDiffer differ = newStringMetricXmlDiffer(SYSTEM_PROFILE_IGNORED_ATTRIBUTES, levenshteinMetricResolver, stringMetricDifferThreshold);
		
		XmlDiffer.Result diffResult = differ.diff(xmlLeft, xmlRight);
		Assert.assertEquals(1, diffResult.nAdded);
		Assert.assertEquals(1, diffResult.nRemoved);
		Assert.assertEquals(0, diffResult.nAttributeChanged);
		
		List<XmlElement> collected;
		
		collected = checkCollectChangedElements(Arrays.asList(
				  "(+) method:methodThree"
				), 1, item, xmlRight);
		
		checkPeers(Arrays.asList(
				  "element (+) method:methodThree"
				, "---peer <none>"
				), collected);
		
		collected = checkCollectChangedElements(Arrays.asList(
				  "(-) method:methodOne"
				), 1, item, xmlLeft);
		
		checkPeers(Arrays.asList(
				  "element (-) method:methodOne"
				, "---peer <none>"
				), collected);
		
		expectGlobalMaxDistances("XmlDifferDepthShuffleTest", -1, -1, 9, /*expectedMetricInvokeCount*/ -1);
	}
	
	@Test
	public void ambiguousPeersReproducable() throws XMLStreamException, IOException {
		String[] inputLeft = {
				"<root>"
				, "  <elem id='ac'/>" // <-- this must be reproducibly peered with ... 
				, "  <elem id='a'/>"
				, "  <elem id='a'/>"
				, "</root>"
		};
		String[] inputRight = {
				"<root>"
				, "  <elem id='a'/>"
				, "  <elem id='a'/>"
				, "  <elem id='a'/>" // <-- ... this, no matter if sorting impl or not (of course not with shuffling impl).
				, "</root>"
		};
		
		List<String> discriminatingAttributes = Arrays.asList("id");
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		
		XmlStruct xmlLeft = lexer.parse(IO.asString(inputLeft));
		XmlStruct xmlRight = lexer.parse(IO.asString(inputRight));
		
		MetricResolver levenshteinMetricResolver = (XmlElement) -> StringMetrics::getLevenshteinDistance;
		XmlDiffer differ = newStringMetricXmlDiffer(SYSTEM_PROFILE_IGNORED_ATTRIBUTES, levenshteinMetricResolver, stringMetricDifferThreshold);
		
		XmlDiffer.Result diffResult = differ.diff(xmlLeft, xmlRight);
		Assert.assertEquals(0, diffResult.nAdded);
		Assert.assertEquals(0, diffResult.nRemoved);
		Assert.assertEquals(1, diffResult.nAttributeChanged);
		
		List<XmlElement> collected;
		
		collected = checkCollectChangedElements(Arrays.asList(
				"(#) elem:a"
				), 1, item, xmlRight);
		
		checkPeers(Arrays.asList(
				"element (#) elem:a"
				, "---peer (#) elem:ac"
				), collected);
		
		if (!isDifferImplShufflingStringMetric(differ)) {
			Assert.assertTrue(xmlRight.elements.containsAll(collected));
			Assert.assertSame(collected.get(0), xmlRight.root().children.get(2));
		}
		
		collected = checkCollectChangedElements(Arrays.asList(
				"(#) elem:ac"
				), 1, item, xmlLeft);
		
		checkPeers(Arrays.asList(
				"element (#) elem:ac"
				, "---peer (#) elem:a"
				), collected);
		
		if (!isDifferImplShufflingStringMetric(differ)) {
			Assert.assertTrue(xmlLeft.elements.containsAll(collected));
			Assert.assertSame(collected.get(0), xmlLeft.root().children.get(0));
		}
	}	
}
