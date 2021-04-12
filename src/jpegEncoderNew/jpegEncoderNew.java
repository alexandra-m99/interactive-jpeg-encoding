package jpegEncoderNew;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.io.File;
import java.io.IOException;
import java.lang.*;
import javax.imageio.plugins.jpeg.*;

import javax.imageio.ImageIO;


//this class is a representation of the individual nodes in the Huffman tree 
class treeNode
{
	short values; 
	short freq;
	
	treeNode left;
	treeNode right;
}

//this class is for comparing the nodes in the Huffman tree based on frequency
class MyComparator implements Comparator<treeNode>
{
	public int compare(treeNode x, treeNode y)
	{
		return x.freq - y.freq;
	}
}

public class jpegEncoderNew {
	
	public static int n = 8, m = 8;
    public static double pi = 3.1415926535897;
    
    //defines the 4 standard Huffman tables from javax.imageio.plugins.jpeg.JPEGHuffmanTable plugin
    //(DC Chrominance, DC Luminance, AC Chrominance, and AC Luminance)
    public static final JPEGHuffmanTable StdDCChrominance = 
    		new JPEGHuffmanTable(new short[] { 0, 3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0 },
    		                     new short[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 });
     
    public static final JPEGHuffmanTable StdDCLuminance = 
    		new JPEGHuffmanTable(new short[] { 0, 1, 5, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0 },
    		                     new short[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 });
    
    public static final JPEGHuffmanTable StdACChrominance =
	         new JPEGHuffmanTable(new short[] { 0, 2, 1, 2, 4, 4, 3, 4, 7, 5, 4, 4, 0, 1, 2, 0x77 },
	                              new short[]  { 0x00, 0x01, 0x02, 0x03, 0x11,
	                                             0x04, 0x05, 0x21, 0x31, 0x06,
	                                             0x12, 0x41, 0x51, 0x07, 0x61,
	                                             0x71, 0x13, 0x22, 0x32, 0x81,
	                                             0x08, 0x14, 0x42, 0x91, 0xa1,
	                                             0xb1, 0xc1, 0x09, 0x23, 0x33,
	                                             0x52, 0xf0, 0x15, 0x62, 0x72,
	                                             0xd1, 0x0a, 0x16, 0x24, 0x34,
	                                             0xe1, 0x25, 0xf1, 0x17, 0x18,
	                                             0x19, 0x1a, 0x26, 0x27, 0x28,
	                                             0x29, 0x2a, 0x35, 0x36, 0x37,
	                                             0x38, 0x39, 0x3a, 0x43, 0x44,
	                                             0x45, 0x46, 0x47, 0x48, 0x49,
	                                             0x4a, 0x53, 0x54, 0x55, 0x56,
	                                             0x57, 0x58, 0x59, 0x5a, 0x63,
	                                             0x64, 0x65, 0x66, 0x67, 0x68,
	                                             0x69, 0x6a, 0x73, 0x74, 0x75,
	                                             0x76, 0x77, 0x78, 0x79, 0x7a,
	                                             0x82, 0x83, 0x84, 0x85, 0x86,
	                                             0x87, 0x88, 0x89, 0x8a, 0x92,
	                                             0x93, 0x94, 0x95, 0x96, 0x97,
	                                             0x98, 0x99, 0x9a, 0xa2, 0xa3,
	                                             0xa4, 0xa5, 0xa6, 0xa7, 0xa8,
	                                             0xa9, 0xaa, 0xb2, 0xb3, 0xb4,
	                                             0xb5, 0xb6, 0xb7, 0xb8, 0xb9,
	                                             0xba, 0xc2, 0xc3, 0xc4, 0xc5,
	                                             0xc6, 0xc7, 0xc8, 0xc9, 0xca,
	                                             0xd2, 0xd3, 0xd4, 0xd5, 0xd6,
	                                             0xd7, 0xd8, 0xd9, 0xda, 0xe2,
	                                             0xe3, 0xe4, 0xe5, 0xe6, 0xe7,
	                                             0xe8, 0xe9, 0xea, 0xf2, 0xf3,
	                                             0xf4, 0xf5, 0xf6, 0xf7, 0xf8,
	                                             0xf9, 0xfa });
    
