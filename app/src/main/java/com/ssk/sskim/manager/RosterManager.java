package com.ssk.sskim.manager;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.roster.Roster;

/**
 * 联系人列表管理器
 * @author Jason
 * QQ: 2904048107
 * @date 2015年8月10日
 * @version 1.0
 */
public class RosterManager {
	
	private static RosterManager instance;
	private Roster roster;
	
	private RosterManager() {
		
	}

	public static RosterManager getInstance(AbstractXMPPConnection connection){
		instance = new RosterManager();
		instance.roster = Roster.getInstanceFor(connection);
		return instance;
	}
	
	/**
     * Adds a new entry to the users Roster.
     *
     * @param jid      the jid.
     * @param nickname the nickname.
     * @param group    the contact group.
     * @return the new RosterEntry.
     */
    public void addEntry(String jid, String nickname, String group) {
    	try {
            roster.createEntry(jid, nickname, new String[]{group});
        }catch (Exception e) {
        	e.printStackTrace();
        }
    }
	
}
