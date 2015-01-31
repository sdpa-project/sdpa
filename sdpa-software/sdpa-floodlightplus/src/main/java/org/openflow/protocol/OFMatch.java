package org.openflow.protocol;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Set;

import org.openflow.util.HexString;
import org.openflow.util.U8;
import org.openflow.util.U16;
import org.openflow.util.U32;

/**
 * Represents an ofp_match structure
 *
 * @author Srini Seetharaman (srini.seetharaman@gmail.com)
 *
 */

public class OFMatch implements Cloneable {
    /**
     *
     */
    public static int MINIMUM_LENGTH = 8;

    public static final short ETH_TYPE_IPV4 = (short)0x800;
    public static final short ETH_TYPE_IPV6 = (short)0x86dd;
    public static final short ETH_TYPE_ARP = (short)0x806;
    public static final short ETH_TYPE_VLAN = (short)0x8100;
    public static final short ETH_TYPE_LLDP = (short)0x88cc;
    public static final short ETH_TYPE_MPLS_UNICAST = (short)0x8847;
    public static final short ETH_TYPE_MPLS_MULTICAST = (short)0x8848;

    public static final byte IP_PROTO_ICMP = 0x1;
    public static final byte IP_PROTO_TCP = 0x6;
    public static final byte IP_PROTO_UDP = 0x11;
    public static final byte IP_PROTO_SCTP = (byte)0x84;

    enum OFMatchType {
        STANDARD, OXM
    }

    // Note: Only supporting OXM and OpenFlow_Basic matches
    public enum OFMatchClass {
        NXM_0                ((short)0x0000),
        NXM_1                ((short)0x0001),
        OPENFLOW_BASIC       ((short)0x8000),
        VENDOR               ((short)0xffff);

        protected short value;

        private OFMatchClass(short value) {
            this.value = value;
        }

        /**
         * @return the value
         */
        public short getValue() {
            return value;
        }
    }

    protected OFMatchType type;
    protected short length; //total length including padding
    protected short matchLength; // length excluding padding
    protected List<OFMatchField> matchFields;

    /**
     * By default, create a OFMatch that matches everything
     * (mostly because it's the least amount of work to make a valid OFMatch)
     */
    public OFMatch() {
        this.type = OFMatchType.OXM;
        this.length = U16.t(MINIMUM_LENGTH);
        this.matchLength = 4; //No padding
        this.matchFields = new ArrayList<OFMatchField>();
    }

    /**
     * Get value of particular field
     * @return
     */
    public Object getMatchFieldValue(OFOXMFieldType matchType) {
        for (OFMatchField matchField: matchFields) {
            if (matchField.getType() == matchType)
                return matchField.getValue();
        }
        throw new IllegalArgumentException("No match exists for matchfield " + matchType.getName());
    }

    /**
     * Get mask of particular field
     * @return
     */
    public Object getMatchFieldMask(OFOXMFieldType matchType) {
        for (OFMatchField matchField: matchFields) {
            if (matchField.getType() == matchType)
                return matchField.getMask();
        }
        //No mask exists for matchfield and it is not illegal
        return null;
    }

    /**
     * Check if a particular match field exists
     * @return boolean indicating if the field value exists
     */
    public boolean fieldExists(OFOXMFieldType matchType) {
        for (OFMatchField matchField: matchFields) {
            if (matchField.getType() == matchType)
                return true;
        }
        return false;
    }

    /**
     * Get in_port
     * @return integer
     */
    public int getInPort() {
        try {
            return (Integer)getMatchFieldValue(OFOXMFieldType.IN_PORT);
        } catch (IllegalArgumentException e) {
            return OFPort.OFPP_ANY.getValue();
        }
    }

    /**
     * Set in_port in match
     * @param in_port
     */
    public OFMatch setInPort(int inPort) {
        this.setField(OFOXMFieldType.IN_PORT, inPort);
        return this;
    }

