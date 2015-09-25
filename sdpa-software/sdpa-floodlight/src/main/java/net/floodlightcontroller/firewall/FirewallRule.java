/**
 *    Copyright 2011, Big Switch Networks, Inc.
 *    Originally created by Amer Tahir
 *    
 *    Licensed under the Apache License, Version 2.0 (the "License"); you may 
 *    not use this file except in compliance with the License. You may obtain
 *    a copy of the License at
 *    
 *         http://www.apache.org/licenses/LICENSE-2.0 
 *    
 *    Unless required by applicable law or agreed to in writing, software 
 *    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *    License for the specific language governing permissions and limitations
 *    under the License.
 **/

package net.floodlightcontroller.firewall;

import java.util.Arrays;
import java.nio.ByteBuffer;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import org.openflow.protocol.OFOXMFieldType;

import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.packet.IPacket;
import net.floodlightcontroller.packet.ARP;
import net.floodlightcontroller.packet.IPv4;
import net.floodlightcontroller.packet.TCP;
import net.floodlightcontroller.packet.UDP;

@JsonSerialize(using=FirewallRuleSerializer.class)
public class FirewallRule implements Comparable<FirewallRule> {
    public int ruleid;

    public long dpid; 
    public short in_port; 
    public long dl_src; 
    public long dl_dst; 
    public short dl_type; 
    public int nw_src_prefix; 
    public int nw_src_maskbits;
    public int nw_dst_prefix;
    public int nw_dst_maskbits;
    public short nw_proto;
    public short tp_src;
    public short tp_dst;

    public boolean wildcard_dpid;
    public boolean wildcard_in_port; 
    public boolean wildcard_dl_src;
    public boolean wildcard_dl_dst;
    public boolean wildcard_dl_type;
    public boolean wildcard_nw_src;
    public boolean wildcard_nw_dst;
    public boolean wildcard_nw_proto;
    public boolean wildcard_tp_src;
    public boolean wildcard_tp_dst;

    public int priority = 0;

    public FirewallAction action;

    public enum FirewallAction {
        /*
         * DENY: Deny rule
         * ALLOW: Allow rule
         */
        DENY, ALLOW
    }

    public FirewallRule() {
        this.in_port = 0; 
        this.dl_src = 0;
        this.nw_src_prefix = 0;
        this.nw_src_maskbits = 0; 
        this.dl_dst = 0;
        this.nw_proto = 0;
        this.tp_src = 0;
        this.tp_dst = 0;
        this.dl_dst = 0;
        this.dl_type = 0;
        this.nw_dst_prefix = 0;
        this.nw_dst_maskbits = 0; 
        this.dpid = -1;
        this.wildcard_dpid = true; 
        this.wildcard_in_port = true; 
        this.wildcard_dl_src = true; 
        this.wildcard_dl_dst = true; 
        this.wildcard_dl_type = true; 
        this.wildcard_nw_src = true; 
        this.wildcard_nw_dst = true; 
        this.wildcard_nw_proto = true; 
        this.wildcard_tp_src = true; 
        this.wildcard_tp_dst = true; 
        this.priority = 0; 
        this.action = FirewallAction.ALLOW; 
        this.ruleid = 0; 
    }

    /**
     * Generates a unique ID for the instance
     * 
     * @return int representing the unique id
     */
    public int genID() {
        int uid = this.hashCode();
        if (uid < 0) {
            uid = Math.abs(uid);
            uid = uid * 15551;
        }
        return uid;
    }

    /**
     * Comparison method for Collections.sort method
     * 
     * @param rule
     *            the rule to compare with
     * @return number representing the result of comparison 0 if equal negative
     *         if less than 'rule' greater than zero if greater priority rule
     *         than 'rule'
     */
    @Override
    public int compareTo(FirewallRule rule) {
        return this.priority - rule.priority;
    }

