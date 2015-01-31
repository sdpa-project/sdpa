package org.openflow.protocol;

import java.nio.ByteBuffer;
import java.util.EnumSet;

import org.openflow.util.U16;
import org.openflow.util.U8;

/**
 * Represents a features reply message
 * @author David Erickson (daviderickson@cs.stanford.edu)
 * @author Srini Seetharaman (srini.seetharaman@gmail.com)
 *
 */
public class OFFeaturesReply extends OFMessage {
    public static int MINIMUM_LENGTH = 32;

    /**
     * Corresponds to bits on the capabilities field
     */
    public enum OFCapabilities {
        OFPC_FLOW_STATS     (1 << 0),
        OFPC_TABLE_STATS    (1 << 1),
        OFPC_PORT_STATS     (1 << 2),
        OFPC_GROUP_STATS    (1 << 3),
        OFPC_IP_REASM       (1 << 5),
        OFPC_QUEUE_STATS    (1 << 6),
        OFPC_PORT_BLOCKED   (1 << 8);

        protected int value;
        
        private OFCapabilities(int value) {
            this.value = value;
        }
        
        /**
         * Given a capabilities value, return the list of OFCapabilities
         * associated with it
         *
         * @param i capabilities value
         * @return EnumSet<OFCapabilities>
         */
    
        public static EnumSet<OFCapabilities> valueOf(int i) {
            EnumSet<OFCapabilities> capabilities = EnumSet.noneOf(OFCapabilities.class);
            for (OFCapabilities value: OFCapabilities.values()) {
                if ((i & value.getValue()) != 0)
                    capabilities.add(value);
            }
            return capabilities;
        }

        /**
         * @return the value
         */
        public int getValue() {
            return value;
        }
    }

    protected long datapathId;
    protected int buffers;
    protected byte tables;
    protected byte auxiliaryId;
    protected int capabilities;
    protected int reserved;

    public OFFeaturesReply() {
        super();
        this.type = OFType.FEATURES_REPLY;
        this.length = U16.t(MINIMUM_LENGTH);
    }

    /**
     * @return the datapathId
     */
    public long getDatapathId() {
        return datapathId;
    }

    /**
     * @param datapathId the datapathId to set
     */
    public OFFeaturesReply setDatapathId(long datapathId) {
        this.datapathId = datapathId;
        return this;
    }

    /**
     * @return the buffers
     */
    public int getBuffers() {
        return buffers;
    }

    /**
     * @param buffers the buffers to set
     */
    public OFFeaturesReply setBuffers(int buffers) {
        this.buffers = buffers;
        return this;
    }

    /**
     * @return the tables
     */
    public byte getTables() {
        return tables;
    }

    /**
     * @param tables the tables to set
     */
    public OFFeaturesReply setTables(byte tables) {
        this.tables = tables;
        return this;
    }

    /**
     * @return the auxiliaryId
     */
    public int getAuxiliaryId() {
        return auxiliaryId;
    }

    /**
     * @param capabilities the capabilities to set
     */
    public OFFeaturesReply setAuxiliaryId(byte auxiliaryId) {
        this.auxiliaryId = auxiliaryId;
        return this;
    }

    /**
     * @return the capabilities
     */
    public int getCapabilities() {
        return capabilities;
    }

    /**
     * @param capabilities the capabilities to set
     */
    public OFFeaturesReply setCapabilities(int capabilities) {
        this.capabilities = capabilities;
        return this;
    }

    /**
     * @return the reserved
     */
    public int getActions() {
        return reserved;
    }

    /**
     * @param reserved the reserved to set
     */
    public OFFeaturesReply setActions(int reserved) {
        this.reserved = reserved;
        return this;
    }


    @Override
    public void readFrom(ByteBuffer data) {
        super.readFrom(data);
        this.datapathId = data.getLong();
        this.buffers = data.getInt();
        this.tables = data.get();
        this.auxiliaryId = data.get();
        data.position(data.position() + 2); // pad
        this.capabilities = data.getInt();
        this.reserved = data.getInt();
    }

    @Override
    public void writeTo(ByteBuffer data) {
        super.writeTo(data);
        data.putLong(this.datapathId);
        data.putInt(this.buffers);
        data.put(this.tables);
        data.put(this.auxiliaryId);
        data.putShort((short) 0); // pad
        data.putInt(this.capabilities);
        data.putInt(this.reserved);
    }

    @Override
    public int hashCode() {
        final int prime = 139;
        int result = super.hashCode();
        result = prime * result + reserved;
        result = prime * result + buffers;
        result = prime * result + capabilities;
        result = prime * result + (int) (datapathId ^ (datapathId >>> 32));
        result = prime * result + tables;
        result = prime * result + auxiliaryId;
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
        if (!(obj instanceof OFFeaturesReply)) {
            return false;
        }
        OFFeaturesReply other = (OFFeaturesReply) obj;
        if (reserved != other.reserved) {
            return false;
        }
        if (buffers != other.buffers) {
            return false;
        }
        if (capabilities != other.capabilities) {
            return false;
        }
        if (datapathId != other.datapathId) {
            return false;
        }
        if (tables != other.tables) {
            return false;
        }
        if (auxiliaryId != other.auxiliaryId) {
            return false;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see org.openflow.protocol.OFMessage#computeLength()
     */
    @Override
    public void computeLength() {
        this.length = U16.t(MINIMUM_LENGTH);
    }
    
    @Override
    public String toString() {
        return "OFFeaturesReply [datapathId=" + Long.toHexString(datapathId) + 
                ", buffers=" + buffers + ", tables=" + U8.f(tables) +
                ", auxiliaryId=" + U8.f(auxiliaryId) + ", capabilities=" + 
                OFCapabilities.valueOf(capabilities) + ", reserved=" + reserved + "]";
    }
}
