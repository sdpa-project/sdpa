package org.openflow.protocol.instruction;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.openflow.protocol.action.OFActionType;

/**
 * Represents an ofp_instruction_goto_table
 * @author Srini Seetharaman (srini.seetharaman@gmail.com)
 */
public class OFInstructionGotoTable extends OFInstruction {
    public static int MINIMUM_LENGTH = 8;

    protected byte tableId;

    public OFInstructionGotoTable() {
        super.setType(OFInstructionType.GOTO_TABLE);
        super.setLength((short) MINIMUM_LENGTH);
    }

    public OFInstructionGotoTable(byte tableId) {
        super.setType(OFInstructionType.GOTO_TABLE);
        super.setLength((short) MINIMUM_LENGTH);
        this.tableId = tableId;
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
    public OFInstructionGotoTable setTableId(byte tableId) {
        this.tableId = tableId;
        return this;
    }

    @Override
    public void readFrom(ByteBuffer data) {
        super.readFrom(data);
        this.tableId = data.get();
        data.position(data.position() + 3); // pad
    }

    @Override
    public void writeTo(ByteBuffer data) {
        super.writeTo(data);
        data.put(this.tableId);
        data.put((byte)0); //pad
        data.putShort((short) 0); //pad
    }

    @Override
    public int hashCode() {
        final int prime = 347;
        int result = super.hashCode();
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
        if (!(obj instanceof OFInstructionGotoTable)) {
            return false;
        }
        OFInstructionGotoTable other = (OFInstructionGotoTable) obj;
        if (tableId != other.tableId) {
            return false;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "OFInstructionGotoTable [tableId=" + tableId + "]";
    }
}
