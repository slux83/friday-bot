package de.slux.line.friday.scheduler;

import java.text.ParseException;

/**
 * @author slux
 *
 */
public class McocDayInfo {
	public enum AqStatus {
		OFF, DAY1, DAY2, DAY3, DAY4, DAY5
	}

	public enum AWStatus {
		MAINTENANCE, ATTACK, PLACEMENT
	}

	public enum CatalystArena {
		OFF, T4B, T1A
	}

	public enum OneDayEvent {
		OFF, LOYALTY_SPEND, ALLY_HELP
	}

	public enum ThreeDaysEvent {
		OFF, COMPLETION, ITEMUSE, TEAMUSE
	}

	private AqStatus aqStatus;
	private AWStatus awStatus;
	private CatalystArena catArenaStatus;
	private OneDayEvent oneDayEventStatus;
	private ThreeDaysEvent threeDaysEventStatus;

	/**
	 * Factory to create the object
	 * 
	 * @param aqStatus
	 * @param awStatus
	 * @param threeDaysEvent
	 * @param oneDayEvent
	 * @param catArena
	 * @throws ParseException
	 */
	public static McocDayInfo createMcocDayInfo(String aqStatus,
			String awStatus, String catArena, String oneDayEvent,
			String threeDaysEvent) throws ParseException {

		McocDayInfo mdi = new McocDayInfo();

		if (aqStatus.equals("off"))
			mdi.aqStatus = AqStatus.OFF;
		else if (aqStatus.equals("on - d1"))
			mdi.aqStatus = AqStatus.DAY1;
		else if (aqStatus.equals("on - d2"))
			mdi.aqStatus = AqStatus.DAY2;
		else if (aqStatus.equals("on - d3"))
			mdi.aqStatus = AqStatus.DAY3;
		else if (aqStatus.equals("on - d4"))
			mdi.aqStatus = AqStatus.DAY4;
		else if (aqStatus.equals("on - d5"))
			mdi.aqStatus = AqStatus.DAY5;
		else
			throw new ParseException("The AQ status " + aqStatus
					+ " is not valid.", -1);

		if (awStatus.contains("Maint"))
			mdi.awStatus = AWStatus.MAINTENANCE;
		else if (awStatus.equals("Place"))
			mdi.awStatus = AWStatus.PLACEMENT;
		else if (awStatus.equals("Attack"))
			mdi.awStatus = AWStatus.ATTACK;
		else
			throw new ParseException("The AW status " + awStatus
					+ " is not valid.", -1);

		if (catArena.equals("Basic"))
			mdi.catArenaStatus = CatalystArena.T4B;
		else if (catArena.equals("Alpha"))
			mdi.catArenaStatus = CatalystArena.T1A;
		else
			mdi.catArenaStatus = CatalystArena.OFF;

		if (oneDayEvent.equals("Loyalty Spend"))
			mdi.oneDayEventStatus = OneDayEvent.LOYALTY_SPEND;
		else if (oneDayEvent.equals("Alliance Help"))
			mdi.oneDayEventStatus = OneDayEvent.ALLY_HELP;
		else
			mdi.oneDayEventStatus = OneDayEvent.OFF;

		if (threeDaysEvent.equals("Completion"))
			mdi.threeDaysEventStatus = ThreeDaysEvent.COMPLETION;
		else if (threeDaysEvent.equals("Item Use"))
			mdi.threeDaysEventStatus = ThreeDaysEvent.ITEMUSE;
		else if (threeDaysEvent.equals("Team Use"))
			mdi.threeDaysEventStatus = ThreeDaysEvent.TEAMUSE;
		else
			mdi.threeDaysEventStatus = ThreeDaysEvent.OFF;

		return mdi;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "MCOC Day Events:\n aqStatus=" + aqStatus + "\n awStatus=" + awStatus
				+ "\n catArenaStatus=" + catArenaStatus + "\n oneDayEventStatus="
				+ oneDayEventStatus + "\n threeDaysEventStatus="
				+ threeDaysEventStatus;
	}

	/**
	 * @return the aqStatus
	 */
	public AqStatus getAqStatus() {
		return aqStatus;
	}

	/**
	 * @param aqStatus
	 *            the aqStatus to set
	 */
	public void setAqStatus(AqStatus aqStatus) {
		this.aqStatus = aqStatus;
	}

	/**
	 * @return the awStatus
	 */
	public AWStatus getAwStatus() {
		return awStatus;
	}

	/**
	 * @param awStatus
	 *            the awStatus to set
	 */
	public void setAwStatus(AWStatus awStatus) {
		this.awStatus = awStatus;
	}

	/**
	 * @return the catArenaStatus
	 */
	public CatalystArena getCatArenaStatus() {
		return catArenaStatus;
	}

	/**
	 * @param catArenaStatus
	 *            the catArenaStatus to set
	 */
	public void setCatArenaStatus(CatalystArena catArenaStatus) {
		this.catArenaStatus = catArenaStatus;
	}

	/**
	 * @return the oneDayEventStatus
	 */
	public OneDayEvent getOneDayEventStatus() {
		return oneDayEventStatus;
	}

	/**
	 * @param oneDayEventStatus
	 *            the oneDayEventStatus to set
	 */
	public void setOneDayEventStatus(OneDayEvent oneDayEventStatus) {
		this.oneDayEventStatus = oneDayEventStatus;
	}

	/**
	 * @return the threeDaysEventStatus
	 */
	public ThreeDaysEvent getThreeDaysEventStatus() {
		return threeDaysEventStatus;
	}

	/**
	 * @param threeDaysEventStatus
	 *            the threeDaysEventStatus to set
	 */
	public void setThreeDaysEventStatus(ThreeDaysEvent threeDaysEventStatus) {
		this.threeDaysEventStatus = threeDaysEventStatus;
	}

}
