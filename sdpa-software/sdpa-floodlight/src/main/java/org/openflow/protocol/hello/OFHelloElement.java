package org.openflow.protocol.hello;

import org.openflow.util.U16;

import java.nio.ByteBuffer;


/**
 * Represents an ofp_hello_element data
 *
 * @author Srini Seetharaaman (srini.seetharaman@gmail.com)
 */
public class OFHelloElement {
    public static int MINIMUM_LENGTH = 4;

    protected OFHelloElementType type;
    protected short length;

    /**
     * Construct a ofp_hello message
     */
    public OFHelloElement() {
        this.length = U16.t(MINIMUM_LENGTH);
    }

    public OFHelloElementType getType() {
        return type;
    }

    public OFHelloElement setType(OFHelloElementType type) {
        this.type = type;
        return this;
    }

    /**
     * Get the length of this message, unsigned
     *
     * @return
     */
    public int getLengthU() {
        return U16.f(length);
    }

    public short getLength() {
        return length;
    }

    public OFHelloElement setLength(short length) {
        this.length = length;
        return this;
    }

    public void readFrom(ByteBuffer data) {
        this.type = OFHelloElementType.valueOf(data.getShort());
        this.length = data.getShort();
    }

    public void writeTo(ByteBuffer data) {
        data.putShort(type.getTypeValue());
        data.putShort(length);
    }
}
