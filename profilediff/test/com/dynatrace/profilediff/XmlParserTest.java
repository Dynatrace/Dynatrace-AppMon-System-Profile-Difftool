package com.dynatrace.profilediff;

import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.junit.Assert;
import org.junit.Test;

public class XmlParserTest  extends TestBase {
	
	@Test
	public void plain() throws XMLStreamException, IOException {
		String[] input = {
				  "<root>"
				, "  <level1>"
				, "    <level2>"
				, "      <level3/>"
				, "    </level2>"
				, "  </level1>"
				, "</root>"
		};
		
		List<String> discriminatingAttributes = (Arrays.asList());
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		XmlStruct xml = lexer.parse(IO.asString(input));
		
		checkList(Arrays.asList(
				  "root [0:0:6:72:73:80]"
				, "root/level1 [6:9:17:60:63:72]"
				, "root/level1/level2 [17:22:30:46:51:60]"
				, "root/level1/level2/level3 [30:37:46:-1:-1:46]"
				), toOffsetStrings(xml.elements));
		
		printContent(input, xml);
		checkFinder(xml);
	}
	
	static void checkFinder(XmlStruct xml) {
//		for (int i = 0; i < 90; i++) {
//			System.out.println(i + " -> " + xml.findElementAtOffset(i));
//		}
		
		for (XmlElement element : xml.elements) {
			// FIXME should be <= 
			for (int i = element.openTag.start; i < element.openTag.end; i++) {
				Assert.assertTrue(element.toOffsetString() + "_" + i, xml.findElementAtOffset(i) == element);
			}
			if (!element.isEmpty()) {
				// FIXME should be <= 
				// FIXME should not need start + 1 
				for (int i = element.closeTag.start + 1; i < element.closeTag.end; i++) {
					Assert.assertTrue(element.toOffsetString() + "_" + i, xml.findElementAtOffset(i) == element);
				}
			}
		}
	}
	
	@Test
	public void food() throws XMLStreamException, IOException {
		String[] input = {
				  " <food a='b'"
				, " x='y'"
				, ">   <fruits><apple"
				, "/>     <banana/>"
				, "   </fruits><noodles>"
				, "   </noodles>"
				, "   <vegetables"
				, "     ><spinach/>"
				, "     <carrot/></vegetables>"
				, "   <rubbish>"
				, "     <ham/>"
				, "   </rubbish></food>"
		};
		
		List<String> discriminatingAttributes = (Arrays.asList());
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		XmlStruct xml = lexer.parse(IO.asString(input));
		
		checkList(Arrays.asList(
				  "food [0:1:21:190:190:197]"
				, "food/fruits [21:24:32:55:59:68]"
				, "food/fruits/apple [32:32:41:-1:-1:41]"
				, "food/fruits/banana [41:46:55:-1:-1:55]"
				, "food/noodles [68:68:77:77:81:91]"
				, "food/vegetables [91:95:113:138:138:151]"
				, "food/vegetables/spinach [113:113:123:-1:-1:123]"
				, "food/vegetables/carrot [123:129:138:-1:-1:138]"
				, "food/rubbish [151:155:164:176:180:190]"
				, "food/rubbish/ham [164:170:176:-1:-1:176]"
				), toOffsetStrings(xml.elements));
		
		printContent(input, xml);
		checkFinder(xml);
	}
	
	@Test
	public void ant() throws XMLStreamException, IOException {
		String[] input = {
				"<target name=\"getLatestNativeAgentWindows\" if=\"isWindows\" unless=\"donotgetnativeAgent\">"
				, "<available file=\"agent\\conf\\dtwsagent.ini\" property=\"existsAgentLibDtwsagentIni\" />"
				, "<available file=\"agent\\conf\\dtwsagent.ini\" property=\"existsAgentLib64DtwsagentIni\" />"
				, "<copy todir=\"agent\\lib\" failonerror=\"true\" file=\"\\\\buildmaster.emea.cpwr.corp\\artifacts\\dynaTrace\\${version.dir}\\latest\\native\\windows-x86-32\\dtagentcore.dll\"/>"
				, "</target>"
		};
		
		List<String> discriminatingAttributes = (Arrays.asList());
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		XmlStruct xml = lexer.parse(IO.asString(input));
		
		checkList(Arrays.asList(
				  "target [0:0:87:418:419:428]"
				, "target/available [87:88:171:-1:-1:171]"
				, "target/available [171:172:257:-1:-1:257]"
				, "target/copy [257:258:418:-1:-1:418]"
				), toOffsetStrings(xml.elements));
		
		printContent(input, xml);
		checkFinder(xml);
	}
	
