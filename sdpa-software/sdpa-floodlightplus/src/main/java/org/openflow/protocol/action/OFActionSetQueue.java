package org.openflow.protocol.action;

import java.nio.ByteBuffer;

import org.openflow.protocol.OFQueue;

/**
 * Represents an ofp_action_set_queue
 * @author Srini Seetharaman (srini.seetharaman@gmail.com)
 * @author David Erickson (daviderickson@cs.stanford.edu) - Mar 11, 2010
 */
public class OFActionSetQueue extends OFAction {
    public static int MINIMUM_LENGTH = 8;

    protected int queueId;

    public OFActionSetQueue() {
        super.setType(OFActionType.SET_QUEUE);
        super.setLength((short) MINIMUM_LENGTH);
        this.queueId = OFQueue.OFPQ_ALL.getValue(); 
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
    public OFActionSetQueue setQueueId(int queueId) {
        this.queueId = queueId;
        return this;
    }

    @Override
    public void readFrom(ByteBuffer data) {
        super.readFrom(data);
        this.queueId = data.getInt();
    }

    @Override
    public void writeTo(ByteBuffer data) {
        super.writeTo(data);
        data.putInt(this.queueId);
    }

    @Override
    public int hashCode() {
        final int prime = 349;
        int result = super.hashCode();
        result = prime * result + queueId;
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
        if (!(obj instanceof OFActionSetQueue)) {
            return false;
        }
        OFActionSetQueue other = (OFActionSetQueue) obj;
        if (queueId != other.queueId) {
            return false;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "OFActionSetQueue [queueId=" + queueId + "]";
    }
}
