package org.openflow.protocol;

import java.nio.ByteBuffer;

/**
 * @author Srini Seetharaman (srini.seetharaman@gmail.com)
 */

public class OFMatchField extends OFOXMField implements Cloneable {
    protected Object mask;

    public OFMatchField() {
        super();
    }
    
    public OFMatchField(OFOXMFieldType type, Object value) {
        super(type, value);
        this.mask = null;
    }

    public OFMatchField(OFOXMFieldType type, Object value, Object mask) {
        super(type, value);
        if (mask != null)
            mask = updateObjectType(mask, type.getPayloadLength());

            if (!isAllZero(mask)) {
                this.length = 4 + 2 * type.getPayloadLength();
    	        this.hasMask = 1;
                this.mask = mask;
    	        //Handling case where OXM value bit 
    	        // should never be 1 when OXM mask bit is 0
    	        updateValue();
            }
    }

    public void updateValue() {
        if (value instanceof Byte) {
            this.value = (byte)((Byte)value & (Byte)mask);
        }
        if (value instanceof Short) {
            this.value = (short)((Short)value & (Short)mask);
        }
        if (value instanceof Integer) {
            this.value = (int)((Integer)value & (Integer)mask);
        }
        if (value instanceof Long) {
            this.value = (long)((Long)value & (Long)mask);
        }
        if (value instanceof byte[]) {
            byte[] v = (byte[])this.value;
            byte[] m = (byte[])this.mask;
            if (v.length != m.length)
                throw new RuntimeException("Value" + v + " and mask " + m 
                        + " are of different lengths in OFMatchField");
            for (int i = 0; i< v.length; i++)
                v[i] = (byte) (v[i] & m[i]);
        }
    }

    public Object getMask() {
        return mask;
    }
    
    public void setMask(Object mask) {
        this.mask = updateObjectType(mask, type.getPayloadLength());
    }

    public void readFrom(ByteBuffer data) {
        super.readFrom(data);
        if (this.hasMask == 1)
            this.mask = readObject(data, this.type.getPayloadLength());
    }

    public void writeTo(ByteBuffer data) {
        super.writeTo(data);
        if (this.hasMask == 1)
            writeObject(data, mask, type.getPayloadLength());
    }

    public int hashCode() {
        final int prime = 367;
        int result = super.hashCode();
        result = result * prime + ((mask==null)? 0: mask.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof OFMatchField)) {
            return false;
        }
        OFMatchField other = (OFMatchField) obj;
        if (mask == null) {
            if (other.mask != null) {
                return false;
            }
        } else if (!mask.equals(other.mask)) {
            return false;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public OFMatchField clone() throws CloneNotSupportedException {
        OFMatchField matchField = (OFMatchField)super.clone();
        matchField.setMask(mask);
        return matchField;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "OFMatchField [type=" + type + ",HasMask=" +
            hasMask + ", length=" + length + ", value=" + value +
            ((hasMask == 1) ? ", mask=" + mask : "") + "]";
    }
}
