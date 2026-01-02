/**
 * 
 */
package com.bluerobotics.blueberry.transcoder.java;

import com.starfishmedical.comms.Packet;

/**
 * A superclass to base all blueberry message upon.
 * Each specific message subclass will have getters for their constituent fields builders to create new messages
 * Note that messages are either created from a received packet, or they are built in a packet being built for transmission
 */
public abstract class BlueberryMessage {
	protected final Packet m_packet;
	protected final int m_index;
	/**
	 * Inits the fields of this message
	 * @param p - the packet that contains his message
	 * @param i - the index in the packet of the start of this message
	 */
	public BlueberryMessage(Packet p, int i) {
		m_packet = p;
		m_index = i;
	}
}
