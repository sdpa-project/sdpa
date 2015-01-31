package org.openflow.protocol;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

import org.openflow.protocol.instruction.OFInstruction;
import org.openflow.protocol.factory.OFInstructionFactory;
import org.openflow.protocol.factory.OFInstructionFactoryAware;
import org.openflow.util.U16;
import org.openflow.util.U32;

/**
 * Represents an ofp_flow_mod message
 * @author David Erickson (daviderickson@cs.stanford.edu)
 * @author Srini Seetharaman (srini.seetharaman@gmail.com)
 *
 */
public class OFFlowMod extends OFMessage implements OFInstructionFactoryAware, Cloneable {
    public static int MINIMUM_LENGTH = 56;

    public static final byte OFPFC_ADD = 0;                /* New flow. */
    public static final byte OFPFC_MODIFY = 1;             /* Modify all matching flows. */
    public static final byte OFPFC_MODIFY_STRICT = 2;      /* Modify entry strictly matching wildcards */
    public static final byte OFPFC_DELETE=3;               /* Delete all matching flows. */
    public static final byte OFPFC_DELETE_STRICT =4;       /* Strictly match wildcards and priority. */

    // Flags
    public static final short OFPFF_SEND_FLOW_REM = 1 << 0;
    public static final short OFPFF_CHECK_OVERLAP = 1 << 1;
    public static final short OFPFF_RESET_COUNTS = 1 << 2;
    public static final short OFPFF_NO_PKT_COUNTS = 1 << 3;
    public static final short OFPFF_NO_BYT_COUNTS = 1 << 4;

    protected OFInstructionFactory instructionFactory;
    protected long cookie;
    protected long cookieMask;
    protected byte tableId;
    protected byte command;
    protected short idleTimeout;
    protected short hardTimeout;
    protected short priority;
    protected int bufferId;
    protected int outPort;
    protected int outGroup;
    protected short flags;
    protected OFMatch match;
    protected List<OFInstruction> instructions;

    public OFFlowMod() {
        super();
        this.type = OFType.FLOW_MOD;
        this.length = U16.t(MINIMUM_LENGTH);
        this.outPort = OFPort.OFPP_ANY.getValue();
        this.outGroup = OFGroup.OFPG_ANY.getValue();
        this.priority = (short)32768; 
        this.idleTimeout = 0;
        this.hardTimeout = 0;
        this.bufferId = OFPacketOut.BUFFER_ID_NONE;
        this.match = null;
        this.flags = 0;
        this.tableId = 0;
    }

    /**
     * Get buffer_id
     * @return
     */
    public int getBufferId() {
        return this.bufferId;
    }

    /**
     * Set buffer_id
     * @param bufferId
     */
    public OFFlowMod setBufferId(int bufferId) {
        this.bufferId = bufferId;
        return this;
    }

    /**
     * Get cookie
     * @return
     */
    public long getCookie() {
        return this.cookie;
    }

    /**
     * Set cookie
     * @param cookie
     */
    public OFFlowMod setCookie(long cookie) {
        this.cookie = cookie;
        return this;
    }

    /**
     * Get cookieMask
     * @return
     */
    public long getCookieMask() {
        return this.cookieMask;
    }

    /**
     * Set cookieMask
     * @param cookieMask
     */
    public OFFlowMod setCookieMask(long cookieMask) {
        this.cookieMask = cookieMask;
        return this;
    }

    /**
     * Get tableId
     * @return
     */
    public byte getTableId() {
        return this.tableId;
    }

    /**
     * Set tableId
     * @param tableId
     */
    public OFFlowMod setTableId(byte tableId) {
        this.tableId = tableId;
        return this;
    }

    /**
     * Get command
     * @return
     */
    public byte getCommand() {
        return this.command;
    }

    /**
     * Set command
     * @param command
     */
    public OFFlowMod setCommand(byte command) {
        this.command = command;
        return this;
    }

