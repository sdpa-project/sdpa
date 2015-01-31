package org.openflow.protocol.statistics.tableFeatures;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

import org.openflow.util.U16;
import org.openflow.protocol.OFOXMField;

/**
 * Represents an ofp_table_features_prop_oxmFields
 * @author Srini Seetharaman (srini.seetharaman@gmail.com)
 */
public class OFTableFeaturesPropertyOXM extends OFTableFeaturesProperty {

    protected List<OFOXMField> oxmFields;

    /**
     * Returns read-only copies of the oxmFields contained in this Flow Mod
     * @return a list of ordered byte table ids
     */
    public List<OFOXMField> getOXMFields() {
        return this.oxmFields;
    }

    /**
     * Sets the list of oxmFields this Flow Mod contains
     * @param oxmFields a list of ordered byte objects
     */
    public OFTableFeaturesPropertyOXM setOXMFields(List<OFOXMField> oxmFields) {
        this.oxmFields = oxmFields;
        this.length = U16.t(MINIMUM_LENGTH + 4 * oxmFields.size());
        return this;
    }

    @Override
    public void readFrom(ByteBuffer data) {
        super.readFrom(data);
        this.oxmFields = new LinkedList<OFOXMField>();
        int oxmHeader;
        for (int i=(getLength() - MINIMUM_LENGTH)/4; i>0; i--) {
            oxmHeader = data.getInt();
            this.oxmFields.add(new OFOXMField(oxmHeader, null));
        }
        int padLength = 8*((length + 7)/8) - length;
        data.position(data.position() + padLength);
    }

    @Override
    public void writeTo(ByteBuffer data) {
        super.writeTo(data);
        if (oxmFields != null) {
            for (OFOXMField oxmField: oxmFields)
                data.putInt(oxmField.getHeader());
        }
        int padLength = 8*((length + 7)/8) - length;
        for (;padLength > 0; padLength--) 
            data.put((byte) 0); // pad
    }
    
    @Override
    public int hashCode() {
        final int prime = 347;
        int result = super.hashCode();
        result = prime * result + ((oxmFields == null) ? 0 : oxmFields.hashCode());
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
        if (!(obj instanceof OFTableFeaturesPropertyOXM)) {
            return false;
        }
        OFTableFeaturesPropertyOXM other = (OFTableFeaturesPropertyOXM) obj;
        if (oxmFields == null) {
            if (other.oxmFields != null) {
                return false;
            }
        } else if (!oxmFields.equals(other.oxmFields)) {
            return false;
        }

        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "OFTableFeaturesPropertyOXM [type=" + type +
            ", length=" + length + ", oxmFields=" + oxmFields +"]";
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public OFTableFeaturesPropertyOXM clone() throws CloneNotSupportedException {
	    try {
	    	OFTableFeaturesPropertyOXM tableFeaturesProp = (OFTableFeaturesPropertyOXM) super.clone();
	        List<OFOXMField> neoOXMFields =  new LinkedList<OFOXMField>();
            for(OFOXMField oxmField: this.oxmFields)
                neoOXMFields.add((OFOXMField) oxmField.clone());
	        tableFeaturesProp.setOXMFields(neoOXMFields);
	        return tableFeaturesProp;
	    } catch (CloneNotSupportedException e) {
	        // Won't happen
	        throw new RuntimeException(e);
	    }
    }
}
