package com.ForgeEssentials.core.moduleLauncher;

import com.ForgeEssentials.core.ForgeEssentials;
import com.ForgeEssentials.core.moduleLauncher.FEModule.Init;
import com.ForgeEssentials.core.moduleLauncher.FEModule.PostInit;
import com.ForgeEssentials.core.moduleLauncher.FEModule.Reload;
import com.ForgeEssentials.core.moduleLauncher.FEModule.ServerInit;
import com.ForgeEssentials.core.moduleLauncher.FEModule.ServerPostInit;
import com.ForgeEssentials.core.moduleLauncher.FEModule.ServerStop;
import com.ForgeEssentials.core.moduleLauncher.FEModule.container;
import com.ForgeEssentials.core.moduleLauncher.FEModule.instance;
import com.ForgeEssentials.core.moduleLauncher.event.FEModuleInitEvent;
import com.ForgeEssentials.core.moduleLauncher.event.FEModulePostInitEvent;
import com.ForgeEssentials.core.moduleLauncher.event.FEModulePreInitEvent;
import com.ForgeEssentials.core.moduleLauncher.event.FEModuleServerInitEvent;
import com.ForgeEssentials.core.moduleLauncher.event.FEModuleServerPostInitEvent;
import com.ForgeEssentials.core.moduleLauncher.event.FEModuleServerStopEvent;
import com.ForgeEssentials.util.OutputHandler;

import net.minecraft.command.ICommandSender;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.google.common.base.Throwables;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;

public class ModuleContainer
{
	private Object			module, mod;

	// methods..
	private Method			preinit, init, postinit, serverinit, serverpostinit, serverstop, reload;

	// fields
	private Field			instance, container;

	// other vars..
	public final String		name;
	public final boolean	isCore;
	private boolean			isLoadable;

	public ModuleContainer(Class c)
	{
		// checks original FEModule annotation.
		assert c.isAnnotationPresent(FEModule.class) : new IllegalArgumentException(c.getName() + " doesn't have the @FEModule annotation!");
		FEModule annot = (FEModule) c.getAnnotation(FEModule.class);
		name = annot.name();
		isCore = annot.isCore();
		mod = annot.parentMod();

		// check method annotations. they are all optional...
		Class[] params;
		for (Method m : c.getDeclaredMethods())
		{
			if (m.isAnnotationPresent(ServerStop.class))
			{
				assert preinit == null : new RuntimeException("Only one class may be marked as ServerStop");
				params = m.getParameterTypes();
				assert params.length == 1 : new RuntimeException(m + " may only have 1 argument!");
				assert params[0].equals(FEModuleServerStopEvent.class) : new RuntimeException(m + " must take " + FEModuleServerStopEvent.class.getSimpleName() + " as a param!");
				m.setAccessible(true);
				preinit = m;
			}
			else if (m.isAnnotationPresent(Init.class))
			{
				assert init == null : new RuntimeException("Only one class may be marked as Init");
				params = m.getParameterTypes();
				assert params.length == 1 : new RuntimeException(m + " may only have 1 argument!");
				assert params[0].equals(FEModuleInitEvent.class) : new RuntimeException(m + " must take " + FEModuleInitEvent.class.getSimpleName() + " as a param!");
				m.setAccessible(true);
				init = m;
			}
			else if (m.isAnnotationPresent(PostInit.class))
			{
				assert postinit == null : new RuntimeException("Only one class may be marked as PostInit");
				params = m.getParameterTypes();
				assert params.length == 1 : new RuntimeException(m + " may only have 1 argument!");
				assert params[0].equals(FEModulePostInitEvent.class) : new RuntimeException(m + " must take " + FEModulePostInitEvent.class.getSimpleName() + " as a param!");
				m.setAccessible(true);
				postinit = m;
			}
			else if (m.isAnnotationPresent(ServerInit.class))
			{
				assert serverinit == null : new RuntimeException("Only one class may be marked as ServerInit");
				params = m.getParameterTypes();
				assert params.length == 1 : new RuntimeException(m + " may only have 1 argument!");
				assert params[0].equals(FEModuleServerInitEvent.class) : new RuntimeException(m + " must take " + FEModuleServerInitEvent.class.getSimpleName() + " as a param!");
				m.setAccessible(true);
				serverinit = m;
			}
			else if (m.isAnnotationPresent(ServerPostInit.class))
			{
				assert serverpostinit == null : new RuntimeException("Only one class may be marked as ServerPostInit");
				params = m.getParameterTypes();
				assert params.length == 1 : new RuntimeException(m + " may only have 1 argument!");
				assert params[0].equals(FEModuleServerPostInitEvent.class) : new RuntimeException(m + " must take " + FEModuleServerPostInitEvent.class.getSimpleName() + " as a param!");
				m.setAccessible(true);
				serverpostinit = m;
			}
			else if (m.isAnnotationPresent(ServerStop.class))
			{
				assert serverstop == null : new RuntimeException("Only one class may be marked as ServerStop");
				params = m.getParameterTypes();
				assert params.length == 1 : new RuntimeException(m + " may only have 1 argument!");
				assert params[0].equals(FEModuleServerStopEvent.class) : new RuntimeException(m + " must take " + FEModuleServerStopEvent.class.getSimpleName() + " as a param!");
				m.setAccessible(true);
				serverstop = m;
			}
			else if (m.isAnnotationPresent(Reload.class))
			{
				assert reload == null : new RuntimeException("Only one class may be marked as Reload");
				params = m.getParameterTypes();
				assert params.length == 1 : new RuntimeException(m + " may only have 1 argument!");
				assert params[0].equals(ICommandSender.class) : new RuntimeException(m + " must take " + ICommandSender.class.getSimpleName() + " as a param!");
				m.setAccessible(true);
				reload = m;
			}
		}

		// collect field annotations... these are also optional.
		for (Field f : c.getDeclaredFields())
		{
			if (f.isAnnotationPresent(instance.class))
			{
				f.setAccessible(true);
				instance = f;
			}
			else if (f.isAnnotationPresent(container.class))
			{
				assert f.getType().equals(ModuleContainer.class) : new RuntimeException("This field must have the type ModuleContainer!");
				f.setAccessible(true);
				container = f;
			}
		}
	}

