package com.dynatrace.profilediff;

import java.io.Reader;
import java.io.StringReader;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;

import com.dynatrace.common.stringcache.StringCacheInterface;
import com.dynatrace.profilediff.lib.CharSequenceReader;

/**
 * A very simplistic xml parser in the sax style with events, but with CORRECT character offsets and line numbers.
 * To have correct offsets which is crucial for correct merging, DO NOT USE reader.getLocation() EVER!
 * 
 * @author cwat-pgrasboe
 */
class XmlParser {
	
	interface Handler {
		void startElement(String name, SortedMap<String, String> attributes, TagPosition tagPosition);
		void endElement(String name, TagPosition tagPosition);
	}
	
	private static int countLF(CharSequence text, int fromIndex, int toIndex) {
		int n = 0;
		for (int i = fromIndex; i < toIndex; i++) {
			char ch = text.charAt(i);
			if (ch == '\n') {
				n++;
			}
		}
		return n;
	}
	
	// that's much more efficient than comparing with substring()
	private static boolean regionEquals(CharSequence left, CharSequence right, int leftOffset, int rightOffset) {
		for (int leftLen = left.length(), rightLen = right.length(); leftOffset < leftLen && rightOffset < rightLen; leftOffset++, rightOffset++) {
			if (left.charAt(leftOffset) != right.charAt(rightOffset)) {
				return false;
			}
		}
		
		return true;
	}
	
	private static int findOpenTagForward(CharSequence text, CharSequence tag, int pos) {
		int scan = pos;
		for (;;) {
			int i = seekForward(text, '<', scan);
			if (regionEquals(text, tag, i + 1, 0)) {
				assert i >= pos : "findOpenTagForward failed";
				return i;
			}
			scan = i + 1;
		}
	}
	
	private static int findCloseTagForward(CharSequence text, CharSequence tag, int pos) {
		int scan = pos;
		for (;;) {
			int i = seekForward(text, '<', scan);
			if (text.charAt(i + 1) == '/' && regionEquals(text, tag, i + 2, 0)) {
				assert i >= pos : "findCloseTagForward failed";
				return i;
			}
			scan = i + 1;
		}
	}

	private static int seekForward(CharSequence text, char ch, int pos) {
		int len = text.length();
		while (pos < len) {
			if (text.charAt(pos) == ch) {
				return pos;
			}
			pos++;
		}
		assert false : "seekForward failed";
		return -1;
	}
	
	private static SortedMap<String, String> getAttributes(XMLStreamReader reader, StringCacheInterface stringCache) {
		SortedMap<String, String> attributes = new TreeMap<String, String>();
		for (int i = 0, count = reader.getAttributeCount(); i < count; i++) {
			attributes.put(stringCache.cache(reader.getAttributeLocalName(i)), stringCache.cache(reader.getAttributeValue(i)));
		}
		return attributes;
	}
	
	static void parse(CharSequence text, Handler handler, StringCacheInterface stringCache) throws XMLStreamException {
		Reader charReader = text instanceof String ? new StringReader((String) text) : new CharSequenceReader(text);
		XMLStreamReader reader = XMLInputFactory.newFactory().createXMLStreamReader(charReader);
		String tagname;
		int tagStart;
		int tagEnd = -1;
		int lineStart;
		int lineNumber = 1; // 1-based line number. have to count line numbers ourselves since stax only offers unpredictable and unreliable approximations ...
		int prevTagEnd = 0; // is passed to handler and also important to correctly count line numbers
		boolean empty = false;
		
		while (reader.hasNext()) {
			switch (reader.next()) {
				case XMLEvent.START_ELEMENT:
					tagname = reader.getLocalName();
					tagStart = findOpenTagForward(text, tagname, prevTagEnd);
					tagEnd = seekForward(text, '>', tagStart) + 1;
					empty = text.charAt(tagEnd - 2) == '/';
					lineNumber += countLF(text, prevTagEnd, tagStart);
					lineStart = lineNumber;
					lineNumber += countLF(text, tagStart, tagEnd);
					
					assert tagStart >= 0 : "bad tagStart: " + tagStart;
					assert tagStart >= prevTagEnd : "bad tagStart: " + tagStart;
					assert tagEnd > tagStart : "bad tagEnd: " + tagEnd;
					assert prevTagEnd >= 0 : "bad prevTagEnd: " + prevTagEnd;
					assert lineStart > 0 : "bad lineStart: " + lineStart;
					assert lineNumber > 0 : "bad lineNumber: " + lineNumber;
					
					handler.startElement(tagname, getAttributes(reader, stringCache), new TagPosition(prevTagEnd, tagStart, tagEnd, lineStart, lineNumber)); 
					prevTagEnd = tagEnd;
					break;
				
				case XMLEvent.END_ELEMENT:
					tagname = reader.getLocalName();
					tagStart  = empty ? -1 : findCloseTagForward(text, tagname, prevTagEnd);
					tagEnd = empty ? tagEnd : seekForward(text, '>', tagStart + 1) + 1;
					lineNumber += empty ? 0 : countLF(text, prevTagEnd, tagStart);
					lineStart = lineNumber;
					lineNumber += empty ? countLF(text, prevTagEnd, tagEnd) : countLF(text, tagStart, tagEnd);
					
					assert empty || tagStart >= 0 : "bad tagStart: " + tagStart;
					assert empty || tagStart >= prevTagEnd : "bad tagStart: " + tagStart;
					assert tagEnd > tagStart : "bad tagEnd: " + tagEnd;
					assert prevTagEnd >= 0 : "bad prevTagEnd: " + prevTagEnd;
					assert lineStart > 0 : "bad lineStart: " + lineStart;
					assert lineNumber > 0 : "bad lineNumber: " + lineNumber;
					
					handler.endElement(tagname, new TagPosition(empty ? -1 : prevTagEnd, tagStart, tagEnd, lineStart, lineNumber)); 
					prevTagEnd = tagEnd;
					empty = false;
					break;
			}
		}
	}
}
