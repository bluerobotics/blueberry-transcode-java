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
	private ByteBuffer m_buf;
	/**
	 * wraps the supplied buffer in a block.
	 * Buffer starting location (index = 0) is assumed to be the beginning of block
	 * @param bb - a ByteBuffer either with a received packet or to be filled with a packet to transmit
	 */
	public BlueberryBlock(ByteBuffer bb) {
		m_buf = bb;
		m_buf.order(ByteOrder.LITTLE_ENDIAN);
	}
	/**
	 * Makes a new block offset from this one by the specified number of bytes
	 * @param i - the number of bytes to offset this block by when creating the new block
	 * @return - the new block that is offset from this one.
	 */
	public BlueberryBlock getNextBlock(int i) {
		int n = m_buf.capacity();
		ByteBuffer bb = m_buf.slice(i, n - i);
		return new BlueberryBlock(bb);
	}
	
	
	public void writeFloat(FieldIndex i, double v){
		m_buf.putFloat(i.getIndex(), (float)v);
	}
	public void writeInt(FieldIndex i, int v) {
		m_buf.putInt(i.getIndex(),  v);
	}
	public void writeByte(FieldIndex i, int v) {
		m_buf.put(i.getIndex(), (byte)v);
	}
	public void writeShort(FieldIndex i, int v) {
		m_buf.putShort(i.getIndex(), (short)v);
	}
	public double readFloat(FieldIndex i) {
		return m_buf.getFloat(i.getIndex());
	}
	public int readInt(FieldIndex i) {
		return m_buf.getInt(i.getIndex());
	}
	public int readByte(FieldIndex i) {
		return m_buf.get(i.getIndex());
	}
	public int readShort(FieldIndex i) {
		return m_buf.getShort(i.getIndex());
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
