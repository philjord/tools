package tools.ddstexture.utils.analysis.etcpack;

import java.nio.ByteBuffer;
/**
 * Note wildly single threaded, you must use a shynchronized call around this guy 
 */

// https://towardsdatascience.com/understanding-and-implementing-neural-networks-in-java-from-scratch-61421bb6352c
public class ETCNeuralNetwork {
	
	public static enum OutputStyle{SIGMOID, SOFTMAX};
	
	OutputStyle outputStyle = OutputStyle.SIGMOID; // default, current derivative of softmax not working
	
	


	Matrix	weights_ih, weights_ho, bias_h, bias_o;
	double	l_rate	= 0.01;

	public ETCNeuralNetwork(int in, int hidden, int out) {
		weights_ih = new Matrix(hidden, in);
		weights_ho = new Matrix(out, hidden);

		bias_h = new Matrix(hidden, 1);
		bias_o = new Matrix(out, 1);

	}
	
	public OutputStyle getOutputStyle() {
		return outputStyle;
	}

	public void setOutputStyle(OutputStyle outputStyle) {
		this.outputStyle = outputStyle;
	}
	
	public int size() {
		return weights_ih.size() + weights_ho.size() + bias_h.size() + bias_o.size();
	}
	
	public ByteBuffer toByteBuffer() {
		ByteBuffer bb = ByteBuffer.allocate(size());
		weights_ih.toByteBuffer(bb);
		weights_ho.toByteBuffer(bb);
		bias_h.toByteBuffer(bb);
		bias_o.toByteBuffer(bb);
		bb.flip();
		return bb;
	}
	
	public void fromByteBuffer(ByteBuffer bb){
		weights_ih.fromByteBuffer(bb);
		weights_ho.fromByteBuffer(bb);
		bias_h.fromByteBuffer(bb);
		bias_o.fromByteBuffer(bb);
	}

	public double[][] predict(byte[] X) {
		Matrix input = Matrix.fromArray(X);
		Matrix hidden = Matrix.multiply(weights_ih, input);
		hidden.add(bias_h);
		//hidden is always sigmoid
		hidden.sigmoid();


		Matrix output = Matrix.multiply(weights_ho, hidden);
		output.add(bias_o);
		if(outputStyle == OutputStyle.SOFTMAX)
			output.softmax();
		else
			output.sigmoid();

		return output.toArray();
	}
	 

	public void train(byte[] X, double[] Y) {
		Matrix input = Matrix.fromArray(X);
		Matrix hidden = Matrix.multiply(weights_ih, input);
		hidden.add(bias_h);
		//hidden is always sigmoid
		hidden.sigmoid();

		Matrix output = Matrix.multiply(weights_ho, hidden);
		output.add(bias_o);
		if(outputStyle == OutputStyle.SOFTMAX)
			output.softmax();
		else
			output.sigmoid();

		Matrix target = Matrix.fromArray(Y);

		Matrix error = Matrix.subtract(target, output);
		Matrix gradient;		
		
		if(outputStyle == OutputStyle.SOFTMAX)
			gradient = output.dsoftmax();
		else
			gradient = output.dsigmoid();
		
		gradient.multiply(error);
		gradient.multiply(l_rate);

		Matrix hidden_T = Matrix.transpose(hidden);
		Matrix who_delta = Matrix.multiply(gradient, hidden_T);

		weights_ho.add(who_delta);
		bias_o.add(gradient);

		Matrix who_T = Matrix.transpose(weights_ho);
		Matrix hidden_errors = Matrix.multiply(who_T, error);
		
		//hidden is always sigmoid
		Matrix h_gradient = hidden.dsigmoid();
		h_gradient.multiply(hidden_errors);
		h_gradient.multiply(l_rate);

		Matrix i_T = Matrix.transpose(input);
		Matrix wih_delta = Matrix.multiply(h_gradient, i_T);

		weights_ih.add(wih_delta);
		bias_h.add(h_gradient);

	}
		

	public static class Matrix {
		double[][]	data;
		int			rows, cols;
		
