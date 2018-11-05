/**
 * 
 */
package de.slux.line.friday.scheduler;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import org.quartz.DateBuilder;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.slux.line.friday.FridayBotApplication;
import de.slux.line.friday.scheduler.McocDayInfo.AWStatus;
import de.slux.line.friday.scheduler.McocDayInfo.CatalystArena;
import de.slux.line.friday.scheduler.McocDayInfo.OneDayEvent;
import de.slux.line.friday.scheduler.McocDayInfo.ThreeDaysEvent;

/**
 * This job is the master job that spawns other jobs to notify about MCOC events
 * on LINE app.
 * <p>
 * All the times are in UTC
 * </p>
 * 
 * @author slux
 */
public class McocSchedulerJob implements Job {

	// Use this to adjust the UTC to the machine timezone
	public static final int TIMEZONE_ADJUSTMENT_FROM_UTC = 1;

	private static Logger LOG = LoggerFactory.getLogger(McocSchedulerJob.class);

	/**
	 * Ctor
	 */
	public McocSchedulerJob() {
	}

	/**
	 * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
	 */
	public void execute(JobExecutionContext context) throws JobExecutionException {
		Date todaysDate = new Date(FridayBotApplication.getInstance().getClockReference().millis());

		String eventId = context.getJobDetail().getJobDataMap().get(EventScheduler.ID_KEY).toString();

		// We get today's date to select the mcoc events
		String today = McocSchedulerImporter.DATE_FORMAT.format(todaysDate);

		// We also need yesterday to understand some events such as 3 days ones,
		// cat arena etc...
		Calendar c = Calendar.getInstance();
		c.setTime(todaysDate);
		c.add(Calendar.DAY_OF_MONTH, -1);
		String yesterday = McocSchedulerImporter.DATE_FORMAT.format(c.getTime());

		LOG.info("EVENT-ID=" + eventId + " ST=" + context.getScheduledFireTime() + " NOW=" + todaysDate
		        + " Running MCOC Master Job for " + today);

		FridayBotApplication app = FridayBotApplication.getInstance();

		McocDayInfo todayInfo = app.getEventScheduler().getMcocSchedulerImporter().getMcocScheduler().get(today);
		McocDayInfo yesterdayInfo = app.getEventScheduler().getMcocSchedulerImporter().getMcocScheduler()
		        .get(yesterday);

		LOG.info("Found MCOC Day Info for today '" + today + ": " + todayInfo + " and yesterday '" + yesterday + "': "
		        + yesterdayInfo);

		if (todayInfo == null) {
			LOG.error("Today '" + today + "' is not a good day. Can't find any day info");
			return;
		}

		try {
			// Create AQ job if needed
			switch (todayInfo.getAqStatus()) {
				case DAY1:
					createAqJob(context, "Day 1", 0);
					break;
				case DAY2:
					createAqJob(context, "Day 2", 5);
					break;
				case DAY3:
					createAqJob(context, "Day 3", 10);
					break;
				case DAY4:
					createAqJob(context, "Day 4", 15);
					break;
				case DAY5:
					createAqJob(context, "Day 5", 20);
					break;
				case OFF:
					// Nothing to do
					LOG.info("AQ is off, nothing to schedule for today");
					break;
			}
		} catch (Exception e) {
			LOG.error("Cannot create AQ job: " + e, e);
		}

		try {
			// Create AW job if needed
			if (yesterdayInfo != null && yesterdayInfo.getAwStatus() == AWStatus.MAINTENANCE
			        && todayInfo.getAwStatus() == AWStatus.PLACEMENT) {
				createGenericJob(context, "aw officer reminder " + AWStatus.PLACEMENT,
				        "Reminder for officers:\nAW Matching phase has opened!", 18 + TIMEZONE_ADJUSTMENT_FROM_UTC, 0);
			}

			switch (todayInfo.getAwStatus()) {
				case PLACEMENT:
					createAwJob(context, "Remember to place your defenders in AW! This is probably your last chance!",
					        todayInfo.getAwStatus());
					break;

				case ATTACK:
					createAwJob(context, "AW Attack Phase should be up by now!", todayInfo.getAwStatus());
					break;

				case MAINTENANCE:
					// Nothing to do
					LOG.info("AW is in maintenance, nothing to schedule for today");
					break;
			}
		} catch (Exception e) {
			LOG.error("Cannot create AW job: " + e, e);
		}

		// 1 day event
		try {
			if (todayInfo.getOneDayEventStatus() == OneDayEvent.ALLY_HELP)
				createGenericJob(context, "ally_help", "Alliance Help event just started!", 18, 30);

			if (todayInfo.getOneDayEventStatus() == OneDayEvent.LOYALTY_SPEND)
				createGenericJob(context, "ally_help", "Loyalty Spend event just started!", 18, 30);
		} catch (Exception e) {
			LOG.error("Cannot create one day event job: " + e, e);
		}

		if (yesterdayInfo == null) {
			LOG.error("Yesterday '" + yesterday + "' is not a good day. Can't find any day info");
			return;
		}

		// 3 days event
		try {
			if (todayInfo.getThreeDaysEventStatus() == ThreeDaysEvent.COMPLETION
			        && yesterdayInfo.getThreeDaysEventStatus() != ThreeDaysEvent.COMPLETION)
				createGenericJob(context, "completion", "Completion event just started!", 17, 30);

			if (todayInfo.getThreeDaysEventStatus() == ThreeDaysEvent.ITEMUSE
			        && yesterdayInfo.getThreeDaysEventStatus() != ThreeDaysEvent.ITEMUSE)
				createGenericJob(context, "itemuse", "Item Use event just started!", 17, 30);

			if (todayInfo.getThreeDaysEventStatus() == ThreeDaysEvent.TEAMUSE
			        && yesterdayInfo.getThreeDaysEventStatus() != ThreeDaysEvent.TEAMUSE) {
				createGenericJob(context, "teamuse", "Team Use " + todayInfo.getTeamUse() + " event just started!", 17,
				        30);
			}
		} catch (Exception e) {
			LOG.error("Cannot create three day event job: " + e, e);
		}

		// catalyst arena event
		try {
			if (todayInfo.getCatArenaStatus() == CatalystArena.T4B
			        && yesterdayInfo.getCatArenaStatus() != CatalystArena.T4B)
				createGenericJob(context, "t4b-arena", "T4 Basic Arena just started!", 22, 59);

			if (todayInfo.getCatArenaStatus() == CatalystArena.T1A
			        && yesterdayInfo.getCatArenaStatus() != CatalystArena.T1A)
				createGenericJob(context, "t1a-arena", "T1 Alpha Arena just started!", 22, 59);

		} catch (Exception e) {
			LOG.error("Cannot create catalyst arena event job: " + e, e);
		}

		// Donations reminder on Wednesday and Saturday
		c = Calendar.getInstance();
		c.setTimeInMillis(FridayBotApplication.getInstance().getClockReference().millis());
		if (c.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY || c.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
			try {
				String reminderOccurrence = (c.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY) ? "First" : "Last";
				String treasuryMessage = String.format(
				        "Treasury Awaits!\n%s friendly reminder to donate to the Alliance Treasury.\n\nIf you have any issues with the requirements, please contact an officer",
				        reminderOccurrence);
				createGenericJob(context, "donations-reminder-" + c.get(Calendar.DAY_OF_WEEK), treasuryMessage, 14, 0);
			} catch (Exception e) {
				LOG.error("Cannot create donation reminder event job: " + e, e);
			}
		}

		LOG.info("MCOC Master Job completed.");

	}

