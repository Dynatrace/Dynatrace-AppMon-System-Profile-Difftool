package com.dynatrace.common.conf;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

public class PropertiesInjectorTest {
	
	private static String asString(String[] lines) {
		return asString(lines, 1, lines.length);
	}
	
	private static String asString(String[] lines, int from, int to) {
		StringBuilder buf = new StringBuilder();
		if (from < 1) {
			from = 1;
		}
		if (to > lines.length) {
			to = lines.length;
		}
		for (int i = from; i <= to; i++) {
			buf.append(lines[i - 1]).append("\n");
		}
		return buf.toString();
	}

	@Test
	public void resourceOverride() throws IOException {
		TestSettings settings;
		
		String[] input = {
				"mystring1=overridden in resource"
				, "myint1=1501"
				, "mystringarray1.0=completely"
				, "mystringarray1.1=replaced"
				, "unknown=unknown" // must not throw - only log
		};
		File file = new File("test-settings.properties");
		try (FileWriter out = new FileWriter(file)) {
			out.write(asString(input));
			out.close();
			settings = PropertiesInjector.getDefault().injectProperties(new TestSettings(), "test-settings.properties", ResourceLoader.getFileSystemOverride());
		} finally {
			file.delete();
		}
		
		Assert.assertEquals("overridden in resource", settings.mystring1);
		Assert.assertEquals(1501, settings.myint1);
		Assert.assertNull("overridden in resource", settings.mystring2);
		
		Assert.assertEquals(2, settings.mystringarray1.length);
		Assert.assertEquals("completely", settings.mystringarray1[0]);
		Assert.assertEquals("replaced", settings.mystringarray1[1]);
		
		Assert.assertNull(settings.mystring2);
		
		Assert.assertEquals(false, settings.myboolean1);
		Assert.assertEquals(false, settings.myboolean2);
		Assert.assertEquals(true, settings.defaultboolean);
		
		Assert.assertNull(settings.mystringarray2);
		Assert.assertNull(settings.mymap1);
		Assert.assertNull(settings.mymap2);
		Assert.assertNull(settings.myset1);
		Assert.assertNull(settings.myset2);
		Assert.assertEquals(""+settings.emptyset, "[]", settings.emptyset.toString());
		Assert.assertEquals(0, settings.emptystringarray.length);
		Assert.assertNull(""+settings.nullset, settings.nullset);
	}
	
	@Test
	public void injection() {
		check(PropertiesInjector.getDefault().injectProperties(new TestSettings(), "test-settings.properties", ResourceLoader.getDefault()));
		check(PropertiesInjector.getDefault().injectProperties(new TestSettings(), "test-settings.properties", ResourceLoader.getFileSystemOverride()));
	}
	
	private void check(TestSettings settings) {
		
		Assert.assertEquals(2000000, settings.myint2);
		Assert.assertEquals(25, settings.defaultint);
		
		Assert.assertEquals('x', settings.mychar1);
		Assert.assertEquals(0, settings.mychar2);
		
		Assert.assertEquals(-5, settings.myint1);
		Assert.assertEquals("hello", settings.mystring1);
		Assert.assertEquals(4, settings.mystringarray1.length);
		Assert.assertEquals("a", settings.mystringarray1[0]);
		Assert.assertEquals("", settings.mystringarray1[1]);
		Assert.assertEquals("b", settings.mystringarray1[2]);
		Assert.assertEquals("fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff", settings.mystringarray1[3]);
			
		Assert.assertEquals("", settings.mystring2);
		
		Assert.assertEquals(false, settings.myboolean1);
		Assert.assertEquals(true, settings.myboolean2);
		Assert.assertEquals(true, settings.defaultboolean);
		
		Assert.assertEquals(1, settings.mystringarray2.length);
		Assert.assertEquals("", settings.mystringarray2[0]);
		
		Assert.assertEquals(0, settings.emptystringarray.length);
		
		Assert.assertEquals(""+settings.mymap1, "{key1=value, key2=value, key3=value, key4=value, key5=value}", settings.mymap1.toString());
		Assert.assertEquals(""+settings.mymap2, "{a=b, =}", settings.mymap2.toString());
		
		Assert.assertEquals(""+settings.myset1, "[a, b]", settings.myset1.toString());
		Assert.assertEquals(""+settings.myset2, "[]", settings.myset2.toString());
		Assert.assertEquals(""+settings.emptyset, "[]", settings.emptyset.toString());
		Assert.assertNull(""+settings.nullset, settings.nullset);
	}
	
	@Test
	public void overrideProperties() {
		Map<String, String> properties = new HashMap<>();
		
		properties.put("myint1", "500");
		properties.put("defaultboolean", "false");
		properties.put("mystring2", "xyz");
		properties.put("mystringarray1.2", "overwritten");
		properties.put("mystringarray1.4", "appended");
		properties.put("mymap1.2", "overwrittenKey=value");
		properties.put("mymap1.5", "appendedKey=value");
		properties.put("emptyset.0", "singleton");
		
		TestSettings settings = PropertiesInjector.getCustom(properties).injectProperties(new TestSettings(), "test-settings.properties", ResourceLoader.getDefault());

		Assert.assertEquals(500, settings.myint1);
		Assert.assertEquals(false, settings.defaultboolean);
		Assert.assertEquals("xyz", settings.mystring2);
		
		Assert.assertEquals(5, settings.mystringarray1.length);
		Assert.assertEquals("overwritten", settings.mystringarray1[2]);
		Assert.assertEquals("appended", settings.mystringarray1[4]);
		Assert.assertEquals(""+settings.mymap1, "{key1=value, key2=value, overwrittenKey=value, key4=value, key5=value, appendedKey=value}", settings.mymap1.toString());
		Assert.assertEquals(""+settings.mymap2, "{a=b, =}", settings.mymap2.toString());
		Assert.assertEquals(""+settings.emptyset, "[singleton]", settings.emptyset.toString());
	}
	
	static class TestSettings {
		
		int myint1;
		int myint2;
		int defaultint = 25;
		
		char mychar1;
		char mychar2;
		
		String mystring1;
		String mystring2;
		
		String[] mystringarray1;
		String[] mystringarray2;
		String[] emptystringarray = {};
		
		boolean myboolean1;
		boolean myboolean2;
		boolean defaultboolean = true;
		
		Map<String, String> mymap1;
		Map<String, String> mymap2;
		
		Set<String> myset1;
		Set<String> myset2;
		Set<String> emptyset = Collections.emptySet();
		Set<String> nullset;
	}
}
