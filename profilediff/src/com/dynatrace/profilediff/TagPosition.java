package com.dynatrace.profilediff;

public class TagPosition {
	public final int prevEnd;   // -1 on close tag: tag is empty. otherwise tag start char offset of prev open/close tag
	public final int start;     // char offset of tag start
	public final int end;       // char offset of tag end
	public final int lineStart; // line number in which tag starts
	public final int lineEnd;  // line number in which tag ends (for multiline tags)
	
	public TagPosition(int prevEnd, int start, int end, int lineStart, int lineEnd) {
		this.prevEnd = prevEnd;
		this.start = start;
		this.end = end;
		this.lineStart = lineStart;
		this.lineEnd = lineEnd;
	}
}