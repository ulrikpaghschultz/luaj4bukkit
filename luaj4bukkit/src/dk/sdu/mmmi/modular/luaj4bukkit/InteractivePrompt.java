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

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.luaj.vm2.LuaValue;

/**
 * Interactive prompt interfacing minecraft to a lua environment
 * @author ups
 *
 */
public class InteractivePrompt {
	
	/**
	 * Environment to use for evaluating global/player commands
	 */
	private LuaEnvironment luaEnvironment;
	
	/**
	 * The plugin that the prompt is used for
	 */
	private LuaJ4BukkitPlugin plugin;
	
	/**
	 * Create interactive prompt handler for the given plugin and environment
	 * @param plugin the plugin to attach to
	 * @param env the environment to use
	 */
	public InteractivePrompt(LuaJ4BukkitPlugin plugin, LuaEnvironment env) {
		this.plugin = plugin; this.luaEnvironment = env;
	}
	
	/**
	 * Command processing
	 */
	
	/**
	 * "lua" command: evaluate a single line of lua
	 * @return true
	 */
	public CommandExecutor getProcessorFor_lua() {
		return new CommandExecutor() {
			public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
				LuaValue env = luaEnvironment.getEnvironment(sender);
				// Rebuild lua command by concatenating arguments
				StringBuffer completeCommand = new StringBuffer();
				if(args.length==0) {
					plugin.getLogger().info("empty command");
					return true;
				}
				for(int i=0; i<args.length; i++) completeCommand.append(args[i]+' ');
				// Process command
				luaEnvironment.evaluateSingleCommand(sender, env, completeCommand.toString());
				return true;
			}
		};
	}
	
	/**
	 * "lua.load" command: load lua file from default directory
	 * @return true
	 */
	public CommandExecutor getProcessorFor_lua_load() {
		return new CommandExecutor() {
			public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
				LuaValue env = luaEnvironment.getEnvironment(sender);
				luaEnvironment.evaluateCommandsFromFile(sender, env, args[0]);
				return true;
			}
		};
	}

	/**
	 * "lua.reload" command: reload lua startup file
	 * @return true
	 */
	public CommandExecutor getProcessorFor_lua_reload() {
		return new CommandExecutor() {
			public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
				LuaValue env = luaEnvironment.getEnvironment(sender);
				luaEnvironment.evaluateCommandsFromFile(sender, env, plugin.getConfig_startup_file());
				return true;
			}
		};
	}

}
