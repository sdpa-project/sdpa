package com.bigswitch.floodlight.vendor;

import java.nio.ByteBuffer;


/**
 * Class that represents the vendor data in the netmask table request
 * extension implemented by Arista switches
 * 
 * @author munish_mehta (munish.mehta@bigswitch.com)
 */

public class OFNetmaskVendorData extends OFBigSwitchVendorData {

    /**
     * Table index for set or get of the the entry from netmask table
     */
    protected byte tableIndex;
    protected byte pad1;
    protected byte pad2;
    protected byte pad3;
    protected int  netMask;
    
    public OFNetmaskVendorData(int dataType) {
        super(dataType);
        this.tableIndex = 0;
        this.netMask = (int)0xffffffffL;
    }

    public OFNetmaskVendorData(int dataType, byte table_index, int netmask) {
        super(dataType);
        this.tableIndex = table_index;
        this.netMask = netmask;
    }


    public byte getTableIndex() {
        return tableIndex;
    }

    public void setTableIndex(byte tableIndex) {
        this.tableIndex = tableIndex;
    }

    public int getNetMask() {
        return netMask;
    }

    public void setNetMask(int netMask) {
        this.netMask = netMask;
    }

    /**
     * @return the total length of the netmask vendor data
     */
    @Override
    public int getLength() {
        return super.getLength() + 8; // 8 extra bytes
    }
    
    /**
     * Read the vendor data from the channel buffer
     * @param data: the channel buffer from which we are deserializing
     * @param length: the length to the end of the enclosing message
     */
    public void readFrom(ByteBuffer data, int length) {
        super.readFrom(data, length);
        tableIndex = data.get();
        pad1 = data.get();
        pad2 = data.get();
        pad3 = data.get();
        netMask = data.getInt();
    }
    
    /**
     * Write the vendor data to the channel buffer
     */
    public void writeTo(ByteBuffer data) {
        super.writeTo(data);
        data.put(tableIndex);
        data.put(pad1);
        data.put(pad2);
        data.put(pad3);
        data.putInt(netMask);
    }
    

}
