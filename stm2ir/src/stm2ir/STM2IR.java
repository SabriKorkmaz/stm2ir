package stm2ir;

import java.io.File;
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

	public static void main(String[] args) throws Exception {

		outputList.add("; ModuleID = 'stm2ir'");
		outputList.add("declare i32 @printf(i8*, ...)");
		outputList.add("@print.str = constant [4 x i8] c\"%d\\0A\\00\"");
		outputList.add("define i32 @main() {");
		// TODO: Get project file folder static variable
		Scanner input = new Scanner(new File("/Users/sabri/Desktop/file.stm"), "UTF-8");
		while (input.hasNextLine()) {
			String line = input.nextLine().replaceAll("\\s+", "");
			lineCounter++;
			if (!checkParenthesis(line))
				throw new Exception("Parenthesis error");
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
			case Equation:
				String left = line.split("=")[0], right = line.split("=")[1];
				if (!variableDictionary.containsValue(left)) {
					outputList.add("%" + left + " = alloca i32");
				}
				Equation(right);
				outputList.add("store i32 %" + (lineCounter - 1) + ", i32* %" + left);
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

	private static String Equation(String expression) throws Exception {

		if(expression.isBlank()) throw new Exception("Unexpected expression");
		
		while (expression.contains("(") || expression.contains(")")) {
			int begin = expression.lastIndexOf('('), end = expression.indexOf(')', begin);
			String inside = expression.substring(begin + 1, end);
			expression = expression.replace("(" + inside + ")", Equation(inside));
		}

		ArrayList<String> lineOfNumbers = new ArrayList<String>(
				Arrays.asList(expression.replaceAll("[+*/-]", " ").replaceAll(" +", " ").strip().split(" ")));

		List<String> lineOfOperations = new ArrayList<String>(
				Arrays.asList(expression.replaceAll("[^-+*/]", "").strip().split("")));

		var operationLength = lineOfOperations.size();

		while (operationLength > 0) {
			int i = 0;

			if (lineOfOperations.get(i) == "+" || lineOfOperations.get(i) == "-") {
				if (lineOfOperations.get(i + 1) == "*" || lineOfOperations.get(i + 1) == "/") {
					operationLength = Calculation(i + 1, lineOfNumbers, lineOfOperations);

				} else {
					operationLength = Calculation(i, lineOfNumbers, lineOfOperations);
				}
			} else {
				operationLength = Calculation(i, lineOfNumbers, lineOfOperations);
			}
		}
		return "%" + lineCounter;
	}

	private static int Calculation(int index, ArrayList<String> lineOfNumbers, List<String> lineOfOperations)
			throws Exception {
		String operation = lineOfOperations.get(index);
		String operand1 = lineOfNumbers.get(index);
		String operand2 = lineOfNumbers.get(index + 1);
		lineOfNumbers.set(index + 1, "%" + PrintCalculation(operand1, operand2, operation));
		lineOfNumbers.remove(index);
		lineOfOperations.remove(index);
		return lineOfOperations.size();
	}

	private static int PrintCalculation(String operand1, String operand2, String operation) throws Exception {
		operand1 = checkVariableExist(operand1);
		operand2 = checkVariableExist(operand2);
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
			if (Double.parseDouble(operand1) == 0)
				throw new Exception("Divided by zero");
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

	private static boolean checkParenthesis(String line) {
		int opened = 0;
		for (int i = 0; i < line.length(); i++) {
			if (line.charAt(i) == '(')
				opened++;
			else if (line.charAt(i) == ')') {
				if (opened == 0) // means that all parentheses are "closed" yet
					return false;
				opened--;
			}
		}
		return opened == 0;
	}

	private static EntryType getEntryType(String inputLine) {

		if (inputLine.contains("=")) {
			for (int i = 0; i < operations.length; i++) {
				if (inputLine.contains(operations[i])) {
					var right = inputLine.split("=")[1].strip();
					if (((right.charAt(0) == '-') || (right.charAt(0) == '+'))
							&& (!right.substring(1).contains("*") || !right.substring(1).contains("+")
									|| !right.substring(1).contains("-") || !right.substring(1).contains("/"))) {
						return EntryType.DirectAssignment;

					}
					return EntryType.Equation;
				}
			}
			return EntryType.DirectAssignment;
		}
		return EntryType.Print;
	}

	private static void DirectAssignments(String inputLine) {
		String[] input = inputLine.split("=", 2);
		String variable = input[0].strip();
		String value = input[1].strip();
		if (!variableDictionary.containsValue(variable)) {
			variableDictionary.put(variable, variable);
			outputList.add("%" + variable + " = alloca i32");
			outputList.add("store i32 " + value + " , i32* %" + variable);
		}
	}

	private static String checkVariableExist(String variable) {
		if (variableDictionary.containsKey(variable)) {
			outputList.add("%" + lineCounter + " = load i32* %" + variable);
			variableDictionary.replace(variable, "%" + lineCounter);
			variable = variableDictionary.get(variable);
			lineCounter++;
		}
		return variable;
	}
}
