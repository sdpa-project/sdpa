package org.openflow.protocol.meter;

import java.nio.ByteBuffer;

import org.openflow.util.U16;

/**
 * Corresponds to the struct struct ofp_meter_band_drop OpenFlow structure
 *
 * @author Srini Seetharaman (srini.seetharaman@gmail.com)
 */
public class OFMeterBandDrop extends OFMeterBand {
    public static int MINIMUM_LENGTH = 16;

    /**
     * 
     */
    public OFMeterBandDrop() {
        super();
        this.type = OFMeterBandType.DROP;
        this.length = U16.t(MINIMUM_LENGTH);
    }

    @Override
    public void readFrom(ByteBuffer data) {
        super.readFrom(data);
        data.getInt(); // pad
    }

    @Override
    public void writeTo(ByteBuffer data) {
        super.writeTo(data);
        data.putInt(0); // pad
    }

    @Override
    public String toString() {
        return "OFMeterBandDrop [type=" + type + ", length=" + length + ", rate=" + rate +
            ",burstSize=" + burstSize + "]";
    }
}
