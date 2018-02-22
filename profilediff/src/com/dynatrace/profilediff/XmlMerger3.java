package com.dynatrace.profilediff;

import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.xml.stream.XMLStreamException;

/**
 * Merges two XML structures.
 * Actually, this merger repeatedly parses, diffs and merges a document until all changes are applied. 
 *  
 * @author cwat-pgrasboe
 */
public class XmlMerger3 {
	
	public static interface IncludeElementCallback {
		default boolean addElement(XmlElement element) { return false; } 
		default boolean removeElement(XmlElement element) { return false; }
		default boolean replaceAttributes(XmlElement element) { return false; }
	}

	protected static void validate(XmlStruct xml) {
		if (xml.elements == null || xml.elements.size() == 0) {
			throw new IllegalStateException("no elements");
		}
		if (xml.root().peer == null) {
			throw new IllegalStateException("documents with different roots are not mergeable");
		}
	}
	
	protected final XmlLexer lexer;
	private final XmlDiffer differ;
	
	XmlMerger3(XmlLexer lexer, XmlDiffer differ) {
		this.lexer = lexer;
		this.differ = differ;
	}
	
	/**
	 * Completely merges two documents.
	 */
	public final XmlStruct merge(CharSequence baseData, CharSequence modData, boolean addComments, IncludeElementCallback callback, AtomicBoolean stop) throws XMLStreamException {
		XmlStruct base = lexer.parse(baseData);
		XmlStruct mod = lexer.parse(modData);
		diffStep(base, mod, stop);
		return merge0(base, mod, addComments, callback, stop);
	}
	
	/**
	 * Completely merges two documents.
	 * ATTENTION: this will change diff+merge state of both documents
	 */
	public final XmlStruct merge(XmlStruct base, XmlStruct mod, boolean addComments, IncludeElementCallback callback, AtomicBoolean stop) throws XMLStreamException {
		return merge0(base, mod, addComments, callback, stop);
	}
	
