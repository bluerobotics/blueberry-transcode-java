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
	private int m_length = 0;
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
		m_length = bb.limit();//TODO: is this correct?
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
	/**
	 * constructs an index for the very next block in this buffer (i.e. the end of the buffer)
	 * Before this can be used, the buffer should be grown
	 * @return
	 */
	public FieldIndex getNextIndex() {
		return FieldIndex.make(getLength());
	}
	public int getCurrentIndex() {
		int i = m_byteOffset;
		
		return i;
	}
	public boolean isEmpty() {
		return m_byteOffset > m_buf.limit();
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
	public void writeFloat32(FieldIndex i, int byteOffset, double v){
		checkIndex(i, byteOffset, 4);
		m_buf.putFloat(i.getIndex() + byteOffset + m_byteOffset, (float)v);
	}
	
	public void writeUint16(FieldIndex i, int byteOffset, int v) {
		checkIndex(i, byteOffset, 2);
		m_buf.putShort(i.getIndex() + byteOffset + m_byteOffset, (short)(v));
	}
	public void writeUint8(FieldIndex i, int byteOffset, int v) {
		checkIndex(i, byteOffset, 1);
		m_buf.put(i.getIndex() + byteOffset + m_byteOffset, (byte)v);
	}
	public void writeInt32(FieldIndex i, int byteOffset, int v) {
		checkIndex(i, byteOffset, 4);
		m_buf.putInt(i.getIndex() + byteOffset + m_byteOffset,  v);
	}
	public void writeInt8(FieldIndex i, int byteOffset, int v) {
		checkIndex(i, byteOffset, 1);
		m_buf.put(i.getIndex() + byteOffset + m_byteOffset, (byte)v);
	}
	public void writeInt16(FieldIndex i, int byteOffset, int v) {
		checkIndex(i, byteOffset, 2);
		m_buf.putShort(i.getIndex() + byteOffset + m_byteOffset, (short)v);
	}
	public double readFloat32(FieldIndex i, int byteOffset) {
		checkIndex(i, byteOffset, 4);
		return m_buf.getFloat(i.getIndex() + byteOffset + m_byteOffset);
	}
	public int readInt32(FieldIndex i, int byteOffset) {
		checkIndex(i, byteOffset, 4);
		return m_buf.getInt(i.getIndex() + byteOffset + m_byteOffset);
	}
	public long readUint32(FieldIndex i, int byteOffset) {
		checkIndex(i, byteOffset, 4);
		return (long)m_buf.getInt(i.getIndex() + byteOffset + m_byteOffset);
	}
	public long readInt64(FieldIndex i, int byteOffset) {
		checkIndex(i, byteOffset, 8);
		return m_buf.getLong(i.getIndex() + byteOffset + m_byteOffset);
	}
	public void writeInt64(FieldIndex i, int byteOffset, long v) {
		checkIndex(i, byteOffset, 8);
		m_buf.putLong(i.getIndex() + byteOffset + m_byteOffset, v);
	}
	public void writeUint32(FieldIndex i, int byteOffset, long v) {
		checkIndex(i, byteOffset, 4);
		m_buf.putInt(i.getIndex() + byteOffset + m_byteOffset, (int)v);
	}
	public double readFloat64(FieldIndex i, int byteOffset) {
		checkIndex(i, byteOffset, 8);
		return m_buf.getDouble(i.getIndex() + byteOffset + m_byteOffset);
	}
	public void writeFloat64(FieldIndex i, int byteOffset, double v) {
		checkIndex(i, byteOffset, 8);
		m_buf.putDouble(i.getIndex() + byteOffset + m_byteOffset, v);
	}
	public int readInt8(FieldIndex i, int byteOffset) {
		checkIndex(i, byteOffset, 1);
		return m_buf.get(i.getIndex() + byteOffset + m_byteOffset);
	}
	/**
	 * reads a string from the specified location, of the specified length
	 * @param i - the start of the block of interest
	 * @param byteOffset - the offset into the block
	 * @param n - the length of the string
	 * @return
	 */
	public String getString(FieldIndex i, int byteOffset, int n) {
		checkIndex(i, byteOffset, n);
		int j = i.getIndex() + byteOffset + m_byteOffset;

		return CHAR_ENC.decode(m_buf.slice(j+4,n)).toString();
	}
	/**
	 * writes a string to the buffer at the specified location
	 * @param i
	 * @param byteOffset
	 * @param s
	 */
	public void putString(FieldIndex i, int byteOffset, String s) {
		int n = s.length();
		checkIndex(i, byteOffset, n);
		int j = i.getIndex() + byteOffset + m_byteOffset;
		
		ByteBuffer bs = CHAR_ENC.encode(s);
		m_buf.putInt(j, n);
		m_buf.put(j+4, bs, 0, n);
	}
	public int readUint16(FieldIndex i, int byteOffset) {
		checkIndex(i, byteOffset, 2);
		int result = m_buf.getShort(i.getIndex() + byteOffset + m_byteOffset);
		if(result < 0) {
			result += 65536;
		}
		return result;
		
	}
	public int readUint8(FieldIndex i, int byteOffset) {
		checkIndex(i, byteOffset, 1);
		int result = (int) m_buf.get(i.getIndex() + byteOffset + m_byteOffset);
		if(result < 0) {
			result += 256;
		}
		return result;
	}
	public int readInt16(FieldIndex i, int byteOffset) {
		checkIndex(i, byteOffset, 2);
		return m_buf.getShort(i.getIndex() + byteOffset + m_byteOffset);
	}
	public void writeBit(FieldIndex i, int byteOffset, int bitNumber, boolean v) {
		checkIndex(i, byteOffset, 1);
		if(bitNumber > 7) {
			throw new RuntimeException("bit number cannot be greater than 7!");
		}
		int bv = readUint8(i, byteOffset);
		if(v) {
			bv |= 1<<bitNumber;
		} else {
			bv &= ~(1<<bitNumber);
		}
		writeUint8(i, byteOffset, bv);
	}
	public boolean readBit(FieldIndex i, int byteOffset, int bitNumber) {
		checkIndex(i, byteOffset, 1);
		if(bitNumber > 7) {
			throw new RuntimeException("bit number cannot be greater than 7!");
		}
		int bv = readUint8(i, byteOffset) & (1<<bitNumber);
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
		return m_length;
	}
	public void setLength(int len) {
		m_length = len;
	}
	public void align(int alignment) {
		grow(getLength(), alignment);
	}
	/**
	 * increase the length of this buffer by a number of bytes
	 * @param len - the length in bytes to grow by
	 * @param alginment - the byte alignment to round up by
	 */
	public void grow(int len, int alignment) {
		int n = len;
		int mod = len % alignment;
		if(mod > 0) {
			n += (alignment - mod);
		}
		m_length += n;
	}
	/**
	 * checks to ensure the buffer is long enough
	 * @param index - the index of the block we're interested in
	 * @param offset - the offset of the element within the block that we're interested in
	 * @param byteNum - the number of bytes of the element that we're interested in
	 */
	protected void checkIndex(FieldIndex index, int offset, int byteNum) {
		if((index.getIndex() + offset + byteNum) >= m_length) {
			throw new RuntimeException("Index is beyond the current length of this buffer.");
		}
	}
	
	
	
}
