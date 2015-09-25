package org.openflow.protocol.statistics;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.ArrayList;
import org.openflow.protocol.OFPhysicalPort;

/**
 * Represents an ofp_port_desc structure
 * @author Srini Seetharaman (srini.seetharaman@gmail.com)
 */
public class OFPortDescription implements OFStatistics {
    protected OFPhysicalPort port;

    /**
     *
     */
    public OFPortDescription() {
        port = new OFPhysicalPort();
    }

    /**
     * @return the port
     */
    public OFPhysicalPort getPort() {
        return port;
    }

    /**
     * @param port the port to set
     */
    public void setPort(OFPhysicalPort port) {
        this.port = port;
    }

    @Override
    public int getLength() {
        return OFPhysicalPort.MINIMUM_LENGTH;
    }

    @Override
    public void readFrom(ByteBuffer data) {
        port.readFrom(data);
    }

    @Override
    public void writeTo(ByteBuffer data) {
        port.writeTo(data);
    }

    @Override
    public int hashCode() {
        final int prime = 139;
        int result = super.hashCode();
        result = prime * result + port.hashCode();
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
        if (!(obj instanceof OFPortDescription)) {
            return false;
        }
        OFPortDescription other = (OFPortDescription) obj;
        if (!port.equals(other.port)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return port.toString();
    }

    @Override
    public int computeLength() {
        return getLength();
    }
}
