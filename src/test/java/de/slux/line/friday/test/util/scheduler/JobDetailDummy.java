package de.slux.line.friday.test.util.scheduler;

import java.util.UUID;

import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;

import de.slux.line.friday.scheduler.EventScheduler;

/**
 * @author slux
 *
 */
public class JobDetailDummy implements JobDetail {

	private static final long serialVersionUID = 1L;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.quartz.JobDetail#getKey()
	 */
	@Override
	public JobKey getKey() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.quartz.JobDetail#getDescription()
	 */
	@Override
	public String getDescription() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.quartz.JobDetail#getJobClass()
	 */
	@Override
	public Class<? extends Job> getJobClass() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.quartz.JobDetail#getJobDataMap()
	 */
	@Override
	public JobDataMap getJobDataMap() {
		JobDataMapDummy dataMap = new JobDataMapDummy();
		dataMap.put(EventScheduler.ID_KEY, "event-id-" + UUID.randomUUID().toString());
		dataMap.put(EventScheduler.MESSAGE_KEY, "message-" + UUID.randomUUID().toString());
		return dataMap;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.quartz.JobDetail#isDurable()
	 */
	@Override
	public boolean isDurable() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.quartz.JobDetail#isPersistJobDataAfterExecution()
	 */
	@Override
	public boolean isPersistJobDataAfterExecution() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.quartz.JobDetail#isConcurrentExectionDisallowed()
	 */
	@Override
	public boolean isConcurrentExectionDisallowed() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.quartz.JobDetail#requestsRecovery()
	 */
	@Override
	public boolean requestsRecovery() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.quartz.JobDetail#getJobBuilder()
	 */
	@Override
	public JobBuilder getJobBuilder() {
		return null;
	}

	@Override
	public Object clone() {
		return null;
	}

}
