package org.openflow.protocol;

import java.nio.ByteBuffer;
import java.util.List;

import org.openflow.protocol.action.OFAction;
import org.openflow.protocol.factory.OFActionFactory;
import org.openflow.protocol.factory.OFActionFactoryAware;
import org.openflow.util.U16;
/**
 * Represents an ofp_bucket
 * @author Srini Seetharaman (srini.seetharaman@gmail.com)
 */
public class OFBucket implements OFActionFactoryAware {
    public static int MINIMUM_LENGTH = 16;

    protected short length;
    protected short weight;
    protected int watchPort;
    protected int watchGroup;
    protected OFActionFactory actionFactory;
    protected List<OFAction> actions;

    public OFBucket() {
        this.length = (short)MINIMUM_LENGTH;
    }

    /**
     * Returns read-only copies of the actions contained in this bucket
     * @return a list of ordered OFAction objects
     */
    public List<OFAction> getActions() {
        return this.actions;
    }

    /**
     * Sets the list of actions this bucket contains
     * @param actions a list of ordered OFAction objects
     */
    public OFBucket setActions(List<OFAction> actions) {
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

    /**
     * Returns the watchPort set for this bucket
     * @return the watch port
     */
    public int getWatchPort() {
        return this.watchPort;
    }

    /**
     * Sets the watchPort for this bucket 
     * @param the watch port
     */
    public OFBucket setWatchPort(int watchPort) {
        this.watchPort = watchPort;
        return this;
    }

    /**
     * Returns the watchGroup set for this bucket
     * @return the watch group
     */
    public int getWatchGroup() {
        return this.watchGroup;
    }

    /**
     * Sets the watchGroup  for this bucket 
     * @param the watch group
     */
    public OFBucket setWatchGroup(int watchGroup) {
        this.watchGroup = watchGroup;
        return this;
    }
 
    /**
     * Returns the length of this bucket (including padding)
     * @return the length
     */
    public short getLength() {
        return this.length;
    }

    /**
     * Get the length of this bucket, unsigned
     * @return the length unsigned
     */
    public int getLengthU() {
        return U16.f(length);
    }

    /**
     * Sets the length of this bucket
     * @param length 
     */
    public OFBucket setLength(short length) {
        this.length = length;
        return this;
    }

    /**
     * Returns the weight of this bucket
     * @return the weight
     */
    public short getWeight() {
        return this.weight;
    }

    /**
     * Sets the weight of this bucket
     * @param weight 
     */
    public OFBucket setWeight(short weight) {
        this.weight = weight;
        return this;
    }

    public void readFrom(ByteBuffer data) {
        this.length = data.getShort();
        this.weight = data.getShort();
        this.watchPort = data.getInt();
        this.watchGroup = data.getInt();
        data.position(data.position() + 4); // pad
        if (this.actionFactory == null)
            throw new RuntimeException("OFActionFactory not set");
        this.actions = this.actionFactory.parseActions(data, getLengthU() -
                MINIMUM_LENGTH);
    }

    public void writeTo(ByteBuffer data) {
        data.putShort(length); 
        data.putShort(weight); 
        data.putInt(watchPort);
        data.putInt(watchGroup);
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
        result = prime * result + length;
        result = prime * result + weight;
        result = prime * result + watchPort;
        result = prime * result + watchGroup;        
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
        if (!(obj instanceof OFBucket)) {
            return false;
        }
        OFBucket other = (OFBucket) obj;
        if (actions == null) {
            if (other.actions != null) {
                return false;
            }
        } else if (!actions.equals(other.actions)) {
            return false;
        }
        if (length != other.length)
            return false;
        if (weight != other.weight)
            return false;
        if (watchPort != other.watchPort)
            return false;
        if (watchGroup != other.watchGroup)
            return false;
        
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "OFBucket [weight=" + weight + ", length=" + length + 
         ", watch_port=" + watchPort + ", watch_group=" + watchGroup + 
         ", actions=" + actions + "]";
    }
}
