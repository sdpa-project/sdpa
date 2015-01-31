package org.openflow.protocol.instruction;

import java.util.List;
import org.openflow.protocol.action.OFAction;

/**
 *
 * @author Srini Seetharaman (srini.seetharaman@gmail.com)
 */
public class OFInstructionClearActions extends OFInstructionActions {
    public OFInstructionClearActions() {
        super();
        super.setType(OFInstructionType.CLEAR_ACTIONS);
        super.setLength((short) OFInstructionActions.MINIMUM_LENGTH);
        this.actions = null;
    }

    public OFInstructionClearActions(List<OFAction> actions) {
        super();
        super.setType(OFInstructionType.CLEAR_ACTIONS);
        super.setLength((short) OFInstructionActions.MINIMUM_LENGTH);
        super.setActions(actions);
    }

    @Override
    public String toString() {
        return "OFInstructionClearActions [actions=" + actions + "]";
    }
}
