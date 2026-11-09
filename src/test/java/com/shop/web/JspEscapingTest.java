package com.shop.web;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertTrue;

/**
 * JSP 裸 EL 输出扫描：用户/管理员可控的字符串字段必须转义后再输出，否则构成反射型或存储型 XSS。
 */
public class JspEscapingTest {

    private static final Pattern EL = Pattern.compile("\\$\\{([^}]*)}");
    private static final Pattern BARE_PATH =
            Pattern.compile("[A-Za-z_][A-Za-z0-9_]*(\\.[A-Za-z_][A-Za-z0-9_]*)*(\\[\\d+])?");

    /** 数据库中以字符串存放、且可由请求方写入的字段 */
    private static final Set<String> TEXT_FIELDS = new HashSet<>(Arrays.asList(
            "keyword", "orderNo", "sort", "redirect", "nickname", "username", "phone", "email",
            "receiver", "province", "city", "district", "detail", "addressSnapshot", "remark",
            "name", "subtitle", "mainImage", "productImage", "productName", "icon", "categoryName"));

    @Test
    public void everyJspUsingFnDeclaresTheTaglib() throws IOException {
        Path root = jspRoot();
        List<String> offenses = new ArrayList<>();
        for (Path jsp : listJsps(root)) {
            String text = new String(Files.readAllBytes(jsp), StandardCharsets.UTF_8);
            // 片段被静态包含时能继承父页声明，但单独翻译（如 JspC 预编译）会失败
            if (text.contains("fn:") && !text.contains("prefix=\"fn\"")) {
                offenses.add(root.relativize(jsp).toString());
            }
        }
        assertTrue("以下 JSP 使用了 fn: 但未声明 functions 标签库:\n" + String.join("\n", offenses),
                offenses.isEmpty());
    }

    private static Path jspRoot() {
        Path root = Paths.get(System.getProperty("basedir", "."), "src", "main", "webapp", "WEB-INF", "jsp");
        assertTrue("JSP 目录不存在: " + root, Files.isDirectory(root));
        return root;
    }

    private static List<Path> listJsps(Path root) throws IOException {
        try (Stream<Path> walk = Files.walk(root)) {
            List<Path> jsps = walk.filter(p -> p.toString().endsWith(".jsp")).sorted().collect(Collectors.toList());
            assertTrue("未扫描到任何 JSP", !jsps.isEmpty());
            return jsps;
        }
    }

    @Test
    public void userControlledTextIsEscapedInEveryJsp() throws IOException {
        Path root = jspRoot();
        List<String> offenses = new ArrayList<>();
        for (Path jsp : listJsps(root)) {
            List<String> lines = Files.readAllLines(jsp, StandardCharsets.UTF_8);
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                // jsp:param 只是把值传给被包含页，由最终输出点转义；在此转义会双重转义
                if (line.contains("<jsp:param")) {
                    continue;
                }
                Matcher m = EL.matcher(line);
                while (m.find()) {
                    String expr = m.group(1).trim();
                    // 只检查"裸属性路径"输出；比较/三元表达式输出的是字面量或布尔值
                    if (!BARE_PATH.matcher(expr).matches()) {
                        continue;
                    }
                    String field = expr.contains(".") ? expr.substring(expr.lastIndexOf('.') + 1) : expr;
                    if (expr.startsWith("param.") || TEXT_FIELDS.contains(field)) {
                        offenses.add(root.relativize(jsp) + ":" + (i + 1) + "  ${" + expr + "}");
                    }
                }
            }
        }
        assertTrue("以下位置直接输出了可控文本，需改为 ${fn:escapeXml(...)} 或经服务端 JSON 序列化:\n"
                        + String.join("\n", offenses),
                offenses.isEmpty());
    }
}