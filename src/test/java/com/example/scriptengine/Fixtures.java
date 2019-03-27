package com.example.scriptengine;

public class Fixtures {
    public static String script1 = "print('Hello ScriptEngine!!!!');";

    public static String scriptSleep3s =
            "function sleepFor( sleepDuration ){\n" +
                    "    var now = new Date().getTime();\n" +
                    "    while(new Date().getTime() < now + sleepDuration){ /* do nothing */ }\n" +
                    "};\n" +
                    "print('Start sleep 3 sec');\n" +
                    "sleepFor(3000);\n" +
                    "print('End sleep');\n";
}
