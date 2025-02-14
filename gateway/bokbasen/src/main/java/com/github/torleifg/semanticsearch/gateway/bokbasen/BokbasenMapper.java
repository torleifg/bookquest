package com.github.torleifg.semanticsearch.gateway.bokbasen;

import com.github.torleifg.semanticsearch.book.domain.Book;
import com.github.torleifg.semanticsearch.book.domain.Contributor;
import org.editeur.ns.onix._3_0.reference.Product;

interface BokbasenMapper {
    Book from(Product product);

    Book from(String id);

    enum ContributorRoleMapping {
        A01(Contributor.Role.AUT),
        A03(Contributor.Role.AUS),
        A06(Contributor.Role.CMP),
        A07(Contributor.Role.ART),
        A08(Contributor.Role.PHT),
        A09(Contributor.Role.CTB),
        A10(Contributor.Role.CCP),
        A12(Contributor.Role.ILL),
        A15(Contributor.Role.AUI),
        A19(Contributor.Role.AFT),
        A23(Contributor.Role.AUI),
        A32(Contributor.Role.CTB),
        A36(Contributor.Role.BJD),
        A38(Contributor.Role.ANT),
        A42(Contributor.Role.AUT),
        A43(Contributor.Role.IVR),
        A44(Contributor.Role.IVE),
        B01(Contributor.Role.EDT),
        B04(Contributor.Role.ABR),
        B05(Contributor.Role.ADP),
        B06(Contributor.Role.TRL),
        B25(Contributor.Role.ARR),
        D01(Contributor.Role.PRO),
        D02(Contributor.Role.DRT),
        E01(Contributor.Role.ACT),
        E03(Contributor.Role.NRT),
        E05(Contributor.Role.SNG),
        E06(Contributor.Role.MUS),
        E07(Contributor.Role.NRT),
        Z02(Contributor.Role.HNR),
        Z03(Contributor.Role.ENJ),
        Z99(Contributor.Role.OTH);

        private final Contributor.Role code;

        ContributorRoleMapping(Contributor.Role code) {
            this.code = code;
        }

        Contributor.Role getCode() {
            return code;
        }
    }
}
