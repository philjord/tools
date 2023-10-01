package tools.ddstexture.utils.analysis;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;

import tools.ddstexture.utils.analysis.AnalysisData.NNMode;
import tools.ddstexture.utils.analysis.dds.DDSDecompressor.Color24;
import tools.ddstexture.utils.analysis.etcpack.ETCNeuralNetwork;
 

public class AnalysisData {
	public String imageName;
	public int imageWidth;
	public int imageHeight;
	
	//A or A1 data indicator?
	
	public int blocksWide;
	public int blocksHigh;
	
	public BlockData[][] inblockData;
	
	public BlockData[][] outblockData;
	

	
	
	
	@Override
	public String toString() {
		String str =  "Name:"+imageName+","+imageWidth+","+imageHeight+","+blocksWide+","+blocksHigh;
		return str;
	}
	
	public String toStringFully() {
		String str =  "Name:"+imageName+","+imageWidth+","+imageHeight+","+blocksWide+","+blocksHigh;
		
		for(int r = 0;r < blocksHigh;r++) {
			str = str + "\n"+r+" ";
			for(int c = 0 ; c < blocksWide ;c++)
				str = str + c + " " + inblockData[r][c] + ", ";
		}
		
		return str;
	}
	
	public abstract static class BlockData {
 
	}
	
	public static class DXT1 extends BlockData {
		public short c0;
		public short c1;
		public int colorIndexMask;
		public boolean hasAlphaBit;
		public Color24[] lookupTable;
		
		public String toString() {
			return "DXT1";
		}
	}
	
	public static class DXT3 extends BlockData {

		public long alphaData;
		public short minColor;
		public short maxColor;
		public int colorIndexMask;
		public Color24[] lookupTable;
		public String toString() {
			return "DXT3 " +alphaData +" "+minColor +" "+maxColor +" "+colorIndexMask;
		}
	}
	
	public static class DXT5 extends BlockData {

		public int alpha0;
		public int alpha1;
		public long alphaBits;
		public short minColor;
		public short maxColor;
		public int colorIndexMask;
		public Color24[] lookupTable;
		public String toString() {
			return "DXT5";
		}
	}
	
	public static class ETC2 extends BlockData {

		public char best_char;
		public int word1;
		public int word2;
		
		public String toString() {
			return "ETC2 " +best_char +" "+word1 +" "+word2;
		}
		
	}
	
	
	
	
	
	public static int IDX_FastNN = 0;
	public static int IDX_FastPerceptualNN = 1;
	
	public static enum NNName{ETC2Fast, ETC2FastPerceptual};
	// 4x4 by 4 bytes so 16 ints
	public static ETCNeuralNetwork nns[] = {new ETCNeuralNetwork(16, 5000, 5), new ETCNeuralNetwork(16, 5000, 5)};
 
	
	public static enum NNBlockStyle{ETC2Fast, ETC2FastPerceptual};
	
	public static enum NNMode{TRAIN,PREDICT};
	
	public static NNMode nnMode = NNMode.TRAIN;
	
	public static int[] nnTrainedCount = {0,0};
	public static int[] nnPredictSuccess = {0,0};
	public static int[] nnPredictFailed = {0,0};
	
	public static int[] nnPredictionIdx = {0,0};
	public static double[][][] nnPredictions = {new double[100000][], new double[100000][]};
	
	private static final DecimalFormat df = new DecimalFormat("0.00");

