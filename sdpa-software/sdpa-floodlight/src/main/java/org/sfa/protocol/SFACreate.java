package org.sfa.protocol;

import java.io.IOException;
import java.nio.ByteBuffer;

import net.floodlightcontroller.core.IOFSwitch;

import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFType;
import org.openflow.util.U16;
import org.slf4j.Logger;


/*
	Author : eric
	Description : create the sfa create msg
	

	msg pattern:
	
	of_header:
		version(4)
		type
		length
		xid
	sfa_body: 
		SFASt.appid
		SFASt.bitmap
		SFASt.count
		... count of below...
		SFASt.status
		SFASt.data.len
		FSASt.data
		..........
		
		SFAStt.count
		.....count of below...
		SFAStt.param1type
		SFAStt.param1
		SFAStt.param2type
		SFAStt.param2
		SFAStt.op
		..........
		
		SFAAt.count
		.....count of below...
		SFAAt.matchmp
		SFAAt.status
		OFAction
		..........

*/

public class SFACreate extends OFMessage {
    public static int MINIMUM_LENGTH = 32;

    protected SFASt stbody;
    protected SFAStt sttbody;
    protected SFAAt  atbody;
    protected static Logger logger;
    
 
    public SFACreate() {
        super();
        //this.computeLength();
        this.type = OFType.SFA_CREATE;
        this.length = U16.t(MINIMUM_LENGTH);
        this.stbody = null;
        this.sttbody = null;
        this.atbody = null;
        
    }

    public SFACreate setST( SFASt st){
    	this.stbody = st;
    	return this;
    }
    public SFACreate setSTT( SFAStt stt){
    	this.sttbody = stt;
    	return this;
    }
    public SFACreate setAT( SFAAt at){
    	this.atbody = at;
    	return this;
    }
    
    

    @Override
    public void readFrom(ByteBuffer data) {
        super.readFrom(data);
        //done nothing 
    }

    @Override
    public void writeTo(ByteBuffer data) {
        super.writeTo(data);
        if( this.stbody == null || this.sttbody == null || this.atbody == null)
        	 throw new RuntimeException("SFA INIT ERROR! ");
        computeLength();
        stbody.writeTo(data);
        sttbody.writeTo(data);
        atbody.writeTo(data);
    }

    /* (non-Javadoc)
     * @see org.openflow.protocol.OFMessage#computeLength()
     */
    @Override
    public void computeLength() {
    	if( this.stbody == null || this.sttbody == null || this.atbody == null)
       	 throw new RuntimeException("SFA CREATE COMPUTE LEN ERROR! ");
    	this.length = (short) (stbody.getByteLength()+sttbody.getByteLength()+atbody.getByteLength()+8);
    }
    

}
