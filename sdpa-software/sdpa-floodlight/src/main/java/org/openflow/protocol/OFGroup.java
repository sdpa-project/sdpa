package org.openflow.protocol;

public enum OFGroup {
    OFPG_MAX                ((int)0xffffff00),
    OFPG_ALL                ((int)0xfffffffc),
    OFPG_ANY                ((int)0xffffffff);

    protected int value;

    private OFGroup(int value) {
        this.value = value;
    }

    /**
     * @return the value
     */
    public int getValue() {
        return value;
    }
}
