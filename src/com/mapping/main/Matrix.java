package com.mapping.main;

import java.awt.List;
import java.util.ArrayList;


public class Matrix {
	private int x, y;
	private ArrayList<double[]> numbers = new ArrayList<double[]>();
	
	public Matrix(int x, int y)
	{
		this.x = x;
		this.y = y;
	}
	
	public void initMatrix(double[] numbers) throws Exception
	{
		if(numbers.length == (x * y))
		{
			for(int z = 0; z < y; z++)
			{
				double[] q = new double[x];
				this.numbers.add(q);
			}
			int columnIndex = 0;
			int rowIndex = 0;
			for(int i = 0; i < numbers.length; i++)
			{
			//	System.out.println("Putting value numbers[" + i + "] into column: " + columnIndex + " row: " + rowIndex + " x: " + x);
			//	System.out.println("i % x = " + i%x);
				this.numbers.get(rowIndex)[columnIndex] = numbers[i];
				columnIndex++;
				if(((i+1) % x == 0 && i > 0) || (x == 1 && columnIndex == x))
				{
					columnIndex = 0;
					rowIndex++;
				}
			}
		}
		else
		{
			throw new Exception("Invalid matrix for given dimensions.");
		}
	}
	public ArrayList<double[]> getNumbers()
	{
		return numbers;
	}
	public void setNumbers(ArrayList<double[]> numbers)
	{
		if(numbers.size() == y && numbers.get(0).length == x)
		{
			this.numbers = numbers;
		}
	}
	public int getX()
	{
		return x;
	}
	public int getY()
	{
		return y;
	}
	public static Matrix multiply(Matrix A, Matrix B) throws Exception
	{
		Matrix C = new Matrix(B.getX(), A.getY());
		
		ArrayList<double[]> Avals = A.getNumbers(), Bvals = B.getNumbers();
		double[] Cvals = new double[C.getX() * C.getY()];
		if(A.getX() == B.getY())
		{
			int next = 0;
		       for (int i = 0; i < A.getY(); i++) { // aRow
		            for (int j = 0; j < B.getX(); j++) { // bColumn
		                for (int k = 0; k < A.getX(); k++) { // aColumn
		                    Cvals[next] += Avals.get(i)[k] * Bvals.get(k)[j];
		                }
		                next++;
		            }
		        }
		       C.initMatrix(Cvals);
		       return C;
		}
		else
		{
			throw new Exception("Invalid inner dimensions. Cannot multiply.");
		}
	}
	public boolean dimEqual(Matrix A)
	{
		if(this.x == A.getX() && this.y == A.getY())
		{
			return true;
		}
		else {
			return false;
		}
	}
	public static Matrix add(Matrix A, Matrix B) throws Exception
	{
		Matrix C = new Matrix(A.getX(), A.getY());
		double[] CVals = new double[A.getX() * A.getY()];
		ArrayList<double[]> Avals = A.getNumbers(), Bvals = B.getNumbers();
		if(A.dimEqual(B))
		{
			int next = 0;
			for(int i = 0; i < A.getY(); i++)
			{
				for(int j = 0; j < A.getX(); j++)
				{
					CVals[next] = Avals.get(i)[j] + Bvals.get(i)[j];
					next++;
				}
			}
			C.initMatrix(CVals);
			return C;
		}
		else
		{
			throw new Exception("Matrix must be same dimensions to add.");
		}

	}
	public static void print(Matrix A)
	{
		System.out.println("");
		for(int i = 0; i < A.getY(); i++)
		{
			String row = "[ ";
			for(int j = 0; j < A.getX(); j++)
			{
				row += "" + A.getNumbers().get(i)[j] + " ";
			}
			System.out.println(row + " ]");
		}
		System.out.println("");
	}
	public String toString()
	{
		String matrix = "";
		for(int i = 0; i < getY(); i++)
		{
			String row = "[ ";
			for(int j = 0; j < getX(); j++)
			{
				row += "" + getNumbers().get(i)[j] + " ";
			}
			matrix += row+"]\n";
		}
		return matrix;
	}
	public Matrix transpose() throws Exception
	{
		Matrix x = new Matrix(this.y, this.x);
		int index = 0;
		double[] n = new double[this.x*this.y];
		for(int i = 0; i < this.x; i++)
		{
			for(int j = 0; j < this.y; j++)
			{
				double l = this.getNumbers().get(j)[i];
				n[index] = l;
				index++;
			}
		}
		x.initMatrix(n);
		return x;
	}
	
