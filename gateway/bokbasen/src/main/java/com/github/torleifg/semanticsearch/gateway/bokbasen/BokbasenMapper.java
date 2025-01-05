package com.github.torleifg.semanticsearch.gateway.bokbasen;

import com.github.torleifg.semanticsearch.book.service.MetadataDTO;
import org.editeur.ns.onix._3_0.reference.Product;

interface BokbasenMapper {
    MetadataDTO from(Product product);

    MetadataDTO from(String id);

    enum ContributorRoleMapping {
        A01(MetadataDTO.Contributor.Role.AUT),
        A07(MetadataDTO.Contributor.Role.ART),
        A08(MetadataDTO.Contributor.Role.PHT),
        A12(MetadataDTO.Contributor.Role.ILL),
        A19(MetadataDTO.Contributor.Role.AFT),
        A23(MetadataDTO.Contributor.Role.AUI),
        B01(MetadataDTO.Contributor.Role.EDT),
        B04(MetadataDTO.Contributor.Role.ABR),
        B05(MetadataDTO.Contributor.Role.ADP),
        B06(MetadataDTO.Contributor.Role.TRL),
        B25(MetadataDTO.Contributor.Role.ARR),
        E01(MetadataDTO.Contributor.Role.ACT),
        E03(MetadataDTO.Contributor.Role.NRT),
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
