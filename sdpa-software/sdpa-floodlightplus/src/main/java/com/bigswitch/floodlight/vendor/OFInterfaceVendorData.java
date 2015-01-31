package com.bigswitch.floodlight.vendor;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;

import net.floodlightcontroller.core.web.serializers.ByteArrayMACSerializer;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.nio.ByteBuffer;

public class OFInterfaceVendorData {
    public static int MINIMUM_LENGTH = 32;
    private static int OFP_ETH_ALEN = 6;
    private static int OFP_MAX_PORT_NAME_LEN = 16;

    protected byte[] hardwareAddress;
    protected String name;
    protected int ipv4Addr;
    protected int ipv4AddrMask;

    /**
     * @return the hardwareAddress
     */
    @JsonSerialize(using=ByteArrayMACSerializer.class)
    public byte[] getHardwareAddress() {
        return hardwareAddress;
    }

    /**
     * @param hardwareAddress the hardwareAddress to set
     */
    public void setHardwareAddress(byte[] hardwareAddress) {
        if (hardwareAddress.length != OFP_ETH_ALEN)
            throw new RuntimeException("Hardware address must have length "
                    + OFP_ETH_ALEN);
        this.hardwareAddress = hardwareAddress;
    }

    public int getIpv4Addr() {
        return ipv4Addr;
    }

    public void setIpv4Addr(int ipv4Addr) {
        this.ipv4Addr = ipv4Addr;
    }

    public int getIpv4AddrMask() {
        return ipv4AddrMask;
    }

    public void setIpv4AddrMask(int ipv4AddrMask) {
        this.ipv4AddrMask = ipv4AddrMask;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Write this message's binary format to the specified ByteBuffer
     * @param data
     */
    public void writeTo(ByteBuffer data) {
        data.put(hardwareAddress);
        data.put(new byte[] {0, 0});

        try {
            byte[] name = this.name.getBytes("ASCII");
            if (name.length < OFP_MAX_PORT_NAME_LEN) {
                data.put(name);
                for (int i = name.length; i < OFP_MAX_PORT_NAME_LEN; ++i) {
                    data.put((byte) 0);
                }
            } else {
                data.put(name, 0, 15);
                data.put((byte) 0);
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        data.putInt(ipv4Addr);
        data.putInt(ipv4AddrMask);
    }

    /**
     * Read this message off the wire from the specified ByteBuffer
     * @param data
     */
    public void readFrom(ByteBuffer data) {
        if (this.hardwareAddress == null)
            this.hardwareAddress = new byte[OFP_ETH_ALEN];
        data.get(this.hardwareAddress);
        data.get(new byte[2]);

        byte[] name = new byte[16];
        data.get(name);
        // find the first index of 0
        int index = 0;
        for (byte b : name) {
            if (0 == b)
                break;
            ++index;
        }
        this.name = new String(Arrays.copyOf(name, index),
                Charset.forName("ascii"));
        ipv4Addr = data.getInt();
        ipv4AddrMask = data.getInt();
    }


}