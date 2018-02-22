package com.dynatrace.profilediff;

import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.dynatrace.profilediff.lib.CharSequenceReader;

public class XmlMergerBuildFilesTest extends DifferMergerTestBase {

	public XmlMergerBuildFilesTest(XmlMerger3Factory xmlMerger3Factory,
			String mergerName, XmlDifferFactory xmlDifferFactory,
			String differName, int stringMetricDifferThreshold) {
		super(xmlMerger3Factory, mergerName, xmlDifferFactory, differName, stringMetricDifferThreshold);
	}

	static XmlMerger3.IncludeElementCallback callback(boolean add, boolean remove) {
		return new XmlMerger3.IncludeElementCallback() {
			@Override
			public boolean addElement(XmlElement element) {
				return add;
			}
			
			@Override
			public boolean removeElement(XmlElement element) {
				return remove;
			}
		};
	}
	
	static XmlMerger3.IncludeElementCallback callback(boolean add, boolean remove, boolean attr) {
		return new XmlMerger3.IncludeElementCallback() {
			@Override
			public boolean addElement(XmlElement element) {
				return add;
			}
			
			@Override
			public boolean removeElement(XmlElement element) {
				return remove;
			}
			
			@Override
			public boolean replaceAttributes(XmlElement element) {
				return attr;
			}
		};
	}

	XmlMerger3 merger;
	XmlLexer lexer;
	XmlDiffer differ;
	
