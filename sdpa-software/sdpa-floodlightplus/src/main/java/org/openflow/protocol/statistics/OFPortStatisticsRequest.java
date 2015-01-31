package org.openflow.protocol.statistics;

import java.nio.ByteBuffer;

/**
 * Represents an ofp_port_stats_request structure
 * @author David Erickson (daviderickson@cs.stanford.edu)
 */
public class OFPortStatisticsRequest implements OFStatistics {
    public static int MINIMUM_LENGTH = 8;

    protected int portNumber;

    /**
     * @return the portNumber
     */
    public int getPortNumber() {
        return portNumber;
    }

    /**
     * @param portNumber the portNumber to set
     */
    public OFPortStatisticsRequest setPortNumber(int portNumber) {
        this.portNumber = portNumber;
        return this;
    }

    @Override
    public int getLength() {
        return MINIMUM_LENGTH;
    }

    @Override
    public void readFrom(ByteBuffer data) {
        this.portNumber = data.getInt();
        data.getInt(); // pad
    }

    @Override
    public void writeTo(ByteBuffer data) {
        data.putInt(this.portNumber);
        data.putInt(0); // pad
    }

    @Override
    public int hashCode() {
        final int prime = 433;
        int result = 1;
        result = prime * result + portNumber;
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
        if (!(obj instanceof OFPortStatisticsRequest)) {
            return false;
        }
        OFPortStatisticsRequest other = (OFPortStatisticsRequest) obj;
        if (portNumber != other.portNumber) {
            return false;
        }
        return true;
    }

    @Override
    public int computeLength() {
        return getLength();
    }
}