	public void blockComplete(double[] nnInput, int best_mode, NNBlockStyle style) {
		
		double[] output = new double[5];
		output[best_mode] = 1;
		
		if(style == NNBlockStyle.ETC2FastPerceptual) {
			synchronized (nns[IDX_FastPerceptualNN]) {						
				if(nnMode == NNMode.TRAIN) {
					//System.out.println("compressBlockETC2FastPerceptual trained");
					
					nns[IDX_FastPerceptualNN].train(nnInput, output);
					
					nnTrainedCount[IDX_FastPerceptualNN]++;
				} else {
					double[][] predictionOrd = nns[IDX_FastPerceptualNN].predict(nnInput);
					//System.out.println("compressBlockETC2FastPerceptual was predicted to be " + predictionOrd[0][0]+ " and was  " + best_mode.ordinal());
					
					nnPredictions[IDX_FastPerceptualNN][nnPredictionIdx[IDX_FastPerceptualNN]]= new double[5];
					nnPredictions[IDX_FastPerceptualNN][nnPredictionIdx[IDX_FastPerceptualNN]][0]=predictionOrd[0][0]; 
					nnPredictions[IDX_FastPerceptualNN][nnPredictionIdx[IDX_FastPerceptualNN]][0]=predictionOrd[1][0]; 
					nnPredictions[IDX_FastPerceptualNN][nnPredictionIdx[IDX_FastPerceptualNN]][0]=predictionOrd[2][0]; 
					nnPredictions[IDX_FastPerceptualNN][nnPredictionIdx[IDX_FastPerceptualNN]][0]=predictionOrd[3][0]; 
					nnPredictions[IDX_FastPerceptualNN][nnPredictionIdx[IDX_FastPerceptualNN]][0]=predictionOrd[4][0]; 
					nnPredictionIdx[IDX_FastPerceptualNN]++;
					
					System.out.println("compressBlockETC2FastPerceptual was "+best_mode+" and predicted 0 " 
							+ df.format(predictionOrd[0][0])+ " 1 " +df.format(predictionOrd[1][0])
									+  " 2 " +df.format(predictionOrd[2][0])
											 + " 3 " +df.format(predictionOrd[3][0])
													   + " 4 " +df.format(predictionOrd[4][0]) );
							
					if(predictionOrd[best_mode][0] > 0.9)
						nnPredictSuccess[IDX_FastPerceptualNN]++;
					else
						nnPredictFailed[IDX_FastPerceptualNN]++;
				}
			}
		}else {
			synchronized (nns[IDX_FastNN]) {						
				if(nnMode == NNMode.TRAIN) {
					//System.out.println("compressBlockETC2FastNN trained");
					
					nns[IDX_FastNN].train(nnInput, output);
					
					nnTrainedCount[IDX_FastNN]++;
				} else {
					double[][] predictionOrd = nns[IDX_FastNN].predict(nnInput);					
					System.out.println("compressBlockETC2Fast was "+best_mode+" and predicted 0 " 
							+ df.format(predictionOrd[0][0])+ " 1 " +df.format(predictionOrd[1][0])
							+  " 2 " +df.format(predictionOrd[2][0])
									 + " 3 " +df.format(predictionOrd[3][0])
											   + " 4 " +df.format(predictionOrd[4][0]) );
					
					if(predictionOrd[best_mode][0] > 0.9)
						nnPredictSuccess[IDX_FastNN]++;
					else
						nnPredictFailed[IDX_FastNN]++;
				}
			}
		}
		
	}
	
	public void outputNNStats()
	{
		if(AnalysisData.nnMode == NNMode.PREDICT) {
			for(int i = 0; i < 2; i++) {
				System.out.println("i " + NNName.values()[i]);
			System.out.println("nnTrainedCount " + AnalysisData.nnTrainedCount[i]
					+" nnPredictSuccess " + AnalysisData.nnPredictSuccess[i]
					+" nnPredictFailed " + AnalysisData.nnPredictFailed[i]);
			System.out.println("Accuracy " + (AnalysisData.nnPredictSuccess[i] /((double)AnalysisData.nnPredictFailed[i]+
												(double)AnalysisData.nnPredictSuccess[i])));	
			}
		}
	}

	public static void changeToMode(NNMode mode) {
		AnalysisData.nnMode = mode;
		System.out.println("changing to "+mode.name()+" now");		
	}
	
	

	public static void save() {
		String filename = "D:\\temp\\nndata.txt";
		File file = new File(filename);
		file.getParentFile().mkdirs();
		try {
			//yeeha! lets write this bad boy out to a file!!!
			RandomAccessFile raf = new RandomAccessFile(filename, "rw");			
			FileChannel fc = raf.getChannel();
			
			ByteBuffer header = ByteBuffer.allocate(3*4);
			header.putInt(-1);// version?
			header.putInt(IDX_FastNN); 
			header.putInt(nns[IDX_FastNN].size()); 
			header.flip();
			fc.write(header);			
			fc.write(nns[IDX_FastNN].toByteBuffer());
			
			header = ByteBuffer.allocate(3*4);			
			header.putInt(-1);// version?
			header.putInt(IDX_FastPerceptualNN); 
			header.putInt(nns[IDX_FastPerceptualNN].size()); 
			header.flip();
			fc.write(header);			
			fc.write(nns[IDX_FastPerceptualNN].toByteBuffer());
			
			fc.close();
			raf.close();
			System.out.println("saved");
		} catch (IOException e1) {	 
			e1.printStackTrace();
		}
		
	}
	public static void load() {
		String filename = "D:\\temp\\nndata.txt";
		File file = new File(filename);
		 
		try {
			//yeeha! lets write this bad boy out to a file!!!
			RandomAccessFile raf = new RandomAccessFile(filename, "rw");			
			FileChannel fc = raf.getChannel();
			
			ByteBuffer header = ByteBuffer.allocate(3*4);
			fc.read(header);
			header.flip();
			header.getInt();// version?
			IDX_FastNN = header.getInt(); 
			int size = header.getInt(); 

			ByteBuffer bb = ByteBuffer.allocate(size); 
			fc.read(bb);
			bb.flip();
			nns[IDX_FastNN].fromByteBuffer(bb);			
			
			header = ByteBuffer.allocate(3*4);
			fc.read(header);
			header.flip();
			header.getInt();// version?
			IDX_FastPerceptualNN = header.getInt(); 
			size = header.getInt(); 

			bb = ByteBuffer.allocate(size); 
			fc.read(bb);
			bb.flip();
			nns[IDX_FastPerceptualNN].fromByteBuffer(bb);
			
			fc.close();
			raf.close();
			System.out.println("loaded");
		} catch (IOException e1) {	 
			e1.printStackTrace();
		}
		
	}
	
	
	

}
