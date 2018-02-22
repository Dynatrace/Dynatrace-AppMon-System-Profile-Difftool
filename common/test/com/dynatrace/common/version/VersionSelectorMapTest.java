package com.dynatrace.common.version;


import org.junit.Assert;
import org.junit.Test;

public class VersionSelectorMapTest {
	
	@Test
	public void normal() {
		VersionSelectorMap<Object> map = new VersionSelectorMap<>();
		check(map);
		Assert.assertEquals("size", 5, map.map.size()); 
	}
	
	@Test
	public void remapping() {
		VersionSelectorMap<Object> map = new VersionSelectorMap.WithRemapping<>();
		check(map);
		Assert.assertEquals("size", 44, map.map.size()); 
	}
	
	@Test
	public void normalRegression() {
		VersionSelectorMap<Object> map = new VersionSelectorMap<>();
		checkRegression(map);
	}
	
	@Test
	public void remappingRegression() {
		VersionSelectorMap<Object> map = new VersionSelectorMap.WithRemapping<>();
		checkRegression(map);
	}
	
	void check(VersionSelectorMap<Object> map) {
		Object m_4_2_0_1000 = "4.2.0.1000";
		Object m_5_6_0_2000 = "5.6.0.2000";
		Object m_6_1_0_1000 = "6.1.0.1000";
		Object m_6_1_0_9999 = "6.1.0.9999";
		Object m_7_0_0_0000 = "7.0.0.0000";
		
		map.put("4.2.0.1000", m_4_2_0_1000);
		map.put("5.6.0.2000", m_5_6_0_2000);
		map.put("6.1.0.1000", m_6_1_0_1000);
		map.put("6.1.0.9999", m_6_1_0_9999);
		map.put("7.0.0.0000", m_7_0_0_0000);
		
		// direct
		mapfind(map, "4.2.0.1000", m_4_2_0_1000);
		mapfind(map, "5.6.0.2000", m_5_6_0_2000);
		mapfind(map, "6.1.0.1000", m_6_1_0_1000);
		mapfind(map, "6.1.0.9999", m_6_1_0_9999);
		mapfind(map, "7.0.0.0000", m_7_0_0_0000);
		
		// in-between
		mapfind(map, "2", m_4_2_0_1000);
		mapfind(map, "3.0.0", m_4_2_0_1000);
		mapfind(map, "4.0.0", m_4_2_0_1000);
		mapfind(map, "4.0.0.1000", m_4_2_0_1000);
		mapfind(map, "4.1.0.1000", m_4_2_0_1000);
		mapfind(map, "4.1.0.9999", m_4_2_0_1000);
		
		mapfind(map, "4", m_4_2_0_1000);
		mapfind(map, "4.2", m_4_2_0_1000);
		mapfind(map, "4.2.0", m_4_2_0_1000);
		mapfind(map, "4.2.0.5000", m_4_2_0_1000);
		mapfind(map, "4.2.0.9999", m_4_2_0_1000);
		mapfind(map, "4.3.0", m_4_2_0_1000);
		
		mapfind(map, "5", m_5_6_0_2000);
		mapfind(map, "5.5.0", m_5_6_0_2000);
		mapfind(map, "5.5.0.9999", m_5_6_0_2000);
		
		mapfind(map, "5.6", m_5_6_0_2000);
		mapfind(map, "5.6.0", m_5_6_0_2000);
		mapfind(map, "5.6.0.0000", m_5_6_0_2000);
		mapfind(map, "5.6.0.9999", m_5_6_0_2000);
		
		mapfind(map, "6", m_6_1_0_1000);
		mapfind(map, "6.0.0", m_6_1_0_1000);
		mapfind(map, "6.0.0.9999", m_6_1_0_1000);
		mapfind(map, "6.1", m_6_1_0_1000);
		mapfind(map, "6.1.0", m_6_1_0_1000);
		mapfind(map, "6.1.0.0000", m_6_1_0_1000);
		mapfind(map, "6.1.0.5000", m_6_1_0_1000);
		mapfind(map, "6.1.0.555", m_6_1_0_1000);
		mapfind(map, "6.1.0.5555", m_6_1_0_1000);
		
		mapfind(map, "6.1.0.9998", m_6_1_0_9999);
		mapfind(map, "6.1.0.9999", m_6_1_0_9999);
		mapfind(map, "6.1.0.99990", m_6_1_0_9999);
		mapfind(map, "6.1.0.99999", m_6_1_0_9999);
		mapfind(map, "6.1.0.99999abc", m_6_1_0_9999);
		mapfind(map, "6.9.0", m_6_1_0_9999);
		
		mapfind(map, "7", m_7_0_0_0000);
		mapfind(map, "7.9", m_7_0_0_0000);
		mapfind(map, "7.0.0", m_7_0_0_0000);
		mapfind(map, "7.0.0.9999", m_7_0_0_0000);
		
		mapfind(map, "8", m_7_0_0_0000);
		mapfind(map, "8.8.8.8.8", m_7_0_0_0000);
	}
	
	void checkRegression(VersionSelectorMap<Object> map) {
		Object m_6_1_0_7880 = "6.1.0.7880";
		Object m_6_1_0_8091 = "6.1.0.8091";
		
		map.put("6.1.0.7880", m_6_1_0_7880);
		map.put("6.1.0.8091", m_6_1_0_8091);
		
		mapfind(map, "6.2.0.9999", m_6_1_0_8091);
		mapfind(map, "6.1.0.9999", m_6_1_0_8091);
		mapfind(map, "6.0.0.9999", m_6_1_0_7880);
	}
	
	static void mapfind(VersionSelectorMap<Object> map, String key, Object expected) {
		Object o = map.find(key);
		Assert.assertTrue(""+o, o == expected);
	}
	
	@Test
	public void commonPrefixLength() {
		
		Assert.assertEquals(0, VersionSelectorMap.commonPrefixLength("", ""));
		Assert.assertEquals(0, VersionSelectorMap.commonPrefixLength("", "b"));
		Assert.assertEquals(0, VersionSelectorMap.commonPrefixLength("a", ""));
		Assert.assertEquals(0, VersionSelectorMap.commonPrefixLength("a", "b"));

		Assert.assertEquals(1, VersionSelectorMap.commonPrefixLength("a", "a"));
		Assert.assertEquals(1, VersionSelectorMap.commonPrefixLength("ab", "a"));
		
		Assert.assertEquals(2, VersionSelectorMap.commonPrefixLength("ab", "ab"));
		Assert.assertEquals(2, VersionSelectorMap.commonPrefixLength("aby", "ab"));
		Assert.assertEquals(2, VersionSelectorMap.commonPrefixLength("ab", "abC"));
		
		Assert.assertEquals(3, VersionSelectorMap.commonPrefixLength("abC", "abC"));
	}
}
