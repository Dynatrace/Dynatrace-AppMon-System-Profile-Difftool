package com.dynatrace.common.conf;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Injection of properties to Java objects with possibility of overring with system or ant properties.
 * 
 * @author cwat-pgrasboe
 */
@SuppressWarnings("rawtypes")
public class PropertiesInjector {
	
	private static final PropertiesInjector instanceDefault = new PropertiesInjector(System.getProperties());
	
	public static PropertiesInjector getDefault() {
		return instanceDefault;
	}
	
	public static PropertiesInjector getCustom(Map overrideProperties) {
		return new PropertiesInjector(overrideProperties);
	}
	
	private final Map overrideProperties;
	
	private PropertiesInjector(Map overrideProperties) {
		this.overrideProperties = overrideProperties;
	}

	private Object getValue(String name, Class<?> type, Map properties) {
		Map override = overrideProperties;
		if (type == String.class) {
			return getValue0(name, properties, override);
		} else if (type == String[].class) {
			List<String> buf = new ArrayList<>();
			for (int i = 0;; i++) {
				String value = getValue0(name + "." + i, properties, override);
				if (value == null) {
					break;
				}
				buf.add(value);
			};
			return buf.isEmpty() ? null : buf.toArray(new String[buf.size()]);
		} else if (type == Set.class) {
			Set<String> set = new LinkedHashSet<>();
			for (int i = 0;; i++) {
				String value = getValue0(name + "." + i, properties, override);
				if (value == null) {
					break;
				}
				set.add(value);
			}
			return set.isEmpty() ? null : set;
		} else if (type == Map.class) {
			Map<String, String> map = new LinkedHashMap<>(); // preserve order!
			for (int i = 0;; i++) {
				String value = getValue0(name + "." + i, properties, override);
				if (value == null) {
					break;
				}
				int j = value.indexOf('=');
				if (j == -1) {
					throw new IllegalArgumentException("Invalid map entry: " + value);
				}
				String mapkey = value.substring(0, j).trim();
				String mapval = value.substring(j + 1).trim();
				map.put(mapkey, mapval);
			}
			return map.isEmpty() ? null : map;
		} else if (type == boolean.class) {
			String val = getValue0(name, properties, override);
			return val == null ? null : Boolean.parseBoolean(val);
		} else if (type == int.class) {
			String val = getValue0(name, properties, override);
			return val == null ? null : Integer.parseInt(val);
		} else if (type == char.class) {
			String val = getValue0(name, properties, override);
			return val == null ? null : castChar(val);
		} else {
			throw new IllegalStateException("Cannot set fields of type " + type.getName());
		}
	}
	
	private static char castChar(String val) {
		if (val.length() != 1) {
			throw new IllegalStateException("Invalid char: " + val);
		}
		return val.charAt(0);
	}

	private static String getValue0(String name, Map properties, Map override) {
		String value = (String) properties.remove(name); // removing so that we can find unhandled properties
		String overrideValue = (String) override.get(name);
		if (overrideValue != null) {
			/*
			 * we don't print the value in case it is a password.
			 */
			System.out.println("Setting overridden: " + name);
		}
		return overrideValue != null ? overrideValue : value;
	}

	private void inject(Object settings, Map properties) {
		for (Field field : settings.getClass().getDeclaredFields()) {
			String name = field.getName();
			Object value =  getValue(name, field.getType(), properties);
			if (value == null) {
				continue; // not setting value - default from sourcefile.
			}
			try {
				field.setAccessible(true);
				field.set(settings, value);
			} catch (IllegalAccessException e) {
				throw new IllegalStateException("Cannot set field " + name + ", value=" + value);
			}
		}
		if (!properties.isEmpty()) {
			System.out.println("Unhandled properties: " + properties.keySet());
		}
	}
	
	public <T> T injectProperties(T instance, String resourceName, ResourceLoader resourceLoader) {
		inject(instance, ResourceLoader.loadProperties(resourceLoader.loadResourceForce(instance.getClass(), resourceName)));
		return instance;
	}
}
