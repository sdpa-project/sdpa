package org.openflow.protocol.action;

import java.nio.ByteBuffer;

/**
 * Represents an ofp_action_group
 * @author David Erickson (daviderickson@cs.stanford.edu) - Mar 11, 2010
 */
public class OFActionGroup extends OFAction {
    public static int MINIMUM_LENGTH = 8;

    protected int groupId;

    public OFActionGroup() {
        super.setType(OFActionType.GROUP);
        super.setLength((short) MINIMUM_LENGTH);
    }

    /**
     * @return the groupId
     */
    public int getGroupId() {
        return groupId;
    }

    /**
     * @param groupId the groupId to set
     */
    public OFActionGroup setGroupId(int groupId) {
        this.groupId = groupId;
        return this;
    }

    @Override
    public void readFrom(ByteBuffer data) {
        super.readFrom(data);
        this.groupId = data.getInt();
    }

    @Override
    public void writeTo(ByteBuffer data) {
        super.writeTo(data);
        data.putInt(this.groupId);
    }

    @Override
    public int hashCode() {
        final int prime = 349;
        int result = super.hashCode();
        result = prime * result + groupId;
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
        if (!(obj instanceof OFActionGroup)) {
            return false;
        }
        OFActionGroup other = (OFActionGroup) obj;
        if (groupId != other.groupId) {
            return false;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "OFActionGroup [groupId=" + groupId + "]";
    }
}
