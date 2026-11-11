package com.touragency;

import com.touragency.sorting.VoucherOrder;
import java.nio.file.Path;

/**
 * Command line options of the application.
 *
 * @param xml    the document with vouchers
 * @param xsd    the schema of the document
 * @param xsl    the stylesheet that groups vouchers by type
 * @param output the file for the transformed document
 * @param order  how to sort the vouchers before printing
 */
public record AppOptions(Path xml, Path xsd, Path xsl, Path output, VoucherOrder order) {

    public static final String HELP = "Options: --xml=<file> --xsd=<file> --xsl=<file> --out=<file> "
            + "--sort=<country|type|duration|stars|cost>";

    private static final Path RESOURCES = Path.of("src", "main", "resources");
    private static final String PREFIX = "--";
    private static final char SEPARATOR = '=';

    /** @return the options used when the application is started without arguments */
    public static AppOptions defaults() {
        return new AppOptions(
                RESOURCES.resolve("tourist-voucher.xml"),
                RESOURCES.resolve("tourist-voucher.xsd"),
                RESOURCES.resolve("tourist-voucher.xsl"),
                Path.of("output", "vouchers-by-type.xml"),
                VoucherOrder.COUNTRY);
    }

    /**
     * Reads options written as {@code --name=value}; options that are not given keep their defaults.
     *
     * @throws IllegalArgumentException if an argument is not a known option
     */
    public static AppOptions parse(String... args) {
        AppOptions options = defaults();
        for (String arg : args) {
            int separator = arg.indexOf(SEPARATOR);
            if (!arg.startsWith(PREFIX) || separator < 0 || separator == arg.length() - 1) {
                throw new IllegalArgumentException("Expected --name=value, but got '" + arg + "'");
            }
            String name = arg.substring(PREFIX.length(), separator);
            String value = arg.substring(separator + 1);
            options = options.with(name, value);
        }
        return options;
    }

    private AppOptions with(String name, String value) {
        return switch (name) {
            case "xml" -> new AppOptions(Path.of(value), xsd, xsl, output, order);
            case "xsd" -> new AppOptions(xml, Path.of(value), xsl, output, order);
            case "xsl" -> new AppOptions(xml, xsd, Path.of(value), output, order);
            case "out" -> new AppOptions(xml, xsd, xsl, Path.of(value), order);
            case "sort" -> new AppOptions(xml, xsd, xsl, output, VoucherOrder.parse(value));
            default -> throw new IllegalArgumentException("Unknown option '--" + name + "'");
        };
    }
}
