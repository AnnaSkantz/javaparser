/*
 * Copyright (C) 2007-2010 Júlio Vilmar Gesser.
 * Copyright (C) 2011, 2013-2019 The JavaParser Team.
 *
 * This file is part of JavaParser.
 *
 * JavaParser can be used either under the terms of
 * a) the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * b) the terms of the Apache License
 *
 * You should have received a copy of both licenses in LICENCE.LGPL and
 * LICENCE.APACHE. Please refer to those files for details.
 *
 * JavaParser is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 */

package com.github.javaparser.printer;

import static com.github.javaparser.StaticJavaParser.parse;
import static com.github.javaparser.utils.TestParser.parseBodyDeclaration;
import static com.github.javaparser.utils.TestParser.parseCompilationUnit;
import static com.github.javaparser.utils.TestParser.parseExpression;
import static com.github.javaparser.utils.TestParser.parseStatement;
import static com.github.javaparser.utils.TestParser.parseVariableDeclarationExpr;
import static com.github.javaparser.utils.TestUtils.assertEqualsStringIgnoringEol;
import static com.github.javaparser.utils.Utils.SYSTEM_EOL;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.utils.TestParser;
import org.junit.jupiter.api.Test;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.expr.CastExpr;
import com.github.javaparser.ast.expr.ClassExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.printer.configuration.ConfigurationOption;
import com.github.javaparser.printer.configuration.DefaultConfigurationOption;
import com.github.javaparser.printer.configuration.DefaultPrinterConfiguration;
import com.github.javaparser.printer.configuration.DefaultPrinterConfiguration.ConfigOption;
import com.github.javaparser.printer.configuration.PrinterConfiguration;

class PrettyPrintVisitorTest extends TestParser {

    private Optional<ConfigurationOption> getOption(PrinterConfiguration config, ConfigOption cOption) {
        return config.get(new DefaultConfigurationOption(cOption));
    }

    @Test
    void getMaximumCommonTypeWithoutAnnotations() {
        VariableDeclarationExpr vde1 = parseVariableDeclarationExpr("int a[], b[]");
        assertEquals("int[]", vde1.getMaximumCommonType().get().toString());

        VariableDeclarationExpr vde2 = parseVariableDeclarationExpr("int[][] a[], b[]");
        assertEquals("int[][][]", vde2.getMaximumCommonType().get().toString());

        VariableDeclarationExpr vde3 = parseVariableDeclarationExpr("int[][] a, b[]");
        assertEquals("int[][]", vde3.getMaximumCommonType().get().toString());
    }

    @Test
    void getMaximumCommonTypeWithAnnotations() {
        VariableDeclarationExpr vde1 = parseVariableDeclarationExpr("int a @Foo [], b[]");
        assertEquals("int", vde1.getMaximumCommonType().get().toString());

        VariableDeclarationExpr vde2 = parseVariableDeclarationExpr("int[]@Foo [] a[], b[]");
        assertEquals("int[][] @Foo []", vde2.getMaximumCommonType().get().toString());
    }

    private String print(Node node) {
        return new DefaultPrettyPrinter().print(node);
    }

    private String print(Node node, PrinterConfiguration conf) {
        return new DefaultPrettyPrinter(conf).print(node);
    }


    @Test
    void printSimpleClassExpr() {
        ClassExpr expr = parseExpression("Foo.class");
        assertEquals("Foo.class", print(expr));
    }


    /**
     * Here is a simple test according to R0 (removing spaces)
     */
    @Test
    void printOperatorsR0(){
        PrinterConfiguration conf1 = new DefaultPrinterConfiguration().removeOption(new DefaultConfigurationOption(ConfigOption.SPACE_AROUND_OPERATORS));
        Statement statement1 = parseStatement("a = 1 + 1;");
        assertEquals("a=1+1;", print(statement1, conf1));
    }

