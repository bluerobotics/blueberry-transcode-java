/**
 * 
 */
package com.bluerobotics.blueberry.transcoder.java;

import com.starfishmedical.comms.Packet;
import com.starfishmedical.comms.PacketReceiver;

/**
 * 
 */
public abstract class BlueberryReceiver extends PacketReceiver {
	private static final int BUFF_MAX_SIZE = 10000;

	@Override
	protected Packet makeNewPacket() {
		return new BlueberryPacket(BUFF_MAX_SIZE);
	}


}
