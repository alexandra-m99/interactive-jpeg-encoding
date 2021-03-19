package jpegEncoder;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Base64;
import java.util.Scanner;
import javax.imageio.ImageIO;
import java.lang.Math;

//class to hold information for the quantization tables
class quantizationTable
{
	byte QuantizationTable[] = new byte[64];
	boolean isSet = false;
	
	public quantizationTable()
	{
		for(int i=0; i<QuantizationTable.length; i++)
		{
			QuantizationTable[i] = 0;
		}
	}
	
	public byte[] getQTable()
	{
		return QuantizationTable;
	}
	
}

//class to hold information about the colour components 
class colourComponent
{
	byte hSamplingFactor;
	byte vSamplingFactor;
	byte qTableID;
	boolean isUsed;
	
	public colourComponent()
	{
		hSamplingFactor = 1;
		vSamplingFactor = 1;
		qTableID = 0;
		isUsed = false;
	}
}

//class that does all the encoding steps 
class jpegEncoder 
{
	static File file;
	static String filename;
	static boolean validJPEG = true;
	static byte tableID = 0;
	static byte table = 0;
	static long appnLength = 0;
	static long dqtLength = 0;
	static long sofLength = 0;
	static byte frameType = 0;
	static byte precision = 0;
	static long height = 0;
	static long width = 0;
	static byte components = 0;
	static byte componentID = 0;
	static byte samplingFactor = 0;
	static StringBuilder builder = new StringBuilder();
	
	//create array of type quantizationTable that will store the tables
	static quantizationTable[] qTables = new quantizationTable[4];
	
	//create array of type colourComponent to hold the colour information
	static colourComponent[] colour = new colourComponent[3];
	
	//create object of type colourComponent to access the class instead of constantly reading into
	//the colour array
	static colourComponent colourComp = new colourComponent();
	
	//Application Segment (APPN) markers	
	static byte APP[] = new byte[16];
	
	//SOF marker
	static byte SOF[] = new byte[20];
	
	//Huffman table
	static final byte DHT = (byte) 0xC4;

	//Other markers
	static final byte SOI = (byte) 0xD8;	
	static final byte EOI = (byte) 0xD9;
	static final byte SOS = (byte) 0xDA;
	static final byte DQT = (byte) 0xDB;
	
	//initialize the zig zag matrix as an array to be used for the quantization tables
	static byte zigZag[] = {
			0 , 1, 8, 16, 9, 2, 3, 10, 
			17, 24, 32, 25, 18, 11, 4, 5, 
			12, 19, 26, 33, 40, 48, 41, 34,
			27, 20, 13, 6, 7, 14, 21, 28,
			35, 42, 49, 56, 7, 50, 43, 36,
			29, 22, 15, 23, 30, 37, 44, 51, 
			58, 59, 52, 45, 38, 31, 39, 46,
			53, 60, 61, 54, 47, 55, 62, 63
	};

	public static void main(String[] args) 
	{
		//Set APPN values
		APP[0] = (byte) 0xE0;
		APP[1] = (byte) 0xE1;
		APP[2] = (byte) 0xE2;
		APP[3] = (byte) 0xE3;
		APP[4] = (byte) 0xE4;
		APP[5] = (byte) 0xE5;
		APP[6] = (byte) 0xE6;
		APP[7] = (byte) 0xE7;
		APP[8] = (byte) 0xE8;
		APP[9] = (byte) 0xE9;
		APP[10] = (byte) 0xEA;
		APP[11] = (byte) 0xEB;
		APP[12] = (byte) 0xEC;
		APP[13] = (byte) 0xED;
		APP[14] = (byte) 0xEE;
		APP[15] = (byte) 0xEF;
		
		//Set Start of Frame (SOF) markers
		SOF[0] = (byte) 0xC0;
		SOF[1] = (byte) 0xC1;
		SOF[2] = (byte) 0xC2;
		SOF[3] = (byte) 0xC3;
		SOF[5] = (byte) 0xC5;
		SOF[6] = (byte) 0xC6;
		SOF[7] = (byte) 0xC7;
		SOF[9] = (byte) 0xC9;
		SOF[10] = (byte) 0xCA;
		SOF[11] = (byte) 0xCB;
		SOF[13] = (byte) 0xCD;
		SOF[14] = (byte) 0xCE;
		SOF[15] = (byte) 0xCF;
		
		//read a jpeg file from user 
		while(true)
		{
			Scanner sc = new Scanner(System.in);
			filename = sc.nextLine();
			
			String ext = filename.substring(filename.lastIndexOf(".") + 1, filename.length());
			
			//check file arguments and extensions
			if(filename.isEmpty() || filename.equals(" "))
			{
				System.out.println("Invalid input argument.\n");
			}
			if(ext.equals("jpeg") == true || ext.equals("jpg") == true)
			{
				break;
			}
			else
			{
				System.out.println("Must enter a jpeg image file.");
			}	
		}
		
		file = new File(filename);
		
		//call functions that read in the JPEG and the function to display the header information
		readJPEG(filename);
		displayHeader();
	}
	