	public Matrix gE() throws Exception
	{
		Matrix C = new Matrix(getX(), getY());
		C.setNumbers(getNumbers());
		for(int i = 0; i < C.getX(); i++)
		{
			Matrix.pivot(C, i, i);
			for(int j = i+1; j < C.getY(); j++)
			{
				Matrix.performRowOp(C, j, i, i);
			}
		}
		ArrayList<double[]> vals = getNumbers();
		double[] answers = new double[getY()];
		int columns = 0;
		double extras = 0;
		double last;
		double coefficient;
		for(int i = getY()-1; i >= 0; i--, columns++)
		{
			extras = 0;
			for(int j = getX()-2; j > getX()-2-columns; j--)
			{
				extras += vals.get(i)[j]*answers[j];
			}
			
			last = vals.get(i)[getX()-1];
			last -= extras;
			coefficient = vals.get(i)[getX()-2-columns];
			answers[i] = last/coefficient;
			if(answers[i] < 0.000001)
			{
				answers[i] = 0;
			}
		}
		Matrix D = new Matrix(1, getY());
		D.initMatrix(answers);
		return D;
	}
	public static void pivot(Matrix A, int startRow, int startCol)
	{
		ArrayList<double[]> nums = A.getNumbers();
		int highestRow = startRow;
		int switchRow = startRow;
		for(int i = startRow; i < A.getY(); i++)
		{
			if(Math.abs(nums.get(i)[startCol]) > Math.abs(nums.get(highestRow)[startCol]))
			{
				highestRow = i;
			}
		}
		if(highestRow != switchRow)
		{
			double[] temp = nums.get(switchRow);
			nums.set(switchRow, nums.get(highestRow));
			nums.set(highestRow, temp);
		}
		A.setNumbers(nums);
	}
	/*
	 * r1 = row to get zero
	 * r2 = row to add 
	 * c = column to get the zero
	 */
	public static void performRowOp(Matrix A, int r1, int r2, int c)
	{
		ArrayList<double[]> nums = A.getNumbers();
		double x = (nums.get(r2)[c]/nums.get(r1)[c]);
		for(int i = 0; i < A.getX(); i++)
		{
			double y = nums.get(r1)[i];
			double z = -1*nums.get(r2)[i];
			nums.get(r1)[i] = (x *  y) + z;
		}
		A.setNumbers(nums);
	}
	
	public static Matrix augment(Matrix A, Matrix B) throws Exception
	{
		if(A.y != B.y)
		{
			throw new Exception("Matrixes must have same amount of rows to augment");
		}
		Matrix a = new Matrix(A.x+B.x, A.y);
		double[][] n = new double[a.y][a.x];
		int row = 0;
		int column = 0;
		for(int i = 0; i < A.y; i++)
		{
			for(int j = 0; j < A.x; j++)
			{
				n[row][column] = A.getNumbers().get(i)[j];
				column++;
			}
			row++;
			column = 0;
		}
		row = 0;
		column = a.x-1;
		for(int i = 0; i < B.y; i++)
		{
			for(int j = 0; j < B.x; j++)
			{
				//System.out.println(row+" "+column);
				n[row][column] = B.getNumbers().get(i)[j];
				column++;
			}
			row++;
			column = a.x-1;
		}
		
		double[] n2 = new double[a.x*a.y];
		int index = 0;
		for(int i = 0; i < n.length; i++)
		{
			for(int j = 0; j < n[0].length; j++)
			{
				n2[index] = n[i][j];
				index++;
			}
		}
		a.initMatrix(n2);
		return a;
	}
	public static void main(String[] argv) throws Exception
	{
		Matrix A = new Matrix(2,4);
		double[] n = new double[]{-1,1,0,1,1,1,2,1};
		A.initMatrix(n);
		Matrix At = A.transpose();
		Matrix.print(At);
		Matrix b = new Matrix(1,4);
		double[] n2 = new double[]{0,1,2,1};
		b.initMatrix(n2);
		
		Matrix AtA = Matrix.multiply(At, A);
		Matrix Atb = Matrix.multiply(At, b);
		
		Matrix.print(AtA);
		System.out.println("* X^* = ");
		Matrix.print(Atb);
		
		Matrix augmented = Matrix.augment(AtA, Atb);
		Matrix.print(augmented);
		Matrix D = augmented.gE();
		Matrix.print(D);		
	}
}