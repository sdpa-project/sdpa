package org.sfa.protocol;

import java.nio.ByteBuffer;
import java.lang.String;

public class ATDATA {
	
	SFAAction act;
	int status;
	int len_of_buf;
	byte[] buffer;
	
	public ATDATA(int stats, byte[] s , SFAAction at)
	{
		act = at;
		buffer = s;
		len_of_buf = s.length;
		status = stats;
	}
	
	public ATDATA(){
		status = 0 ;
		len_of_buf = 0;
		act = null;
		buffer = null;
	}
	
	public ATDATA setAction( SFAAction at){
		act = at ;
		return this;
	}
	
	public ATDATA setStatus( int stats )
	{
		status = stats;
		return this;
	}
	public int getStatus(){
		return status;
	}
	
	public ATDATA setBuffer( byte[] s){
		buffer = s;
		len_of_buf = s.length;
		return this;
	}
	public String getBuffer(){
		return buffer.toString();
	}
	
	public void WirteTo(ByteBuffer data)
	{
		act.WriteTo(data);
		data.putInt(status);
		data.putInt(len_of_buf);
		data.put(this.buffer);	
	}
	
	public short getByteLength()
	{
		return (short) (act.getByteLength()+4+4+len_of_buf);
	}

}
