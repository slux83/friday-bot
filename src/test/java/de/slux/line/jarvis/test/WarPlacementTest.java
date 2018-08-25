/**
 * 
 */
package de.slux.line.jarvis.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import de.slux.line.jarvis.dao.DbConnectionPool;
import de.slux.line.jarvis.dao.exception.GenericDaoException;
import de.slux.line.jarvis.dao.exception.SummonerNotFoundException;
import de.slux.line.jarvis.dao.war.WarSummonerDao;
import de.slux.line.jarvis.data.war.WarSummoner;
import de.slux.line.jarvis.data.war.WarSummonerPlacement;
import de.slux.line.jarvis.logic.war.WarReportModel;

/**
 * Testing the war stuff
 * 
 * @author Slux
 */
public class WarPlacementTest {

	private static final String GROUP1_ID = "test_group1";
	private static final String GROUP2_ID = "test_group2";
	private static final String GROUP3_ID = "test_group3";

	private WarReportModel warModel;
	private int group1Key;
	private int group2Key;
	private int group3Key;

	@Before
	public void tierUp() throws Exception {
		this.warModel = new WarReportModel();
		this.warModel.register(GROUP1_ID, "test_group_1");
		this.warModel.register(GROUP2_ID, "test_group_2");
		this.warModel.register(GROUP3_ID, "test_group_3");
		this.warModel.resetFor(GROUP1_ID);
		this.warModel.resetFor(GROUP2_ID);
		this.warModel.resetFor(GROUP3_ID);

		this.group1Key = this.warModel.getKeyOfGroup(GROUP1_ID);
		this.group2Key = this.warModel.getKeyOfGroup(GROUP2_ID);
		this.group3Key = this.warModel.getKeyOfGroup(GROUP3_ID);
		assertNotEquals(-1, this.group1Key);
		assertNotEquals(-1, this.group2Key);
		assertNotEquals(-1, this.group3Key);
	}

	@Test
	public void testAddSummoners() throws Exception {
		WarSummonerDao dao = new WarSummonerDao(DbConnectionPool.getConnection());

		Assert.assertTrue(dao.getAll(Integer.MAX_VALUE).isEmpty());
		dao = new WarSummonerDao(DbConnectionPool.getConnection());
		dao.storeData(this.group1Key, Lists.newArrayList("Foo", "Bar", "John"));
		dao = new WarSummonerDao(DbConnectionPool.getConnection());
		Map<Integer, WarSummoner> summoners = dao.getAll(this.group1Key);
		Assert.assertEquals(3, summoners.size());
		System.out.println(summoners);

		// Test some placement edit
		dao = new WarSummonerDao(DbConnectionPool.getConnection());
		dao.editPlacement(this.group1Key, 1, 'C', 55, "5* dupe Medusa");

		dao = new WarSummonerDao(DbConnectionPool.getConnection());
		summoners = dao.getAll(this.group1Key);
		assertNotNull(summoners.get(1));
		assertNotNull(summoners.get(1).getPlacements().get('C'));
		WarSummonerPlacement placement = summoners.get(1).getPlacements().get('C');
		assertEquals(Integer.valueOf(55), placement.getNode());
		assertEquals("5* dupe Medusa", placement.getChampion());

		// Test summoner not found
		try {
			dao = new WarSummonerDao(DbConnectionPool.getConnection());
			dao.editPlacement(this.group1Key, 100, 'C', 55, "5* dupe Medusa");
			Assert.assertTrue(false); // must have an exception thrown
		} catch (SummonerNotFoundException e) {
			// Good one in this case
			System.err.println(e);
		}

		// Test summoner not found
		try {
			dao = new WarSummonerDao(DbConnectionPool.getConnection());
			dao.editPlacement(this.group1Key, 2, 'Z', 55, "5* dupe Medusa");
			Assert.assertTrue(false); // must have an exception thrown
		} catch (GenericDaoException e) {
			// Good one in this case
			System.err.println(e);
		}

	}

	@Test(expected = Exception.class)
	public void testAddSummonersFailTooManySummoners() throws Exception {
		WarSummonerDao dao = new WarSummonerDao(DbConnectionPool.getConnection());

		Assert.assertTrue(dao.getAll(Integer.MAX_VALUE).isEmpty());
		dao = new WarSummonerDao(DbConnectionPool.getConnection());
		dao.storeData(this.group1Key, Lists.newArrayList("Foo", "Bar", "Bar1", "Bar2", "John", "Dude", "Duff", "Joseph",
		        "Slux", "Nemesis", "Alvaro 88"));
		dao = new WarSummonerDao(DbConnectionPool.getConnection());
		Map<Integer, WarSummoner> summoners = dao.getAll(this.group1Key);
		Assert.assertEquals(3, summoners.size());
		System.out.println(summoners);
	}

}
