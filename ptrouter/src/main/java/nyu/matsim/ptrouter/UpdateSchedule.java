package nyu.matsim.ptrouter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

import org.matsim.core.utils.io.IOUtils;

public class UpdateSchedule {

	public static void main(String[] args) throws IOException {

		BufferedReader reader = IOUtils.getBufferedReader(args[0]);
		BufferedWriter writer = IOUtils.getBufferedWriter(args[1]);
		
		String s = reader.readLine();
		
		while (s != null) {
			
			if (s.contains("<stop refId")) {
				
				if (!s.contains("departureOffset")) {
					
					String s2 = s.substring(0, s.length() - 2) + " departureOffset=\"" + s.substring(s.length() - 33, s.length() - 25) +"\"" +"/>";
					writer.write(s2);
					writer.newLine();
				}
				else {
					writer.write(s);
					writer.newLine();
				}
					
			}
			else {
				writer.write(s);
				writer.newLine();
			}
			s = reader.readLine();
		}
		writer.flush();
		writer.close();
	}

}