	/**
	 * Create a generic one-shot job
	 * 
	 * @param context
	 * @param eventId
	 * @param message
	 * @param hour
	 * @param minute
	 * @throws SchedulerException
	 */
	private void createGenericJob(JobExecutionContext context, String eventId, String message, int hour, int minute)
	        throws SchedulerException {
		Scheduler s = context.getScheduler();

		LOG.info("Adding event: " + eventId);

		JobDetail job1 = newJob(LinePushJob.class).withIdentity(eventId + ":" + UUID.randomUUID().toString())
		        .usingJobData(EventScheduler.MESSAGE_KEY, message).usingJobData(EventScheduler.ID_KEY, eventId).build();
		int theHour = (hour + TIMEZONE_ADJUSTMENT_FROM_UTC) % 24;
		Trigger trigger1 = newTrigger().withIdentity(UUID.randomUUID().toString() + "_trigger_" + eventId)
		        .startAt(DateBuilder.todayAt(theHour, minute, 0)).build();

		s.scheduleJob(job1, trigger1);

	}

	/**
	 * Create the AW job for the day
	 * 
	 * @param context
	 * @param awStatus
	 * @throws SchedulerException
	 */
	private void createAwJob(JobExecutionContext context, String awMessage, AWStatus awStatus)
	        throws SchedulerException {
		Scheduler s = context.getScheduler();

		String eventId = "aw reminder " + awStatus;
		LOG.info("Adding event: " + eventId);

		// One shot AW reminder
		JobDetail job1 = newJob(LinePushJob.class).withIdentity(eventId + ":" + UUID.randomUUID().toString())
		        .usingJobData(EventScheduler.MESSAGE_KEY, awMessage).usingJobData(EventScheduler.ID_KEY, eventId)
		        .build();

		Trigger trigger1 = null;

		if (awStatus.equals(AWStatus.PLACEMENT)) {
			trigger1 = newTrigger().withIdentity(UUID.randomUUID().toString() + "_trigger_" + eventId)
			        .startAt(DateBuilder.tomorrowAt(17 + TIMEZONE_ADJUSTMENT_FROM_UTC, 0, 0)).build();
		} else { // Attack
			trigger1 = newTrigger().withIdentity(UUID.randomUUID().toString() + "_trigger_" + eventId)
			        .startAt(DateBuilder.tomorrowAt(2 + TIMEZONE_ADJUSTMENT_FROM_UTC, 0, 0)).build();
		}

		s.scheduleJob(job1, trigger1);

	}

	/**
	 * Create the AQ job for the day
	 * 
	 * @param context
	 * @param aqDay
	 * @param deltaTime
	 * @throws SchedulerException
	 */
	private void createAqJob(JobExecutionContext context, String aqDay, int deltaTime) throws SchedulerException {
		Scheduler s = context.getScheduler();

		String eventId = "aq reminder " + aqDay;
		LOG.info("Adding event: " + eventId);

		// One shot AQ reminder
		JobDetail job1 = newJob(LinePushJob.class).withIdentity(eventId + ":" + UUID.randomUUID().toString())
		        .usingJobData(EventScheduler.MESSAGE_KEY, "AQ " + aqDay + " will start shortly! Get ready!")
		        .usingJobData(EventScheduler.ID_KEY, eventId).build();
		Trigger trigger1 = newTrigger().withIdentity(UUID.randomUUID().toString() + "_trigger_" + eventId)
		        .startAt(DateBuilder.todayAt(20 + TIMEZONE_ADJUSTMENT_FROM_UTC, 0 + deltaTime, 0)).build();

		s.scheduleJob(job1, trigger1);
	}

}
