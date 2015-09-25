package org.openflow.protocol.instruction;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Represents an ofp_instruction_write_metadata
 * @author Srini Seetharaman (srini.seetharaman@gmail.com)
 */
public class OFInstructionWriteMetaData extends OFInstruction {
    public static int MINIMUM_LENGTH = 24;

    protected long metaData;
    protected long metaDataMask;

    public OFInstructionWriteMetaData() {
        super.setType(OFInstructionType.WRITE_METADATA);
        super.setLength((short) MINIMUM_LENGTH);
        this.metaDataMask = Long.MAX_VALUE;
    }

    public OFInstructionWriteMetaData(long metaData) {
        super.setType(OFInstructionType.WRITE_METADATA);
        super.setLength((short) MINIMUM_LENGTH);
        this.metaData = metaData;
        this.metaDataMask = Long.MAX_VALUE;
    }

    public OFInstructionWriteMetaData(long metaData, long metaDataMask) {
        super.setType(OFInstructionType.WRITE_METADATA);
        super.setLength((short) MINIMUM_LENGTH);
        this.metaData = metaData;
        this.metaDataMask = metaDataMask;
    }

    /**
     * Get metaData
     * @return
     */
    public long getMetaData() {
        return this.metaData;
    }

    /**
     * Set metaData
     * @param metaData
     */
    public OFInstructionWriteMetaData setMetaData(long metaData) {
        this.metaData = metaData;
        return this;
    }

    /**
     * Get metaDataMask
     * @return
     */
    public long getMetaDataMask() {
        return this.metaDataMask;
    }

    /**
     * Set metaDataMask
     * @param metaDataMask
     */
    public OFInstructionWriteMetaData setMetaDataMask(long metaDataMask) {
        this.metaDataMask = metaDataMask;
        return this;
    }

    @Override
    public void readFrom(ByteBuffer data) {
        super.readFrom(data);
        data.position(data.position() + 4); // pad
        this.metaData = data.getLong();
        this.metaDataMask = data.getLong();
    }

    @Override
    public void writeTo(ByteBuffer data) {
        super.writeTo(data);
        data.putInt((int) 0); // pad
        data.putLong(metaData);
        data.putLong(metaDataMask);
    }

    @Override
    public int hashCode() {
        final int prime = 347;
        int result = super.hashCode();
        result = prime * result + (int) (metaData ^ (metaData >>> 32));
        result = prime * result + (int) (metaDataMask ^ (metaDataMask >>> 32));
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
        if (!(obj instanceof OFInstructionWriteMetaData)) {
            return false;
        }
        OFInstructionWriteMetaData other = (OFInstructionWriteMetaData) obj;
        if (metaData != other.metaData) {
            return false;
        }
        if (metaDataMask != other.metaDataMask) {
            return false;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "OFInstructionWriteMetaData [metaData=" + metaData + ", metaDataMask=" + metaDataMask + "]";
    }

}
