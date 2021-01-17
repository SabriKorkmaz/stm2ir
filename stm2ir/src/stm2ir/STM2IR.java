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

	private LinkedList<String> outputList = new LinkedList<String>();

	public Map<String, String> variableDictionary = new HashMap<String, String>();

	private EntryType entryType;

	public int lineCounter = 0;

	private String[] operations = { "+", "-", "/", "*" };

	public void main(String[] args) throws Exception {

		outputList.add("; ModuleID = 'stm2ir'");
		outputList.add("declare i32 @printf(i8*, ...)");
		outputList.add("@print.str = constant [4 x i8] c\"%d\\0A\\00\"");
		outputList.add("define i32 @main() {");
		// TODO: Get project file folder static variable
		Scanner input = new Scanner(new File(args[0]), "UTF-8");
		while (input.hasNextLine()) {
			String line = input.nextLine().replaceAll("\\s+", "");
			if (!checkParenthesis(line))
				throw new Exception("Parenthesis error");
			switch (getEntryType(line)) {
			case DirectAssignment:
				directAssignment(line);
				break;
			case Print:
				outputList.add("%" + lineCounter + " = load i32* %" + line);
				outputList.add(
						"call i32 (i8*, ...)* @printf(i8* getelementptr ([4 x i8]* @print.str, i32 0, i32 0), i32 %"
								+ lineCounter + " )");
				break;
			case Equation:
				String left = "", right = "";
				if (line.contains("=")) {
					left = line.split("=")[0];
					right = line.split("=")[1];
					if (!variableDictionary.containsKey(left)) {
						outputList.add("%" + left + " = alloca i32");
						variableDictionary.put(left, "%" + lineCounter);
					}
					outputList.add("store i32 %" + (lineCounter) + ", i32* %" + left);
					variableDictionary.replace(left, "%" + String.valueOf(lineCounter));
				} else {
					right = line;
				}

				equation(right);

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

	public String equation(String expression) throws Exception {

		if (expression == null)
			throw new Exception("Unexpected expression");

		while (expression.contains("(") || expression.contains(")")) {
			int begin = expression.lastIndexOf('('), end = expression.indexOf(')', begin);
			String inside = expression.substring(begin + 1, end);
			expression = expression.replace("(" + inside + ")", equation(inside));
		}

		ArrayList<String> lineOfNumbers = new ArrayList<String>(
				Arrays.asList(expression.replaceAll("[+*/-]", " ").replaceAll(" +", " ").strip().split(" ")));

		List<String> lineOfOperations = new ArrayList<String>(
				Arrays.asList(expression.replaceAll("[^-+*/]", "").strip().split("")));

		var operationLength = lineOfOperations.size();

		while (operationLength > 0) {
			int i = 0;
			lineCounter++;
			if (lineOfOperations.get(i).equals("+") || lineOfOperations.get(i).equals("-")) {
				if (!(i + 1 >= lineOfOperations.size())
						&& (lineOfOperations.get(i + 1).equals("*") || lineOfOperations.get(i + 1).equals("/"))) {
					operationLength = calculation(i + 1, lineOfNumbers, lineOfOperations);

				} else {
					operationLength = calculation(i, lineOfNumbers, lineOfOperations);
				}
			} else {
				operationLength = calculation(i, lineOfNumbers, lineOfOperations);
			}

		}
		return "%" + lineCounter;
	}

	public int calculation(int index, ArrayList<String> lineOfNumbers, List<String> lineOfOperations)
			throws Exception {
		String operation = lineOfOperations.get(index);
		String operand1 = lineOfNumbers.get(index);
		String operand2 = lineOfNumbers.get(index + 1);
		lineOfNumbers.set(index + 1, "%" + String.valueOf(printCalculation(operand1, operand2, operation)));
		lineOfNumbers.remove(index);
		lineOfOperations.remove(index);
		return lineOfOperations.size();
	}

	public int printCalculation(String operand1, String operand2, String operation) throws Exception {
		operand1 = checkVariableExist(operand1);
		operand2 = checkVariableExist(operand2);

		switch (operation) {
		case ("*"):
			outputList.add("%" + lineCounter + " = mul i32 " + operand1 + " , " + operand2);
			return lineCounter;
		case ("+"):
			outputList.add("%" + lineCounter + " = add i32 " + operand1 + " , " + operand2);
			return lineCounter;
		case ("/"):
			if (Double.parseDouble(operand1) == 0)
				throw new Exception("Divided by zero");
			outputList.add("%" + lineCounter + " = udiv i32 " + operand1 + " , " + operand2);
			return lineCounter;
		case ("-"):
			outputList.add("%" + lineCounter + " = sub i32 " + operand1 + " , " + operand2);
			return lineCounter;
		default:
			return 0;
		}
	}

	public boolean checkParenthesis(String line) {
		int opened = 0;
		for (int i = 0; i < line.length(); i++) {
			if (line.charAt(i) == '(')
				opened++;
			else if (line.charAt(i) == ')') {
				if (opened == 0)
					return false;
				opened--;
			}
		}
		return opened == 0;
	}

	public EntryType getEntryType(String inputLine) {

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
		} else {
			for (int i = 0; i < operations.length; i++) {
				if (inputLine.contains(operations[i])) {
					return EntryType.Equation;
				}
			}
			return EntryType.Print;
		}

	}

	public void directAssignment(String inputLine) throws Exception {
		String[] input = inputLine.split("=",2);
		if (input.length != 2)
			throw new Exception("Unhandled expression");
		String variable = input[0].strip();
		String value = input[1].strip();
		if (!variableDictionary.containsKey(variable)) {
			variableDictionary.put(variable, "%" + variable);
			outputList.add("%" + variable + " = alloca i32");
			outputList.add("store i32 " + value + " , i32* %" + variable);
		}
	}

	public String checkVariableExist(String variable) {
		if (variableDictionary.containsKey(variable)) {
			outputList.add("%" + lineCounter + " = load i32* %" + variable);
			variableDictionary.replace(variable, "%" + String.valueOf(lineCounter));
			variable = variableDictionary.get(variable);
			lineCounter++;
		}
		return variable;
	}
}
