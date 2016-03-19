package com.scarsz.discordsrv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.entities.TextChannel;

import org.bukkit.plugin.Plugin;

@SuppressWarnings("unchecked")
public class ServerLogWatcher extends Thread{
	JDA api;
	Plugin plugin;
    public ServerLogWatcher(JDA api, Plugin plugin){
        this.api = api;
        this.plugin = plugin;
    }
    
	public void run(){
		int rate = plugin.getConfig().getInt("DiscordConsoleChannelLogRefreshRate");
		String message = "";
		
		FileReader fr = null;
		try {
			fr = new FileReader(new File(new File(".").getAbsolutePath() + "/logs/latest.log").getAbsolutePath());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	    BufferedReader br = new BufferedReader(fr);
	    
	    Boolean done = false;
	    while (!done)
	    {
	    	String line = null;
			try {
				line = br.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (line == null) done = true;
	    }
	    
	    TextChannel channel = DiscordSRV.getChannel(plugin.getConfig().getString("DiscordConsoleChannelName"));
	    while (!isInterrupted())
	    {
	    	try {
			    if (channel == null) return;
		    	
		    	String line = null;
				try {
					line = br.readLine();
				} catch (IOException e) {
					e.printStackTrace();
				}
		    	if (line == null){
		    		if (message.length() > 0){
						DiscordSRV.sendMessage(channel, message);
						message = "";
					}
		    		try { Thread.sleep(rate); } catch (InterruptedException e) {}
		    		continue;
		    	}else{
		    		Boolean goodToSend = true;
		    		for (String phrase : (List<String>) plugin.getConfig().getList("DiscordConsoleChannelDoNotSendPhrases")) if (line.toLowerCase().contains(phrase.toLowerCase())) goodToSend = false;
		    		if (!goodToSend) continue;
		    		if (message.length() + line.length() + 2 <= 2000 && line.length() > 0){
		    			message += line + "\n";
		    		} else {
		    			DiscordSRV.sendMessage(channel, message);
		    			message = line + "\n";
		    		}
		    	}
	    	} catch (Exception e) {
	    		e.printStackTrace();
	    	}
	    }
	}
}