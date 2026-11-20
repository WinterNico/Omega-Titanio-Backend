package com.example.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// En la API y en el frontend esto se llama "orden". La tabla se mantiene como "boleta" según la Figura 1.
@Entity
@Table(name = "boleta")
public class Boleta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String nroOrden;

    @Column(nullable = false, length = 20)
    private String codigoOrden;

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoBoleta estado = EstadoBoleta.EXITOSO;

    @Column(nullable = false)
    private Integer total = 0;

    // Opcional (Figura 1): una compra puede hacerse sin usuario logueado
    @ManyToOne(optional = true)
    @JoinColumn(name = "usuario_id", nullable = true)
    private Usuario usuario;

    @Embedded
    private DatosEnvio cliente = new DatosEnvio();

    @OneToMany(mappedBy = "boleta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleBoleta> detalles = new ArrayList<>();

    public Boleta() {}

    public void agregarDetalle(DetalleBoleta detalle) {
        detalles.add(detalle);
        detalle.setBoleta(this);
    }

    public Long getId() { return id; }
    public String getNroOrden() { return nroOrden; }
    public void setNroOrden(String nroOrden) { this.nroOrden = nroOrden; }
    public String getCodigoOrden() { return codigoOrden; }
    public void setCodigoOrden(String codigoOrden) { this.codigoOrden = codigoOrden; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public EstadoBoleta getEstado() { return estado; }
    public void setEstado(EstadoBoleta estado) { this.estado = estado; }
    public Integer getTotal() { return total; }
    public void setTotal(Integer total) { this.total = total; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public DatosEnvio getCliente() { return cliente; }
    public void setCliente(DatosEnvio cliente) { this.cliente = cliente; }
    public List<DetalleBoleta> getDetalles() { return detalles; }
}