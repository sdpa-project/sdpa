package org.openflow.protocol;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.ArrayList;

import org.openflow.util.U16;

/**
 * Represents an ofp_role_request/reply
 * @author Srini Seetharaman (srini.seetharaman@gmail.com)
 */
public class OFRoleMessage extends OFMessage {
    public static int MINIMUM_LENGTH = 24;

    // Comtroller role values
    public enum OFControllerRole {
        OFPCR_NOCHANGE,
        OFPCR_EQUAL,
        OFPCR_MASTER,
        OFPCR_SLAVE
    }

    protected int role;
    protected long generationId;

    public OFRoleMessage() {
        super();
        this.length = U16.t(MINIMUM_LENGTH);
    }

    /**
     * @return the role
     */
    public int getRole() {
        return role;
    }

    /**
     * @param role integer value of the role to set
     */
    public OFRoleMessage setRole(int role) {
        this.role = role;
        return this;
    }

    /**
     * @param role enum value of the role to set
     */
    public OFRoleMessage setRole(OFControllerRole role) {
        this.role = role.ordinal();
        return this;
    }

    /**
     * @return the generationId
     */
    public long getGenerationId() {
        return generationId;
    }

    /**
     * @param generationId the generationId to set
     */
    public OFRoleMessage setGenerationId(long generationId) {
        this.generationId = generationId;
        return this;
    }

    @Override
    public void readFrom(ByteBuffer data) {
        super.readFrom(data);
        this.role = data.getInt();
        data.getInt(); // pad
        this.generationId = data.getLong();
    }

    @Override
    public void writeTo(ByteBuffer data) {
        super.writeTo(data);
        data.putInt(this.role);
        data.putInt(0); // pad
        data.putLong(this.generationId);
    }

    @Override
    public int hashCode() {
        final int prime = 311;
        int result = super.hashCode();
        result = prime * result + role;
        result = prime * result + (int) (generationId ^ (generationId >>> 32));;
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
        if (!(obj instanceof OFRoleMessage)) {
            return false;
        }
        OFRoleMessage other = (OFRoleMessage) obj;
        if (generationId != other.generationId) {
            return false;
        }
        if (role != other.role) {
            return false;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see org.openflow.protocol.OFMessage#computeLength()
     */
    @Override
    public void computeLength() {
        this.length = (short) MINIMUM_LENGTH;
    }
}
