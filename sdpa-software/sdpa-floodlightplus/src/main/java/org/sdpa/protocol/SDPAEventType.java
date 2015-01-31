package org.sdpa.protocol;
/*
	Author : eric
	Description : define the event type as enum

*/

public enum SDPAEventType{
	
	SDPAPARAM_NON 		 (0),
	SDPAPARAM_IN_PORT     (1),  /* Switch input port. */
	SDPAPARAM_DL_VLAN     (2),  /* VLAN id. */
	SDPAPARAM_DL_VLAN_PCP (3),  /* VLAN priority. */
	SDPAPARAM_DL_TYPE     (4),  /* Ethernet frame type. */
	SDPAPARAM_NW_TOS      (5),  /* IP ToS (DSCP field, 6 bits). */
	SDPAPARAM_NW_PROTO    (6),  /* IP protocol. */
	SDPAPARAM_TP_SRC      (7),  /* TCP/UDP/SCTP source port. */
	SDPAPARAM_TP_DST      (8),  /* TCP/UDP/SCTP destination port. */
	SDPAPARAM_TP_FLAG	 (9),  /* TCP FLAG  */
	SDPAPARAM_DL_SRC      (10), /* Ethernet source address. */
	SDPAPARAM_DL_DST      (11), /* Ethernet destination address. */
	SDPAPARAM_NW_SRC      (12), /* IP source address. */
	SDPAPARAM_NW_DST      (13), /* IP destination address. */
	SDPAPARAM_METADATA    (14), /* Upper level data	*/
	SDPAPARAM_CONST		 (15);

    protected int value;

    private SDPAEventType(int value) {
        this.value = value;
    }

    /**
     * @return the
     * value
     */
    public int getValue() {
        return value;
    }
	
}





