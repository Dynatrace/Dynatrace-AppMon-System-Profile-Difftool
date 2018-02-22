package com.dynatrace.profilediff;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.Stack;

import javax.xml.stream.XMLStreamException;

import com.dynatrace.common.stringcache.StringCacheInterface;

public class XmlLexer {
	
	public static final String ATTRIBUTE_SEPARATOR = ":";
	public static final String LEVEL_SEPARATOR = "/";
	
	private final List<String> discriminatorAttributeNames;
	private final StringCacheInterface stringCache;

	public XmlLexer(List<String> discriminatorAttributeNames, StringCacheInterface stringCache) {
		this.discriminatorAttributeNames = discriminatorAttributeNames;
		this.stringCache = stringCache;
	}

	/**
	 * Parses an XML text and returns the structure.
	 * @throws XMLStreamException 
	 */
	public XmlStruct parse(CharSequence data) throws XMLStreamException {
		return parse0(data);
	}
	
	private XmlStruct parse0(CharSequence data) throws XMLStreamException {
		List<XmlElement> elements = new ArrayList<>();
		XmlStruct xml = new XmlStruct(elements, data, stringCache);
		XmlParser.parse(data, new MyHandler(xml), stringCache);
		assert verifyOffsets(data, xml.elements); 
		assert verifyIndicesAndLevels(elements); 
		return xml;
	}
	
	private static boolean verifyOffsets(CharSequence data, List<XmlElement> elements) {
		for (XmlElement element : elements) {
			assert element.openTag.start < element.closeTag.end  : element.toOffsetString() + " offsets error";
			String content = data.subSequence(element.openTag.start, element.closeTag.end).toString();
			assert content.startsWith("<" + element.rawName) : element + " xml: '" + content + "'";;
			assert element.closeTag.start == -1 || content.endsWith(">") : element + " xml: '" + content + "'";
		}
		return true;
	}

	private static boolean verifyIndicesAndLevels(List<XmlElement> elements) {
		int index = 0;
		for (XmlElement element : elements) {
			assert element.index == index++;
			assert  element.level == calcDepth(element); 
		}
		return true;
	}
	
	private static int calcDepth(XmlElement e) {
		int depth = 0;
		while (e.parent != null) {
			e = e.parent;
			depth++;
		}
		return depth;
	}
	
	private class MyHandler implements XmlParser.Handler {
		private final XmlStruct xml;
		private final Stack<XmlElement> stack = new Stack<>();
		private final Stack<String> path = new Stack<>();
		private final Stack<String> rawPath = new Stack<>();
		private int level;
		private int index;
		
		MyHandler(XmlStruct xml) {
			this.xml = xml;
		}
		
		@Override
		public void startElement(String rawName, SortedMap<String, String> attributes, TagPosition tagPosition) {
			List<String> discriminatorAttributes = new ArrayList<>(); // order is important since discriminators are part of the paths
			
			rawPath.push(rawName);
			String rawPathString = asString(rawPath);
			storeDiscriminatorAttributes(attributes, discriminatorAttributes, discriminatorAttributeNames, rawPathString);
			
			String discriminator = buildDisriminatorString(discriminatorAttributes);
			String name = discriminator == null ? rawName : rawName + ATTRIBUTE_SEPARATOR + discriminator;
			path.push(name);
			String pathString = asString(path);
			
			XmlElement parent = stack.isEmpty() ? null : stack.peek();
			XmlElement predecessor = parent == null ? null : parent.children.isEmpty() ? null : parent.children.get(parent.children.size() - 1);
			String firstDiscriminator = discriminatorAttributes.isEmpty() ? null : discriminatorAttributes.get(0);
			XmlElement currElement = new XmlElement(
					  xml
					, index
					, level
					, firstDiscriminator // no need to cache - attribute already cached
					, stringCache.cache(pathString)
					, stringCache.cache(rawPathString)
					, stringCache.cache(name)
					, stringCache.cache(rawName)
					, parent
					, predecessor
					, attributes
				);
			currElement.openTag = tagPosition;
			if (parent  != null) {
				parent.children.add(currElement);
			}
			stack.push(currElement);
			xml.elements.add(currElement);
			level++;
			index++;
			
			if (level > xml.depth) {
				xml.depth = level;
			}
		}
		
		@Override
		public void endElement(String name, TagPosition tagPosition) {
			XmlElement currElement = stack.pop();
			currElement.closeTag = tagPosition;
			path.pop();
			rawPath.pop();
			level--;
		}
	}
	
	private static void storeDiscriminatorAttributes(Map<String, String> attributes, List<String> discriminatorAttributes, List<String> discriminatorAttributeNames, String rawPath) {
		for (String specifier : discriminatorAttributeNames) {
			String key = AttributeSpecifier.stripKey(specifier, rawPath);
			String value = key == null ? null : attributes.get(key);
			if (value != null) {
				discriminatorAttributes.add(value);
			}
		}
	}
	
	private static String buildDisriminatorString(List<String> attributes) {
		if (attributes.isEmpty()) {
			return null;
		}
		StringBuilder buf = new StringBuilder();
		String sep = "";
		for (String value : attributes) {
			buf.append(sep).append(value);
			sep = ATTRIBUTE_SEPARATOR;
		}
		return buf.toString();
	}
	
	private static String asString(List<String> path) {
		StringBuilder buf = new StringBuilder();
		String sep = "";
		for (String s : path) {
			buf.append(sep).append(s);
			sep = LEVEL_SEPARATOR;
		}
		return buf.toString();
	}
}
