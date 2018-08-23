/**
 * 
 */
package de.slux.line.jarvis.command.admin;

import java.text.DecimalFormat;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.message.TextMessage;

import de.slux.line.jarvis.JarvisBotApplication;
import de.slux.line.jarvis.command.AbstractCommand;
import de.slux.line.jarvis.war.WarReportModel;

/**
 * This command is triggered for information
 * 
 * @author slux
 */
public class AdminStatusCommand extends AbstractCommand {
	public static final String CMD_PREFIX = "jarvis status";
	private static Logger LOG = LoggerFactory.getLogger(AdminStatusCommand.class);
	private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.000"); 
	/**
	 * Ctor
	 * 
	 * @param messagingClient
	 */
	public AdminStatusCommand(LineMessagingClient messagingClient) {
		super(messagingClient);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.slux.line.jarvis.command.AbstractCommand#canTrigger(java.lang.String)
	 */
	@Override
	public boolean canTrigger(String message) {
		return message.toLowerCase().startsWith(CMD_PREFIX);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.slux.line.jarvis.command.AbstractCommand#execute(java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public TextMessage execute(String userId, String senderId, String message) {
		StringBuilder sb = new StringBuilder("*** J.A.R.V.I.S. MCOC Line Bot Status ***\n\n");
		
		// Check if we need to change the status
		List<String> args = super.extractArgs(message);
		if (args.size() == 3) {
			String newStatus = args.get(2).trim();
			
			if (newStatus.equalsIgnoreCase("operational")) {
				JarvisBotApplication.getInstance().getIsOperational().set(true);
			} else if (newStatus.equalsIgnoreCase("maintenance")) {
				JarvisBotApplication.getInstance().getIsOperational().set(false);
			} else {
				sb.append("WARNING: submitted unknown status ");
				sb.append(newStatus);
				sb.append("\n");
			}
		}
		
		sb.append("Version: ");
		sb.append(JarvisBotApplication.JARVIS_VERSION);
		sb.append("\n");
		sb.append("Status: ");
		sb.append(JarvisBotApplication.getInstance().getIsOperational().get()? "OPERATIONAL" : "MAINTENANCE");
		sb.append("\n");
		long msgCounter = JarvisBotApplication.getInstance().getIncomingMsgCounter();
		long startupMs = JarvisBotApplication.getInstance().getStartup().getTime();
		long nowMs = System.currentTimeMillis();
		long msDiff = Math.abs(nowMs - startupMs);
		double msgSec = (msgCounter / (msDiff/1000.0));
		sb.append("Messages/sec: ");
		sb.append(DECIMAL_FORMAT.format(msgSec));
		sb.append("\n");
		
		sb.append("Total groups: ");
		WarReportModel model = new WarReportModel();
		int groupCounter = -1;
		try {
			groupCounter = model.getAllGroups().size();
		} catch (Exception e) {
			LOG.error("Unexpected error: " + e, e);
		}
		sb.append(groupCounter != -1 ? Integer.toString(groupCounter) : "unknown");
		sb.append("\n");

		LOG.info("Messages/sec: " + DECIMAL_FORMAT.format(msgSec));
		LOG.info("Total groups: " + groupCounter);
		LOG.info("Status: " + (JarvisBotApplication.getInstance().getIsOperational().get()? "OPERATIONAL" : "MAINTENANCE"));
		
		return new TextMessage(sb.toString());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.slux.line.jarvis.command.AbstractCommand#getType()
	 */
	@Override
	public CommandType getType() {
		return CommandType.CommandTypeAdmin;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.slux.line.jarvis.command.AbstractCommand#getHelp()
	 */
	@Override
	public String getHelp() {
		StringBuilder sb = new StringBuilder();
		sb.append("[" + CMD_PREFIX + "<(maintenance|operational)?>]\n");
		sb.append("Prints the current status of JARVIS. Set JARVIS new status (maintenance|operational)");

		return sb.toString();
	}
}
