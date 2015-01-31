package org.openflow.protocol;

import org.openflow.protocol.OFMatch.OFMatchClass;

/**
 * @author Srini Seetharaman (srini.seetharaman@gmail.com)
 */
public enum OFOXMFieldType {
    IN_PORT         ("in_port",     0, 4),
    IN_PHY_PORT     ("in_phy_port", 1, 4),
    METADATA        ("metadata",    2, 8),
    ETH_DST         ("eth_dst",     3, 6),
    ETH_SRC         ("eth_src",     4, 6),
    ETH_TYPE        ("eth_type",    5, 2),
    VLAN_VID        ("vlan_vid",    6, 2),
    VLAN_PCP        ("vlan_pcp",    7, 1),
    IP_DSCP         ("ip_dscp",     8, 1),
    IP_ECN          ("ip_ecn",      9, 1),
    IP_PROTO        ("ip_proto",    10, 1),
    IPV4_SRC        ("ipv4_src",    11, 4),
    IPV4_DST        ("ipv4_dst",    12, 4),
    TCP_SRC         ("tcp_src",     13, 2),
    TCP_DST         ("tcp_dst",     14, 2),
    UDP_SRC         ("udp_src",     15, 2),
    UDP_DST         ("udp_dst",     16, 2),
    SCTP_SRC        ("sctp_src",    17, 2),
    SCTP_DST        ("sctp_dst",    18, 2),
    ICMPV4_TYPE     ("icmpv4_type", 19, 1),
    ICMPV4_CODE     ("icmpv4_code", 20, 1),
    ARP_OP          ("arp_op",      21, 2),
    ARP_SPA         ("arp_spa",     22, 4),
    ARP_TPA         ("arp_tpa",     23, 4),
    ARP_SHA         ("arp_sha",     24, 6),
    ARP_THA         ("arp_tha",     25, 6),
    IPV6_SRC        ("ipv6_src",    26, 16),
    IPV6_DST        ("ipv6_dst",    27, 16),
    IPV6_FLABEL     ("ipv6_flabel", 28, 4),
    ICMPV6_TYPE     ("icmpv6_type", 29, 1),
    ICMPV6_CODE     ("icmpv6_code", 30, 1),
    IPV6_ND_TARGET  ("ipv6_nd_target", 31, 16),
    IPV6_ND_SLL     ("ipv6_nd_sll", 32, 6),
    IPV6_ND_TLL     ("ipv6_nd_tll", 33, 6),
    MPLS_LABEL      ("mpls_label",  34, 4),
    MPLS_TC         ("mpls_tc",     35, 1),
    MPLS_BOS        ("mpls_bos",    36, 1),
    PBB_ISID        ("pbb_isid",    37, 3),
    TUNNEL_ID       ("tunnel_id",   38, 8),
    IPV6_EXTHDR     ("ipv6_exthdr", 39, 2);

    static OFOXMFieldType[] mapping;
    protected short value;
    protected String name;
    protected short matchClass;
    protected byte payloadLength;

    private OFOXMFieldType(String name, int value, int payloadLength) {
        this.name = name;
        this.value = (short) value;
        this.matchClass = OFMatchClass.OPENFLOW_BASIC.getValue();
        this.payloadLength = (byte) payloadLength;

        // Ideally the mapping should be class specific. For now,
        // only supporting OpenFlowBasic
        OFOXMFieldType.addMapping(this.value, this);
    }

    /**
     * Adds a mapping from type value to OFOXMFieldType enum
     *
     * @param i field type field value
     * @param mt type
     */
    static public void addMapping(short i, OFOXMFieldType mt) {
        if (mapping == null)
                mapping = new OFOXMFieldType[50];
            OFOXMFieldType.mapping[i] = mt;
    }

    /**
     * Remove a mapping from type value to OFOXMFieldType enum
     *
     * @param i field type field value
     */
    static public void removeMapping(short i) {
        mapping[i] = null;
    }

    /**
     * Given a wire protocol field type number, return the OFOXMFieldType
     * associated with it
     *
     * @param i field type field value
     * @return OFOXMFieldType enum type
     */
    static public OFOXMFieldType valueOf(byte i) {
        return mapping[i];
    }

    /**
     * @return the values
     */
    public short getMatchClass() {
        return matchClass;
    }
    public String getName() {
        return name;
    }
    public short getValue() {
        return value;
    }
    public byte getPayloadLength() {
        return payloadLength;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
        public String toString() {
            return "OFOXMFieldType [name=" + name + ", value=" + value +
                ", payloadLength=" + payloadLength + "]";
    }
}
