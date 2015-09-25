package org.openflow.protocol.statistics;

import java.nio.ByteBuffer;
import java.util.List;

import org.openflow.protocol.OFMatch;
import org.openflow.protocol.instruction.OFInstruction;
import org.openflow.protocol.factory.OFInstructionFactory;
import org.openflow.protocol.factory.OFInstructionFactoryAware;
import org.openflow.util.U16;

/**
 * Represents an ofp_flow_stats structure
 * @author David Erickson (daviderickson@cs.stanford.edu)
 * @author Srini Seetharaman (srini.seetharaman@gmail.com)
 */
public class OFFlowStatisticsReply implements OFStatistics, OFInstructionFactoryAware {
    public static int MINIMUM_LENGTH = 56;

    protected OFInstructionFactory instructionFactory;
    protected short length = (short) MINIMUM_LENGTH;
    protected byte tableId;
    protected int durationSeconds;
    protected int durationNanoseconds;
    protected short priority;
    protected short idleTimeout;
    protected short hardTimeout;
    protected short flags;
    protected long cookie;
    protected long packetCount;
    protected long byteCount;
    protected OFMatch match;
    protected List<OFInstruction> instructions;

    /**
     * @return the tableId
     */
    public byte getTableId() {
        return tableId;
    }

    /**
     * @param tableId the tableId to set
     */
    public OFFlowStatisticsReply setTableId(byte tableId) {
        this.tableId = tableId;
        return this;
    }

    /**
     * @return the match
     */
    public OFMatch getMatch() {
        return match;
    }

    /**
     * @param match the match to set
     */
    public OFFlowStatisticsReply setMatch(OFMatch match) {
        this.match = match;
        updateLength();
        return this;
    }

    /**
     * @return the durationSeconds
     */
    public int getDurationSeconds() {
        return durationSeconds;
    }

    /**
     * @param durationSeconds the durationSeconds to set
     */
    public OFFlowStatisticsReply setDurationSeconds(int durationSeconds) {
        this.durationSeconds = durationSeconds;
        return this;
    }

    /**
     * @return the durationNanoseconds
     */
    public int getDurationNanoseconds() {
        return durationNanoseconds;
    }

    /**
     * @param durationNanoseconds the durationNanoseconds to set
     */
    public OFFlowStatisticsReply setDurationNanoseconds(int durationNanoseconds) {
        this.durationNanoseconds = durationNanoseconds;
        return this;
    }

    /**
     * @return the priority
     */
    public short getPriority() {
        return priority;
    }

    /**
     * @param priority the priority to set
     */
    public OFFlowStatisticsReply setPriority(short priority) {
        this.priority = priority;
        return this;
    }

    /**
     * @return the idleTimeout
     */
    public short getIdleTimeout() {
        return idleTimeout;
    }

    /**
     * @param idleTimeout the idleTimeout to set
     */
    public OFFlowStatisticsReply setIdleTimeout(short idleTimeout) {
        this.idleTimeout = idleTimeout;
        return this;
    }

    /**
     * @return the hardTimeout
     */
    public short getHardTimeout() {
        return hardTimeout;
    }

    /**
     * @param hardTimeout the hardTimeout to set
     */
    public OFFlowStatisticsReply setHardTimeout(short hardTimeout) {
        this.hardTimeout = hardTimeout;
        return this;
    }

    /**
     * @return the flags
     */
    public short getFlags() {
        return flags;
    }

    /**
     * @param flags the flags to set
     */
    public OFFlowStatisticsReply setFlags(short flags) {
        this.flags = flags;
        return this;
    }

    /**
     * @return the cookie
     */
    public long getCookie() {
        return cookie;
    }

    /**
     * @param cookie the cookie to set
     */
    public OFFlowStatisticsReply setCookie(long cookie) {
        this.cookie = cookie;
        return this;
    }

    /**
     * @return the packetCount
     */
    public long getPacketCount() {
        return packetCount;
    }

    /**
     * @param packetCount the packetCount to set
     */
    public OFFlowStatisticsReply setPacketCount(long packetCount) {
        this.packetCount = packetCount;
        return this;
    }

    /**
     * @return the byteCount
     */
    public long getByteCount() {
        return byteCount;
    }

    /**
     * @param byteCount the byteCount to set
     */
    public OFFlowStatisticsReply setByteCount(long byteCount) {
        this.byteCount = byteCount;
        return this;
    }

    /**
     * @param length the length to set
     */
    public void setLength(short length) {
        this.length = length;
    }

    @Override
    public int getLength() {
        return U16.f(length);
    }

