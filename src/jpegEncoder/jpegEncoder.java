package jpegEncoder;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Base64;
import java.util.Scanner;
import javax.imageio.ImageIO;

public class jpegEncoder 
{
	static File file;
	static boolean validJPEG = true;
	
	static final byte SOI = (byte) 0xD8;
	
	public static void main(String[] args) 
	{
		
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

		//Application Segment (APPN) markers
		final byte APP[] = new byte[16];
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

		//Restart Interval (RST) markers
		final byte RST[] = new byte[8];
		RST[0] = (byte) 0xD0;
		RST[1] = (byte) 0xD1;
		RST[2] = (byte) 0xD2;
		RST[3] = (byte) 0xD3;
		RST[4] = (byte) 0xD4;
		RST[5] = (byte) 0xD5;
		RST[6] = (byte) 0xD6;
		RST[7] = (byte) 0xD7;

		//Huffman table
		final byte DHT = (byte) 0xC4;
	
		//JPEG extensions
		final byte JPG = (byte) 0xC8;
	
		//Arithmetic coding 
		final byte DAC = (byte) 0xCC;
	
		//Other markers
		
		final byte EOI = (byte) 0xD9;
		final byte SOS = (byte) 0xDA;
		final byte DQT = (byte) 0xDB;
		final byte DNL = (byte) 0xDC;
		final byte DRI = (byte) 0xDD;
		final byte DHP = (byte) 0xDE;
		final byte EXP = (byte) 0xDF;
		
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
		
		readJPEG(filename);
		
	}
	
	public static void readJPEG(String filename)
	{
		StringBuilder builder = new StringBuilder();
		BufferedImage bImage = null;
		
		try
		{
			if(file.isFile())
			{
				//read image and write to byte array
				bImage = ImageIO.read(file);
				ByteArrayOutputStream output = new ByteArrayOutputStream();
				ImageIO.write(bImage, "jpg", output);
				byte[] data = output.toByteArray();
				
				int len = data.length;
				
				//get last byte and current byte of image
				byte last = data[len-2];
				byte current = data[1];
				
				//For testing purposes
				//builder.append(String.format("%02x", data[len-2] & 0xFF));
				//System.out.println(builder.toString());
				
				//Check if input is valid JPEG file
				if((last & 0xFF) != 0xFF || (current & 0xFF) != (SOI & 0xFF))
				{
				    //System.out.println("Invalid JPEG");
					validJPEG = false;
					output.close();
				}
				
				//check if the file reaches the end without encountering EOI marker
				while(validJPEG == true)
				{
					if((last & 0xFF) != 0xFF)
					{
						System.out.println("Error - expected to read a marker.");
						validJPEG = false;
						output.close();
					}
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

}
