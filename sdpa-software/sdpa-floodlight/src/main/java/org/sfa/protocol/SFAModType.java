package org.sfa.protocol;
/*
	Author : eric
	Description : define the event type as enum

*/

public enum SFAModType{
	
	ENTRY_ADD    (0),
    ENTRY_UPDATE     (1),
    ENTRY_DEL	 (2);
	
    protected int value;

    SFAModType(int value) {
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
