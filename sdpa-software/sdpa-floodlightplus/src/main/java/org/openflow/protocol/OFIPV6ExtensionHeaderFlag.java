package org.openflow.protocol;

public enum OFIPV6ExtensionHeaderFlag {
    OFPIEH_NONEXT ((short)(1 << 0)), 
    OFPIEH_ESP ((short)(1 << 1)), 
    OFPIEH_AUTH ((short)(1 << 2)), 
    OFPIEH_DEST ((short)(1 << 3)), 
    OFPIEH_FRAG ((short)(1 << 4)), 
    OFPIEH_ROUTER ((short)(1 << 5)), 
    OFPIEH_HOP ((short)(1 << 6)), 
    OFPIEH_UNREP ((short)(1 << 7)), 
    OFPIEH_UNSEQ ((short)(1 << 8)); 

    protected short value;

    private OFIPV6ExtensionHeaderFlag(short value) {
        this.value = value;
    }

    /**
     * @return the value
     */
    public short getValue() {
        return value;
    }
}
