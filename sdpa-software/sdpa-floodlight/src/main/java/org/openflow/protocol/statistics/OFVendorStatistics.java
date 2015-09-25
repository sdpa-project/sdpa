package org.openflow.protocol.statistics;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * The base class for vendor implemented statistics message
 * @author David Erickson (daviderickson@cs.stanford.edu)
 * @author Srini Seetharaman (srini.seetharaman@gmail.com)
 */
public class OFVendorStatistics implements OFStatistics {
    protected static int MINIMUM_LENGTH = 8;

    protected int vendor;
    protected int vendorType;
    protected byte[] vendorData;

    // non-message fields
    protected int length = MINIMUM_LENGTH;

    /**
     * @return the vendor
     */
    public int getVendor() {
        return vendor;
    }

    /**
     * @param vendor the vendor to set
     */
    public OFVendorStatistics setVendor(int vendor) {
        this.vendor = vendor;
        return this;
    }

    /**
     * @return the experiment type
     */
    public int getVendorType() {
        return vendorType;
    }

    /**
     * @param vendorType the experiment type to set
     */
    public OFVendorStatistics setVendorType(int vendorType) {
        this.vendorType = vendorType;
        return this;
    }

    /**
     * @return the vendorData
     */
    public byte[] getVendorData() {
        return vendorData;
    }

    /**
     * @param vendorData the vendorData to set
     */
    public OFVendorStatistics setVendorData(byte[] vendorData) {
        this.vendorData = vendorData;
        if (vendorData != null)
            this.length = MINIMUM_LENGTH + vendorData.length;
        return this;
    }

    @Override
    public void readFrom(ByteBuffer data) {
        this.vendor = data.getInt();
        this.vendorType = data.getInt();
        if (vendorData == null)
            vendorData = new byte[length - MINIMUM_LENGTH];
        data.get(vendorData);
    }

    @Override
    public void writeTo(ByteBuffer data) {
        data.putInt(this.vendor);
        data.putInt(this.vendorType);
        if (vendorData != null)
            data.put(vendorData);
    }

    @Override
    public int hashCode() {
        final int prime = 457;
        int result = 1;
        result = prime * result + Arrays.hashCode(vendorData);
        result = prime * result + vendor;
        result = prime * result + vendorType;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof OFVendorStatistics)) {
            return false;
        }
        OFVendorStatistics other = (OFVendorStatistics) obj;
        if (!Arrays.equals(vendorData, other.vendorData)) {
            return false;
        }
        if (vendor != other.vendor) {
            return false;
        }
        if (vendorType != other.vendorType) {
            return false;
        }
        return true;
    }

    @Override
    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    @Override
    public int computeLength() {
        return getLength();
    }
}
