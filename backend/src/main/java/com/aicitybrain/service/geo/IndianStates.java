package com.aicitybrain.service.geo;

import com.aicitybrain.dto.GeoDtos;

import java.util.List;

/**
 * India's 28 states + 8 union territories. This is stable administrative data (not
 * something that needs a live API), so it's a plain constant list rather than a
 * database table or external call.
 */
public final class IndianStates {
    private IndianStates() {}

    public static final List<GeoDtos.IndianState> ALL = List.of(
        new GeoDtos.IndianState("Andhra Pradesh", "STATE"),
        new GeoDtos.IndianState("Arunachal Pradesh", "STATE"),
        new GeoDtos.IndianState("Assam", "STATE"),
        new GeoDtos.IndianState("Bihar", "STATE"),
        new GeoDtos.IndianState("Chhattisgarh", "STATE"),
        new GeoDtos.IndianState("Goa", "STATE"),
        new GeoDtos.IndianState("Gujarat", "STATE"),
        new GeoDtos.IndianState("Haryana", "STATE"),
        new GeoDtos.IndianState("Himachal Pradesh", "STATE"),
        new GeoDtos.IndianState("Jharkhand", "STATE"),
        new GeoDtos.IndianState("Karnataka", "STATE"),
        new GeoDtos.IndianState("Kerala", "STATE"),
        new GeoDtos.IndianState("Madhya Pradesh", "STATE"),
        new GeoDtos.IndianState("Maharashtra", "STATE"),
        new GeoDtos.IndianState("Manipur", "STATE"),
        new GeoDtos.IndianState("Meghalaya", "STATE"),
        new GeoDtos.IndianState("Mizoram", "STATE"),
        new GeoDtos.IndianState("Nagaland", "STATE"),
        new GeoDtos.IndianState("Odisha", "STATE"),
        new GeoDtos.IndianState("Punjab", "STATE"),
        new GeoDtos.IndianState("Rajasthan", "STATE"),
        new GeoDtos.IndianState("Sikkim", "STATE"),
        new GeoDtos.IndianState("Tamil Nadu", "STATE"),
        new GeoDtos.IndianState("Telangana", "STATE"),
        new GeoDtos.IndianState("Tripura", "STATE"),
        new GeoDtos.IndianState("Uttar Pradesh", "STATE"),
        new GeoDtos.IndianState("Uttarakhand", "STATE"),
        new GeoDtos.IndianState("West Bengal", "STATE"),
        new GeoDtos.IndianState("Andaman and Nicobar Islands", "UNION_TERRITORY"),
        new GeoDtos.IndianState("Chandigarh", "UNION_TERRITORY"),
        new GeoDtos.IndianState("Dadra and Nagar Haveli and Daman and Diu", "UNION_TERRITORY"),
        new GeoDtos.IndianState("Delhi", "UNION_TERRITORY"),
        new GeoDtos.IndianState("Jammu and Kashmir", "UNION_TERRITORY"),
        new GeoDtos.IndianState("Ladakh", "UNION_TERRITORY"),
        new GeoDtos.IndianState("Lakshadweep", "UNION_TERRITORY"),
        new GeoDtos.IndianState("Puducherry", "UNION_TERRITORY")
    );
}