    /**
     * Here we test different operators according to requirement R1 (handling different operators)
     */
    @Test
    void printOperatorsR1(){

        Statement statement1 = parseStatement("a = 1 + 1;");
        assertEquals("a = 1 + 1;", print(statement1));

        Statement statement2 = parseStatement("a = 1 - 1;");
        assertEquals("a = 1 - 1;", print(statement2));

        Statement statement3 = parseStatement("a = 1 * 1;");
        assertEquals("a = 1 * 1;", print(statement3));

        Statement statement4 = parseStatement("a = 1 % 1;");
        assertEquals("a = 1 % 1;", print(statement4));

        Statement statement5 = parseStatement("a=1/1;");
        assertEquals("a = 1 / 1;", print(statement5));

        Statement statement6 = parseStatement("if (1 > 2 && 1 < 3 || 1 < 3){}");
        assertEquals("if (1 > 2 && 1 < 3 || 1 < 3) {" + SYSTEM_EOL
                + "}", print(statement6));

    }

    /**
     * Here is a simple test according to R2 (that it should be optional/modifiable)
     */
    @Test
    void printOperatorsR2(){
        PrinterConfiguration conf1 = new DefaultPrinterConfiguration().removeOption(new DefaultConfigurationOption(ConfigOption.SPACE_AROUND_OPERATORS));
        Statement statement1 = parseStatement("a = 1 + 1;");
        assertEquals("a=1+1;", print(statement1, conf1));

        PrinterConfiguration conf2 = new DefaultPrinterConfiguration().removeOption(new DefaultConfigurationOption(ConfigOption.SPACE_AROUND_OPERATORS));
        Statement statement2 = parseStatement("a=1+1;");
        assertEquals("a=1+1;", print(statement2, conf2));

        PrinterConfiguration conf3 = new DefaultPrinterConfiguration().addOption(new DefaultConfigurationOption(ConfigOption.SPACE_AROUND_OPERATORS));
        Statement statement3 = parseStatement("a = 1 + 1;");
        assertEquals("a = 1 + 1;", print(statement3, conf3));

        PrinterConfiguration conf4 = new DefaultPrinterConfiguration().addOption(new DefaultConfigurationOption(ConfigOption.SPACE_AROUND_OPERATORS));
        Statement statement4 = parseStatement("a=1+1;");
        assertEquals("a = 1 + 1;", print(statement4, conf4));

    }

    @Test
    void printOperatorA(){
        PrinterConfiguration conf = new DefaultPrinterConfiguration().removeOption(new DefaultConfigurationOption(ConfigOption.SPACE_AROUND_OPERATORS));
        Statement statement6 = parseStatement("if(1>2&&1<3||1<3){}");
        assertEquals("if (1>2&&1<3||1<3) {" + SYSTEM_EOL
                + "}", print(statement6, conf));
    }

    @Test
    void printOperator2(){
        Expression expression = parseExpression("1+1");
        PrinterConfiguration spaces = new DefaultPrinterConfiguration().removeOption(new DefaultConfigurationOption(ConfigOption.SPACE_AROUND_OPERATORS));
        assertEquals("1+1", print(expression, spaces));
    }

    @Test
    void printArrayClassExpr() {
        ClassExpr expr = parseExpression("Foo[].class");
        assertEquals("Foo[].class", print(expr));
    }

    @Test
    void printGenericClassExpr() {
        ClassExpr expr = parseExpression("Foo<String>.class");
        assertEquals("Foo<String>.class", print(expr));
    }

    @Test
    void printSimplestClass() {
        Node node = parse("class A {}");
        assertEquals("class A {" + SYSTEM_EOL +
                "}" + SYSTEM_EOL, print(node));
    }

    @Test
    void printAClassWithField() {
        Node node = parse("class A { int a; }");
        assertEquals("class A {" + SYSTEM_EOL
                + SYSTEM_EOL +
                "    int a;" + SYSTEM_EOL +
                "}" + SYSTEM_EOL, print(node));
    }

    @Test
    void printAReceiverParameter() {
        Node node = parseBodyDeclaration("int x(@O X A.B.this, int y) { }");
        assertEquals("int x(@O X A.B.this, int y) {" + SYSTEM_EOL + "}", print(node));
    }

