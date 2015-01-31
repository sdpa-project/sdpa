package org.sdpa.protocol;

import java.nio.ByteBuffer;
import java.lang.String;

public class STDATA {
	
	int status;
	int len_of_buf;
	byte[] buffer;
	
	public STDATA(int stats, byte[] s)
	{
		buffer = s;
		len_of_buf = s.length;
		status = stats;
	}
	
	public STDATA(){
		status = 0 ;
		len_of_buf = 0;
		buffer = null;
	}
	
	public STDATA setStatus( int stats )
	{
		status = stats;
		return this;
	}
	public int getStatus(){
		return status;
	}
	
	public STDATA setBuffer( byte[] s){
		buffer = s;
		len_of_buf = s.length;
		return this;
	}
	public String getBuffer(){
		return buffer.toString();
	}
	
	public void WriteTo(ByteBuffer data)
	{
		data.putInt(status);
		data.putInt(len_of_buf);
		// TODO: buffer is empty
		//buffer = "10.0.0.110.".getBytes();
		//System.out.println("Buffer is: " + buffer.toString());
		data.put(buffer);	
	}
	
	public short getByteLength()
	{
		return (short) (4+4+len_of_buf);
	}

}
