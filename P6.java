import java.io.*;

import java_cup.runtime.*;
///////////////////////////////////////////////////////////////////////////////
//ALL STUDENTS COMPLETE THESE SECTIONS
//Title:            P6.java
//Files:            ast.java DuplicateSymException.java EmptySymTableException.java 
//		ErrMsg.java SemSym.java SymTable.java Type.java harambe.cup harambe.grammar 
//		harambe.jlex Makefile test.ha. typeErrors.ha typeErrorsExp.ha 
//Semester:         CS536 Fall 2016
//
//Author:           Nic Harsy
//Email:            nharsy@wisc.edu
//CS Login:         nicolas
//Lecturer's Name:  Loris D'Antoni
//
////////////////////PAIR PROGRAMMERS COMPLETE THIS SECTION ////////////////////
//
//CHECK ASSIGNMENT PAGE TO see IF PAIR-PROGRAMMING IS ALLOWED
//If pair programming is allowed:
//1. Read PAIR-PROGRAMMING policy (in cs302 policy) 
//2. choose a partner wisely
//3. REGISTER THE TEAM BEFORE YOU WORK TOGETHER 
//a. one partner creates the team
//b. the other partner must join the team
//4. complete this section for each program file.
//
//Pair Partner:     Alex Valaitis
//Email:            valaitis@wisc.edu
//CS Login:         valaitis
//Lecturer's Name:  Loris D'Antoni
//
////////////////////STUDENTS WHO GET HELP FROM OTHER THAN THEIR PARTNER //////
//must fully acknowledge and credit those sources of help.
//Instructors and TAs do not have to be credited here,
//but tutors, roommates, relatives, strangers, etc do.
//
//Persons:          Identify persons by name, relationship to you, and email.
//Describe in detail the the ideas and help they provided.
//
//Online sources:   avoid web searches to solve your problems, but if you do
//search, be sure to include Web URLs and description of 
//of any information you find.
////////////////////////////80 columns wide //////////////////////////////////

/**
 * Main program to test the parser.
 * 
 * There should be 2 command-line arguments: 1. the file to be parsed 2. the
 * output file into which the AST built by the parser should be unparsed The
 * program opens the two files, creates a scanner and a parser, and calls the
 * parser. If the parse is successful, the AST is unparsed.
 */
public class P6 {
	FileReader inFile;
	private PrintWriter outFile;
	private static PrintStream outStream = System.err;

	public static final int RESULT_CORRECT = 0;
	public static final int RESULT_SYNTAX_ERROR = 1;
	public static final int RESULT_TYPE_ERROR = 2;
	public static final int RESULT_OTHER_ERROR = -1;

	/**
	 * P6 constructor for client programs and testers. Note that users MUST
	 * invoke {@link setInfile} and {@link setOutfile}
	 */
	public P6() {
	}

	/**
	 * If we are directly invoking P6 from the command line, this is the command
	 * line to use. It shouldn't be invoked from outside the class (hence the
	 * private constructor) because it
	 * 
	 * @param args
	 *            command line args array for [<infile> <outfile>]
	 */
	private P6(String[] args) {
		// Parse arguments
		if (args.length < 2) {
			String msg = "please supply name of file to be parsed"
					+ "and name of file for unparsed version.";
			pukeAndDie(msg);
		}

		try {
			setInfile(args[0]);
			setOutfile(args[1]);
		} catch (BadInfileException e) {
			pukeAndDie(e.getMessage());
		} catch (BadOutfileException e) {
			pukeAndDie(e.getMessage());
		}
	}

	/**
	 * Source code file path
	 * 
	 * @param filename
	 *            path to source file
	 */
	public void setInfile(String filename) throws BadInfileException {
		try {
			inFile = new FileReader(filename);
		} catch (FileNotFoundException ex) {
			throw new BadInfileException(ex, filename);
		}
	}

	/**
	 * Text file output
	 * 
	 * @param filename
	 *            path to destination file
	 */
	public void setOutfile(String filename) throws BadOutfileException {
		try {
			outFile = new PrintWriter(filename);
		} catch (FileNotFoundException ex) {
			throw new BadOutfileException(ex, filename);
		}
	}

	/**
	 * Perform cleanup at the end of parsing. This should be called after both
	 * good and bad input so that the files are all in a consistent state
	 */
	public void cleanup() {
		if (inFile != null) {
			try {
				inFile.close();
			} catch (IOException e) {
				// At this point, users already know they screwed
				// up. No need to rub it in.
			}
		}
		if (outFile != null) {
			// If there is any output that needs to be
			// written to the stream, force it out.
			outFile.flush();
			outFile.close();
		}
	}

	/**
	 * Private error handling method. Convenience method for
	 * 
	 * @link pukeAndDie(String, int) with a default error code
	 * @param error
	 *            message to print on exit
	 */
	private void pukeAndDie(String error) {
		pukeAndDie(error, -1);
	}

	/**
	 * Private error handling method. Prints an error message
	 * 
	 * @link pukeAndDie(String, int) with a default error code
	 * @param error
	 *            message to print on exit
	 */
	private void pukeAndDie(String error, int retCode) {
		outStream.println(error);
		cleanup();
		System.exit(-1);
	}

	/**
	 * the parser will return a Symbol whose value field is the translation of
	 * the root nonterminal (i.e., of the nonterminal "program")
	 * 
	 * @return root of the CFG
	 */
	private Symbol parseCFG() {
		try {
			parser P = new parser(new Yylex(inFile));
			return P.parse();
		} catch (Exception e) {
			return null;
		}
	}

	public int process() {
		Symbol cfgRoot = parseCFG();

		ProgramNode astRoot = (ProgramNode) cfgRoot.value;
		if (ErrMsg.getErr()) {
			return P6.RESULT_SYNTAX_ERROR;
		}
		astRoot.nameAnalysis(); // perform name analysis

		astRoot.typeCheck();

		astRoot.unparse(outFile, 0);
		return P6.RESULT_CORRECT;
	}

	public void run() {
		int resultCode = process();
		if (resultCode == RESULT_CORRECT) {
			cleanup();
			return;
		}

		switch (resultCode) {
		case RESULT_SYNTAX_ERROR:
			pukeAndDie("Syntax error", resultCode);
		case RESULT_TYPE_ERROR:
			pukeAndDie("Type checking error", resultCode);
		default:
			pukeAndDie("Type checking error", RESULT_OTHER_ERROR);
		}
	}

	private class BadInfileException extends Exception {
		private static final long serialVersionUID = 1L;
		private String message;

		public BadInfileException(Exception cause, String filename) {
			super(cause);
			this.message = "Could not open " + filename + " for reading";
		}

		@Override
		public String getMessage() {
			return message;
		}
	}

	private class BadOutfileException extends Exception {
		private static final long serialVersionUID = 1L;
		private String message;

		public BadOutfileException(Exception cause, String filename) {
			super(cause);
			this.message = "Could not open " + filename + " for reading";
		}

		@Override
		public String getMessage() {
			return message;
		}
	}

	public static void main(String[] args) {
		P6 instance = new P6(args);
		instance.run();
	}
}
