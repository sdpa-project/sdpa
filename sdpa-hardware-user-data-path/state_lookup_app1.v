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
// Modified as: state_lookup.v
// Modifier: Wu, Chenghui
//***********************
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

`include "onet_defines.v"
  module state_lookup_app1
    #(
      parameter PKT_SIZE_WIDTH = 12,                    // number of bits for pkt size
      parameter UDP_REG_SRC_WIDTH = 2,                   // identifies which module started this request
      parameter ST_WIDTH = 104,
      parameter ST_DATA_WIDTH = `TCP_STATE_WIDTH,
      parameter ST_SIZE = 8,
      parameter ST_WIDTH_2 = 112,
      parameter ST_SIZE_BITS = log2(ST_SIZE),
      parameter APP_ID = 2'b01,
      parameter NEXT_APP = 2'b10,
      parameter STATE_TABLE_LOOKUP_REG_ADDR_WIDTH = `STATE_TABLE_LOOKUP_REG_ADDR_WIDTH,
      parameter STATE_TABLE_LOOKUP_BLOCK_ADDR = `STATE_TABLE_LOOKUP_1_BLOCK_ADDR
      )
   (// --- Interface to state_lookup
   input                                     is_ACK,
   input                                     is_RST,
   input                                     is_SYN,
   input                                     is_FIN,
   input                                     is_tcp,
   input [ST_WIDTH-1:0]                      match_field,
   input                                     match_field_vld,
   input [`OPENFLOW_ACTION_WIDTH-1:0]        action_in,
   
   input                                           is_dns_query,
   input                                           is_dns_response,
   input [`OPENFLOW_ENTRY_SRC_PORT_WIDTH-1:0]      flow_entry_src_port,
   input [15:0]                                    query_id,
   input [1:0]                                     next_app_in,
   
   output reg [ST_WIDTH_2-1:0]                        match_field_out,
   //output reg                                         match_field_vld_out,
   
   output reg                                         app_done,
   output reg [`OPENFLOW_ACTION_WIDTH-1:0]            action_out,
   output reg [1:0]                                   next_app,
   
   output reg                                            is_dns_query_out,
   output reg                                            is_dns_response_out,
   output reg [`OPENFLOW_ENTRY_SRC_PORT_WIDTH-1:0]       flow_entry_src_port_out,
   //output reg [15:0]                                     query_id_held,

    // --- Interface to registers
    input                                  reg_req_in,
    input                                  reg_ack_in,
    input                                  reg_rd_wr_L_in,
    input  [`UDP_REG_ADDR_WIDTH-1:0]       reg_addr_in,
    input  [`CPCI_NF2_DATA_WIDTH-1:0]      reg_data_in,
    input  [UDP_REG_SRC_WIDTH-1:0]         reg_src_in,

    output                                 reg_req_out,
    output                                reg_ack_out,
    output                                 reg_rd_wr_L_out,
    output     [`UDP_REG_ADDR_WIDTH-1:0]   reg_addr_out,
    output    [`CPCI_NF2_DATA_WIDTH-1:0]  reg_data_out,
    output     [UDP_REG_SRC_WIDTH-1:0]     reg_src_out,

    // --- Interface to Watchdog Timer
    input                                  table_flush,

    // --- Misc
    input                                  reset,
    input                                  clk
   );

   `LOG2_FUNC
   `CEILDIV_FUNC

   //-------------------- Internal Parameters ------------------------
   localparam ST_NUM_DATA_WORDS_USED = ceildiv(`TCP_STATE_WIDTH,`CPCI_NF2_DATA_WIDTH);
   localparam ST_NUM_CMP_WORDS_USED  = ceildiv(ST_WIDTH, `CPCI_NF2_DATA_WIDTH);
   localparam ST_NUM_REGS_USED = (2 // for the read and write address registers
                                        + ST_NUM_DATA_WORDS_USED // for data associated with an entry
                                        + ST_NUM_CMP_WORDS_USED  // for the data to match on
                                        + ST_NUM_CMP_WORDS_USED  // for the don't cares
                                        );

   localparam SIMULATION = 0
         // synthesis translate_off
         || 1
         // synthesis translate_on
         ;


   //---------------------- Wires and regs----------------------------
   wire                                                      cam_busy;
   wire                                                      cam_match;
   wire [`STATE_TABLE_SIZE-1:0]                             cam_match_addr;
   wire [`STATE_TABLE_ENTRY_WIDTH-1:0]                       cam_cmp_din, cam_cmp_data_mask;
   wire [`STATE_TABLE_ENTRY_WIDTH-1:0]                       cam_din, cam_data_mask;
   wire                                                      cam_we;
   wire [ST_SIZE_BITS-1:0]                                 cam_wr_addr;

   //wire [ST_NUM_CMP_WORDS_USED-1:0]                          cam_busy_ind;
   //wire [ST_NUM_CMP_WORDS_USED-1:0]                           cam_match_addr_ind[`STATE_TABLE_SIZE-1:0];
   //wire [31:0]                                               cam_cmp_din_ind[ST_NUM_CMP_WORDS_USED-1:0];
   //wire [31:0]                                               cam_cmp_data_mask_ind[ST_NUM_CMP_WORDS_USED-1:0];
   //wire [31:0]                                               cam_din_ind[ST_NUM_CMP_WORDS_USED-1:0];
   //wire [31:0]                                               cam_data_mask_ind[ST_NUM_CMP_WORDS_USED-1:0];

   wire [`UDP_REG_ADDR_WIDTH-1:0]                            cam_reg_addr_out;
   wire [`CPCI_NF2_DATA_WIDTH-1:0]                           cam_reg_data_out;
   wire [UDP_REG_SRC_WIDTH-1:0]                              cam_reg_src_out;

   wire                                       state_lookup_result_tcam;
   wire                                       lookup_ack;
