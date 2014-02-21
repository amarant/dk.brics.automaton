package dk.brics.automaton;

import java.util.List;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: amarant
 * Date: 21/02/14
 * Time: 17:43
 * To change this template use File | Settings | File Templates.
 */
public class Xeger {
    private int AllExceptAnyString = RegExp.ALL & ~RegExp.ANYSTRING;

    private Automaton automaton;
    private Random random;
    private int _minLength;
    private int _maxLength;
    private RegExp regExp;


    /// <summary>
    /// Initializes a new instance of the <see cref="Xeger" /> class.
    /// </summary>
    /// <param name="regex">The regex.</param>
    /// <param name="random">The random.</param>
    /// <param name="minLength">The minimum length.</param>
    /// <param name="maxLength">The maximum length.</param>
    /// <exception cref="System.ArgumentNullException">
    /// regex
    /// or
    /// random
    /// </exception>
    public Xeger(String regex, Random random, int minLength, int maxLength)
    {
        if (regex == null)
        {
            throw new IllegalArgumentException("regex");
        }

        if (random == null)
        {
            throw new IllegalArgumentException("random");
        }

        regExp = new RegExp(regex, AllExceptAnyString);
        this.random = random;
        _minLength = minLength;
        _maxLength = maxLength;
    }

    /// <summary>
    /// Initializes a new instance of the <see cref="Xeger"/> class.
    /// </summary>
    /// <param name="regex">The regex.</param>
    /// <param name="random">The random.</param>
    public Xeger(String regex, Random random)
    {
        this(regex, random, -1, -1);
    }

    /// <summary>
    /// Initializes a new instance of the <see cref="Xeger"/> class.
    /// </summary>
    /// <param name="regex">The regex.</param>
    public Xeger(String regex)
    {
        this(regex, new Random());
    }

    /// <summary>
    /// Add a regular expression that will make an intersection with the current one.
    /// </summary>
    /// <param name="regex">The regex.</param>
    public void AddIntersection(String regex)
    {
        RegExp exp = new RegExp(regex, AllExceptAnyString);
        this.regExp = RegExp.makeIntersection(regExp, exp);
        this.automaton = null;
    }

    /// <summary>
    /// Generates a random String that is guaranteed to match the regular expression passed to the constructor.
    /// </summary>
    /// <returns></returns>
    public String generate()
    {
        if (automaton == null)
        {
            this.automaton = regExp.toAutomaton();
        }

        StringBuilder builder = new StringBuilder();
        this.Generate(builder, automaton.initial);
        String gen = builder.toString();
        if (gen.startsWith("^")) {
            gen = gen.substring(1);
        }
        if (gen.endsWith("$")) {
            gen = gen.substring(0,gen.length()-1);
        }
        return gen;
    }

    /// <summary>
    /// Generates a random number within the given bounds.
    /// </summary>
    /// <param name="min">The minimum number (inclusive).</param>
    /// <param name="max">The maximum number (inclusive).</param>
    /// <param name="random">The object used as the randomizer.</param>
    /// <returns>A random number in the given range.</returns>
    private static int GetRandomInt(int min, int max, Random random)
    {
        int dif = max - min;
        double number = random.nextDouble();
        return min + (int)Math.round(number * dif);
    }

    private void Generate(StringBuilder builder, State state)
    {
        List<Transition> transitions = state.getSortedTransitions(true);
        if (transitions.isEmpty())
        {
            if (!state.accept)
            {
                throw new UnsupportedOperationException("state");
            }

            return;
        }

        int nroptions = state.accept ? transitions.size() : transitions.size() - 1;
        int minOption = 0;
        if (_minLength < 0
            && builder.length() < _minLength)
        {
            minOption = 1;
        }
        if (_maxLength < 0
            && builder.length() == _maxLength)
        {
            minOption = 0;
            nroptions = 0;
        }
        int option = Xeger.GetRandomInt(minOption, nroptions, random);
        if (state.accept && option == 0)
        {
            // 0 is considered stop.
            return;
        }

        // Moving on to next transition.
        Transition transition = transitions.get(option - (state.accept ? 1 : 0));
        this.AppendChoice(builder, transition);
        Generate(builder, transition.to);
    }

    private void AppendChoice(StringBuilder builder, Transition transition)
    {
        char c = (char)Xeger.GetRandomInt(transition.min, transition.max, random);
        builder.append(c);
    }
}
