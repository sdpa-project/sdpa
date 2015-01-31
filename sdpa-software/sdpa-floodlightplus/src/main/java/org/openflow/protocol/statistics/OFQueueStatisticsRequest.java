package org.openflow.protocol.statistics;

import java.nio.ByteBuffer;

import org.openflow.protocol.OFQueue;

/**
 * Represents an ofp_queue_stats_request structure
 * @author David Erickson (daviderickson@cs.stanford.edu)
 */
public class OFQueueStatisticsRequest implements OFStatistics {
    public static int MINIMUM_LENGTH = 8;

    protected int portNumber;
    protected int queueId;

    /**
     * @return the portNumber
     */
    public int getPortNumber() {
        return portNumber;
    }

    /**
     * @param portNumber the portNumber to set
     */
    public OFQueueStatisticsRequest setPortNumber(int portNumber) {
        this.portNumber = portNumber;
        return this;
    }

    /**
     * @return the queueId
     */
    public int getQueueId() {
        return queueId;
    }

    /**
     * @param queueId the queueId to set
     */
    public OFQueueStatisticsRequest setQueueId(int queueId) {
        this.queueId = queueId;
        return this;
    }

    /**
     * @param queueId the queueId to set
     */
    public OFQueueStatisticsRequest setQueueId(OFQueue queue) {
        this.queueId = queue.getValue();
        return this;
    }

    @Override
    public int getLength() {
        return MINIMUM_LENGTH;
    }

    @Override
    public void readFrom(ByteBuffer data) {
        this.portNumber = data.getInt();
        this.queueId = data.getInt();
    }

    @Override
    public void writeTo(ByteBuffer data) {
        data.putInt(this.portNumber);
        data.putInt(this.queueId);
    }

    @Override
    public int hashCode() {
        final int prime = 443;
        int result = 1;
        result = prime * result + portNumber;
        result = prime * result + queueId;
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
        if (!(obj instanceof OFQueueStatisticsRequest)) {
            return false;
        }
        OFQueueStatisticsRequest other = (OFQueueStatisticsRequest) obj;
        if (portNumber != other.portNumber) {
            return false;
        }
        if (queueId != other.queueId) {
            return false;
        }
        return true;
    }

    @Override
    public int computeLength() {
        return getLength();
    }
}
