/*
Copyright (c) 2026  Blue Robotics North Inc.

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

/**
 * A superclass to base all blueberry message upon.
 * Each specific message subclass will have getters for their constituent fields builders to create new messages
 * Note that messages are either created from a received packet, or they are built in a packet being built for transmission
 */
public abstract class BlueberryMessage {
	public interface MessageLookup {
		/**
		 * determines the message type from the provided module/message key and wraps the corresponding message type around the provided buffer
		 * the buffer is assumed to point to the start of the message (message sub-header), NOT the key field (message header)
		 * @param key
		 * @return
		 */
		BlueberryMessage wrap(int key, BlueberryBuffer buf);
	}
	protected static final int MODULE_MESSAGE_KEY_INDEX = 0;
	protected static final int SIZE_INDEX = 4;
	protected static final int MAX_ORD_INDEX = 6;
	protected static final int MIN_MAX_ORDINAL = 2;//this is the max ordinal if no fields are populated
	protected static final int MIN_MESSAGE_LENGTH = 8;//this is the length of a message if no fields are populated
	protected static final int INVALID_BLOCK = 0xffff;
	protected static final int FIRST_MESSAGE_INDEX = 8;
	protected static final int STRING_BLOCK_LENGTH_INDEX = 0;
	protected static final int STRING_BLOCK_DATA_START_INDEX = 4;
	protected static final int STRING_PLACEHOLDER_BLOCK_INDEX = 0;
	protected static final int SEQUENCE_PLACEHOLDER_BLOCK_INDEX = 0;
	protected static final int SEQUENCE_PLACEHOLDER_ELEMENT_BYTES_INDEX = 2;
	protected static final int SEQUENCE_BLOCK_LENGTH_INDEX = 0;
	protected static final int SEQUENCE_BLOCK_DATA_START_INDEX = 4;

	protected final BlueberryBuffer m_buf;
	
	/**
	 * Inits the fields of this message
	 * @param b - the buffer that contains his message
	 * @param i - the index in the packet of the start of this message
	 */
	public BlueberryMessage(BlueberryBuffer b) {
		m_buf = b;
	}
	
	public abstract boolean isFull();
	public boolean isEmpty() {
		return getMaxOrdinal() <= MIN_MAX_ORDINAL;
	}
	/**
	 * writes the header for this message with the intention of building it for transmission
	 * The buffer must have zero length initially. It will be grown as data is added.
	 * Upon completion of this method, the message length will only include the header information
	 * @param b
	 * @param key - the module/message key to include in the message header
	 * @param maxOrdinal - the maximum ordinal value of all the fields to be populated
	 * @param byteLength - the length that this message should be
	 */
	protected void setupHeader(int key, int maxOrdinal, int byteLength) {
		if(m_buf.getLength() > 0) {
			throw new RuntimeException("A buffer must be empty prior to constructing a new message.");
		}

		updateByteLength(8);
		m_buf.writeInt32(FieldIndex.ZERO, MODULE_MESSAGE_KEY_INDEX, key);
		m_buf.writeInt16(FieldIndex.ZERO, MAX_ORD_INDEX, maxOrdinal);
	}
	


	/**
	 * gets the length of the message contained in the specified buffer, measured in bytes.
	 * This is used by @link BlueberryReceiver to step through messages without relying on it being parsed.
	 * @param buf
	 * @return
	 */
	public static int getByteLength(BlueberryBuffer buf) {
		int i = buf.readUint16(FieldIndex.ZERO, SIZE_INDEX);
		return i * 4;
	}
	protected int getByteLength() {
		return m_buf.readUint16(FieldIndex.ZERO, SIZE_INDEX) * 4;
	}
	protected BlueberryBuffer getNextMessageBuffer() {
		return m_buf.getNextBuffer(getByteLength());
	}
	private void updateByteLength(int n) {
		m_buf.grow(n, 4);
		int len = m_buf.getLength()/4;
		m_buf.writeUint16(FieldIndex.ZERO, SIZE_INDEX, len);
	}

