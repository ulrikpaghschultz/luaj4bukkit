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

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.CoerceLuaToJava;

/**
 * Internally loaded minecraft Lua library, providing various functionalities from Java
 * that are convenient for Lua programming
 * @author ups
 *
 */
public class LuaMinecraftLib extends OneArgFunction {

	/**
	 * Must have default constructor (redundant to put it, but just to remember, does no harm)
	 */
	public LuaMinecraftLib() { }

	/**
	 * One-arg calls: cleverly used for initialization (opcode 0) and one-arg calls to 
	 * library (opcodes 1 and up)
	 */
	@Override
	public LuaValue call(LuaValue arg) {
		switch(opcode) {
		case 0: { // initialize function table
			LuaValue dispatchTable = tableOf();
			this.bind(dispatchTable, LuaMinecraftLib.class, new String[] { "classForName" }, 1 );
			this.bind(dispatchTable, TwoArgDispatch.class, new String[] { "handleEvent", "displayString" }, 1 );
			env.set("minecraft", dispatchTable);
			return dispatchTable;
		}
		case 1: // classForName: convenience shortcut for Class.forName
			try {
				return CoerceJavaToLua.coerce(Class.forName(arg.checkjstring()));
			} catch (ClassNotFoundException e) {
				throw new LuaError("Illegal Java class name: not found");
			}
		default: return error("bad opcode: "+opcode);
		}
	}	
	
	/**
	 * Two-arg calls
	 * @author ups
	 *
	 */
	public static class TwoArgDispatch extends TwoArgFunction {

		@Override
		public LuaValue call(LuaValue arg1, LuaValue arg2) {
			switch(opcode) {
			case 1: { // handleEvent: use GameEventHandler for given type (string class name) to call handler (one-arg closure)
				String eventClassName = arg1.checkjstring();
				try {
					Class<? extends Event> eventClass = (Class<Event>)Class.forName(eventClassName);
					if(!Event.class.isAssignableFrom(eventClass)) throw new Error("Illegal event class type, not Event subtype");
					LuaJ4BukkitPlugin.getInstance().getEventHandler().register(eventClass,arg2);
					return this;
				} catch (ClassNotFoundException e) {
					throw new LuaError("Illegal event class name, not found: "+eventClassName);
				}
			}
			case 2: { // print: a print function with output to the player (arg1), if any, of text string (arg2)
				LuaEnvironment env = LuaJ4BukkitPlugin.getInstance().getLuaEnvironment(); // has display message function
				CommandSender receiver = null;
				if(arg1!=LuaValue.NIL) {
					receiver = (Player) CoerceLuaToJava.coerce(arg1, Player.class);
				}
				env.displayMessage(receiver, arg2.tojstring());
				return this;
			}
			default: return error("bad opcode: "+opcode);
			}
		}
		
	}
	
}