	private XmlStruct merge0(XmlStruct base, XmlStruct mod, boolean addComments, IncludeElementCallback callback, AtomicBoolean stop) throws XMLStreamException {
		StringBuilder result = new StringBuilder(base.data);
		validate(base);
		validate(mod);
		resetMergeState(base);
		resetMergeState(mod);
		
		try {
			/*
			 * mind this order. first, handle deletions, then attributes BEFORE insertions, so it's guaranteed that it's 
			 * possible to not merge attributes in the replace-parent (insertIntoParent) case. Plus, the number of calls to
			 * the callback is deterministic, then.
			 * It's still not correct since the insertion automatically always uses the new attributes in insertIntoParent mode,
			 * but that's O.K. for now.
			 */
			XmlStruct merged = base;
			if (base.root().nStructureChanged > 0) {
				merged = mergeDeletions(merged, mod, addComments, callback, result, stop);
			}
			if (merged.root().nAttributeChanged > 0) {
				merged = mergeAttributes(merged, mod, addComments, callback, result, stop);
			}
			if (mod.root().nStructureChanged > 0) {
				merged = mergeInsertions(merged, mod, addComments, callback, result, stop);
			}
			return merged;
		} catch (XMLStreamException e) {
			try (FileWriter out = new FileWriter("error.xml")) {
				out.write(result.toString());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			throw e;
		}
	}
	
	private static void resetMergeState(XmlStruct xml) {
		for (XmlElement element : xml.elements) {
			element.handled = false;
			if (element.attributeDiff == DiffState.changed && element.structureDiff == DiffState.changed) {
				throw new IllegalStateException("An element cannot be changed in both ways");
			}
		}
	}
	
	protected void diffStep(XmlStruct merged, XmlStruct mod, AtomicBoolean stop) {
		if (stop.get()) {
			throw new MergeStopException(merged, "Merging was stopped, but resulting document is valid.");
		}
		differ.diff(merged, mod);
		validate(merged);
		validate(mod);
	}

	protected final XmlStruct mergeDeletions(XmlStruct base, XmlStruct mod, boolean addComments, IncludeElementCallback callback, StringBuilder result, AtomicBoolean stop) throws XMLStreamException {
		handleDeletions(base, addComments, result, callback);
		XmlStruct merged = lexer.parse(result);
		diffStep(merged, mod, stop);
		return merged;
	}
	
	private static void handleDeletions(XmlStruct base, boolean addComments, StringBuilder result, IncludeElementCallback callback) {
		for (int i = base.elements.size() - 1; i >= 0; i--) {  // merge backwards - makes life so much easier
			XmlElement element = base.elements.get(i);
			if (element.structureDiff == DiffState.changed && !element.handled) {
				element.handled = true;
				if (callback.removeElement(element)) {
					handleDeletion(element, addComments, result);
				}
			}
		}
	}
	
	private static void handleDeletion(XmlElement difference, boolean addComments, StringBuilder result) {
//		System.out.println("Merge deletion: " + difference.toPathString());
		
		String replacement = addComments ? "\n<!-- @xmlmerge: lines " + difference.toPositionString() + " removed -->" : "";
		result.replace(difference.openTag.prevEnd, difference.closeTag.end, replacement);
	}
	
	protected XmlStruct mergeInsertions(XmlStruct merged, XmlStruct mod, boolean addComments, IncludeElementCallback callback, StringBuilder result, AtomicBoolean stop) throws XMLStreamException {
		diffStep(merged, mod, stop);
		int prevStructureChanged = mod.root().nStructureChanged;
		
		while (handleNextInsertion(mod, addComments, result, callback)) {
			merged = lexer.parse(result);
			diffStep(merged, mod, stop);
			int nStructureChanged = mod.root().nStructureChanged;
			assert nStructureChanged == prevStructureChanged - 1 :  "wrong insertion change counts prev=" + prevStructureChanged + ", curr=" + nStructureChanged + ", differ=" + differ;
			prevStructureChanged = nStructureChanged;
		}
			
		return merged;
	}
	
	private static boolean handleNextInsertion(XmlStruct mod, boolean addComments, StringBuilder result, IncludeElementCallback callback) {
		for (int i = 0; i < mod.elements.size(); i++) {
			XmlElement element = mod.elements.get(i);
			if (element.structureDiff == DiffState.changed && !element.handled) {
				element.handled = true;
				if (callback.addElement(element)) {
					handleInsertion(element, addComments, result);
					return true;
				}
			}
		}
		return false;
	}
	
	protected static boolean isInsertionIntoEmptyParent(XmlElement difference, XmlElement insertionPos) {
		return insertionPos == difference.parent.peer && insertionPos.children.isEmpty();
	}
	
	private static void handleInsertion(XmlElement difference, boolean addComments, StringBuilder result) {
		XmlElement insertionPos = findInsertionPos(difference);
		if (isInsertionIntoEmptyParent(difference, insertionPos)) {
			handleInsertionIntoEmptyParent(difference, insertionPos, addComments, result);
		} else {
			handleInsertionNormal(difference, insertionPos, addComments, result);
		}
	}
	
	protected static void handleInsertionNormal(XmlElement difference, XmlElement insertionPos, boolean addComments, StringBuilder result) {
//		System.out.println("Merge insertion: " + difference.toPathString() + " at position: " + insertionPos.toPathString() + " into parent: " + insertIntoParent + " parent empty: " + (insertIntoParent ? insertionPos.children.isEmpty() : "-false-"));
		int insertionIndex;
		CharSequence newContent =  difference.xml.data.subSequence(difference.openTag.prevEnd, difference.closeTag.end);
		if (insertionPos == difference.parent.peer) { // i.e. insert into parent
			assert !insertionPos.children.isEmpty() : "wrong insertion handler";
			insertionIndex = insertionPos.openTag.end; // inside not-empty parent
		} else {
			insertionIndex = insertionPos.closeTag.end; // after predecessor
		}
		
		if (addComments) {
			insertionIndex += doInsert(result, insertionIndex, "\n<!-- @xmlmerge: lines " + difference.toPositionString() + " inserted -->");
		}
		insertionIndex += doInsert(result, insertionIndex, newContent);
	}
	
	protected static void handleInsertionIntoEmptyParent(XmlElement difference, XmlElement insertionPos, boolean addComments, StringBuilder result) {
		CharSequence newContent =  difference.xml.data.subSequence(difference.openTag.prevEnd, difference.closeTag.end);
		int insertionIndex = insertionPos.openTag.prevEnd;

		//FIXME this automatically merges the parent's attributes, too, so they can't be not-merged (rejected)
		CharSequence newOpenTag = difference.xml.data.subSequence(difference.parent.openTag.prevEnd, difference.parent.openTag.end);
		CharSequence newCloseTag = difference.xml.data.subSequence(difference.parent.closeTag.prevEnd, difference.parent.closeTag.end);
		
		result.replace(insertionPos.openTag.prevEnd, insertionPos.closeTag.end, "");
		insertionIndex += doInsert(result, insertionIndex, newOpenTag);
		if (addComments) {
			insertionIndex += doInsert(result, insertionIndex, "\n<!-- @xmlmerge: lines " + difference.toPositionString() + " inserted  (empty parent replaced) -->");
		}
		insertionIndex += doInsert(result, insertionIndex, newContent);
		insertionIndex += doInsert(result, insertionIndex, newCloseTag);
	}
	
	protected static XmlElement findInsertionPos(XmlElement difference) {
		XmlElement insertionPos = findInsertionPosInPredecessors(difference);
		if (insertionPos == null) {
			insertionPos = findInsertionPosInParent(difference);
		}
		return insertionPos;
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

	protected XmlStruct mergeAttributes(XmlStruct merged, XmlStruct mod, boolean addComments, IncludeElementCallback callback, StringBuilder result, AtomicBoolean stop) throws XMLStreamException {
		diffStep(merged, mod, stop);
		int prevAttributeChanged = mod.root().nAttributeChanged;
		
		while (handleNextAttributeChange(mod, addComments, result, callback)) {
			merged = lexer.parse(result);
			diffStep(merged, mod, stop);
			int nAttributeChanged = mod.root().nAttributeChanged;
			assert nAttributeChanged == prevAttributeChanged - 1 : "wrong attribute change counts prev=" + prevAttributeChanged + ", curr=" + nAttributeChanged + ", differ=" + differ;
			prevAttributeChanged = nAttributeChanged;
		}
		
		return merged;
	}
	
	private static boolean handleNextAttributeChange(XmlStruct mod, boolean addComments, StringBuilder result, IncludeElementCallback callback) {
		for (int i = 0; i < mod.elements.size(); i++) {
			XmlElement element = mod.elements.get(i);
			if (element.attributeDiff == DiffState.changed && !element.handled) {
				element.handled = true;
				if (callback.replaceAttributes(element)) {
					handleAttributeChange(element, addComments, result);
					return true;
				}
			}
		}
		return false;
	}
	
	protected static void handleAttributeChange(XmlElement difference, boolean addComments, StringBuilder result) {
		XmlElement peer = difference.peer;
		assert peer != null;
		assert peer.rawName.equals(difference.rawName);
		
		int peerStartOffset = 1 + peer.rawName.length(); // "<tagname"
		int peerEndOffset = peer.isEmpty() ? 2 : 1; // '>' or '/>'
		CharSequence toRemove = peer.xml.data.subSequence(peer.openTag.start + peerStartOffset, peer.openTag.end - peerEndOffset);
		result.replace(peer.openTag.start + peerStartOffset, peer.openTag.end - peerEndOffset, ""); // remove old attributes
		int insertionIndex = peer.openTag.start;
		if (addComments) {
			insertionIndex += doInsert(result, insertionIndex, "<!-- @xmlmerge: " + toRemove + " -->");
		}
		insertionIndex += peerStartOffset;
		int startOffset = 1 + difference.rawName.length(); // "<tagname"
		int endOffset = difference.isEmpty() ? 2 : 1; // '>' or '/>'
		CharSequence toInsert = difference.xml.data.subSequence(difference.openTag.start + startOffset, difference.openTag.end - endOffset);
		insertionIndex += doInsert(result, insertionIndex, toInsert);
//		System.out.println("Merge attributes: old: " + toRemove + ", new: " + toInsert); 
		insertionIndex += peerEndOffset;
	}
	
	private static int doInsert(StringBuilder buf, int index, CharSequence text) {
		buf.insert(index, text);
		return text.length();
	}
}
