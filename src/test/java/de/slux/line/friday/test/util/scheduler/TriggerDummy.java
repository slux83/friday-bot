package de.slux.line.friday.test.util.scheduler;

import java.util.Calendar;
import java.util.Date;

import org.quartz.JobDataMap;
import org.quartz.JobKey;
import org.quartz.ScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;

/**
 * @author slux
 *
 */
public class TriggerDummy implements Trigger {

	private static final long serialVersionUID = 1L;
	private boolean missfire;

	public TriggerDummy(boolean missfire) {
		this.missfire = missfire;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.quartz.Trigger#getKey()
	 */
	@Override
	public TriggerKey getKey() {

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.quartz.Trigger#getJobKey()
	 */
	@Override
	public JobKey getJobKey() {

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.quartz.Trigger#getDescription()
	 */
	@Override
	public String getDescription() {

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.quartz.Trigger#getCalendarName()
	 */
	@Override
	public String getCalendarName() {

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.quartz.Trigger#getJobDataMap()
	 */
	@Override
	public JobDataMap getJobDataMap() {

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.quartz.Trigger#getPriority()
	 */
	@Override
	public int getPriority() {

		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.quartz.Trigger#mayFireAgain()
	 */
	@Override
	public boolean mayFireAgain() {

		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.quartz.Trigger#getStartTime()
	 */
	@Override
	public Date getStartTime() {
		if (!missfire) {
			return new Date();
		} else {
			Calendar c = Calendar.getInstance();
			c.add(Calendar.HOUR_OF_DAY, -1);
			return c.getTime();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.quartz.Trigger#getEndTime()
	 */
	@Override
	public Date getEndTime() {

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.quartz.Trigger#getNextFireTime()
	 */
	@Override
	public Date getNextFireTime() {
		// tomorrow same time
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DAY_OF_YEAR, 1);
		return c.getTime();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.quartz.Trigger#getPreviousFireTime()
	 */
	@Override
	public Date getPreviousFireTime() {

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.quartz.Trigger#getFireTimeAfter(java.util.Date)
	 */
	@Override
	public Date getFireTimeAfter(Date afterTime) {

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.quartz.Trigger#getFinalFireTime()
	 */
	@Override
	public Date getFinalFireTime() {

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.quartz.Trigger#getMisfireInstruction()
	 */
	@Override
	public int getMisfireInstruction() {

		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.quartz.Trigger#getTriggerBuilder()
	 */
	@Override
	public TriggerBuilder<? extends Trigger> getTriggerBuilder() {

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.quartz.Trigger#getScheduleBuilder()
	 */
	@Override
	public ScheduleBuilder<? extends Trigger> getScheduleBuilder() {

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.quartz.Trigger#compareTo(org.quartz.Trigger)
	 */
	@Override
	public int compareTo(Trigger other) {

		return 0;
	}

}