		public int size() {
			return 4+4+(rows*cols*8);
		}
		public void toByteBuffer(ByteBuffer bb) {
			bb.putInt(rows);
			bb.putInt(cols);
			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < cols; j++) {
					bb.putDouble(data [i] [j]);
				}
			}
		}
		
		public void fromByteBuffer(ByteBuffer bb){
			rows = bb.getInt();
			cols = bb.getInt();
			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < cols; j++) {
					data [i] [j] = bb.getDouble();
				}
			}
		}

		public Matrix(int rows, int cols) {
			data = new double[rows][cols];
			this.rows = rows;
			this.cols = cols;
			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < cols; j++) {
					data [i] [j] = Math.random() * 2 - 1;
				}
			}
		}

		public void add(double scaler) {
			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < cols; j++) {
					this.data [i] [j] += scaler;
				}

			}
		}

		public void add(Matrix m) {
			if (cols != m.cols || rows != m.rows) {
				new Throwable("Shape Mismatch").printStackTrace();
				return;
			}

			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < cols; j++) {
					this.data [i] [j] += m.data [i] [j];
				}
			}
		}

		public static Matrix subtract(Matrix a, Matrix b) {
			Matrix temp = new Matrix(a.rows, a.cols);
			for (int i = 0; i < a.rows; i++) {
				for (int j = 0; j < a.cols; j++) {
					temp.data [i] [j] = a.data [i] [j] - b.data [i] [j];
				}
			}
			return temp;
		}

		public static Matrix transpose(Matrix a) {
			Matrix temp = new Matrix(a.cols, a.rows);
			for (int i = 0; i < a.rows; i++) {
				for (int j = 0; j < a.cols; j++) {
					temp.data [j] [i] = a.data [i] [j];
				}
			}
			return temp;
		}

		public static Matrix multiply(Matrix a, Matrix b) {
			Matrix temp = new Matrix(a.rows, b.cols);
			for (int i = 0; i < temp.rows; i++) {
				for (int j = 0; j < temp.cols; j++) {
					double sum = 0;
					for (int k = 0; k < a.cols; k++) {
						sum += a.data [i] [k] * b.data [k] [j];
					}
					temp.data [i] [j] = sum;
				}
			}
			return temp;
		}

		public void multiply(Matrix a) {
			for (int i = 0; i < a.rows; i++) {
				for (int j = 0; j < a.cols; j++) {
					this.data [i] [j] *= a.data [i] [j];
				}
			}

		}

		public void multiply(double a) {
			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < cols; j++) {
					this.data [i] [j] *= a;
				}
			}

		}

		public void sigmoid() {
			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < cols; j++)
					this.data [i] [j] = 1 / (1 + Math.exp(-this.data [i] [j]));
			}

		}

		public Matrix dsigmoid() {
			Matrix temp = new Matrix(rows, cols);
			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < cols; j++)
					temp.data [i] [j] = this.data [i] [j] * (1 - this.data [i] [j]);
			}
			return temp;
		}
		
		public void softmax() {
			softmax(this.data);
		}
		
		//https://stackoverflow.com/questions/57631507/how-can-i-take-the-derivative-of-the-softmax-output-in-back-prop
		//https://stats.stackexchange.com/questions/453539/softmax-derivative-implementation
		public static void softmax(double[][] input) {
			
			//softmax for output is a 5rowx1col matrix so the softmax goes down the single column
			//a row iterator as it were
			
			double sum = 0;
			for (int n = 0; n < input.length; n++) {
				input [n][0] = Math.exp(input [n][0]);
				sum += input [n][0];
			}
			
			for (int n = 0; n < input.length; n++) {
				input [n][0] = input [n][0] / sum;
			}
		}

		
		

		//https://levelup.gitconnected.com/killer-combo-softmax-and-cross-entropy-5907442f60ba
		//https://stats.stackexchange.com/questions/453539/softmax-derivative-implementation
		public Matrix dsoftmax() {	
			// I'm told d of softmax is simply answer-target, which my error matrix from training has so 
			// just 1's will cause the gradient matrix to be simply error
			Matrix temp = new Matrix(rows, cols);
			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < cols; j++)
					temp.data [i] [j] = 1;
			}
			return temp;
		}
		
		//unused currently
		public static double[][] dsoftmaxArray(double[][] input) {	
			int n = input.length;
			double[][] out = new double[n][1];
			
			double[][] p = new double[n][1];
			System.arraycopy(input, 0, p, 0, n);
			softmax(p);
			
		 
			for (int i = 0; i < n; i++) {				 
				for (int j = 0; j < n; j++) {
					out[i][j] = p[i][0]*(i==j?1:0 - p[j][0]); 
				}
			}
			
			return out;
		}
		
		 
		
		public static Matrix fromArray(double[] x) {
			Matrix temp = new Matrix(x.length, 1);
			for (int i = 0; i < x.length; i++)
				temp.data [i] [0] = x [i];
			return temp;

		}
		
		public static Matrix fromArray(byte[] x) {
			Matrix temp = new Matrix(x.length, 1);
			for (int i = 0; i < x.length; i++)
				temp.data [i] [0] = Byte.toUnsignedInt(x [i]);
			return temp;

		}

		public double[][] toArray() {
			double[][] temp = new double[rows][cols];

			for (int i = 0; i < rows; i++) {
				//System.arraycopy(data, 0, temp, 0, cols);
				for (int j = 0; j < cols; j++) {
					temp[i] [j] =  data [i] [j];
				}
			}
			return temp;
		}
		
		
	}
	
	
}