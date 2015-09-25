package org.sfa.protocol;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import net.floodlightcontroller.core.IOFSwitch;

import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFType;
import org.openflow.util.U16;
import org.slf4j.Logger;


/*
	Author : eric
	Description : create the sfa at mode  msg
	
	msg pattern:
	
	of_header:
		version(4)
		type
		length
		xid
	sfa_body:
		SFAAt.tableid
		SFAAt.count
		... count of below...
		SFAAt.modtype
		SFAAt.atdata
		..........
*/


public class SFAAtMod extends OFMessage{
	public static int MINIMUM_LENGTH = 32;
    
    protected int appid;
    protected int count;
    protected static Logger logger;
    

    
    public class ATMOD_DATA{
    	SFAModType type;
    	ATDATA atdata;
    
    	public ATMOD_DATA( SFAModType t , ATDATA a){
    		type = t ;
    		atdata = a;
    	}
    	
    }
  
    protected List<ATMOD_DATA> atdatas;
    
    public SFAAtMod(){
    	super();
    	this.type = OFType.SFA_AT_MOD;
    	this.length = U16.t(MINIMUM_LENGTH);
    	appid = 0 ;
    	count = 0 ;	
    	atdatas = new ArrayList<ATMOD_DATA>();
    }
    
    
    public SFAAtMod addATMod( int id , SFAModType t, ATDATA a){
    	appid = id;
    	atdatas.add(new ATMOD_DATA(t,a));
    	count = atdatas.size();
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
        if( atdatas == null )
        	 throw new RuntimeException("--------SFAAtMod  ERROR! ---------");
        computeLength();
        data.putInt(appid);
        data.putInt(count);
    	for(ATMOD_DATA tmp : atdatas){
    		data.putInt(tmp.type.getValue());
    		tmp.atdata.WirteTo(data);
    	}
    

    }
    
    public boolean sendmsg( IOFSwitch sw ){
    	 try {
		     sw.write(this, null);
		     logger.info("mod msg send !");
		     return true;
		 } catch (IOException e) {
		     logger.error("Failed to write mod msg to siwtch");
		     return false;
		 }
    }

    /* (non-Javadoc)
     * @see org.openflow.protocol.OFMessage#computeLength()
     */
    @Override
    public void computeLength() {
    	short len = 0;
    	if( atdatas == null )
       	 throw new RuntimeException("-----------SFA ATMOD ERROR!-------- ");
    
    	for(ATMOD_DATA tmp : atdatas ){
    			len = (short) (len + 4 + tmp.atdata.getByteLength());
    		}
    	
    	this.length = (short) (len+4+4+8);   		
    	}


}