    public static final JPEGHuffmanTable StdACLuminance =
    		        new JPEGHuffmanTable(new short[] { 0, 2, 1, 3, 3, 2, 4, 3, 5, 5, 4, 4, 0, 0, 1, 0x7d },
    		                             new short[] { 0x01, 0x02, 0x03, 0x00, 0x04,
    		                                           0x11, 0x05, 0x12, 0x21, 0x31,
    		                                           0x41, 0x06, 0x13, 0x51, 0x61,
    		                                           0x07, 0x22, 0x71, 0x14, 0x32,
    		                                           0x81, 0x91, 0xa1, 0x08, 0x23,
    		                                           0x42, 0xb1, 0xc1, 0x15, 0x52,
    		                                           0xd1, 0xf0, 0x24, 0x33, 0x62,
    		                                           0x72, 0x82, 0x09, 0x0a, 0x16,
    		                                           0x17, 0x18, 0x19, 0x1a, 0x25,
    		                                           0x26, 0x27, 0x28, 0x29, 0x2a,
    		                                           0x34, 0x35, 0x36, 0x37, 0x38,
    		                                           0x39, 0x3a, 0x43, 0x44, 0x45,
    		                                           0x46, 0x47, 0x48, 0x49, 0x4a,
    		                                           0x53, 0x54, 0x55, 0x56, 0x57,
    		                                           0x58, 0x59, 0x5a, 0x63, 0x64,
    		                                           0x65, 0x66, 0x67, 0x68, 0x69,
    		                                           0x6a, 0x73, 0x74, 0x75, 0x76,
    		                                           0x77, 0x78, 0x79, 0x7a, 0x83,
    		                                           0x84, 0x85, 0x86, 0x87, 0x88,
    		                                           0x89, 0x8a, 0x92, 0x93, 0x94,
    		                                           0x95, 0x96, 0x97, 0x98, 0x99,
    		                                           0x9a, 0xa2, 0xa3, 0xa4, 0xa5,
    		                                           0xa6, 0xa7, 0xa8, 0xa9, 0xaa,
    		                                           0xb2, 0xb3, 0xb4, 0xb5, 0xb6,
    		                                           0xb7, 0xb8, 0xb9, 0xba, 0xc2,
    		                                           0xc3, 0xc4, 0xc5, 0xc6, 0xc7,
    		                                           0xc8, 0xc9, 0xca, 0xd2, 0xd3,
    		                                           0xd4, 0xd5, 0xd6, 0xd7, 0xd8,
    		                                           0xd9, 0xda, 0xe1, 0xe2, 0xe3,
    		                                           0xe4, 0xe5, 0xe6, 0xe7, 0xe8,
    		                                           0xe9, 0xea, 0xf1, 0xf2, 0xf3,
    		                                           0xf4, 0xf5, 0xf6, 0xf7, 0xf8,
    		                                           0xf9, 0xfa });
    
    
    // Function to find discrete cosine transform and print it
    static strictfp double[][] dctTransform(int matrix[][])
    {
        int u, v, x, y;
  
        // dct will store the discrete cosine transform
        double[][] dct = new double[m][n];
  
        double ci, cj, dct1, sum;
  
        for (u = 0; u < m; u++) 
        {
            for (v = 0; v < n; v++) 
            {
                // ci and cj depends on frequency as well as
                // number of row and columns of specified matrix
                if (u == 0)
                    ci = 1 / Math.sqrt(m);
                else
                    ci = Math.sqrt(2) / Math.sqrt(m);
                     
                if (v == 0)
                    cj = 1 / Math.sqrt(n);
                else
                    cj = Math.sqrt(2) / Math.sqrt(n);
  
                // sum will temporarily store the sum of 
                // cosine signals
                sum = 0;
                for (x = 0; x < m; x++) 
                {
                    for (y = 0; y < n; y++) 
                    {
                        dct1 = matrix[x][y] * 
                               Math.cos((2 * x + 1) * u * pi / (2 * m)) * 
                               Math.cos((2 * y + 1) * v * pi / (2 * n));
                        sum = sum + dct1;
                    }
                }
                dct[u][v] = ci * cj * sum;
            }
        }
  
        for (u = 0; u < m; u++) 
        {
            for (v = 0; v < n; v++) 
                System.out.printf("%f\t", dct[u][v]);
            System.out.println();
        }
        
        return dct;
    }
    
