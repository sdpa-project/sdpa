package org.openflow.protocol.instruction;

import java.util.List;
import org.openflow.protocol.action.OFAction;

/**
 *
 * @author Srini Seetharaman (srini.seetharaman@gmail.com)
 */
public class OFInstructionWriteActions extends OFInstructionActions {
    public OFInstructionWriteActions() {
        super();
        super.setType(OFInstructionType.WRITE_ACTIONS);
        super.setLength((short) OFInstructionActions.MINIMUM_LENGTH);
        this.actions = null;
    }

    public OFInstructionWriteActions(List<OFAction> actions) {
        super();
        super.setType(OFInstructionType.WRITE_ACTIONS);
        super.setLength((short) OFInstructionActions.MINIMUM_LENGTH);
        super.setActions(actions);
    }

    @Override
    public String toString() {
        return "OFInstructionWriteActions [actions=" + actions + "]";
    }
}
