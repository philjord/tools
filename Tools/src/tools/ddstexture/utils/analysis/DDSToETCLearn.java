package tools.ddstexture.utils.analysis;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.filechooser.FileNameExtensionFilter;

import tools.ddstexture.utils.analysis.AnalysisData.NNMode;
import tools.ddstexture.utils.analysis.dds.DDSDecompressor;
import tools.ddstexture.utils.analysis.dds.NioImageBuffer;
import tools.ddstexture.utils.analysis.etcpack.ETCNeuralNetwork;
import tools.ddstexture.utils.analysis.etcpack.ETCPackNN;
import tools.ddstexture.utils.analysis.etcpack.ETCPackNN.FORMAT;
import tools.ddstexture.utils.analysis.ktx.KTXFormatException;
import tools.swing.DetailsFileChooser;

/**
 * dds image loading tester, note this use the decompressor to buffered image util system not the jogl compressed call
 * @author philip
 *
 */
public class DDSToETCLearn {
	private static Preferences prefs;

	public static void main(String[] args) {
		prefs = Preferences.userNodeForPackage(DDSToETCLearn.class);

		DetailsFileChooser dfc = new DetailsFileChooser(prefs.get("DDSToTexture", ""),
				new DetailsFileChooser.Listener() {
					@Override
					public void directorySelected(File dir) {
						prefs.put("DDSToTexture", dir.getAbsolutePath());
						System.out.println("Selected dir: " + dir);
						processDir(dir);
					}

					@Override
					public void fileSelected(File file) {
						prefs.put("DDSToTexture", file.getAbsolutePath());
						System.out.println("Selected file: " + file);
						trainImage(file, 15000);
					}
				});

		dfc.setFileFilter(new FileNameExtensionFilter("dds", "dds"));

		JFrame extraWin = new JFrame("For button clicks");
		extraWin.getContentPane().setLayout(new GridLayout(2,2));

		JButton trainButton = new JButton("train");
		trainButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				AnalysisData.setMode(AnalysisData.NNMode.TRAIN);
			}
		});
		JButton predictButton = new JButton("Predict");
		predictButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				AnalysisData.setMode(AnalysisData.NNMode.PREDICT);
			}
		});
		extraWin.getContentPane().add(trainButton);
		extraWin.getContentPane().add(predictButton);
		
		JButton saveButton = new JButton("Save");
		saveButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				AnalysisData.save();
			}

			
		});
		JButton loadButton = new JButton("Load");
		loadButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				AnalysisData.load();
			}
		});
		extraWin.getContentPane().add(saveButton);
		extraWin.getContentPane().add(loadButton);
		extraWin.invalidate();
		extraWin.setSize(600, 100);
		extraWin.setLocation(600, 0);

		extraWin.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				final int keyCode = e.getKeyCode();
				if (keyCode == KeyEvent.VK_T) {
					AnalysisData.setMode(AnalysisData.NNMode.TRAIN);
				} else if (keyCode == KeyEvent.VK_P) {
					AnalysisData.setMode(AnalysisData.NNMode.PREDICT);
				}
			}
		});

		extraWin.setVisible(true);
		
