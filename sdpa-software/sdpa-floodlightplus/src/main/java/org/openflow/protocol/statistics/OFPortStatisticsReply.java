package org.openflow.protocol.statistics;

import java.nio.ByteBuffer;

/**
 * Represents an ofp_port_stats structure
 * @author David Erickson (daviderickson@cs.stanford.edu)
 * @author Srini Seetharaman (srini.seetharaman@gmail.com)
  */
public class OFPortStatisticsReply implements OFStatistics {
    public static int MINIMUM_LENGTH = 112;

    protected int portNumber;
    protected long receivePackets;
    protected long transmitPackets;
    protected long receiveBytes;
    protected long transmitBytes;
    protected long receiveDropped;
    protected long transmitDropped;
    protected long receiveErrors;
    protected long transmitErrors;
    protected long receiveFrameErrors;
    protected long receiveOverrunErrors;
    protected long receiveCRCErrors;
    protected long collisions;
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
    public OFPortStatisticsReply setPortNumber(int portNumber) {
        this.portNumber = portNumber;
        return this;
    }

    /**
     * @return the receivePackets
     */
    public long getReceivePackets() {
        return receivePackets;
    }

    /**
     * @param receivePackets the receivePackets to set
     */
    public OFPortStatisticsReply setReceivePackets(long receivePackets) {
        this.receivePackets = receivePackets;
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
    public OFPortStatisticsReply setTransmitPackets(long transmitPackets) {
        this.transmitPackets = transmitPackets;
        return this;
    }

    /**
     * @return the receiveBytes
     */
    public long getReceiveBytes() {
        return receiveBytes;
    }

    /**
     * @param receiveBytes the receiveBytes to set
     */
    public OFPortStatisticsReply setReceiveBytes(long receiveBytes) {
        this.receiveBytes = receiveBytes;
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
    public OFPortStatisticsReply setTransmitBytes(long transmitBytes) {
        this.transmitBytes = transmitBytes;
        return this;
    }

    /**
     * @return the receiveDropped
     */
    public long getReceiveDropped() {
        return receiveDropped;
    }

    /**
     * @param receiveDropped the receiveDropped to set
     */
    public OFPortStatisticsReply setReceiveDropped(long receiveDropped) {
        this.receiveDropped = receiveDropped;
        return this;
    }

    /**
     * @return the transmitDropped
     */
    public long getTransmitDropped() {
        return transmitDropped;
    }

    /**
     * @param transmitDropped the transmitDropped to set
     */
    public OFPortStatisticsReply setTransmitDropped(long transmitDropped) {
        this.transmitDropped = transmitDropped;
        return this;
    }

    /**
     * @return the receiveErrors
     */
    public long getreceiveErrors() {
        return receiveErrors;
    }

    /**
     * @param receiveErrors the receiveErrors to set
     */
    public OFPortStatisticsReply setreceiveErrors(long receiveErrors) {
        this.receiveErrors = receiveErrors;
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
    public OFPortStatisticsReply setTransmitErrors(long transmitErrors) {
        this.transmitErrors = transmitErrors;
        return this;
    }

    /**
     * @return the receiveFrameErrors
     */
    public long getReceiveFrameErrors() {
        return receiveFrameErrors;
    }

    /**
     * @param receiveFrameErrors the receiveFrameErrors to set
     */
    public OFPortStatisticsReply setReceiveFrameErrors(long receiveFrameErrors) {
        this.receiveFrameErrors = receiveFrameErrors;
        return this;
    }

    /**
     * @return the receiveOverrunErrors
     */
    public long getReceiveOverrunErrors() {
        return receiveOverrunErrors;
    }

    /**
     * @param receiveOverrunErrors the receiveOverrunErrors to set
     */
    public OFPortStatisticsReply setReceiveOverrunErrors(long receiveOverrunErrors) {
        this.receiveOverrunErrors = receiveOverrunErrors;
        return this;
    }

    /**
     * @return the receiveCRCErrors
     */
    public long getReceiveCRCErrors() {
        return receiveCRCErrors;
    }

    /**
     * @param receiveCRCErrors the receiveCRCErrors to set
     */
    public OFPortStatisticsReply setReceiveCRCErrors(long receiveCRCErrors) {
        this.receiveCRCErrors = receiveCRCErrors;
        return this;
    }

    /**
     * @return the collisions
     */
    public long getCollisions() {
        return collisions;
    }

    /**
     * @param collisions the collisions to set
     */
    public OFPortStatisticsReply setCollisions(long collisions) {
        this.collisions = collisions;
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
    public OFPortStatisticsReply setDurationSeconds(int durationSeconds) {
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
    public OFPortStatisticsReply setDurationNanoseconds(int durationNanoseconds) {
        this.durationNanoseconds = durationNanoseconds;
        return this;
    }

    @Override
    public void readFrom(ByteBuffer data) {
        this.portNumber = data.getInt();
        data.getInt(); // pad
        this.receivePackets = data.getLong();
        this.transmitPackets = data.getLong();
        this.receiveBytes = data.getLong();
        this.transmitBytes = data.getLong();
        this.receiveDropped = data.getLong();
        this.transmitDropped = data.getLong();
        this.receiveErrors = data.getLong();
        this.transmitErrors = data.getLong();
        this.receiveFrameErrors = data.getLong();
        this.receiveOverrunErrors = data.getLong();
        this.receiveCRCErrors = data.getLong();
        this.collisions = data.getLong();
        this.durationSeconds = data.getInt();
        this.durationNanoseconds = data.getInt();
    }

    @Override
    public void writeTo(ByteBuffer data) {
        data.putInt(this.portNumber);
        data.putInt(0); // pad
        data.putLong(this.receivePackets);
        data.putLong(this.transmitPackets);
        data.putLong(this.receiveBytes);
        data.putLong(this.transmitBytes);
        data.putLong(this.receiveDropped);
        data.putLong(this.transmitDropped);
        data.putLong(this.receiveErrors);
        data.putLong(this.transmitErrors);
        data.putLong(this.receiveFrameErrors);
        data.putLong(this.receiveOverrunErrors);
        data.putLong(this.receiveCRCErrors);
        data.putLong(this.collisions);
        data.putInt(this.durationSeconds);
        data.putInt(this.durationNanoseconds);        
    }

    @Override
    public int hashCode() {
        final int prime = 431;
        int result = 1;
        result = prime * result + (int) (collisions ^ (collisions >>> 32));
        result = prime * result + portNumber;
        result = prime * result
                + (int) (receivePackets ^ (receivePackets >>> 32));
        result = prime * result + (int) (receiveBytes ^ (receiveBytes >>> 32));
        result = prime * result
                + (int) (receiveCRCErrors ^ (receiveCRCErrors >>> 32));
        result = prime * result
                + (int) (receiveDropped ^ (receiveDropped >>> 32));
        result = prime * result
                + (int) (receiveFrameErrors ^ (receiveFrameErrors >>> 32));
        result = prime * result
                + (int) (receiveOverrunErrors ^ (receiveOverrunErrors >>> 32));
        result = prime * result
                + (int) (receiveErrors ^ (receiveErrors >>> 32));
        result = prime * result
                + (int) (transmitBytes ^ (transmitBytes >>> 32));
        result = prime * result
                + (int) (transmitDropped ^ (transmitDropped >>> 32));
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
        if (!(obj instanceof OFPortStatisticsReply)) {
            return false;
        }
        OFPortStatisticsReply other = (OFPortStatisticsReply) obj;
        if (collisions != other.collisions) {
            return false;
        }
        if (portNumber != other.portNumber) {
            return false;
        }
        if (receivePackets != other.receivePackets) {
            return false;
        }
        if (receiveBytes != other.receiveBytes) {
            return false;
        }
        if (receiveCRCErrors != other.receiveCRCErrors) {
            return false;
        }
        if (receiveDropped != other.receiveDropped) {
            return false;
        }
        if (receiveFrameErrors != other.receiveFrameErrors) {
            return false;
        }
        if (receiveOverrunErrors != other.receiveOverrunErrors) {
            return false;
        }
        if (receiveErrors != other.receiveErrors) {
            return false;
        }
        if (transmitBytes != other.transmitBytes) {
            return false;
        }
        if (transmitDropped != other.transmitDropped) {
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
