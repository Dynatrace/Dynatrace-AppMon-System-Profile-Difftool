package com.dynatrace.profilediff.ui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.xml.stream.XMLStreamException;

import com.dynatrace.common.stringcache.NullStringCache;
import com.dynatrace.common.stringcache.StringCache;
import com.dynatrace.common.stringcache.StringCacheInterface;
import com.dynatrace.common.swing.ui.TreeCheckboxManager;
import com.dynatrace.common.worker.AWTWorker;
import com.dynatrace.profilediff.IO;
import com.dynatrace.profilediff.StringMetricXmlDiffer;
import com.dynatrace.profilediff.StringMetricXmlDiffer.MetricResolver;
import com.dynatrace.profilediff.StringMetricXmlDifferFactory;
import com.dynatrace.profilediff.StringMetricXmlDifferFactoryImpl;
import com.dynatrace.profilediff.XmlDiffer;
import com.dynatrace.profilediff.XmlDifferFactoryImpl;
import com.dynatrace.profilediff.XmlElement;
import com.dynatrace.profilediff.XmlLexer;
import com.dynatrace.profilediff.XmlMerger3;
import com.dynatrace.profilediff.XmlMerger3Factory;
import com.dynatrace.profilediff.XmlMerger3FactoryImpl;
import com.dynatrace.profilediff.XmlStruct;
import com.dynatrace.profilediff.lib.StringMetrics;
import com.dynatrace.profilediff.togglemodel.NullToggleModel;
import com.dynatrace.profilediff.togglemodel.SystemProfileToggleModel;
import com.dynatrace.profilediff.togglemodel.ToggleModel;
import com.dynatrace.profilediff.ui.decorationmodel.DecorationModel;
import com.dynatrace.profilediff.ui.decorationmodel.HierarchicalResourceDecorationModel;
import com.dynatrace.profilediff.ui.decorationmodel.NullDecorationModel;

/**
 * Main-Class
 * 
 * @author cwat-pgrasboe
 */
public class ProfileDiff implements Runnable {
	
	static final AWTWorker worker = new AWTWorker() {
		private final ExecutorService service = Executors.newCachedThreadPool();
		
		@Override
		protected void startThread(Runnable run) {
			service.submit(run);
		}
	};
	
	private static Settings settings;
	private static XmlMerger3Factory xmlMerger3Factory;
	private static StringCacheInterface stringCache;
	private static XmlLexer lexer;
	private static XmlDiffer differ;
	private static ToggleModel<XmlElement> toggleModel;
	private static DecorationModel<XmlElement> decorationModel;
	private static UserObjectLogic userObjectLogic;
	
	static JFrame mainFrame;
	static MainPanel mainPanel;
	
	final String[] args;
	
	ProfileDiff(String[] args) {
		this.args = args;
	}

	public static void main(String[] args) {
		staticInit();
		EventQueue.invokeLater(new ProfileDiff(args));
	}
	
	@Override
	public void run() {
		MainPanel panel = openMainFrame("xmldiff");
		if (args.length == 2) {
			panel.open(XmlInput.fromFile(new File(args[0])), XmlInput.fromFile(new File(args[1])));
		} else if (args.length == 1) {
			panel.open(XmlInput.fromFile(new File(args[0])), null);
		} else if (args.length > 0) {
			System.err.println("Invalid number of arguments. Expecting 1 or 2 file names.");
		}
	}
	
	static boolean getDefaultInitialSelection(XmlElement element) {
		if (element.hasDirectStructureChange()) {
			return element.isInsertion() ? getDefaultInitialSelectionInsertion() : getDefaultInitialSelectionDeletion(); 
		}
		if (element.hasDirectAttributeChange()) {
			return getDefaultInitialSelectionAttribute(); // isInsertion() check needed?
		}
		return false;
	}
	
	static boolean getDefaultInitialSelectionDeletion() {
		return settings.initialSelectionDeletion;
	}
	
	static boolean getDefaultInitialSelectionInsertion() {
		return settings.initialSelectionInsertion;
	}
	
	static boolean getDefaultInitialSelectionAttribute() {
		return settings.initialSelectionAttribute;
	}
	
