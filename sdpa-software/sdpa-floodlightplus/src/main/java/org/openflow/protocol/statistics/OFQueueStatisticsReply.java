package org.openflow.protocol.statistics;

import java.nio.ByteBuffer;

/**
 * Represents an ofp_queue_stats structure
 * @author David Erickson (daviderickson@cs.stanford.edu)
 * @author Srini Seetharaman (srini.seetharaman@gmail.com)
 */
public class OFQueueStatisticsReply implements OFStatistics {
    public static int MINIMUM_LENGTH = 40;

    protected int portNumber;
    protected int queueId;
    protected long transmitBytes;
    protected long transmitPackets;
    protected long transmitErrors;
    protected int durationSeconds;
    protected int durationNanoseconds;    

    /**
     * @return the portNumber
     */
    public int getPortNumber() {
        return portNumber;
    }

    /**
     * @param portNumber the portNumber to set
     */
    public OFQueueStatisticsReply setPortNumber(int portNumber) {
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
    public OFQueueStatisticsReply setQueueId(int queueId) {
        this.queueId = queueId;
        return this;
    }

    /**
     * @return the transmitBytes
     */
    public long getTransmitBytes() {
        return transmitBytes;
    }

    /**
     * @param transmitBytes the transmitBytes to set
     */
    public OFQueueStatisticsReply setTransmitBytes(long transmitBytes) {
        this.transmitBytes = transmitBytes;
        return this;
    }

    /**
     * @return the transmitPackets
     */
    public long getTransmitPackets() {
        return transmitPackets;
    }

    /**
     * @param transmitPackets the transmitPackets to set
     */
    public OFQueueStatisticsReply setTransmitPackets(long transmitPackets) {
        this.transmitPackets = transmitPackets;
        return this;
    }

    /**
     * @return the transmitErrors
     */
    public long getTransmitErrors() {
        return transmitErrors;
    }

    /**
     * @param transmitErrors the transmitErrors to set
     */
    public OFQueueStatisticsReply setTransmitErrors(long transmitErrors) {
        this.transmitErrors = transmitErrors;
        return this;
    }

    @Override
    public int getLength() {
        return MINIMUM_LENGTH;
    }

    /**
     * @return the duration_seconds
     */
    public int getDurationSeconds() {
        return durationSeconds;
    }

    /**
     * @param durationSeconds the duration_seconds to set
     */
    public OFQueueStatisticsReply setDurationSeconds(int durationSeconds) {
        this.durationSeconds = durationSeconds;
        return this;
    }

    /**
     * @return the duration_nanoseconds
     */
    public int getDurationNanoseconds() {
        return durationNanoseconds;
    }

    /**
     * @param durationNanoseconds the duration_nanoseconds to set
     */
    public OFQueueStatisticsReply setDurationNanoseconds(int durationNanoseconds) {
        this.durationNanoseconds = durationNanoseconds;
        return this;
    }

    @Override
    public void readFrom(ByteBuffer data) {
        this.portNumber = data.getInt();
        this.queueId = data.getInt();
        this.transmitBytes = data.getLong();
        this.transmitPackets = data.getLong();
        this.transmitErrors = data.getLong();
        this.durationSeconds = data.getInt();
        this.durationNanoseconds = data.getInt();
    }

    @Override
    public void writeTo(ByteBuffer data) {
        data.putInt(this.portNumber);
        data.putInt(this.queueId);
        data.putLong(this.transmitBytes);
        data.putLong(this.transmitPackets);
        data.putLong(this.transmitErrors);
        data.putInt(this.durationSeconds);
        data.putInt(this.durationNanoseconds);        
    }

    @Override
    public int hashCode() {
        final int prime = 439;
        int result = 1;
        result = prime * result + portNumber;
        result = prime * result + queueId;
        result = prime * result
                + (int) (transmitBytes ^ (transmitBytes >>> 32));
        result = prime * result
                + (int) (transmitErrors ^ (transmitErrors >>> 32));
        result = prime * result
                + (int) (transmitPackets ^ (transmitPackets >>> 32));
        result = prime * result + durationSeconds;
        result = prime * result + durationNanoseconds;
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
        if (!(obj instanceof OFQueueStatisticsReply)) {
            return false;
        }
        OFQueueStatisticsReply other = (OFQueueStatisticsReply) obj;
        if (portNumber != other.portNumber) {
            return false;
        }
        if (queueId != other.queueId) {
            return false;
        }
        if (transmitBytes != other.transmitBytes) {
            return false;
        }
        if (transmitErrors != other.transmitErrors) {
            return false;
        }
        if (transmitPackets != other.transmitPackets) {
            return false;
        }
        if (durationSeconds != other.durationSeconds) {
            return false;
        }
        if (durationNanoseconds != other.durationNanoseconds) {
            return false;
        }
        return true;
    }

    @Override
    public int computeLength() {
        return getLength();
    }
}
