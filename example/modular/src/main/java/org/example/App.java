package org.example;
import com.google.common.math.BigIntegerMath;

public class App {
    public String getGreeting() {
        return "Hello World!";
    }

    public static void main(String[] args) {
        System.out.println(BigIntegerMath.factorial(5));
        System.out.println(new App().getGreeting());
    }
}
