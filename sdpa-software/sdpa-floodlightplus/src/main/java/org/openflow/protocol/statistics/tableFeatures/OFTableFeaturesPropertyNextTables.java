package org.openflow.protocol.statistics.tableFeatures;

import java.nio.ByteBuffer;
import org.openflow.util.U16;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an ofp_table_features_prop_nextTableIds
 * @author Srini Seetharaman (srini.seetharaman@gmail.com)
 */
public class OFTableFeaturesPropertyNextTables extends OFTableFeaturesProperty {

    protected byte[] nextTableIds;

    /**
     * Returns read-only copies of the nextTableIds contained in this Flow Mod
     * @return a list of ordered byte table ids
     */
    public byte[] getNextTableIds() {
        return this.nextTableIds;
    }

    /**
     * Sets the list of nextTableIds this Flow Mod contains
     * @param nextTableIds a list of ordered byte objects
     */
    public OFTableFeaturesPropertyNextTables setNextTableIds(byte[] nextTableIds) {
        this.nextTableIds = nextTableIds;
        this.length = U16.t(MINIMUM_LENGTH + nextTableIds.length);
        return this;
    }

    @Override
    public void readFrom(ByteBuffer data) {
        super.readFrom(data);
        this.nextTableIds = new byte[getLength() - MINIMUM_LENGTH];
        data.get(this.nextTableIds);
        int padLength = 8*((length + 7)/8) - length;
        data.position(data.position() + padLength);
    }

    @Override
    public void writeTo(ByteBuffer data) {
        super.writeTo(data);
        if (nextTableIds != null) {
            data.put(nextTableIds);
        }
        int padLength = 8*((length + 7)/8) - length;
        for (;padLength > 0; padLength--) 
            data.put((byte) 0); // pad
    }
    
    @Override
    public int hashCode() {
        final int prime = 347;
        int result = super.hashCode();
        result = prime * result + ((nextTableIds == null) ? 0 : nextTableIds.hashCode());
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
        if (!(obj instanceof OFTableFeaturesPropertyNextTables)) {
            return false;
        }
        OFTableFeaturesPropertyNextTables other = (OFTableFeaturesPropertyNextTables) obj;
        if (nextTableIds == null) {
            if (other.nextTableIds != null) {
                return false;
            }
        } else if (!nextTableIds.equals(other.nextTableIds)) {
            return false;
        }

        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        List<Byte> nextTableIdList = new ArrayList<Byte>();
        for(byte b : nextTableIds) 
            nextTableIdList.add(new Byte(b));

        return "OFTableFeaturesPropertyNextTables [type=" + type +
            ", length=" + length + ", nextTableIds=" + nextTableIdList +"]";
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public OFTableFeaturesPropertyNextTables clone() throws CloneNotSupportedException {
	    try {
	    	OFTableFeaturesPropertyNextTables tableFeaturesProp = (OFTableFeaturesPropertyNextTables) super.clone();
	        byte[] neoNextTableIds = nextTableIds.clone();
	        tableFeaturesProp.setNextTableIds(neoNextTableIds);
	        return tableFeaturesProp;
	    } catch (CloneNotSupportedException e) {
	        // Won't happen
	        throw new RuntimeException(e);
	    }
    }
}