//   wire                                       st_hit;
//   wire []                                       st_data;
//   wire [ST_SIZE_BITS-1:0]                                 st_address;
   reg [`OPENFLOW_ACTION_WIDTH-1:0]                action_held;
   reg [ST_WIDTH_2-1:0]                            match_field_held;
   reg                                             is_dns_query_held;
   reg                                             is_dns_response_held;
   reg [`OPENFLOW_ENTRY_SRC_PORT_WIDTH-1:0]        flow_entry_src_port_held;

   //reg [`STATE_TABLE_SIZE-1:0]                             st_hit_address_decoded;
   //wire [`STATE_TABLE_SIZE*PKT_SIZE_WIDTH - 1:0]           st_hit_address_decoded_expanded;
   //wire [`STATE_TABLE_SIZE*PKT_SIZE_WIDTH - 1:0]           st_entry_hit_byte_size;
   //wire [`STATE_TABLE_SIZE*32 - 1:0]                       st_entry_last_seen_timestamps;

   wire [2 * `STATE_TABLE_SIZE * `STATE_TABLE_ENTRY_WIDTH - 1 :0]  lut_linear;
   
   wire    lookup_data_vld;
   //------------------------- Modules -------------------------------
   assign st_match_rdy = 1;

   assign lookup_data_vld = (next_app_in == 2'b01)? match_field_vld:0;
   st_unencoded_cam_lut_sm
     #(.CMP_WIDTH (`STATE_TABLE_ENTRY_WIDTH),
       .DATA_WIDTH (`TCP_STATE_WIDTH),
       .LUT_DEPTH  (`STATE_TABLE_SIZE),
       .TAG (STATE_TABLE_LOOKUP_BLOCK_ADDR),
       .REG_ADDR_WIDTH (STATE_TABLE_LOOKUP_REG_ADDR_WIDTH))
       state_cam_lut_sm
         (// --- Interface for lookups
        .lookup_data_vld      (lookup_data_vld),
          .lookup_req          (lookup_data_vld & is_tcp),
          .lookup_cmp_data     (match_field),
          .lookup_cmp_dmask    ({`STATE_TABLE_ENTRY_WIDTH{1'b0}}),
          .lookup_ack          (lookup_ack),
          //.next_app           (next_app_cam),
//          .lookup_hit          (st_hit),
//          .lookup_data         (st_data),
//          .lookup_address      (st_address),
         // Add for FW
         .add_state_entry      (is_tcp & lookup_data_vld & is_SYN & !is_ACK),
         .state_lookup_result   (state_lookup_result_tcam),
         .is_ACK               (is_ACK),
         .is_RST               (is_RST),
         .is_SYN               (is_SYN),
         .is_FIN               (is_FIN),
          // --- Interface to registers
          .reg_req_in          (reg_req_in),
          .reg_ack_in          (reg_ack_in),
          .reg_rd_wr_L_in      (reg_rd_wr_L_in),
          .reg_addr_in         (reg_addr_in),
          .reg_data_in         (reg_data_in),
          .reg_src_in          (reg_src_in),

          .reg_req_out         (reg_req_out),
          .reg_ack_out         (reg_ack_out),
          .reg_rd_wr_L_out     (reg_rd_wr_L_out),
          .reg_addr_out        (reg_addr_out),
          .reg_data_out        (reg_data_out),
          .reg_src_out         (reg_src_out),

          // --- CAM interface
          .cam_busy            (cam_busy),
          .cam_match           (cam_match),
          .cam_match_addr      (cam_match_addr),
          .cam_cmp_din         (cam_cmp_din),
          .cam_din             (cam_din),
          .cam_we              (cam_we),
          .cam_wr_addr         (cam_wr_addr),
          .cam_cmp_data_mask   (cam_cmp_data_mask),
          .cam_data_mask       (cam_data_mask),
          .lut_linear          (lut_linear),

          // --- Watchdog Timer Interface
          .table_flush         (table_flush),

          // --- Misc
          .reset               (reset),
          .clk                 (clk));

   tcam_parallel_matcher 
   #(
      .CMP_WIDTH (`STATE_TABLE_ENTRY_WIDTH),
      .DEPTH (`STATE_TABLE_SIZE),
      .DEPTH_BITS (log2(`STATE_TABLE_SIZE)),
      .ENCODE (0)
   ) state_cam
   (
      // Outputs
      .busy                             (cam_busy),
      .match                            (cam_match),
      .match_addr                       (cam_match_addr),
      // Inputs
      .clk                              (clk),
      .cmp_din                          (cam_cmp_din),
      .din                              (cam_din),
      .cmp_data_mask                    (cam_cmp_data_mask),
      .data_mask                        (cam_data_mask),
      .we                               (cam_we),
      .wr_addr                          (cam_wr_addr),
      .lut_linear                       (lut_linear)
   );

   //generic_regs
   //  #(.UDP_REG_SRC_WIDTH (UDP_REG_SRC_WIDTH),
   //    .TAG (STATE_TABLE_LOOKUP_BLOCK_ADDR),
   //    .REG_ADDR_WIDTH (STATE_TABLE_LOOKUP_REG_ADDR_WIDTH),
   //    .NUM_COUNTERS (`STATE_TABLE_SIZE  // for number of bytes
   //                   +`STATE_TABLE_SIZE // for number of packets
   //                   ),
   //    /*****************
   //* JN: FIXME For now we will reset on read during simulation only
   //*****************/
   //    .RESET_ON_READ (SIMULATION),
   //    .NUM_SOFTWARE_REGS (2),
   //    .NUM_HARDWARE_REGS (`STATE_TABLE_SIZE), // for last seen timestamps
   //    .COUNTER_INPUT_WIDTH (PKT_SIZE_WIDTH), // max pkt size
   //    .REG_START_ADDR (ST_NUM_REGS_USED) // used for the access to the cam/lut
   //    )
   //generic_regs
   //  (
   //   .reg_req_in        (cam_reg_req_out),
   //   .reg_ack_in        (cam_reg_ack_out),
   //   .reg_rd_wr_L_in    (cam_reg_rd_wr_L_out),
   //   .reg_addr_in       (cam_reg_addr_out),
   //   .reg_data_in       (cam_reg_data_out),
   //   .reg_src_in        (cam_reg_src_out),
   //
   //   .reg_req_out       (reg_req_out),
   //   .reg_ack_out       (reg_ack_out),
   //   .reg_rd_wr_L_out   (reg_rd_wr_L_out),
   //   .reg_addr_out      (reg_addr_out),
   //   .reg_data_out      (reg_data_out),
   //   .reg_src_out       (reg_src_out),
   //
   //   // --- counters interface
   //   .counter_updates   ({st_hit_address_decoded_expanded,
   //                        st_entry_hit_byte_size}
   //                       ),
   //   .counter_decrement ({(2*`STATE_TABLE_SIZE){1'b0}}),
   //
   //   // --- SW regs interface
   //   .software_regs     (),
   //
   //   // --- HW regs interface
   //   .hardware_regs     ({st_entry_last_seen_timestamps}),
   //
   //   .clk               (clk),
   //   .reset             (reset));

   /* we might receive four input packets simultaneously from ethernet. In addition,
    * we might receive a pkt from DMA. So we need at least 5 spots. */

   //fallthrough_small_fifo
   //  #(.WIDTH(1),
   //    .MAX_DEPTH_BITS(3))
   //   state_result_fifo
   //     (.din           (state_lookup_result_tcam),                      // Data in
   //      .wr_en         (lookup_ack),                 // Write enable
   //      .rd_en         (state_result_rd_en),              // Read the next word
   //      .dout          (state_lookup_result),
   //      .full          (),
   //      .nearly_full   (),
   //      .empty         (state_result_fifo_empty),
   //      .reset         (reset),
   //      .clk           (clk)
   //);

   //-------------------------- Logic --------------------------------
   //assign next_app_cam  = st_data[1:0];

    
   always @(posedge clk) begin
      if(reset)begin
         app_done    <= 0;
         next_app    <= 0;
      end
      else begin
         app_done                   <= 0;
         next_app                   <= NEXT_APP;
         if (match_field_vld) begin
            if (next_app_in != APP_ID) begin
               app_done                   <= 1;
               match_field_out            <= {match_field[103:40], match_field[31:0], query_id};
               //match_field_vld_out        <= 1;
               action_out                 <= action_in;
               is_dns_query_out           <= is_dns_query;
               is_dns_response_out        <= is_dns_response;
               flow_entry_src_port_out    <= flow_entry_src_port;
            end
            else begin
               match_field_held              <= {match_field[103:40], match_field[31:0], query_id};
               action_held                   <= action_in;
               is_dns_query_held             <= is_dns_query;
               is_dns_response_held          <= is_dns_response;
               flow_entry_src_port_held      <= flow_entry_src_port;
            end
         end
         if(lookup_ack) begin
            app_done                   <= 1;
            match_field_out            <= match_field_held;
            //match_field_vld_out        <= 1;
            is_dns_query_out           <= is_dns_query_held;
            is_dns_response_out        <= is_dns_response_held;
            if(!state_lookup_result_tcam)begin
               action_out  <= {action_held[`OPENFLOW_ACTION_WIDTH - 1: 16], 16'h0};
            end
            else begin
               action_out  <= action_held;
            end
         end
      end
   end

endmodule // state_lookup


