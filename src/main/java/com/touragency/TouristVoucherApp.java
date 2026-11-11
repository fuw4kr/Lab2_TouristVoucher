package com.touragency;

import com.touragency.model.Voucher;
import com.touragency.parser.ParserKind;
import com.touragency.parser.VoucherReader;
import com.touragency.transform.XsltTransformer;
import com.touragency.validation.XsdValidator;
import com.touragency.xml.VoucherXmlException;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point. Validates the voucher document, reads it with the DOM, SAX and
 * StAX parsers, prints the sorted vouchers and saves the document regrouped by
 * the type of the tour.
 */
public final class TouristVoucherApp {

    private static final Logger LOG = LoggerFactory.getLogger(TouristVoucherApp.class);

    private final AppOptions options;

    public TouristVoucherApp(AppOptions options) {
        this.options = options;
    }

    public static void main(String[] args) {
        boolean success = launch(args);
        LOG.info(success ? "Done" : "Finished with errors, see the messages above");
    }

    /** @return {@code true} if everything has been done, {@code false} if something went wrong */
    static boolean launch(String... args) {
        try {
            return new TouristVoucherApp(AppOptions.parse(args)).run();
        } catch (IllegalArgumentException e) {
            LOG.error("{}. {}", e.getMessage(), AppOptions.HELP);
        } catch (VoucherXmlException e) {
            LOG.error(e.getMessage());
            LOG.debug("Cause of the failure", e);
        }
        return false;
    }

    /**
     * @return {@code false} if the document does not match the schema and was not processed
     * @throws VoucherXmlException if one of the files cannot be read or processed
     */
    public boolean run() {
        LOG.info("Options: {}", options);
        if (!new XsdValidator(options.xsd()).isValid(options.xml())) {
            return false;
        }

        Map<ParserKind, List<Voucher>> results = readWithAllParsers();
        boolean same = new HashSet<>(results.values()).size() == 1;
        LOG.info("DOM, SAX and StAX parsers returned the same vouchers: {}", same);

        new XsltTransformer(options.xsl()).transform(options.xml(), options.output());
        return true;
    }

    /** Reads the document with every parser and logs the vouchers in the chosen order. */
    Map<ParserKind, List<Voucher>> readWithAllParsers() {
        Map<ParserKind, List<Voucher>> results = new EnumMap<>(ParserKind.class);
        for (ParserKind kind : ParserKind.values()) {
            List<Voucher> vouchers = options.order().sort(new VoucherReader(kind).read(options.xml()));
            LOG.info("{} parser, vouchers sorted by {}:", kind, options.order());
            vouchers.forEach(voucher -> LOG.info("  {}", voucher));
            results.put(kind, vouchers);
        }
        return results;
    }
}
