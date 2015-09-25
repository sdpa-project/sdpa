package org.openflow.protocol;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.openflow.protocol.OFOXMFieldType;
import org.openflow.util.HexString;

/**
 * @author Srini Seetharaman (srini.seetharaman@gmail.com)
 */
public class OFOXMField implements Cloneable {
    public static int MINIMUM_LENGTH = 4;

    protected OFOXMFieldType type;
    protected Object value;
    protected byte hasMask;
    protected int length; //Length including the OXM TLV header

    public OFOXMField() {
        this.length = 0;
        this.hasMask = 0;
    }
    
    public OFOXMField(OFOXMFieldType type, Object value) {
        this.type = type;
        this.hasMask = 0;
        this.length = 4 + type.getPayloadLength();
        this.value = updateObjectType(value, type.getPayloadLength());
    }

    public OFOXMField(int header, Object value) {
        byte typeVal = (byte)((header >> 9) & 0x7F);
        this.type = OFOXMFieldType.valueOf(typeVal);
        this.hasMask = (byte) ((header >> 8) & 0x1);
        this.length = 4 + (header & 0xFF);
        this.value = updateObjectType(value, type.getPayloadLength());
    }

    public OFOXMFieldType getType() {
        return type;
    }

    public int getHasMask() {
        return hasMask;
    }
    public int getLength() {
        return length;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = updateObjectType(value, type.getPayloadLength());
    }

    public int getHeader() {
        int payloadLength = type.getPayloadLength();
        if (hasMask != 0) 
            payloadLength *= 2; 

        return (type.getMatchClass() << 16)
            | (type.getValue() << 9)
            | (hasMask << 8)
            | ((byte)payloadLength);
    }

    public static Object updateObjectType(Object val, int length) {
        if (val instanceof Number) {
            switch (length) {
            case 1: 
                return (Byte)(((Number)val).byteValue());
            case 2: 
                return (Short)(((Number)val).shortValue());
            case 4: 
                return (Integer)(((Number)val).intValue());
            case 8:
                return (Long)(((Number)val).longValue());
            }
        }
        //Default is to retain same object
        return val;
    }

    public boolean isAllZero(Object val) {
        if (val == null)
            return true;

        if (val instanceof Byte) {
            return ((Byte)val == 0);
        }
        if (val instanceof Short) {
            return ((Short)val== 0);
        }
        if (val instanceof Integer) {
            return ((Integer)val == 0);
        }
        if (val instanceof Long) {
            return ((Long)val == 0);
        }
        if (val instanceof byte[]) {
            for (byte b: (byte[])val)
                if (b != 0)
                    return false;
            return true;
        }
        //TODO: error check
        return false;
    }

    public Object readObject(ByteBuffer data, int length)
    {
        switch (length) {
            case 1: 
                return new Byte(data.get());
            case 2: 
                return new Short(data.getShort());
            case 4: 
                return new Integer(data.getInt());
            case 8:
                return new Long(data.getLong());
            default:
                byte val[] = new byte[length];
                data.get(val, 0, length);
                return val;
        }
    }       
    
    public void readFrom(ByteBuffer data) {
        int header = data.getInt();
        short matchClass =  (short) (header >> 16);
        byte value = (byte) ((header >> 9) & 0x7f);
        
        //TODO: Sanity check the field payload length reported

        this.hasMask = (byte) ((header >> 8) & 1);
        this.length = 4 + (header & 0xff);
        this.type = OFOXMFieldType.valueOf(value);
        this.value = readObject(data, this.type.getPayloadLength());
    }

    public void writeObject(ByteBuffer data, Object value, int length)
    {
        if (value == null)
            return;
        
        switch (length) {
            case 1: 
                data.put((Byte)value);
                break;
            case 2: 
                data.putShort((Short)value);
                break;
            case 4: 
                data.putInt((Integer)value);
                break;
            case 8:
                data.putLong((Long)value);
                break;
            default:
                data.put((byte[])value, 0, length);
                break;
        }
    }       
            
    public void writeTo(ByteBuffer data) {
        data.putInt(getHeader());
        writeObject(data, value, type.getPayloadLength());
    }

    public int hashCode() {
        final int prime = 367;
        int result = 1;
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        result = prime * result + getHeader();
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
        if (!(obj instanceof OFOXMField)) {
            return false;
        }
        OFOXMField other = (OFOXMField) obj;
        if (type != other.type) {
            return false;
        }
        if (length != other.length) {
            return false;
        }
        if (hasMask != other.hasMask) {
            return false;
        }
        if (!value.equals(other.value)) {
            return false;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public OFOXMField clone() throws CloneNotSupportedException {
        OFOXMField oxmField = (OFOXMField)super.clone();
        oxmField.setValue(value);
        return oxmField;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        String valueString;
        if (value instanceof byte[])
            valueString = HexString.toHexString((byte[])value);
        else
            valueString = value.toString();
        
        return "OFOXMField [type=" + type + ",hasMask=" +
            hasMask + ", length=" + length +
            ", value=" + valueString + "]";
    }
}
