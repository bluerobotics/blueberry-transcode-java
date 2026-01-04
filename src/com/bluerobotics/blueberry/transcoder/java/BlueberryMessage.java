/**
 * 
 */
package com.bluerobotics.blueberry.transcoder.java;

/**
 * A superclass to base all blueberry message upon.
 * Each specific message subclass will have getters for their constituent fields builders to create new messages
 * Note that messages are either created from a received packet, or they are built in a packet being built for transmission
 */
public abstract class BlueberryMessage {
	public static final int SIZE_INDEX = 0;
	public static final int MAX_ORD_INDEX = 2;
	protected final BlueberryBuffer m_buf;
	/**
	 * Inits the fields of this message
	 * @param b - the buffer that contains his message
	 * @param i - the index in the packet of the start of this message
	 */
	public BlueberryMessage(BlueberryBuffer b) {
		m_buf = b;
	}
	/**
	 * gets the length of this message, measured in bytes.
	 * @return
	 */
	public int getLength() {
		int i = m_buf.readShort(FieldIndex.ZERO, SIZE_INDEX);
		return i * 4;
	}
	/**
	 * compute the maximum number of top-level fields in this message, as defined by the IDL schema.
	 * This is useful to determine which fields might be present in this message
	 * For instance, if the max ordinal is zero, then there are no fields populated in this message
	 * @return
	 */
	public int getMaxOrdinal() {
		int i = m_buf.readByte(FieldIndex.ZERO, MAX_ORD_INDEX);
		return i;
	}
	/**
	 * updates the length field with the specified value
	 * @param length - the message length, measured in bytes. This must be a multiple of 4!
	 */
	public void writeLength(int length) {
		if((length % 4) != 0) {
			throw new RuntimeException("length must be a multiple of 4!");
		}
		m_buf.writeShort(FieldIndex.ZERO, SIZE_INDEX, length/4);
	}
	/**
	 * updates the max ordinal field of this message with the specified value
	 * This indicate the highest ordinal field contained in this message
	 * All lower ordinal fields are assumed to be present
	 * This is only used during message construction and will e based on the current version of the IDL schema
	 * A value of zero means there are no fields populated
	 * @param ord
	 */
	public void writeMaxOrdinal(int ord) {
		m_buf.writeByte(FieldIndex.ZERO, MAX_ORD_INDEX, ord);
	}
}
