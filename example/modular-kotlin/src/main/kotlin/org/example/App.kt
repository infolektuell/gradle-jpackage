package org.example

class App {
    val greeting = "Hello World!"

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val app = App()
            println(com.google.common.math.BigIntegerMath.factorial(5))
            println(app.greeting)
        }
    }
}
