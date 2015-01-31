///////////////////////////////////////////////////////////////////////////////
// $Id: wildcard_match.v 5697 2009-06-17 22:32:11Z tyabe $
//
// Module: wildcard_match.v
// Project: NF2.1 OpenFlow Switch
// Author: Jad Naous <jnaous@stanford.edu>
// Description: matches a flow entry allowing a wildcard
//   Uses a register block to maintain counters associated with the table
//
//***********************
// Modified as: state_processor.v
// Modifier: Wu, Chenghui
//***********************
///////////////////////////////////////////////////////////////////////////////

`include "onet_defines.v"
  module state_processor
    #(
      parameter PKT_SIZE_WIDTH = 12,                    // number of bits for pkt size
      parameter UDP_REG_SRC_WIDTH = 2,                   // identifies which module started this request
      parameter ST_WIDTH_1 = 104,
      parameter ST_DATA_WIDTH_1 = 2,
      parameter ST_SIZE_1 = 32,
      parameter ST_WIDTH_2 = 112,
      parameter ST_DATA_WIDTH_2 = 2,
      parameter ST_SIZE_2 = 32,
      parameter ST_WIDTH_3 = 64,
      parameter ST_DATA_WIDTH_3 = 2,
      parameter ST_SIZE_3 = 32,
      parameter STATE_TABLE_LOOKUP_REG_ADDR_WIDTH = `STATE_TABLE_LOOKUP_REG_ADDR_WIDTH,
      parameter STATE_TABLE_LOOKUP_1_BLOCK_ADDR = `STATE_TABLE_LOOKUP_1_BLOCK_ADDR,
      parameter STATE_TABLE_LOOKUP_2_BLOCK_ADDR = `STATE_TABLE_LOOKUP_2_BLOCK_ADDR,
      parameter STATE_TABLE_LOOKUP_3_BLOCK_ADDR = `STATE_TABLE_LOOKUP_3_BLOCK_ADDR
      )
   (// --- Interface from wildcard_match
   input                                           is_ACK,
   input                                           is_RST,
   input                                           is_SYN,
   input                                           is_FIN,
   input                                           is_tcp,
   
   input                                           is_dns_query,
   input                                           is_dns_response,
   input [15:0]                                    query_id,
   
   input [`STATE_TABLE_ENTRY_WIDTH-1:0]            state_entry,
   input                                           state_entry_vld,
   input [`OPENFLOW_ENTRY_SRC_PORT_WIDTH-1:0]      flow_entry_src_port_in,
   input [`OPENFLOW_ACTION_WIDTH-1:0]              action_in,
   input                                           action_fifo_empty,
   output reg                                      action_fifo_rd_en,

   
   // --- Interface to opl_processor
   output [`OPENFLOW_ACTION_WIDTH+`OPENFLOW_ENTRY_SRC_PORT_WIDTH-1:0]  result_fifo_dout,
   input                                           result_fifo_rd_en,
   output                                          result_fifo_empty,
    // --- Interface to registers
    input                                          reg_req_in,
    input                                          reg_ack_in,
    input                                          reg_rd_wr_L_in,
    input  [`UDP_REG_ADDR_WIDTH-1:0]               reg_addr_in,
    input  [`CPCI_NF2_DATA_WIDTH-1:0]              reg_data_in,
    input  [UDP_REG_SRC_WIDTH-1:0]                 reg_src_in,


    output                                         reg_req_out,
    output                                         reg_ack_out,
    output                                         reg_rd_wr_L_out,
    output     [`UDP_REG_ADDR_WIDTH-1:0]           reg_addr_out,
    output     [`CPCI_NF2_DATA_WIDTH-1:0]          reg_data_out,
    output     [UDP_REG_SRC_WIDTH-1:0]             reg_src_out,
   
    // --- Interface to Watchdog Timer
    input                                  table_flush,

    // --- Misc
    input                                  reset,
    input                                  clk
   );

   `LOG2_FUNC
   `CEILDIV_FUNC

   //-------------------- Internal Parameters ------------------------

   localparam WAIT_FOR_INPUT  = 1;
   localparam GO_TO_APP       = 2;
   localparam WAIT_FOR_APP_1  = 4;
   localparam WAIT_FOR_APP_2  = 8;
   localparam WAIT_FOR_APP_3  = 16;

   //---------------------- Wires and regs----------------------------
   wire [1:0]                                   go_to_app_id;
   wire                                         go_to_fp;

   wire[ST_WIDTH_1-1:0]                                        match_field_1;
   wire[`OPENFLOW_ACTION_WIDTH-1:0]                            action_out_1;   
   wire[ST_WIDTH_2-1:0]                                        match_field_2;
   wire[`OPENFLOW_ACTION_WIDTH-1:0]                            action_out_2;   
   wire[ST_WIDTH_3-1:0]                                        match_field_3;
   wire[`OPENFLOW_ACTION_WIDTH-1:0]                            action_out_3;
   wire                                                        is_dns_query_1;
   wire                                                        is_dns_response_1;
   wire[`OPENFLOW_ENTRY_SRC_PORT_WIDTH-1:0]                    flow_entry_src_port_1;
   wire[`OPENFLOW_ENTRY_SRC_PORT_WIDTH-1:0]                    flow_entry_src_port_2;
   wire[`OPENFLOW_ENTRY_SRC_PORT_WIDTH-1:0]                    flow_entry_src_port_3;
   wire                                                        reg_req_out_1;
   wire                                                        reg_ack_out_1;
   wire                                                        reg_rd_wr_L_out_1;
   wire     [`UDP_REG_ADDR_WIDTH-1:0]                          reg_addr_out_1;
   wire    [`CPCI_NF2_DATA_WIDTH-1:0]                          reg_data_out_1;
   wire     [UDP_REG_SRC_WIDTH-1:0]                            reg_src_out_1;  
   wire                                                        reg_req_out_2;
   wire                                                        reg_ack_out_2;
   wire                                                        reg_rd_wr_L_out_2;
   wire     [`UDP_REG_ADDR_WIDTH-1:0]                          reg_addr_out_2;
   wire    [`CPCI_NF2_DATA_WIDTH-1:0]                          reg_data_out_2;
   wire     [UDP_REG_SRC_WIDTH-1:0]                            reg_src_out_2; 
   //wire                                                        reg_req_out_3;
   //wire                                                        reg_ack_out_3;
   //wire                                                        reg_rd_wr_L_out_3;
   //wire     [`UDP_REG_ADDR_WIDTH-1:0]                          reg_addr_out_3;
   //wire    [`CPCI_NF2_DATA_WIDTH-1:0]                          reg_data_out_3;
   //wire     [UDP_REG_SRC_WIDTH-1:0]                            reg_src_out_3; 
   
   wire[1:0]                                    next_app_1;
   wire[1:0]                                    next_app_2;
   wire                                         app_done_1;
   wire                                         app_done_2;
   wire                                         app_done_3;
   //------------------------- Modules ------------------------------- 
   small_fifo
     #(.WIDTH(`OPENFLOW_ACTION_WIDTH+`OPENFLOW_ENTRY_SRC_PORT_WIDTH),
       .MAX_DEPTH_BITS(3))
      result_fifo
        (.din           ({flow_entry_src_port_3,action_out_3}), // Data in
         .wr_en         (app_done_3),   // Write enable
         .rd_en         (result_fifo_rd_en),   // Read the next word
         .dout          (result_fifo_dout),
         .full          (),
         .nearly_full   (result_fifo_nearly_full),
         .empty         (result_fifo_empty),
         .reset         (reset),
         .clk           (clk)
         );

      
   state_lookup_app1
      state_lookup_app1
      (
      .is_ACK                                (is_ACK),
      .is_RST                                (is_RST),
      .is_SYN                                (is_SYN),
      .is_FIN                                (is_FIN),
      .is_tcp                                (is_tcp),
      .match_field                           (state_entry),
      .match_field_vld                       (state_entry_vld),
      .action_in                             (action_in),
      .is_dns_query                          (is_dns_query),
      .is_dns_response                       (is_dns_response),
      .flow_entry_src_port                   (flow_entry_src_port_in),
      .query_id                              (query_id),
      .next_app_in                           (go_to_app_id),
      
      .match_field_out                       (match_field_2),
      .app_done                              (app_done_1),
      .action_out                            (action_out_1),
      .next_app                              (next_app_1),
      .is_dns_query_out                      (is_dns_query_1),
      .is_dns_response_out                   (is_dns_response_1),
      .flow_entry_src_port_out               (flow_entry_src_port_1),
      //.query_id                        (query_id_held_1),
      
      // --- Interace to register
            // --- Interface to registers
         .reg_req_in                          (reg_req_in),
         .reg_ack_in                          (reg_ack_in),
         .reg_rd_wr_L_in                      (reg_rd_wr_L_in),
         .reg_addr_in                         (reg_addr_in),
         .reg_data_in                         (reg_data_in),
         .reg_src_in                          (reg_src_in),

         .reg_req_out                          (reg_req_out_1),
         .reg_ack_out                          (reg_ack_out_1),
         .reg_rd_wr_L_out                      (reg_rd_wr_L_out_1),
         .reg_addr_out                         (reg_addr_out_1),
         .reg_data_out                         (reg_data_out_1),
         .reg_src_out                          (reg_src_out_1),
       // --- Interface to Watchdog Timer
      .table_flush               (table_flush),

      // --- Misc
      .reset                     (reset),
      .clk                     (clk)
      );

   state_lookup_app2
      state_lookup_app2
      (
       .is_dns_query                         (is_dns_query_1),
       .is_dns_response                      (is_dns_response_1),
       .match_field                          (match_field_2),
       .match_field_vld                      (app_done_1),
       .action_in                            (action_out_1),
       .flow_entry_src_port                  (flow_entry_src_port_1),
       .next_app_in                          (next_app_1),
       .match_field_out                      (match_field_3),
       //.match_field_vld_out                     (match_field_vld_3),
       .app_done                             (app_done_2),
       .action_out                           (action_out_2),
       .next_app                             (next_app_2),
       .flow_entry_src_port_out              (flow_entry_src_port_2),
      // --- Interace to register
            // --- Interface to registers
         .reg_req_in                          (reg_req_out_1),
         .reg_ack_in                          (reg_ack_out_1),
         .reg_rd_wr_L_in                      (reg_rd_wr_L_out_1),
         .reg_addr_in                         (reg_addr_out_1),
         .reg_data_in                         (reg_data_out_1),
         .reg_src_in                          (reg_src_out_1),

         .reg_req_out                          (reg_req_out_2),
         .reg_ack_out                          (reg_ack_out_2),
         .reg_rd_wr_L_out                      (reg_rd_wr_L_out_2),
         .reg_addr_out                         (reg_addr_out_2),
         .reg_data_out                         (reg_data_out_2),
         .reg_src_out                          (reg_src_out_2),
       // --- Interface to Watchdog Timer
      .table_flush               (table_flush),

      // --- Misc
      .reset                     (reset),
      .clk                     (clk)       
      );

      
   state_lookup_app3
      state_lookup_app3
      (
       .match_field                          (match_field_3),
       .match_field_vld                      (app_done_2),
       .action_in                            (action_out_2),
       .flow_entry_src_port                  (flow_entry_src_port_2),
       .app_done                             (app_done_3),
       .action_out                           (action_out_3),
       .flow_entry_src_port_out              (flow_entry_src_port_3),
       //.next_app                             (next_app_3),
      // --- Interface to registers
         .reg_req_in                          (reg_req_out_2),
         .reg_ack_in                          (reg_ack_out_2),
         .reg_rd_wr_L_in                      (reg_rd_wr_L_out_2),
         .reg_addr_in                         (reg_addr_out_2),
         .reg_data_in                         (reg_data_out_2),
         .reg_src_in                          (reg_src_out_2),

         .reg_req_out                          (reg_req_out),
         .reg_ack_out                          (reg_ack_out),
         .reg_rd_wr_L_out                      (reg_rd_wr_L_out),
         .reg_addr_out                         (reg_addr_out),
         .reg_data_out                         (reg_data_out),
         .reg_src_out                          (reg_src_out),
       // --- Interface to Watchdog Timer
      .table_flush               (table_flush),

      // --- Misc
      .reset                     (reset),
      .clk                     (clk)
      );

   //-------------------------- Logic --------------------------------
   assign go_to_fp      = |(action_in[31:16] & `NF2_OFPAT_GO_TO_FP);
   assign go_to_app_id  = action_in[`GO_TO_APP_ID_POS+:`GO_TO_APP_ID_WIDTH];

endmodule // state_lookup