    @Test
    void printLambdaIntersectionTypeAssignment() {
        String code = "class A {" + SYSTEM_EOL +
                "  void f() {" + SYSTEM_EOL +
                "    Runnable r = (Runnable & Serializable) (() -> {});" + SYSTEM_EOL +
                "    r = (Runnable & Serializable)() -> {};" + SYSTEM_EOL +
                "    r = (Runnable & I)() -> {};" + SYSTEM_EOL +
                "  }}";
        CompilationUnit cu = parse(code);
        MethodDeclaration methodDeclaration = (MethodDeclaration) cu.getType(0).getMember(0);

        assertEquals("Runnable r = (Runnable & Serializable) (() -> {" + SYSTEM_EOL + "});", print(methodDeclaration.getBody().get().getStatements().get(0)));
    }

    @Test
    void printIntersectionType() {
        String code = "(Runnable & Serializable) (() -> {})";
        Expression expression = parseExpression(code);
        Type type = ((CastExpr) expression).getType();

        assertEquals("Runnable & Serializable", print(type));
    }

    @Test
    void printLambdaIntersectionTypeReturn() {
        String code = "class A {" + SYSTEM_EOL
                + "  Object f() {" + SYSTEM_EOL
                + "    return (Comparator<Map.Entry<K, V>> & Serializable)(c1, c2) -> c1.getKey().compareTo(c2.getKey()); " + SYSTEM_EOL
                + "}}";
        CompilationUnit cu = parse(code);
        MethodDeclaration methodDeclaration = (MethodDeclaration) cu.getType(0).getMember(0);

        assertEquals("return (Comparator<Map.Entry<K, V>> & Serializable) (c1, c2) -> c1.getKey().compareTo(c2.getKey());", print(methodDeclaration.getBody().get().getStatements().get(0)));
    }

    @Test
    void printClassWithoutJavaDocButWithComment() {
        String code = String.format("/** javadoc */ public class A { %s// stuff%s}", SYSTEM_EOL, SYSTEM_EOL);
        CompilationUnit cu = parse(code);
        PrinterConfiguration ignoreJavaDoc = new DefaultPrinterConfiguration().removeOption(new DefaultConfigurationOption(ConfigOption.PRINT_JAVADOC));
        String content = cu.toString(ignoreJavaDoc);
        assertEquals(String.format("public class A {%s    // stuff%s}%s", SYSTEM_EOL, SYSTEM_EOL, SYSTEM_EOL), content);
    }

    @Test
    void printImportsDefaultOrder() {
        String code = "import x.y.z;import a.b.c;import static b.c.d;class c {}";
        CompilationUnit cu = parse(code);
        String content = cu.toString();
        assertEqualsStringIgnoringEol("import x.y.z;\n" +
                "import a.b.c;\n" +
                "import static b.c.d;\n" +
                "\n" +
                "class c {\n" +
                "}\n", content);
    }

    @Test
    void printImportsOrdered() {
        String code = "import x.y.z;import a.b.c;import static b.c.d;class c {}";
        CompilationUnit cu = parse(code);
        PrinterConfiguration orderImports = new DefaultPrinterConfiguration().addOption(new DefaultConfigurationOption(ConfigOption.ORDER_IMPORTS));
        String content = cu.toString(orderImports);
        assertEqualsStringIgnoringEol("import static b.c.d;\n" +
                "import a.b.c;\n" +
                "import x.y.z;\n" +
                "\n" +
                "class c {\n" +
                "}\n", content);
    }

    @Test
    void multilineJavadocGetsFormatted() {
        CompilationUnit cu = new CompilationUnit();
        cu.addClass("X").addMethod("abc").setJavadocComment("line1\n   line2 *\n * line3");

        assertEqualsStringIgnoringEol("public class X {\n" +
                "\n" +
                "    /**\n" +
                "     * line1\n" +
                "     *    line2 *\n" +
                "     *  line3\n" +
                "     */\n" +
                "    void abc() {\n" +
                "    }\n" +
                "}\n", cu.toString());
    }

    @Test
    void emptyJavadocGetsFormatted() {
        CompilationUnit cu = new CompilationUnit();
        cu.addClass("X").addMethod("abc").setJavadocComment("");

        assertEqualsStringIgnoringEol("public class X {\n" +
                "\n" +
                "    /**\n" +
                "     */\n" +
                "    void abc() {\n" +
                "    }\n" +
                "}\n", cu.toString());
    }

