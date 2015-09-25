package org.openflow.protocol;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.ArrayList;

import org.openflow.protocol.factory.OFActionFactory;
import org.openflow.protocol.factory.OFActionFactoryAware;
import org.openflow.util.U16;
import org.openflow.protocol.OFBucket;

/**
 * Represents an ofp_group_mod message
 * @author Srini Seetharaman (srini.seetharaman@gmail.com)
 */
public class OFGroupMod extends OFMessage implements OFActionFactoryAware {
    public static int MINIMUM_LENGTH = 16;

    // Group mod commands
    public static final short OFPGC_ADD = 0;
    public static final short OFPGC_MODIFY = 1;
    public static final short OFPGC_DELETE = 2;
    
    // Group types
    public static final byte OFPGT_ALL = 0;
    public static final byte OFPGT_SELECT = 1;
    public static final byte OFPGT_INDIRECT = 2;
    public static final byte OFPGT_FF = 3;

    protected OFActionFactory actionFactory;
    protected short command;
    protected byte groupType;
    protected int groupId;
    protected List<OFBucket> buckets;

    public OFGroupMod() {
        super();
        this.type = OFType.GROUP_MOD;
        this.length = U16.t(MINIMUM_LENGTH);
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
    public OFGroupMod setGroupId(int groupId) {
        this.groupId = groupId;
        return this;
    }

    /**
     * @return the type
     */
    public byte getGroupType() {
        return groupType;
    }

    /**
     * @param type the type to set
     */
    public OFGroupMod setGroupType(byte groupType) {
        this.groupType = groupType;
        return this;
    }

    /**
     * @return the command
     */
    public short getCommand() {
        return command;
    }

    /**
     * @param command the command to set
     */
    public OFGroupMod setCommand(short command) {
        this.command = command;
        return this;
    }

    /**
     * Returns read-only copies of the buckets
     * @return a list of ordered OFBucket objects
     */
    public List<OFBucket> getBuckets() {
        return this.buckets;
    }

    /**
     * Sets the list of buckets this groupmod contains
     * @param buckets a list of ordered OFBucket objects
     */
    public OFGroupMod setBuckets(List<OFBucket> buckets) {
        this.buckets = buckets;
        if (buckets != null) {
            int l = MINIMUM_LENGTH;
            for (OFBucket bucket : buckets) {
                l += bucket.getLengthU();
            }
            this.length = U16.t(l);
        }
        return this;
    }

    @Override
    public void setActionFactory(OFActionFactory actionFactory) {
        this.actionFactory = actionFactory;
    }

    @Override
    public void readFrom(ByteBuffer data) {
        super.readFrom(data);
        this.command = data.getShort();
        this.groupType = data.get();
        data.get(); // pad
        this.groupId = data.getInt();

        int remaining = this.getLengthU() - MINIMUM_LENGTH;
        if (data.remaining() < remaining)
            remaining = data.remaining();
        this.buckets = new ArrayList<OFBucket>();
        while (remaining >= OFBucket.MINIMUM_LENGTH) {
            OFBucket bucket = new OFBucket();
            bucket.setActionFactory(actionFactory);
            bucket.readFrom(data);
            this.buckets.add(bucket);
            remaining -= U16.f(bucket.getLength());
        }
    }

    @Override
    public void writeTo(ByteBuffer data) {
        super.writeTo(data);
        data.putShort(this.command);
        data.put(this.groupType);
        data.put((byte)0); // pad
        data.putInt(this.groupId);
        if (buckets != null) {
            for (OFBucket bucket : buckets ) {
                bucket.writeTo(data);
            }
        }
    }

    @Override
    public int hashCode() {
        final int prime = 311;
        int result = super.hashCode();
        result = prime * result + command;
        result = prime * result + groupType;
        result = prime * result + groupId;
        result = prime * result + ((buckets == null) ? 0 : buckets.hashCode());
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
        if (!(obj instanceof OFGroupMod)) {
            return false;
        }
        OFGroupMod other = (OFGroupMod) obj;
        if (command != other.command) {
            return false;
        }
        if (groupType != other.groupType) {
            return false;
        }
        if (groupId != other.groupId) {
            return false;
        }
        if (buckets == null) {
            if (other.buckets != null) {
                return false;
            }
        } else if (!buckets.equals(other.buckets)) {
            return false;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see org.openflow.protocol.OFMessage#computeLength()
     */
    @Override
    public void computeLength() {
        int l = MINIMUM_LENGTH;
        if (buckets != null) {
            for (OFBucket bucket : buckets) {
                l += bucket.getLengthU();
            }
        }
        this.length = U16.t(l);
    }
}