    /**
     * Determines if this instance matches an existing rule instance
     * 
     * @param r
     *            : the FirewallRule instance to compare with
     * @return boolean: true if a match is found
     **/
    public boolean isSameAs(FirewallRule r) {
        if (this.action != r.action
                || this.wildcard_dl_type != r.wildcard_dl_type
                || (this.wildcard_dl_type == false && this.dl_type != r.dl_type)
                || this.wildcard_tp_src != r.wildcard_tp_src
                || (this.wildcard_tp_src == false && this.tp_src != r.tp_src)
                || this.wildcard_tp_dst != r.wildcard_tp_dst
                || (this.wildcard_tp_dst == false &&this.tp_dst != r.tp_dst)
                || this.wildcard_dpid != r.wildcard_dpid
                || (this.wildcard_dpid == false && this.dpid != r.dpid)
                || this.wildcard_in_port != r.wildcard_in_port
                || (this.wildcard_in_port == false && this.in_port != r.in_port)
                || this.wildcard_nw_src != r.wildcard_nw_src
                || (this.wildcard_nw_src == false && (this.nw_src_prefix != r.nw_src_prefix || this.nw_src_maskbits != r.nw_src_maskbits))
                || this.wildcard_dl_src != r.wildcard_dl_src
                || (this.wildcard_dl_src == false && this.dl_src != r.dl_src)
                || this.wildcard_nw_proto != r.wildcard_nw_proto
                || (this.wildcard_nw_proto == false && this.nw_proto != r.nw_proto)
                || this.wildcard_nw_dst != r.wildcard_nw_dst
                || (this.wildcard_nw_dst == false && (this.nw_dst_prefix != r.nw_dst_prefix || this.nw_dst_maskbits != r.nw_dst_maskbits))
                || this.wildcard_dl_dst != r.wildcard_dl_dst                
                || (this.wildcard_dl_dst == false && this.dl_dst != r.dl_dst)) {
            return false;
        }
        return true;
    }

