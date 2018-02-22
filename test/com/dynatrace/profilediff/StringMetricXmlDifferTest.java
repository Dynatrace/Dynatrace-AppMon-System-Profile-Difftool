package com.dynatrace.profilediff;

import java.io.IOException;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class StringMetricXmlDifferTest extends TestBase {

	XmlLexer lexer;
	
	@Before
	public void before() {
		lexer = new XmlLexer(Arrays.asList("attr"), stringCache);
	}
	
	@Test
	public void childrenNames() throws XMLStreamException, IOException {
		String[] input = {
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
		XmlStruct xml = lexer.parse(IO.asString(input));
		
		checkList(Arrays.asList(
				  "root:y"
				, "element:x"
				, "element:y"
				, "element:z"
				, "child"
				, "child"
				, "child"
				, "grandchild"
				), toChildrenNamesStrings(xml.elements, 0));
		
		checkList(Arrays.asList(
				  "root:y->1;element:x;1;element:y;1;element:z"
				, "element:x"
				, "element:y"
				, "element:z->2;child;2;child;2;child"
				, "child"
				, "child"
				, "child->3;grandchild"
				, "grandchild"
				), toChildrenNamesStrings(xml.elements, 1));
		
		checkList(Arrays.asList(
				"root:y->1;element:x;1;element:y;1;element:z;2;child;2;child;2;child"
				, "element:x"
				, "element:y"
				, "element:z->2;child;2;child;2;child;3;grandchild"
				, "child"
				, "child"
				, "child->3;grandchild"
				, "grandchild"
				), toChildrenNamesStrings(xml.elements, 2));
		
		List<String> asofLevel3 = Arrays.asList(
				  "root:y->1;element:x;1;element:y;1;element:z;2;child;2;child;2;child;3;grandchild"
				, "element:x"
				, "element:y"
				, "element:z->2;child;2;child;2;child;3;grandchild"
				, "child"
				, "child"
				, "child->3;grandchild"
				, "grandchild"
		); 
		
		Assert.assertEquals(4, xml.depth);
		for (int i = 3; i < xml.depth * 10; i++) {
			checkList(asofLevel3, toChildrenNamesStrings(xml.elements, i));
		}
	}
	
	@Test
	public void smallestComparable0() {
		{
			List<Integer> list = Arrays.asList( 0, 1, 2, 3, 4, 5 );
			BitSet set = new BitSet();
			set.set(0, list.size());
			Assert.assertEquals(0, StringMetricXmlDiffer.smallestComparable0(list, set));
		}
		{
			List<Integer> list = Arrays.asList( 5, 4, 3, 2, 1, 0 );
			BitSet set = new BitSet();
			set.set(0, list.size());
			Assert.assertEquals(5, StringMetricXmlDiffer.smallestComparable0(list, set));
		}
		{
			List<Integer> list = Arrays.asList( 1, 3, 0, 1, 4, 6, 5 );
			BitSet set = new BitSet();
			set.set(0, list.size());
			Assert.assertEquals(2, StringMetricXmlDiffer.smallestComparable0(list, set));
		}
		{
			List<Integer> list = Arrays.asList( 1, 3, 0, 1, 4, 6, 5 );
			BitSet set = new BitSet();
			set.set(0, list.size());
			set.clear(2);
			Assert.assertEquals(0, StringMetricXmlDiffer.smallestComparable0(list, set)); // list[0] == 1 smallest, but not the only one!
		}
		{
			List<Integer> list = Arrays.asList( 1, 3, 0, 1, 4, 6, 5 );
			BitSet set = new BitSet();
			set.set(0, list.size());
			set.clear(0);
			set.clear(2);
			Assert.assertEquals(3, StringMetricXmlDiffer.smallestComparable0(list, set)); // list[3] == 1 smallest
		}
		{
			List<Integer> list = Arrays.asList( 1, 3, 0, 1, 4, 6, 5 );
			BitSet set = new BitSet();
			Assert.assertEquals(-1, StringMetricXmlDiffer.smallestComparable0(list, set));
		}
	}
}
