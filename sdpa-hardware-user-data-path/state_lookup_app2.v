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
// Modified as: state_lookup_app2.v
// Modifier: Wu, Chenghui
///////////////////////////////////////////////////////////////////////////////

`include "onet_defines.v"
  module state_lookup_app2
    #(
      parameter PKT_SIZE_WIDTH = 12,                    // number of bits for pkt size
      parameter UDP_REG_SRC_WIDTH = 2,                   // identifies which module started this request
      parameter ST_WIDTH_1 = 104,
     parameter ST_WIDTH = 112,
      parameter ST_DATA_WIDTH = 2,
      parameter ST_SIZE = 8,
      parameter ST_SIZE_BITS = log2(ST_SIZE),
      parameter ST_WIDTH_3 = 64,
      parameter APP_ID = 2'b10,
      parameter NEXT_APP = 2'b11,
      //parameter STT_WIDTH = 4,
      //parameter STT_DATA_WIDTH = 2,
      //parameter STT_SIZE = 16,
      //parameter STT_SIZE_BITS = log2(STT_SIZE),
      //parameter AT_WIDTH = 2,
      //parameter AT_DATA_WIDTH = 1,
      //parameter AT_SIZE = 4,
      //parameter AT_SIZE_BITS = log2(AT_SIZE),
      parameter OPENFLOW_ACTION_WIDTH = `OPENFLOW_ACTION_WIDTH,
      parameter STATE_TABLE_LOOKUP_REG_ADDR_WIDTH = `STATE_TABLE_LOOKUP_REG_ADDR_WIDTH,
      parameter STATE_TABLE_LOOKUP_BLOCK_ADDR = `STATE_TABLE_LOOKUP_2_BLOCK_ADDR
      )
   (// --- input from state_lookup
   input                                           is_dns_query,
   input                                           is_dns_response,
   input [ST_WIDTH-1:0]                            match_field,
   input                                           match_field_vld,
   input [OPENFLOW_ACTION_WIDTH-1:0]               action_in,
   input [`OPENFLOW_ENTRY_SRC_PORT_WIDTH-1:0]      flow_entry_src_port,
   input [1:0]                                     next_app_in,
   // --- output to next lookup
   
   output reg [ST_WIDTH_3-1:0]                     match_field_out,
   //output reg                                      match_field_vld_out,

   // --- output to state_lookup
   output reg                                            app_done,
   output reg [OPENFLOW_ACTION_WIDTH-1:0]                action_out,
   output reg [1:0]                                      next_app,
   output reg [`OPENFLOW_ENTRY_SRC_PORT_WIDTH-1:0]       flow_entry_src_port_out,
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
   localparam ST_NUM_DATA_WORDS_USED = ceildiv(ST_DATA_WIDTH,`CPCI_NF2_DATA_WIDTH);
   localparam ST_NUM_CMP_WORDS_USED  = ceildiv(ST_WIDTH, `CPCI_NF2_DATA_WIDTH);
   localparam ST_NUM_REGS_USED = (2 // for the read and write address registers
                                        + ST_NUM_DATA_WORDS_USED // for data associated with an entry
                                        + ST_NUM_CMP_WORDS_USED  // for the data to match on
                                        + ST_NUM_CMP_WORDS_USED  // for the don't cares
                                        );

   localparam WAIT_FOR_INPUT = 1;
   localparam WAIT_FOR_LOOKUP = 2;
   
   localparam SIMULATION = 0
         // synthesis translate_off
         || 1
         // synthesis translate_on
         ;


   //---------------------- Wires and regs----------------------------
   //-------
   reg                                                      write_vld;
   reg [ST_SIZE_BITS-1:0]                                   write_addr;
   reg [ST_WIDTH-1:0]                                       write_cmp_data;
   reg [ST_WIDTH-1:0]                                       write_cmp_dmask;
   reg [ST_DATA_WIDTH-1:0]                                  write_data;
   
   wire                                                     cam_busy;
   wire                                                     cam_match;
   wire [ST_SIZE-1:0]                                       cam_match_addr;
   wire [ST_WIDTH-1:0]                                      cam_cmp_din, cam_cmp_data_mask;
   wire [ST_WIDTH-1:0]                                      cam_din, cam_data_mask;
   wire                                                     cam_we;
   wire [ST_SIZE_BITS-1:0]                                  cam_wr_addr;
   wire [2*ST_WIDTH*ST_SIZE - 1:0]                          lut_linear;
   
   wire                                                     cam_reg_req_out;
   wire                                                     cam_reg_ack_out;
   wire                                                     cam_reg_rd_wr_L_out;
   wire [`UDP_REG_ADDR_WIDTH-1:0]                           cam_reg_addr_out;
   wire [`CPCI_NF2_DATA_WIDTH-1:0]                          cam_reg_data_out;
   wire [UDP_REG_SRC_WIDTH-1:0]                             cam_reg_src_out;

   reg                                                      lookup_req;
   reg  [ST_WIDTH-1:0]                                      lookup_cmp_data;
   wire                                                     lookup_ack;
   wire                                                      lookup_hit;
   wire [ST_DATA_WIDTH-1:0]                                 lookup_data;
   wire [ST_SIZE_BITS-1:0]                                  lookup_address;

   reg [OPENFLOW_ACTION_WIDTH-1:0]                          action_temp;
   reg [1:0]                                                state;
   reg [ST_SIZE_BITS - 1:0]                                 current_addr;
   //-------
   reg [ST_WIDTH_3-1:0]                                     match_field_held;
   reg [`OPENFLOW_ENTRY_SRC_PORT_WIDTH-1:0]                 flow_entry_src_port_held;
   //------------------------- Modules -------------------------------
   assign st_match_rdy = 1;
   //assign match_field = {match_field_tmp[103:40], match_field_tmp[31:0], query_id};
   //assign match_field_vld = (next_app_id == 2'b10)? match_field_vld_tmp:0;
   unencoded_cam_lut_sm_write
     #(.CMP_WIDTH (ST_WIDTH),
       .DATA_WIDTH (ST_DATA_WIDTH),
       .LUT_DEPTH  (ST_SIZE),
       .TAG (STATE_TABLE_LOOKUP_BLOCK_ADDR),
       .REG_ADDR_WIDTH (STATE_TABLE_LOOKUP_REG_ADDR_WIDTH))
       cam_lut_sm
         (// --- Interface for lookups
          .lookup_req          (lookup_req),
          .lookup_cmp_data     (lookup_cmp_data),
          .lookup_cmp_dmask    ({ST_WIDTH{1'b0}}),
          .lookup_ack          (lookup_ack),
          .lookup_hit          (lookup_hit),
          .lookup_data         (lookup_data),
          .lookup_address      (lookup_address),
          
          .write_vld          (write_vld),
          .write_addr         (write_addr),
          .write_cmp_data     (write_cmp_data),
          .write_cmp_dmask    (write_cmp_dmask),
          .write_data         (write_data),
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
      .CMP_WIDTH (ST_WIDTH),
      .DEPTH (ST_SIZE),
      .DEPTH_BITS (log2(ST_SIZE)),
      .ENCODE (0)
   ) cam
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

// --- set next app id
// --- if action has been processed by NAT, then send out the action directly, 
// --- otherwise send the action to NAT


   always @(posedge clk) begin
      if (reset) begin
         current_addr         <= 0;
         state                <= WAIT_FOR_INPUT;
         app_done             <= 0;
         next_app             <= NEXT_APP;
      end
      else begin/*
         next_app    <= NEXT_APP;
         if (match_field_vld) begin
            if (next_app_in != APP_ID) begin 
               match_field_out               <= match_field[103:40];
               match_field_vld_out           <= match_field_vld;
               action_out                    <= action_in;
               flow_entry_src_port_held_out  <= flow_entry_src_port_held;
            
               app_done    <= 1;
            end
            else begin
               match_field_held                 <= match_field[103:40];
               action_held                      <= action_in;
               flow_entry_src_port_held         <= flow_entry_src_port;
            end
         end*/
         app_done    <= 0;
         case (state)
            WAIT_FOR_INPUT:begin
               if (match_field_vld) begin
                  action_temp       <= action_in;
                  match_field_held  <= match_field[111:48];
                  flow_entry_src_port_held   <= flow_entry_src_port;
                  if(is_dns_query) begin
                     write_vld      <= 1;
                     write_addr     <= current_addr;
                     current_addr   <= current_addr + 1'b1;
                     write_cmp_data <= {match_field[79:48], match_field[111:80], match_field[31:16], match_field[47:32], match_field[15:0]};
                     write_cmp_dmask <= {ST_WIDTH{1'b0}};
                     write_data     <= NEXT_APP;
                     app_done       <= 1'b1;
                     next_app       <= NEXT_APP;
                     action_out     <= action_in;
                     flow_entry_src_port_out <= flow_entry_src_port;
                  end
                  else if(is_dns_response) begin
                     lookup_req           <= 1'b1;
                     //lookup_cmp_data      <= {match_field[79:48], match_field[111:80], match_field[47:32], match_field[31:16], match_field[15:0]};
                     lookup_cmp_data      <= match_field;
                     state                <= WAIT_FOR_LOOKUP;

                     app_done             <= 1'b0;
                     action_out           <= action_in;
                  end
                  else begin
                     app_done              <=  1'b1;
                     action_out            <=  action_in;
                     match_field_out       <= match_field[111:48];
                     flow_entry_src_port_out  <= flow_entry_src_port;
                     //match_field_vld_out   <= 1;
                     next_app              <= NEXT_APP;
                  end
               end
            end
            
            WAIT_FOR_LOOKUP: begin
               if(lookup_ack) begin
                  app_done          <= 1'b1;
                  state             <= WAIT_FOR_INPUT;
                  next_app          <= NEXT_APP;
                  match_field_out   <= match_field_held;
                  flow_entry_src_port_out  <= flow_entry_src_port_held;
                  if(!lookup_hit) begin
                     action_out  <= {action_temp[OPENFLOW_ACTION_WIDTH-1:16], 16'h0};
                  end
                  else begin
                     action_out  <=  action_temp;
                  end
               end
            end
         endcase
      end
   end
   
//   
//   
//   fallthrough_small_fifo
//     #(.WIDTH(1),
//       .MAX_DEPTH_BITS(3))
//      state_result_fifo
//        (.din           (state_lookup_result_tcam),                      // Data in
//         .wr_en         (st_data_vld),                 // Write enable
//         .rd_en         (state_result_rd_en),              // Read the next word
//         .dout          (state_lookup_result),
//         .full          (),
//         .nearly_full   (),
//         .empty         (state_result_fifo_empty),
//         .reset         (reset),
//         .clk           (clk)
//   );
//
   //-------------------------- Logic --------------------------------

endmodule // state_lookup


