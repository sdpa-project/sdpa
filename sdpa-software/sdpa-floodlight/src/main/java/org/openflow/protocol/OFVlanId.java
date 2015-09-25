package org.openflow.protocol;

public enum OFVlanId {
    OFPVID_PRESENT           ((short)0x1000),
    OFPVID_NONE              ((byte)0x0000);

    protected short value;

    private OFVlanId(short value) {
        this.value = value;
    }

    /**
     * @return the value
     */
    public short getValue() {
        return value;
    }
}