    /**
     * Get dl_dst
     *
     * @return an arrays of bytes
     */
    public byte[] getDataLayerDestination() {
        try {
            return (byte[])getMatchFieldValue(OFOXMFieldType.ETH_DST);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Set dl_dst
     *
     * @param dataLayerDestination
     */
    public OFMatch setDataLayerDestination(byte[] dataLayerDestination) {
        this.setField(OFOXMFieldType.ETH_DST, dataLayerDestination);
        return this;
    }

    /**
     * Set dl_dst, but first translate to byte[] using HexString
     *
     * @param mac
     *            A colon separated string of 6 pairs of octets, e..g.,
     *            "00:17:42:EF:CD:8D"
     */
    public OFMatch setDataLayerDestination(String mac) {
        byte bytes[] = HexString.fromHexString(mac);
        if (bytes.length != OFPhysicalPort.OFP_ETH_ALEN)
            throw new IllegalArgumentException("expected string with 6 octets, got '" + mac + "'");
        this.setField(OFOXMFieldType.ETH_DST, bytes);
        return this;
    }

    /**
     * Get dl_src
     *
     * @return an array of bytes
     */
    public byte[] getDataLayerSource() {
        try {
            return (byte[])getMatchFieldValue(OFOXMFieldType.ETH_SRC);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Set dl_src
     *
     * @param dataLayerSource
     */
    public OFMatch setDataLayerSource(byte[] dataLayerSource) {
        this.setField(OFOXMFieldType.ETH_SRC, dataLayerSource);
        return this;
    }

    /**
     * Set dl_src, but first translate to byte[] using HexString
     *
     * @param mac
     *            A colon separated string of 6 pairs of octets, e..g.,
     *            "00:17:42:EF:CD:8D"
     */
    public OFMatch setDataLayerSource(String mac) {
        byte bytes[] = HexString.fromHexString(mac);
        if (bytes.length != OFPhysicalPort.OFP_ETH_ALEN)
            throw new IllegalArgumentException("expected string with 6 octets, got '" + mac + "'");
        this.setField(OFOXMFieldType.ETH_SRC, bytes);
        return this;
    }

    /**
     * Get dl_type
     *
     * @return ether_type
     */
    public short getDataLayerType() {
        try {
            return (Short)getMatchFieldValue(OFOXMFieldType.ETH_TYPE);
        } catch (IllegalArgumentException e) {
            return 0;
        }
    }

    /**
     * Set dl_type
     *
     * @param dataLayerType
     */
    public OFMatch setDataLayerType(short dataLayerType) {
        this.setField(OFOXMFieldType.ETH_TYPE, dataLayerType);
        return this;
    }

    /**
     * Get dl_vlan
     *
     * @return vlan tag without the VLAN present bit set
     */
    public short getDataLayerVirtualLan() {
        try {
            return (short)((Short)getMatchFieldValue(OFOXMFieldType.VLAN_VID) & 0xFFF);
        } catch (IllegalArgumentException e) {
            return OFVlanId.OFPVID_NONE.getValue();
        }
    }

    /**
     * Set dl_vlan
     *
     * @param dataLayerVirtualLan VLAN ID without the VLAN present bit set
     */
    public OFMatch setDataLayerVirtualLan(short vlan) {
        this.setField(OFOXMFieldType.VLAN_VID, (short)(vlan | OFVlanId.OFPVID_PRESENT.getValue()));
        return this;
    }

    /**
     * Get dl_vlan_pcp
     *
     * @return VLAN PCP value
     */
    public byte getDataLayerVirtualLanPriorityCodePoint() {
        try {
            return (Byte) getMatchFieldValue(OFOXMFieldType.VLAN_PCP);
        } catch (IllegalArgumentException e) {
            return 0;
        }
    }

    /**
     * Set dl_vlan_pcp
     *
     * @param pcp
     */
    public OFMatch setDataLayerVirtualLanPriorityCodePoint(byte pcp) {
        this.setField(OFOXMFieldType.VLAN_VID, pcp);
        return this;
    }

    /**
     * Get nw_proto
     *
     * @return
     */
    public byte getNetworkProtocol() {
        try {
            return (Byte)getMatchFieldValue(OFOXMFieldType.IP_PROTO);
        } catch (IllegalArgumentException e) {
            return 0;
        }
    }

    /**
     * Set nw_proto
     *
     * @param networkProtocol
     */
    public OFMatch setNetworkProtocol(byte networkProtocol) {
        this.setField(OFOXMFieldType.IP_PROTO, networkProtocol);
        return this;
    }

    /**
     * Get nw_tos OFMatch stores the ToS bits as 6-bits in the lower significant bits
     *
     * @return : 6-bit DSCP value (0-63) in higher bits and 2-bit ECN in lower bits
     */
    public byte getNetworkTypeOfService() {
        try {
            byte dscp = (byte)((((Byte)getMatchFieldValue(OFOXMFieldType.IP_DSCP)) & 0x3f) << 2);
            byte ecn = (byte)(((Byte)getMatchFieldValue(OFOXMFieldType.IP_ECN)) & 0x3);
            return (byte)(dscp & ecn);
        } catch (IllegalArgumentException e) {
            return 0;
        }
    }

    /**
     * Set nw_tos OFMatch stores the DSCP and ECN separately
     *
     * @param networkTypeOfService TOS value with 6-bit DSCP value (0-63)
     * in higher significant bits and ECN in the lower 2 bits
     */
    public OFMatch setNetworkTypeOfService(byte networkTypeOfService) {
        this.setField(OFOXMFieldType.IP_DSCP, (byte)((networkTypeOfService >> 2) & 0x3f));
        this.setField(OFOXMFieldType.IP_ECN, (byte)(networkTypeOfService & 0x3));
        return this;
    }

    /**
     * Get nw_dst
     *
     * @return integer destination IP address
     */
    public int getNetworkDestination() {
        try {
            return (Integer)getMatchFieldValue(OFOXMFieldType.IPV4_DST);
        } catch (IllegalArgumentException e) {
            return 0;
        }
    }

    /**
     * Get nw_dst mask
     *
     * @return integer destination IP address mask
     */
    public int getNetworkDestinationMask() {
        Object mask = getMatchFieldMask(OFOXMFieldType.IPV4_DST);
        if (mask == null)
            return 0;
        else
            return (Integer)mask;
    }

    /**
     * Set nw_dst
     *
     * @param networkDestination destination IP address
     */
    public OFMatch setNetworkDestination(int networkDestination) {
        setNetworkDestination(ETH_TYPE_IPV4, networkDestination);
        return this;
    }

    /**
     * Set nw_dst
     *
     * @param dataLayerType ether type
     * @param networkDestination destination IP address
     */
    public OFMatch setNetworkDestination(short dataLayerType, int networkDestination) {
        switch (dataLayerType) {
            case ETH_TYPE_IPV4:
                this.setField(OFOXMFieldType.IPV4_DST, networkDestination);
                break;
            case ETH_TYPE_IPV6:
                this.setField(OFOXMFieldType.IPV6_DST, networkDestination);
                break;
            case ETH_TYPE_ARP:
                this.setField(OFOXMFieldType.ARP_THA, networkDestination);
                break;
        }
        return this;
    }

    /**
     * Set nw_dst and nw_dst_mask
     *
     * @param networkDestination destination IP address
     * @param networkMask network mask
     */
    public OFMatch setNetworkDestinationMask(int networkDestination, int networkMask) {
        this.setField(OFOXMFieldType.IPV4_DST, networkDestination, networkMask);
        return this;
    }

    /**
     * Set nw_dst and nw_dst_mask
     *
     * @param dataLayerType ether type
     * @param networkDestination destination IP address
     * @param networkMask network mask
     */
    public OFMatch setNetworkDestinationMask(short dataLayerType, int networkDestination, int networkMask) {
        switch (dataLayerType) {
            case ETH_TYPE_IPV4:
                this.setField(OFOXMFieldType.IPV4_DST, networkDestination, networkMask);
                break;
            case ETH_TYPE_IPV6:
                this.setField(OFOXMFieldType.IPV6_DST, networkDestination, networkMask);
                break;
            case ETH_TYPE_ARP:
                this.setField(OFOXMFieldType.ARP_THA, networkDestination, networkMask);
                break;
        }
        return this;
    }

    /**
     * Get nw_src
     *
     * @return integer source IP address
     */
    // TODO: Add support for IPv6
    public int getNetworkSource() {
        try {
            return (Integer)getMatchFieldValue(OFOXMFieldType.IPV4_SRC);
        } catch (IllegalArgumentException e) {
            return 0;
        }
    }

    /**
     * Get nw_src mask
     *
     * @return integer source IP address mask
     */
    public int getNetworkSourceMask() {
        Object mask = getMatchFieldMask(OFOXMFieldType.IPV4_SRC);
        if (mask == null)
            return 0;
        else
            return (Integer)mask;
    }

    /**
     * Set nw_src
     *
     * @param networkSource source IP address
     */
    // TODO: Add support for IPv6
    public OFMatch setNetworkSource(int networkSource) {
        setNetworkSource(ETH_TYPE_IPV4, networkSource);
        return this;
    }

    /**
     * Set nw_src
     *
     * @param dataLayerType ether type
     * @param networkSource source IP address
     */
    // TODO: Add support for IPv6
    public OFMatch setNetworkSource(short dataLayerType, int networkSource) {
        switch (dataLayerType) {
            case ETH_TYPE_IPV4:
                this.setField(OFOXMFieldType.IPV4_SRC, networkSource);
                break;
            case ETH_TYPE_ARP:
                this.setField(OFOXMFieldType.ARP_SHA, networkSource);
                break;
        }
        return this;
    }

    /**
     * Set nw_src and nw_src_mask
     *
     * @param networkSource source IP address
     * @param networkMask network mask
     */
    public OFMatch setNetworkSourceMask(int networkSource, int networkMask) {
        this.setField(OFOXMFieldType.IPV4_SRC, networkSource, networkMask);
        return this;
    }

    /**
     * Set nw_src and nw_src_mask
     *
     * @param dataLayerType ether type
     * @param networkSource source IP address
     * @param networkMask network mask
     */
    public OFMatch setNetworkSourceMask(short dataLayerType, int networkSource, int networkMask) {
        switch (dataLayerType) {
            case ETH_TYPE_IPV4:
                this.setField(OFOXMFieldType.IPV4_SRC, networkSource, networkMask);
                break;
            case ETH_TYPE_IPV6:
                this.setField(OFOXMFieldType.IPV6_SRC, networkSource, networkMask);
                break;
            case ETH_TYPE_ARP:
                this.setField(OFOXMFieldType.ARP_SHA, networkSource, networkMask);
                break;
        }
        return this;
    }

    /**
     * Get tp_dst
     *
     * @return destination port number
     */
    public short getTransportDestination() {
        byte networkProtocol = getNetworkProtocol();
        switch (networkProtocol) {
            case IP_PROTO_TCP:
                return (Short)getMatchFieldValue(OFOXMFieldType.TCP_DST);
            case IP_PROTO_UDP:
                return (Short)getMatchFieldValue(OFOXMFieldType.UDP_DST);
            case IP_PROTO_SCTP:
                return (Short)getMatchFieldValue(OFOXMFieldType.SCTP_DST);
            default:
                return 0;
        }
    }

    /**
     * Set tp_dst
     *
     * @param transportDestination TCP destination port number
     */
    public OFMatch setTransportDestination(short transportDestination) {
        setTransportDestination(IP_PROTO_TCP, transportDestination);
        return this;
    }

    /**
     * Set tp_dst
     *
     * @param networkProtocol IP protocol
     * @param transportDestination Destination Transport port number
     */
    public OFMatch setTransportDestination(byte networkProtocol, short transportDestination) {
        switch (networkProtocol) {
            case IP_PROTO_TCP:
                this.setField(OFOXMFieldType.TCP_DST, transportDestination);
                break;
            case IP_PROTO_UDP:
                this.setField(OFOXMFieldType.UDP_DST, transportDestination);
                break;
            case IP_PROTO_SCTP:
                this.setField(OFOXMFieldType.SCTP_DST, transportDestination);
                break;
        }
        return this;
    }

    /**
     * Get tp_src
     *
     * @return transportSource Source Transport port number
     */
    public short getTransportSource() {
        byte networkProtocol = getNetworkProtocol();
        switch (networkProtocol) {
            case IP_PROTO_TCP:
                return (Short)getMatchFieldValue(OFOXMFieldType.TCP_SRC);
            case IP_PROTO_UDP:
                return (Short)getMatchFieldValue(OFOXMFieldType.UDP_SRC);
            case IP_PROTO_SCTP:
                return (Short)getMatchFieldValue(OFOXMFieldType.SCTP_SRC);
            default:
                return 0;
        }
    }

    /**
     * Set tp_src
     *
     * @param transportSource TCP source port number
     */
    public OFMatch setTransportSource(short transportSource) {
        setTransportSource(IP_PROTO_TCP, transportSource);
        return this;
    }

    /**
     * Set tp_src
     *
     * @param networkProtocol IP protocol
     * @param transportSource Source Transport port number
     */
    public OFMatch setTransportSource(byte networkProtocol, short transportSource) {
        switch (networkProtocol) {
            case IP_PROTO_TCP:
                this.setField(OFOXMFieldType.TCP_SRC, transportSource);
                break;
            case IP_PROTO_UDP:
                this.setField(OFOXMFieldType.UDP_SRC, transportSource);
                break;
            case IP_PROTO_SCTP:
                this.setField(OFOXMFieldType.SCTP_SRC, transportSource);
                break;
        }
        return this;
    }

    public OFMatchType getType() {
        return type;
    }

    /**
     * Get the length of this message
     * @return length
     */
    public short getLength() {
        return length;
    }

    /**
     * Get the length of this message, unsigned
     * @return unsigned length
     */
    public int getLengthU() {
        return U16.f(length);
    }

    public short getMatchLength() {
        return matchLength;
    }

    /** Sets match field. In case of existing field, checks for existing value
     *
     * @param matchField Check for uniqueness of field and add matchField
     */
    public void setField(OFMatchField newMatchField) {
        if (this.matchFields == null)
            this.matchFields = new ArrayList<OFMatchField>();
        for (OFMatchField matchField: this.matchFields) {
            if (matchField.getType() == newMatchField.getType()) {
                matchField.setValue(newMatchField.getValue());
                matchField.setMask(newMatchField.getMask());
                return;
            }
        }
        this.matchFields.add(newMatchField);
        this.matchLength += newMatchField.getLength();
        this.length = U16.t(8*((this.matchLength + 7)/8)); //includes padding
    }

    public void setField(OFOXMFieldType matchFieldType, Object matchFieldValue) {
        OFMatchField matchField = new OFMatchField(matchFieldType, matchFieldValue);
        setField(matchField);
    }

    public void setField(OFOXMFieldType matchFieldType, Object matchFieldValue, Object matchFieldMask) {
        OFMatchField matchField = new OFMatchField(matchFieldType, matchFieldValue, matchFieldMask);
        setField(matchField);
    }

    /**
     * Returns read-only copies of the matchfields contained in this OFMatch
     * @return a list of ordered OFMatchField objects
     */
    public List<OFMatchField> getMatchFields() {
        return this.matchFields;
    }

    /**
     * Sets the list of matchfields this OFMatch contains
     * @param matchFields a list of ordered OFMatchField objects
     */
    public OFMatch setMatchFields(List<OFMatchField> matchFields) {
        this.matchFields = matchFields;

        //Recalculate lengths
        this.matchLength = 4; //No padding
        if (matchFields != null)
            for (OFMatchField newMatchField: this.matchFields)
                this.matchLength += newMatchField.getLength();
        this.length = U16.t(8*((this.matchLength + 7)/8)); //includes padding
        return this;
    }

    /**
     * Utility function to wildcard all fields except those specified in the set
     * @param nonWildcardedFieldTypes set of match field types preserved,
     * if null all fields are wildcarded
     */
    public OFMatch setNonWildcards(Set<OFOXMFieldType> nonWildcardedFieldTypes) {
        if (nonWildcardedFieldTypes == null)
            setMatchFields(null);
        else if (nonWildcardedFieldTypes.size() == 0)
            setMatchFields(null);
        else {
            List <OFMatchField> newMatchFields = new ArrayList<OFMatchField>();

            if (nonWildcardedFieldTypes != null) {
                for (OFMatchField matchField: matchFields) {
                    OFOXMFieldType type = matchField.getType();
                    if (nonWildcardedFieldTypes.contains(type))
                        newMatchFields.add(matchField);
                }
            }
            setMatchFields(newMatchFields);
        }
        return this;
    }

    public void readFrom(ByteBuffer data) {
        byte[] dataLayerAddress = new byte[OFPhysicalPort.OFP_ETH_ALEN];
        byte[] dataLayerAddressMask = new byte[OFPhysicalPort.OFP_ETH_ALEN];
        int networkAddress;
        int networkAddressMask;
        int wildcards;
        short dataLayerType = 0;
        byte networkProtocol = 0;
        byte networkTOS;
        short transportNumber;
        int mplsLabel;
        byte mplsTC;

        this.type = OFMatchType.values()[data.getShort()];
        this.matchLength = data.getShort();
        this.length = U16.t(8*((this.matchLength + 7)/8)); //includes padding
        int remaining = matchLength - 4; //length - sizeof(type and length)
        int end = data.position() + remaining; //includes padding in case of STANDARD match

        if (type == OFMatchType.OXM) {
            int padLength = length - matchLength;
            end += padLength; // including pad

            if (data.remaining() < remaining)
                remaining = data.remaining();
            this.matchFields = new ArrayList<OFMatchField>();
            while (remaining >= OFMatchField.MINIMUM_LENGTH) {
                OFMatchField matchField = new OFMatchField();
                matchField.readFrom(data);
                this.matchFields.add(matchField);
                remaining -= U32.f(matchField.getLength()); //value length + header length
            }
        } else {
            this.setField(OFOXMFieldType.IN_PORT, data.getInt());
            wildcards = data.getInt();

            if ((wildcards & OFMatchWildcardMask.ALL.getValue()) == 0) {
                data.position(end);
                return;
            }

            data.get(dataLayerAddress);
            data.get(dataLayerAddressMask);
            this.setField(OFOXMFieldType.ETH_SRC, dataLayerAddress.clone(), dataLayerAddressMask.clone());

            data.get(dataLayerAddress);
            data.get(dataLayerAddressMask);
            this.setField(OFOXMFieldType.ETH_DST, dataLayerAddress.clone(), dataLayerAddressMask.clone());

            if ((wildcards & OFMatchWildcardMask.DL_VLAN.getValue()) == 0)
                setDataLayerVirtualLan(data.getShort());
            else
                data.getShort(); //skip

            if ((wildcards & OFMatchWildcardMask.DL_VLAN_PCP.getValue()) == 0)
                setDataLayerVirtualLanPriorityCodePoint(data.get());
            else
                data.get(); //skip

            data.get(); //pad

            if ((wildcards & OFMatchWildcardMask.DL_TYPE.getValue()) == 0) {
                dataLayerType = data.getShort();
                setDataLayerType(dataLayerType);
            } else
                data.getShort(); //skip

            if ((dataLayerType != ETH_TYPE_IPV4) && (dataLayerType != ETH_TYPE_ARP) && (dataLayerType != ETH_TYPE_VLAN) &&
                    (dataLayerType != ETH_TYPE_MPLS_UNICAST) && (dataLayerType != ETH_TYPE_MPLS_MULTICAST)) {
                data.position(end);
                return;
            }

            if ((wildcards & OFMatchWildcardMask.NW_TOS.getValue()) == 0) {
                networkTOS = data.get();
                setNetworkTypeOfService(networkTOS);
            } else
                data.get(); //skip

            if ((wildcards & OFMatchWildcardMask.NW_PROTO.getValue()) == 0) {
                networkProtocol = data.get();
                setNetworkProtocol(networkProtocol);
            } else
                data.get(); //skip

            networkAddress = data.getInt();
            networkAddressMask = data.getInt();
            if (networkAddress != 0)
                this.setField(OFOXMFieldType.IPV4_SRC, networkAddress, networkAddressMask);

            networkAddress = data.getInt();
            networkAddressMask = data.getInt();
            if (networkAddress != 0)
                this.setField(OFOXMFieldType.IPV4_DST, networkAddress, networkAddressMask);

            transportNumber = data.getShort();
            if ((wildcards & OFMatchWildcardMask.TP_SRC.getValue()) == 0) {
                setTransportSource(networkProtocol, transportNumber);
            }

            transportNumber = data.getShort();
            if ((wildcards & OFMatchWildcardMask.TP_DST.getValue()) == 0) {
                setTransportDestination(networkProtocol, transportNumber);
            }

            mplsLabel = data.getInt();
            mplsTC = data.get();
            if ((dataLayerType == ETH_TYPE_MPLS_UNICAST) ||
                    (dataLayerType == ETH_TYPE_MPLS_MULTICAST)) {
                if ((wildcards & OFMatchWildcardMask.MPLS_LABEL.getValue()) == 0)
                    this.setField(OFOXMFieldType.MPLS_LABEL, mplsLabel);
                if ((wildcards & OFMatchWildcardMask.MPLS_TC.getValue()) == 0)
                    this.setField(OFOXMFieldType.MPLS_TC, mplsTC);
            }

            data.get(); //pad
            data.get(); //pad
            data.get(); //pad

            this.setField(OFOXMFieldType.METADATA, data.getLong(), data.getLong());
        }

        data.position(end);
    }

    public void writeTo(ByteBuffer data) {
        short matchLength = getMatchLength();
        data.putShort((short)this.type.ordinal());
        data.putShort(matchLength); //length does not include padding
        if (matchFields != null)
            for (OFMatchField matchField : matchFields)
                matchField.writeTo(data);

        int padLength = 8*((matchLength + 7)/8) - matchLength;
        for (;padLength>0;padLength--)
            data.put((byte)0); //pad
    }

    public int hashCode() {
        final int prime = 227;
        int result = 1;
        result = prime * result + ((matchFields == null) ? 0 : matchFields.hashCode());
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
        if (!(obj instanceof OFMatchField)) {
            return false;
        }
        OFMatch other = (OFMatch) obj;
        if (matchFields == null) {
            if (other.matchFields != null)
                return false;
        } else if (!matchFields.equals(other.matchFields)) {
            return false;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public OFMatch clone() {
        OFMatch match = new OFMatch();
        try {
            List<OFMatchField> neoMatchFields = new LinkedList<OFMatchField>();
            for(OFMatchField matchField: this.matchFields)
                neoMatchFields.add((OFMatchField) matchField.clone());
            match.setMatchFields(neoMatchFields);
            return match;
        } catch (CloneNotSupportedException e) {
            // Won't happen
            throw new RuntimeException(e);
        }
    }

    /**
     * Load and return a new OFMatch based on supplied packetData, see
     * {@link #loadFromPacket(byte[], short)} for details.
     *
     * @param packetData
     * @param inputPort
     * @return
     */
    public static OFMatch load(byte[] packetData, int inPort) {
        OFMatch ofm = new OFMatch();
        return ofm.loadFromPacket(packetData, inPort);
    }

    /**
     * Initializes this OFMatch structure with the corresponding data from the
     * specified packet.
     *
     * Must specify the input port, to ensure that this.in_port is set
     * correctly.
     *
     * Specify OFPort.NONE or OFPort.ANY if input port not applicable or
     * available
     *
     * @param packetData
     *            The packet's data
     * @param inputPort
     *            the port the packet arrived on
     */
    public OFMatch loadFromPacket(byte[] packetData, int inPort) {
        short scratch;
        byte[] dataLayerAddress = new byte[OFPhysicalPort.OFP_ETH_ALEN];
        short dataLayerType = 0;
        byte networkProtocol = 0;

        int transportOffset = 34;
        ByteBuffer packetDataBB = ByteBuffer.wrap(packetData);
        int limit = packetDataBB.limit();

        setInPort(inPort);

        //TODO: Extend to support more packet types
        assert (limit >= 14);
        // dl dst
        packetDataBB.get(dataLayerAddress);
        setDataLayerDestination(dataLayerAddress.clone());
        // dl src
        packetDataBB.get(dataLayerAddress);
        setDataLayerSource(dataLayerAddress.clone());
        // dl type
        dataLayerType = packetDataBB.getShort();
        setDataLayerType(dataLayerType);

        if (dataLayerType == (short) ETH_TYPE_VLAN) { // need cast to avoid signed
            // has vlan tag
            scratch = packetDataBB.getShort();
            setDataLayerVirtualLan((short) (0xfff & scratch));
            setDataLayerVirtualLanPriorityCodePoint((byte) ((0xe000 & scratch) >> 13));
            dataLayerType = packetDataBB.getShort();
        }

        //TODO: Add support for IPv6
        switch (dataLayerType) {
        case ETH_TYPE_IPV4: // ipv4
            // check packet length
            scratch = packetDataBB.get();
            scratch = (short) (0xf & scratch);
            transportOffset = (packetDataBB.position() - 1) + (scratch * 4);
            // nw tos (dscp and ecn)
            setNetworkTypeOfService(packetDataBB.get());
            // nw protocol
            packetDataBB.position(packetDataBB.position() + 7);
            networkProtocol = packetDataBB.get();
            setNetworkProtocol(networkProtocol);
            // nw src
            packetDataBB.position(packetDataBB.position() + 2);
            setNetworkSource(dataLayerType, packetDataBB.getInt());
            // nw dst
            setNetworkDestination(dataLayerType, packetDataBB.getInt());
            packetDataBB.position(transportOffset);
            break;

        case ETH_TYPE_ARP: // arp
            int arpPos = packetDataBB.position();
            // opcode
            scratch = packetDataBB.getShort(arpPos + 6);
            this.setField(OFOXMFieldType.ARP_OP, ((short) (0xff & scratch)));

            scratch = packetDataBB.getShort(arpPos + 2);
            // if ipv4 and addr len is 4
            if (scratch == 0x800 && packetDataBB.get(arpPos + 5) == 4) {
                // nw src
                this.setField(OFOXMFieldType.ARP_SPA, packetDataBB.getInt(arpPos + 14));
                // nw dst
                this.setField(OFOXMFieldType.ARP_TPA, packetDataBB.getInt(arpPos + 24));
            }
            return this;

        default: //No OXM field added
            return this;
        }

        switch (networkProtocol) {
        case IP_PROTO_ICMP:
            // icmp type
            this.setField(OFOXMFieldType.ICMPV4_TYPE, packetDataBB.get());
            // code
            this.setField(OFOXMFieldType.ICMPV4_CODE, packetDataBB.get());
            break;
        case IP_PROTO_TCP:
        case IP_PROTO_UDP:
        case IP_PROTO_SCTP:
            setTransportSource(networkProtocol, packetDataBB.getShort());
            setTransportDestination(networkProtocol, packetDataBB.getShort());
            break;
        default:
            break;
        }
        return this;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "OFMatch [type=" + type + ", length=" + length + ", matchFields=" + matchFields + "]";
    }

    /**
     * Set the networkSource or networkDestination address and their OXM
     * field mask from the CIDR string
     *
     * @param cidr
     *            "192.168.0.0/16" or "172.16.1.5"
     * @param which
     *            one of IPV4_DST or IPV4_SRC
     * @throws IllegalArgumentException
     */
    private void setNetworkAddressFromCIDR(OFMatch m, String cidr, OFOXMFieldType which)
                 throws IllegalArgumentException {
        String values[] = cidr.split("/");
        String[] ip_str = values[0].split("\\.");
        int ip = 0;
        ip += Integer.valueOf(ip_str[0]) << 24;
        ip += Integer.valueOf(ip_str[1]) << 16;
        ip += Integer.valueOf(ip_str[2]) << 8;
        ip += Integer.valueOf(ip_str[3]);
        int prefix = 32; // all bits are fixed, by default

        if (values.length >= 2)
            prefix = Integer.valueOf(values[1]);
        int mask = (Integer.MAX_VALUE - 1) << (32 - prefix);
        m.setField(which, ip, mask);
    }

    /**
     * Set this OFMatch's parameters based on a comma-separated key=value pair
     * dpctl-style string, e.g., from the output of OFMatch.toString() <br>
     * <p>
     * Supported keys/values include <br>
     * <p>
     * <TABLE border=1>
     * <TR>
     * <TD>KEY(s)
     * <TD>VALUE
     * </TR>
     * <TR>
     * <TD>"in_port","input_port"
     * <TD>integer
     * </TR>
     * <TR>
     * <TD>"dl_src","eth_src", "dl_dst","eth_dst"
     * <TD>hex-string
     * </TR>
     * <TR>
     * <TD>"dl_type", "dl_vlan", "dl_vlan_pcp"
     * <TD>integer
     * </TR>
     * <TR>
     * <TD>"nw_src", "nw_dst", "ip_src", "ip_dst"
     * <TD>CIDR-style netmask
     * </TR>
     * <TR>
     * <TD>"tp_src","tp_dst"
     * <TD>integer (max 64k)
     * </TR>
     * </TABLE>
     * <p>
     * The CIDR-style netmasks assume 32 netmask if none given, so:
     * "128.8.128.118/32" is the same as "128.8.128.118"
     *
     * @param match
     *            a key=value comma separated string, e.g.
     *            "in_port=5,ip_dst=192.168.0.0/16,tp_src=80"
     * @throws IllegalArgumentException
     *             on unexpected key or value
     */
    public static OFMatch fromString(String match) throws IllegalArgumentException {
        OFMatch m = new OFMatch();
        if (match.equals("") || match.equalsIgnoreCase("any")
            || match.equalsIgnoreCase("all") || match.equals("[]"))
            match = "OFMatch[]";

        String[] tokens = match.split("[\\[,\\]]");
        String[] values;
        byte networkProtocol = 0;
        int initArg = 0;
        if (tokens[0].equals("OFMatch"))
            initArg = 1;
        int i;
        for (i = initArg; i < tokens.length; i++) {
            values = tokens[i].split("=");
            if (values.length != 2)
                   throw new IllegalArgumentException(
                                                      "Token " + tokens[i]
                                                              + " does not have form 'key=value' parsing "
                                                              + match);
            values[0] = values[0].toLowerCase(); // try to make this case insensitive
            if (values[0].equals(OFOXMFieldType.IN_PORT.getName())
                    || values[0].equals("input_port")) {
                m.setInPort(U16.t(Integer.valueOf(values[1])));
            } else if (values[0].equals(OFOXMFieldType.ETH_DST.getName())
                    || values[0].equals("dl_dst")) {
                m.setDataLayerDestination(HexString.fromHexString(values[1]));
            } else if (values[0].equals(OFOXMFieldType.ETH_SRC.getName())
                    || values[0].equals("dl_src")) {
                m.setDataLayerSource(HexString.fromHexString(values[1]));
            } else if (values[0].equals(OFOXMFieldType.ETH_TYPE.getName())
                    || values[0].equals("dl_type")) {
                if (values[1].startsWith("0x"))
                    m.setDataLayerType(U16.t(Integer.valueOf(values[1].replaceFirst("0x", ""), 16)));
                else
                    m.setDataLayerType(U16.t(Integer.valueOf(values[1])));
            } else if (values[0].equals(OFOXMFieldType.VLAN_VID.getName())
                    || values[0].equals("dl_vlan")) {
                if (values[1].startsWith("0x"))
                    m.setDataLayerVirtualLan(U16.t(Integer.valueOf(values[1].replaceFirst("0x", ""), 16)));
                else
                    m.setDataLayerVirtualLan(U16.t(Integer.valueOf(values[1])));
            } else if (values[0].equals(OFOXMFieldType.VLAN_PCP.getName())
                    || values[0].equals("dl_vlan_pcp")) {
                m.setDataLayerVirtualLanPriorityCodePoint(U8.t(Short.valueOf(values[1])));
            } else if (values[0].equals(OFOXMFieldType.IPV4_DST.getName())
                    || values[0].equals("ip_dst") || values[0].equals("nw_dst")) {
                m.setNetworkAddressFromCIDR(m, values[1], OFOXMFieldType.IPV4_DST);
            } else if (values[0].equals(OFOXMFieldType.IPV4_SRC.getName())
                    || values[0].equals("ip_src") || values[0].equals("nw_src")) {
                m.setNetworkAddressFromCIDR(m, values[1], OFOXMFieldType.IPV4_SRC);
            } else if (values[0].equals(OFOXMFieldType.IP_PROTO.getName()) || values[0].equals("nw_proto")) {
                if (values[1].startsWith("0x"))
                    networkProtocol = U8.t(Short.valueOf(values[1].replaceFirst("0x",""),16));
                else
                    networkProtocol = U8.t(Short.valueOf(values[1]));
                m.setNetworkProtocol(networkProtocol);
            } else if (values[0].equals(OFOXMFieldType.IP_DSCP.getName())
                    || values[0].equals("nw_tos")) {
                m.setNetworkTypeOfService(U8.t(Short.valueOf(values[1])));
            } else if (values[0].equals(OFOXMFieldType.TCP_DST.getName())
                    || values[0].equals(OFOXMFieldType.UDP_DST.getName())
                    || values[0].equals("tp_dst")) {
                if (networkProtocol == 0)
                    throw new IllegalArgumentException("specifying transport src/dst without establishing nw_proto first");
                m.setTransportDestination(networkProtocol, U16.t(Integer.valueOf(values[1])));
            } else if (values[0].equals(OFOXMFieldType.TCP_SRC.getName())
                    || values[0].equals(OFOXMFieldType.UDP_SRC.getName())
                    || values[0].equals("tp_src")) {
                if (networkProtocol == 0)
                    throw new IllegalArgumentException("specifying transport src/dst without establishing nw_proto first");
                m.setTransportSource(networkProtocol, U16.t(Integer.valueOf(values[1])));
            } else {
                throw new IllegalArgumentException("unknown token "
                                                   + tokens[i] + " parsing "
                                                   + match);
            }
        }
        return m;
    }
}
