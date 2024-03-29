/**
 * 
 */
package de.slux.line.friday.scheduler;

import java.io.IOException;
import java.sql.SQLException;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.slux.line.friday.FridayBotApplication;
import de.slux.line.friday.logic.StatsLogic;

/**
 * Job that periodically updates the war node statistics
 * 
 * @author slux
 */
public class WarStatsJob implements Job {
	private static Logger LOG = LoggerFactory.getLogger(WarStatsJob.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
	 */
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		String eventId = context.getJobDetail().getJobDataMap().get(EventScheduler.ID_KEY).toString();

		LOG.info("Refreshing War statistics: " + eventId);

		try {
			long start = System.currentTimeMillis();
			StatsLogic logic = new StatsLogic(true);

			String outcome = logic.updateStatistics();

			long end = System.currentTimeMillis();

			LOG.info("War node stats updated in " + String.format("%.2f", (Math.abs(end - start) / 1000.0))
			        + " sec(s).\n" + outcome);

			notifyAdmin("War node stats updated in " + String.format("%.2f", (Math.abs(end - start) / 1000.0))
			        + " sec(s).\n" + outcome, false);
		} catch (IOException e) {
			LOG.error("Can't retrieve the list of champions from Paste Bin: " + e, e);
			notifyAdmin("Can't retrieve the list of champions from Paste Bin: " + e, true);
		} catch (SQLException e) {
			LOG.error("Can't update war stats: " + e, e);
			notifyAdmin("Can't update war stats: " + e, true);
		}

	}

	/**
	 * Push to the admin
	 * 
	 * @param message
	 * @param isError
	 */
	private void notifyAdmin(String message, boolean isError) {
		String error = "";
		if (isError)
			error = "ERROR: ";
		FridayBotApplication.getInstance().pushMessageToAdmin(error + message);

	}

}
