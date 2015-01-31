package org.openflow.protocol.statistics;

import java.nio.ByteBuffer;

import org.openflow.protocol.OFMatch;
import org.openflow.protocol.OFPort;
import org.openflow.protocol.OFGroup;
import org.openflow.protocol.OFTable;

/**
 * Represents an ofp_flow_stats_request structure
 * @author David Erickson (daviderickson@cs.stanford.edu)
 * @author Srini Seetharaman (srini.seetharaman@gmail.com)
 */
public class OFFlowStatisticsRequest implements OFStatistics {
    public final static int MINIMUM_LENGTH = 40;

    protected OFMatch match;
    protected byte tableId;
    protected int outPort;
    protected int outGroup;
    protected long cookie;
    protected long cookieMask;

    public OFFlowStatisticsRequest() {
        super();
        this.outPort = OFPort.OFPP_ANY.getValue();
        this.outGroup = OFGroup.OFPG_ANY.getValue();
        this.tableId = OFTable.OFPTT_ALL;
        this.cookieMask = 0;
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
    public OFFlowStatisticsRequest setMatch(OFMatch match) {
        this.match = match;
        return this;
    }

    /**
     * @return the tableId
     */
    public byte getTableId() {
        return tableId;
    }

    /**
     * @param tableId the tableId to set
     */
    public OFFlowStatisticsRequest setTableId(byte tableId) {
        this.tableId = tableId;
        return this;
    }

    /**
     * @return the outPort
     */
    public int getOutPort() {
        return outPort;
    }

    /**
     * @param outPort the outPort to set
     */
    public OFFlowStatisticsRequest setOutPort(int outPort) {
        this.outPort = outPort;
        return this;
    }

    /**
     * @return the outGroup
     */
    public int getOutGroup() {
        return outGroup;
    }

    /**
     * @param outGroup the outGroup to set
     */
    public OFFlowStatisticsRequest setOutGroup(int outGroup) {
        this.outGroup = outGroup;
        return this;
    }

    /**
     * Get cookie
     * @return
     */
    public long getCookie() {
        return this.cookie;
    }

    /**
     * Set cookie
     * @param cookie
     */
    public OFFlowStatisticsRequest setCookie(long cookie) {
        this.cookie = cookie;
        return this;
    }

    /**
     * Get cookieMask
     * @return
     */
    public long getCookieMask() {
        return this.cookieMask;
    }

    /**
     * Set cookieMask
     * @param cookieMask
     */
    public OFFlowStatisticsRequest setCookieMask(long cookieMask) {
        this.cookieMask = cookieMask;
        return this;
    }

    @Override
    public int getLength() {
        int l = MINIMUM_LENGTH;
        if (match != null) 
           l += match.getLength() - OFMatch.MINIMUM_LENGTH;
        return l;
    }

    @Override
    public void readFrom(ByteBuffer data) {
        this.tableId = data.get();
        data.get(); // pad
        data.getShort(); // pad
        this.outPort = data.getInt();
        this.outGroup = data.getInt();
        data.getInt(); //pad
        this.cookie = data.getLong();
        this.cookieMask = data.getLong();
        if (this.match == null)
            this.match = new OFMatch();
        this.match.readFrom(data);
    }

    @Override
    public void writeTo(ByteBuffer data) {
        data.put(this.tableId);
        data.put((byte) 0); //pad
        data.putShort((short) 0); //pad
        data.putInt(this.outPort);
        data.putInt(this.outGroup);
        data.putInt(0);
        data.putLong(cookie);
        data.putLong(cookieMask);
        this.match.writeTo(data);
    }

    @Override
    public int hashCode() {
        final int prime = 401;
        int result = 1;
        result = prime * result + ((match == null) ? 0 : match.hashCode());
        result = prime * result + outPort;
        result = prime * result + outGroup;
        result = prime * result + tableId;
        result = prime * result + (int) (cookie ^ (cookie >>> 32));
        result = prime * result + (int) (cookieMask ^ (cookieMask >>> 32));
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
        if (!(obj instanceof OFFlowStatisticsRequest)) {
            return false;
        }
        OFFlowStatisticsRequest other = (OFFlowStatisticsRequest) obj;
        if (match == null) {
            if (other.match != null) {
                return false;
            }
        } else if (!match.equals(other.match)) {
            return false;
        }
        if (outPort != other.outPort) {
            return false;
        }
        if (outGroup != other.outGroup) {
            return false;
        }
        if (tableId != other.tableId) {
            return false;
        }
        if (cookie != other.cookie) {
            return false;
        }
        if (cookieMask != other.cookieMask) {
            return false;
        }
        return true;
    }

    @Override
    public int computeLength() {
        return getLength();
    }
}
