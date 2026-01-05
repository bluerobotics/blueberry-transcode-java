/**
 * 
 */
package com.bluerobotics.blueberry.transcoder.java;

import java.nio.BufferOverflowException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import com.bluerobotics.blueberry.transcoder.java.BlueberryMessage.MessageLookup;
import com.starfishmedical.comms.Packet;
import com.starfishmedical.comms.PacketReceiver;

/**
 * 
 */
public abstract class BlueberryReceiver {
	protected static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(1, new ThreadFactory(){
		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r, "PacketReceiver");
			t.setDaemon(true);
			t.setPriority(Thread.NORM_PRIORITY);
			return t;
		}
		
	});
	private static final int BUFF_MAX_SIZE = 10000;
	private BlueberryPacket m_packet = new BlueberryPacket(BUFF_MAX_SIZE);
	private long lastByteTime = 0;
	private long maxByteTime = 0;
	private boolean m_checkCrc = true;
	private int m_rxCount = 0;
	private MessageLookup m_msgLookup;
	public interface MessageParser {
		public void parse(BlueberryMessage msg);
	}
	private  HashMap<Class<? extends BlueberryMessage>, MessageParser> m_parsers = new HashMap<>();

	/**
	 * processes the specified bytes from the incoming array
	 * @param bs
	 * @param from
	 * @param num
	 * @return true if a packet was found in the data
	 */
	public boolean use(byte[] bs, int from, int num) {
		boolean result = false;
		if(bs != null) {
			long t = System.currentTimeMillis();
			long dt = t - lastByteTime;
			if(dt > maxByteTime){
				maxByteTime = dt;
			}
			lastByteTime = t;
			
			for(int i = from; i < from + num; ++i){
				result |= addByte(bs[i]);
			}
		}
		return result;
	}


	public void addParser(Class<? extends BlueberryMessage> c, MessageParser p) {
		m_parsers.put(c,  p);
	}
	


	
	/**
	 * adds the bytes to this packet.
	 * It will throw away bytes that occur before a start word
	 * @param bs the new bytes to add
	 * @return true if a whole packet has been decoded from the bytes otherwise false
	 * @see bytesNeeded()
	 */
	protected synchronized boolean addByte(byte b){
		
		boolean result = false;
		
		int i;
		
		try {
			i = m_packet.put(b);
			
		} catch (BufferOverflowException e) {
			reset();
			i = 0;
			
		
		}
	
		if(m_packet.checkStartWord()) {
			reset();
		} else if(m_packet.checkLength()){
			//check to see if we're done
			
			if(!m_checkCrc || m_packet.checkCrc()){
				publish(m_packet);
				result = true;
			} else {
			}
			reset();
		}
		return result;
	}
	
	public void reset() {
		m_packet = new BlueberryPacket(BUFF_MAX_SIZE);
	}
	
	/**
	 * Now that we know this buffer contains a valid packet,
	 * give it to the packet decoder to parse it
	 * @param bs
	 */
	protected void publish(BlueberryPacket p) {
		p.complete();
		++m_rxCount;
		EXECUTOR.submit(() -> parse(p), "PacketReceiver.publish");
		reset();

	}
	
	public int getRxCount() {
		return m_rxCount;
	}
	
	

	private void parse(BlueberryPacket p){
		Iterator<BlueberryMessage> ms = p.getMessages(m_msgLookup);
		while(ms.hasNext()) {
			BlueberryMessage m = ms.next();
			MessageParser mp = m_parsers.get(m.getClass());
			if(mp != null) {
				mp.parse(m);
			}
		}
	}

}
