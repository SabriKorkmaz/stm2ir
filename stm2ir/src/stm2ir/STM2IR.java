package stm2ir;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Scanner;

public class STM2IR {

	private static LinkedList<String> outputList = new LinkedList<String>();

	private static Map<String, String> variableDictionary = new HashMap<String, String>();

	private static EntryType entryType;

	private static int lineCounter = 0;

	private static String[] operations = { "+", "-", "/", "*" };

	public static void main(String[] args) throws IOException {

		outputList.add("; ModuleID = 'stm2ir'");
		outputList.add("declare i32 @printf(i8*, ...)");
		outputList.add("@print.str = constant [4 x i8] c\"%d\\0A\\00\"");
		outputList.add("define i32 @main() {");

		//Scanner input = new Scanner(new File(args[0]), "UTF-8");
		Scanner input = new Scanner(new File("/Users/sabri/Desktop/file.stm"), "UTF-8");
		while (input.hasNextLine()) {
			 String line = input.nextLine().replaceAll("\\s+", "");
			lineCounter++;
			switch (getEntryType(line)) {
			case DirectAssignment:
				DirectAssignments(line);
				break;
			case Print:
				outputList.add("%" + lineCounter + " = load i32* %" + line);
				outputList.add(
						"call i32 (i8*, ...)* @printf(i8* getelementptr ([4 x i8]* @print.str, i32 0, i32 0), i32 %"
								+ lineCounter + " )");
				break;
			case Calculation:
				String left = line.split("=")[0], right = line.split("=")[1];
				if (!variableDictionary.containsValue(left)) {
					outputList.add("%" + left + " = alloca i32");
				}
				Calculation(right);
				outputList.add("store i32 %" + (lineCounter - 1) + "," + left);
				variableDictionary.replace(left, String.valueOf(lineCounter - 1));
				break;
			default:
				break;
			}
		}
		input.close();
		outputList.add("ret i32 0");
		outputList.add("}");

		PrintStream printStream = new PrintStream("/Users/sabri/Desktop/file.ll", "UTF-8");
		for (ListIterator<String> iterator = outputList.listIterator(); iterator.hasNext();) {
			printStream.println(iterator.next());
		}
		printStream.close();

	}

	private static void DirectAssignments(String inputLine) {
		/*
		 * splits the string on the first "=" sign, takes the first part and adds to the
		 * variableList (if it has not been added yet)
		 */
		String[] input = inputLine.split("=", 2);
		String variable = input[0].strip();
		String value = input[1].strip();
		if (!variableDictionary.containsValue(variable)) {
			variableDictionary.put(variable, variable);
			outputList.add("%" + variable + " = alloca i32");
			outputList.add("store i32 " + value + " , i32* %" + variable);
		}

	}

	private static int Operation(String operand1, String operand2, String operation) {

		if (variableDictionary.containsKey(operand1)) {
			outputList.add("%" + lineCounter + " = load i32* %" + operand1);
			variableDictionary.replace(operand1, "%" + lineCounter);
			operand1 = variableDictionary.get(operand1);
			lineCounter++;
		}
		if (variableDictionary.containsKey(operand2)) {
			outputList.add("%" + lineCounter + " = load i32* %" + operand2);
			variableDictionary.replace(operand2, "%" + lineCounter);
			operand2 = variableDictionary.get(operand2);
			lineCounter++;
		}
		switch (operation) {
		case ("*"):
			outputList.add("%" + lineCounter + " = mul i32 " + operand1 + " , " + operand2);
			lineCounter++;
			return lineCounter;
		case ("+"):
			outputList.add("%" + lineCounter + " = add i32 " + operand1 + " , " + operand2);
			lineCounter++;
			return lineCounter;
		case ("/"):
			outputList.add("%" + lineCounter + " = udiv i32 " + operand1 + " , " + operand2);
			lineCounter++;
			return lineCounter;
		case ("-"):
			outputList.add("%" + lineCounter + " = sub i32 " + operand1 + " , " + operand2);
			lineCounter++;
			return lineCounter;
		default:
			return 0;
		}
	}

	private static String Calculation(String expression) {

		while (expression.contains("(") || expression.contains(")")) {
			int begin = expression.lastIndexOf('('), end = expression.indexOf(')', begin);

			String inside = expression.substring(begin + 1, end);
			expression = expression.replace("(" + inside + ")", Calculation(inside));
		}

		List<String> lineOfNumbers = new ArrayList(Arrays
				.asList(expression.replaceAll("[+*/-]", " ").replaceAll(" +", " ").strip().split(" ")));

		List<String> lineOfOperations = new ArrayList(Arrays.asList(expression.replaceAll("[^-+*/]", "").strip().split("")));

		var operationLength = lineOfOperations.size();

		while (operationLength > 0) {
			int i = 0;
			if (lineOfOperations.get(i) == "+" || lineOfOperations.get(i) == "-") {
				if (lineOfOperations.get(i + 1) == "*" || lineOfOperations.get(i + 1) == "/") {
					String operation = lineOfOperations.get(i + 1);
					String operand1 = lineOfNumbers.get(i + 1);
					String operand2 = lineOfNumbers.get(i + 2);
					lineOfNumbers.set(i + 2, "%" + Operation(operand1, operand2, operation));
					lineOfNumbers.remove(i + 1);
					lineOfOperations.remove(i + 1);
					
				} else {
					String operation = lineOfOperations.get(i);
					String operand1 = lineOfNumbers.get(i);
					String operand2 = lineOfNumbers.get(i + 1);
					lineOfNumbers.set(i + 1, "%" + Operation(operand1, operand2, operation));
					lineOfNumbers.remove(i);
					lineOfOperations.remove(i);
					
				}
			} else {
				String operation = lineOfOperations.get(i);
				String operand1 = lineOfNumbers.get(i);
				String operand2 = lineOfNumbers.get(i + 1);
				lineOfNumbers.set(i + 1, "%" + Operation(operand1, operand2, operation));
				lineOfNumbers.remove(i);
				lineOfOperations.remove(i);
			}
			operationLength = lineOfOperations.size();
		}
		// expression return 23+x1*1+y  sadece parantez siliniyor. burada işlemin sonucu olan lineCount dönmesi gerek
		return "%"+lineCounter;
	}

	private static void Print(String inputLine) {

	}

	private static EntryType getEntryType(String inputLine) {
		if (inputLine.contains("=")) {
			for (int i = 0; i < operations.length; i++) {
				if (inputLine.contains(operations[i])) {
					return EntryType.Calculation;
				}
			}
			return EntryType.DirectAssignment;
		}
		return EntryType.Print;
	}

}
