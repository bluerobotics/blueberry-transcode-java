/*
Copyright (c) 2024  Blue Robotics

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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * 
 */
public class BlueberryBlock {
	private final int m_start;
	private final ByteBuffer m_buf;
	/**
	 * wraps the supplied buffer in a block.
	 * Buffer position assumed to be the start of this block
	 * @param bb - a ByteBuffer either with a received packet or to be filled with a packet to transmit
	 */
	public BlueberryBlock(ByteBuffer bb) {
		m_buf = bb;
		m_start = bb.position();
		m_buf.order(ByteOrder.LITTLE_ENDIAN);
	}
	
	public void writeFloat(int i, double v){
		m_buf.putFloat(i + m_start, (float)v);
	}
	public void writeInt(int i, int v) {
		m_buf.putInt(i + m_start,  v);
	}
	public void writeByte(int i, int v) {
		m_buf.put(i + m_start, (byte)v);
	}
	public void writeShort(int i, int v) {
		m_buf.putShort(i + m_start, (short)v);
	}
	public double readFloat(int i) {
		return m_buf.getFloat(i + m_start);
	}
	public int readInt(int i) {
		return m_buf.getInt(i + m_start);
	}
	public int readByte(int i) {
		return m_buf.get(i + m_start);
	}
	public int readShort(int i) {
		return m_buf.getShort(i + m_start);
	}
	public void writeBit(int i, int bitNum, boolean v) {
		if(bitNum > 7) {
			throw new RuntimeException("bit number cannot be greater than 7!");
		}
		int bv = readByte(i);
		if(v) {
			bv |= 1<<bitNum;
		} else {
			bv &= ~(1<<bitNum);
		}
		writeByte(i, bv);
	}
	public boolean readBit(int i, int bitNum) {
		if(bitNum > 7) {
			throw new RuntimeException("bit number cannot be greater than 7!");
		}
		int bv = readByte(i) & (1<<bitNum);
		return bv != 0;
	}
}
