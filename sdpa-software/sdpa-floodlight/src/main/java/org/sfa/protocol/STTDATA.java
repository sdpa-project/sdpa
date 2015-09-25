package org.sfa.protocol;

import java.nio.ByteBuffer;


public class STTDATA{
	
		protected SFAEventType 	paramlefttype;
		protected long paramleft;
		protected SFAEventType 	paramrighttype;
		protected long paramright; 
		protected SFAEventOp 	opt;
		protected int prestatus;
		protected int nextstatus;
		
		public STTDATA(SFAEventType lefttype, long leftparam,
					   SFAEventType righttype, long rightparam,
					   SFAEventOp  op,  int  prestat, int nextstat){
			paramlefttype = lefttype;
			paramleft = leftparam;
			paramrighttype = righttype;
			paramright = rightparam;
			opt = op;
			prestatus = prestat;
			nextstatus = nextstat;
		}
		
		public STTDATA( ){
			paramlefttype = SFAEventType.SFAPARAM_NON;
			paramleft = 0;
			paramrighttype = SFAEventType.SFAPARAM_NON;
			paramright = 0 ;
			opt = SFAEventOp.OPRATOR_NON;
			prestatus = 0 ;
			nextstatus = 0 ;
		}
		
		public STTDATA setLeft( SFAEventType type, long p){
			paramlefttype = type;
			paramleft = p;
			return this;
		}
		public STTDATA setRight( SFAEventType t , long r){
			paramrighttype = t;
			paramright = r;
			return this;
		}
		public STTDATA setOp( SFAEventOp op){
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