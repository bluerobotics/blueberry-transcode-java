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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 
 */
public class BlueberryBuffer {
	private static final Charset CHAR_ENC = StandardCharsets.UTF_8;

	private int m_byteOffset = 0;
	private ByteBuffer m_buf;
	private static final ByteOrder BYTE_ORDER = ByteOrder.LITTLE_ENDIAN;

	/**
	 * wraps the supplied buffer in a buffer.
	 * Buffer starting location (index = 0) is assumed to be the beginning of buffer
	 * @param bb - a ByteBuffer either with a received packet or to be filled with a packet to transmit
	 */
	public BlueberryBuffer(ByteBuffer bb) {
		m_buf = bb;
		m_byteOffset = 0;
		m_buf.order(BYTE_ORDER);
	}
	
	/**
	 * Makes a new buffer offset from this one by the specified number of bytes
	 * @param offset - the number of bytes to offset this buffer by when creating the new buffer
	 * @return - the new buffer that is offset from this one.
	 */
	public BlueberryBuffer getNextBuffer(int offset) {
		int i = offset;
		BlueberryBuffer result = new BlueberryBuffer(m_buf);
		result.m_byteOffset = m_byteOffset + i;

		return result;
	}
	public int getCurrentIndex() {
		int i = m_byteOffset;
		
		return i;
	}
	
	/**
	 * puts the specified byte into the buffer
	 * @param b - the byte to add
	 * @return - the number of bytes in the buffer
	 */
	public int put(byte b) {
		m_buf.put(b);
		return m_buf.position();
	}
	public void writeFloat(FieldIndex i, int byteOffset, double v){
		m_buf.putFloat(i.getIndex() + byteOffset + m_byteOffset, (float)v);
	}
	
	public void writeUnsignedShort(FieldIndex i, int byteOffset, int v) {
		m_buf.putShort(i.getIndex() + byteOffset + m_byteOffset, (short)(v & 0xffff));
	}
	public void writeUnsignedByte(FieldIndex i, int byteOffset, int v) {
		m_buf.put(i.getIndex() + byteOffset + m_byteOffset, (byte)(v & 0xff));
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
	public long readLong(FieldIndex i, int byteOffset) {
		return m_buf.getLong(i.getIndex() + byteOffset + m_byteOffset);
	}
	public void writeLong(FieldIndex i, int byteOffset, long v) {
		m_buf.putLong(i.getIndex() + byteOffset + m_byteOffset, v);
	}
	public double readDouble(FieldIndex i, int byteOffset) {
		return m_buf.getDouble(i.getIndex() + byteOffset + m_byteOffset);
	}
	public void writeDouble(FieldIndex i, int byteOffset, double v) {
		m_buf.putDouble(i.getIndex() + byteOffset + m_byteOffset, v);
	}
	public int readByte(FieldIndex i, int byteOffset) {
		return m_buf.get(i.getIndex() + byteOffset + m_byteOffset);
	}
	
	public String getString(FieldIndex i, int byteOffset) {
		
		int j = i.getIndex() + byteOffset + m_byteOffset;
		int n = m_buf.getInt();
		return CHAR_ENC.decode(m_buf.slice(j+4,n)).toString();
	}
	public void putString(FieldIndex i, int byteOffset, String s) {
		int n = s.length();
		int j = i.getIndex() + byteOffset + m_byteOffset;

		ByteBuffer bs = CHAR_ENC.encode(s);
		m_buf.putInt(j, n);
		m_buf.put(j+4, bs, 0, n);
	}
	public int readUnsignedShort(FieldIndex i, int byteOffset) {
		int result = m_buf.getShort(i.getIndex() + byteOffset + m_byteOffset);
		if(result < 0) {
			result += 65536;
		}
		return result;
		
	}
	public int readUnsignedByte(FieldIndex i, int byteOffset) {
		int result = (int) m_buf.get(i.getIndex() + byteOffset + m_byteOffset);
		if(result < 0) {
			result += 256;
		}
		return result;
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
	 * updates this buffer's position with the index of the specified buffer
	 * @param bb
	 */
	public void setPosition(BlueberryBuffer bb) {
		if(bb.m_buf.array() != m_buf.array()) {
			throw new RuntimeException("Can't set buffer position with position from buffer with different underlying arrays.");
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
	public int getBufferHash() {
		return m_buf.hashCode();
	}
	/**
	 * indicates that this packet is finished. This means it cannot be added to are changed.
	 * This is true for packets that have been received or ones that have had their {@link complete()} methods called.
	 * @return
	 */
	public boolean isComplete() {
		return m_buf.isReadOnly();
	}
	public void complete() {
		if(!isComplete()) {
			m_buf.flip();
			m_buf = m_buf.asReadOnlyBuffer();
			m_buf.order(BYTE_ORDER);
		}
	}
	/**
	 * gets the length of this buffer in bytes
	 * This is the length of useful data in the buffer so far, not the maximum possible length of the underlying byte array
	 * @return
	 */
	public int getLength() {
		int result = -1;
		if(isComplete()) {
			result = m_buf.limit();
		} else {
			result = m_buf.position();
		}
		return result;
	}
}
