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
	public static final int INVALID_INDEX = 0xffff;
	private static final Charset CHAR_ENC = StandardCharsets.UTF_8;

	private int m_byteOffset = 0;
	private ByteBuffer m_buf;
	/**
	 * a simple class to contain the index of the last byte
	 * This allows buffers derived from this one to share the same last index
	 */
	private class LastIndex {
		int i;
	}
	private LastIndex m_lastIndex;//tracks the number of bytes used in the buffer
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
		m_lastIndex = new LastIndex();
	}
	
	/**
	 * Makes a new buffer offset from this one by the specified number of bytes
	 * @param offset - the number of bytes to offset this buffer by when creating the new buffer
	 * @return - the new buffer that is offset from this one.
	 */
	public BlueberryBuffer getNextBuffer(int offset) {
		
		BlueberryBuffer result = new BlueberryBuffer(m_buf);
		result.m_byteOffset = m_byteOffset + offset;
		result.m_lastIndex = m_lastIndex;

		return result;
	}
	/**
	 * constructs an index for the very next block in this buffer (i.e. the end of the buffer)
	 * Before this can be used, the buffer should be grown
	 * @return
	 */
	public int getNextIndex() {
		return m_lastIndex.i - m_byteOffset;
	}
	public int getCurrentIndex() {
		int i = m_byteOffset;
		
		return i;
	}
	public boolean isEmpty() {
		return m_byteOffset >= m_lastIndex.i;
	}
	
	
	public void writeFloat32(int i, double v){
		checkIndex(i, 4);
		m_buf.putFloat(m_byteOffset + i, (float)v);
	}
	
	public void writeUint16(int i, int v) {
		checkIndex(i, 2);
		m_buf.putShort(i + m_byteOffset, (short)(v));
	}
	public void writeUint8(int i, int v) {
		checkIndex(i, 1);
		m_buf.put(i + m_byteOffset, (byte)v);
	}
	public void writeInt32(int i, int v) {
		checkIndex(i, 4);
		m_buf.putInt(i + m_byteOffset,  v);
	}
	public void writeInt8(int i, int v) {
		checkIndex(i, 1);
		m_buf.put(i + m_byteOffset, (byte)v);
	}
	public void writeInt16(int i, int v) {
		checkIndex(i, 2);
		m_buf.putShort(i + m_byteOffset, (short)v);
	}
	public double readFloat32(int i) {
		checkIndex(i, 4);
		return m_buf.getFloat(i + m_byteOffset);
	}
	public int readInt32(int i) {
		checkIndex(i, 4);
		return m_buf.getInt(i + m_byteOffset);
	}
	public long readUint32(int i) {
		checkIndex(i, 4);
		return (long)m_buf.getInt(i + m_byteOffset);
	}
	public long readInt64(int i) {
		checkIndex(i, 8);
		return m_buf.getLong(i + m_byteOffset);
	}
	/**
	 * reads an unsigned long from the buffer.
	 * Note that these are treated like normal longs. This means it will be signed and it's up to the user to treat it as unsigned.
	 * @param i
	 * @return
	 */
	public long readUint64(int i) {
		checkIndex(i, 8);
		return m_buf.getLong(i + m_byteOffset);
	}
	public void writeInt64(int i, long v) {
		checkIndex(i, 8);
		m_buf.putLong(i + m_byteOffset, v);
	}
	/**
	 * Writes a long to a spot in the buffer allocated for an unsigned long.
	 * Note that this uses normal longs. This means it will be signed and it's up to the user to treat it as unsigned.

	 * @param i
	 * @param v
	 */
	public void writeUint64(int i, long v) {
		checkIndex(i, 8);
		m_buf.putLong(i + m_byteOffset, v);
	}
	public void writeUint32(int i, long v) {
		checkIndex(i, 4);
		m_buf.putInt(i + m_byteOffset, (int)v);
	}
	public double readFloat64(int i) {
		checkIndex(i, 8);
		return m_buf.getDouble(i + m_byteOffset);
	}
	public void writeFloat64(int i, double v) {
		checkIndex(i, 8);
		m_buf.putDouble(i + m_byteOffset, v);
	}
	public int readInt8(int i) {
		checkIndex(i, 1);
		return m_buf.get(i + m_byteOffset);
	}
	/**
	 * reads a string from the specified location, of the specified length
	 * @param i - the start of the block of interest
	 * @param byteOffset - the offset into the block
	 * @param n - the length of the string
	 * @return
	 */
	public String getString(int i, int n) {
		checkIndex(i, n);
		int j = i + m_byteOffset;

		return CHAR_ENC.decode(m_buf.slice(j,n)).toString();
	}
	/**
	 * writes a string to the buffer at the specified location
	 * also writes the string length
	 * @param i
	 * @param byteOffset
	 * @param s
	 */
	public void putString(int i, String s) {
		int n = s.length();
		checkIndex(i, n);
		int j = i + m_byteOffset;
		
		ByteBuffer bs = CHAR_ENC.encode(s);
		
		m_buf.put(j, bs, 0, n);
	}
	public int readUint16(int i) {
		checkIndex(i, 2);
		int result = m_buf.getShort(i + m_byteOffset);
		if(result < 0) {
			result += 65536;
		}
		return result;
		
	}
	public int readUint8(int i) {
		checkIndex(i, 1);
		int result = (int) m_buf.get(i + m_byteOffset);
		if(result < 0) {
			result += 256;
		}
		return result;
	}
	public int readInt16(int i) {
		checkIndex(i, 2);
		return m_buf.getShort(i + m_byteOffset);
	}
	public void writeBit(int i, int bitNumber, boolean v) {
		checkIndex(i, 1);
		if(bitNumber > 7) {
			throw new RuntimeException("bit number cannot be greater than 7!");
		}
		int bv = readUint8(i);
		if(v) {
			bv |= 1<<bitNumber;
		} else {
			bv &= ~(1<<bitNumber);
		}
		writeUint8(i, bv);
	}
	public boolean readBit(int i, int bitNumber) {
		checkIndex(i, 1);
		if(bitNumber > 7) {
			throw new RuntimeException("bit number cannot be greater than 7!");
		}
		int bv = readUint8(i) & (1<<bitNumber);
		return bv != 0;
	}
