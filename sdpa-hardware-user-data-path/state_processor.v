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
         parameter ENTRY_WIDTH = `OPENFLOW_ENTRY_WIDTH,
         parameter STATE_TABLE_LOOKUP_REG_ADDR_WIDTH = `STATE_TABLE_LOOKUP_REG_ADDR_WIDTH,

         // --- BLOCK adress for generic regs
         parameter APP1_ST_TAG    = 13'h1,
         parameter APP1_STT_TAG   = 13'h2,
         parameter APP1_AT_TAG    = 13'h3,
         parameter APP2_ST_TAG    = 13'h4,
         parameter APP2_STT_TAG   = 13'h5,
         parameter APP2_AT_TAG    = 13'h6,
         parameter APP3_ST_TAG    = 13'h7,
         parameter APP3_STT_TAG   = 13'h8,
         parameter APP3_AT_TAG    = 13'h9
      )
   (// --- Interface from wildcard_match
      input [`EVENT_PARAM_WIDTH-1:0]   event_param_in_1,
      input [`EVENT_PARAM_WIDTH-1:0]   event_param_in_2,
      input [`EVENT_PARAM_WIDTH-1:0]   event_param_in_3,

      input [ENTRY_WIDTH-1:0]	         state_entry,
      input [`OPENFLOW_ENTRY_SRC_PORT_WIDTH-1:0]      flow_entry_src_port_in,
      input [`OPENFLOW_ACTION_WIDTH-1:0]              action_in,
      input                                           action_fifo_empty,
      output                                      action_fifo_rd_en,


      // --- Interface to opl_processor
      output [`OPENFLOW_ACTION_WIDTH+`OPENFLOW_ENTRY_SRC_PORT_WIDTH-1:0]  result_fifo_dout,
      input                                           result_fifo_rd_en,
      output                                          result_fifo_empty,
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

   localparam WAIT_FOR_INPUT  = 1;
   localparam WIAT_FOR_UPDATE       = 2;

   //---------------------- Debug Parameters ------------------------
   localparam  FLOW_ENTRY     = 248'hffff000000219bf4e78af8b1569ee33808000a0000010a0000020604000050;
   localparam  ACTION         = {62'h0,2'b11,40'h0,32'h1,32'h4,88'h0,32'h0,16'hc1,16'h3};
   localparam  STATE          = 8'h2;
   localparam  NEXT_STATE      = 8'h3;
   localparam  ACTION_FLAG    = 16'h0202;
   localparam  ACTION_PARAM   = {62'h0,2'b11,40'h0,32'h1,32'h4,88'h0,32'h0,16'hb2,16'h4};
   localparam  NEXT_APP       = 2'b01;
   localparam  EVENT_PARAM     = 32'h2;
   localparam  EVENT_MASK     = {32{1'b1}};
   localparam  EVENT_PARAM_HDR = 32'h3;   

   //---------------------- Wires and regs----------------------------
   wire [`GO_TO_APP_ID_WIDTH-1:0]               go_to_app_id;
   wire [15:0]                                  go_to_fp;
   wire [`EVENT_PARAM_WIDTH*3-1:0]                event_param_in,event_param_out_1,event_param_out_2;             
   wire [`OPENFLOW_ENTRY_WIDTH-1:0]            match_field_out_1,match_field_out_2;

   reg [ENTRY_WIDTH-1:0]                        match_field_1;
   reg [`OPENFLOW_ACTION_WIDTH-1:0]             action_in_1;
   wire                                         app_done_1;
   wire [`OPENFLOW_ACTION_WIDTH-1:0]            action_out_1;
   wire [`GO_TO_APP_ID_WIDTH-1:0]               next_app_1;

   reg [ENTRY_WIDTH-1:0]                        match_field_2;
   reg [`OPENFLOW_ACTION_WIDTH-1:0]             action_in_2;
   wire                                         app_done_2;
   wire [`OPENFLOW_ACTION_WIDTH-1:0]            action_out_2;
   wire [`GO_TO_APP_ID_WIDTH-1:0]               next_app_2;
   
   wire                                         reg_req_out_1;
   wire                                         reg_ack_out_1;
   wire                                         reg_rd_wr_L_out_1;
   wire [`UDP_REG_ADDR_WIDTH-1:0]               reg_addr_out_1;
   wire [`CPCI_NF2_DATA_WIDTH-1:0]              reg_data_out_1;
   wire [UDP_REG_SRC_WIDTH-1:0]                 reg_src_out_1; 
   
   wire                                         reg_req_out_2;
   wire                                         reg_ack_out_2;
   wire                                         reg_rd_wr_L_out_2;
   wire [`UDP_REG_ADDR_WIDTH-1:0]               reg_addr_out_2;
   wire [`CPCI_NF2_DATA_WIDTH-1:0]              reg_data_out_2;
   wire [UDP_REG_SRC_WIDTH-1:0]                 reg_src_out_2; 
   
   wire                                          action_vld;
   reg [`OPENFLOW_ENTRY_SRC_PORT_WIDTH-1:0]     flow_entry_src_port;
   wire [`OPENFLOW_ACTION_WIDTH-1:0]             action_out;

   reg [4:0]                                    state;
   reg [4:0]                                    state_counter;
   reg                                          state_entry_req;
   wire                                         update_done;

   //------------------------- Modules -------------------------------
   
   stateful_app
      #(
      .PKT_SIZE_WIDTH(PKT_SIZE_WIDTH),
      .ST_WIDTH(`OPENFLOW_ENTRY_WIDTH),
      .APP_ID(1),

      .STATE_TABLE_LOOKUP_REG_ADDR_WIDTH(`STATE_TABLE_LOOKUP_REG_ADDR_WIDTH),

      .ST_TAG(APP1_ST_TAG),
      .AT_TAG(APP1_AT_TAG),
      .STT_TAG(APP1_STT_TAG),

      .ST_DEFAULT_CMP_DATA(FLOW_ENTRY),
      .ST_DEFAULT_DATA(STATE),
      .AT_DEFAULT_CMP_DATA({STATE,FLOW_ENTRY}),
      .AT_DEFAULT_LOOKUP_DATA({NEXT_APP,EVENT_MASK,EVENT_PARAM,ACTION_PARAM,ACTION_FLAG}),
      .STT_DEFAULT_CMP_DATA({2'h2,EVENT_PARAM,STATE}),
      .STT_DEFAULT_LOOKUP_DATA(NEXT_STATE)
      )
      stateful_app1
      (// --- Interface from wildcard_match
         .event_param_in      (event_param_in),
         .match_field         (state_entry),
         .match_field_vld     (state_entry_req),
         .action_in           (action_in),
         .app_id              (go_to_app_id),

         .update_done         (update_done),

         .app_done            (app_done_1),
         .event_param_out     (event_param_out_1),
         .match_field_out       (match_field_out_1),
         .action_out          (action_out_1),
         .next_app            (next_app_1),
      
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
         .clk                       (clk)
      );

   
   stateful_app
      #(
      .PKT_SIZE_WIDTH(PKT_SIZE_WIDTH),
      .ST_WIDTH(`OPENFLOW_ENTRY_WIDTH),
      .APP_ID(2),

      .STATE_TABLE_LOOKUP_REG_ADDR_WIDTH(`STATE_TABLE_LOOKUP_REG_ADDR_WIDTH),

      .ST_TAG(APP2_ST_TAG),
      .AT_TAG(APP2_AT_TAG),
      .STT_TAG(APP2_STT_TAG),

      .ST_DEFAULT_CMP_DATA(FLOW_ENTRY),
      .ST_DEFAULT_DATA(STATE),
      .AT_DEFAULT_CMP_DATA({STATE,FLOW_ENTRY}),
      .AT_DEFAULT_LOOKUP_DATA({NEXT_APP,EVENT_MASK,EVENT_PARAM,ACTION_PARAM,ACTION_FLAG}),
      .STT_DEFAULT_CMP_DATA({2'h2,EVENT_PARAM,STATE}),
      .STT_DEFAULT_LOOKUP_DATA(NEXT_STATE)
      )
      stateful_app2
		(// --- Interface from wildcard_match
      // --- connect dns signals to app2
         .event_param_in      (event_param_out_1),
         .match_field         (match_field_out_1),
         .match_field_vld     (app_done_1),
         .action_in           (action_out_1),
         .app_id              (next_app_1),

         .app_done            (app_done_2),
         .event_param_out     (event_param_out_2),
         .match_field_out       (match_field_out_2),
         .action_out          (action_out_2),
         .next_app            (next_app_2),    		
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
         .table_flush					(table_flush),

         // --- Misc
         .reset							(reset),
         .clk							   (clk)
		);
   
   stateful_app
      #(
      .PKT_SIZE_WIDTH(PKT_SIZE_WIDTH),
      .ST_WIDTH(`OPENFLOW_ENTRY_WIDTH),
      .APP_ID(3),

      .STATE_TABLE_LOOKUP_REG_ADDR_WIDTH(`STATE_TABLE_LOOKUP_REG_ADDR_WIDTH),
      
      .ST_TAG(APP3_ST_TAG),
      .AT_TAG(APP3_AT_TAG),
      .STT_TAG(APP3_STT_TAG),

      .ST_DEFAULT_CMP_DATA(FLOW_ENTRY),
      .ST_DEFAULT_DATA(STATE),
      .AT_DEFAULT_CMP_DATA({STATE,FLOW_ENTRY}),
      .AT_DEFAULT_LOOKUP_DATA({NEXT_APP,EVENT_MASK,EVENT_PARAM,ACTION_PARAM,ACTION_FLAG}),
      .STT_DEFAULT_CMP_DATA({2'h2,EVENT_PARAM,STATE}),
      .STT_DEFAULT_LOOKUP_DATA(NEXT_STATE)
      )
      stateful_app3
		(// --- Interface from wildcard_match
         .event_param_in      (event_param_out_2),
         .match_field         (match_field_out_2),
         .match_field_vld     (app_done_2),
         .action_in           (action_out_2),
         .app_id              (next_app_2),

         .app_done            (action_vld),
         .action_out          (action_out),
   		
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
   		.table_flush					(table_flush),

   		// --- Misc
   		.reset							(reset),
   		.clk							   (clk)
		);
   
   
   small_fifo
     #(.WIDTH(`OPENFLOW_ACTION_WIDTH+`OPENFLOW_ENTRY_SRC_PORT_WIDTH),
       .MAX_DEPTH_BITS(3))
      result_fifo
        (
         .din           ({flow_entry_src_port_in,action_out}), // Data in
         .wr_en         (action_vld),   // Write enable
         .rd_en         (result_fifo_rd_en),   // Read the next word
         .dout          (result_fifo_dout),
         .full          (),
         .nearly_full   (result_fifo_nearly_full),
         .empty         (result_fifo_empty),
         .reset         (reset),
         .clk           (clk)
         );

   //-------------------------- Logic --------------------------------
   assign go_to_app_id  = action_in[`GO_TO_APP_ID_POS+:`GO_TO_APP_ID_WIDTH];
   assign event_param_in = {event_param_in_3,event_param_in_2,event_param_in_1};

   assign action_fifo_rd_en = (state == WAIT_FOR_INPUT) & ~action_fifo_empty;
   
   always @(posedge clk or posedge reset) begin
      if (reset) begin
         state    <=    WAIT_FOR_INPUT;
      end
      else if (action_fifo_rd_en) begin
         state    <=    WIAT_FOR_UPDATE;
      end
      else if(state_counter == 7) begin
         state    <=    WAIT_FOR_INPUT;
      end
      else begin
         state    <=    state;
      end
   end

   always @(posedge clk or posedge reset) begin
      if (reset) begin
         state_counter     <=    0;         
      end
      else if (state==WAIT_FOR_INPUT) begin
         state_counter  <=    0;
      end
      else  begin
         state_counter  <=    state_counter +1;
      end
   end

   always @(posedge clk or posedge reset) begin
      if (reset) begin
         state_entry_req   <=     0;
      end
      else begin
         state_entry_req   <=    action_fifo_rd_en;
      end
   end

endmodule // state_lookup
