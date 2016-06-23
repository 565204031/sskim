package com.ssk.sskim.manager;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration.Builder;

/**
 * XMPPConnection连接管理器
 */
public class ConnectionManager {

	private static AbstractXMPPConnection connection;
	
	//服务器的IP地址
	private static String host = "192.168.0.103";
	//服务Openfire的端口号
	private static int port = 5222;
	//服务器名称（本地计算机的名称、网站域名）
	private static String serviceName = "user-20160302ag";
	
	/**
	 * 获取连接对象
	 * @return
	 */
	public static AbstractXMPPConnection getConnection(){
		if (connection == null) {
			openConnection();
		}
		return connection;
	}

	/**
	 * 打开连接
	 */
	public static void openConnection() {
		//设置连接的配置
		Builder builder = XMPPTCPConnectionConfiguration.builder();
		builder.setHost(host);
		builder.setPort(port);
		builder.setServiceName(serviceName);
		builder.setSecurityMode(SecurityMode.disabled);
		builder.setDebuggerEnabled(true);
		connection = new XMPPTCPConnection(builder.build());
		try {
			connection.connect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 关闭连接
	 */
	public static void release(){
		if (connection != null) {
			connection.disconnect();
		}
	}
	
}
