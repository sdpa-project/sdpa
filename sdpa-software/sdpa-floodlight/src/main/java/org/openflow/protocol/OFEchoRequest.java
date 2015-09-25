package org.openflow.protocol;

import java.nio.ByteBuffer;
import java.util.Arrays;
import org.openflow.util.U16;

/**
 * Represents an ofp_echo_request message
 * 
 * @author Rob Sherwood (rob.sherwood@stanford.edu)
 * @author Srini Seetharaman (srini.seetharaman@gmail.com)
 */

public class OFEchoRequest extends OFMessage {
    byte[] payload;

    public OFEchoRequest() {
        super();
        this.type = OFType.ECHO_REQUEST;
    }

    @Override
    public void readFrom(ByteBuffer bb) {
        super.readFrom(bb);
        int datalen = this.getLengthU() - OFMessage.MINIMUM_LENGTH;
        if (datalen > 0) {
            this.payload = new byte[datalen];
            bb.get(payload);
        }
    }

    /**
     * @return the payload
     */
    public byte[] getPayload() {
        return payload;
    }

    /**
     * @param payload
     *            the payload to set
     */
    public OFEchoRequest setPayload(byte[] payload) {
        this.payload = payload;
        return this;
    }

    @Override
    public void writeTo(ByteBuffer bb) {
        super.writeTo(bb);
        if (payload != null)
            bb.put(payload);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Arrays.hashCode(payload);
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        OFEchoRequest other = (OFEchoRequest) obj;
        if (!Arrays.equals(payload, other.payload))
            return false;
        return true;
    }

    /* (non-Javadoc)
     * @see org.openflow.protocol.OFMessage#computeLength()
     */
    @Override
    public void computeLength() {
        this.length = U16.t(OFMessage.MINIMUM_LENGTH + ((payload != null) ? payload.length : 0));
    }
}
