package com.github.torleifg.semanticsearch.gateway.bokbasen;

import com.github.torleifg.semanticsearch.book.service.MetadataDTO;
import org.editeur.ns.onix._3_0.reference.Product;

interface BokbasenMapper {
    MetadataDTO from(Product product);

    MetadataDTO from(String id);

    enum ContributorRoleMapping {
        A01(MetadataDTO.Contributor.Role.AUT),
        A03(MetadataDTO.Contributor.Role.AUS),
        A06(MetadataDTO.Contributor.Role.CMP),
        A07(MetadataDTO.Contributor.Role.ART),
        A08(MetadataDTO.Contributor.Role.PHT),
        A09(MetadataDTO.Contributor.Role.CTB),
        A10(MetadataDTO.Contributor.Role.CCP),
        A12(MetadataDTO.Contributor.Role.ILL),
        A15(MetadataDTO.Contributor.Role.AUI),
        A19(MetadataDTO.Contributor.Role.AFT),
        A23(MetadataDTO.Contributor.Role.AUI),
        A32(MetadataDTO.Contributor.Role.CTB),
        A36(MetadataDTO.Contributor.Role.BJD),
        A38(MetadataDTO.Contributor.Role.ANT),
        A42(MetadataDTO.Contributor.Role.AUT),
        A43(MetadataDTO.Contributor.Role.IVR),
        A44(MetadataDTO.Contributor.Role.IVE),
        B01(MetadataDTO.Contributor.Role.EDT),
        B04(MetadataDTO.Contributor.Role.ABR),
        B05(MetadataDTO.Contributor.Role.ADP),
        B06(MetadataDTO.Contributor.Role.TRL),
        B25(MetadataDTO.Contributor.Role.ARR),
        D01(MetadataDTO.Contributor.Role.PRO),
        D02(MetadataDTO.Contributor.Role.DRT),
        E01(MetadataDTO.Contributor.Role.ACT),
        E03(MetadataDTO.Contributor.Role.NRT),
        E05(MetadataDTO.Contributor.Role.SNG),
        E06(MetadataDTO.Contributor.Role.MUS),
        E07(MetadataDTO.Contributor.Role.NRT),
        Z02(MetadataDTO.Contributor.Role.HNR),
        Z03(MetadataDTO.Contributor.Role.ENJ),
        Z99(MetadataDTO.Contributor.Role.OTH);

        private final MetadataDTO.Contributor.Role code;

        ContributorRoleMapping(MetadataDTO.Contributor.Role code) {
            this.code = code;
        }

        MetadataDTO.Contributor.Role getCode() {
            return code;
        }
    }
}
