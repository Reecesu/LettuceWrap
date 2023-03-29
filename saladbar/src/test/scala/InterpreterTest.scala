import org.scalatest.funsuite._

class InterpreterTest extends AnyFunSuite {

    def interpreterTest(interpreter: Interpreter, e: Expr, l: List[Expr]): Unit = {
        val lFound = interpreter.evaluate(e){r => r}
        try {
            assert(lFound == l)
        } catch {
            case _: Throwable => {
                (lFound zip l ) foreach { case (ef, ei) => println(s"found: $ef\nexpected: $ei\nequivalent? ${ef == ei}\n\n")}
                assert(lFound == l)
            }
        }
    }
    def lexicalInterpreterTest(e: Expr, l: List[Expr]): Unit = {
        val interpreter = new Interpreter(new EvalConditions(LexicalScope, NoConversions, EagerCondition))
        interpreterTest(interpreter, e, l)
    }

    def dynamicInterpreterTest(e: Expr, l: List[Expr]): Unit = {
        val interpreter = new Interpreter(new EvalConditions(DynamicScope, NoConversions, EagerCondition))
        interpreterTest(interpreter, e, l)
    }


    test("number") {
        val e = N(2)
        val l = List(e)
        lexicalInterpreterTest(e, l)
    }

    test("ident failure") {
        val e = Ident("x")
        val l = List(e, LettuceError(new InterpreterError("Unbound variable found: x")))
        lexicalInterpreterTest(e, l)
    }

    test("let") {
        // let y = 1 + 2 in 4 * y
        val e = Let("y", Binary(Plus, N(1), N(2)), Binary(Times, N(4), Ident("y")))
        val l = List(e,
            // let y = 3 in 4 * y
            Let("y", N(3), Binary(Times, N(4), Ident("y"))),
            // 4 * 3
            Binary(Times, N(4),  N(3)),
            // 12
            N(12),
            )
        lexicalInterpreterTest(e, l)
    }

    test("fundef") {
        // function(x) 1
        val e = FunDef("x", N(1))
        val l = List(e, Closure("x", N(1), EmptyEnv))
        lexicalInterpreterTest(e, l)
    }

    test("static test") {
        // let x = 1 in let f = function(y) x in let x = 2 in f(3)
        // value should be 1
        val e = Let("x", N(1), Let("f", FunDef("y", Ident("x")), Let("x", N(2), FunCall(Ident("f"), N(3)))))
        val l = List(e, 
            Let("f", FunDef("y", N(1)), Let("x", N(2), FunCall(Ident("f"), N(3)))),
            Let("f", Closure("y", N(1), EmptyEnv), Let("x", N(2), FunCall(Ident("f"), N(3)))),
            Let("x", N(2), FunCall(Closure("y", N(1), EmptyEnv), N(3))),
            FunCall(Closure("y", N(1), EmptyEnv), N(3)),
            N(1)
        )
        lexicalInterpreterTest(e, l)
    }

    test("dynamic test") {
        // let x = 1 in let f = function(y) x in let x = 2 in f(3)
        // value should be 2
        val e = Let("x", N(1), Let("f", FunDef("y", Ident("x")), Let("x", N(2), FunCall(Ident("f"), N(3)))))
        val l = List(e, 
            Let("f", Closure("y", Ident("x"), Extend("x", N(1), EmptyEnv)), Let("x", N(2), FunCall(Ident("f"), N(3)))),
            Let("x", N(2), FunCall(Closure("y", Ident("x"), Extend("x", N(1), EmptyEnv)), N(3))),
            FunCall(Closure("y", Ident("x"), Extend("x", N(2), Extend("x", N(1), EmptyEnv))), N(3)),
            N(2)
        )
        dynamicInterpreterTest(e, l)
    }

    // IDK this semantic yet
    // test("letrec") {
    //     // letrec f = function(x) 1 in 2
    //     val e = LetRec("f", "x", N(1), N(2))
    //     val l = List(e, 
    //         // let f = closure(x, 1, {}) in 2
    //         LetRec("f")
    //         // 2
    //         Closure("x", N(1), EmptyEnv))
    //     staticInterpreterTest(e, l)
    // }
}
