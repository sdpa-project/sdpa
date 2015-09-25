package org.sfa.protocol;
/*
	Author : eric
	Description : define the event type as enum

*/

public enum SFAEventType{
	
	SFAPARAM_NON 		 (0),
	SFAPARAM_IN_PORT     (1),  /* Switch input port. */
	SFAPARAM_DL_VLAN     (2),  /* VLAN id. */
	SFAPARAM_DL_VLAN_PCP (3),  /* VLAN priority. */
	SFAPARAM_DL_TYPE     (4),  /* Ethernet frame type. */
	SFAPARAM_NW_TOS      (5),  /* IP ToS (DSCP field, 6 bits). */
	SFAPARAM_NW_PROTO    (6),  /* IP protocol. */
	SFAPARAM_TP_SRC      (7),  /* TCP/UDP/SCTP source port. */
	SFAPARAM_TP_DST      (8),  /* TCP/UDP/SCTP destination port. */
	SFAPARAM_TP_FLAG	 (9),  /* TCP FLAG  */
	SFAPARAM_DL_SRC      (10), /* Ethernet source address. */
	SFAPARAM_DL_DST      (11), /* Ethernet destination address. */
	SFAPARAM_NW_SRC      (12), /* IP source address. */
	SFAPARAM_NW_DST      (13), /* IP destination address. */
	SFAPARAM_METADATA    (14), /* Upper level data	*/
	SFAPARAM_CONST		 (15);

    protected int value;

    private SFAEventType(int value) {
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





