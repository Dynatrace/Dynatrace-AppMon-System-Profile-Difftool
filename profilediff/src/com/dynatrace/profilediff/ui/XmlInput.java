package com.dynatrace.profilediff.ui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import javax.xml.stream.XMLStreamException;

import com.dynatrace.profilediff.XmlStruct;

class XmlInput {
	
	final String name;
	final File file;
	final CharSequence xmlData;
	
	private XmlInput(String name, File file, CharSequence xmlData) {
		this.name = name;
		this.file = file;
		this.xmlData = xmlData;
	}
	
	static XmlInput fromFile(File file) {
		if (file == null) {
			throw new IllegalArgumentException("file must not be null");
		}
		return new XmlInput(file.getName(), file, /*xmlData*/ null);
	}
	
	static XmlInput fromText(String name, CharSequence xmlData) {
		if (name == null) {
			throw new IllegalArgumentException("name must not be null");
		}
		if (xmlData == null) {
			throw new IllegalArgumentException("xmlData must not be null");
		}
		return new XmlInput(name, /*file*/ null, xmlData);
	}
	
	XmlStruct fetch() throws XMLStreamException, IOException {
		if (file != null) {
			return ProfileDiff.parse(readFully(new FileReader(file)));
		} else if (xmlData != null) {
			return ProfileDiff.parse(xmlData);
		} else {
			throw new IllegalStateException("have neither file nor xmlData");
		}
	}
	
	private static String readFully(Reader reader) throws IOException {
		StringBuilder buf = new StringBuilder();
		try (BufferedReader in = new BufferedReader(reader)) {
			String line;
			while ((line = in.readLine()) != null) {
				buf.append(line).append("\n");
			}
			return buf.toString();
		}
	}
}
