package com.gacha;

import com.gacha.GachaEngineTest;
import com.gacha.resource.ResourceManagerTest;
import com.gacha.simulator.SimulatorTest;
import com.gacha.strategy.StrategyTest;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

/**
 * 测试运行器 - 用于逐个验证测试
 */
public class RunTests {

    public static void main(String[] args) {
        System.out.println("==============================================");
        System.out.println("       抽卡模拟器测试运行器                   ");
        System.out.println("==============================================\n");

        // 运行所有测试
        runAllTests();

        // 或者单独运行某类测试，取消下面的注释即可：
        // runTestClass("GachaEngineTest", GachaEngineTest.class);
        // runTestClass("StrategyTest", StrategyTest.class);
        // runTestClass("SimulatorTest", SimulatorTest.class);
        // runTestClass("ResourceManagerTest", ResourceManagerTest.class);
    }

    private static void runAllTests() {
        System.out.println("运行所有测试...\n");

        Result result = JUnitCore.runClasses(
                GachaEngineTest.class,
                StrategyTest.class,
                SimulatorTest.class,
                ResourceManagerTest.class
        );

        printResult(result);
    }

    private static void runTestClass(String name, Class<?> testClass) {
        System.out.println("运行 " + name + "...\n");

        Result result = JUnitCore.runClasses(testClass);

        printResult(result);
    }

    private static void printResult(Result result) {
        System.out.println("========================================");
        System.out.println("测试运行完成");
        System.out.println("========================================");
        System.out.println("总测试数: " + result.getRunCount());
        System.out.println("通过数: " + (result.getRunCount() - result.getFailureCount()));
        System.out.println("失败数: " + result.getFailureCount());
        System.out.println("忽略数: " + result.getIgnoreCount());
        System.out.println("运行时间: " + result.getRunTime() + "ms");

        if (result.getFailureCount() > 0) {
            System.out.println("\n失败的测试:");
            for (Failure failure : result.getFailures()) {
                System.out.println("  - " + failure.getTestHeader());
                System.out.println("    原因: " + failure.getMessage());
                System.out.println();
            }
        }

        System.out.println("\n结果: " + (result.wasSuccessful() ? "全部通过 ✓" : "存在失败 ✗"));
    }
}
