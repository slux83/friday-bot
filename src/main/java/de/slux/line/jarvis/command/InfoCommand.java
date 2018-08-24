/**
 * 
 */
package de.slux.line.jarvis.command;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.message.TextMessage;

import de.slux.line.jarvis.JarvisBotApplication;

/**
 * This command is triggered for information
 * 
 * @author slux
 */
public class InfoCommand extends AbstractCommand {
	public static final String CMD_PREFIX = "jarvis info";

	// Replace $GUID with the groupID
	private static final String PAYPAL_LINK = "https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=slux83@gmail.com&lc=US&item_name=jarvis%3A$GUID&no_note=0&cn=&currency_code=USD&bn=PP-DonationsBF:btn_donateCC_LG.gif:NonHosted";
	private static final String JARVIS_USERS_LINE_GROUP_LINK = "https://line.me/R/ti/g/oNF5Riui79";

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
	 * de.slux.line.jarvis.command.AbstractCommand#canTrigger(java.lang.String)
	 */
	@Override
	public boolean canTrigger(String message) {
		return message.equalsIgnoreCase(CMD_PREFIX);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.slux.line.jarvis.command.AbstractCommand#execute(java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public TextMessage execute(String userId, String senderId, String message) {
		StringBuilder sb = new StringBuilder("*** J.A.R.V.I.S. MCOC Line Bot ***\n\n");
		sb.append("JARVIS Bot Line users group: ");
		sb.append(JARVIS_USERS_LINE_GROUP_LINK);
		sb.append("\n");
		sb.append("You can join the LINE group of JARVIS users to ask for help and play with the Bot.\n\n");
		sb.append("Version: ");
		sb.append(JarvisBotApplication.JARVIS_VERSION);
		sb.append("\n");
		sb.append("Author: slux83\n");
		sb.append("Issues tracker: https://github.com/slux83/jarvis-bot/issues\n\n");
		sb.append("Please cosider a small donation if you want to support the JARVIS Bot development!\n");
		sb.append("JARVIS Bot is hosted on a dedicated server and it has some running costs.\n\n");
		sb.append("PAYPAL: ");
		sb.append(PAYPAL_LINK.replace("$GUID", senderId));
		sb.append("\n");

		return new TextMessage(sb.toString());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.slux.line.jarvis.command.AbstractCommand#getType()
	 */
	@Override
	public CommandType getType() {
		return CommandType.CommandTypeUtility;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.slux.line.jarvis.command.AbstractCommand#getHelp()
	 */
	@Override
	public String getHelp() {
		StringBuilder sb = new StringBuilder();
		sb.append("[" + CMD_PREFIX + "]\n");
		sb.append("Prints informations about J.A.R.V.I.S. Line Bot, issue tracker, donations, etc...");

		return sb.toString();
	}
}
