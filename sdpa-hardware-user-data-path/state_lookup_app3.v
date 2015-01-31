`include "onet_defines.v"

module state_lookup_app3
   #(
   parameter PKT_SIZE_WIDTH = 12,                     // number of bits for pkt size
   parameter UDP_REG_SRC_WIDTH = 2,                   // identifies which module started this request

   //action table
   parameter ST_WIDTH = 64,
   parameter ST_DATA_WIDTH = 66,
   parameter ST_SIZE = 8,
   parameter ST_SIZE_BITS = log2(ST_SIZE),

   parameter OPENFLOW_ACTION_WIDTH = `OPENFLOW_ACTION_WIDTH,

   //AT?????????????
   parameter AT_TABLE_LOOKUP_REG_ADDR_WIDTH =    `STATE_TABLE_LOOKUP_REG_ADDR_WIDTH,
   parameter AT_TABLE_LOOKUP_BLOCK_ADDR =       `STATE_TABLE_LOOKUP_3_BLOCK_ADDR
   )
   (   
   // ---- input from state_processor
   input [ST_WIDTH-1:0]                               match_field,
   input                                              match_field_vld,
   input [OPENFLOW_ACTION_WIDTH-1:0]                  action_in,
   input [`OPENFLOW_ENTRY_SRC_PORT_WIDTH-1:0]         flow_entry_src_port,
   output reg [`OPENFLOW_ENTRY_SRC_PORT_WIDTH-1:0]    flow_entry_src_port_out,

   // --- output to state_processor
   output reg                                      app_done,
   output reg [OPENFLOW_ACTION_WIDTH-1:0]          action_out,
   //output reg [`AT_NEXT_APP_WIDTH-1:0]          next_app,
   // --- Interface to registers
   input                                           reg_req_in,
   input                                           reg_ack_in,
   input                                           reg_rd_wr_L_in,
   input  [`UDP_REG_ADDR_WIDTH-1:0]                reg_addr_in,
   input  [`CPCI_NF2_DATA_WIDTH-1:0]               reg_data_in,
   input  [UDP_REG_SRC_WIDTH-1:0]                  reg_src_in,

   output                                          reg_req_out,
   output                                          reg_ack_out,
   output                                          reg_rd_wr_L_out,
   output     [`UDP_REG_ADDR_WIDTH-1:0]            reg_addr_out,
   output     [`CPCI_NF2_DATA_WIDTH-1:0]           reg_data_out,
   output     [UDP_REG_SRC_WIDTH-1:0]              reg_src_out,

   // --- Interface to Watchdog Timer
   input                                           table_flush,

   // --- Misc
   input                                           reset,
   input                                           clk
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

   localparam SIMULATION = 0
                     // synthesis translate_off
                     || 1
                     // synthesis translate_on
                     ;

//---------------------- Wires and regs----------------------------
   // --- lookup
   wire                                                        lookup_ack;
   wire                                                        lookup_hit;
   wire [ST_DATA_WIDTH-1 : 0]                                  lookup_data;
   wire [ST_SIZE_BITS-1 : 0]                                   lookup_address;

   // --- cam
   wire                                                        cam_busy;
   wire                                                        cam_match;
   wire [ST_SIZE-1:0]                                          cam_match_addr;

   wire [ST_WIDTH-1:0]                                         cam_cmp_din, cam_cmp_data_mask;
   wire [ST_WIDTH-1:0]                                         cam_din, cam_data_mask;
   wire                                                        cam_we;
   wire [ST_SIZE_BITS-1:0]                                     cam_wr_addr;
   wire [2 * ST_SIZE * ST_WIDTH - 1 :0]                        lut_linear;
   
   // --- logic
   wire                                                     at_is_wr_src;
   wire                                                     at_is_wr_dst;
   //wire [`AT_NEXT_APP_WIDTH-1 : 0]                          next_app_cam;
   wire [`AT_SRC_WIDTH-1 : 0]                               at_new_src;
   wire [`AT_DST_WIDTH-1 : 0]                               at_new_dst;
   wire [15:0]                                              action_flag;

   reg [`OPENFLOW_ACTION_WIDTH-1:0]                         action_held;
   reg [`OPENFLOW_ENTRY_SRC_PORT_WIDTH-1:0]                 flow_entry_src_port_held;
/*
   wire [ST_SIZE_BITS-1:0]                                  wildcard_address;
   wire [ST_SIZE_BITS-1:0]                                  dout_wildcard_address;

   reg [`OPENFLOW_WILDCARD_TABLE_SIZE-1:0]                    wildcard_hit_address_decoded;
   wire [`OPENFLOW_WILDCARD_TABLE_SIZE*PKT_SIZE_WIDTH - 1:0]  wildcard_hit_address_decoded_expanded;
   wire [`OPENFLOW_WILDCARD_TABLE_SIZE*PKT_SIZE_WIDTH - 1:0]  wildcard_entry_hit_byte_size;
   wire [`OPENFLOW_WILDCARD_TABLE_SIZE*32 - 1:0]              wildcard_entry_last_seen_timestamps;

   wire [PKT_SIZE_WIDTH-1:0]                                 dout_pkt_size;

   reg [PKT_SIZE_WIDTH-1:0]                                  wildcard_entry_hit_byte_size_word [`OPENFLOW_WILDCARD_TABLE_SIZE-1:0];
   reg [31:0]                                                wildcard_entry_last_seen_timestamps_words[`OPENFLOW_WILDCARD_TABLE_SIZE-1:0];
*/
   integer                                                   i;
   
   //wire    match_field_vld;
   //------------------------- Modules -------------------------------
   assign wildcard_match_rdy = 1;
   unencoded_cam_lut_sm
   #(   
      .CMP_WIDTH (ST_WIDTH),
      .DATA_WIDTH (ST_DATA_WIDTH),
      .LUT_DEPTH  (ST_SIZE),
      .TAG (AT_TABLE_LOOKUP_BLOCK_ADDR),
      .REG_ADDR_WIDTH (AT_TABLE_LOOKUP_REG_ADDR_WIDTH)
   )cam_lut_sm_1
      (// --- Interface for lookups
      .lookup_req          (match_field_vld),
      .lookup_cmp_data     (match_field),
      .lookup_cmp_dmask    ({ST_WIDTH{1'b0}}),
      .lookup_ack          (lookup_ack),
      .lookup_hit          (lookup_hit),
      .lookup_data         (lookup_data),
      .lookup_address      (lookup_address),

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
      .clk                 (clk)
      );

   tcam_parallel_matcher 
   #(
      .CMP_WIDTH (ST_WIDTH),
      .DEPTH (ST_SIZE),
      .DEPTH_BITS (ST_SIZE_BITS),
      .ENCODE (0)
   ) cam_1
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


//-------------------------- Logic --------------------------------
   
   //parse lookup data
   assign at_new_src    = lookup_data[`AT_SRC_POS + `AT_DST_WIDTH-1 : `AT_SRC_POS];
   assign at_new_dst    = lookup_data[`AT_DST_POS + `AT_DST_WIDTH-1 : `AT_DST_POS];
   //assign next_app_cam  = lookup_data[`AT_NEXT_APP_POS + `AT_NEXT_APP_WIDTH -1: `AT_NEXT_APP_POS];
   assign at_is_wr_src  = lookup_data[`AT_MOD_SRC_POS];
   assign at_is_wr_dst  = lookup_data[`AT_MOD_DST_POS];
   assign action_flag   = (at_is_wr_src ? 16'h0040:16'h0000) | (at_is_wr_dst? 16'h0080:16'h0000);

   always @(posedge clk) begin
      if (reset) begin
         // reset
         action_out              <= 0;
         //next_app                <= 0;
         app_done                <= 0;
      end
      else  begin
         action_out              <= action_in;
         app_done                <= 0;
         if (match_field_vld) begin
            action_held          <= action_in;
            flow_entry_src_port_held   <= flow_entry_src_port;
         end
         if(lookup_ack) begin
            //next_app                <= next_app_cam;
            app_done                <= 1;
            flow_entry_src_port_out <= flow_entry_src_port_held;
            if(lookup_hit) begin
               if(at_is_wr_src || at_is_wr_dst) begin
                  action_out[`OPENFLOW_SET_NW_SRC_POS +: `OPENFLOW_SET_NW_SRC_WIDTH]    <= at_is_wr_src ? at_new_src : action_held[`OPENFLOW_SET_NW_SRC_POS +: `OPENFLOW_SET_NW_SRC_WIDTH];
                  action_out[`OPENFLOW_SET_NW_DST_POS +: `OPENFLOW_SET_NW_DST_WIDTH]    <= at_is_wr_dst ? at_new_dst : action_held[`OPENFLOW_SET_NW_DST_POS +: `OPENFLOW_SET_NW_DST_WIDTH];
                  action_out[`OPENFLOW_NF2_ACTION_FLAG_POS +: `OPENFLOW_NF2_ACTION_FLAG_WIDTH] 
                     <= action_held[`OPENFLOW_NF2_ACTION_FLAG_POS +: `OPENFLOW_NF2_ACTION_FLAG_WIDTH] | action_flag;
               end
            end
         end
      end
   end
endmodule