    /**
     * Get flags, see OFPFF_* constants
     * @return
     */
    public short getFlags() {
        return this.flags;
    }

    /**
     * Set flags, see OFPFF_* constants
     * @param flags
     */
    public OFFlowMod setFlags(short flags) {
        this.flags = flags;
        return this;
    }

    /**
     * Get hard_timeout
     * @return
     */
    public short getHardTimeout() {
        return this.hardTimeout;
    }

    /**
     * Set hard_timeout
     * @param hardTimeout
     */
    public OFFlowMod setHardTimeout(short hardTimeout) {
        this.hardTimeout = hardTimeout;
        return this;
    }

    /**
     * Get idle_timeout
     * @return
     */
    public short getIdleTimeout() {
        return this.idleTimeout;
    }

    /**
     * Set idle_timeout
     * @param idleTimeout
     */
    public OFFlowMod setIdleTimeout(short idleTimeout) {
        this.idleTimeout = idleTimeout;
        return this;
    }

    /**
     * Gets a copy of the OFMatch object for this FlowMod, changes to this
     * object do not modify the FlowMod
     * @return
     */
    public OFMatch getMatch() {
        return this.match;
    }

    /**
     * Set match
     * @param match
     */
    public OFFlowMod setMatch(OFMatch match) {
        this.match = match;
        return this;
    }

    /**
     * Get out_port
     * @return
     */
    public int getOutPort() {
        return this.outPort;
    }

    /**
     * Set out_port
     * @param outPort
     */
    public OFFlowMod setOutPort(int outPort) {
        this.outPort = outPort;
        return this;
    }

    /**
     * Set out_port
     * @param port
     */
    public OFFlowMod setOutPort(OFPort port) {
        this.outPort = port.getValue();
        return this;
    }

    /**
     * Get out_group
     * @return
     */
    public int getOutGroup() {
        return this.outGroup;
    }

    /**
     * Set out_group
     * @param outGroup
     */
    public OFFlowMod setOutGroup(int outGroup) {
        this.outGroup = outGroup;
        return this;
    }

    /**
     * Set out_group
     * @param group object
     */
    public OFFlowMod setOutGroup(OFGroup group) {
        this.outGroup = group.getValue();
        return this;
    }

    /**
     * Get priority
     * @return
     */
    public short getPriority() {
        return this.priority;
    }

    /**
     * Set priority
     * @param priority
     */
    public OFFlowMod setPriority(short priority) {
        this.priority = priority;
        return this;
    }

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
    public OFFlowMod setInstructions(List<OFInstruction> instructions) {
        this.instructions = instructions;
        return this;
    }

    @Override
    public void readFrom(ByteBuffer data) {
        super.readFrom(data);
        this.cookie = data.getLong();
        this.cookieMask = data.getLong();
        this.tableId = data.get();
        this.command = data.get();
        this.idleTimeout = data.getShort();
        this.hardTimeout = data.getShort();
        this.priority = data.getShort();
        this.bufferId = data.getInt();
        this.outPort = data.getInt();
        this.outGroup = data.getInt();
        this.flags = data.getShort();
        data.position(data.position() + 2); // pad
        if (this.match == null)
            this.match = new OFMatch();
        this.match.readFrom(data);

        if (this.instructionFactory == null)
            throw new RuntimeException("OFInstructionFactory not set");
        this.instructions = this.instructionFactory.parseInstructions(data, getLengthU() -
                MINIMUM_LENGTH + OFMatch.MINIMUM_LENGTH - match.getLength());
    }

    @Override
    public void writeTo(ByteBuffer data) {
        super.writeTo(data);
        data.putLong(cookie);
        data.putLong(cookieMask);
        data.put(tableId);
        data.put(command);
        data.putShort(idleTimeout);
        data.putShort(hardTimeout);
        data.putShort(priority);
        data.putInt(bufferId);
        data.putInt(outPort);
        data.putInt(outGroup);
        data.putShort(flags);
        data.putShort((short) 0); // pad
        if (match == null)
            this.match = new OFMatch();
        this.match.writeTo(data);

        if (instructions != null) {
            for (OFInstruction instruction : instructions) {
                instruction.writeTo(data);
            }
        }
    }

