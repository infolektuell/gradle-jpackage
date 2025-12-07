package org.example;
import com.google.common.math.BigIntegerMath;
import java.awt.color.ICC_ProfileGray;
public class App {
    public String getGreeting() {
        return "Hello World!";
    }

    public static void main(String[] args) {
        try {
            ICC_ProfileGray.getInstance("hello.icc");
        } catch (Exception ignored) {
            System.out.println("file not found");
        }
        System.out.println(BigIntegerMath.factorial(5));
        System.out.println(new App().getGreeting());
    }
}
