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
public class BlueberryMessageParser {
	
	
	
//	private HashMap<Integer, Consumer<BlueberryMessage>> m_consumers = new HashMap<>();
//	private HashMap<Integer, Function<BlueberryBuffer, BlueberryMessage>> m_factories = new HashMap<>();
	private HashMap<Integer, Consumer<BlueberryBuffer>> m_processors = new HashMap<>();
	
	/**
	 * steps through all messages in a packet and applies the registered consumer
	 * @param p - the packet to parse
	 */
	public void parse(BlueberryPacket p){
		
		BlueberryBuffer buf = p.getDataBuffer();
		
		while(!buf.isEmpty()) {
			int key = buf.readInt(FieldIndex.ZERO, 0);
			
			buf = buf.getNextBuffer(4);//skip past the transport header now that we have the key
			
			parse(key, buf);
			int offset = 4 + BlueberryMessage.getLength(buf);//skip the sub-header and all the message data
			buf = buf.getNextBuffer(offset);
		}
		
	}
	
	/**
	 * This registers a builder for a type of message, as well as a consumer and the unique module/method key that is used in the simple transport mechanism
	 * It may seem odd to include the key for the Zenoh transport use-case. We'll see if we can address this later.
	 * @param key - this is the module/message key that is used in the alternate simple transport mechanism
	 * @param builder - a method to instantiate a message given a buffer
	 * @param consumer - a method to consume a message after it has been instantiated
	 */
	public void registerMessage(int key, Function<BlueberryBuffer, BlueberryMessage> factory, Consumer<BlueberryMessage> consumer) {
//		m_factories.put(key, factory);
//		m_consumers.put(key, consumer);
		m_processors.put(key, bb -> {
			BlueberryMessage bm = factory.apply(bb);
			consumer.accept(bm);
		});
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
