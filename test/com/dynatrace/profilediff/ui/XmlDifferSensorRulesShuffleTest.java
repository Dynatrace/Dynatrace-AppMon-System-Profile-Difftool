package com.dynatrace.profilediff.ui;

import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.dynatrace.profilediff.IO;
import com.dynatrace.profilediff.StringMetricXmlDiffer;
import com.dynatrace.profilediff.StringMetricXmlDiffer.MetricResolver;
import com.dynatrace.profilediff.StringMetricXmlDifferFactory;
import com.dynatrace.profilediff.XmlDiffer;
import com.dynatrace.profilediff.XmlDifferFactory;
import com.dynatrace.profilediff.XmlDifferFactoryImpl;
import com.dynatrace.profilediff.XmlElement;
import com.dynatrace.profilediff.XmlLexer;
import com.dynatrace.profilediff.XmlStruct;
import com.dynatrace.profilediff.lib.StringMetrics;
import com.dynatrace.profilediff.ui.TwoWayModel.ChangeItem;
@RunWith(Parameterized.class)
public class XmlDifferSensorRulesShuffleTest extends ShuffleTestBase {
	
	public XmlDifferSensorRulesShuffleTest(StringMetricXmlDifferFactory stringMetricXmlDifferFactory, String name, int run) {
		super(stringMetricXmlDifferFactory, name, run);
	}

	@BeforeClass
	public static void beforeClass() {
		resetGlobalMaxDistances();
	}
	
	@AfterClass
	public static void afterClass() {
		expectGlobalMaxDistances("XmlDifferSensorRulesShuffleTest", 56, /*3144*/ -1, 1, /*expectedMetricInvokeCount*/ -1);
	}

	private final ChangeItem item = ChangeItem.changes;
	
	XmlLexer lexer;

	private int stringMetricDifferThreshold = -1;
	
	@Before
	public void before() {
		lexer = new XmlLexer(LEVENSHTEIN_DISCRIMINATOR_ATTRIBUTES, stringCache);
	}
	
	@Test
	public void jdbcSensor() throws XMLStreamException, IOException {
		String[] inputLeft = IO.readLines(new FileReader("samples/jdbc-sensor-rev-390963.xml"));
		String[] inputRight = IO.readLines(new FileReader("samples/jdbc-sensor-rev-425369.xml"));
		
		XmlStruct xmlLeft = lexer.parse(IO.asString(inputLeft));
		XmlStruct xmlRight = lexer.parse(IO.asString(inputRight));
		
		List<String> ignoreAttributeNames = Collections.emptyList();
		XmlDifferFactory xmlDifferFactory = XmlDifferFactoryImpl.newDefault();
		XmlDiffer differ = xmlDifferFactory.create(ignoreAttributeNames);
		XmlDiffer.Result diffResult = differ.diff(xmlLeft, xmlRight);
		Assert.assertEquals(24, diffResult.nAdded);
		Assert.assertEquals(22, diffResult.nRemoved);
		Assert.assertEquals(1, diffResult.nAttributeChanged);
		
		checkModel(Arrays.asList(
				  "plugin"
				, "  extension"
				, "    sensorpack"
				, "      (#) sensor"
				, "        (+) class:com\\.(ibm\\.ws\\.rsadapter\\.jdbc\\.WSJdbcDataSource|mchange\\.v2\\.c3p0.*(PoolBacked|DriverManager)DataSource.*)|java\\.sql\\.Driver(Manager)?|javax\\.sql\\.(ConnectionPoolDataSource|DataSource|PooledConnection|XAConnection|XADataSource)|oracle\\.jdbc\\.(pool\\.OracleConnectionPoolDataSource|pool\\.OracleDataSource|xa\\.client\\.OracleXADataSource|xa\\.OracleXADataSource)|org\\.(apache\\.tomcat\\.jdbc\\.pool\\.DataSourceProxy|jboss\\.resource\\.adapter\\.jdbc\\.(local\\.LocalDataSource|WrapperDataSource))|weblogic\\.jdbc\\.(common\\.internal\\.RmiDataSource|jta\\.DataSource|jts\\.Driver|pool\\.Driver)|org\\.apache\\..*\\.dbcp.?\\.(BasicDataSource|datasources\\.InstanceKeyDataSource|datasources\\.InstanceKeyDataSource|datasources\\.SharedPoolDataSource|managed\\.ManagedDataSource|PoolingDataSource|PoolingDriver)|com\\.sun\\.gjc\\.spi\\.jdbc.?.?\\.DataSource.?.?|com\\.sun\\.gjc\\.spi\\.base\\.(Abstract)?DataSource"
				, "        (+) class:com\\.(ibm\\.ws\\.rsadapter\\.jdbc\\.WSJdbc(Connection|PreparedStatement|Statement)|mchange\\.v2\\.c3p0\\.(C3P0ProxyConnection|impl\\.C3P0PooledConnection\\$ProxyConnection|impl\\.C3P0PooledConnection.*Proxy.*Statement|impl\\.NewProxyCallableStatement|impl\\.NewProxyConnection|impl\\.NewProxyConnection|impl\\.NewProxyPreparedStatement|impl\\.NewProxyStatement)|microsoft\\.sqlserver\\.jdbc\\.SQLServer(CallableStatement|Connection|PreparedStatement|Statement)|mysql\\.jdbc\\.(CallableStatement|ConnectionImpl|PreparedStatement|StatementImpl))|org\\.apache\\.(derby(\\.iapi\\.jdbc\\.(BrokeredCallableStatement.*|BrokeredPreparedStatement.*)|\\.impl\\.jdbc\\.Embed(CallableStatement|Connection|PreparedStatement|Statement)).*|.*\\.dbcp.?\\.(cpdsadapter\\.ConnectionImpl|cpdsadapter\\.PooledConnectionImpl|managed\\.ManagedConnection|PoolableConnection|PoolingConnection|PoolingDataSource$PoolGuardConnectionWrapper|DelegatingConnection|.cpdsadapter\\.PoolablePreparedStatementStub|DelegatingCallableStatement|DelegatingPreparedStatement|DelegatingStatement|PoolableCallableStatement|PoolablePreparedStatement))|java\\.sql\\.(CallableStatement|Connection|PreparedStatement|Statement)|net\\.sourceforge\\.jtds\\.jdbc\\.Jtds(CallableStatement|PreparedStatement|Statement)|oracle\\.jdbc\\.(OracleConnectionWrapper|driver\\.(OracleCallableStatement|OraclePreparedStatementWrapper|OraclePreparedStatement|OracleStatement|PhysicalConnection))|org\\.compiere\\.util\\.C(PreparedStatement|Statement)|org\\.jboss\\.resource\\.adapter\\.jdbc\\.(CachedCallableStatement|CachedPreparedStatement|jdk6\\.CachedCallableStatementJDK6|jdk6\\.CachedPreparedStatementJDK6|jdk6\\.WrappedCallableStatementJDK6|jdk6\\.WrappedConnectionJDK6|jdk6\\.WrappedPreparedStatementJDK6|jdk6\\.WrappedStatementJDK6|local\\.LocalCallableStatement|local\\.LocalConnection|local\\.LocalPreparedStatement|local\\.LocalStatement|WrappedCallableStatement|WrappedConnection|WrappedPreparedStatement|WrappedStatement)|org\\.postgresql\\.jdbc(2\\.AbstractJdbc2|3\\.AbstractJdbc3)Statement|weblogic\\.jdbc\\.wrapper\\.(CallableStatement|Connection|XAConnection|JTAConnection|JTSConnection|PreparedStatement|Statement)|com\\.sybase\\.jdbc(x|3\\.jdbc|4\\.jdbc)\\.SybConnection"
				, "        (+) class:.ool.PoolUtils$CheckedKeyedObjectPool"
				, "        (+) class:.ol.PoolUtils$CheckedObjectPool"
				, "        (+) class:.l.PoolUtils$ErodingKeyedObjectPool"
				, "        (+) class:.PoolUtils$ErodingObjectPool"
				, "        (+) class:.impl.GenericKeyedObjectPool"
				, "        (+) class:.pool2.impl.BaseGenericObjectPool"
				, "        (+) class:.pool2.impl.GenericObjectPool"
				, "        (+) class:.ObjectPool"
				, "        (+) class:.PoolUtils$ObjectPoolAdaptor"
				, "        (+) class:.impl.SoftReferenceKeyedObjectPool"
				, "        (+) class:.impl.SoftReferenceObjectPool"
				, "        (+) class:.impl.StackKeyedObjectPool"
				, "        (+) class:.impl.StackObjectPool"
				, "        (+) class:.PoolUtils$SynchronizedKeyedObjectPool"
				, "        (+) class:.PoolUtils$SynchronizedObjectPool"
				, "        (+) class:.PoolableConnection"
				, "        (+) class:.AbandonedObjectPool"
				, "        (+) class:.PoolingDriver"
				, "        (+) class:.PoolingDataSource"
				, "        (+) class:.PoolableConnectionFactory"
				, "        (+) class:.datasources.PerUserPoolDataSource"
				, "        (+) class:.cpdsadapter.PooledConnectionImpl"
				), item, xmlRight);
		
		checkModel(Arrays.asList(
				  "plugin"
				, "  extension"
				, "    sensorpack"
				, "      (#) sensor"
				, "        (-) class:com\\.(ibm\\.ws\\.rsadapter\\.jdbc\\.WSJdbcDataSource|mchange\\.v2\\.c3p0.*(PoolBacked|DriverManager)DataSource.*)|java\\.sql\\.Driver(Manager)?|javax\\.sql\\.(ConnectionPoolDataSource|DataSource|PooledConnection|XAConnection|XADataSource)|oracle\\.jdbc\\.(pool\\.OracleConnectionPoolDataSource|pool\\.OracleDataSource|xa\\.client\\.OracleXADataSource|xa\\.OracleXADataSource)|org\\.(apache\\.tomcat\\.jdbc\\.pool\\.DataSourceProxy|jboss\\.resource\\.adapter\\.jdbc\\.(local\\.LocalDataSource|WrapperDataSource))|weblogic\\.jdbc\\.(common\\.internal\\.RmiDataSource|jta\\.DataSource|jts\\.Driver|pool\\.Driver)|org\\.apache\\..*\\.dbcp\\.(BasicDataSource|datasources\\.InstanceKeyDataSource|datasources\\.InstanceKeyDataSource|datasources\\.SharedPoolDataSource|managed\\.ManagedDataSource|PoolingDataSource|PoolingDriver)|com\\.sun\\.gjc\\.spi\\.jdbc.?.?\\.DataSource.?.?|com\\\\.sun\\\\.gjc\\\\.spi\\\\.base\\\\.(Abstract)?DataSource"
				, "        (-) class:com\\.(ibm\\.ws\\.rsadapter\\.jdbc\\.WSJdbc(Connection|PreparedStatement|Statement)|mchange\\.v2\\.c3p0\\.(C3P0ProxyConnection|impl\\.C3P0PooledConnection\\$ProxyConnection|impl\\.C3P0PooledConnection.*Proxy.*Statement|impl\\.NewProxyCallableStatement|impl\\.NewProxyConnection|impl\\.NewProxyConnection|impl\\.NewProxyPreparedStatement|impl\\.NewProxyStatement)|microsoft\\.sqlserver\\.jdbc\\.SQLServer(CallableStatement|Connection|PreparedStatement|Statement)|mysql\\.jdbc\\.(CallableStatement|ConnectionImpl|PreparedStatement|StatementImpl))|org\\.apache\\.(derby(\\.iapi\\.jdbc\\.(BrokeredCallableStatement.*|BrokeredPreparedStatement.*)|\\.impl\\.jdbc\\.Embed(CallableStatement|Connection|PreparedStatement|Statement)).*|.*\\.dbcp\\.(cpdsadapter\\.ConnectionImpl|cpdsadapter\\.PooledConnectionImpl|managed\\.ManagedConnection|PoolableConnection|PoolingConnection|PoolingDataSource$PoolGuardConnectionWrapper|DelegatingConnection|.cpdsadapter\\.PoolablePreparedStatementStub|DelegatingCallableStatement|DelegatingPreparedStatement|DelegatingStatement|PoolableCallableStatement|PoolablePreparedStatement))|java\\.sql\\.(CallableStatement|Connection|PreparedStatement|Statement)|net\\.sourceforge\\.jtds\\.jdbc\\.Jtds(CallableStatement|PreparedStatement|Statement)|oracle\\.jdbc\\.(OracleConnectionWrapper|driver\\.(OracleCallableStatement|OraclePreparedStatementWrapper|OraclePreparedStatement|OracleStatement|PhysicalConnection))|org\\.compiere\\.util\\.C(PreparedStatement|Statement)|org\\.jboss\\.resource\\.adapter\\.jdbc\\.(CachedCallableStatement|CachedPreparedStatement|jdk6\\.CachedCallableStatementJDK6|jdk6\\.CachedPreparedStatementJDK6|jdk6\\.WrappedCallableStatementJDK6|jdk6\\.WrappedConnectionJDK6|jdk6\\.WrappedPreparedStatementJDK6|jdk6\\.WrappedStatementJDK6|local\\.LocalCallableStatement|local\\.LocalConnection|local\\.LocalPreparedStatement|local\\.LocalStatement|WrappedCallableStatement|WrappedConnection|WrappedPreparedStatement|WrappedStatement)|org\\.postgresql\\.jdbc(2\\.AbstractJdbc2|3\\.AbstractJdbc3)Statement|weblogic\\.jdbc\\.wrapper\\.(CallableStatement|Connection|XAConnection|JTAConnection|JTSConnection|PreparedStatement|Statement)|com\\.sybase\\.jdbc(x|3\\.jdbc|4\\.jdbc)\\.SybConnection"
				, "        (-) class:.pool.PoolUtils$CheckedKeyedObjectPool"
				, "        (-) class:.pool.PoolUtils$CheckedObjectPool"
				, "        (-) class:.pool.PoolUtils$ErodingKeyedObjectPool"
				, "        (-) class:.pool.PoolUtils$ErodingObjectPool"
				, "        (-) class:.pool.impl.GenericKeyedObjectPool"
				, "        (-) class:.pool.ObjectPool"
				, "        (-) class:.pool.PoolUtils$ObjectPoolAdaptor"
				, "        (-) class:.pool.impl.SoftReferenceKeyedObjectPool"
				, "        (-) class:.pool.impl.SoftReferenceObjectPool"
				, "        (-) class:.pool.impl.StackKeyedObjectPool"
				, "        (-) class:.pool.impl.StackObjectPool"
				, "        (-) class:.pool.PoolUtils$SynchronizedKeyedObjectPool"
				, "        (-) class:.pool.PoolUtils$SynchronizedObjectPool"
				, "        (-) class:.dbcp.PoolableConnection"
				, "        (-) class:.dbcp.AbandonedObjectPool"
				, "        (-) class:.dbcp.PoolingDriver"
				, "        (-) class:.dbcp.PoolingDataSource"
				, "        (-) class:.dbcp.PoolableConnectionFactory"
				, "        (-) class:.dbcp.datasources.PerUserPoolDataSource"
				, "        (-) class:.dbcp.cpdsadapter.PooledConnectionImpl"
				), item, xmlLeft);
		
		checkCollectChangedElements(Arrays.asList(
				  "(#) sensor"
				, "(+) class:com\\.(ibm\\.ws\\.rsadapter\\.jdbc\\.WSJdbcDataSource|mchange\\.v2\\.c3p0.*(PoolBacked|DriverManager)DataSource.*)|java\\.sql\\.Driver(Manager)?|javax\\.sql\\.(ConnectionPoolDataSource|DataSource|PooledConnection|XAConnection|XADataSource)|oracle\\.jdbc\\.(pool\\.OracleConnectionPoolDataSource|pool\\.OracleDataSource|xa\\.client\\.OracleXADataSource|xa\\.OracleXADataSource)|org\\.(apache\\.tomcat\\.jdbc\\.pool\\.DataSourceProxy|jboss\\.resource\\.adapter\\.jdbc\\.(local\\.LocalDataSource|WrapperDataSource))|weblogic\\.jdbc\\.(common\\.internal\\.RmiDataSource|jta\\.DataSource|jts\\.Driver|pool\\.Driver)|org\\.apache\\..*\\.dbcp.?\\.(BasicDataSource|datasources\\.InstanceKeyDataSource|datasources\\.InstanceKeyDataSource|datasources\\.SharedPoolDataSource|managed\\.ManagedDataSource|PoolingDataSource|PoolingDriver)|com\\.sun\\.gjc\\.spi\\.jdbc.?.?\\.DataSource.?.?|com\\.sun\\.gjc\\.spi\\.base\\.(Abstract)?DataSource"
				, "(+) class:com\\.(ibm\\.ws\\.rsadapter\\.jdbc\\.WSJdbc(Connection|PreparedStatement|Statement)|mchange\\.v2\\.c3p0\\.(C3P0ProxyConnection|impl\\.C3P0PooledConnection\\$ProxyConnection|impl\\.C3P0PooledConnection.*Proxy.*Statement|impl\\.NewProxyCallableStatement|impl\\.NewProxyConnection|impl\\.NewProxyConnection|impl\\.NewProxyPreparedStatement|impl\\.NewProxyStatement)|microsoft\\.sqlserver\\.jdbc\\.SQLServer(CallableStatement|Connection|PreparedStatement|Statement)|mysql\\.jdbc\\.(CallableStatement|ConnectionImpl|PreparedStatement|StatementImpl))|org\\.apache\\.(derby(\\.iapi\\.jdbc\\.(BrokeredCallableStatement.*|BrokeredPreparedStatement.*)|\\.impl\\.jdbc\\.Embed(CallableStatement|Connection|PreparedStatement|Statement)).*|.*\\.dbcp.?\\.(cpdsadapter\\.ConnectionImpl|cpdsadapter\\.PooledConnectionImpl|managed\\.ManagedConnection|PoolableConnection|PoolingConnection|PoolingDataSource$PoolGuardConnectionWrapper|DelegatingConnection|.cpdsadapter\\.PoolablePreparedStatementStub|DelegatingCallableStatement|DelegatingPreparedStatement|DelegatingStatement|PoolableCallableStatement|PoolablePreparedStatement))|java\\.sql\\.(CallableStatement|Connection|PreparedStatement|Statement)|net\\.sourceforge\\.jtds\\.jdbc\\.Jtds(CallableStatement|PreparedStatement|Statement)|oracle\\.jdbc\\.(OracleConnectionWrapper|driver\\.(OracleCallableStatement|OraclePreparedStatementWrapper|OraclePreparedStatement|OracleStatement|PhysicalConnection))|org\\.compiere\\.util\\.C(PreparedStatement|Statement)|org\\.jboss\\.resource\\.adapter\\.jdbc\\.(CachedCallableStatement|CachedPreparedStatement|jdk6\\.CachedCallableStatementJDK6|jdk6\\.CachedPreparedStatementJDK6|jdk6\\.WrappedCallableStatementJDK6|jdk6\\.WrappedConnectionJDK6|jdk6\\.WrappedPreparedStatementJDK6|jdk6\\.WrappedStatementJDK6|local\\.LocalCallableStatement|local\\.LocalConnection|local\\.LocalPreparedStatement|local\\.LocalStatement|WrappedCallableStatement|WrappedConnection|WrappedPreparedStatement|WrappedStatement)|org\\.postgresql\\.jdbc(2\\.AbstractJdbc2|3\\.AbstractJdbc3)Statement|weblogic\\.jdbc\\.wrapper\\.(CallableStatement|Connection|XAConnection|JTAConnection|JTSConnection|PreparedStatement|Statement)|com\\.sybase\\.jdbc(x|3\\.jdbc|4\\.jdbc)\\.SybConnection"
				, "(+) class:.ool.PoolUtils$CheckedKeyedObjectPool"
				, "(+) class:.ol.PoolUtils$CheckedObjectPool"
				, "(+) class:.l.PoolUtils$ErodingKeyedObjectPool"
				, "(+) class:.PoolUtils$ErodingObjectPool"
				, "(+) class:.impl.GenericKeyedObjectPool"
				, "(+) class:.pool2.impl.BaseGenericObjectPool"
				, "(+) class:.pool2.impl.GenericObjectPool"
				, "(+) class:.ObjectPool"
				, "(+) class:.PoolUtils$ObjectPoolAdaptor"
				, "(+) class:.impl.SoftReferenceKeyedObjectPool"
				, "(+) class:.impl.SoftReferenceObjectPool"
				, "(+) class:.impl.StackKeyedObjectPool"
				, "(+) class:.impl.StackObjectPool"
				, "(+) class:.PoolUtils$SynchronizedKeyedObjectPool"
				, "(+) class:.PoolUtils$SynchronizedObjectPool"
				, "(+) class:.PoolableConnection"
				, "(+) class:.AbandonedObjectPool"
				, "(+) class:.PoolingDriver"
				, "(+) class:.PoolingDataSource"
				, "(+) class:.PoolableConnectionFactory"
				, "(+) class:.datasources.PerUserPoolDataSource"
				, "(+) class:.cpdsadapter.PooledConnectionImpl"
				), 24 + 1, item, xmlRight);

		checkCollectChangedElements(Arrays.asList(
				  "(#) sensor"
				, "(-) class:com\\.(ibm\\.ws\\.rsadapter\\.jdbc\\.WSJdbcDataSource|mchange\\.v2\\.c3p0.*(PoolBacked|DriverManager)DataSource.*)|java\\.sql\\.Driver(Manager)?|javax\\.sql\\.(ConnectionPoolDataSource|DataSource|PooledConnection|XAConnection|XADataSource)|oracle\\.jdbc\\.(pool\\.OracleConnectionPoolDataSource|pool\\.OracleDataSource|xa\\.client\\.OracleXADataSource|xa\\.OracleXADataSource)|org\\.(apache\\.tomcat\\.jdbc\\.pool\\.DataSourceProxy|jboss\\.resource\\.adapter\\.jdbc\\.(local\\.LocalDataSource|WrapperDataSource))|weblogic\\.jdbc\\.(common\\.internal\\.RmiDataSource|jta\\.DataSource|jts\\.Driver|pool\\.Driver)|org\\.apache\\..*\\.dbcp\\.(BasicDataSource|datasources\\.InstanceKeyDataSource|datasources\\.InstanceKeyDataSource|datasources\\.SharedPoolDataSource|managed\\.ManagedDataSource|PoolingDataSource|PoolingDriver)|com\\.sun\\.gjc\\.spi\\.jdbc.?.?\\.DataSource.?.?|com\\\\.sun\\\\.gjc\\\\.spi\\\\.base\\\\.(Abstract)?DataSource"
				, "(-) class:com\\.(ibm\\.ws\\.rsadapter\\.jdbc\\.WSJdbc(Connection|PreparedStatement|Statement)|mchange\\.v2\\.c3p0\\.(C3P0ProxyConnection|impl\\.C3P0PooledConnection\\$ProxyConnection|impl\\.C3P0PooledConnection.*Proxy.*Statement|impl\\.NewProxyCallableStatement|impl\\.NewProxyConnection|impl\\.NewProxyConnection|impl\\.NewProxyPreparedStatement|impl\\.NewProxyStatement)|microsoft\\.sqlserver\\.jdbc\\.SQLServer(CallableStatement|Connection|PreparedStatement|Statement)|mysql\\.jdbc\\.(CallableStatement|ConnectionImpl|PreparedStatement|StatementImpl))|org\\.apache\\.(derby(\\.iapi\\.jdbc\\.(BrokeredCallableStatement.*|BrokeredPreparedStatement.*)|\\.impl\\.jdbc\\.Embed(CallableStatement|Connection|PreparedStatement|Statement)).*|.*\\.dbcp\\.(cpdsadapter\\.ConnectionImpl|cpdsadapter\\.PooledConnectionImpl|managed\\.ManagedConnection|PoolableConnection|PoolingConnection|PoolingDataSource$PoolGuardConnectionWrapper|DelegatingConnection|.cpdsadapter\\.PoolablePreparedStatementStub|DelegatingCallableStatement|DelegatingPreparedStatement|DelegatingStatement|PoolableCallableStatement|PoolablePreparedStatement))|java\\.sql\\.(CallableStatement|Connection|PreparedStatement|Statement)|net\\.sourceforge\\.jtds\\.jdbc\\.Jtds(CallableStatement|PreparedStatement|Statement)|oracle\\.jdbc\\.(OracleConnectionWrapper|driver\\.(OracleCallableStatement|OraclePreparedStatementWrapper|OraclePreparedStatement|OracleStatement|PhysicalConnection))|org\\.compiere\\.util\\.C(PreparedStatement|Statement)|org\\.jboss\\.resource\\.adapter\\.jdbc\\.(CachedCallableStatement|CachedPreparedStatement|jdk6\\.CachedCallableStatementJDK6|jdk6\\.CachedPreparedStatementJDK6|jdk6\\.WrappedCallableStatementJDK6|jdk6\\.WrappedConnectionJDK6|jdk6\\.WrappedPreparedStatementJDK6|jdk6\\.WrappedStatementJDK6|local\\.LocalCallableStatement|local\\.LocalConnection|local\\.LocalPreparedStatement|local\\.LocalStatement|WrappedCallableStatement|WrappedConnection|WrappedPreparedStatement|WrappedStatement)|org\\.postgresql\\.jdbc(2\\.AbstractJdbc2|3\\.AbstractJdbc3)Statement|weblogic\\.jdbc\\.wrapper\\.(CallableStatement|Connection|XAConnection|JTAConnection|JTSConnection|PreparedStatement|Statement)|com\\.sybase\\.jdbc(x|3\\.jdbc|4\\.jdbc)\\.SybConnection"
				, "(-) class:.pool.PoolUtils$CheckedKeyedObjectPool"
				, "(-) class:.pool.PoolUtils$CheckedObjectPool"
				, "(-) class:.pool.PoolUtils$ErodingKeyedObjectPool"
				, "(-) class:.pool.PoolUtils$ErodingObjectPool"
				, "(-) class:.pool.impl.GenericKeyedObjectPool"
				, "(-) class:.pool.ObjectPool"
				, "(-) class:.pool.PoolUtils$ObjectPoolAdaptor"
				, "(-) class:.pool.impl.SoftReferenceKeyedObjectPool"
				, "(-) class:.pool.impl.SoftReferenceObjectPool"
				, "(-) class:.pool.impl.StackKeyedObjectPool"
				, "(-) class:.pool.impl.StackObjectPool"
				, "(-) class:.pool.PoolUtils$SynchronizedKeyedObjectPool"
				, "(-) class:.pool.PoolUtils$SynchronizedObjectPool"
				, "(-) class:.dbcp.PoolableConnection"
				, "(-) class:.dbcp.AbandonedObjectPool"
				, "(-) class:.dbcp.PoolingDriver"
				, "(-) class:.dbcp.PoolingDataSource"
				, "(-) class:.dbcp.PoolableConnectionFactory"
				, "(-) class:.dbcp.datasources.PerUserPoolDataSource"
				, "(-) class:.dbcp.cpdsadapter.PooledConnectionImpl"
				), 22 + 1, item, xmlLeft);
	}
	
