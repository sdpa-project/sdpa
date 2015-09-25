package org.openflow.protocol;

public enum OFMeter {
    OFPM_MAX                ((int)0xffff0000),
    OFPM_SLOWPATH           ((int)0xfffffffd),
    OFPM_CONTROLLER         ((int)0xfffffffe),
    OFPM_ALL                ((int)0xffffffff);

    protected int value;

    private OFMeter(int value) {
        this.value = value;
    }

    /**
     * @return the value
     */
    public int getValue() {
        return value;
    }
}
