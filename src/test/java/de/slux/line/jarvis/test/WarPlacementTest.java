/**
 * 
 */
package de.slux.line.jarvis.test;

import static org.junit.Assert.assertNotEquals;

import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import de.slux.line.jarvis.dao.DbConnectionPool;
import de.slux.line.jarvis.dao.WarSummonerDao;
import de.slux.line.jarvis.data.war.WarSummoner;
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
