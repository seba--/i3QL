package unisson.prolog.utils;

import java.util.ArrayList;

public class ISOPrologStringConversion
{

    public static final String comma = ", ";

    private static final String[] emptyArgs = new String[0];

    public static String getFunctor(String compound)
    {
        int parens = compound.indexOf('(');
        if (parens == -1)
            return compound;
        return compound.substring(0, parens);
    }

    public static String[] getArgs(String compound)
    {
        int parens = compound.indexOf('(');
        if (parens == -1)
            return emptyArgs;

        String terms = compound.substring(parens + 1, compound.length() - 1);
        String next = nextArg(terms);
        ArrayList<String> values = new ArrayList<String>(1);
        if (terms.indexOf(',', next.length()) > 0)
            terms = terms.substring(next.length() + comma.length());
        else
            terms = terms.substring(next.length());
        values.add(next);
        while (terms.length() > 0) {
            next = nextArg(terms);
            if (next == null)
                throw new IllegalStateException(compound + " is not a correct term");
            values.add(next);
            if (terms.indexOf(',', next.length()) > 0)
                terms = terms.substring(next.length() + comma.length());
            else
                terms = terms.substring(next.length());

        }
        String[] result = new String[values.size()];
        return values.toArray(result);
    }


    private static String nextArg(String argList)
    {
        int colon = argList.indexOf(',');
        int openParens = argList.indexOf('(');
        int openBracks = argList.indexOf('[');
        int closeBracks = argList.indexOf(']');
        int closeParens = argList.indexOf(')');

        if (colon == -1)
            return argList;
        // the colon is after any parenthesis or brackets
        if (!inRange(colon, openParens, closeParens) && !inRange(colon, openBracks, closeBracks))
            return argList.substring(0, colon);

        // we need to find matching closing parenthesis
        int parensCount = 0;
        int bracksCount = 0;
        int close = 0;
        boolean parseParens = false;
        boolean parseBrackets = false;
        if (openParens >= 0 && inRange(openParens, 0, colon)) {
            parseParens = true;
            parensCount++;
        } else {
            openParens = -1;
            closeParens = -1;
        }

        if (openBracks >= 0 && inRange(openBracks, 0, colon)) {
            parseBrackets = true;
            bracksCount++;
        } else {
            openBracks = -1;
            closeBracks = -1;
        }

        while (true) {
            if (bracksCount == 0 && parensCount == 0)
                break;
            // parse all parenthesis to make sure the closing brackets delimit the end
            if (bracksCount > 0 && inRange(
                    openParens,
                    openBracks,
                    closeBracks
            ) || (parensCount > 0 && bracksCount == 0)) {
                parseParens = true;
                parseBrackets = false;
                close = closeBracks;
            }
            // parse all brackets to make sure the closing parenthesis delimit the end
            if (parensCount > 0 && inRange(
                    openBracks,
                    openParens,
                    closeParens
            ) || (bracksCount > 0 && parensCount == 0)) {
                parseParens = false;
                parseBrackets = true;
                close = closeParens;
            }
            if (parseParens) {
                openParens = argList.indexOf('(', openParens + 1);
                close = closeParens;
                if (openParens >= 0 && (openParens < closeParens || openParens < closeBracks))
                    parensCount++;

                closeParens = argList.indexOf(')', closeParens + 1);
                parensCount--;
                parseParens = false;
            }
            if (parseBrackets) {
                openBracks = argList.indexOf('[', openBracks + 1);
                close = closeBracks;
                if (openBracks >= 0 && (openBracks < closeBracks || openBracks < closeParens))
                    bracksCount++;


                closeBracks = argList.indexOf(']', closeBracks + 1);
                bracksCount--;
                parseBrackets = false;
            }
        }
        return argList.substring(0, close + 1);
    }

    private static boolean inRange(int index, int start, int end)
    {
        if (start == -1 || end == -1)
            return false;
        if (start <= index && index <= end)
            return true;
        return false;
    }

    /**
     * quotes the string with atomic quotes
     *
     * @param s
     * @return
     */
    public static String asAtomic(String s)
    {
        return "'" + s + "'";
    }


    /**
     * un-quotes the string with atomic quotes
     * Warning: No check is performed, the first and last characters are assumed to be quotes
     *
     * @param atomic
     * @return
     */
    public static String atomicValue(String atomic)
    {
        return atomic.substring(1, atomic.length() - 1);
    }


    public static String asList(String[] list)
    {
        if (list.length == 0)
            return "[]";
        StringBuilder result = new StringBuilder("[" + list[0]);

        for (int i = 1; i < list.length; i++) {
            String entry = list[i];
            result.append(comma);
            result.append(entry);
        }
        return result.append("]").toString();
    }

}