	@Test
	public void jdbcSensorEqualityMetric() throws XMLStreamException, IOException {
		String[] inputLeft = IO.readLines(new FileReader("samples/jdbc-sensor-rev-390963.xml"));
		String[] inputRight = IO.readLines(new FileReader("samples/jdbc-sensor-rev-425369.xml"));
		
		XmlStruct xmlLeft = lexer.parse(IO.asString(inputLeft));
		XmlStruct xmlRight = lexer.parse(IO.asString(inputRight));
		
		List<String> ignoreAttributeNames = Collections.emptyList();
		XmlDiffer differ = newStringMetricXmlDiffer(ignoreAttributeNames, StringMetricXmlDiffer.getEqualityMetricResolver(), stringMetricDifferThreshold);
		XmlDiffer.Result diffResult = differ.diff(xmlLeft, xmlRight);
		Assert.assertEquals(24, diffResult.nAdded);
		Assert.assertEquals(22, diffResult.nRemoved);
		Assert.assertEquals(1, diffResult.nAttributeChanged);
		
		checkModel(Arrays.asList(
				  "plugin"
				, "  extension"
				, "    sensorpack"
				, "      (#) sensor"
				, "        (+) class:com\\.(ibm\\.ws\\.rsadapter\\.jdbc\\.WSJdbcDataSource|mchange\\.v2\\.c3p0.*(PoolBacked|DriverManager)DataSource.*)|java\\.sql\\.Driver(Manager)?|javax\\.sql\\.(ConnectionPoolDataSource|DataSource|PooledConnection|XAConnection|XADataSource)|oracle\\.jdbc\\.(pool\\.OracleConnectionPoolDataSource|pool\\.OracleDataSource|xa\\.client\\.OracleXADataSource|xa\\.OracleXADataSource)|org\\.(apache\\.tomcat\\.jdbc\\.pool\\.DataSourceProxy|jboss\\.resource\\.adapter\\.jdbc\\.(local\\.LocalDataSource|WrapperDataSource))|weblogic\\.jdbc\\.(common\\.internal\\.RmiDataSource|jta\\.DataSource|jts\\.Driver|pool\\.Driver)|org\\.apache\\..*\\.dbcp.?\\.(BasicDataSource|datasources\\.InstanceKeyDataSource|datasources\\.InstanceKeyDataSource|datasources\\.SharedPoolDataSource|managed\\.ManagedDataSource|PoolingDataSource|PoolingDriver)|com\\.sun\\.gjc\\.spi\\.jdbc.?.?\\.DataSource.?.?|com\\.sun\\.gjc\\.spi\\.base\\.(Abstract)?DataSource"
				, "        (+) class:com\\.(ibm\\.ws\\.rsadapter\\.jdbc\\.WSJdbc(Connection|PreparedStatement|Statement)|mchange\\.v2\\.c3p0\\.(C3P0ProxyConnection|impl\\.C3P0PooledConnection\\$ProxyConnection|impl\\.C3P0PooledConnection.*Proxy.*Statement|impl\\.NewProxyCallableStatement|impl\\.NewProxyConnection|impl\\.NewProxyConnection|impl\\.NewProxyPreparedStatement|impl\\.NewProxyStatement)|microsoft\\.sqlserver\\.jdbc\\.SQLServer(CallableStatement|Connection|PreparedStatement|Statement)|mysql\\.jdbc\\.(CallableStatement|ConnectionImpl|PreparedStatement|StatementImpl))|org\\.apache\\.(derby(\\.iapi\\.jdbc\\.(BrokeredCallableStatement.*|BrokeredPreparedStatement.*)|\\.impl\\.jdbc\\.Embed(CallableStatement|Connection|PreparedStatement|Statement)).*|.*\\.dbcp.?\\.(cpdsadapter\\.ConnectionImpl|cpdsadapter\\.PooledConnectionImpl|managed\\.ManagedConnection|PoolableConnection|PoolingConnection|PoolingDataSource$PoolGuardConnectionWrapper|DelegatingConnection|.cpdsadapter\\.PoolablePreparedStatementStub|DelegatingCallableStatement|DelegatingPreparedStatement|DelegatingStatement|PoolableCallableStatement|PoolablePreparedStatement))|java\\.sql\\.(CallableStatement|Connection|PreparedStatement|Statement)|net\\.sourceforge\\.jtds\\.jdbc\\.Jtds(CallableStatement|PreparedStatement|Statement)|oracle\\.jdbc\\.(OracleConnectionWrapper|driver\\.(OracleCallableStatement|OraclePreparedStatementWrapper|OraclePreparedStatement|OracleStatement|PhysicalConnection))|org\\.compiere\\.util\\.C(PreparedStatement|Statement)|org\\.jboss\\.resource\\.adapter\\.jdbc\\.(CachedCallableStatement|CachedPreparedStatement|jdk6\\.CachedCallableStatementJDK6|jdk6\\.CachedPreparedStatementJDK6|jdk6\\.WrappedCallableStatementJDK6|jdk6\\.WrappedConnectionJDK6|jdk6\\.WrappedPreparedStatementJDK6|jdk6\\.WrappedStatementJDK6|local\\.LocalCallableStatement|local\\.LocalConnection|local\\.LocalPreparedStatement|local\\.LocalStatement|WrappedCallableStatement|WrappedConnection|WrappedPreparedStatement|WrappedStatement)|org\\.postgresql\\.jdbc(2\\.AbstractJdbc2|3\\.AbstractJdbc3)Statement|weblogic\\.jdbc\\.wrapper\\.(CallableStatement|Connection|XAConnection|JTAConnection|JTSConnection|PreparedStatement|Statement)|com\\.sybase\\.jdbc(x|3\\.jdbc|4\\.jdbc)\\.SybConnection"
				, "        (+) class:.ool.PoolUtils$CheckedKeyedObjectPool"
				, "        (+) class:.ol.PoolUtils$CheckedObjectPool"
				, "        (+) class:.l.PoolUtils$ErodingKeyedObjectPool"
				, "        (+) class:.PoolUtils$ErodingObjectPool"
				, "        (+) class:.impl.GenericKeyedObjectPool"
				, "        (+) class:.pool2.impl.BaseGenericObjectPool"
				, "        (+) class:.pool2.impl.GenericObjectPool"
				, "        (+) class:.ObjectPool"
				, "        (+) class:.PoolUtils$ObjectPoolAdaptor"
				, "        (+) class:.impl.SoftReferenceKeyedObjectPool"
				, "        (+) class:.impl.SoftReferenceObjectPool"
				, "        (+) class:.impl.StackKeyedObjectPool"
				, "        (+) class:.impl.StackObjectPool"
				, "        (+) class:.PoolUtils$SynchronizedKeyedObjectPool"
				, "        (+) class:.PoolUtils$SynchronizedObjectPool"
				, "        (+) class:.PoolableConnection"
				, "        (+) class:.AbandonedObjectPool"
				, "        (+) class:.PoolingDriver"
				, "        (+) class:.PoolingDataSource"
				, "        (+) class:.PoolableConnectionFactory"
				, "        (+) class:.datasources.PerUserPoolDataSource"
				, "        (+) class:.cpdsadapter.PooledConnectionImpl"
				), item, xmlRight);
		
		checkModel(Arrays.asList(
				"plugin"
				, "  extension"
				, "    sensorpack"
				, "      (#) sensor"
				, "        (-) class:com\\.(ibm\\.ws\\.rsadapter\\.jdbc\\.WSJdbcDataSource|mchange\\.v2\\.c3p0.*(PoolBacked|DriverManager)DataSource.*)|java\\.sql\\.Driver(Manager)?|javax\\.sql\\.(ConnectionPoolDataSource|DataSource|PooledConnection|XAConnection|XADataSource)|oracle\\.jdbc\\.(pool\\.OracleConnectionPoolDataSource|pool\\.OracleDataSource|xa\\.client\\.OracleXADataSource|xa\\.OracleXADataSource)|org\\.(apache\\.tomcat\\.jdbc\\.pool\\.DataSourceProxy|jboss\\.resource\\.adapter\\.jdbc\\.(local\\.LocalDataSource|WrapperDataSource))|weblogic\\.jdbc\\.(common\\.internal\\.RmiDataSource|jta\\.DataSource|jts\\.Driver|pool\\.Driver)|org\\.apache\\..*\\.dbcp\\.(BasicDataSource|datasources\\.InstanceKeyDataSource|datasources\\.InstanceKeyDataSource|datasources\\.SharedPoolDataSource|managed\\.ManagedDataSource|PoolingDataSource|PoolingDriver)|com\\.sun\\.gjc\\.spi\\.jdbc.?.?\\.DataSource.?.?|com\\\\.sun\\\\.gjc\\\\.spi\\\\.base\\\\.(Abstract)?DataSource"
				, "        (-) class:com\\.(ibm\\.ws\\.rsadapter\\.jdbc\\.WSJdbc(Connection|PreparedStatement|Statement)|mchange\\.v2\\.c3p0\\.(C3P0ProxyConnection|impl\\.C3P0PooledConnection\\$ProxyConnection|impl\\.C3P0PooledConnection.*Proxy.*Statement|impl\\.NewProxyCallableStatement|impl\\.NewProxyConnection|impl\\.NewProxyConnection|impl\\.NewProxyPreparedStatement|impl\\.NewProxyStatement)|microsoft\\.sqlserver\\.jdbc\\.SQLServer(CallableStatement|Connection|PreparedStatement|Statement)|mysql\\.jdbc\\.(CallableStatement|ConnectionImpl|PreparedStatement|StatementImpl))|org\\.apache\\.(derby(\\.iapi\\.jdbc\\.(BrokeredCallableStatement.*|BrokeredPreparedStatement.*)|\\.impl\\.jdbc\\.Embed(CallableStatement|Connection|PreparedStatement|Statement)).*|.*\\.dbcp\\.(cpdsadapter\\.ConnectionImpl|cpdsadapter\\.PooledConnectionImpl|managed\\.ManagedConnection|PoolableConnection|PoolingConnection|PoolingDataSource$PoolGuardConnectionWrapper|DelegatingConnection|.cpdsadapter\\.PoolablePreparedStatementStub|DelegatingCallableStatement|DelegatingPreparedStatement|DelegatingStatement|PoolableCallableStatement|PoolablePreparedStatement))|java\\.sql\\.(CallableStatement|Connection|PreparedStatement|Statement)|net\\.sourceforge\\.jtds\\.jdbc\\.Jtds(CallableStatement|PreparedStatement|Statement)|oracle\\.jdbc\\.(OracleConnectionWrapper|driver\\.(OracleCallableStatement|OraclePreparedStatementWrapper|OraclePreparedStatement|OracleStatement|PhysicalConnection))|org\\.compiere\\.util\\.C(PreparedStatement|Statement)|org\\.jboss\\.resource\\.adapter\\.jdbc\\.(CachedCallableStatement|CachedPreparedStatement|jdk6\\.CachedCallableStatementJDK6|jdk6\\.CachedPreparedStatementJDK6|jdk6\\.WrappedCallableStatementJDK6|jdk6\\.WrappedConnectionJDK6|jdk6\\.WrappedPreparedStatementJDK6|jdk6\\.WrappedStatementJDK6|local\\.LocalCallableStatement|local\\.LocalConnection|local\\.LocalPreparedStatement|local\\.LocalStatement|WrappedCallableStatement|WrappedConnection|WrappedPreparedStatement|WrappedStatement)|org\\.postgresql\\.jdbc(2\\.AbstractJdbc2|3\\.AbstractJdbc3)Statement|weblogic\\.jdbc\\.wrapper\\.(CallableStatement|Connection|XAConnection|JTAConnection|JTSConnection|PreparedStatement|Statement)|com\\.sybase\\.jdbc(x|3\\.jdbc|4\\.jdbc)\\.SybConnection"
				, "        (-) class:.pool.PoolUtils$CheckedKeyedObjectPool"
				, "        (-) class:.pool.PoolUtils$CheckedObjectPool"
				, "        (-) class:.pool.PoolUtils$ErodingKeyedObjectPool"
				, "        (-) class:.pool.PoolUtils$ErodingObjectPool"
				, "        (-) class:.pool.impl.GenericKeyedObjectPool"
				, "        (-) class:.pool.ObjectPool"
				, "        (-) class:.pool.PoolUtils$ObjectPoolAdaptor"
				, "        (-) class:.pool.impl.SoftReferenceKeyedObjectPool"
				, "        (-) class:.pool.impl.SoftReferenceObjectPool"
				, "        (-) class:.pool.impl.StackKeyedObjectPool"
				, "        (-) class:.pool.impl.StackObjectPool"
				, "        (-) class:.pool.PoolUtils$SynchronizedKeyedObjectPool"
				, "        (-) class:.pool.PoolUtils$SynchronizedObjectPool"
				, "        (-) class:.dbcp.PoolableConnection"
				, "        (-) class:.dbcp.AbandonedObjectPool"
				, "        (-) class:.dbcp.PoolingDriver"
				, "        (-) class:.dbcp.PoolingDataSource"
				, "        (-) class:.dbcp.PoolableConnectionFactory"
				, "        (-) class:.dbcp.datasources.PerUserPoolDataSource"
				, "        (-) class:.dbcp.cpdsadapter.PooledConnectionImpl"
				), item, xmlLeft);
		
		checkCollectChangedElements(Arrays.asList(
				  "(#) sensor"
				, "(+) class:com\\.(ibm\\.ws\\.rsadapter\\.jdbc\\.WSJdbcDataSource|mchange\\.v2\\.c3p0.*(PoolBacked|DriverManager)DataSource.*)|java\\.sql\\.Driver(Manager)?|javax\\.sql\\.(ConnectionPoolDataSource|DataSource|PooledConnection|XAConnection|XADataSource)|oracle\\.jdbc\\.(pool\\.OracleConnectionPoolDataSource|pool\\.OracleDataSource|xa\\.client\\.OracleXADataSource|xa\\.OracleXADataSource)|org\\.(apache\\.tomcat\\.jdbc\\.pool\\.DataSourceProxy|jboss\\.resource\\.adapter\\.jdbc\\.(local\\.LocalDataSource|WrapperDataSource))|weblogic\\.jdbc\\.(common\\.internal\\.RmiDataSource|jta\\.DataSource|jts\\.Driver|pool\\.Driver)|org\\.apache\\..*\\.dbcp.?\\.(BasicDataSource|datasources\\.InstanceKeyDataSource|datasources\\.InstanceKeyDataSource|datasources\\.SharedPoolDataSource|managed\\.ManagedDataSource|PoolingDataSource|PoolingDriver)|com\\.sun\\.gjc\\.spi\\.jdbc.?.?\\.DataSource.?.?|com\\.sun\\.gjc\\.spi\\.base\\.(Abstract)?DataSource"
				, "(+) class:com\\.(ibm\\.ws\\.rsadapter\\.jdbc\\.WSJdbc(Connection|PreparedStatement|Statement)|mchange\\.v2\\.c3p0\\.(C3P0ProxyConnection|impl\\.C3P0PooledConnection\\$ProxyConnection|impl\\.C3P0PooledConnection.*Proxy.*Statement|impl\\.NewProxyCallableStatement|impl\\.NewProxyConnection|impl\\.NewProxyConnection|impl\\.NewProxyPreparedStatement|impl\\.NewProxyStatement)|microsoft\\.sqlserver\\.jdbc\\.SQLServer(CallableStatement|Connection|PreparedStatement|Statement)|mysql\\.jdbc\\.(CallableStatement|ConnectionImpl|PreparedStatement|StatementImpl))|org\\.apache\\.(derby(\\.iapi\\.jdbc\\.(BrokeredCallableStatement.*|BrokeredPreparedStatement.*)|\\.impl\\.jdbc\\.Embed(CallableStatement|Connection|PreparedStatement|Statement)).*|.*\\.dbcp.?\\.(cpdsadapter\\.ConnectionImpl|cpdsadapter\\.PooledConnectionImpl|managed\\.ManagedConnection|PoolableConnection|PoolingConnection|PoolingDataSource$PoolGuardConnectionWrapper|DelegatingConnection|.cpdsadapter\\.PoolablePreparedStatementStub|DelegatingCallableStatement|DelegatingPreparedStatement|DelegatingStatement|PoolableCallableStatement|PoolablePreparedStatement))|java\\.sql\\.(CallableStatement|Connection|PreparedStatement|Statement)|net\\.sourceforge\\.jtds\\.jdbc\\.Jtds(CallableStatement|PreparedStatement|Statement)|oracle\\.jdbc\\.(OracleConnectionWrapper|driver\\.(OracleCallableStatement|OraclePreparedStatementWrapper|OraclePreparedStatement|OracleStatement|PhysicalConnection))|org\\.compiere\\.util\\.C(PreparedStatement|Statement)|org\\.jboss\\.resource\\.adapter\\.jdbc\\.(CachedCallableStatement|CachedPreparedStatement|jdk6\\.CachedCallableStatementJDK6|jdk6\\.CachedPreparedStatementJDK6|jdk6\\.WrappedCallableStatementJDK6|jdk6\\.WrappedConnectionJDK6|jdk6\\.WrappedPreparedStatementJDK6|jdk6\\.WrappedStatementJDK6|local\\.LocalCallableStatement|local\\.LocalConnection|local\\.LocalPreparedStatement|local\\.LocalStatement|WrappedCallableStatement|WrappedConnection|WrappedPreparedStatement|WrappedStatement)|org\\.postgresql\\.jdbc(2\\.AbstractJdbc2|3\\.AbstractJdbc3)Statement|weblogic\\.jdbc\\.wrapper\\.(CallableStatement|Connection|XAConnection|JTAConnection|JTSConnection|PreparedStatement|Statement)|com\\.sybase\\.jdbc(x|3\\.jdbc|4\\.jdbc)\\.SybConnection"
				, "(+) class:.ool.PoolUtils$CheckedKeyedObjectPool"
				, "(+) class:.ol.PoolUtils$CheckedObjectPool"
				, "(+) class:.l.PoolUtils$ErodingKeyedObjectPool"
				, "(+) class:.PoolUtils$ErodingObjectPool"
				, "(+) class:.impl.GenericKeyedObjectPool"
				, "(+) class:.pool2.impl.BaseGenericObjectPool"
				, "(+) class:.pool2.impl.GenericObjectPool"
				, "(+) class:.ObjectPool"
				, "(+) class:.PoolUtils$ObjectPoolAdaptor"
				, "(+) class:.impl.SoftReferenceKeyedObjectPool"
				, "(+) class:.impl.SoftReferenceObjectPool"
				, "(+) class:.impl.StackKeyedObjectPool"
				, "(+) class:.impl.StackObjectPool"
				, "(+) class:.PoolUtils$SynchronizedKeyedObjectPool"
				, "(+) class:.PoolUtils$SynchronizedObjectPool"
				, "(+) class:.PoolableConnection"
				, "(+) class:.AbandonedObjectPool"
				, "(+) class:.PoolingDriver"
				, "(+) class:.PoolingDataSource"
				, "(+) class:.PoolableConnectionFactory"
				, "(+) class:.datasources.PerUserPoolDataSource"
				, "(+) class:.cpdsadapter.PooledConnectionImpl"
				), 24 + 1, item, xmlRight);
		
		checkCollectChangedElements(Arrays.asList(
				"(#) sensor"
				, "(-) class:com\\.(ibm\\.ws\\.rsadapter\\.jdbc\\.WSJdbcDataSource|mchange\\.v2\\.c3p0.*(PoolBacked|DriverManager)DataSource.*)|java\\.sql\\.Driver(Manager)?|javax\\.sql\\.(ConnectionPoolDataSource|DataSource|PooledConnection|XAConnection|XADataSource)|oracle\\.jdbc\\.(pool\\.OracleConnectionPoolDataSource|pool\\.OracleDataSource|xa\\.client\\.OracleXADataSource|xa\\.OracleXADataSource)|org\\.(apache\\.tomcat\\.jdbc\\.pool\\.DataSourceProxy|jboss\\.resource\\.adapter\\.jdbc\\.(local\\.LocalDataSource|WrapperDataSource))|weblogic\\.jdbc\\.(common\\.internal\\.RmiDataSource|jta\\.DataSource|jts\\.Driver|pool\\.Driver)|org\\.apache\\..*\\.dbcp\\.(BasicDataSource|datasources\\.InstanceKeyDataSource|datasources\\.InstanceKeyDataSource|datasources\\.SharedPoolDataSource|managed\\.ManagedDataSource|PoolingDataSource|PoolingDriver)|com\\.sun\\.gjc\\.spi\\.jdbc.?.?\\.DataSource.?.?|com\\\\.sun\\\\.gjc\\\\.spi\\\\.base\\\\.(Abstract)?DataSource"
				, "(-) class:com\\.(ibm\\.ws\\.rsadapter\\.jdbc\\.WSJdbc(Connection|PreparedStatement|Statement)|mchange\\.v2\\.c3p0\\.(C3P0ProxyConnection|impl\\.C3P0PooledConnection\\$ProxyConnection|impl\\.C3P0PooledConnection.*Proxy.*Statement|impl\\.NewProxyCallableStatement|impl\\.NewProxyConnection|impl\\.NewProxyConnection|impl\\.NewProxyPreparedStatement|impl\\.NewProxyStatement)|microsoft\\.sqlserver\\.jdbc\\.SQLServer(CallableStatement|Connection|PreparedStatement|Statement)|mysql\\.jdbc\\.(CallableStatement|ConnectionImpl|PreparedStatement|StatementImpl))|org\\.apache\\.(derby(\\.iapi\\.jdbc\\.(BrokeredCallableStatement.*|BrokeredPreparedStatement.*)|\\.impl\\.jdbc\\.Embed(CallableStatement|Connection|PreparedStatement|Statement)).*|.*\\.dbcp\\.(cpdsadapter\\.ConnectionImpl|cpdsadapter\\.PooledConnectionImpl|managed\\.ManagedConnection|PoolableConnection|PoolingConnection|PoolingDataSource$PoolGuardConnectionWrapper|DelegatingConnection|.cpdsadapter\\.PoolablePreparedStatementStub|DelegatingCallableStatement|DelegatingPreparedStatement|DelegatingStatement|PoolableCallableStatement|PoolablePreparedStatement))|java\\.sql\\.(CallableStatement|Connection|PreparedStatement|Statement)|net\\.sourceforge\\.jtds\\.jdbc\\.Jtds(CallableStatement|PreparedStatement|Statement)|oracle\\.jdbc\\.(OracleConnectionWrapper|driver\\.(OracleCallableStatement|OraclePreparedStatementWrapper|OraclePreparedStatement|OracleStatement|PhysicalConnection))|org\\.compiere\\.util\\.C(PreparedStatement|Statement)|org\\.jboss\\.resource\\.adapter\\.jdbc\\.(CachedCallableStatement|CachedPreparedStatement|jdk6\\.CachedCallableStatementJDK6|jdk6\\.CachedPreparedStatementJDK6|jdk6\\.WrappedCallableStatementJDK6|jdk6\\.WrappedConnectionJDK6|jdk6\\.WrappedPreparedStatementJDK6|jdk6\\.WrappedStatementJDK6|local\\.LocalCallableStatement|local\\.LocalConnection|local\\.LocalPreparedStatement|local\\.LocalStatement|WrappedCallableStatement|WrappedConnection|WrappedPreparedStatement|WrappedStatement)|org\\.postgresql\\.jdbc(2\\.AbstractJdbc2|3\\.AbstractJdbc3)Statement|weblogic\\.jdbc\\.wrapper\\.(CallableStatement|Connection|XAConnection|JTAConnection|JTSConnection|PreparedStatement|Statement)|com\\.sybase\\.jdbc(x|3\\.jdbc|4\\.jdbc)\\.SybConnection"
				, "(-) class:.pool.PoolUtils$CheckedKeyedObjectPool"
				, "(-) class:.pool.PoolUtils$CheckedObjectPool"
				, "(-) class:.pool.PoolUtils$ErodingKeyedObjectPool"
				, "(-) class:.pool.PoolUtils$ErodingObjectPool"
				, "(-) class:.pool.impl.GenericKeyedObjectPool"
				, "(-) class:.pool.ObjectPool"
				, "(-) class:.pool.PoolUtils$ObjectPoolAdaptor"
				, "(-) class:.pool.impl.SoftReferenceKeyedObjectPool"
				, "(-) class:.pool.impl.SoftReferenceObjectPool"
				, "(-) class:.pool.impl.StackKeyedObjectPool"
				, "(-) class:.pool.impl.StackObjectPool"
				, "(-) class:.pool.PoolUtils$SynchronizedKeyedObjectPool"
				, "(-) class:.pool.PoolUtils$SynchronizedObjectPool"
				, "(-) class:.dbcp.PoolableConnection"
				, "(-) class:.dbcp.AbandonedObjectPool"
				, "(-) class:.dbcp.PoolingDriver"
				, "(-) class:.dbcp.PoolingDataSource"
				, "(-) class:.dbcp.PoolableConnectionFactory"
				, "(-) class:.dbcp.datasources.PerUserPoolDataSource"
				, "(-) class:.dbcp.cpdsadapter.PooledConnectionImpl"
				), 22 + 1, item, xmlLeft);
	}

