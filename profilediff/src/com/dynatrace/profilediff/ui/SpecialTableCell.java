package com.dynatrace.profilediff.ui;

import com.dynatrace.profilediff.XmlElement;

// used for having special rendering of some table rows...
class SpecialTableCell {
	enum Kind {
		  ElementHeader
		, AttributeValueEqual
		, AttributeValueDifferent
		, AttributeAdded
		, AttributeRemoved
	}
	final SpecialTableCell.Kind kind;
	final String text;
	final XmlElement element;
	
	SpecialTableCell(String text, SpecialTableCell.Kind kind, XmlElement element) {
		this.text = text;
		this.kind = kind;
		this.element = element;
	}

	@Override
	public String toString() {
		return text;
	};
}