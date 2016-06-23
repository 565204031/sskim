package com.ssk.sskim.manager;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.packet.Presence;

public class SubscribeManager {

	/**
	 * 请求通过
	 * @param connection
	 */
	public static void subscribed(AbstractXMPPConnection connection,String jid){
		try {
			Presence p = new Presence(Presence.Type.subscribed);
			p.setTo(jid);
			connection.sendStanza(p);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 请求被拒绝
	 * @param connection
	 */
	public static void unsubscribe(AbstractXMPPConnection connection,String jid){
		try {
			Presence p = new Presence(Presence.Type.unsubscribe);
			p.setTo(jid);
			connection.sendStanza(p);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