    //HUFFMAN CODING IS INCOMPLETE - THERE ARE ERRORS WITH THE ARRAY INDEXES AND WE HAVEN'T FIGURED OUT HOW TO USE THE DCT VALUES
    //this function computes the huffman coding (there are errors with this code that still need to be fixed)
    public static void huffmanCoding(short[] lengths, short[] values)
    {
    	//Not sure how to get the length of the tables since the standard Huffman tables have two arrays, one for lengths, 
    	//and one for values, so this len variable might be wrong since it cannot access the table properly
    	int len = lengths.length + values.length;
    	
    	//TESTING
    	//System.out.println(len);
    	//JPEGHuffmanTable[] huffmanTable;
    	
    	//create a priority queue 
    	PriorityQueue<treeNode> pq = new PriorityQueue<treeNode>(len, new MyComparator());
    	
    	for(int i=0; i<len; i++)
    	{
    		//create a new node for the tree
    		treeNode node = new treeNode();
    		
    		//node.values = huffmanTable[i].getValues();
    		
    		//set the node value and frequency variables to equal the function parameter arrays
    		node.values = values[i];
    		node.freq = lengths[i];
    		
    		//add the new node to the tree
    		node.left = null;
    		node.right = null;
    		pq.add(node);
    	}
    	
    	//set the root node
    	treeNode root = null;
    	
    	//loop while the size of the queue is greater than 1 (this is where the two values with the 
    	//lowest frequency will be found) - repeats until there is only one node, since that will become the root
    	while(pq.size() > 1)
    	{
    		//find the first node and then remove it from queue
    		treeNode x = pq.peek();
    		pq.poll();
    		
    		//find the second node and then remove it from queue
    		treeNode y = pq.peek();
    		pq.poll();
    		
    		//create a new tree node which will be those two nodes merged
    		treeNode t = new treeNode();
    		
    		//find the sum of the two nodes' frequencies and add it to the tree 
    		t.freq = (short) (x.freq + y.freq);
    		t.left = x;
    		t.right = y;
    		root = t;
    		pq.add(t);
    		
    	}
    	
    }
	
