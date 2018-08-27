/**
 * 
 */
package de.slux.line.friday.command;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.message.TextMessage;

import de.slux.line.friday.FridayBotApplication;

/**
 * This command is triggered for information
 * 
 * @author slux
 */
public class InfoCommand extends AbstractCommand {
	public static final String CMD_PREFIX = "friday info";

	// Replace $GUID with the groupID
	private static final String PAYPAL_LINK = "https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=slux83@gmail.com&lc=US&item_name=friday%3A$GUID&no_note=0&cn=&currency_code=USD&bn=PP-DonationsBF:btn_donateCC_LG.gif:NonHosted";
	private static final String FRIDAY_USERS_LINE_GROUP_LINK = "https://line.me/R/ti/g/oNF5Riui79";

	/**
	 * Ctor
	 * 
	 * @param messagingClient
	 */
	public InfoCommand(LineMessagingClient messagingClient) {
		super(messagingClient);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.slux.line.friday.command.AbstractCommand#canTrigger(java.lang.String)
	 */
	@Override
	public boolean canTrigger(String message) {
		return message.equalsIgnoreCase(CMD_PREFIX);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.slux.line.friday.command.AbstractCommand#execute(java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public TextMessage execute(String userId, String senderId, String message) {
		StringBuilder sb = new StringBuilder("*** F.R.I.D.A.Y. MCOC Line Bot ***\n\n");
		sb.append(getInfo(senderId));
		return new TextMessage(sb.toString());
	}

	/**
	 * Get the info
	 * 
	 * @return
	 */
	public static String getInfo(String senderId) {
		StringBuilder sb = new StringBuilder();
		sb.append("FRIDAY Bot Line users group: ");
		sb.append(FRIDAY_USERS_LINE_GROUP_LINK);
		sb.append("\n");
		sb.append("You can join the LINE group of FRIDAY users to ask for help and play with the Bot.\n\n");
		sb.append("Version: ");
		sb.append(FridayBotApplication.FRIDAY_VERSION);
		sb.append("\n");
		sb.append("Author: slux83\n");
		sb.append("Issues tracker: https://github.com/slux83/friday-bot/issues\n\n");
		sb.append("Please cosider a small donation if you want to support the FRIDAY Bot development!\n");
		sb.append("FRIDAY Bot is hosted on a dedicated server and it has some running costs.\n\n");
		sb.append("PAYPAL: ");
		sb.append(PAYPAL_LINK.replace("$GUID", senderId));
		sb.append("\n");

		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.slux.line.friday.command.AbstractCommand#getType()
	 */
	@Override
	public CommandType getType() {
		return CommandType.CommandTypeUtility;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.slux.line.friday.command.AbstractCommand#getHelp()
	 */
	@Override
	public String getHelp() {
		StringBuilder sb = new StringBuilder();
		sb.append("[" + CMD_PREFIX + "]\n");
		sb.append("Prints informations about J.A.R.V.I.S. Line Bot, issue tracker, donations, etc...");

		return sb.toString();
	}
}
