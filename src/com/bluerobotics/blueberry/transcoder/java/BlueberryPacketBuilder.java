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

/**
 * An abstract class to base a builder class on.
 * The builder is used to construct a packet for transmission
 */
public abstract class BlueberryPacketBuilder {
	private BlueberryPacket m_packet;
	private final int m_maxByteCount;
	private BlueberryBlock m_topLevelBlock;
	private BlueberryBlock m_currentBlock = null;
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
		m_packet = new BlueberryPacket(m_maxByteCount);
		m_topLevelBlock = m_packet.getTopLevelBlock();
	}
	public BlueberryBlock getTopLevelBlock() {
		return m_topLevelBlock;
	}
	public BlueberryBlock getCurrentBlock() {
		return m_currentBlock;
	}
	/**

	 * creates a new block backed by the same bytes but with an starting point shifted by the specified amount
	 * @param wordOffset
	 */
	protected void advanceBlock(int wordOffset) {
		if(m_currentBlock == null) {
			m_currentBlock = m_topLevelBlock;
		}
		m_currentBlock = m_currentBlock.getNextBlock(wordOffset);
		m_topLevelBlock.setPosition(m_currentBlock);
	}
	/**
	 * finishes any last items in the packet, like finalizing the length, computing crc, etc.
	 * This method will be implemented by subclasses
	 * It does not need to be called, it is called as part of the getPacket() method
	 */
	protected abstract void finish();
	
	/**
	 * get the packet that was just constructed
	 * @return
	 */
	public BlueberryPacket getPacket() {
		return m_packet;
	}
}
