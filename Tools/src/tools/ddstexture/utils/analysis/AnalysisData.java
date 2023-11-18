package tools.ddstexture.utils.analysis;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;

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
	
	public static String[] NNName = new String[]{"ETC2Fast", "ETC2FastPerceptual"};
	// 4x4 by 4 bytes so 16 ints
	//https://medium.com/geekculture/introduction-to-neural-network-2f8b8221fbd3 suggests 2/3 input nodes! or less than twice
	public static ETCNeuralNetwork nns[] = {new ETCNeuralNetwork(4*4*4, 64*2, 5), new ETCNeuralNetwork(4*4*4, 64*2, 5)};
 
	
	public static enum NNBlockStyle {
		ETC2Fast, ETC2FastPerceptual
	};

	public static enum NNMode {
		TRAIN, PREDICT
	};
	
	public static NNMode nnMode = NNMode.TRAIN;
	
	public static int[] nnTrainedCount = {0,0};
	public static int[] nnPredictSuccess = {0,0};
	public static int[] nnPredictFailed = {0,0};
	
//	public static int[] nnPredictionIdx = {0,0};
//	public static double[][][] nnPredictions = {new double[100000][], new double[100000][]};
	
	private static final DecimalFormat df = new DecimalFormat("0.00");

	public void blockComplete(byte[] nnInput, int best_mode, NNBlockStyle style) {
		
		double[] output = new double[5];
		output[best_mode] = 1;
		
		
		int nnIdx = IDX_FastPerceptualNN;

		if (style == NNBlockStyle.ETC2FastPerceptual) {
			nnIdx = IDX_FastPerceptualNN;
		} else if (style == NNBlockStyle.ETC2Fast) {
			nnIdx = IDX_FastNN;
		}
		
		ETCNeuralNetwork nn = nns[nnIdx];
		synchronized (nn) {						
			if(nnMode == NNMode.TRAIN) {
				//System.out.println("compressBlockETC2FastPerceptual trained");
				
				for(int i = 0; i <100;i++) {
					nn.train(nnInput, output);
				}
				
				nnTrainedCount[nnIdx]++;
			} else {
				double[][] predictionOrd = nn.predict(nnInput);
				//System.out.println("compressBlockETC2FastPerceptual was predicted to be " + predictionOrd[0][0]+ " and was  " + best_mode.ordinal());
				
/*				nnPredictions[nnIdx][nnPredictionIdx[nnIdx]]= new double[5];
				nnPredictions[nnIdx][nnPredictionIdx[nnIdx]][0]=predictionOrd[0][0]; 
				nnPredictions[nnIdx][nnPredictionIdx[nnIdx]][1]=predictionOrd[1][0]; 
				nnPredictions[nnIdx][nnPredictionIdx[nnIdx]][2]=predictionOrd[2][0]; 
				nnPredictions[nnIdx][nnPredictionIdx[nnIdx]][3]=predictionOrd[3][0]; 
				nnPredictions[nnIdx][nnPredictionIdx[nnIdx]][4]=predictionOrd[4][0]; 
				nnPredictionIdx[nnIdx]++;*/
				
				double m = 0;
				int maxidx = 0;
				for(int i = 0 ; i < predictionOrd.length; i++)
					if(predictionOrd[i][0]>m) {
						m=predictionOrd[i][0];						
						maxidx = i;
					}
				
				System.out.println(""	+ NNName[nnIdx] + " was " + best_mode + " predicted = " + maxidx + " "
									+ (best_mode == maxidx ? "SUCCESS" : "FAIL   ")

									+ " and predicted 0 " + df.format(predictionOrd[0][0])//
									+ " 1 " + df.format(predictionOrd[1][0])//
									+ " 2 " + df.format(predictionOrd[2][0])//
									+ " 3 " + df.format(predictionOrd[3][0])//
									+ " 4 " + df.format(predictionOrd[4][0]));
						
				if(best_mode == maxidx)
					nnPredictSuccess[nnIdx]++;
				else
					nnPredictFailed[nnIdx]++;
			}
		}
		
		
		
		
	}
	
	public void outputNNStats()
	{
		if (AnalysisData.nnMode == NNMode.PREDICT) {
			for (int i = 0; i < 2; i++) {
				System.out.println("" + i + " " + NNName[i]);
				System.out.println("nnTrainedCount "	+ AnalysisData.nnTrainedCount[i] + " nnPredictSuccess "
									+ AnalysisData.nnPredictSuccess[i] + " nnPredictFailed "
									+ AnalysisData.nnPredictFailed[i]);
				if(AnalysisData.nnTrainedCount[i]  > 0)
					System.out.println("Accuracy " + df.format((AnalysisData.nnPredictSuccess[i]
													/ ((double)AnalysisData.nnPredictFailed[i]
														+ (double)AnalysisData.nnPredictSuccess[i]))));
			}
		}
	}

	public static void setMode(NNMode mode) {
		AnalysisData.nnMode = mode;
		System.out.println("setMode "+mode.name());		
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
