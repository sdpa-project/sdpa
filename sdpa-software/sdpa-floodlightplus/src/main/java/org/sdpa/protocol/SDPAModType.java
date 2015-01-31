package org.sdpa.protocol;
/*
	Author : eric
	Description : define the event type as enum

*/

public enum SDPAModType{
	
	ENTRY_ADD    (0),
    ENTRY_UPDATE     (1),
    ENTRY_DEL	 (2);
	
    protected int value;

    SDPAModType(int value) {
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
