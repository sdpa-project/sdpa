package org.openflow.protocol;

import java.nio.ByteBuffer;

import org.openflow.util.U16;

/**
 *
 * @author David Erickson (daviderickson@cs.stanford.edu)
 */
public class OFQueueGetConfigRequest extends OFMessage implements Cloneable {
    public static int MINIMUM_LENGTH = 16;

    protected int portNumber;

    /**
     * 
     */
    public OFQueueGetConfigRequest() {
        super();
        this.type = OFType.QUEUE_GET_CONFIG_REQUEST;
        this.length = U16.t(MINIMUM_LENGTH);
    }

    /**
     * @return the portNumber
     */
    public int getPortNumber() {
        return portNumber;
    }

    /**
     * @param portNumber the port to set
     */
    public OFQueueGetConfigRequest setPortNumber(int portNumber) {
        this.portNumber = portNumber;
        return this;
    }

    @Override
    public void readFrom(ByteBuffer data) {
        super.readFrom(data);
        this.portNumber = data.getInt();
        data.getInt(); // pad
    }

    @Override
    public void writeTo(ByteBuffer data) {
        super.writeTo(data);
        data.putInt(this.portNumber);
        data.putInt(0); // pad
    }

    @Override
    public int hashCode() {
        final int prime = 7211;
        int result = super.hashCode();
        result = prime * result + portNumber;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (!(obj instanceof OFQueueGetConfigRequest))
            return false;
        OFQueueGetConfigRequest other = (OFQueueGetConfigRequest) obj;
        if (portNumber != other.portNumber)
            return false;
        return true;
    }

    @Override
    public OFQueueGetConfigRequest clone() {
        try {
            return (OFQueueGetConfigRequest) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    /* (non-Javadoc)
     * @see org.openflow.protocol.OFMessage#computeLength()
     */
    @Override
    public void computeLength() {
        this.length = (short) MINIMUM_LENGTH;
    }
}
