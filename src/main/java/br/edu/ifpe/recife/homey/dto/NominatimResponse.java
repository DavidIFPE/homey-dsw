package br.edu.ifpe.recife.homey.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NominatimResponse(
    String lat,
    String lon,
    @JsonProperty("display_name") String displayName
) {}