    /**
     * Matches this rule to a given flow - incoming packet
     * 
     * @param switchDpid
     *            the Id of the connected switch
     * @param inPort
     *            the switch port where the packet originated from
     * @param packet
     *            the Ethernet packet that arrives at the switch
     * @param wildcards
     *            the pair of wildcards (allow and deny) given by Firewall
     *            module that is used by the Firewall module's matchWithRule
     *            method to derive wildcards for the decision to be taken
     * @return true if the rule matches the given packet-in, false otherwise
     */
    public boolean matchesFlow(long switchDpid, int inPort, Ethernet packet,
            NonWildcardsPair nonWildcards) {
        IPacket pkt = packet.getPayload();

        // dl_type type
        ARP pkt_arp = null;
        IPv4 pkt_ip = null;

        // nw_proto types
        TCP pkt_tcp = null;
        UDP pkt_udp = null;
        
        // nw_src and nw_dst (IP addresses)
        int pkt_nw_src = 0;
        int pkt_nw_dst = 0;
        
        // tp_src and tp_dst (tp port numbers)
        short pkt_tp_src = 0;
        short pkt_tp_dst = 0;

        OFOXMFieldType pkt_nw_src_field=null, pkt_nw_dst_field=null;
        OFOXMFieldType pkt_tp_src_field=null, pkt_tp_dst_field=null;

        // switchID matches?
        if (wildcard_dpid == false && dpid != switchDpid)
            return false;

        // in_port matches?
        if (wildcard_in_port == false && in_port != inPort)
            return false;
        if (action == FirewallRule.FirewallAction.DENY) {
            nonWildcards.drop.add(OFOXMFieldType.IN_PORT);
        } else {
        	nonWildcards.allow.add(OFOXMFieldType.IN_PORT);
        }

        // mac address (src and dst) match?
        if (wildcard_dl_src == false
                && dl_src != packet.getSourceMAC().toLong())
            return false;
        if (action == FirewallRule.FirewallAction.DENY) {
            nonWildcards.drop.add(OFOXMFieldType.ETH_SRC);
        } else {
            nonWildcards.allow.add(OFOXMFieldType.ETH_SRC);
        }

        if (wildcard_dl_dst == false
                && dl_dst != packet.getDestinationMAC().toLong())
            return false;
        if (action == FirewallRule.FirewallAction.DENY) {
            nonWildcards.drop.add(OFOXMFieldType.ETH_DST);
        } else {
            nonWildcards.allow.add(OFOXMFieldType.ETH_DST);
        }

        // dl_type check: ARP, IP

        // if this is not an ARP rule but the pkt is ARP,
        // return false match - no need to continue protocol specific check
        if (wildcard_dl_type == false) {
            if (dl_type == Ethernet.TYPE_ARP) {
                if (packet.getEtherType() != Ethernet.TYPE_ARP)
                    return false;
                else {
                	if (action == FirewallRule.FirewallAction.DENY) {
                        nonWildcards.drop.add(OFOXMFieldType.ETH_TYPE);
                    } else {
                        nonWildcards.allow.add(OFOXMFieldType.ETH_TYPE);
                    }
                	pkt_nw_src_field = OFOXMFieldType.ARP_SHA;
                	pkt_nw_dst_field = OFOXMFieldType.ARP_THA;
                	pkt_arp = (ARP) pkt;
                	pkt_nw_src = ByteBuffer.wrap(pkt_arp.getSenderProtocolAddress()).getInt();
                	pkt_nw_dst = ByteBuffer.wrap(pkt_arp.getTargetProtocolAddress()).getInt();
                }
            } else if (dl_type == Ethernet.TYPE_IPv4) {
                if (packet.getEtherType() != Ethernet.TYPE_IPv4)
                    return false;
                else {
                    if (action == FirewallRule.FirewallAction.DENY) {
                        nonWildcards.drop.add(OFOXMFieldType.ETH_TYPE);
                    } else {
                        nonWildcards.allow.add(OFOXMFieldType.ETH_TYPE);
                    }
                	pkt_nw_src_field = OFOXMFieldType.IPV4_SRC;
                	pkt_nw_dst_field = OFOXMFieldType.IPV4_DST;
                    pkt_ip = (IPv4) pkt;
                	pkt_nw_src = pkt_ip.getSourceAddress();
                	pkt_nw_dst = pkt_ip.getDestinationAddress();
                }
            } else {
                // non-IP packet - not supported - report no match
                return false;
            }
                
            // For IPv4 or ARP packet, proceed with ip address check

            // IP addresses (src and dst) match?
            if (wildcard_nw_src == false
                    && this.matchIPAddress(nw_src_prefix,
                            nw_src_maskbits, pkt_nw_src) == false)
                return false;
            if (action == FirewallRule.FirewallAction.DENY) {
                nonWildcards.drop.add(pkt_nw_src_field);
                //TODO: Figure out how to set the IPv4 mask
                //nonWildcards.drop |= (nw_src_maskbits << OFMatch.OFPFW_NW_SRC_SHIFT);
            } else {
                nonWildcards.allow.add(pkt_nw_src_field);
                //TODO: Figure out how to set the IPv4 mask
                //nonWildcards.allow |= (nw_src_maskbits << OFMatch.OFPFW_NW_SRC_SHIFT);
            }

            if (wildcard_nw_dst == false
                    && this.matchIPAddress(nw_dst_prefix,
                            nw_dst_maskbits,
                            pkt_nw_dst) == false)
                return false;
            if (action == FirewallRule.FirewallAction.DENY) {
                nonWildcards.drop.add(pkt_nw_dst_field);
                //nonWildcards.drop |= (nw_dst_maskbits << OFMatch.OFPFW_NW_DST_SHIFT);
            } else {
                nonWildcards.allow.add(pkt_nw_dst_field);
                //nonWildcards.allow |= (nw_dst_maskbits << OFMatch.OFPFW_NW_DST_SHIFT);
            }

            // nw_proto check
            if ((wildcard_nw_proto == false) && (dl_type == Ethernet.TYPE_IPv4)) {
                if (nw_proto == IPv4.PROTOCOL_TCP) {
                    if (pkt_ip.getProtocol() != IPv4.PROTOCOL_TCP)
                        return false;
                    else {
                        pkt_tcp = (TCP) pkt_ip.getPayload();
                        pkt_tp_src = pkt_tcp.getSourcePort();
                        pkt_tp_dst = pkt_tcp.getDestinationPort();
                        pkt_tp_src_field = OFOXMFieldType.TCP_SRC;
                        pkt_tp_dst_field = OFOXMFieldType.TCP_DST;
                    }
                } else if (nw_proto == IPv4.PROTOCOL_UDP) {
                    if (pkt_ip.getProtocol() != IPv4.PROTOCOL_UDP)
                        return false;
                    else {
                        pkt_udp = (UDP) pkt_ip.getPayload();
                        pkt_tp_src = pkt_udp.getSourcePort();
                        pkt_tp_dst = pkt_udp.getDestinationPort();
                        pkt_tp_src_field = OFOXMFieldType.UDP_SRC;
                        pkt_tp_dst_field = OFOXMFieldType.UDP_DST;
                    }
                } else if (nw_proto == IPv4.PROTOCOL_ICMP) {
                    if (pkt_ip.getProtocol() != IPv4.PROTOCOL_ICMP)
                        return false;
                    else {
                        // nothing more needed for ICMP
                    }
                }
                if (action == FirewallRule.FirewallAction.DENY) {
                    nonWildcards.drop.add(OFOXMFieldType.ETH_TYPE);//prereq
                    nonWildcards.drop.add(OFOXMFieldType.IP_PROTO);
                } else {
                    nonWildcards.drop.add(OFOXMFieldType.ETH_TYPE);//prereq
                    nonWildcards.allow.add(OFOXMFieldType.IP_PROTO);
                }

                // TCP/UDP source and destination ports match?
                if (pkt_tcp != null || pkt_udp != null) {
                    // does the source port match?
                    if ((tp_src != 0 && tp_src != pkt_tp_src) || (pkt_tp_dst_field != null))
                        return false;
                    if (action == FirewallRule.FirewallAction.DENY) {
                        nonWildcards.drop.addAll(Arrays.asList(OFOXMFieldType.ETH_TYPE, OFOXMFieldType.IP_PROTO, //prereq
                        		pkt_tp_src_field));
                    } else {
                        nonWildcards.allow.addAll(Arrays.asList(OFOXMFieldType.ETH_TYPE, OFOXMFieldType.IP_PROTO, //prereq 
                        		pkt_tp_src_field));
                    }

                    // does the destination port match?
                    if ((tp_dst != 0 && tp_dst != pkt_tp_dst) || (pkt_tp_dst_field != null))
                        return false;
                    if (action == FirewallRule.FirewallAction.DENY) {
                        nonWildcards.drop.addAll(Arrays.asList(OFOXMFieldType.ETH_TYPE, OFOXMFieldType.IP_PROTO, //prereq
                        		pkt_tp_dst_field));
                    } else {
                        nonWildcards.allow.addAll(Arrays.asList(OFOXMFieldType.ETH_TYPE, OFOXMFieldType.IP_PROTO, //prereq
                        		pkt_tp_dst_field));
                    }
                }
            }
        }
        /* This code is probably not needed. TODO: Check
         */
        if (action == FirewallRule.FirewallAction.DENY) {
            nonWildcards.drop.add(OFOXMFieldType.ETH_TYPE);
        } else {
            nonWildcards.allow.add(OFOXMFieldType.ETH_TYPE);
        }

        // all applicable checks passed
        return true;
    }

