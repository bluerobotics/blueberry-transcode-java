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
	public interface MessageLookup {
		/**
		 * determines the message type from the provided module/message key and wraps the corresponding message type around the provided buffer
		 * the buffer is assumed to point to the start of the message (message sub-header), NOT the key field (message header)
		 * @param key
		 * @return
		 */
		BlueberryMessage wrap(int key, BlueberryBuffer buf);
	}
	public static final int MODULE_KEY_INDEX = 0;
	public static final int MESSAGE_KEY_INDEX = 2;
	public static final int SIZE_INDEX = 4;
	public static final int MAX_ORD_INDEX = 6;
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
	 * gets the length of the message contained in the specified buffer, measured in bytes.
	 * This is used by @link BlueberryReceiver to step through messages without relying on it being parsed.
	 * @param buf
	 * @return
	 */
	public static int getByteLength(BlueberryBuffer buf) {
		int i = buf.readShort(FieldIndex.ZERO, SIZE_INDEX);
		return i * 4;
	}
	/**
	 * gets the module/message key from the specified buffer.
	 * This is useful to establish the message key prior to actually parsing the message
	 * @param buf
	 * @return
	 */
	public static int getModuleMessageKey(BlueberryBuffer buf) {
		return buf.readInt(FieldIndex.ZERO, MODULE_KEY_INDEX);//this will ready 4-bytes formatted as a 32-bit int, with the LSb at the specified index
	}

	/**
	 * gets the 4-byte word formed from the combination of the module key and the message key
	 * Because the message encoding is little endian, the 4-byte result has the module key in the LSBs and the message key in the MSBs
	 * This method should be added by the automatic schema parser and should not need to be hand-coded
	 * This is defined here so that this class has access to the method.
	 * Note that if the schema is edited such that it does not have this field then a compilation error will likely result
	 * @return
	 */
	public abstract int getModuleMessageKey();
	/**
	 * This method should be added by the automatic schema parser and should not need to be hand-coded
	 * This is defined here so that this class has access to the method.
	 * Note that if the schema is edited such that it does not have this field then a compilation error will likely result
	 * @param v
	 */
	public abstract void setModuleMessageKey(int v);
	/**
	 * gets the length of this message, measured in 4-byte words.
	 * This method should be added by the automatic schema parser and should not need to be hand-coded
	 * This is defined here so that this class has access to the method.
	 * Note that if the schema is edited such that it does not have this field then a compilation error will likely result
	 * @return the length
	 */
	public abstract int getLength();
	/**
	 * Sets the length of this message, measured in 4-byte words
	 * This method should be added by the automatic schema parser and should not need to be hand-coded
	 * This is defined here so that this class has access to the method.
	 * Note that if the schema is edited such that it does not have this field then a compilation error will likely result
	 * @param v - the value 
	 */
	public abstract void setLength(int v);
	/**
	 * compute the maximum number of top-level fields in this message, as defined by the IDL schema.
	 * This is useful to determine which fields might be present in this message
	 * For instance, if the max ordinal is zero, then there are no fields populated in this message
 	 * This method should be added by the automatic schema parser and should not need to be hand-coded
 	 * This is defined here so that this class has access to the method.
 	 * Note that if the schema is edited such that it does not have this field then a compilation error will likely result
	 * @return
	 */
	public abstract int getMaxOrdinal();
	/**
	 * updates the max ordinal field of this message with the specified value
	 * This indicate the highest ordinal field contained in this message
	 * All lower ordinal fields are assumed to be present
	 * This is only used during message construction and will e based on the current version of the IDL schema
	 * A value of zero means there are no fields populated
	 * This method should be added by the automatic schema parser and should not need to be hand-coded
	 * This is defined here so that this class has access to the method.
	 * Note that if the schema is edited such that it does not have this field then a compilation error will likely result
	 * @param v - the value to set the ordinal too.
	 */
	public abstract void setMaxOrdinal(int v);

}
