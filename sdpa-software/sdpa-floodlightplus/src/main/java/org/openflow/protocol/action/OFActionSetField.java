package org.openflow.protocol.action;

import java.nio.ByteBuffer;

import org.openflow.protocol.OFVlanId;
import org.openflow.protocol.OFOXMField;
import org.openflow.protocol.OFOXMFieldType;
import org.openflow.util.U16;

/**
 * Represents an ofp_action_set_action
 * @author Srini Seetharaman (srini.seetharaman@gmail.com)
 */
public class OFActionSetField extends OFAction {
    public static int MINIMUM_LENGTH = 8;

    // Should only be used when the bottom of stack bit is set,
    // else it should be 0x8847 or 0x8848
    protected OFOXMField field;

    public OFActionSetField() {
        super.setType(OFActionType.SET_FIELD);
        super.setLength((short) MINIMUM_LENGTH);
        this.field = new OFOXMField();
    }

    public OFActionSetField(OFOXMFieldType type, Object value) {
        super.setType(OFActionType.SET_FIELD);
        super.setLength((short) MINIMUM_LENGTH);
        
        // Disallow some pipeline fields
        if (type == OFOXMFieldType.IN_PORT ||
            type == OFOXMFieldType.IN_PHY_PORT ||
            type == OFOXMFieldType.METADATA ||
            type == OFOXMFieldType.IPV6_EXTHDR) { 
            throw new RuntimeException("Set field action disallowed for " + type);
        }
        setField(new OFOXMField(type, value));
    }

    public OFActionSetField(OFOXMField field) {
        super.setType(OFActionType.SET_FIELD);
        super.setLength((short) MINIMUM_LENGTH);
        
        // Disallow some pipeline fields
        OFOXMFieldType type = field.getType();
        if (type == OFOXMFieldType.IN_PORT ||
                type == OFOXMFieldType.IN_PHY_PORT ||
                type == OFOXMFieldType.METADATA ||
                type == OFOXMFieldType.IPV6_EXTHDR) { 
                throw new RuntimeException("Set field action disallowed for " + type);
            }
        setField(field);
    }

    /**
     * @return the field info
     */
    public OFOXMField getField() {
        return field;
    }

    /**
     * @param field the field to set
     */
    public OFActionSetField setField(OFOXMField field) {
        this.field = field;
        int l = 4 + field.getLength();
        l = (8*((l + 7)/8)); //include padding
        this.length = U16.t(l);
        return this;
    }

    @Override
    public void readFrom(ByteBuffer data) {
        super.readFrom(data);
        this.field.readFrom(data);

        // In case of SET_VLAN_VID action, set the VLAN_PRESENT bit to 1
        if (field.getType() == OFOXMFieldType.VLAN_VID) {
            short vlan = (Short)field.getValue();
            this.field.setValue((short)(vlan & ~OFVlanId.OFPVID_PRESENT.getValue()));
        }
        int padLength = length - this.field.getLength() - 4;
        data.position(data.position() + padLength); // pad
    }

    @Override
    public void writeTo(ByteBuffer data) {
        super.writeTo(data);

        // In case of SET_VLAN_VID action, set the VLAN_PRESENT bit to 1
        if (field.getType() == OFOXMFieldType.VLAN_VID) {
            short vlan = (Short)field.getValue();
            field.setValue((short)(vlan | OFVlanId.OFPVID_PRESENT.getValue()));
        }
        field.writeTo(data);
        int padLength = length - this.field.getLength() - 4;
        for (;padLength>0;padLength--)
            data.put((byte)0); //pad
    }

    @Override
    public int hashCode() {
        final int prime = 383;
        int result = super.hashCode();
        result = prime * result + field.hashCode();
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
        if (!(obj instanceof OFActionSetField)) {
            return false;
        }
        OFActionSetField other = (OFActionSetField) obj;
        if (!field.equals(other.field)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "OFActionSetField [field=" + field + "]";
    }
}
