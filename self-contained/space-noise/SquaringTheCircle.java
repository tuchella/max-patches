import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;


/**
 * Takes a circular image (e.g. https://commons.wikimedia.org/wiki/File:Ilc_9yr_moll4096.png) and turns it into a (bilinear) interpolated square image. 
 */
public class SquaringTheCircle {
	private static final float LUMINANCE_LIMIT = 0.9f;
	
	public static void main(String[] args) throws Exception {
		File inputFile = new File(args[0]);
		BufferedImage img = ImageIO.read(inputFile);
		for (int y = 0; y < img.getHeight(); y++) {
			int[] line = new int[img.getWidth()]; 
			img.getRGB(0, y, img.getWidth(), 1, line, 0, 0);
			line = stretch(line);
			img.setRGB(0, y, img.getWidth(), 1, line, 0, 0);
		}
		
		File outputFile = new File(inputFile.getParent(), inputFile.getName().replace(".", "_out."));
		ImageIO.write(img, "png", outputFile);
	}
	
	public static int[] stretch(int[] in) {
		int[] out = new int[in.length];
		int start = -1;
		int end = in.length;
		for (int i = 0; i < in.length; i++) {
			float luminance = getLuminance(in[i]);

			if (start == -1 && luminance < LUMINANCE_LIMIT) {
				start = i;
			} else if (start != -1 && luminance >= LUMINANCE_LIMIT) {
				end = i - 1;
				break;
			}
		}
		
		if(end >= in.length) {
			return in;
		}
		
		for (int i = 0; i < out.length; i++) {
			double intepolatedCoordinate = scale(i, 0, out.length - 1, start, end);
			
			Color leftColor = new Color(in[(int) Math.floor(intepolatedCoordinate)]);
			Color rightColor = new Color(in[(int) Math.ceil(intepolatedCoordinate)]);
			
			double leftAmount = intepolatedCoordinate - Math.floor(intepolatedCoordinate);
			double rightAmount = 1 - leftAmount;
			
			out[i] = interpolateColor(leftColor, rightColor, leftAmount, rightAmount).getRGB();
		}
		return out;
	}
	
	private static float getLuminance(int color) {
		// thanks Boann <3 https://stackoverflow.com/a/21210977
		int red   = (color >>> 16) & 0xFF;
		int green = (color >>>  8) & 0xFF;
		int blue  = (color >>>  0) & 0xFF;

		return (red * 0.333f + green * 0.333f + blue * 0.333f) / 255;
	}

	private static Color interpolateColor(Color leftColor, Color rightColor, double leftAmount, double rightAmount) {
		int r = (int) (rightColor.getRed() * leftAmount + leftColor.getRed() * rightAmount);
		int g = (int) (rightColor.getGreen() * leftAmount + leftColor.getGreen() * rightAmount);
		int b = (int) (rightColor.getBlue() * leftAmount + leftColor.getBlue() * rightAmount);
		return new Color(r, g, b);
	}
	
	private static double scale(int i, double inMin, double inMax, double outMin, double outMax) {
		return (((i - inMin) * (outMax - outMin)) / (inMax - inMin)) + outMin;
	}
}