    /**
     * Determines if rule's CIDR address matches IP address of the packet
     * 
     * @param rulePrefix
     *            prefix part of the CIDR address
     * @param ruleBits
     *            the size of mask of the CIDR address
     * @param packetAddress
     *            the IP address of the incoming packet to match with
     * @return true if CIDR address matches the packet's IP address, false
     *         otherwise
     */
    protected boolean matchIPAddress(int rulePrefix, int ruleBits,
            int packetAddress) {
        boolean matched = true;

        int rule_iprng = 32 - ruleBits;
        int rule_ipint = rulePrefix;
        int pkt_ipint = packetAddress;
        // if there's a subnet range (bits to be wildcarded > 0)
        if (rule_iprng > 0) {
            // right shift bits to remove rule_iprng of LSB that are to be
            // wildcarded
            rule_ipint = rule_ipint >> rule_iprng;
            pkt_ipint = pkt_ipint >> rule_iprng;
            // now left shift to return to normal range, except that the
            // rule_iprng number of LSB
            // are now zeroed
            rule_ipint = rule_ipint << rule_iprng;
            pkt_ipint = pkt_ipint << rule_iprng;
        }
        // check if we have a match
        if (rule_ipint != pkt_ipint)
            matched = false;

        return matched;
    }

    @Override
    public int hashCode() {
        final int prime = 2521;
        int result = super.hashCode();
        result = prime * result + (int) dpid;
        result = prime * result + in_port;
        result = prime * result + (int) dl_src;
        result = prime * result + (int) dl_dst;
        result = prime * result + dl_type;
        result = prime * result + nw_src_prefix;
        result = prime * result + nw_src_maskbits;
        result = prime * result + nw_dst_prefix;
        result = prime * result + nw_dst_maskbits;
        result = prime * result + nw_proto;
        result = prime * result + tp_src;
        result = prime * result + tp_dst;
        result = prime * result + action.ordinal();
        result = prime * result + priority;
        result = prime * result + (new Boolean(wildcard_dpid)).hashCode();
        result = prime * result + (new Boolean(wildcard_in_port)).hashCode();
        result = prime * result + (new Boolean(wildcard_dl_src)).hashCode();
        result = prime * result + (new Boolean(wildcard_dl_dst)).hashCode();
        result = prime * result + (new Boolean(wildcard_dl_type)).hashCode();
        result = prime * result + (new Boolean(wildcard_nw_src)).hashCode();
        result = prime * result + (new Boolean(wildcard_nw_dst)).hashCode();
        result = prime * result + (new Boolean(wildcard_nw_proto)).hashCode();
        result = prime * result + (new Boolean(wildcard_tp_src)).hashCode();
        result = prime * result + (new Boolean(wildcard_tp_dst)).hashCode();
        return result;
    }
}
