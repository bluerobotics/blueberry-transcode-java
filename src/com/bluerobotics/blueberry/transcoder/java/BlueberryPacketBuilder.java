/*

Copyright (c) 2025  Blue Robotics North Inc.

Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the "Software"), to deal in
the Software without restriction, including without limitation the rights to
use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
of the Software, and to permit persons to whom the Software is furnished to do
so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/
package com.bluerobotics.blueberry.transcoder.java;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An abstract class to base a builder class on.
 * The builder is used to construct a packet for transmission
 */
public class BlueberryPacketBuilder {
	public interface BlueberryBuilder {
		void buildMessage(BlueberryBuffer bb);
	}
	private BlueberryPacket m_packet;
	private final int m_maxByteCount;
	private ConcurrentHashMap<Integer, BlueberryBuilder> m_builders = new ConcurrentHashMap<>(); 
	private ConcurrentHashMap<Integer, String> m_builderNames = new ConcurrentHashMap<>();
	public BlueberryPacketBuilder(int maxByteCount) {
		m_maxByteCount = maxByteCount;
		reset();
	}
	/**
	 * Note that this doesn't generate the first current block
	 * It is expected that subclasses will override this so that it does
	 * If not, then the current block will never be non-null and exceptions will occur
	 */
	public void reset() {
		m_packet = BlueberryPacket.makeForTransmit(m_maxByteCount);
		
		
	
		
	}
	


	
	/**
	 * get the packet that was just constructed
	 * @return
	 */
	public BlueberryPacket getPacket() {
		return m_packet;
	}
	/**
	 * adds a new builder for the specified message key
	 * @param key
	 * @param b
	 */
	public void addMessageBuilder(int key, BlueberryBuilder b) {
		m_builders.put(key, b);
	}
	
	public void addMessageBuilder(int key, String name, BlueberryBuilder b) {
		addMessageBuilder(key, b);
		m_builderNames.put(key, name);
	}
	/**
	 * triggers the bulid of the message identified by the specified key
	 * There must be a builder registered against the specified key for this to have any effect
	 * @param keys - the messages to add to the packet
	 */
	public BlueberryPacket build(boolean crc, int... keys) {
		start();
		
		addTo(keys);
		return finish(crc);
	}
	public void start() {
		reset();
		m_packet.setupHeader();
	}
	public void addTo(int... keys) {
		for(int k : keys) {
			BlueberryBuilder b = m_builders.get(k);
			if(b != null) {
				b.buildMessage(m_packet.getNextMessageBuffer());
			}
		}
	}
	/**
	 * finishes any last items in the packet, like finalizing the length, computing crc, etc.
	 * This method will be implemented by subclasses
	 * It does not need to be called, it is called as part of the getPacket() method
	 * @param omputeCrc - indicates whether the CRC should be computed for the paket header.
	 */
	public BlueberryPacket finish(boolean crc) {
		m_packet.complete(crc);
		return m_packet;
	}
	
	public Iterator<Integer> getKeys(){
		return m_builders.keys().asIterator();
	}
	public String getBuilderName(int key) {
		String  result = m_builderNames.get(key);
		if(result == null) {
			result = Integer.toHexString(key);
			int n = 8 - result.length();
			if(n < 0) {
				n = 0;
			}
			result = "0".repeat(n);
		}
		return "0x"+result;
	}
}
