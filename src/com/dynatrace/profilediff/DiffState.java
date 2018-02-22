package com.dynatrace.profilediff;

enum DiffState {
	unchanged
	, changed           // this was added
	, @Deprecated 		//THIS IS ONLY KEPT FOR TEST PURPOSES. Descendant changes are derived from counters. 
	  descendantChanged // something in the descendants of this element was changed. 
	, parentChanged     // the parent or one of its parents was changed
}