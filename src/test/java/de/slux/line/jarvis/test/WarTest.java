/**
 * 
 */
package de.slux.line.jarvis.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.slux.line.jarvis.dao.exception.WarDaoDuplicatedAllianceTagException;
import de.slux.line.jarvis.dao.exception.WarDaoUnregisteredException;
import de.slux.line.jarvis.data.war.WarGroup;
import de.slux.line.jarvis.data.war.WarGroup.TotalDeathReport;
import de.slux.line.jarvis.logic.war.WarDeathLogic;

/**
 * Testing the war stuff
 * 
 * @author Slux
 */
public class WarTest {

	private static final String GROUP1_ID = "test_group1";
	private static final String GROUP2_ID = "test_group2";
	private static final String GROUP3_ID = "test_group3";

	private WarDeathLogic warModel;

	@Before
	public void tierUp() throws Exception {
		this.warModel = new WarDeathLogic();
		this.warModel.register(GROUP1_ID, "test_group_1");
		this.warModel.register(GROUP2_ID, "test_group_2");
		this.warModel.register(GROUP3_ID, "test_group_3");
		this.warModel.resetFor(GROUP1_ID);
		this.warModel.resetFor(GROUP2_ID);
		this.warModel.resetFor(GROUP3_ID);
	}

	@AfterClass
	public static void tierDown() throws Exception {
		WarDeathLogic model = new WarDeathLogic();
		model.resetFor(GROUP1_ID);
		model.resetFor(GROUP2_ID);
		model.resetFor(GROUP3_ID);
		
		model.deleteHistoryEntries(GROUP1_ID, "ISO8A", new Date());
		model.deleteHistoryEntries(GROUP2_ID, "DH", new Date());
		model.deleteHistoryEntries(GROUP2_ID, "DH-DM", new Date());
		model.deleteHistoryEntries(GROUP3_ID, "BRAE", new Date());

	}

	@Test(expected = WarDaoUnregisteredException.class)
	public void testUnregisteredOperation() throws Exception {
		this.warModel.addDeath("test_group_dummy", 1, 1, "champName", "userName");
	}