	private static void staticInit() {
		try {
			UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		
		settings = Settings.load();
		
		List<String> discriminatorAttributes = Arrays.asList(settings.discriminatorAttributes);
		List<String> ignoreAttributes = Arrays.asList(settings.ignoreAttributes);
		
		System.out.println("discriminatorAttributes: " + discriminatorAttributes);
		System.out.println("ignoreAttributes: " + ignoreAttributes);

		xmlMerger3Factory = settings.useFastMerger ? XmlMerger3FactoryImpl.newFast() : XmlMerger3FactoryImpl.newSafe();
		stringCache = settings.useStringCache ? new StringCache(new HashMap<>()) : NullStringCache.INSTANCE;
		lexer = new XmlLexer(discriminatorAttributes, stringCache);
		
		if (settings.useStringMetricDiffer) {
			StringMetricXmlDifferFactory xmlDifferFactory;
			MetricResolver metricResolver;
			
			if (settings.useSortingStringMetricDiffer) {
				xmlDifferFactory = StringMetricXmlDifferFactoryImpl.newSorting();
			} else {
				xmlDifferFactory = StringMetricXmlDifferFactoryImpl.newDefault();
			}
			if (settings.useEqualityMetricGlobally) {
				metricResolver = StringMetricXmlDiffer.getEqualityMetricResolver();
				System.out.println("levenshteinPaths: (none) using equality metric globally");
			} else if (settings.useLevenshteinMetricGlobally) {
				metricResolver = (XmlElment) -> StringMetrics::getLevenshteinDistance;
				System.out.println("levenshteinPaths: (all) using Levenshtein metric globally");
			} else {
				metricResolver = StringMetricXmlDiffer.getSubTreeMetricResolver(StringMetrics::getLevenshteinDistance, settings.levenshteinPaths);
				System.out.println("levenshteinPaths: " + Arrays.toString(settings.levenshteinPaths));
			}
			
			differ = xmlDifferFactory.create(ignoreAttributes, metricResolver, settings.levenshteinThreshold);
			System.out.println("levenshteinThreshold: " + settings.levenshteinThreshold);
		} else {
			differ = XmlDifferFactoryImpl.newDefault().create(ignoreAttributes);
		}
			
		toggleModel = settings.useSystemProfileToggleModel ? new SystemProfileToggleModel() : NullToggleModel.getInstance();
		
		decorationModel = settings.useHierarchicalResourceDecorationModel 
				? new HierarchicalResourceDecorationModel(settings.scheme, settings.imageExt, settings.pathLevelThreshold) 
			    : NullDecorationModel.getInstance();
				
		userObjectLogic = UserObjectLogic.newDefault();
		
		System.out.println("Using Differ: " + differ);
		System.out.println("Using StringCache: " + stringCache);
		System.out.println("Using DecorationModel: " + decorationModel);
		System.out.println("Using ToggleModel: " + toggleModel);
	}
	
	static MainPanel openMainFrame(String title) {
		checkUIThread();
		JFrame frame = new JFrame(title);
		mainFrame = frame;
		frame.setBounds(100, 50, 1400, 850);
		MainPanel  panel = new MainPanel(userObjectLogic, toggleModel, decorationModel);
		mainPanel = panel;
		frame.setContentPane(panel);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		return panel;
	}
	
	static ResultPanel openResultDialog(String title, TreeCheckboxManager deletionManager, TreeCheckboxManager insertionManager, TreeCheckboxManager attrManager) {
		checkUIThread();
		JDialog dialog = new JDialog(mainFrame, title);
		dialog.setResizable(true);
		dialog.setModal(false); // FIXME setModal(true) => dialog is not refreshing content!!!
		dialog.setBounds(200, 150, 800, 600);
		ResultPanel panel = new ResultPanel(dialog, deletionManager, insertionManager, attrManager);
		dialog.setContentPane(panel);
		dialog.setVisible(true);
		return panel;
	}
	
	static void openHelpWindow(String title) {
		checkUIThread();
		JFrame frame = new JFrame(title);
		frame.setBounds(200, 150, 820, 600);
		JPanel panel = new JPanel(new BorderLayout());
		JTextArea text;
		panel.add(new JScrollPane(text = TextTools.createTextArea()));
		text.setEditable(false);
		text.setText(loadHelpDocument());
		text.setCaretPosition(0);
		frame.setContentPane(panel);
		frame.setVisible(true);
	}
	
	private static String loadHelpDocument() {
		try {
			return loadHelpDocument0();
		} catch (Exception e) {
			e.printStackTrace();
			return e.toString();
		}				
	}
	
	private static String loadHelpDocument0() throws IOException {
		URL resource = ProfileDiff.class.getResource("help.txt");
		if (resource == null) {
			throw new IllegalStateException("Cannot load help document");
		}
		try (InputStreamReader in = new InputStreamReader(resource.openStream())) {
			return IO.asString(IO.readLines(in));
		}
	}
	
	static void clearStringCache() {
		checkThread();
		stringCache.clear();
	}
	
	static void printStringCacheStats() {
		checkThread();
		stringCache.printStats("stringCache");
	}
		
	static XmlStruct parse(CharSequence data) throws XMLStreamException {
		checkThread();
		return lexer.parse(data);	
	}
	
	static void diff(XmlStruct base, XmlStruct mod) {
		checkThread();
		differ.diff(base, mod);
	}
	
	static XmlStruct merge(XmlStruct base, XmlStruct mod, boolean addComments, XmlMerger3.IncludeElementCallback callback, AtomicBoolean stop) throws XMLStreamException {
		checkThread();
		XmlMerger3 merger = xmlMerger3Factory.create(lexer, differ);
		System.out.println("Created merger: " + merger);
		return merger.merge(base, mod, addComments, callback, stop);
	}
	
	private static void checkThread() {
		assert !EventQueue.isDispatchThread() : "wrong thread";
	}
	
	private static void checkUIThread() {
		assert EventQueue.isDispatchThread() : "wrong thread";
	}
}