	//function to read the JPEG headers
	public static void readJPEG(String filename)
	{
		//initialize the object array
		for(int i=0; i<qTables.length; i++)
		{
			qTables[i] = new quantizationTable();
		}
		
		for(int i=0; i<colour.length; i++)
		{
			colour[i] = new colourComponent();
		}
		
		//TESTING
		/*quantizationTable q = new quantizationTable();
		byte[] accessArray = new byte[4];
		accessArray = q.getQTable();*/
		//accessArray = q.QuantizationTable[1];
		
		/*for(int i=0; i<qTables.length; i++)
		{
			System.out.println(qTables[0].QuantizationTable[1]);
		}*/
				
		/*System.out.println(accessArray[0]);
		System.out.println(accessArray[1]);
		System.out.println(accessArray[2]);
		System.out.println(accessArray[3]);*/
		
		
		BufferedImage bImage = null;
		
		try
		{
			//if file exists, read the JPEG image headers
			if(file.isFile())
			{
				//read image and write to byte array
				bImage = ImageIO.read(file);
				ByteArrayOutputStream output = new ByteArrayOutputStream();
				ImageIO.write(bImage, "jpg", output);
				byte[] data = output.toByteArray();
				
				int fileLength = data.length;
				
				//get last byte and current byte of image
				byte last = data[fileLength-2];
				byte last2 = data[fileLength-1];
				byte current = data[0];
				byte current2 = data[1];
				
				//TESTING - prints out values in the jpeg file (from array)
				System.out.println("Values from JPEG file:");
				for(int i=0; i<fileLength; i++)
				{
					System.out.print(String.format("%02x", data[i])+ " ");
				}
				
				
				//For testing purposes
				//builder.append(String.format("%02x", data & 0xFF));
				//System.out.println(builder.toString());
				
				
				//Check if input is valid JPEG file
				if((last & 0xFF) != 0xFF || (last2 & 0xFF) != (EOI & 0xFF)|| (current & 0xFF) != 0xFF || (current2 & 0xFF) != (SOI & 0xFF))
				{
				    System.out.println("Invalid JPEG");
					validJPEG = false;
					output.close();
				}
						
				//TESTING
				//int pos = 0;
				
				//loop while JPEG is valid
				while(validJPEG == true)
				{
					//TESTING
					//System.out.println(pos);
					//pos++;
					
					//check if the file reaches the end without encountering EOI marker
					if(((last & 0xFF) != 0xFF) && ((last2 & 0xFF) != (EOI & 0xFF)))
					{
						System.out.println("Error - expected to read a marker.");
						validJPEG = false;
						output.close();
					}
			
					
					//Reading DQT Marker and populate the Quantization tables
					for(int i=0; i<fileLength; i++)
					{
						//check if current element in file array is the DQT marker
						//TESTING
						//if((data[pos] & 0xFF) == (DQT & 0xFF))
						if((data[i] & 0xFF) == (DQT & 0xFF))
						{
							//System.out.println("Reading DQT marker");
							
							//TESTING
							//dqtLength = (data[pos] << 8) + data[pos];
							
							//get length of DQT header and subtract 2 since 2 bytes were just read while finding length
							dqtLength = (data[i] << 8) + data[i];
							dqtLength -= 2;
							
							
							//Testing
							//builder.append(String.format("%02x", dqtLength & 0xFF));
							//System.out.println(builder.toString());
							
							//TESTING
							//change length to positive number if it is negative
							if(dqtLength < 0)
							{
								dqtLength = Math.abs(dqtLength);
							}
							
							while(dqtLength > 0)
							{
								
								//System.out.println(String.format("%02x", data[i+2] & 0xFF));
								
								//Get the table info and table ID, and subtract 1 from length after reading
								//the table info since that is 1 byte already read
								table = (byte) (data[i+1] & 0xFF);
								dqtLength -= 1;
								tableID =  (byte) (table & 0x0F);
							
								//check if table id is greater than 3, if so it is invalid 
								//(the table ID must be a value from 0 through 3)
								if(tableID > 3)
								{
									//System.out.println("Invalid DQT table ID " + Byte.toUnsignedInt(tableID));
									validJPEG = false;
									break;
								}
								
								//update boolean variable if tableID is valid
								qTables[tableID].isSet = true;
								
								//checks the first part of the byte for table information by shifting 4 bits
								//to see if a 16 bit Q table or a 8 bit Q table is needed (0 for 8 bit, 1 for 16 bit)
								if((table >> 4) != 0)
								{
									//set current quantization table to equal next 2 bytes in the array (file)
									//then subtract 128 from length since the values are already read in
									for(int j=0; j<64; j++)
									{
										qTables[tableID].QuantizationTable[zigZag[j]] = (byte) ((data[j] << 8) + data[j]);
									}
									dqtLength -= 128;
								}
								else
								{
									//set current quantization table equal to next byte in file and subtract 64 from
									//length since this is a 8 bit table
									for(int j=0; j<64; j++)
									{
										qTables[tableID].QuantizationTable[zigZag[j]] = data[j];
									}
									dqtLength -= 64;
								}
							}
						}
					}
					
					//Read SOF marker
					for(int i=0; i<fileLength; i++)
					{
						//check if current element in file array is the SOF marker
						if((data[i] & 0xFF) == (SOF[0] & 0xFF))
						{
							
							
							//System.out.println("Reading SOF marker");
							
							//set the type as baseline jpeg
							frameType = SOF[0];
							
							//check to make sure that only one SOF marker is read
							if(components != 0)
							{
								System.out.println("Error - found multiple SOF markers");
								validJPEG = false;
								break;
							}
							
							//get length of SOF marker
							sofLength = (data[i] << 8) + data[i];
							
							//get the precision (how many bits are used for each colour channel).
							//This must always be 8
							precision = (byte) (data[i+3] & 0xFF);
							
							//TESTING
							//System.out.println("\n" + String.format("%02x", data[i]));
							
							//check if the precision is any other value than 8
							if(precision != 8)
							{
								System.out.println("\nInvalid JPEG precision " + (int)precision );
								validJPEG = false;
								break;
							}
							
							//get the height and width of jpeg
							height = (data[i+4] << 8) + data[i+5];
							width = (data[i+6] << 8) + data[i+7];
							
							//check if the height and width are 0, if so, this is invalid
							if(height == 0 || width == 0)
							{
								System.out.println("Invalid height and width values");
								validJPEG = false;
								break;
							}
							
							//get the number of components (has to be either 1 or 3)
							components = (byte) (data[i+8] & 0xFF);
							
							
							//check if the components is 4, if so, this is invalid
							if(components == 4)
							{
								System.out.println("Invalid color mode");
								validJPEG = false;
								break;
							}
							
							//Another check to see if the components are 0, if so, this is also invalid
							if(components == 0)
							{
								System.out.println("Amount of components must be greater than 0");
								validJPEG = false;
								break;
							}
							
							//System.out.println("\n" + String.format("%02x", data[i+8]));
							
							//int ind = i+8;
							int ind = 0;
							//THIS IS WHERE THE ERROR IS
							//loop for the amount of components in order to continue reading for the component ID, 
							//sampling factor, and quantization ID
							for(int j=0; j<components; j++)
							{
								
								if(componentID == 01)
								{
									ind = i+11;
								}
								else if(componentID == 02)
								{
									ind = i+14;
								}
								else if(componentID == 03)
								{
									break;
								}
								else if(componentID == 0)
								{
									ind = i+8;
								}
								
								//get the component ID
								componentID = (byte) (data[ind+1] & 0xFF);
								
								//Since the YCbCr jpeg has colour component ID's as 1, 2, and 3, anything else
								//is invalid
								if(componentID == 0 || componentID > 3)
								{
									System.out.println("Invalid component ID" + (int)componentID);
									validJPEG = false;
									break;
								}
								
								//use the object to store the array at index componentID - 1 b/c the loop index
								//runs from 0,1,2, and since the component IDs are 1,2,3, then we need to subtract 1
								colourComp = colour[(int)(componentID-1)];
								//System.out.println("\n" + String.format("%02x", colour[componentID-1]));
								
								//check if colour component is already being used, if so, this is an error
								//since there cannot be duplicate component IDs
								if(colourComp.isUsed)
								{
									System.out.println("Duplicate colour component ID");
									validJPEG = false;
									break;
								}
								
								colourComp.isUsed = true;
								
								//get the sampling factor 
								samplingFactor = (byte) (data[ind+2] & 0xFF);
								
								//get first half of sampling factor (horizontal) by shifting right 4 bits
								colourComp.hSamplingFactor = (byte) (samplingFactor >> 4);
								
								//get other half of sampling factor (vertical) by using bitwise AND
								colourComp.vSamplingFactor = (byte) (samplingFactor & 0x0F);
								
								//get the quantization table id
								colourComp.qTableID = (byte) (data[ind+3] & 0xFF);
								
								//check that the quantization table ID is not greater than 3 for this component
								if(colourComp.qTableID > 3)
								{
									System.out.println("Invalid quantization table ID for this frame");
									validJPEG = false;
									break;
								}
								
							
							}
							
							//check that the SOF marker length is correct by subtracting length minus precision minus number of components
							//and verify it is not equal to 0
							if(sofLength - 8 - (3*components) != 0)
							{
								System.out.println("Invalid SOF marker");
								validJPEG = false;
							}
							
						}
							
					}	
					
					//TESTING
				   //System.out.println(dqtLength);
					
					/*if(dqtLength != 0)
					{
						System.out.println("Invalid DQT marker");
					}
					break;*/
					
					
					//Read the APPN marker
					for(int i=0; i<fileLength; i++)
					{
						if(((data[i] & 0xFF) >= (APP[0] & 0xFF)) && ((data[i] & 0xFF) <= (APP[15] & 0xFF)))
						{
							//System.out.println("Reading APPN");
							appnLength = ((data[i] << 8) + data[i]) & 0xFFFFFFFFL;
							
							break;
						}	
					}	
				}
			}
		}	
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (IndexOutOfBoundsException i)
		{
			i.printStackTrace();
		}
		catch(NullPointerException n)
		{
			n.printStackTrace();
		}
	
	}
	
