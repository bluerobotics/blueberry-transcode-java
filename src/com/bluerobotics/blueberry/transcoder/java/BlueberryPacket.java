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

import com.starfishmedical.comms.Crc1021;
import com.starfishmedical.comms.Packet;

/**
 * 
 */
public class BlueberryPacket extends Packet {
	protected BlueberryPacket(int bufferSize) {
		super(bufferSize);
	}

	@Override
	public int computeCrc(int startWord) {
		int result = -1;
		
		int pl = getWordLength()*4;
		Crc1021 crc = new Crc1021();

		for(int i = startWord*4; i < pl; ++i){
			byte b = get(i);
//			crc.addByte1021(b);
			crc.addByte(b);
		}
			
		result = crc.getCrc();
		
		return result;
	}
	public BlueberryBlock getTopLevelBlock() {
		return new BlueberryBlock(getActualBuffer());
	}
	public int getWordLength() {
		int result = getByteLength();
		if((result & 0b11) != 0) {
			result |= 0b11;
			++result;
		}
		result >>= 2;//same as divide by 4
		return result;
	}
	public String toString() {
		String s = getClass().getName();
		s += "(";
		ByteBuffer bb = getActualBuffer();
		boolean firstTime = true;
		int pl = getWordLength()*4;
		for(int i = 0; i < pl; ++i) {
			byte b = bb.get(i);
			if(!firstTime) {
				s += ",";
			}
			firstTime = false;
			s += " 0x"+Integer.toHexString(((int)b) & 0xff);
		}
		s += " )";
		return s;
	}


}
