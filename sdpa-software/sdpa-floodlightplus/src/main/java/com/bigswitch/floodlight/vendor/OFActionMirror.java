package com.bigswitch.floodlight.vendor;

import java.nio.ByteBuffer;


public class OFActionMirror extends OFActionBigSwitchVendor {
    public final static int MINIMUM_LENGTH = 12;
    public final static int BSN_ACTION_MIRROR = 1;

    protected int destPort;
    protected int vlanTag;
    protected byte copyStage;
    protected byte pad0;
    protected byte pad1;
    protected byte pad2;

    public OFActionMirror(int portNumber) {
        super(BSN_ACTION_MIRROR);
        super.setLength((short) (OFActionBigSwitchVendor.MINIMUM_LENGTH + OFActionMirror.MINIMUM_LENGTH));
        this.destPort = portNumber;
        this.vlanTag = 0;
        this.copyStage = 0;
    }

    public int getDestPort() {
        return destPort;
    }

    public void setDestPort(int destPort) {
        this.destPort = destPort;
    }

    public int getVlanTag() {
        return vlanTag;
    }

    public void setVlanTag(int vlanTag) {
        this.vlanTag = vlanTag;
    }

    public byte getCopyStage() {
        return copyStage;
    }

    public void setCopyStage(byte copyStage) {
        this.copyStage = copyStage;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + copyStage;
        result = prime * result + destPort;
        result = prime * result + pad0;
        result = prime * result + pad1;
        result = prime * result + pad2;
        result = prime * result + vlanTag;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!super.equals(obj)) return false;
        if (getClass() != obj.getClass()) return false;
        OFActionMirror other = (OFActionMirror) obj;
        if (copyStage != other.copyStage) return false;
        if (destPort != other.destPort) return false;
        if (pad0 != other.pad0) return false;
        if (pad1 != other.pad1) return false;
        if (pad2 != other.pad2) return false;
        if (vlanTag != other.vlanTag) return false;
        return true;
    }

    @Override
    public void readFrom(ByteBuffer data) {
        super.readFrom(data);
        this.destPort = data.getInt();
        this.vlanTag = data.getInt();
        this.copyStage = data.get();
        this.pad0 = data.get();
        this.pad1 = data.get();
        this.pad2 = data.get();
    }

    @Override
    public void writeTo(ByteBuffer data) {
        super.writeTo(data);
        data.getInt(this.destPort);
        data.getInt(this.vlanTag);
        data.get(this.copyStage);
        data.get(this.pad0);
        data.get(this.pad1);
        data.get(this.pad2);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(type);
        builder.append("[");
        builder.append("BSN-MIRROR");
        builder.append(", Dest Port: ");
        builder.append(destPort);
        builder.append(", Vlan: ");
        builder.append(vlanTag);
        builder.append(", Copy Stage: ");
        builder.append(copyStage);
        builder.append("]");
        return builder.toString();
    }
}
