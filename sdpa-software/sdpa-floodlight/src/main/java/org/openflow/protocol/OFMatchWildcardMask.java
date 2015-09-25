package org.openflow.protocol;

public enum OFMatchWildcardMask {
    ALL         ((1 << 10) - 1),
    IN_PORT     (1 << 0),
    DL_VLAN     (1 << 1),
    DL_VLAN_PCP (1 << 2),
    DL_TYPE     (1 << 3),
    NW_TOS      (1 << 4),
    NW_PROTO    (1 << 5),
    TP_SRC      (1 << 6),
    TP_DST      (1 << 7),
    MPLS_LABEL  (1 << 8),
    MPLS_TC     (1 << 9); 

    protected int value;

    private OFMatchWildcardMask(int value) {
        this.value = value;
    }

    /**
     * @return the
     * value
     */
    public int getValue() {
        return value;
    }
}