    @Test
    void multilineJavadocWithLotsOfEmptyLinesGetsFormattedNeatly() {
        CompilationUnit cu = new CompilationUnit();
        cu.addClass("X").addMethod("abc").setJavadocComment("\n\n\n ab\n\n\n cd\n\n\n");

        assertEqualsStringIgnoringEol("public class X {\n" +
                "\n" +
                "    /**\n" +
                "     * ab\n" +
                "     *\n" +
                "     * cd\n" +
                "     */\n" +
                "    void abc() {\n" +
                "    }\n" +
                "}\n", cu.toString());
    }

    @Test
    void singlelineJavadocGetsFormatted() {
        CompilationUnit cu = new CompilationUnit();
        cu.addClass("X").addMethod("abc").setJavadocComment("line1");

        assertEqualsStringIgnoringEol("public class X {\n" +
                "\n" +
                "    /**\n" +
                "     * line1\n" +
                "     */\n" +
                "    void abc() {\n" +
                "    }\n" +
                "}\n", cu.toString());
    }

    @Test
    void javadocAlwaysGetsASpaceBetweenTheAsteriskAndTheRestOfTheLine() {
        CompilationUnit cu = new CompilationUnit();
        cu.addClass("X").addMethod("abc").setJavadocComment("line1\nline2");

        assertEqualsStringIgnoringEol("public class X {\n" +
                "\n" +
                "    /**\n" +
                "     * line1\n" +
                "     * line2\n" +
                "     */\n" +
                "    void abc() {\n" +
                "    }\n" +
                "}\n", cu.toString());
    }

    @Test
    void javadocAlwaysGetsAnAdditionalSpaceOrNeverGetsIt() {
        CompilationUnit cu = new CompilationUnit();
        cu.addClass("X").addMethod("abc").setJavadocComment("line1\n" +
                "line2\n" +
                "    3");

        assertEqualsStringIgnoringEol("public class X {\n" +
                "\n" +
                "    /**\n" +
                "     * line1\n" +
                "     * line2\n" +
                "     *     3\n" +
                "     */\n" +
                "    void abc() {\n" +
                "    }\n" +
                "}\n", cu.toString());
    }

    @Test
    void singlelineCommentGetsFormatted() {
        CompilationUnit cu = new CompilationUnit();
        cu.addClass("X").addMethod("abc").setComment(new LineComment("   line1  \n "));

        assertEqualsStringIgnoringEol("public class X {\n" +
                "\n" +
                "    // line1\n" +
                "    void abc() {\n" +
                "    }\n" +
                "}\n", cu.toString());
    }

    @Test
    void blockcommentGetsNoFormatting() {
        CompilationUnit cu = parse("class A {\n" +
                "    public void helloWorld(String greeting, String name) {\n" +
                "        //sdfsdfsdf\n" +
                "            //sdfds\n" +
                "        /*\n" +
                "                            dgfdgfdgfdgfdgfd\n" +
                "         */\n" +
                "    }\n" +
                "}\n");

        assertEqualsStringIgnoringEol("class A {\n" +
                "\n" +
                "    public void helloWorld(String greeting, String name) {\n" +
                "        // sdfsdfsdf\n" +
                "        // sdfds\n" +
                "        /*\n" +
                "                            dgfdgfdgfdgfdgfd\n" +
                "         */\n" +
                "    }\n" +
                "}\n", cu.toString());
    }

    private String expected = "public class SomeClass {\n" +
            "\n" +
            "    /**\n" +
            "     * tester line\n" +
            "     * multi line comment\n" +
            "     *   multi line comment\n" +
            "     * multi line comment\n" +
            "     *    multi line comment\n" +
            "     */\n" +
            "    public void add(int x, int y) {\n" +
            "    }\n" +
            "}\n";

    @Test
    void javadocIssue1907_allLeadingSpaces() {
        String input_allLeadingSpaces = "public class SomeClass{" +
                "/**\n" +
                " * tester line\n" +
                " * multi line comment\n" +
                " *   multi line comment\n" +
                "   * multi line comment\n" +
                "    multi line comment\n" +
                " */\n" +
                "public void add(int x, int y){}}";

        CompilationUnit cu_allLeadingSpaces = parse(input_allLeadingSpaces);
        assertEqualsStringIgnoringEol(expected, cu_allLeadingSpaces.toString());
    }