	//function to display all the header information
	public static void displayHeader()
	{
		System.out.println("\nDQT HEADER\n-------------------------");
		
		for(int i=0; i<2; i++)
		{
			if(qTables[i].isSet)
			{
				System.out.println("\nTable ID: "+ i);
				System.out.println("Data:");
				
				for(int j=0; j<64; j++)
				{
					if(j % 8 == 0)
					{
						System.out.print("\n");
					}
					System.out.print(((qTables[i].QuantizationTable[j])) + " ");
					//System.out.print((String.format("%02d", (qTables[i].QuantizationTable[j]) )+ " "));
				}
				System.out.print("\n");
			}
		}
		
		System.out.print("\nSOF HEADER\n-------------------\n");
		System.out.print("Frame Type: 0x"+ String.format("%02x", frameType));
		System.out.print("\nHeight: " + height);
		System.out.print("\nWidth: " + width);
		System.out.print("\nColour Components:\n");
		for(int i=0; i<components; i++)
		{
			System.out.print("Component ID: " + (i+1) + "\n");
			System.out.print("Horizontal Sampling Factor: " + (int)colour[i].hSamplingFactor + "\n");
			System.out.print("Vertical Sampling Factor: " + (int)colour[i].vSamplingFactor + "\n");
			System.out.print("Quantization Table ID: " + (int)colour[i].qTableID + "\n");
		}
	}
	

}
