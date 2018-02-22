package com.dynatrace.profilediff;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Deprecated
public class XmlMerger {
	
	private static String getIndent(String line) {
		int i = 0;
		while (i < line.length() && Character.isWhitespace(line.charAt(i))) {
			i++;
		}
		return line.substring(0, i);
	}
	
	private static XmlElement findInsertionPosInPredecessors(XmlElement difference) {
		assert difference.peer == null : "difference must not have a peer: " + difference + "; peer: " + difference.peer + "; refs equal: " + (difference.peer == difference) + "; maybe the peer has not been reset properly";
		
		for (XmlElement element = difference; element != null; element = element.pred) {
			if (element.peer != null) {
				return element.peer;
			}
		}
		return null;
	}
	
	private static XmlElement findInsertionPosInParent(XmlElement difference) {
		assert difference.peer == null : "difference must not have a peer: " + difference + "; peer: " + difference.peer + "; refs equal: " + (difference.peer == difference) + "; maybe the peer has not been reset properly";
		assert difference.parent != null : "difference must have a parent: " + difference;
		assert difference.parent.peer != null : "parent of difference must have a peer: " + difference + "; parent: " + difference.parent;
		return difference.parent.peer;
	}
	
	public static interface IncludeElementCallback {
		boolean addElement(XmlElement element);
		boolean removeElement(XmlElement element);
	}
	
	static void validate(XmlStruct xml) {
		if (xml.elements == null || xml.elements.size() == 0) {
			throw new IllegalStateException("no elements");
		}
		if (xml.root().peer == null) {
			throw new IllegalStateException("documents with different roots are not mergeable");
		}
	}
	
	/**
	 * Merge the changes into the original structure and return it.
	 * @param addComments whether to add comments about changes in the XML result
	 */
	public String[] merge(XmlStruct original, XmlStruct changed, boolean addComments, IncludeElementCallback callback) {
		validate(original);
		validate(changed);
		List<String> result = new ArrayList<>(Arrays.asList(original.lines));
		// first, we prepare deletions. This step does not change any line offsets
		handleDeletions(original, addComments, result, callback);
		// now, we actually insert.
		handleInsertions(changed, addComments, result, callback);
		// finally, toArrayNonNull gives us all lines, except the ones marked as deleted by being nulled out.
		return toArrayNonNull(result);
	}
	
	private static String[] toArrayNonNull(List<String> list) {
		int size = list.size();
		int length = size;
		
		for (int i = 0; i < size; i++) {
			if (list.get(i) == null) {
				length--;
			}
		}
		
		String[] array = new String[length];
		for (int i = 0, j = 0; i < size; i++) {
			String element = list.get(i);
			if (element != null) {
				array[j++] = element; 
			}
		}
		
		return array;
	}
	
	private static void handleDeletions(XmlStruct original, boolean addComments, List<String> result, IncludeElementCallback callback) {
		for (int i = original.elements.size() - 1; i >= 0; i--) {  // merge backwards - makes life so much easier
			XmlElement element = original.elements.get(i);
			if (element.structureDiff == DiffState.changed && callback.removeElement(element)) {
				handleDeletion(element, addComments, result);
			}
		}
	}
	
	private static void handleDeletion(XmlElement difference, boolean addComments, List<String> result) {
//		System.out.println("Merge deletion: " + difference.toPathString());
		
		for (int i = difference.openTag.lineStart; i <= difference.closeTag.lineStart; i++) {
			result.set(i - 1, null);
		}
		// we just null out lines to be removed later. this avoids headache with line offset between deletions and insertions.
		if (addComments) {
			result.set(difference.openTag.lineStart - 1, "<!-- @xmlmerge: lines " + difference.toPositionString() + " removed -->");
		}
	}
	
	private static void handleInsertions(XmlStruct changed, boolean addComments, List<String> result, IncludeElementCallback callback) {
		for (int i = changed.elements.size() - 1; i >= 0; i--) {  // merge backwards - makes life so much easier
			XmlElement element = changed.elements.get(i);
			if (element.structureDiff == DiffState.changed && callback.addElement(element)) {
				handleInsertion(element, changed, addComments, result);
			}
		}
	}
	