    @Test
    void javadocIssue1907_singleMissingLeadingSpace() {
        String input_singleMissingLeadingSpace = "public class SomeClass{" +
                "/**\n" +
                "* tester line\n" +
                " * multi line comment\n" +
                " *   multi line comment\n" +
                "   * multi line comment\n" +
                "    multi line comment\n" +
                " */\n" +
                "public void add(int x, int y){}}";

        CompilationUnit cu_singleMissingLeadingSpace = parse(input_singleMissingLeadingSpace);
        assertEqualsStringIgnoringEol(expected, cu_singleMissingLeadingSpace.toString());
    }

    @Test
    void javadocIssue1907_leadingTab() {
        String input_leadingTab = "public class SomeClass{" +
                "/**\n" +
                "\t * tester line\n" +
                " * multi line comment\n" +
                " *   multi line comment\n" +
                "   * multi line comment\n" +
                "    multi line comment\n" +
                " */\n" +
                "public void add(int x, int y){}}";

        CompilationUnit cu_leadingTab = parseCompilationUnit(input_leadingTab);
        assertEqualsStringIgnoringEol(expected, cu_leadingTab.toString());
    }

    @Test
    void printYield() {
        Statement statement = parseStatement("yield 5*5;");
        assertEqualsStringIgnoringEol("yield 5 * 5;", statement.toString());
    }

    @Test
    void printTextBlock() {
        CompilationUnit cu = parseCompilationUnit(
                ParserConfiguration.LanguageLevel.JAVA_13_PREVIEW,
                "class X{String html = \"\"\"\n" +
                "              <html>\n" +
                "                  <body>\n" +
                "                      <p>Hello, world</p>\n" +
                "                  </body>\n" +
                "              </html>\n" +
                "              \"\"\";}"
        );

        assertEqualsStringIgnoringEol("String html = \"\"\"\n" +
                "    <html>\n" +
                "        <body>\n" +
                "            <p>Hello, world</p>\n" +
                "        </body>\n" +
                "    </html>\n" +
                "    \"\"\";", cu.getClassByName("X").get().getFieldByName("html").get().toString());
    }

    @Test
    void printTextBlock2() {
        CompilationUnit cu = parseCompilationUnit(
                ParserConfiguration.LanguageLevel.JAVA_13_PREVIEW,
                "class X{String html = \"\"\"\n" +
                "              <html>\n" +
                "              </html>\"\"\";}"
        );

        assertEqualsStringIgnoringEol("String html = \"\"\"\n" +
                "    <html>\n" +
                "    </html>\"\"\";", cu.getClassByName("X").get().getFieldByName("html").get().toString());
    }

    /**
     * Tests that the PrettyPrinter config option OPENING_BRACE_ON_NEWLINE affects if statements
     */
    @Test
    public void openingBraceOnNewlineIfStatements() {
        PrinterConfiguration config = new DefaultPrinterConfiguration()
            .addOption(new DefaultConfigurationOption(ConfigOption.OPENING_BRACE_ON_NEWLINE));
        DefaultPrettyPrinter printer = new DefaultPrettyPrinter(config);
        final String EOL = getOption(config, ConfigOption.END_OF_LINE_CHARACTER).get().asValue();

        Statement s = parseStatement("if (true) { a = 1; } else if (false) { a = 2; } else { a = 3; }");
        String printed = printer.print(s);

        String expected = "if (true) " + EOL +
                            "{" + EOL +
                            "    a = 1;" + EOL +
                            "} else if (false) " + EOL +
                            "{" + EOL +
                            "    a = 2;" + EOL +
                            "} else " + EOL +
                            "{" + EOL +
                            "    a = 3;" + EOL +
                            "}";
        assertEquals(expected, printed);
    }

