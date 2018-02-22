package com.dynatrace.profilediff;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.xml.stream.XMLStreamException;

/**
 * Merges two XML structures.
 * Actually, this merger repeatedly parses, diffs and merges a document until all changes are applied. 
 *  
 * @author cwat-pgrasboe
 */
public class FastXmlMerger3 extends XmlMerger3 {
	
	FastXmlMerger3(XmlLexer lexer, XmlDiffer differ) {
		super(lexer, differ);
	}
	
	@Override
	protected XmlStruct mergeAttributes(XmlStruct merged, XmlStruct mod, boolean addComments, IncludeElementCallback callback, StringBuilder result, AtomicBoolean stop) throws XMLStreamException {
		diffStep(merged, mod, stop);
		handleAllAttributeChanges(merged, mod, addComments, result, callback);
		return lexer.parse(result);
	}
	
	private static void handleAllAttributeChanges(XmlStruct merged, XmlStruct mod, boolean addComments, StringBuilder result, IncludeElementCallback callback) {
		/*
		 * order is very important: we iterate backwards so that indices don't have to be adapted.
		 * we iterate over the merged (i.e. ORIGINAL) order!
		 * Note - we don't do prevInsertionPos > insertionPos check, it's not needed (it's also tested)
		 */
		for (int i = merged.elements.size() - 1; i >= 0; i--) {  // merge backwards - makes life so much easier
			XmlElement element = merged.elements.get(i).peer; // it's crucial to use the peer (from mod document) since we iterate over base (merged)
			if (element != null && element.attributeDiff == DiffState.changed && !element.handled) {
				element.handled = true;
				if (callback.replaceAttributes(element)) {
					handleAttributeChange(element, addComments, result);
				}
			}
		}
	}
	
	@Override
	protected XmlStruct mergeInsertions(XmlStruct merged, XmlStruct mod, boolean addComments, IncludeElementCallback callback, StringBuilder result, AtomicBoolean stop) throws XMLStreamException {
		diffStep(merged, mod, stop);
		boolean complete = handleAllInsertions(merged, mod, addComments, result, callback);
		merged = lexer.parse(result);
		if (complete) {
			return merged;
		}
		// still need to safe merge all the elements that couldn't be handled using fast algorithm.
		return super.mergeInsertions(merged, mod, addComments, callback, result, stop);
	}
	
	private static boolean handleAllInsertions(XmlStruct merged, XmlStruct mod, boolean addComments, StringBuilder result, IncludeElementCallback callback) {
		/*
		 * order is very important: we iterate backwards so that indices don't have to be adapted.
		 * we iterate over the mod document.
		 * we can only fast-merge merged that are not "insert into parent",
		 * and also, only if the insertion pos is also declining.
		 */
		int skipped = 0;
		XmlElement prevInsertionPos = null;
		for (int i = mod.elements.size() - 1; i >= 0; i--) {  // merge backwards - makes life so much easier
			XmlElement element = mod.elements.get(i);
			if (element.structureDiff == DiffState.changed && !element.handled) {
				XmlElement insertionPos = findInsertionPos(element);
				if (isInsertionIntoEmptyParent(element, insertionPos)) {
					skipped++;
					continue; // this cannot be handled with fast merging, skip.
				}
				if (prevInsertionPos != null && prevInsertionPos.openTag.prevEnd < insertionPos.openTag.prevEnd) {
					skipped++;
					continue; // if the insertionPos are not declining, we must also skip it.
				}
				element.handled = true;
				if (callback.addElement(element)) {
//					System.out.println(element.openTag.prevEnd + " -> " + insertionPos.openTag.prevEnd + " " + element.toPathString());
					handleInsertionNormal(element, insertionPos, addComments, result);
				}
				prevInsertionPos = insertionPos;
			}
		}
		
		return skipped == 0; // i.e. complete
	}
}
