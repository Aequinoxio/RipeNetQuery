import org.junit.jupiter.api.Test;

import java.util.ArrayList;

class HttpStatusCodesTest {

    @Test
    void getCodeAsText() {
    }

    @Test
    void getCode() {
    }

    @Test
    void getDesc() {
    }

    @Test
    void intToHttpStatusCode() {
        System.gc();

        Runtime runtime = Runtime.getRuntime();
        long usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();

        // working code here
        HttpStatusCodes[] httpStatusCodes = HttpStatusCodes.values();
        ArrayList<Integer> httpCodes = new ArrayList<>();
        for (HttpStatusCodes i : httpStatusCodes) {
            httpCodes.add(i.getCode());
        }
        HttpStatusCodes decodedType;
        for (HttpStatusCodes type : httpStatusCodes) {
            System.out.println(type.getCode() + " - " + type.getCodeAsText() + " - " + type.getDesc());
            decodedType = HttpStatusCodes.intToHttpStatusCode(type.getCode());
            System.out.println(decodedType.getCode() + " - " + decodedType.getCodeAsText() + " - " + decodedType.getDesc());
        }

        for (int i = 0; i < 10000;i++) {
            for (int code : httpCodes) {
                decodedType = HttpStatusCodes.intToHttpStatusCode(code);
            }
        }

        long usedMemoryAfter = runtime.totalMemory() - runtime.freeMemory();
        System.out.println("Used Memory before" + usedMemoryBefore);
        System.out.println("Memory increased:" + (usedMemoryAfter-usedMemoryBefore));

    }
}