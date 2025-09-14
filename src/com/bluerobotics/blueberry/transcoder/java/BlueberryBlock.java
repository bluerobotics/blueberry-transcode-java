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
	private int m_byteOffset = 0;
	private final ByteBuffer m_buf;
	/**
	 * wraps the supplied buffer in a block.
	 * Buffer starting location (index = 0) is assumed to be the beginning of block
	 * @param bb - a ByteBuffer either with a received packet or to be filled with a packet to transmit
	 */
	public BlueberryBlock(ByteBuffer bb) {
		m_buf = bb;
		m_byteOffset = 0;
		m_buf.order(ByteOrder.LITTLE_ENDIAN);
	}
	
	/**
	 * Makes a new block offset from this one by the specified number of bytes
	 * @param wordOffset - the number of 4-byte words to offset this block by when creating the new block
	 * @return - the new block that is offset from this one.
	 */
	public BlueberryBlock getNextBlock(int wordOffset) {
		int i = wordOffset * 4;
		BlueberryBlock result = new BlueberryBlock(m_buf);
		result.m_byteOffset = m_byteOffset + i;

		return result;
	}
	public int getCurrentWordIndex() {
		int i = m_byteOffset;
		if((i & 0b11) != 0) {
			throw new RuntimeException("Somehow the buffer length is not a multiple of 4!");
		}
		return i/4;
	}
	
	
	public void writeFloat(FieldIndex i, int byteOffset, double v){
		m_buf.putFloat(i.getIndex() + byteOffset + m_byteOffset, (float)v);
	}
	public void writeInt(FieldIndex i, int byteOffset, int v) {
		m_buf.putInt(i.getIndex() + byteOffset + m_byteOffset,  v);
	}
	public void writeByte(FieldIndex i, int byteOffset, int v) {
		m_buf.put(i.getIndex() + byteOffset + m_byteOffset, (byte)v);
	}
	public void writeShort(FieldIndex i, int byteOffset, int v) {
		m_buf.putShort(i.getIndex() + byteOffset + m_byteOffset, (short)v);
	}
	public double readFloat(FieldIndex i, int byteOffset) {
		return m_buf.getFloat(i.getIndex() + byteOffset + m_byteOffset);
	}
	public int readInt(FieldIndex i, int byteOffset) {
		return m_buf.getInt(i.getIndex() + byteOffset + m_byteOffset);
	}
	public int readByte(FieldIndex i, int byteOffset) {
		return m_buf.get(i.getIndex() + byteOffset + m_byteOffset);
	}
	public int readShort(FieldIndex i, int byteOffset) {
		return m_buf.getShort(i.getIndex() + byteOffset + m_byteOffset);
	}
	public void writeBool(BitIndex i, int byteOffset, boolean v) {
		if(i.getBitIndex() > 7) {
			throw new RuntimeException("bit number cannot be greater than 7!");
		}
		int bv = readByte(i, byteOffset);
		if(v) {
			bv |= 1<<i.getBitIndex();
		} else {
			bv &= ~(1<<i.getBitIndex());
		}
		writeByte(i, byteOffset, bv);
	}
	public boolean readBool(BitIndex i, int byteOffset) {
		if(i.getBitIndex() > 7) {
			throw new RuntimeException("bit number cannot be greater than 7!");
		}
		int bv = readByte(i, byteOffset) & (1<<i.getBitIndex());
		return bv != 0;
	}
	/**
	 * updates this block's position with the index of the specified block
	 * @param bb
	 */
	public void setPosition(BlueberryBlock bb) {
		if(bb.m_buf.array() != m_buf.array()) {
			throw new RuntimeException("Can't set block position with position from block with different underlying arrays.");
		}
		int i = bb.m_byteOffset;
		m_buf.position(i);
	}
	@Override
	public String toString() {
		String s = "0x";
		for(int i = 0; i < 8; ++i) {
			byte b = m_buf.get(i + m_byteOffset);
			String s2 = Integer.toHexString(b);
			s += "0".repeat(2 - s2.length()) + s2;
				
		}
		return getClass().getName()+"("+s+"...)";
	}
}
