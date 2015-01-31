package org.openflow.protocol.action;

import java.nio.ByteBuffer;


/**
 * Represents an ofp_action_copy_ttl_out
 * @author Srini Seetharaman (srini.seetharaman@gmail.com)
 */
public class OFActionCopyTTLOut extends OFAction {
    public static int MINIMUM_LENGTH = 8;

    public OFActionCopyTTLOut() {
        super();
        super.setType(OFActionType.COPY_TTL_OUT);
        super.setLength((short) MINIMUM_LENGTH);
    }

    @Override
    public void readFrom(ByteBuffer data) {
        super.readFrom(data);
        data.getInt(); //pad
    }

    @Override
    public void writeTo(ByteBuffer data) {
        super.writeTo(data);
        data.putInt(0); //pad
    }

    @Override
    public String toString() {
        return "OFActionCopyTTLOut []";
    }
}
