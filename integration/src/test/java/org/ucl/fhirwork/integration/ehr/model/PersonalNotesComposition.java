package org.ucl.fhirwork.integration.ehr.model;

import com.google.gson.annotations.SerializedName;

public class PersonalNotesComposition extends FlatComposition
{
    @SerializedName("ctx/language") private String language;
    @SerializedName("ctx/territory") private String territory;
    @SerializedName("ctx/composer_name") private String composer;
    @SerializedName("ctx/time") private String time;
    @SerializedName("ctx/id_namespace") private String idNamespace;
    @SerializedName("ctx/id_scheme") private String idScheme;
    @SerializedName("ctx/health_care_facility|name") private String facilityName;
    @SerializedName("ctx/health_care_facility|id") private String facilityId;
    @SerializedName("personal_notes/clinical_synopsis:0/_name|value") private String title;
    @SerializedName("personal_notes/clinical_synopsis:0/notes") private String description;

    public PersonalNotesComposition(String title, String description)
    {
        super("RIPPLE - Personal Notes.v1");
        this.title = title;
        this.description = description;
    }
}
