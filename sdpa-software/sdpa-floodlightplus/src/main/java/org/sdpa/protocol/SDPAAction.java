package org.sdpa.protocol;

import java.nio.ByteBuffer;

public class SDPAAction{
	
	public enum ActType
	{
            ACT_NON		 (0),
	    ACT_OUTPUT    	 (1),
	    ACT_DROP     	 (2),
	    ACT_TOOPENFLOW	 (3);
		
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
	
	public SDPAAction(){
		type = ActType.ACT_TOOPENFLOW;
		param = 0 ;
	}
	
	public SDPAAction( ActType t , int p ){
		type = t ;
		param = p;
	}
	
	public SDPAAction setAction( ActType t , int p ){
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
