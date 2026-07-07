package com.fleetops.vehicles.infrastructure.messaging.sqs.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IncidentRegisteredEvent {

    @JsonProperty("incident_id")
    @JsonAlias("id")
    private String incidentId;

    @JsonProperty("event_date")
    @JsonAlias("fecha_hora")
    private String eventDate;

    @JsonProperty("driver_id")
    @JsonAlias("id_conductor")
    private String driverId;

    /** Placa del vehículo (nombre del campo en Incidentes: vehicle_id). */
    @JsonProperty("vehicle_id")
    @JsonAlias("placa_vehiculo")
    private String vehiclePlate;

    @JsonProperty("incident_type")
    @JsonAlias("tipo_incidente")
    private String incidentType;

    @JsonProperty("severity")
    @JsonAlias("gravedad")
    private String severity;

    @JsonProperty("description")
    @JsonAlias("descripcion")
    private String description;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("updated_at")
    private String updatedAt;

    public String getIncidentId() {
        return incidentId;
    }

    public void setIncidentId(String incidentId) {
        this.incidentId = incidentId;
    }

    public String getEventDate() {
        return eventDate;
    }

    public void setEventDate(String eventDate) {
        this.eventDate = eventDate;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public String getVehiclePlate() {
        return vehiclePlate;
    }

    public void setVehiclePlate(String vehiclePlate) {
        this.vehiclePlate = vehiclePlate;
    }

    public String getIncidentType() {
        return incidentType;
    }

    public void setIncidentType(String incidentType) {
        this.incidentType = incidentType;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