    /**
     * Tests that the PrettyPrinter config option OPENING_BRACE_ON_NEWLINE affects switch expressions
     */
    @Test
    public void openingBraceOnNewlineSwitchExpressions() {
        PrinterConfiguration config = new DefaultPrinterConfiguration()
                .addOption(new DefaultConfigurationOption(ConfigOption.OPENING_BRACE_ON_NEWLINE));
        DefaultPrettyPrinter printer = new DefaultPrettyPrinter(config);
        final String EOL = getOption(config, ConfigOption.END_OF_LINE_CHARACTER).get().asValue();

        Expression e = parseExpression("switch (a) { case \"1\": break; default: break; }");
        String printed = printer.print(e);
        String expected = "switch(a)" + EOL +
                            "{" + EOL +
                            "    case \"1\":" + EOL +
                            "        break;" + EOL +
                            "    default:" + EOL +
                            "        break;" + EOL +
                            "}";
        assertEquals(expected, printed);
    }

    /**
     * Tests that the PrettyPrinter config option OPENING_BRACE_ON_NEWLINE affects Lambda expressions
     */
    @Test
    public void openingBraceOnNewlineLambdaExpressions() {
        PrinterConfiguration config = new DefaultPrinterConfiguration()
                .addOption(new DefaultConfigurationOption(ConfigOption.OPENING_BRACE_ON_NEWLINE));
        DefaultPrettyPrinter printer = new DefaultPrettyPrinter(config);
        final String EOL = getOption(config, ConfigOption.END_OF_LINE_CHARACTER).get().asValue();

        Expression e = parseExpression("x -> { return x + 1; }");
        String printed = printer.print(e);
        String expected = "x -> " + EOL +
                            "{" + EOL +
                            "    return x + 1;" + EOL +
                            "}";
        assertEquals(expected, printed);
    }

    /**
     * Tests that the PrettyPrinter config option OPENING_BRACE_ON_NEWLINE affects different types of try statements
     */
    @Test
    public void openingBraceOnNewlineTryStatements() {
        PrinterConfiguration config = new DefaultPrinterConfiguration()
                .addOption(new DefaultConfigurationOption(ConfigOption.OPENING_BRACE_ON_NEWLINE));
        DefaultPrettyPrinter printer = new DefaultPrettyPrinter(config);
        final String EOL = getOption(config, ConfigOption.END_OF_LINE_CHARACTER).get().asValue();

        Statement s = parseStatement("try { a = b; } catch (Exception e) { a(); } finally { a = 1; }");
        String printed = printer.print(s);
        String expected = "try " + EOL +
                            "{" + EOL +
                            "    a = b;" + EOL +
                            "} catch (Exception e) " + EOL +
                            "{" + EOL +
                            "    a();" + EOL +
                            "} finally " + EOL +
                            "{" + EOL +
                            "    a = 1;" + EOL +
                            "}";
        assertEquals(expected, printed);

        s = parseStatement("try (int a = b) { a(); }");
        printed = printer.print(s);
        expected = "try (int a = b) " + EOL +
                    "{" + EOL +
                    "    a();" + EOL +
                    "}";
        assertEquals(expected, printed);
    }

    /**
     * Tests that the PrettyPrinter config option OPENING_BRACE_ON_NEWLINE affects do statements
     */
    @Test
    public void openingBraceOnNewlineDoStatements() {
        PrinterConfiguration config = new DefaultPrinterConfiguration()
                .addOption(new DefaultConfigurationOption(ConfigOption.OPENING_BRACE_ON_NEWLINE));
        DefaultPrettyPrinter printer = new DefaultPrettyPrinter(config);
        final String EOL = getOption(config, ConfigOption.END_OF_LINE_CHARACTER).get().asValue();

        Statement s = parseStatement("do { a(); } while(a == b);");
        String printed = printer.print(s);
        String expected = "do " + EOL +
                            "{" + EOL +
                            "    a();" + EOL +
                            "} while (a == b);";
        assertEquals(expected, printed);
    }

    /**
     * Tests that the PrettyPrinter config option OPENING_BRACE_ON_NEWLINE affects for statements
     */
    @Test
    public void openingBraceOnNewlineForStatements() {
        PrinterConfiguration config = new DefaultPrinterConfiguration()
                .addOption(new DefaultConfigurationOption(ConfigOption.OPENING_BRACE_ON_NEWLINE));
        DefaultPrettyPrinter printer = new DefaultPrettyPrinter(config);
        final String EOL = getOption(config, ConfigOption.END_OF_LINE_CHARACTER).get().asValue();

        Statement s = parseStatement("for (int i = 0; i < 10; i++) { f(); }");
        String printed = printer.print(s);
        String expected = "for (int i = 0; i < 10; i++) " + EOL +
                            "{" + EOL +
                            "    f();" + EOL +
                            "}";
        assertEquals(expected, printed);
    }

