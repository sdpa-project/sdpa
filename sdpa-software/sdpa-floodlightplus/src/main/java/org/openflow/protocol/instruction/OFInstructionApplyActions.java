package org.openflow.protocol.instruction;

import java.util.List;
import org.openflow.protocol.action.OFAction;

/**
 *
 * @author Srini Seetharaman (srini.seetharaman@gmail.com)
 */
public class OFInstructionApplyActions extends OFInstructionActions {
    public OFInstructionApplyActions() {
        super();
        super.setType(OFInstructionType.APPLY_ACTIONS);
        super.setLength((short) OFInstructionActions.MINIMUM_LENGTH);
        this.actions = null;
    }

    public OFInstructionApplyActions(List<OFAction> actions) {
        super();
        super.setType(OFInstructionType.APPLY_ACTIONS);
        super.setLength((short) OFInstructionActions.MINIMUM_LENGTH);
        super.setActions(actions);
    }

    @Override
    public String toString() {
        return "OFInstructionApplyActions [actions=" + actions + "]";
    }
}
