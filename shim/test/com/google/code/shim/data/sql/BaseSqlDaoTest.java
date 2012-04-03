package com.google.code.shim.data.sql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.code.shim.collections.StringKeyMap;
import com.google.code.shim.data.DataAccessException;
import com.google.code.shim.data.sql.handler.RowHandler;
import com.google.code.shim.data.sql.handler.RowListHandler;

public class BaseSqlDaoTest {

	static JDBCDataSource ds = new JDBCDataSource();
	static {
		try {
			ds.setUrl("jdbc:hsqldb:mem:testdb");
			Connection conn = ds.getConnection("sa", "");

			// Create
			String create =
				"create table t_elements( element_id identity, "+
					"symbol varchar(3) not null, " +
					"element_name varchar(50) not null, " +
					"atomic_number integer not null, " +
					"atomic_weight numeric(12,8) not null," +
					"category varchar(50) not null," +
					")";
			Statement createStatement = conn.createStatement();
			createStatement.execute(create);
			createStatement.close();

			// Build Data
			Object[][] periodicTable = new Object[][] {
				new Object[] { "H", "Hydrogen", 1, 1.00794, "Nonmetal" },
				new Object[] { "He", "Helium", 2, 4.00262, "Noble Gas" },
				new Object[] { "Li", "Lithium", 3, 6.941, "Alkali Metal" },
				new Object[] { "Be", "Beryllium", 4, 9.012182, "Nonmetal" },
				new Object[] { "B", "Boron", 5, 10.811, "Metalloid" },
				new Object[] { "C", "Carbon", 6, 12.0107, "Nonmetal" },
				new Object[] { "N", "Nitrogen", 7, 14.0067, "Nonmetal" },
				new Object[] { "O", "Oxygen", 8, 15.9994, "Nonmetal" },
				new Object[] { "F", "Fluorine", 9, 18.9984032, "Halogen" },
				new Object[] { "Ne", "Neon", 10, 20.1797, "Noble Gas" }
			};

			// Populate with data.
			String insertElement = "insert into t_elements (symbol, element_name, atomic_number, atomic_weight, category) values (?,?,?,?,?)";
			QueryRunner qr = new QueryRunner(ds);
			qr.batch(insertElement, periodicTable);

			DbUtils.closeQuietly(conn);

		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

	}

	private static BaseSqlDaoTestHarness dao;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		// without dialect
		dao = new BaseSqlDaoTestHarness(ds);
		assertNotNull(dao);

		// with generic dialect
		dao = new BaseSqlDaoTestHarness(ds, "generic");
		assertNotNull(dao);

		try {
			// With unspecified dialect(should fail).
			dao = new BaseSqlDaoTestHarness(ds, "");
			fail();
		} catch (DataAccessException e) {
			assertNotNull(e);
		}
		dao.setParameterMetadataSupport(true);

	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	private static void assertHydrogen(StringKeyMap result) {
		assertNotNull(result);

		// new Object[]{"H", "Hydrogen", 1, 1.00794, "Nonmetal"},
		assertEquals(true, result.getInteger("element_id")>=0);
		assertEquals("H", result.getString("symbol"));
		assertEquals("Hydrogen", result.getString("element_name"));
		assertEquals(new Integer(1), result.getInteger("atomic_number"));
		assertEquals(new Double(1.00794), result.getDouble("atomic_weight"));
		assertEquals("Nonmetal", result.getString("category"));
	}

	private static void assertNonMetalsWithAtomicWeightGreaterThan1(List<StringKeyMap> results) {
		assertEquals(4, results.size());

		Iterator<StringKeyMap> iter = results.iterator();

		// new Object[]{"Be", "Beryllium", 4, 9.012182, "Nonmetal"},
		StringKeyMap row = iter.next();
		assertEquals(true, row.getInteger("element_id")>=0);
		assertEquals("Be", row.getString("symbol"));
		assertEquals("Beryllium", row.getString("element_name"));
		assertEquals(new Integer(4), row.getInteger("atomic_number"));
		assertEquals(new Double(9.012182), row.getDouble("atomic_weight"));
		assertEquals("Nonmetal", row.getString("category"));

		// new Object[]{"C", "Carbon", 6, 12.0107, "Nonmetal"},
		row = iter.next();
		assertEquals(true, row.getInteger("element_id")>0);
		assertEquals("C", row.getString("symbol"));
		assertEquals("Carbon", row.getString("element_name"));
		assertEquals(new Integer(6), row.getInteger("atomic_number"));
		assertEquals(new Double(12.0107), row.getDouble("atomic_weight"));
		assertEquals("Nonmetal", row.getString("category"));

		// new Object[]{"N", "Nitrogen", 7, 14.0067, "Nonmetal"},
		row = iter.next();
		assertEquals(true, row.getInteger("element_id")>0);
		assertEquals("N", row.getString("symbol"));
		assertEquals("Nitrogen", row.getString("element_name"));
		assertEquals(new Integer(7), row.getInteger("atomic_number"));
		assertEquals(new Double(14.0067), row.getDouble("atomic_weight"));
		assertEquals("Nonmetal", row.getString("category"));

		// new Object[]{"O", "Oxygen", 8, 15.9994, "Nonmetal"},
		row = iter.next();
		assertEquals(true, row.getInteger("element_id")>0);
		assertEquals("O", row.getString("symbol"));
		assertEquals("Oxygen", row.getString("element_name"));
		assertEquals(new Integer(8), row.getInteger("atomic_number"));
		assertEquals(new Double(15.9994), row.getDouble("atomic_weight"));
		assertEquals("Nonmetal", row.getString("category"));
	}

	@Test
	public void testSelectValueEasily() {
		try {

			Integer result = dao.selectValueEasily();

			assertEquals(new Integer(1), result);

		} catch (DataAccessException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		try {
			dao.testSelectValueEasilyBad();

			fail();
		} catch (DataAccessException e) {
			assertNotNull(e);
			assertSame(e.getCause(), (SQLException) e.getCause());
		}

	}

	@Test
	public void testSelectSingleUsingStatement() {
		try {
			String query = "select element_id, symbol, element_name, atomic_number, atomic_weight, category " +
				"from t_elements where symbol = ?";

			StringKeyMap result = dao.selectSingleUsingStatement(new RowHandler(), query, "H");

			assertHydrogen(result);

		} catch (DataAccessException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void testSelectSingleEasily() {
		try {

			StringKeyMap result = dao.testSelectSingleEasily("H");

			assertHydrogen(result);

		} catch (DataAccessException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		try {
			dao.testSelectSingleEasilyBad("H");

			fail();
		} catch (DataAccessException e) {
			assertNotNull(e);
			assertSame(e.getCause(), (SQLException) e.getCause());
		}

	}

	@Test
	public void testSelectSingleEasilyWithHandler() {
		try {

			StringKeyMap result = dao.testSelectSingleEasilyWithHandler(new RowHandler(), "H");

			assertHydrogen(result);

		} catch (DataAccessException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		try {
			dao.testSelectSingleEasilyWithHandlerBad(new RowHandler(), "H");

			fail();

		} catch (DataAccessException e) {
			assertNotNull(e);
			assertSame(e.getCause(), (SQLException) e.getCause());
		}
	}

	@Test
	public void testSelectSingleUsingProperty() {
		try {

			StringKeyMap result = dao.selectSingleUsingProperty(new RowHandler(), "sql.testSelectSingleEasily", "H");

			assertHydrogen(result);

		} catch (DataAccessException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

	}

	@Test
	public void testSelectMultipleUsingProperty() {
		try {
			List<StringKeyMap> results = dao.selectMultipleUsingProperty(new RowListHandler(),
				"sql.testSelectMultipleEasily", "Nonmetal", 1);

			assertNonMetalsWithAtomicWeightGreaterThan1(results);

		} catch (DataAccessException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void testSelectMultipleUsingStatement() {
		try {
			String query = "select element_id, symbol, element_name, atomic_number, atomic_weight, category " +
				"from t_elements where category = ? and atomic_number > ? order by atomic_weight asc";

			List<StringKeyMap> results = dao.selectMultipleUsingStatement(new RowListHandler(), query, "Nonmetal", 1);

			assertNonMetalsWithAtomicWeightGreaterThan1(results);

		} catch (DataAccessException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

	}

	@Test
	public void testSelectMultipleEasily() {
		try {

			List<StringKeyMap> results = dao.testSelectMultipleEasily("Nonmetal", 1);

			assertNonMetalsWithAtomicWeightGreaterThan1(results);

		} catch (DataAccessException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		try {
			dao.testSelectMultipleEasilyBad("Nonmetal", 1);

			fail();

		} catch (DataAccessException e) {
			assertNotNull(e);
			assertSame(e.getCause(), (SQLException) e.getCause());
		}

	}

	@Test
	public void testSelectMultipleEasilyWithHandler() {
		try {

			List<StringKeyMap> results = dao.testSelectMultipleEasilyWithHandler(new RowListHandler(), "Nonmetal", 1);

			assertNonMetalsWithAtomicWeightGreaterThan1(results);

		} catch (DataAccessException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		try {
			dao.testSelectMultipleEasilyWithHandlerBad(new RowListHandler(), "Nonmetal", 1);

		} catch (DataAccessException e) {
			assertNotNull(e);
			assertSame(e.getCause(), (SQLException) e.getCause());
		}
	}

	@Test
	public void testInsertUpdateDeleteEasily() {
		try {
			StringKeyMap map = new StringKeyMap();
			map.put("symbol", "X");
			map.put("element_name", "Xigencium");
			map.put("atomic_number", 0);
			map.put("atomic_weight", 0.000001);
			map.put("category", "imaginary");

			dao.testInsertEasily(map);
			
			map = dao.testSelectSingleEasily("X");
			assertNotNull(map);
			assertEquals(true, map.getInteger("element_id")>0);
			assertEquals("X",map.getString("symbol"));
			assertEquals("Xigencium",map.getString("element_name"));
			assertEquals(new Integer(0),map.getInteger("atomic_number"));
			assertEquals(new Double(0.000001),map.getDouble("atomic_weight"));
			assertEquals("imaginary",map.getString("category"));
			
			//update test
			map.put("element_name", "Xylophoneum");
			dao.testUpdateEasily(map, "symbol");
			
			map = dao.testSelectSingleEasily("X");
			assertNotNull(map);
			assertEquals(true, map.getInteger("element_id")>0);
			assertEquals("X",map.getString("symbol"));
			assertEquals("Xylophoneum",map.getString("element_name"));
			assertEquals(new Integer(0),map.getInteger("atomic_number"));
			assertEquals(new Double(0.000001),map.getDouble("atomic_weight"));
			assertEquals("imaginary",map.getString("category"));
			
			//delete test
			dao.testDeleteEasily("X");
			map = dao.testSelectSingleEasily("X");
			assertSame(null,map);
			
			
		} catch (DataAccessException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testSaveEasily() {
		try {
			StringKeyMap map = new StringKeyMap();
			map.put("symbol", "X");
			map.put("element_name", "Xigencium");
			map.put("atomic_number", 0);
			map.put("atomic_weight", 0.000001);
			map.put("category", "imaginary");

			dao.testSaveEasily(map, "symbol");

			map = dao.testSelectSingleEasily("X");
			assertNotNull(map);
			
			assertEquals("X",map.getString("symbol"));
			assertEquals("Xigencium",map.getString("element_name"));
			assertEquals(new Integer(0),map.getInteger("atomic_number"));
			assertEquals(new Double(0.000001),map.getDouble("atomic_weight"));
			assertEquals("imaginary",map.getString("category"));
			
			//update test
			map.put("element_name", "Xylophoneum");
			map.put("atomic_number", 123456789);
			
			dao.testSaveEasily(map, "symbol");

			
			map = dao.testSelectSingleEasily("X");
			assertNotNull(map);
			assertEquals("X",map.getString("symbol"));
			assertEquals("Xylophoneum",map.getString("element_name"));
			assertEquals(new Integer(123456789),map.getInteger("atomic_number"));
			assertEquals(new Double(0.000001),map.getDouble("atomic_weight"));
			assertEquals("imaginary",map.getString("category"));
			
			//delete test
			dao.testDeleteEasily("X");
			map = dao.testSelectSingleEasily("X");
			assertSame(null,map);
			
			
		} catch (DataAccessException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
}
