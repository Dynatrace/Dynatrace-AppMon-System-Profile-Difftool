package com.dynatrace.profilediff;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.xml.stream.XMLStreamException;

import org.junit.Assert;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.dynatrace.profilediff.XmlMerger3.IncludeElementCallback;

@RunWith(Parameterized.class)
public abstract class DifferMergerTestBase extends TestBase {
	
	protected static final AtomicBoolean stop = new AtomicBoolean();
	
	@Parameters(name="{1}+{3}")
	public static Collection<Object[]> parameters() {
		return Arrays.asList(
  				  new Object[] { XmlMerger3FactoryImpl.newFast(), "FastMerger", XmlDifferFactoryImpl.newDefault(), "DefaultDiffer", -1 }
  				, new Object[] { XmlMerger3FactoryImpl.newFast(), "FastMerger", XmlDifferFactoryImpl.newStringMetric(), "StringMetricDiffer", -1 }
  				, new Object[] { XmlMerger3FactoryImpl.newFast(), "FastMerger", XmlDifferFactoryImpl.newSortingStringMetric(), "SortingStringMetricDiffer", -1 }
  				  
  				, new Object[] { XmlMerger3FactoryImpl.newSafe(), "SafeMerger", XmlDifferFactoryImpl.newDefault(), "DefaultDiffer", -1 }
  				, new Object[] { XmlMerger3FactoryImpl.newSafe(), "SafeMerger", XmlDifferFactoryImpl.newStringMetric(), "StringMetricDiffer", -1 }
  				, new Object[] { XmlMerger3FactoryImpl.newSafe(), "SafeMerger", XmlDifferFactoryImpl.newSortingStringMetric(), "SortingStringMetricDiffer", -1 }
		);
	}
	
	protected final XmlMerger3Factory xmlMerger3Factory;
	protected final XmlDifferFactory xmlDifferFactory;
	protected final int stringMetricDifferThreshold;
	
	public DifferMergerTestBase(XmlMerger3Factory xmlMerger3Factory, String mergerName, XmlDifferFactory xmlDifferFactory, String differName, int stringMetricDifferThreshold) {
		this.xmlMerger3Factory = xmlMerger3Factory;
		this.xmlDifferFactory = xmlDifferFactory;
		this.stringMetricDifferThreshold = stringMetricDifferThreshold;
	}
	
	protected static void checkAttributesStorage(XmlStruct xml) {
		for (XmlElement element : xml.elements) {
			if (element.attributes.equals(element.selectedAttributes)) {
				Assert.assertSame("same attributes", element.attributes, element.selectedAttributes);
			}
		}
	}
	
	protected static void checkCallbackCounts(XmlStruct xmlLeft, XmlStruct xmlRight, int max, XmlDiffer differ, XmlMerger3 merger, boolean removeElement) throws IOException, XMLStreamException {
		if (isDifferImplShufflingStringMetric(differ)) {
			return; // that's evil since repeated diffing will cause repeated shuffling and unpredictable results
		}
		
		class CountingCallback implements IncludeElementCallback {
			int nInsertions; 
			int nDeletions; 
			int nAttrChanges;
			final Random rnd = new Random();
			
			@Override
			public boolean addElement(XmlElement element) {
				nInsertions++;
				return rnd.nextBoolean();
			}
			
			@Override
			public boolean removeElement(XmlElement element) {
				nDeletions++;
				return removeElement;
			}
			
			@Override
			public boolean replaceAttributes(XmlElement element) {
				nAttrChanges++;
				return rnd.nextBoolean();
			}
		}
		
		XmlDiffer.Result diffResult = differ.diff(xmlLeft, xmlRight);
		
		Assert.assertEquals("deletions", diffResult.nRemoved, xmlLeft.root().nStructureChanged);
		Assert.assertEquals("insertions", diffResult.nAdded, xmlRight.root().nStructureChanged);
		Assert.assertEquals("attr changes", diffResult.nAttributeChanged, xmlLeft.root().nAttributeChanged);
		Assert.assertEquals("attr changes", diffResult.nAttributeChanged, xmlRight.root().nAttributeChanged);
		
		for (int i = 0; i < max; i++) {
			/*
			 * we don't care about the result, we just want to check that the callbacks get invoked a deterministic number of times.
			 */
			CountingCallback callback = new CountingCallback();
			merger.merge(xmlLeft, xmlRight, false, callback, stop);
			Assert.assertEquals("deletions run#" + i, diffResult.nRemoved, callback.nDeletions);
			Assert.assertEquals("insertions run#" + i, diffResult.nAdded, callback.nInsertions);
			Assert.assertEquals("attr changes run#" + i, diffResult.nAttributeChanged, callback.nAttrChanges);
		}
	}
}
