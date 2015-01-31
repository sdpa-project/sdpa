package com.bigswitch.floodlight.vendor;

import java.nio.ByteBuffer;

public class OFBsnL2TableVendorData extends OFBigSwitchVendorData {
    /*
     * uint8_t l2_table_enable;    // 1 == enabled, 0 == disabled
     * uint8_t pad;
     * uint16_t l2_table_priority;  // priority of all flows in L2 table
     * uint8_t pad[4];
     */
    protected boolean l2TableEnabled;
    protected short l2TablePriority;
    
    
    public OFBsnL2TableVendorData(int dataType) {
        super(dataType);
        this.l2TableEnabled = false;
        this.l2TablePriority = (short)0;
    }


    public OFBsnL2TableVendorData(int dataType, boolean l2TableEnabled,
                                  short l2TablePriority) {
        super(dataType);
        this.l2TableEnabled = l2TableEnabled;
        this.l2TablePriority = l2TablePriority;
    }


    public boolean isL2TableEnabled() {
        return l2TableEnabled;
    }


    public short getL2TablePriority() {
        return l2TablePriority;
    }


    public void setL2TableEnabled(boolean l2TableEnabled) {
        this.l2TableEnabled = l2TableEnabled;
    }


    public void setL2TablePriority(short l2TablePriority) {
        this.l2TablePriority = l2TablePriority;
    }
    
    
    @Override
    public int getLength() {
        return super.getLength() + 8; // 8 additional bytes
    }
    
    /*
     * (non-Javadoc)
     * @see com.bigswitch.floodlight.vendor.OFBigSwitchVendorData#readFrom(java.nio.ByteBuffer, int)
     */
    @Override 
    public void readFrom(ByteBuffer data, int length) {
        super.readFrom(data, length);
        l2TableEnabled = (data.get() == 0) ? false : true;
        data.get();  // pad
        l2TablePriority = data.getShort();
        data.getInt();   // 4 bad bytes
    }
    
    /*
     * (non-Javadoc)
     * @see com.bigswitch.floodlight.vendor.OFBigSwitchVendorData#writeTo(java.nio.ByteBuffer)
     */
    @Override
    public void writeTo(ByteBuffer data) {
        super.writeTo(data);
        data.put((byte)(isL2TableEnabled() ? 1 : 0));
        data.put((byte)0);  // pad
        data.putShort(l2TablePriority);
        data.putInt(0);   // 4 pad bytes
    }
}
