package org.openflow.protocol.statistics.tableFeatures;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

import org.openflow.protocol.action.OFAction;
import org.openflow.util.U16;

/**
 * Represents an ofp_table_features_prop_actions
 * @author Srini Seetharaman (srini.seetharaman@gmail.com)
 */
public class OFTableFeaturesPropertyActions extends OFTableFeaturesProperty {

    protected List<OFAction> actions;

    /**
     * Returns read-only copies of the actions contained in this Flow Mod
     * @return a list of ordered OFAction objects
     */
    public List<OFAction> getActions() {
        return this.actions;
    }

    /**
     * Sets the list of actions this Flow Mod contains
     * @param actions a list of ordered OFAction objects
     */
    public OFTableFeaturesPropertyActions setActions(List<OFAction> actions) {
        this.actions = actions;
        if (actions != null) {
            int l = MINIMUM_LENGTH;
            for (OFAction action : actions) {
                l += action.getLengthU();
            }
            this.length = U16.t(l);
        }
        return this;
    }

    @Override
    public void readFrom(ByteBuffer data) {
        super.readFrom(data);
        int padLength = 8*((length + 7)/8) - length;
        this.actions = new LinkedList<OFAction>();
        for (int i=(getLengthU() - padLength); i>0; i-=OFAction.MINIMUM_LENGTH) {
            OFAction action = new OFAction();
            action.readFrom(data);
            this.actions.add(action);
        }
        data.position(data.position() + padLength);
    }

    @Override
    public void writeTo(ByteBuffer data) {
        super.writeTo(data);
        if (actions != null) {
            for (OFAction action : actions) {
                action.writeTo(data);
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
        result = prime * result + ((actions == null) ? 0 : actions.hashCode());
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
        if (!(obj instanceof OFTableFeaturesPropertyActions)) {
            return false;
        }
        OFTableFeaturesPropertyActions other = (OFTableFeaturesPropertyActions) obj;
        if (actions == null) {
            if (other.actions != null) {
                return false;
            }
        } else if (!actions.equals(other.actions)) {
            return false;
        }

        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "OFTableFeaturesPropertyActions [type=" + type +
            ", length=" + length + ", actions=" + actions +"]";
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public OFTableFeaturesPropertyActions clone() throws CloneNotSupportedException {
	    try {
	    	OFTableFeaturesPropertyActions tableFeaturesProp = (OFTableFeaturesPropertyActions) super.clone();
	        List<OFAction> neoActions = new LinkedList<OFAction>();
	        for(OFAction action: this.actions)
	            neoActions.add((OFAction) action.clone());
	        tableFeaturesProp.setActions(neoActions);
	        return tableFeaturesProp;
	    } catch (CloneNotSupportedException e) {
	        // Won't happen
	        throw new RuntimeException(e);
	    }
    }
}
