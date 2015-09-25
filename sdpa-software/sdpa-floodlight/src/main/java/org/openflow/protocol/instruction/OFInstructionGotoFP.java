package org.openflow.protocol.instruction;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.openflow.protocol.action.OFActionType;

/**
 * Represents an ofp_instruction_goto_fp
 * @author chen sun (sunchen2050@163.com)
 */
public class OFInstructionGotoFP extends OFInstruction {
    public static int MINIMUM_LENGTH = 8;

    protected byte bitmap;

    public OFInstructionGotoFP() {
        super.setType(OFInstructionType.GOTO_FP);
        super.setLength((short) MINIMUM_LENGTH);
    }

    public OFInstructionGotoFP(byte bitmap) {
        super.setType(OFInstructionType.GOTO_FP);
        super.setLength((short) MINIMUM_LENGTH);
        this.bitmap = bitmap;
    }

    /**
     * @return the bitmap
     */
    public byte getBitmap() {
        return bitmap;
    }

    /**
     * @param bitmap the bitmap to set
     */
    public OFInstructionGotoFP setBitmap(byte bitmap) {
        this.bitmap = bitmap;
        return this;
    }

    @Override
    public void readFrom(ByteBuffer data) {
        super.readFrom(data);
        this.bitmap = data.get();
        data.position(data.position() + 3); // pad
    }

    @Override
    public void writeTo(ByteBuffer data) {
        super.writeTo(data);
        data.put(this.bitmap);
        data.put((byte)0); //pad
        data.putShort((short) 0); //pad
    }

    @Override
    public int hashCode() {
        final int prime = 347;
        int result = super.hashCode();
        result = prime * result + bitmap;
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
        if (!(obj instanceof OFInstructionGotoFP)) {
            return false;
        }
        OFInstructionGotoFP other = (OFInstructionGotoFP) obj;
        if (bitmap != other.bitmap) {
            return false;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "OFInstructionGotoFP [bitmap=" + bitmap + "]";
    }
}
