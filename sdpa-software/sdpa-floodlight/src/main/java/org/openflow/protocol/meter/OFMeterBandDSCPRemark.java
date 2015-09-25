package org.openflow.protocol.meter;

import java.nio.ByteBuffer;

import org.openflow.util.U8;
import org.openflow.util.U16;

/**
 * Corresponds to the struct struct ofp_meter_band_dscp_remark OpenFlow structure
 *
 * @author Srini Seetharaman (srini.seetharaman@gmail.com)
 */
public class OFMeterBandDSCPRemark extends OFMeterBand {
    public static int MINIMUM_LENGTH = 16;

    protected byte precedenceLevel;

    /**
     * 
     */
    public OFMeterBandDSCPRemark() {
        super();
        this.type = OFMeterBandType.DSCP_REMARK;
        this.length = U16.t(MINIMUM_LENGTH);
    }

    /**
     * @return the precedenceLevel
     */
    public byte getPrecedenceLevel() {
        return precedenceLevel;
    }

    /**
     * @param precedenceLevel the precedenceLevel to set
     */
    public OFMeterBandDSCPRemark setPrecedenceLevel(byte precedenceLevel) {
        this.precedenceLevel = precedenceLevel;
        return this;
    }

    @Override
    public void readFrom(ByteBuffer data) {
        super.readFrom(data);
        this.precedenceLevel = data.get();
        data.getInt(); // pad
        data.getShort(); // pad
    }

    @Override
    public void writeTo(ByteBuffer data) {
        super.writeTo(data);
        data.put(this.precedenceLevel);
        data.putInt(0); // pad
        data.putShort((byte) 0); // pad
    }

    @Override
    public int hashCode() {
        final int prime = 3259;
        int result = super.hashCode();
        result = prime * result + precedenceLevel;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (!(obj instanceof OFMeterBandDSCPRemark))
            return false;
        OFMeterBandDSCPRemark other = (OFMeterBandDSCPRemark) obj;
        if (precedenceLevel != other.precedenceLevel)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "OFMeterBandDSCPRemark [type=" + type + ", length=" + length + ", rate=" + rate +
            ",burstSize=" + burstSize + ", precedenceLevel=" + U8.f(precedenceLevel) + "]";
    }
}
