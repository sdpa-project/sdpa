/**
 * @author David Erickson (daviderickson@cs.stanford.edu) - Mar 11, 2010
 */
package org.openflow.protocol.action;

import java.nio.ByteBuffer;

import org.openflow.util.U8;

/**
 * Represents an ofp_action_set_mpls_ttl
 * @author David Erickson (daviderickson@cs.stanford.edu) - Mar 11, 2010
 */
public class OFActionSetMPLSTTL extends OFAction {
    public static int MINIMUM_LENGTH = 8;

    // Should only be used when the bottom of stack bit is set,
    // else it should be 0x8847 or 0x8848
    protected byte mplsTTL; 

    public OFActionSetMPLSTTL() {
        super.setType(OFActionType.SET_MPLS_TTL);
        super.setLength((short) MINIMUM_LENGTH);
    }

    public OFActionSetMPLSTTL(byte mplsTTL) {
        super.setType(OFActionType.SET_MPLS_TTL);
        super.setLength((short) MINIMUM_LENGTH);
        this.mplsTTL = mplsTTL;
    }

    /**
     * @return the mplsTTL
     */
    public byte getMPLSTTL() {
        return mplsTTL;
    }

    /**
     * @param mplsTTL the mplsTTL to set
     */
    public OFActionSetMPLSTTL setMPLSTTL(byte mplsTTL) {
        this.mplsTTL = mplsTTL;
        return this;
    }

    @Override
    public void readFrom(ByteBuffer data) {
        super.readFrom(data);
        this.mplsTTL = data.get();
        data.getShort(); //pad
        data.get(); //pad
    }

    @Override
    public void writeTo(ByteBuffer data) {
        super.writeTo(data);
        data.put(this.mplsTTL);
        data.putShort((short) 0); //pad
        data.put((byte) 0); //pad
    }

    @Override
    public int hashCode() {
        final int prime = 383;
        int result = super.hashCode();
        result = prime * result + mplsTTL;
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
        if (!(obj instanceof OFActionSetMPLSTTL)) {
            return false;
        }
        OFActionSetMPLSTTL other = (OFActionSetMPLSTTL) obj;
        if (mplsTTL != other.mplsTTL) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "OFActionSetMPLSTTL [mplsTTL="
                + U8.f(mplsTTL) + "]";
    }
}