    @Override
    public void setInstructionFactory(OFInstructionFactory instructionFactory) {
        this.instructionFactory = instructionFactory;
    }

    @Override
    public int hashCode() {
        final int prime = 227;
        int result = super.hashCode();
        result = prime * result + ((instructions == null) ? 0 : instructions.hashCode());
        result = prime * result + bufferId;
        result = prime * result + tableId;
        result = prime * result + command;
        result = prime * result + (int) (cookie ^ (cookie >>> 32));
        result = prime * result + (int) (cookieMask ^ (cookieMask >>> 32));
        result = prime * result + flags;
        result = prime * result + hardTimeout;
        result = prime * result + idleTimeout;
        result = prime * result + ((match == null) ? 0 : match.hashCode());
        result = prime * result + outPort;
        result = prime * result + outGroup;
        result = prime * result + priority;
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
        if (!(obj instanceof OFFlowMod)) {
            return false;
        }
        OFFlowMod other = (OFFlowMod) obj;
        if (instructions == null) {
            if (other.instructions != null) {
                return false;
            }
        } else if (!instructions.equals(other.instructions)) {
            return false;
        }
        if (bufferId != other.bufferId) {
            return false;
        }
        if (tableId != other.tableId) {
            return false;
        }
        if (command != other.command) {
            return false;
        }
        if (cookie != other.cookie) {
            return false;
        }
        if (cookieMask != other.cookieMask) {
            return false;
        }
        if (flags != other.flags) {
            return false;
        }
        if (hardTimeout != other.hardTimeout) {
            return false;
        }
        if (idleTimeout != other.idleTimeout) {
            return false;
        }
        if (match == null) {
            if (other.match != null) {
                return false;
            }
        } else if (!match.equals(other.match)) {
            return false;
        }
        if (outPort != other.outPort) {
            return false;
        }
        if (outPort != other.outGroup) {
            return false;
        }
        if (priority != other.priority) {
            return false;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public OFFlowMod clone() {
        try {
            OFMatch neoMatch = match.clone();
            OFFlowMod flowMod = (OFFlowMod) super.clone();
            flowMod.setMatch(neoMatch);
            List<OFInstruction> neoInstructions = new LinkedList<OFInstruction>();
            for(OFInstruction instruction: this.instructions)
                neoInstructions.add((OFInstruction) instruction.clone());
            flowMod.setInstructions(neoInstructions);
            return flowMod;
        } catch (CloneNotSupportedException e) {
            // Won't happen
            throw new RuntimeException(e);
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "OFFlowMod [instructions=" + instructions 
                + ", bufferId=" + U32.f(bufferId) + ", tableId=" + tableId +  ", command=" + command
                + ", cookie=" + cookie + ", cookieMask=" + cookieMask + ", flags=" + flags + ", hardTimeout="
                + hardTimeout + ", idleTimeout=" + idleTimeout + ", match="
                + match + ", outPort=" + outPort + ", outGroup=" + outGroup + ", priority=" + priority
                + ", length=" + length + ", type=" + type + ", version="
                + version + ", xid=" + xid + "]";
    }

    /* (non-Javadoc)
     * @see org.openflow.protocol.OFMessage#computeLength()
     */
    @Override
    public void computeLength() {
        int l = MINIMUM_LENGTH - OFMatch.MINIMUM_LENGTH;
        if (instructions != null) {
            for (OFInstruction instruction : instructions) {
                l += instruction.getLengthU();
            }
        }
        if (match == null)
            match = new OFMatch();
        l += match.getLength();
        this.length = U16.t(l);
    }
}
