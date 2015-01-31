///////////////////////////////////////////////////////////////////////////////
// vim:set shiftwidth=3 softtabstop=3 expandtab:
// $Id: output_port_lookup.v 5697 2009-06-17 22:32:11Z tyabe $
//
// Module: output_port_lookup.v
// Project: NF2.1 OpenFlow Switch
// Author: Jad Naous <jnaous@stanford.edu>
//
// Description: Implements type 0 switching.
// This module brings together the modules that make up the openflow-specific
// functionality. When a packet comes in, the header parser gets the packet size
// and assembles the flow header to match against. The flow header is sent to
// the wildcard and the exact match modules which compare it against their
// entries. The result is then sent to the match_arbiter which decides which
// entry should be used (chooses exact over wildcard) and writes the actions
// into the result_fifo. The opl_processor reads the result_fifo, and writes
// the actions into the module headers. If the packet has no forwarding action
// or doesn't match any entries, it is dropped.
//
// Licensing: In addition to the NetFPGA license, the following license applies
//            to the source code in the OpenFlow Switch implementation on NetFPGA.
//
// Copyright (c) 2008 The Board of Trustees of The Leland Stanford Junior University
//
// We are making the OpenFlow specification and associated documentation (Software)
// available for public use and benefit with the expectation that others will use,
// modify and enhance the Software and contribute those enhancements back to the
// community. However, since we would like to make the Software available for
// broadest use, with as few restrictions as possible permission is hereby granted,
// free of charge, to any person obtaining a copy of this Software to deal in the
// Software under the copyrights without restriction, including without limitation
// the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
// sell copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.
//
// The name and trademarks of copyright holder(s) may NOT be used in advertising
// or publicity pertaining to the Software or any derivatives without specific,
// written prior permission.
///////////////////////////////////////////////////////////////////////////////
`timescale 1ns/1ps
`include "onet_defines.v"
  module output_port_lookup
    #(parameter DATA_WIDTH = 64,
      parameter CTRL_WIDTH=DATA_WIDTH/8,
      parameter UDP_REG_SRC_WIDTH = 2,
      parameter IO_QUEUE_STAGE_NUM = `IO_QUEUE_STAGE_NUM,
      parameter NUM_OUTPUT_QUEUES = 8,
      parameter NUM_IQ_BITS = 3,
      parameter STAGE_NUM = 4,
      parameter SRAM_ADDR_WIDTH = 19,
      parameter CPU_QUEUE_NUM = 0,
      parameter OPENFLOW_LOOKUP_REG_ADDR_WIDTH = 6,
      parameter OPENFLOW_LOOKUP_BLOCK_ADDR = 13'h9,
      parameter OPENFLOW_WILDCARD_LOOKUP_REG_ADDR_WIDTH = 10,
      parameter OPENFLOW_WILDCARD_LOOKUP_BLOCK_ADDR = 13'h1
   )

   (// --- data path interface
    output     [DATA_WIDTH-1:0]        out_data,
    output     [CTRL_WIDTH-1:0]        out_ctrl,
    output                             out_wr,
    input                              out_rdy,

    input  [DATA_WIDTH-1:0]            in_data,
    input  [CTRL_WIDTH-1:0]            in_ctrl,
    input                              in_wr,
    output                             in_rdy,

    // --- Register interface
    input                              reg_req_in,
    input                              reg_ack_in,
    input                              reg_rd_wr_L_in,
    input  [`UDP_REG_ADDR_WIDTH-1:0]   reg_addr_in,
    input  [`CPCI_NF2_DATA_WIDTH-1:0]  reg_data_in,
    input  [UDP_REG_SRC_WIDTH-1:0]     reg_src_in,

    output                             reg_req_out,
    output                             reg_ack_out,
    output                             reg_rd_wr_L_out,
    output  [`UDP_REG_ADDR_WIDTH-1:0]  reg_addr_out,
    output  [`CPCI_NF2_DATA_WIDTH-1:0] reg_data_out,
    output  [UDP_REG_SRC_WIDTH-1:0]    reg_src_out,
    
    // --- Watchdog Timer Interface
    input                              table_flush,

    // --- Misc
    input                              clk,
    input                              reset);

   `LOG2_FUNC
   `CEILDIV_FUNC

   //-------------------- Internal Parameters ------------------------
   localparam PKT_SIZE_WIDTH = 12;

   //------------------------ Wires/Regs -----------------------------
   // size is the action + input port
   wire [`OPENFLOW_ACTION_WIDTH-1:0]                          exact_data;
   wire [`OPENFLOW_ACTION_WIDTH-1:0]                          wildcard_data;

   wire [CTRL_WIDTH-1:0]                                      in_fifo_ctrl;
   wire [DATA_WIDTH-1:0]                                      in_fifo_data;
   
   //Add for FW
   // --- Interface between header_parser and wildcard_match
   wire                                       is_tcp_hp_wm;
   wire                                       is_ACK_hp_wm;
   wire                                       is_RST_hp_wm;
   wire                                       is_SYN_hp_wm;
   wire                                       is_FIN_hp_wm;
   wire                                      is_udp_hp_wm;
   wire [15:0]                               query_id_hp_wm;
   // -- Interface between wildcard_match and state_lookup
   wire                                       is_tcp_wm_sl;
   wire                                       is_ACK_wm_sl;
   wire                                       is_RST_wm_sl;
   wire                                       is_SYN_wm_sl;
   wire                                       is_FIN_wm_sl;
   wire                                      is_dns_query;
   wire                                      is_dns_response;
   wire [15:0]                               query_id_wm_sl;
   wire [`STATE_TABLE_ENTRY_WIDTH-1:0]         state_entry;
   wire                                       state_entry_vld;
   //wire                                       state_lookup_req;
   // --- Interface between state_lookup and opl_processsor
   //wire                                       state_result_fifo_empty;
   //wire                                       state_lookup_result;
   //wire                                       state_result_rd_en;
   wire [`OPENFLOW_ACTION_WIDTH-1:0]         wm_action_out;
   wire                                      wm_action_fifo_empty;
   wire                                      wm_action_fifo_rd_en;
   
   
   wire [`OPENFLOW_ENTRY_WIDTH-1:0]                           flow_entry;
   wire [`OPENFLOW_ENTRY_SRC_PORT_WIDTH-1:0]                  flow_entry_src_port_parsed;
   wire [`OPENFLOW_ENTRY_SRC_PORT_WIDTH-1:0]                  flow_entry_src_port;
   wire [PKT_SIZE_WIDTH-1:0]                                  pkt_size;

   reg [31:0]                                                 s_counter;
   reg [27:0]                                                 ns_counter;

   wire                                                       wildcard_reg_req_out;
   wire                                                       wildcard_reg_ack_out;
   wire                                                       wildcard_reg_rd_wr_L_out;
   wire [`UDP_REG_ADDR_WIDTH-1:0]                             wildcard_reg_addr_out;
   wire [`CPCI_NF2_DATA_WIDTH-1:0]                            wildcard_reg_data_out;
   wire [UDP_REG_SRC_WIDTH-1:0]                               wildcard_reg_src_out;

   wire                                                       state_reg_req_out;
   wire                                                       state_reg_ack_out;
   wire                                                       state_reg_rd_wr_L_out;
   wire [`UDP_REG_ADDR_WIDTH-1:0]                             state_reg_addr_out;
   wire [`CPCI_NF2_DATA_WIDTH-1:0]                            state_reg_data_out;
   wire [UDP_REG_SRC_WIDTH-1:0]                               state_reg_src_out;
   
