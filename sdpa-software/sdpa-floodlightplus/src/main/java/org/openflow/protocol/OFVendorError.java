package org.openflow.protocol;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.openflow.util.U16;

/**
 * Represents an ofp_error_vendor_msg
 *
 * @author Srini Seetharaman (srini.seetharaman@gmail.com)
 */
public class OFVendorError extends OFError {
    public static int MINIMUM_LENGTH = 16;

    protected short expType;
    protected int vendor;
    protected byte[] errorData;

    public OFVendorError() {
        super();
        this.errorType = (short) OFError.OFErrorType.OFPET_VENDOR.ordinal();
        this.length = U16.t(MINIMUM_LENGTH);
    }

    /**
     * Get vendor_id
     * @return
     */
    public int getVendor() {
        return this.vendor;
    }

    /**
     * Set vendor_id
     * @param vendor
     */
    public OFVendorError setVendor(int vendor) {
        this.vendor = vendor;
        return this;
    }

    /**
     * Returns the packet data
     * @return
     */
    public byte[] getPacketData() {
        return this.errorData;
    }

    /**
     * Sets the packet data, and updates the length of this message
     * @param errorData
     */
    public OFVendorError setPacketData(byte[] errorData) {
        this.errorData = errorData;
        this.length = U16.t(OFVendorError.MINIMUM_LENGTH + errorData.length);
        return this;
    }

    /**
     * Get in_port
     * @return
     */
    public short getExpType() {
        return this.expType;
    }

    /**
     * Set in_port
     * @param expType
     */
    public OFVendorError setExpType(short expType) {
        this.expType = expType;
        return this;
    }

    @Override
    public void readFrom(ByteBuffer data) {
        super.readFrom(data);
        this.expType = data.getShort();
        this.vendor = data.getInt();
        this.errorData = new byte[getLengthU() - MINIMUM_LENGTH];
        data.get(this.errorData);
    }

    @Override
    public void writeTo(ByteBuffer data) {
        super.writeTo(data);
        data.putShort(expType);
        data.putInt(vendor);
        data.put(this.errorData);
    }

    @Override
    public int hashCode() {
        final int prime = 283;
        int result = super.hashCode();
        result = prime * result + expType;
        result = prime * result + vendor;
        result = prime * result + Arrays.hashCode(errorData);
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
        if (!(obj instanceof OFVendorError)) {
            return false;
        }
        OFVendorError other = (OFVendorError) obj;
        if (vendor != other.vendor) {
            return false;
        }
        if (expType != other.expType) {
            return false;
        }
        if (!Arrays.equals(errorData, other.errorData)) {
            return false;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see org.openflow.protocol.OFMessage#computeLength()
     */
    @Override
    public void computeLength() {
        this.length = U16.t(MINIMUM_LENGTH + ((errorData != null) ? errorData.length : 0));
    }
}
