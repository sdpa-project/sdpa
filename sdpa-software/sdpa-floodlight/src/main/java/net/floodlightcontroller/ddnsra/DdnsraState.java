package net.floodlightcontroller.ddnsra;

public enum DdnsraState {
	//STT table
	/*		Prev-State								Event					Next-State
	 * 		DDRA_STATE_START				  		DNS request				DDRA_STATE_REQUEST_SENT
	 * 		DDRA_STATE_REQUEST_SENT					DNS response			DDRA_STATE_START
	 * 		
	 *		If we find a flow in st but we cannot find it stt, we should remain the next-status unchanged
	 */
	
	 // ST table
	 /*		Direction					State
	  * 	c--->s						DDRA_STATE_START
	  * 	s--->c						DDRA_STATE_START
	  */
	
	//AT TABLE
	/*		
	 * 			Direction				State									Action			

	 * 			c--->s 					DDRA_STATE_START						output( a port)
	 * 			s--->c					DDRA_STATE_START						output( b port)
	 * 			c--->s					DDRA_STATE_REQUEST_SENT					output( a port)
	 * 			s--->c					DDRA_STATE_REQUEST_SENT					output( b port)					
	 * 			c--->s					DDRA_STATE_DEFAULT_ERROR				Drop
	 *			s--->c					DDRA_STATE_DEFAULT_ERROR				Drop

	 */
	
	DDRA_STATE_DEFAULT_ERROR		(0),
	DDRA_STATE_START				(1),
	DDRA_STATE_REQUEST_SENT			(2);
	
	
	protected int  value;
	
	private DdnsraState(int v){
		this.value = v;
	}

	public int getValue(){
		return value;
	}
}

