package org.openflow.protocol.instruction;

import java.nio.ByteBuffer;

import org.openflow.util.U16;

/**
 * The base class for all OpenFlow Instructions.
 *
 * @author Srini Seetharaman (srini.seetharaman@gmail.com)
 */
public class OFInstruction implements Cloneable {
    public static int MINIMUM_LENGTH = 4;
    public static int OFFSET_LENGTH = 2;
    public static int OFFSET_TYPE = 0;

    protected OFInstructionType type;
    protected short length;

    /**
     * Get the length of this message
     *
     * @return
     */
    public short getLength() {
        return length;
    }

    /**
     * Get the length of this message, unsigned
     *
     * @return
     */
    public int getLengthU() {
        return U16.f(length);
    }

    /**
     * Set the length of this message
     *
     * @param length
     */
    public OFInstruction setLength(short length) {
        this.length = length;
        return this;
    }

    /**
     * Get the type of this message
     *
     * @return OFInstructionType enum
     */
    public OFInstructionType getType() {
        return this.type;
    }

    /**
     * Set the type of this message
     *
     * @param type
     */
    public OFInstruction setType(OFInstructionType type) {
        this.type = type;
        return this;
    }

    /**
     * Returns a summary of the message
     * @return "ofmsg=v=$version;t=$type:l=$len:xid=$xid"
     */
    public String toString() {
        return "ofinstruction" +
            ";t=" + this.getType() +
            ";l=" + this.getLength();
    }
    
    /**
     * Given the output from toString(), 
     * create a new OFInstruction
     * @param val
     * @return
     */
    public static OFInstruction fromString(String val) {
        String tokens[] = val.split(";");
        if (!tokens[0].equals("ofinstruction"))
            throw new IllegalArgumentException("expected 'ofinstruction' but got '" + 
                    tokens[0] + "'");
        String type_tokens[] = tokens[1].split("="); 
        String len_tokens[] = tokens[2].split("=");
        OFInstruction instruction = new OFInstruction();
        instruction.setLength(Short.valueOf(len_tokens[1]));
        instruction.setType(OFInstructionType.valueOf(type_tokens[1]));
        return instruction;
    }

    public void readFrom(ByteBuffer data) {
        this.type = OFInstructionType.valueOf(data.getShort());
        this.length = data.getShort();
    }

    public void writeTo(ByteBuffer data) {
        data.putShort(type.getTypeValue());
        data.putShort(length);
    }

    @Override
    public int hashCode() {
        final int prime = 347;
        int result = 1;
        result = prime * result + length;
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof OFInstruction)) {
            return false;
        }
        OFInstruction other = (OFInstruction) obj;
        if (length != other.length) {
            return false;
        }
        if (type == null) {
            if (other.type != null) {
                return false;
            }
        } else if (!type.equals(other.type)) {
            return false;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public OFInstruction clone() throws CloneNotSupportedException {
        return (OFInstruction) super.clone();
    }
    
}
