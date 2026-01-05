/*
Copyright (c) 2025  Kenneth MacCallum

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
import java.time.Instant;
import java.util.Iterator;

import com.bluerobotics.blueberry.transcoder.java.BlueberryMessage.MessageLookup;
import com.starfishmedical.comms.Crc1021;
import com.starfishmedical.comms.Packet;

/**
 * A wrapper for a buffer that assumes the contents are structured according to the alternate, Blueberry simple transport layer
 * This means the buffer contains an 8-byte header, followed by message data
 * the header consists of:
 *  - 4 constant bytes: 'B', 'l', 'u', 'e'
 *  - a uint16 of the packet length, measured in 4-byte words
 *  - a uint16 containing the CRC-16-CCITT computed on the message data 
 * Messages are encoded, end-to-end, each message with a 4-byte header consisting of:
 *  - a uint16 of the module key-value
 *  - a uint16 of the message key-value
 *  - the message header can be treated as a unique 4-byte, uint32
 * Each message also encodes its length, which can be determined by creating a message from it and interrogating it.
 */
public class BlueberryPacket {
	private BlueberryBuffer m_buf;
	private Instant m_receiveTime;
	private static final int START_WORD_INDEX = 0;
	private static final int LENGTH_INDEX = 4;
	private static final int CRC_INDEX = 6;
	private static final int DATA_INDEX = 8;
	private static final byte[] START_WORD_BYTES = {0x42, 0x6c, 0x75, 0x65};
	private static final int START_WORD_VAL = 0x65756c42; //little endian means the lowest index byte (first received byte) is the least significant byte
	
	/**
	 * creates a new packet, ready for receiving into or buiding prior to transmission
	 * @param bufferSize
	 */
	public BlueberryPacket(int bufferSize) {
		m_buf = new BlueberryBuffer(ByteBuffer.wrap(new byte[bufferSize]).order(ByteOrder.LITTLE_ENDIAN));
		m_receiveTime = Instant.now();
	}
	
	private int getPublishedWordLength() {
		return m_buf.readShort(FieldIndex.ZERO, LENGTH_INDEX);
	}
	private void setPublishedWordLength(int len) {
		m_buf.writeShort(FieldIndex.ZERO, LENGTH_INDEX, len);
	}
	private int getPublishedCrc() {
		return m_buf.readShort(FieldIndex.ZERO, CRC_INDEX);
	}
	private void setPublishedCrc(int crc) {
		m_buf.writeShort(FieldIndex.ZERO, CRC_INDEX, crc);
	}
	/**
	 * computes the CRC of this packet and compares it to its published CRC
	 * @return - true if computed CRC matches published one
	 */
	protected boolean checkCrc() {
		int acrc = computeCrc();
		int pcrc = getPublishedCrc();
		return acrc == pcrc;
	}
	/**
	 * compares the actual length of this packet with its published length
	 * @return
	 */
	protected boolean checkLength() {
		int al = m_buf.getLength()/4;
		return al >= DATA_INDEX && al >= getPublishedWordLength();
	}
	
	/**
	 * checks the recevied bytes against the start word
	 * this only checks up to the number received so far, so will pass if only one byte receieved and it matches the first expected start byte
	 * @return
	 */
	protected boolean checkStartWord() {
		int i = m_buf.getLength();
		//i represents the number of bytes received so far
		int sw = m_buf.readInt(FieldIndex.ZERO, START_WORD_INDEX);
		sw ^= START_WORD_VAL;
		//mask off bits we have not received yet. Note that this could be written shorter but a switch-case seemed more understandable
		switch(i){
		case 0://we've received no bytes yet so don't check any
			sw &= 0x0;
			break;
		case 1://we've only received one byte so only check the lowest order (first received) byte
			sw &= 0xff;
			break;
		case 2:
			sw &= 0xffff;
			break;
		case 3:
			sw &= 0xffffff;
			break;
		default://we've received 4 or more bytes so we'll check all four start bytes
			break;
		}
		//now if there are any non-zero bits in sw then we've failed the match
		return sw == 0;
		
		
	}
	public int computeCrc() {
		int result = -1;
		
		int pl = getWordLength()*4;
		Crc1021 crc = new Crc1021();

		for(int i = DATA_INDEX; i < pl; ++i){
			byte b = (byte)m_buf.readByte(FieldIndex.ZERO, i);
			crc.addByte(b);
		}
			
		result = crc.getCrc();
		
		return result;
	}
	/**
	 * puts the specified byte into the buffer
	 * @param b - the byte to add
	 * @return - the number of bytes in the buffer
	 */
	public int put(byte b) {
		return m_buf.put(b);
	}
	public boolean isComplete() {
		return m_buf.isComplete();
	}
	/**
	 * if this packet is not complete then this method: appends zero-value bytes if necessary, updates the length field, computes the CRC, completes the buffer. 
	 * zero-value bytes are appended if the length is not a multiple of 4-bytes
	 */
	public void complete() {
		if(!isComplete()) {
			//append zero-value bytes as necessary
			int n = getByteLength();
			int m = n % 4;
			for(int i = 0; i < m; ++i) {
				m_buf.put((byte)0);
			}
			
			//update the length field
			setPublishedWordLength(n / 4);
			
			//compute and set crc
			int crc = computeCrc();
			setPublishedCrc(crc);
			
			//complete the buffer
			m_buf.complete();
		}
	}
	public int getByteLength() {
		int result = -1;
		if(isComplete()) {
			result = getPublishedWordLength() * 4;
		} else {
			result = m_buf.getLength();
		}
		return result;
	}
	public int getWordLength() {
		int result = getByteLength();
		if((result & 0b11) != 0) {
			result |= 0b11;
			++result;
		}
		result /= 4;
		return result;
	}

	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append(getClass().getName());
		s.append("(");
		
		boolean firstTime = true;
		int pl = getWordLength()*4;
		for(int i = 0; i < pl; ++i) {
			byte b = (byte)m_buf.readByte(FieldIndex.ZERO, i);
			if(!firstTime) {
				s.append(",");
			}
			firstTime = false;
			s.append("0x");
			s.append(Integer.toHexString(((int)b) & 0xff));
		}
		s.append(" )");
		return s.toString();
	}
	
	public Iterator<BlueberryMessage> getMessages(MessageLookup lookup){
		MessageIterator result = new MessageIterator(lookup);
		
		return result;
	}
	
	private class MessageIterator implements Iterator<BlueberryMessage> {
		private int i = 0;
		private BlueberryPacket p;
		private BlueberryMessage msg = null;
		private final MessageLookup lu;
		MessageIterator(MessageLookup mlu){
			lu = mlu;
		}

		@Override
		public boolean hasNext() {
			int j = 0;
			if(msg == null) {
				j = DATA_INDEX;
			} else {
				j = i + 4 + msg.getLength();//the 4 is to jump past the module/message key
			}
			return j < p.getPublishedWordLength()*4;
		}
		
		@Override
		public BlueberryMessage next() {
			if(msg == null) {
				i = DATA_INDEX;
			} else {
				i += 4 + msg.getLength();//the 4 is to jump past the module/message key
			}
			int key = m_buf.readInt(FieldIndex.ZERO, i);
			
			msg = lu.wrap(key, p.m_buf.getNextBuffer(i + 4));//the 4 here is also to look past the module/message key
			return msg;
		}
		
		
	}
	

}
