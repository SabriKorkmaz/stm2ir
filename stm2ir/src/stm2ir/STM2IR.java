package stm2ir;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class STM2IR {
    	
	// private static final List<String> VARIABLES = new ArrayList<String>();

	// private static int lineCounter = 0;

	// private static final LinkedList<String> LIST = new LinkedList<>();

	// private static int tempCounter = 0;

	private static int entryType;

	private static String[] operations = { "+", "-", "/", "*" };

	public static void main(String[] args) throws IOException {
		
		/*
		LIST.add("; ModuleID = 'stm2ir'");
		LIST.add("declare i32 @printf(i8*, ...)");
		LIST.add("@print.str = constant [4 x i8] c\"%d\\0A\\00\"");
		LIST.add("define i32 @main() {");

		
		Scanner input = new Scanner(new File(args[0]), "UTF-8");
		while (input.hasNextLine()) {

			
			 lineCounter++; String line = input.nextLine().strip(); if
			 (line.contains("=")) { String left = line.split("=")[0], right =
			 line.split("=")[1]; if (!VARIABLES.contains(left)) { VARIABLES.add(left);
			 LIST.add("%" + left + " = alloca i32"); } 
			 LIST.add("store i32 " + findVariable(expression(right)) + ", i32* %" + left); } else { //
			 LIST.add("call i32 (i8*, ...)* @printf(i8* getelementptr ([4 x i8]* @print.str, i32 0, i32 0), i32 " + findVariable(expression(line)) + " )");
			 tempCounter++; }
			 

		}
		input.close();
		LIST.add("ret i32 0");
		LIST.add("}");
		
		 PrintStream printStream = new PrintStream(args[0].split("\\.")[0] + ".ll",
		 "UTF-8"); for (ListIterator<String> iterator = LIST.listIterator();
		 iterator.hasNext();) { printStream.println(iterator.next()); }
		 printStream.close();
	*/
		
		InputStreamReader isr = new InputStreamReader(System.in);
		BufferedReader br = new BufferedReader(isr);
		String input = br.readLine();

		entryType = getEntryType(input);
		System.out.print(entryType);
		 
	}
	// cem deneme github

	private static int getEntryType(String str) {
		// TODO Auto-generated method stub
		if (!str.contains("=")) {
			// if the statement doesn't include = , then this is a variable to be printed
			return 1;
		} else if (str.contains("=")) {
			for (int i = 0; i < operations.length; i++) {
				if (str.contains(operations[i])) {
					// if returns 2, there is a computation to do
					return 2;
				}
			}
			// if returns 3, this is an assignment of a variable
			return 3;

		}
		return 0;
	}
	   

}
