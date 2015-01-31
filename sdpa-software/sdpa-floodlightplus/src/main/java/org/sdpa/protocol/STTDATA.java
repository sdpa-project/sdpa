package org.sdpa.protocol;

import java.nio.ByteBuffer;


public class STTDATA{
	
		protected SDPAEventType 	paramlefttype;
		protected long paramleft;
		protected SDPAEventType 	paramrighttype;
		protected long paramright; 
		protected SDPAEventOp 	opt;
		protected int prestatus;
		protected int nextstatus;
		
		public STTDATA(SDPAEventType lefttype, long leftparam,
					   SDPAEventType righttype, long rightparam,
					   SDPAEventOp  op,  int  prestat, int nextstat){
			paramlefttype = lefttype;
			paramleft = leftparam;
			paramrighttype = righttype;
			paramright = rightparam;
			opt = op;
			prestatus = prestat;
			nextstatus = nextstat;
		}
		
		public STTDATA( ){
			paramlefttype = SDPAEventType.SDPAPARAM_NON;
			paramleft = 0;
			paramrighttype = SDPAEventType.SDPAPARAM_NON;
			paramright = 0 ;
			opt = SDPAEventOp.OPRATOR_NON;
			prestatus = 0 ;
			nextstatus = 0 ;
		}
		
		public STTDATA setLeft( SDPAEventType type, long p){
			paramlefttype = type;
			paramleft = p;
			return this;
		}
		public STTDATA setRight( SDPAEventType t , long r){
			paramrighttype = t;
			paramright = r;
			return this;
		}
		public STTDATA setOp( SDPAEventOp op){
			opt = op;
			return this;
		}
		public STTDATA setStatus( int prev , int next){
			prestatus = prev;
			nextstatus = next;
			return this;
		}
		
		public void WriteTo(ByteBuffer data){
			data.putInt(paramlefttype.getValue());
			data.putLong(paramleft);
			data.putInt(paramrighttype.getValue());
			data.putLong(paramright);
			data.putInt(opt.getValue());
			data.putInt(prestatus);
			data.putInt(nextstatus);
		}
		
		public short getByteLength(){
			return 36;
		}
		

	
}