	@Test
	public void antFile() throws XMLStreamException, IOException {
		String[] input = IO.readLines(new FileReader("samples/build.core.xml"));
		
		List<String> discriminatingAttributes = (Arrays.asList());
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		XmlStruct xml = lexer.parse(IO.asString(input));
		
		checkList(Arrays.asList(
				  "project [0:0:54:805:806:816]"
				, "project/description [54:56:69:69:141:155]"
				, "project/property [155:158:199:-1:-1:199]"
				, "project/property [199:202:251:-1:-1:251]"
				, "project/property [251:253:300:-1:-1:300]"
				, "project/condition [300:304:428:467:469:481]"
				, "project/condition/isset [428:431:467:-1:-1:467]"
				, "project/condition [481:562:599:714:716:728]"
				, "project/condition/OR [599:602:606:706:709:714]"
				, "project/condition/OR/istrue [606:610:648:-1:-1:648]"
				, "project/condition/OR/istrue [648:652:706:-1:-1:706]"
				, "project/condition [728:734:762:788:793:805]"
				, "project/condition/os [762:771:788:-1:-1:788]"
				), toOffsetStrings(xml.elements));
		
		printContent(input, xml);
		checkFinder(xml);
	}
	
	@Test
	public void plain2() throws XMLStreamException, IOException {
		String[] input = {
				"<element>"
				,"  <inserted key='new1'/>"
				,"  <inserted key='new2'/>"
				,"</element>"
		};
		
		List<String> discriminatingAttributes = (Arrays.asList("key"));
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		XmlStruct xml = lexer.parse(IO.asString(input));
				
		checkList(Arrays.asList(
				  "element [0:0:9:59:60:70]"
				, "element/inserted:new1 [9:12:34:-1:-1:34]"
				, "element/inserted:new2 [34:37:59:-1:-1:59]"
				), toOffsetStrings(xml.elements));
		
		printContent(input, xml);
		checkFinder(xml);
	}
	
	@Test
	public void plain2AttrWS() throws XMLStreamException, IOException {
		String[] input = {
				"<element>"
				,"  <inserted key = 'new1'/>"
				,"  <inserted key  =  \"new2\"/>"
				,"</element>"
		};
		
		List<String> discriminatingAttributes = (Arrays.asList("key"));
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		XmlStruct xml = lexer.parse(IO.asString(input));
		
		checkList(Arrays.asList(
				  "element [0:0:9:65:66:76]"
				, "element/inserted:new1 [9:12:36:-1:-1:36]"
				, "element/inserted:new2 [36:39:65:-1:-1:65]"
				), toOffsetStrings(xml.elements));
		
		printContent(input, xml);
		checkFinder(xml);
	}
	
	@Test
	public void plain2AttrEscapes() throws XMLStreamException, IOException {
		String[] input = {
				"<element>"
				,"  <inserted key = 'n\"ew1'/>"
				,"  <inserted key  =  \"new'2\"/>"
				,"  <inserted key  =  'with\\escape'/>"
				,"</element>"
		};
		
		List<String> discriminatingAttributes = (Arrays.asList("key"));
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		XmlStruct xml = lexer.parse(IO.asString(input));
		
		checkList(Arrays.asList(
				  "element [0:0:9:103:104:114]"
				, "element/inserted:n\"ew1 [9:12:37:-1:-1:37]"
				, "element/inserted:new'2 [37:40:67:-1:-1:67]"
				, "element/inserted:with\\escape [67:70:103:-1:-1:103]"
				), toOffsetStrings(xml.elements));
		
		printContent(input, xml);
		checkFinder(xml);
	}
	