	@Test
	public void jdbcSensorLevenshtein() throws XMLStreamException, IOException {
		String[] inputLeft = IO.readLines(new FileReader("samples/jdbc-sensor-rev-390963.xml"));
		String[] inputRight = IO.readLines(new FileReader("samples/jdbc-sensor-rev-425369.xml"));
		
		XmlStruct xmlLeft = lexer.parse(IO.asString(inputLeft));
		XmlStruct xmlRight = lexer.parse(IO.asString(inputRight));
		
		List<String> ignoreAttributeNames = Collections.emptyList();
		XmlDiffer differ = newStringMetricXmlDiffer(ignoreAttributeNames, levenshteinMetricResolver, stringMetricDifferThreshold);
		XmlDiffer.Result diffResult = differ.diff(xmlLeft, xmlRight);
		Assert.assertEquals(5, diffResult.nAdded);
		Assert.assertEquals(2, diffResult.nRemoved);
		Assert.assertEquals(25, diffResult.nAttributeChanged);
		
		List<XmlElement> collected;
		
		checkModel(Arrays.asList(
				  "plugin"
				, "  extension"
				, "    sensorpack"
				, "      (#) sensor"
				, "        (#) class:com\\.(ibm\\.ws\\.rsadapter\\.jdbc\\.WSJdbcDataSource|mchange\\.v2\\.c3p0.*(PoolBacked|DriverManager)DataSource.*)|java\\.sql\\.Driver(Manager)?|javax\\.sql\\.(ConnectionPoolDataSource|DataSource|PooledConnection|XAConnection|XADataSource)|oracle\\.jdbc\\.(pool\\.OracleConnectionPoolDataSource|pool\\.OracleDataSource|xa\\.client\\.OracleXADataSource|xa\\.OracleXADataSource)|org\\.(apache\\.tomcat\\.jdbc\\.pool\\.DataSourceProxy|jboss\\.resource\\.adapter\\.jdbc\\.(local\\.LocalDataSource|WrapperDataSource))|weblogic\\.jdbc\\.(common\\.internal\\.RmiDataSource|jta\\.DataSource|jts\\.Driver|pool\\.Driver)|org\\.apache\\..*\\.dbcp.?\\.(BasicDataSource|datasources\\.InstanceKeyDataSource|datasources\\.InstanceKeyDataSource|datasources\\.SharedPoolDataSource|managed\\.ManagedDataSource|PoolingDataSource|PoolingDriver)|com\\.sun\\.gjc\\.spi\\.jdbc.?.?\\.DataSource.?.?|com\\.sun\\.gjc\\.spi\\.base\\.(Abstract)?DataSource"
				, "          method:getConnection"
				, "            (#) argument:javax.lang.String"
				, "          method:getConnection"
				, "            (#) argument:javax.lang.String"
				, "        (#) class:com\\.(ibm\\.ws\\.rsadapter\\.jdbc\\.WSJdbc(Connection|PreparedStatement|Statement)|mchange\\.v2\\.c3p0\\.(C3P0ProxyConnection|impl\\.C3P0PooledConnection\\$ProxyConnection|impl\\.C3P0PooledConnection.*Proxy.*Statement|impl\\.NewProxyCallableStatement|impl\\.NewProxyConnection|impl\\.NewProxyConnection|impl\\.NewProxyPreparedStatement|impl\\.NewProxyStatement)|microsoft\\.sqlserver\\.jdbc\\.SQLServer(CallableStatement|Connection|PreparedStatement|Statement)|mysql\\.jdbc\\.(CallableStatement|ConnectionImpl|PreparedStatement|StatementImpl))|org\\.apache\\.(derby(\\.iapi\\.jdbc\\.(BrokeredCallableStatement.*|BrokeredPreparedStatement.*)|\\.impl\\.jdbc\\.Embed(CallableStatement|Connection|PreparedStatement|Statement)).*|.*\\.dbcp.?\\.(cpdsadapter\\.ConnectionImpl|cpdsadapter\\.PooledConnectionImpl|managed\\.ManagedConnection|PoolableConnection|PoolingConnection|PoolingDataSource$PoolGuardConnectionWrapper|DelegatingConnection|.cpdsadapter\\.PoolablePreparedStatementStub|DelegatingCallableStatement|DelegatingPreparedStatement|DelegatingStatement|PoolableCallableStatement|PoolablePreparedStatement))|java\\.sql\\.(CallableStatement|Connection|PreparedStatement|Statement)|net\\.sourceforge\\.jtds\\.jdbc\\.Jtds(CallableStatement|PreparedStatement|Statement)|oracle\\.jdbc\\.(OracleConnectionWrapper|driver\\.(OracleCallableStatement|OraclePreparedStatementWrapper|OraclePreparedStatement|OracleStatement|PhysicalConnection))|org\\.compiere\\.util\\.C(PreparedStatement|Statement)|org\\.jboss\\.resource\\.adapter\\.jdbc\\.(CachedCallableStatement|CachedPreparedStatement|jdk6\\.CachedCallableStatementJDK6|jdk6\\.CachedPreparedStatementJDK6|jdk6\\.WrappedCallableStatementJDK6|jdk6\\.WrappedConnectionJDK6|jdk6\\.WrappedPreparedStatementJDK6|jdk6\\.WrappedStatementJDK6|local\\.LocalCallableStatement|local\\.LocalConnection|local\\.LocalPreparedStatement|local\\.LocalStatement|WrappedCallableStatement|WrappedConnection|WrappedPreparedStatement|WrappedStatement)|org\\.postgresql\\.jdbc(2\\.AbstractJdbc2|3\\.AbstractJdbc3)Statement|weblogic\\.jdbc\\.wrapper\\.(CallableStatement|Connection|XAConnection|JTAConnection|JTSConnection|PreparedStatement|Statement)|com\\.sybase\\.jdbc(x|3\\.jdbc|4\\.jdbc)\\.SybConnection"
				, "        (#) class:.ool.PoolUtils$CheckedKeyedObjectPool"
				, "        (#) class:.ol.PoolUtils$CheckedObjectPool"
				, "        (#) class:.l.PoolUtils$ErodingKeyedObjectPool"
				, "        (#) class:.PoolUtils$ErodingObjectPool"
				, "        (#) class:.impl.GenericKeyedObjectPool"
				, "        (+) class:.pool2.impl.BaseGenericObjectPool"
				, "        (+) class:.pool2.impl.GenericObjectPool"
				, "        (#) class:.ObjectPool"
				, "        (#) class:.PoolUtils$ObjectPoolAdaptor"
				, "        (#) class:.impl.SoftReferenceKeyedObjectPool"
				, "        (#) class:.impl.SoftReferenceObjectPool"
				, "        (#) class:.impl.StackKeyedObjectPool"
				, "        (#) class:.impl.StackObjectPool"
				, "        (#) class:.PoolUtils$SynchronizedKeyedObjectPool"
				, "        (#) class:.PoolUtils$SynchronizedObjectPool"
				, "        (#) class:.PoolableConnection"
				, "        (#) class:.AbandonedObjectPool"
				, "        (#) class:.PoolingDriver"
				, "          (+) method:registerPool"
				, "          (+) method:registerPool"
				, "        (#) class:.PoolingDataSource"
				, "        (#) class:.PoolableConnectionFactory"
				, "          (+) method:setPool"
				, "        (#) class:.datasources.PerUserPoolDataSource"
				, "        (#) class:.cpdsadapter.PooledConnectionImpl"
				), item, xmlRight);
		
		checkModel(Arrays.asList(
				  "plugin"
				, "  extension"
				, "    sensorpack"
				, "      (#) sensor"
				, "        (#) class:com\\.(ibm\\.ws\\.rsadapter\\.jdbc\\.WSJdbcDataSource|mchange\\.v2\\.c3p0.*(PoolBacked|DriverManager)DataSource.*)|java\\.sql\\.Driver(Manager)?|javax\\.sql\\.(ConnectionPoolDataSource|DataSource|PooledConnection|XAConnection|XADataSource)|oracle\\.jdbc\\.(pool\\.OracleConnectionPoolDataSource|pool\\.OracleDataSource|xa\\.client\\.OracleXADataSource|xa\\.OracleXADataSource)|org\\.(apache\\.tomcat\\.jdbc\\.pool\\.DataSourceProxy|jboss\\.resource\\.adapter\\.jdbc\\.(local\\.LocalDataSource|WrapperDataSource))|weblogic\\.jdbc\\.(common\\.internal\\.RmiDataSource|jta\\.DataSource|jts\\.Driver|pool\\.Driver)|org\\.apache\\..*\\.dbcp\\.(BasicDataSource|datasources\\.InstanceKeyDataSource|datasources\\.InstanceKeyDataSource|datasources\\.SharedPoolDataSource|managed\\.ManagedDataSource|PoolingDataSource|PoolingDriver)|com\\.sun\\.gjc\\.spi\\.jdbc.?.?\\.DataSource.?.?|com\\\\.sun\\\\.gjc\\\\.spi\\\\.base\\\\.(Abstract)?DataSource"
				, "          method:getConnection"
				, "            (#) argument:java.lang.String"
				, "          method:getConnection"
				, "            (#) argument:java.lang.String"
				, "        (#) class:com\\.(ibm\\.ws\\.rsadapter\\.jdbc\\.WSJdbc(Connection|PreparedStatement|Statement)|mchange\\.v2\\.c3p0\\.(C3P0ProxyConnection|impl\\.C3P0PooledConnection\\$ProxyConnection|impl\\.C3P0PooledConnection.*Proxy.*Statement|impl\\.NewProxyCallableStatement|impl\\.NewProxyConnection|impl\\.NewProxyConnection|impl\\.NewProxyPreparedStatement|impl\\.NewProxyStatement)|microsoft\\.sqlserver\\.jdbc\\.SQLServer(CallableStatement|Connection|PreparedStatement|Statement)|mysql\\.jdbc\\.(CallableStatement|ConnectionImpl|PreparedStatement|StatementImpl))|org\\.apache\\.(derby(\\.iapi\\.jdbc\\.(BrokeredCallableStatement.*|BrokeredPreparedStatement.*)|\\.impl\\.jdbc\\.Embed(CallableStatement|Connection|PreparedStatement|Statement)).*|.*\\.dbcp\\.(cpdsadapter\\.ConnectionImpl|cpdsadapter\\.PooledConnectionImpl|managed\\.ManagedConnection|PoolableConnection|PoolingConnection|PoolingDataSource$PoolGuardConnectionWrapper|DelegatingConnection|.cpdsadapter\\.PoolablePreparedStatementStub|DelegatingCallableStatement|DelegatingPreparedStatement|DelegatingStatement|PoolableCallableStatement|PoolablePreparedStatement))|java\\.sql\\.(CallableStatement|Connection|PreparedStatement|Statement)|net\\.sourceforge\\.jtds\\.jdbc\\.Jtds(CallableStatement|PreparedStatement|Statement)|oracle\\.jdbc\\.(OracleConnectionWrapper|driver\\.(OracleCallableStatement|OraclePreparedStatementWrapper|OraclePreparedStatement|OracleStatement|PhysicalConnection))|org\\.compiere\\.util\\.C(PreparedStatement|Statement)|org\\.jboss\\.resource\\.adapter\\.jdbc\\.(CachedCallableStatement|CachedPreparedStatement|jdk6\\.CachedCallableStatementJDK6|jdk6\\.CachedPreparedStatementJDK6|jdk6\\.WrappedCallableStatementJDK6|jdk6\\.WrappedConnectionJDK6|jdk6\\.WrappedPreparedStatementJDK6|jdk6\\.WrappedStatementJDK6|local\\.LocalCallableStatement|local\\.LocalConnection|local\\.LocalPreparedStatement|local\\.LocalStatement|WrappedCallableStatement|WrappedConnection|WrappedPreparedStatement|WrappedStatement)|org\\.postgresql\\.jdbc(2\\.AbstractJdbc2|3\\.AbstractJdbc3)Statement|weblogic\\.jdbc\\.wrapper\\.(CallableStatement|Connection|XAConnection|JTAConnection|JTSConnection|PreparedStatement|Statement)|com\\.sybase\\.jdbc(x|3\\.jdbc|4\\.jdbc)\\.SybConnection"
				, "        (#) class:.pool.PoolUtils$CheckedKeyedObjectPool"
				, "        (#) class:.pool.PoolUtils$CheckedObjectPool"
				, "        (#) class:.pool.PoolUtils$ErodingKeyedObjectPool"
				, "        (#) class:.pool.PoolUtils$ErodingObjectPool"
				, "        (#) class:.pool.impl.GenericKeyedObjectPool"
				, "        (#) class:.pool.ObjectPool"
				, "        (#) class:.pool.PoolUtils$ObjectPoolAdaptor"
				, "        (#) class:.pool.impl.SoftReferenceKeyedObjectPool"
				, "        (#) class:.pool.impl.SoftReferenceObjectPool"
				, "        (#) class:.pool.impl.StackKeyedObjectPool"
				, "        (#) class:.pool.impl.StackObjectPool"
				, "        (#) class:.pool.PoolUtils$SynchronizedKeyedObjectPool"
				, "        (#) class:.pool.PoolUtils$SynchronizedObjectPool"
				, "        (#) class:.dbcp.PoolableConnection"
				, "        (#) class:.dbcp.AbandonedObjectPool"
				, "        (#) class:.dbcp.PoolingDriver"
				, "          method:registerPool"
				, "            (-) argument:deletedArgument1"
				, "            (-) argument:deletedArgument2"
				, "        (#) class:.dbcp.PoolingDataSource"
				, "        (#) class:.dbcp.PoolableConnectionFactory"
				, "        (#) class:.dbcp.datasources.PerUserPoolDataSource"
				, "        (#) class:.dbcp.cpdsadapter.PooledConnectionImpl"
				), item, xmlLeft);
		
		collected = checkCollectChangedElements(Arrays.asList(
				  "(#) sensor"
				, "(#) class:com\\.(ibm\\.ws\\.rsadapter\\.jdbc\\.WSJdbcDataSource|mchange\\.v2\\.c3p0.*(PoolBacked|DriverManager)DataSource.*)|java\\.sql\\.Driver(Manager)?|javax\\.sql\\.(ConnectionPoolDataSource|DataSource|PooledConnection|XAConnection|XADataSource)|oracle\\.jdbc\\.(pool\\.OracleConnectionPoolDataSource|pool\\.OracleDataSource|xa\\.client\\.OracleXADataSource|xa\\.OracleXADataSource)|org\\.(apache\\.tomcat\\.jdbc\\.pool\\.DataSourceProxy|jboss\\.resource\\.adapter\\.jdbc\\.(local\\.LocalDataSource|WrapperDataSource))|weblogic\\.jdbc\\.(common\\.internal\\.RmiDataSource|jta\\.DataSource|jts\\.Driver|pool\\.Driver)|org\\.apache\\..*\\.dbcp.?\\.(BasicDataSource|datasources\\.InstanceKeyDataSource|datasources\\.InstanceKeyDataSource|datasources\\.SharedPoolDataSource|managed\\.ManagedDataSource|PoolingDataSource|PoolingDriver)|com\\.sun\\.gjc\\.spi\\.jdbc.?.?\\.DataSource.?.?|com\\.sun\\.gjc\\.spi\\.base\\.(Abstract)?DataSource"
				, "(#) argument:javax.lang.String"
				, "(#) argument:javax.lang.String"
				, "(#) class:com\\.(ibm\\.ws\\.rsadapter\\.jdbc\\.WSJdbc(Connection|PreparedStatement|Statement)|mchange\\.v2\\.c3p0\\.(C3P0ProxyConnection|impl\\.C3P0PooledConnection\\$ProxyConnection|impl\\.C3P0PooledConnection.*Proxy.*Statement|impl\\.NewProxyCallableStatement|impl\\.NewProxyConnection|impl\\.NewProxyConnection|impl\\.NewProxyPreparedStatement|impl\\.NewProxyStatement)|microsoft\\.sqlserver\\.jdbc\\.SQLServer(CallableStatement|Connection|PreparedStatement|Statement)|mysql\\.jdbc\\.(CallableStatement|ConnectionImpl|PreparedStatement|StatementImpl))|org\\.apache\\.(derby(\\.iapi\\.jdbc\\.(BrokeredCallableStatement.*|BrokeredPreparedStatement.*)|\\.impl\\.jdbc\\.Embed(CallableStatement|Connection|PreparedStatement|Statement)).*|.*\\.dbcp.?\\.(cpdsadapter\\.ConnectionImpl|cpdsadapter\\.PooledConnectionImpl|managed\\.ManagedConnection|PoolableConnection|PoolingConnection|PoolingDataSource$PoolGuardConnectionWrapper|DelegatingConnection|.cpdsadapter\\.PoolablePreparedStatementStub|DelegatingCallableStatement|DelegatingPreparedStatement|DelegatingStatement|PoolableCallableStatement|PoolablePreparedStatement))|java\\.sql\\.(CallableStatement|Connection|PreparedStatement|Statement)|net\\.sourceforge\\.jtds\\.jdbc\\.Jtds(CallableStatement|PreparedStatement|Statement)|oracle\\.jdbc\\.(OracleConnectionWrapper|driver\\.(OracleCallableStatement|OraclePreparedStatementWrapper|OraclePreparedStatement|OracleStatement|PhysicalConnection))|org\\.compiere\\.util\\.C(PreparedStatement|Statement)|org\\.jboss\\.resource\\.adapter\\.jdbc\\.(CachedCallableStatement|CachedPreparedStatement|jdk6\\.CachedCallableStatementJDK6|jdk6\\.CachedPreparedStatementJDK6|jdk6\\.WrappedCallableStatementJDK6|jdk6\\.WrappedConnectionJDK6|jdk6\\.WrappedPreparedStatementJDK6|jdk6\\.WrappedStatementJDK6|local\\.LocalCallableStatement|local\\.LocalConnection|local\\.LocalPreparedStatement|local\\.LocalStatement|WrappedCallableStatement|WrappedConnection|WrappedPreparedStatement|WrappedStatement)|org\\.postgresql\\.jdbc(2\\.AbstractJdbc2|3\\.AbstractJdbc3)Statement|weblogic\\.jdbc\\.wrapper\\.(CallableStatement|Connection|XAConnection|JTAConnection|JTSConnection|PreparedStatement|Statement)|com\\.sybase\\.jdbc(x|3\\.jdbc|4\\.jdbc)\\.SybConnection"
				, "(#) class:.ool.PoolUtils$CheckedKeyedObjectPool"
				, "(#) class:.ol.PoolUtils$CheckedObjectPool"
				, "(#) class:.l.PoolUtils$ErodingKeyedObjectPool"
				, "(#) class:.PoolUtils$ErodingObjectPool"
				, "(#) class:.impl.GenericKeyedObjectPool"
				, "(+) class:.pool2.impl.BaseGenericObjectPool"
				, "(+) class:.pool2.impl.GenericObjectPool"
				, "(#) class:.ObjectPool"
				, "(#) class:.PoolUtils$ObjectPoolAdaptor"
				, "(#) class:.impl.SoftReferenceKeyedObjectPool"
				, "(#) class:.impl.SoftReferenceObjectPool"
				, "(#) class:.impl.StackKeyedObjectPool"
				, "(#) class:.impl.StackObjectPool"
				, "(#) class:.PoolUtils$SynchronizedKeyedObjectPool"
				, "(#) class:.PoolUtils$SynchronizedObjectPool"
				, "(#) class:.PoolableConnection"
				, "(#) class:.AbandonedObjectPool"
				, "(#) class:.PoolingDriver"
				, "(+) method:registerPool"
				, "(+) method:registerPool"
				, "(#) class:.PoolingDataSource"
				, "(#) class:.PoolableConnectionFactory"
				, "(+) method:setPool"
				, "(#) class:.datasources.PerUserPoolDataSource"
				, "(#) class:.cpdsadapter.PooledConnectionImpl"
				), 5 + 25, item, xmlRight);
		
		checkPeers(Arrays.asList(
				  "element (#) sensor"
				, "---peer (#) sensor"
				, "element (#) class:com\\.(ibm\\.ws\\.rsadapter\\.jdbc\\.WSJdbcDataSource|mchange\\.v2\\.c3p0.*(PoolBacked|DriverManager)DataSource.*)|java\\.sql\\.Driver(Manager)?|javax\\.sql\\.(ConnectionPoolDataSource|DataSource|PooledConnection|XAConnection|XADataSource)|oracle\\.jdbc\\.(pool\\.OracleConnectionPoolDataSource|pool\\.OracleDataSource|xa\\.client\\.OracleXADataSource|xa\\.OracleXADataSource)|org\\.(apache\\.tomcat\\.jdbc\\.pool\\.DataSourceProxy|jboss\\.resource\\.adapter\\.jdbc\\.(local\\.LocalDataSource|WrapperDataSource))|weblogic\\.jdbc\\.(common\\.internal\\.RmiDataSource|jta\\.DataSource|jts\\.Driver|pool\\.Driver)|org\\.apache\\..*\\.dbcp.?\\.(BasicDataSource|datasources\\.InstanceKeyDataSource|datasources\\.InstanceKeyDataSource|datasources\\.SharedPoolDataSource|managed\\.ManagedDataSource|PoolingDataSource|PoolingDriver)|com\\.sun\\.gjc\\.spi\\.jdbc.?.?\\.DataSource.?.?|com\\.sun\\.gjc\\.spi\\.base\\.(Abstract)?DataSource"
				, "---peer (#) class:com\\.(ibm\\.ws\\.rsadapter\\.jdbc\\.WSJdbcDataSource|mchange\\.v2\\.c3p0.*(PoolBacked|DriverManager)DataSource.*)|java\\.sql\\.Driver(Manager)?|javax\\.sql\\.(ConnectionPoolDataSource|DataSource|PooledConnection|XAConnection|XADataSource)|oracle\\.jdbc\\.(pool\\.OracleConnectionPoolDataSource|pool\\.OracleDataSource|xa\\.client\\.OracleXADataSource|xa\\.OracleXADataSource)|org\\.(apache\\.tomcat\\.jdbc\\.pool\\.DataSourceProxy|jboss\\.resource\\.adapter\\.jdbc\\.(local\\.LocalDataSource|WrapperDataSource))|weblogic\\.jdbc\\.(common\\.internal\\.RmiDataSource|jta\\.DataSource|jts\\.Driver|pool\\.Driver)|org\\.apache\\..*\\.dbcp\\.(BasicDataSource|datasources\\.InstanceKeyDataSource|datasources\\.InstanceKeyDataSource|datasources\\.SharedPoolDataSource|managed\\.ManagedDataSource|PoolingDataSource|PoolingDriver)|com\\.sun\\.gjc\\.spi\\.jdbc.?.?\\.DataSource.?.?|com\\\\.sun\\\\.gjc\\\\.spi\\\\.base\\\\.(Abstract)?DataSource"
				, "element (#) argument:javax.lang.String"
				, "---peer (#) argument:java.lang.String"
				, "element (#) argument:javax.lang.String"
				, "---peer (#) argument:java.lang.String"
				, "element (#) class:com\\.(ibm\\.ws\\.rsadapter\\.jdbc\\.WSJdbc(Connection|PreparedStatement|Statement)|mchange\\.v2\\.c3p0\\.(C3P0ProxyConnection|impl\\.C3P0PooledConnection\\$ProxyConnection|impl\\.C3P0PooledConnection.*Proxy.*Statement|impl\\.NewProxyCallableStatement|impl\\.NewProxyConnection|impl\\.NewProxyConnection|impl\\.NewProxyPreparedStatement|impl\\.NewProxyStatement)|microsoft\\.sqlserver\\.jdbc\\.SQLServer(CallableStatement|Connection|PreparedStatement|Statement)|mysql\\.jdbc\\.(CallableStatement|ConnectionImpl|PreparedStatement|StatementImpl))|org\\.apache\\.(derby(\\.iapi\\.jdbc\\.(BrokeredCallableStatement.*|BrokeredPreparedStatement.*)|\\.impl\\.jdbc\\.Embed(CallableStatement|Connection|PreparedStatement|Statement)).*|.*\\.dbcp.?\\.(cpdsadapter\\.ConnectionImpl|cpdsadapter\\.PooledConnectionImpl|managed\\.ManagedConnection|PoolableConnection|PoolingConnection|PoolingDataSource$PoolGuardConnectionWrapper|DelegatingConnection|.cpdsadapter\\.PoolablePreparedStatementStub|DelegatingCallableStatement|DelegatingPreparedStatement|DelegatingStatement|PoolableCallableStatement|PoolablePreparedStatement))|java\\.sql\\.(CallableStatement|Connection|PreparedStatement|Statement)|net\\.sourceforge\\.jtds\\.jdbc\\.Jtds(CallableStatement|PreparedStatement|Statement)|oracle\\.jdbc\\.(OracleConnectionWrapper|driver\\.(OracleCallableStatement|OraclePreparedStatementWrapper|OraclePreparedStatement|OracleStatement|PhysicalConnection))|org\\.compiere\\.util\\.C(PreparedStatement|Statement)|org\\.jboss\\.resource\\.adapter\\.jdbc\\.(CachedCallableStatement|CachedPreparedStatement|jdk6\\.CachedCallableStatementJDK6|jdk6\\.CachedPreparedStatementJDK6|jdk6\\.WrappedCallableStatementJDK6|jdk6\\.WrappedConnectionJDK6|jdk6\\.WrappedPreparedStatementJDK6|jdk6\\.WrappedStatementJDK6|local\\.LocalCallableStatement|local\\.LocalConnection|local\\.LocalPreparedStatement|local\\.LocalStatement|WrappedCallableStatement|WrappedConnection|WrappedPreparedStatement|WrappedStatement)|org\\.postgresql\\.jdbc(2\\.AbstractJdbc2|3\\.AbstractJdbc3)Statement|weblogic\\.jdbc\\.wrapper\\.(CallableStatement|Connection|XAConnection|JTAConnection|JTSConnection|PreparedStatement|Statement)|com\\.sybase\\.jdbc(x|3\\.jdbc|4\\.jdbc)\\.SybConnection"
				, "---peer (#) class:com\\.(ibm\\.ws\\.rsadapter\\.jdbc\\.WSJdbc(Connection|PreparedStatement|Statement)|mchange\\.v2\\.c3p0\\.(C3P0ProxyConnection|impl\\.C3P0PooledConnection\\$ProxyConnection|impl\\.C3P0PooledConnection.*Proxy.*Statement|impl\\.NewProxyCallableStatement|impl\\.NewProxyConnection|impl\\.NewProxyConnection|impl\\.NewProxyPreparedStatement|impl\\.NewProxyStatement)|microsoft\\.sqlserver\\.jdbc\\.SQLServer(CallableStatement|Connection|PreparedStatement|Statement)|mysql\\.jdbc\\.(CallableStatement|ConnectionImpl|PreparedStatement|StatementImpl))|org\\.apache\\.(derby(\\.iapi\\.jdbc\\.(BrokeredCallableStatement.*|BrokeredPreparedStatement.*)|\\.impl\\.jdbc\\.Embed(CallableStatement|Connection|PreparedStatement|Statement)).*|.*\\.dbcp\\.(cpdsadapter\\.ConnectionImpl|cpdsadapter\\.PooledConnectionImpl|managed\\.ManagedConnection|PoolableConnection|PoolingConnection|PoolingDataSource$PoolGuardConnectionWrapper|DelegatingConnection|.cpdsadapter\\.PoolablePreparedStatementStub|DelegatingCallableStatement|DelegatingPreparedStatement|DelegatingStatement|PoolableCallableStatement|PoolablePreparedStatement))|java\\.sql\\.(CallableStatement|Connection|PreparedStatement|Statement)|net\\.sourceforge\\.jtds\\.jdbc\\.Jtds(CallableStatement|PreparedStatement|Statement)|oracle\\.jdbc\\.(OracleConnectionWrapper|driver\\.(OracleCallableStatement|OraclePreparedStatementWrapper|OraclePreparedStatement|OracleStatement|PhysicalConnection))|org\\.compiere\\.util\\.C(PreparedStatement|Statement)|org\\.jboss\\.resource\\.adapter\\.jdbc\\.(CachedCallableStatement|CachedPreparedStatement|jdk6\\.CachedCallableStatementJDK6|jdk6\\.CachedPreparedStatementJDK6|jdk6\\.WrappedCallableStatementJDK6|jdk6\\.WrappedConnectionJDK6|jdk6\\.WrappedPreparedStatementJDK6|jdk6\\.WrappedStatementJDK6|local\\.LocalCallableStatement|local\\.LocalConnection|local\\.LocalPreparedStatement|local\\.LocalStatement|WrappedCallableStatement|WrappedConnection|WrappedPreparedStatement|WrappedStatement)|org\\.postgresql\\.jdbc(2\\.AbstractJdbc2|3\\.AbstractJdbc3)Statement|weblogic\\.jdbc\\.wrapper\\.(CallableStatement|Connection|XAConnection|JTAConnection|JTSConnection|PreparedStatement|Statement)|com\\.sybase\\.jdbc(x|3\\.jdbc|4\\.jdbc)\\.SybConnection"
				, "element (#) class:.ool.PoolUtils$CheckedKeyedObjectPool"
				, "---peer (#) class:.pool.PoolUtils$CheckedKeyedObjectPool"
				, "element (#) class:.ol.PoolUtils$CheckedObjectPool"
				, "---peer (#) class:.pool.PoolUtils$CheckedObjectPool"
				, "element (#) class:.l.PoolUtils$ErodingKeyedObjectPool"
				, "---peer (#) class:.pool.PoolUtils$ErodingKeyedObjectPool"
				, "element (#) class:.PoolUtils$ErodingObjectPool"
				, "---peer (#) class:.pool.PoolUtils$ErodingObjectPool"
				, "element (#) class:.impl.GenericKeyedObjectPool"
				, "---peer (#) class:.pool.impl.GenericKeyedObjectPool"
				, "element (+) class:.pool2.impl.BaseGenericObjectPool"
				, "---peer <none>"
				, "element (+) class:.pool2.impl.GenericObjectPool"
				, "---peer <none>"
				, "element (#) class:.ObjectPool"
				, "---peer (#) class:.pool.ObjectPool"
				, "element (#) class:.PoolUtils$ObjectPoolAdaptor"
				, "---peer (#) class:.pool.PoolUtils$ObjectPoolAdaptor"
				, "element (#) class:.impl.SoftReferenceKeyedObjectPool"
				, "---peer (#) class:.pool.impl.SoftReferenceKeyedObjectPool"
				, "element (#) class:.impl.SoftReferenceObjectPool"
				, "---peer (#) class:.pool.impl.SoftReferenceObjectPool"
				, "element (#) class:.impl.StackKeyedObjectPool"
				, "---peer (#) class:.pool.impl.StackKeyedObjectPool"
				, "element (#) class:.impl.StackObjectPool"
				, "---peer (#) class:.pool.impl.StackObjectPool"
				, "element (#) class:.PoolUtils$SynchronizedKeyedObjectPool"
				, "---peer (#) class:.pool.PoolUtils$SynchronizedKeyedObjectPool"
				, "element (#) class:.PoolUtils$SynchronizedObjectPool"
				, "---peer (#) class:.pool.PoolUtils$SynchronizedObjectPool"
				, "element (#) class:.PoolableConnection"
				, "---peer (#) class:.dbcp.PoolableConnection"
				, "element (#) class:.AbandonedObjectPool"
				, "---peer (#) class:.dbcp.AbandonedObjectPool"
				, "element (#) class:.PoolingDriver"
				, "---peer (#) class:.dbcp.PoolingDriver"
				, "element (+) method:registerPool"
				, "---peer <none>"
				, "element (+) method:registerPool"
				, "---peer <none>"
				, "element (#) class:.PoolingDataSource"
				, "---peer (#) class:.dbcp.PoolingDataSource"
				, "element (#) class:.PoolableConnectionFactory"
				, "---peer (#) class:.dbcp.PoolableConnectionFactory"
				, "element (+) method:setPool"
				, "---peer <none>"
				, "element (#) class:.datasources.PerUserPoolDataSource"
				, "---peer (#) class:.dbcp.datasources.PerUserPoolDataSource"
				, "element (#) class:.cpdsadapter.PooledConnectionImpl"
				, "---peer (#) class:.dbcp.cpdsadapter.PooledConnectionImpl"
				), collected);
		
		collected = checkCollectChangedElements(Arrays.asList(
				  "(#) sensor"
				, "(#) class:com\\.(ibm\\.ws\\.rsadapter\\.jdbc\\.WSJdbcDataSource|mchange\\.v2\\.c3p0.*(PoolBacked|DriverManager)DataSource.*)|java\\.sql\\.Driver(Manager)?|javax\\.sql\\.(ConnectionPoolDataSource|DataSource|PooledConnection|XAConnection|XADataSource)|oracle\\.jdbc\\.(pool\\.OracleConnectionPoolDataSource|pool\\.OracleDataSource|xa\\.client\\.OracleXADataSource|xa\\.OracleXADataSource)|org\\.(apache\\.tomcat\\.jdbc\\.pool\\.DataSourceProxy|jboss\\.resource\\.adapter\\.jdbc\\.(local\\.LocalDataSource|WrapperDataSource))|weblogic\\.jdbc\\.(common\\.internal\\.RmiDataSource|jta\\.DataSource|jts\\.Driver|pool\\.Driver)|org\\.apache\\..*\\.dbcp\\.(BasicDataSource|datasources\\.InstanceKeyDataSource|datasources\\.InstanceKeyDataSource|datasources\\.SharedPoolDataSource|managed\\.ManagedDataSource|PoolingDataSource|PoolingDriver)|com\\.sun\\.gjc\\.spi\\.jdbc.?.?\\.DataSource.?.?|com\\\\.sun\\\\.gjc\\\\.spi\\\\.base\\\\.(Abstract)?DataSource"
				, "(#) argument:java.lang.String"
				, "(#) argument:java.lang.String"
				, "(#) class:com\\.(ibm\\.ws\\.rsadapter\\.jdbc\\.WSJdbc(Connection|PreparedStatement|Statement)|mchange\\.v2\\.c3p0\\.(C3P0ProxyConnection|impl\\.C3P0PooledConnection\\$ProxyConnection|impl\\.C3P0PooledConnection.*Proxy.*Statement|impl\\.NewProxyCallableStatement|impl\\.NewProxyConnection|impl\\.NewProxyConnection|impl\\.NewProxyPreparedStatement|impl\\.NewProxyStatement)|microsoft\\.sqlserver\\.jdbc\\.SQLServer(CallableStatement|Connection|PreparedStatement|Statement)|mysql\\.jdbc\\.(CallableStatement|ConnectionImpl|PreparedStatement|StatementImpl))|org\\.apache\\.(derby(\\.iapi\\.jdbc\\.(BrokeredCallableStatement.*|BrokeredPreparedStatement.*)|\\.impl\\.jdbc\\.Embed(CallableStatement|Connection|PreparedStatement|Statement)).*|.*\\.dbcp\\.(cpdsadapter\\.ConnectionImpl|cpdsadapter\\.PooledConnectionImpl|managed\\.ManagedConnection|PoolableConnection|PoolingConnection|PoolingDataSource$PoolGuardConnectionWrapper|DelegatingConnection|.cpdsadapter\\.PoolablePreparedStatementStub|DelegatingCallableStatement|DelegatingPreparedStatement|DelegatingStatement|PoolableCallableStatement|PoolablePreparedStatement))|java\\.sql\\.(CallableStatement|Connection|PreparedStatement|Statement)|net\\.sourceforge\\.jtds\\.jdbc\\.Jtds(CallableStatement|PreparedStatement|Statement)|oracle\\.jdbc\\.(OracleConnectionWrapper|driver\\.(OracleCallableStatement|OraclePreparedStatementWrapper|OraclePreparedStatement|OracleStatement|PhysicalConnection))|org\\.compiere\\.util\\.C(PreparedStatement|Statement)|org\\.jboss\\.resource\\.adapter\\.jdbc\\.(CachedCallableStatement|CachedPreparedStatement|jdk6\\.CachedCallableStatementJDK6|jdk6\\.CachedPreparedStatementJDK6|jdk6\\.WrappedCallableStatementJDK6|jdk6\\.WrappedConnectionJDK6|jdk6\\.WrappedPreparedStatementJDK6|jdk6\\.WrappedStatementJDK6|local\\.LocalCallableStatement|local\\.LocalConnection|local\\.LocalPreparedStatement|local\\.LocalStatement|WrappedCallableStatement|WrappedConnection|WrappedPreparedStatement|WrappedStatement)|org\\.postgresql\\.jdbc(2\\.AbstractJdbc2|3\\.AbstractJdbc3)Statement|weblogic\\.jdbc\\.wrapper\\.(CallableStatement|Connection|XAConnection|JTAConnection|JTSConnection|PreparedStatement|Statement)|com\\.sybase\\.jdbc(x|3\\.jdbc|4\\.jdbc)\\.SybConnection"
				, "(#) class:.pool.PoolUtils$CheckedKeyedObjectPool"
				, "(#) class:.pool.PoolUtils$CheckedObjectPool"
				, "(#) class:.pool.PoolUtils$ErodingKeyedObjectPool"
				, "(#) class:.pool.PoolUtils$ErodingObjectPool"
				, "(#) class:.pool.impl.GenericKeyedObjectPool"
				, "(#) class:.pool.ObjectPool"
				, "(#) class:.pool.PoolUtils$ObjectPoolAdaptor"
				, "(#) class:.pool.impl.SoftReferenceKeyedObjectPool"
				, "(#) class:.pool.impl.SoftReferenceObjectPool"
				, "(#) class:.pool.impl.StackKeyedObjectPool"
				, "(#) class:.pool.impl.StackObjectPool"
				, "(#) class:.pool.PoolUtils$SynchronizedKeyedObjectPool"
				, "(#) class:.pool.PoolUtils$SynchronizedObjectPool"
				, "(#) class:.dbcp.PoolableConnection"
				, "(#) class:.dbcp.AbandonedObjectPool"
				, "(#) class:.dbcp.PoolingDriver"
				, "(-) argument:deletedArgument1"
				, "(-) argument:deletedArgument2"
				, "(#) class:.dbcp.PoolingDataSource"
				, "(#) class:.dbcp.PoolableConnectionFactory"
				, "(#) class:.dbcp.datasources.PerUserPoolDataSource"
				, "(#) class:.dbcp.cpdsadapter.PooledConnectionImpl"
				), 2 + 25, item, xmlLeft);
		
		checkPeers(Arrays.asList(
				  "element (#) sensor"
				, "---peer (#) sensor"
				, "element (#) class:com\\.(ibm\\.ws\\.rsadapter\\.jdbc\\.WSJdbcDataSource|mchange\\.v2\\.c3p0.*(PoolBacked|DriverManager)DataSource.*)|java\\.sql\\.Driver(Manager)?|javax\\.sql\\.(ConnectionPoolDataSource|DataSource|PooledConnection|XAConnection|XADataSource)|oracle\\.jdbc\\.(pool\\.OracleConnectionPoolDataSource|pool\\.OracleDataSource|xa\\.client\\.OracleXADataSource|xa\\.OracleXADataSource)|org\\.(apache\\.tomcat\\.jdbc\\.pool\\.DataSourceProxy|jboss\\.resource\\.adapter\\.jdbc\\.(local\\.LocalDataSource|WrapperDataSource))|weblogic\\.jdbc\\.(common\\.internal\\.RmiDataSource|jta\\.DataSource|jts\\.Driver|pool\\.Driver)|org\\.apache\\..*\\.dbcp\\.(BasicDataSource|datasources\\.InstanceKeyDataSource|datasources\\.InstanceKeyDataSource|datasources\\.SharedPoolDataSource|managed\\.ManagedDataSource|PoolingDataSource|PoolingDriver)|com\\.sun\\.gjc\\.spi\\.jdbc.?.?\\.DataSource.?.?|com\\\\.sun\\\\.gjc\\\\.spi\\\\.base\\\\.(Abstract)?DataSource"
				, "---peer (#) class:com\\.(ibm\\.ws\\.rsadapter\\.jdbc\\.WSJdbcDataSource|mchange\\.v2\\.c3p0.*(PoolBacked|DriverManager)DataSource.*)|java\\.sql\\.Driver(Manager)?|javax\\.sql\\.(ConnectionPoolDataSource|DataSource|PooledConnection|XAConnection|XADataSource)|oracle\\.jdbc\\.(pool\\.OracleConnectionPoolDataSource|pool\\.OracleDataSource|xa\\.client\\.OracleXADataSource|xa\\.OracleXADataSource)|org\\.(apache\\.tomcat\\.jdbc\\.pool\\.DataSourceProxy|jboss\\.resource\\.adapter\\.jdbc\\.(local\\.LocalDataSource|WrapperDataSource))|weblogic\\.jdbc\\.(common\\.internal\\.RmiDataSource|jta\\.DataSource|jts\\.Driver|pool\\.Driver)|org\\.apache\\..*\\.dbcp.?\\.(BasicDataSource|datasources\\.InstanceKeyDataSource|datasources\\.InstanceKeyDataSource|datasources\\.SharedPoolDataSource|managed\\.ManagedDataSource|PoolingDataSource|PoolingDriver)|com\\.sun\\.gjc\\.spi\\.jdbc.?.?\\.DataSource.?.?|com\\.sun\\.gjc\\.spi\\.base\\.(Abstract)?DataSource"
				, "element (#) argument:java.lang.String"
				, "---peer (#) argument:javax.lang.String"
				, "element (#) argument:java.lang.String"
				, "---peer (#) argument:javax.lang.String"
				, "element (#) class:com\\.(ibm\\.ws\\.rsadapter\\.jdbc\\.WSJdbc(Connection|PreparedStatement|Statement)|mchange\\.v2\\.c3p0\\.(C3P0ProxyConnection|impl\\.C3P0PooledConnection\\$ProxyConnection|impl\\.C3P0PooledConnection.*Proxy.*Statement|impl\\.NewProxyCallableStatement|impl\\.NewProxyConnection|impl\\.NewProxyConnection|impl\\.NewProxyPreparedStatement|impl\\.NewProxyStatement)|microsoft\\.sqlserver\\.jdbc\\.SQLServer(CallableStatement|Connection|PreparedStatement|Statement)|mysql\\.jdbc\\.(CallableStatement|ConnectionImpl|PreparedStatement|StatementImpl))|org\\.apache\\.(derby(\\.iapi\\.jdbc\\.(BrokeredCallableStatement.*|BrokeredPreparedStatement.*)|\\.impl\\.jdbc\\.Embed(CallableStatement|Connection|PreparedStatement|Statement)).*|.*\\.dbcp\\.(cpdsadapter\\.ConnectionImpl|cpdsadapter\\.PooledConnectionImpl|managed\\.ManagedConnection|PoolableConnection|PoolingConnection|PoolingDataSource$PoolGuardConnectionWrapper|DelegatingConnection|.cpdsadapter\\.PoolablePreparedStatementStub|DelegatingCallableStatement|DelegatingPreparedStatement|DelegatingStatement|PoolableCallableStatement|PoolablePreparedStatement))|java\\.sql\\.(CallableStatement|Connection|PreparedStatement|Statement)|net\\.sourceforge\\.jtds\\.jdbc\\.Jtds(CallableStatement|PreparedStatement|Statement)|oracle\\.jdbc\\.(OracleConnectionWrapper|driver\\.(OracleCallableStatement|OraclePreparedStatementWrapper|OraclePreparedStatement|OracleStatement|PhysicalConnection))|org\\.compiere\\.util\\.C(PreparedStatement|Statement)|org\\.jboss\\.resource\\.adapter\\.jdbc\\.(CachedCallableStatement|CachedPreparedStatement|jdk6\\.CachedCallableStatementJDK6|jdk6\\.CachedPreparedStatementJDK6|jdk6\\.WrappedCallableStatementJDK6|jdk6\\.WrappedConnectionJDK6|jdk6\\.WrappedPreparedStatementJDK6|jdk6\\.WrappedStatementJDK6|local\\.LocalCallableStatement|local\\.LocalConnection|local\\.LocalPreparedStatement|local\\.LocalStatement|WrappedCallableStatement|WrappedConnection|WrappedPreparedStatement|WrappedStatement)|org\\.postgresql\\.jdbc(2\\.AbstractJdbc2|3\\.AbstractJdbc3)Statement|weblogic\\.jdbc\\.wrapper\\.(CallableStatement|Connection|XAConnection|JTAConnection|JTSConnection|PreparedStatement|Statement)|com\\.sybase\\.jdbc(x|3\\.jdbc|4\\.jdbc)\\.SybConnection"
				, "---peer (#) class:com\\.(ibm\\.ws\\.rsadapter\\.jdbc\\.WSJdbc(Connection|PreparedStatement|Statement)|mchange\\.v2\\.c3p0\\.(C3P0ProxyConnection|impl\\.C3P0PooledConnection\\$ProxyConnection|impl\\.C3P0PooledConnection.*Proxy.*Statement|impl\\.NewProxyCallableStatement|impl\\.NewProxyConnection|impl\\.NewProxyConnection|impl\\.NewProxyPreparedStatement|impl\\.NewProxyStatement)|microsoft\\.sqlserver\\.jdbc\\.SQLServer(CallableStatement|Connection|PreparedStatement|Statement)|mysql\\.jdbc\\.(CallableStatement|ConnectionImpl|PreparedStatement|StatementImpl))|org\\.apache\\.(derby(\\.iapi\\.jdbc\\.(BrokeredCallableStatement.*|BrokeredPreparedStatement.*)|\\.impl\\.jdbc\\.Embed(CallableStatement|Connection|PreparedStatement|Statement)).*|.*\\.dbcp.?\\.(cpdsadapter\\.ConnectionImpl|cpdsadapter\\.PooledConnectionImpl|managed\\.ManagedConnection|PoolableConnection|PoolingConnection|PoolingDataSource$PoolGuardConnectionWrapper|DelegatingConnection|.cpdsadapter\\.PoolablePreparedStatementStub|DelegatingCallableStatement|DelegatingPreparedStatement|DelegatingStatement|PoolableCallableStatement|PoolablePreparedStatement))|java\\.sql\\.(CallableStatement|Connection|PreparedStatement|Statement)|net\\.sourceforge\\.jtds\\.jdbc\\.Jtds(CallableStatement|PreparedStatement|Statement)|oracle\\.jdbc\\.(OracleConnectionWrapper|driver\\.(OracleCallableStatement|OraclePreparedStatementWrapper|OraclePreparedStatement|OracleStatement|PhysicalConnection))|org\\.compiere\\.util\\.C(PreparedStatement|Statement)|org\\.jboss\\.resource\\.adapter\\.jdbc\\.(CachedCallableStatement|CachedPreparedStatement|jdk6\\.CachedCallableStatementJDK6|jdk6\\.CachedPreparedStatementJDK6|jdk6\\.WrappedCallableStatementJDK6|jdk6\\.WrappedConnectionJDK6|jdk6\\.WrappedPreparedStatementJDK6|jdk6\\.WrappedStatementJDK6|local\\.LocalCallableStatement|local\\.LocalConnection|local\\.LocalPreparedStatement|local\\.LocalStatement|WrappedCallableStatement|WrappedConnection|WrappedPreparedStatement|WrappedStatement)|org\\.postgresql\\.jdbc(2\\.AbstractJdbc2|3\\.AbstractJdbc3)Statement|weblogic\\.jdbc\\.wrapper\\.(CallableStatement|Connection|XAConnection|JTAConnection|JTSConnection|PreparedStatement|Statement)|com\\.sybase\\.jdbc(x|3\\.jdbc|4\\.jdbc)\\.SybConnection"
				, "element (#) class:.pool.PoolUtils$CheckedKeyedObjectPool"
				, "---peer (#) class:.ool.PoolUtils$CheckedKeyedObjectPool"
				, "element (#) class:.pool.PoolUtils$CheckedObjectPool"
				, "---peer (#) class:.ol.PoolUtils$CheckedObjectPool"
				, "element (#) class:.pool.PoolUtils$ErodingKeyedObjectPool"
				, "---peer (#) class:.l.PoolUtils$ErodingKeyedObjectPool"
				, "element (#) class:.pool.PoolUtils$ErodingObjectPool"
				, "---peer (#) class:.PoolUtils$ErodingObjectPool"
				, "element (#) class:.pool.impl.GenericKeyedObjectPool"
				, "---peer (#) class:.impl.GenericKeyedObjectPool"
				, "element (#) class:.pool.ObjectPool"
				, "---peer (#) class:.ObjectPool"
				, "element (#) class:.pool.PoolUtils$ObjectPoolAdaptor"
				, "---peer (#) class:.PoolUtils$ObjectPoolAdaptor"
				, "element (#) class:.pool.impl.SoftReferenceKeyedObjectPool"
				, "---peer (#) class:.impl.SoftReferenceKeyedObjectPool"
				, "element (#) class:.pool.impl.SoftReferenceObjectPool"
				, "---peer (#) class:.impl.SoftReferenceObjectPool"
				, "element (#) class:.pool.impl.StackKeyedObjectPool"
				, "---peer (#) class:.impl.StackKeyedObjectPool"
				, "element (#) class:.pool.impl.StackObjectPool"
				, "---peer (#) class:.impl.StackObjectPool"
				, "element (#) class:.pool.PoolUtils$SynchronizedKeyedObjectPool"
				, "---peer (#) class:.PoolUtils$SynchronizedKeyedObjectPool"
				, "element (#) class:.pool.PoolUtils$SynchronizedObjectPool"
				, "---peer (#) class:.PoolUtils$SynchronizedObjectPool"
				, "element (#) class:.dbcp.PoolableConnection"
				, "---peer (#) class:.PoolableConnection"
				, "element (#) class:.dbcp.AbandonedObjectPool"
				, "---peer (#) class:.AbandonedObjectPool"
				, "element (#) class:.dbcp.PoolingDriver"
				, "---peer (#) class:.PoolingDriver"
				, "element (-) argument:deletedArgument1"
				, "---peer <none>"
				, "element (-) argument:deletedArgument2"
				, "---peer <none>"
				, "element (#) class:.dbcp.PoolingDataSource"
				, "---peer (#) class:.PoolingDataSource"
				, "element (#) class:.dbcp.PoolableConnectionFactory"
				, "---peer (#) class:.PoolableConnectionFactory"
				, "element (#) class:.dbcp.datasources.PerUserPoolDataSource"
				, "---peer (#) class:.datasources.PerUserPoolDataSource"
				, "element (#) class:.dbcp.cpdsadapter.PooledConnectionImpl"
				, "---peer (#) class:.cpdsadapter.PooledConnectionImpl"
				), collected);
	}
	
