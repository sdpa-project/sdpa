package org.sfa.protocol;

import java.nio.ByteBuffer;

import net.floodlightcontroller.packet.IPv4;

public class SFAAction{
	
	public enum ActType
	{
        ACT_NON	    	 (0),
	    ACT_OUTPUT    	 (1),
	    ACT_DROP     	 (2),
	    ACT_TOOPENFLOW	 (3),
	    //added by zzl
	    ACT_SETSRCFIELD  (4),
	    ACT_SETDSTFIELD  (5);
		
	    protected int value;

	    ActType(int value) {
	        this.value = value;
	    }

	    /**
	     * @return the
	     * value
	     */
	    public int getValue() {
	        return value;
	    }
	}

	protected ActType type;
	protected int param;
	
	public SFAAction(){
		type = ActType.ACT_TOOPENFLOW;
		param = 0 ;
	}
	
	public SFAAction( ActType t , int p ){
		type = t ;
		param = p;
	}
	public SFAAction( ActType t , String ip ){
		type = t ;
		param = IPv4.toIPv4Address(ip);
	}
	
	public SFAAction setAction( ActType t , int p ){
		type = t;
		param = p;
		return this;
	}
	
	public void WriteTo(ByteBuffer data){
		data.putInt(type.getValue());
		data.putInt(param);
	}
	
	public short getByteLength(){
		return 8;
	}
	
	
}
