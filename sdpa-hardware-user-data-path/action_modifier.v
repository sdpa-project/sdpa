`include "onet_defines.v"

module action_modifier
	#(parameter FLAG_WIDTH = 16,
		parameter MODIFY_FORWARD_BITMASK = 16'h0001,
		parameter MODIFY_ACTION_FLAG = 16'h0002,
		parameter MODIFY_VLAN_VID = 16'h0004,
		parameter MODIFY_VLAN_PCP = 16'h0008,
		parameter MODIFY_DL_SRC = 16'h0010,
		parameter MODIFY_DL_DST = 16'h0020,
		parameter MODIFY_NW_SRC = 16'h0040,
		parameter MODIFY_NW_DST = 16'h0080,
		parameter MODIFY_NW_TOS = 16'h0100,
		parameter MODIFY_TP_SRC = 16'h0200,
		parameter MODIFY_TP_DST = 16'h0400
		)
	(
		input 	clk,
		input		reset,
		input 	enable,

		input		[`OPENFLOW_ACTION_WIDTH-1:0] 	action_in,
		input 	[`OPENFLOW_ACTION_WIDTH-1:0] 	param,
		input 	[FLAG_WIDTH-1:0] 					flag,

		output 	reg [`OPENFLOW_ACTION_WIDTH-1:0] 	action_out,
		output 	reg	modification_done
		);

	always @(posedge clk) begin
		if (reset) begin
			action_out 	<=	0;			
			modification_done <= 0;
		end
		else begin
			if (~enable) begin
				action_out 	<= 	action_out;
				modification_done 	<= 	0;
			end
			else begin

				modification_done 	<= 	1;

				/* modify forward bit */
				if (flag & MODIFY_FORWARD_BITMASK) begin
			  		action_out[`OPENFLOW_FORWARD_BITMASK_POS +: `OPENFLOW_FORWARD_BITMASK_WIDTH] 
			  			<= param[`OPENFLOW_FORWARD_BITMASK_POS +: `OPENFLOW_FORWARD_BITMASK_WIDTH];
				end
				else begin
			  		action_out[`OPENFLOW_FORWARD_BITMASK_POS +: `OPENFLOW_FORWARD_BITMASK_WIDTH] 
			  			<= action_in[`OPENFLOW_FORWARD_BITMASK_POS +: `OPENFLOW_FORWARD_BITMASK_WIDTH];
				end

				/* modify action flag */
				if (flag & MODIFY_ACTION_FLAG) begin
			  		action_out[`OPENFLOW_NF2_ACTION_FLAG_POS +: `OPENFLOW_NF2_ACTION_FLAG_WIDTH] 
			  			<= (action_in[`OPENFLOW_NF2_ACTION_FLAG_POS +: `OPENFLOW_NF2_ACTION_FLAG_WIDTH] | param[`OPENFLOW_NF2_ACTION_FLAG_POS +: `OPENFLOW_NF2_ACTION_FLAG_WIDTH]);
				end
				else begin
			  		action_out[`OPENFLOW_NF2_ACTION_FLAG_POS +: `OPENFLOW_NF2_ACTION_FLAG_WIDTH] 
			  			<= action_in[`OPENFLOW_NF2_ACTION_FLAG_POS +: `OPENFLOW_NF2_ACTION_FLAG_WIDTH];
				end

				/* modify vlan id */
				if (flag & MODIFY_VLAN_VID) begin
			  		action_out[`OPENFLOW_SET_VLAN_VID_POS +: `OPENFLOW_SET_VLAN_VID_WIDTH] 
			  			<= param[`OPENFLOW_SET_VLAN_VID_POS +: `OPENFLOW_SET_VLAN_VID_WIDTH];
				end
				else begin
			  		action_out[`OPENFLOW_SET_VLAN_VID_POS +: `OPENFLOW_SET_VLAN_VID_WIDTH] 
			  			<= action_in[`OPENFLOW_SET_VLAN_VID_POS +: `OPENFLOW_SET_VLAN_VID_WIDTH];
				end

				/* modify vlan pcp */
				if (flag & MODIFY_VLAN_PCP) begin
			  		action_out[`OPENFLOW_SET_VLAN_PCP_POS +: `OPENFLOW_SET_VLAN_PCP_WIDTH] 
			  			<= param[`OPENFLOW_SET_VLAN_PCP_POS +: `OPENFLOW_SET_VLAN_PCP_WIDTH];
				end
				else begin
			  		action_out[`OPENFLOW_SET_VLAN_PCP_POS +: `OPENFLOW_SET_VLAN_PCP_WIDTH]
			  			<= action_in[`OPENFLOW_SET_VLAN_PCP_POS +: `OPENFLOW_SET_VLAN_PCP_WIDTH];
				end

				/* modify SET_DL_SRC*/
				if (flag & MODIFY_DL_SRC) begin
					action_out[`OPENFLOW_SET_DL_SRC_POS +: `OPENFLOW_SET_DL_SRC_WIDTH]
					<= param[`OPENFLOW_SET_DL_SRC_POS +: `OPENFLOW_SET_DL_SRC_WIDTH];
				end
				else begin
					action_out[`OPENFLOW_SET_DL_SRC_POS +: `OPENFLOW_SET_DL_SRC_WIDTH]
					<= action_in[`OPENFLOW_SET_DL_SRC_POS +: `OPENFLOW_SET_DL_SRC_WIDTH];
				end

				/* modify SET_DL_DST*/
				if (flag & MODIFY_DL_DST) begin
					action_out[`OPENFLOW_SET_DL_DST_POS +: `OPENFLOW_SET_DL_DST_WIDTH]
					<= param[`OPENFLOW_SET_DL_DST_POS +: `OPENFLOW_SET_DL_DST_WIDTH];
				end
				else begin
					action_out[`OPENFLOW_SET_DL_DST_POS +: `OPENFLOW_SET_DL_DST_WIDTH]
					<= action_in[`OPENFLOW_SET_DL_DST_POS +: `OPENFLOW_SET_DL_DST_WIDTH];
				end

				/* modify SET_NW_SRC*/
				if (flag & MODIFY_NW_SRC) begin
					action_out[`OPENFLOW_SET_NW_SRC_POS +: `OPENFLOW_SET_NW_SRC_WIDTH]
					<= param[`OPENFLOW_SET_NW_SRC_POS +: `OPENFLOW_SET_NW_SRC_WIDTH];
				end
				else begin
					action_out[`OPENFLOW_SET_NW_SRC_POS +: `OPENFLOW_SET_NW_SRC_WIDTH]
					<= action_in[`OPENFLOW_SET_NW_SRC_POS +: `OPENFLOW_SET_NW_SRC_WIDTH];
				end

				/* modify SET_NW_DST*/
				if (flag & MODIFY_NW_DST) begin
					action_out[`OPENFLOW_SET_NW_DST_POS +: `OPENFLOW_SET_NW_DST_WIDTH]
					<= param[`OPENFLOW_SET_NW_DST_POS +: `OPENFLOW_SET_NW_DST_WIDTH];
				end
				else begin
					action_out[`OPENFLOW_SET_NW_DST_POS +: `OPENFLOW_SET_NW_DST_WIDTH]
					<= action_in[`OPENFLOW_SET_NW_DST_POS +: `OPENFLOW_SET_NW_DST_WIDTH];
				end

				/* modify SET_NW_TOS*/
				if (flag & MODIFY_NW_TOS) begin
					action_out[`OPENFLOW_SET_NW_TOS_POS +: `OPENFLOW_SET_NW_TOS_WIDTH]
					<= param[`OPENFLOW_SET_NW_TOS_POS +: `OPENFLOW_SET_NW_TOS_WIDTH];
				end
				else begin
					action_out[`OPENFLOW_SET_NW_TOS_POS +: `OPENFLOW_SET_NW_TOS_WIDTH]
					<= action_in[`OPENFLOW_SET_NW_TOS_POS +: `OPENFLOW_SET_NW_TOS_WIDTH];
				end

				/* modify SET_TP_SRC*/
				if (flag & MODIFY_TP_SRC) begin
					action_out[`OPENFLOW_SET_TP_SRC_POS +: `OPENFLOW_SET_TP_SRC_WIDTH]
					<= param[`OPENFLOW_SET_TP_SRC_POS +: `OPENFLOW_SET_TP_SRC_WIDTH];
				end
				else begin
					action_out[`OPENFLOW_SET_TP_SRC_POS +: `OPENFLOW_SET_TP_SRC_WIDTH]
					<= action_in[`OPENFLOW_SET_TP_SRC_POS +: `OPENFLOW_SET_TP_SRC_WIDTH];
				end

				/* modify SET_TP_DST*/
				if (flag & MODIFY_TP_DST) begin
					action_out[`OPENFLOW_SET_TP_DST_POS +: `OPENFLOW_SET_TP_DST_WIDTH]
					<= param[`OPENFLOW_SET_TP_DST_POS +: `OPENFLOW_SET_TP_DST_WIDTH];
				end
				else begin
					action_out[`OPENFLOW_SET_TP_DST_POS +: `OPENFLOW_SET_TP_DST_WIDTH]
					<= action_in[`OPENFLOW_SET_TP_DST_POS +: `OPENFLOW_SET_TP_DST_WIDTH];
				end		
		
			end
		end
	end

endmodule