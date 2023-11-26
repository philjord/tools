package tools.ddstexture.utils.analysis;

public class QuantizedLookUp {

	//4x4 with 64*64*64 possible value =  1million to the power of 16 is too much
	// 4x4 with 4x4x4 =  64 ^ 16 = heaps
	// 8 option (2x2x2) ^ 16 = heaps
	byte[] data = new byte[4*4*64*64*64];
	
	/**
	 * 4x4 by 4byte ints = 4x4x4 = 64
	 */
	public void train(byte[] nnInput, int best_mode) {
		
		int i = 0;
		int idx = 0;
		for (int y = 0; y < 4; ++y) {
			for (int x = 0; x < 4; ++x) {	
				
				// quantize down from 256 colours to simpler 64 colours
				int a = (nnInput[i++]&0xff)/4;
				int r = (nnInput[i++]&0xff)/4;
				int g = (nnInput[i++]&0xff)/4; 
				int b = (nnInput[i++]&0xff)/4;							
				
				/// umm go inside outward I think? ignore a
				int idxIncr = b + (g<<6) + (r<<12) + (x<<18) + (y<<20);
				
				int z = (y<<20);
				
				
				idx += idxIncr;
			}
		}
		
		data[idx] = (byte)best_mode;

	}
	public byte predict(byte[] nnInput) {
		int idx = 0;
		for (int y = 0; y < 4; ++y) {
			for (int x = 0; x < 4; ++x) {	
				
				// quntize down from 256 colours to simpler 64 colours
				byte a = (byte)(nnInput[idx++]/4);
				byte r = (byte)(nnInput[idx++]/4); 
				byte g = (byte)(nnInput[idx++]/4); 
				byte b = (byte)(nnInput[idx++]/4);
				
							
				
				/// umm go inside outward I think?
				idx = b + (g<<6) + (r<<12) + (a<<18) + (x<<24) + (y<<2);
			}
		}
		
		return data[idx];
	}
}
