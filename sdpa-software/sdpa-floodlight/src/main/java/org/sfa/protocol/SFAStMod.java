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
		SFASt.tableid
		SFASt.count
		... count of below...
		SFASt.modtype
		SFASt.stdata
		..........
*/


public class SFAStMod extends OFMessage {
	
public static int MINIMUM_LENGTH = 32;
    
    protected int appid;
    protected int count;
    protected static Logger logger;
   
    public class STMOD_DATA{
    	SFAModType type;
    	STDATA stdata;
    
    	public STMOD_DATA( SFAModType t , STDATA s){
    		type = t ;
    		stdata = s;
    	}
    	
    }
  
    protected List<STMOD_DATA> stdatas;
    
    public SFAStMod(){
    	super();
    	this.type = OFType.SFA_ST_MOD;
    	this.length = U16.t(MINIMUM_LENGTH);
    	appid = 0 ;
    	count = 0 ;	
    	stdatas = new ArrayList<STMOD_DATA>();
    }
    
    
    public SFAStMod addSTMod( int id , SFAModType t, STDATA a){
    	appid = id;
    	stdatas.add(new STMOD_DATA(t,a));
    	count = stdatas.size();
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
        if( stdatas == null )
        	 throw new RuntimeException("--------SFAStMod  ERROR! ---------");
        computeLength();
        data.putInt(appid);
        data.putInt(count);
    	for(STMOD_DATA tmp : stdatas){
    		data.putInt(tmp.type.getValue());
    		tmp.stdata.WirteTo(data);
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
    	if( stdatas == null )
       	 throw new RuntimeException("-----------SFA STMOD ERROR!-------- ");
    
    	for(STMOD_DATA tmp : stdatas ){
    			len = (short) (len + 4 + tmp.stdata.getByteLength());
    		}
    	
    	this.length = (short) (len+4+4+8);   		
    	}

}
