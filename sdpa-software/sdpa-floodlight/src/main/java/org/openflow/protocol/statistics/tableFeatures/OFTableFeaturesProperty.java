package org.openflow.protocol.statistics.tableFeatures;

import java.nio.ByteBuffer;

import org.openflow.util.U16;

/**
 * The base class for all OpenFlow TableFeaturesProperty
 *
 * @author Srini Seetharaman (srini.seetharaman@gmail.com)
 */
public class OFTableFeaturesProperty implements Cloneable {
    public static int MINIMUM_LENGTH = 4;
    public static int OFFSET_LENGTH = 2;
    public static int OFFSET_TYPE = 0;

    protected OFTableFeaturesPropertyType type;
    protected short length;

    /**
     * Get the length of this message
     *
     * @return
     */
    public short getLength() {
        return length;
    }

    /**
     * Get the length of this message, unsigned
     *
     * @return
     */
    public int getLengthU() {
        return U16.f(length);
    }

    /**
     * Set the length of this message
     *
     * @param length
     */
    public OFTableFeaturesProperty setLength(short length) {
        this.length = length;
        return this;
    }

    /**
     * Get the type of this message
     *
     * @return OFTableFeaturesPropertyType enum
     */
    public OFTableFeaturesPropertyType getType() {
        return this.type;
    }

    /**
     * Set the type of this message
     *
     * @param type
     */
    public OFTableFeaturesProperty setType(OFTableFeaturesPropertyType type) {
        this.type = type;
        return this;
    }

    /**
     * Returns a summary of the message
     * @return string summary of message
     */
    public String toString() {
        return "OFTableFeaturesProperty [type=" + type + ", length=" + length + "]";
    }
    
    public void readFrom(ByteBuffer data) {
        this.type = OFTableFeaturesPropertyType.valueOf(data.getShort());
        this.length = data.getShort();
    }

    public void writeTo(ByteBuffer data) {
        data.putShort(type.getTypeValue());
        data.putShort(length);
    }

    @Override
    public int hashCode() {
        final int prime = 347;
        int result = 1;
        result = prime * result + length;
        result = prime * result + ((type == null) ? 0 : type.hashCode());
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
        if (!(obj instanceof OFTableFeaturesProperty)) {
            return false;
        }
        OFTableFeaturesProperty other = (OFTableFeaturesProperty) obj;
        if (length != other.length) {
            return false;
        }
        if (type == null) {
            if (other.type != null) {
                return false;
            }
        } else if (!type.equals(other.type)) {
            return false;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public OFTableFeaturesProperty clone() throws CloneNotSupportedException {
        return (OFTableFeaturesProperty) super.clone();
    }
    
}
