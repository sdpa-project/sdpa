package org.openflow.protocol.instruction;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.openflow.protocol.action.OFActionType;

/**
 * Represents an ofp_instruction_goto_table
 * @author Srini Seetharaman (srini.seetharaman@gmail.com)
 */
public class OFInstructionMeter extends OFInstruction {
    public static int MINIMUM_LENGTH = 8;

    protected int meterId;

    public OFInstructionMeter() {
        super.setType(OFInstructionType.METER);
        super.setLength((short) MINIMUM_LENGTH);
    }

    public OFInstructionMeter(int meterId) {
        super.setType(OFInstructionType.METER);
        super.setLength((short) MINIMUM_LENGTH);
        this.meterId = meterId;
    }

    /**
     * @return the meterId
     */
    public int getMeterId() {
        return meterId;
    }

    /**
     * @param meterId the meterId to set
     */
    public OFInstructionMeter setMeterId(int meterId) {
        this.meterId = meterId;
        return this;
    }

    @Override
    public void readFrom(ByteBuffer data) {
        super.readFrom(data);
        this.meterId = data.getInt();
    }

    @Override
    public void writeTo(ByteBuffer data) {
        super.writeTo(data);
        data.putInt(this.meterId);
    }

    @Override
    public int hashCode() {
        final int prime = 347;
        int result = super.hashCode();
        result = prime * result + meterId;
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
        if (!(obj instanceof OFInstructionMeter)) {
            return false;
        }
        OFInstructionMeter other = (OFInstructionMeter) obj;
        if (meterId != other.meterId) {
            return false;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "OFInstructionMeter [meterId=" + meterId + "]";
    }
}
