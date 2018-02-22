package com.dynatrace.profilediff;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

public class XmlElement implements Comparable<XmlElement> {
	
	private static final String TAG_DELETION_DESCENDANTS = "-";
	private static final String TAG_INSERTION_DESCENDANTS = "+";
	private static final String TAG_ATTRIBUTE_CHANGE_DESCENDANTS = "#";
	
	public static final String TAG_DELETION = "(-)";
	public static final String TAG_INSERTION = "(+)";
	public static final String TAG_ATTRIBUTE_CHANGE = "(#)";

	private static final TagPosition UNDEFINED = new TagPosition(-1, -1, -1, -1, -1);
	
	public final int index;
	public final int level;
	public final XmlStruct xml;
	public final String firstDiscriminator;
	public final String path;     // full path with discriminators on all levels
	public final String rawPath;  // full path without any discriminators
	public final String name;     // name with discriminators
	public final String rawName;  // name w/o discriminators
	public final XmlElement parent;
	public final XmlElement pred;
	public final SortedMap<String, String> attributes; // including ignored attributes
	public final List<XmlElement> children;
	
	public TagPosition openTag = UNDEFINED;
	public TagPosition closeTag = UNDEFINED;
	
	transient boolean visible = true; // for filtering

	// differ state
	transient String uniquePath;                      // might contain unique parts of children of ambiguous.
	public transient SortedMap<String, String> selectedAttributes; // excluding ignored attributes.
	public transient int nStructureChanged; // added or removed
	public transient int nAttributeChanged; // attributes differed
	public transient int distance;
	public transient int cardinality; //TODO can we store it differently?
	
	// differ state
	public transient XmlElement peer; // the one element considered equal to this element in the other document
	transient DiffState structureDiff = DiffState.unchanged;
	transient DiffState attributeDiff = DiffState.unchanged;
	
	// merger state
	// to avoid asking for one element to be merged over and over again.
	transient boolean handled;
	
	public transient Object[] userObjects; // normal use: model children
	
	@Override
	public String toString() {
		return path; // actually, this is not used by the tree model (which is good), since we have a custom renderer.
	}
	
	public String toPathString() {
		return path + " " + toPositionString();
	}
	
	public String toPositionString() {
		return "[" + openTag.lineStart + ":" + closeTag.lineEnd + "]"; // tests rely on this!
	}
	
	// this format is relied on by tests!
	public String toStateString() {
		return "structure:" + legacyStrucureDiff() + "/attr:" + legacyAttributeDiff() + ": " + toPathString();
	}
	
	// for test compatibility
	private DiffState legacyStrucureDiff() {
		if (structureDiff == DiffState.unchanged) {
			if (hasDescendantStructureChange() || hasPeerDescendantStructureChange()) {
				return DiffState.descendantChanged;
			}
		}
		return structureDiff;
	}
	
	// for test compatibility
	private DiffState legacyAttributeDiff() {
		if (attributeDiff == DiffState.unchanged) {
			if (hasDescendantAttributeChange()) {
				return DiffState.descendantChanged;
			}
		}
		return attributeDiff;
	}
	
	public String toOffsetString() {
		assert openTag.prevEnd <= openTag.start;
		assert openTag.start < openTag.end;
		if (closeTag.prevEnd != -1) {
			assert openTag.end <= closeTag.prevEnd;
			assert closeTag.prevEnd <= closeTag.start;
			assert closeTag.start < closeTag.end;
		}
		
		return path + " " + "[" + openTag.prevEnd + ":" + openTag.start + ":" + openTag.end + ":" + closeTag.prevEnd + ":" + closeTag.start + ":" + closeTag.end + "]"; // tests rely on this!
	}
	
	XmlElement(XmlStruct xml, int index, int level, String firstDiscriminator, String path, String rawPath, String name, String rawName, XmlElement parent, XmlElement pred, SortedMap<String, String> attributes) {
		this.xml = xml;
		this.index = index;
		this.level = level;
		this.firstDiscriminator = firstDiscriminator;
		this.path = path;
		this.rawPath = rawPath;
		this.name = name;
		this.rawName = rawName;
		this.parent = parent;
		this.pred = pred;
		this.attributes = attributes;
		this.children = new ArrayList<>();
	}
	
	/*
	 * don't implement hashCode and equals()
	 * we rely on object identity
	 */
	
	public CharSequence getContent() {
		return xml.getElementData(this);
	}
	
	/**
	 * Whether this is a tag with EMPTY SYNTAX, i.e. "<tag.../>"
	 * @return
	 */
	boolean isEmpty() {
		return closeTag.prevEnd == -1;
	}

	public boolean isDeletion() {
		return xml.isBase == Boolean.TRUE;
	}

	public boolean isInsertion() {
		return xml.isBase == Boolean.FALSE;
	}

	public boolean hasDirectAttributeChange() {
		return attributeDiff == DiffState.changed;
	}

	public boolean hasParentStructureChange() {
		return structureDiff == DiffState.parentChanged;
	}

	public boolean hasDirectStructureChange() {
		return structureDiff == DiffState.changed;
	}

	public boolean hasPeerDescendantStructureChange() {
		return peer != null && peer.nStructureChanged > 0;
	}

	public boolean hasDescendantAttributeChange() {
		return nAttributeChanged > 0;
	}

	public boolean hasDescendantStructureChange() {
		return nStructureChanged > 0;
	}

	public boolean hasNoChanges() {
		return structureDiff == DiffState.unchanged && attributeDiff == DiffState.unchanged;
	}
	
	public String getTag() {
		if (hasDirectAttributeChange()) {
			return TAG_ATTRIBUTE_CHANGE;
		}
		if (hasDirectStructureChange()) {
			return isInsertion() ? TAG_INSERTION : TAG_DELETION;
		}
		return null;
	}
	
	public String getDescendantsTag() {
		if (nAttributeChanged == 0 && nStructureChanged == 0 && (peer == null || peer.nStructureChanged == 0)) {
			return null; // no change.
		}
		StringBuilder buf = new StringBuilder();
		String sep = "";
		if (isDeletion() && nStructureChanged > 0) {
			buf.append(sep).append(TAG_DELETION_DESCENDANTS).append(nStructureChanged);
			sep = " ";
		} else if (peer != null && peer.isDeletion() && peer.nStructureChanged > 0) {
			buf.append(sep).append(TAG_DELETION_DESCENDANTS).append(peer.nStructureChanged);
			sep = " ";
		}
		if (isInsertion() && nStructureChanged > 0) {
			buf.append(sep).append(TAG_INSERTION_DESCENDANTS).append(nStructureChanged);
			sep = " ";
		} else if (peer != null && peer.isInsertion() && peer.nStructureChanged > 0) {
			buf.append(sep).append(TAG_INSERTION_DESCENDANTS).append(peer.nStructureChanged);
			sep = " ";
		}
		if (nAttributeChanged > 0) {
			buf.append(sep).append(TAG_ATTRIBUTE_CHANGE_DESCENDANTS).append(nAttributeChanged);
			sep = " ";
		}
		return buf.toString();
	}

	@Override
	public int compareTo(XmlElement o) {
		return name.compareTo(o.name);
	}

	public int getDistance() {
		return distance;
	}
}
