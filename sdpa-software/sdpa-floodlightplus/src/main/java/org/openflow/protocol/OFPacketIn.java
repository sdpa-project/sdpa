package org.openflow.protocol;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.openflow.util.U16;
import org.openflow.util.U32;
import org.openflow.util.U8;

/**
 * Represents an ofp_packet_in
 *
 * @author David Erickson (daviderickson@cs.stanford.edu) - Feb 8, 2010
 */
public class OFPacketIn extends OFMessage {
    public static int MINIMUM_LENGTH = 32;

    public enum OFPacketInReason {
        NO_MATCH, ACTION, INVALID_TTL
    }

    protected int bufferId;
    protected short totalLength;
    protected OFPacketInReason reason;
    protected byte tableId;
    protected long cookie;    
    protected OFMatch match;
    protected byte[] packetData;

    public OFPacketIn() {
        super();
        this.type = OFType.PACKET_IN;
        this.length = U16.t(MINIMUM_LENGTH);
    }

    /**
     * Get buffer_id
     * @return
     */
    public int getBufferId() {
        return this.bufferId;
    }

    /**
     * Set buffer_id
     * @param bufferId
     */
    public OFPacketIn setBufferId(int bufferId) {
        this.bufferId = bufferId;
        return this;
    }

    /**
     * Get cookie
     * @return
     */
    public long getCookie() {
        return this.cookie;
    }

    /**
     * Set cookie
     * @param cookie
     */
    public OFPacketIn setCookie(long cookie) {
        this.cookie = cookie;
        return this;
    }

    /**
     * Get tableId
     * @return
     */
    public byte getTableId() {
        return this.tableId;
    }

    /**
     * Set tableId
     * @param tableId
     */
    public OFPacketIn setTableId(byte tableId) {
        this.tableId = tableId;
        return this;
    }

    /**
     * Get match
     * @return
     */
    public OFMatch getMatch() {
        return this.match;
    }

    /**
     * Set match
     * @param match
     */
    public OFPacketIn setMatch(OFMatch match) {
        this.match = match;
        return this;
    }

    /**
     * Returns the packet data
     * @return
     */
    public byte[] getPacketData() {
        return this.packetData;
    }

    /**
     * Sets the packet data, and updates the length of this message
     * @param packetData
     */
    public OFPacketIn setPacketData(byte[] packetData) {
        this.packetData = packetData;
        this.length = U16.t(OFPacketIn.MINIMUM_LENGTH + packetData.length);
        return this;
    }

    /**
     * Get in_port
     * @return
     */
    public int getInPort() {
        for (OFMatchField matchField: match.matchFields) {
            if (matchField.getType() == OFOXMFieldType.IN_PORT)
                return (Integer) matchField.value;
        }
        return -1;
    }

    /**
     * Get reason
     * @return
     */
    public OFPacketInReason getReason() {
        return this.reason;
    }

    /**
     * Set reason
     * @param reason
     */
    public OFPacketIn setReason(OFPacketInReason reason) {
        this.reason = reason;
        return this;
    }

    /**
     * Get total_len
     * @return
     */
    public short getTotalLength() {
        return this.totalLength;
    }

    /**
     * Set total_len
     * @param totalLength
     */
    public OFPacketIn setTotalLength(short totalLength) {
        this.totalLength = totalLength;
        return this;
    }

    @Override
    public void readFrom(ByteBuffer data) {
        super.readFrom(data);
        this.bufferId = data.getInt();
        this.totalLength = data.getShort();
        this.reason = OFPacketInReason.values()[U8.f(data.get())];
        this.tableId = data.get();
        this.cookie = data.getLong();
        if (this.match == null)
            this.match = new OFMatch();
        this.match.readFrom(data);
        data.getShort(); // pad

        // safeguard in case miss_send_len is left at default value of 128 bytes
        this.packetData = new byte[Math.min(data.remaining(), getTotalLength())];
        data.get(this.packetData);
    }

    @Override
    public void writeTo(ByteBuffer data) {
        super.writeTo(data);
        data.putInt(bufferId);
        data.putShort(totalLength);
        data.put((byte) reason.ordinal());
        data.put(tableId);
        data.putLong(cookie);
        this.match.writeTo(data);
        data.putShort((short) 0x0); // pad
        data.put(this.packetData);
    }

    @Override
    public int hashCode() {
        final int prime = 283;
        int result = super.hashCode();
        result = prime * result + bufferId;
        result = prime * result + Arrays.hashCode(packetData);
        result = prime * result + ((reason == null) ? 0 : reason.hashCode());
        result = prime * result + totalLength;
        result = prime * result + reason.ordinal();
        result = prime * result + tableId;
        result = prime * result + (int) (cookie ^ (cookie >>> 32));
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
        if (!(obj instanceof OFPacketIn)) {
            return false;
        }
        OFPacketIn other = (OFPacketIn) obj;
        if (bufferId != other.bufferId) {
            return false;
        }
        if (!Arrays.equals(packetData, other.packetData)) {
            return false;
        }
        if (reason == null) {
            if (other.reason != null) {
                return false;
            }
        } else if (!reason.equals(other.reason)) {
            return false;
        }
        if (tableId != other.tableId) {
            return false;
        }
        if (cookie != other.cookie) {
            return false;
        }
        if (totalLength != other.totalLength) {
            return false;
        }
        if (match == null) {
            if (other.match != null) {
                return false;
            }
        } else if (!match.equals(other.match)) {
            return false;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "OFPacketIn [bufferId=" + U32.f(bufferId) + ", totalLength=" + totalLength + 
                ", reason=" + reason.ordinal() + ", tableId=" + tableId + ", cookie=" + cookie + 
                ", match=" + match + ", length=" + length + "]";
    }

    /* (non-Javadoc)
     * @see org.openflow.protocol.OFMessage#computeLength()
     */
    @Override
    public void computeLength() {
        int l = MINIMUM_LENGTH - OFMatch.MINIMUM_LENGTH;
        l += match.getLength();
        l += ((packetData != null) ? packetData.length : 0);
        this.length = U16.t(l);
    }

}
