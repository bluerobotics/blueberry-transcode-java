/*
 * Copyright (c) 2025 Blue Robotics
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.bluerobotics.blueberry.transcoder.java;

import java.util.HashMap;
import java.util.function.Function;


/**
 * 
 */
public abstract class BlueberryPacketConsumerManager {
	private final HashMap<Integer, Function<BlueberryBlock, BlueberryBlockParser>> m_consumers = new HashMap<Integer, Function<BlueberryBlock, BlueberryBlockParser>>();
	
	protected BlueberryPacketConsumerManager() {
		
	}
	
	protected void add(int key, Function<BlueberryBlock, BlueberryBlockParser> c) {
		m_consumers.put(key, c);
	}
	
	protected abstract int getKey(BlueberryBlock bb); 
	protected abstract int getLength(BlueberryBlock bb); 
	
	/**
	 * 
	 * @param p
	 * @param maxIndex
	 */
	public void processPacket(BlueberryPacket p, int maxIndex) {
		BlueberryBlock bb = p.getFirstBlock();//this will be the start of the packet
		
		
	}
	

}
