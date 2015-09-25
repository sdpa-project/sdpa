package org.openflow.protocol.statistics.tableFeatures;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

import org.openflow.protocol.instruction.OFInstruction;
import org.openflow.util.U16;

/**
 * Represents an ofp_table_features_prop_instructions
 * @author Srini Seetharaman (srini.seetharaman@gmail.com)
 */
public class OFTableFeaturesPropertyInstructions extends OFTableFeaturesProperty {

    protected List<OFInstruction> instructions;

    /**
     * Returns read-only copies of the instructions contained in this Flow Mod
     * @return a list of ordered OFInstruction objects
     */
    public List<OFInstruction> getInstructions() {
        return this.instructions;
    }

    /**
     * Sets the list of instructions this Flow Mod contains
     * @param instructions a list of ordered OFInstruction objects
     */
    public OFTableFeaturesPropertyInstructions setInstructions(List<OFInstruction> instructions) {
        this.instructions = instructions;
        if (instructions != null) {
            int l = MINIMUM_LENGTH;
            for (OFInstruction instruction : instructions) {
                l += instruction.getLengthU();
            }
            this.length = U16.t(l);
        }
        return this;
    }

    @Override
    public void readFrom(ByteBuffer data) {
        super.readFrom(data);
        int padLength = 8*((length + 7)/8) - length;
        this.instructions = new LinkedList<OFInstruction>();
        for (int i=(getLengthU() - padLength); i>0; i-=OFInstruction.MINIMUM_LENGTH) {
            OFInstruction instruction = new OFInstruction();
            instruction.readFrom(data);
            this.instructions.add(instruction);
        }
        data.position(data.position() + padLength);
    }

    @Override
    public void writeTo(ByteBuffer data) {
        super.writeTo(data);
        if (instructions != null) {
            for (OFInstruction instruction : instructions) {
                instruction.writeTo(data);
            }
        }
        int padLength = 8*((length + 7)/8) - length;
        for (;padLength > 0; padLength--) 
            data.put((byte) 0); // pad
    }
    
    @Override
    public int hashCode() {
        final int prime = 347;
        int result = super.hashCode();
        result = prime * result + ((instructions == null) ? 0 : instructions.hashCode());
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
        if (!(obj instanceof OFTableFeaturesPropertyInstructions)) {
            return false;
        }
        OFTableFeaturesPropertyInstructions other = (OFTableFeaturesPropertyInstructions) obj;
        if (instructions == null) {
            if (other.instructions != null) {
                return false;
            }
        } else if (!instructions.equals(other.instructions)) {
            return false;
        }

        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "OFTableFeaturesPropertyInstructions [type=" + type +
            ", length=" + length + ", instructions=" + instructions +"]";
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public OFTableFeaturesPropertyInstructions clone() throws CloneNotSupportedException {
	    try {
	    	OFTableFeaturesPropertyInstructions tableFeaturesProp = (OFTableFeaturesPropertyInstructions) super.clone();
	        List<OFInstruction> neoInstructions = new LinkedList<OFInstruction>();
	        for(OFInstruction instruction: this.instructions)
	            neoInstructions.add((OFInstruction) instruction.clone());
	        tableFeaturesProp.setInstructions(neoInstructions);
	        return tableFeaturesProp;
	    } catch (CloneNotSupportedException e) {
	        // Won't happen
	        throw new RuntimeException(e);
	    }
    }
}
