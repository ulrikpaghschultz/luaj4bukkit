/*******************************************************************************
 * Copyright (c) 2012 University of Southern Denmark. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ******************************************************************************/

package dk.sdu.mmmi.modular.luaj4bukkit;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.JsePlatform;

/**
 * The collective lua environment: global and per-player, with a means of looking up a specific
 * environment and reporting errors.
 * @author ups
 *
 */
public class LuaEnvironment {

	/**
	 * Global interactive environment accessible at the server prompt
	 */
	private LuaValue globalEnvironment;
	
	/**
	 * Per-player interactive environments
	 */
	private Map<Player,LuaValue> playerEnvironments = new HashMap<Player,LuaValue>();
	
	/**
	 * Owner plugin
	 */
	private LuaJ4BukkitPlugin plugin;

	/**
	 * Create a command processor for the plugin
	 * @param plugin the owner plugin
	 */
	public LuaEnvironment(LuaJ4BukkitPlugin plugin) {
		this.plugin = plugin;
		globalEnvironment = createLuaEnvironment(null);
		if(globalEnvironment==null) plugin.getLogger().severe("initialization failed");
	}

	/**
	 * Create a lua environment for a given sender (errors are sent back to the sender), initialize
	 * according to options, loading internal class and startup file if appropriate
	 * @param sender where to report errors
	 * @return the newly initialized environment
	 */
	private LuaValue createLuaEnvironment(CommandSender sender) {
		LuaValue env = JsePlatform.standardGlobals();
		if(plugin.isConfig_load_internal()) {
			String className = plugin.getConfig_internal_defs();
			Class<LuaValue> internal;
			try {
				internal = (Class<LuaValue>)Class.forName(className);
				if(!LuaValue.class.isAssignableFrom(internal)) throw new Error("Illegal internal class, must subclass LuaValue");
				env.load(internal.newInstance());
			} catch (ClassNotFoundException e) {
				throw new Error("Could not locate internally loaded class "+className);
			} catch (InstantiationException e) {
				throw new Error("Internally loaded class cannot be instantiated, missing default constructor");
			} catch (IllegalAccessException e) {
				throw new Error("Internally loaded class cannot be instantiated, default constructor not accessible");
			}
		}
		if(plugin.isConfig_load_startup())
			evaluateCommandsFromFile(sender, env, plugin.getConfig_startup_file());
		return env;
	}

	/**
	 * Get the environment to use for a given command sender
	 * @param sender the command sender
	 * @return the environment matching the sender: global unless something more specific applies
	 */
	public LuaValue getEnvironment(CommandSender sender) {
		LuaValue env;
		// Global or player environment?
		if(sender instanceof Player) {
			env = getPlayerEnvironment((Player)sender);
		} else {
			env = globalEnvironment;
			globalEnvironment.set("server", CoerceJavaToLua.coerce(sender.getServer()));
		}
		return env;
	}

	/**
	 * Get the lua environment for the interactive prompt for the specific player
	 * @param sender the player
	 * @return the interactive environment for that player, with variables set useful values
	 */
	public synchronized LuaValue getPlayerEnvironment(Player sender) {
		// Get the unique per-player environment
		LuaValue env = playerEnvironments.get(sender);
		if(env==null) {
			// Not created yet for this player, create and save
			env = createLuaEnvironment(sender);
			env.set("player", CoerceJavaToLua.coerce(sender));
			playerEnvironments.put(sender,env);
		}
		env.set("location", CoerceJavaToLua.coerce(sender.getLocation()));
		env.set("world", CoerceJavaToLua.coerce(sender.getLocation().getWorld()));
		return env;
	}

	public void evaluateSingleCommand(CommandSender sender, LuaValue env, String completeCommand) {
		try {
			LuaValue closure = env.get("loadstring").call(LuaValue.valueOf(completeCommand));
			if(closure.isnil())
				reportError(sender,"Error parsing lua command");
			else {
				LuaValue result = closure.call();
			}
		} catch(LuaError error) {
			reportError(sender,error.getMessage());
		}
	}

	public void evaluateCommandsFromFile(CommandSender sender, LuaValue env, String fileName) {
		try {
			LuaValue result = env.get("dofile").call(LuaValue.valueOf(plugin.getConfig_path_prefix()+fileName));
		} catch(LuaError error) {
			reportError(sender,error.getMessage());
		}
	}

	/**
	 * Report an error from evaluating a Lua command
	 * @param sender who the error should be report to
	 * @param message the message to report
	 */
	private void reportError(CommandSender sender, String message) {
		if(sender instanceof Player) {
			Player player = (Player)sender;
			player.sendMessage("Error evaluating lua command: "+message);
		} else {
			plugin.getLogger().info("Lua error: "+message);
		}
	}

}
