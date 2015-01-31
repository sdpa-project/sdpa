package com.bigswitch.floodlight.vendor;

import java.nio.ByteBuffer;

public class OFActionNiciraTtlDecrement extends OFActionNiciraVendor {
    public static int MINIMUM_LENGTH_TTL_DECREMENT = 16;
    public static final short TTL_DECREMENT_SUBTYPE = 18;
    
    
    public OFActionNiciraTtlDecrement() {
        super(TTL_DECREMENT_SUBTYPE);
        super.setLength((short)MINIMUM_LENGTH_TTL_DECREMENT);
    }
    
    @Override
    public void readFrom(ByteBuffer data) {
        super.readFrom(data);
        data.position(data.position() + 6);  // pad
    }
    
    @Override
    public void writeTo(ByteBuffer data) {
        super.writeTo(data);
        data.putShort((short)0); //pad
        data.putInt((int)0); //pad
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(type);
        builder.append("[");
        builder.append("NICIRA-TTL-DECR");
        builder.append("]");
        return builder.toString();
    }
}
