package org.sdpa.protocol;

import java.io.IOException;
import java.nio.ByteBuffer;

import net.floodlightcontroller.core.IOFSwitch;

import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFType;
import org.openflow.util.U16;
import org.slf4j.Logger;


/*
	Author : eric
	Description : create the sdpa create msg
	

	msg pattern:
	
	of_header:
		version(4)
		type
		length
		xid
	sdpa_body: 
		SDPASt.appid
		SDPASt.bitmap
		SDPASt.count
		... count of below...
		SDPASt.status
		SDPASt.data.len
		FSASt.data
		..........
		
		SDPAStt.count
		.....count of below...
		SDPAStt.param1type
		SDPAStt.param1
		SDPAStt.param2type
		SDPAStt.param2
		SDPAStt.op
		..........
		
		SDPAAt.count
		.....count of below...
		SDPAAt.matchmp
		SDPAAt.status
		OFAction
		..........

*/

public class SDPACreate extends OFMessage {
    public static int MINIMUM_LENGTH = 32;

    protected SDPASt stbody;
    protected SDPAStt sttbody;
    protected SDPAAt  atbody;
    protected static Logger logger;
    
 
    public SDPACreate() {
        super();
        //this.computeLength();
        this.type = OFType.SDPA_CREATE;
        this.length = U16.t(MINIMUM_LENGTH);
        this.stbody = null;
        this.sttbody = null;
        this.atbody = null;
        
    }

    public SDPACreate setST( SDPASt st){
    	this.stbody = st;
    	return this;
    }
    public SDPACreate setSTT( SDPAStt stt){
    	this.sttbody = stt;
    	return this;
    }
    public SDPACreate setAT( SDPAAt at){
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
        	 throw new RuntimeException("SDPA INIT ERROR! ");
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
       	 throw new RuntimeException("SDPA CREATE COMPUTE LEN ERROR! ");
    	this.length = (short) (stbody.getByteLength()+sttbody.getByteLength()+atbody.getByteLength()+8);
    }
    
    public boolean sendmsg( IOFSwitch sw ){
   	 try {
		     sw.write(this, null);
		     //logger.info("init msg send !");
		     return true;
		 } catch (IOException e) {
		     logger.error("Failed to write init msg to siwtch");
		     return false;
		 }
   }
}