	@Test
	public void jdbcSensorLevenshteinSwapped() throws XMLStreamException, IOException {
		String[] inputLeft = IO.readLines(new FileReader("samples/jdbc-sensor-rev-425369.xml"));
		String[] inputRight = IO.readLines(new FileReader("samples/jdbc-sensor-rev-390963.xml"));
		
		XmlStruct xmlLeft = lexer.parse(IO.asString(inputLeft));
		XmlStruct xmlRight = lexer.parse(IO.asString(inputRight));
		
		List<String> ignoreAttributeNames = Collections.emptyList();
		XmlDiffer differ = newStringMetricXmlDiffer(ignoreAttributeNames, levenshteinMetricResolver, stringMetricDifferThreshold );
		XmlDiffer.Result diffResult = differ.diff(xmlLeft, xmlRight);
		Assert.assertEquals(2, diffResult.nAdded);
		Assert.assertEquals(5, diffResult.nRemoved);
		Assert.assertEquals(25, diffResult.nAttributeChanged);
		
		checkModel(Arrays.asList(
				  "plugin"
				, "  extension"
				, "    sensorpack"
				, "      (#) sensor"
				, "        (#) class:com\\.(ibm\\.ws\\.rsadapter\\.jdbc\\.WSJdbcDataSource|mchange\\.v2\\.c3p0.*(PoolBacked|DriverManager)DataSource.*)|java\\.sql\\.Driver(Manager)?|javax\\.sql\\.(ConnectionPoolDataSource|DataSource|PooledConnection|XAConnection|XADataSource)|oracle\\.jdbc\\.(pool\\.OracleConnectionPoolDataSource|pool\\.OracleDataSource|xa\\.client\\.OracleXADataSource|xa\\.OracleXADataSource)|org\\.(apache\\.tomcat\\.jdbc\\.pool\\.DataSourceProxy|jboss\\.resource\\.adapter\\.jdbc\\.(local\\.LocalDataSource|WrapperDataSource))|weblogic\\.jdbc\\.(common\\.internal\\.RmiDataSource|jta\\.DataSource|jts\\.Driver|pool\\.Driver)|org\\.apache\\..*\\.dbcp\\.(BasicDataSource|datasources\\.InstanceKeyDataSource|datasources\\.InstanceKeyDataSource|datasources\\.SharedPoolDataSource|managed\\.ManagedDataSource|PoolingDataSource|PoolingDriver)|com\\.sun\\.gjc\\.spi\\.jdbc.?.?\\.DataSource.?.?|com\\\\.sun\\\\.gjc\\\\.spi\\\\.base\\\\.(Abstract)?DataSource"
				, "          method:getConnection"
				, "            (#) argument:java.lang.String"
				, "          method:getConnection"
				, "            (#) argument:java.lang.String"
				, "        (#) class:com\\.(ibm\\.ws\\.rsadapter\\.jdbc\\.WSJdbc(Connection|PreparedStatement|Statement)|mchange\\.v2\\.c3p0\\.(C3P0ProxyConnection|impl\\.C3P0PooledConnection\\$ProxyConnection|impl\\.C3P0PooledConnection.*Proxy.*Statement|impl\\.NewProxyCallableStatement|impl\\.NewProxyConnection|impl\\.NewProxyConnection|impl\\.NewProxyPreparedStatement|impl\\.NewProxyStatement)|microsoft\\.sqlserver\\.jdbc\\.SQLServer(CallableStatement|Connection|PreparedStatement|Statement)|mysql\\.jdbc\\.(CallableStatement|ConnectionImpl|PreparedStatement|StatementImpl))|org\\.apache\\.(derby(\\.iapi\\.jdbc\\.(BrokeredCallableStatement.*|BrokeredPreparedStatement.*)|\\.impl\\.jdbc\\.Embed(CallableStatement|Connection|PreparedStatement|Statement)).*|.*\\.dbcp\\.(cpdsadapter\\.ConnectionImpl|cpdsadapter\\.PooledConnectionImpl|managed\\.ManagedConnection|PoolableConnection|PoolingConnection|PoolingDataSource$PoolGuardConnectionWrapper|DelegatingConnection|.cpdsadapter\\.PoolablePreparedStatementStub|DelegatingCallableStatement|DelegatingPreparedStatement|DelegatingStatement|PoolableCallableStatement|PoolablePreparedStatement))|java\\.sql\\.(CallableStatement|Connection|PreparedStatement|Statement)|net\\.sourceforge\\.jtds\\.jdbc\\.Jtds(CallableStatement|PreparedStatement|Statement)|oracle\\.jdbc\\.(OracleConnectionWrapper|driver\\.(OracleCallableStatement|OraclePreparedStatementWrapper|OraclePreparedStatement|OracleStatement|PhysicalConnection))|org\\.compiere\\.util\\.C(PreparedStatement|Statement)|org\\.jboss\\.resource\\.adapter\\.jdbc\\.(CachedCallableStatement|CachedPreparedStatement|jdk6\\.CachedCallableStatementJDK6|jdk6\\.CachedPreparedStatementJDK6|jdk6\\.WrappedCallableStatementJDK6|jdk6\\.WrappedConnectionJDK6|jdk6\\.WrappedPreparedStatementJDK6|jdk6\\.WrappedStatementJDK6|local\\.LocalCallableStatement|local\\.LocalConnection|local\\.LocalPreparedStatement|local\\.LocalStatement|WrappedCallableStatement|WrappedConnection|WrappedPreparedStatement|WrappedStatement)|org\\.postgresql\\.jdbc(2\\.AbstractJdbc2|3\\.AbstractJdbc3)Statement|weblogic\\.jdbc\\.wrapper\\.(CallableStatement|Connection|XAConnection|JTAConnection|JTSConnection|PreparedStatement|Statement)|com\\.sybase\\.jdbc(x|3\\.jdbc|4\\.jdbc)\\.SybConnection"
				, "        (#) class:.pool.PoolUtils$CheckedKeyedObjectPool"
				, "        (#) class:.pool.PoolUtils$CheckedObjectPool"
				, "        (#) class:.pool.PoolUtils$ErodingKeyedObjectPool"
				, "        (#) class:.pool.PoolUtils$ErodingObjectPool"
				, "        (#) class:.pool.impl.GenericKeyedObjectPool"
				, "        (#) class:.pool.ObjectPool"
				, "        (#) class:.pool.PoolUtils$ObjectPoolAdaptor"
				, "        (#) class:.pool.impl.SoftReferenceKeyedObjectPool"
				, "        (#) class:.pool.impl.SoftReferenceObjectPool"
				, "        (#) class:.pool.impl.StackKeyedObjectPool"
				, "        (#) class:.pool.impl.StackObjectPool"
				, "        (#) class:.pool.PoolUtils$SynchronizedKeyedObjectPool"
				, "        (#) class:.pool.PoolUtils$SynchronizedObjectPool"
				, "        (#) class:.dbcp.PoolableConnection"
				, "        (#) class:.dbcp.AbandonedObjectPool"
				, "        (#) class:.dbcp.PoolingDriver"
				, "          method:registerPool"
				, "            (+) argument:deletedArgument1"
				, "            (+) argument:deletedArgument2"
				, "        (#) class:.dbcp.PoolingDataSource"
				, "        (#) class:.dbcp.PoolableConnectionFactory"
				, "        (#) class:.dbcp.datasources.PerUserPoolDataSource"
				, "        (#) class:.dbcp.cpdsadapter.PooledConnectionImpl"
				), item, xmlRight);
		
		checkModel(Arrays.asList(
				  "plugin"
				, "  extension"
				, "    sensorpack"
				, "      (#) sensor"
				, "        (#) class:com\\.(ibm\\.ws\\.rsadapter\\.jdbc\\.WSJdbcDataSource|mchange\\.v2\\.c3p0.*(PoolBacked|DriverManager)DataSource.*)|java\\.sql\\.Driver(Manager)?|javax\\.sql\\.(ConnectionPoolDataSource|DataSource|PooledConnection|XAConnection|XADataSource)|oracle\\.jdbc\\.(pool\\.OracleConnectionPoolDataSource|pool\\.OracleDataSource|xa\\.client\\.OracleXADataSource|xa\\.OracleXADataSource)|org\\.(apache\\.tomcat\\.jdbc\\.pool\\.DataSourceProxy|jboss\\.resource\\.adapter\\.jdbc\\.(local\\.LocalDataSource|WrapperDataSource))|weblogic\\.jdbc\\.(common\\.internal\\.RmiDataSource|jta\\.DataSource|jts\\.Driver|pool\\.Driver)|org\\.apache\\..*\\.dbcp.?\\.(BasicDataSource|datasources\\.InstanceKeyDataSource|datasources\\.InstanceKeyDataSource|datasources\\.SharedPoolDataSource|managed\\.ManagedDataSource|PoolingDataSource|PoolingDriver)|com\\.sun\\.gjc\\.spi\\.jdbc.?.?\\.DataSource.?.?|com\\.sun\\.gjc\\.spi\\.base\\.(Abstract)?DataSource"
				, "          method:getConnection"
				, "            (#) argument:javax.lang.String"
				, "          method:getConnection"
				, "            (#) argument:javax.lang.String"
				, "        (#) class:com\\.(ibm\\.ws\\.rsadapter\\.jdbc\\.WSJdbc(Connection|PreparedStatement|Statement)|mchange\\.v2\\.c3p0\\.(C3P0ProxyConnection|impl\\.C3P0PooledConnection\\$ProxyConnection|impl\\.C3P0PooledConnection.*Proxy.*Statement|impl\\.NewProxyCallableStatement|impl\\.NewProxyConnection|impl\\.NewProxyConnection|impl\\.NewProxyPreparedStatement|impl\\.NewProxyStatement)|microsoft\\.sqlserver\\.jdbc\\.SQLServer(CallableStatement|Connection|PreparedStatement|Statement)|mysql\\.jdbc\\.(CallableStatement|ConnectionImpl|PreparedStatement|StatementImpl))|org\\.apache\\.(derby(\\.iapi\\.jdbc\\.(BrokeredCallableStatement.*|BrokeredPreparedStatement.*)|\\.impl\\.jdbc\\.Embed(CallableStatement|Connection|PreparedStatement|Statement)).*|.*\\.dbcp.?\\.(cpdsadapter\\.ConnectionImpl|cpdsadapter\\.PooledConnectionImpl|managed\\.ManagedConnection|PoolableConnection|PoolingConnection|PoolingDataSource$PoolGuardConnectionWrapper|DelegatingConnection|.cpdsadapter\\.PoolablePreparedStatementStub|DelegatingCallableStatement|DelegatingPreparedStatement|DelegatingStatement|PoolableCallableStatement|PoolablePreparedStatement))|java\\.sql\\.(CallableStatement|Connection|PreparedStatement|Statement)|net\\.sourceforge\\.jtds\\.jdbc\\.Jtds(CallableStatement|PreparedStatement|Statement)|oracle\\.jdbc\\.(OracleConnectionWrapper|driver\\.(OracleCallableStatement|OraclePreparedStatementWrapper|OraclePreparedStatement|OracleStatement|PhysicalConnection))|org\\.compiere\\.util\\.C(PreparedStatement|Statement)|org\\.jboss\\.resource\\.adapter\\.jdbc\\.(CachedCallableStatement|CachedPreparedStatement|jdk6\\.CachedCallableStatementJDK6|jdk6\\.CachedPreparedStatementJDK6|jdk6\\.WrappedCallableStatementJDK6|jdk6\\.WrappedConnectionJDK6|jdk6\\.WrappedPreparedStatementJDK6|jdk6\\.WrappedStatementJDK6|local\\.LocalCallableStatement|local\\.LocalConnection|local\\.LocalPreparedStatement|local\\.LocalStatement|WrappedCallableStatement|WrappedConnection|WrappedPreparedStatement|WrappedStatement)|org\\.postgresql\\.jdbc(2\\.AbstractJdbc2|3\\.AbstractJdbc3)Statement|weblogic\\.jdbc\\.wrapper\\.(CallableStatement|Connection|XAConnection|JTAConnection|JTSConnection|PreparedStatement|Statement)|com\\.sybase\\.jdbc(x|3\\.jdbc|4\\.jdbc)\\.SybConnection"
				, "        (#) class:.ool.PoolUtils$CheckedKeyedObjectPool"
				, "        (#) class:.ol.PoolUtils$CheckedObjectPool"
				, "        (#) class:.l.PoolUtils$ErodingKeyedObjectPool"
				, "        (#) class:.PoolUtils$ErodingObjectPool"
				, "        (#) class:.impl.GenericKeyedObjectPool"
				, "        (-) class:.pool2.impl.BaseGenericObjectPool"
				, "        (-) class:.pool2.impl.GenericObjectPool"
				, "        (#) class:.ObjectPool"
				, "        (#) class:.PoolUtils$ObjectPoolAdaptor"
				, "        (#) class:.impl.SoftReferenceKeyedObjectPool"
				, "        (#) class:.impl.SoftReferenceObjectPool"
				, "        (#) class:.impl.StackKeyedObjectPool"
				, "        (#) class:.impl.StackObjectPool"
				, "        (#) class:.PoolUtils$SynchronizedKeyedObjectPool"
				, "        (#) class:.PoolUtils$SynchronizedObjectPool"
				, "        (#) class:.PoolableConnection"
				, "        (#) class:.AbandonedObjectPool"
				, "        (#) class:.PoolingDriver"
				, "          (-) method:registerPool"
				, "          (-) method:registerPool"
				, "        (#) class:.PoolingDataSource"
				, "        (#) class:.PoolableConnectionFactory"
				, "          (-) method:setPool"
				, "        (#) class:.datasources.PerUserPoolDataSource"
				, "        (#) class:.cpdsadapter.PooledConnectionImpl"
				), item, xmlLeft);
	}

