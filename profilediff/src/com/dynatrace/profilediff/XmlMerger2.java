package com.dynatrace.profilediff;


@Deprecated
public class XmlMerger2 {
	
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
		for (XmlElement element : xml.elements) {
			element.handled = false;
		}
	}
	
	static void validate(CharSequence text) {
		for (int i = 0; i < text.length(); i++) {
			if (text.charAt(i) == 0) {
				throw new IllegalStateException("invalid zero marker at position " + i);
			}
		}
	}
	
	int prevInsertionIndex;
	int prevReplaceIndex;
	
	/**
	 * Merge the changes into the original structure and return it.
	 * @param addComments whether to add comments about changes in the XML result
	 */
	public String merge(XmlStruct original, XmlStruct changed, boolean addComments, IncludeElementCallback callback) {
		validate(original);
		validate(changed);
		validate(original.data);
		validate(changed.data);
		StringBuilder buf = new StringBuilder(original.data);
		
		// first, we prepare deletions. This step does not change any line offsets
		handleDeletions(original, addComments, buf, callback);
		// now, we actually insert.
		handleInsertions(changed, addComments, buf, callback);
		
		String result = toStringNonZeroes(buf);
		validate(result);
		return result;
	}
	
	private static String toStringNonZeroes(StringBuilder result) {
		StringBuilder buf = new StringBuilder(result.length());
		
		for (int i = 0; i < result.length(); i++) {
			char ch = result.charAt(i);
			if (ch != 0) {
				buf.append(ch);
			}
		}
		
		return buf.toString();
	}
	
	private static void handleDeletions(XmlStruct original, boolean addComments, StringBuilder result, IncludeElementCallback callback) {
		for (int i = original.elements.size() - 1; i >= 0; i--) {  // merge backwards - makes life so much easier
			XmlElement element = original.elements.get(i);
			if (element.structureDiff == DiffState.changed && callback.removeElement(element)) {
				handleDeletion(element, addComments, result);
			}
		}
	}
	
	private static void handleDeletion(XmlElement difference, boolean addComments, StringBuilder result) {
//		System.out.println("Merge deletion: " + difference.toPathString());
		
		for (int i = difference.openTag.prevEnd; i < difference.closeTag.end; i++) {
			result.setCharAt(i, (char) 0);
		}
		
		// we just null out lines to be removed later. this avoids headache with line offset between deletions and insertions.
//		if (addComments) {
//			// that's not easily possible
//			String text = "\n<!-- @xmlmerge: lines " + difference.toPositionString() + " removed -->";
//			result.insert(difference.fromOffset, text);
//		}
	}
	
	private void handleInsertions(XmlStruct changed, boolean addComments, StringBuilder result, IncludeElementCallback callback) {
		prevInsertionIndex = -1;
		prevReplaceIndex = -1;
		
		for (int i = changed.elements.size() - 1; i >= 0; i--) {  // merge backwards - makes life so much easier
			XmlElement element = changed.elements.get(i);
			if (element.structureDiff == DiffState.changed && callback.addElement(element)) {
				handleInsertion(element, changed, addComments, result);
			}
		}
	}
	
	private void handleInsertion(XmlElement difference, XmlStruct changed, boolean addComments, StringBuilder result) {
		XmlElement insertionPos = findInsertionPosInPredecessors(difference);
		boolean insertIntoParent = false;
		if (insertionPos == null) {
			insertionPos = findInsertionPosInParent(difference);
			insertIntoParent = true;
		}
		
//		System.out.println("Merge insertion: " + difference.toPathString() + " at position: " + insertionPos.toPathString() + " into parent: " + insertIntoParent + " already replaced: " + insertionPos.handled);
		
		int insertionIndex;
		
		if (insertIntoParent) {
			/*
			 * If we run here, it's either an empty tag, or the new element is the first and hence there is no predecessor.
			 */
			if (insertionPos.children.isEmpty()) {
				if (!insertionPos.handled) {
					insertionPos.handled = true;
					/*
					 * let's not run in here more than once in case of several new children since we replace the empty tag with the whole new tag incl. all children anyway.
					 * FIXME: bug: we replace all children even though not all of them might be selected.
					 * to optionalClosingTag instead. replace 
					 */
					XmlElement parent = difference.parent;
					String toReplace = changed.data.subSequence(parent.openTag.prevEnd, parent.closeTag.end).toString();
					if (addComments) {
						String before = "\n<!-- @xmlmerge: begin replaced lines -->\n";
						String after = "\n<!-- @xmlmerge: end replaced lines -->";
						toReplace = before + toReplace + after;
					}
					int replaceIndex = insertionPos.openTag.prevEnd;
					if (prevReplaceIndex != -1 && prevInsertionIndex < replaceIndex) {
						/*
						 * shouldn't happen since insertionPos is from parent, not from predecessors, and the replaced flag was set on parent peer
						 */
						throw new IllegalStateException("Ignoring replacement since index order is wrong: " + replaceIndex + ", prev: " + prevReplaceIndex + ", insertion: " + difference.toPathString());
					}
					prevReplaceIndex = replaceIndex;
					result.replace(replaceIndex, insertionPos.closeTag.end, toReplace);
				}
				return;
			}
			insertionIndex = insertionPos.openTag.end;
		} else {
			insertionIndex = insertionPos.closeTag.end;
		}
		
		if (prevInsertionIndex != -1 && prevInsertionIndex < insertionIndex) {
			/*
			 * let's correct the insertion pos to the parent, since the predecessor's peer is to far and would cause index trouble.
			 */
			insertionPos = findInsertionPosInParent(difference);
			insertIntoParent = true;
			insertionIndex = insertionPos.openTag.end;
		}
		if (prevInsertionIndex != -1 && prevInsertionIndex < insertionIndex) {
			/*
			 * shouldn't happen since we just corrected for that
			 */
			throw new IllegalStateException("Ignoring insertion since index order is wrong: " + insertionIndex + ", prev: " + prevInsertionIndex + ", insertion: " + difference.toPathString());
			
		}
		prevInsertionIndex = insertionIndex;
		
		if (addComments) {
			String text = "\n<!-- @xmlmerge: begin inserted lines -->";
			result.insert(insertionIndex, text);
			insertionIndex += text.length();
		}
		
		String toInsert = changed.data.subSequence(difference.openTag.prevEnd, difference.closeTag.end).toString();
		result.insert(insertionIndex, toInsert);
		insertionIndex += toInsert.length();
		
		if (addComments) {
			String text = "\n<!-- @xmlmerge: end inserted lines -->";
			result.insert(insertionIndex, text);
			insertionIndex += text.length();
		}
	}
}