	protected int getMaxOrdinal() {
		return m_buf.readUint8(FieldIndex.ZERO, MAX_ORD_INDEX);
	}
	/**
	 * gets the module/message key from the specified buffer.
	 * This is useful to establish the message key prior to actually parsing the message
	 * @param buf
	 * @return
	 */
	protected static int getModuleMessageKey(BlueberryBuffer buf) {
		return buf.readInt32(FieldIndex.ZERO, MODULE_MESSAGE_KEY_INDEX);//this will ready 4-bytes formatted as a 32-bit int, with the LSb at the specified index
	}
	protected static boolean isModuleMessageKeyCorrect(BlueberryBuffer buf, int key) {
		return buf.readInt32(FieldIndex.ZERO, MODULE_MESSAGE_KEY_INDEX) == key;//this will ready 4-bytes formatted as a 32-bit int, with the LSb at the specified index
	}

	


	
	/**
	 * retrieves a string given the index and offset of the string placeholder in the message
	 * the placeholder consists of the following:
	 * - uint16 index - the index of the string block
	 * - uint16 unused
	 * the string block consists f the following:
	 * - uint32 length - the number of characters of the string
	 * - uint8... the characters of the string
	 * @param i - the index of the message
	 * @param offset - the offset into the message of the string placeholder
	 * @return the string
	 */
	protected String getString(FieldIndex i, int offset) {
		FieldIndex sb = FieldIndex.make(m_buf.readUint16(i, offset + STRING_PLACEHOLDER_BLOCK_INDEX));
		
		int len = m_buf.readInt32(sb, STRING_BLOCK_LENGTH_INDEX);
		return m_buf.getString(sb, STRING_BLOCK_DATA_START_INDEX, len);
	}
	/**
	 * adds a string given the index and offset of the string placeholder in the message
	 * the placeholder consists of the following:
	 * - uint16 index - the index of the string block
	 * - uint16 unused
	 * the string block consists f the following:
	 * - uint32 length - the number of characters of the string
	 * - uint8... the characters of the string
	 * This method also expands the buffer by the length of the string block
	 * The string block will be placed at the end of the buffer
	 * @param i - the index of the message
	 * @param offset - the offset into the message of the string placeholder
	 * @return the string
	 */
	protected void addString(FieldIndex i, int offset, String s) {
		int n = s.length();
		//determine the string block index
		FieldIndex sb = m_buf.getNextIndex();
		//first grow the buffer
		updateByteLength(STRING_BLOCK_DATA_START_INDEX + n);
		//then write the length
		m_buf.writeInt32(sb, STRING_BLOCK_LENGTH_INDEX, n);
		m_buf.putString(sb, STRING_BLOCK_DATA_START_INDEX, s);
	}
	/**
	 * determine the index of a sequence element block, given the index of the sequence placeholder and the element index
	 * the placeholder consists of the following fields:
	 * - uint16 - the index to the start of the sequence block
	 * - uint16 - the number of bytes per sequence element
	 * @param i
	 * @param offset
	 * @return
	 */
	protected FieldIndex getSequenceElementBlock(FieldIndex i, int offset, int elementIndex) {
		if(i == FieldIndex.INVALID) {
			return FieldIndex.INVALID;
		}
		int j = m_buf.readUint16(i, offset + SEQUENCE_PLACEHOLDER_BLOCK_INDEX);
		if(j == INVALID_BLOCK) {
			return FieldIndex.INVALID;
		}
		int sbl = m_buf.readUint16(i,  offset + SEQUENCE_PLACEHOLDER_ELEMENT_BYTES_INDEX);
		return FieldIndex.make(j + sbl * elementIndex);
		
	}
	
	protected int getSequenceLength(FieldIndex i, int offset) {
		FieldIndex sb = FieldIndex.make(m_buf.readUint16(i, offset + SEQUENCE_PLACEHOLDER_BLOCK_INDEX));
		return m_buf.readInt32(sb, SEQUENCE_BLOCK_LENGTH_INDEX);
	}
	protected FieldIndex initSequenceBlock(FieldIndex i, int offset, int elementByteLength, int elementNum) {
		//determine the sequence block index
		FieldIndex sb = m_buf.getNextIndex();
		//first grow the buffer
		int bn = elementByteLength * elementNum;
		updateByteLength(SEQUENCE_BLOCK_DATA_START_INDEX + bn);
		m_buf.writeUint16(i, offset + SEQUENCE_PLACEHOLDER_BLOCK_INDEX, sb.getIndex());
		m_buf.writeUint16(i, offset + SEQUENCE_PLACEHOLDER_ELEMENT_BYTES_INDEX, elementByteLength);
		m_buf.writeUint32(sb, SEQUENCE_BLOCK_LENGTH_INDEX, elementNum);
		return sb;
	}
	/**
	 * determines the field index of an array element, given the index and offset of the array
	 * @param i - the index of the block containing the array 
	 * @param offset - the offset into the block of the array
	 * @param elementIndex - the index of the desired element of the array
	 * @param elementByteLength - the number of bytes per element of the array
	 * @return
	 */
	protected FieldIndex getArrayElementBlock(FieldIndex i, int offset, int elementIndex, int elementByteLength) {
		return FieldIndex.make(i, offset + (elementByteLength * elementIndex));
	}
	
	
}
