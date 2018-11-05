package de.slux.line.friday.scheduler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Scheduler importer based on the CSV export of the MCOC Google Sheet scheduler
 * 
 * @author slux
 */
public class McocSchedulerImporter {
	private static Logger LOG = LoggerFactory.getLogger(McocSchedulerImporter.class);
	// private static final String MCOC_SCHEDULER_URL =
	// "https://pastebin.com/raw/tpUuZnBQ";//latest
	private static final String MCOC_SCHEDULER_URL = "https://pastebin.com/raw/9MhEghze";// dev
	private static final int DATE_INDEX = 1;
	private static final int AQ_INDEX = 7;
	private static final int AW_INDEX = 10;
	private static final int CATALYST_ARENA_INDEX = 6;
	private static final int THREE_DAYS_EVENT_INDEX = 9;
	private static final int ONE_DAY_EVENT_INDEX = 8;
	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM dd yyyy", Locale.ENGLISH);

	private Map<String, McocDayInfo> mcocScheduler;

	/**
	 * Ctor
	 * 
	 * @throws Exception
	 */
	public McocSchedulerImporter() throws Exception {

		this.mcocScheduler = new ConcurrentHashMap<String, McocDayInfo>();

		URL url = new URL(MCOC_SCHEDULER_URL);

		BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8));

		boolean firstLine = true;
		String line = null;
		while ((line = in.readLine()) != null) {
			if (firstLine) {
				// Skip header of the CSV
				firstLine = false;
				continue;
			}

			String[] columns = line.split(",");
			Calendar c = Calendar.getInstance();
			int currentYear = c.get(Calendar.YEAR);
			String date = columns[DATE_INDEX] + " " + currentYear;
			Date today = null;
			try {
				today = DATE_FORMAT.parse(date);
			} catch (ParseException e) {
				LOG.error("Parser exception. Cannot parse the date " + date + ": " + e);
			}

			if (today == null) {
				// Not a valid day entry
				continue;
			}

			try {

				this.mcocScheduler.put(date, McocDayInfo.createMcocDayInfo(columns[AQ_INDEX], columns[AW_INDEX],
				        columns[CATALYST_ARENA_INDEX], columns[ONE_DAY_EVENT_INDEX], columns[THREE_DAYS_EVENT_INDEX]));
			} catch (ParseException e) {
				LOG.error("Cannot parse data for day '" + date + "'. Columns: " + Arrays.toString(columns)
				        + ". Exception: " + e);
			}

			LOG.debug("DATE=" + date + " INFO=" + this.mcocScheduler.get(date));
			if (LOG.isTraceEnabled()) {
				for (int i = 0; i < columns.length; ++i) {
					LOG.trace(i + "\t" + columns[i]);
				}
			}

		}

		LOG.info("Scheduler importer initialization completed");

	}

	/**
	 * @return the mcocScheduler
	 */
	public Map<String, McocDayInfo> getMcocScheduler() {
		return mcocScheduler;
	}

}
