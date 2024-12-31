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
	
	public void writeFloat(FieldIndex i, double v){
		m_buf.putFloat(i.getIndex() + m_start, (float)v);
	}
	public void writeInt(FieldIndex i, int v) {
		m_buf.putInt(i.getIndex() + m_start,  v);
	}
	public void writeByte(FieldIndex i, int v) {
		m_buf.put(i.getIndex() + m_start, (byte)v);
	}
	public void writeShort(FieldIndex i, int v) {
		m_buf.putShort(i.getIndex() + m_start, (short)v);
	}
	public double readFloat(FieldIndex i) {
		return m_buf.getFloat(i.getIndex() + m_start);
	}
	public int readInt(FieldIndex i) {
		return m_buf.getInt(i.getIndex() + m_start);
	}
	public int readByte(FieldIndex i) {
		return m_buf.get(i.getIndex() + m_start);
	}
	public int readShort(FieldIndex i) {
		return m_buf.getShort(i.getIndex() + m_start);
	}
	public void writeBit(BitIndex i, boolean v) {
		if(i.getBitIndex() > 7) {
			throw new RuntimeException("bit number cannot be greater than 7!");
		}
		int bv = readByte(i);
		if(v) {
			bv |= 1<<i.getBitIndex();
		} else {
			bv &= ~(1<<i.getBitIndex());
		}
		writeByte(i, bv);
	}
	public boolean readBit(BitIndex i) {
		if(i.getBitIndex() > 7) {
			throw new RuntimeException("bit number cannot be greater than 7!");
		}
		int bv = readByte(i) & (1<<i.getBitIndex());
		return bv != 0;
	}
}
