/**
 * 
 */
package de.slux.line.friday.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Map;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import de.slux.line.friday.dao.DbConnectionPool;
import de.slux.line.friday.dao.exception.GenericDaoException;
import de.slux.line.friday.dao.exception.SummonerNotFoundException;
import de.slux.line.friday.dao.war.WarSummonerDao;
import de.slux.line.friday.data.war.WarSummoner;
import de.slux.line.friday.data.war.WarSummonerPlacement;
import de.slux.line.friday.data.war.WarGroup.GroupStatus;
import de.slux.line.friday.logic.war.WarDeathLogic;

/**
 * Testing the war stuff
 * 
 * @author Slux
 */
public class WarPlacementTest {

	private static final String GROUP1_ID = UUID.randomUUID().toString();
	private static final String GROUP2_ID = UUID.randomUUID().toString();
	private static final String GROUP3_ID = UUID.randomUUID().toString();

	private WarDeathLogic warModel;
	private int group1Key;
	private int group2Key;
	private int group3Key;

	@Before
	public void tierUp() throws Exception {
		this.warModel = new WarDeathLogic();
		this.warModel.register(GROUP1_ID, "test_group_1");
		this.warModel.register(GROUP2_ID, "test_group_2");
		this.warModel.register(GROUP3_ID, "test_group_3");
		this.warModel.resetFor(GROUP1_ID);
		this.warModel.resetFor(GROUP2_ID);
		this.warModel.resetFor(GROUP3_ID);

		this.group1Key = WarDeathLogic.getKeyOfGroup(GROUP1_ID, GroupStatus.GroupStatusActive);
		this.group2Key = WarDeathLogic.getKeyOfGroup(GROUP2_ID, GroupStatus.GroupStatusActive);
		this.group3Key = WarDeathLogic.getKeyOfGroup(GROUP3_ID, GroupStatus.GroupStatusActive);
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

		// Rename a summoner
		dao = new WarSummonerDao(DbConnectionPool.getConnection());
		dao.renameSummoner(this.group1Key, 3, "Slux is Testing");

		dao = new WarSummonerDao(DbConnectionPool.getConnection());
		summoners = dao.getAll(this.group1Key);
		assertNotNull(summoners.get(3));
		assertEquals("Slux is Testing", summoners.get(3).getName());

		// Test summoner not found
		try {
			dao = new WarSummonerDao(DbConnectionPool.getConnection());
			dao.renameSummoner(this.group1Key, 13, "dummy");
			Assert.assertTrue(false); // must have an exception thrown
		} catch (SummonerNotFoundException e) {
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