	@Test
	public void testWarModel() throws Exception {
		this.warModel.addNewGroup(GROUP2_ID, "ALLY-BG2");
		this.warModel.addNewGroup(GROUP1_ID, "ALLY-BG1");
		this.warModel.addNewGroup(GROUP3_ID, "ALLY-BG3");

		int group1Key = this.warModel.getKeyOfGroup(GROUP1_ID);
		int group2Key = this.warModel.getKeyOfGroup(GROUP2_ID);
		int group3Key = this.warModel.getKeyOfGroup(GROUP3_ID);
		assertNotEquals(-1, group1Key);
		assertNotEquals(-1, group2Key);
		assertNotEquals(-1, group3Key);

		Map<String, String> groups = this.warModel.getAllGroups();
		assertFalse(groups.isEmpty());
		System.out.println(groups);

		// Group 1 reports
		this.warModel.addDeath(GROUP1_ID, 1, 10, "Modok", "Slux");
		this.warModel.addDeath(GROUP1_ID, 2, 13, "SIM", "Slux");
		this.warModel.addDeath(GROUP1_ID, 1, 35, "Hela", "Tony");
		this.warModel.addDeath(GROUP1_ID, 1, 12, "Hulk", "Ron");
		this.warModel.addDeath(GROUP1_ID, 4, 44, "Medusa", "Ron");
		this.warModel.addDeath(GROUP1_ID, 1, 44, "Medusa", "Tom");
		this.warModel.addDeath(GROUP1_ID, 10, 54, "Dormammu", "Jack");

		// Group 2 reports
		this.warModel.addDeath(GROUP2_ID, 1, 1, "Red Hulk", "Slig");
		this.warModel.addDeath(GROUP2_ID, 5, 38, "IMIW", "Brok");
		this.warModel.addDeath(GROUP2_ID, 1, 50, "Mephisto", "Skizz");
		this.warModel.addDeath(GROUP2_ID, 1, 12, "Hulk", "Billy");
		this.warModel.addDeath(GROUP2_ID, 4, 44, "Medusa", "Cassy");
		this.warModel.addDeath(GROUP2_ID, 1, 44, "Sentinel", "unknown");
		this.warModel.addDeath(GROUP2_ID, 3, 54, "Dormammu", "Nem");
		this.warModel.addDeath(GROUP2_ID, 2, 54, "Dormammu", "Diffy");

		// Group 3 reports are huge
		for (int i = 1; i < 56; i++) {
			this.warModel.addDeath(GROUP3_ID, 1, i, "Champ_" + i, "User_" + i);
		}

		// Test UNDO
		this.warModel.addDeath(GROUP1_ID, 1, 100, "wrong1", "someone1");
		this.warModel.addDeath(GROUP1_ID, 1, 100, "wrong2", "someone2");
		WarGroup groupModel1 = this.warModel.getReportModel(group1Key);
		Assert.assertEquals(9, groupModel1.getDeathReports().size());
		this.warModel.undoLast(GROUP1_ID);
		this.warModel.undoLast(GROUP1_ID);
		groupModel1 = this.warModel.getReportModel(group1Key);
		Assert.assertEquals(7, groupModel1.getDeathReports().size());

		// Test the reporting feature
		System.out.println(this.warModel.getReport(GROUP1_ID));
		System.out.println(this.warModel.getReport(GROUP2_ID));

		groupModel1 = this.warModel.getReportModel(group1Key);
		TotalDeathReport totalDeathReport = groupModel1.calculateDeathReport();
		Assert.assertEquals(20, totalDeathReport.getTotalDeaths());
		Assert.assertEquals(880, totalDeathReport.getTotalLostPoints());

		WarGroup groupModel2 = this.warModel.getReportModel(group2Key);
		totalDeathReport = groupModel2.calculateDeathReport();
		Assert.assertEquals(18, totalDeathReport.getTotalDeaths());
		Assert.assertEquals(960, totalDeathReport.getTotalLostPoints());

		Assert.assertEquals(groupModel1.getDeathReports().size(), 7);
		System.out.println(groupModel1.getDeathReports());

		Assert.assertEquals(groupModel2.getDeathReports().size(), 8);
		System.out.println(groupModel2.getDeathReports());

		// Test the summary
		System.out.println(this.warModel.getSummary(GROUP1_ID));
		System.out.println(this.warModel.getSummary(GROUP2_ID));
		List<String> bg3summary = this.warModel.getSummary(GROUP3_ID);
		System.out.println("HUGE SUMMARY:\n" + bg3summary);
		Assert.assertTrue(bg3summary.size() > 1);

		this.warModel.saveWar(GROUP1_ID, "ISO8A");

		this.warModel.saveWar(GROUP2_ID, "DH");
		this.warModel.saveWar(GROUP2_ID, "DH-DM");

		this.warModel.saveWar(GROUP3_ID, "BRAE");

		try {
			this.warModel.saveWar(GROUP2_ID, "DH");
			assertFalse("expected exception here", true);
		} catch (WarDaoDuplicatedAllianceTagException e) {
			System.err.println(e);
		}
		System.out.println(this.warModel.getHistoryText(GROUP1_ID));
		System.out.println(this.warModel.getHistoryText(GROUP2_ID));

		List<String> bg3History = this.warModel.getHistoryText(GROUP3_ID);
		System.out.println("HUGE HISTORY:\n" + bg3History);
		Assert.assertTrue(bg3History.size() > 0);

		Map<String, WarGroup> historyToday = this.warModel.getHistorySummary(GROUP1_ID, new Date());
		for (Entry<String, WarGroup> entry : historyToday.entrySet()) {
			System.out.println("ALLY " + entry.getKey());
			System.out.println(entry.getValue().getSummaryText());
		}

		assertTrue(this.warModel.deleteHistoryEntries(GROUP1_ID, "ISO8A", new Date()));

		assertTrue(this.warModel.deleteHistoryEntries(GROUP2_ID, "DH", new Date()));
		assertTrue(this.warModel.deleteHistoryEntries(GROUP2_ID, "DH-DM", new Date()));
		assertFalse(this.warModel.deleteHistoryEntries(GROUP2_ID, "FAKE", new Date()));

		historyToday = this.warModel.getHistorySummary(GROUP1_ID, new Date());
		for (Entry<String, WarGroup> entry : historyToday.entrySet()) {
			System.out.println("ALLY " + entry.getKey());
			System.out.println(entry.getValue().getSummaryText());
		}
	}

}
