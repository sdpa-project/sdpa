package org.openflow.protocol;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.openflow.protocol.factory.OFMeterBandFactory;
import org.openflow.protocol.factory.OFMeterBandFactoryAware;
import org.openflow.protocol.meter.OFMeterBand;
import org.openflow.util.U16;

/**
 * Corresponds to the struct ofp_meter_mod OpenFlow structure
 *
 * @author Srini Seetharaman (srini.seetharaman@gmail.com)
 */
public class OFMeterMod extends OFMessage implements Cloneable, OFMeterBandFactoryAware {
    public static int MINIMUM_LENGTH = 16;

    public final static int OFPMC_ADD = 0;
    public final static int OFPMC_MODIFY = 1;
    public final static int OFPMC_DELETE = 2;

    public final static int OFPMF_KBPS = 1 << 0;
    public final static int OFPMF_PKTPS = 1 << 1;
    public final static int OFPMF_BURST = 1 << 2;
    public final static int OFPMF_STATS = 1 << 3;

    protected OFMeterBandFactory meterBandFactory;
    protected int meterId;
    protected short flags;
    protected short command;
    protected List<OFMeterBand> bands;

    /**
     * @return the meterId
     */
    public int getMeterId() {
        return meterId;
    }

    /**
     * @param meterId the meterId to set
     */
    public OFMeterMod setMeterId(int meterId) {
        this.meterId = meterId;
        return this;
    }

    /**
     * @return the flags
     */
    public int getFlags() {
        return flags;
    }

    /**
     * @param flags the flags associated with the meter
     */
    public OFMeterMod setFlags(short flags) {
        this.flags = flags;
        return this;
    }

    /**
     * @return the command
     */
    public short getCommand() {
        return command;
    }

    /**
     * Get the unsigned int command
     * @return
     */
    public int getCommandU() {
        return U16.f(this.command);
    }

    /**
     * @param command the command to set
     */
    public void setCommand(short command) {
        this.command = command;
    }

    /**
     * @return the bands
     */
    public List<OFMeterBand> getBands() {
        return bands;
    }

    /**
     * @param bands the bands to set
     */
    public OFMeterMod setBands(List<OFMeterBand> bands) {
        this.bands = bands;
        return this;
    }

    public void readFrom(ByteBuffer data) {
        this.command = data.getShort();
        this.flags = data.getShort();
        this.meterId = data.getInt();
        if (this.meterBandFactory == null)
            throw new RuntimeException("OFMeterBandFactory not set");
        this.bands = meterBandFactory.parseMeterBands(data,
                U16.f(this.command) - MINIMUM_LENGTH);
    }

    public void writeTo(ByteBuffer data) {
        data.putShort(this.command);
        data.putShort(this.flags);
        data.putInt(this.meterId);
        if (this.bands != null) {
            for (OFMeterBand meterBand : this.bands) {
                meterBand.writeTo(data);
            }
        }
    }

    @Override
    public int hashCode() {
        final int prime = 6367;
        int result = 1;
        result = prime * result + command;
        result = prime * result
                + ((bands == null) ? 0 : bands.hashCode());
        result = prime * result + meterId;
        result = prime * result + flags;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof OFMeterMod))
            return false;
        OFMeterMod other = (OFMeterMod) obj;
        if (command != other.command)
            return false;
        if (bands == null) {
            if (other.bands != null)
                return false;
        } else if (!bands.equals(other.bands))
            return false;
        if (meterId != other.meterId)
            return false;
        if (flags != other.flags)
            return false;
        return true;
    }

    @Override
    public OFMeterMod clone() {
        try {
            OFMeterMod clone = (OFMeterMod) super.clone();
            if (this.bands != null) {
                List<OFMeterBand> meterBands = new ArrayList<OFMeterBand>();
                for (OFMeterBand band : this.bands) {
                    meterBands.add(band);
                }
                clone.setBands(meterBands);
            }
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setMeterBandFactory(
            OFMeterBandFactory meterBandFactory) {
        this.meterBandFactory = meterBandFactory;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "OFMeterMod [meterId=" + meterId + ", bands="
                + bands + "]";
    }

    /**
     * This method computes the command of the OFMeterMod message, both setting
     * the command field and returning the value.
     * @return the command
     */
    public void computeLength() {
        int l = MINIMUM_LENGTH;
        if (this.bands != null) {
            for (OFMeterBand prop : this.bands) {
                l += prop.getLengthU();
            }
        }
        this.length = U16.t(l);
    }
}
