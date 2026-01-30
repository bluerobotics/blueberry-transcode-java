/**
 * 
 */
package com.bluerobotics.blueberry.transcoder.java;

import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A class to keep track of messages, message parsers and module/message keys
 * This will likely eventually also deal with 
 */
public class BlueberryMessageParser implements Consumer<BlueberryPacket>{
	
	
	
//	private HashMap<Integer, Consumer<BlueberryMessage>> m_consumers = new HashMap<>();
//	private HashMap<Integer, Function<BlueberryBuffer, BlueberryMessage>> m_factories = new HashMap<>();
	private HashMap<Integer, Consumer<BlueberryBuffer>> m_processors = new HashMap<>();
	
	/**
	 * steps through all messages in a packet and applies the registered consumer
	 * @param p - the packet to parse
	 */
	
	@Override
	public void accept(BlueberryPacket p) {
		
		BlueberryBuffer buf = p.getDataBuffer();

		while(!buf.isEmpty()) {
			int key = BlueberryMessage.getModuleMessageKey(buf);
			int len = BlueberryMessage.getByteLength(buf);
			parse(key, buf);
			buf = buf.getNextBuffer(len);
		}
		
		
		
	}
	
	/**
	 * This registers a builder for a type of message, as well as a consumer and the unique module/method key that is used in the simple transport mechanism
	 * It may seem odd to include the key for the Zenoh transport use-case. We'll see if we can address this later.
	 * @param key - this is the module/message key that is used in the alternate simple transport mechanism
	 * @param p - a method to parse a message from the buffer
	 */
	public void registerParser(int key, Consumer<BlueberryBuffer> p) {
//		m_factories.put(key, factory);
//		m_consumers.put(key, consumer);
		m_processors.put(key, p);
	}
	/**
	 * parse the blueberry message contained in the specified buffer
	 * @param key - the message identifier (module/message key)
	 * @param buf - the buffer containing the message. This should be aligned with the message sub-header
	 */
	public void parse(int key, BlueberryBuffer buf) {
//		Function<BlueberryBuffer, BlueberryMessage> factory = m_factories.get(key);
//		Consumer<BlueberryMessage> consumer = m_consumers.get(key);
		Consumer<BlueberryBuffer> consumer = m_processors.get(key);
		
		if(consumer != null) {
//			BlueberryMessage msg = factory.apply(buf);
			consumer.accept(buf);
		}
	}

	
}
