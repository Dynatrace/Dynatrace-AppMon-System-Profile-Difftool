package com.dynatrace.profilediff;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

public class AttributeSpecifierTest {
	
	@Test
	public void stripKey() {
		Assert.assertEquals("", AttributeSpecifier.stripKey("", ""));
		Assert.assertEquals("", AttributeSpecifier.stripKey("", "root"));
		Assert.assertEquals("attribute", AttributeSpecifier.stripKey("attribute", "root"));
		Assert.assertEquals("attribute", AttributeSpecifier.stripKey("attribute", "root/element"));
		Assert.assertEquals("attribute", AttributeSpecifier.stripKey("root/element:attribute", "root/element"));
		Assert.assertEquals("attribute", AttributeSpecifier.stripKey("/root/element:attribute", "root/element"));
		Assert.assertEquals("attribute", AttributeSpecifier.stripKey("root/element/child:attribute", "root/element/child"));
		Assert.assertEquals("attribute", AttributeSpecifier.stripKey("element/child:attribute", "root/element/child"));
		Assert.assertEquals("attribute", AttributeSpecifier.stripKey("child:attribute", "root/element/child"));
		Assert.assertEquals("attribute", AttributeSpecifier.stripKey("attribute", "root/element/child"));
		Assert.assertEquals("attribute", AttributeSpecifier.stripKey("oot/element:attribute", "oot/element"));
		Assert.assertEquals("attribute", AttributeSpecifier.stripKey("oot/element:attribute", "root/element"));
		
		Assert.assertEquals(null, AttributeSpecifier.stripKey("/child:attribute", "root/element/child"));
		Assert.assertEquals(null, AttributeSpecifier.stripKey("root/element:attribute", "root/element1"));
		Assert.assertEquals(null, AttributeSpecifier.stripKey("root/element:attribute", "oot/element"));
		Assert.assertEquals(null, AttributeSpecifier.stripKey("root/element:attribute", "root/element/child"));
		Assert.assertEquals(null, AttributeSpecifier.stripKey("root/element:attribute", "element"));
	}
	
	@Test
	public void matches() {
		Assert.assertTrue(AttributeSpecifier.matches("", ""));
		Assert.assertTrue(AttributeSpecifier.matches("", "root"));
		Assert.assertTrue(AttributeSpecifier.matches("root/element", "root/element"));
		Assert.assertTrue(AttributeSpecifier.matches("/root/element", "root/element"));
		Assert.assertTrue(AttributeSpecifier.matches("root/element/child", "root/element/child"));
		Assert.assertTrue(AttributeSpecifier.matches("element/child", "root/element/child"));
		Assert.assertTrue(AttributeSpecifier.matches("child", "root/element/child"));
		Assert.assertTrue(AttributeSpecifier.matches("oot/element", "oot/element"));
		Assert.assertTrue(AttributeSpecifier.matches("oot/element", "root/element"));
		
		Assert.assertFalse(AttributeSpecifier.matches("/child", "root/element/child"));
		Assert.assertFalse(AttributeSpecifier.matches("root/element", "root/element1"));
		Assert.assertFalse(AttributeSpecifier.matches("root/element", "oot/element"));
		Assert.assertFalse(AttributeSpecifier.matches("root/element", "root/element/child"));
		Assert.assertFalse(AttributeSpecifier.matches("root/element", "element"));
	}
	
	@Test
	public void containsKey() {
		Assert.assertFalse(AttributeSpecifier.containsKey(createSet(
		), "", ""));
		Assert.assertFalse(AttributeSpecifier.containsKey(createSet(
		), "ignore", ""));
		Assert.assertTrue(AttributeSpecifier.containsKey(createSet(
				"ignore"
		), "ignore", ""));
		Assert.assertTrue(AttributeSpecifier.containsKey(createSet(
				"ignore"
				), "ignore", "root"));
		Assert.assertFalse(AttributeSpecifier.containsKey(createSet(
				"ignore1"
				), "ignore", "root"));
		Assert.assertTrue(AttributeSpecifier.containsKey(createSet(
				"root:ignore"
				), "ignore", "root"));
		Assert.assertFalse(AttributeSpecifier.containsKey(createSet(
				"root:ignore"
				), "ignore", "root/element"));
		Assert.assertTrue(AttributeSpecifier.containsKey(createSet(
				"root/element:ignore"
				), "ignore", "root/element"));
		Assert.assertFalse(AttributeSpecifier.containsKey(createSet(
				"root/element:ignore"
				), "ignore", "root/element/child"));
		Assert.assertTrue(AttributeSpecifier.containsKey(createSet(
				"root/element:ignore"
				, "root/element/child:ignore"
				), "ignore", "root/element/child"));
		Assert.assertTrue(AttributeSpecifier.containsKey(createSet(
				"root/element:ignore"
				, "element/child:ignore"
				), "ignore", "root/element/child"));
		Assert.assertTrue(AttributeSpecifier.containsKey(createSet(
				"root/element:ignore"
				, "child:ignore"
				), "ignore", "root/element/child"));
		Assert.assertTrue(AttributeSpecifier.containsKey(createSet(
				"child:ignore"
				), "ignore", "root/element/child"));
	}
	
	private static Set<String> createSet(String...strings) {
		Set<String> result = new HashSet<>();
		for (String string : strings) {
			result.add(string);
		}
		return result;
	}
}