	protected void createAndPopulate(Class c)
	{
		// instantiate.
		try
		{
			module = c.newInstance();
		}
		catch (Exception e)
		{
			OutputHandler.SOP(name + " could not be instantiated. FE will not load this module.");
			e.printStackTrace();
			isLoadable = false;
			return;
		}

		// now for the fields...
		try
		{
			instance.set(module, module);
			container.set(module, this);
		}
		catch (Exception e)
		{
			OutputHandler.SOP("Error populating fields of " + name);
			Throwables.propagate(e);
		}
	}

	// make the methods to run the events now...

	public void runPreInit(FMLPreInitializationEvent fmlEvent)
	{
		if (!isLoadable)
			return;

		FEModulePreInitEvent event = new FEModulePreInitEvent(this, fmlEvent);
		try
		{
			preinit.invoke(module, event);
		}
		catch (Exception e)
		{
			OutputHandler.SOP("Error with invoking preInit event for " + name);
			Throwables.propagate(e);
		}
	}

	public void runInit(FMLInitializationEvent fmlEvent)
	{
		if (!isLoadable)
			return;

		FEModuleInitEvent event = new FEModuleInitEvent(this, fmlEvent);
		try
		{
			init.invoke(module, event);
		}
		catch (Exception e)
		{
			OutputHandler.SOP("Error with invoking Init event for " + name);
			Throwables.propagate(e);
		}
	}

	public void runPostInit(FMLPostInitializationEvent fmlEvent)
	{
		if (!isLoadable)
			return;

		FEModulePostInitEvent event = new FEModulePostInitEvent(this, fmlEvent);
		try
		{
			postinit.invoke(module, event);
		}
		catch (Exception e)
		{
			OutputHandler.SOP("Error with invoking PostInit event for " + name);
			Throwables.propagate(e);
		}
	}

	public void runServerInit(FMLServerStartingEvent fmlEvent)
	{
		if (!isLoadable)
			return;

		FEModuleServerInitEvent event = new FEModuleServerInitEvent(this, fmlEvent);
		try
		{
			serverinit.invoke(module, event);
		}
		catch (Exception e)
		{
			OutputHandler.SOP("Error with invoking ServerInit event for " + name);
			Throwables.propagate(e);
		}
	}

	public void runServerPostInit(FMLServerStartedEvent fmlEvent)
	{
		if (!isLoadable)
			return;

		FEModuleServerPostInitEvent event = new FEModuleServerPostInitEvent(this, fmlEvent);
		try
		{
			serverpostinit.invoke(module, event);
		}
		catch (Exception e)
		{
			OutputHandler.SOP("Error with invoking ServerPostInit event for " + name);
			Throwables.propagate(e);
		}
	}

	public void runServerStop(FMLServerStoppingEvent fmlEvent)
	{
		if (!isLoadable)
			return;

		FEModuleServerStopEvent event = new FEModuleServerStopEvent(this, fmlEvent);
		try
		{
			serverstop.invoke(module, event);
		}
		catch (Exception e)
		{
			OutputHandler.SOP("Error with invoking ServerStop event for " + name);
			Throwables.propagate(e);
		}
	}

	public File getModuleDir()
	{
		return new File(ForgeEssentials.FEDIR, name);
	}
}