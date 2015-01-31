package org.sdpa.protocol;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class SDPAStt{
	
	
	protected int count;
	protected List<STTDATA> sttdatas;
	
	public SDPAStt(){
		this.count = 0 ;
		this.sttdatas = new ArrayList<STTDATA>();
		sttdatas.clear();
	}
	
	public int getCount(){
		return count;
	}
	
	public SDPAStt addSttData( STTDATA sttdat){
		sttdatas.add(sttdat);
		count = sttdatas.size();
		return this;
		
	}
	
	public void writeTo(ByteBuffer data) {	 
		data.putInt(count);
		for(STTDATA tmp : sttdatas){
			tmp.WriteTo(data);
		}
	}
	
	public short getByteLength(){
    	short len = 0 ;
    	for( STTDATA tmp : sttdatas){
    		len = (short) (len + tmp.getByteLength());
    	}
    	return (short) (len+4);
    }

	
}