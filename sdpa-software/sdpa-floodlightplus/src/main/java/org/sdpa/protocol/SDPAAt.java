package org.sdpa.protocol;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.openflow.protocol.action.OFAction;


public class SDPAAt{
	
	  protected long bitmap;
	  protected int  counts;
	  protected List<ATDATA> atdatas;
	  
	  
	  public SDPAAt(){
		  this.counts = 0 ;
		  this.atdatas = new ArrayList<ATDATA>();
		  atdatas.clear();
	  }
	  
	  public SDPAAt setBitmap(long bm){
		  bitmap = bm;
		  return this;
	  }
	  
	  public int getCounts(){
		  return this.counts;
	  }
	  public SDPAAt addAtData(ATDATA atdat){	
		  atdatas.add(atdat);
		  counts = atdatas.size();
		  return this;
	  }
	  
	  public void writeTo(ByteBuffer data) {
		  data.putLong(bitmap);
		  data.putInt(counts);
	      for(ATDATA tmp : atdatas ){
	    	  tmp.WriteTo(data);
	      }	        
	    }
	  
	  public int getByteLength(){
		  int len = 0;
		  for( ATDATA tmp : atdatas){
			 len = len + tmp.getByteLength();
		  }
		  return len+4+8;
	  }
	  
}