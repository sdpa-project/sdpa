`timescale 1ns / 1ps
`include "onet_defines.v"

module stateful_app_tb;
	localparam 	FLOW_ENTRY 		= 248'hffff000000219bf4e78af8b1569ee33808000a0000010a0000020604000050;
	localparam 	FLOW_ENTRY2 		= 248'hffff000000219bf4e78af8b1569ee33808000a0000010a0000020604000051;
	localparam  ACTION 	 		= {62'h0,2'b11,40'h0,32'h1,32'h4,88'h0,32'h0,16'h1,16'h3};
	localparam 	STATE 			= 8'h2;
	localparam  NEXT_STATE      = 8'h3;
	localparam  ACTION_FLAG 	= 16'h042;
	localparam  ACTION_PARAM	= {62'h0,2'b11,40'h0,32'h1,32'h5,88'h0,32'h0,16'h2,16'h4};
	localparam  NEXT_APP 		= 2'b10;
	localparam  EVENT_PARAM     = {64'h0,32'h2};
	localparam 	EVENT_PARAM_HDR = 32'h3;

	localparam  PKT_SIZE_WIDTH = 12;

	reg 								clk;
	reg 								reset;

	// --- lookup
	reg [`OPENFLOW_ENTRY_WIDTH-1:0] 	match_field_1;
	reg 								match_field_vld_1;
	reg [`OPENFLOW_ACTION_WIDTH-1:0] 	action_in_1;
	reg [1:0] 							app_id_1;
	reg [`EVENT_PARAM_WIDTH*3-1:0]     	event_param_in;

	wire [`OPENFLOW_ACTION_WIDTH-1:0] 	action_out_1;
	wire	 							app_done_1;
	wire [1:0]							next_app_1;
	wire [`EVENT_PARAM_WIDTH*3-1:0]     event_param_out;
	wire [`OPENFLOW_ENTRY_WIDTH-1:0] 	match_field_out;

	// --- registers
	reg 								reg_req_in;
	reg 								reg_ack_in;
	reg 								reg_rd_wr_L_in;
	reg  [`UDP_REG_ADDR_WIDTH-1:0]      reg_addr_in;
	reg  [`CPCI_NF2_DATA_WIDTH-1:0]     reg_data_in;
	reg  [1:0]        reg_src_in;

	initial begin
		clk  	<= 1;
		reset 	<= 1;
		match_field_vld_1 	<= 0;
		action_in_1 		<= 0;
		match_field_1 		<= 0;
		app_id_1 			<= 0;
		event_param_in 		<= 0;

		reg_req_in 			<= 0;
		reg_ack_in 			<= 0;
		reg_rd_wr_L_in 		<= 0;
		reg_addr_in 		<= 0;
		reg_data_in 		<= 0;
		reg_src_in 			<= 0;


		#10		reset 		<= 0;
		#700 	
		match_field_vld_1 	<= 1;
		action_in_1 		<= ACTION;
		match_field_1 		<= FLOW_ENTRY;
		app_id_1 			<= 1;
		event_param_in 		<= EVENT_PARAM;
		#10    
		match_field_vld_1 	<= 0;
		action_in_1 		<= 0;
		match_field_1 		<= 0;
		app_id_1 			<= 0;
		event_param_in 		<= 0;
		#80
		match_field_vld_1 	<= 1;
		action_in_1 		<= ACTION;
		match_field_1 		<= FLOW_ENTRY;
		app_id_1 			<= 1;
		event_param_in 		<= EVENT_PARAM;
		#10    
		match_field_vld_1 	<= 0;
		action_in_1 		<= 0;
		match_field_1 		<= 0;
		app_id_1 			<= 0;
		event_param_in 		<= 0;
	end

	always begin
		#5 clk  	<= ~clk;
	end

	stateful_app
		#(
		.PKT_SIZE_WIDTH(PKT_SIZE_WIDTH),
		.ST_WIDTH(`OPENFLOW_ENTRY_WIDTH),
		.APP_ID(1),

		.STATE_TABLE_LOOKUP_REG_ADDR_WIDTH(`STATE_TABLE_LOOKUP_REG_ADDR_WIDTH),

		.ST_DEFAULT_CMP_DATA(FLOW_ENTRY),
		.ST_DEFAULT_DATA(STATE),
		.AT_DEFAULT_CMP_DATA({NEXT_STATE,FLOW_ENTRY}),
		.AT_DEFAULT_LOOKUP_DATA({NEXT_APP,ACTION_PARAM,ACTION_FLAG}),
		.STT_DEFAULT_CMP_DATA({EVENT_PARAM[31:0],STATE}),
		.STT_DEFAULT_LOOKUP_DATA(NEXT_STATE)
		)
		state_lookup_app1
		(// --- Interface from wildcard_match
		.event_param_in 	(event_param_in),
		.match_field			(match_field_1),
		.match_field_vld		(match_field_vld_1),
		.action_in           (action_in_1),
		.app_id 			(app_id_1),

		.app_done            (app_done_1),
		.update_done 		   (update_done),
		.event_param_out 		(event_param_out),
		.match_field_out 		(match_field_out),
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

		// --- Misc
		.reset							(reset),
		.clk							   (clk)
		);

endmodule