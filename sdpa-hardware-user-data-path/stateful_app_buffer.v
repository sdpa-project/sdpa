module stateful_app_buffer
	#(
		parameter WIDTH =1,
		parameter LEN=1
		)
	(
		input 		[WIDTH-1:0]		din,
		output	 	[WIDTH-1:0]		dout,

		input 		clk,
		input 		reset
		);

	reg [WIDTH-1:0] 					buf_reg[LEN-1:0];

	integer 							i;

	assign dout = buf_reg[LEN-1];

	always @(posedge clk or posedge reset) begin
		if (reset) begin
			for (i = 0; i < LEN; i = i + 1) begin
				buf_reg[i] 	<= 	0;
			end	
		end
		else begin
			buf_reg[0] 		<= 	din;
			for (i = 0; i < LEN-1; i = i + 1) begin
				buf_reg[i+1] 	<= 	buf_reg[i];
			end
		end
	end

endmodule