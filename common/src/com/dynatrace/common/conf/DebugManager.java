package com.dynatrace.common.conf;

import java.util.Arrays;

public class DebugManager {
	
	public static boolean isFlagEnabled(String name, boolean defaultValue) {
		Throwable throwable = new Throwable(name);
//		throwable.printStackTrace();
		/*
		java.lang.Throwable: printMissingDiscriminators
			at com.dynatrace.common.conf.DebugManager.isFlagEnabled(DebugManager.java:10)
			at com.dynatrace.profilediff.XmlDiffer.<clinit>(XmlDiffer.java:20)
		*/
		StackTraceElement[] stacktrace = throwable.getStackTrace();
		if (stacktrace != null && stacktrace.length > 1) {
			String className = stacktrace[1].getClassName();
			String key = className + "." + name;
			String string = System.getProperty(key);
			boolean value = string != null ? Boolean.parseBoolean(string) : defaultValue;
			System.err.printf("-D%s=%s%n",key, !value);
			return value;
		}
		System.err.println("DebugManager: Cannot get stacktrace: " + Arrays.toString(stacktrace));
		return false;
	}
}
