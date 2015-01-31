///////////////////////////////////////////////////////////////////////////////
// $Id: unencoded_cam_lut_sm.v 5697 2009-06-17 22:32:11Z tyabe $
//
// Module: unencoded_cam_lut_sm.v
// Project: NF2.1
// Author: Jad Naous <jnaous@stanford.edu>
// Description: controls an unencoded muli-match cam and provides a LUT.
//  Matches data and provides reg access
//
//  The sizes of the compare input and the data to store in the LUT can be
//  specified either by number of words or by exact size. The benefit of the
//  first is that you don't have to calculate the exact number of words used
//  in the parent module, but then the granularity of your matches will be
//  in increments of `CPCI_NF2_DATA_WIDTH bits, which might or might not matter
///////////////////////////////////////////////////////////////////////////////
`include "onet_defines.v"
  module st_unencoded_cam_lut_sm
    #(
      parameter DATA_WIDTH = 8,
      parameter CMP_WIDTH = 104,
      parameter LUT_DEPTH  = 64,
      parameter LUT_DEPTH_BITS = log2(LUT_DEPTH),
      parameter DEFAULT_DATA = 0,                       // DATA to return on a miss
      parameter RESET_DATA = {DATA_WIDTH{1'b0}},        // value of data on reset
      parameter RESET_CMP_DATA = {CMP_WIDTH{1'b0}},     // value of compare data on reset
      parameter RESET_CMP_DMASK = {CMP_WIDTH{1'b0}},    // value compare of data mask on reset
      parameter UDP_REG_SRC_WIDTH = 2,                  // identifies which module started this request
      parameter TAG = 0,                                // Tag identifying the address block
      parameter REG_ADDR_WIDTH = 5                      // Width of addresses in the same block
      )
   (// --- Interface for lookups
    input 							         lookup_data_vld,
    input                              lookup_req,
    input      [CMP_WIDTH-1:0]         lookup_cmp_data,
    input      [CMP_WIDTH-1:0]         lookup_cmp_dmask,
    output reg                         lookup_ack,
    output reg                         lookup_hit,
    output     [DATA_WIDTH-1:0]        lookup_data,
    output reg [LUT_DEPTH_BITS-1:0]    lookup_address,
	// Add for FW
	input									add_state_entry,
	output reg								state_lookup_result,
	input 									is_ACK,
	input 									is_RST,
	input 									is_SYN,
	input 									is_FIN,
	
    // --- Interface to registers
    input                                  reg_req_in,
    input                                  reg_ack_in,
    input                                  reg_rd_wr_L_in,
    input  [`UDP_REG_ADDR_WIDTH-1:0]       reg_addr_in,
    input  [`CPCI_NF2_DATA_WIDTH-1:0]      reg_data_in,
    input  [UDP_REG_SRC_WIDTH-1:0]         reg_src_in,

    output reg                             reg_req_out,
    output reg                             reg_ack_out,
    output reg                             reg_rd_wr_L_out,
    output reg [`UDP_REG_ADDR_WIDTH-1:0]   reg_addr_out,
    output reg [`CPCI_NF2_DATA_WIDTH-1:0]  reg_data_out,
    output reg [UDP_REG_SRC_WIDTH-1:0]     reg_src_out,

    // --- CAM interface
    output wire [2 * LUT_DEPTH * CMP_WIDTH - 1 :0]  lut_linear,
    input                              cam_busy,
    input                              cam_match,
    input      [LUT_DEPTH-1:0]         cam_match_addr,
    output     [CMP_WIDTH-1:0]         cam_cmp_din,
    output reg [CMP_WIDTH-1:0]         cam_din,
    output reg                         cam_we,
    output reg [LUT_DEPTH_BITS-1:0]    cam_wr_addr,
    output     [CMP_WIDTH-1:0]         cam_cmp_data_mask,
    output reg [CMP_WIDTH-1:0]         cam_data_mask,

    // --- Watchdog Timer Interface
    input                              table_flush,

    // --- Misc
    input                              reset,
    input                              clk
   );


   function integer log2;
      input integer number;
      begin
         log2=0;
         while(2**log2<number) begin
            log2=log2+1;
         end
      end
   endfunction // log2

   function integer ceildiv;
      input integer num;
      input integer divisor;
      begin
         if (num <= divisor)
            ceildiv = 1;
         else begin
            ceildiv = num / divisor;
            if (ceildiv * divisor < num)
               ceildiv = ceildiv + 1;
         end
      end
   endfunction // ceildiv

   //-------------------- Internal Parameters ------------------------
   localparam NUM_DATA_WORDS_USED = ceildiv(DATA_WIDTH,`CPCI_NF2_DATA_WIDTH);
   localparam NUM_CMP_WORDS_USED  = ceildiv(CMP_WIDTH, `CPCI_NF2_DATA_WIDTH);
   localparam NUM_REGS_USED = (2 // for the read and write address registers
                               + NUM_DATA_WORDS_USED // for data associated with an entry
                               + NUM_CMP_WORDS_USED  // for the data to match on
                               + NUM_CMP_WORDS_USED);  // for the don't cares

   localparam READ_ADDR  = NUM_REGS_USED-2;
   localparam WRITE_ADDR = READ_ADDR+1;

   localparam RESET = 0;
   localparam READY = 1;

   localparam WAIT_FOR_REQUEST = 1;
   localparam WAIT_FOR_READ_ACK = 2;
   localparam WAIT_FOR_WRITE_ACK = 4;
   
   localparam CLOSED			= 8'b00000001;
   localparam SYN				= 8'b00000011;
   localparam SYN_ACK			= 8'b00000111;
   localparam ESTABLISHED		= 8'b00001111;
   localparam FIN_WAIT1			= 8'b00011111;
   localparam FIN_WAIT2			= 8'b00111111;
   localparam CLOSING0			= 8'b01111111;
   localparam CLOSING1			= 8'b11111111;

   localparam WRITE0			= 1;
   localparam WRITE1			= 2;
   localparam WRITE2			= 3;
   localparam WRITE3			= 4;
   localparam WRITE4			= 5;

   //---------------------- Wires and regs----------------------------
   reg [LUT_DEPTH_BITS-1:0]              lut_rd_addr;
   reg [DATA_WIDTH+2*CMP_WIDTH-1:0]      lut_rd_data;
   reg [DATA_WIDTH-1:0]                  lut_wr_data;

   reg [LUT_DEPTH_BITS-1:0]					lut_new_addr;
   
   reg [DATA_WIDTH+2*CMP_WIDTH-1:0]      lut[LUT_DEPTH-1:0];

   reg                                   lookup_latched;
   reg                                   cam_match_found;
   reg                                   cam_lookup_done;
   reg                                   rd_req_latched;

   reg                                   cam_match_encoded;
   reg                                   cam_match_found_d1;
   reg [LUT_DEPTH-1:0]                   cam_match_unencoded_addr;

   reg [LUT_DEPTH_BITS-1:0]              cam_match_encoded_addr;
   
   // Add for FW
   reg									lookup_ack_held;
   reg									lookup_hit_held;
   reg [DATA_WIDTH+2*CMP_WIDTH-1:0]		lut_rd_data_held;
   reg [LUT_DEPTH_BITS-1:0]				lookup_address_held;
   reg 									rd_ack_held;
   
   reg [DATA_WIDTH-1:0]					current_tcp_state;
   reg [LUT_DEPTH_BITS-1:0]				update_addr0;
   reg [LUT_DEPTH_BITS-1:0]				update_addr1;
//   wire [2*CMP_WIDTH-1:0]				lut_update_data0;
//   wire [2*CMP_WIDTH-1:0]				lut_update_data1;
   reg [2:0]							write_state;
	reg [CMP_WIDTH-1:0]         		lookup_cmp_data_held;
	reg									delete_entry0;
	reg									delete_entry1;
	reg [LUT_DEPTH_BITS-1:0]			delete_addr0;
	reg [LUT_DEPTH_BITS-1:0]			delete_addr1;
	
	reg									is_SYN_held;
	reg									is_ACK_held;
	reg									is_RST_held;
	reg									is_FIN_held;
	
	reg									pass_directly0;
	reg									pass_directly1;
	reg									pass_directly2;
	reg									pass_directly;
   // synthesis attribute PRIORITY_EXTRACT of cam_match_encoded_addr is force;

   /* used to track the addresses for resetting the CAM and the LUT */
   reg [LUT_DEPTH_BITS:0]                reset_count;
   reg                                   state;

   wire [REG_ADDR_WIDTH-1:0]             addr;
   wire [`UDP_REG_ADDR_WIDTH-REG_ADDR_WIDTH-1:0] tag_addr;

   wire                                  addr_good;
   wire                                  tag_hit;

   reg [`CPCI_NF2_DATA_WIDTH-1:0]        reg_file[0:NUM_REGS_USED-1];

   reg [2:0]                             reg_state;

   integer                               i;

   reg [LUT_DEPTH_BITS-1:0]              rd_addr;          // address in table to read
   reg                                   rd_req;           // request a read
   wire [DATA_WIDTH-1:0]                 rd_data;          // data found for the entry
   wire [CMP_WIDTH-1:0]                  rd_cmp_data;      // matching data for the entry
   wire [CMP_WIDTH-1:0]                  rd_cmp_dmask;     // don't cares entry
   reg                                   rd_ack;           // pulses high

   reg [LUT_DEPTH_BITS-1:0]              wr_addr;
   reg                                   wr_req;
   wire [DATA_WIDTH-1:0]                 wr_data;          // data found for the entry
   wire [CMP_WIDTH-1:0]                  wr_cmp_data;      // matching data for the entry
   wire [CMP_WIDTH-1:0]                  wr_cmp_dmask;     // don't cares for the entry
   reg                                   wr_ack;

   //------------------------- Logic --------------------------------

   assign cam_cmp_din       = lookup_cmp_data;
   assign cam_cmp_data_mask = lookup_cmp_dmask;

   assign lookup_data       = (lookup_hit & lookup_ack) ? lut_rd_data[DATA_WIDTH-1:0] : DEFAULT_DATA;

//   assign current_tcp_state = lut_rd_data_held[DATA_WIDTH-1:0];
//   assign lut_update_data0  = lut[update_addr0][DATA_WIDTH+2*CMP_WIDTH-1:DATA_WIDTH];
//   assign lut_update_data1  = lut[update_addr1][DATA_WIDTH+2*CMP_WIDTH-1:DATA_WIDTH];
   
   assign rd_data           = lut_rd_data[DATA_WIDTH-1:0];
   assign rd_cmp_data       = lut_rd_data[DATA_WIDTH+CMP_WIDTH-1:DATA_WIDTH];
   assign rd_cmp_dmask      = lut_rd_data[DATA_WIDTH+2*CMP_WIDTH-1:DATA_WIDTH+CMP_WIDTH];


   assign addr                  = reg_addr_in;                        // addresses in this module
   assign tag_addr              = reg_addr_in[`UDP_REG_ADDR_WIDTH - 1:REG_ADDR_WIDTH];

   assign addr_good             = addr < NUM_REGS_USED;   // address is used in this module
   assign tag_hit               = tag_addr == TAG;        // address is in this block

   generate
      genvar ii;
      for (ii=0; ii<NUM_DATA_WORDS_USED-1; ii=ii+1) begin:gen_wrdata
         assign wr_data[ii*`CPCI_NF2_DATA_WIDTH +: `CPCI_NF2_DATA_WIDTH] = reg_file[ii];
      end
      assign wr_data[DATA_WIDTH-1:(NUM_DATA_WORDS_USED-1)*`CPCI_NF2_DATA_WIDTH] = reg_file[NUM_DATA_WORDS_USED-1];

      for (ii=0; ii<NUM_CMP_WORDS_USED-1; ii=ii+1) begin:gen_wrcmpdmask
         assign wr_cmp_dmask[ii*`CPCI_NF2_DATA_WIDTH +: `CPCI_NF2_DATA_WIDTH] = reg_file[ii+NUM_DATA_WORDS_USED];
      end
      assign wr_cmp_dmask[CMP_WIDTH-1:(NUM_CMP_WORDS_USED-1)*`CPCI_NF2_DATA_WIDTH]
             = reg_file[NUM_CMP_WORDS_USED+NUM_DATA_WORDS_USED-1];

      for (ii=0; ii<NUM_CMP_WORDS_USED-1; ii=ii+1) begin:gen_wrcmpdata
         assign wr_cmp_data[ii*`CPCI_NF2_DATA_WIDTH +: `CPCI_NF2_DATA_WIDTH]
                = reg_file[ii+NUM_CMP_WORDS_USED+NUM_DATA_WORDS_USED];
      end
      assign wr_cmp_data[CMP_WIDTH-1:(NUM_CMP_WORDS_USED-1)*`CPCI_NF2_DATA_WIDTH]
             = reg_file[2*NUM_CMP_WORDS_USED+NUM_DATA_WORDS_USED-1];
   endgenerate

   /* Handle registers */
   always @(posedge clk) begin
      if(reset) begin
         reg_req_out        <= 0;
         reg_ack_out        <= 0;
         reg_rd_wr_L_out    <= 0;
         reg_addr_out       <= 0;
         reg_src_out        <= 0;
         reg_data_out       <= 0;

         rd_req             <= 0;
         wr_req             <= 0;
         reg_state          <= WAIT_FOR_REQUEST;

         wr_addr            <= 0;
         rd_addr            <= 0;

         for (i=0; i<NUM_REGS_USED; i=i+1) begin
            reg_file[i] <= 0;
         end
      end
      else begin
         reg_req_out     <= 1'b0;
         reg_ack_out     <= 1'b0;
         rd_req          <= 0;
         wr_req          <= 0;
         case (reg_state)
            WAIT_FOR_REQUEST: begin
               /* check if we should respond to this address */
               if(addr_good && tag_hit && reg_req_in) begin

                  /* check if this is a write to the read addr register
                   * or the write addr register. */
                  if (!reg_rd_wr_L_in && addr == READ_ADDR) begin
                     /* we need to pull data from the cam/lut */
                     rd_addr                 <= reg_data_in;
                     rd_req                  <= 1;
                     reg_state               <= WAIT_FOR_READ_ACK;
                     reg_file[READ_ADDR]     <= reg_data_in;

                     reg_req_out             <= 1'b0;
                     reg_ack_out             <= 1'b0;
                     reg_rd_wr_L_out         <= reg_rd_wr_L_in;
                     reg_addr_out            <= reg_addr_in;
                     reg_src_out             <= reg_src_in;
                     reg_data_out            <= reg_data_in;
                  end // if (!reg_rd_wr_L_in && addr == READ_ADDR)

                  else if (!reg_rd_wr_L_in && addr == WRITE_ADDR) begin
                     /* we need to write data to the cam/lut */
                     wr_addr            <= reg_data_in;
                     wr_req             <= 1;
                     reg_state          <= WAIT_FOR_WRITE_ACK;

                     reg_req_out        <= 1'b0;
                     reg_ack_out        <= 1'b0;
                     reg_rd_wr_L_out    <= reg_rd_wr_L_in;
                     reg_addr_out       <= reg_addr_in;
                     reg_src_out        <= reg_src_in;
                     reg_data_out       <= reg_data_in;
                  end // if (!reg_rd_wr_L_in && addr == WRITE_ADDR)

                  else begin
                     /* not a write to a special address */
                     reg_req_out        <= reg_req_in;
                     reg_ack_out        <= 1'b1;
                     reg_rd_wr_L_out    <= reg_rd_wr_L_in;
                     reg_addr_out       <= reg_addr_in;
                     reg_src_out        <= reg_src_in;
                     /* if read */
                     if(reg_rd_wr_L_in) begin
                        reg_data_out       <= reg_file[addr];
                     end
                     /* if write */
                     else begin
                        reg_data_out       <= reg_data_in;
                        reg_file[addr]     <= reg_data_in;
                     end
                  end // else: !if(!reg_rd_wr_L_in && addr == WRITE_ADDR)

               end // if (addr_good && tag_hit && reg_req_in)

               /* otherwise just forward anything that comes over */
               else begin
                  reg_req_out        <= reg_req_in;
                  reg_ack_out        <= reg_ack_in;
                  reg_rd_wr_L_out    <= reg_rd_wr_L_in;
                  reg_addr_out       <= reg_addr_in;
                  reg_src_out        <= reg_src_in;
                  reg_data_out       <= reg_data_in;
               end // else: !if(addr_good && tag_hit && reg_req_in)

            end // case: WAIT_FOR_REQUEST

            WAIT_FOR_READ_ACK: begin
               if(rd_ack) begin
                  reg_req_out    <= 1'b1;
                  reg_ack_out    <= 1'b1;
                  reg_state      <= WAIT_FOR_REQUEST;

                  /* put the info in the registers */
                  for (i=0; i<NUM_DATA_WORDS_USED-1; i=i+1) begin
                     reg_file[i] <= rd_data[i*`CPCI_NF2_DATA_WIDTH +: `CPCI_NF2_DATA_WIDTH];
                  end
                  reg_file[NUM_DATA_WORDS_USED-1] <= {{(DATA_WIDTH % `CPCI_NF2_DATA_WIDTH){1'b0}},
                                                      rd_data[DATA_WIDTH-1:(NUM_DATA_WORDS_USED-1)*`CPCI_NF2_DATA_WIDTH]};

                  for (i=0; i<NUM_CMP_WORDS_USED-1; i=i+1) begin
                     reg_file[i+NUM_DATA_WORDS_USED] <= rd_cmp_dmask[i*`CPCI_NF2_DATA_WIDTH +: `CPCI_NF2_DATA_WIDTH];
                  end
                  reg_file[NUM_CMP_WORDS_USED+NUM_DATA_WORDS_USED-1]
                    <= {{(CMP_WIDTH % `CPCI_NF2_DATA_WIDTH){1'b0}},
                        rd_cmp_dmask[CMP_WIDTH-1:(NUM_CMP_WORDS_USED-1)*`CPCI_NF2_DATA_WIDTH]};

                  for (i=0; i<NUM_CMP_WORDS_USED-1; i=i+1) begin
                     reg_file[i+NUM_CMP_WORDS_USED+NUM_DATA_WORDS_USED]
                       <= rd_cmp_data[i*`CPCI_NF2_DATA_WIDTH +: `CPCI_NF2_DATA_WIDTH];
                  end
                  reg_file[2*NUM_CMP_WORDS_USED+NUM_DATA_WORDS_USED-1]
                    <= {{(CMP_WIDTH % `CPCI_NF2_DATA_WIDTH){1'b0}},
                        rd_cmp_data[CMP_WIDTH-1:(NUM_CMP_WORDS_USED-1)*`CPCI_NF2_DATA_WIDTH]};
               end // if (rd_ack)
               else begin
                  rd_req <= 1;
               end // else: !if(rd_ack)
            end

            WAIT_FOR_WRITE_ACK: begin
               if(wr_ack) begin
                  reg_req_out    <= 1'b1;
                  reg_ack_out    <= 1'b1;
                  reg_state      <= WAIT_FOR_REQUEST;
               end
               else begin
                  wr_req <= 1;
               end
            end
         endcase // case(reg_state)
      end // else: !if(reset)
   end // always @ (posedge clk)

   /* encode the match address */
   always @(*) begin
      cam_match_encoded_addr = LUT_DEPTH[LUT_DEPTH_BITS-1:0] - 1'b1;
      for (i = LUT_DEPTH-2; i >= 0; i = i-1) begin
         if (cam_match_unencoded_addr[i]) begin
            cam_match_encoded_addr = i[LUT_DEPTH_BITS-1:0];
         end
      end
   end

   generate 
      genvar n;
      for(n = 0; n < LUT_DEPTH; n = n + 1) begin: lut_to_linear
         assign lut_linear[n*2*CMP_WIDTH +:2*CMP_WIDTH] = lut[n][DATA_WIDTH +: 2*CMP_WIDTH];
      end
   endgenerate

   //Add for FW
	always @(*) begin
		update_addr0 = {lookup_address_held[LUT_DEPTH_BITS-1:1],1'b0};
		update_addr1 = {lookup_address_held[LUT_DEPTH_BITS-1:1],1'b1};
	end

	always @(posedge clk) begin
		if(lookup_data_vld) begin
			is_SYN_held <= is_SYN;
			is_ACK_held <= is_ACK;
			is_FIN_held <= is_FIN;
			is_RST_held <= is_RST;
		end
	end
   
   always @(posedge clk) begin

      if(reset || table_flush) begin
         lookup_latched              <= 0;
         cam_match_found             <= 0;
         cam_lookup_done             <= 0;
         rd_req_latched              <= 0;
		 lookup_ack_held			 <= 0;
         lookup_ack                  <= 0;
         lookup_hit                  <= 0;
         cam_we                      <= 0;
         cam_wr_addr                 <= 0;
         cam_din                     <= 0;
         cam_data_mask               <= 0;
         wr_ack                      <= 0;
         rd_ack                      <= 0;
         state                       <= RESET;
         lookup_address              <= 0;
         reset_count                 <= 0;
         cam_match_unencoded_addr    <= 0;
         cam_match_encoded           <= 0;
         cam_match_found_d1          <= 0;
		 lut_new_addr				<= 0;
		 write_state				<= WRITE0;
      end // if (reset)
      else begin

         // defaults
         lookup_latched     <= 0;
         cam_match_found    <= 0;
         cam_lookup_done    <= 0;
         rd_req_latched     <= 0;
         lookup_ack         <= 0;
         lookup_hit         <= 0;
         cam_we             <= 0;
         cam_din            <= 0;
         cam_data_mask      <= 0;
         wr_ack             <= 0;
         rd_ack             <= 0;
		 lookup_ack_held    <= 0;
		 lookup_hit_held    <= 0;
		 rd_ack_held		<= 0;
		 state_lookup_result<= 0;
		 pass_directly0		<= 0;
		 pass_directly1		<= 0;
		 pass_directly2		<= 0;
		 pass_directly		<= 0;
		 
         if (state == RESET && !cam_busy) begin
            if(reset_count == LUT_DEPTH) begin
               state  <= READY;
               cam_we <= 1'b0;
            end
            else begin
               reset_count      <= reset_count + 1'b1;
               cam_we           <= 1'b1;
               cam_wr_addr      <= reset_count[LUT_DEPTH_BITS-1:0];
               cam_din          <= RESET_CMP_DATA;
               cam_data_mask    <= RESET_CMP_DMASK;
               lut_wr_data      <= RESET_DATA;
            end
         end

         else if (state == READY) begin
            /* first pipeline stage -- do CAM lookup */
            lookup_latched                <= lookup_req & lookup_data_vld;
            pass_directly0				      <= lookup_data_vld & !lookup_req &!add_state_entry;

            /* second pipeline stage -- CAM result/LUT input*/
            cam_match_found               <= lookup_latched & cam_match;
            cam_lookup_done               <= lookup_latched;
            cam_match_unencoded_addr      <= cam_match_addr;
            pass_directly1				      <= pass_directly0;

            /* third pipeline stage -- encode the CAM output */
            cam_match_encoded             <= cam_lookup_done;
            cam_match_found_d1            <= cam_match_found;
            lut_rd_addr                   <= (!cam_match_found && rd_req) ? rd_addr : cam_match_encoded_addr;
            rd_req_latched                <= (!cam_match_found && rd_req);
            pass_directly2				      <= pass_directly1;

            /* fourth pipeline stage -- read LUT */
            lookup_ack_held               <= cam_match_encoded;
            lookup_hit_held               <= cam_match_found_d1;
            lut_rd_data_held              <= lut[lut_rd_addr];
            current_tcp_state			      <= (cam_match_found_d1) ? lut[lut_rd_addr][DATA_WIDTH-1:0] : 8'h00;
            lookup_address_held           <= lut_rd_addr;
            rd_ack_held                   <= rd_req_latched;
            pass_directly				      <= pass_directly2;
			
			//Add for FW
			/* fifth pipeline stage -- update TCP state */
			lookup_ack					<= lookup_ack_held || pass_directly;
			lookup_hit					<= lookup_hit_held;
			lut_rd_data					<= lut_rd_data_held;
			lookup_address				<= lookup_address_held;
			rd_ack						<= rd_ack_held;
			
			if(pass_directly)begin
				state_lookup_result		<= 1;
			end
			
			if(lookup_ack_held & lookup_hit_held) begin
				if(is_RST_held)begin
					lut[update_addr0] <= {lut[update_addr0][DATA_WIDTH+2*CMP_WIDTH-1:DATA_WIDTH],CLOSED};
					lut[update_addr1] <= {lut[update_addr1][DATA_WIDTH+2*CMP_WIDTH-1:DATA_WIDTH],CLOSED};
					state_lookup_result <= 1;
				end
				else begin //if(!iS_RST)
					case (current_tcp_state)
						CLOSED:begin
							if(is_SYN_held & !is_ACK_held & !is_FIN_held)begin
								lut[update_addr0] <= {lut[update_addr0][DATA_WIDTH+2*CMP_WIDTH-1:DATA_WIDTH],SYN};
								lut[update_addr1] <= {lut[update_addr1][DATA_WIDTH+2*CMP_WIDTH-1:DATA_WIDTH],SYN};
								state_lookup_result <= 1;
							end
						end
						SYN:begin
							if(is_SYN_held & is_ACK_held & !is_FIN_held) begin
								lut[update_addr0] <= {lut[update_addr0][DATA_WIDTH+2*CMP_WIDTH-1:DATA_WIDTH],SYN_ACK};
								lut[update_addr1] <= {lut[update_addr1][DATA_WIDTH+2*CMP_WIDTH-1:DATA_WIDTH],SYN_ACK};
								state_lookup_result <= 1;
							end
						end
						SYN_ACK:begin
							if(is_ACK_held & !is_SYN_held & !is_FIN_held)begin
								lut[update_addr0] <= {lut[update_addr0][DATA_WIDTH+2*CMP_WIDTH-1:DATA_WIDTH],ESTABLISHED};
								lut[update_addr1] <= {lut[update_addr1][DATA_WIDTH+2*CMP_WIDTH-1:DATA_WIDTH],ESTABLISHED};
								state_lookup_result <= 1;
							end
						end
						ESTABLISHED:begin
							if(!is_SYN_held & !is_ACK_held)begin
								state_lookup_result <= 1;
								if(is_FIN_held)begin
									lut[update_addr0] <= {lut[update_addr0][DATA_WIDTH+2*CMP_WIDTH-1:DATA_WIDTH],FIN_WAIT1};
									lut[update_addr1] <= {lut[update_addr1][DATA_WIDTH+2*CMP_WIDTH-1:DATA_WIDTH],FIN_WAIT1};
								end
							end
						end
						FIN_WAIT1:begin
							if(!is_SYN_held) begin
								state_lookup_result <= 1;
								if(is_FIN_held & is_ACK_held) begin
									lut[update_addr0] <= {lut[update_addr0][DATA_WIDTH+2*CMP_WIDTH-1:DATA_WIDTH],CLOSING1};
									lut[update_addr1] <= {lut[update_addr1][DATA_WIDTH+2*CMP_WIDTH-1:DATA_WIDTH],CLOSING1};
								end
								else if(is_FIN_held & !is_ACK_held) begin
									lut[update_addr0] <= {lut[update_addr0][DATA_WIDTH+2*CMP_WIDTH-1:DATA_WIDTH],CLOSING0};
									lut[update_addr1] <= {lut[update_addr1][DATA_WIDTH+2*CMP_WIDTH-1:DATA_WIDTH],CLOSING0};
								end
								else if(!is_FIN_held & is_ACK_held) begin
									lut[update_addr0] <= {lut[update_addr0][DATA_WIDTH+2*CMP_WIDTH-1:DATA_WIDTH],FIN_WAIT2};
									lut[update_addr1] <= {lut[update_addr1][DATA_WIDTH+2*CMP_WIDTH-1:DATA_WIDTH],FIN_WAIT2};
								end
							end
						end
						FIN_WAIT2:begin
							if(!is_SYN_held & !is_ACK_held)begin
								state_lookup_result <= 1;
								if(is_FIN_held)begin
									lut[update_addr0] <= {lut[update_addr0][DATA_WIDTH+2*CMP_WIDTH-1:DATA_WIDTH],CLOSING1};
									lut[update_addr1] <= {lut[update_addr1][DATA_WIDTH+2*CMP_WIDTH-1:DATA_WIDTH],CLOSING1};
								end
							end
						end
						CLOSING0:begin
							if(is_ACK_held & !is_SYN_held & !is_FIN_held)begin
								lut[update_addr0] <= {lut[update_addr0][DATA_WIDTH+2*CMP_WIDTH-1:DATA_WIDTH],CLOSING1};
								lut[update_addr1] <= {lut[update_addr1][DATA_WIDTH+2*CMP_WIDTH-1:DATA_WIDTH],CLOSING1};
								state_lookup_result <= 1;
							end
						end
						CLOSING1:begin
							if(is_ACK_held & !is_FIN_held & !is_SYN_held)begin
								state_lookup_result <= 1;
								delete_entry0		<= 1;
								delete_addr0		<= update_addr0;
								delete_addr1		<= update_addr1;
							end
						end
						default:begin
							state_lookup_result <= 0;
						end
					endcase
				end // if(is_RST)
			end //if(rd_ack_held)
			if(delete_entry0)begin
				cam_we				<= 1'b1;
				cam_wr_addr			<= delete_addr0;
				cam_din          	<= RESET_CMP_DATA;
				cam_data_mask    	<= RESET_CMP_DMASK;
				lut_wr_data      	<= RESET_DATA;
				delete_entry0		<= 0;
				delete_entry1		<= 1;
			end
			else if(delete_entry1)begin
				cam_we				<= 1'b1;
				cam_wr_addr			<= delete_addr1;
				cam_din          	<= RESET_CMP_DATA;
				cam_data_mask    	<= RESET_CMP_DMASK;
				lut_wr_data      	<= RESET_DATA;
				delete_entry1		<= 0;
			end
			
            /* Handle writes */
			if((add_state_entry & !cam_busy & is_SYN & !is_ACK & !is_RST & !is_FIN ) || write_state != WRITE0)begin
				case(write_state)
					WRITE0:begin
						lookup_cmp_data_held <= lookup_cmp_data;
						write_state		<= WRITE1;
					end
					WRITE1:begin
						write_state		<= WRITE2;
					end
					WRITE2:begin
						write_state		<= WRITE3;
					end
					WRITE3:begin
						if(!cam_match_found_d1)begin
							cam_we			<= 1;
							cam_wr_addr		<= lut_new_addr;
							cam_din			<= lookup_cmp_data_held;
							cam_data_mask	<= {CMP_WIDTH{1'b0}};
							lut_wr_data		<= SYN;
							lut_new_addr	<= lut_new_addr + 1'b1;
						end
						write_state		<= WRITE4;
					end
					default:begin
						if(!lookup_hit_held)begin
							cam_we			<= 1;
							cam_wr_addr		<= lut_new_addr;
							cam_din			<= {lookup_cmp_data_held[71:40],lookup_cmp_data_held[103:72],lookup_cmp_data_held[39:32],
												lookup_cmp_data_held[15:0],lookup_cmp_data_held[31:16]};
							cam_data_mask	<= {CMP_WIDTH{1'b0}};
							lut_wr_data		<= SYN;
							lut_new_addr	<= lut_new_addr + 1'b1;
							state_lookup_result <= 1;
						end
						write_state		<= WRITE0;
						lookup_ack		<= 1;
					end
				endcase
			end
            else if(wr_req & !cam_busy & !lookup_latched & !cam_match_found & !cam_match_found_d1 & !lookup_hit_held) begin
               cam_we           <= 1;
               cam_wr_addr      <= wr_addr;
               cam_din          <= wr_cmp_data ;
               cam_data_mask    <= wr_cmp_dmask;
               wr_ack           <= 1;
               lut_wr_data      <= wr_data;
            end // else: !if(wr_req & !cam_busy & !lookup_latched & !cam_match_found & !cam_match_found_d1)
         end // else: !if(state == RESET)

      end // else: !if(reset)

      // separate this out to allow implementation as BRAM
      if(cam_we) begin
         lut[cam_wr_addr] <= {cam_data_mask, cam_din, lut_wr_data};
      end

   end // always @ (posedge clk)

endmodule // cam_lut_sm

