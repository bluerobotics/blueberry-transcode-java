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
import java.util.function.Consumer;


/**
 * an abstract class for consuming the blocks of a packet
 * T is the class that will consume each parsed block
 */
public abstract class BlueberryPacketConsumerManager<T> {
	private final HashMap<Integer, Consumer<BlueberryBlock>> m_consumers = new HashMap<Integer, Consumer<BlueberryBlock>>();
	private T m_parserConsumer;
	
	protected BlueberryPacketConsumerManager() {
		
	}
	
	protected void add(int key, Consumer<BlueberryBlock> c) {
		m_consumers.put(key, c);
	}
	
	protected abstract int getBlockKey(BlueberryBlock bb); 
	protected abstract int getBlockLength(BlueberryBlock bb); 
	protected abstract BlueberryBlock getFirstBlock(BlueberryPacket p);
	protected T getParserConsumer() {
		return m_parserConsumer;
	}
	protected void setParserConsumer(T c) {
		m_parserConsumer = c;
	}
	
	/**
	 * 
	 * @param p
	 * @param maxIndex
	 */
	public void processPacket(BlueberryPacket p) {
		//don't do anything if there's no parser consumer set
		if(getParserConsumer() == null) {
			return;
		}
		BlueberryBlock bb = getFirstBlock(p);//this will be the first block after the header
		while(bb.getCurrentWordIndex() < p.getWordLength()) {
			m_consumers.get(getBlockKey(bb)).accept(bb);
		}
		
		
	}
	

}