	public static void main(String[] args) throws IOException 
	{
			
		//reading the image file
		BufferedImage bi=ImageIO.read(new File("painting.jpg"));
		
		//initializing arrays to hold rgb values
		int[] rArray = new int[64];
		int[] gArray = new int[64];
		int[] bArray = new int[64];
	
		//initializing and declaring values for the quantization table for Luminance
		double[] qTableY = new double[64];
		qTableY[0] = 16;
	    qTableY[1] = 11;
	    qTableY[2] = 10;
	    qTableY[3] = 16;
	    qTableY[4] = 24;
	    qTableY[5] = 40;
	    qTableY[6] = 51;
	    qTableY[7] = 61;
	    qTableY[8] = 12;
	    qTableY[9] = 12;
	    qTableY[10] = 14;
	    qTableY[11] = 19;
	    qTableY[12] = 26;
	    qTableY[13] = 58;
	    qTableY[14] = 60;
	    qTableY[15] = 55;
	    qTableY[16] = 14;
	    qTableY[17] = 13;
	    qTableY[18] = 16;
	    qTableY[19] = 24;
	    qTableY[20] = 40;
	    qTableY[21] = 57;
	    qTableY[22] = 69;
	    qTableY[23] = 56;
	    qTableY[24] = 14;
	    qTableY[25] = 17;
	    qTableY[26] = 22;
	    qTableY[27] = 29;
	    qTableY[28] = 51;
	    qTableY[29] = 87;
	    qTableY[30] = 80;
	    qTableY[31] = 62;
	    qTableY[32] = 18;
	    qTableY[33] = 22;
	    qTableY[34] = 37;
	    qTableY[35] = 56;
	    qTableY[36] = 68;
	    qTableY[37] = 109;
	    qTableY[38] = 103;
	    qTableY[39] = 77;
	    qTableY[40] = 24;
	    qTableY[41] = 35;
	    qTableY[42] = 55;
	    qTableY[43] = 64;
	    qTableY[44] = 81;
	    qTableY[45] = 104;
	    qTableY[46] = 113;
	    qTableY[47] = 92;
	    qTableY[48] = 49;
	    qTableY[49] = 64;
	    qTableY[50] = 78;
	    qTableY[51] = 87;
	    qTableY[52] = 103;
	    qTableY[53] = 121;
	    qTableY[54] = 120;
	    qTableY[55] = 101;
	    qTableY[56] = 72;
	    qTableY[57] = 92;
	    qTableY[58] = 95;
	    qTableY[59] = 98;
	    qTableY[60] = 112;
	    qTableY[61] = 100;
	    qTableY[62] = 103;
	    qTableY[63] = 99;
		
	    //initializing and declaring values for quantization table for Chrominance
		double[] qTableC = new double[64];
		qTableC[0] = 17;
	    qTableC[1] = 18;
	    qTableC[2] = 24;
	    qTableC[3] = 47;
	    qTableC[4] = 99;
	    qTableC[5] = 99;
	    qTableC[6] = 99;
	    qTableC[7] = 99;
	    qTableC[8] = 18;
	    qTableC[9] = 21;
	    qTableC[10] = 26;
	    qTableC[11] = 66;
	    qTableC[12] = 99;
	    qTableC[13] = 99;
	    qTableC[14] = 99;
	    qTableC[15] = 99;
	    qTableC[16] = 24;
	    qTableC[17] = 26;
	    qTableC[18] = 56;
	    qTableC[19] = 99;
	    qTableC[20] = 99;
	    qTableC[21] = 99;
	    qTableC[22] = 99;
	    qTableC[23] = 99;
	    qTableC[24] = 47;
	    qTableC[25] = 66;
	    qTableC[26] = 99;
	    qTableC[27] = 99;
	    qTableC[28] = 99;
	    qTableC[29] = 99;
	    qTableC[30] = 99;
	    qTableC[31] = 99;
	    qTableC[32] = 99;
	    qTableC[33] = 99;
	    qTableC[34] = 99;
	    qTableC[35] = 99;
	    qTableC[36] = 99;
	    qTableC[37] = 99;
	    qTableC[38] = 99;
	    qTableC[39] = 99;
	    qTableC[40] = 99;
	    qTableC[41] = 99;
	    qTableC[42] = 99;
	    qTableC[43] = 99;
	    qTableC[44] = 99;
	    qTableC[45] = 99;
	    qTableC[46] = 99;
	    qTableC[47] = 99;
	    qTableC[48] = 99;
	    qTableC[49] = 99;
	    qTableC[50] = 99;
	    qTableC[51] = 99;
	    qTableC[52] = 99;
	    qTableC[53] = 99;
	    qTableC[54] = 99;
	    qTableC[55] = 99;
	    qTableC[56] = 99;
	    qTableC[57] = 99;
	    qTableC[58] = 99;
	    qTableC[59] = 99;
	    qTableC[60] = 99;
	    qTableC[61] = 99;
	    qTableC[62] = 99;
	    qTableC[63] = 99;
	    
	    //zig zag array to hold the "order" in which the final arrays will be read by huffman code
	    int[] zigZag = {
				0 , 1, 8, 16, 9, 2, 3, 10, 
				17, 24, 32, 25, 18, 11, 4, 5, 
				12, 19, 26, 33, 40, 48, 41, 34,
				27, 20, 13, 6, 7, 14, 21, 28,
				35, 42, 49, 56, 7, 50, 43, 36,
				29, 22, 15, 23, 30, 37, 44, 51, 
				58, 59, 52, 45, 38, 31, 39, 46,
				53, 60, 61, 54, 47, 55, 62, 63
		};
		
		//iterating through the first 8bit MCU of the image to obtain the rgb values
		int iterator = 0;
		for (int y = 0; y < 8; y++) {
		    for (int x = 0; x < 8; x++) {
		        int pixel = bi.getRGB(x, y);
		        rArray[iterator] = (pixel >> 16) & 0xff;
		        gArray[iterator] = (pixel >> 8) & 0xff;
		        bArray[iterator] = (pixel) & 0xff;  
		        iterator++;
		    }
		}
		
		//printing the rgb values read 
		System.out.println("RGB Values:");
		for(int b=0;b<64;b++) {
			System.out.println(b+"    red=="+rArray[b]+" green=="+gArray[b]+" blue=="+bArray[b]);
		}
		
		//initializing the arrays which will hold the YCbCr values
		int[] yArray = new int[64];
		int[] cbArray = new int[64];
		int[] crArray = new int[64];
		
		//ask if this is correct, should it be rounding down|Performing conversion from RGB-->YCbCr
		for(int v=0;v<64;v++) {
			yArray[v] = (int)((0.299*rArray[v])+(0.587*gArray[v])+(0.114*bArray[v]));
			cbArray[v] = (int)(128-(0.168736*rArray[v])-(0.331264*gArray[v])+(0.5*bArray[v]));
			crArray[v] = (int)(128+(0.5*rArray[v])-(0.418688*gArray[v])-(0.081312*bArray[v]));
		}
		
		//displaying YCbCr values
		System.out.println("\nNow, the YCbCr values:\n");
		
		for(int b1=0;b1<64;b1++) {
			System.out.println(b1+"    Y:"+yArray[b1]+", Cb:"+cbArray[b1]+", Cr:"+crArray[b1]);
		}
		
		//initializing 2d array for the YCbCr values since it is easier to run DCT algorithms on a 2D array.
		int yArray2d[][] = new int[8][8];
		int cbArray2d[][] = new int[8][8];
		int crArray2d[][] = new int[8][8];
	
		//converting the 1d "yArray,cbArray,crArray" into new 2D arrays
		for(int i=0; i<8;i++)
		   for(int j=0;j<8;j++) {
		       yArray2d[i][j] = yArray[(j*8) + i];
		       cbArray2d[i][j] = cbArray[(j*8) + i];
		       crArray2d[i][j] = crArray[(j*8) + i];
		   }
		
		//printing the 2d arrays
		System.out.println("\nY Values 2d array:");
		System.out.println(Arrays.deepToString(yArray2d).replace("], ", "]\n").replace("[[", "[").replace("]]", "]"));
		
		System.out.println("\nCb Values 2d array:");
		System.out.println(Arrays.deepToString(cbArray2d).replace("], ", "]\n").replace("[[", "[").replace("]]", "]"));
		
		System.out.println("\nCr Values 2d array:");
		System.out.println(Arrays.deepToString(crArray2d).replace("], ", "]\n").replace("[[", "[").replace("]]", "]"));
		
		//removing 128 from each value in all tables to ensure the DCT values computed remain between -1024 and 1024
		for(int i=0; i<8;i++)
			   for(int j=0;j<8;j++) {
			       yArray2d[i][j] -=128;
			       cbArray2d[i][j] -=128;
			       crArray2d[i][j] -=128;
			   }
		
		//initializing arrays to hold the new DCT values
		double yArrayDCT[][] = new double[8][8];
		double cbArrayDCT[][] = new double[8][8];
		double crArrayDCT[][] = new double[8][8];
		
		//the DCT method performs the transform and prints the resulting matrix. It is performed on all 3 Arrays.
		System.out.println("\nDCT values for luminance:\n");
		yArrayDCT = dctTransform(yArray2d);
		System.out.println("\nDCT values for blue chrominance\n");
		cbArrayDCT = dctTransform(cbArray2d);
		System.out.println("\nDCT values for red chrominance\n");
		crArrayDCT = dctTransform(crArray2d);
		
		//initializing arrays to hold the quantized values of the DCT results.
		int yArrayQuantized[][] = new int[8][8];
		int cbArrayQuantized[][] = new int[8][8];
		int crArrayQuantized[][] = new int[8][8];
		
		int iterator2 = 0;
		
		//looping through each table and dividing the DCT results by the corresponding values in the Quantization tables
		for(int i=0; i<8;i++)
			   for(int j=0;j<8;j++) {
			       yArrayQuantized[i][j] =(int)Math.abs((yArrayDCT[i][j]/(qTableY[iterator2])));
			       cbArrayQuantized[i][j] =(int)Math.abs((cbArrayDCT[i][j]/(qTableC[iterator2])));
			       crArrayQuantized[i][j] =(int)Math.abs((crArrayDCT[i][j]/(qTableC[iterator2])));
			       iterator2++;
			   }
		
		//displaying the quantized DCT matrices
		System.out.println("\nAfter Quantizing the DCT matrices:");
		System.out.println("\nY:");	
		System.out.println(Arrays.deepToString(yArrayQuantized).replace("], ", "]\n").replace("[[", "[").replace("]]", "]"));
		System.out.println("\nCb:");
		System.out.println(Arrays.deepToString(cbArrayQuantized).replace("], ", "]\n").replace("[[", "[").replace("]]", "]"));
		System.out.println("\nCr:");
		System.out.println(Arrays.deepToString(crArrayQuantized).replace("], ", "]\n").replace("[[", "[").replace("]]", "]"));
		
		//initializing a 1D array
		int yArrayQuantized1D[] = new int[64];
		int cbArrayQuantized1D[] = new int[64];
		int crArrayQuantized1D[] = new int[64];
		
		int iterator3 = 0;
		
		//inserting the 2d array values into a 1D array
		for(int i=0; i<8;i++)
			   for(int j=0;j<8;j++) {
			       yArrayQuantized1D[iterator3] = yArrayQuantized[i][j];
			       cbArrayQuantized1D[iterator3] = cbArrayQuantized[i][j];
			       crArrayQuantized1D[iterator3] = crArrayQuantized[i][j];
			       iterator3++;
			   }
		
		//initializing final array which will be used by huffman coding process
		int yArrayZigZag[] = new int[64];
		int cbArrayZigZag[] = new int[64];
		int crArrayZigZag[] = new int[64];
		
		//placing the values in the correct spot as defined by the ZigZag array
		for(int i = 0; i<64;i++) {
			yArrayZigZag[i] = yArrayQuantized1D[zigZag[i]];
			cbArrayZigZag[i] = cbArrayQuantized1D[zigZag[i]];
			crArrayZigZag[i] = crArrayQuantized1D[zigZag[i]];	
		}
		
		//printing out final values before huffman coding is performed
		System.out.println("\nQuantized Array for Luminance:");
		for(int i=0;i<64;i++) {
			System.out.print(yArrayZigZag[i]+", ");
		}
		System.out.println("\nQuantized Array for Blue Chrominance:");	
	
		for(int i=0;i<64;i++) {
			System.out.print(cbArrayZigZag[i]+", ");
		}
		System.out.println("\nQuantized Array for Red Chrominance:");	
		
		for(int i=0;i<64;i++) {
			System.out.print(crArrayZigZag[i]+", ");
		}
		
		//The function calls for performing Huffman coding are commented out because this step is incomplete and will throw an error
		//huffmanCoding(StdACChrominance.getLengths(), StdACChrominance.getValues());
		//huffmanCoding(StdDCChrominance.getLengths(), StdDCChrominance.getValues());

	}
}