    /**
     * Tests that the PrettyPrinter config option OPENING_BRACE_ON_NEWLINE affects for-each statements
     */
    @Test
    public void openingBraceOnNewlineForEachStatements() {
        PrinterConfiguration config = new DefaultPrinterConfiguration()
                .addOption(new DefaultConfigurationOption(ConfigOption.OPENING_BRACE_ON_NEWLINE));
        DefaultPrettyPrinter printer = new DefaultPrettyPrinter(config);
        final String EOL = getOption(config, ConfigOption.END_OF_LINE_CHARACTER).get().asValue();

        Statement s = parseStatement("for (String s : list) { f(); }");
        String printed = printer.print(s);
        String expected = "for (String s : list) " + EOL +
                            "{" + EOL +
                            "    f();" + EOL +
                            "}";
        assertEquals(expected, printed);
    }

    /**
     * Tests that the PrettyPrinter config option OPENING_BRACE_ON_NEWLINE affects while statements
     */
    @Test
    public void openingBraceOnNewlineWhileStatements() {
        PrinterConfiguration config = new DefaultPrinterConfiguration()
                .addOption(new DefaultConfigurationOption(ConfigOption.OPENING_BRACE_ON_NEWLINE));
        DefaultPrettyPrinter printer = new DefaultPrettyPrinter(config);
        final String EOL = getOption(config, ConfigOption.END_OF_LINE_CHARACTER).get().asValue();

        Statement s = parseStatement("while (i < 0) { f(); }");
        String printed = printer.print(s);
        String expected = "while (i < 0) " + EOL +
                            "{" + EOL +
                            "    f();" + EOL +
                            "}";
        assertEquals(expected, printed);
    }

    /**
     * Tests that the PrettyPrinter config option OPENING_BRACE_ON_NEWLINE affects different types of enum declarations
     */
    @Test
    public void openingBraceOnNewlineEnums() {
        PrinterConfiguration config = new DefaultPrinterConfiguration()
                .addOption(new DefaultConfigurationOption(ConfigOption.OPENING_BRACE_ON_NEWLINE));
        DefaultPrettyPrinter printer = new DefaultPrettyPrinter(config);
        final String EOL = getOption(config, ConfigOption.END_OF_LINE_CHARACTER).get().asValue();

        CompilationUnit cu = parseCompilationUnit("enum Hello { A, B, C }");
        String printed = printer.print(cu);
        String expected = "enum Hello" + EOL +
                            "{" + EOL +
                            "    A, B, C" + EOL +
                            "}" + EOL;
        assertEquals(expected, printed);

        cu = parseCompilationUnit("enum Hello { A(a), B(a), C(a); Hello(String a) { a = \"1\"; } }");
        printed = printer.print(cu);
        expected = "enum Hello" + EOL +
                    "{" + EOL +
                    "    A(a), B(a), C(a);" + EOL +
                    "" + EOL +
                    "    Hello(String a) " + EOL +
                    "    {" + EOL +
                    "        a = \"1\";" + EOL +
                    "    }" + EOL +
                    "}" + EOL;

        assertEquals(expected, printed);

        cu = parseCompilationUnit("enum Hello { f(1){ void mm() { } }, f(2){ void m() { } } }");
        printed = printer.print(cu);
        expected = "enum Hello" + EOL +
                    "{" + EOL +
                    "    f(1)" + EOL +
                    "    {" + EOL +
                    "        void mm() " + EOL +
                    "        {" + EOL +
                    "        }" + EOL +
                    "    }" + EOL +
                    "    , f(2)" + EOL +
                    "    {" + EOL +
                    "        void m() " + EOL +
                    "        {" + EOL +
                    "        }" + EOL +
                    "    }" + EOL +
                    "" + EOL +
                    "}" + EOL;

        assertEquals(expected, printed);
    }

