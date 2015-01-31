package org.openflow.protocol.instruction;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

import org.openflow.protocol.action.OFAction;
import org.openflow.protocol.factory.OFActionFactory;
import org.openflow.protocol.factory.OFActionFactoryAware;
import org.openflow.util.U16;

/**
 * Represents an ofp_instruction_actions
 * @author Srini Seetharaman (srini.seetharaman@gmail.com)
 */
public abstract class OFInstructionActions extends OFInstruction implements OFActionFactoryAware {
    public static int MINIMUM_LENGTH = 8;
    protected OFActionFactory actionFactory;
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
    public OFInstructionActions setActions(List<OFAction> actions) {
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
        data.position(data.position() + 4); // pad
        if (this.actionFactory == null)
            throw new RuntimeException("OFActionFactory not set");
        this.actions = this.actionFactory.parseActions(data, getLengthU() -
                MINIMUM_LENGTH);
    }

    @Override
    public void writeTo(ByteBuffer data) {
        super.writeTo(data);
        data.putInt((int) 0); // pad
        if (actions != null) {
            for (OFAction action : actions) {
                action.writeTo(data);
            }
        }
    }
    
    @Override
    public void setActionFactory(OFActionFactory actionFactory) {
        this.actionFactory = actionFactory;
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
        if (!(obj instanceof OFInstructionActions)) {
            return false;
        }
        OFInstructionActions other = (OFInstructionActions) obj;
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
        return "OFInstructionActions [type=" + type + ", length=" + length + "]";
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public OFInstructionActions clone() throws CloneNotSupportedException {
	    try {
	    	OFInstructionActions instruction = (OFInstructionActions) super.clone();
	        List<OFAction> neoActions = new LinkedList<OFAction>();
	        for(OFAction action: this.actions)
	            neoActions.add((OFAction) action.clone());
	        instruction.setActions(neoActions);
	        return instruction;
	    } catch (CloneNotSupportedException e) {
	        // Won't happen
	        throw new RuntimeException(e);
	    }
    }
}
