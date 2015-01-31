package org.sdpa.protocol;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class SDPASt
{
	protected int appid;
	//@sc
	// the state when event exists but no stt entry match
    protected long bitmap;
    protected int count;  
     
	protected List<STDATA> stdatas;
    
    //construct
    public SDPASt(){
    	appid = 0;
    	bitmap = 0;
    	count = 0;
    	stdatas = new ArrayList<STDATA>();
    	stdatas.clear(); 	
    }
    
    //set appid
    public SDPASt setAppid( int aid){
    	appid = aid;
    	return this;
    }
    public int getAppid(){
    	return appid;
    }
    
    //set bitmap
    public SDPASt setBitmap(long bp){
    	bitmap = bp;
    	return this;
    }
    public long getBitmap(){
    	return bitmap;
    }
    
    public SDPASt addStData(STDATA stdata)
    {
    	stdatas.add(stdata);
    	count = stdatas.size();
    	return this;
    }
  
    public void writeTo(ByteBuffer data) {
        data.putInt(appid);
        data.putLong(bitmap);
        data.putInt(count);
       for( STDATA tmp : this.stdatas){
    	   tmp.WriteTo(data);
       }
        
    }
    
    @Override
    public int hashCode() {
        final int prime = 311;
        int result = super.hashCode();
        result = prime * result + appid;
        result = (int) (prime * result + bitmap);
        result = prime * result + count;
        result = prime * result + stdatas.hashCode();
        return result;
    }
    
    public short getByteLength(){
    	short len = 0 ;
    	for( STDATA tmp : stdatas){
    		len = (short) (len + tmp.getByteLength());
    	}
    	return (short) (len+4+8+4);
    }
       
}