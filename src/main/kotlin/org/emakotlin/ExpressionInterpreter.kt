package org.emakotlin

internal object ExpressionInterpreter {
    fun evaluate(expression: String, context: Map<String, Any?>): Any? {
        return try {
            val parser = Parser(expression)
            val node = parser.parseExpression()
            parser.ensureEof()
            node.eval(context)
        } catch (_: UnknownVariableException) {
            false
        }
    }

    private class UnknownVariableException(name: String) : RuntimeException("Unknown variable: $name")

    private enum class TokenType {
        NUMBER,
        IDENTIFIER,
        BOOLEAN,
        OPERATOR,
        LPAREN,
        RPAREN,
        EOF
    }

    private data class Token(val type: TokenType, val text: String)

    private sealed interface Node {
        fun eval(context: Map<String, Any?>): Any?
    }

    private data class LiteralNode(private val value: Any?) : Node {
        override fun eval(context: Map<String, Any?>): Any? = value
    }

    private data class IdentifierNode(private val name: String) : Node {
        override fun eval(context: Map<String, Any?>): Any? {
            if (!context.containsKey(name)) {
                throw UnknownVariableException(name)
            }
            return context[name]
        }
    }

    private data class UnaryNode(private val op: String, private val right: Node) : Node {
        override fun eval(context: Map<String, Any?>): Any? {
            val rightValue = right.eval(context)
            return when (op) {
                "!" -> !JsTruthiness.isTruthy(rightValue)
                "-" -> -JsTruthiness.toNumber(rightValue)
                else -> throw IllegalArgumentException("Unsupported unary operator: $op")
            }
        }
    }

    private data class BinaryNode(private val left: Node, private val op: String, private val right: Node) : Node {
        override fun eval(context: Map<String, Any?>): Any? {
            return when (op) {
                "||" -> {
                    val leftValue = left.eval(context)
                    if (JsTruthiness.isTruthy(leftValue)) leftValue else right.eval(context)
                }
                "&&" -> {
                    val leftValue = left.eval(context)
                    if (!JsTruthiness.isTruthy(leftValue)) leftValue else right.eval(context)
                }
                "==" -> left.eval(context) == right.eval(context)
                "!=" -> left.eval(context) != right.eval(context)
                ">" -> JsTruthiness.toNumber(left.eval(context)) > JsTruthiness.toNumber(right.eval(context))
                ">=" -> JsTruthiness.toNumber(left.eval(context)) >= JsTruthiness.toNumber(right.eval(context))
                "<" -> JsTruthiness.toNumber(left.eval(context)) < JsTruthiness.toNumber(right.eval(context))
                "<=" -> JsTruthiness.toNumber(left.eval(context)) <= JsTruthiness.toNumber(right.eval(context))
                "+" -> {
                    val leftValue = left.eval(context)
                    val rightValue = right.eval(context)
                    if (leftValue is String || rightValue is String) {
                        JsTruthiness.toJsString(leftValue) + JsTruthiness.toJsString(rightValue)
                    } else {
                        JsTruthiness.toNumber(leftValue) + JsTruthiness.toNumber(rightValue)
                    }
                }
                "-" -> JsTruthiness.toNumber(left.eval(context)) - JsTruthiness.toNumber(right.eval(context))
                "*" -> JsTruthiness.toNumber(left.eval(context)) * JsTruthiness.toNumber(right.eval(context))
                "/" -> JsTruthiness.toNumber(left.eval(context)) / JsTruthiness.toNumber(right.eval(context))
                else -> throw IllegalArgumentException("Unsupported binary operator: $op")
            }
        }
    }

    private class Parser(expression: String) {
        private val tokens = tokenize(expression)
        private var index = 0

        fun parseExpression(): Node = parseOr()

        fun ensureEof() {
            if (peek().type != TokenType.EOF) {
                throw IllegalArgumentException("Unexpected token '${peek().text}'")
            }
        }

        private fun parseOr(): Node {
            var left = parseAnd()
            while (matchOperator("||")) {
                left = BinaryNode(left, "||", parseAnd())
            }
            return left
        }

        private fun parseAnd(): Node {
            var left = parseEquality()
            while (matchOperator("&&")) {
                left = BinaryNode(left, "&&", parseEquality())
            }
            return left
        }

