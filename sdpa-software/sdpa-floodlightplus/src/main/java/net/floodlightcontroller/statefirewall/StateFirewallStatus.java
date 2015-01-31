package net.floodlightcontroller.statefirewall;

public enum StateFirewallStatus {
	
	
	//STT table ( NOTE: simple demo ,do not use all of the status below!!)
	/*		Prev-Status								Event					Next-Status
	 * 		SFW_STATUS_REQUESTER_NONE		  		SYN					SFW_STATUS_REQUESTER_SYN_SENT
	 * 		SFW_STATUS_RESPONSER_NONE				(syn)				SFW_STATUS_RESPONSER_ESTABLISH
	 * 		SFW_STATUS_REQUESTER_SYN_SENT			ack					SFW_STATUS_REQUESTER_ESTABLISH
	 * 		SFW_STATUS_REQUESTER_ESTABLISH			FIN					SFW_STATUS_REQUESTER_FIN_SENT
	 * 		SFW_STATUS_RESPONSER_ESTABLISH			fin					SFW_STATUS_RESPONSER_NONE
	 * 		SFW_STATUS_REQUESTER_FIN_SENT			ack					SFW_STATUS_REQUESTER_NONE
	 *		
	 *		If we find a flow in st but we cannot find it stt, we should remain the next-status unchanged
	 */
	
	 // ST table
	 /*		Direction					Status
	  * 	c--->s						requester_none
	  * 	s--->c						responser_none
	  */
	
	//AT TABLE
	/*		
	 * 			Direction				Status										Action			
	 * 			c--->s 					SFW_STATUS_REQUESTER_NONE				output( a port)
	 * 			s--->c					SFW_STATUS_RESPONSER_NONE				output( b port)
	 * 			c--->s					SFW_STATUS_REQUESTER_SYN_SENT			output( a port)
	 * 			s--->c					SFW_STATUS_RESPONSER_ESTABLISH			output( b port)					
	 * 			c--->s					SFW_STATUS_REQUESTER_ESTABLISH			output( a port)
	 * 			c--->s					SFW_STATUS_REQUESTER_FIN_SENT			output( a port)
	 * 			c--->s					SFW_STATUS_DEFAULT_ERROR				Drop
	 *			s--->c					SFW_STATUS_DEFAULT_ERROR				Drop
	 */
	
	SFW_STATUS_DEFAULT_ERROR		(0),
	SFW_STATUS_REQUESTER_NONE		(1),
	SFW_STATUS_RESPONSER_NONE		(2),
	
	// first SYN from c--->s
	SFW_STATUS_REQUESTER_SYN_SENT	(3),
	SFW_STATUS_RESPONSER_WAITSYN	(4),
	// syn+ack from s ---->c
	SFW_STATUS_RESPONSER_SYNACK_SENT (6),
	SFW_STATUS_REQUESTER_ESTABLISH	 (7),
	// ack from c ---> s
	SFW_STATUS_RESPONSER_ESTABLISH	(8),
	
	// fin from c ---> s
	SFW_STATUS_REQUESTER_FIN_SENT	(9),
	SFW_STATUS_RESPONSER_WAIT	(10),
	
	// IF ack from s ---> c
	SFW_STATUS_RESPONSER_CLOSEWAIT1 (11),
	SFW_STATUS_REQUESTER_FINWAIT1 (12),
	
	// then fin from s----> c
	// IF fin + ack from s ---> c
	SFW_STATUS_RESPONSER_CLOSEWAIT2  (13),
	SFW_STATUS_REQUESTER_FINWAIT2 (14);
	
	// ack from c ---> s
	//SFW_STATUS_REQUESTER_NONE
	//SFW_STATUS_RESPONSER_NONE
	
	protected int  value;
	
	private StateFirewallStatus(int v){
		this.value = v;
	}

	public int getValue(){
		return value;
	}
}