	@Test
	public void plain3() throws XMLStreamException, IOException {
		String[] input = {
				"<root a='1' attr1='only present in original document'>"
				, "  <level1 b='2'>"
				, "    <level2 c='3'>"
				, "      <level3 d='4' attr2='only present in original document'/>"
				, "      <level3 d='5'/>"
				, "      <level3 d='6'>"
				, "        <level4 e='1'/>"
				, "      </level3>"
				, "    </level2>"
				, "  </level1>"
				, "  <level1 b='20'/>"
				, "</root>"
		};
		
		List<String> discriminatingAttributes = (Arrays.asList("a", "b", "c", "d", "e"));
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		XmlStruct xml = lexer.parse(IO.asString(input));
		
		checkList(Arrays.asList(
				  "root:1 [0:0:54:282:283:290]"
				, "root:1/level1:2 [54:57:71:251:254:263]"
				, "root:1/level1:2/level2:3 [71:76:90:237:242:251]"
				, "root:1/level1:2/level2:3/level3:4 [90:97:154:-1:-1:154]"
				, "root:1/level1:2/level2:3/level3:5 [154:161:176:-1:-1:176]"
				, "root:1/level1:2/level2:3/level3:6 [176:183:197:221:228:237]"
				, "root:1/level1:2/level2:3/level3:6/level4:1 [197:206:221:-1:-1:221]"
				, "root:1/level1:20 [263:266:282:-1:-1:282]"
				), toOffsetStrings(xml.elements));
		
		printContent(input, xml);
		checkFinder(xml);
	}

	@Test
	public void multiLines() throws XMLStreamException, IOException {
		String[] input = {
				  "     <root>"
				, "  <level1>"
				, "    <level2"
				, "    a='a'"
				, "    b='b'"
				, "    key='mykey'"
				, "    >"
				, "      <level3/>"
				, "    </level2>"
				, "  </level1> <something></something> <empty/> "
				, "<food>"
				, "   <fruits><respberry/>"
				, "     <apple/>"
				, "     <banana/>"
				, "     <strawberry/></fruits>"
				, "   <vegetables>"
				, "     <carrot/>"
				, "     <potato/></vegetables><sweets>"
				, "     <chocolate/>"
				, "     <candy/>"
				, "   </sweets>"
				, "   <other>"
				, "     <dogfood/>"
				, "   </other></food>"
				, "</root"
				, ""
				, ">" // closeTag.lineNumber actually here
		};
		
		List<String> discriminatingAttributes = (Arrays.asList("key"));
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		XmlStruct xml = lexer.parse(IO.asString(input));
		
		checkList(Arrays.asList(
				  "root [0:5:11:398:399:408]"
				, "root/level1 [11:14:22:106:109:118]"
				, "root/level1/level2:mykey [22:27:76:92:97:106]"
				, "root/level1/level2:mykey/level3 [76:83:92:-1:-1:92]"
				, "root/something [118:119:130:130:130:142]"
				, "root/empty [142:143:151:-1:-1:151]"
				, "root/food [151:153:159:391:391:398]"
				, "root/food/fruits [159:163:171:231:231:240]"
				, "root/food/fruits/respberry [171:171:183:-1:-1:183]"
				, "root/food/fruits/apple [183:189:197:-1:-1:197]"
				, "root/food/fruits/banana [197:203:212:-1:-1:212]"
				, "root/food/fruits/strawberry [212:218:231:-1:-1:231]"
				, "root/food/vegetables [240:244:256:286:286:299]"
				, "root/food/vegetables/carrot [256:262:271:-1:-1:271]"
				, "root/food/vegetables/potato [271:277:286:-1:-1:286]"
				, "root/food/sweets [299:299:307:339:343:352]"
				, "root/food/sweets/chocolate [307:313:325:-1:-1:325]"
				, "root/food/sweets/candy [325:331:339:-1:-1:339]"
				, "root/food/other [352:356:363:379:383:391]"
				, "root/food/other/dogfood [363:369:379:-1:-1:379]"
				), toOffsetStrings(xml.elements));
		
		
		checkList(Arrays.asList(
				  "root [1:27]"
				, "root/level1 [2:10]"
				, "root/level1/level2:mykey [3:9]"
				, "root/level1/level2:mykey/level3 [8:8]"
				, "root/something [10:10]"
				, "root/empty [10:10]"
				, "root/food [11:24]"
				, "root/food/fruits [12:15]"
				, "root/food/fruits/respberry [12:12]"
				, "root/food/fruits/apple [13:13]"
				, "root/food/fruits/banana [14:14]"
				, "root/food/fruits/strawberry [15:15]"
				, "root/food/vegetables [16:18]"
				, "root/food/vegetables/carrot [17:17]"
				, "root/food/vegetables/potato [18:18]"
				, "root/food/sweets [18:21]"
				, "root/food/sweets/chocolate [19:19]"
				, "root/food/sweets/candy [20:20]"
				, "root/food/other [22:24]"
				, "root/food/other/dogfood [23:23]"
				), toPathStrings(xml.elements));
		
		printContent(input, xml);
		checkFinder(xml);
	}
	
