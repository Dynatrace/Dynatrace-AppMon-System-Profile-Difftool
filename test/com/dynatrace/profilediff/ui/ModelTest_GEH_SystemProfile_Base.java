package com.dynatrace.profilediff.ui;

import java.io.FileReader;
import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import org.junit.Assert;
import org.junit.BeforeClass;

import com.dynatrace.profilediff.IO;
import com.dynatrace.profilediff.XmlDiffer;
import com.dynatrace.profilediff.XmlDifferFactory;
import com.dynatrace.profilediff.XmlDifferFactoryImpl;
import com.dynatrace.profilediff.XmlLexer;
import com.dynatrace.profilediff.XmlStruct;

public abstract class ModelTest_GEH_SystemProfile_Base extends ModelTestBase {
	
	static XmlStruct xmlLeft;
	static XmlStruct xmlRight;
	
	static final int insertions = 10;
	static final int deletions = 26;
	static final int attributes = 27;
	
	/**
	 * We statically load the documents only once
	 * and construct different models from the same documents.
	 * This way, the invalidation (modCount) of XmlStructTreeModel is also tested.
	 */
	@BeforeClass
	public static void beforeClass() throws XMLStreamException, IOException {
		XmlLexer lexer = new XmlLexer(SYSTEM_PROFILE_DISCRIMINATOR_ATTRIBUTES, stringCache);
		XmlDifferFactory xmlDifferFactory = XmlDifferFactoryImpl.newDefault(); 
		XmlDiffer differ = xmlDifferFactory.create(SYSTEM_PROFILE_IGNORED_ATTRIBUTES);
		
		String[] inputLeft = IO.readLines(new FileReader("samples/GEH_Staging.profile.xml"));
		String[] inputRight = IO.readLines(new FileReader("samples/GEH_Production.profile.xml"));
		
		xmlLeft = lexer.parse(IO.asString(inputLeft));
		xmlRight = lexer.parse(IO.asString(inputRight));
		
		XmlDiffer.Result diffResult = differ.diff(xmlLeft, xmlRight);
		Assert.assertEquals(insertions, diffResult.nAdded);
		Assert.assertEquals(deletions, diffResult.nRemoved);
		Assert.assertEquals(attributes, diffResult.nAttributeChanged);
	}
}