    /**
     * @param instructionFactory the instructionFactory to set
     */
    @Override
    public void setInstructionFactory(OFInstructionFactory instructionFactory) {
        this.instructionFactory = instructionFactory;
    }

    /**
     * @return the instructions
     */
    public List<OFInstruction> getInstructions() {
        return instructions;
    }

    /**
     * @param instructions the instructions to set
     */
    public OFFlowStatisticsReply setInstructions(List<OFInstruction> instructions) {
        this.instructions = instructions;
        updateLength();
        return this;
    }

    @Override
    public void readFrom(ByteBuffer data) {
        this.length = data.getShort();
        this.tableId = data.get();
        data.get(); // pad
        this.durationSeconds = data.getInt();
        this.durationNanoseconds = data.getInt();
        this.priority = data.getShort();
        this.idleTimeout = data.getShort();
        this.hardTimeout = data.getShort();
        this.flags = data.getShort();
        data.getInt(); // pad
        this.cookie = data.getLong();
        this.packetCount = data.getLong();
        this.byteCount = data.getLong();
        if (this.match == null)
            this.match = new OFMatch();
        this.match.readFrom(data);
        if (this.instructionFactory == null)
            throw new RuntimeException("OFInstructionFactory not set");
        this.instructions = this.instructionFactory.parseInstructions(data, getLength() -
                MINIMUM_LENGTH + OFMatch.MINIMUM_LENGTH - match.getLength());
    }

    @Override
    public void writeTo(ByteBuffer data) {
        data.putShort(this.length);
        data.put(this.tableId);
        data.put((byte) 0); //pad
        data.putInt(this.durationSeconds);
        data.putInt(this.durationNanoseconds);
        data.putShort(this.priority);
        data.putShort(this.idleTimeout);
        data.putShort(this.hardTimeout);
        data.putShort(this.flags);
        data.putInt(0); // pad
        data.putLong(this.cookie);
        data.putLong(this.packetCount);
        data.putLong(this.byteCount);
        this.match.writeTo(data);
        if (instructions != null) {
            for (OFInstruction instruction : instructions) {
                instruction.writeTo(data);
            }
        }
    }

    @Override
    public int hashCode() {
        final int prime = 419;
        int result = 1;
        result = prime * result + (int) (byteCount ^ (byteCount >>> 32));
        result = prime * result + (int) (cookie ^ (cookie >>> 32));
        result = prime * result + durationNanoseconds;
        result = prime * result + durationSeconds;
        result = prime * result + hardTimeout;
        result = prime * result + idleTimeout;
        result = prime * result + flags;
        result = prime * result + length;
        result = prime * result + ((match == null) ? 0 : match.hashCode());
        result = prime * result + (int) (packetCount ^ (packetCount >>> 32));
        result = prime * result + priority;
        result = prime * result + tableId;
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
        if (!(obj instanceof OFFlowStatisticsReply)) {
            return false;
        }
        OFFlowStatisticsReply other = (OFFlowStatisticsReply) obj;
        if (byteCount != other.byteCount) {
            return false;
        }
        if (cookie != other.cookie) {
            return false;
        }
        if (durationNanoseconds != other.durationNanoseconds) {
            return false;
        }
        if (durationSeconds != other.durationSeconds) {
            return false;
        }
        if (hardTimeout != other.hardTimeout) {
            return false;
        }
        if (idleTimeout != other.idleTimeout) {
            return false;
        }
        if (flags != other.flags) {
            return false;
        }
        if (length != other.length) {
            return false;
        }
        if (match == null) {
            if (other.match != null) {
                return false;
            }
        } else if (!match.equals(other.match)) {
            return false;
        }
        if (packetCount != other.packetCount) {
            return false;
        }
        if (priority != other.priority) {
            return false;
        }
        if (tableId != other.tableId) {
            return false;
        }
        return true;
    }

    public void updateLength() {
        int l = MINIMUM_LENGTH - OFMatch.MINIMUM_LENGTH;
        if (instructions != null) {
            for (OFInstruction instruction : instructions) {
                l += instruction.getLengthU();
            }
        }
        l += match.getLength();
        this.length = U16.t(l);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "OFFlowStatisticsReply [length=" + length + ", tableId=" + tableId + ", match="
                + match + ", durationSeconds=" + durationSeconds
                + ", durationNanoseconds=" + durationNanoseconds
                + ", priority=" + priority + ", idleTimeout=" + idleTimeout
                + ", hardTimeout=" + hardTimeout + ", flags=" + flags + ", cookie=" + cookie
                + ", packetCount=" + packetCount + ", byteCount=" + byteCount
                + ", instructions=" + instructions + "]";
    }

    @Override
    public int computeLength() {
        return getLength();
    }
}
