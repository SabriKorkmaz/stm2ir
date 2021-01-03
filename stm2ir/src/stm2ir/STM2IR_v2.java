package stm2ir;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class STM2IR_v2 {

	private static LinkedList<String> outputList = new LinkedList<String>();

	private static Map<String, String> variableDictionary = new HashMap<String, String>();

	private static int entryType;

	private static String inputLine;

	private static String[] operations = { "+", "-", "/", "*" };

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub

		InputStreamReader isr = new InputStreamReader(System.in);
		BufferedReader br = new BufferedReader(isr);
		inputLine = br.readLine();

		entryType = getEntryType();

		System.out.print(entryType + "\n");

		if (entryType == 1) {
			generateOutputForDirectAssignments();
		}

		Object[] output = outputList.toArray();
		for (int i = 0; i < outputList.size(); i++) {
			System.out.print(output[i] + "\n");
		}

		System.out.print(variableDictionary);

	}

	private static void generateOutputForDirectAssignments() {
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

	private static int getEntryType() {
		// TODO Auto-generated method stub
		if (!inputLine.contains("=")) {
			// if the statement doesn't include = , then this is a variable to be printed
			return 3;
		} else if (inputLine.contains("=")) {
			for (int i = 0; i < operations.length; i++) {
				if (inputLine.contains(operations[i])) {
					// if returns 2, there is a computation to do
					return 2;
				}
			}
			// if returns 3, this is an assignment of a variable
			return 1;

		}
		return 0;
	}


}
