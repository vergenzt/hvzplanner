package zombieplanner;


import java.io.InputStream;

public class ResourceLoader {

	static ResourceLoader rl = new ResourceLoader();
	
	public static InputStream getInputStream(String filename) {
		return (rl.getClass().getResourceAsStream(filename));
	}
}
