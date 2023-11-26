package tools.ddstexture.utils.analysis;

import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

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
	public static ETCNeuralNetwork nns[] = {new ETCNeuralNetwork(4*4*4, 64, 1), new ETCNeuralNetwork(4*4*4, 64, 1)};
 
	
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
	

	// 2 nn x 5 modes by 100 rounded opiton as an int count
	public static int[][][] nnPredictions = new int[2][5][100];
	public static int[][] targetTrainedCount = new int[2][5];
	
	private static final DecimalFormat df = new DecimalFormat("0.00");
	
	// this suggest that image classification whilst compressed is to poor to work
	//https://sigport.org/sites/default/files/docs/DCC.pdf
	
	
	// in fact quick etc is my best option
	//https://nahjaeho.github.io/papers/SA20/QUICKETC2_SA20.pdf
	//https://nahjaeho.github.io/papers/SA20/QUICKETC2_SA20_slides.pdf
	//https://github.com/wolfpld/etcpak/commit/da85020e690890f4356d42ab5802e4f957f220fd#diff-8e19e1bd6d1d238c9a2a23d0199e8fe248c88023f64439b1b7444085fa6c1c24
	
	QuantizedLookUp qlu = new QuantizedLookUp();
	

	/**
	 * 
	 * @param nnInput 64 byte being 4x4 ints
	 * @param best_mode 1,2,3,4 only
	 * @param style TRAIN or PREDICT
	 */
	public void blockComplete(byte[] nnInput, int best_mode, NNBlockStyle style, NNMode mode) {
 		
		
		double[] targetVal = new double[] {best_mode/4.0};

		
		double[] input = new double[nnInput.length];		
		for(int i = 0; i <nnInput.length;i++) {
			input[i] = ((nnInput[i]+128.0)/256.0);
		}
		
		int nnIdx = IDX_FastPerceptualNN;

		if (style == NNBlockStyle.ETC2FastPerceptual) {
			nnIdx = IDX_FastPerceptualNN;
		} else if (style == NNBlockStyle.ETC2Fast) {
			nnIdx = IDX_FastNN;
		}
		
		ETCNeuralNetwork nn = nns[nnIdx];
		synchronized (nn) {						
			if(mode == NNMode.TRAIN) {
				//System.out.println("compressBlockETC2FastPerceptual trained");
				
//				nn.train(input, targetVal);
				
				qlu.train(nnInput, best_mode);
				
				nnTrainedCount[nnIdx]++;
			} else {
				double[][] predictionOrd = nn.predict(input);
				double prediction = predictionOrd[0][0];
				
				boolean success = (Math.abs(targetVal[0] - prediction) < 0.1);
				
				
				
				byte qlup = qlu.predict(nnInput);
				
				
				System.out.println(""	+ NNName[nnIdx] + " was " + best_mode 
						+ " targetVal[0] = " + df.format(targetVal[0]) + " "
									+ (success ? "SUCCESS" : "FAIL   ")
									+ " and predicted 0 " + df.format(prediction) 
									+ " qlup " + qlup);
						
				if(success)
					nnPredictSuccess[nnIdx]++;
				else
					nnPredictFailed[nnIdx]++;
				
								
				nnPredictions[nnIdx][best_mode][(int)(prediction*100)]++;
				targetTrainedCount[nnIdx][best_mode]++;
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
			
			// if no windows showing them set them up
			if (statsArea == null) {
				JFrame nnModelWin = new JFrame("NN Model");
				nnModelWin.getContentPane().setLayout(new GridLayout(1, 1));

				JTextArea modelArea = new JTextArea();
				nnModelWin.getContentPane().add(new JScrollPane(modelArea));

				nnModelWin.setSize(400, 200);
				nnModelWin.setVisible(true);

				JFrame statsWin = new JFrame("Stats");
				statsWin.getContentPane().setLayout(new GridLayout(1, 1));

				statsArea = new JTextArea();
				statsWin.getContentPane().add(new JScrollPane(statsArea));
				statsWin.setSize(600, 1200);
				statsWin.setVisible(true);

			}
			
			// replace the output text
			String statsOutputString = "Stats:";
			statsOutputString += "Model\tValue\tTarget0\tTarget1\tTarget2\tTarget3\tTarget4\n";
			for (int nn = 0; nn < 2; nn++) {
				statsOutputString += "\t\t" + targetTrainedCount[nn][0] + "\t"
							+ targetTrainedCount[nn][1] + "\t" + targetTrainedCount[nn][2] + "\t"
							+ targetTrainedCount[nn][3] + "\t" + targetTrainedCount[nn][4] + "\n";
				for (int i = 0; i < 100; i++) {
					statsOutputString += "" + nn + "\t" + i + "\t" + nnPredictions[nn][0][i] + "\t"
											+ nnPredictions[nn][1][i] + "\t" + nnPredictions[nn][2][i] + "\t"
											+ nnPredictions[nn][3][i] + "\t" + nnPredictions[nn][4][i] + "\n";
				}
			}
						
			statsArea.setText(statsOutputString);			
		}
	}
	private static JTextArea statsArea;
	
	

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