	@Test
	public void evenUglierSyntax() throws XMLStreamException, IOException {
		String[] input = {
				"<food a='c'>"
				, "   <fruits"
				, "     ><raspberry/>"
				, "     <apple />"
				, "     <banana  />"
				, "     <strawberry/> </fruits"
				, ">"
				, "   <vegetables>"
				, "     <carrot/><potato/>"
				, "   </vegetables><sweets>"
				, "     <chocolate/>"
				, "     <candy/>"
				, "   </sweets><other>"
				, "     <dogfood/>"
				, "   </other>"
				, "</food>"
		};
		
		List<String> discriminatingAttributes = (Arrays.asList("key"));
		XmlLexer lexer = new XmlLexer(discriminatingAttributes, stringCache);
		XmlStruct xml = lexer.parse(IO.asString(input));
		
		checkList(Arrays.asList(
				  "food [0:0:12:249:250:257]"
				, "food/fruits [12:16:30:93:94:104]"
				, "food/fruits/raspberry [30:30:42:-1:-1:42]"
				, "food/fruits/apple [42:48:57:-1:-1:57]"
				, "food/fruits/banana [57:63:74:-1:-1:74]"
				, "food/fruits/strawberry [74:80:93:-1:-1:93]"
				, "food/vegetables [104:108:120:144:148:161]"
				, "food/vegetables/carrot [120:126:135:-1:-1:135]"
				, "food/vegetables/potato [135:135:144:-1:-1:144]"
				, "food/sweets [161:161:169:201:205:214]"
				, "food/sweets/chocolate [169:175:187:-1:-1:187]"
				, "food/sweets/candy [187:193:201:-1:-1:201]"
				, "food/other [214:214:221:237:241:249]"
				, "food/other/dogfood [221:227:237:-1:-1:237]"
				), toOffsetStrings(xml.elements));
		
		printContent(input, xml);
		checkFinder(xml);
	}
	
	private static boolean PRINT = false;
	
	static void printContent(String[] input, XmlStruct xml) {
		if (PRINT) {
			for (XmlElement element : xml.elements) {
				System.out.println("*** element: " + element.toPathString());
				System.out.println("open tag: '" + xml.data.subSequence(element.openTag.start, element.openTag.end) + "'");
				System.out.println("open tag prev: '" + xml.data.subSequence(element.openTag.prevEnd, element.openTag.end) + "'");
				if (element.closeTag.start == -1) {
					System.out.println("(empty tag)");
				} else {
					System.out.println("close tag: '" + xml.data.subSequence(element.closeTag.start, element.closeTag.end) + "'");
					System.out.println("close tag prev: '" + xml.data.subSequence(element.closeTag.prevEnd, element.closeTag.end) + "'");
				}
				System.out.println("whole content:");
				System.out.println("'" + xml.data.subSequence(element.openTag.prevEnd, element.closeTag.end) + "'");
				System.out.println("insertionOffset content:");
				System.out.println("'" + xml.data.subSequence(element.openTag.end, element.closeTag.end) + "'");
			}
		}
	}
}
