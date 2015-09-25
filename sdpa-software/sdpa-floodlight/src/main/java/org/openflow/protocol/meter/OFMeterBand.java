package org.openflow.protocol.meter;

import java.nio.ByteBuffer;

import org.openflow.util.U16;

/**
 * Corresponds to the struct ofp_meter_band_header OpenFlow structure
 *
 * @author Srini Seetharaman (srini.seetharaman@gmail.com)
 */
public class OFMeterBand implements Cloneable {
    public static int MINIMUM_LENGTH = 12;

    protected OFMeterBandType type;
    protected short length;
    protected int rate;
    protected int burstSize;

    /**
     * @return the type
     */
    public OFMeterBandType getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public OFMeterBand setType(OFMeterBandType type) {
        this.type = type;
        return this;
    }

    /**
     * @return the length
     */
    public short getLength() {
        return length;
    }

    /**
     * Returns the unsigned length
     *
     * @return the length
     */
    public int getLengthU() {
        return U16.f(length);
    }

    /**
     * @param length the length to set
     */
    public void setLength(short length) {
        this.length = length;
    }

    /**
     * @return the rate
     */
    public int getRate() {
        return rate;
    }

    /**
     * @param rate the rate to set
     */
    public OFMeterBand setRate(int rate) {
        this.rate = rate;
        return this;
    }

    /**
     * @return the burst_size
     */
    public int getBurstSize() {
        return burstSize;
    }

    /**
     * @param burstSize the burst_size to set
     */
    public OFMeterBand setBurstSize(int burstSize) {
        this.burstSize = burstSize;
        return this;
    }

    public void readFrom(ByteBuffer data) {
        this.type = OFMeterBandType.valueOf(data.getShort());
        this.length = data.getShort();
        this.rate = data.getInt();
        this.burstSize = data.getInt();
    }

    public void writeTo(ByteBuffer data) {
        data.putShort(this.type.getTypeValue());
        data.putShort(this.length);
        data.putInt(this.rate); 
        data.putInt(this.burstSize);
    }

    @Override
    public int hashCode() {
        final int prime = 2777;
        int result = 1;
        result = prime * result + length;
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + rate;
        result = prime * result + burstSize;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof OFMeterBand))
            return false;
        OFMeterBand other = (OFMeterBand) obj;
        if (length != other.length)
            return false;
        if (type != other.type)
            return false;
        if (rate != other.rate)
            return false;
        if (burstSize != other.burstSize)
            return false;
        return true;
    }

    @Override
    protected OFMeterBand clone() {
        try {
            return (OFMeterBand) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
