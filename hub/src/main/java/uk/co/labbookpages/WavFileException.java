package uk.co.labbookpages;

/**
 * WavFile code obtained from http://www.labbookpages.co.uk/audio/javaWavFiles.html
 * License: http://www.labbookpages.co.uk/home/licences.html
 */

public class WavFileException extends Exception
{
	public WavFileException()
	{
		super();
	}

	public WavFileException(String message)
	{
		super(message);
	}

	public WavFileException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public WavFileException(Throwable cause) 
	{
		super(cause);
	}
}