	private static void handleInsertion(XmlElement difference, XmlStruct changed, boolean addComments, List<String> result) {
		XmlElement insertionPos = findInsertionPosInPredecessors(difference);
		boolean insertIntoParent = false;
		if (insertionPos == null) {
			insertionPos = findInsertionPosInParent(difference);
			insertIntoParent = true;
		}
		
//		System.out.println("Merge insertion: " + difference.toPathString() + " at position: " + insertionPos.toPathString() + " into parent: " + insertIntoParent);
		
		String optionalClosingTag = null; // might need to be added for empty tags.
		int startIndex;
		
		if (insertIntoParent) {
			/*
			 * If we run here, it's either an empty tag, or the new element is the first and hence there is no predecessor.
			 */
			if (insertionPos.openTag.lineStart == insertionPos.closeTag.lineStart) check: {
				/*
				 * probably an empty xml element. we have to add the end tag and remove the "/>" empty syntax
				 */
				int index = insertionPos.openTag.lineStart - 1;
				String lineWithEmptyTag = result.get(index);
				if (lineWithEmptyTag != null) {
					String indent = getIndent(lineWithEmptyTag);
					String closingTag = "</" + insertionPos.rawName + ">";
					String lineWithOpenTag;
					String comment;
					/*
					 * FIXME: these checks won't work if <!-- xml comments --> are after "/>"
					 */
					if (lineWithEmptyTag.trim().endsWith("/>")) {  // that's correct, whitespace between "/" and ">" is invalid!
						/* 
						 * empty tag syntax
						 */
						int i = lineWithEmptyTag.lastIndexOf("/>");
						assert i != -1;
						lineWithOpenTag = lineWithEmptyTag.substring(0, i).trim() + ">";
						comment = "<!-- @xmlmerge: empty tag changed to opening tag -->";
					} else if (lineWithEmptyTag.trim().endsWith(closingTag)) { 
						/*
						 * pseudo-empty tag with open and closing tag in same line
						 */
						int i = lineWithEmptyTag.lastIndexOf(closingTag);
						assert i != -1;
						lineWithOpenTag = lineWithEmptyTag.substring(0, i).trim();
						comment = "<!-- @xmlmerge: closing tag in same line removed -->";
					} else {
						/*
						 * more than one element was inserted in a previous empty element. goto end of this block.
						 */
						break check;
					}
					if (addComments) {
						result.set(index, indent + lineWithOpenTag + comment);
					} else {
						result.set(index, indent + lineWithOpenTag);
					}
					optionalClosingTag = indent + closingTag;
				}
			}
			startIndex = insertionPos.openTag.lineStart;
		} else {
			startIndex = insertionPos.closeTag.lineStart;
		}
		int index = startIndex;
		
		if (addComments) {
			result.add(index, "<!-- @xmlmerge: begin inserted lines -->");
			index++;
		}
		for (int i = difference.openTag.lineStart; i <= difference.closeTag.lineStart; i++) {
			boolean firstLine = i == difference.openTag.lineStart;
			boolean lastLine = i == difference.closeTag.lineStart;
			result.add(index, getLine(difference, changed.lines[i - 1], firstLine, lastLine)); 
			index++;
		}
		if (addComments) {
			result.add(index, "<!-- @xmlmerge: end inserted lines -->");
			index++;
		}
		if (optionalClosingTag != null) {
			if (addComments) {
				result.add(index, optionalClosingTag + "<!-- @xmlmerge: closing tag inserted since containing tag was empty -->");
			} else {
				result.add(index, optionalClosingTag);
			}
			index++;
		}
	}
	
	private static String getLine(XmlElement difference, String line, boolean firstLine, boolean lastLine) {
		if (firstLine || lastLine) {
			int fromIndex = 0;
			int toIndex = line.length();
			String indent = "";
			if (firstLine) {
//				fromIndex = difference.fromColumn - 1;
				indent = getIndent(line);
			}
			if (lastLine) {
//				toIndex = difference.toColumn - 1;
			}
			return indent + line.substring(fromIndex, toIndex);
		}
		return line;
	}
}
