package de.slux.line.friday.test.util.scheduler;

import org.quartz.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author slux
 */
public class ContextDummy implements JobExecutionContext {

    private Object result;
    private Map<Object, Object> properties;
    private boolean missfire;

    /**
     * Constructor
     */
    public ContextDummy(boolean missfire) {
        this.properties = new HashMap<>();
        this.missfire = missfire;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.quartz.JobExecutionContext#getScheduler()
     */
    @Override
    public Scheduler getScheduler() {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.quartz.JobExecutionContext#getTrigger()
     */
    @Override
    public Trigger getTrigger() {
        return new TriggerDummy(missfire);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.quartz.JobExecutionContext#getCalendar()
     */
    @Override
    public Calendar getCalendar() {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.quartz.JobExecutionContext#isRecovering()
     */
    @Override
    public boolean isRecovering() {
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.quartz.JobExecutionContext#getRecoveringTriggerKey()
     */
    @Override
    public TriggerKey getRecoveringTriggerKey() throws IllegalStateException {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.quartz.JobExecutionContext#getRefireCount()
     */
    @Override
    public int getRefireCount() {
        return 0;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.quartz.JobExecutionContext#getMergedJobDataMap()
     */
    @Override
    public JobDataMap getMergedJobDataMap() {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.quartz.JobExecutionContext#getJobDetail()
     */
    @Override
    public JobDetail getJobDetail() {
        return new JobDetailDummy();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.quartz.JobExecutionContext#getJobInstance()
     */
    @Override
    public Job getJobInstance() {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.quartz.JobExecutionContext#getFireTime()
     */
    @Override
    public Date getFireTime() {
        return new Date();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.quartz.JobExecutionContext#getScheduledFireTime()
     */
    @Override
    public Date getScheduledFireTime() {
        return new Date();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.quartz.JobExecutionContext#getPreviousFireTime()
     */
    @Override
    public Date getPreviousFireTime() {
        return new Date();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.quartz.JobExecutionContext#getNextFireTime()
     */
    @Override
    public Date getNextFireTime() {
        return new Date();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.quartz.JobExecutionContext#getFireInstanceId()
     */
    @Override
    public String getFireInstanceId() {
        return UUID.randomUUID().toString();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.quartz.JobExecutionContext#getResult()
     */
    @Override
    public Object getResult() {
        return this.result;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.quartz.JobExecutionContext#setResult(java.lang.Object)
     */
    @Override
    public void setResult(Object result) {
        this.result = result;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.quartz.JobExecutionContext#getJobRunTime()
     */
    @Override
    public long getJobRunTime() {
        return System.currentTimeMillis();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.quartz.JobExecutionContext#put(java.lang.Object,
     * java.lang.Object)
     */
    @Override
    public void put(Object key, Object value) {
        this.properties.put(key, value);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.quartz.JobExecutionContext#get(java.lang.Object)
     */
    @Override
    public Object get(Object key) {
        return this.properties.get(key);
    }

}
