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
  module stateful_app
    #(
      parameter APP_ID = 1,
      parameter PKT_SIZE_WIDTH = 12,                    // number of bits for pkt size
      parameter UDP_REG_SRC_WIDTH = 2,                   // identifies which module started this request
      // --- State Table Parameters
      parameter ST_WIDTH = `OPENFLOW_ENTRY_WIDTH,
      parameter ST_DATA_WIDTH = `TCP_STATE_WIDTH,
      parameter ST_SIZE = 8,
      parameter ST_SIZE_BITS = log2(ST_SIZE),
      parameter ST_TAG = 13'h1,
      // ---  Action Table Parameters
      parameter AT_CMP_WIDTH = `OPENFLOW_ENTRY_WIDTH+`TCP_STATE_WIDTH,
      parameter AT_DATA_WIDTH = `AT_ACTION_PARAM_WIDTH+`AT_ACTION_FLAG_WIDTH+`AT_NEXT_APP_WIDTH,
      parameter AT_SIZE = 8,
      parameter AT_SIZE_BITS = log2(AT_SIZE),
      parameter AT_TAG = 13'h2,
      parameter ACTION_FLAG_WIDTH = 16,
      // ---  State Transition Table ----
      parameter STT_CMP_WIDTH = `TCP_STATE_WIDTH+`EVENT_PARAM_WIDTH,
      parameter STT_DATA_WIDTH = `TCP_STATE_WIDTH,
      parameter STT_SIZE = 8,
      parameter STT_SIZE_BITS = log2(STT_SIZE),
      parameter STT_TAG = 13'h3,
      
      parameter STATE_TABLE_LOOKUP_REG_ADDR_WIDTH = `STATE_TABLE_LOOKUP_REG_ADDR_WIDTH,
      // --- Debug interface
      parameter ST_DEFAULT_CMP_DATA = 248'h0,
      parameter ST_DEFAULT_DATA = 8'h0,

      parameter AT_DEFAULT_CMP_DATA = {8'h0,248'h0},
      parameter AT_DEFAULT_LOOKUP_DATA = {2'b01,320'h0,16'h0},
      
      parameter STT_DEFAULT_CMP_DATA = {32'h0,8'h0},
      parameter STT_DEFAULT_LOOKUP_DATA = {8'h0}
      )
     (// --- Interface to state_lookup
        input [`EVENT_PARAM_WIDTH*3-1:0]        event_param_in,
        input [ST_WIDTH-1:0]                    match_field,
        input                                   match_field_vld,
        input [`OPENFLOW_ACTION_WIDTH-1:0]      action_in,

        input [`AT_NEXT_APP_WIDTH-1:0]          app_id,

        output reg [`EVENT_PARAM_WIDTH*3-1:0]     event_param_out,
        output reg [ST_WIDTH-1:0]              match_field_out,
        output reg [`OPENFLOW_ACTION_WIDTH-1:0] action_out,
        output reg [`AT_NEXT_APP_WIDTH-1:0]     next_app,

        output                               update_done,
        output reg                              app_done,
        // --- Interface to registers
        input                                  reg_req_in,
        input                                  reg_ack_in,
        input                                  reg_rd_wr_L_in,
        input  [`UDP_REG_ADDR_WIDTH-1:0]       reg_addr_in,
        input  [`CPCI_NF2_DATA_WIDTH-1:0]      reg_data_in,
        input  [UDP_REG_SRC_WIDTH-1:0]         reg_src_in,

        output                                 reg_req_out,
        output                                 reg_ack_out,
        output                                 reg_rd_wr_L_out,
        output     [`UDP_REG_ADDR_WIDTH-1:0]   reg_addr_out,
        output     [`CPCI_NF2_DATA_WIDTH-1:0]  reg_data_out,
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
    wire [STT_SIZE-1:0]                          cam_match_addr;
    wire [ST_WIDTH-1:0]                       cam_cmp_din, cam_cmp_data_mask;
    wire [ST_WIDTH-1:0]                       cam_din, cam_data_mask;
    wire                                                      cam_we;
    wire [ST_SIZE_BITS-1:0]                                 cam_wr_addr;

    wire                                                      at_cam_busy;
    wire                                                      at_cam_match;
    wire [AT_SIZE-1:0]                          at_cam_match_addr;
    wire [AT_CMP_WIDTH-1:0]                       at_cam_cmp_din, at_cam_cmp_data_mask;
    wire [AT_CMP_WIDTH-1:0]                       at_cam_din, at_cam_data_mask;
    wire                                                      at_cam_we;
    wire [ST_SIZE_BITS-1:0]                                 at_cam_wr_addr;

    /*
    wire [`UDP_REG_ADDR_WIDTH-1:0]                            cam_reg_addr_out;
    wire [`CPCI_NF2_DATA_WIDTH-1:0]                           cam_reg_data_out;
    wire [UDP_REG_SRC_WIDTH-1:0]                              cam_reg_src_out;
    */
    wire                                 st_reg_req_out;
    wire                                 st_reg_ack_out;
    wire                                 st_reg_rd_wr_L_out;
    wire     [`UDP_REG_ADDR_WIDTH-1:0]   st_reg_addr_out;
    wire     [`CPCI_NF2_DATA_WIDTH-1:0]  st_reg_data_out;
    wire     [UDP_REG_SRC_WIDTH-1:0]     st_reg_src_out;

    wire                                 stt_reg_req_out;
    wire                                 stt_reg_ack_out;
    wire                                 stt_reg_rd_wr_L_out;
    wire     [`UDP_REG_ADDR_WIDTH-1:0]   stt_reg_addr_out;
    wire     [`CPCI_NF2_DATA_WIDTH-1:0]  stt_reg_data_out;
    wire     [UDP_REG_SRC_WIDTH-1:0]     stt_reg_src_out;


    wire                                  st_lookup_ack;

    wire [ST_SIZE_BITS-1:0]               st_lookup_address;
    wire                                  st_lookup_hit;
    wire [ST_DATA_WIDTH-1:0]                   state;

    wire                                  at_lookup_ack;
    wire                                  at_lookup_hit;
    wire [AT_DATA_WIDTH-1:0]              at_lookup_data;

    wire  [ACTION_FLAG_WIDTH-1:0]         action_flag;
    wire  [`OPENFLOW_ACTION_WIDTH-1:0]    action_param;
    wire  [`AT_NEXT_APP_WIDTH-1:0]        action_next_app;
    reg  [`AT_NEXT_APP_WIDTH-1:0]        action_next_app_d2;    

    wire  [`EVENT_PARAM_WIDTH-1:0]         event_param_local;
    wire  [`EVENT_PARAM_WIDTH-1:0]        event_param_at;
    wire  [`EVENT_PARAM_WIDTH-1:0]        event_param_mask;

    wire [2 * ST_SIZE * ST_WIDTH - 1 :0]  lut_linear;
    wire [2 * AT_SIZE * AT_CMP_WIDTH - 1 :0]  at_lut_linear;
    wire [2 * STT_SIZE * STT_CMP_WIDTH - 1 :0]  stt_lut_linear;

    wire                                 stt_lookup_ack,stt_lookup_hit;
    wire [`TCP_STATE_WIDTH-1:0]          next_state;
    reg [ST_SIZE_BITS-1:0]              update_addr;

    wire                                                      stt_cam_busy;
    wire                                                      stt_cam_match;
    wire [STT_SIZE-1:0]                                       stt_cam_match_addr;
    wire [STT_CMP_WIDTH-1:0]                       stt_cam_cmp_din, stt_cam_cmp_data_mask;
    wire [STT_CMP_WIDTH-1:0]                       stt_cam_din, stt_cam_data_mask;
    wire                                                      stt_cam_we;
    wire [STT_SIZE_BITS-1:0]                                 stt_cam_wr_addr;

    wire                                  app_hit_st;

    wire                                  mod_done;
    wire [`OPENFLOW_ACTION_WIDTH-1:0]     action_mod_out;


    // ----------------------- sustain input singals------------------
    wire [`EVENT_PARAM_WIDTH*3-1:0]       event_param_fifo_out;
    wire [ST_WIDTH-1:0]                   match_field_fifo_out;
    wire [`OPENFLOW_ACTION_WIDTH-1:0]     action_fifo_out;
    wire [`AT_NEXT_APP_WIDTH-1:0]         app_id_fifo_out;

    wire [`EVENT_PARAM_WIDTH-1:0]         event_param_stt_fifo_out;

    wire                                  output_fifo_wr_en,output_fifo_rd_en;
    wire                                  stt_fifo_wr_en,stt_fifo_rd_en;
    
    wire                                  st_fifo_wr_en,st_fifo_rd_en;
    wire [`AT_NEXT_APP_WIDTH-1:0]         app_id_st_fifo_out;

    wire                                  at_fifo_wr_en,at_fifo_rd_en;
    wire [`OPENFLOW_ENTRY_WIDTH-1:0]      match_field_at_fifo_out;

    wire                                  am_fifo_wr_en,am_fifo_rd_en;
    wire [`OPENFLOW_ACTION_WIDTH-1:0]     am_fifo_out;

    // ----------------------- Logics --------------------------------
    assign action_flag = at_lookup_data[`AT_ACTION_FLAG_POS +: `AT_ACTION_FLAG_WIDTH];
    assign action_param = at_lookup_data[`AT_ACTION_PARAM_POS +: `AT_ACTION_PARAM_WIDTH];
    assign action_next_app = at_lookup_data[`AT_NEXT_APP_POS +: `AT_NEXT_APP_WIDTH];

    assign app_hit_st = app_id_st_fifo_out == APP_ID;

    assign event_param_local = event_param_in[`EVENT_PARAM_WIDTH*APP_ID-1:`EVENT_PARAM_WIDTH*(APP_ID-1)];

    assign output_fifo_wr_en = match_field_vld;
    assign output_fifo_rd_en = at_lookup_ack;

    assign st_fifo_wr_en = match_field_vld;
    assign stt_fifo_wr_en = match_field_vld;
    assign at_fifo_wr_en = match_field_vld;
    assign am_fifo_wr_en = match_field_vld;

   //------------------------- Modules -------------------------------
   assign st_match_rdy = 1;
   // -------------------------State Table----------------------------
   st_unencoded_cam_lut_sm
     #(.CMP_WIDTH (ST_WIDTH),
       .DATA_WIDTH (ST_DATA_WIDTH),
       .LUT_DEPTH  (ST_SIZE),
       .TAG (ST_TAG),
       .REG_ADDR_WIDTH (STATE_TABLE_LOOKUP_REG_ADDR_WIDTH),
       .DEFAULT_CMP_DATA(ST_DEFAULT_CMP_DATA),
       .DEFAULT_LOOKUP_DATA(ST_DEFAULT_DATA)
       )
       state_cam_lut_sm
         (// --- Interface for lookups
          .lookup_req          (match_field_vld),
          .lookup_cmp_data     (match_field),
          .lookup_cmp_dmask    ({ST_WIDTH{1'b0}}),
          .lookup_ack          (st_lookup_ack),
          .lookup_hit          (st_lookup_hit),
          .lookup_data         (state),
          .lookup_address      (st_lookup_address),

          // --- Interface to update
          .next_state           (next_state),
          .next_state_vld       (stt_lookup_hit & app_hit_st),
          .update_req           (stt_lookup_ack),
          .update_addr          (update_addr),

          .update_done          (update_done),

          // --- Interface to registers
          .reg_req_in          (reg_req_in),
          .reg_ack_in          (reg_ack_in),
          .reg_rd_wr_L_in      (reg_rd_wr_L_in),
          .reg_addr_in         (reg_addr_in),
          .reg_data_in         (reg_data_in),
          .reg_src_in          (reg_src_in),

          .reg_req_out         (st_reg_req_out),
          .reg_ack_out         (st_reg_ack_out),
          .reg_rd_wr_L_out     (st_reg_rd_wr_L_out),
          .reg_addr_out        (st_reg_addr_out),
          .reg_data_out        (st_reg_data_out),
          .reg_src_out         (st_reg_src_out),

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
        .CMP_WIDTH (ST_WIDTH),
        .DEPTH (ST_SIZE),
        .DEPTH_BITS (log2(ST_SIZE)),
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

    //------------------------- State Transition Table----------------

   at_unencoded_cam_lut_sm
     #(.CMP_WIDTH (STT_CMP_WIDTH),
       .DATA_WIDTH (STT_DATA_WIDTH),
       .LUT_DEPTH  (STT_SIZE),
       .TAG (STT_TAG),
       .REG_ADDR_WIDTH (STATE_TABLE_LOOKUP_REG_ADDR_WIDTH),
       // --- Debug Interface
       .DEFAULT_CMP_DATA(STT_DEFAULT_CMP_DATA),
       .DEFAULT_LOOKUP_DATA(STT_DEFAULT_LOOKUP_DATA)
       )
       stt_cam_lut_sm
         (// --- Interface for lookups
          .lookup_req          (st_lookup_ack ),
          .lookup_cmp_data     ({event_param_stt_fifo_out,state}),
          .lookup_cmp_dmask    ({STT_CMP_WIDTH{1'b0}}),
          .lookup_ack          (stt_lookup_ack),
          .lookup_hit          (stt_lookup_hit),
          .lookup_data         (next_state),

          // --- Interface to registers
          .reg_req_in          (stt_reg_req_out),
          .reg_ack_in          (stt_reg_ack_out),
          .reg_rd_wr_L_in      (stt_reg_rd_wr_L_out),
          .reg_addr_in         (stt_reg_addr_out),
          .reg_data_in         (stt_reg_data_out),
          .reg_src_in          (stt_reg_src_out),

          .reg_req_out         (reg_req_out),
          .reg_ack_out         (reg_ack_out),
          .reg_rd_wr_L_out     (reg_rd_wr_L_out),
          .reg_addr_out        (reg_addr_out),
          .reg_data_out        (reg_data_out),
          .reg_src_out         (reg_src_out),

          // --- CAM interface
          .cam_busy            (stt_cam_busy),
          .cam_match           (stt_cam_match),
          .cam_match_addr      (stt_cam_match_addr),
          .cam_cmp_din         (stt_cam_cmp_din),
          .cam_din             (stt_cam_din),
          .cam_we              (stt_cam_we),
          .cam_wr_addr         (stt_cam_wr_addr),
          .cam_cmp_data_mask   (stt_cam_cmp_data_mask),
          .cam_data_mask       (stt_cam_data_mask),
          .lut_linear          (stt_lut_linear),

          // --- Watchdog Timer Interface
          .table_flush         (table_flush),

          // --- Misc
          .reset               (reset),
          .clk                 (clk));

   tcam_parallel_matcher 
     #(
        .CMP_WIDTH (STT_CMP_WIDTH),
        .DEPTH (STT_SIZE),
        .DEPTH_BITS (log2(STT_SIZE)),
        .ENCODE (0)
     ) stt_cam
     (
        // Outputs
        .busy                             (stt_cam_busy),
        .match                            (stt_cam_match),
        .match_addr                       (stt_cam_match_addr),
        // Inputs
        .clk                              (clk),
        .cmp_din                          (stt_cam_cmp_din),
        .din                              (stt_cam_din),
        .cmp_data_mask                    (stt_cam_cmp_data_mask),
        .data_mask                        (stt_cam_data_mask),
        .we                               (stt_cam_we),
        .wr_addr                          (stt_cam_wr_addr),
        .lut_linear                       (stt_lut_linear)
     );


    //--------------------------Action Table-------------------------
   at_unencoded_cam_lut_sm
     #(.CMP_WIDTH (AT_CMP_WIDTH),
       .DATA_WIDTH (AT_DATA_WIDTH),
       .LUT_DEPTH  (AT_SIZE),
       .TAG (AT_TAG),
       .REG_ADDR_WIDTH (STATE_TABLE_LOOKUP_REG_ADDR_WIDTH),
       // --- Debug Interface
       .DEFAULT_CMP_DATA(AT_DEFAULT_CMP_DATA),
       .DEFAULT_LOOKUP_DATA(AT_DEFAULT_LOOKUP_DATA)
       )
       at_cam_lut_sm
         (// --- Interface for lookups
          .lookup_req          (stt_lookup_ack),
          .lookup_cmp_data     ({next_state,match_field_at_fifo_out}),
          .lookup_cmp_dmask    ({AT_CMP_WIDTH{1'b0}}),
          .lookup_ack          (at_lookup_ack),
          .lookup_hit          (at_lookup_hit),
          .lookup_data         (at_lookup_data),

          // --- Interface to registers
          .reg_req_in          (st_reg_req_out),
          .reg_ack_in          (st_reg_ack_out),
          .reg_rd_wr_L_in      (st_reg_rd_wr_L_out),
          .reg_addr_in         (st_reg_addr_out),
          .reg_data_in         (st_reg_data_out),
          .reg_src_in          (st_reg_src_out),

          .reg_req_out         (stt_reg_req_out),
          .reg_ack_out         (stt_reg_ack_out),
          .reg_rd_wr_L_out     (stt_reg_rd_wr_L_out),
          .reg_addr_out        (stt_reg_addr_out),
          .reg_data_out        (stt_reg_data_out),
          .reg_src_out         (stt_reg_src_out),

          // --- CAM interface
          .cam_busy            (at_cam_busy),
          .cam_match           (at_cam_match),
          .cam_match_addr      (at_cam_match_addr),
          .cam_cmp_din         (at_cam_cmp_din),
          .cam_din             (at_cam_din),
          .cam_we              (at_cam_we),
          .cam_wr_addr         (at_cam_wr_addr),
          .cam_cmp_data_mask   (at_cam_cmp_data_mask),
          .cam_data_mask       (at_cam_data_mask),
          .lut_linear          (at_lut_linear),

          // --- Watchdog Timer Interface
          .table_flush         (table_flush),

          // --- Misc
          .reset               (reset),
          .clk                 (clk));

   tcam_parallel_matcher 
     #(
        .CMP_WIDTH (AT_CMP_WIDTH),
        .DEPTH (AT_SIZE),
        .DEPTH_BITS (log2(AT_SIZE)),
        .ENCODE (0)
     ) at_cam
     (
        // Outputs
        .busy                             (at_cam_busy),
        .match                            (at_cam_match),
        .match_addr                       (at_cam_match_addr),
        // Inputs
        .clk                              (clk),
        .cmp_din                          (at_cam_cmp_din),
        .din                              (at_cam_din),
        .cmp_data_mask                    (at_cam_cmp_data_mask),
        .data_mask                        (at_cam_data_mask),
        .we                               (at_cam_we),
        .wr_addr                          (at_cam_wr_addr),
        .lut_linear                       (at_lut_linear)
     );

  //-------------------Action Modifier-------------------------------

  action_modifier am(
        //inputs
        .clk(clk),
        .reset(reset),
        .enable(at_lookup_ack),

        .action_in(am_fifo_out),
        .param(action_param),
        .flag(action_flag),

        //outputs
        .action_out(action_mod_out),
        .modification_done(mod_done)
      );

   //-------------------------- Logic --------------------------------

   /*-----------handle output-------------*/
   fallthrough_small_fifo
     #(.WIDTH(`EVENT_PARAM_WIDTH*3+ST_WIDTH+`AT_NEXT_APP_WIDTH+`OPENFLOW_ACTION_WIDTH),
       .MAX_DEPTH_BITS(3))
      output_fifo
        (.din           ({event_param_in,match_field,app_id,action_in}),     // Data in
         .wr_en         (output_fifo_wr_en),                 // Write enable
         .rd_en         (output_fifo_rd_en),              // Read the next word
         .dout          ({event_param_fifo_out,match_field_fifo_out,app_id_fifo_out,action_fifo_out}),
         .full          (),
         .nearly_full   (),
         .empty         (),
         .reset         (reset),
         .clk           (clk)
   );

   fallthrough_small_fifo
     #(.WIDTH(`EVENT_PARAM_WIDTH),
       .MAX_DEPTH_BITS(3))
      stt_fifo
        (.din           (event_param_local),     // Data in
         .wr_en         (stt_fifo_wr_en),                 // Write enable
         .rd_en         (stt_fifo_rd_en),              // Read the next word
         .dout          ({event_param_stt_fifo_out}),
         .full          (),
         .nearly_full   (),
         .empty         (),
         .reset         (reset),
         .clk           (clk)
   );

  stateful_app_buffer
    #(.WIDTH(1),
      .LEN(3))
    stt_fifo_en_buf(
      .din              (stt_fifo_wr_en),
      .dout             (stt_fifo_rd_en),
      .clk              (clk),
      .reset            (reset)
      );

   fallthrough_small_fifo
     #(.WIDTH(`OPENFLOW_ENTRY_WIDTH),
       .MAX_DEPTH_BITS(3))
      at_fifo
        (.din           (match_field),     // Data in
         .wr_en         (at_fifo_wr_en),                 // Write enable
         .rd_en         (at_fifo_rd_en),              // Read the next word
         .dout          (match_field_at_fifo_out),
         .full          (),
         .nearly_full   (),
         .empty         (),
         .reset         (reset),
         .clk           (clk)
   );

  stateful_app_buffer
    #(.WIDTH(1),
      .LEN(7))
    at_fifo_en_buf(
      .din              (at_fifo_wr_en),
      .dout             (at_fifo_rd_en),
      .clk              (clk),
      .reset            (reset)
      );

   fallthrough_small_fifo
     #(.WIDTH(`OPENFLOW_ACTION_WIDTH),
       .MAX_DEPTH_BITS(3))
      am_fifo
        (.din           ({action_in}),     // Data in
         .wr_en         (am_fifo_wr_en),                 // Write enable
         .rd_en         (am_fifo_rd_en),              // Read the next word
         .dout          ({am_fifo_out}),
         .full          (),
         .nearly_full   (),
         .empty         (),
         .reset         (reset),
         .clk           (clk)
   );

  stateful_app_buffer
    #(.WIDTH(1),
      .LEN(11))
    am_fifo_en_buf(
      .din              (am_fifo_wr_en),
      .dout             (am_fifo_rd_en),
      .clk              (clk),
      .reset            (reset)
      );

   fallthrough_small_fifo
     #(.WIDTH(`AT_NEXT_APP_WIDTH),
       .MAX_DEPTH_BITS(3))
      st_fifo
        (.din           ({app_id}),     // Data in
         .wr_en         (st_fifo_wr_en),                 // Write enable
         .rd_en         (st_fifo_rd_en),              // Read the next word
         .dout          ({app_id_st_fifo_out}),
         .full          (),
         .nearly_full   (),
         .empty         (),
         .reset         (reset),
         .clk           (clk)
   );      

  stateful_app_buffer
    #(.WIDTH(1),
      .LEN(7))
    st_fifo_en_buf(
      .din              (st_fifo_wr_en),
      .dout             (st_fifo_rd_en),
      .clk              (clk),
      .reset            (reset)
      );



  /*------------------sustain input signals----------------*/

  always @(posedge clk or posedge reset) begin
     if (reset) begin
        update_addr   <=  0;
     end
     else if (st_lookup_hit & st_lookup_ack) begin
        update_addr    <=  st_lookup_address;
     end
     else begin
        update_addr  <=  update_addr;
     end
   end

   always @(posedge clk or posedge reset) begin
     if (reset) begin
       action_out     <=  0;
       match_field_out  <=  0;
       event_param_out  <=  0;
       next_app         <=  0;
       app_done         <=  0;
     end
     else if (mod_done) begin
       app_done         <=  1;
       match_field_out  <=  match_field_fifo_out;
       event_param_out  <=  event_param_fifo_out;

       if (app_id_fifo_out==APP_ID) begin
         action_out     <=  action_mod_out;
         next_app       <=  action_next_app_d2;
       end
       else begin
         action_out     <=  action_fifo_out;
         next_app       <=  app_id_fifo_out;
       end

     end
     else begin
       action_out     <=  0;
       match_field_out  <=  0;
       event_param_out  <=  0;
       next_app         <=  0;
       app_done         <=  0;       
     end
   end

   always @(posedge clk) begin
     action_next_app_d2   <=  action_next_app;
   end

endmodule // state_lookup