	@Before
	public void before() {
		List<String> discriminatingAttributes = (Arrays.asList());
		lexer = new XmlLexer(discriminatingAttributes, stringCache);
		List<String> ignoreAttributeNames = Collections.emptyList();
		differ = xmlDifferFactory.create(ignoreAttributeNames);
		merger = xmlMerger3Factory.create(lexer, differ);
	}

	
	@Test
	public void case1a() throws XMLStreamException, IOException {
		String[] inputLeft = IO.readLines(new FileReader("samples/build.core.xml"));
		String[] inputRight = IO.readLines(new FileReader("samples/build.devlocal.xml"));
		
		XmlStruct xmlLeft = lexer.parse(IO.asString(inputLeft));
		XmlStruct xmlRight = lexer.parse(IO.asString(inputRight));
		
		XmlDiffer.Result diffResult = differ.diff(xmlLeft, xmlRight);
		Assert.assertEquals(6, diffResult.nAdded);
		Assert.assertEquals(5, diffResult.nRemoved);
		Assert.assertEquals(4, diffResult.nAttributeChanged);
		
		XmlStruct merged = merger.merge(IO.asString(inputLeft), IO.asString(inputRight), false, callback(/*add*/ true, /*remove*/ false), stop);
		String[] lines = IO.readLines(new CharSequenceReader(merged.data));
		
		checkList(Arrays.asList(
				  "<project name=\"dynaTrace core\" default=\"buildAllJars\">"
				, "	<description>"
				, "        dynaTrace core (Java) build:"
				, ""
				, "		Product release core build"
				, "    </description>"
				, ""
				, "	<property name=\"dir.root\" location=\".\" />"
				, "	<condition property=\"native.lib.dir\" value=\"lib64\" else=\"lib\">"
				, "		<matches string=\"${native.os.arch}\" pattern=\".*\\.x64$\" />"
				, "	</condition>"
				, "	<condition property=\"native.bits\" value=\"64\" else=\"32\">"
				, "		<matches string=\"${native.os.arch}\" pattern=\".*\\.x64$\" />"
				, "	</condition>"
				, ""
				, ""
				, "	<target name=\"getLatestNativeAgentWindows\" if=\"isWindows\" unless=\"donotgetnativeAgent\">"
				, "		<available file=\"agent\\conf\\dtwsagent.ini\" property=\"existsAgentLibDtwsagentIni\" />"
				, "		<available file=\"agent\\conf\\dtwsagent.ini\" property=\"existsAgentLib64DtwsagentIni\" />"
				, "		<copy todir=\"agent\\lib\" failonerror=\"true\" file=\"\\\\buildmaster.emea.cpwr.corp\\artifacts\\dynaTrace\\${version.dir}\\latest\\native\\windows-x86-32\\dtagentcore.dll\"/>"
				, "		<copy todir=\"agent\\lib\" failonerror=\"true\" file=\"\\\\buildmaster.emea.cpwr.corp\\artifacts\\dynaTrace\\${version.dir}\\latest\\native\\windows-x86-32\\dtagent.dll\"/>"
				, "		<copy todir=\"agent\\lib64\" failonerror=\"true\" file=\"\\\\buildmaster.emea.cpwr.corp\\artifacts\\dynaTrace\\${version.dir}\\latest\\native\\windows-x86-64\\dtwsagent.exe\"/>"
				, "		<copy todir=\"agent\\lib64\" failonerror=\"true\" file=\"\\\\buildmaster.emea.cpwr.corp\\artifacts\\dynaTrace\\${version.dir}\\latest\\native\\windows-x86-64\\dtwsagent.dll\"/>"
				, "		<copy todir=\"agent\\lib64\" failonerror=\"true\" file=\"\\\\buildmaster.emea.cpwr.corp\\artifacts\\dynaTrace\\${version.dir}\\latest\\native\\windows-x86-64\\dtzagent.dll\"/>"
				, "		<antcall target=\"getLatestNativeAgentDtwsagentIniWindows32\" />"
				, "		<antcall target=\"getLatestNativeAgentDtwsagentIniWindows64\" />"
				, "	</target>"
				, ""
				, "	<property file=\"${dir.root}/../ant.properties\" />"
				, "	<property file=\"${user.home}/ant.properties\" />"
				, ""
				, ""
				, "	<condition property=\"bundle.properties.filename\" value=\"bundles.${target.platform}.properties\""
				, "			else=\"bundles.properties\">"
				, "		<isset property=\"target.platform\" />"
				, "	</condition>"
				, ""
				, "	<!-- Check if any component is obfuscated (independent of release build) -->"
				, "	<condition property=\"doObfuscateAny\">"
				, "		<OR>"
				, "			<istrue value=\"${doObfuscateAgent}\" />"
				, "			<istrue value=\"${doObfuscateServerCollectorClient}\" />"
				, "		</OR>"
				, "	</condition>"
				, ""
				, "    <condition property=\"isAix\">"
				, "        <os name=\"AIX\" />"
				, "    </condition>"
				, "	<condition property=\"isLinux\">"
				, "		<and>"
				, "			<os family=\"unix\" />"
				, "			<not>"
				, "				<os family=\"mac\" />"
				, "			</not>"
				, "		</and>"
				, "	</condition>"
				, "	<condition property=\"isMac\">"
				, "		<os family=\"mac\" />"
				, "	</condition>"
				, "	<condition property=\"isUnix\">"
				, "		<os family=\"unix\" />"
				, "	</condition>"
				, "</project>"
				), Arrays.asList(lines));
		
		lexer.parse(IO.asString(lines));
		
		merged = merger.merge(IO.asString(inputLeft), IO.asString(inputRight), false, callback(/*add*/ true, /*remove*/ true), stop);
		lines = IO.readLines(new CharSequenceReader(merged.data));
		
		checkList(Arrays.asList(
				  "<project name=\"dynaTrace core\" default=\"buildAllJars\">"
				, ""
				, "	<property name=\"dir.root\" location=\".\" />"
				, "	<condition property=\"native.lib.dir\" value=\"lib64\" else=\"lib\">"
				, "		<matches string=\"${native.os.arch}\" pattern=\".*\\.x64$\" />"
				, "	</condition>"
				, "	<condition property=\"native.bits\" value=\"64\" else=\"32\">"
				, "		<matches string=\"${native.os.arch}\" pattern=\".*\\.x64$\" />"
				, "	</condition>"
				, ""
				, ""
				, "	<target name=\"getLatestNativeAgentWindows\" if=\"isWindows\" unless=\"donotgetnativeAgent\">"
				, "		<available file=\"agent\\conf\\dtwsagent.ini\" property=\"existsAgentLibDtwsagentIni\" />"
				, "		<available file=\"agent\\conf\\dtwsagent.ini\" property=\"existsAgentLib64DtwsagentIni\" />"
				, "		<copy todir=\"agent\\lib\" failonerror=\"true\" file=\"\\\\buildmaster.emea.cpwr.corp\\artifacts\\dynaTrace\\${version.dir}\\latest\\native\\windows-x86-32\\dtagentcore.dll\"/>"
				, "		<copy todir=\"agent\\lib\" failonerror=\"true\" file=\"\\\\buildmaster.emea.cpwr.corp\\artifacts\\dynaTrace\\${version.dir}\\latest\\native\\windows-x86-32\\dtagent.dll\"/>"
				, "		<copy todir=\"agent\\lib64\" failonerror=\"true\" file=\"\\\\buildmaster.emea.cpwr.corp\\artifacts\\dynaTrace\\${version.dir}\\latest\\native\\windows-x86-64\\dtwsagent.exe\"/>"
				, "		<copy todir=\"agent\\lib64\" failonerror=\"true\" file=\"\\\\buildmaster.emea.cpwr.corp\\artifacts\\dynaTrace\\${version.dir}\\latest\\native\\windows-x86-64\\dtwsagent.dll\"/>"
				, "		<copy todir=\"agent\\lib64\" failonerror=\"true\" file=\"\\\\buildmaster.emea.cpwr.corp\\artifacts\\dynaTrace\\${version.dir}\\latest\\native\\windows-x86-64\\dtzagent.dll\"/>"
				, "		<antcall target=\"getLatestNativeAgentDtwsagentIniWindows32\" />"
				, "		<antcall target=\"getLatestNativeAgentDtwsagentIniWindows64\" />"
				, "	</target>"
				, ""
				, "    <condition property=\"isAix\">"
				, "        <os name=\"AIX\" />"
				, "    </condition>"
				, "	<condition property=\"isLinux\">"
				, "		<and>"
				, "			<os family=\"unix\" />"
				, "			<not>"
				, "				<os family=\"mac\" />"
				, "			</not>"
				, "		</and>"
				, "	</condition>"
				, "	<condition property=\"isMac\">"
				, "		<os family=\"mac\" />"
				, "	</condition>"
				, "	<condition property=\"isUnix\">"
				, "		<os family=\"unix\" />"
				, "	</condition>"
				, "</project>"
				), Arrays.asList(lines));
		
		merged = merger.merge(IO.asString(inputLeft), IO.asString(inputRight), false, callback(/*add*/ true, /*remove*/ true, /*attr*/ true), stop);
		lines = IO.readLines(new CharSequenceReader(merged.data));
		
		checkList(Arrays.asList(
				  "<project name=\"dynaTrace developer local\" default=\"clean+buildAll\">"
				, ""
				, "	<property name=\"native.os.arch\" value=\"windows.x86\" />"
				, "	<condition property=\"native.lib.dir\" value=\"lib64\" else=\"lib\">"
				, "		<matches string=\"${native.os.arch}\" pattern=\".*\\.x64$\" />"
				, "	</condition>"
				, "	<condition property=\"native.bits\" value=\"64\" else=\"32\">"
				, "		<matches string=\"${native.os.arch}\" pattern=\".*\\.x64$\" />"
				, "	</condition>"
				, ""
				, ""
				, "	<target name=\"getLatestNativeAgentWindows\" if=\"isWindows\" unless=\"donotgetnativeAgent\">"
				, "		<available file=\"agent\\conf\\dtwsagent.ini\" property=\"existsAgentLibDtwsagentIni\" />"
				, "		<available file=\"agent\\conf\\dtwsagent.ini\" property=\"existsAgentLib64DtwsagentIni\" />"
				, "		<copy todir=\"agent\\lib\" failonerror=\"true\" file=\"\\\\buildmaster.emea.cpwr.corp\\artifacts\\dynaTrace\\${version.dir}\\latest\\native\\windows-x86-32\\dtagentcore.dll\"/>"
				, "		<copy todir=\"agent\\lib\" failonerror=\"true\" file=\"\\\\buildmaster.emea.cpwr.corp\\artifacts\\dynaTrace\\${version.dir}\\latest\\native\\windows-x86-32\\dtagent.dll\"/>"
				, "		<copy todir=\"agent\\lib64\" failonerror=\"true\" file=\"\\\\buildmaster.emea.cpwr.corp\\artifacts\\dynaTrace\\${version.dir}\\latest\\native\\windows-x86-64\\dtwsagent.exe\"/>"
				, "		<copy todir=\"agent\\lib64\" failonerror=\"true\" file=\"\\\\buildmaster.emea.cpwr.corp\\artifacts\\dynaTrace\\${version.dir}\\latest\\native\\windows-x86-64\\dtwsagent.dll\"/>"
				, "		<copy todir=\"agent\\lib64\" failonerror=\"true\" file=\"\\\\buildmaster.emea.cpwr.corp\\artifacts\\dynaTrace\\${version.dir}\\latest\\native\\windows-x86-64\\dtzagent.dll\"/>"
				, "		<antcall target=\"getLatestNativeAgentDtwsagentIniWindows32\" />"
				, "		<antcall target=\"getLatestNativeAgentDtwsagentIniWindows64\" />"
				, "	</target>"
				, ""
				, "    <condition property=\"isWindows\">"
				, "        <os family=\"windows\" />"
				, "    </condition>"
				, "	<condition property=\"isLinux\">"
				, "		<and>"
				, "			<os family=\"unix\" />"
				, "			<not>"
				, "				<os family=\"mac\" />"
				, "			</not>"
				, "		</and>"
				, "	</condition>"
				, "	<condition property=\"isMac\">"
				, "		<os family=\"mac\" />"
				, "	</condition>"
				, "	<condition property=\"isUnix\">"
				, "		<os family=\"unix\" />"
				, "	</condition>"
				, "</project>"
				), Arrays.asList(lines));
	}
}