//	/**
//	 * updates this buffer's position with the index of the specified buffer
//	 * @param bb
//	 */
//	public void setPosition(BlueberryBuffer bb) {
//		if(bb.m_buf.array() != m_buf.array()) {
//			throw new RuntimeException("Can't set buffer position with position from buffer with different underlying arrays.");
//		}
//		int i = bb.m_byteOffset;
//		m_buf.position(i);
//	}
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
//			m_buf.flip();
			m_buf.limit(getLength());
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
		return m_lastIndex.i - m_byteOffset;
	}
//	public void setLength(int len) {
//		m_length = len;
//	}
	public void align(int alignment) {
		grow(getLength(), alignment);
	}
	/**
	 * increase the length of this buffer by a number of bytes
	 * @param len - the length in bytes to grow by
	 * @param alginment - the byte alignment to round up by. Only has an effect when greater than zero
	 */
	public void grow(int len, int alignment) {
		int n = len;
		if(alignment > 0) {
			int mod = len % alignment;
			if(mod > 0) {
				n += (alignment - mod);
			}
		}
		m_lastIndex.i += n;
	}
	/**
	 * checks to ensure the buffer is long enough
	 * @param index - the index of the block we're interested in
	 * @param offset - the offset of the element within the block that we're interested in
	 * @param byteNum - the number of bytes of the element that we're interested in
	 */
	protected void checkIndex(int i, int byteNum) {
		int j = i + m_byteOffset;
		if(j > m_lastIndex.i) {
			throw new RuntimeException("Index is beyond the current length of this buffer.");
		}
	}
	/**
	 * adds a new byte to the end of the buffer
	 * this is intended for use when receiving bytes one at a time
	 * @param b
	 * @return
	 */
	public int putNewByte(byte b) {
		int i = getLength();
		grow(1,0);
		 m_buf.put(i, b);
		return getLength();
	}
	/**
	 * makes a buffer that starts where this one ends
	 * @return
	 */
	public BlueberryBuffer getNextBuffer() {
		return getNextBuffer(getLength());
	}

	public byte[] toArray() {
		byte[] bs = new byte[getLength()];
		m_buf.get(bs);
		return bs;
	}
}
