package com.ForgeEssentials.core;

import java.util.Random;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import com.ForgeEssentials.api.APIRegistry;
import com.ForgeEssentials.core.misc.LoginMessage;
import com.ForgeEssentials.util.FunctionHelper;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.IPlayerTracker;

public class PlayerTracker implements IPlayerTracker
{
	public static boolean		KickForVIP;
	public static int			VIPslots;
	public static String		kickMessage;
	public static final String	PERMISSION	= "ForgeEssentials.vip";

	@Override
	public void onPlayerLogin(EntityPlayer player)
	{
		PlayerInfo.getPlayerInfo(player.username);
		LoginMessage.sendLoginMessage(player);

		if (KickForVIP)
		{
			if (FMLCommonHandler.instance().getMinecraftServerInstance().getAllUsernames().length + VIPslots > FMLCommonHandler.instance().getMinecraftServerInstance().getMaxPlayers())
			{
				if (!APIRegistry.perms.checkPermAllowed(player, PERMISSION))
				{
					((EntityPlayerMP) player).playerNetServerHandler.kickPlayerFromServer(kickMessage);
				}
			}

			while (FMLCommonHandler.instance().getMinecraftServerInstance().getAllUsernames().length + VIPslots > FMLCommonHandler.instance().getMinecraftServerInstance().getMaxPlayers())
			{
				EntityPlayerMP player2 = FunctionHelper.getPlayerForName(FMLCommonHandler.instance().getMinecraftServerInstance().getAllUsernames()[new Random().nextInt(FMLCommonHandler.instance().getMinecraftServerInstance()
						.getAllUsernames().length)]);

				if (!APIRegistry.perms.checkPermAllowed(player2, PERMISSION))
				{
					player2.playerNetServerHandler.kickPlayerFromServer(kickMessage);
				}
			}
		}
	}

	@Override
	public void onPlayerLogout(EntityPlayer player)
	{
		PlayerInfo.discardInfo(player.username);
	}

	@Override
	public void onPlayerChangedDimension(EntityPlayer player)
	{
		// Not sure if we need to do anything here.
	}

	@Override
	public void onPlayerRespawn(EntityPlayer player)
	{
		// Not sure if we need to do anything here.
	}
}
