package org.openflow.protocol.hello;

import org.openflow.util.U16;

import java.util.List;
import java.util.ArrayList;
import java.nio.ByteBuffer;

/**
 * Represents an ofp_hello_element_versionbitmap data
 *
 * @author Srini Seetharaaman (srini.seetharaman@gmail.com)
 */
public class OFHelloElementVersionBitmap extends OFHelloElement {
    public static int MINIMUM_LENGTH = 4;

    protected List<Integer> bitmaps;

    /**
     * Construct a ofp_hello message
     */
    public OFHelloElementVersionBitmap() {
        this.length = U16.t(MINIMUM_LENGTH);
        this.type = OFHelloElementType.VERSION_BITMAP;
    }

    /**
     * Get the length of this message
     * @return length
     */
    public short getLength() {
        return length;
    }

    /**
     * Get the length of this message, unsigned
     * @return
     */
    public int getLengthU() {
        return U16.f(length);
    }

    /**
     * Returns read-only copies of the bitmaps contained in this HelloElement 
     * @return a list of ordered bitmaps
     */
    public List<Integer> getBitmaps() {
        return this.bitmaps;
    }

    /**
     * Sets the list of bitmaps this HelloElement contains
     * @param bitmaps a list of ordered bitmaps
     */
    public OFHelloElementVersionBitmap setBitmaps(List<Integer> bitmaps) {
        this.bitmaps = bitmaps;
        if (bitmaps != null)
            this.length = U16.t(MINIMUM_LENGTH + bitmaps.size() * 4);
        return this;
    }

    public void readFrom(ByteBuffer data) {
        super.readFrom(data);
        int remaining = this.getLengthU() - MINIMUM_LENGTH;
        if (data.remaining() < remaining)
            remaining = data.remaining();
        this.bitmaps = new ArrayList<Integer>();
        while (remaining >= 4) {
            int bitmap = data.getInt();
            this.bitmaps.add(bitmap);
            remaining -= 4;
        }
        int padLength = 8*((length + 7)/8) - length;
        data.position(data.position() + padLength); // pad
    }

    public void writeTo(ByteBuffer data) {
        super.writeTo(data);
        for (int bitmap: bitmaps) {
            data.putInt(bitmap);
        }
        int padLength = 8*((length + 7)/8) - length;
        for (;padLength>0;padLength--)
            data.put((byte)0); //pad
    }

}
