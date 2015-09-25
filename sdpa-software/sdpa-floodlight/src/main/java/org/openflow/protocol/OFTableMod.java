package org.openflow.protocol;

import java.nio.ByteBuffer;
import org.openflow.util.U16;

/**
 * Represents an ofp_table_mod message
 * @author Srini Seetharaman (srini.seetharaman@gmail.com)
 */
public class OFTableMod extends OFMessage {
    public static int MINIMUM_LENGTH = 16;

    protected byte tableId;
    protected int config;

    public OFTableMod() {
        super();
        this.type = OFType.TABLE_MOD;
        this.length = U16.t(MINIMUM_LENGTH);
    }

    /**
     * @return the tableId
     */
    public byte getTableId() {
        return tableId;
    }

    /**
     * @param tableId the tableId to set
     */
    public OFTableMod setTableId(byte tableId) {
        this.tableId = tableId;
        return this;
    }

    /**
     * @return the config
     */
    public int getConfig() {
        return config;
    }

    /**
     * @param config the config to set
     */
    public OFTableMod setConfig(int config) {
        this.config = config;
        return this;
    }

    @Override
    public void readFrom(ByteBuffer data) {
        super.readFrom(data);
        this.tableId = data.get();
        data.position(data.position() + 3); // pad
        this.config = data.getInt();
    }

    @Override
    public void writeTo(ByteBuffer data) {
        super.writeTo(data);
        data.put(this.tableId);
        data.putShort((short)0); // pad
        data.put((byte)0); // pad
        data.putInt(this.config);
    }

    @Override
    public int hashCode() {
        final int prime = 311;
        int result = super.hashCode();
        result = prime * result + config;
        result = prime * result + tableId;
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
        if (!(obj instanceof OFTableMod)) {
            return false;
        }
        OFTableMod other = (OFTableMod) obj;
        if (config != other.config) {
            return false;
        }
        if (tableId != other.tableId) {
            return false;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see org.openflow.protocol.OFMessage#computeLength()
     */
    @Override
    public void computeLength() {
        this.length = (short) MINIMUM_LENGTH;
    }
}