//   wire [`OPENFLOW_ACTION_WIDTH+`OPENFLOW_ENTRY_SRC_PORT_WIDTH-1:0] result_fifo_din;
   wire [`OPENFLOW_ACTION_WIDTH+`OPENFLOW_ENTRY_SRC_PORT_WIDTH-1:0] result_fifo_dout;

   wire [NUM_OUTPUT_QUEUES-1:0]                                pkts_dropped;


   //------------------------- Modules -------------------------------

   /* each pkt can have up to:
    * - 18 bytes of Eth header including VLAN
    * - 15*4 = 60 bytes IP header including max number of options
    * - at least 4 bytes of tcp/udp header
    * total = 82 bytes approx 4 bits (8 bytes x 2^4 = 128 bytes)
    */
   fallthrough_small_fifo #(.WIDTH(CTRL_WIDTH+DATA_WIDTH), .MAX_DEPTH_BITS(8))
      input_fifo
        (.din           ({in_ctrl, in_data}),  // Data in
         .wr_en         (in_wr),             // Write enable
         .rd_en         (in_fifo_rd_en),    // Read the next word
         .dout          ({in_fifo_ctrl, in_fifo_data}),
         .prog_full     (),
         .full          (),
         .nearly_full   (in_fifo_nearly_full),
         .empty         (in_fifo_empty),
         .reset         (reset),
         .clk           (clk)
         );

   header_parser
     #(.DATA_WIDTH                  (DATA_WIDTH),
       .CTRL_WIDTH                  (CTRL_WIDTH),
       .PKT_SIZE_WIDTH              (PKT_SIZE_WIDTH),
       .ADDITIONAL_WORD_SIZE        (`OPENFLOW_ENTRY_VLAN_ID_WIDTH),
       .ADDITIONAL_WORD_POS         (`OPENFLOW_ENTRY_VLAN_ID_POS),
       .ADDITIONAL_WORD_BITMASK     (16'hEFFF),  // --- PCP:3bits VID:12bits
       .ADDITIONAL_WORD_CTRL        (`VLAN_CTRL_WORD),
       .ADDITIONAL_WORD_DEFAULT     (16'hFFFF),
       .FLOW_ENTRY_SIZE             (`OPENFLOW_ENTRY_WIDTH)
       )
       header_parser
         ( // --- Interface to the previous stage
           .in_data                   (in_data),
           .in_ctrl                   (in_ctrl),
           .in_wr                     (in_wr),

           // --- Interface to matchers
           .flow_entry                (flow_entry),
           .flow_entry_src_port       (flow_entry_src_port_parsed),
           .pkt_size                  (pkt_size),
           .flow_entry_vld            (flow_entry_vld),
         //Interface to wildcard_match
         
         .is_tcp                  (is_tcp_hp_wm),
         .is_ACK                  (is_ACK_hp_wm),
         .is_RST                  (is_RST_hp_wm),
         .is_SYN                  (is_SYN_hp_wm),
         .is_FIN                  (is_FIN_hp_wm),
         
         .is_udp                 (is_udp_hp_wm),
         .query_id               (query_id_hp_wm),

           // --- Misc
           .reset                     (reset),
           .clk                       (clk));

   wildcard_match
     #(.NUM_OUTPUT_QUEUES(NUM_OUTPUT_QUEUES),
       .PKT_SIZE_WIDTH(PKT_SIZE_WIDTH),
       .OPENFLOW_WILDCARD_LOOKUP_REG_ADDR_WIDTH(OPENFLOW_WILDCARD_LOOKUP_REG_ADDR_WIDTH),
       .OPENFLOW_WILDCARD_LOOKUP_BLOCK_ADDR(OPENFLOW_WILDCARD_LOOKUP_BLOCK_ADDR)
       ) wildcard_match
       ( // --- Interface to flow entry collector
         .flow_entry                           (flow_entry),          // size `OPENFLOW_ENTRY_WIDTH
         .flow_entry_src_port_parsed           (flow_entry_src_port_parsed),
         .flow_entry_vld                       (flow_entry_vld),
         .wildcard_match_rdy                   (wildcard_match_rdy),
         .pkt_size                             (pkt_size),            // size 12
      // --- Interface to header_parser
       .is_tcp_in                  (is_tcp_hp_wm),
       .is_ACK_in                  (is_ACK_hp_wm),
       .is_RST_in                  (is_RST_hp_wm),
       .is_SYN_in                  (is_SYN_hp_wm),
       .is_FIN_in                  (is_FIN_hp_wm),
       .is_udp_in                (is_udp_hp_wm),
       .query_id_in              (query_id_hp_wm),
      // --- Interface to state_processor
      .is_tcp                     (is_tcp_wm_sl),
      .is_ACK                     (is_ACK_wm_sl),
      .is_RST                     (is_RST_wm_sl),
      .is_SYN                     (is_SYN_wm_sl),
      .is_FIN                     (is_FIN_wm_sl),
      
      .is_dns_query              (is_dns_query),
      .is_dns_response           (is_dns_response),
      .query_id                  (query_id_wm_sl),
      
      .state_entry               (state_entry),
      .state_entry_vld            (state_entry_vld),
      .action_out                (wm_action_out),
      //.action_fifo_empty         (wm_action_fifo_empty),      
      //.action_fifo_rd_en         (wm_action_fifo_rd_en),   
      .flow_entry_src_port       (flow_entry_src_port),
         // --- Interface to arbiter
         .wildcard_hit                         (wildcard_hit),
         .wildcard_miss                        (wildcard_miss),
         .wildcard_data                        (wildcard_data[`OPENFLOW_ACTION_WIDTH-1 : 0]),
         .wildcard_data_vld                    (wildcard_data_vld),
         .wildcard_wins                        (wildcard_wins),
         .wildcard_loses                       (wildcard_loses),

         // --- Interface to register bus
         .reg_req_in                           (reg_req_in),
         .reg_ack_in                           (reg_ack_in),
         .reg_rd_wr_L_in                       (reg_rd_wr_L_in),
         .reg_addr_in                          (reg_addr_in),
         .reg_data_in                          (reg_data_in),
         .reg_src_in                           (reg_src_in),

         .reg_req_out                          (wildcard_reg_req_out),
         .reg_ack_out                          (wildcard_reg_ack_out),
         .reg_rd_wr_L_out                      (wildcard_reg_rd_wr_L_out),
         .reg_addr_out                         (wildcard_reg_addr_out),
         .reg_data_out                         (wildcard_reg_data_out),
         .reg_src_out                          (wildcard_reg_src_out),

         .openflow_timer                       (s_counter), // bus size 32

         // --- Interface to Watchdog Timer
         .table_flush                          (table_flush),

         .clk                                  (clk),
         .reset                                (reset));
   
//   small_fifo
//     #(.WIDTH(`OPENFLOW_ACTION_WIDTH+`OPENFLOW_ENTRY_SRC_PORT_WIDTH),
//       .MAX_DEPTH_BITS(3))
//      action_fifo
//        (.din           ({flow_entry_src_port,wildcard_data}), // Data in
//         .wr_en         (wildcard_data_vld),   // Write enable
//         .rd_en         (result_fifo_rd_en),   // Read the next word
//         .dout          (result_fifo_dout),
//         .full          (),
//         .nearly_full   (result_fifo_nearly_full),
//         .empty         (result_fifo_empty),
//         .reset         (reset),
//         .clk           (clk)
//         );
   
   state_processor
      #(
      .PKT_SIZE_WIDTH(PKT_SIZE_WIDTH),
      .STATE_TABLE_LOOKUP_REG_ADDR_WIDTH(`STATE_TABLE_LOOKUP_REG_ADDR_WIDTH),
    .STATE_TABLE_LOOKUP_1_BLOCK_ADDR(`STATE_TABLE_LOOKUP_1_BLOCK_ADDR),
    .STATE_TABLE_LOOKUP_2_BLOCK_ADDR(`STATE_TABLE_LOOKUP_2_BLOCK_ADDR),
    .STATE_TABLE_LOOKUP_3_BLOCK_ADDR(`STATE_TABLE_LOOKUP_3_BLOCK_ADDR)
      )
      state_processor
      (// --- Interface from wildcard_match
      .is_ACK                     (is_ACK_wm_sl),
      .is_RST                     (is_RST_wm_sl),
      .is_SYN                     (is_SYN_wm_sl),
      .is_FIN                     (is_FIN_wm_sl),
      .is_tcp                     (is_tcp_wm_sl),
      .is_dns_query              (is_dns_query),
      .is_dns_response           (is_dns_response),
      .query_id                  (query_id_wm_sl),
      .state_entry               (state_entry),
      .state_entry_vld            (state_entry_vld),
      .flow_entry_src_port_in    (flow_entry_src_port),
      .action_in                 (wm_action_out),
      //.action_fifo_empty         (wm_action_fifo_empty),
      //.action_fifo_rd_en         (wm_action_fifo_rd_en),
      
      // --- Interface to opl_processor
      .result_fifo_dout          (result_fifo_dout),
      .result_fifo_rd_en         (result_fifo_rd_en),
      .result_fifo_empty         (result_fifo_empty),
      
      // --- Interface to registers
         .reg_req_in                          (wildcard_reg_req_out),
         .reg_ack_in                          (wildcard_reg_ack_out),
         .reg_rd_wr_L_in                      (wildcard_reg_rd_wr_L_out),
         .reg_addr_in                         (wildcard_reg_addr_out),
         .reg_data_in                         (wildcard_reg_data_out),
         .reg_src_in                          (wildcard_reg_src_out),

         .reg_req_out                          (state_reg_req_out),
         .reg_ack_out                          (state_reg_ack_out),
         .reg_rd_wr_L_out                      (state_reg_rd_wr_L_out),
         .reg_addr_out                         (state_reg_addr_out),
         .reg_data_out                         (state_reg_data_out),
         .reg_src_out                          (state_reg_src_out),
       
      // --- Interface to Watchdog Timer
      .table_flush               (table_flush),

      // --- Misc
      .reset                     (reset),
      .clk                     (clk)
      );

   opl_processor
     opl_processor
       (// --- interface to results fifo
        .result_fifo_dout    (result_fifo_dout),
        .result_fifo_rd_en   (result_fifo_rd_en),
        .result_fifo_empty   (result_fifo_empty),
         // --- Interface to state_lookup
         //.state_lookup_result   (state_lookup_result),
         //.state_result_fifo_empty (state_result_fifo_empty),
         //.state_result_rd_en      (state_result_rd_en),

        // --- interface to input fifo
        .in_fifo_ctrl        (in_fifo_ctrl),
        .in_fifo_data        (in_fifo_data),
        .in_fifo_rd_en       (in_fifo_rd_en),
        .in_fifo_empty       (in_fifo_empty),

        // --- interface to output
        .out_wr              (out_wr),
        .out_rdy             (out_rdy),
        .out_data            (out_data),
        .out_ctrl            (out_ctrl),

        // --- interface to registers
        .pkts_dropped        (pkts_dropped), // bus[NUM_OUTPUT_QUEUES-1:0]
        .wildcard_wins        (wildcard_wins),
        .wildcard_lose        (wildcard_lose),
        // --- Misc
        .clk                 (clk),
        .reset               (reset));

   generic_regs
     #(.UDP_REG_SRC_WIDTH (UDP_REG_SRC_WIDTH),
       .TAG (OPENFLOW_LOOKUP_BLOCK_ADDR),
       .REG_ADDR_WIDTH (OPENFLOW_LOOKUP_REG_ADDR_WIDTH),
       .NUM_COUNTERS (2*2                               // hits and misses for both tables
                      + NUM_OUTPUT_QUEUES               // num dropped per port
                      ),
       .NUM_SOFTWARE_REGS (2),
       .NUM_HARDWARE_REGS (2),
       .COUNTER_INPUT_WIDTH (1)
       )
   generic_regs
     (
      .reg_req_in        (state_reg_req_out),
      .reg_ack_in        (state_reg_ack_out),
      .reg_rd_wr_L_in    (state_reg_rd_wr_L_out),
      .reg_addr_in       (state_reg_addr_out),
      .reg_data_in       (state_reg_data_out),
      .reg_src_in        (state_reg_src_out),

      .reg_req_out       (reg_req_out),
      .reg_ack_out       (reg_ack_out),
      .reg_rd_wr_L_out   (reg_rd_wr_L_out),
      .reg_addr_out      (reg_addr_out),
      .reg_data_out      (reg_data_out),
      .reg_src_out       (reg_src_out),

      // --- counters interface
      .counter_updates   ({pkts_dropped,
                           1'b0,//exact_wins,
                           1'b0,//exact_miss,
                           wildcard_wins,
                           wildcard_lose}
                          ),
      .counter_decrement ({(4+NUM_OUTPUT_QUEUES){1'b0}}),

      // --- SW regs interface
      .software_regs     (),

      // --- HW regs interface
      .hardware_regs     ({32'h0,
                           s_counter}),

      .clk               (clk),
      .reset             (reset));

   //--------------------------- Logic ------------------------------
   assign in_rdy = !in_fifo_nearly_full && wildcard_match_rdy ;

   // timer
   always @(posedge clk) begin
      if(reset) begin
         ns_counter <= 0;
         s_counter  <= 0;
      end
      else begin
         if(ns_counter == (1_000_000_000/`FAST_CLOCK_PERIOD - 1'b1)) begin
            s_counter  <= s_counter + 1'b1;
            ns_counter <= 0;
         end
         else begin
            ns_counter <= ns_counter + 1'b1;
         end
      end // else: !if(reset)
   end // always @ (posedge clk)


endmodule // router_output_port
