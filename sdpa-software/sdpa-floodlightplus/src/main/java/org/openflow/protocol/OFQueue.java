package org.openflow.protocol;

public enum OFQueue {
    OFPQ_ALL                (0xffffffff),
    OFPQ_MIN_RATE_UNCFG     (0xffff),
    OFPQ_MAX_RATE_UNCFG     (0xffff);

    protected int queueId;

    OFQueue(int queueId) {
        this.queueId = queueId;
    }

    /**
     * @return the queueId
     */
    public int getValue() {
        return queueId;
    }
}
