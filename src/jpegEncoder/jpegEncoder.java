package jpegEncoder;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Base64;
import java.util.Scanner;
import javax.imageio.ImageIO;
import java.lang.Math;

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

class jpegEncoder 
{
	static File file;
	static boolean validJPEG = true;
	static long appnLength = 0;
	static long dqtLength = 0;
	static StringBuilder builder = new StringBuilder();
	static quantizationTable[] qTables = new quantizationTable[4];		
	
	//Application Segment (APPN) markers	
	static byte APP[] = new byte[16];
	
	//Huffman table
	static final byte DHT = (byte) 0xC4;

	//Other markers
	static final byte SOI = (byte) 0xD8;	
	static final byte EOI = (byte) 0xD9;
	static final byte SOS = (byte) 0xDA;
	static final byte DQT = (byte) 0xDB;

	
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
		
		//Start of Frame (SOF) markers
	    final byte SOF[] = new byte[20];
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
		
		String filename;
		
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
	
	public static void readJPEG(String filename)
	{
		for(int i=0; i<qTables.length; i++)
		{
			qTables[i] = new quantizationTable();
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
		int pos = 0;
		byte tableID = 0;
		byte table = 0;
		
		try
		{
			if(file.isFile())
			{
				
				//read image and write to byte array
				bImage = ImageIO.read(file);
				ByteArrayOutputStream output = new ByteArrayOutputStream();
				ImageIO.write(bImage, "jpg", output);
				byte[] data = output.toByteArray();
				int fileLength = data.length;
				//System.out.println(fileLength);
				
				//get last byte and current byte of image
				byte last = data[fileLength-2];
				byte last2 = data[fileLength-1];
				byte current = data[0];
				byte current2 = data[1];
				
				//TESTING - prints out values in the jpeg file (from array)
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
							
				
				//check if the file reaches the end without encountering EOI marker
				while(validJPEG == true)
				{
					if(((last & 0xFF) != 0xFF) && ((last2 & 0xFF) != (EOI & 0xFF)))
					{
						System.out.println("Error - expected to read a marker.");
						validJPEG = false;
						output.close();
					}
					
					
					//Reading DQT Marker and populate the Quantization tables
					for(int i=0; i<fileLength; i++)
					{
						if((data[i] & 0xFF) == (DQT & 0xFF))
						{
							//System.out.println("Reading DQT marker");
							//System.out.println(String.format("%02x", data[i]));
							
							dqtLength = (data[i] << 8) + data[i];
							dqtLength -= 2;
							
							
							//Testing
							//builder.append(String.format("%02x", dqtLength & 0xFF));
							//System.out.println(builder.toString());
							
							
							if(dqtLength < 0)
							{
								dqtLength = Math.abs(dqtLength);
							}
							
							while(dqtLength > 0)
							{
								//System.out.println(String.format("%02x", data[i+2] & 0xFF));
								table = (byte) (data[i+1] & 0xFF);
								dqtLength -= 1;
								tableID =  (byte) (table & 0x0F);
							
								
								if(tableID > 3)
								{
									//System.out.println("Invalid DQT table ID " + Byte.toUnsignedInt(tableID));
									validJPEG = false;
									break;
								}
								//System.out.println(builder.append(String.format("%02x", tableID & 0xFF)));
								
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
										qTables[tableID].QuantizationTable[j] = (byte) ((data[j] << 8) + data[j]);
									}
									dqtLength -= 128;
								}
								else
								{
									//set current quantization table equal to next byte in file and subtract 64 from
									//length since this is a 8 bit table
									for(int j=0; j<64; j++)
									{
										qTables[tableID].QuantizationTable[j] = data[j];
									}
									dqtLength -= 64;
								}
							}
						}
					}
					
							
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
				
				/*if(validJPEG == false)
				{
					System.out.println("Invalid JPEG.");
				}
				else
				{
					System.out.println("Valid JPEG");
				}*/	
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
	
	
	public static void displayHeader()
	{
		System.out.println("\nDQT HEADER");
		
		for(int i=0; i<4; i++)
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
	}
	

}
