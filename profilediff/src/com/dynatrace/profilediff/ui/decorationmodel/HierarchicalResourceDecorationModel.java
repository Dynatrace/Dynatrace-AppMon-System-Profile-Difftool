package com.dynatrace.profilediff.ui.decorationmodel;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.dynatrace.common.conf.DebugManager;
import com.dynatrace.common.conf.ResourceLoader;
import com.dynatrace.profilediff.XmlElement;
import com.dynatrace.profilediff.XmlLexer;

public class HierarchicalResourceDecorationModel implements DecorationModel<XmlElement> {
	
	private final String scheme;
	private final String[] imageExt;
	private final int pathLevelThreshold;
	
	private final Properties names;
	private final Map<String, Icon> iconCache;
	
	public HierarchicalResourceDecorationModel(String scheme, String[] imageExt, int pathLevelThreshold) {
		if (scheme == null) {
			throw new IllegalArgumentException("scheme must be non-null");
		}
		if (imageExt == null) {
			throw new IllegalArgumentException("imageExt must be non-null");
		}
		if (pathLevelThreshold < 0) {
			throw new IllegalArgumentException("rawPathLevelThreshold must be positive");
		}
		this.scheme = scheme;
		this.imageExt = imageExt;
		this.pathLevelThreshold = pathLevelThreshold;
		
		names = ResourceLoader.loadProperties(getResource("names.properties"));
		iconCache = new HashMap<>();
	}
	
	@Override
	public String getText(XmlElement element) {
		if (element.level <= pathLevelThreshold) {
			String name = names.getProperty(escapePath(element.path));
			if (name != null) {
				return name;
			}
		}
		String name = names.getProperty(escapePath(element.rawPath));
		if (name != null) {
			return name;
		}
		return null; // we don't decide the name.
	}
	
	@Override
	public Icon getIcon(XmlElement element) {
		Icon icon = getIcon0(element);
		return icon != null ? icon : doGetIcon(element.children.isEmpty() ? ".leaf" : ".node");
	}
	
	private Icon getIcon0(XmlElement element) {
		while (element != null) {
			if (element.level <= pathLevelThreshold) {
				Icon icon = doGetIcon(escapePath(element.path));
				if (icon != null) {
					return icon;
				}
			}
			Icon icon = doGetIcon(escapePath(element.rawPath));
			if (icon != null) {
				return icon;
			}
			element = element.parent;
		}
		return null;
	}

	
	private Icon doGetIcon(String path) {
		if (iconCache.containsKey(path)) {
			return iconCache.get(path); // this may be null. but avoid accessing file system over and over for non existing files.
		}
		Icon icon = tryLoadIcon(path);
		iconCache.put(path, icon);
		return icon;
	}
	
	private static boolean debugLoading = DebugManager.isFlagEnabled("debugLoading", false);
	
	// @TestOnly
	int tryLoadIconCount;

	private Icon tryLoadIcon(String path) {
		tryLoadIconCount++;
		if (debugLoading) {
			System.out.println("Trying to load icon with path: " + path);
		}
		for (int i = 0; i < imageExt.length; i++) {
			URL url = getResource(path + imageExt[i]);
			if (url != null) {
				return new ImageIcon(url);
			}
		}
		return null;
	}
	
	private URL getResource(String name) {
		return ResourceLoader.getFileSystemOverride().loadResource(getClass(), scheme + "/" + name);
	}
	
	private static String escapePath(String path) {
		return path
			.replace(XmlLexer.LEVEL_SEPARATOR, LEVEL_SEPARATOR)
			.replace(XmlLexer.ATTRIBUTE_SEPARATOR, ATTRIBUTE_SEPARATOR);
	}
	
	// escaped to be valid for file names and property keys. 
	private static final String LEVEL_SEPARATOR = ".";
	private static final String ATTRIBUTE_SEPARATOR = "_";
}
