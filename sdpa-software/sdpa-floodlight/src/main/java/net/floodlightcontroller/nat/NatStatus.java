package net.floodlightcontroller.nat;

public enum NatStatus {
	
	/*Just a prototype
	 * A private ip --> a public ip
	 * Assume 10.0.0.1 nat to 66.66.66.66(can be configured)
	*/
	
	//AT
	/*
	 *   -------------------------------------------
	 *     Match Field      -      Status          
	 *   -------------------------------------------
	 *    (src)10.0.0.1     -      SRC_IP  
	 *   (dst)66.66.66.66   -      DST_IP
	 *   -------------------------------------------
	 */
	
	//STT   (all events are null)
	/*
	 *   ---------------------------------------------------
	 *        Status           -    Event   -  Next Status       
	 *   ---------------------------------------------------
	 *         SRC_IP          -    Null    -      SRC_IP
	 *         DST_IP          -    Null    -      DST_IP
	 *   ---------------------------------------------------
	 */
	
	//AT
	/*
	 *   ---------------------------------------------------
	 *      Match Field     - Next Status -     Action       
	 *   ---------------------------------------------------
	 *    (src)10.0.0.1     -    SRC_IP   -     FIXFIELD(ip)
	 *    (dst)66.66.66.66  -    DST_IP   -     FIXFIELD(dst-ip)
	 *   ---------------------------------------------------
	 */
	NAT_STATUS_DEFAULT_ERROR	(0),
//	FORWARD_TRANSFERRED 		(1),
//	BACK_TRANSFERRED		    (2),
	
	SRC_IP						(1),
	DST_IP						(2);
	
	protected int value;
	
	private NatStatus(int v){
		this.value = v;
	}

	public int getValue(){
		return value;
	}
}