        private fun parseEquality(): Node {
            var left = parseComparison()
            while (true) {
                left = when {
                    matchOperator("==") -> BinaryNode(left, "==", parseComparison())
                    matchOperator("!=") -> BinaryNode(left, "!=", parseComparison())
                    else -> return left
                }
            }
        }

        private fun parseComparison(): Node {
            var left = parseAddition()
            while (true) {
                left = when {
                    matchOperator(">=") -> BinaryNode(left, ">=", parseAddition())
                    matchOperator("<=") -> BinaryNode(left, "<=", parseAddition())
                    matchOperator(">") -> BinaryNode(left, ">", parseAddition())
                    matchOperator("<") -> BinaryNode(left, "<", parseAddition())
                    else -> return left
                }
            }
        }

        private fun parseAddition(): Node {
            var left = parseMultiplication()
            while (true) {
                left = when {
                    matchOperator("+") -> BinaryNode(left, "+", parseMultiplication())
                    matchOperator("-") -> BinaryNode(left, "-", parseMultiplication())
                    else -> return left
                }
            }
        }

        private fun parseMultiplication(): Node {
            var left = parseUnary()
            while (true) {
                left = when {
                    matchOperator("*") -> BinaryNode(left, "*", parseUnary())
                    matchOperator("/") -> BinaryNode(left, "/", parseUnary())
                    else -> return left
                }
            }
        }

        private fun parseUnary(): Node {
            return when {
                matchOperator("!") -> UnaryNode("!", parseUnary())
                matchOperator("-") -> UnaryNode("-", parseUnary())
                else -> parsePrimary()
            }
        }

        private fun parsePrimary(): Node {
            val token = advance()
            return when (token.type) {
                TokenType.NUMBER -> LiteralNode(token.text.toDouble())
                TokenType.BOOLEAN -> LiteralNode(token.text == "true")
                TokenType.IDENTIFIER -> IdentifierNode(token.text)
                TokenType.LPAREN -> {
                    val node = parseExpression()
                    consume(TokenType.RPAREN, ")")
                    node
                }
                else -> throw IllegalArgumentException("Unexpected token '${token.text}'")
            }
        }

        private fun consume(type: TokenType, expected: String) {
            if (peek().type != type) {
                throw IllegalArgumentException("Expected '$expected' but found '${peek().text}'")
            }
            advance()
        }

        private fun matchOperator(op: String): Boolean {
            val token = peek()
            if (token.type == TokenType.OPERATOR && token.text == op) {
                advance()
                return true
            }
            return false
        }

        private fun peek(): Token = tokens[index]

        private fun advance(): Token {
            val token = tokens[index]
            index += 1
            return token
        }

        private fun tokenize(expression: String): List<Token> {
            val tokens = mutableListOf<Token>()
            var i = 0

            while (i < expression.length) {
                val c = expression[i]

                if (c.isWhitespace()) {
                    i += 1
                    continue
                }

                if (c.isDigit()) {
                    var j = i + 1
                    while (j < expression.length && (expression[j].isDigit() || expression[j] == '.')) {
                        j += 1
                    }
                    tokens.add(Token(TokenType.NUMBER, expression.substring(i, j)))
                    i = j
                    continue
                }

                if (c.isLetter() || c == '_') {
                    var j = i + 1
                    while (j < expression.length && (expression[j].isLetterOrDigit() || expression[j] == '_')) {
                        j += 1
                    }
                    val text = expression.substring(i, j)
                    if (text == "true" || text == "false") {
                        tokens.add(Token(TokenType.BOOLEAN, text))
                    } else {
                        tokens.add(Token(TokenType.IDENTIFIER, text))
                    }
                    i = j
                    continue
                }

                val twoChars = if (i + 1 < expression.length) expression.substring(i, i + 2) else ""
                if (twoChars in setOf("&&", "||", "==", "!=", ">=", "<=")) {
                    tokens.add(Token(TokenType.OPERATOR, twoChars))
                    i += 2
                    continue
                }

                when (c) {
                    '(', ')' -> {
                        tokens.add(Token(if (c == '(') TokenType.LPAREN else TokenType.RPAREN, c.toString()))
                        i += 1
                    }
                    '+', '-', '*', '/', '>', '<', '!' -> {
                        tokens.add(Token(TokenType.OPERATOR, c.toString()))
                        i += 1
                    }
                    else -> throw IllegalArgumentException("Unsupported character '$c' in expression '$expression'")
                }
            }

            tokens.add(Token(TokenType.EOF, "<eof>"))
            return tokens
        }
    }
}
