package br.edu.ifpe.recife.homey.dto;

public record GeocodingResponseDTO(
    String enderecoPesquisado,
    String displayName,
    String cep,
    String logradouro,
    String bairro,
    String cidade,
    String estado,
    Double latitude,
    Double longitude
) {
    public static GeocodingResponseDTO fromViaCepAndNominatim(String enderecoPesquisado, ViaCepResponse viaCep, NominatimResponse nominatim) {
        return new GeocodingResponseDTO(
            enderecoPesquisado,
            nominatim != null ? nominatim.displayName() : null,
            viaCep.cep(),
            viaCep.logradouro(),
            viaCep.bairro(),
            viaCep.localidade(),
            viaCep.uf(),
            nominatim != null ? parseDoubleSafe(nominatim.lat()) : null,
            nominatim != null ? parseDoubleSafe(nominatim.lon()) : null
        );
    }

    public static GeocodingResponseDTO fromNominatim(String enderecoPesquisado, NominatimResponse nominatim) {
        return new GeocodingResponseDTO(
            enderecoPesquisado,
            nominatim != null ? nominatim.displayName() : null,
            null,
            null,
            null,
            null,
            null,
            nominatim != null ? parseDoubleSafe(nominatim.lat()) : null,
            nominatim != null ? parseDoubleSafe(nominatim.lon()) : null
        );
    }

    private static Double parseDoubleSafe(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
