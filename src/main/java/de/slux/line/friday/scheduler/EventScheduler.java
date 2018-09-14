/**
 * 
 */
package de.slux.line.friday.scheduler;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.util.Calendar;
import java.util.UUID;

import org.quartz.CronScheduleBuilder;
import org.quartz.DateBuilder;
import org.quartz.DateBuilder.IntervalUnit;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author slux
 *
 */
public class EventScheduler {
	private static Logger LOG = LoggerFactory.getLogger(EventScheduler.class);

	public static final String ID_KEY = "id_key";
	public static final String MESSAGE_KEY = "message_key";

	private Scheduler scheduler;
	private McocSchedulerImporter mcocSchedulerImporter;

	/**
	 * ctor
	 * 
	 * @throws Exception
	 */
	public EventScheduler(McocSchedulerImporter importer) throws Exception {
		this.mcocSchedulerImporter = importer;
		this.scheduler = StdSchedulerFactory.getDefaultScheduler();
		this.scheduler.start();

		initScheduler();
	}

	/**
	 * Initialize the scheduler with the tasks
	 * 
	 * @param events
	 * @throws SchedulerException
	 */
	private void initScheduler() throws SchedulerException {
		addDailyMcocMasterJob();
	}

	/**
	 * This job every day reads up the mcoc scheduler and decides what jobs need
	 * to be triggered for the day
	 * 
	 * @throws SchedulerException
	 */
	private void addDailyMcocMasterJob() throws SchedulerException {
		String eventId = "mcoc schedule master job";
		LOG.info("Adding event: " + eventId);

		// Every night we check for events for the day
		JobDetail job1 = newJob(McocSchedulerJob.class).withIdentity(eventId + ":" + UUID.randomUUID().toString())
		        .usingJobData(ID_KEY, eventId).build();
		Trigger trigger1 = newTrigger().withIdentity(UUID.randomUUID().toString() + "_trigger_" + eventId).startNow()
		        .withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(3, 0)).build();

		this.scheduler.scheduleJob(job1, trigger1);

		// If it's already 3AM past, we need to trigger the event now (one shot)
		// to make sure we don't lose today's events
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(System.currentTimeMillis());
		int hourOfDay = c.get(Calendar.HOUR_OF_DAY);

		if (hourOfDay >= 3) {
			LOG.info("Adding event (NOW): " + eventId);
			// Trigger the mcoc scheduler now (one shot)
			// Every night we check for events for the day
			JobDetail jobNow = newJob(McocSchedulerJob.class).withIdentity(eventId + ":" + UUID.randomUUID().toString())
			        .usingJobData(ID_KEY, eventId).build();
			Trigger triggerNow = newTrigger().withIdentity(UUID.randomUUID().toString() + "_trigger_" + eventId)
			        .startAt(DateBuilder.futureDate(10, IntervalUnit.SECOND)).build();

			this.scheduler.scheduleJob(jobNow, triggerNow);
		}

	}

	/**
	 * Shutdown the scheduler
	 * 
	 * @throws SchedulerException
	 * @see {@link Scheduler#shutdown()}
	 */
	public void terminate() throws SchedulerException {
		this.scheduler.shutdown();
	}

	/**
	 * @return the mcocSchedulerImporter
	 */
	public McocSchedulerImporter getMcocSchedulerImporter() {
		return mcocSchedulerImporter;
	}

	public Scheduler getScheduler() {
		return this.scheduler;
	}

}