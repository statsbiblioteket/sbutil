package dk.statsbiblioteket.util.reader;

import junit.framework.TestCase;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class VerbatimMatcherTest extends TestCase {

    public void testSimple() {
        CollectingMatcher matcher = new CollectingMatcher();
        matcher.addRule("World");
        assertMatches(matcher, "Hello World!",
                      "World");
    }

    public void testMulti() {
        CollectingMatcher matcher = new CollectingMatcher();
        matcher.addRules("World", "Cruel");
        assertMatches(matcher, "Hello Cruel Worlds",
                      "Cruel", "World");
    }

    public void testOverlap() {
        CollectingMatcher matcher = new CollectingMatcher();
        matcher.addRules("London", "East London");
        assertMatches(matcher, "Welcome to East London",
                      "East London", "London");
    }

    public void testNone() {
        CollectingMatcher matcher = new CollectingMatcher();
        matcher.addRules("London", "East London");
        assertMatches(matcher, "This is somewhere else");
    }

    public void testPerfect() {
        CollectingMatcher matcher = new CollectingMatcher();
        matcher.addRules("London");
        assertMatches(matcher, "London",
                      "London");
    }

    public void testDelimiter1() {
        CollectingMatcher matcher = new CollectingMatcher();
        Pattern delimiter = Pattern.compile(" +");
        matcher.addRules("London", "East London");
        assertMatches(matcher, delimiter, "East London is burning",
                      "East London", "London");
    }

    public void testDelimiter2() {
        CollectingMatcher matcher = new CollectingMatcher();
        Pattern delimiter = Pattern.compile(" +");
        matcher.addRules("London", "East-London");
        assertMatches(matcher, delimiter, "East-London is burning",
                      "East-London");
    }

    public void testDelimiter3() {
        CollectingMatcher matcher = new CollectingMatcher();
        Pattern delimiter = Pattern.compile("-");
        matcher.addRules("London", "East-London");
        assertMatches(matcher, delimiter, "Look, East-London is burning",
                      "London");
    }

    public void testNoRules() {
        CollectingMatcher matcher = new CollectingMatcher();
        assertMatches(matcher, "London");
    }

    public void testNoSource() {
        CollectingMatcher matcher = new CollectingMatcher();
        matcher.addRules("London");
        assertMatches(matcher, "");
    }

    public void testGetExistingNode() {
        CollectingMatcher matcher = new CollectingMatcher();
        matcher.addRules("London");
        assertNotNull("There should be a Node for 'London'", matcher.getNode("London", false));
    }

    public void testGetNonExistingNode() {
        CollectingMatcher matcher = new CollectingMatcher();
        matcher.addRules("London");
        assertNull("There should not be a Node for 'France'", matcher.getNode("France", false));
    }

    public void testAutoCreateNode() {
        CollectingMatcher matcher = new CollectingMatcher();
        matcher.addRules("London");
        assertNotNull("A Node for 'France' should be created", matcher.getNode("France", true));
        assertNotNull("The newly created Node for 'France' should be available", matcher.getNode("France", false));
    }

    public void testPayload() {
        CollectingMatcher matcher = new CollectingMatcher();
        matcher.addRule("London", "old");
        matcher.addRule("East London", "medium");
        assertMatchesPayload(matcher, "East London is burning",
                             Arrays.asList("East London", "London"), Arrays.asList("medium", "old"));
    }

    public void testPartialPayloads() {
        CollectingMatcher matcher = new CollectingMatcher();
        matcher.addRule("London", "old");
        matcher.addRule("East London");
        assertMatchesPayload(matcher, "East London is burning",
                             Arrays.asList("East London", "London"), Arrays.asList(null, "old"));
    }

    public void testMatchShortest() {
        CollectingMatcher matcher = new CollectingMatcher();
        matcher.addRules("East", "London", "East London");
        matcher.setMatchMode(VerbatimMatcher.MATCH_MODE.shortest);
        matcher.setSkipMatching(true);
        assertMatches(matcher, "Come visit East London in the fall",
                      "East", "London");

    }

    public void testMatchLongest() {
        CollectingMatcher matcher = new CollectingMatcher();
        matcher.addRules("East", "London", "East London");
        matcher.setMatchMode(VerbatimMatcher.MATCH_MODE.longest);
        matcher.setSkipMatching(true);
        assertMatches(matcher, "Come visit East London in the fall",
                      "East London");

    }

    public void testMatchLongestNoSkip() {
        CollectingMatcher matcher = new CollectingMatcher();
        matcher.addRules("East", "London", "East London");
        matcher.setMatchMode(VerbatimMatcher.MATCH_MODE.longest);
        matcher.setSkipMatching(false);
        assertMatches(matcher, "Come visit East London in the fall",
                      "East London", "London");

    }

    public void testMatchShortestNoSkip() {
        CollectingMatcher matcher = new CollectingMatcher();
        matcher.addRules("East", "London", "East London");
        matcher.setMatchMode(VerbatimMatcher.MATCH_MODE.shortest);
        matcher.setSkipMatching(false);
        assertMatches(matcher, "Come visit East London in the fall",
                      "East", "London");
    }

    public void testNoLeading() {
        CollectingMatcher matcher = new CollectingMatcher();
        matcher.addRules("East", "London", "East London", "Come");
        matcher.setMatchMode(VerbatimMatcher.MATCH_MODE.all);
        matcher.setSkipMatching(false);
        matcher.setLeading((char) 0); // default
        assertMatches(matcher, "Come visit East-London in the fall",
                      "Come", "East", "London");
    }

    public void testLeading() {
        CollectingMatcher matcher = new CollectingMatcher();
        matcher.addRules("East", "London", "East London", "Come");
        matcher.setMatchMode(VerbatimMatcher.MATCH_MODE.all);
        matcher.setSkipMatching(false);
        matcher.setLeading(' ');
        assertMatches(matcher, "Come visit East-London in the fall",
                      "Come", "East");
    }

    public void testNoFollowing() {
        CollectingMatcher matcher = new CollectingMatcher();
        matcher.addRules("East", "London", "East London", "Come", "fall");
        matcher.setMatchMode(VerbatimMatcher.MATCH_MODE.all);
        matcher.setSkipMatching(false);
        matcher.setFollowing((char) 0); // default
        assertMatches(matcher, "Come visit East-London in the fall",
                      "Come", "East", "London", "fall");
    }

    public void testFollowing() {
        CollectingMatcher matcher = new CollectingMatcher();
        matcher.addRules("East", "London", "East London", "Come", "fall");
        matcher.setMatchMode(VerbatimMatcher.MATCH_MODE.all);
        matcher.setSkipMatching(false);
        matcher.setFollowing(' ');
        assertMatches(matcher, "Come visit East-London in the fall",
                      "Come", "London", "fall");
    }

    public void testSpecificProblem() throws IOException {
        Path INPUT = Paths.get("/home/te/tmp/sumfresh/sites/aviser/names_and_coordinates.dat");
        if (!Files.exists(INPUT)) {
            return;
        }
        CollectingMatcher matcher = new CollectingMatcher();
        BufferedReader in = new BufferedReader(new FileReader(INPUT.toFile()));
        String line;
        int count = 0;
        while ((line = in.readLine()) != null) {
            count++;
            String[] tokens = line.split(",");
            try {
                if (count == 34) {
                    System.out.println("Entering problematic phase with " + line);
                }
                matcher.addRules(tokens[0], tokens[1] + "," + tokens[2]);
            } catch (Exception e) {
                throw new RuntimeException("Exception adding line " + count + ": '" + line + "'");
            }
        }
    }

    public void testLeadingAndFollowingWithSkip() {
        CollectingMatcher matcher = new CollectingMatcher();
        matcher.addRules("East", "London", "East London", "Come", "fall");
        matcher.setMatchMode(VerbatimMatcher.MATCH_MODE.all);
        matcher.setSkipMatching(true);
        matcher.setLeading(' ');
        matcher.setFollowing(' ');
        assertMatches(matcher, "Come visit East-London in the fall",
                      "Come", "fall");
    }

    private void assertMatchesPayload(
            CollectingMatcher matcher, String source, List<String> verbatims, List<String> payloads) {
        assertEquals("There should be the right number of matches",
                     verbatims.size(), matcher.findMatches(source));
        for (int i = 0 ; i < verbatims.size() ; i++) {
            assertEquals("Match " + i + " verbatim should be as expected", verbatims.get(i), matcher.matches.get(i));
            assertEquals("Match " + i + " payload should be as expected", payloads.get(i), matcher.payloads.get(i));
        }
    }

    private void assertMatches(CollectingMatcher matcher, String source, String... matches) {
        assertEquals("There should be the right number of matches",
                     matches.length, matcher.findMatches(source));
        for (int i = 0 ; i < matches.length ; i++) {
            assertEquals("Match " + i + " should be as expected", matches[i], matcher.matches.get(i));
        }
    }

    private void assertMatches(CollectingMatcher matcher, Pattern delimiter, String source, String... matches) {
        assertEquals("There should be the right number of matches",
                     matches.length, matcher.findMatches(source, delimiter));
        for (int i = 0 ; i < matches.length ; i++) {
            assertEquals("Match " + i + " should be as expected", matches[i], matcher.matches.get(i));
        }
    }

    private class CollectingMatcher extends VerbatimMatcher<String> {
        public final List<String> matches = new ArrayList<String>();
        public final List<String> payloads = new ArrayList<String>();

        @Override
        public void callback(String match, String payload) {
            matches.add(match);
            payloads.add(payload);
        }
    }
}