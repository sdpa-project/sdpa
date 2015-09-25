/**
 * @author David Erickson (daviderickson@cs.stanford.edu) - Mar 11, 2010
 */
package org.openflow.protocol.action;

import java.nio.ByteBuffer;

import org.openflow.util.U8;

/**
 * Represents an ofp_action_set_nw_ttl
 * @author David Erickson (daviderickson@cs.stanford.edu) - Mar 11, 2010
 */
public class OFActionSetNwTTL extends OFAction {
    public static int MINIMUM_LENGTH = 8;

    // Should only be used when the bottom of stack bit is set,
    // else it should be 0x8847 or 0x8848
    protected byte nwTTL; 

    public OFActionSetNwTTL() {
        super.setType(OFActionType.SET_NW_TTL);
        super.setLength((short) MINIMUM_LENGTH);
    }

    public OFActionSetNwTTL(byte nwTTL) {
        super.setType(OFActionType.SET_NW_TTL);
        super.setLength((short) MINIMUM_LENGTH);
        this.nwTTL = nwTTL;
    }

    /**
     * @return the nwTTL
     */
    public byte getNwTTL() {
        return nwTTL;
    }

    /**
     * @param nwTTL the nwTTL to set
     */
    public OFActionSetNwTTL setNwTTL(byte nwTTL) {
        this.nwTTL = nwTTL;
        return this;
    }

    @Override
    public void readFrom(ByteBuffer data) {
        super.readFrom(data);
        this.nwTTL = data.get();
        data.getShort(); //pad
        data.get(); //pad
    }

    @Override
    public void writeTo(ByteBuffer data) {
        super.writeTo(data);
        data.put(this.nwTTL);
        data.putShort((short) 0); //pad
        data.put((byte) 0); //pad
    }

    @Override
    public int hashCode() {
        final int prime = 383;
        int result = super.hashCode();
        result = prime * result + nwTTL;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof OFActionSetNwTTL)) {
            return false;
        }
        OFActionSetNwTTL other = (OFActionSetNwTTL) obj;
        if (nwTTL != other.nwTTL) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "OFActionSetNwTTL [nwTTL="
                + U8.f(nwTTL) + "]";
    }
}
