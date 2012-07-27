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

import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;


public class LuaJ4BukkitPlugin extends JavaPlugin {

	/**
	 * Singleton accessor for getting reference to plugin where needed (classes not controlled by plugin)
	 * @return
	 */
	public static LuaJ4BukkitPlugin getInstance() {
		if(instance==null) throw new Error("plugin not enabled");
		return instance;
	}
	
	/**
	 * Singleton storage 
	 */
	private static LuaJ4BukkitPlugin instance;
	
	/**
	 * The event handler instance
	 */
	private GameEventHandler eventHandler;
	
	/**
	 * Obtain event handler used for routing events from bukkit to lua
	 * @return event handler instance
	 */
	public GameEventHandler getEventHandler() {
		return eventHandler;
	}
	
	/**
	 * Initialize resources
	 */
	@Override
	public void onEnable() {
		instance = this;
		loadConfiguration();
		// Initialize lua environment manager
		LuaEnvironment env = new LuaEnvironment(this);
		InteractivePrompt prompt = new InteractivePrompt(this,env);
		// Instantiate event handler
		eventHandler = new GameEventHandler(this);
		// Set up commands & ready
		getCommand("lua").setExecutor(prompt.getProcessorFor_lua());
		getCommand("lua.load").setExecutor(prompt.getProcessorFor_lua_load());
		getCommand("lua.reload").setExecutor(prompt.getProcessorFor_lua_reload());
		getLogger().info("WARNING: luaj4bukkit plugin enabled, enables arbitrary code execution and arbitrary file access!");
	}

	/**
	 * Clean up resources
	 */
	@Override
	public void onDisable() {
		eventHandler = null;
		HandlerList.unregisterAll(this);
		instance = null;
	}

	/**
	 * Load configuration
	 */
	private void loadConfiguration() {
		config_path_prefix = getConfig().getString(CONFIG_PATH_PREFIX);
		config_startup_file = getConfig().getString(CONFIG_STARTUP_FILE);
		config_load_startup = getConfig().getBoolean(CONFIG_LOAD_STARTUP);
		config_load_internal = getConfig().getBoolean(CONFIG_LOAD_INTERNAL);
		config_internal_defs = getConfig().getString(CONFIG_INTERNAL_DEFS);
	}

	/**
	 * Configuration data
	 */
	
	/**
	 * Configuration: prefix inserted before loading any lua files
	 */
	public String getConfig_path_prefix() {
		return config_path_prefix;
	}

	/**
	 * Configuration: name of the startup file to load
	 */
	public String getConfig_startup_file() {
		return config_startup_file;
	}

	/**
	 * Configuration: should the startup file be loaded in each environment?
	 */
	public boolean isConfig_load_startup() {
		return config_load_startup;
	}

	/**
	 * Configuration: should an internally defined class (e.g., minecraft.*) bo laoded into each environment?
	 */
	public boolean isConfig_load_internal() {
		return config_load_internal;
	}

	/**
	 * Configuration: name of Java class providing internal definitions
	 */
	public String getConfig_internal_defs() {
		return config_internal_defs;
	}

	/**
	 * Configuration data
	 */
	private String config_path_prefix;
	private String config_startup_file;
	private boolean config_load_startup;
	private boolean config_load_internal;
	private String config_internal_defs;

	/**
	 * Configuration key names
	 */
	private static final String CONFIG_PATH_PREFIX = "files.default_path_prefix";
	private static final String CONFIG_STARTUP_FILE = "startup.init_file_name";
	private static final String CONFIG_LOAD_STARTUP = "startup.load_init_file";
	private static final String CONFIG_LOAD_INTERNAL = "startup.load_internal_definitions";
	private static final String CONFIG_INTERNAL_DEFS = "startup.internal_definitions";
}