	@Test
	public void regression1() throws XMLStreamException, IOException {
		String[] inputLeft = IO.readLines(new FileReader("samples/sensorrules-regression-1.left.xml"));
		String[] inputRight = IO.readLines(new FileReader("samples/sensorrules-regression-1.right.xml"));
		
		List<String> discriminatorAttributeNames = Arrays.asList("pattern");
		lexer = new XmlLexer(discriminatorAttributeNames, stringCache);

		XmlStruct xmlLeft = lexer.parse(IO.asString(inputLeft));
		XmlStruct xmlRight = lexer.parse(IO.asString(inputRight));
		
		MetricResolver levenshteinMetricResolver = (XmlElement) -> StringMetrics::getLevenshteinDistance;
		XmlDiffer differ = newStringMetricXmlDiffer(SYSTEM_PROFILE_IGNORED_ATTRIBUTES, levenshteinMetricResolver, stringMetricDifferThreshold);
		
		XmlDiffer.Result diffResult = differ.diff(xmlLeft, xmlRight);
		Assert.assertEquals(0, diffResult.nAdded);
		Assert.assertEquals(2, diffResult.nRemoved);
		Assert.assertEquals(1, diffResult.nAttributeChanged);
		
		List<XmlElement> collected;
		
		collected = checkCollectChangedElements(Arrays.asList(
				  "(#) class:myclass123"
				), 1, item, xmlRight);
		
		checkPeers(Arrays.asList(
				  "element (#) class:myclass123"
				, "---peer (#) class:myclass1"
				), collected);
		
		collected = checkCollectChangedElements(Arrays.asList(
				  "(#) class:myclass1"
				, "(-) argument:deletedArgument1"
				, "(-) argument:deletedArgument2"
				), 3, item, xmlLeft);
		
		checkPeers(Arrays.asList(
				  "element (#) class:myclass1"
				, "---peer (#) class:myclass123"
				, "element (-) argument:deletedArgument1"
				, "---peer <none>"
				, "element (-) argument:deletedArgument2"
				, "---peer <none>"
				), collected);
	}
	

}
