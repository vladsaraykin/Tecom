package app;
import java.util.Scanner;

public class ConsoleScanner {
	private Scanner scanner = new Scanner(System.in);
	public ConsoleScanner() {
		super();
	}
	
	public Integer nextInt() {
		Integer result = null;
		if(scanner.hasNextInt()) {
			result = scanner.nextInt();
		}
		return result;
		
	}
	
	public String next() {
		String result = null;
		if(scanner.hasNext()) {
			result = scanner.next();
		}
		return result;
	}
}
