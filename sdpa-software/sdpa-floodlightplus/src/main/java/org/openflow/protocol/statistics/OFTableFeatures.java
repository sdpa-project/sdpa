package org.openflow.protocol.statistics;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openflow.protocol.OFTable;
import org.openflow.protocol.OFTable.OFTableConfig;
import org.openflow.protocol.factory.OFTableFeaturesPropertyFactory;
import org.openflow.protocol.factory.OFTableFeaturesPropertyFactoryAware;
import org.openflow.protocol.statistics.tableFeatures.OFTableFeaturesProperty;
import org.openflow.util.U16;

/**
 * Corresponds to the struct ofp_packet_tableRequest OpenFlow structure
 *
 * @author Srini Seetharamana (srini.seetharaman@gmail.com)
 */
public class OFTableFeatures implements OFStatistics, Cloneable, 
    OFTableFeaturesPropertyFactoryAware {

    public static short MINIMUM_LENGTH = 64;
    public static final int OFP_MAX_TABLE_NAME_LEN = 32;

    protected OFTableFeaturesPropertyFactory tableRequestPropertyFactory;
    protected int maxEntries;
    protected byte tableId;
    protected short length;
    protected long metadataMatch;
    protected long metadataWrite;
    protected List<OFTableFeaturesProperty> properties;
    protected String name;
    protected int config;

    public OFTableFeatures() {
        this.length = MINIMUM_LENGTH;
        this.tableId = OFTable.OFPTT_ALL;
    }

    /**
     * @return the tableId
     */
    public int getTableFeaturesId() {
        return tableId;
    }

    /**
     * @param tableId the tableId to set
     */
    public OFTableFeatures setTableFeaturesId(byte tableId) {
        this.tableId = tableId;
        return this;
    }

    /**
     * @return the maxEntries
     */
    public int getMaxEntries() {
        return maxEntries;
    }

    /**
     * @param maxEntries the port to associate the tableRequest with
     */
    public OFTableFeatures setMaxEntries(int maxEntries) {
        this.maxEntries = maxEntries;
        return this;
    }

    /**
     * @return the length
     */
    public int getLength() {
        return this.length;
    }

    /**
     * @param length the length to set
     */
    public void setLength(short length) {
        this.length = length;
    }

    /**
     * @return the properties
     */
    public List<OFTableFeaturesProperty> getProperties() {
        return properties;
    }

    /**
     * @param properties the properties to set
     */
    public OFTableFeatures setProperties(List<OFTableFeaturesProperty> properties) {
        this.properties = properties;
        int l = MINIMUM_LENGTH;
        if (this.properties != null) {
            for (OFTableFeaturesProperty prop : this.properties) {
                l += prop.getLengthU();
            }
        }
        this.length = U16.t(l);
        return this;
    }

    /**
     * Get metadataMatch
     * @return
     */
    public long getMetadataMatch() {
        return this.metadataMatch;
    }

    /**
     * Set metadataMatch
     * @param metadataMatch
     */
    public OFTableFeatures setMetadataMatch(long metadataMatch) {
        this.metadataMatch = metadataMatch;
        return this;
    }

    /**
     * Get metadataWrite
     * @return
     */
    public long getMetadataWrite() {
        return this.metadataWrite;
    }

    /**
     * Set metadataWrite
     * @param metadataWrite
     */
    public OFTableFeatures setMetadataWrite(long metadataWrite) {
        this.metadataWrite = metadataWrite;
        return this;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public OFTableFeatures setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * @return the config
     */
    public int getConfig() {
        return config;
    }

    /**
     * @param config the config to set
     */
    public OFTableFeatures setConfig(int config) {
        this.config = config;
        return this;
    }

    public void readFrom(ByteBuffer data) {
        this.length = data.getShort();
        this.tableId = data.get();
        for (int i=0;i<5;i++)
            data.get(); //pad
        byte[] name = new byte[OFP_MAX_TABLE_NAME_LEN];
        data.get(name);
        // find the first index of 0
        int index = 0;
        for (byte b : name) {
            if (0 == b)
                break;
            ++index;
        }
        this.name = new String(Arrays.copyOf(name, index),
                Charset.forName("ascii"));
        this.metadataMatch = data.getLong();
        this.metadataWrite = data.getLong();
        this.config = data.getInt();
        this.maxEntries = data.getInt();
        if (this.tableRequestPropertyFactory == null)
            throw new RuntimeException("OFTableFeaturesPropertyFactory not set");
        this.properties = tableRequestPropertyFactory.parseTableFeaturesProperties(data,
                U16.f(this.length) - MINIMUM_LENGTH);
    }

    public void writeTo(ByteBuffer data) {
        data.putShort((short)this.length);
        data.put(this.tableId);
        for (int i=0;i<5;i++)
            data.get(); //pad
        try {
            byte[] name = this.name.getBytes("ASCII");
            if (name.length < OFP_MAX_TABLE_NAME_LEN) {
                data.put(name);
                for (int i = name.length; i < OFP_MAX_TABLE_NAME_LEN; ++i) {
                    data.put((byte) 0);
                }
            } else {
                data.put(name, 0, OFP_MAX_TABLE_NAME_LEN-1);
                data.put((byte) 0);
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        data.putLong(this.metadataMatch);
        data.putLong(this.metadataWrite);
        data.putInt(this.config);
        data.putInt(this.maxEntries);
        if (this.properties != null) {
            for (OFTableFeaturesProperty tableRequestProperty : this.properties) {
                tableRequestProperty.writeTo(data);
            }
        }
    }

    @Override
    public int hashCode() {
        final int prime = 6367;
        int result = 1;
        result = prime * result + length;
        result = prime * result + tableId;
        result = prime * result + maxEntries;
        result = prime * result + (int) (metadataMatch ^ (metadataMatch >>> 32));;
        result = prime * result + (int) (metadataWrite ^ (metadataWrite >>> 32));;
        result = prime * result
                + ((properties == null) ? 0 : properties.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof OFTableFeatures))
            return false;
        OFTableFeatures other = (OFTableFeatures) obj;
        if (length != other.length)
            return false;
        if (properties == null) {
            if (other.properties != null)
                return false;
        } else if (!properties.equals(other.properties))
            return false;
        if (tableId != other.tableId)
            return false;
        if (maxEntries != other.maxEntries)
            return false;
        return true;
    }

    @Override
    public OFTableFeatures clone() {
        try {
            OFTableFeatures clone = (OFTableFeatures) super.clone();
            if (this.properties != null) {
                List<OFTableFeaturesProperty> tableRequestProps = new ArrayList<OFTableFeaturesProperty>();
                for (OFTableFeaturesProperty prop : this.properties) {
                    tableRequestProps.add(prop.clone());
                }
                clone.setProperties(tableRequestProps);
            }
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setTableFeaturesPropertyFactory(
            OFTableFeaturesPropertyFactory tableRequestPropertyFactory) {
        this.tableRequestPropertyFactory = tableRequestPropertyFactory;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "OFTableFeatures [name=" + name + ", tableId=" + tableId 
                + ", length=" + length + ", maxEntries=" + maxEntries
                + ", config=" + OFTableConfig.valueOf(config) 
                + ", metadataMatch=" + metadataMatch 
                + ", metadataWrite=" + metadataWrite 
                + ", properties=" + properties + 
                "]";
    }

    /**
     * This method computes the length of the OFTableFeatures message, both setting
     * the length field and returning the value.
     * @return the length
     */
    public int computeLength() {
        return getLength();
    }
}
