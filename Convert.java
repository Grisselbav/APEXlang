/*
 * Copyright 2026 Philipp Salvisberg <philipp.salvisberg@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
private static String fixGrammar(String input) {
    var output = input;

    // fix multiline comments
    var multiLineCommentPattern = Pattern.compile("(\\(\\*)(.+?)(\\*\\))", Pattern.DOTALL);
    var multiLineCommentMatcher = multiLineCommentPattern.matcher(output);
    while (multiLineCommentMatcher.find()) {
        var comment = multiLineCommentMatcher.group(0);
        var newComment = "/*" + multiLineCommentMatcher.group(2) + "*/";
        output = output.replace(comment, newComment);
        multiLineCommentMatcher = multiLineCommentPattern.matcher(output);
    }

    // fix symbols, remove leading "<" and trailing ">"
    var symbolPattern = Pattern.compile("<([A-Za-z_][A-Za-z0-9_-]*)>");
    output = symbolPattern.matcher(output).replaceAll("$1");

    // fix optionality expressed by [] with ()?
    var optionalityPattern = Pattern.compile("(\\s+)(\\[)(.+?)(])(\\s+)", Pattern.DOTALL);
    var optionalityMatcher = optionalityPattern.matcher(output);
    while (optionalityMatcher.find()) {
        var optionality = optionalityMatcher.group(0);
        var newOptionality = optionalityMatcher.group(1) + "(" + optionalityMatcher.group(3) + ")?" + optionalityMatcher.group(5);
        output = output.replace(optionality, newOptionality);
        optionalityMatcher = optionalityPattern.matcher(output);
    }

    // fix optional repetitions expressed by {} with ()*
    var repetitionPattern = Pattern.compile("(\\s+)(\\{)(.+?)(})(\\s+)", Pattern.DOTALL);
    var repetitionMatcher = repetitionPattern.matcher(output);
    while (repetitionMatcher.find()) {
        var repetition = repetitionMatcher.group(0);
        var newRepetition = repetitionMatcher.group(1) + "(" + repetitionMatcher.group(3) + ")*" + repetitionMatcher.group(5);
        output = output.replace(repetition, newRepetition);
        repetitionMatcher = repetitionPattern.matcher(output);
    }

    // fix escaped strings
    output = output.replace("\"\\\"\"", "'\"'");
    output = output.replace("\"\\\\\"", "'\\'");

    // fix ranges
    output = output.replace("\"0\"..\"9\"", "[0-9]");
    output = output.replace("\"a\"..\"z\"", "[a-z]");
    output = output.replace("\"A\"..\"Z\"", "[A-Z]");
    output = output.replace("\"a\"..\"f\"", "[a-f]");
    output = output.replace("\"A\"..\"F\"", "[A-F]");

    // return W3C EBNF according to https://www.w3.org/TR/xml/#sec-notation
    return output;
}

/**
 * Transform original Oracle-specific EBNF from
 * <a href="https://docs.oracle.com/en/database/oracle/apex/26.1/apxln">APEXlang API Reference</a>
 * to W3C EBNF to be used as input for
 * <a href="https://github.com/GuntherRademacher/rr">RR - Railroad Diagram Generator</a>
 */
void main(String[] args) throws IOException {
    if (args.length != 2) {
        System.err.println("Usage: java Convert.java <input-file> <output-file>");
        System.exit(1);
    }
    var inputFile = Path.of(args[0]);
    var outputFile = Path.of(args[1]);
    var input = Files.readString(inputFile);
    Files.createDirectories(outputFile.toAbsolutePath().getParent());
    Files.writeString(outputFile, fixGrammar(input));
}
