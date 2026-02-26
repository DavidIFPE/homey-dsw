package br.edu.ifpe.recife.homey.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "ENDERECO")
public class Endereco {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID")
	private Long id;

	@Column(name = "LOGRADOURO", length = 120, nullable = true)
	private String logradouro; // Rua, avenida, etc.

	@Column(name = "NUMERO", length = 10, nullable = true)
	private String numero;

	@Column(name = "COMPLEMENTO", length = 60, nullable = true)
	private String complemento;

	@Column(name = "BAIRRO", length = 80, nullable = true)
	private String bairro;

	@Column(name = "CIDADE", length = 80, nullable = true)
	private String cidade;

	@Column(name = "ESTADO", length = 2, nullable = true)
	private String estado; // UF

	@Column(name = "CEP", length = 8, nullable = true)
	private String cep;

	// Coordenadas geográficas obrigatórias para uso com OpenStreetMap / Leaflet
	@Column(name = "LATITUDE", nullable = false)
	private Double latitude;

	@Column(name = "LONGITUDE", nullable = false)
	private Double longitude;

	public Endereco() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getLogradouro() {
		return logradouro;
	}

	public void setLogradouro(String logradouro) {
		this.logradouro = logradouro;
	}

	public String getNumero() {
		return numero;
	}

	public void setNumero(String numero) {
		this.numero = numero;
	}

	public String getComplemento() {
		return complemento;
	}

	public void setComplemento(String complemento) {
		this.complemento = complemento;
	}

	public String getBairro() {
		return bairro;
	}

	public void setBairro(String bairro) {
		this.bairro = bairro;
	}

	public String getCidade() {
		return cidade;
	}

	public void setCidade(String cidade) {
		this.cidade = cidade;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	public String getCep() {
		return cep;
	}

	public void setCep(String cep) {
		this.cep = cep;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public boolean hasCoordenadas() {
		return latitude != null && longitude != null;
	}
}
