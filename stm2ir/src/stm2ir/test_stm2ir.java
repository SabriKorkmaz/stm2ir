package stm2ir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class test_stm2ir {

	@Test
	void Equation_NullExpression_ThrowException() {
		try {
			var stm = new STM2IR();
			stm.equation(null);
		} catch (Exception e) {
			String expectedMessage = "Unexpected expression";
			assertEquals(expectedMessage, e.getMessage());
		}
	}

	@Test
	void Equation_SpecifiedExpression_ThrowException() throws Exception {
		var stm = new STM2IR();
		var lineCounter = stm.lineCounter;
		stm.equation("X1=X4+(2+4)");
		assertEquals(lineCounter+2,stm.lineCounter);
	}

	@Test
	void CheckVariableExist_X1Exist_ReturnValue() throws Exception {
		var stm = new STM2IR();
		stm.lineCounter = 1;
		stm.directAssignment("X1 = 2");
		
		assertEquals(stm.checkVariableExist("X1"),"%1");
	}

	@Test
	void CheckVariableExist_X1NotExist_ReturnVariable() {
		var stm = new STM2IR();
		assertEquals(stm.checkVariableExist("X1"),"X1");
	}

	@Test
	void DirectAssignments_X1Equals1_VariableDictornaryValue() throws Exception {
		var stm = new STM2IR();
		stm.directAssignment("x2=2");
		assertEquals(stm.variableDictionary.get("x2"),"%x2");
	}

	@Test
	void DirectAssignments_NotDirectAssingmentExpression_Exception() throws Exception {
		try {
			var stm = new STM2IR();
			stm.directAssignment("x2=2=3");
			
		} catch (Exception e) {
			String expectedMessage = "Unhandled expression";
			assertEquals(expectedMessage, e.getMessage());
		}
		
	}

	@Test
	void DirectAssignments_ExistInVariableDictionary_AddedVariableDictionary() throws Exception {
		var stm = new STM2IR();
		stm.directAssignment("x2=2");
		stm.lineCounter++;
		assertEquals(stm.variableDictionary.get("x2"),"%x2");
	}

	@Test
	void GetEntryType_TypeEquation_ReturnEquaiton() {
		var stm = new STM2IR();
		var type = stm.getEntryType("x2=x1 + 2");
		assertEquals(type,EntryType.Equation);
	}

	@Test
	void GetEntryType_TypeDirectAssignment_DirectAssignment() {
		var stm = new STM2IR();
		var type = stm.getEntryType("x2= 2");
		assertEquals(type,EntryType.DirectAssignment);
	}

	@Test
	void GetEntryType_TypePrint_Print() {
		var stm = new STM2IR();
		var type = stm.getEntryType("x2");
		assertEquals(type,EntryType.Print);
	}

	@Test
	void CheckParenthesis_ProperParenthesis_True() {
		var stm = new STM2IR();
		var result = stm.checkParenthesis("4 + 2 + (4+9)");
		assertEquals(result,true);
	}

	@Test
	void CheckParenthesis_NotProperParenthesis_False() {
		var stm = new STM2IR();
		var result = stm.checkParenthesis("(4 + 2 + (4+9)");
		assertEquals(result,false);
	}

	@Test
	void PrintCalculation_Operand1X2_Operand2Zero_Exception() {
		try {
			var stm = new STM2IR();
			stm.printCalculation("x2","y","+");
			
		} catch (Exception e) {
			String expectedMessage = "Divided by zero";
			assertEquals(expectedMessage, e.getMessage());
		}
	}
}
