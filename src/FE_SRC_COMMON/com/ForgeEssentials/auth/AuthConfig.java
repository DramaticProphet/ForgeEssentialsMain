package com.ForgeEssentials.auth;

import java.io.File;

import net.minecraft.command.ICommandSender;
import net.minecraftforge.common.Configuration;

import com.ForgeEssentials.auth.lists.PlayerTracker;
import com.ForgeEssentials.core.moduleLauncher.ModuleConfigBase;

public class AuthConfig extends ModuleConfigBase
{
	private Configuration		config;
	private static final String	CATEGORY_MAIN	= "main";

	public AuthConfig(File file)
	{
		super(file);
	}

	@Override
	public void init()
	{
		config = new Configuration(file);

		config.addCustomCategoryComment("main", "all the main important stuff");
		ModuleAuth.forceEnabled = config.get(CATEGORY_MAIN, "forceEnable", false, "Forces the authentication server to be loaded regardless of Minecraft auth services").getBoolean(false);
		ModuleAuth.checkVanillaAuthStatus = config.get(CATEGORY_MAIN, "autoEnable", true, "Enables authentication server if and when the Minecraft Auth servers go down.").getBoolean(false);
		ModuleAuth.allowOfflineReg = config.get(CATEGORY_MAIN, "allowOfflineReg", false, "Allows people to register usernames while server is offline. Don't allow this for primarily Online servers.").getBoolean(false);
		ModuleAuth.salt = config.get(CATEGORY_MAIN, "salt", ModuleAuth.salt, "The salt to be used when hashing passwords").getString();
		ModuleAuth.checkInterval = config.get(CATEGORY_MAIN, "checkInterval", 10, "Interval to check Vanill Auth service. In minutes.").getInt();
		
		config.addCustomCategoryComment("lists", "Alternative ban/whitelist/VIP/max players implementation. Make sure vipslots and offset added together is less than the amount of players specified in server.properties.");
		PlayerTracker.offset = config.get("lists", "offset", 0, "If you need to be able to have less than the amount of players specified in server.properties logged into your server, use this.").getInt();
		PlayerTracker.vipslots = config.get("lists", "vipslots", 0, "Amount of slots reserved for VIP players.").getInt();
		PlayerTracker.whitelist = config.get("lists", "whitelistEnabled", false, "Enable or disable the ForgeEssentials whitelist. Note that server.properties will be used if this is set to false.").getBoolean(false);
		
		config.addCustomCategoryComment("lists.kickmsg", "Kick messages for banned/unwhitelisted players or when the server is full (not counting VIP slots");
		PlayerTracker.banned = config.get("lists.kick", "bannedmsg", "You have been banned from this server.").getString();
		PlayerTracker.notwhitelisted = config.get("lists.kick", "unwhitelistedmsg", "You are not whitelisted on this server.").getString();
		PlayerTracker.notvip = config.get("lists.kick", "notVIPmsg", "This server is full, and you are not a VIP.").getString();
		config.save();
	}

	@Override
	public void forceSave()
	{
		config.get(CATEGORY_MAIN, "forceEnable", false, "Forces the authentication server to be loaded regardless of Minecraft auth services").set(ModuleAuth.forceEnabled);
		config.get(CATEGORY_MAIN, "autoEnable", true, "Enables the authentication server if and when the Minecraft Auth servers go down.").set(ModuleAuth.checkVanillaAuthStatus);
		config.get(CATEGORY_MAIN, "allowOfflineReg", false, "Allow registration while server is offline. Don't allow this.").set(ModuleAuth.allowOfflineReg);
		config.get(CATEGORY_MAIN, "salt", "", "The salt to be used when hashing passwords").set(ModuleAuth.salt);
		config.get(CATEGORY_MAIN, "chcekInterval", "", "Interval to check Vanill Auth service. In minutes.").set(ModuleAuth.checkInterval);

		config.get("lists", "offset", 0, "If you need to be able to have less than the amount of players specified in server.properties logged into your server, use this.").set(0);
		config.get("lists", "vipslots", 0, "Amount of slots reserved for VIP players.").set(0);
		config.get("lists", "whitelistEnabled", false, "Enable or disable the ForgeEssentials whitelist. Note that server.properties will be used if this is set to false.").set(false);
		
		config.get("lists.kick", "bannedmsg", "You have been banned from this server.").set("You have been banned from this server.");
		config.get("lists.kick", "unwhitelistedmsg", "You are not whitelisted on this server.").set("You are not whitelisted on this server.");
		config.get("lists.kick", "notVIPmsg", "This server is full, and you are not a VIP.").set("This server is full, and you are not a VIP.");

		config.save();
	}

	@Override
	public void forceLoad(ICommandSender sender)
	{
		config.load();
		ModuleAuth.forceEnabled = config.get(CATEGORY_MAIN, "forceEnable", false).getBoolean(false);
		ModuleAuth.checkVanillaAuthStatus = config.get(CATEGORY_MAIN, "autoEnable", true).getBoolean(false);
		ModuleAuth.allowOfflineReg = config.get(CATEGORY_MAIN, "allowOfflineReg", false).getBoolean(false);
		ModuleAuth.salt = config.get(CATEGORY_MAIN, "salt", ModuleAuth.salt).getString();
		ModuleAuth.checkInterval = config.get(CATEGORY_MAIN, "checkInterval", 10).getInt();

		PlayerTracker.offset = config.get("lists", "offset", 0).getInt();
		PlayerTracker.vipslots = config.get("lists", "vipslots", 0).getInt();
		PlayerTracker.whitelist = config.get("lists", "whitelistEnabled", false).getBoolean(false);
		
		PlayerTracker.banned = config.get("lists.kick", "bannedmsg", "You have been banned from this server.").getString();
		PlayerTracker.notwhitelisted = config.get("lists.kick", "unwhitelistedmsg", "You are not whitelisted on this server.").getString();
		PlayerTracker.notvip = config.get("lists.kick", "notVIPmsg", "This server is full, and you are not a VIP.").getString();
	}

}
