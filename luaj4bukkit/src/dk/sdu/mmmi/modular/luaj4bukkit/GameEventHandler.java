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

import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

/**
 * Generic event dispatcher from Minecraft to Lua
 * @author ups
 *
 */
public class GameEventHandler implements Listener {
	
	/**
	 * The plugin that this event handler is attached to
	 */
	private LuaJ4BukkitPlugin plugin;
	
	/**
	 * Create event handler for given lua environment
	 * @param env the environment
	 */
	public GameEventHandler(LuaJ4BukkitPlugin plugin) {
		this.plugin = plugin;
	}
	
	/**
	 * Register an event to a given dispatcher
	 * @param eventType the type of the event
	 * @param handler lua closure that will be called with one argument, the event, whenever an event is dispatched
	 */
	public void register(Class<? extends Event> eventType, final LuaValue handler) {
		plugin.getServer().getPluginManager().registerEvent(eventType, this, EventPriority.NORMAL, new EventExecutor() {
			@Override
			public void execute(Listener listener, Event event) throws EventException {
				handler.call(CoerceJavaToLua.coerce(event));
			}
		}, plugin);
	}
}
