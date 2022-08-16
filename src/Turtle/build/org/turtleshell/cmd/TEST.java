/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.turtleshell.cmd;

import java.io.IOException;
import org.turtleshell.TEnv;
import org.turtleshell.strings.TString;
import org.turtleshell.tasks.TTask;

/**
 * A task for comparing the value of two strings.
 **
 * This task compares the values of two turtle strings and returns zero (0) if the
 * comparison fails, and one (1) if the comparison succeeds.  The comparisons available are:
 *
 * EQ: equal
 * LE: less than, OR equal to
 * LT: less than, but NOT equal to
 * GE: greater than, OR equal to
 * GT: greater than, but NOT equal to
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public class TEST extends TTask {
    /**
     * The first string to compare
     */
    private TString  test1;
    /**
     * The second string to compare
     */
    private TString  test2;
    /**
     * The type of comparison to perform:
     *
     * EQ: equal
     * LE: less than, OR equal to
     * LT: less than, but NOT equal to
     * GE: greater than, OR equal to
     * GT: greater than, but NOT equal to
     */
    private TestType     comparison;
    /**
     * An enumeration of all of the comparisons available:
     * 
     * EQ: equal
     * LE: less than, OR equal to
     * LT: less than, but NOT equal to
     * GE: greater than, OR equal to
     * GT: greater than, but NOT equal to
     */
    public static enum  TestType { EQ, LT, GT, LE, GE; };

    /**
     * Creates a new comparison task
     **
     * @param comparison the type of comparison to perform
     * @param test1 the first value to test
     * @param test2 the second value, the value to test the first value against
     */
    public TEST(TestType comparison, TString test1, TString test2) {
        this.comparison = comparison;
        this.test1 = test1;
        this.test2 = test2;
    }

    /**
     * Performs the comparison
     **
     * The comparison will be performed based on Java's internal compareTo method.
     * The objects being compared will first be compared as strings.  If, however,
     * both objects are numbers, the comparison will be switched to a numerical
     * comparison.  The objects will be tested by testNumber() to determine whether
     * they are strings or numbers.
     **
     * @param env the Turtle shell environment to read any necessary variable values from.
     * @return one (1) if the comparison succeeds, zero (0) if the comparison fails
     */
    @Override
    public int exec(TEnv env) {
        boolean result = false;
        String intermediate1 = test1.getValue(env);
        String intermediate2 = test2.getValue(env);
        Comparable result1 = intermediate1;
        Comparable result2 = intermediate2;

        if (testNumber(intermediate1) && testNumber(intermediate2)) {
            result1 = Integer.parseInt(intermediate1);
            result2 = Integer.parseInt(intermediate2);
        }

        switch (comparison) {
            case GT:
                result = result1.compareTo(result2) < 1;
                break;
            case LT:
                result = result1.compareTo(result2) > 1;
                break;
            case EQ:
                result = result1.compareTo(result2) == 0;
                break;
            case GE:
                result = result1.compareTo(result2) <= 1;
                break;
            case LE:
                result = result1.compareTo(result2) >= 1;
                break;
        }
        return (result ? 1 : 0);
    }


    /**
     * Appends data to the standard input of the task
     **
     * @param csq the character sequence to append
     * @return the appendable object to append to.
     * @throws IOException any IOExceptions that occur while appending the data
     */
    @Override
    public Appendable append(CharSequence csq) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Appends data to the standard input of the task
     **
     * @param csq the character sequence to append
     * @param start the start location within the stream to append
     * @param end the end location within the stream to append
     * @return the appendable object to append to.
     * @throws IOException any IOExceptions that occur while appending the data
     */
    @Override
    public Appendable append(CharSequence csq, int start, int end) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Appends data to the standard input of the task
     **
     * @param c the character to append
     * @return the appendable object to append to.
     * @throws IOException any IOExceptions that occur while appending the data
     */
    @Override
    public Appendable append(char c) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Used to determine whether the current command is running and able to accept
     * input from another command, or from a file
     **
     * @return whether the command is able to accept input
     */
    @Override
    public boolean isReady() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}





/************
 * OLD CODE *
 ************/
/**
 * Parses a conditional phrase
 **
 * This method will create a compare object, which will compare two strings
 * based on a given comparator method.  The comparisons available are:
 *
 *  =   equal
 *  <=  less than, OR equal to
 *  <   less than, but NOT equal to
 *  >   greater than, OR equal to
 *  >=  greater than, but NOT equal to
 *
 * The comparison will be performed based on Java's internal compareTo method.
 * The objects being compared will first be compared as strings.  If, however,
 * both objects are numbers, the comparison will be switched to a numerical
 * comparison.  The objects will be tested by testNumber() to determine whether
 * they are strings or numbers.
 **
 * @return the turtle shell task object which performs the condition comparison
 */
/*TTask Condition() :
{
    TString               test   = null;
    TString               value  = null;
    CompareTask.TestType  type   = CompareTask.TestType.EQ;
}
{
    <OPENB>

    ( test  = Strn() )

    (
      <LT>  { type = CompareTask.TestType.LT; }
    | <GT>  { type = CompareTask.TestType.GT; }
    | <TEQ> { type = CompareTask.TestType.EQ; }
    | <LE>  { type = CompareTask.TestType.LE; }
    | <GE>  { type = CompareTask.TestType.GE; }
    )

    ( value = Strn() )

    <CLOSEB>

    { return new CompareTask(type, test, value); }
}*/
