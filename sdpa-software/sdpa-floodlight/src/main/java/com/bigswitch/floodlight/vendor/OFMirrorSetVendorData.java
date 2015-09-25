package com.bigswitch.floodlight.vendor;

import java.nio.ByteBuffer;

public class OFMirrorSetVendorData extends OFBigSwitchVendorData {
    
    /**
     * Opcode/dataType to set mirroring
     */
    public static final int BSN_SET_MIRRORING = 3;

    protected byte reportMirrorPorts;
    protected byte pad1;
    protected byte pad2;
    protected byte pad3;
    
    public OFMirrorSetVendorData() {
        super(BSN_SET_MIRRORING);
        this.reportMirrorPorts=1;
    }

    public byte getReportMirrorPorts() {
        return reportMirrorPorts;
    }

    public void setReportMirrorPorts(byte report) {
        this.reportMirrorPorts = report;
    }
    
    /**
     * @return the total length vendor date
     */
    @Override
    public int getLength() {
        return super.getLength() + 4; // 4 extra bytes
    }
    
    /**
     * Read the vendor data from the channel buffer
     * @param data: the channel buffer from which we are deserializing
     * @param length: the length to the end of the enclosing message
     */
    public void readFrom(ByteBuffer data, int length) {
        super.readFrom(data, length);
        reportMirrorPorts = data.get();
        pad1 = data.get();
        pad2 = data.get();
        pad3 = data.get();
    }
    
    /**
     * Write the vendor data to the channel buffer
     */
    public void writeTo(ByteBuffer data) {
        super.writeTo(data);
        data.put(reportMirrorPorts);
        data.put(pad1);
        data.put(pad2);
        data.put(pad3);
    }
    
}