    /**
     * Tests that the PrettyPrinter config option OPENING_BRACE_ON_NEWLINE affects class and method declarations
     */
    @Test
    public void openingBraceOnNewlineClassAndMethodDeclarations() {
        PrinterConfiguration config = new DefaultPrinterConfiguration()
                .addOption(new DefaultConfigurationOption(ConfigOption.OPENING_BRACE_ON_NEWLINE));
        DefaultPrettyPrinter printer = new DefaultPrettyPrinter(config);
        final String EOL = getOption(config, ConfigOption.END_OF_LINE_CHARACTER).get().asValue();

        CompilationUnit cu = parseCompilationUnit("class Hello { public String hello(String a) { return a; } }");
        String printed = printer.print(cu);
        String expected = "class Hello" + EOL +
                            "{" + EOL +
                            "    public String hello(String a) " + EOL +
                            "    {" + EOL +
                            "        return a;" + EOL +
                            "    }" + EOL +
                            "}" + EOL;

        assertEquals(expected, printed);
    }

    /**
     * Tests that the PrettyPrinter config option OPENING_BRACE_ON_NEWLINE affects interface declarations
     */
    @Test
    public void openingBraceOnNewlineInterfaceDeclarations() {
        PrinterConfiguration config = new DefaultPrinterConfiguration()
                .addOption(new DefaultConfigurationOption(ConfigOption.OPENING_BRACE_ON_NEWLINE));
        DefaultPrettyPrinter printer = new DefaultPrettyPrinter(config);
        final String EOL = getOption(config, ConfigOption.END_OF_LINE_CHARACTER).get().asValue();

        CompilationUnit cu = parseCompilationUnit("interface Hello extends World { public void f(); }");
        String printed = printer.print(cu);
        String expected = "interface Hello extends World" + EOL +
                            "{" + EOL +
                            "    public void f();" + EOL +
                            "}" + EOL;

        assertEquals(expected, printed);
    }

    /**
     * Tests that the PrettyPrinter config option OPENING_BRACE_ON_NEWLINE affects record declarations
     */
    @Test
    public void openingBraceOnNewlineRecordDeclarations() {
        PrinterConfiguration config = new DefaultPrinterConfiguration()
                .addOption(new DefaultConfigurationOption(ConfigOption.OPENING_BRACE_ON_NEWLINE));
        DefaultPrettyPrinter printer = new DefaultPrettyPrinter(config);
        final String EOL = getOption(config, ConfigOption.END_OF_LINE_CHARACTER).get().asValue();

        CompilationUnit cu = parseCompilationUnit("record Hello(String hello) { Hello { int a = 1; } }");
        String printed = printer.print(cu);
        String expected = "record Hello(String hello)" + EOL +
                            "{" + EOL +
                            "    Hello " + EOL +
                            "    {" + EOL +
                            "        int a = 1;" + EOL +
                            "    }" + EOL +
                            "}" + EOL;

        assertEquals(expected, printed);
    }

    /**
     * Tests that the PrettyPrinter config option OPENING_BRACE_ON_NEWLINE affects static blocks
     */
    @Test
    public void openingBraceOnNewlineStaticBlocks() {
        PrinterConfiguration config = new DefaultPrinterConfiguration()
                .addOption(new DefaultConfigurationOption(ConfigOption.OPENING_BRACE_ON_NEWLINE));
        DefaultPrettyPrinter printer = new DefaultPrettyPrinter(config);
        final String EOL = getOption(config, ConfigOption.END_OF_LINE_CHARACTER).get().asValue();

        CompilationUnit cu = parseCompilationUnit("class Hello { static { int a = 1; } Hello() { int b = 1; } }");
        String printed = printer.print(cu);
        String expected = "class Hello" + EOL +
                            "{" + EOL +
                            "    static " + EOL +
                            "    {" + EOL +
                            "        int a = 1;" + EOL +
                            "    }" + EOL +
                            "" + EOL +
                            "    Hello() " + EOL +
                            "    {" + EOL +
                            "        int b = 1;" + EOL +
                            "    }" + EOL +
                            "}" + EOL;
        
        assertEquals(expected, printed);
    }
}
