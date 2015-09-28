`timescale 1ns / 1ps
`include "onet_defines.v"

module output_tb;
	localparam WORDS          = 83;
   localparam WORDS_FT       = 19;
   localparam FLOW_ENTRY_NUM = 4;
   localparam NUM_REG        = 18;
   localparam REG_REQ_NUM    = 100;

   localparam NUM_REG_ST_ENTRY_USED = 17;
   localparam NUM_REG_AT_ENTRY_USED = 29;
   localparam NUM_REG_STT_ENTRY_USED = 3;

   localparam FLOW_ENTRY     = 248'hffff000000219bf4e78af8b1569ee33808000a0000010a0000020604000050;
   localparam STATE          = 8'h2;
   localparam ACTION_FLAG    = 16'h0202;
   localparam ACTION_PARAM   = {62'h0,2'b11,40'h0,32'h1,32'h4,88'h0,32'h0,16'hb2,16'h4};
   localparam NEXT_APP       = 2'b01;
   localparam EVENT_PARAM    = 32'h2;
   localparam EVENT_MASK     = {32{1'b1}};
   localparam  NEXT_STATE      = 8'h3;

	reg [63:0] in_data;
	reg [7:0] in_ctrl;
	reg in_wr;
	reg out_rdy;
	reg reset;
	reg clk;
   reg                              reg_req_in;
   reg                              reg_ack_in;
   reg                              reg_rd_wr_L_in;
   reg  [`UDP_REG_ADDR_WIDTH-1:0]   reg_addr_in;
   reg  [`CPCI_NF2_DATA_WIDTH-1:0]  reg_data_in;
   reg  [1:0]     reg_src_in;

	// Outputs
	wire in_rdy;
	wire [63:0] out_data;
	wire [7:0] out_ctrl;
	wire out_wr;
   wire                             reg_req_out;
   wire                             reg_ack_out;
   wire                             reg_rd_wr_L_out;
   wire  [`UDP_REG_ADDR_WIDTH-1:0]  reg_addr_out;
   wire  [`CPCI_NF2_DATA_WIDTH-1:0] reg_data_out;
   wire  [1:0]    reg_src_out;
	
   reg [63:0] pkt [0:WORDS-1];
   reg [7:0] ctrl [0:WORDS-1];
   reg [7:0] word_count;
   reg [7:0] pkt_count;
   reg busy;

   // --- initiate flow table
   reg   [`OPENFLOW_ENTRY_WIDTH-1:0]    flow_entry[0:FLOW_ENTRY_NUM-1];
   reg   [`OPENFLOW_ACTION_WIDTH-1:0]   action[0:FLOW_ENTRY_NUM-1];
   reg   [`OPENFLOW_ENTRY_WIDTH-1:0]    flow_entry_wr;
   reg   [`OPENFLOW_ACTION_WIDTH-1:0]   action_wr;

   reg   [NUM_REG_ST_ENTRY_USED*`CPCI_NF2_DATA_WIDTH-1:0]   st_entry;
   reg   [NUM_REG_AT_ENTRY_USED*`CPCI_NF2_DATA_WIDTH-1:0]   at_entry;
   reg   [NUM_REG_STT_ENTRY_USED*`CPCI_NF2_DATA_WIDTH-1:0]          stt_entry;

   reg                                  app_busy;

   wire  [31:0]                        st_entry_wr[0:NUM_REG_ST_ENTRY_USED-1];
   wire  [31:0]                        at_entry_wr[0:NUM_REG_AT_ENTRY_USED-1];
   wire  [31:0]                        stt_entry_wr[0:NUM_REG_STT_ENTRY_USED-1];

   wire  [31:0]                         entry[0:7];
   wire   [31:0]                         at[0:9];

   reg                                  ft_busy;
   reg   [9:0]                          word_count_ft,word_count_ft_d1,word_count_ft_d2;
   reg   [9:0]                          flow_count,flow_count_d1,flow_count_d2;
   reg   [9:0]                          st_reg_count,at_reg_count,stt_reg_count;

   assign   at[0]    =  action_wr[31:0];
   assign   at[1]    =  action_wr[63:32];
   assign   at[2]    =  action_wr[95:64];
   assign   at[3]    =  action_wr[127:96];
   assign   at[4]    =  action_wr[159:128];
   assign   at[5]    =  action_wr[191:160];
   assign   at[6]    =  action_wr[223:192];
   assign   at[7]    =  action_wr[255:224];
   assign   at[8]    =  action_wr[287:256];
   assign   at[9]    =  action_wr[319:288];

   assign   entry[0] =  flow_entry_wr[31:0];
   assign   entry[1] =  flow_entry_wr[63:32];
   assign   entry[2] =  flow_entry_wr[95:64];
   assign   entry[3] =  flow_entry_wr[127:96];
   assign   entry[4] =  flow_entry_wr[159:128];
   assign   entry[5] =  flow_entry_wr[191:160];
   assign   entry[6] =  flow_entry_wr[223:192];
   assign   entry[7] =  flow_entry_wr[`OPENFLOW_ENTRY_WIDTH-1:224];

   generate

      genvar ii;
      for (ii=0; ii<NUM_REG_ST_ENTRY_USED-1; ii=ii+1) begin:gen_st_wrdata
         assign st_entry_wr[ii] = st_entry[ii*`CPCI_NF2_DATA_WIDTH +: `CPCI_NF2_DATA_WIDTH];
      end
      assign st_entry_wr[NUM_REG_ST_ENTRY_USED-1] = {8'h0,st_entry[NUM_REG_ST_ENTRY_USED*`CPCI_NF2_DATA_WIDTH-1:(NUM_REG_ST_ENTRY_USED-1)*`CPCI_NF2_DATA_WIDTH]};

      for (ii=0; ii<NUM_REG_AT_ENTRY_USED-1; ii=ii+1) begin:gen_at_wrdata
         assign at_entry_wr[ii] = at_entry[ii*`CPCI_NF2_DATA_WIDTH +: `CPCI_NF2_DATA_WIDTH];
      end
      assign at_entry_wr[NUM_REG_AT_ENTRY_USED-1] = {14'h0,at_entry[NUM_REG_AT_ENTRY_USED*`CPCI_NF2_DATA_WIDTH-1:(NUM_REG_AT_ENTRY_USED-1)*`CPCI_NF2_DATA_WIDTH]};

      for (ii=0; ii<NUM_REG_STT_ENTRY_USED-1; ii=ii+1) begin:gen_stt_wrdata
         assign stt_entry_wr[ii] = stt_entry[ii*`CPCI_NF2_DATA_WIDTH +: `CPCI_NF2_DATA_WIDTH];
      end
      assign stt_entry_wr[NUM_REG_STT_ENTRY_USED-1] = {4'h0,stt_entry[NUM_REG_STT_ENTRY_USED*`CPCI_NF2_DATA_WIDTH-1:(NUM_REG_STT_ENTRY_USED-1)*`CPCI_NF2_DATA_WIDTH]};

   endgenerate


	// Instantiate the Unit Under Test (UUT)
	output_port_lookup uut (
		.in_data(in_data), 
		.in_ctrl(in_ctrl), 
		.in_wr(in_wr), 
		.in_rdy(in_rdy), 
		
		.out_data(out_data), 
		.out_ctrl(out_ctrl), 
		.out_wr(out_wr), 
		.out_rdy(out_rdy), 
		
		 // --- Register interface
		 .reg_req_in        (reg_req_in),
		 .reg_ack_in        (reg_ack_in),
		 .reg_rd_wr_L_in    (reg_rd_wr_L_in),
		 .reg_addr_in       (reg_addr_in),
		 .reg_data_in       (reg_data_in),
		 .reg_src_in        (reg_src_in),

		 .reg_req_out          (reg_req_out),
		 .reg_ack_out          (reg_ack_out),
		 .reg_rd_wr_L_out      (reg_rd_wr_L_out),
		 .reg_addr_out         (reg_addr_out),
		 .reg_data_out         (reg_data_out),
		 .reg_src_out          (reg_src_out),
		 
		 .table_flush       (1'b0),
		.reset(reset), 
		.clk(clk)
	);


   initial begin
      forever begin
         #5 clk = 0;
         #5 clk = 1;
      end
   end
   
	initial begin
		reset = 1;
		busy = 1;
      ft_busy     <=    1;
      app_busy    <=    1;
      flow_count  <=    0;

// --- initiage flow table
      reg_req_in   <=   0;
      reg_addr_in  <=   0;
      reg_data_in  <=   0;
      reg_rd_wr_L_in    <=    0;
      reg_src_in   <=   0;
      reg_ack_in   <=   0;

      flow_entry[0]  <= 248'hffff000000219bf4e78af8b1569ee33808000a0000010a0000020604000050;
      flow_entry[1]  <= 248'hffff000000219bf4e78af8b1569ee33808000a0000010a0000020604000060;
      flow_entry[2]  <= 248'hffff000020689d48f0c214144b6b2a5908006505804fa66f081c110035de28;
      flow_entry[3]  <= 248'hffff000020689d48f0c214144b6b2a5908006505804fa66f081c11de280035;
      action[0]      <= {62'h0,2'b11,40'h0,32'h1,32'h4,88'h0,32'h0,16'hc1,16'h3};
      action[1]      <= {62'h0,2'b11,40'h0,32'h1,32'h6,152'h0};
      action[2]      <= {62'h0,2'b11,40'h0,32'h1,32'h4,88'h0,32'h0,16'hc1,16'h3};
      action[3]      <= {62'h0,2'b11,40'h0,32'h1,32'h4,88'h0,32'h0,16'hc1,16'h3};

      st_entry    <= {8'h0,FLOW_ENTRY,256'h0,24'h0,STATE};
      at_entry    <= {FLOW_ENTRY,STATE,256'h0,18'h0,ACTION_FLAG,ACTION_PARAM,NEXT_APP,EVENT_PARAM,EVENT_MASK};
      stt_entry   <= {22'h0,STATE,2'b01,EVENT_PARAM,64'h0,24'h0,NEXT_STATE};

		#12;
      reset = 0;
      
      flow_entry_wr  <=    flow_entry[0];
      action_wr      <=    action[0];
// --- initiage flow table
      
      #800
      ft_busy     <=    0;

      #2000
      app_busy    <=    0;

      #300
      busy        <=    0;


	end

   /* initiate table entries */
   always @(posedge clk ) begin
      if (ft_busy) begin
         word_count_ft  <=    0;
         flow_count     <=    0;
         word_count_ft_d1  <=    NUM_REG  ;
         flow_count_d1     <=    0;
         word_count_ft_d2  <=    NUM_REG;
         flow_count_d2     <=    0;
      end
      else begin
         if (word_count_ft<NUM_REG&&flow_count<FLOW_ENTRY_NUM) begin
            word_count_ft_d1  <= 18;
            word_count_ft_d2  <= 18;
            if (word_count_ft<10) begin
               reg_addr_in    <=    {13'h1,word_count_ft};
               reg_data_in    <=    at[word_count_ft];

               word_count_ft  <=    word_count_ft + 1;
               reg_req_in     <=    1;             
            end
            else begin
               reg_addr_in    <=    {13'h1,word_count_ft}+8;
               reg_data_in    <=    entry[word_count_ft-10];

               word_count_ft  <=    word_count_ft + 1;
               reg_req_in     <=    1;               
            end
         end
         else begin
            word_count_ft_d1  <= 0;

            word_count_ft_d2  <= word_count_ft_d1;

            word_count_ft     <= word_count_ft_d2;

            if (flow_count<FLOW_ENTRY_NUM) begin
               reg_addr_in <=     {13'h1,10'd27};
               reg_data_in <=     {22'h0,flow_count};
               reg_req_in     <=    1;               

               flow_count_d1  <= flow_count+1;

               flow_count_d2  <= flow_count_d1;

               flow_count     <= flow_count_d2;
               if (flow_count<FLOW_ENTRY_NUM-1) begin
                  flow_entry_wr  <=    flow_entry[flow_count+1];
                  action_wr      <=    action[flow_count+1];                  
               end
            end
            else begin
               ft_busy  <= 1;
               reg_req_in  <= 0;
            end
         end

      end
   end

   /* initiate st entries */   
   always @(posedge clk) begin
      if (app_busy) begin
         at_reg_count   <= 0;         
      end
      else begin
         if (at_reg_count == NUM_REG_AT_ENTRY_USED) begin
            at_reg_count   <=    0;
            app_busy       <=    1;

            reg_req_in  <=    1;
            reg_addr_in <=    {`REG_APP1_AT_TAG,at_reg_count}+1;
            reg_data_in <=    32'h3;
         end
         else begin
            at_reg_count   <=    at_reg_count+1;
            reg_req_in  <=    1;
            reg_addr_in <=    {`REG_APP1_AT_TAG,at_reg_count};
            reg_data_in <=    at_entry_wr[at_reg_count];
         end         
      end
   end

   always @(posedge clk) begin
      if (app_busy && ft_busy && busy) begin
         reg_req_in  <= 0;
      end
   end

   initial begin

   // --- TCP SYN
         pkt[0] = 64'h0000000900000042; 
         pkt[1] = 64'hF8B1569EE3380021;
         pkt[2] = 64'h9BF4E78A08004500;
         pkt[3] = 64'h002E04D240007F06;
         pkt[4] = 64'hE2EF0A0000010A00;
         pkt[5] = 64'h0002040000500001;
         pkt[6] = 64'hF7FA000000005002;
         pkt[7] = 64'h04009B8E0000FFFF;
         pkt[8] = 64'hFFFFFFFFFFFFFFFF;
         pkt[9] = 64'hFFFF000000000000;

         ctrl[0] = 8'hff;
         ctrl[1] = 8'h0;
         ctrl[2] = 8'h0;
         ctrl[3] = 8'h0;
         ctrl[4] = 8'h0;
         ctrl[5] = 8'h0;
         ctrl[6] = 8'h0;
         ctrl[7] = 8'h0;
         ctrl[8] = 8'h0;
         ctrl[9] = 8'b01000000;
   // --- TCP SYN +ACK
      pkt[10] = 64'h0000000900000042;
      pkt[11] = 64'hF8B1569EE3380021;
      pkt[12] = 64'h9BF4E78A08004500;
      pkt[13] = 64'h002E04D240007F06;
      pkt[14] = 64'hE2EF0A0000010A00;
      pkt[15] = 64'h0002040000500001;
      pkt[16] = 64'hF7FA000000005012;
      pkt[17] = 64'h04009B780000FFFF;
      pkt[18] = 64'hFFFFFFFFFFFFFFFF;
      pkt[19] = 64'hFFFF000000000000;
      ctrl[10] = 8'hff;
      ctrl[11] = 8'h0;
      ctrl[12] = 8'h0;
      ctrl[13] = 8'h0;
      ctrl[14] = 8'h0;
      ctrl[15] = 8'h0;
      ctrl[16] = 8'h0;
      ctrl[17] = 8'h0;
      ctrl[18] = 8'h0;
      ctrl[19] = 8'b01000000;
   // --- TCP ACK
      pkt[20] = 64'h0000000900000042;
      pkt[21] = 64'hF8B1569EE3380020;
      pkt[22] = 64'h9BF4E78A08004500;
      pkt[23] = 64'h002E04D240007F06;
      pkt[24] = 64'hE2EF0A0000010A00;
      pkt[25] = 64'h0002040000500001;
      pkt[26] = 64'hF7FA000000005010;
      pkt[27] = 64'h04009B7A0000FFFF;
      pkt[28] = 64'hFFFFFFFFFFFFFFFF;
      pkt[29] = 64'hFFFF000000000000;
      ctrl[20] = 8'hff;
      ctrl[21] = 8'h0;
      ctrl[22] = 8'h0;
      ctrl[23] = 8'h0;
      ctrl[24] = 8'h0;
      ctrl[25] = 8'h0;
      ctrl[26] = 8'h0;
      ctrl[27] = 8'h0;
      ctrl[28] = 8'h0;
      ctrl[29] = 8'b01000000;	  
   // --- TCP ACK
      pkt[30] = 64'h0000000900000042;
      pkt[31] = 64'hF8B1569EE3380021;
      pkt[32] = 64'h9BF4E78A08004500;
      pkt[33] = 64'h002E04D240007F06;
      pkt[34] = 64'hE2EF0A0000010A00;
      pkt[35] = 64'h0002040000500001;
      pkt[36] = 64'hF7FA000000005010;
      pkt[37] = 64'h04009B8E0000FFFF;
      pkt[38] = 64'hFFFFFFFFFFFFFFFF;
      pkt[39] = 64'hFFFF000000000000;
      ctrl[30] = 8'hff;
      ctrl[31] = 8'h0;
      ctrl[32] = 8'h0;
      ctrl[33] = 8'h0;
      ctrl[34] = 8'h0;
      ctrl[35] = 8'h0;
      ctrl[36] = 8'h0;
      ctrl[37] = 8'h0;
      ctrl[38] = 8'h0;
      ctrl[39] = 8'b01000000;
   // --- TCP ACK
      pkt[40] = 64'h0000000900000042;
      pkt[41] = 64'hF8B1569EE3380021;
      pkt[42] = 64'h9BF4E78A08004500;
      pkt[43] = 64'h002E04D240007F06;
      pkt[44] = 64'hE2EF0A0000010A00;
      pkt[45] = 64'h0002040000500001;
      pkt[46] = 64'hF7FA000000005010;
      pkt[47] = 64'h04009B7A0000FFFF;
      pkt[48] = 64'hFFFFFFFFFFFFFFFF;
      pkt[49] = 64'hFFFF000000000000;
      ctrl[40] = 8'hff;
      ctrl[41] = 8'h0;
      ctrl[42] = 8'h0;
      ctrl[43] = 8'h0;
      ctrl[44] = 8'h0;
      ctrl[45] = 8'h0;
      ctrl[46] = 8'h0;
      ctrl[47] = 8'h0;
      ctrl[48] = 8'h0;
      ctrl[49] = 8'b01000000;
   // --- DNS query
      pkt[50] = 64'h0000000a0000004d;
      pkt[51] = 64'h14144b6b2a592068;
      pkt[52] = 64'h9d48f0c208004500;
      pkt[53] = 64'h003f61fb00008011;
      pkt[54] = 64'h44d36505804fa66f;
      pkt[55] = 64'h081c0035de28002b;
      pkt[56] = 64'hda24087101000001;
      pkt[57] = 64'h0000000000000363;
      pkt[58] = 64'h726c096d6963726f;
      pkt[59] = 64'h736f667403636f6d;
      pkt[60] = 64'h0000010001000000;
      ctrl[50] = 8'hff;
      ctrl[51] = 8'h0;
      ctrl[52] = 8'h0;
      ctrl[53] = 8'h0;
      ctrl[54] = 8'h0;
      ctrl[55] = 8'h0;
      ctrl[56] = 8'h0;
      ctrl[57] = 8'h0;
      ctrl[58] = 8'h0;
      ctrl[59] = 8'h0;
      ctrl[60] = 8'b00001000;   
   // --- DNS response
      pkt[61] = 64'h0000000a0000004d;
      pkt[62] = 64'h14144b6b2a592068;
      pkt[63] = 64'h9d48f0c208004500;
      pkt[64] = 64'h003f61fb00008011;
      pkt[65] = 64'h44d36505804fa66f;
      pkt[66] = 64'h081cde280035002b;
      pkt[67] = 64'hda24087101000001;
      pkt[68] = 64'h0000000000000363;
      pkt[69] = 64'h726c096d6963726f;
      pkt[70] = 64'h736f667403636f6d;
      pkt[71] = 64'h0000010001000000;

      ctrl[61] = 8'hff;
      ctrl[62] = 8'h0;
      ctrl[63] = 8'h0;
      ctrl[64] = 8'h0;
      ctrl[65] = 8'h0;
      ctrl[66] = 8'h0;
      ctrl[67] = 8'h0;
      ctrl[68] = 8'h0;
      ctrl[69] = 8'h0;
      ctrl[70] = 8'h0;
      ctrl[71] = 8'b00001000;	  
   // --- DNS response
      pkt[72] = 64'h0000000a0000004d;
      pkt[73] = 64'h14144b6b2a592068;
      pkt[74] = 64'h9d48f0c208004500;
      pkt[75] = 64'h003f61fb00008011;
      pkt[76] = 64'h44d36505804fa66f;
      pkt[77] = 64'h081c0035de28002b;
      pkt[78] = 64'hda24087101000001;
      pkt[79] = 64'h0000000000000363;
      pkt[80] = 64'h726c096d6963726f;
      pkt[81] = 64'h736f667403636f6d;
      pkt[82] = 64'h0000010001000000;

      ctrl[72] = 8'hff;
      ctrl[73] = 8'h0;
      ctrl[74] = 8'h0;
      ctrl[75] = 8'h0;
      ctrl[76] = 8'h0;
      ctrl[77] = 8'h0;
      ctrl[78] = 8'h0;
      ctrl[79] = 8'h0;
      ctrl[80] = 8'h0;
      ctrl[81] = 8'h0;
      ctrl[82] = 8'b00001000;   
   out_rdy = 1;
   end
   
   always @(posedge clk)begin
      if(busy)begin
         in_data <= 0;
         in_ctrl <= 0;
         in_wr <= 0;
         word_count <= 0;
         pkt_count <= 0;
      end
      else begin
         in_data <= 0;
         in_ctrl <= 0;
         in_wr <= 0;
         if(pkt_count < 1)begin
            in_data <= pkt[word_count];
            in_ctrl <= ctrl[word_count];
            in_wr <= 1;
            if(word_count == WORDS)begin
               word_count <= 0;
               pkt_count <= pkt_count + 1;
            end
            else begin
               word_count <= word_count + 1;
            end
         end
      end
   end
      
endmodule

