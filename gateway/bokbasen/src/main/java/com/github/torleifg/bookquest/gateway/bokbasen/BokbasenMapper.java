package com.github.torleifg.bookquest.gateway.bokbasen;

import com.github.torleifg.bookquest.core.domain.Book;
import com.github.torleifg.bookquest.core.domain.Role;
import org.editeur.ns.onix._3_1.reference.Product;

interface BokbasenMapper {
    Book from(Product product);

    Book from(String id);

    enum ContributorRoleMapping {
        A01(Role.AUT),
        A03(Role.AUS),
        A06(Role.CMP),
        A07(Role.ART),
        A08(Role.PHT),
        A09(Role.CTB),
        A10(Role.CCP),
        A12(Role.ILL),
        A15(Role.AUI),
        A19(Role.AFT),
        A23(Role.AUI),
        A32(Role.CTB),
        A36(Role.BJD),
        A38(Role.ANT),
        A42(Role.AUT),
        A43(Role.IVR),
        A44(Role.IVE),
        B01(Role.EDT),
        B04(Role.ABR),
        B05(Role.ADP),
        B06(Role.TRL),
        B25(Role.ARR),
        D01(Role.PRO),
        D02(Role.DRT),
        E01(Role.ACT),
        E03(Role.NRT),
        E05(Role.SNG),
        E06(Role.MUS),
        E07(Role.NRT),
        Z02(Role.HNR),
        Z03(Role.ENJ),
        Z99(Role.OTH);

        private final Role code;

        ContributorRoleMapping(Role code) {
            this.code = code;
        }

        Role getCode() {
            return code;
        }
    }
}