//		playAbout();
	}
	
	
	public static void playAbout() {
		
		//https://stats.stackexchange.com/questions/181/how-to-choose-the-number-of-hidden-layers-and-nodes-in-a-feedforward-neural-netw
		
		// how about a little play play 
		ETCNeuralNetwork nn = new ETCNeuralNetwork(4*4*4, (int)(64*0.75), 1);
		DecimalFormat df = new DecimalFormat("0.00");
		
		
		double[][] nnInputs = new double[10][4*4*4];
		 
		//nnInput1[0] = 10;
		
		
		for(int i = 0; i< 10;i++) {
			for(int j = 0; j< 10;j++) {
				int idx = (int)(Math.random()*64);
				
				double val = Math.random()*0.5;
				
				//low idx go high colors
				if(i< 5)
					val +=  0.5 ;
				
				nnInputs[i][idx] = val;			 
			}
		}
		
		double[][] outs = new double[][] {new double[] {1.0},
			new double[] {0.90},
			new double[] {0.80},
			new double[] {0.7},
			new double[] {0.6},
			new double[] {0.1},
			new double[] {0.2},
			new double[] {0.3},
			new double[] {0.4},
			new double[] {0.0} };
		
		 
		
		// 10 training = no learning, 1000 = some learning 10000 = good learning
		for(int x = 0; x <10000;x++) {
			for(int i = 0; i< 10;i++) {
				nn.train(nnInputs[i], outs[i]);			 
			}
		}
				
		for(int i = 0; i< 10;i++) {
			double[][] predictionOrd = nn.predict(nnInputs[i]);
			System.out.println( "predict on trained ="+outs[i][0] + " " + df.format(predictionOrd[0][0])); 			
		}
		
		// now crazy it up!
		nnInputs = new double[10][4*4*4];
		for(int i = 0; i< 10;i++) {
			for(int j = 0; j< 10;j++) {
				int idx = (int)(Math.random()*64);
				double val = Math.random()*0.5;
				
				//low idx go high colors
				if(i< 5)
					val +=  0.5 ;
				nnInputs[i][idx] = val;			 
			}
		}
		for(int i = 0; i< 10;i++) {
			double[][] predictionOrd = nn.predict(nnInputs[i]);
			System.out.println( "predict on rando ="+ i  + " " + df.format(predictionOrd[0][0])); 			
		}
	}
	
	
	
	private static void processDir(File dir) {
		System.out.println("Processing directory " + dir);
		File[] fs = dir.listFiles();
		for (int i = 0; i < fs.length; i++) {
			try {
				if (fs [i].isFile() && fs [i].getName().endsWith(".dds")) {
					System.out.println("\tFile: " + fs [i]);
					trainImage(fs [i], 5000);

					//pause between each show to give it a chance to work
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
					}
				} else if (fs [i].isDirectory()) {
					processDir(fs [i]);
				}

			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public static void trainImage(File file, long stayTime) {
		String filename = file.getAbsolutePath();
		try {
			trainImage(filename, new FileInputStream(file), stayTime);

			//TODO: this should be fine, but shows blank?
			//showImageInShape(filename, new FileInputStream(file));
		} catch (IOException e) {
			System.out
					.println("" + DDSToETCLearn.class + " had a  IO problem with " + filename + " : " + e.getMessage());
		}

	}

	public static void trainImage(String filename, InputStream inputStream, final long stayTime) {

		Thread worker = new Thread() {

			@Override
			public void run() {

				DDSImage ddsImage;
				try {
					ddsImage = DDSImage.read(toByteBuffer(inputStream));
					ddsImage.debugPrint();
				} catch (IOException e) {
					System.out.println(
							"" + DDSToETCLearn.class + " had a  IO problem with " + filename + " : " + e.getMessage());
					return;
				}

				DDSDecompressor decomp = new DDSDecompressor(ddsImage, 0, filename);
				NioImageBuffer decompressedImage = decomp.convertImageNio();

				AnalysisData inAD = decomp.analysisData;

				Buffer b = decompressedImage.getDataBuffer();
				if (b instanceof ByteBuffer) {
					//ok so now find the RGB or RGBA byte buffers
					ByteBuffer bb = (ByteBuffer)decompressedImage.getDataBuffer();
					byte[] img = null;
					byte[] imgalpha = null;
					if (decompressedImage.getImageType() == NioImageBuffer.ImageType.TYPE_3BYTE_RGB) {
						// just put the RGB data straight into the img byte array 
						img = new byte[bb.capacity()];
						bb.get(img, 0, bb.capacity());
					} else if (decompressedImage.getImageType() == NioImageBuffer.ImageType.TYPE_4BYTE_RGBA) {
						byte[] ddsimg = new byte[bb.capacity()];
						bb.get(ddsimg, 0, bb.capacity());
						// copy RGB 3 sets out then 1 sets of alpha 
						img = new byte[(bb.capacity() / 4) * 3];
						imgalpha = new byte[(bb.capacity() / 4)];
						for (int i = 0; i < img.length / 3; i++) {
							img [i * 3 + 0] = ddsimg [i * 4 + 0];
							img [i * 3 + 1] = ddsimg [i * 4 + 1];
							img [i * 3 + 2] = ddsimg [i * 4 + 2];
							imgalpha [i] = ddsimg [i * 4 + 3];
						}
					} else {
						System.err.println("Bad Image Type " + decompressedImage.getImageType());
						return;
					}

					//System.out.println("Debug of dds image " + filename);
					//ddsImage.debugPrint();
					int fmt = ddsImage.getPixelFormat();
					FORMAT format = FORMAT.ETC2PACKAGE_RGBA;

					if (fmt == DDSImage.D3DFMT_R8G8B8) {
						format = FORMAT.ETC2PACKAGE_RGB;
					} else if (fmt == DDSImage.D3DFMT_A8R8G8B8 || fmt == DDSImage.D3DFMT_X8R8G8B8) {
						format = FORMAT.ETC2PACKAGE_RGBA;
					} else if (fmt == DDSImage.D3DFMT_DXT1) {
						// DXT1 might have the odd punch through alpha in it, but there is no way to say if it's just RGB or RGB and some A1
						format = FORMAT.ETC2PACKAGE_sRGBA1;
					} else if (fmt == DDSImage.D3DFMT_DXT2	|| fmt == DDSImage.D3DFMT_DXT3
								|| fmt == DDSImage.D3DFMT_DXT4 || fmt == DDSImage.D3DFMT_DXT5) {
						format = FORMAT.ETC2PACKAGE_sRGBA;
					}
					ETCPackNN ep = new ETCPackNN();
					ep.nnMode = AnalysisData.nnMode;
					ByteBuffer ktxBB = null;
					try {
						// let's compress it 10 times, as traingin data
						int iters = AnalysisData.nnMode == NNMode.TRAIN ? 100 :1;
						
						for(int i = 0; i < iters; i++) {
							ktxBB = ep.compressImageToByteBuffer(img, imgalpha, ddsImage.getWidth(), ddsImage.getHeight(),
									format, false); //NOTE FALSE!!
						}
						KTXImage ktxImage = new KTXImage(ktxBB);
					} catch (KTXFormatException e) {
						System.out.println("DDS to KTX image: " + filename);
						e.printStackTrace();
					} catch (IOException e) {
						System.out.println("DDS to KTX image: " + filename);
						e.printStackTrace();
					} catch (BufferOverflowException e) {
						System.out.println("DDS to KTX image: " + filename);
						e.printStackTrace();
					}

					AnalysisData outAD = ep.analysisData;

					System.out.println("Name:"	+ inAD.imageName
										+ "," + inAD.imageWidth + "," + inAD.imageHeight + "," + inAD.blocksWide + ","
										+ inAD.blocksHigh);

					outAD.outputNNStats();

				}
			}
		};
		worker.start();

	}

	private static int BUFSIZE = 16000;

	public static ByteBuffer toByteBuffer(InputStream in) throws IOException {

		//note toByteArray trims to size
		ByteArrayOutputStream out = new ByteArrayOutputStream(BUFSIZE);
		byte[] tmp = new byte[BUFSIZE];
		while (true) {
			int r = in.read(tmp);
			if (r == -1)
				break;

			out.write(tmp, 0, r);
		}

		return ByteBuffer.wrap(out.toByteArray());

	}
}
